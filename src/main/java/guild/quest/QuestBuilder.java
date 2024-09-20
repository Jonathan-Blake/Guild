package guild.quest;

import guild.util.RandUtil;

import java.util.EnumSet;

public class QuestBuilder {
    private int startDate;
    private EnumSet<QuestDifficulty> possibleDifficulties = EnumSet.allOf(QuestDifficulty.class);

    public QuestBuilder fromDate(int day) {
        this.startDate = day;
        return this;
    }

    public QuestBuilder notDifficulty(QuestDifficulty difficulty) {
        this.possibleDifficulties.remove(difficulty);
        return this;
    }

    public Quest build() {
        QuestDifficulty difficulty = RandUtil.pick(possibleDifficulties);
        return new Quest(startDate + RandUtil.stdAround(difficulty.getExpectedLength()), difficulty);
    }
}
