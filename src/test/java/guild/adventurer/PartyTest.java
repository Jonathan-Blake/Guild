package guild.adventurer;

import guild.quest.QuestBoard;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class PartyTest {

    @Test
    void partyConstructor_AddsAdventurersToParty() {
        final List<Adventurer> build = List.of(
                Adventurer.randomise().build(),
                Adventurer.randomise().build(),
                Adventurer.randomise().build(),
                Adventurer.randomise().build()
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
                        Adventurer.randomise().build(),
                        Adventurer.randomise().build(),
                        Adventurer.randomise().build(),
                        Adventurer.randomise().build()
                ),
                null);
        QuestBoard q = new QuestBoard();
        q.generateNewQuests(0);
        p.selectQuest(q, 0);

        assertFalse(p.isUnassigned());
        assertEquals(1, q.viewQuests().stream().mapToInt(each -> {
            if (each.isQuestAccepted()) {
                System.out.println("Accepted Quest " + each + each.difficulty());
                return 1;
            }
            return 0;
        }).sum());
    }

    @Test
    void selectQuest_individual() {
        Party p = new Party(
                List.of(
                        Adventurer.randomise().build()
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
            final Adventurer adventurer = Adventurer.randomise().build();
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
            final Adventurer adventurer = Adventurer.randomise().build();
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
                    Adventurer.randomise().build(),
                    Adventurer.randomise().build(),
                    Adventurer.randomise().build(),
                    Adventurer.randomise().build(),
                    Adventurer.randomise().build(),
                    Adventurer.randomise().build()
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
            final Adventurer adventurer = Adventurer.randomise().build();
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
            final Adventurer build = Adventurer.randomise().build();
            Party p = new Party(List.of(build), null);
            p.divideReward(50);
            assertEquals(50, build.wealth());
        }

        @Test
        void partyOfTwoSplit() {
            final Adventurer build = Adventurer.randomise().build();
            final Adventurer build2 = Adventurer.randomise().build();
            Party p = new Party(List.of(build, build2), null);
            p.divideReward(50);
            assertEquals(25, build.wealth());
            assertEquals(25, build2.wealth());
        }

        @Test
        void partyOfThreeSplit() {
            final Adventurer build = Adventurer.randomise().build();
            final Adventurer build2 = Adventurer.randomise().build();
            final Adventurer build3 = Adventurer.randomise().build();
            Party p = new Party(new ArrayList<>(List.of(build, build2, build3)), null);
            p.divideReward(50);
            assertEquals(17, build.wealth());
            assertEquals(17, build2.wealth());
            assertEquals(16, build3.wealth());
        }

        @Test
        void partyOfThree_balancesOverTime() {
            final Adventurer build = Adventurer.randomise().build();
            final Adventurer build2 = Adventurer.randomise().build();
            final Adventurer build3 = Adventurer.randomise().build();
            Party p = new Party(new ArrayList<>(List.of(build, build2, build3)), null);
            p.divideReward(4);
            p.divideReward(4);
            p.divideReward(4);
            assertEquals(4, build.wealth());
            assertEquals(4, build2.wealth());
            assertEquals(4, build3.wealth());
        }
    }
}