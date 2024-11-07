package guild.names;

import guild.util.ListUtil;
import guild.util.RandUtil;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class BasicNamedObject extends BaseNamedObject {
    private static final EnumSet<ReplacementString> replacements = EnumSet.allOf(ReplacementString.class);

    public static String replaceTemplatedStrings(String temp, Integer attempts, ReplacementString each) {
        List<String> expansions = new ArrayList<>(List.of(each.expansions()));
        if (attempts > 2) {
            List<String> expansionsWithoutRecursion = ListUtil.removeIf(expansions, (replacementString -> replacementString.contains("[")));
            if (!expansionsWithoutRecursion.isEmpty()) {
                expansions = expansionsWithoutRecursion;
            }
        }
        temp = temp.replace(each.getSymbol(), RandUtil.pick(expansions));
        return temp;
    }

    public static String generateString(ReplacementString template) {
        return new WeightedNamedObject() {

            @Override
            public String getNameTemplate() {
                return template.getSymbol();
            }
        }.getName();
    }

    @Override
    protected String initName() {
        var ref = new Object() {
            String temp = getNameTemplate();
        };
        AtomicInteger attempts = new AtomicInteger();
        while (ref.temp.contains("[")) {
            for (ReplacementString each : replacements) {
                if (ref.temp.contains(each.getSymbol())) {
                    ref.temp = replaceTemplatedStrings(ref.temp, attempts.get(), each);
                }
            }
            attempts.getAndIncrement();
        }
        return ref.temp;
    }


    @SuppressWarnings("unused") // Enums are used internally based on the string
    public enum ReplacementString {
        PARTY("[GROUP_DESCRIPTOR] the [ITEM]", "[DEED]ers", "[GROUP_DESCRIPTOR] [NAME]"),
        ADVENTURER("[NAME] the [TITLE]", "[CREATURE_DESCRIPTOR] [NAME]", "[NAME]", "[NAME] of [LOCATION]"),
        QUEST("Exterminate [MONSTER_TARGET]", "Seek [ITEM]", "Escort [NAME] to [LOCATION]"),
        RECURRING_QUEST("Patrol [LOCATION] for [MONSTER]s", "Explore the [DUNGEON_NAME]", "Gather [INGREDIENTS] for [NAME]"),
        DEED("[MONSTER]-slay", "[ITEM] seek", "[LOCATION] seek"),

        DUNGEON_NAME("[DUNGEON_TYPE] of [NAME_OR_LOCATION]", "[LOCATION]'s [DUNGEON_TYPE]", "[MONSTER] filled [DUNGEON_TYPE]"),
        DUNGEON_TYPE("Tomb", "Caves", "Dungeon", "Barrow"),

        INGREDIENTS("[MONSTER] parts", "Magical Herbs", "Magical Ores", "Lumber", "[INGREDIENTS] and [INGREDIENTS]"),

        MONSTER_TARGET("a single [MONSTER]", "the [GROUP_DESCRIPTOR] [MONSTER]s", "the Tribe of [MONSTER]s", "[LOCATION]'s [MONSTER] infestation"),
        MONSTER("Sewer Rat", "Goblin", "Minotaur", "[CREATURE_DESCRIPTOR] [MONSTER]"),

        NAME_OR_LOCATION("[NAME]", "[LOCATION]"),

        LOCATION("[LOCATION_PREFIX][LOCATION_SUFFIX]"),
        LOCATION_PREFIX("Bleak", "Storm", "Mourn", "Grim", "Lost", "Frost", "New", "Thorn", "Stone", "Rock",
                Constants.COLOUR_DESCRIPTOR_STRING, "[ITEM_TYPE]", "Great", "Wood", "Long", "Low", "High", "Wolf", "Raven"),
        LOCATION_SUFFIX("moor", "ford", "crag", "watch", "hope", "wood", "ridge", "stone",
                "haven", "fall[OPTIONAL_PLURAL]", "river", "field", "hill", "bridge", "mark", "cairn", "land", "hall",
                "mount", "rock", "brook", "barrow", "stead", "home", "wick", "fjord", "valley", "heights"),


        CREATURE_DESCRIPTOR("Angry", "Medium Sized", Constants.COLOUR_DESCRIPTOR_STRING, "Bookish"),
        GROUP_DESCRIPTOR("Hordes of", "Warband of", "Party of", "Fellowship of"),

        ITEM("[ITEM_DESCRIPTOR] [ITEM_TYPE]"),
        ITEM_DESCRIPTOR("[MATERIAL_DESCRIPTOR]", "Priceless", "Large", "Stolen"),
        MATERIAL_DESCRIPTOR("Golden", "Copper", "Wood", "Gem Covered"),
        ITEM_TYPE("Hammer", "Book", "Axe", "Sword", "Shield"),

        TITLE("[CREATURE_DESCRIPTOR]", "Baron", "Hated", "[PROFESSION]", "Child of [NAME]"),
        NAME("[HUMAN_NAME]", "[ELF_NAME]", "[DWARF_NAME]", "Frank"),
        PROFESSION("Blacksmith", "Adventurer", "Scout"),
        HUMAN_NAME("Bob", "Gill"),
        ELF_NAME("Pointy eared [NAME]", "Legolas"),
        DWARF_NAME("[CREATURE_DESCRIPTOR] Gimli"),

        COLOUR(Constants.COLOUR_DESCRIPTOR_STRING, "[COLOUR_DESCRIPTOR]ish", "[COLOUR_DESCRIPTOR]-[COLOUR]"),// Not Used
        COLOUR_DESCRIPTOR("Green", "Gray", "Red", "White", "Black", "Blue"),
        OPTIONAL_PLURAL("", "s")

        ;


        private final String[] expansions;
        private final String symbol;

        ReplacementString(String... expansions) {
            this.expansions = expansions;
            this.symbol = "[" + this + "]";
        }

        public String[] expansions() {
            return expansions;
        }

        public String getSymbol() {
            return symbol;
        }

        private static class Constants {
            public static final String COLOUR_DESCRIPTOR_STRING = "[COLOUR_DESCRIPTOR]";
        }
    }
}
