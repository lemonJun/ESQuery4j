package lemon.elastic4j.test.jobmv;

import java.util.List;

import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.junit.Test;

import com.alibaba.fastjson.JSON;

import core.ESTemplateClient;
import lemon.elastic.query4j.Init;
import lemon.elastic.query4j.esproxy.core.query.NativeSearchQueryBuilder;
import lemon.elastic.query4j.esproxy.core.query.SearchQuery;

public class ComAggr {

    static {
        Init.init();
    }
    
    //聚合结果无效啊
    @Test
    public void agrname() {
        try {
            NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();
            builder.addAggregation(AggregationBuilders.avg("source_avg"));
            SearchQuery searchQuery = builder.build();

            List<CompanyEsInfo> list = ESTemplateClient.getInstance().getTemplate().queryForList(searchQuery, CompanyEsInfo.class);
            for (CompanyEsInfo cv : list) {
                System.out.println(JSON.toJSON(cv));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
