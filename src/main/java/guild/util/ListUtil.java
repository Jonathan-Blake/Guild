package guild.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class ListUtil {
    private ListUtil() {
    }

    public static <T> List<T> remove(List<T> listToCopy, T remove) {
        ArrayList<T> ret = new ArrayList<>(listToCopy);
        ret.remove(remove);
        return ret;
    }

    public static <T> List<T> removeIf(List<T> listToCopy, Predicate<T> condition) {
        ArrayList<T> ret = new ArrayList<>(listToCopy);
        ret.removeIf(condition);
        return ret;
    }
}
