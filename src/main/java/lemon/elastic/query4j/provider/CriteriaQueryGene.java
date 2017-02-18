//package lemon.elastic.query4j.provider;
//
//import lemon.elastic.query4j.esproxy.core.query.CriteriaQuery;
//
///**
// * 根据POJO对象的字段注解及实际值，生成适用于ES的查询query
// * 
// * 
// * @author WangYazhou
// * @date  2015年12月9日 下午3:50:56
// * @see
// */
//
//public interface CriteriaQueryGene {
//
//    /**
//     * 生成索引所需要的查询条件
//     * @param criteriaBean
//     * @param clazz
//     * @return
//     */
//    public abstract CriteriaQuery geneESQuery(String criteriaBean, Class<?> clazz);
//
//    public abstract CriteriaQuery geneESQueryPageable(String criteriaBean, Class<?> clazz, int currentPage, int pageSize);
//
//}
