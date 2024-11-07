package guild.world;

import guild.util.RandUtil;

public class World {
    private static final World instance = new World();
    public final Tree<String> contents = new Tree<>();

    public static World getInstance() {
        return instance;
    }

    public String pick(String namespace) {
        return RandUtil.pick(contents.lookup(namespace).getLeaves()).data;
    }
}
