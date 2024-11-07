package guild.world;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraphTest {

    public static Map.Entry<String, String> reverse(Map.Entry<String, String> actual) {
        return Map.entry(actual.getValue(), actual.getKey());
    }

    @Test
    void test() throws JsonProcessingException {
        final Map<String, String> nodes = makeNodes();
        final List<Map.Entry<String, String>> connections = List.of(
                Map.entry("key1", "key2"),
                Map.entry("key2", "key3"),
                Map.entry("key4", "key4"),
                Map.entry("key6", "key7"),
                Map.entry("key8", "key7")
        );
        Graph<String> g = new Graph<>(
                nodes,
                connections
        );
        assertEquals(nodes, g.getNodes());
        assertTrue(g.getConnections().stream().allMatch(actual -> connections.contains(actual) || connections.contains(reverse(actual))));
        ObjectMapper mapper = new ObjectMapper();
        final String x = mapper.writeValueAsString(g);
        System.out.println(x);
        assertEquals(g, mapper.readValue(x, Graph.class));
    }

    private Map<String, String> makeNodes() {
        HashMap<String, String> ret = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            ret.put("key" + i, "value" + i);
        }
        return ret;
    }
}