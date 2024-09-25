package guild.adventurer;

import guild.quest.QuestRank;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class AdventurerTest {

    @Nested
    class wealth {
        @Test
        void gainsWealthCorrectly() {
            Adventurer a = Adventurer.randomise(0).build();
            assertEquals(0, a.wealth());
            a.gainReward(10);
            assertEquals(10, a.wealth());
        }

        @Test
        void chargeReducesWealth() {
            Adventurer a = Adventurer.randomise(0).build();
            a.gainReward(10);
            assertEquals(10, a.wealth());
            assertTrue(a.charge(10));
            assertEquals(0, a.wealth());
            a.gainReward(15);
            assertEquals(15, a.wealth());
            assertTrue(a.charge(10));
            assertEquals(5, a.wealth());
        }

        @Test
        void chargeNeverGoesNegative() {
            Adventurer a = Adventurer.randomise(0).build();
            assertEquals(0, a.wealth());
            assertFalse(a.charge(10));
            assertEquals(0, a.wealth());
            a.gainReward(9);
            assertFalse(a.charge(10));
            assertEquals(9, a.wealth());
        }
    }

    @Nested
    class exp {
        @Test
        void testExpGainLevels_levelsWith4Exp() {
            Adventurer a = Adventurer.randomise(0).build();
            assertEquals(1, a.level());
            a.gainExp(4);
            assertEquals(2, a.level());
        }

        @Test
        void testExpGainLevels_levelsWith4Exp_SmallIncrements() {
            Adventurer a = Adventurer.randomise(0).build();
            for (int i = 0; i < 4; i++) {
                assertEquals(1, a.level());
                a.gainExp(1);
            }
            assertEquals(2, a.level());
        }

        @Test
        void testExpGainHigherLevels() {
            Adventurer a = Adventurer.randomise(0).build();
            assertEquals(1, a.level());
            a.gainExp(4);
            assertEquals(2, a.level());
            a.gainExp(8);
            assertEquals(3, a.level());
            a.gainExp(12);
            assertEquals(4, a.level());
        }

        @Test
        void testExpGainOverflow() {
            Adventurer a = Adventurer.randomise(0).build();
            assertEquals(1, a.level());
            a.gainExp(24);
            assertEquals(4, a.level());
        }
    }

    @Nested
    class injuries {
        private static final short UNINJURED = 0;
        private static final short LIGHT_INJURIES = 1;
        private static final short MODERATE_INJURIES = 2;
        private static final short SEVERE_INJURIES = 3;

        @Test
        void injure() {
            Adventurer a = Adventurer.randomise(0).build();
            assertFalse(a.isDead());
            a.injure();
            assertFalse(a.isDead());
            a.injure();
            assertFalse(a.isDead());
            a.injure();
            assertFalse(a.isDead());
            a.injure();
            assertTrue(a.isDead());
        }

        @Test
        void singleInjuryHealing() {

            Adventurer a = Adventurer.randomise(0).build();
            a.injure();
            for (int i = 0; i < 4; i++) {
                assertEquals(LIGHT_INJURIES, a.injuriesSustained());
                a.rest();
            }
            assertEquals(UNINJURED, a.injuriesSustained());
        }

        @Test
        void moderateInjuryHealing() {
            Adventurer a = Adventurer.randomise(0).build();
            a.injure();
            a.injure();
            for (int i = 0; i < 12; i++) {
                assertEquals(MODERATE_INJURIES, a.injuriesSustained());
                a.rest();
            }
            for (int i = 0; i < 4; i++) {
                assertEquals(LIGHT_INJURIES, a.injuriesSustained());
                a.rest();
            }
            assertEquals(UNINJURED, a.injuriesSustained());
        }

        @Test
        void severeInjuryHealing() {
            Adventurer a = Adventurer.randomise(0).build();
            a.injure();
            a.injure();
            a.injure();
            for (int i = 0; i < 24; i++) {
                assertEquals(SEVERE_INJURIES, a.injuriesSustained());
                a.rest();
            }
            for (int i = 0; i < 8; i++) {
                assertEquals(MODERATE_INJURIES, a.injuriesSustained());
                a.rest();
            }
            for (int i = 0; i < 4; i++) {
                assertEquals(LIGHT_INJURIES, a.injuriesSustained());
                a.rest();
            }
            assertEquals(UNINJURED, a.injuriesSustained());
        }
    }

    @Nested
    class wouldAccept {
        @Test
        void wouldAcceptSolo_Easy() {
            assertTrue(Adventurer.randomise(0)
                    .preferredDifficulty(QuestRank.VERYEASY)
                    .build().wouldAccept(List.of()));
        }

        @Test
        void wouldNotAcceptSolo_Hard() {
            assertFalse(Adventurer.randomise(0)
                    .preferredDifficulty(QuestRank.NORMAL)
                    .build().wouldAccept(List.of()));
        }

        @Test
        void wouldAcceptDuo_Normal() {
            final Adventurer adventurer = Adventurer.randomise(0).preferredDifficulty(QuestRank.NORMAL).build();
            assertFalse(adventurer.wouldAccept(List.of()));
            assertTrue(adventurer.wouldAccept(List.of(adventurer)));
        }

        @Test
        void willEventuallyAcceptAnUnwantedParty() {
            Adventurer a = Adventurer.randomise(0)
                    .preferredDifficulty(QuestRank.NORMAL)
                    .build();
            for (int i = 0; i < 12; i++) {
                a.increaseDesperation();
            }
            assertTrue(a.wouldAccept(List.of()));
        }

        @Test
        void resetsDesperationInAParty() {
            Adventurer a = Adventurer.randomise(0)
                    .preferredDifficulty(QuestRank.NORMAL)
                    .build();
            for (int i = 0; i < 12; i++) {
                a.increaseDesperation();
            }
            final Party party = mock(Party.class);
            a.setParty(party);
            assertEquals(party, a.getParty());
            assertEquals(0, a.partyDesperation);
        }
    }
}