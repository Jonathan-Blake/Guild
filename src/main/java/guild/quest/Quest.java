package guild.quest;

import guild.adventurer.Party;
import guild.names.WeightedNamedObject;
import guild.util.RandUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class Quest extends WeightedNamedObject {
    private static final Logger logger = LoggerFactory.getLogger(Quest.class);

    final int expiryDate;
    private final QuestRank rank;
    private int reward;
    private HashMap<Integer, List<Party.QuestPlan>> contractsAccepted;
    private boolean completed = false;

    public Quest(int expiryDate, QuestRank rank) {
        this.expiryDate = expiryDate;
        this.rank = rank;
        this.reward = RandUtil.stdAround(rank.getDifficulty());
        contractsAccepted = new HashMap<>();
    }

    public static QuestBuilder randomQuest() {
        return new QuestBuilder();
    }

    public int expiryDate() {
        return expiryDate;
    }

    public QuestRank rank() {
        return rank;
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
        return "{QUEST}";
    }

    public int earliestAttemptDate() {
        return contractsAccepted.keySet().stream().mapToInt(i -> i).min().orElse(0);
    }


    @Override
    public Map<String, Map<String, Integer>> getContextMapping() {
        Map<String, Map<String, Integer>> ret = super.getContextMapping();
        ret.get("{MONSTER}").putAll(QuestRank.getMonstersForRank(rank).stream().collect(Collectors.toMap(
                string -> string,
                member -> (25),
                Integer::sum
        )));
        ret.get("{ITEM_DESCRIPTOR}").putAll(QuestRank.getItemsForRank(rank).stream().collect(Collectors.toMap(
                string -> string,
                member -> (15),
                Integer::sum
        )));
        ret.get("{MATERIAL_DESCRIPTOR}").putAll(QuestRank.getMaterialsForRank(rank).stream().collect(Collectors.toMap(
                string -> string,
                member -> (15),
                Integer::sum
        )));
        ret.get("{GROUP_DESCRIPTOR}").putAll(QuestRank.getGroupForRank(rank).stream().collect(Collectors.toMap(
                string -> string,
                member -> (15),
                Integer::sum
        )));
        return ret;
    }
}
