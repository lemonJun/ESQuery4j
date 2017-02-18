package lemon.elastic.query4j.util;

/**
 * 简单工具类
 * @author wangyazhou
 * @version 1.0
 * @date  2016年1月11日 下午4:50:05
 * @see 
 * @since
 */
public abstract class StringUtil {

    public static boolean isNullOrEmpty(String value) {
        if (null == value || value.trim().length() < 1) {
            return true;
        }
        return false;
    }

    public static boolean isNullOrEmptyOrNullStr(String value) {
        if (null == value || value.trim().length() < 1 || "null".equals(value.toLowerCase())) {
            return true;
        }
        return false;
    }

    public static boolean isNotNullOrEmpty(String value) {
        return !isNullOrEmpty(value);
    }

    public static String trimAllWhitespace(String str) {
        if (!hasLength(str)) {
            return str;
        }
        StringBuilder sb = new StringBuilder(str);
        int index = 0;
        while (sb.length() > index) {
            if (Character.isWhitespace(sb.charAt(index))) {
                sb.deleteCharAt(index);
            } else {
                index++;
            }
        }
        return sb.toString();
    }

    public static boolean hasLength(CharSequence str) {
        return (str != null && str.length() > 0);
    }

}
