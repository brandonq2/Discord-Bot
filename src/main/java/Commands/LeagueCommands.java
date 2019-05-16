package Commands;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.rithms.riot.api.ApiConfig;
import net.rithms.riot.api.RiotApi;
import net.rithms.riot.api.RiotApiException;
import net.rithms.riot.api.endpoints.summoner.dto.Summoner;
import net.rithms.riot.constant.Platform;

public class LeagueCommands {

    private ApiConfig config = new ApiConfig().setKey("RGAPI-b5133aed-bb79-499d-bb9b-857d37ee3649");
    private RiotApi api = new RiotApi(config);

    public void leagueInfo(GuildMessageReceivedEvent event){
        String[] args = event.getMessage().getContentRaw().split("\\s+");
        EmbedBuilder embed = new EmbedBuilder();
        TextChannel channel = event.getChannel();

        try {
            Summoner summoner = api.getSummonerByName(Platform.NA, "MapleKombat");
            switch (args[1].toUpperCase()) {
                case "BR":
                    summoner = api.getSummonerByName(Platform.BR, args[2]);
                    break;
                case "EUNE":
                    summoner = api.getSummonerByName(Platform.EUNE, args[2]);
                    break;
                case "EUW":
                    summoner = api.getSummonerByName(Platform.EUW, args[2]);
                    break;
                case "JP":
                    summoner = api.getSummonerByName(Platform.JP, args[2]);
                    break;
                case "KR":
                    summoner = api.getSummonerByName(Platform.KR, args[2]);
                    break;
                case "LAN":
                    summoner = api.getSummonerByName(Platform.LAN, args[2]);
                    break;
                case "LAS":
                    summoner = api.getSummonerByName(Platform.LAS, args[2]);
                    break;
                case "NA":
                    summoner = api.getSummonerByName(Platform.NA, args[2]);
                    break;
                case "OCE":
                    summoner = api.getSummonerByName(Platform.OCE, args[2]);
                    break;
                case "RU":
                    summoner = api.getSummonerByName(Platform.RU, args[2]);
                    break;
                case "TR":
                    summoner = api.getSummonerByName(Platform.TR, args[2]);
                    break;
            }
            embed.setTitle(":joystick: Summoner Info");
            embed.addField("Name:", summoner.getName(), false);
            embed.addField("Level:", "" + summoner.getSummonerLevel(), false);
            channel.sendMessage(embed.build()).queue();
        }
        catch (RiotApiException e){
            System.out.println(e.getMessage());

        }
    }


}
