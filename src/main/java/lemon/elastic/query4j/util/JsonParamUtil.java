package lemon.elastic.query4j.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class JsonParamUtil {

    public static JSONObject getJSONParam(String paramJson) {
        if (StringUtil.isNotNullOrEmpty(paramJson)) {
            JSONObject jsonObj = JSON.parseObject(paramJson);
            return jsonObj;
        }
        return null;
    }

    public static void putParamLong(String paramJson, String key, long value) {
        putParamValue(paramJson, key, value);
    }

    public static void putParamValue(String paramJson, String key, Object value) {
        if (StringUtil.isNotNullOrEmpty(paramJson)) {
            JSONObject obj = JSON.parseObject(paramJson);
            obj.put(key, value);
            paramJson = JSON.toJSONString(obj);
        } else {
            JSONObject obj = new JSONObject();
            obj.put(key, value);
            paramJson = JSON.toJSONString(obj);
        }
    }

    public static void removeParamByKey(String paramJson, String key) {
        if (StringUtil.isNotNullOrEmpty(paramJson)) {
            JSONObject obj = JSON.parseObject(paramJson);
            obj.remove(key);
            paramJson = JSON.toJSONString(obj);
        }
        paramJson = null;
    }
}
