package Commands;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import UrbanAPI.UrbanAPI;
import UrbanAPI.UrbanException;

public class UrbanDictionaryCommands extends ListenerAdapter {
    public void urbanDefine(GuildMessageReceivedEvent event){
        String[] args = event.getMessage().getContentRaw().split("\\s+");
        UrbanAPI api = new UrbanAPI("0adf3082dbmshdcc359339d6fb2ap10691ajsn5f1cd1cda034");
        String word = "";
        EmbedBuilder embed = new EmbedBuilder();

        if (args.length == 1){
            embed.setColor(0x6F3C89);
            embed.setTitle(":no_entry: Please enter in a word to search");
            event.getChannel().sendMessage(embed.build()).queue();
            return;
        }

        for (int i = 1; i < args.length; i++){
            word += args[i] + " ";
        }
        try {
            embed.setColor(0x6F3C89);
            embed.setTitle(":book: Urban Definition for: " + word);
            embed.addField("Definition:", api.getUrbanInfo(word).getInfo().get(0).definition.replaceAll("[\\[\\]]",""), false);
            event.getChannel().sendMessage(embed.build()).queue();
            return;
        }
        catch (UrbanException e){
            embed.setColor(0x6F3C89);
            embed.setTitle(":no_entry: Couldn't find a definition for: " + word);
            event.getChannel().sendMessage(embed.build()).queue();
            return;
        }
        catch (IndexOutOfBoundsException e){
            embed.setColor(0x6F3C89);
            embed.setTitle(":no_entry: Couldn't find a definition for: " + word);
            event.getChannel().sendMessage(embed.build()).queue();
            return;
        }
    }
}
