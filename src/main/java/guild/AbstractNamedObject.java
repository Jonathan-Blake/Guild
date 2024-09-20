package guild;

import guild.util.ListUtil;
import guild.util.RandUtil;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractNamedObject {
    private static final EnumSet<ReplacementString> replacements = EnumSet.allOf(ReplacementString.class);

    private final String name;

    protected AbstractNamedObject() {
        var ref = new Object() {
            String temp = getNameTemplate();
        };
        AtomicInteger attempts = new AtomicInteger();
        while (replacements.stream().anyMatch(each -> ref.temp.contains(each.getSymbol()))) {
            replacements.stream().filter(each -> ref.temp.contains(each.getSymbol())).forEach(each -> {
                final String[] expansions = each.expansions();
                if (attempts.get() > 2) {
                    List<String> expansionsWithoutRecursion = ListUtil.removeIf(new ArrayList<>(List.of(each.expansions)), (replacementString -> replacementString.contains("[")));
                    if (expansionsWithoutRecursion.isEmpty()) {
                        ref.temp = ref.temp.replace(each.getSymbol(), RandUtil.pick(expansions));
                    } else {
                        ref.temp = ref.temp.replace(each.getSymbol(), RandUtil.pick(expansionsWithoutRecursion));
                    }
                } else {
                    ref.temp = ref.temp.replace(each.getSymbol(), RandUtil.pick(expansions));
                }
            });
            attempts.getAndIncrement();
        }
        name = ref.temp;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return super.toString() + " {" +
                "name='" + name + '\'' +
                '}';
    }

    public abstract String getNameTemplate();

    @SuppressWarnings("unused") // Enums are used internally based on the string
    public enum ReplacementString {
        PARTY("[GROUP_DESCRIPTOR] the [ITEM]", "[DEED]ers", "[GROUP_DESCRIPTOR] [NAME]"),
        ADVENTURER("[NAME] the [TITLE]", "[CREATURE_DESCRIPTOR] [NAME]", "[NAME]", "[NAME] of [LOCATION]"),
        QUEST("Exterminate [MONSTER_TARGET]", "Seek [ITEM]", "Escort [NAME] to [LOCATION]"),
        DEED("[MONSTER]-slay", "[ITEM] seek", "[LOCATION] seek"),

        MONSTER_TARGET("a single [MONSTER]", "the [GROUP_DESCRIPTOR] [MONSTER]s", "the Tribe of [MONSTER]s", "[LOCATION]'s [MONSTER] infestation"),
        MONSTER("Sewer Rat", "Goblin", "Minotaur", "[CREATURE_DESCRIPTOR] [MONSTER]"),

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
        OPTIONAL_PLURAL("", "s"),

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
