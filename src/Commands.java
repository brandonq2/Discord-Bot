import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import java.util.List;

public class Commands extends ListenerAdapter{
    public void onGuildMessageReceived(GuildMessageReceivedEvent event){
        String[] args = event.getMessage().getContentRaw().split("\\s+");

        if (args[0].equalsIgnoreCase(DiscordBot.prefix + "arrive")){
            arrive(event);
        }
        else if (args[0].equalsIgnoreCase(DiscordBot.prefix + "snap")){
            snapMessages(event);
        }
    }

    public void arrive(GuildMessageReceivedEvent event){
        event.getChannel().sendTyping().queue();
        event.getChannel().sendMessage("Dread it. Run from it. Destiny arrives all the same. And now, it's here. Or should I say, I am.").queue();
    }

    public void snapMessages(GuildMessageReceivedEvent event){
        String[] args = event.getMessage().getContentRaw().split("\\s+");
        EmbedBuilder error = new EmbedBuilder();

        if (args.length < 2){
            error.setColor(0x6F3C89);
            error.setTitle("Missing Arguments");
            error.setDescription("Please enter in a number");
            event.getChannel().sendMessage(error.build()).queue();
        }
        else if(args.length > 2){
            error.setColor(0x6F3C89);
            error.setTitle("Too Many Arguments");
            error.setDescription("Please check that there's only one argument");
            event.getChannel().sendMessage(error.build()).queue();
        }
        else if(!args[1].matches("-?(0|[1-9]\\d*)")){
            error.setColor(0x6F3C89);
            error.setTitle("Invalid Argument");
            error.setDescription("Please enter a number as the argument");
            event.getChannel().sendMessage(error.build()).queue();
        }
        else {
            try{
                List<Message> messages = event.getChannel().getHistory().retrievePast(Integer.parseInt(args[1]) + 1).complete();
                event.getChannel().deleteMessages(messages).queue();

                EmbedBuilder success = new EmbedBuilder();
                success.setColor(0x6F3C89);
                success.setTitle("Fun isn't something one considers when balancing the universe. But this... does put a smile on my face.");
                success.setDescription("Messages deleted successfully");
                event.getChannel().sendMessage(success.build()).queue();
            }
            catch (IllegalArgumentException e){
                if (e.toString().startsWith("java.lang.IllegalArgumentException: Message retrieval")){
                    error.setColor(0x6F3C89);
                    error.setTitle("Try snapping a couple times");
                    error.setDescription("You can only delete 1-99 messages");
                    event.getChannel().sendMessage(error.build()).queue();
                }
                else{
                    error.setColor(0x6F3C89);
                    error.setTitle("The Time Stone isn't powerful enough");
                    error.setDescription("Can't delete messages older than 2 weeks");
                    event.getChannel().sendMessage(error.build()).queue();

                }
            }
        }
    }
}
