package boost;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Test;

import com.alibaba.fastjson.JSON;

import core.ESTemplateClient;
import lemon.elastic.query4j.Init;
import lemon.elastic.query4j.esproxy.core.query.IndexQuery;
import lemon.elastic.query4j.esproxy.core.query.IndexQueryBuilder;
import lemon.elastic.query4j.esproxy.core.query.NativeSearchQueryBuilder;
import lemon.elastic.query4j.esproxy.core.query.SearchQuery;

public class BoostMain {

    static {
        Init.init();
    }

    @Test
    public void mapping() {
        try {
            ESTemplateClient.getInstance().getTemplate().deleteIndex(BoostBean.class);
            ESTemplateClient.getInstance().getTemplate().createIndex(BoostBean.class);
            ESTemplateClient.getInstance().getTemplate().putMapping(BoostBean.class);
            JSON.toJSONString(ESTemplateClient.getInstance().getTemplate().getMapping(BoostBean.class));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void index() {
        BoostBean bean1 = new BoostBean();
        bean1.setId("11111");
        bean1.setName("hello java world this is not a good idea apple");
        bean1.set_boost(72.0f);
        IndexQuery query = new IndexQueryBuilder().withId(bean1.getId()).withObject(bean1).build();

        BoostBean bean2 = new BoostBean();
        bean2.setId("1111221");
        bean2.setName("english need to be learn well in the world");
        bean2.set_boost(100.0f);
        IndexQuery query2 = new IndexQueryBuilder().withId(bean2.getId()).withObject(bean2).build();

        BoostBean bean3 = new BoostBean();
        bean3.setId("11112210");
        bean3.setName("learn java in the big world is import");
        bean3.set_boost(95.0f);
        IndexQuery query3 = new IndexQueryBuilder().withId(bean3.getId()).withObject(bean3).build();
        ESTemplateClient.getInstance().getTemplate().index(query);
        ESTemplateClient.getInstance().getTemplate().index(query2);
        ESTemplateClient.getInstance().getTemplate().index(query3);
        ESTemplateClient.getInstance().getTemplate().refresh(BoostBean.class, true);
    }

    @Test
    public void query() {
        try {
            NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();
            BoolQueryBuilder query = QueryBuilders.boolQuery();
            query.must(QueryBuilders.matchPhraseQuery("name", "java world").analyzer("mmseg").slop(5));
            SearchQuery searchQuery = builder.withQuery(query).build();
            ESTemplateClient.getInstance().getTemplate().queryForPage(searchQuery, BoostBean.class);
            Thread.sleep(Integer.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
