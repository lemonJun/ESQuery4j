//package lemon.elastic.query4j.provider;
//
//import java.lang.reflect.Field;
//import java.text.DecimalFormat;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONObject;
//import com.google.common.base.Preconditions;
//
//import lemon.elastic.query4j.esproxy.core.geo.GeoPoint;
//import lemon.elastic.query4j.esproxy.core.query.Criteria;
//import lemon.elastic.query4j.esproxy.core.query.CriteriaQuery;
//import lemon.elastic.query4j.esproxy.domain.PageRequest;
//import lemon.elastic.query4j.esproxy.domain.Pageable;
//import lemon.elastic.query4j.util.ReflectionUtils;
//import lemon.elastic.query4j.util.StringUtil;
//
///**
// * 生成elastic-search所需要的查询条件
// * 
// *
// *
// * @author wangyazhou
// * @version 1.0
// * @date 2016年3月4日 下午12:04:24
// * @see
// * @since
// */
//public class ElasticCriteriaQueryGene implements CriteriaQueryGene {
//
//    private static final Logger logger = LoggerFactory.getLogger(ElasticCriteriaQueryGene.class);
//
//    private static final String DIS_UNIT = "km";
//    public static final DecimalFormat deformat = new DecimalFormat("#.000000");
//
//    @Override
//    public CriteriaQuery geneESQuery(String criteriaBean, Class<?> clazz) {
//        try {
//            Preconditions.checkState(StringUtil.isNotNullOrEmpty(criteriaBean), "json value cannot be null");
//            logger.info(criteriaBean);
//            JSONObject json = JSON.parseObject(criteriaBean);
//            Preconditions.checkNotNull(json, "json cannot be null");
//            CriteriaQuery criQuery = null;
//            for (String key : json.keySet()) {
//                Field field = ReflectionUtils.findField(clazz, key);
//                Object obj = json.get(key);
//                if (null == obj || field == null) {
//                    continue;
//                }
//                //ZTODO  这里实现的真是不好 
//                SearchAnno criteria = field.getAnnotation(SearchAnno.class);
//                Criteria cri = geneOne(criteria, key, obj);
//                if (cri == null)
//                    continue;
//                if (criQuery == null) {
//                    criQuery = new CriteriaQuery(cri);
//                } else {
//                    criQuery.addCriteria(cri);
//                }
//                route(criteria.routing(), criQuery, obj);
//            }
//            return criQuery;
//        } catch (Exception e) {
//            logger.error("gene error", e);
//        }
//        return null;
//    }
//
//    //加入路由
//    private static void route(boolean routing, CriteriaQuery criQuery, Object obj) {
//        try {
//            if (routing && StringUtil.isNotNullOrEmpty(String.valueOf(obj))) {
//                criQuery.setRoute(String.valueOf(obj));
//            }
//        } catch (Exception e) {
//            logger.error("", e);
//        }
//    }
//
//    //生成一个条件
//    private static Criteria geneOne(SearchAnno criteriaAnno, String key, Object vobj) {
//        try {
//            Preconditions.checkNotNull(criteriaAnno);
//            CriteriaEnum condition = criteriaAnno.condition();
//            Criteria crit = null;
//            Preconditions.checkNotNull(vobj, "criteria value cannot be null");
//            StringBuffer onesb = new StringBuffer();
//            if (condition == CriteriaEnum.EQ) {//直接相等
//                crit = new Criteria(key).is((String) vobj);
//            } else if (condition == CriteriaEnum.RANGE) {//范围之间
//                crit = between(key, JSON.toJSONString(vobj));
//            } else if (condition == CriteriaEnum.DATE) {//日期
//                crit = between(key, JSON.toJSONString(vobj));
//            } else if (condition == CriteriaEnum.GEO) {//地理位置
//                crit = lonlat(key, (String) vobj);
//            } else if (condition == CriteriaEnum.CONTAINS) {//关键词要做分词
//                crit = contains(key, (String) vobj);
//            } else if (condition == CriteriaEnum.NOTEQ) {
//                crit = new Criteria(key).notIn((String) vobj);
//            }
//            logger.info(String.format("%s; key=%s,condition=%s", onesb.toString(), key, condition.getValue()));
//            return crit;
//        } catch (Exception e) {
//            logger.error(String.format("geneOne error key=%s,condition=%s", key, JSON.toJSONString(criteriaAnno)), e);
//        }
//        return null;
//    }
//
//    //包含查询
//    public static Criteria contains(String key, String value) {
//        Criteria crit = new Criteria(key).is(value);
//        return crit;
//    }
//
//    //范围查询
//    public static Criteria between(String key, String value) {
//        String[] items = value.split(CriteriaHelper.scopeSeperator);
//        Preconditions.checkState(items.length >= 1, "range must be two number");
//        RangeValue range = JSON.parseObject(value, RangeValue.class);
//        Criteria crit = new Criteria(key).between(range.getStart(), range.getEnd());
//        return crit;
//    }
//
//    //生成经纬度的查询   目前是以，来进行值分隔的   后此类型值很特殊   可以写成BEAN对象
//    //value是纬,经度的格式
//    private static Criteria lonlat(String key, String value) {
//        try {
//            String[] points = value.split(",");
//            Preconditions.checkState(points != null && points.length == 3, "not geo point");
//            System.out.println(value);
//            String lat = "";
//            String lon = "";
//            if (!StringUtil.isNullOrEmpty(points[1]) && !Double.isNaN(Double.parseDouble(points[1]))) {
//                lon = deformat.format(Double.parseDouble(points[1]));
//            }
//            if (!StringUtil.isNullOrEmpty(points[0]) && !Double.isNaN(Double.parseDouble(points[0]))) {
//                lat = deformat.format(Double.parseDouble(points[0]));
//            }
//
//            Preconditions.checkState(StringUtil.isNotNullOrEmpty(lon), "not valid lng");
//            Preconditions.checkState(StringUtil.isNotNullOrEmpty(lat), "not valid lat");
//
//            String distance = points[2];
//            Preconditions.checkState(distance.matches("\\d+"), "not valid distance");
//
//            Criteria crit = new Criteria(key).within(new GeoPoint(Double.valueOf(lat), Double.valueOf(lon)), String.format("%s%s", distance, DIS_UNIT)).boost(2.0f);
//            return crit;
//        } catch (NumberFormatException e) {
//            logger.error(String.format("lonlat error key=%s value=%s", key, value), e);
//        }
//        return null;
//    }
//    
//    @Override
//    public CriteriaQuery geneESQueryPageable(String criteriaBean, Class<?> clazz, int currentPage, int pageSize) {
//        try {
//            CriteriaQuery query = geneESQuery(criteriaBean, clazz);
//            if (query == null) {
//                return null;
//            }
//            Pageable page = new PageRequest(currentPage, pageSize);
//            query.setPageable(page);
//            return query;
//        } catch (Exception e) {
//            logger.error("geneESQueryPageable erroe", e);
//        }
//        return null;
//    }
//
//}
