//package com.bj58.chr.sm.service;
//
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.List;
//import java.util.Map;
//
//import org.apache.commons.lang.ArrayUtils;
//import org.apache.commons.lang.StringUtils;
//import org.elasticsearch.action.search.SearchRequestBuilder;
//import org.elasticsearch.action.search.SearchResponse;
//import org.elasticsearch.action.search.SearchType;
//import org.elasticsearch.index.query.BoolQueryBuilder;
//import org.elasticsearch.index.query.QueryBuilders;
//import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
//import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
//import org.elasticsearch.index.query.functionscore.gauss.GaussDecayFunctionBuilder;
//import org.elasticsearch.search.SearchHit;
//import org.elasticsearch.search.SearchHits;
//import org.elasticsearch.search.aggregations.AggregationBuilders;
//import org.elasticsearch.search.aggregations.Aggregations;
//import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
//import org.elasticsearch.search.sort.SortBuilder;
//import org.elasticsearch.search.sort.SortBuilders;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import com.bj58.chr.sm.bean.enu.*;
//import com.bj58.chr.sm.bean.request.SMConstant;
//import com.bj58.chr.sm.bean.request.SMSearchRequest;
//import com.bj58.chr.sm.bean.response.SMSearchResponse;
//import com.bj58.chr.sm.contract.ISMCvSearchService;
//import com.bj58.chr.sm.helper.ESClientHelper;
//import com.bj58.chr.sm.helper.JedisClientHelper;
//import com.bj58.chr.sm.helper.SearchHelper;
//import com.bj58.chr.sm.rediscommon.JedisPoolClient;
//import com.bj58.chr.sm.util.Constant;
//import com.bj58.chr.sm.util.DateTimeUtil;
//import com.bj58.spat.scf.server.contract.annotation.ServiceBehavior;
//
//@ServiceBehavior
//public class CvSearchService implements ISMCvSearchService {
//
//    private static final Logger log = LoggerFactory.getLogger(CvSearchService.class);
//    private static JedisPoolClient redis = JedisClientHelper.getInstance().getClient();
//
//    @Override
//    public SMSearchResponse search(SMSearchRequest request) {
//        if (request == null) {
//            return new SMSearchResponse(ResponseCode.MISS_PARAM.getCode(), ResponseCode.MISS_PARAM.getMsg());
//        }
//        log.info("cv es query req:{}", request);
//        SMSearchResponse resp = null;
//        try {
//            int pageNo = request.getPageNo();
//            int from = (pageNo > 0 ? pageNo - 1 : 0) * Constant.CV_PAGE_SIZE;
//            BoolQueryBuilder boolquery = makeQuery(request, false);
//            SortBuilder sort = makeSort();
//            String _index = getCVIndexByCache();
//            // FunctionScoreQueryBuilder functionScore = QueryBuilders.functionScoreQuery(boolquery, ScoreFunctionBuilders.gaussDecayFunction("refreshdate", "2d").setOffset("2d").setDecay(0.1));
//            FunctionScoreQueryBuilder functionQuery = createFunctionQuery(boolquery);
//            SearchRequestBuilder searchBuilder = ESClientHelper.getClient().prepareSearch(_index).setTypes(Constant.CVTYPE).setQuery(functionQuery).setFrom(from).setSize(Constant.CV_PAGE_SIZE).addSort(sort).setSearchType(SearchType.DFS_QUERY_THEN_FETCH);
//            log.info("buid:{} cv es query builder:{}", request.getBuid(), searchBuilder.toString());
//            long startTime = System.currentTimeMillis();
//            SearchResponse searchResponse = searchBuilder.execute().actionGet();
//            log.info("cv es query cost:{}ms ", System.currentTimeMillis() - startTime);
//            resp = makeResponse(searchResponse, null);
//            resp.setQuery(searchBuilder.toString());
//            log.info("buid:{} cv es query response:{}", request.getBuid(), resp);
//        } catch (Exception e) {
//            resp = new SMSearchResponse(ResponseCode.SYSTEM_ERROR.getCode(), ResponseCode.SYSTEM_ERROR.getMsg());
//            log.error(String.format(" buid:%s cv es query exception:%s ", request.getBuid(), e.toString()), e);
//        }
//        return resp;
//    }
//
//    @Override
//    public SMSearchResponse aggSearch(SMSearchRequest request, String aggType) {
//        if (request == null || StringUtils.isBlank(aggType)) {
//            return new SMSearchResponse(ResponseCode.MISS_PARAM.getCode(), ResponseCode.MISS_PARAM.getMsg());
//        }
//        log.info("cv es query req:{}", request);
//        SMSearchResponse resp = null;
//        try {
//            BoolQueryBuilder query = makeQuery(request, false);
//            // SortBuilder sort = makeSort();
//            TermsBuilder tb = null;
//            if (aggType.equals(SMConstant.AGGR_EDU)) {
//                tb = AggregationBuilders.terms(Constant.AGGR_EDU).field("edu");
//            } else if (aggType.equals(SMConstant.AGGR_WORK_YEAR)) {
//                tb = AggregationBuilders.terms(Constant.AGGR_WORK_YEAR).field("workYear").size(40);
//            } else if (aggType.equals(SMConstant.AGGR_SALARY)) {
//                tb = AggregationBuilders.terms(Constant.AGGR_SALARY).field("salaryRange").size(40);
//            } else if (aggType.equals(SMConstant.AGGR_JOB)) {
//                tb = AggregationBuilders.terms(Constant.AGGR_JOB).field("lastJobName").size(12);
//            } else if (aggType.equals(SMConstant.AGGR_COMPANYS)) {
//                tb = AggregationBuilders.terms(Constant.AGGR_COMPANYS).field("companys");
//            }
//            String _index = getCVIndexByCache();
//            SearchRequestBuilder searchBuilder = ESClientHelper.getClient().prepareSearch(_index).setTypes(Constant.CVTYPE).setQuery(query).addAggregation(tb).setSearchType(SearchType.QUERY_AND_FETCH).setSize(0);
//            log.info("buid:{} cv es aggSearchquery builder:{}", request.getBuid(), searchBuilder.toString());
//            long startTime = System.currentTimeMillis();
//            SearchResponse searchResponse = searchBuilder.execute().actionGet();
//            log.info("cv es aggSearchquery cost:{}ms", System.currentTimeMillis() - startTime);
//            resp = makeResponse(searchResponse, aggType);
//            resp.setQuery(searchBuilder.toString());
//            log.info("buid:{} cv es aggSearchquery response:{}", request.getBuid(), resp);
//        } catch (Exception e) {
//            resp = new SMSearchResponse(ResponseCode.SYSTEM_ERROR.getCode(), ResponseCode.SYSTEM_ERROR.getMsg());
//            log.error("cv es aggSearchquery exception:{}", e.toString(), e);
//            log.error(String.format(" buid:%s cv es aggSearchquery exception:%s ", request.getBuid(), e.toString()), e);
//        }
//        return resp;
//    }
//
//    @Override
//    public SMSearchResponse search(SMSearchRequest request, int days) {
//
//        if (request == null) {
//            return new SMSearchResponse(ResponseCode.MISS_PARAM.getCode(), ResponseCode.MISS_PARAM.getMsg());
//        }
//        BoolQueryBuilder query = makeQuery(request, false);
//        Long from = DateTimeUtil.addDay(days);
//        query.must(QueryBuilders.rangeQuery("lModTime").from(from).to(DateTimeUtil.currentSecond()));
//        log.info("buid:{} cv query string:{}", request.getBuid(), query.toString());
//
//        long startTime = System.currentTimeMillis();
//        SortBuilder sort = makeSort();
//        String _index = getCVIndexByCache();
//        SearchResponse searchResponse = ESClientHelper.getClient().prepareSearch(_index).setTypes(Constant.CVTYPE).setQuery(query).addSort(sort).execute().actionGet();
//        log.info("cv es query cost:{}ms", System.currentTimeMillis() - startTime);
//        SMSearchResponse resp = new SMSearchResponse();
//        if (searchResponse == null) {
//            log.info("buid:{} cv es query response null", request.getBuid());
//            return resp;
//        }
//        SearchHits hits = searchResponse.getHits();
//        if (hits == null || hits.totalHits() <= 0) {
//            log.info("buid:{} cv es query response hits is null", request.getBuid());
//            return resp;
//        }
//        List<String> cvIds = new ArrayList<String>();
//        for (SearchHit hit : hits) {
//            cvIds.add(hit.getId());
//            if (log.isDebugEnabled()) {
//                log.debug("score:{}:\t  {}", hit.getScore(), hit.getSource().get("companys"));
//            }
//        }
//        resp.setTotal(hits == null ? 0 : hits.getTotalHits());
//        resp.setCvIds(cvIds);
//        resp.setQuery(query.toString());
//        log.info("buid:{} cv query interface response:{}", request.getBuid(), resp);
//        return resp;
//    }
//
//    @Override
//    public SMSearchResponse analySearch(SMSearchRequest request) {
//        if (request == null) {
//            return new SMSearchResponse(ResponseCode.MISS_PARAM.getCode(), ResponseCode.MISS_PARAM.getMsg());
//        }
//        log.info("cv es query req:{}", request);
//        SMSearchResponse resp = null;
//        try {
//            BoolQueryBuilder query = makeQuery(request, true);
//            TermsBuilder companysAgg = AggregationBuilders.terms(Constant.AGGR_COMPANYS).field("companys").size(30);
//            String _index = getCVIndexByCache();
//            SearchRequestBuilder searchBuilder = ESClientHelper.getClient().prepareSearch(_index).setTypes(Constant.CVTYPE).setQuery(query).addAggregation(companysAgg).setFrom(0).setSize(Constant.TARGET_COMPANY_SIZE).setSearchType(SearchType.QUERY_AND_FETCH);
//            log.info("buid:{} cv es query builder:{}", request.getBuid(), searchBuilder.toString());
//            long startTime = System.currentTimeMillis();
//            SearchResponse searchResponse = searchBuilder.execute().actionGet();
//            log.info("cv es query cost:{}ms", System.currentTimeMillis() - startTime);
//            resp = makeResponseForAnaly(searchResponse);
//            resp.setQuery(searchBuilder.toString());
//            log.info("buid:{} cv es query response:{}", request.getBuid(), resp);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return resp;
//    }
//
//    private static String getCVIndexByCache() {
//        String _index = null;
//        try {
//            _index = redis.get(Constant.CV_ES_INDEX_KEY);
//        } catch (Exception e) {
//            log.error("get cv es index by cache exception:{}", e.toString(), e);
//        }
//
//        if (StringUtils.isBlank(_index)) {
//            log.error("get cv es index by cache is blank , use default");
//            _index = Constant.CV_ES_INDEX_KEY_DEFAULT;
//        }
//        return _index;
//    }
//
//    private SortBuilder makeSort() {
//        SortBuilder sort = SortBuilders.scoreSort();
//        return sort;
//    }
//
//    private BoolQueryBuilder makeQuery(SMSearchRequest request, boolean isAnalyzed) {
//
//        BoolQueryBuilder query = QueryBuilders.boolQuery();
//        String keyword = request.getKeyword();
//        if (StringUtils.isNotBlank(keyword)) {
//            BoolQueryBuilder keywordQuery = QueryBuilders.boolQuery();
//            keywordQuery.should(QueryBuilders.matchPhraseQuery("expectJob", keyword).analyzer(Constant.ANALYZED).slop(5).boost(8));
//            keywordQuery.should(QueryBuilders.matchPhraseQuery("expectJobSynonym", keyword).analyzer(Constant.ANALYZED).slop(5).boost(8));
//            keywordQuery.should(QueryBuilders.matchPhraseQuery("peojectName", keyword).analyzer(Constant.ANALYZED).slop(5).boost(3));
//            keywordQuery.should(QueryBuilders.matchPhraseQuery("peojectDesc", keyword).analyzer(Constant.ANALYZED).slop(15).boost(5));
//            keywordQuery.should(QueryBuilders.matchPhraseQuery("jobs", keyword).analyzer(Constant.ANALYZED).slop(5).boost(10));
//            keywordQuery.should(QueryBuilders.matchPhraseQuery("jobSynonym", keyword).analyzer(Constant.ANALYZED).slop(5).boost(8));
//            keywordQuery.should(QueryBuilders.matchPhraseQuery("jobdesc", keyword).analyzer(Constant.ANALYZED).slop(15).boost(5));
//            query.must(keywordQuery);
//        }
//        //职位
//        String[] jobs = SearchHelper.split(request.getJob());
//        if (ArrayUtils.isNotEmpty(jobs)) {
//            query.must(QueryBuilders.termsQuery("lastJobName", jobs));
//        }
//        //公司
//        String[] companys = SearchHelper.split(request.getCompany());
//        if (ArrayUtils.isNotEmpty(companys)) {
//            BoolQueryBuilder companyQuery = new BoolQueryBuilder();
//            if (request.getIsLastJob() != 1) {//无当前任职条件
//                if (!isAnalyzed) {//图谱详情页用于搜索简历
//                    //短语搜索-原公司
//                    for (String companyName : companys) {
//                        companyQuery.should(QueryBuilders.matchPhraseQuery("sourceCompanyName", companyName).analyzer(Constant.ANALYZED).slop(5));
//                    }
//                }
//                //term-整理的公司
//                companyQuery.should(QueryBuilders.termsQuery("companys", companys));
//                query.must(companyQuery);
//            } else {//当前任职
//                query.must(QueryBuilders.termsQuery("lastCompanyName", companys));
//            }
//        }
//        //
//        String[] edus = SearchHelper.split(request.getEdu());
//        if (ArrayUtils.isNotEmpty(edus)) {
//            BoolQueryBuilder eduQuery = QueryBuilders.boolQuery();
//            for (String eduStr : edus) {
//                try {
//                    int edu = Integer.parseInt(eduStr);
//                    int[] eduMappers = EduSelect.get(edu);
//                    if (eduMappers == null) {
//                        continue;
//                    }
//                    for (int eduMapper : eduMappers) {
//                        eduQuery.should(QueryBuilders.termQuery("edu", eduMapper));
//                    }
//                } catch (Exception e) {
//                }
//            }
//            query.must(eduQuery);
//        }
//
//        String[] salarys = SearchHelper.split(request.getSalary());
//        if (ArrayUtils.isNotEmpty(salarys)) {
//            BoolQueryBuilder salaryQuery = QueryBuilders.boolQuery();
//            for (String salaryStr : salarys) {
//                int salary = Integer.parseInt(salaryStr);
//                if (salary == SalarySelect.TEN.getId()) {
//                    salaryQuery.should(QueryBuilders.rangeQuery("salary").gte(SalarySelect.TEN.getLow()));
//                } else if (salary == SalarySelect.OTHER.getId()) {
//                    salaryQuery.should(QueryBuilders.rangeQuery("salary").lt(SalarySelect.OTHER.getHigh()));
//                } else {
//                    SalarySelect salarySelect = SalarySelect.get(salary);
//                    if (salarySelect == null) {
//                        continue;
//                    }
//                    salaryQuery.should(QueryBuilders.rangeQuery("salary").gte(salarySelect.getLow()).lte(salarySelect.getHigh()));
//                }
//            }
//            query.must(salaryQuery);
//        }
//
//        String[] workYears = SearchHelper.split(request.getWorkYear());
//        if (ArrayUtils.isNotEmpty(workYears)) {
//            BoolQueryBuilder workYearQuery = QueryBuilders.boolQuery();
//            for (String workYearStr : workYears) {
//                try {
//                    int workYear = Integer.parseInt(workYearStr);
//                    if (workYear == WorkYearSelect.OTHER.getId()) {
//                        workYearQuery.should(QueryBuilders.rangeQuery("workYear").lte(WorkYearSelect.OTHER.getHigh()));
//                    } else if (workYear == WorkYearSelect.OVER_TEN.getId()) {
//                        workYearQuery.should(QueryBuilders.rangeQuery("workYear").gt(WorkYearSelect.OVER_TEN.getLow()));
//                    } else {
//                        WorkYearSelect workYearSelect = WorkYearSelect.get(workYear);
//                        if (workYearSelect == null) {
//                            continue;
//                        }
//                        workYearQuery.should(QueryBuilders.rangeQuery("workYear").gt(workYearSelect.getLow()).lte(workYearSelect.getHigh()));
//                    }
//                } catch (NumberFormatException e) {
//                }
//            }
//            query.must(workYearQuery);
//        }
//
//        int[] cities = SearchHelper.splitPaseInteger(request.getCity());
//        if (ArrayUtils.isNotEmpty(cities)) {
//            BoolQueryBuilder cityQuery = QueryBuilders.boolQuery();
//            cityQuery.should(QueryBuilders.termsQuery("hopeCity", cities));
//            cityQuery.should(QueryBuilders.termsQuery("livingCity", cities));
//            query.must(cityQuery);
//        }
//
//        int lModTime = request.getlModTime();
//        LastModTimeSelect modTimeSelect = LastModTimeSelect.get(lModTime);
//        if (modTimeSelect != null) {
//            int calendarType = modTimeSelect.getType();
//            Long from = calendarType == Calendar.DATE ? DateTimeUtil.getStartTime(modTimeSelect.getAddCount()) : DateTimeUtil.addMonth(modTimeSelect.getAddCount());
//            query.must(QueryBuilders.rangeQuery("lModTime").from(from).to(DateTimeUtil.currentSecond()));
//        }
//        return query;
//    }
//
//    /**
//     * @param searchResponse
//     * @param aggType        聚类type
//     * @return
//     */
//    private SMSearchResponse makeResponse(SearchResponse searchResponse, String aggType) {
//
//        SMSearchResponse resp = new SMSearchResponse();
//        if (searchResponse == null) {
//            log.info("cv es query response null");
//            return resp;
//        }
//        if (StringUtils.isNotBlank(aggType)) {
//            Aggregations aggregations = searchResponse.getAggregations();
//            if (aggType.equals(SMConstant.AGGR_JOB)) {
//                resp.setJobDistribution(SearchHelper.convertJob(aggregations));
//            }
//            if (aggType.equals(SMConstant.AGGR_EDU)) {
//                resp.setEduDistribution(SearchHelper.convert(aggregations, Constant.AGGR_EDU));
//            }
//            if (aggType.equals(SMConstant.AGGR_SALARY)) {
//                resp.setSalaryDistribution(SearchHelper.convertSalary(aggregations));
//            }
//            if (aggType.equals(SMConstant.AGGR_WORK_YEAR)) {
//                resp.setWorkYearDistribution(SearchHelper.convert(aggregations, Constant.AGGR_WORK_YEAR));
//            }
//            if (aggType.equals(SMConstant.AGGR_WORK_YEAR)) {
//                resp.setCompanysDistribution(SearchHelper.convertCompanys(aggregations));
//            }
//        } else {
//            SearchHits hits = searchResponse.getHits();
//            if (hits == null || hits.totalHits() <= 0) {
//                log.info("cv es query response hits is null");
//                return resp;
//            }
//            long total = hits.totalHits();
//            resp.setTotal(total);
//            List<String> cvIds = new ArrayList<String>();
//            for (SearchHit hit : hits) {
//                cvIds.add(hit.getId());
//                if (log.isDebugEnabled()) {
//                    log.debug("score:{}:\t  {}", hit.getScore(), hit.getSource().get("companys"));
//                }
//            }
//            resp.setCvIds(cvIds);
//        }
//        resp.setCode(ResponseCode.SUCCESS.getCode());
//        resp.setMsg(ResponseCode.SUCCESS.getMsg());
//        return resp;
//    }
//
//    private SMSearchResponse makeResponseForAnaly(SearchResponse searchResponse) {
//        SMSearchResponse resp = new SMSearchResponse();
//        if (searchResponse == null) {
//            log.info("cv es query response null");
//            return resp;
//        }
//        SearchHits hits = searchResponse.getHits();
//        if (hits == null || hits.totalHits() <= 0) {
//            log.info("cv es query response hits is null");
//            return resp;
//        }
//        long total = hits.totalHits();
//        resp.setTotal(total);
//        List<String> cvIds = new ArrayList<String>();
//        for (SearchHit hit : hits) {
//            cvIds.add(hit.getId());
//            if (log.isDebugEnabled()) {
//                log.debug("score:{}:\t  {}", hit.getScore(), hit.getSource().get("companys"));
//            }
//        }
//        resp.setCvIds(cvIds);
//        Aggregations aggregations = searchResponse.getAggregations();
//        resp.setTargetCompanys(SearchHelper.convertCompanys(aggregations));
//        // resp.setTargetCompanys(targetCompanys);
//        resp.setCode(ResponseCode.SUCCESS.getCode());
//        resp.setMsg(ResponseCode.SUCCESS.getMsg());
//        return resp;
//    }
//
//    public FunctionScoreQueryBuilder createFunctionQuery(BoolQueryBuilder boolquery) {
//        Map<String, String> wight_set = redis.hgetAll("wight_set");
//        float decay = Float.parseFloat(wight_set.get("decay"));
//        System.out.println(decay);
//        String offset = wight_set.get("offset");
//        GaussDecayFunctionBuilder gaussDecayFunctionBuilder = ScoreFunctionBuilders.gaussDecayFunction("refreshdate", wight_set.get("scale"));
//        if (StringUtils.isNotBlank(offset)) {
//            gaussDecayFunctionBuilder.setOffset(offset);
//        }
//        gaussDecayFunctionBuilder.setDecay(decay);
//        FunctionScoreQueryBuilder functionScore = QueryBuilders.functionScoreQuery(boolquery, gaussDecayFunctionBuilder);
//        return functionScore;
//    }
//
//    public static void main(String[] args) {
//        Map<String, String> wight_set = redis.hgetAll("wight_set");
//        System.out.println(Float.parseFloat(wight_set.get("decay")));
//    }
//}
