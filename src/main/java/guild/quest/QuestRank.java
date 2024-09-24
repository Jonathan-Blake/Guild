package guild.quest;

import java.util.List;

public enum QuestRank {
    VERYEASY(3, 2, 1),
    NORMAL(5, 16, 3),
    HARD(10, 64, 6),
    LEGENDARY(30, 300, 12);

    private final int expectedDuration;
    private final int difficulty;
    private final int risk;

    QuestRank(int expectedDuration, int difficulty, int risk) {
        this.expectedDuration = expectedDuration;
        this.difficulty = difficulty;
        this.risk = risk;
    }

    public static List<String> getMonstersForRank(QuestRank rank) {
        return switch (rank) {
            case VERYEASY -> List.of("Sewer Rat", "Goblin", "Slime");
            case NORMAL -> List.of("Goblin", "Gnoll", "Skeleton");
            case HARD -> List.of("Gnoll", "Vampire", "Demi Lich");
            case LEGENDARY -> List.of("Dragon", "Lich");
        };
    }

    public static List<String> getItemsForRank(QuestRank rank) {
        return switch (rank) {
            case VERYEASY, NORMAL -> List.of("Broken", "Iron", "[NAME]'s", "Lost");
            case HARD, LEGENDARY -> List.of("[ITEM_DESCRIPTOR] [ITEM_DESCRIPTOR]", "Huge");
        };
    }

    public static List<String> getMaterialsForRank(QuestRank rank) {
        return switch (rank) {
            case VERYEASY, NORMAL -> List.of("Iron");
            case HARD, LEGENDARY -> List.of("[MATERIAL_DESCRIPTOR] [ITEM_DESCRIPTOR]", "Sapphire", "Rare");
        };
    }

    public static List<String> getGroupForRank(QuestRank rank) {
        return switch (rank) {
            case VERYEASY, NORMAL -> List.of("", "the", "several");
            case HARD, LEGENDARY -> List.of("many");
        };
    }

    public int getExpectedLength() {
        return expectedDuration;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public int injuries() {
        return risk;
    }
}
