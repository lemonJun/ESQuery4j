package core;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.AbstractIdleService;

import lemon.elastic.query4j.esproxy.core.ElasticsearchTemplate;
import lemon.elastic.query4j.util.PropertaiesHelper;
import lemon.elastic.query4j.util.StringUtil;

/**
 * 封装spring-data-es提供的模板调用客户端
 * 做为一个客户端  不需要提供成单例的  这个由用户自已来实现就可以了
 * 
 * @author WangYazhou
 * @date 2016年1月7日 下午5:29:01
 * @see
 */
public class ESTemplateClient extends AbstractIdleService {

    private static final Logger logger = LoggerFactory.getLogger(ESTemplateClient.class);

    private volatile ElasticsearchTemplate template;
    private volatile TransportClient client;

    private static volatile ESTemplateClient instance = null;

    private ESTemplateClient() {
        try {
            String path = System.getProperty("user.dir") + "/config/elastic.properties";
            PropertaiesHelper pro = new PropertaiesHelper(path);
            String address = pro.getString("elastic.address");
            String cluster = pro.getString("cluster.name", "chr_elastic");
            String node = pro.getString("node.name", "default");

            if (StringUtil.isNullOrEmpty(address)) {
                throw new Exception("address invalid");
            }

            String[] ads = address.split(",");
            if (ads == null || ads.length < 1) {
                throw new Exception("address invalid");
            }
            Settings settings = ImmutableSettings.settingsBuilder().put("cluster.name", cluster).build();
            //            Settings settings = ImmutableSettings.settingsBuilder().put("cluster.name", cluster).put("node.name", node).build();
            client = new TransportClient(settings);

            for (String ad : ads) {
                client.addTransportAddress(new InetSocketTransportAddress(ad.split(":")[0], Integer.parseInt(ad.split(":")[1])));
            }

            template = new ElasticsearchTemplate(client);
            template.setSearchTimeout("2000");//2S超时
            Thread.sleep(2000);
            logger.info(String.format("es init success culster.name=%s,address=%s", cluster, address));
        } catch (Exception e) {
            logger.error("elastic init failed", e);
        } finally {

        }
    }
    
    //延时
    public static ESTemplateClient getInstance() {
        if (instance == null) {
            synchronized (ESTemplateClient.class) {
                if (instance == null) {
                    instance = new ESTemplateClient();
                }
            }
        }
        return instance;
    }
    
    public ElasticsearchTemplate getTemplate() {
        return template;
    }

    @Override
    protected void shutDown() throws Exception {
        try {
            client.close();
        } catch (Exception e) {
            logger.error("client close", e);
        }
    }

    @Override
    protected void startUp() throws Exception {

    }

}
