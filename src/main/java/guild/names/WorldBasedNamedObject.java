package guild.names;

import guild.world.World;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class WorldBasedNamedObject extends BaseNamedObject {
    private static final Pattern pattern = Pattern.compile("(\\(A-Z*\\))");

    private static String replaceTemplatedStrings(String string) {
        Matcher matcher = pattern.matcher(string);
//        Map<String, String> replacements = new HashMap<>();
//        int i = 0;
//        while (!matcher.find()){
//            String group = matcher.group(i++);
//            String replacement = World.getInstance().pick(group.split(":"));
//            replacements.put(group, replacement);
//        }
        return matcher.replaceAll(matchResult -> World.getInstance().pick(matcher.group()));
    }

    @Override
    protected String initName() {
        String temp = getNameTemplate();
        AtomicInteger attempts = new AtomicInteger();
        while (temp.contains("[")) {
            temp = replaceTemplatedStrings(temp);
            attempts.getAndIncrement();
        }
        return temp;
    }
}
