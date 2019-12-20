package Commands;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.HierarchyException;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

public class TextCommands extends ListenerAdapter{

    public void arrive(GuildMessageReceivedEvent event){
        EmbedBuilder arrival = new EmbedBuilder();
        arrival.setColor(0x6F3C89);
        arrival.setTitle("Dread it. Run from it. Destiny arrives all the same. And now, it's here. Or should I say, I am.");
        arrival.setImage("https://i.redd.it/4t0doipqxy811.gif");
        arrival.setFooter("Created by Thanos", "https://i.imgur.com/SMsNJN6.jpg");
        event.getChannel().sendMessage(arrival.build()).queue();
    }

    public void ban(GuildMessageReceivedEvent event){
        String[] args = event.getMessage().getContentRaw().split("\\s+");

        if (args.length <= 2 || event.getMessage().getMentionedMembers().size() == 0){
            EmbedBuilder error = new EmbedBuilder();
            error.setColor(0x6F3C89);
            error.setTitle(":no_entry: Please specify who to ban with @USER, how far back to delete his messages");
            event.getChannel().sendMessage(error.build()).queue();
            return;
        }
        else {
            Member banner = event.getMember();
            Member banned = event.getMessage().getMentionedMembers().get(0);
            TextChannel channel = event.getChannel();

            if (!banner.canInteract(banned)){
                EmbedBuilder error = new EmbedBuilder();
                error.setColor(0x6F3C89);
                error.setTitle(":no_entry: You don't have sufficient permissions to ban this person.");
                channel.sendMessage(error.build()).queue();
                return;
            }
            event.getGuild().getController().ban(banned,Integer.parseInt(args[2])).reason("Because.").queue();
            EmbedBuilder success = new EmbedBuilder();
            success.setColor(0x6F3C89);
            success.setTitle(":smiling_imp: Fun isn’t something one considers when balancing the universe. But this… does put a smile on my face.");
            success.setDescription("User: " + banned.getUser().getName() + " has been banned.");
            success.setImage("https://cdn.vox-cdn.com/uploads/chorus_asset/file/10439411/Infinity_War__1.gif");
            channel.sendMessage(success.build()).queue();
        }
    }

    public void kick(GuildMessageReceivedEvent event){
        String[] args = event.getMessage().getContentRaw().split("\\s+");

        if (args.length <= 1 || event.getMessage().getMentionedMembers().size() == 0){
            EmbedBuilder error = new EmbedBuilder();
            error.setColor(0x6F3C89);
            error.setTitle(":no_entry: Please specify who to kick with @USER.");
            event.getChannel().sendMessage(error.build()).queue();
            return;
        }
        else {
            Member kicker = event.getMember();
            Member kicked = event.getMessage().getMentionedMembers().get(0);
            TextChannel channel = event.getChannel();

            if (!kicker.canInteract(kicked)){
                EmbedBuilder error = new EmbedBuilder();
                error.setColor(0x6F3C89);
                error.setTitle(":no_entry: You don't have sufficient permissions to kick this person.");
                channel.sendMessage(error.build()).queue();
                return;
            }

            try {
                event.getGuild().getController().kick(kicked).reason("Because.").queue();
                EmbedBuilder success = new EmbedBuilder();
                success.setColor(0x6F3C89);
                success.setTitle(":smiling_imp: Fun isn’t something one considers when balancing the universe. But this… does put a smile on my face.");
                success.setImage("https://cdn.vox-cdn.com/uploads/chorus_asset/file/10439411/Infinity_War__1.gif");
                success.setDescription("User: " + kicked.getUser().getName() + " has been kicked.");
                channel.sendMessage(success.build()).queue();
            }
            catch (HierarchyException e){
                EmbedBuilder error = new EmbedBuilder();
                error.setColor(0x6F3C89);
                error.setTitle(":no_entry: You can't kick someone with equal permissions.");
                channel.sendMessage(error.build()).queue();
                return;
            }
        }
    }

