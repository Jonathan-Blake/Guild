package guild.names;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class BasicNamedObjectTest {

    private static final Logger logger = LoggerFactory.getLogger(BasicNamedObjectTest.class);
    private static final List<String> templates = List.of("TEST",
            BasicNamedObject.ReplacementString.ADVENTURER.getSymbol(),
            BasicNamedObject.ReplacementString.QUEST.getSymbol(),
            BasicNamedObject.ReplacementString.ITEM.getSymbol(),
            BasicNamedObject.ReplacementString.COLOUR.getSymbol()
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

    private static BasicNamedObject getAbstractNamedObjectFromTemplate(String template) {
        return new BasicNamedObject() {
            @Override
            public String getNameTemplate() {
                return template;
            }
        };
    }

    @ParameterizedTest
    @MethodSource
    void getName(String template, BasicNamedObject a) {
        assertEquals(template, a.getNameTemplate());
        logger.info(a.getName());
        assertFalse(a.getName().contains("["));
        assertFalse(a.getName().contains("]"));
    }
}