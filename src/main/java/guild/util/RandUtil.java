package guild.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Random;

public class RandUtil {

    private static final Random random = new Random();

    private RandUtil() {
    }

    public static int stdAround(int expectedLength) {
        final int bound = expectedLength / 2;
        return random.nextInt(bound) - random.nextInt(bound) + expectedLength;
    }

    public static boolean probabilityRoll(int difficulty) {
        return random.nextInt(100) > difficulty;
    }

    @SafeVarargs
    public static <T> T pick(T... values) {
        return values[random.nextInt(values.length)];
    }

    public static <T> T pick(Collection<T> values) {
        final int i = random.nextInt(values.size());
        Iterator<T> itr = values.iterator();
        for (int j = 0; j < i; j++) {
            itr.next();
        }
        return itr.next();
    }

    public static double nextDouble() {
        return random.nextDouble();
    }
}
