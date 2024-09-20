package guild.quest;

public enum QuestDifficulty {
    VERYEASY(3, 2, 1),
    NORMAL(5, 16, 3),
    HARD(10, 64, 6),
    LEGENDARY(30, 300, 12);

    private final int expectedDuration;
    private final int difficulty;
    private final int risk;

    QuestDifficulty(int expectedDuration, int difficulty, int risk) {
        this.expectedDuration = expectedDuration;
        this.difficulty = difficulty;
        this.risk = risk;
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
