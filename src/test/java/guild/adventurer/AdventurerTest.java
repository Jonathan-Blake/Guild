package guild.adventurer;

import guild.quest.QuestDifficulty;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AdventurerTest {

    @Nested
    class injuries {
        private static final short UNINJURED = 0;
        private static final short LIGHT_INJURIES = 1;
        private static final short MODERATE_INJURIES = 2;
        private static final short SEVERE_INJURIES = 3;

        @Test
        void injure() {
            Adventurer a = Adventurer.randomise().build();
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

            Adventurer a = Adventurer.randomise().build();
            a.injure();
            for (int i = 0; i < 4; i++) {
                assertEquals(LIGHT_INJURIES, a.injuriesSustained());
                a.rest();
            }
            assertEquals(UNINJURED, a.injuriesSustained());
        }

        @Test
        void moderateInjuryHealing() {
            Adventurer a = Adventurer.randomise().build();
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
            Adventurer a = Adventurer.randomise().build();
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
            assertTrue(Adventurer.randomise()
                    .preferredDifficulty(QuestDifficulty.VERYEASY)
                    .build().wouldAccept(List.of()));
        }

        @Test
        void wouldNotAcceptSolo_Hard() {
            assertFalse(Adventurer.randomise()
                    .preferredDifficulty(QuestDifficulty.NORMAL)
                    .build().wouldAccept(List.of()));
        }

        @Test
        void wouldAcceptDuo_Normal() {
            final Adventurer adventurer = Adventurer.randomise().preferredDifficulty(QuestDifficulty.NORMAL).build();
            assertFalse(adventurer.wouldAccept(List.of()));
            assertTrue(adventurer.wouldAccept(List.of(adventurer)));
        }
    }
}