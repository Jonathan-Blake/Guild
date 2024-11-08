package guild.adventurer;

import guild.names.BaseNamedObject;
import guild.names.BasicNamedObject;
import guild.names.WeightedNamedObject;
import guild.quest.Quest;
import guild.quest.QuestBoard;
import guild.util.ListUtil;
import guild.util.RandUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class Party extends WeightedNamedObject {
    private static final Logger logger = LoggerFactory.getLogger(Party.class);

    private final List<Adventurer> members;
    private final AdventurerRoster roster;
    private Quest currentQuest;
    private int partyMorale;

    public Party(List<Adventurer> proposedParty, AdventurerRoster roster) {
        super();
        members = proposedParty;
        this.roster = roster;
        proposedParty.forEach(each -> each.setParty(this));
        partyMorale = 3;
    }

    public static boolean wouldAccept(List<Adventurer> proposedParty) {
        return proposedParty.stream().allMatch(adventurer -> adventurer.wouldAccept(ListUtil.remove(proposedParty, adventurer)));
    }

    public void selectQuest(QuestBoard questBoard, int currentDate) {
        if (isUnassigned()) {
            List<Quest> options = questBoard.viewQuests();
            options.stream()
                    .map(quest -> rateQuest(quest, currentDate))
                    .filter(Objects::nonNull)
                    .max(Comparator.comparingDouble(QuestPlan::getPreference))
                    .ifPresentOrElse(questPlan -> questPlan.embark(this), () -> logger.info("{} could not find interesting guild.quest.", this));
        }
    }

    private QuestPlan rateQuest(Quest quest, int currentDate) {
        if (quest.isCompleted()) {
            return null;
        }
        QuestPlan ret = new QuestPlan(quest, currentDate);
        if ((ret.preference == 0)) {
            return null;
        }
        return ret;
    }

    public boolean isUnassigned() {
        return currentQuest == null;
    }

    public boolean checkDisband() {
        return !wouldAccept(members);
    }

    public void divideReward(int amount) {
        this.partyMorale = Math.min(20, this.partyMorale + currentQuest().rank().ordinal() + 1);
        int remainder = amount % members.size();
        int split = amount / members.size();
        members.forEach(each -> each.gainReward(split));
        int i = 0;
        while (i < remainder) {
            Adventurer tmp = members.get(0);
            members.remove(0);
            tmp.gainReward(1);
            members.add(members.size(), tmp);
            i++;
        }
    }

    public void divideInjuries(int injuries) {
        this.partyMorale -= injuries;
        for (int i = 0; i < injuries; i++) {
            if (members.isEmpty()) {
                logger.warn("{} was wiped out due to injuries.", this);
                return;
            }
            final Adventurer pick = RandUtil.pick(members);
            assert !pick.isDead();
            pick.injure();
            if (pick.isDead()) {
                roster.kill(pick);
            }
        }
    }

    public int partyPower() {
        return members.stream().mapToInt(each -> each.level() + each.getGear()).sum();
    }

    public int partyMorale() {
        return this.partyMorale;
    }

    public List<Adventurer> members() {
        return this.members;
    }

    @Override
    public String getNameTemplate() {
        return BasicNamedObject.ReplacementString.PARTY.getSymbol();
    }

    public Quest currentQuest() {
        return currentQuest;
    }

    @Override
    public Map<BasicNamedObject.ReplacementString, Map<String, Integer>> getContextMapping() {
        Map<BasicNamedObject.ReplacementString, Map<String, Integer>> ret = super.getContextMapping();
        ret.get(BasicNamedObject.ReplacementString.NAME).putAll(members.stream().collect(Collectors.toMap(
                BaseNamedObject::getName,
                member -> (50),
                Integer::sum
        )));
        return ret;
    }

    void removeMember(Adventurer adventurer) {
        assert members.contains(adventurer);
        assert adventurer.getParty() == this;
        members.remove(adventurer);
        adventurer.setParty(null);
        if (members.isEmpty()) {
            logger.info("{} disbanded due to lack of members.", this);
            roster.getParties().remove(this);
        }
    }

    public String voteOnDay() {
        StringBuilder sb = new StringBuilder(this + " decided today they would ");
        try {
            return members.stream()
                    .collect(Collectors.toMap(
                            Adventurer::preferredDayActivity,
                            each -> 1,
                            Integer::sum
                    )).entrySet().stream()
                    .peek(each -> sb.append("votes for ").append(each.getKey()).append(" : ").append(each.getValue()))
                    .max(Comparator.comparingInt(Map.Entry::getValue))
                    .map(Map.Entry::getKey)
                    .orElseThrow();
        } finally {
            logger.info(sb.toString());
        }
    }

    public class QuestPlan {
        private final double preference;
        private final Quest quest;
        private final Integer completionDate;
        private final int questEffort;
        @SuppressWarnings(value = "unused")
        private final int dateEmbarked;

        public QuestPlan(Quest quest, int currentDate) {
            this.quest = quest;
            dateEmbarked = currentDate;
            int maxTime = quest.expiryDate() - currentDate;
            final double partyPower1 = Party.this.partyPower();
            final int expectedLengthForQuestType = quest.rank().getDifficulty();
            int estTime = (int) Math.ceil(expectedLengthForQuestType / partyPower1);
            double proportion;
            if (estTime <= maxTime) {
                this.completionDate = estTime + currentDate;
                proportion = 1;
                questEffort = (int) (estTime * partyPower1);
            } else {
                this.completionDate = maxTime + currentDate;
                questEffort = (int) (maxTime * partyPower1);
                proportion = Math.pow(maxTime / (double) estTime, 3);
            }
            if (questEffort < quest.rank().getDifficulty() - 5) {
                preference = 0;
                return;
            }
            boolean competition = quest.isQuestAccepted();
            double wiggle = (RandUtil.nextDouble() / 100) + 1;

            preference = wiggle * quest.reward() * proportion * (competition ? 0.25 : 1.0);
        }

        public void embark(Party party) {
            quest.accept(this);
            party.currentQuest = quest;
        }

        public double getPreference() {
            return this.preference;
        }

        public void attempt() {
            final StringBuilder s = new StringBuilder().append(Party.this).append(" attempted ").append(quest).append(" ");
            if (quest.isCompleted()) {
                s.append("But it was already completed");
            } else {
                int attempt = (RandUtil.stdAround(10) - 10 + questEffort);
                if (attempt > quest.rank().getDifficulty()) {
                    //Success
                    s.append("And had great success.");
                    Party.this.divideReward(quest.complete());
                    Party.this.members.forEach(adventurer -> adventurer.gainExp(quest.rank().ordinal() + 1));
                } else if (attempt > quest.rank().getDifficulty() - 3) {
                    //Success with drawbacks
                    Party.this.divideInjuries(quest.rank().injuries());
                    if (Party.this.members().isEmpty()) {
                        s.append("But died in the attempt.");
                    } else {
                        s.append("and succeeded despite injuries.");
                        Party.this.divideReward(quest.complete());
                        Party.this.members.forEach(adventurer -> adventurer.gainExp(quest.rank().ordinal()));
                    }
                } else if (attempt > quest.rank().getDifficulty() - 5) {
                    //Failure
                    s.append("and were rebuffed with injuries");
                    Party.this.divideInjuries(quest.rank().injuries() + 1);
                } else {
                    //TPK
                    s.append("but they were never heard from again.");
                    Party.this.roster.tpk(Party.this);
                }
            }
            final String s1 = String.valueOf(s);
            logger.warn(s1);
            Party.this.currentQuest = null;
        }

        public Party getParty() {
            return Party.this;
        }

        public int effort() {
            return questEffort;
        }

        public int completionDate() {
            return completionDate;
        }
    }
}
