package Commands;

import MainFiles.DiscordBot;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class TextCommands extends ListenerAdapter{
    public void onGuildMessageReceived(GuildMessageReceivedEvent event){
        String[] args = event.getMessage().getContentRaw().split("\\s+");

        if (args[0].equalsIgnoreCase(DiscordBot.prefix + "arrive")){
            arrive(event);
        }
        else if (args[0].equalsIgnoreCase(DiscordBot.prefix + "snap")){
            snapMessages(event);
        }
        else if (args[0].equalsIgnoreCase(DiscordBot.prefix + "avatar")){
            getAvatar(event);
        }
        else if (args[0].equalsIgnoreCase(DiscordBot.prefix + "info")){
            getUserInfo(event);
        }
    }

    public void arrive(GuildMessageReceivedEvent event){
        event.getChannel().sendTyping().queue();
        event.getChannel().sendMessage("Dread it. Run from it. Destiny arrives all the same. And now, it's here. Or should I say, I am.").queue();
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
            String avatarURL = event.getMessage().getAuthor().getAvatarUrl();

            EmbedBuilder showAvatar = new EmbedBuilder();
            showAvatar.setColor(0x6F3C89);
            showAvatar.setTitle(event.getMessage().getAuthor().getName() + "'s Avatar:");
            showAvatar.setImage(avatarURL);
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
            userInfo.setFooter("Created by: " + user.getUser().getName(), event.getAuthor().getAvatarUrl());
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
                userInfo.setFooter("Created by: " + user.getUser().getName(), event.getAuthor().getAvatarUrl());
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
}
