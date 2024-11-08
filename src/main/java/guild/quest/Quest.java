package guild.quest;

import guild.adventurer.Party;
import guild.names.BasicNamedObject;
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
    int reward;
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

    public boolean accept(Party.QuestPlan party) {
        logger.info("{} was accepted by {}", this, party.getParty());
        final int completionDate = party.completionDate();
        if (completionDate <= this.expiryDate) {
            this.contractsAccepted.merge(completionDate, new ArrayList<>(List.of(party)), (o, n) -> {
                o.addAll(n);
                return o;
            });
            return true;
        } else {
            logger.warn("{} submitted Plan for {} too late.", party.getParty(), this);
        }
        return false;
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

    public int earliestAttemptDate() {
        return contractsAccepted.keySet().stream().mapToInt(i -> i).min().orElse(0);
    }

    @Override
    public String getNameTemplate() {
        return BasicNamedObject.ReplacementString.QUEST.getSymbol();
    }

    @Override
    public Map<BasicNamedObject.ReplacementString, Map<String, Integer>> getContextMapping() {
        Map<BasicNamedObject.ReplacementString, Map<String, Integer>> ret = super.getContextMapping();
        ret.get(BasicNamedObject.ReplacementString.MONSTER).putAll(QuestRank.getMonstersForRank(rank).stream().collect(Collectors.toMap(
                string -> string,
                member -> (25),
                Integer::sum
        )));
        ret.get(BasicNamedObject.ReplacementString.ITEM_DESCRIPTOR).putAll(QuestRank.getItemsForRank(rank).stream().collect(Collectors.toMap(
                string -> string,
                member -> (15),
                Integer::sum
        )));
        ret.get(BasicNamedObject.ReplacementString.MATERIAL_DESCRIPTOR).putAll(QuestRank.getMaterialsForRank(rank).stream().collect(Collectors.toMap(
                string -> string,
                member -> (15),
                Integer::sum
        )));
        ret.get(BasicNamedObject.ReplacementString.GROUP_DESCRIPTOR).putAll(QuestRank.getGroupForRank(rank).stream().collect(Collectors.toMap(
                string -> string,
                member -> (15),
                Integer::sum
        )));
        return ret;
    }
}
