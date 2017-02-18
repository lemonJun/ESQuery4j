package lemon.elastic.query4j.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class PropertaiesHelper {
    private Properties pro = null;

    public PropertaiesHelper(String path) throws Exception {
        this.pro = loadProperty(path);
    }

    public PropertaiesHelper(InputStream inputStream) {
        this.pro = new Properties();
        try {
            this.pro.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getString(String key, String defaultValue) throws Exception {
        try {
            return this.pro.getProperty(key.trim(), defaultValue);
        } catch (Exception e) {
        }
        throw new Exception("key:" + key);
    }

    public String getString(String key) throws Exception {
        try {
            return this.pro.getProperty(key.trim());
        } catch (Exception e) {
        }
        throw new Exception("key:" + key);
    }

    public int getInt(String key) throws Exception {
        try {
            return Integer.parseInt(this.pro.getProperty(key.trim()));
        } catch (Exception e) {
        }
        throw new Exception("key:" + key);
    }

    public int getInt(String key, String defaultValue) throws Exception {
        try {
            return Integer.parseInt(this.pro.getProperty(key.trim(), defaultValue));
        } catch (Exception e) {
        }
        throw new Exception("key:" + key);
    }

    public double getDouble(String key) throws Exception {
        try {
            return Double.parseDouble(this.pro.getProperty(key.trim()));
        } catch (Exception e) {
        }
        throw new Exception("key:" + key);
    }

    public double getDouble(String key, String defaultValue) throws Exception {
        try {
            return Double.parseDouble(this.pro.getProperty(key.trim(), defaultValue));
        } catch (Exception e) {
        }
        throw new Exception("key:" + key);
    }

    public long getLong(String key) throws Exception {
        try {
            return Long.parseLong(this.pro.getProperty(key.trim()));
        } catch (Exception e) {
        }
        throw new Exception("key:" + key);
    }

    public long getLong(String key, String defaultValue) throws Exception {
        try {
            return Long.parseLong(this.pro.getProperty(key.trim(), defaultValue));
        } catch (Exception e) {
        }
        throw new Exception("key:" + key);
    }

    public float getFloat(String key) throws Exception {
        try {
            return Float.parseFloat(this.pro.getProperty(key.trim()));
        } catch (Exception e) {
        }
        throw new Exception("key:" + key);
    }

    public float getFloat(String key, String defaultValue) throws Exception {
        try {
            return Float.parseFloat(this.pro.getProperty(key.trim(), defaultValue));
        } catch (Exception e) {
        }
        throw new Exception("key:" + key);
    }

    public boolean getBoolean(String key) throws Exception {
        try {
            return Boolean.parseBoolean(this.pro.getProperty(key.trim()));
        } catch (Exception e) {
        }
        throw new Exception("key:" + key);
    }

    public boolean getBoolean(String key, String defaultValue) throws Exception {
        try {
            return Boolean.parseBoolean(this.pro.getProperty(key.trim(), defaultValue));
        } catch (Exception e) {
        }
        throw new Exception("key:" + key);
    }

    public Set<Object> getAllKey() {
        return this.pro.keySet();
    }

    public Collection<Object> getAllValue() {
        return this.pro.values();
    }

    public Map<String, Object> getAllKeyValue() {
        Map mapAll = new HashMap();
        Set keys = getAllKey();

        Iterator it = keys.iterator();
        while (it.hasNext()) {
            String key = it.next().toString();
            mapAll.put(key, this.pro.get(key.trim()));
        }
        return mapAll;
    }

    private Properties loadProperty(String filePath) throws Exception {
        FileInputStream fin = null;
        Properties pro = new Properties();
        try {
            fin = new FileInputStream(filePath);
        } catch (FileNotFoundException e) {
            throw e;
        }
        try {
            if (fin != null) {
                pro.load(fin);
                fin.close();
            }
        } catch (IOException e) {
            throw e;
        }
        return pro;
    }
}
