package MainFiles;

import Commands.CommandHub;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import javax.security.auth.login.LoginException;

public class DiscordBot{
    public static yeet yeet = new yeet();
    public static JDA jda;
    public static String prefix = ">";
    public static void main(String[] args) throws LoginException{
        jda = new JDABuilder(AccountType.BOT).setToken(yeet.secret).buildAsync();
        jda.getPresence().setGame(Game.playing("Farming Simulator 2019"));
        jda.addEventListener(new CommandHub());
    }
}
