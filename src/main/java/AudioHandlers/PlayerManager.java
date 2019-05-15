package AudioHandlers;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import java.util.HashMap;
import java.util.Map;

public class PlayerManager {
    private static PlayerManager INSTANCE;
    private final AudioPlayerManager pm;
    private final Map<Long, GuildMusicManager> mm;
    public String currentSongName = "";

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

    public void loadPlay(GuildMessageReceivedEvent event,TextChannel channel, String url){
        GuildMusicManager musicManager = getGMM(channel.getGuild());
        EmbedBuilder embed = new EmbedBuilder();
          pm.loadItem(url, new AudioLoadResultHandler() {

            @Override
            public void trackLoaded(AudioTrack audioTrack) {
                embed.setTitle(":headphones: Added to queue: " + audioTrack.getInfo().title);
                embed.setColor(0x6F3C89);
                Member user = event.getMember();
                embed.setFooter("Requested by: " + (user.getNickname() == null ? user.getEffectiveName() : user.getNickname()), event.getAuthor().getAvatarUrl());
                channel.sendMessage(embed.build()).queue();
                play(musicManager, audioTrack);
            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {
                AudioTrack first = audioPlaylist.getTracks().remove(0);
                if (first == null){
                    currentSongName = first.getInfo().title;
                    first = audioPlaylist.getTracks().remove(0);
                }
                currentSongName = first.getInfo().title;
                embed.setTitle(":headphones: Added to queue: " + first.getInfo().title);
                embed.setColor(0x6F3C89);
                Member user = event.getMember();
                embed.setFooter("Requested by: " + (user.getNickname() == null ? user.getEffectiveName() : user.getNickname()), event.getAuthor().getAvatarUrl());                channel.sendMessage(embed.build()).queue();
                play(musicManager, first);
            }

            @Override
            public void noMatches() {
                embed.setTitle(":no_entry: Nothing Found");
                embed.setColor(0x6F3C89);
                channel.sendMessage(embed.build()).queue();
            }

            @Override
            public void loadFailed(FriendlyException e) {
                embed.setTitle(":mo_entry: Could not play: " + e.getMessage());
                embed.setColor(0x6F3C89);
                channel.sendMessage(embed.build()).queue();
            }
        });
    }

    private void play (GuildMusicManager musicManager, AudioTrack track){
        musicManager.scheduler.queue(track);
    }
}