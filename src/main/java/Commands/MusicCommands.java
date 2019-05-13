package Commands;

import Handlers.GuildMusicManager;
import Handlers.PlayerManager;
import Handlers.TrackScheduler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
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
        EmbedBuilder embed = new EmbedBuilder();

        if (audioManager.isConnected()){
            embed.setTitle(":no_entry: Already Connected");
            embed.setColor(0x6F3C89);
            channel.sendMessage(embed.build()).queue();
            return;
        }

        GuildVoiceState voiceState = event.getMember().getVoiceState();

        if (!voiceState.inVoiceChannel()){
            embed.setTitle(":no_entry: Please join a voice channel");
            embed.setColor(0x6F3C89);
            channel.sendMessage(embed.build()).queue();
            return;
        }

        VoiceChannel vc = voiceState.getChannel();
        Member member = event.getGuild().getSelfMember();

        if (!member.hasPermission(vc, Permission.VOICE_CONNECT)){
            embed.setTitle(":no_entry: Insufficient Permissions");
            embed.setColor(0x6F3C89);
            return;
        }

        audioManager.openAudioConnection(vc);
        embed.setTitle(":white_check_mark: Joining Voice Channel");
        embed.setColor(0x6F3C89);
        channel.sendMessage(embed.build()).queue();
    }

    public void leave (GuildMessageReceivedEvent event){
        TextChannel channel = event.getChannel();
        AudioManager audioManager = event.getGuild().getAudioManager();
        EmbedBuilder embed = new EmbedBuilder();

        if (!audioManager.isConnected()){
            embed.setTitle(":no_entry: Not Connected");
            embed.setColor(0x6F3C89);
            channel.sendMessage(embed.build()).queue();
            return;
        }

        VoiceChannel vc = audioManager.getConnectedChannel();
        if (!vc.getMembers().contains(event.getMember())){
            embed.setTitle(":no_entry: Must be in the same voice channel");
            embed.setColor(0x6F3C89);
            channel.sendMessage(embed.build()).queue();
            return;
        }
        stop(event);
        audioManager.closeAudioConnection();
        embed.setTitle(":v: Cya Nerds");
        embed.setColor(0x6F3C89);
        channel.sendMessage(embed.build()).queue();
    }

    public void playSong (GuildMessageReceivedEvent event){
        TextChannel channel = event.getChannel();
        String[] args = event.getMessage().getContentRaw().split("\\s+");
        PlayerManager manager = PlayerManager.getINSTANCE();
        EmbedBuilder embed = new EmbedBuilder();

        if (args.length == 1){
            embed.setTitle(":no_entry: No Song Requested");
            embed.setColor(0x6F3C89);
            channel.sendMessage(embed.build()).queue();
            return;
        }
        String url = args[1];
        if (!URLCheck(url)){ // Might get blocked
            String yt = "ytsearch: ";
            for (int i = 1; i < args.length; i++){
                yt += args[i] + " ";
            }
            manager.loadPlay(event, event.getChannel(), yt);
            manager.getGMM(event.getGuild()).player.setVolume(10);
            return;
        }


        manager.loadPlay(event, event.getChannel(), url);
        manager.getGMM(event.getGuild()).player.setVolume(10);
    }

    public void stop(GuildMessageReceivedEvent event){
        PlayerManager manager = PlayerManager.getINSTANCE();
        GuildMusicManager musicManager = manager.getGMM(event.getGuild());
        EmbedBuilder embed = new EmbedBuilder();
        TextChannel channel = event.getChannel();

        musicManager.scheduler.getQueue().clear();
        musicManager.player.stopTrack();
        musicManager.player.setPaused(false);

        embed.setTitle("Stopping Music");
        embed.setColor(0x6F3C89);
        channel.sendMessage(embed.build()).queue();
        return;
    }

    public void pause(GuildMessageReceivedEvent event){
        PlayerManager manager = PlayerManager.getINSTANCE();
        GuildMusicManager musicManager = manager.getGMM(event.getGuild());
        EmbedBuilder embed = new EmbedBuilder();
        TextChannel channel = event.getChannel();

        if(musicManager.player.isPaused()){
            embed.setTitle(":no_entry: Already Paused");
            embed.setColor(0x6F3C89);
            channel.sendMessage(embed.build()).queue();
            return;
        }
        //musicManager.player.stopTrack();
        musicManager.player.setPaused(true);

        embed.setTitle(":pause_button: Paused Music");
        embed.setColor(0x6F3C89);
        channel.sendMessage(embed.build()).queue();
        return;
    }

    public void resume(GuildMessageReceivedEvent event){
        PlayerManager manager = PlayerManager.getINSTANCE();
        GuildMusicManager musicManager = manager.getGMM(event.getGuild());
        EmbedBuilder embed = new EmbedBuilder();
        TextChannel channel = event.getChannel();

        if(!musicManager.player.isPaused()){
            embed.setTitle(":no_entry: Not Paused");
            embed.setColor(0x6F3C89);
            channel.sendMessage(embed.build()).queue();
            return;
        }
        //musicManager.player;
        musicManager.player.setPaused(false);

        embed.setTitle(":arrow_forward: Resumed Music");
        embed.setColor(0x6F3C89);
        channel.sendMessage(embed.build()).queue();
        return;
    }

    public void queue(GuildMessageReceivedEvent event){
        TextChannel channel = event.getChannel();
        PlayerManager manager = PlayerManager.getINSTANCE();
        GuildMusicManager musicManager = manager.getGMM(event.getGuild());
        BlockingQueue<AudioTrack> queue = musicManager.scheduler.getQueue();
        EmbedBuilder embed = new EmbedBuilder();

        if (queue.isEmpty() && musicManager.player.getPlayingTrack() == null){
            embed.setTitle(":no_entry: Queue is Empty");
            embed.setColor(0x6F3C89);
            channel.sendMessage(embed.build()).queue();
            return;
        }

        int count = Math.min(queue.size(), 5);
        List<AudioTrack> tracks = new ArrayList<>(queue);
        EmbedBuilder queueEmbed = new EmbedBuilder();
        AudioTrack currentSong = musicManager.player.getPlayingTrack();
        AudioTrackInfo currentInfo = currentSong.getInfo();
        queueEmbed.setTitle(":musical_score:  Music Queue");
        queueEmbed.addField("Current Queue Size:", "" + queue.size(), false);
        queueEmbed.setColor(0x6F3C89);
        queueEmbed.setThumbnail(getThumbnail(currentInfo));
        queueEmbed.addField("Currently Playing:", "[" + currentInfo.title + "]" + "(" + currentInfo.uri+ ")", false);
        queueEmbed.addField("Uploader:", currentInfo.author, false);
        queueEmbed.addBlankField(true);

        String queued = "";
        for (int i = 0; i < count; i++){
            AudioTrack track = tracks.get(i);
            AudioTrackInfo info = track.getInfo();
            queued += "" + (i+1) + ") ";
            queued += "Song Name: " + "[" + info.title + "]" + "(" + info.uri+ ")"+ "\n" + "Uploaded By: " + info.author + "\n\n";
        }

        queueEmbed.addField("Queued Songs:", queued, false);
        queueEmbed.setFooter("Requested by:" + event.getMember().getUser().getName(), event.getAuthor().getAvatarUrl());
        channel.sendMessage(queueEmbed.build()).queue();
    }

    public void skip(GuildMessageReceivedEvent event){
        TextChannel channel = event.getChannel();
        PlayerManager manager = PlayerManager.getINSTANCE();
        GuildMusicManager musicManager = manager.getGMM(event.getGuild());
        TrackScheduler scheduler = musicManager.scheduler;
        AudioPlayer player = musicManager.player;
        EmbedBuilder embed = new EmbedBuilder();

        if (player.getPlayingTrack() == null){
            embed.setTitle(":no_entry: Nothing is Playing");
            embed.setColor(0x6F3C89);
            channel.sendMessage(embed.build()).queue();
            return;
        }

        scheduler.nextTrack();
        embed.setTitle(":track_next: Song Skipped");
        embed.setColor(0x6F3C89);
        channel.sendMessage(embed.build()).queue();
        return;
    }

    public void currentSong(GuildMessageReceivedEvent event){
        TextChannel channel = event.getChannel();
        PlayerManager manager = PlayerManager.getINSTANCE();
        GuildMusicManager musicManager = manager.getGMM(event.getGuild());
        BlockingQueue<AudioTrack> queue = musicManager.scheduler.getQueue();
        EmbedBuilder embed = new EmbedBuilder();

        if (musicManager.player.isPaused() || musicManager.player.getPlayingTrack() == null){
            embed.setTitle(":no_entry: Nothing is Playing");
            embed.setColor(0x6F3C89);
            channel.sendMessage(embed.build()).queue();
            return;
        }

        AudioTrack currentSong = musicManager.player.getPlayingTrack();
        AudioTrackInfo currentInfo = currentSong.getInfo();
        embed.setTitle(":musical_score:  Current Song");
        embed.setColor(0x6F3C89);
        embed.setImage(getThumbnail(currentInfo));
        embed.addField("Currently Playing:", "[" + currentInfo.title + "]" + "(" + currentInfo.uri+ ")", false);
        embed.addField("Uploader:", currentInfo.author, false);
        embed.setFooter("Requested by:" + event.getMember().getUser().getName(), event.getAuthor().getAvatarUrl());

        channel.sendMessage(embed.build()).queue();
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