    public void snapMessages(GuildMessageReceivedEvent event){
        String[] args = event.getMessage().getContentRaw().split("\\s+");
        EmbedBuilder error = new EmbedBuilder();

        if (args.length < 2){
            error.setColor(0x6F3C89);
            error.setTitle("Missing Arguments");
            error.setDescription("Please enter in a number");
            event.getChannel().sendMessage(error.build()).queue();
        }
        else if(args.length > 2){
            error.setColor(0x6F3C89);
            error.setTitle("Too Many Arguments");
            error.setDescription("Please check that there's only one argument");
            event.getChannel().sendMessage(error.build()).queue();
        }
        else if(!args[1].matches("-?(0|[1-9]\\d*)")){
            error.setColor(0x6F3C89);
            error.setTitle("Invalid Argument");
            error.setDescription("Please enter a number as the argument");
            event.getChannel().sendMessage(error.build()).queue();
        }
        else {
            try{
                List<Message> messages = event.getChannel().getHistory().retrievePast(Integer.parseInt(args[1]) + 1).complete();
                event.getChannel().deleteMessages(messages).queue();

                EmbedBuilder success = new EmbedBuilder();
                success.setColor(0x6F3C89);
                success.setTitle("Fun isn't something one considers when balancing the universe. But this... does put a smile on my face.");
                success.setDescription("Messages deleted successfully");
                success.setImage("https://mtv.mtvnimages.com/uri/mgid:file:http:shared:mtv.com/news/wp-content/uploads/2019/03/Thanos-Snap-1552933559.gif?quality=.8&height=251&width=600");
                Member user = event.getMember();
                success.setFooter("Requested by: " + (user.getNickname() == null ? user.getEffectiveName() : user.getNickname()), event.getAuthor().getAvatarUrl());
                event.getChannel().sendMessage(success.build()).queue();
            }
            catch (IllegalArgumentException e){
                if (e.toString().startsWith("java.lang.IllegalArgumentException: Message retrieval")){
                    error.setColor(0x6F3C89);
                    error.setTitle("Try snapping a couple times");
                    error.setDescription("You can only delete 1-99 messages");
                    event.getChannel().sendMessage(error.build()).queue();
                }
                else{
                    error.setColor(0x6F3C89);
                    error.setTitle("The Time Stone isn't powerful enough");
                    error.setDescription("Can't delete messages older than 2 weeks");
                    event.getChannel().sendMessage(error.build()).queue();

                }
            }
        }
    }

    public void getAvatar(GuildMessageReceivedEvent event){
        String[] args = event.getMessage().getContentRaw().split("\\s+");

        if (args.length == 1){
            EmbedBuilder showAvatar = new EmbedBuilder();
            showAvatar.setColor(0x6F3C89);
            showAvatar.setTitle(event.getMessage().getAuthor().getName() + "'s Avatar:");
            Member user = event.getMember();
            showAvatar.setFooter("Requested by: " + (user.getNickname() == null ? user.getEffectiveName() : user.getNickname()), event.getAuthor().getAvatarUrl());
            event.getChannel().sendMessage(showAvatar.build()).queue();
        }
        else if (args.length == 2){
            try {
                Member member = event.getMessage().getMentionedMembers().get(0);
                String avatarURL = member.getUser().getAvatarUrl();

                EmbedBuilder showAvatar = new EmbedBuilder();
                showAvatar.setColor(0x6F3C89);
                showAvatar.setTitle(member.getEffectiveName() + "'s Avatar:");
                showAvatar.setImage(avatarURL);
                Member user = event.getMember();
                showAvatar.setFooter("Requested by: " + (user.getNickname() == null ? user.getEffectiveName() : user.getNickname()), event.getAuthor().getAvatarUrl());
                event.getChannel().sendMessage(showAvatar.build()).queue();
            }
            catch (IndexOutOfBoundsException e){
                EmbedBuilder error = new EmbedBuilder();
                error.setColor(0x6F3C89);
                error.setTitle("No mention found");
                error.setDescription("Please mention a user with @USER");
                event.getChannel().sendMessage(error.build()).queue();
            }
        }
        else{
            EmbedBuilder error = new EmbedBuilder();
            error.setColor(0x6F3C89);
            error.setTitle("Too many arguments");
            error.setDescription("Please ensure there's either 0 or 1 arguments");
            event.getChannel().sendMessage(error.build()).queue();
        }
    }

