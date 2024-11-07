package guild.world;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Graph<T> {
    Map<String, GraphNode> nodeMap;

    @JsonCreator
    public Graph(@JsonProperty("nodes") Map<String, T> nodes, @JsonProperty("connections") List<Map.Entry<String, String>> connections) {
        nodeMap = new HashMap<>();
        nodes.forEach((each, data) -> nodeMap.putIfAbsent(each, new GraphNode(each, data)));
        connections.forEach(keyValue -> nodeMap.get(keyValue.getKey()).connect(keyValue.getValue()));
    }

    public Map<String, T> getNodes() {
        return nodeMap.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                kv -> kv.getValue().data,
                (o, n) -> {
                    throw new IllegalArgumentException();
                }
        ));
    }

    public List<Map.Entry<String, String>> getConnections() {
        ArrayList<Map.Entry<String, String>> ret = new ArrayList<>();
        HashSet<String> explore = new HashSet<>();
        for (Map.Entry<String, GraphNode> entry : nodeMap.entrySet()) {
            GraphNode node = entry.getValue();
            node.getConnectedNodes().filter(connection -> !explore.contains(connection.id)).forEach(
                    connection -> ret.add(Map.entry(node.id, connection.id))
            );
            explore.add(node.id);
        }
        return ret;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Graph<?> graph = (Graph<?>) o;

        return nodeMap.equals(graph.nodeMap);
    }

    @Override
    public int hashCode() {
        return nodeMap.hashCode();
    }

    class GraphNode {
        public final String id;
        public final T data;
        Set<String> connections;

        public GraphNode(String id, T data) {
            this.id = id;
            this.data = data;
            connections = new HashSet<>();
        }

        @JsonIgnore
        Stream<GraphNode> getConnectedNodes() {
            return connections.stream().map(nodeMap::get);
        }

        void connect(String other) {
            if (!connections.contains(other)) {
                connections.add(other);
                nodeMap.get(other).connect(this.id);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            GraphNode graphNode = (GraphNode) o;

            if (!id.equals(graphNode.id)) return false;
            if (!data.equals(graphNode.data)) return false;
            return connections.equals(graphNode.connections);
        }

        @Override
        public int hashCode() {
            int result = id.hashCode();
            result = 31 * result + data.hashCode();
            result = 31 * result + connections.hashCode();
            return result;
        }
    }
}

