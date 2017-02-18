package lemon.elastic.query4j.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

public class SerializeUtil {

    private static final Logger logger = LoggerFactory.getLogger(SerializeUtil.class);

    // fastjson序列化
    public static String serializeJSON(Object object) {
        try {
            String bytes = JSON.toJSONString(object, SerializerFeature.WriteClassName);
            return bytes;
        } catch (Exception e) {
            logger.error("serializeJSON", e);
        }
        return null;
    }

    // fastjson反序列化
    @SuppressWarnings("unchecked")
    public static <T> T deserializeJSON(String value) {
        if (StringUtil.isNullOrEmpty(value)) {
            return null;
        }
        try {
            T ObjT = (T) JSON.parse(value);
            return (T) ObjT;
        } catch (Exception e) {
            logger.error("deserializeJSON", e);
        }
        return null;
    }

}
