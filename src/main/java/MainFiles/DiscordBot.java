package MainFiles;

import Commands.CommandHub;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import javax.security.auth.login.LoginException;

public class DiscordBot{
    public static JDA jda;
    public static String prefix = ">";


    public static void main(String[] args) throws LoginException{
        jda = new JDABuilder(AccountType.BOT).setToken("NDM4OTQ5Njg5MDU2ODIxMjQ5.XNeiag.ah-fRuP4qWZXy4MuwE6WuS938P8").buildAsync();
        jda.getPresence().setGame(Game.playing("Finding the Infinity Stones"));
        jda.addEventListener(new CommandHub());
    }
}