    public void getUserInfo(GuildMessageReceivedEvent event){
        String[] args = event.getMessage().getContentRaw().split("\\s+");

        if (args.length == 1){
            Member user = event.getMember();
            DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            EmbedBuilder userInfo = new EmbedBuilder();

            userInfo.setColor(0x6F3C89);
            userInfo.setTitle(user.getEffectiveName() + "'s Info");
            userInfo.setThumbnail(event.getMessage().getAuthor().getAvatarUrl());
            userInfo.setDescription("");
            userInfo.addField("Username: ", user.getUser().getName(), true);
            userInfo.addField("Nickname: ", user.getNickname() == null ? "N/A" : user.getNickname(), true);
            userInfo.addField("Joined: ", user.getJoinDate().format(format), true);
            userInfo.addField("Game: ", user.getGame() == null ? "N/A" : user.getGame().asRichPresence().getName().equalsIgnoreCase("Spotify") ? "N/A" : user.getGame().asRichPresence().getName(), true);
            userInfo.addField("Listening to: ", user.getGame() == null ? "N/A": !user.getGame().asRichPresence().getName().equalsIgnoreCase("spotify") ? "N/A" : user.getGame().asRichPresence().getDetails(), true);
            userInfo.addField("Artist: ", user.getGame() == null ? "N/A": !user.getGame().asRichPresence().getName().equalsIgnoreCase("spotify") ? "N/A" : user.getGame().asRichPresence().getState(), true);
            userInfo.setFooter("Requested by: " + (user.getNickname() == null ? user.getEffectiveName() : user.getNickname()), event.getAuthor().getAvatarUrl());
            event.getChannel().sendMessage(userInfo.build()).queue();
        }
        else if (args.length == 2){
            try {
                Member user = event.getMessage().getMentionedMembers().get(0);
                DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                EmbedBuilder userInfo = new EmbedBuilder();

                userInfo.setColor(0x6F3C89);
                userInfo.setTitle(user.getEffectiveName() + "'s Info");
                userInfo.setThumbnail(user.getUser().getAvatarUrl());
                userInfo.setDescription("");
                userInfo.addField("Username: ", user.getUser().getName(), true);
                userInfo.addField("Nickname: ", user.getNickname() == null ? "N/A" : user.getNickname(), true);
                userInfo.addField("Joined: ", user.getJoinDate().format(format), true);
                userInfo.addField("Game: ", user.getGame() == null ? "N/A" : user.getGame().isRich() ? "N/A"  : user.getGame().toString(), true);
                userInfo.addField("Listening to: ", user.getGame() == null ? "N/A": !user.getGame().isRich() ? "N/A" : user.getGame().asRichPresence().getDetails(), true);
                userInfo.addField("Artist: ", user.getGame() == null ? "N/A": !user.getGame().isRich() ? "N/A" : user.getGame().asRichPresence().getState(), true);
                userInfo.setFooter("Requested by: " + (user.getNickname() == null ? user.getEffectiveName() : user.getNickname()), event.getAuthor().getAvatarUrl());
                event.getChannel().sendMessage(userInfo.build()).queue();

            }
            catch (IndexOutOfBoundsException e){
                EmbedBuilder error = new EmbedBuilder();
                error.setColor(0x6F3C89);
                error.setTitle("No mention found");
                error.setDescription("Please mention a user with @USER");
                event.getChannel().sendMessage(error.build()).queue();
            }
        }
    }

    public void randomQuote(GuildMessageReceivedEvent event){
        try (Stream<String> lines = Files.lines(Paths.get("C:\\Users\\Brandon\\Desktop\\Discord-Bot\\src\\main\\java\\quotes.txt"))) {
            Stream<String> lines2 = Files.lines(Paths.get("C:\\Users\\Brandon\\Desktop\\Discord-Bot\\src\\main\\java\\quotes.txt"));
            Random rand = new Random();
            int n1 = rand.nextInt(18) + 1;
            int n2 = (2 * n1) - 2;
            String line1 = lines.skip(n2).findFirst().get();
            String line2 = lines2.skip(n2 + 1).findFirst().get();

            EmbedBuilder quote = new EmbedBuilder();
            quote.setColor(0x6F3C89);
            quote.setTitle("Random Quote");
            quote.setDescription(line1);
            quote.setFooter(line2,"https://t7.rbxcdn.com/51f6b12d75ae04e2864633677ec9fa24");
            event.getChannel().sendMessage(quote.build()).queue();
        }
        catch (IOException e){
            EmbedBuilder error = new EmbedBuilder();
            error.setColor(0x6F3C89);
            error.setTitle("Error");
            error.setDescription("Yea idk wtf happened");
            event.getChannel().sendMessage(error.build()).queue();
        }

    }

