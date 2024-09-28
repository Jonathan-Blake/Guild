package guild.quest;

import guild.names.BasicNamedObject;

public class EndlessQuest extends Quest {
    private final int finalReward;

    public EndlessQuest(QuestRank rank) {
        super(Integer.MAX_VALUE, rank);
        finalReward = reward();
    }

    @Override
    public void resolve(int day) {
        super.resolve(day);
        reward = finalReward;
    }

    @Override
    public boolean isCompleted() {
        return false;
    }

    @Override
    public int complete() {
        try {
            return super.complete();
        } finally {
            reward = finalReward / 2;
        }
    }

    @Override
    public String getNameTemplate() {
        return BasicNamedObject.ReplacementString.RECURRING_QUEST.getSymbol();
    }
}
