package lemon.elastic4j.test.jobmv;

import java.util.List;

import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Test;

import com.alibaba.fastjson.JSON;

import core.ESTemplateClient;
import lemon.elastic.query4j.BootStrap;
import lemon.elastic.query4j.esproxy.core.query.NativeSearchQueryBuilder;
import lemon.elastic.query4j.esproxy.core.query.SearchQuery;

/**
 * 
 *
 * @author WangYazhou
 * @date  2016年10月17日 下午6:35:47
 * @see
 */
public class ComFilter {

    static {
        BootStrap.init();
    }

    //聚合结果无效啊
    @Test
    public void filtname() {
        try {
            NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();
            builder.withQuery(QueryBuilders.matchQuery("comName", "宝洁"));
            builder.withFilter(FilterBuilders.termFilter("source", 1));
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
