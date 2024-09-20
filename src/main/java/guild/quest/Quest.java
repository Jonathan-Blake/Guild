package guild.quest;

import guild.AbstractNamedObject;
import guild.adventurer.Party;
import guild.util.RandUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class Quest extends AbstractNamedObject {
    private static final Logger logger = LoggerFactory.getLogger(Quest.class);

    final int expiryDate;
    private final QuestDifficulty difficulty;
    private int reward;
    private HashMap<Integer, List<Party.QuestPlan>> contractsAccepted;
    private boolean completed = false;

    public Quest(int expiryDate, QuestDifficulty difficulty) {
        this.expiryDate = expiryDate;
        this.difficulty = difficulty;
        this.reward = RandUtil.stdAround(difficulty.getDifficulty());
        contractsAccepted = new HashMap<>();
    }

    public static QuestBuilder randomQuest() {
        return new QuestBuilder();
    }

    public int expiryDate() {
        return expiryDate;
    }

    public QuestDifficulty difficulty() {
        return difficulty;
    }

    public int reward() {
        return this.reward;
    }

    public boolean isQuestAccepted() {
        return !contractsAccepted.keySet().isEmpty();
    }

    public void accept(Party.QuestPlan party, Integer completionDate) {
        logger.info("{} was accepted by {}", this, party.getParty());
        this.contractsAccepted.merge(completionDate, new ArrayList<>(List.of(party)), (o, n) -> {
            o.addAll(n);
            return o;
        });
    }

    public void resolve(int day) {
        List<Party.QuestPlan> contracts = this.contractsAccepted.get(day);
        if (contracts == null) {
            return;
        }
        contracts.sort(Comparator.comparingInt(Party.QuestPlan::effort)); // Least prepared most rushed goes first
        contracts.forEach(Party.QuestPlan::attempt);
        this.contractsAccepted.remove(day);
    }

    public boolean isCompleted() {
        return this.completed;
    }

    public int complete() {
        this.completed = true;
        int tmp = this.reward;
        this.reward = 0;
        return tmp;
    }

    @Override
    public String getNameTemplate() {
        return ReplacementString.QUEST.getSymbol();
    }

    public int earliestAttemptDate() {
        return contractsAccepted.keySet().stream().mapToInt(i -> i).min().orElse(0);
    }
}
