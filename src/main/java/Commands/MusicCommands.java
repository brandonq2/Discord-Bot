package Commands;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
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
import java.net.URI;
import java.net.URISyntaxException;
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
        if (!URLCheck(url)){ // Might get blocked
            String yt = "ytsearch: ";
            for (int i = 1; i < args.length; i++){
                yt += args[i] + " ";
            }
            manager.loadPlay(event.getChannel(), yt);
            manager.getGMM(event.getGuild()).player.setVolume(10);
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

        if (queue.isEmpty() && musicManager.player.getPlayingTrack() == null){
            channel.sendMessage("Queue is empty").queue();
            return;
        }

        int count = Math.min(queue.size(), 10);
        List<AudioTrack> tracks = new ArrayList<>(queue);
        EmbedBuilder queueEmbed = new EmbedBuilder();
        AudioTrack currentSong = musicManager.player.getPlayingTrack();
        AudioTrackInfo currentInfo = currentSong.getInfo();

        queueEmbed.setTitle("Current Queue Size: " + queue.size());
        queueEmbed.setColor(0x6F3C89);
        queueEmbed.setThumbnail(getThumbnail(currentInfo));
        queueEmbed.addField("Currently Playing: ", currentInfo.title, false);
        queueEmbed.addField("Uploader: ", currentInfo.author, false);


        String queued = "";
        for (int i = 0; i < count; i++){
            AudioTrack track = tracks.get(i);
            AudioTrackInfo info = track.getInfo();
            queued += "" + (i+1) + ") ";
            queued += "Song Name: " + info.title + "\n" + "Uploaded By: " + info.author + "\n\n";
        }
        queueEmbed.addField("Queued Song:", queued, false);
        queueEmbed.setFooter("Requested by: " + event.getMember().getUser().getName(), event.getAuthor().getAvatarUrl());
        channel.sendMessage(queueEmbed.build()).queue();
    }

    public void skip(GuildMessageReceivedEvent event){
        TextChannel channel = event.getChannel();
        PlayerManager manager = PlayerManager.getINSTANCE();
        GuildMusicManager musicManager = manager.getGMM(event.getGuild());
        TrackScheduler scheduler = musicManager.scheduler;
        AudioPlayer player = musicManager.player;

        if (player.getPlayingTrack() == null){
            channel.sendMessage("Nothing is playing").queue();
            return;
        }

        scheduler.nextTrack();
        channel.sendMessage("Song skipped").queue();
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

    private String getThumbnail(AudioTrackInfo info) {
        try {
            URI uri = new URI(info.uri);
            if (uri.getHost().contains("youtube.com") || uri.getHost().contains("youtu.be")) {
                return String.format("https://img.youtube.com/vi/%s/0.jpg", info.identifier);
            }
        } catch (URISyntaxException e) {
            // fall down
        }
        return null;
    }

}
