package guild.names;

import guild.adventurer.Adventurer;
import guild.adventurer.Party;
import guild.quest.Quest;
import guild.util.RandUtil;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;

class WeightedNamedObjectTest {

    private static final Logger logger = LoggerFactory.getLogger(WeightedNamedObjectTest.class);
    private static final List<Supplier<WeightedNamedObject>> templates = List.of(
            WeightedNamedObjectTest::buildRandomParty,
            WeightedNamedObjectTest::buildRandomQuest,
            WeightedNamedObjectTest::buildEndlessQuest,
            WeightedNamedObjectTest::buildRecurringQuest
    );

    private static Quest buildRandomQuest() {
        return Quest.randomQuest().notRecurring().build();
    }

    private static Quest buildRecurringQuest() {
        return Quest.randomQuest().recurring().build();
    }

    private static Quest buildEndlessQuest() {
        return Quest.randomQuest().endless().build();
    }

    private static Party buildRandomParty() {
        return new Party(
                RandUtil.randCountOf(() -> Adventurer.randomise(0).build(), 7),
                null
        );
    }

    static Stream<Arguments> getName() {
        Stream.Builder<Stream<Arguments>> ret = Stream.builder();
        templates.forEach(template -> ret.add(
                Stream.generate(() -> Arguments.of(
                                template.get()
                        )
                ).limit(300)
        ));
        return ret.build().flatMap(each -> each);
    }

    @ParameterizedTest
    @MethodSource
    void getName(WeightedNamedObject a) {
        logger.info("{}", a);
        if (a instanceof Party p) {
            p.members().forEach(adventurer -> {
                if (p.getName().contains(adventurer.getName())) {
                    logger.info("Adventurer Name was used to make Party name");
                }
            });
        }
        assertFalse(a.getName().contains("["));
        assertFalse(a.getName().contains("]"));
        assertFalse(a.getName().contains("{"));
    }
}