package guild.util;

import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.HashMap;
import java.util.IntSummaryStatistics;
import java.util.Map;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RandUtilTest {

    @Test
    void stdAround() {
        HashMap<Integer, Integer> histogram = new HashMap<>();
        IntSummaryStatistics stats = IntStream.generate(() -> RandUtil.stdAround(50)).limit(1000)
                .peek(each -> histogram.merge(each, 1, (o, n) -> ++o)).summaryStatistics();
        histogram.entrySet().stream().sorted(Comparator.comparingInt(Map.Entry::getKey)).forEach(
                (kv) -> System.out.println(kv.getKey() + " " + kv.getValue())
        );
        System.out.println(stats);
        assertEquals(50.0, stats.getAverage(), 0.75);
    }
}