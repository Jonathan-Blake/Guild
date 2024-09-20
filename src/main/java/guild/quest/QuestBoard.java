package guild.quest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class QuestBoard {
    public static final int PREFERRED_QUEST_NUMBER = 15;
    private static final Logger logger = LoggerFactory.getLogger(QuestBoard.class);
    final List<Quest> quests;

    public QuestBoard() {
        quests = new ArrayList<>();
    }

    public void removeOldQuests(int day) {
        this.quests.removeIf(each -> {
            boolean b = each.expiryDate() < day && !each.isQuestAccepted();
            if (b) logger.info("{} expired.", each);
            else if (each.isCompleted() && !each.isQuestAccepted()) {
                logger.info("{} was Completed", each);
                b = true;
            }
            return b;
        });
    }

    public void generateNewQuests(int day) {
        int numberOfNewQuests = PREFERRED_QUEST_NUMBER - quests.stream().filter(quest -> !quest.isCompleted()).toList().size();
        long numberofHardQuests = quests.stream().filter(quest -> quest.difficulty() == QuestDifficulty.HARD).count();
        long numberofLegendaryQuests = quests.stream().filter(quest -> quest.difficulty() == QuestDifficulty.LEGENDARY).count();
        if (numberOfNewQuests > 0) {
            for (int i = 0; i < numberOfNewQuests; i++) {
                QuestBuilder qb = Quest.randomQuest().fromDate(day);
                if (numberofHardQuests >= 3) {
                    qb.notDifficulty(QuestDifficulty.HARD);
                }
                if (numberofLegendaryQuests >= 1) {
                    qb.notDifficulty(QuestDifficulty.LEGENDARY);
                }
                final Quest quest = qb.build();
                quests.add(quest);
                if (quest.difficulty() == QuestDifficulty.HARD) numberofHardQuests++;
                if (quest.difficulty() == QuestDifficulty.LEGENDARY) numberofLegendaryQuests++;
            }
        }
    }

    public void checkCompletedQuests(int day) {
        viewQuests().stream().filter(Quest::isQuestAccepted).forEach(quest -> quest.resolve(day));
    }

    public List<Quest> viewQuests() {
        return this.quests;
    }
}
