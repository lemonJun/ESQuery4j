//package lemon.elastic4j.test.cv;
//
//import java.util.List;
//
//import org.elasticsearch.index.query.QueryBuilders;
//import org.junit.Test;
//
//import com.alibaba.fastjson.JSON;
//import com.bj58.chr.scf.cvsearch.entity.CVIndexBean;
//
//import core.ESTemplateClient;
//import lemon.elastic.query4j.Init;
//import lemon.elastic.query4j.esproxy.core.query.NativeSearchQueryBuilder;
//import lemon.elastic.query4j.esproxy.core.query.SearchQuery;
//import lemon.elastic4j.test.jobmv.CompanyEsInfo;
//
//public class CVQueryTest {
//
//    static {
//        Init.init();
//    }
//
//    //    @Test
//    //    public void crite() {
//    //        try {
//    //            CVCriteriaBean bean = new CVCriteriaBean();
//    //            bean.setCvname("java");
//    //            CriteriaQueryGene gene = new ElasticCriteriaQueryGene();
//    //            CriteriaQuery query = gene.geneESQueryPageable(JSON.toJSONString(bean), CVCriteriaBean.class, 0, 10);
//    //            Page<CVIndexBean> list = ESTemplateClient.getInstance().getTemplate().queryForPage(query, CVIndexBean.class);
//    //            for (CVIndexBean cv : list) {
//    //                System.out.println(JSON.toJSON(cv));
//    //            }
//    //        } catch (Exception e) {
//    //            e.printStackTrace();
//    //        }
//    //    }
//    //
//    //    @Test
//    //    public void crite2() {
//    //        try {
//    //            Criteria crit = new Criteria("explocal").is(169);
//    //            CriteriaQuery query = new CriteriaQuery(crit);
//    //            
//    //            Page<CVIndexBean> page = ESTemplateClient.getInstance().getTemplate().queryForPage(query, CVIndexBean.class);
//    //            for (CVIndexBean cv : page.getContent()) {
//    //                System.out.println(JSON.toJSON(cv));
//    //            }
//    //        } catch (Exception e) {
//    //            e.printStackTrace();
//    //        }
//    //    }
//
//    @Test
//    public void qslliving() {
//        try {
//            NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();
//            builder.withQuery(QueryBuilders.termQuery("allexp", "司"));
//            SearchQuery searchQuery = builder.build();
//            List<CVIndexBean> list = ESTemplateClient.getInstance().getTemplate().queryForList(searchQuery, CVIndexBean.class);
//            for (CVIndexBean cv : list) {
//                System.out.println(JSON.toJSON(cv));
//            }
//        } catch (Exception e) {
//        }
//    }
//
//    @Test
//    public void qslrecentcomp() {
//        try {
//            NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();
//            //            builder.withQuery(QueryBuilders.termQuery("recentcomp", "学院"));//这个就是按明确分词后的结果查询
//            builder.withQuery(QueryBuilders.matchQuery("comName", "学院"));
//            //            builder.withQuery(QueryBuilders.fuzzyQuery("recentcomp", "学院"));
//            SearchQuery searchQuery = builder.build();
//            List<CompanyEsInfo> list = ESTemplateClient.getInstance().getTemplate().queryForList(searchQuery, CompanyEsInfo.class);
//            for (CompanyEsInfo cv : list) {
//                System.out.println(JSON.toJSON(cv));
//            }
//        } catch (Exception e) {
//        }
//    }
//}
