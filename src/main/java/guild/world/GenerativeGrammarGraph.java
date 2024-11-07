package guild.world;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import guild.util.RandUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GenerativeGrammarGraph extends Graph<List<String>> {
    private static final Pattern pattern = Pattern.compile("(\\([_A-Z]*\\))");

    @JsonCreator
    public GenerativeGrammarGraph(@JsonProperty("nodes") Map<String, List<String>> nodes) {
        super(nodes, connectionsFromNodes(nodes));
    }

    private static List<Map.Entry<String, String>> connectionsFromNodes(Map<String, List<String>> nodes) {
        List<Map.Entry<String, String>> ret = new ArrayList<>();
        nodes.forEach(
                (key, list) -> list.forEach(
                        string -> {
                            Matcher matcher = pattern.matcher(string);
                            while (matcher.find()) {
                                final Map.Entry<String, String> entry = Map.entry(key, string.substring(matcher.start(), matcher.end()));
                                ret.add(entry);
                            }
                        }
                )
        );
        return ret;
    }

    public String expand(String startNode) {
        String temp = RandUtil.pick(nodeMap.get(startNode).data);
        while (temp.contains("(")) {
            Matcher matcher = pattern.matcher(temp);
            String replace = matcher.replaceAll(matchResult -> RandUtil.pick(nodeMap.get(matchResult.group()).data));
            if (Objects.equals(temp, replace)) {
                return temp;
            }
            temp = replace;
        }
        return temp;
    }
}
