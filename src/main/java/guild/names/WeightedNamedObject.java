package guild.names;

import guild.util.RandUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class WeightedNamedObject extends BaseNamedObject {

    private static String replaceTemplatedStrings(String string, Map<String, Map<String, Integer>> contextMapping, String key) {
        return string.replace(key, RandUtil.pick(contextMapping.get(key)));
    }

    @Override
    protected String initName() {
        final String[] temp = {getNameTemplate()};
        Map<String, Map<String, Integer>> contextMapping = getContextMapping();
        while (temp[0].contains("{")) {
            AtomicBoolean replaced = new AtomicBoolean(false);
            contextMapping.keySet().forEach(
                    key -> {
                        if (temp[0].contains(key)) {
                            temp[0] = WeightedNamedObject.replaceTemplatedStrings(temp[0], contextMapping, key);
                            replaced.set(true);
                        }
                    }
            );
            if (!replaced.get()) {
                throw new IllegalArgumentException("Failed to replace Weighted String " + temp[0]);
            }
        }
        return temp[0];
    }

    public Map<String, Map<String, Integer>> getContextMapping() {
        HashMap<String, Map<String, Integer>> ret = new HashMap<>();
        for (BasicNamedObject.ReplacementString value : BasicNamedObject.ReplacementString.values()) {
            HashMap<String, Integer> weights = new HashMap<>();
            for (String expansion : value.expansions()) {
                weights.put(expansion.replace("[", "{").replace("]", "}"), 10);
            }
            ret.put("{" + value.name() + "}", weights);
        }

        return ret;
    }
}
