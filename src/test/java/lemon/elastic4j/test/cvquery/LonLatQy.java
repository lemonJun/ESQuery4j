package lemon.elastic4j.test.cvquery;

import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.bj58.chr.scf.cvsearch.entity.CVCriteriaBean;
import com.bj58.chr.scf.cvsearch.entity.CVIndexBean;

import core.ESTemplateClient;
import lemon.elastic.query4j.BootStrap;
import lemon.elastic.query4j.esproxy.core.geo.GeoPoint;
import lemon.elastic.query4j.esproxy.core.query.Criteria;
import lemon.elastic.query4j.esproxy.core.query.CriteriaQuery;
import lemon.elastic.query4j.esproxy.domain.Page;
import lemon.elastic.query4j.provider.CriteriaQueryGene;
import lemon.elastic.query4j.provider.ElasticCriteriaQueryGene;

public class LonLatQy {

    //经纬度查询不通  为啥呢
    //
    @Test
    public void lnglatqy() {
        BootStrap.init();
        CVCriteriaBean bean = new CVCriteriaBean();
        bean.setLatlng("39.83369,116.481853,10");
        CriteriaQueryGene gene = new ElasticCriteriaQueryGene();
        CriteriaQuery query = gene.geneESQueryPageable(JSON.toJSONString(bean), CVCriteriaBean.class, 0, 10);
        //        Criteria crit = new Criteria("latlng").within(new GeoPoint(Double.valueOf("39.83369"), Double.valueOf("116.481853")), "10km");
        //        CriteriaQuery cq = new CriteriaQuery(crit);
        Page<CVIndexBean> list = ESTemplateClient.getInstance().getTemplate().queryForPage(query, CVIndexBean.class);
        for (CVIndexBean cv : list) {
            System.out.println(JSON.toJSON(cv));
        }
    }

    @Test
    public void lnglat2() {
        BootStrap.init();
        //        CVCriteriaBean bean = new CVCriteriaBean();
        //        bean.setLatlng("39.83369,116.481853,100");
        //        CriteriaQueryGene gene = new ElasticCriteriaQueryGene();
        //        CriteriaQuery query = gene.geneESQueryPageable(JSON.toJSONString(bean), CVCriteriaBean.class, 0, 10);
        Criteria crit = new Criteria("latlng").within(new GeoPoint(Double.valueOf("39.83369"), Double.valueOf("116.481853")), "10km");
        CriteriaQuery cq = new CriteriaQuery(crit);
        Page<CVIndexBean> list = ESTemplateClient.getInstance().getTemplate().queryForPage(cq, CVIndexBean.class);
        for (CVIndexBean cv : list) {
            System.out.println(JSON.toJSON(cv));
        }
    }
}
