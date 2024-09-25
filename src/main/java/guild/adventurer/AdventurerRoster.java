package guild.adventurer;

import guild.Guild;
import guild.util.RandUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class AdventurerRoster {
    private static final Logger logger = LoggerFactory.getLogger(AdventurerRoster.class);

    private final Guild guild;
    private List<Party> parties;
    private List<Adventurer> adventurers;

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

    public void generateNewHeros() {
        for (int i = 0; i < 10; i++) {
            int chance = adventurers.size() * 10 - 100;
            if (RandUtil.probabilityRoll(chance)) {
                final Adventurer adventurer = Adventurer.randomise().build();
                adventurers.add(adventurer);
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
        List<Adventurer> heroesToParty = new ArrayList<>(adventurers.stream().filter(Adventurer::outOfParty).toList());
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
        return adventurers.size();
    }

    public List<Party> getParties() {
        return this.parties;
    }

    public Stream<Adventurer> unpartiedHeroesStream() {
        return adventurers.stream().filter(Adventurer::outOfParty);
    }

    public long unpartiedHeroesCount() {
        return unpartiedHeroesStream().count();
    }

    public void tpk(Party members) {
        this.parties.remove(members);
        this.adventurers.removeAll(members.members());
    }

    public void kill(Adventurer pick) {
        this.adventurers.remove(pick);
        final Party party = pick.getParty();
        party.members().remove(pick);
        if (party.members().isEmpty()) {
            this.parties.remove(party);
        }
        logger.warn("{} died in the attempt of guild.quest {}.", pick, party.currentQuest());
    }

    public List<Adventurer> getMembers() {
        return adventurers;
    }

    public void rest() {
        getMembers().forEach(Adventurer::rest);
    }

    public void disband(Party party) {
        logger.warn("{} disbanded.", party);
        for (Adventurer member : party.members()) {
            member.setParty(null);
        }
        parties.remove(party);
    }
}
