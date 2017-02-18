package lemon.elastic4j.test.jobmvBM25;

import com.alibaba.fastjson.JSON;

import core.ESTemplateClient;
import lemon.elastic.query4j.BootStrap;

public class CompanyV2Mapping {

    public static void main(String[] args) {
        BootStrap.init();
        new CompanyV2Mapping().build();
    }
    
    public void build() {
        try {
            ESTemplateClient.getInstance().getTemplate().deleteIndex(CompanyEsInfoV2.class);
            ESTemplateClient.getInstance().getTemplate().createIndex(CompanyEsInfoV2.class);
            ESTemplateClient.getInstance().getTemplate().putMapping(CompanyEsInfoV2.class);
            JSON.toJSONString(ESTemplateClient.getInstance().getTemplate().getMapping(CompanyEsInfoV2.class));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
