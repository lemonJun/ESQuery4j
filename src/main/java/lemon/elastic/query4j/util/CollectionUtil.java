package lemon.elastic.query4j.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Robert HG (254963746@qq.com) on 6/23/14.
 */
public class CollectionUtil {

    private CollectionUtil() {
    }

    @SuppressWarnings("rawtypes")
    public static boolean isNotEmpty(Map map) {
        return map != null && map.size() > 0;
    }

    @SuppressWarnings("rawtypes")
    public static boolean isEmpty(Map map) {
        return !isNotEmpty(map);
    }

    public static boolean isNotEmpty(Collection<?> collection) {
        return collection != null && collection.size() > 0;
    }

    public static boolean isEmpty(Collection<?> collection) {
        return !isNotEmpty(collection);
    }

    @SuppressWarnings("rawtypes")
    public static int sizeOf(Collection collection) {
        if (isEmpty(collection)) {
            return 0;
        }
        return collection.size();
    }

    public static boolean noNullElements(Object[] array) {
        if (array != null) {
            for (int i = 0; i < array.length; i++) {
                if (array[i] == null) {
                    return false;
                }
            }
            return true;
        }
        return false;

    }

    public static String[] toArray(List<String> values) {
        String[] valuesAsArray = new String[values.size()];
        return values.toArray(valuesAsArray);
    }

    /**
     * 返回第一个列表中比第二个多出来的元素
     *
     * @param list1
     * @param list2
     * @return
     */
    public static <T> List<T> getLeftDiff(List<T> list1, List<T> list2) {
        if (isEmpty(list2)) {
            return list1;
        }
        List<T> list = new ArrayList<T>();
        if (isNotEmpty(list1)) {
            for (T o : list1) {
                if (!list2.contains(o)) {
                    list.add(o);
                }
            }
        }
        return list;
    }
}
