package lemon.elastic.query4j.esproxy.core.query;

import java.util.List;

import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilder;

import lemon.elastic.query4j.esproxy.core.facet.FacetRequest;

/**
 * 这个是基于ES客户端的查询
 * 因此通过NativeSearchQueryBuilder借用QueryBuilder可以生成各样各样的查询
 *
 * @author Rizwan Idrees
 * @author Mohsin Husen
 * @author Artur Konczak
 */
public interface SearchQuery extends Query {

    QueryBuilder getQuery();

    FilterBuilder getFilter();

    List<SortBuilder> getElasticsearchSorts();

    List<FacetRequest> getFacets();

    List<AbstractAggregationBuilder> getAggregations();

    HighlightBuilder.Field[] getHighlightFields();
}
