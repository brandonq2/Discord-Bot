package Commands;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.GuildVoiceState;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.managers.AudioManager;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

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
        TextChannel channel = event.getChannel();
        String[] args = event.getMessage().getContentRaw().split("\\s+");
        PlayerManager manager = PlayerManager.getINSTANCE();

        if (args.length == 1){
            channel.sendMessage("No song requested").queue();
            return;
        }
        String url = args[1];
        if (!URLCheck(url) && !url.startsWith("ytsearch:")){ // Might get blocked
            channel.sendMessage("Please provide a valid link").queue();
            return;
        }
        manager.loadPlay(event.getChannel(), url);
        manager.getGMM(event.getGuild()).player.setVolume(10);
    }

    public void stop(GuildMessageReceivedEvent event){
        PlayerManager manager = PlayerManager.getINSTANCE();
        GuildMusicManager musicManager = manager.getGMM(event.getGuild());

        musicManager.scheduler.getQueue().clear();
        musicManager.player.stopTrack();
        musicManager.player.setPaused(false);
    }

    public void queue(GuildMessageReceivedEvent event){
        TextChannel channel = event.getChannel();
        PlayerManager manager = PlayerManager.getINSTANCE();
        GuildMusicManager musicManager = manager.getGMM(event.getGuild());
        BlockingQueue<AudioTrack> queue = musicManager.scheduler.getQueue();

        if (queue.isEmpty()){
            channel.sendMessage("Queue is empty").queue();
            return;
        }

        int count = Math.min(queue.size(), 10);
        List<AudioTrack> tracks = new ArrayList<>(queue);
        EmbedBuilder queueEmbed = new EmbedBuilder();
        AudioTrack currentSong = manager.currentSong;
        AudioTrackInfo currentInfo = currentSong.getInfo();

        queueEmbed.setTitle("Current Queue Size: " + queue.size());
        queueEmbed.addField("Currently Playing: ", currentInfo.title, true);
        queueEmbed.addField("Uploader ", currentInfo.author, true);

        for (int i = 0; i < count; i++){
            AudioTrack track = tracks.get(i);
            AudioTrackInfo info = track.getInfo();
            queueEmbed.appendDescription(String.format("%d) %s   %s\n\n", i+1,info.title, info.author));
        }

        channel.sendMessage(queueEmbed.build()).queue();

    }

    private boolean URLCheck(String url){
        try{
            new URL(url);
            return true;
        }
        catch (MalformedURLException e){
            return false;
        }
    }

}
