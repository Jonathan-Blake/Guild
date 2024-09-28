package guild.names;

import guild.util.RandUtil;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public abstract class WeightedNamedObject extends BaseNamedObject {

    private static String replaceTemplatedStrings(String string, Map<BasicNamedObject.ReplacementString, Map<String, Integer>> contextMapping, BasicNamedObject.ReplacementString key) {
        int start = string.indexOf(key.getSymbol());
        int end = start + key.getSymbol().length();
        return string.substring(0, start) + RandUtil.pick(contextMapping.get(key)) + string.substring(end);
//        return string.replace(key.getSymbol(), RandUtil.pick(contextMapping.get(key)));
    }

    @Override
    protected String initName() {
        String temp = getNameTemplate();
        Map<BasicNamedObject.ReplacementString, Map<String, Integer>> contextMapping = getContextMapping();
        while (temp.contains("[")) {
            boolean replaced = (false);
            for (BasicNamedObject.ReplacementString key : contextMapping.keySet()) {
                while (temp.contains(key.getSymbol())) {
                    temp = WeightedNamedObject.replaceTemplatedStrings(temp, contextMapping, key);
                    replaced = (true);
//                    if(!temp.contains("[")){  //Faster to not check
//                        break;
//                    }
                }
            }
            if (!replaced) {
                throw new IllegalArgumentException("Failed to replace Weighted String " + temp);
            }
        }
        return temp;
    }

    public Map<BasicNamedObject.ReplacementString, Map<String, Integer>> getContextMapping() {
        EnumMap<BasicNamedObject.ReplacementString, Map<String, Integer>> ret = new EnumMap<>(BasicNamedObject.ReplacementString.class);
//        Map<BasicNamedObject.ReplacementString, Map<String, Integer>> ret = new HashMap<>();
        for (BasicNamedObject.ReplacementString value : BasicNamedObject.ReplacementString.values()) {
            HashMap<String, Integer> weights = new HashMap<>();
            for (String expansion : value.expansions()) {
                weights.put(expansion, 10);
            }
            ret.put(value, weights);
        }

        return ret;
    }
}
