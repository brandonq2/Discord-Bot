package Commands;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.rithms.riot.api.ApiConfig;
import net.rithms.riot.api.RiotApi;
import net.rithms.riot.api.RiotApiException;
import net.rithms.riot.api.endpoints.champion_mastery.dto.ChampionMastery;
import net.rithms.riot.api.endpoints.summoner.dto.Summoner;
import net.rithms.riot.constant.Platform;

public class LeagueCommands {

    private ApiConfig config = new ApiConfig().setKey("RGAPI-ae9554a2-0230-4c5c-969f-4f9fac5d004d");
    private RiotApi api = new RiotApi(config);

    public void leagueInfo(GuildMessageReceivedEvent event){
        String[] args = event.getMessage().getContentRaw().split("\\s+");
        EmbedBuilder embed = new EmbedBuilder();
        TextChannel channel = event.getChannel();

        try {
            Summoner summoner = api.getSummonerByName(Platform.NA, "MapleKombat");
           // ChampionMastery yeet = api.getSummonerByName();
            //yeet.getChampionId();
            String summonerName = "";
            for (int i = 2; i < args.length; i++){
                summonerName += args[i];
            }
            switch (args[1].toUpperCase()) {
                case "BR":
                    summoner = api.getSummonerByName(Platform.BR, summonerName);
                    break;
                case "EUNE":
                    summoner = api.getSummonerByName(Platform.EUNE, summonerName);
                    break;
                case "EUW":
                    summoner = api.getSummonerByName(Platform.EUW, summonerName);
                    break;
                case "JP":
                    summoner = api.getSummonerByName(Platform.JP, summonerName);
                    break;
                case "KR":
                    summoner = api.getSummonerByName(Platform.KR, summonerName);
                    break;
                case "LAN":
                    summoner = api.getSummonerByName(Platform.LAN, summonerName);
                    break;
                case "LAS":
                    summoner = api.getSummonerByName(Platform.LAS, summonerName);
                    break;
                case "NA":
                    summoner = api.getSummonerByName(Platform.NA, summonerName);
                    break;
                case "OCE":
                    summoner = api.getSummonerByName(Platform.OCE, summonerName);
                    break;
                case "RU":
                    summoner = api.getSummonerByName(Platform.RU, summonerName);
                    break;
                case "TR":
                    summoner = api.getSummonerByName(Platform.TR, summonerName);
                    break;
            }

            embed.setTitle(":joystick: Summoner Info");
            embed.setImage()
            embed.addField("Name:", summoner.getName(), false);
            embed.addField("Level:", "" + summoner.getSummonerLevel(), false);
            channel.sendMessage(embed.build()).queue();
        }
        catch (RiotApiException e){
            System.out.println(e.getMessage());

        }
    }


}
