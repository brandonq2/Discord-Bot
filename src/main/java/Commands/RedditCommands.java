package Commands;

import ga.dryco.redditjerk.api.Reddit;
import ga.dryco.redditjerk.exceptions.OAuthClientException;
import ga.dryco.redditjerk.exceptions.RedditJerkException;
import ga.dryco.redditjerk.implementation.RedditApi;
import ga.dryco.redditjerk.wrappers.*;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class RedditCommands extends ListenerAdapter {

    private Reddit reddit = RedditApi.getRedditInstance("spikenrexrexnspike");

    public void randomPostTitle(GuildMessageReceivedEvent event){
        String[] args = event.getMessage().getContentRaw().split("\\s+");
        TextChannel channel = event.getChannel();
        String subreddit = args[1];
        try{
            Subreddit sr = reddit.getSubreddit(subreddit);
            RedditThread post = sr.getRandom();
            Link info = post.getSubmissionPost().getData();
            String title = info.getTitle();
        }
        catch (IllegalStateException e){
            EmbedBuilder error = new EmbedBuilder();
            error.setTitle(":no_entry: Could not find subreddit");
            channel.sendMessage(error.build()).queue();
            return;
        }
        catch (IndexOutOfBoundsException e){
            EmbedBuilder error = new EmbedBuilder();
            error.setTitle(":no_entry: Subreddit has too few posts");
            channel.sendMessage(error.build()).queue();
            return;
        }
        catch (OAuthClientException e){
            EmbedBuilder error = new EmbedBuilder();
            error.setTitle(":no_entry: Something went wrong. Istg it wasn't me.");
            channel.sendMessage(error.build()).queue();
            return;
        }
        Subreddit sr = reddit.getSubreddit(subreddit);
        RedditThread post = sr.getRandom();
        Link info = post.getSubmissionPost().getData();
        String title = info.getTitle();
        try {
            if (info.getMedia() == null) {

                String image = info.getUrl();
                if (info.getUrl().contains(".gifv")) {
                    randomPostTitle(event);
                    return;
                } else if (image.contains("gfycat.com") && !image.contains(".gif")) {
                    image += ".gif";
                }
                EmbedBuilder embed = new EmbedBuilder();
                embed.addField("Post:", info.getUrl(), false);
                embed.setTitle("Random Post: " + subreddit);
                embed.addField("Title:", title, false);
                embed.setImage(image);
                embed.setFooter("Requested by: " + event.getMember().getUser().getName(), event.getAuthor().getAvatarUrl());
                channel.sendMessage(embed.build()).queue();
            } else if (!info.getMedia().toString().contains(".gif")) {
                randomPostTitle(event);
                return;
            } else {


                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle(title);
                String temp = info.getMedia().toString();
                int startIndex = temp.indexOf("thumbnail_url=") + 14;
                int endIndex = temp.indexOf(".gif,") + 4;
                String substring = temp.substring(startIndex, endIndex);
                embed.addField("Post:", info.getUrl(), false);
                embed.addField("Title:", title, false);
                embed.setImage(substring);
                embed.setFooter("Requested by: " + event.getMember().getUser().getName(), event.getAuthor().getAvatarUrl());
                channel.sendMessage(embed.build()).queue();
            }
        }
        catch (RedditJerkException e){
            EmbedBuilder error = new EmbedBuilder();
            error.setTitle(":no_entry: Couldn't find a post with a picture.");
            channel.sendMessage(error.build()).queue();
            return;
        }
    }

}