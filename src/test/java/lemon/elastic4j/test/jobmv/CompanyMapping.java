package lemon.elastic4j.test.jobmv;

import org.junit.Test;

import com.alibaba.fastjson.JSON;

import core.ESTemplateClient;
import lemon.elastic.query4j.BootStrap;

public class CompanyMapping {

    static {
        BootStrap.init();
    }

    @Test
    public void mapping() {
        try {
            ESTemplateClient.getInstance().getTemplate().deleteIndex(CompanyEsInfo.class);
            ESTemplateClient.getInstance().getTemplate().createIndex(CompanyEsInfo.class);
            ESTemplateClient.getInstance().getTemplate().putMapping(CompanyEsInfo.class);
            JSON.toJSONString(ESTemplateClient.getInstance().getTemplate().getMapping(CompanyEsInfo.class));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void refresh() {
        try {
            ESTemplateClient.getInstance().getTemplate().refresh(CompanyEsInfo.class, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
