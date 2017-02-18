package lemon.elastic.query4j.util;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

public class RangeUtil {

    /**
     * 获取一段范围内的整数  传入的是以-分隔的一个数据段 
     * @param value  清洗过后的3.4-6.7这样的数
     * @param notLimit 是否把 不限 考虑进去
     * @return
     */
    public static Set<String> getIntRange(String value, boolean notLimit) {
        Set<String> result = new HashSet<String>();
        String[] values = value.split("-");
        if (values.length >= 2) {
            int start = new BigDecimal(values[0]).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
            int end = new BigDecimal(values[1]).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
            if (start <= end) {
                for (int i = start; i <= end; i++) {
                    result.add(String.valueOf(i));
                }
            }
        }

        if (notLimit) {
            result.add("0");
        }
        return result;
    }
}
