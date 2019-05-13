package Commands;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.GuildVoiceState;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.managers.AudioManager;

public class MusicCommands {

    public void join (GuildMessageReceivedEvent event){
        TextChannel channel = event.getChannel();
        AudioManager audioManager = event.getGuild().getAudioManager();

        if (audioManager.isConnected()){
            channel.sendMessage("Already Conncected").queue();
            return;
        }

        GuildVoiceState voiceState = event.getMember().getVoiceState();

        if (!voiceState.inVoiceChannel()){
            channel.sendMessage("Please join a voice channel").queue();
            return;
        }

        VoiceChannel vc = voiceState.getChannel();
        Member member = event.getGuild().getSelfMember();

        if (!member.hasPermission(vc, Permission.VOICE_CONNECT)){
            channel.sendMessage("Missing perms").queue();
            return;
        }

        audioManager.openAudioConnection(vc);
        channel.sendMessage("Joining").queue();
    }

    public void leave (GuildMessageReceivedEvent event){
        TextChannel channel = event.getChannel();
        AudioManager audioManager = event.getGuild().getAudioManager();

        if (!audioManager.isConnected()){
            channel.sendMessage("Not connected").queue();
            return;
        }

        VoiceChannel vc = audioManager.getConnectedChannel();
        if (!vc.getMembers().contains(event.getMember())){
            channel.sendMessage("Must be in the same voice channel");
            return;
        }

        audioManager.closeAudioConnection();
        channel.sendMessage("Cya nerds").queue();
    }

    public void playSong (GuildMessageReceivedEvent event){
        PlayerManager manager = PlayerManager.getINSTANCE();

        manager.loadPlay(event.getChannel(), "https://www.youtube.com/watch?v=kOHB85vDuow");

        manager.getGMM(event.getGuild()).player.setVolume(10);
    }
}
