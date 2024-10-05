package guild;

import guild.adventurer.Adventurer;
import guild.adventurer.AdventurerRoster;
import guild.adventurer.Party;
import guild.quest.Quest;
import guild.quest.QuestBoard;
import guild.quest.QuestRank;
import guild.shop.Shop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Guild {
    private static final Logger logger = LoggerFactory.getLogger(Guild.class);
    private static final int WEEKLY_DUE = 1;

    QuestBoard questBoard;
    AdventurerRoster adventurerRoster;
    private int day;
    private int guildBank;

    public Guild() {
        day = 1;
        questBoard = new QuestBoard();
        adventurerRoster = new AdventurerRoster(this);
    }

    public static void main(String[] args) {
        Guild g = new Guild();
        g.questBoard.specifyQuests(
                Quest.randomQuest()
                        .endless()
                        .difficulty(QuestRank.NORMAL)
                        .build(),
                Quest.randomQuest()
                        .endless()
                        .difficulty(QuestRank.VERYEASY)
                        .build());
        while (true) {
            //DayStart
            g.newDay();
            //
            g.selectDaytimeActivity();
            //Day End
            g.endDay();
            if (g.day % 365 == 0) {
                logger.info("Year Complete");
            }
        }
    }

    private void newDay() {
        questBoard.removeOldQuests(day);
        questBoard.generateNewQuests(day);
        adventurerRoster.generateNewHeros(day);
        adventurerRoster.mergeOrSplitParties();
    }

    private void selectDaytimeActivity() {
        List<Party> parties = adventurerRoster.getUnassignedParties();
        parties.forEach(party -> {
            switch (party.voteOnDay()) {
                case "QUEST" -> party.selectQuest(questBoard, day);
                case "SHOP" -> party.members().forEach(Shop::browse);
                case "REST" -> party.members().forEach(Adventurer::rest);
                default -> throw new IllegalArgumentException("Unknown Activity");
            }
        });
    }

    private void endDay() {
        questBoard.resolveAcceptedQuestsForCompletion(day);
        guildBank += adventurerRoster.collectDues(day, WEEKLY_DUE);
        logger.info("{}  Active Parties: {}   Active Heros: {}", day, adventurerRoster.getParties().size(), adventurerRoster.getMembers().size());
        logger.info("Adventurer Wealth Stats: {}  GuildWealth: {}", adventurerRoster.getMembers().stream().mapToInt(Adventurer::wealth).summaryStatistics(), guildBank);
        logger.info("Level Stats: {}", adventurerRoster.getMembers().stream().mapToInt(Adventurer::level).summaryStatistics());

        adventurerRoster.rest();
        day++;
    }
}
