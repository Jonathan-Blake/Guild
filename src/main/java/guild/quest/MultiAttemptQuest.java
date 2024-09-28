package guild.quest;

import guild.names.BasicNamedObject;

public class MultiAttemptQuest extends Quest {
    private final int finalReward;
    private int successesRequired;

    public MultiAttemptQuest(int expiryDate, QuestRank rank, int successesRequired) {
        super(expiryDate, rank);
        this.successesRequired = successesRequired;
        finalReward = reward();
    }

    @Override
    public boolean isCompleted() {
        return successesRequired <= 0;
    }

    @Override
    public int complete() {
        try {
            return super.complete();
        } finally {
            if (reward() == 0) {
                successesRequired--;
                if (successesRequired > 0) {
                    reward = finalReward;
                }
            }
        }
    }

    @Override
    public String getNameTemplate() {
        return BasicNamedObject.ReplacementString.RECURRING_QUEST.getSymbol();
    }
}
