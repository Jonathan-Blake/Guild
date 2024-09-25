package guild.adventurer;

import guild.quest.QuestRank;
import guild.util.RandUtil;

import java.util.EnumSet;

public class AdventurerBuilder {
    private final AdventurePreferences preferences;

    public AdventurerBuilder(int date) {
        preferences = new AdventurePreferences();
        preferences.creationDate = date;
    }

    public AdventurerBuilder preferredDifficulty(QuestRank difficulty) {
        preferences.difficulty = difficulty;
        return this;
    }

    public Adventurer build() {
        if (preferences.difficulty == null) {
            preferences.difficulty = RandUtil.pick(EnumSet.complementOf(EnumSet.of(QuestRank.HARD, QuestRank.LEGENDARY)));
        }
        return new Adventurer(preferences);
    }
}
