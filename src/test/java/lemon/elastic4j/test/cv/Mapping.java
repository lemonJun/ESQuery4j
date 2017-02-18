package lemon.elastic4j.test.cv;

import com.alibaba.fastjson.JSON;
import com.bj58.chr.scf.cvsearch.entity.CVIndexBean;

import core.ESTemplateClient;
import lemon.elastic.query4j.Init;

public class Mapping {

    public static void main(String[] args) {
        Init.init();

        new Mapping().build();
    }
    
    public void build() {
        try {
            ESTemplateClient.getInstance().getTemplate().deleteIndex(CVIndexBean.class);
            ESTemplateClient.getInstance().getTemplate().createIndex(CVIndexBean.class);
            ESTemplateClient.getInstance().getTemplate().putMapping(CVIndexBean.class);
            JSON.toJSONString(ESTemplateClient.getInstance().getTemplate().getMapping(CVIndexBean.class));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
