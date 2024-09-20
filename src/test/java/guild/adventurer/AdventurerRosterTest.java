package guild.adventurer;

import guild.Guild;
import guild.quest.QuestDifficulty;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.spy;

class AdventurerRosterTest {
    @Mock
    private Guild mockGuild;

    @Test
    void assembleParty() {
        AdventurerRoster roster = spy(new AdventurerRoster(mockGuild));
        final Adventurer hardGuy = Adventurer.randomise().preferredDifficulty(QuestDifficulty.HARD).build();
        List<Adventurer> allAdventurers = new ArrayList<>(List.of(
                hardGuy,
                Adventurer.randomise().build(),
                Adventurer.randomise().build(),
                Adventurer.randomise().build(),
                Adventurer.randomise().build()
        ));
        assertTrue(roster.assembleParty(new ArrayList<>(), allAdventurers));
        assertEquals(1, roster.getParties().size());
        assertFalse(roster.getParties().get(0).members().contains(hardGuy));
    }

    @Nested
    class mergeOrSplitParties {
        @Test
        void splitParties_doesntErrorWhenEmpty() {
            AdventurerRoster roster = new AdventurerRoster(mockGuild);
            assertDoesNotThrow(roster::mergeOrSplitParties);
        }

        @Test
        void splitParties_() {
            AdventurerRoster roster = new AdventurerRoster(mockGuild);
            roster.generateNewHeros();
            roster.generateNewHeros();
            roster.mergeOrSplitParties();
            assertFalse(roster.getParties().isEmpty());
        }

        @Test
        @Disabled("Used for manual analysis")
        void consistencyTest() {
            LongSummaryStatistics result = IntStream.range(0, 100).mapToObj(i -> new AdventurerRoster(mockGuild)).mapToLong(roster -> {
                roster.generateNewHeros();
                roster.generateNewHeros();
                roster.mergeOrSplitParties();
                return roster.unpartiedHeroesCount();
            }).summaryStatistics();
            System.out.println(result);
        }
    }

    @Nested
    class generateNewHeroes {
        @Test
        void generateHeroes_initial() {
            AdventurerRoster roster = new AdventurerRoster(mockGuild);
            for (int i = 0; i < 2; i++) {
                roster.generateNewHeros();
            }
            assertEquals(20, roster.heroCount());
        }

        @Test
        void generateHeroes_caps() {
            AdventurerRoster roster = new AdventurerRoster(mockGuild);
            for (int i = 0; i < 25; i++) {
                roster.generateNewHeros();
            }
            assertEquals(40, roster.heroCount());
        }
    }
}