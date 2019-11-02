/*
 * Copyright 2015-2019 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.internal.requests.ratelimit;

import net.dv8tion.jda.api.events.ExceptionEvent;
import net.dv8tion.jda.api.requests.Request;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.requests.RateLimiter;
import net.dv8tion.jda.internal.requests.Requester;
import net.dv8tion.jda.internal.requests.Route;
import net.dv8tion.jda.internal.requests.Route.RateLimit;
import net.dv8tion.jda.internal.utils.IOUtil;
import net.dv8tion.jda.internal.utils.UnlockHook;
import okhttp3.Headers;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class BotRateLimiter extends RateLimiter
{
    private static final String RESET_HEADER = "X-RateLimit-Reset";
    private static final String LIMIT_HEADER = "X-RateLimit-Limit";
    private static final String REMAINING_HEADER = "X-RateLimit-Remaining";
    protected volatile Long timeOffset = null;

    public BotRateLimiter(Requester requester)
    {
        super(requester);
    }

    @Override
    public Long getRateLimit(Route.CompiledRoute route)
    {
        Bucket bucket = getBucket(route);
        synchronized (bucket)
        {
            return bucket.getRateLimit();
        }
    }

    @Override
    protected void queueRequest(Request request)
    {
        Bucket bucket = getBucket(request.getRoute());
        synchronized (bucket)
        {
            bucket.addToQueue(request);
        }
    }

    @Override
    protected Long handleResponse(Route.CompiledRoute route, okhttp3.Response response)
    {
        Bucket bucket = getBucket(route);
        synchronized (bucket)
        {
            Headers headers = response.headers();
            int code = response.code();
            if (timeOffset == null)
                setTimeOffset(headers);

            if (code == 429)
            {
                String global = headers.get("X-RateLimit-Global");
                String retry = headers.get("Retry-After");
                if (retry == null || retry.isEmpty())
                {
                    try (InputStream in = IOUtil.getBody(response))
                    {
                        DataObject limitObj = DataObject.fromJson(in);
                        retry = limitObj.get("retry_after").toString();
                    }
                    catch (IOException e)
                    {
                        throw new UncheckedIOException(e);
                    }
                }
                long retryAfter = Long.parseLong(retry);
                if (Boolean.parseBoolean(global))  //global ratelimit
                {
                    //If it is global, lock down the threads.
                    log.warn("Encountered global rate-limit! Retry-After: {}", retryAfter);
                    requester.getJDA().getSessionController().setGlobalRatelimit(getNow() + retryAfter);
                }
                else
                {
                    log.warn("Encountered 429 on route /{}", bucket.getRoute());
                    updateBucket(bucket, headers, retryAfter);
                }

                return retryAfter;
            }
            else
            {
                updateBucket(bucket, headers, -1);
                return null;
            }
        }

    }

    private Bucket getBucket(Route.CompiledRoute route)
    {
        String rateLimitRoute = route.getRatelimitRoute();
        Bucket bucket = (Bucket) buckets.get(rateLimitRoute);
        if (bucket == null)
        {
            synchronized (buckets)
            {
                bucket = (Bucket) buckets.get(rateLimitRoute);
                if (bucket == null)
                {
                    Route baseRoute = route.getBaseRoute();
                    bucket = new Bucket(rateLimitRoute, baseRoute.getRatelimit(), baseRoute.isMissingHeaders());
                    buckets.put(rateLimitRoute, bucket);
                }
            }
        }
        return bucket;
    }

    public long getNow()
    {
        return System.currentTimeMillis() + getTimeOffset();
    }

    public long getTimeOffset()
    {
        return timeOffset == null ? 0 : timeOffset;
    }

    private void setTimeOffset(Headers headers)
    {
        //Store as soon as possible to get the most accurate time difference;
        long time = System.currentTimeMillis();
        if (timeOffset == null)
        {
            //Get the date header provided by Discord.
            //Format:  "date" : "Fri, 16 Sep 2016 05:49:36 GMT"
            String date = headers.get("Date");
            if (date != null)
            {
                OffsetDateTime tDate = OffsetDateTime.parse(date, DateTimeFormatter.RFC_1123_DATE_TIME);
                long lDate = tDate.toInstant().toEpochMilli(); //We want to work in milliseconds, not seconds
                timeOffset = lDate - time; //Get offset in milliseconds.
            }
        }
    }

    private void updateBucket(Bucket bucket, Headers headers, long retryAfter)
    {
        int headerCount = 0;
        if (retryAfter > 0)
        {
            bucket.resetTime = getNow() + retryAfter;
            bucket.routeUsageRemaining = 0;
        }

        if (bucket.hasRatelimit()) // Check if there's a hardcoded rate limit
        {
            bucket.resetTime = getNow() + bucket.getRatelimit().getResetTime();
            headerCount += 2;
            //routeUsageLimit provided by the ratelimit object already in the bucket.
        }
        else
        {
            headerCount += parseLong(headers.get(RESET_HEADER), bucket, (time, b)  -> b.resetTime = time * 1000); //Seconds to milliseconds
            headerCount += parseInt(headers.get(LIMIT_HEADER),  bucket, (limit, b) -> b.routeUsageLimit = limit);
        }

        //Currently, we check the remaining amount even for hardcoded ratelimits just to further respect Discord
        // however, if there should ever be a case where Discord informs that the remaining is less than what
        // it actually is and we add a custom ratelimit to handle that, we will need to instead move this to the
        // above else statement and add a bucket.routeUsageRemaining-- decrement to the above if body.
        //An example of this statement needing to be moved would be if the custom ratelimit reset time interval is
        // equal to or greater than 1000ms, and the remaining count provided by discord is less than the ACTUAL
        // amount that their systems allow in such a way that isn't a bug.
        //The custom ratelimit system is primarily for ratelimits that can't be properly represented by Discord's
        // header system due to their headers only supporting accuracy to the second. The custom ratelimit system
        // allows for hardcoded ratelimits that allow accuracy to the millisecond which is important for some
        // ratelimits like Reactions which is 1/0.25s, but discord reports the ratelimit as 1/1s with headers.
        headerCount += parseInt(headers.get(REMAINING_HEADER), bucket, (remaining, b) -> b.routeUsageRemaining = remaining);
        if (!bucket.missingHeaders && headerCount < 3)
        {
            log.debug("Encountered issue with headers when updating a bucket\n" +
                      "Route: {}\nHeaders: {}", bucket.getRoute(), headers);
        }
    }

    private int parseInt(String input, Bucket bucket, IntObjectConsumer<? super Bucket> consumer)
    {
        try
        {
            int parsed = Integer.parseInt(input);
            consumer.accept(parsed, bucket);
            return 1;
        }
        catch (NumberFormatException ignored) {}
        return 0;
    }

    private int parseLong(String input, Bucket bucket, LongObjectConsumer<? super Bucket> consumer)
    {
        try
        {
            long parsed = Long.parseLong(input);
            consumer.accept(parsed, bucket);
            return 1;
        }
        catch (NumberFormatException ignored) {}
        return 0;
    }

    private class Bucket implements IBucket, Runnable
    {
        final String route;
        final boolean missingHeaders;
        final RateLimit rateLimit;
        final ConcurrentLinkedQueue<Request> requests = new ConcurrentLinkedQueue<>();
        final ReentrantLock requestLock = new ReentrantLock();
        volatile boolean processing = false;
        volatile long resetTime = 0;
        volatile int routeUsageRemaining = 1;    //These are default values to only allow 1 request until we have properly
        volatile int routeUsageLimit = 1;        // ratelimit information.

        public Bucket(String route, RateLimit rateLimit, boolean missingHeaders)
        {
            this.route = route;
            this.rateLimit = rateLimit;
            this.missingHeaders = missingHeaders;
            if (rateLimit != null)
            {
                this.routeUsageRemaining = rateLimit.getUsageLimit();
                this.routeUsageLimit = rateLimit.getUsageLimit();
            }
        }

        void addToQueue(Request request)
        {
            requests.add(request);
            submitForProcessing();
        }

        void submitForProcessing()
        {
            synchronized (submittedBuckets)
            {
                if (!submittedBuckets.contains(this))
                {
                    Long delay = getRateLimit();
                    if (delay == null)
                        delay = 0L;

                    if (delay > 0)
                    {
                        log.debug("Backing off {} milliseconds on route /{}", delay, getRoute());
                        requester.getJDA().getRateLimitPool().schedule(this, delay, TimeUnit.MILLISECONDS);
                    }
                    else
                    {
                        requester.getJDA().getRateLimitPool().execute(this);
                    }
                    submittedBuckets.add(this);
                }
            }
        }

        Long getRateLimit()
        {
            long gCooldown = requester.getJDA().getSessionController().getGlobalRatelimit();
            if (gCooldown > 0) //Are we on global cooldown?
            {
                long now = getNow();
                if (now > gCooldown)   //Verify that we should still be on cooldown.
                {
                    //If we are done cooling down, reset the globalCooldown and continue.
                    requester.getJDA().getSessionController().setGlobalRatelimit(Long.MIN_VALUE);
                }
                else
                {
                    //If we should still be on cooldown, return when we can go again.
                    return gCooldown - now;
                }
            }
            if (this.routeUsageRemaining <= 0)
            {
                if (getNow() > this.resetTime)
                {
                    this.routeUsageRemaining = this.routeUsageLimit;
                    this.resetTime = 0;
                }
            }
            if (this.routeUsageRemaining > 0)
                return null;
            else
                return this.resetTime - getNow();
        }

        @Override
        public boolean equals(Object o)
        {
            if (!(o instanceof Bucket))
                return false;

            Bucket oBucket = (Bucket) o;
            return route.equals(oBucket.route);
        }

        @Override
        public int hashCode()
        {
            return route.hashCode();
        }

        private void handleResponse(Iterator<Request> it, Long retryAfter)
        {
            if (retryAfter == null)
            {
                // We were not rate-limited! Then just continue with the rest of the requests
                it.remove();
                processIterator(it);
            }
            else
            {
                // Rate-limited D: Guess we have to backoff for now
                finishProcess();
            }
        }

        private void processIterator(Iterator<Request> it)
        {
            Request request = null;
            try
            {
                do
                {
                    if (!it.hasNext())
                    {
                        // End loop, no more requests left
                        finishProcess();
                        return;
                    }
                    request = it.next();
                } while (isSkipped(it, request));

                CompletableFuture<Long> handle = requester.execute(request);
                final Request request0 = request;
                // Hook the callback for the request
                handle.whenComplete((retryAfter, error) ->
                {
                    requester.setContext();
                    if (error != null)
                    {
                        // There was an error, handle it and continue with the next request
                        log.error("Requester system encountered internal error", error);
                        it.remove();
                        request0.onFailure(error);
                        processIterator(it);
                    }
                    else
                    {
                        // Handle the response and backoff if necessary
                        handleResponse(it, retryAfter);
                    }
                });
            }
            catch (Throwable t)
            {
                log.error("Requester system encountered an internal error", t);
                it.remove();
                if (request != null)
                    request.onFailure(t);
                // don't forget to end the loop and start over
                finishProcess();
            }
        }

        private void finishProcess()
        {
            // We are done with processing
            MiscUtil.locked(requestLock, () ->
            {
                processing = false;
            });
            // Re-submit if new requests were added or rate-limit was hit
            synchronized (submittedBuckets)
            {
                submittedBuckets.remove(this);
                if (!requests.isEmpty())
                {
                    try
                    {
                        this.submitForProcessing();
                    }
                    catch (RejectedExecutionException e)
                    {
                        log.debug("Caught RejectedExecutionException when re-queuing a ratelimited request. The requester is probably shutdown, thus, this can be ignored.");
                    }
                }
            }
        }

        @Override
        public void run()
        {
            requester.setContext();
            requestLock.lock();
            try (UnlockHook hook = new UnlockHook(requestLock))
            {
                // Ensure the processing is synchronized
                if (processing)
                    return;
                processing = true;
                // Start processing loop
                Iterator<Request> it = requests.iterator();
                processIterator(it);
            }
            catch (Throwable err)
            {
                log.error("Requester system encountered an internal error from beyond the synchronized execution blocks. NOT GOOD!", err);
                if (err instanceof Error)
                {
                    JDAImpl api = requester.getJDA();
                    api.handleEvent(new ExceptionEvent(api, err, true));
                }
            }
        }

        @Override
        public RateLimit getRatelimit()
        {
            return rateLimit;
        }

        @Override
        public String getRoute()
        {
            return route;
        }

        @Override
        public Queue<Request> getRequests()
        {
            return requests;
        }
    }

    private interface LongObjectConsumer<T>
    {
        void accept(long n, T t);
    }

    private interface IntObjectConsumer<T>
    {
        void accept(int n, T t);
    }
}
