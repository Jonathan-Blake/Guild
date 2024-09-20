package guild;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class AbstractNamedObjectTest {

    private static final Logger logger = LoggerFactory.getLogger(AbstractNamedObjectTest.class);
    private static final List<String> templates = List.of("TEST",
            AbstractNamedObject.ReplacementString.ADVENTURER.getSymbol(),
            AbstractNamedObject.ReplacementString.QUEST.getSymbol(),
            AbstractNamedObject.ReplacementString.ITEM.getSymbol()
    );

    static Stream<Arguments> getName() {
        Stream.Builder<Stream<Arguments>> ret = Stream.builder();
        templates.forEach(template -> ret.add(
                Stream.generate(() -> Arguments.of(
                        template,
                        getAbstractNamedObjectFromTemplate(template))
                ).limit(10)
        ));
        return ret.build().flatMap(each -> each);
    }

    private static AbstractNamedObject getAbstractNamedObjectFromTemplate(String template) {
        return new AbstractNamedObject() {
            @Override
            public String getNameTemplate() {
                return template;
            }
        };
    }

    @ParameterizedTest
    @MethodSource
    void getName(String template, AbstractNamedObject a) {
        assertEquals(template, a.getNameTemplate());
        logger.info(a.getName());
        assertFalse(a.getName().contains("["));
        assertFalse(a.getName().contains("]"));
    }
}