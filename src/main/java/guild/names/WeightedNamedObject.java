package guild.names;

import guild.util.RandUtil;

import java.util.HashMap;
import java.util.Map;

public abstract class WeightedNamedObject extends BaseNamedObject {

    private static String replaceTemplatedStrings(String string, Map<String, Map<String, Integer>> contextMapping, String key) {
        return string.replace(key, RandUtil.pick(contextMapping.get(key)));
    }

    @Override
    protected String initName() {
        String temp = getNameTemplate();
        Map<String, Map<String, Integer>> contextMapping = getContextMapping();
        while (temp.contains("[")) {
            boolean replaced = (false);
            for (String key : contextMapping.keySet()) {
                if (temp.contains(key)) {
                    temp = WeightedNamedObject.replaceTemplatedStrings(temp, contextMapping, key);
                    replaced = (true);
                }
            }
            if (!replaced) {
                throw new IllegalArgumentException("Failed to replace Weighted String " + temp);
            }
        }
        return temp;
    }

    public Map<String, Map<String, Integer>> getContextMapping() {
        HashMap<String, Map<String, Integer>> ret = new HashMap<>();
        for (BasicNamedObject.ReplacementString value : BasicNamedObject.ReplacementString.values()) {
            HashMap<String, Integer> weights = new HashMap<>();
            for (String expansion : value.expansions()) {
                weights.put(expansion, 10);
            }
            ret.put(value.getSymbol(), weights);
        }

        return ret;
    }
}
