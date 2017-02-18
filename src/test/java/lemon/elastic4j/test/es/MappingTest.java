package lemon.elastic4j.test.es;

import org.junit.Test;

import com.alibaba.fastjson.JSON;

import core.ESTemplateClient;
import lemon.elastic.query4j.Init;
import lemon.elastic4j.test.chew.SplitIndex;

public class MappingTest {

    static {
        try {
            Init.init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void build() {
        try {
            ESTemplateClient.getInstance().getTemplate().deleteIndex(SplitIndex.class);
            ESTemplateClient.getInstance().getTemplate().createIndex(SplitIndex.class);
            ESTemplateClient.getInstance().getTemplate().putMapping(SplitIndex.class);
            System.out.println(JSON.toJSONString(ESTemplateClient.getInstance().getTemplate().getMapping(SplitIndex.class)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
