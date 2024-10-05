package guild.adventurer;

import guild.names.BasicNamedObject;
import guild.shop.Shop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class Adventurer extends BasicNamedObject {
    private static final Logger logger = LoggerFactory.getLogger(Adventurer.class);
    private final AdventurePreferences preferences;
    protected int partyDesperation = 0;
    private int power;
    private Party party;
    private int wealth = 0;
    private int injuries;
    private Integer dateInjured = 0;
    private int xp = 0;
    private int duesDateOwed;
    private int gear;

    Adventurer(AdventurePreferences preferences) {
        super();
        this.preferences = preferences;
        power = 1;
        gear = 1;
        setDuesDateOwed(preferences.creationDate() + 14);
    }

    public static AdventurerBuilder randomise(int date) {
        return new AdventurerBuilder(date);
    }


    public Party getParty() {
        return party;
    }

    public void setParty(Party party) {
        this.party = party;
        if (party != null) {
            this.partyDesperation = 0;
        }
    }

    public void increaseDesperation() {
        this.partyDesperation++;
    }

    public int getDuesDateOwed() {
        return duesDateOwed;
    }

    public void setDuesDateOwed(int duesDateOwed) {
        this.duesDateOwed = duesDateOwed;
    }

    public boolean outOfParty() {
        return this.party == null;
    }

    public boolean wouldAccept(List<Adventurer> otherMembers) {
        int partyPower = this.power;
        partyPower += otherMembers.stream().mapToInt(each -> each.power).sum();
        //Party is sufficiently Strong
        final int expectedPower = partyPower * preferences.difficulty.getExpectedLength();
        int initialAcceptance = expectedPower - preferences.difficulty.getDifficulty();
        ArrayList<String> reasons = new ArrayList<>();
        reasons.add("%s considered party of %s at rating %s".formatted(this, otherMembers, initialAcceptance));
        if (initialAcceptance < 0) {
            reasons.add("party was under desired strength.");
        }
        if (getParty() == null) {
            initialAcceptance += partyDesperation;
            reasons.add("desperate for a party %s.".formatted(partyDesperation));
        } else {
            initialAcceptance += getParty().partyMorale();
            reasons.add("party morale was %s".formatted(getParty().partyMorale()));
        }
        logger.debug("{}", reasons);
        return initialAcceptance >= 0;
    }

    public int level() {
        return power;
    }

    public int wealth() {
        return this.wealth;
    }

    public void gainReward(int i) {
        this.wealth += i;
    }

    public boolean charge(int amount) {
        if (amount > wealth) {
            return false;
        } else {
            wealth -= amount;
            return true;
        }
    }

    public int injuriesSustained() {
        return this.injuries;
    }

    public boolean isDead() {
        return this.injuries > 3;
    }

    public void injure() {
        this.injuries += 1;
        this.dateInjured += this.injuries * 4;
    }

    @Override
    public String getNameTemplate() {
        return ReplacementString.ADVENTURER.getSymbol();
    }

    public void rest() {
        if (this.injuries != 0 && !isDead()) {
            this.dateInjured--;
            if (dateInjured <= 0) {
                logger.info("{} recovered an injury {}", this, --this.injuries);
                this.dateInjured = injuries * 4;
            }
        }
    }

    public void gainExp(int xp) {
        this.xp += xp;
        while (this.xp >= level() * 4) {
            logger.info("{} Level UP! ", this);
            this.xp -= level() * 4;
            power++;
        }
    }

    public String preferredDayActivity() {
        if (injuriesSustained() > 0) {
            return "REST";
        } else if (wealth() > Shop.calculateCostOfUpgrade(this)) {
            return "SHOP";
        } else {
            return "QUEST";
        }
    }

    public int getGear() {
        return this.gear;
    }

    public void improveGear() {
        this.gear += 1;
    }
}
