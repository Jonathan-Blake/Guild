package guild.world;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static guild.world.GraphTest.reverse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GenerativeGrammarGraphTest {

    @Test
    void test_CreatesAsExpected() {
        GenerativeGrammarGraph g = new GenerativeGrammarGraph(makeNodes());
        assertEquals(g.getNodes(), makeNodes());
        List<Map.Entry<String, String>> connections = List.of(
                Map.entry("(CHIMERA)", "(MONSTER)"),
                Map.entry("(MONSTER)", "(MONSTER)"),
                Map.entry("(MONSTER)", "(CREATURE_DESCRIPTOR)"),
                Map.entry("(CHIMERA)", "(BODY_PART)")
        );
        assertTrue(g.getConnections().stream().allMatch(actual -> connections.contains(actual) || connections.contains(reverse(actual))));
    }

    @Test
    void test_SerialisedAndDeserializes() throws JsonProcessingException {
        GenerativeGrammarGraph g = new GenerativeGrammarGraph(makeNodes());
        assertEquals(g.getNodes(), makeNodes());
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writeValueAsString(g));
        assertEquals(g, mapper.readValue(mapper.writeValueAsString(g), GenerativeGrammarGraph.class));
    }

    @Test
    void test2() {
        GenerativeGrammarGraph g = new GenerativeGrammarGraph(makeNodes());
        for (int i = 0; i < 10; i++) {
            System.out.println(g.expand("(CHIMERA)"));
        }
    }

    private Map<String, List<String>> makeNodes() {
        return Map.of(
                "(CHIMERA)", List.of("A Chimera with the (BODY_PART) of a (MONSTER), the (BODY_PART) of a (MONSTER) and the (BODY_PART) of a (MONSTER)"),
                "(BODY_PART)", List.of("Legs", "Head", "Body", "Fearsome Visage", "Tail", "Wings", "Claws", "Fangs"),
                "(MONSTER)", List.of("Wolf", "Goat", "Lion", "Hawk", "Weasel", "(CREATURE_DESCRIPTOR) (MONSTER)"),
                "(CREATURE_DESCRIPTOR)", List.of("Angry", "Medium Sized", "Bookish")
        );
    }
}