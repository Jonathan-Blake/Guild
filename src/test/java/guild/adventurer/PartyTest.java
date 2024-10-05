package guild.adventurer;

import guild.quest.Quest;
import guild.quest.QuestBoard;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PartyTest {

    @Test
    void partyConstructor_AddsAdventurersToParty() {
        final List<Adventurer> build = List.of(
                Adventurer.randomise(0).build(),
                Adventurer.randomise(0).build(),
                Adventurer.randomise(0).build(),
                Adventurer.randomise(0).build()
        );
        Party p = new Party(
                build,
                null);
        assertEquals(build.size(), p.members().size());
        p.members().forEach(each -> assertTrue(build.contains(each)));
        build.forEach(each -> assertSame(each.getParty(), p, each + " was not part of the party"));
    }

    @Test
    void selectQuest_DoesntError() {
        Party p = new Party(
                List.of(
                        Adventurer.randomise(0).build(),
                        Adventurer.randomise(0).build(),
                        Adventurer.randomise(0).build(),
                        Adventurer.randomise(0).build()
                ),
                null);
        QuestBoard q = new QuestBoard();
        q.generateNewQuests(0);
        p.selectQuest(q, 0);

        assertFalse(p.isUnassigned());
        assertEquals(1, q.viewQuests().stream().mapToInt(each -> {
            if (each.isQuestAccepted()) {
                System.out.println("Accepted Quest " + each + each.rank());
                return 1;
            }
            return 0;
        }).sum());
    }

    @Test
    void selectQuest_individual() {
        Party p = new Party(
                List.of(
                        Adventurer.randomise(0).build()
                ),
                null);
        QuestBoard q = new QuestBoard();
        q.generateNewQuests(0);
        p.selectQuest(q, 0);

        assertFalse(p.isUnassigned());
        assertEquals(1, q.viewQuests().stream().mapToInt(each -> each.isQuestAccepted() ? 1 : 0).sum());
    }

    @Nested
    class divideInjuries {
        @Test
        void divideInjuries_kills() {
            final Adventurer adventurer = Adventurer.randomise(0).build();
            final List<Adventurer> build = List.of(
                    adventurer
            );
            AdventurerRoster mockRoster = mock(AdventurerRoster.class);
            Party p = new Party(
                    build,
                    mockRoster);
            p.divideInjuries(4);
            verify(mockRoster).kill(adventurer);
        }

        @Test
        void divideInjuries_appliesDamage() {
            final Adventurer adventurer = Adventurer.randomise(0).build();
            final List<Adventurer> build = List.of(
                    adventurer
            );
            AdventurerRoster mockRoster = mock(AdventurerRoster.class);
            Party p = new Party(
                    build,
                    mockRoster);
            for (int i = 1; i < 4; i++) {
                p.divideInjuries(1);
                assertEquals(i, adventurer.injuriesSustained());
            }
        }

        @Test
        void divideInjuries_appliesCorrectTotalDamage() {
            final List<Adventurer> build = List.of(
                    Adventurer.randomise(0).build(),
                    Adventurer.randomise(0).build(),
                    Adventurer.randomise(0).build(),
                    Adventurer.randomise(0).build(),
                    Adventurer.randomise(0).build(),
                    Adventurer.randomise(0).build()
            );
            AdventurerRoster mockRoster = mock(AdventurerRoster.class);
            Party p = new Party(
                    new ArrayList<>(build),
                    mockRoster);
            p.divideInjuries(10);
            assertEquals(10, build.stream().mapToInt(Adventurer::injuriesSustained).sum());
        }

        @Test
        void divideInjuries_handlesOverkill() {
            final Adventurer adventurer = Adventurer.randomise(0).build();
            final List<Adventurer> build = new ArrayList<>(List.of(
                    adventurer
            ));
            AdventurerRoster mockRoster = new AdventurerRoster(null);
            Party p = new Party(
                    build,
                    mockRoster);
            p.divideInjuries(10);
            assertEquals(4, adventurer.injuriesSustained());
        }
    }

    @Nested
    class divideReward {
        @Test
        void partyOfOneSplit() {
            final Adventurer build = Adventurer.randomise(0).build();
            Party p = spy(new Party(List.of(build), null));
            when(p.currentQuest()).thenReturn(Quest.randomQuest().build());
            p.divideReward(50);
            assertEquals(50, build.wealth());
        }

        @Test
        void partyOfTwoSplit() {
            final Adventurer build = Adventurer.randomise(0).build();
            final Adventurer build2 = Adventurer.randomise(0).build();
            Party p = spy(new Party(new ArrayList<>(List.of(build, build2)), null));
            when(p.currentQuest()).thenReturn(Quest.randomQuest().build());
            p.divideReward(50);
            assertEquals(25, build.wealth());
            assertEquals(25, build2.wealth());
        }

        @Test
        void partyOfThreeSplit() {
            final Adventurer build = Adventurer.randomise(0).build();
            final Adventurer build2 = Adventurer.randomise(0).build();
            final Adventurer build3 = Adventurer.randomise(0).build();
            Party p = spy(new Party(new ArrayList<>(List.of(build, build2, build3)), null));
            when(p.currentQuest()).thenReturn(Quest.randomQuest().build());
            p.divideReward(50);
            assertEquals(17, build.wealth());
            assertEquals(17, build2.wealth());
            assertEquals(16, build3.wealth());
        }

        @Test
        void partyOfThree_balancesOverTime() {
            final Adventurer build = Adventurer.randomise(0).build();
            final Adventurer build2 = Adventurer.randomise(0).build();
            final Adventurer build3 = Adventurer.randomise(0).build();
            Party p = spy(new Party(new ArrayList<>(List.of(build, build2, build3)), null));
            when(p.currentQuest()).thenReturn(Quest.randomQuest().build());
            p.divideReward(4);
            p.divideReward(4);
            p.divideReward(4);
            assertEquals(4, build.wealth());
            assertEquals(4, build2.wealth());
            assertEquals(4, build3.wealth());
        }
    }

    @Test
    void voteOnDay() {
        Adventurer m1 = mock(Adventurer.class);
        when(m1.preferredDayActivity()).thenReturn("1");
        Adventurer m2 = mock(Adventurer.class);
        when(m2.preferredDayActivity()).thenReturn("2");
        Adventurer m3 = mock(Adventurer.class);
        when(m3.preferredDayActivity()).thenReturn("3");
        Adventurer m4 = mock(Adventurer.class);
        when(m4.preferredDayActivity()).thenReturn("3");
        Party p = new Party(List.of(m1, m2, m3, m4), null);
        assertEquals("3", p.voteOnDay());
        List.of(m1, m2, m3, m4).forEach(each -> verify(each).preferredDayActivity());
    }
}