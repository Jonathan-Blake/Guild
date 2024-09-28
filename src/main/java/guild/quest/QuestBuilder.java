package guild.quest;

import guild.util.RandUtil;

import java.util.EnumSet;

public class QuestBuilder {
    private int startDate;
    private EnumSet<QuestRank> possibleDifficulties = EnumSet.allOf(QuestRank.class);
    private boolean createEndless = false;
    private boolean createRecurring;
    private Integer recurringAttempts = null;


    public QuestBuilder endless() {
        this.createEndless = true;
        this.createRecurring = RandUtil.probabilityRoll(20);
        this.startDate = 0;
        return this;
    }

    public QuestBuilder recurring() {
        this.createRecurring = true;
        this.createEndless = false;
        return this;
    }

    public QuestBuilder notRecurring() {
        this.createRecurring = false;
        this.createEndless = false;
        return this;
    }

    public QuestBuilder fromDate(int day) {
        this.startDate = day;
        this.createEndless = false;
        return this;
    }

    public QuestBuilder difficulty(QuestRank preferredDifficulty) {
        this.possibleDifficulties = EnumSet.of(preferredDifficulty);
        return this;
    }

    public QuestBuilder notDifficulty(QuestRank difficulty) {
        this.possibleDifficulties.remove(difficulty);
        return this;
    }

    public Quest build() {
        QuestRank difficulty = RandUtil.pick(possibleDifficulties);
        if (createEndless) {
            return new EndlessQuest(difficulty);
        } else {
            if (createRecurring) {
                return new MultiAttemptQuest(startDate + RandUtil.stdAround(difficulty.getExpectedLength()) * 3,
                        difficulty,
                        recurringAttempts != null ? recurringAttempts : RandUtil.nextInt(2, 5));
            } else {
                return new Quest(startDate + RandUtil.stdAround(difficulty.getExpectedLength()), difficulty);
            }
        }
    }

    public QuestBuilder recurring(int i) {
        recurringAttempts = i;
        recurring();
        return this;
    }
}
