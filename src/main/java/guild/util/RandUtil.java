package guild.util;

import java.util.*;
import java.util.function.Supplier;

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

    public static <T> T pick(Map<T, Integer> weightedValues) {
        int i = random.nextInt(weightedValues.values().stream().mapToInt(j -> j).sum());
        for (Map.Entry<T, Integer> entry : weightedValues.entrySet()) {
            i -= entry.getValue();
            if (i <= 0) {
                return entry.getKey();
            }
        }
        throw new IllegalArgumentException("Logic got messed up");
    }

    public static double nextDouble() {
        return random.nextDouble();
    }

    public static <T> List<T> randCountOf(Supplier<T> supplier, int bound) {
        int count = random.nextInt(bound);
        ArrayList<T> ret = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            ret.add(i, supplier.get());
        }
        return ret;
    }

    public static int nextInt(int start, int limit) {
        return random.nextInt(start, limit);
    }
}
