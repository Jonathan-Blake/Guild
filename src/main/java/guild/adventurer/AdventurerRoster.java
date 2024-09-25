package guild.adventurer;

import guild.Guild;
import guild.util.RandUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class AdventurerRoster {
    private static final Logger logger = LoggerFactory.getLogger(AdventurerRoster.class);

    private final Guild guild;
    protected final List<Adventurer> adventurers;
    private final List<Party> parties;

    public AdventurerRoster(Guild guild) {
        this.guild = guild;
        parties = new ArrayList<>();
        adventurers = new ArrayList<>();
    }

    public List<Party> getUnassignedParties() {
        return getUnassignedPartiesStream().toList();
    }

    public Stream<Party> getUnassignedPartiesStream() {
        return this.parties.stream().filter(Party::isUnassigned);
    }

    public void generateNewHeros(int currDate) {
        for (int i = 0; i < 10; i++) {
            int chance = getMembers().size() * 10 - 100;
            if (RandUtil.probabilityRoll(chance)) {
                final Adventurer adventurer = Adventurer.randomise(currDate).build();
                getMembers().add(adventurer);
            }
        }
    }

    public void mergeOrSplitParties() {
        ArrayList<Party> partiesToDisband = new ArrayList<>();
        getUnassignedPartiesStream().forEach(party -> {
            if (party.checkDisband()) {
                partiesToDisband.add(party);
            }
        });
        partiesToDisband.forEach(this::disband);
        List<Adventurer> heroesToParty = new ArrayList<>(getMembers().stream().filter(Adventurer::outOfParty).toList());
        while (assembleParty(new ArrayList<>(), heroesToParty)) {
            final Party party = parties.get(parties.size() - 1);
            logger.info("Created new party {} {}", party, party.members());
        }
        logger.info("Could not create new parties {}", unpartiedHeroesCount());
        unpartiedHeroesStream().forEach(Adventurer::increaseDesperation);
    }

    public boolean assembleParty(List<Adventurer> proposedParty, List<Adventurer> possibleMembers) {
        for (int i = 0; i < possibleMembers.size(); i++) {
            Adventurer adventurer = possibleMembers.get(i);
            proposedParty.add(adventurer);
            possibleMembers.remove(adventurer);
            if (Party.wouldAccept(proposedParty)) {
                parties.add(new Party(proposedParty, this));
                return true;
            } else if (assembleParty(proposedParty, possibleMembers)) {
                return true;
            } else {
                proposedParty.remove(adventurer);
                possibleMembers.add(adventurer);
            }
        }
        return false;
    }

    public int heroCount() {
        return getMembers().size();
    }

    public List<Party> getParties() {
        return this.parties;
    }

    public Stream<Adventurer> unpartiedHeroesStream() {
        return getMembers().stream().filter(Adventurer::outOfParty);
    }

    public long unpartiedHeroesCount() {
        return unpartiedHeroesStream().count();
    }

    public void tpk(Party members) {
        this.parties.remove(members);
        this.getMembers().removeAll(members.members());
    }

    public void kill(Adventurer pick) {
        this.getMembers().remove(pick);
        final Party party = pick.getParty();
        logger.warn("{} died in the attempt of guild.quest {}.", pick, party.currentQuest());
        party.removeMember(pick);
    }

    public List<Adventurer> getMembers() {
        return adventurers;
    }

    public void rest() {
        getMembers().forEach(Adventurer::rest);
    }

    public void disband(Party party) {
        logger.warn("{} disbanded.", party);
        for (Adventurer member : new ArrayList<>(party.members())) {
            party.removeMember(member);
        }
        parties.remove(party);
    }

    public int collectDues(final int day, final int amount) {
        AtomicInteger amountCollected = new AtomicInteger();
        List<Adventurer> cantPay = new ArrayList<>();
        getUnassignedPartiesStream()
                .flatMap(party -> party.members().stream())
                .filter(adventurer -> day > adventurer.getDuesDateOwed())
                .forEach(
                        adventurer -> amountCollected.addAndGet(
                                chargeDues(day, amount, cantPay, adventurer))
                );
        cantPay.forEach(each -> {
            logger.info("{} can't pay their dues and quit.", each);
            each.getParty().removeMember(each);
            getMembers().remove(each);
        });
        return amountCollected.get();
    }

    private int chargeDues(int day, int amount, List<Adventurer> cantPay, Adventurer adventurer) {
        int ret = 0;
        while (day > adventurer.getDuesDateOwed()) {
            if (!adventurer.charge(amount)) {
                cantPay.add(adventurer);
                break;
            } else {
                ret += (amount);
                adventurer.setDuesDateOwed(adventurer.getDuesDateOwed() + 7);
            }
        }
        return ret;
    }
}
