package Commands;

import com.github.Doomsdayrs.Jikan4java.connection.Anime.AnimeConnection;
import com.github.Doomsdayrs.Jikan4java.connection.Manga.MangaConnection;
import com.github.Doomsdayrs.Jikan4java.types.Main.Anime.Anime;
import com.github.Doomsdayrs.Jikan4java.types.Main.Manga.Manga;
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
                producers += "[" + anime.producers.get(i).name + "](" + anime.producers.get(i).url + ")";
                if (!(i+1 == anime.producers.size())){
                    producers += ", ";
                }
            }
            String licensors = "";
            for (int i = 0; i < anime.licensors.size(); i++){
                licensors +="[" + anime.licensors.get(i).name + "](" + anime.licensors.get(i).url + ")";
                if (!(i+1 == anime.licensors.size())){
                    licensors += ", ";
                }
            }
            String studios = "";
            for (int i = 0; i < anime.studios.size(); i++){
                studios += "[" + anime.studios.get(i).name + "](" + anime.studios.get(i).url + ")";
                if (!(i+1 == anime.studios.size())){
                    studios += ", ";
                }
            }
            String genres = "";
            for (int i = 0; i < anime.genres.size(); i++){
                genres += "[" + anime.genres.get(i).name + "](" + anime.genres.get(i).url + ")";
                if (!(i+1 == anime.genres.size())){
                    genres += ", ";
                }
            }
            embed.addField("English Title: ", (anime.title_english == null ? "[" + anime.title  +"](" + anime.url + ")": "[" + anime.title_english +"](" + anime.url + ")"), false);
            embed.addField("Japanese Title: ", (anime.title_japanese == null ? "[" + anime.title  +"](" + anime.url + ")": "[" + anime.title_japanese +"](" + anime.url + ")"), false);
            embed.addField("MAL URL: ", "" + anime.url, false);
            embed.addField("Episodes: ", "" + (anime.episodes == 0 ? "Unknown" : anime.episodes), false);
            embed.addField("Status: ", "" + (anime.status == null ? "Unknown" : anime.status), false);
            embed.addField("Premiered: ", (anime.premiered == null ? "Unknown" : anime.premiered), false);
            embed.addField("Producers: ", "" + (producers.length() == 0 ? "Unknown" : producers), false);
            embed.addField("Licensors: ", "" + (licensors.length() == 0 ? "Unknown" : licensors), false);
            embed.addField("Studios: ", "" + (studios.length() == 0 ? "Unknown" : studios), false);
            embed.addField("Genres: ", "" + (genres.length() == 0 ? "Unknown" : genres), false);
            embed.addField("Score: ", "" + anime.score + " ★", false);
            embed.addField("Synopsis: ",(anime.synopsis.length() > 500 ? "*" + anime.synopsis.substring(0, 500) + "...*\n" + "([" + "To read the rest, check it out on MAL" +"](" + anime.url + "))": "*" +anime.synopsis) + "*", false);
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

    public void getManga(GuildMessageReceivedEvent event){
        String[] args = event.getMessage().getContentRaw().split("\\s+");
        MangaConnection mangaConnection = new MangaConnection();
        TextChannel channel =  event.getChannel();
        EmbedBuilder embed = new EmbedBuilder();


        if (args.length == 1){
            embed.setColor(0x6F3C89);
            embed.setTitle(":no_entry: Please enter in a manga");
        }

        String input = "";
        for (int i = 1; i < args.length; i++){
            input += args[i] + " ";
        }

        try {
            Manga manga = mangaConnection.search(input).get();
            embed.setColor(0x6F3C89);
            embed.setTitle(":books: Manga Information");
            embed.setThumbnail(manga.image_url);
            String authors = "";
            for (int i = 0; i < manga.authors.size(); i++){
                authors += "[" + manga.authors.get(i).name + "](" + manga.authors.get(i).url + ")";
                if (!(i+1 == manga.authors.size())){
                    authors += ", ";
                }
            }
            String serialization = "";
            for (int i = 0; i < manga.serializations.size(); i++){
                serialization +="[" + manga.serializations.get(i).name + "](" + manga.serializations.get(i).url + ")";
                if (!(i+1 == manga.serializations.size())){
                    serialization += ", ";
                }
            }

            String genres = "";
            for (int i = 0; i < manga.genres.size(); i++){
                genres += "[" + manga.genres.get(i).name + "](" + manga.genres.get(i).url + ")";
                if (!(i+1 == manga.genres.size())){
                    genres += ", ";
                }
            }
            embed.addField("English Title: ", (manga.title_english == null ? "[" + manga.title  +"](" + manga.url + ")": "[" + manga.title_english +"](" + manga.url + ")"), false);
            embed.addField("Japanese Title: ", (manga.title_japanese == null ? "[" + manga.title  +"](" + manga.url + ")": "[" + manga.title_japanese +"](" + manga.url + ")"), false);
            embed.addField("MAL URL: ", "" + manga.url, false);
            embed.addField("Type: ", "" + (manga.type == null ? "Unknown" : manga.type), false);
            embed.addField("Volumes: ", "" + (manga.volumes == 0 ? "Unknown" : manga.volumes), false);
            embed.addField("Chapters: ", "" + (manga.chapters == 0 ? "Unknown" : manga.chapters), false);
            embed.addField("Status: ", "" + (manga.status == null ? "Unknown" : manga.status), false);
            embed.addField("Authors: ", "" + (authors.length() == 0 ? "Unknown" : authors), false);
            embed.addField("Serialization: ", "" + (serialization.length() == 0 ? "Unknown" : serialization), false);
            embed.addField("Genres: ", "" + (genres.length() == 0 ? "Unknown" : genres), false);
            embed.addField("Score: ", "" + manga.score + " ★", false);
            embed.addField("Synopsis: ",(manga.synopsis.length() > 500 ? "*" + manga.synopsis.substring(0, 500) + "...*\n" + "([" + "To read the rest, check it out on MAL" +"](" + manga.url + "))": "*" +manga.synopsis) + "*", false);
            Member user = event.getMember();
            embed.setFooter("Requested by: " + (user.getNickname() == null ? user.getEffectiveName() : user.getNickname()), event.getAuthor().getAvatarUrl());
            channel.sendMessage(embed.build()).queue();
        }
        catch (InterruptedException e){
            System.out.println("Interrupt");
        }
        catch (ExecutionException e){
            System.out.println("Execute");
        }
    }

}
