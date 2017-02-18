package lemon.elastic4j.test.jobmvBM25;

import java.util.List;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Test;

import com.alibaba.fastjson.JSON;

import core.ESTemplateClient;
import lemon.elastic.query4j.BootStrap;
import lemon.elastic.query4j.esproxy.core.query.Criteria;
import lemon.elastic.query4j.esproxy.core.query.CriteriaQuery;
import lemon.elastic.query4j.esproxy.core.query.NativeSearchQueryBuilder;
import lemon.elastic.query4j.esproxy.core.query.SearchQuery;

public class ComQuery {

    static {
        BootStrap.init();
    }

    @Test
    public void criname() {
        try {
            Criteria crit = new Criteria("comName").is("宝洁").or(new Criteria("source").is(2).not());
            CriteriaQuery query = new CriteriaQuery(crit);
            query.setMinScore(1.0f);

            List<CompanyEsInfoV2> list = ESTemplateClient.getInstance().getTemplate().queryForList(query, CompanyEsInfoV2.class);
            for (CompanyEsInfoV2 cv : list) {
                System.out.println(JSON.toJSON(cv));
            }
        } catch (Exception e) {
        }
    }

    @Test
    public void qsl1name() {
        try {
            NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();
            SearchQuery searchQuery = builder.withQuery(QueryBuilders.matchQuery("comName", "宝洁")).withMinScore(1.0f).build();

            List<CompanyEsInfoV2> list = ESTemplateClient.getInstance().getTemplate().queryForList(searchQuery, CompanyEsInfoV2.class);
            for (CompanyEsInfoV2 cv : list) {
                System.out.println(JSON.toJSON(cv));
            }
        } catch (Exception e) {
        }
    }

    @Test
    public void qslname() {
        try {
            NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();
            BoolQueryBuilder bqb = new BoolQueryBuilder();
            bqb.should(QueryBuilders.matchQuery("comName", "宝洁"));
            //            bqb.should(QueryBuilders.termQuery("source", 2));
            SearchQuery searchQuery = builder.withQuery(bqb).withMinScore(1.0f).build();

            List<CompanyEsInfoV2> list = ESTemplateClient.getInstance().getTemplate().queryForList(searchQuery, CompanyEsInfoV2.class);
            for (CompanyEsInfoV2 cv : list) {
                System.out.println(JSON.toJSON(cv));
            }
        } catch (Exception e) {
        }
    }

}
