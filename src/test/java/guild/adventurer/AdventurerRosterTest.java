package guild.adventurer;

import guild.Guild;
import guild.quest.QuestRank;
import guild.util.RandUtil;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdventurerRosterTest {
    @Mock
    private Guild mockGuild;

    @Test
    void assembleParty() {
        AdventurerRoster roster = (new AdventurerRoster(mockGuild));
        final Adventurer hardGuy = Adventurer.randomise(0).preferredDifficulty(QuestRank.HARD).build();
        List<Adventurer> allAdventurers = new ArrayList<>(List.of(
                hardGuy,
                Adventurer.randomise(0).build(),
                Adventurer.randomise(0).build(),
                Adventurer.randomise(0).build(),
                Adventurer.randomise(0).build()
        ));
        assertTrue(roster.assembleParty(new ArrayList<>(), allAdventurers));
        assertEquals(1, roster.getParties().size());
        assertFalse(roster.getParties().get(0).members().contains(hardGuy));
    }

    @Nested
    class partyMembership {

        @Test
        @Disabled("Small stuff")
        void tpk() {
        }

        @Test
        void killRemovesMembersFromParties() {
            AdventurerRoster roster = (new AdventurerRoster(mockGuild));
            roster.generateNewHeros(0);
            roster.mergeOrSplitParties();

            Party party = RandUtil.pick(roster.getParties());

            ArrayList<Adventurer> finalMembers = new ArrayList<>(party.members());
            for (Adventurer member : finalMembers) {
                roster.kill(member);
                assertFalse(party.members().contains(member));
                assertNull(member.getParty());
                assertFalse(roster.getMembers().contains(member));
            }
        }

        @Test
        void killDisbandsEmptyParties() {
            AdventurerRoster roster = (new AdventurerRoster(mockGuild));
            roster.generateNewHeros(0);
            roster.mergeOrSplitParties();

            Party party = RandUtil.pick(roster.getParties());

            ArrayList<Adventurer> finalMembers = new ArrayList<>(party.members());
            for (Adventurer member : finalMembers) {
                roster.kill(member);
            }
            assertTrue(party.members().isEmpty());
            assertFalse(roster.getParties().contains(party));
        }

        @Test
        void disband() {
            AdventurerRoster roster = (new AdventurerRoster(mockGuild));
            roster.generateNewHeros(0);
            roster.mergeOrSplitParties();

            Party party = RandUtil.pick(roster.getParties());
            ArrayList<Adventurer> finalMembers = new ArrayList<>(party.members());

            roster.disband(party);

            assertFalse(roster.getParties().contains(party));
            finalMembers.forEach(adventurer -> assertNull(adventurer.getParty()));
            assertTrue(party.members().isEmpty());
        }
    }

    @Nested
    class rest {
        @Test
        void rest_CallsRestMethodOnAdventurers() {
            AdventurerRoster roster = spy(new AdventurerRoster(mockGuild));
            List<Adventurer> mockAdventurers = List.of(mock(Adventurer.class),
                    mock(Adventurer.class), mock(Adventurer.class), mock(Adventurer.class),
                    mock(Adventurer.class), mock(Adventurer.class), mock(Adventurer.class));
            when(roster.getMembers()).thenReturn(
                    mockAdventurers
            );
            assertTrue(roster.getMembers().stream().allMatch(adventurer -> adventurer.injuriesSustained() == 0));

            roster.rest();
            mockAdventurers.forEach(adventurer -> verify(adventurer, times(1)).rest());
        }
    }

    @Nested
    class collectDues {
        @Test
        void allAdventurersQuitWhenTheyHaveNoMoney() {
            AdventurerRoster roster = (new AdventurerRoster(mockGuild));
            roster.generateNewHeros(0);
            roster.mergeOrSplitParties();

            assertTrue(roster.getMembers().stream().allMatch(each -> each.wealth() == 0));

            assertEquals(0, roster.collectDues(100, 1));

            assertTrue(roster.getParties().isEmpty());
        }

        @Test
        void adventurersAreFilteredIfDuesArentDue() {
            AdventurerRoster roster = (new AdventurerRoster(mockGuild));
            roster.generateNewHeros(0);
            roster.mergeOrSplitParties();
            ArrayList<Party> oldParties = new ArrayList<>(roster.getParties());
            ArrayList<Adventurer> oldMembers = new ArrayList<>(roster.getMembers());

            assertTrue(roster.getMembers().stream().allMatch(each -> each.getDuesDateOwed() == 14));

            assertEquals(0, roster.collectDues(13, 1));

            assertEquals(oldParties, roster.getParties());
            assertEquals(oldMembers, roster.getMembers());
        }

        @Test
        void adventurersPayExpectedAmount() {
            AdventurerRoster roster = (new AdventurerRoster(mockGuild));
            roster.generateNewHeros(0);
            roster.mergeOrSplitParties();
            ArrayList<Party> oldParties = new ArrayList<>(roster.getParties());
            ArrayList<Adventurer> oldMembers = new ArrayList<>(roster.getMembers());
            oldMembers.forEach(each -> each.gainReward(50));

            assertTrue(roster.getMembers().stream().allMatch(each -> each.getDuesDateOwed() == 14));

            assertEquals(oldParties.stream().mapToLong(each -> each.members().size()).sum(), roster.collectDues(15, 1));

            assertEquals(oldParties, roster.getParties());
            assertEquals(oldMembers, roster.getMembers());
            assertTrue(roster.getMembers().stream().allMatch(each -> each.outOfParty() || each.wealth() == 49));
            // This logic may change if I charge members out of party money
        }
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
            roster.generateNewHeros(0);
            roster.generateNewHeros(0);
            roster.mergeOrSplitParties();
            assertFalse(roster.getParties().isEmpty());
        }

        @Test
//        @Disabled("Trying to think of an elegant way to test this")
        void splitParties_Disband() {
            AdventurerRoster roster = spy(new AdventurerRoster(mockGuild));
            Supplier<Adventurer> buildAdventurer = () -> {
                Adventurer ret = mock(Adventurer.class);
                when(ret.wouldAccept(anyList())).thenReturn(true).thenReturn(false);

                doCallRealMethod().when(ret).setParty(any());
                when(ret.getParty()).thenCallRealMethod();
                when(ret.outOfParty()).thenCallRealMethod();

                return ret;
            };
            List<Adventurer> mockList = List.of(
                    buildAdventurer.get()
            );
            when(roster.getMembers()).thenReturn(mockList);
            roster.mergeOrSplitParties();
            assertEquals(1, roster.getParties().size());
            roster.mergeOrSplitParties();
            assertEquals(0, roster.getParties().size());
        }

        @Test
        @Disabled("Used for manual analysis")
        void consistencyTest() {
            LongSummaryStatistics result = IntStream.range(0, 100).mapToObj(i -> new AdventurerRoster(mockGuild)).mapToLong(roster -> {
                roster.generateNewHeros(0);
                roster.generateNewHeros(0);
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
                roster.generateNewHeros(0);
            }
            assertEquals(20, roster.heroCount());
        }

        @Test
        void generateHeroes_caps() {
            AdventurerRoster roster = new AdventurerRoster(mockGuild);
            for (int i = 0; i < 25; i++) {
                roster.generateNewHeros(0);
            }
            assertEquals(40, roster.heroCount());
        }
    }
}