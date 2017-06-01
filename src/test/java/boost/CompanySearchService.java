//package com.bj58.chr.sm.service;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import org.apache.commons.lang.ArrayUtils;
//import org.apache.commons.lang.StringUtils;
//import org.elasticsearch.action.search.SearchResponse;
//import org.elasticsearch.index.query.BoolQueryBuilder;
//import org.elasticsearch.index.query.QueryBuilder;
//import org.elasticsearch.index.query.QueryBuilders;
//import org.elasticsearch.search.SearchHit;
//import org.elasticsearch.search.SearchHits;
//import org.elasticsearch.search.sort.SortBuilder;
//import org.elasticsearch.search.sort.SortBuilders;
//import org.elasticsearch.search.sort.SortOrder;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import com.bj58.chr.sm.bean.Distribution;
//import com.bj58.chr.sm.bean.enu.ResponseCode;
//import com.bj58.chr.sm.bean.response.CompanySuggestResponse;
//import com.bj58.chr.sm.contract.ISMCompanySearchService;
//import com.bj58.chr.sm.helper.ESClientHelper;
//import com.bj58.chr.sm.helper.JedisClientHelper;
//import com.bj58.chr.sm.rediscommon.JedisPoolClient;
//import com.bj58.chr.sm.util.Constant;
//import com.bj58.spat.scf.server.contract.annotation.ServiceBehavior;
//
//@ServiceBehavior
//public class CompanySearchService implements ISMCompanySearchService {
//
//    private static final Logger log = LoggerFactory.getLogger(CompanySearchService.class.getName());
//
//    private static JedisPoolClient redis = JedisClientHelper.getInstance().getClient();
//
//    private QueryBuilder makeQuery(String company) {
//        QueryBuilder query = QueryBuilders.regexpQuery("companys", company + ".*");
//        return query;
//    }
//
//    private CompanySuggestResponse makeResponse(SearchResponse searchResponse) {
//
//        CompanySuggestResponse resp = new CompanySuggestResponse();
//
//        if (searchResponse == null) {
//            log.info("company suggest response null");
//            resp.setCode(ResponseCode.SUCCESS.getCode());
//            resp.setMsg(ResponseCode.SUCCESS.getMsg());
//            return resp;
//        }
//        SearchHits hits = searchResponse.getHits();
//        if (hits == null || hits.totalHits() <= 0) {
//            log.info("company suggest response hits is null");
//            resp.setCode(ResponseCode.SUCCESS.getCode());
//            resp.setMsg(ResponseCode.SUCCESS.getMsg());
//            return resp;
//        }
//
//        List<Distribution> companys = new ArrayList<Distribution>(20);
//
//        for (SearchHit hit : hits) {
//            String company = hit.getSource().get("companys").toString();
//            long cvCount = Long.valueOf(hit.getSource().get("cvcount").toString());
//            companys.add(new Distribution(company, cvCount));
//            if (log.isDebugEnabled()) {
//                log.debug("score:{}:\t  {}", hit.getScore(), hit.getSource());
//            }
//        }
//        resp.setCompanys(companys);
//
//        if (log.isDebugEnabled()) {
//            log.debug("company suggest interface response:{}", resp);
//        }
//
//        return resp;
//    }
//
//    @Override
//    public CompanySuggestResponse suggest(String company) {
//
//        if (StringUtils.isBlank(company)) {
//            return new CompanySuggestResponse(ResponseCode.MISS_PARAM.getCode(), ResponseCode.MISS_PARAM.getMsg());
//        }
//
//        QueryBuilder query = makeQuery(company);
//        log.info("company suggest string:{}", query.toString());
//
//        SortBuilder sort = makeSort();
//
//        String _index = getCompanyIndexByCache();
//
//        long startTime = System.currentTimeMillis();
//        SearchResponse searchResponse = ESClientHelper.getClient().prepareSearch(_index).setTypes("company_data").setQuery(query).setSize(Constant.COMPANY_SUGGEST_SIZE).addSort(sort).execute().actionGet();
//        log.info("company suggest cost:{}ms", System.currentTimeMillis() - startTime);
//
//        CompanySuggestResponse resp = makeResponse(searchResponse);
//
//        return resp;
//    }
//
//    private String getCompanyIndexByCache() {
//        String _index = null;
//        try {
//            _index = redis.get(Constant.COMPANY_ES_INDEX_KEY);
//        } catch (Exception e) {
//            log.error("get company es index by cache exception:{}", e.toString(), e);
//        }
//
//        if (StringUtils.isBlank(_index)) {
//            log.error("get company es index by cache is blank , use default");
//            _index = Constant.COMPANY_ES_INDEX_KEY_DEFAULT;
//        }
//        return _index;
//    }
//
//    public List<Distribution> targetCompany(String[] companys) {
//
//        if (ArrayUtils.isEmpty(companys)) {
//            return null;
//        }
//
//        BoolQueryBuilder query = new BoolQueryBuilder();
//
//        for (String company : companys) {
//            query.should(QueryBuilders.regexpQuery("companys", company));
//        }
//
//        SortBuilder sort = makeSort();
//
//        String _index = getCompanyIndexByCache();
//
//        SearchResponse searchResponse = ESClientHelper.getClient().prepareSearch(_index).setTypes("company_data").setQuery(query).setSize(Constant.TARGET_COMPANY_SIZE).addSort(sort).execute().actionGet();
//
//        if (searchResponse == null) {
//            log.info("company target es response null");
//            return null;
//        }
//
//        SearchHits hits = searchResponse.getHits();
//        if (hits == null || hits.totalHits() <= 0) {
//            log.info("company target es response hits is null");
//            return null;
//        }
//
//        List<Distribution> list = new ArrayList<Distribution>(20);
//
//        for (SearchHit hit : hits) {
//            String company = hit.getSource().get("companys").toString();
//            long cvCount = Long.valueOf(hit.getSource().get("cvcount").toString());
//            list.add(new Distribution(company, cvCount));
//            if (log.isDebugEnabled()) {
//                log.debug("score:{}:\t  {}", hit.getScore(), hit.getSource());
//            }
//        }
//        return list;
//    }
//
//    private SortBuilder makeSort() {
//        SortBuilder sort = SortBuilders.fieldSort("cvcount").order(SortOrder.DESC);
//        return sort;
//    }
//}
