package Commands;

import MainFiles.DiscordBot;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;


public class CommandHub extends ListenerAdapter {
    public void onGuildMessageReceived(GuildMessageReceivedEvent event){
        String[] args = event.getMessage().getContentRaw().split("\\s+");
        TextCommands text = new TextCommands();
        MusicCommands music = new MusicCommands();

        if (args[0].equalsIgnoreCase(DiscordBot.prefix + "arrive")){
            text.arrive(event);
        }
        else if (args[0].equalsIgnoreCase(DiscordBot.prefix + "snap") && event.getMember().hasPermission(Permission.ADMINISTRATOR)){
            text.snapMessages(event);
        }
        else if (args[0].equalsIgnoreCase(DiscordBot.prefix + "ban") && event.getMember().hasPermission(Permission.ADMINISTRATOR)){
            text.ban(event);
        }
        else if (args[0].equalsIgnoreCase(DiscordBot.prefix + "kick") && event.getMember().hasPermission(Permission.ADMINISTRATOR)){
            text.kick(event);
        }
        else if (args[0].equalsIgnoreCase(DiscordBot.prefix + "avatar")){
            text.getAvatar(event);
        }
        else if (args[0].equalsIgnoreCase(DiscordBot.prefix + "info")){
            text.getUserInfo(event);
        }
        else if (args[0].equalsIgnoreCase(DiscordBot.prefix + "quote")){
            text.randomQuote(event);
        }
        else if(args[0].equalsIgnoreCase(DiscordBot.prefix + "f")){
            text.payRespects(event);
        }
        else if (args[0].equalsIgnoreCase(DiscordBot.prefix + "join") || args[0].equalsIgnoreCase(DiscordBot.prefix + "j")){
            music.joinVolume(event);
            music.join(event);
        }
        else if (args[0].equalsIgnoreCase(DiscordBot.prefix + "leave") || args[0].equalsIgnoreCase(DiscordBot.prefix + "l")){
            music.leave(event);
        }
        else if (args[0].equalsIgnoreCase(DiscordBot.prefix + "play") || args[0].equalsIgnoreCase(DiscordBot.prefix + "p")){
            music.playSong(event);
        }
        else if (args[0].equalsIgnoreCase(DiscordBot.prefix + "stop")){
            music.stop(event);
        }
        else if (args[0].equalsIgnoreCase(DiscordBot.prefix + "queue") || args[0].equalsIgnoreCase(DiscordBot.prefix + "q")){
            music.queue(event);
        }
        else if (args[0].equalsIgnoreCase(DiscordBot.prefix + "skip") || args[0].equalsIgnoreCase(DiscordBot.prefix + "s")){
            music.skip(event);
        }
        else if (args[0].equalsIgnoreCase(DiscordBot.prefix + "pause")){
            music.pause(event);
        }
        else if (args[0].equalsIgnoreCase(DiscordBot.prefix + "resume")){
            music.resume(event);
        }
        else if (args[0].equalsIgnoreCase(DiscordBot.prefix + "song")){
            music.currentSong(event);
        }
        else if (args[0].equalsIgnoreCase(DiscordBot.prefix + "setvolume") || args[0].equalsIgnoreCase(DiscordBot.prefix + "setv")){
            music.userVolume(event);
        }
        else if (args[0].equalsIgnoreCase(DiscordBot.prefix + "volume") || args[0].equalsIgnoreCase(DiscordBot.prefix + "v")){
            music.checkVolume(event);
        }
        else if (args[0].equalsIgnoreCase(DiscordBot.prefix + "seek")){
            music.seek(event);
        }
        else if (args[0].equalsIgnoreCase(DiscordBot.prefix + "rewind")){
            music.rewind(event);
        }
        else if (args[0].equalsIgnoreCase(DiscordBot.prefix + "mhelp")){
            text.musicHelp(event);
        }
        else if (args[0].equalsIgnoreCase(DiscordBot.prefix + "help")){
            text.textHelp(event);
        }
    }

}
