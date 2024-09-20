package guild;

import guild.adventurer.Adventurer;
import guild.adventurer.AdventurerRoster;
import guild.adventurer.Party;
import guild.quest.QuestBoard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Guild {
    private static final Logger logger = LoggerFactory.getLogger(Guild.class);

    QuestBoard questBoard;
    AdventurerRoster adventurerRoster;
    private int day;

    public Guild() {
        day = 1;
        questBoard = new QuestBoard();
        adventurerRoster = new AdventurerRoster(this);
    }

    public static void main(String[] args) {
        Guild g = new Guild();
        while (true) {
            //DayStart
            g.newDay();
            //
            g.assignQuests();
            //Day End
            g.endDay();
        }
    }

    private void newDay() {
        questBoard.removeOldQuests(day);
        questBoard.generateNewQuests(day);
        adventurerRoster.generateNewHeros();
        adventurerRoster.mergeOrSplitParties();
    }

    private void assignQuests() {
        List<Party> parties = adventurerRoster.getUnassignedParties();
        parties.forEach(party -> party.selectQuest(questBoard, day));
    }

    private void endDay() {
        questBoard.checkCompletedQuests(day);
        logger.info("{}  Active Parties: {}   Active Heros: {}", day, adventurerRoster.getParties().size(), adventurerRoster.getMembers().size());
        logger.info("Wealth Stats: {}", adventurerRoster.getMembers().stream().mapToInt(Adventurer::wealth).summaryStatistics());
        logger.info("Level Stats: {}", adventurerRoster.getMembers().stream().mapToInt(Adventurer::level).summaryStatistics());

        adventurerRoster.rest();
        day++;
    }
}
