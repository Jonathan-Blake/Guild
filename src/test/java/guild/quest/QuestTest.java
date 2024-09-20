package guild.quest;

import guild.adventurer.Adventurer;
import guild.adventurer.Party;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestTest {

    @Test
    void resolve() {
        Party p = new Party(
                new ArrayList<>(List.of(
                        Adventurer.randomise().build(),
                        Adventurer.randomise().build(),
                        Adventurer.randomise().build(),
                        Adventurer.randomise().build(),
                        Adventurer.randomise().build(),
                        Adventurer.randomise().build(),
                        Adventurer.randomise().build(),
                        Adventurer.randomise().build()
                )),// Enough to guarentee complete?
                null);
        Quest q = Quest.randomQuest().notDifficulty(QuestDifficulty.HARD).notDifficulty(QuestDifficulty.LEGENDARY).notDifficulty(QuestDifficulty.NORMAL).build();

        QuestBoard mockQB = mock(QuestBoard.class);
        when(mockQB.viewQuests()).thenReturn(List.of(q));
        p.selectQuest(mockQB, 1);
        assertTrue(q.isQuestAccepted());
        q.resolve(q.earliestAttemptDate());
        assertTrue(q.isCompleted());
    }
}