package guild.shop;

import guild.adventurer.Adventurer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ShopTest {

    @Nested
    class browse {
        @Test
        void testDoesNotSellToPoorAdventurer() {
            Adventurer a = Adventurer.randomise(0).build();
            assertEquals(1, a.getGear());
            Shop.browse(a);
            assertEquals(1, a.getGear());
        }

        @Test
        void testDoesSellToRichAdventurer() {
            Adventurer a = Adventurer.randomise(0).build();
            a.gainReward(4 + 1);
            assertEquals(1, a.getGear());
            Shop.browse(a);
            assertEquals(2, a.getGear());
            assertEquals(1, a.wealth());
            a.gainReward(16);
            Shop.browse(a);
            assertEquals(3, a.getGear());
            assertEquals(1, a.wealth());
            a.gainReward(36);
            Shop.browse(a);
            assertEquals(4, a.getGear());
            assertEquals(1, a.wealth());
            a.gainReward(64);
            Shop.browse(a);
            assertEquals(5, a.getGear());
            assertEquals(1, a.wealth());
            a.gainReward(100);
            Shop.browse(a);
            assertEquals(6, a.getGear());
            assertEquals(1, a.wealth());
        }
    }
}