package guild.quest;

import guild.adventurer.Adventurer;
import guild.adventurer.Party;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RecurringQuestTest {

    @Test
    void recurringQuestsCanBeCompletedByMultipleParties() {
        List<Party> partyList = List.of(
                new Party(
                        new ArrayList<>(List.of(
                                Adventurer.randomise(0).build()
                        )),
                        null),
                new Party(
                        new ArrayList<>(List.of(
                                Adventurer.randomise(0).build()
                        )),
                        null),
                new Party(
                        new ArrayList<>(List.of(
                                Adventurer.randomise(0).build()
                        )),
                        null),
                new Party(
                        new ArrayList<>(List.of(
                                Adventurer.randomise(0).build()
                        )),
                        null));
        partyList.forEach(each -> each.members().forEach(member -> member.gainExp(13)));
        Quest q = Quest.randomQuest().difficulty(QuestRank.VERYEASY).recurring(5)
                .build();

        QuestBoard mockQB = mock(QuestBoard.class);
        when(mockQB.viewQuests()).thenReturn(List.of(q));
        partyList.forEach(each -> each.selectQuest(mockQB, 1));
        assertTrue(q.isQuestAccepted());
        partyList.forEach(each -> assertEquals(q, each.currentQuest()));
        q.resolve(q.earliestAttemptDate());
        assertFalse(q.isCompleted());

        partyList.forEach(each -> assertNull(each.currentQuest()));
        assertEquals(q.reward(), partyList.get(0).members().get(0).wealth());
        partyList.stream()
                .flatMap(each -> each.members().stream())
                //Either success or Injuries
                .peek(each -> System.out.println(each + " " + each.getParty() + " " + each.wealth() + " " + each.injuriesSustained()))
                .forEach(member -> assertTrue((member.wealth() > 0 || member.injuriesSustained() != 0)));
    }

    @Test
    void recurringQuestsNotCompletedOnFirstAttempt() {
        Party p = new Party(
                new ArrayList<>(List.of(
                        Adventurer.randomise(0).build(),
                        Adventurer.randomise(0).build(),
                        Adventurer.randomise(0).build(),
                        Adventurer.randomise(0).build()
                )),
                null);
        Quest q = Quest.randomQuest()
                .difficulty(QuestRank.VERYEASY)
                .recurring(5)
                .build();
        int oldReward = q.reward();

        QuestBoard mockQB = mock(QuestBoard.class);
        when(mockQB.viewQuests()).thenReturn(List.of(q));
        p.selectQuest(mockQB, 0);
        assertTrue(q.isQuestAccepted());
        assertEquals(q, p.currentQuest());
        q.resolve(q.earliestAttemptDate());
        assertFalse(q.isCompleted());

        assertNull(p.currentQuest());
        assertEquals(oldReward, q.reward());
        assertEquals(q.reward(), p.members().stream().mapToInt(Adventurer::wealth).sum());
    }
}