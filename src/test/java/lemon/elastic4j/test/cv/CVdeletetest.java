package lemon.elastic4j.test.cv;

import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Test;

import com.bj58.chr.scf.cvsearch.entity.CVIndexBean;

import core.ESTemplateClient;
import lemon.elastic.query4j.BootStrap;
import lemon.elastic.query4j.esproxy.core.query.DeleteQuery;
import lemon.elastic.query4j.esproxy.core.query.NativeSearchQueryBuilder;

public class CVdeletetest {

    static {
        BootStrap.init();
    }

    //此处确实可以按条件删除  但无法得到按条件删除的dsl
    @Test
    public void deletebyid() {
        try {
            //            ESTemplateClient.getInstance().getTemplate().delete(CVIndexBean.class, "AVesxE8omS-IH0yx7Ybn");
            ESTemplateClient.getInstance().getTemplate().delete(CVIndexBean.class, "AVesxE8omS-IH0yx7Ybs");
            ESTemplateClient.getInstance().getTemplate().refresh(CVIndexBean.class, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void deletebysearch() {
        try {
            DeleteQuery dquery = new DeleteQuery();
            dquery.setQuery(QueryBuilders.termQuery("recentcomp", "学院"));
            ESTemplateClient.getInstance().getTemplate().delete(dquery, CVIndexBean.class);
            ESTemplateClient.getInstance().getTemplate().refresh(CVIndexBean.class, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
