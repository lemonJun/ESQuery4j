
package lemon.elastic.query4j.esproxy.core.query;

import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilder;

import lemon.elastic.query4j.esproxy.core.facet.FacetRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * 这个是直接构建于es客户端之上的查询
 * 不建议使用
 *
 * @author lemon
 * @version 1.0
 * @date  2016年4月7日 下午11:10:19
 * @see 
 * @since
 */
public class NativeSearchQuery extends AbstractQuery implements SearchQuery {

    private QueryBuilder query;
    private FilterBuilder filter;
    private List<SortBuilder> sorts;
    private List<FacetRequest> facets;
    private List<AbstractAggregationBuilder> aggregations;
    private HighlightBuilder.Field[] highlightFields;

    public NativeSearchQuery(QueryBuilder query) {
        this.query = query;
    }

    public NativeSearchQuery(QueryBuilder query, FilterBuilder filter) {
        this.query = query;
        this.filter = filter;
    }

    public NativeSearchQuery(QueryBuilder query, FilterBuilder filter, List<SortBuilder> sorts) {
        this.query = query;
        this.filter = filter;
        this.sorts = sorts;
    }

    public NativeSearchQuery(QueryBuilder query, FilterBuilder filter, List<SortBuilder> sorts, HighlightBuilder.Field[] highlightFields) {
        this.query = query;
        this.filter = filter;
        this.sorts = sorts;
        this.highlightFields = highlightFields;
    }

    public QueryBuilder getQuery() {
        return query;
    }

    public FilterBuilder getFilter() {
        return filter;
    }

    public List<SortBuilder> getElasticsearchSorts() {
        return sorts;
    }

    @Override
    public HighlightBuilder.Field[] getHighlightFields() {
        return highlightFields;
    }

    public void addFacet(FacetRequest facetRequest) {
        if (facets == null) {
            facets = new ArrayList<FacetRequest>();
        }
        facets.add(facetRequest);
    }

    public void setFacets(List<FacetRequest> facets) {
        this.facets = facets;
    }

    @Override
    public List<FacetRequest> getFacets() {
        return facets;
    }

    @Override
    public List<AbstractAggregationBuilder> getAggregations() {
        return aggregations;
    }

    public void addAggregation(AbstractAggregationBuilder aggregationBuilder) {
        if (aggregations == null) {
            aggregations = new ArrayList<AbstractAggregationBuilder>();
        }
        aggregations.add(aggregationBuilder);
    }

    public void setAggregations(List<AbstractAggregationBuilder> aggregations) {
        this.aggregations = aggregations;
    }
}
