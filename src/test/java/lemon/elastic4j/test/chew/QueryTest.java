//package lemon.elastic4j.test.chew;
//
//import java.util.HashSet;
//import java.util.Set;
//
//import org.elasticsearch.index.query.QueryBuilders;
//import org.junit.Test;
//
//import com.alibaba.fastjson.JSON;
//
//import core.ESTemplateClient;
//import lemon.elastic.query4j.esproxy.core.query.Criteria;
//import lemon.elastic.query4j.esproxy.core.query.CriteriaQuery;
//import lemon.elastic.query4j.esproxy.core.query.NativeSearchQueryBuilder;
//import lemon.elastic.query4j.esproxy.core.query.SearchQuery;
//import lemon.elastic.query4j.esproxy.domain.Page;
//
//public class QueryTest {
//
//    @Test
//    public void query_farea_crit() {
//        Criteria cate = new Criteria("farea").contains("_40_").boost(1f);
//        //        Criteria crit = new Criteria("farea").contains("_1_").boost(1f);
//        CriteriaQuery critquery = new CriteriaQuery(cate);
//
//        //        critquery.addCriteria(crit);
//
//        System.out.println(JSON.toJSONString(critquery));
//        Page<ElasticResume> querylist = ESTemplateClient.getInstance().getTemplate().queryForPage(critquery, ElasticResume.class);
//
//        if (querylist == null) {
//            return;
//        }
//
//        for (ElasticResume entity : querylist) {
//            System.out.println(entity.toString());
//        }
//    }
//
//    @Test
//    public void in_age_crit() {
//        Set<String> set = new HashSet<String>();
//        set.add("5");
//        set.add("0");
//        try {
//            Criteria cate = new Criteria("edu").in(set);
//            CriteriaQuery critquery = new CriteriaQuery(cate);
//
//            //        critquery.addCriteria(crit);
//
//            System.out.println(JSON.toJSONString(critquery));
//            Page<ElasticResume> querylist = ESTemplateClient.getInstance().getTemplate().queryForPage(critquery, ElasticResume.class);
//
//            if (querylist == null) {
//                return;
//            }
//
//            for (ElasticResume entity : querylist) {
//                System.out.println(entity.toString());
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Test
//    public void query_farea_search() {
//        try {
//            NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();
//            //        GeoDistanceFilterBuilder filter = FilterBuilders.geoDistanceFilter("location").point(30.766912, 120.731529).distance(5.0d, DistanceUnit.KILOMETERS);
//            //        GeoDistanceSortBuilder sort = SortBuilders.geoDistanceSort("location").point(30.766912, 120.731529).order(SortOrder.ASC).unit(DistanceUnit.METERS);
//
//            builder.withQuery(QueryBuilders.matchQuery("fcate", 2198));
//            //            builder.withQuery(QueryBuilders.termQuery("farea", "1"));
//            //            builder.withQuery(QueryBuilders.prefixQuery("farea", "1,"));
//            SearchQuery searchQuery = builder.build();
//
//            Page<ElasticResume> querylist = ESTemplateClient.getInstance().getTemplate().queryForPage(searchQuery, ElasticResume.class);
//
//            if (querylist == null) {
//                return;
//            }
//
//            for (ElasticResume entity : querylist) {
//                System.out.println(entity.toString());
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}
