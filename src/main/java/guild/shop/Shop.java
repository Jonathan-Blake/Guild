package guild.shop;

import guild.adventurer.Adventurer;
import guild.names.BasicNamedObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Shop {
    private static final Logger logger = LoggerFactory.getLogger(Shop.class);

    private Shop() {
    }

    public static int calculateCostOfUpgrade(Adventurer adventurer) {
        return (int) Math.pow(adventurer.getGear() * 2.0, 2);
    }

    public static void browse(Adventurer each) {
        final int i = calculateCostOfUpgrade(each);
        if (each.wealth() > i) {
            final String item = BasicNamedObject.generateString(BasicNamedObject.ReplacementString.ITEM);
            logger.info("{} purchased a {}", each, item);
            each.gainReward(-i);
            each.improveGear();
        }
    }
}
