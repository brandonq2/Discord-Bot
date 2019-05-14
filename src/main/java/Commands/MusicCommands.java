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
import java.util.concurrent.TimeUnit;

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

        if (checkBotConnected(audioManager,channel)){
            return;
        }
        else if (checkConnected(event, audioManager, channel)){
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
        AudioManager audioManager = event.getGuild().getAudioManager();

        if (checkBotConnected(audioManager,channel)){
            return;
        }
        else if (checkConnected(event, audioManager, channel)){
            return;
        }

        if (args.length == 1){
            embed.setTitle(":no_entry: No Song Requested");
            embed.setColor(0x6F3C89);
            channel.sendMessage(embed.build()).queue();
            return;
        }
        String url = args[1];
        if (!URLCheck(url)){
            String yt = "ytsearch: ";
            for (int i = 1; i < args.length; i++){
                yt += args[i] + " ";
            }
            manager.loadPlay(event, event.getChannel(), yt);
            return;
        }
        manager.loadPlay(event, event.getChannel(), url);
    }

    public void stop(GuildMessageReceivedEvent event){
        PlayerManager manager = PlayerManager.getINSTANCE();
        GuildMusicManager musicManager = manager.getGMM(event.getGuild());
        EmbedBuilder embed = new EmbedBuilder();
        TextChannel channel = event.getChannel();
        AudioManager audioManager = event.getGuild().getAudioManager();

        if (checkBotConnected(audioManager,channel)){
            return;
        }
        else if (checkConnected(event, audioManager, channel)){
            return;
        }

        if (musicManager.player.getPlayingTrack() == null && musicManager.scheduler.getQueue().size() == 0){
            embed.setTitle(":no_entry: Nothing is Playing or Queued");
            embed.setColor(0x6F3C89);
            channel.sendMessage(embed.build()).queue();
            return;
        }
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
        AudioManager audioManager = event.getGuild().getAudioManager();

        if (checkBotConnected(audioManager,channel)){
            return;
        }
        else if (checkConnected(event, audioManager, channel)){
            return;
        }

        if (musicManager.player.getPlayingTrack() == null){
            embed.setTitle(":no_entry: Nothing is Playing");
            embed.setColor(0x6F3C89);
            channel.sendMessage(embed.build()).queue();
            return;
        }

        if(musicManager.player.isPaused()){
            embed.setTitle(":no_entry: Already Paused");
            embed.setColor(0x6F3C89);
            channel.sendMessage(embed.build()).queue();
            return;
        }
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
        AudioManager audioManager = event.getGuild().getAudioManager();

        if (checkBotConnected(audioManager,channel)){
            return;
        }
        else if (checkConnected(event, audioManager, channel)){
            return;
        }

        if(!musicManager.player.isPaused()){
            embed.setTitle(":no_entry: Nothing is Playing");
            embed.setColor(0x6F3C89);
            channel.sendMessage(embed.build()).queue();
            return;
        }
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
        AudioManager audioManager = event.getGuild().getAudioManager();

        if (checkBotConnected(audioManager,channel)){
            return;
        }

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
        AudioPlayer player = musicManager.player;

        queueEmbed.setTitle(":musical_score:  Music Queue");
        queueEmbed.addField("Songs in Queue:", "" + queue.size(), false);
        queueEmbed.setColor(0x6F3C89);
        queueEmbed.setThumbnail(getThumbnail(currentInfo));
        queueEmbed.addField("Currently Playing:", "[" + currentInfo.title + "]" + "(" + currentInfo.uri+ ")", false);
        queueEmbed.addField("Uploader:", currentInfo.author, false);
        queueEmbed.addField("Time:", ":clock1: " + String.format("%s / %s", formatTime(player.getPlayingTrack().getPosition()), formatTime(player.getPlayingTrack().getDuration())), false);
        queueEmbed.addBlankField(true);

        String queued = "";
        for (int i = 0; i < count; i++){
            AudioTrack track = tracks.get(i);
            AudioTrackInfo info = track.getInfo();
            queued += "" + (i+1) + ") ";
            queued += "Song Name: " + "[" + info.title + "]" + "(" + info.uri+ ")"+ "\n" + "Uploaded By: " + info.author + "\n" + "Duration: " + formatTime(track.getDuration()) + "\n\n";
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
        AudioManager audioManager = event.getGuild().getAudioManager();

        if (checkBotConnected(audioManager,channel)){
            return;
        }
        else if (checkConnected(event, audioManager, channel)){
            return;
        }
        else if (checkIfPlaying(channel,player)){
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
        EmbedBuilder embed = new EmbedBuilder();
        AudioPlayer player = musicManager.player;
        AudioManager audioManager = event.getGuild().getAudioManager();

        if (checkBotConnected(audioManager,channel)){
            return;
        }
        else if (checkIfPlaying(channel,player)){
            return;
        }

        AudioTrack currentSong = musicManager.player.getPlayingTrack();
        AudioTrackInfo currentInfo = currentSong.getInfo();
        embed.setTitle(":musical_score:  Current Song");
        embed.setColor(0x6F3C89);
        embed.setImage(getThumbnail(currentInfo));
        embed.addField("Currently Playing:", "[" + currentInfo.title + "]" + "(" + currentInfo.uri+ ")", false);
        embed.addField("Uploader:", currentInfo.author, false);
        embed.addField("Duration:", ":clock1: " + String.format("%s / %s", formatTime(player.getPlayingTrack().getPosition()), formatTime(player.getPlayingTrack().getDuration())), false);
        embed.setFooter("Requested by:" + event.getMember().getUser().getName(), event.getAuthor().getAvatarUrl());

        channel.sendMessage(embed.build()).queue();
    }

    public void joinVolume(GuildMessageReceivedEvent event){
        PlayerManager manager = PlayerManager.getINSTANCE();
        GuildMusicManager musicManager = manager.getGMM(event.getGuild());
        AudioPlayer player = musicManager.player;

        player.setVolume(10);
    }

    public void checkVolume(GuildMessageReceivedEvent event){
        PlayerManager manager = PlayerManager.getINSTANCE();
        GuildMusicManager musicManager = manager.getGMM(event.getGuild());
        AudioPlayer player = musicManager.player;
        TextChannel channel = event.getChannel();
        AudioManager audioManager = event.getGuild().getAudioManager();
        EmbedBuilder embed = new EmbedBuilder();

        if (checkBotConnected(audioManager,channel)){
            return;
        }
        else if (checkConnected(event, audioManager, channel)){
            return;
        }

        embed.setColor(0x6F3C89);
        embed.setTitle(":sound: Volume: " + player.getVolume() + "%");
        event.getChannel().sendMessage(embed.build()).queue();
    }

    public void userVolume(GuildMessageReceivedEvent event){
        String[] args = event.getMessage().getContentRaw().split("\\s+");
        PlayerManager manager = PlayerManager.getINSTANCE();
        GuildMusicManager musicManager = manager.getGMM(event.getGuild());
        AudioPlayer player = musicManager.player;
        EmbedBuilder embed = new EmbedBuilder();
        TextChannel channel = event.getChannel();
        AudioManager audioManager = event.getGuild().getAudioManager();

        if (checkBotConnected(audioManager,channel)){
            return;
        }
        else if (checkConnected(event, audioManager, channel)){
            return;
        }

        if(args.length == 1){
            EmbedBuilder error = new EmbedBuilder();
            error.setColor(0x6F3C89);
            error.setTitle("Missing Argument");
            error.setDescription("Please enter a volume percentage from 0-100");
            event.getChannel().sendMessage(error.build()).queue();
            return;
        }
        if(!args[1].matches("-?(0|[1-9]\\d*)") || Integer.parseInt(args[1]) < 0 || Integer.parseInt(args[1]) > 100){
            EmbedBuilder error = new EmbedBuilder();
            error.setColor(0x6F3C89);
            error.setTitle("Invalid Argument");
            error.setDescription("Please enter a volume percentage from 0-100");
            event.getChannel().sendMessage(error.build()).queue();
            return;
        }


        player.setVolume(Integer.parseInt(args[1]));
        embed.setTitle(":sound: Volume has been set to: " + args[1] + "%" );
        embed.setColor(0x6F3C89);
        channel.sendMessage(embed.build()).queue();
        return;

    }

    public void seek(GuildMessageReceivedEvent event){
        String[] args = event.getMessage().getContentRaw().split("\\s+");

        TextChannel channel = event.getChannel();
        PlayerManager manager = PlayerManager.getINSTANCE();
        GuildMusicManager musicManager = manager.getGMM(event.getGuild());
        EmbedBuilder embed = new EmbedBuilder();
        AudioPlayer player = musicManager.player;
        AudioManager audioManager = event.getGuild().getAudioManager();


        if (checkBotConnected(audioManager,channel)){
            return;
        }
        else if (checkConnected(event, audioManager, channel)){
            return;
        }
        else if (checkIfPlaying(channel,player)){
            return;
        }

        if (args.length < 2){
            embed.setTitle(":no_entry: Please specify how many seconds to seek.");
            embed.setColor(0x6F3C89);
            channel.sendMessage(embed.build()).queue();
            return;
        }
        if(!args[1].matches("-?(0|[1-9]\\d*)")){
            embed.setColor(0x6F3C89);
            embed.setTitle("Invalid Argument");
            embed.setDescription("Please enter a positive number as the argument");
            event.getChannel().sendMessage(embed.build()).queue();
            return;
        }
        if(Integer.parseInt(args[1]) < 0){
            embed.setColor(0x6F3C89);
            embed.setTitle("Invalid Argument");
            embed.setDescription("Please enter a positive number as the argument");
            event.getChannel().sendMessage(embed.build()).queue();
            return;
        }
        long length = Long.parseLong(args[1]) * 1000;
        long requested = player.getPlayingTrack().getPosition() + length;
        long maxSeek = player.getPlayingTrack().getDuration() - 1000;
        String originalTime = formatTime(player.getPlayingTrack().getPosition());
        String newTime = "";
        if (requested >= maxSeek){
            player.getPlayingTrack().setPosition(maxSeek);
            newTime = formatTime(player.getPlayingTrack().getPosition());
        }
        else {
            player.getPlayingTrack().setPosition(requested);
            newTime = formatTime(player.getPlayingTrack().getPosition());

        }
        String songLength = formatTime(player.getPlayingTrack().getDuration());
        embed.setColor(0x6F3C89);
        embed.setTitle(":fast_forward: Fast forwarded: " + args[1] + "s");
        embed.addField("From:", ":clock1: " + originalTime + " / " + songLength, true);
        embed.addField("To:", ":clock1: " + newTime + " / " + songLength, true);
        event.getChannel().sendMessage(embed.build()).queue();
    }

    public void rewind(GuildMessageReceivedEvent event){
        String[] args = event.getMessage().getContentRaw().split("\\s+");

        TextChannel channel = event.getChannel();
        PlayerManager manager = PlayerManager.getINSTANCE();
        GuildMusicManager musicManager = manager.getGMM(event.getGuild());
        EmbedBuilder embed = new EmbedBuilder();
        AudioPlayer player = musicManager.player;
        AudioManager audioManager = event.getGuild().getAudioManager();

        if (checkBotConnected(audioManager,channel)){
            return;
        }
        else if (checkConnected(event, audioManager, channel)){
            return;
        }
        else if (checkIfPlaying(channel,player)){
            return;
        }

        if (args.length < 2){
            embed.setTitle(":no_entry: Please specify how many seconds to rewind.");
            embed.setColor(0x6F3C89);
            channel.sendMessage(embed.build()).queue();
            return;
        }
        if(!args[1].matches("-?(0|[1-9]\\d*)")){
            embed.setColor(0x6F3C89);
            embed.setTitle("Invalid Argument");
            embed.setDescription("Please enter a positive number as the argument");
            event.getChannel().sendMessage(embed.build()).queue();
            return;
        }
        if(Integer.parseInt(args[1]) < 0){
            embed.setColor(0x6F3C89);
            embed.setTitle("Invalid Argument");
            embed.setDescription("Please enter a positive number as the argument");
            event.getChannel().sendMessage(embed.build()).queue();
            return;
        }
        long length = Long.parseLong(args[1]) * 1000;
        long requested = player.getPlayingTrack().getPosition() - length;
        long maxRewind = 0;
        String originalTime = formatTime(player.getPlayingTrack().getPosition());
        String newTime = "";
        if (requested <= maxRewind){
            player.getPlayingTrack().setPosition(maxRewind);
            newTime = formatTime(player.getPlayingTrack().getPosition());
        }
        else {
            player.getPlayingTrack().setPosition(requested);
            newTime = formatTime(player.getPlayingTrack().getPosition());
        }
        String songLength = formatTime(player.getPlayingTrack().getDuration());
        embed.setColor(0x6F3C89);
        embed.setTitle(":rewind: Rewinded: " + args[1] + "s");
        embed.addField("From:", ":clock1: " + originalTime + " / " + songLength, true);
        embed.addField("To:",":clock1: " +  newTime + " / " + songLength, true);
        event.getChannel().sendMessage(embed.build()).queue();
    }

    public boolean checkConnected (GuildMessageReceivedEvent event, AudioManager audioManager, TextChannel channel){
        EmbedBuilder embed = new EmbedBuilder();
        VoiceChannel vc = audioManager.getConnectedChannel();

        if (!vc.getMembers().contains(event.getMember())){
            embed.setTitle(":no_entry: Must be in the same voice channel");
            embed.setColor(0x6F3C89);
            channel.sendMessage(embed.build()).queue();
            return true;
        }
        return false;
    }

    public boolean checkBotConnected (AudioManager audioManager, TextChannel channel){
        EmbedBuilder embed = new EmbedBuilder();

        if (!audioManager.isConnected()){
            embed.setTitle(":no_entry: Bot Not Connected");
            embed.setColor(0x6F3C89);
            channel.sendMessage(embed.build()).queue();
            return true;
        }
        return false;
    }

    public boolean checkIfPlaying (TextChannel channel, AudioPlayer player){
        EmbedBuilder embed = new EmbedBuilder();

        if (player.isPaused() || player.getPlayingTrack() == null){
            embed.setTitle(":no_entry: Nothing is Playing");
            embed.setColor(0x6F3C89);
            channel.sendMessage(embed.build()).queue();
            return true;
        }
        return false;
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

    private String formatTime(long time){
        final long hours = time / TimeUnit.HOURS.toMillis(1);
        final long minutes = time / TimeUnit.MINUTES.toMillis(1);
        final long seconds = time % TimeUnit.MINUTES.toMillis(1) / TimeUnit.SECONDS.toMillis(1);


        if (hours == 0 || minutes < 10){
            return String.format("%01d:%02d", minutes, seconds);
        }
        if (hours == 0){
            return String.format("%02d:%02d", minutes, seconds);
        }

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}