    public void payRespects(GuildMessageReceivedEvent event){
        String[] args = event.getMessage().getContentRaw().split("\\s+");

        if (args.length == 1){
            Member user = event.getMember();
            EmbedBuilder respect = new EmbedBuilder();
            respect.setColor(0x6F3C89);
            respect.setTitle("You have my respect, " + (user.getNickname() == null ? user.getEffectiveName() : user.getNickname()));
            respect.setImage("https://media.giphy.com/media/26gR1v0rIDrjSsca4/giphy.gif");
            respect.setFooter("Paid by: " + (user.getNickname() == null ? user.getEffectiveName() : user.getNickname()), event.getAuthor().getAvatarUrl());
            event.getChannel().sendMessage(respect.build()).queue();
        }
    }

    public void warn(GuildMessageReceivedEvent event){
        String[] args = event.getMessage().getContentRaw().split("\\s+");

        if (args.length == 2){
            Member user = event.getMember();
            EmbedBuilder respect = new EmbedBuilder();
            Member mentioned = event.getMessage().getMentionedMembers().get(0);
            respect.setColor(0x6F3C89);
            respect.setTitle("You're strong. But I could snap my fingers, and you'd all cease to exist. User: " + (mentioned.getNickname() == null ? mentioned.getEffectiveName() : mentioned.getNickname()) + " has been warned.");
            respect.setImage("https://media.giphy.com/media/aqMY57vLdkghi/giphy.gif");
            respect.setFooter("Warned by: " + (user.getNickname() == null ? user.getEffectiveName() : user.getNickname()), event.getAuthor().getAvatarUrl());
            event.getChannel().sendMessage(respect.build()).queue();
        }
    }

    public void musicHelp(GuildMessageReceivedEvent event){

        EmbedBuilder embed = new EmbedBuilder();
        TextChannel channel = event.getChannel();

        embed.setTitle(":orange_book: Music Commands");
        embed.setColor(0x6F3C89);
        embed.addField(">join or >j", "The bot will join the voice channel.", false);
        embed.addField(">leave or >l", "The bot will leave the voice channel, and clear the queue.", false);
        embed.addField(">play or >p", "Queues a song using a URL or will search on Youtube if no URL found.", false);
        embed.addField(">queue or >q", "Displays info about the current song, and what songs are in queue.", false);
        embed.addField(">resume", "Resumes a paused song.", false);
        embed.addField(">rewind", "Rewinds a specified amount of seconds.", false);
        embed.addField(">seek", "Skips a specified amount of seconds.", false);
        embed.addField(">setvolume or >setv", "Sets the volume from 1-100%.", false);
        embed.addField(">skip or >s", "Skips current song.", false);
        embed.addField(">song", "Displays info about the current song.", false);
        embed.addField(">skip or >s", "Skips current song.", false);
        embed.addField(">stop", "Stops music and clears queue.", false);
        embed.addField(">volume or >v", "Displays the current volume.", false);
        Member user = event.getMember();
        embed.setFooter("Requested by: " + (user.getNickname() == null ? user.getEffectiveName() : user.getNickname()), event.getAuthor().getAvatarUrl());
        channel.sendMessage(embed.build()).queue();
    }

    public void textHelp(GuildMessageReceivedEvent event){

        EmbedBuilder embed = new EmbedBuilder();
        TextChannel channel = event.getChannel();

        embed.setTitle(":orange_book: Text Commands");
        embed.setColor(0x6F3C89);
        embed.addField(">anime", "Retrieves information about the anime.", false);
        embed.addField(">arrive", "Thanos.", false);
        embed.addField(">avatar or >avatar @USER", "Retrieves either your avatar, or the mentioned users avatar.", false);
        embed.addField(">ban @USER #Days", "Bans a user and deletes their messages from the past amount of days. **Admin permissions needed**.", false);
        embed.addField(">f", "Press to pay respects.", false);
        embed.addField(">info or >info @USER", "Retrieves either your info, or the mentioned users info.", false);
        embed.addField(">kick @USER", "Kicks a user. **Admin permissions needed**.", false);
        embed.addField(">manga", "Retrieves information about the manga.", false);
        embed.addField(">quote", "Gives you a random quote. Prob some weeb shit.", false);
        embed.addField(">reddit or >r", "Gets a random post from the subreddit of your choosing.", false);
        embed.addField(">snap #", "Deletes the specified number of messages. **Admin permissions needed**.", false);
        embed.addField(">urban", "Retrieves the urban definition of the word.", false);
        Member user = event.getMember();
        embed.setFooter("Requested by: " + (user.getNickname() == null ? user.getEffectiveName() : user.getNickname()), event.getAuthor().getAvatarUrl());
        channel.sendMessage(embed.build()).queue();
    }
}
