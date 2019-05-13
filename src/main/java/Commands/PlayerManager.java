package Commands;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.util.HashMap;
import java.util.Map;

public class PlayerManager {
    private static PlayerManager INSTANCE;
    private final AudioPlayerManager pm;
    private final Map<Long, GuildMusicManager> mm;

    private PlayerManager(){
        this.mm = new HashMap<>();
        this.pm = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(pm);
        AudioSourceManagers.registerLocalSource(pm);
    }

    public static synchronized PlayerManager getINSTANCE() {
        if (INSTANCE == null){
            INSTANCE = new PlayerManager();
        }
        return INSTANCE;
    }

    public synchronized GuildMusicManager getGMM(Guild guild){
        long guildID = guild.getIdLong();
        GuildMusicManager musicManager = mm.get(guildID);

        if (musicManager == null){
            musicManager = new GuildMusicManager(pm);
            mm.put(guildID, musicManager);
        }

        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

        return  musicManager;
    }

    public void loadPlay(TextChannel channel, String url){
        GuildMusicManager musicManager = getGMM(channel.getGuild());

        pm.loadItemOrdered(musicManager, url, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack audioTrack) {
                channel.sendMessage("Added to queue: " + audioTrack.getInfo().title).queue();
                play(musicManager, audioTrack);
            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {
                AudioTrack first = audioPlaylist.getSelectedTrack();

                if (first == null){
                    first = audioPlaylist.getTracks().get(0);
                }
                channel.sendMessage("Playing: " + first.getInfo().title).queue();
                play(musicManager, first);
            }

            @Override
            public void noMatches() {
                channel.sendMessage("Nothing found").queue();
            }

            @Override
            public void loadFailed(FriendlyException e) {
                channel.sendMessage("Could not play: " + e.getMessage()).queue();
            }
        });
    }

    private void play (GuildMusicManager musicManager, AudioTrack track){
        musicManager.scheduler.queue(track);
    }
}
