package lemon.elastic.query4j;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 初始化类
 *
 * @author WangYazhou
 * @date 2016年3月28日 下午9:24:37
 * @see
 */
public class BootStrap {

    private static final Logger logger = LoggerFactory.getLogger(BootStrap.class);

    public static void init(String filepath) {
        try {
            //            String path = System.getProperty("user.dir") + "/config/log4j.properties";
            PropertyConfigurator.configure(filepath);
            logger.info(filepath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void init() {
        try {
            String path = System.getProperty("user.dir") + "/config/log4j.properties";
            PropertyConfigurator.configure(path);
            logger.info(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
