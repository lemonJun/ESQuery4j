
package lemon.elastic.query4j.esproxy.core.query;

import java.util.Collection;
import java.util.List;

import org.elasticsearch.action.search.SearchType;

import lemon.elastic.query4j.esproxy.domain.PageRequest;
import lemon.elastic.query4j.esproxy.domain.Pageable;
import lemon.elastic.query4j.esproxy.domain.Sort;

/**
 * 查询请求接口类
 *
 *
 * @author lemon
 * @version 1.0
 * @date  2016年4月8日 下午11:02:14
 * @see 
 * @since
 */
public interface Query {

    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final Pageable DEFAULT_PAGE = new PageRequest(0, DEFAULT_PAGE_SIZE);

    /**
     * 设计分页， 对应elasticsearch中的  'start' and 'rows' parameter in elasticsearch
     *
     * @param pageable
     * @return
     */
    <T extends Query> T setPageable(Pageable pageable);

    /**
     * Get filter queries if defined
     *
     * @return
     */
    //    List<FilterQuery> getFilterQueries();

    /**
     * Get page settings if defined
     * 
     * @return
     */
    Pageable getPageable();

    /**
     * Add {@link lemon.elastic.query4j.esproxy.domain.Sort} to query
     *
     * @param sort
     * @return
     */
    <T extends Query> T addSort(Sort sort);

    /**
     * @return null if not set
     */
    Sort getSort();

    /**
     * 参与搜索的索引
     *
     * @return
     */
    List<String> getIndices();

    /**
     * Add Indices to be added as part of search request
     *
     * @param indices
     */
    void addIndices(String... indices);

    /**
     * 参与搜索的表
     *
     * @param types
     */
    void addTypes(String... types);

    /**
     * Get types to be searched
     *
     * @return
     */
    List<String> getTypes();

    /**
     * 设置返回的字段  不设置默认返回全表
     *
     * @param fields
     */
    void addFields(String... fields);

    /**
     * Get fields to be returned as part of search request
     *
     * @return
     */
    List<String> getFields();

    /**
     * 过滤最小相关性得分
     *
     * @return
     */
    float getMinScore();

    /**
     * Get Ids
     *
     * @return
     */
    Collection<String> getIds();

    /**
     * 设置路由
     *
     * @return
     */
    String getRoute();

    /**
     * 查询的类型：fetch_and_get ,fetch_then_get...
     *
     * @return
     */
    SearchType getSearchType();
}
