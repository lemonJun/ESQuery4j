package lemon.elastic4j.test.chew;

import java.util.Date;
import java.util.Map;

import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Preconditions;

import core.ESTemplateClient;
import lemon.elastic.query4j.BootStrap;
import lemon.elastic.query4j.esproxy.core.query.CriteriaQuery;
import lemon.elastic.query4j.esproxy.core.query.IndexQuery;
import lemon.elastic.query4j.esproxy.domain.Page;
import lemon.elastic.query4j.provider.CriteriaQueryGene;
import lemon.elastic.query4j.provider.ElasticCriteriaQueryGene;

public class ChewTest {
    static {
        try {
            BootStrap.init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void query() {
        try {
            SplitCrit bean = new SplitCrit();
            bean.setId("24");
            CriteriaQueryGene gene = new ElasticCriteriaQueryGene();
            CriteriaQuery query = gene.geneESQueryPageable(JSON.toJSONString(bean), SplitCrit.class, 0, 10);
            Preconditions.checkNotNull(query, "criteriaquery can not be null");
            Page<SplitIndex> list = ESTemplateClient.getInstance().getTemplate().queryForPage(query, SplitIndex.class);
            for (SplitIndex cv : list) {
                System.out.println(JSON.toJSON(cv));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void index() {
        try {
            for (int i = 1; i < 10; i++) {
                for (int j = 6; j < 18; j++) {
                    SplitIndex sp = new SplitIndex();
                    sp.setCate(i + "");
                    sp.setCvname("name" + i);
                    sp.setId(i * j + "");
                    sp.setDate(new Date());
                    IndexQuery query = new IndexQuery();
                    query.setId(sp.getId());
                    query.setObject(sp);
                    ESTemplateClient.getInstance().getTemplate().index(query);
                }
            }
            ESTemplateClient.getInstance().getTemplate().refresh(SplitIndex.class, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("rawtypes")
    public static void build() {
        try {
            ESTemplateClient.getInstance().getTemplate().deleteIndex(SplitIndex.class);
            ESTemplateClient.getInstance().getTemplate().createIndex(SplitIndex.class);
            ESTemplateClient.getInstance().getTemplate().putMapping(SplitIndex.class);

            //
            Map smap = ESTemplateClient.getInstance().getTemplate().getSetting(SplitIndex.class);
            System.out.println(JSON.toJSON(smap));
            Map map = ESTemplateClient.getInstance().getTemplate().getMapping(SplitIndex.class);
            System.out.println(JSON.toJSON(map));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
