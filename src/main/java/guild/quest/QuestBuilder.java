package guild.quest;

import guild.util.RandUtil;

import java.util.EnumSet;

public class QuestBuilder {
    private int startDate;
    private EnumSet<QuestRank> possibleDifficulties = EnumSet.allOf(QuestRank.class);

    public QuestBuilder fromDate(int day) {
        this.startDate = day;
        return this;
    }

    public QuestBuilder notDifficulty(QuestRank difficulty) {
        this.possibleDifficulties.remove(difficulty);
        return this;
    }

    public Quest build() {
        QuestRank difficulty = RandUtil.pick(possibleDifficulties);
        return new Quest(startDate + RandUtil.stdAround(difficulty.getExpectedLength()), difficulty);
    }
}
