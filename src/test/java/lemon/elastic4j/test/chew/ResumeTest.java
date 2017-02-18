package lemon.elastic4j.test.chew;

import java.util.Date;
import java.util.Map;

import org.junit.Test;

import com.alibaba.fastjson.JSON;

import core.ESTemplateClient;
import lemon.elastic.query4j.esproxy.core.query.IndexQuery;

public class ResumeTest {

    @SuppressWarnings("rawtypes")
    @Test
    public void init() {
        try {
            ESTemplateClient.getInstance().getTemplate().deleteIndex(ElasticResume.class);
            ESTemplateClient.getInstance().getTemplate().createIndex(ElasticResume.class);
            ESTemplateClient.getInstance().getTemplate().refresh(ElasticResume.class, true);
            ESTemplateClient.getInstance().getTemplate().putMapping(ElasticResume.class);

            Map smap = ESTemplateClient.getInstance().getTemplate().getSetting(ElasticResume.class);
            System.out.println(JSON.toJSON(smap));
            Map map = ESTemplateClient.getInstance().getTemplate().getMapping(ElasticResume.class);
            System.out.println(JSON.toJSON(map));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void add() {
        try {
            IndexQuery query = new ElasticResumeBuilder().id(12).uid(2342).age(12).gender(1).farea("1,2").fcate("123,11").exp(1).edu(2).date(new Date()).sal(11).location("11.001,1.11").title("haah").buildIndex();
            //            ESTemplateClient.client().index(query);
            //            ESTemplateClient.client().refresh(ElasticResume.class, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void areasplit() {
        String[] areas = "_9630_135_1_9630_".split("_");
        for (String a : areas) {
            System.out.println("--" + a);
        }
    }
}
