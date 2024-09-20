package guild.adventurer;

import guild.quest.QuestDifficulty;
import guild.util.RandUtil;

import java.util.EnumSet;

public class AdventurerBuilder {
    private final AdventurePreferences preferences = new AdventurePreferences();

    public AdventurerBuilder preferredDifficulty(QuestDifficulty difficulty) {
        preferences.difficulty = difficulty;
        return this;
    }

    public Adventurer build() {
        if (preferences.difficulty == null) {
            preferences.difficulty = RandUtil.pick(EnumSet.complementOf(EnumSet.of(QuestDifficulty.HARD, QuestDifficulty.LEGENDARY)));
        }
        return new Adventurer(preferences);
    }
}
