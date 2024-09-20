package guild.quest;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestBoardTest {

    @Test
    void removeOldQuests() {
        QuestBoard questBoard = new QuestBoard();
        questBoard.generateNewQuests(0);
        questBoard.removeOldQuests(100);
        assertTrue(questBoard.quests.isEmpty());
    }

    @Nested
    class generateNewQuests {
        private final int day = 0;

        @Test
        void createsCorrectNumber() {
            QuestBoard questBoard = new QuestBoard();
            questBoard.generateNewQuests(day);
            assertEquals(15, questBoard.quests.size());
        }

        @Test
        void createsQuestsDueTomorrowAtLeast() {
            QuestBoard questBoard = new QuestBoard();
            questBoard.generateNewQuests(day);
            assertTrue(questBoard.quests.stream()
                    .peek(each -> System.out.println(each + " is due " + each.expiryDate()))
                    .allMatch(each -> each.expiryDate > day), "Expected no quests to be due on " + day);
        }
    }
}