package guild.quest;

import guild.adventurer.Adventurer;
import guild.adventurer.Party;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestTest {

    @Test
    void resolve() {
        Party p = new Party(
                new ArrayList<>(List.of(
                        Adventurer.randomise(0).build(),
                        Adventurer.randomise(0).build(),
                        Adventurer.randomise(0).build(),
                        Adventurer.randomise(0).build(),
                        Adventurer.randomise(0).build(),
                        Adventurer.randomise(0).build(),
                        Adventurer.randomise(0).build(),
                        Adventurer.randomise(0).build()
                )),// Enough to guarentee complete?
                null);
        Quest q = Quest.randomQuest().notDifficulty(QuestRank.HARD)
                .notDifficulty(QuestRank.LEGENDARY)
                .notDifficulty(QuestRank.NORMAL)
                .build();

        QuestBoard mockQB = mock(QuestBoard.class);
        when(mockQB.viewQuests()).thenReturn(List.of(q));
        p.selectQuest(mockQB, 1);
        assertTrue(q.isQuestAccepted());
        q.resolve(q.earliestAttemptDate());
        assertTrue(q.isCompleted());
    }

    @Nested
    class accept {
        @Test
        void acceptedTestsReportAsSuch() {
            Quest q = Quest.randomQuest().fromDate(1).build();
            assertFalse(q.isQuestAccepted());
            final Party.QuestPlan mock = mock(Party.QuestPlan.class);
            when(mock.completionDate()).thenReturn(2);
            assertTrue(q.accept(mock));
            assertTrue(q.isQuestAccepted());
            assertEquals(2, q.earliestAttemptDate());
        }

        @Test
        void acceptedTestsHandlesSameDaySuggestions_AndAttemptsAll() {
            Quest q = Quest.randomQuest().fromDate(1).build();
            assertFalse(q.isQuestAccepted());
            final Party.QuestPlan mock = mock(Party.QuestPlan.class);
            when(mock.completionDate()).thenReturn(2);
            assertTrue(q.accept(mock));
            assertTrue(q.accept(mock));
            assertTrue(q.accept(mock));
            assertTrue(q.accept(mock));
            assertTrue(q.isQuestAccepted());
            assertEquals(2, q.earliestAttemptDate());
            q.resolve(2);
            verify(mock, times(4)).attempt();
        }

        @Test
        void acceptedTestsHandlesSameDaySuggestions_AndAttemptsCorrect() {
            Quest q = Quest.randomQuest().fromDate(1).build();
            assertFalse(q.isQuestAccepted());
            final Party.QuestPlan mock = mock(Party.QuestPlan.class);
            when(mock.completionDate()).thenReturn(2);
            final Party.QuestPlan mockB = mock(Party.QuestPlan.class);
            when(mockB.completionDate()).thenReturn(3);
            assertTrue(q.accept(mock));
            assertTrue(q.accept(mock));
            assertTrue(q.accept(mockB));
            assertTrue(q.accept(mockB));
            assertTrue(q.isQuestAccepted());
            assertEquals(2, q.earliestAttemptDate());
            q.resolve(2);
            verify(mock, times(2)).attempt();
            verify(mockB, never()).attempt();
        }

        @Test
        void acceptedTestsRejectsExpiredPlans() {
            Quest q = Quest.randomQuest().fromDate(1).build();
            final Party.QuestPlan mock = mock(Party.QuestPlan.class);
            when(mock.completionDate()).thenReturn(2000);
            q.accept(mock);
            assertFalse(q.isQuestAccepted());
        }
    }
}