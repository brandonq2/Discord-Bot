package Commands;

import com.github.Doomsdayrs.Jikan4java.connection.Anime.AnimeConnection;
import com.github.Doomsdayrs.Jikan4java.types.Main.Anime.Anime;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class AnimeCommands {

    public void getAnime(GuildMessageReceivedEvent event){
        String[] args = event.getMessage().getContentRaw().split("\\s+");
        AnimeConnection animeConnection = new AnimeConnection();
        TextChannel channel =  event.getChannel();
        EmbedBuilder embed = new EmbedBuilder();


        if (args.length == 1){
            embed.setColor(0x6F3C89);
            embed.setTitle(":no_entry: Please enter in an anime");
        }

        String input = "";
        for (int i = 1; i < args.length; i++){
            input += args[i] + " ";
        }

        try {
            Anime anime = animeConnection.search(input).get();
            embed.setColor(0x6F3C89);
            embed.setTitle(":tv: Anime Information");
            embed.setThumbnail(anime.imageURL);
            String producers = "";
            for (int i = 0; i < anime.producers.size(); i++){
                producers += anime.producers.get(i).name;
                if (!(i+1 == anime.producers.size())){
                    producers += ", ";
                }
            }
            String licensors = "";
            for (int i = 0; i < anime.licensors.size(); i++){
                licensors += anime.licensors.get(i).name;
                if (!(i+1 == anime.licensors.size())){
                    licensors += ", ";
                }
            }
            String studios = "";
            for (int i = 0; i < anime.studios.size(); i++){
                studios += anime.studios.get(i).name;
                if (!(i+1 == anime.studios.size())){
                    studios += ", ";
                }
            }
            String genres = "";
            for (int i = 0; i < anime.genres.size(); i++){
                genres += anime.genres.get(i).name;
                if (!(i+1 == anime.genres.size())){
                    genres += ", ";
                }
            }

            embed.addField("English Title: ", (anime.title_english == null ? anime.title : anime.title_english), false);
            embed.addField("Japanese Title: ", (anime.title_japanese == null ? anime.title : anime.title_japanese), false);
            embed.addField("Episodes: ", "" + anime.episodes, false);
            embed.addField("Status: ", "" + anime.status, false);
            embed.addField("Premiered: ", anime.premiered, false);
            embed.addField("Producers: ", "" + (producers.length() == 0 ? "Unknown" : producers), false);
            embed.addField("Licensors: ", "" + (licensors.length() == 0 ? "Unknown" : licensors), false);
            embed.addField("Studios: ", "" + (studios.length() == 0 ? "Unknown" : studios), false);
            embed.addField("Genres: ", "" + (genres.length() == 0 ? "Unknown" : genres), false);
            embed.addField("Score: ", "" + anime.score + " ★", false);
            embed.addField("Synopsis: ", anime.synopsis, false);
            Member user = event.getMember();
            embed.setFooter("Requested by: " + (user.getNickname() == null ? user.getEffectiveName() : user.getNickname()), event.getAuthor().getAvatarUrl());
            channel.sendMessage(embed.build()).queue();
        }
        catch (ParseException e){
            embed.setColor(0x6F3C89);
            embed.setTitle(":no_entry: Something went wrong, please try again");
        }
        catch (IOException e){
            System.out.println("io");
        }
        catch (InterruptedException e){
            System.out.println("Interrupt");
        }
        catch (ExecutionException e){
            System.out.println("Execute");
        }
    }
}
