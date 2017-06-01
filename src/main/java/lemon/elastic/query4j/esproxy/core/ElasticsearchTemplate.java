package lemon.elastic.query4j.esproxy.core;

import static org.elasticsearch.cluster.metadata.AliasAction.Type.ADD;
import static org.elasticsearch.index.VersionType.EXTERNAL;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.admin.cluster.state.ClusterStateRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.mapping.delete.DeleteMappingRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequestBuilder;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.count.CountRequestBuilder;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.get.MultiGetRequestBuilder;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.mlt.MoreLikeThisRequestBuilder;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.suggest.SuggestRequestBuilder;
import org.elasticsearch.action.suggest.SuggestResponse;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.elasticsearch.cluster.metadata.AliasAction;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.collect.MapBuilder;
import org.elasticsearch.common.collect.Maps;
import org.elasticsearch.common.collect.Sets;
import org.elasticsearch.common.lang3.StringUtils;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.facet.FacetBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Preconditions;

import lemon.elastic.query4j.esproxy.annotations.Document;
import lemon.elastic.query4j.esproxy.annotations.Mapping;
import lemon.elastic.query4j.esproxy.annotations.Setting;
import lemon.elastic.query4j.esproxy.core.convert.ElasticsearchConverter;
import lemon.elastic.query4j.esproxy.core.convert.MappingElasticsearchConverter;
import lemon.elastic.query4j.esproxy.core.facet.FacetRequest;
import lemon.elastic.query4j.esproxy.core.mapping.ElasticsearchPersistentEntity;
import lemon.elastic.query4j.esproxy.core.mapping.SimpleElasticsearchMappingContext;
import lemon.elastic.query4j.esproxy.core.query.AliasQuery;
import lemon.elastic.query4j.esproxy.core.query.DSLQuery;
import lemon.elastic.query4j.esproxy.core.query.DeleteQuery;
import lemon.elastic.query4j.esproxy.core.query.GetQuery;
import lemon.elastic.query4j.esproxy.core.query.IndexQuery;
import lemon.elastic.query4j.esproxy.core.query.MoreLikeThisQuery;
import lemon.elastic.query4j.esproxy.core.query.Query;
import lemon.elastic.query4j.esproxy.core.query.SearchQuery;
import lemon.elastic.query4j.esproxy.core.query.StringQuery;
import lemon.elastic.query4j.esproxy.core.query.UpdateQuery;
import lemon.elastic.query4j.esproxy.domain.Page;
import lemon.elastic.query4j.esproxy.domain.Sort;
import lemon.elastic.query4j.esproxy.mapping.PersistentProperty;
import lemon.elastic.query4j.exceptions.ElasticsearchException;
import lemon.elastic.query4j.util.CloseableIterator;
import lemon.elastic.query4j.util.CollectionUtil;
import lemon.elastic.query4j.util.StringUtil;

/**
 * 此类并没有实现ES客户端的所有功能 也真是蛋疼
 *
 * @author WangYazhou
 * @date 2016年4月7日 下午9:09:26
 * @see
 */
@SuppressWarnings("deprecation")
public class ElasticsearchTemplate implements ElasticsearchOperations {

    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchTemplate.class);
    private Client client;
    // ZTODO
    private ElasticsearchConverter elasticsearchConverter;
    private ResultsMapper resultsMapper;
    private String searchTimeout;

    public ElasticsearchTemplate(Client client) {
        this(client, null, null);
    }

    public ElasticsearchTemplate(Client client, EntityMapper entityMapper) {
        this(client, null, new DefaultResultMapper(entityMapper));
    }

    public ElasticsearchTemplate(Client client, ResultsMapper resultsMapper) {
        this(client, null, resultsMapper);
    }

    public ElasticsearchTemplate(Client client, ElasticsearchConverter elasticsearchConverter) {
        this(client, elasticsearchConverter, null);
    }

    public ElasticsearchTemplate(Client client, ElasticsearchConverter elasticsearchConverter, ResultsMapper resultsMapper) {
        this.client = client;
        this.elasticsearchConverter = (elasticsearchConverter == null) ? new MappingElasticsearchConverter(new SimpleElasticsearchMappingContext()) : elasticsearchConverter;
        this.resultsMapper = (resultsMapper == null) ? new DefaultResultMapper(this.elasticsearchConverter.getMappingContext()) : resultsMapper;
    }

    public void setSearchTimeout(String searchTimeout) {
        this.searchTimeout = searchTimeout;
    }

    @Override
    public <T> boolean createIndex(Class<T> clazz) {
        return createIndexIfNotCreated(clazz);
    }

    @Override
    public boolean createIndex(String indexName) {
        Preconditions.checkNotNull(indexName, "No index defined for Query");
        return client.admin().indices().create(Requests.createIndexRequest(indexName)).actionGet().isAcknowledged();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> boolean putMapping(Class<T> clazz) {
        //直接读文件
        if (clazz.isAnnotationPresent(Mapping.class)) {
            String mappingPath = clazz.getAnnotation(Mapping.class).mappingPath();
            if (StringUtils.isNotBlank(mappingPath)) {
                String mappings = readFileFromClasspath(mappingPath);
                if (StringUtils.isNotBlank(mappings)) {
                    return putMapping(clazz, mappings);
                }
            } else {
                logger.info("mappingPath in @Mapping has to be defined. Building mappings using @Field");
            }
        }
        ElasticsearchPersistentEntity<T> persistentEntity = getPersistentEntityFor(clazz);
        String mapping = "";
        try {
            mapping = MappingJSONBuilder.buildMapping(clazz, persistentEntity.getIndexType(), persistentEntity.getIdProperty().getFieldName(), persistentEntity.getParentType());
        } catch (Exception e) {
            throw new ElasticsearchException("Failed to build mapping for " + clazz.getSimpleName(), e);
        }
        return putMapping(clazz, mapping);
    }

    @Override
    public <T> boolean putMapping(Class<T> clazz, Object mapping) {
        return putMapping(getPersistentEntityFor(clazz).getIndexName(), getPersistentEntityFor(clazz).getIndexType(), mapping);
    }

    //此处做兼容   可以是个JSON字符串 也可以是MAP 也可以是XCONTENTBUIDER 
    //最终都会被转化成JSON字符
    @SuppressWarnings("rawtypes")
    @Override
    public boolean putMapping(String indexName, String type, Object mapping) {
        Preconditions.checkNotNull(indexName, "No index defined for putMapping()");
        Preconditions.checkNotNull(type, "No type defined for putMapping()");
        PutMappingRequestBuilder requestBuilder = client.admin().indices().preparePutMapping(indexName).setType(type);
        if (mapping instanceof String) {
            requestBuilder.setSource(String.valueOf(mapping));
        } else if (mapping instanceof Map) {
            requestBuilder.setSource((Map) mapping);
        } else if (mapping instanceof XContentBuilder) {
            requestBuilder.setSource((XContentBuilder) mapping);
        }
        return requestBuilder.execute().actionGet().isAcknowledged();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Map getMapping(String indexName, String type) {
        Preconditions.checkNotNull(indexName, "No index defined for putMapping()");
        Preconditions.checkNotNull(type, "No type defined for putMapping()");
        Map mappings = null;
        try {
            mappings = client.admin().indices().getMappings(new GetMappingsRequest().indices(indexName).types(type)).actionGet().getMappings().get(indexName).get(type).getSourceAsMap();
        } catch (Exception e) {
            throw new ElasticsearchException("Error while getting mapping for indexName : " + indexName + " type : " + type + " " + e.getMessage());
        }
        return mappings;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public <T> Map getMapping(Class<T> clazz) {
        return getMapping(getPersistentEntityFor(clazz).getIndexName(), getPersistentEntityFor(clazz).getIndexType());
    }

    @Override
    public <T> T queryForObject(GetQuery query, Class<T> clazz) {
        return queryForObject(query, clazz, resultsMapper);
    }

    @Override
    public <T> T queryForObject(StringQuery query, Class<T> clazz) {
        Page<T> page = queryForPage(query, clazz);
        Preconditions.checkState(page.getTotalElements() < 2, "Expected 1 but found " + page.getTotalElements() + " results");
        return page.getTotalElements() > 0 ? page.getContent().get(0) : null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T queryForObject(GetQuery query, Class<T> clazz, GetResultMapper mapper) {
        ElasticsearchPersistentEntity<T> persistentEntity = getPersistentEntityFor(clazz);
        GetResponse response = client.prepareGet(persistentEntity.getIndexName(), persistentEntity.getIndexType(), query.getId()).execute().actionGet();

        T entity = mapper.mapResult(response, clazz);
        return entity;
    }

    //    @Override
    //    public <T> T queryForObject(CriteriaQuery query, Class<T> clazz) {
    //        Page<T> page = queryForPage(query, clazz);
    //        Preconditions.checkState(page.getTotalElements() < 2, "Expected 1 but found " + page.getTotalElements() + " results");
    //        return (T) (page.getTotalElements() > 0 ? page.getContent().get(0) : null);
    //    }

    @Override
    public <T> List<T> queryForList(StringQuery query, Class<T> clazz) {
        return queryForPage(query, clazz).getContent();
    }

    @Override
    public <T> FacetedPage<T> queryForPage(SearchQuery query, Class<T> clazz) {
        return queryForPage(query, clazz, resultsMapper);
    }

    @Override
    public <T> FacetedPage<T> queryForPage(SearchQuery query, Class<T> clazz, SearchResultMapper mapper) {
        SearchResponse response = doSearch(prepareSearch(query, clazz), query);
        return mapper.mapResults(response, clazz, query.getPageable());
    }

    @Override
    public <T> T query(SearchQuery query, ResultsExtractor<T> resultsExtractor) {
        SearchResponse response = doSearch(prepareSearch(query), query);
        return resultsExtractor.extract(response);
    }

    //    @Override
    //    public <T> List<T> queryForList(CriteriaQuery query, Class<T> clazz) {
    //        return queryForPage(query, clazz).getContent();
    //    }

    @Override
    public <T> List<T> queryForList(SearchQuery query, Class<T> clazz) {
        return queryForPage(query, clazz).getContent();
    }

    //为啥不给定路由信息呢
    @Override
    public <T> List<String> queryForIds(SearchQuery query) {
        SearchRequestBuilder request = prepareSearch(query).setQuery(query.getQuery()).setNoFields();
        if (query.getFilter() != null) {
            request.setPostFilter(query.getFilter());
        }
        if (StringUtil.isNotNullOrEmpty(query.getRoute())) {
            request.setRouting(query.getRoute());
        }
        SearchResponse response = getSearchResponse(request.execute());
        return extractIds(response);
    }

    @Override
    public <T> FacetedPage<T> queryForPage(StringQuery query, Class<T> clazz) {
        return queryForPage(query, clazz, resultsMapper);
    }

    @Override
    public <T> FacetedPage<T> queryForPage(StringQuery query, Class<T> clazz, SearchResultMapper mapper) {
        SearchResponse response = getSearchResponse(prepareSearch(query, clazz).setQuery(query.getSource()).execute());
        return mapper.mapResults(response, clazz, query.getPageable());
    }

    //    /**
    //     * 只有查询 最小分 过滤等内容 比原生的es客户端要少好多东西的 而在Query层面上 各种参数又是支持的 所以不晓得此外为啥支持的这么少
    //     */
    //    @SuppressWarnings("unused")
    //    @Override
    //    public <T> Page<T> queryForPage(CriteriaQuery criteriaQuery, Class<T> clazz) {
    //        QueryBuilder elasticsearchQuery = new CriteriaQueryProcessor().createQueryFromCriteria(criteriaQuery.getCriteria());
    //
    //        //        logger.info(elasticsearchQuery.toString());
    //        FilterBuilder elasticsearchFilter = new CriteriaFilterProcessor().createFilterFromCriteria(criteriaQuery.getCriteria());
    //
    //        SearchRequestBuilder searchRequestBuilder = prepareSearch(criteriaQuery, clazz);
    //
    //        if (elasticsearchQuery != null) {
    //            searchRequestBuilder.setQuery(elasticsearchQuery);
    //        } else {
    //            searchRequestBuilder.setQuery(QueryBuilders.matchAllQuery());
    //        }
    //
    //        if (criteriaQuery.getMinScore() > 0) {
    //            searchRequestBuilder.setMinScore(criteriaQuery.getMinScore());
    //        }
    //
    //        if (StringUtil.isNotNullOrEmpty(criteriaQuery.getRoute())) {
    //            searchRequestBuilder.setRouting(criteriaQuery.getRoute());
    //        }
    //
    //        if (elasticsearchFilter != null)
    //            searchRequestBuilder.setPostFilter(elasticsearchFilter);
    //        // 查询的语句是以格式化后的json传递的
    //        logger.info("criteriaqueyr dsl:\n" + searchRequestBuilder.toString());
    //
    //        // 获取返回结果
    //        SearchResponse response = getSearchResponse(searchRequestBuilder.execute());
    //        return resultsMapper.mapResults(response, clazz, criteriaQuery.getPageable());
    //    }
    //
    //    @Override
    //    public <T> CloseableIterator<T> stream(CriteriaQuery query, Class<T> clazz) {
    //        final long scrollTimeInMillis = TimeValue.timeValueMinutes(1).millis();
    //        setPersistentEntityIndexAndType(query, clazz);
    //        final String initScrollId = scan(query, scrollTimeInMillis, false);
    //        return doStream(initScrollId, scrollTimeInMillis, clazz, resultsMapper);
    //    }

    @Override
    public <T> CloseableIterator<T> stream(SearchQuery query, Class<T> clazz) {
        return stream(query, clazz, resultsMapper);
    }

    @Override
    public <T> CloseableIterator<T> stream(SearchQuery query, final Class<T> clazz, final SearchResultMapper mapper) {
        final long scrollTimeInMillis = TimeValue.timeValueMinutes(1).millis();
        setPersistentEntityIndexAndType(query, clazz);
        final String initScrollId = scan(query, scrollTimeInMillis, false);
        return doStream(initScrollId, scrollTimeInMillis, clazz, mapper);
    }

    //    @Override
    //    public <T> long count(CriteriaQuery criteriaQuery, Class<T> clazz) {
    //        QueryBuilder elasticsearchQuery = new CriteriaQueryProcessor().createQueryFromCriteria(criteriaQuery.getCriteria());
    //        FilterBuilder elasticsearchFilter = new CriteriaFilterProcessor().createFilterFromCriteria(criteriaQuery.getCriteria());
    //
    //        if (elasticsearchFilter == null) {
    //            return doCount(prepareCount(criteriaQuery, clazz), elasticsearchQuery);
    //        } else {
    //            // filter could not be set into CountRequestBuilder, convert request
    //            // into search request
    //            return doCount(prepareSearch(criteriaQuery, clazz), elasticsearchQuery, elasticsearchFilter);
    //        }
    //    }

    @Override
    public <T> long count(SearchQuery searchQuery, Class<T> clazz) {
        QueryBuilder elasticsearchQuery = searchQuery.getQuery();
        FilterBuilder elasticsearchFilter = searchQuery.getFilter();

        if (elasticsearchFilter == null) {
            return doCount(prepareCount(searchQuery, clazz), elasticsearchQuery);
        } else {
            // filter could not be set into CountRequestBuilder, convert request
            // into search request
            return doCount(prepareSearch(searchQuery, clazz), elasticsearchQuery, elasticsearchFilter);
        }
    }

    //    @Override
    //    public <T> long count(CriteriaQuery query) {
    //        return count(query, null);
    //    }

    @Override
    public <T> long count(SearchQuery query) {
        return count(query, null);
    }

    private long doCount(CountRequestBuilder countRequestBuilder, QueryBuilder elasticsearchQuery) {
        if (elasticsearchQuery != null) {
            countRequestBuilder.setQuery(elasticsearchQuery);
        }
        return countRequestBuilder.execute().actionGet().getCount();
    }

    private long doCount(SearchRequestBuilder searchRequestBuilder, QueryBuilder elasticsearchQuery, FilterBuilder elasticsearchFilter) {
        if (elasticsearchQuery != null) {
            searchRequestBuilder.setQuery(elasticsearchQuery);
        } else {
            searchRequestBuilder.setQuery(QueryBuilders.matchAllQuery());
        }
        if (elasticsearchFilter != null) {
            searchRequestBuilder.setPostFilter(elasticsearchFilter);
        }
        searchRequestBuilder.setSearchType(SearchType.COUNT);
        return searchRequestBuilder.execute().actionGet().getHits().getTotalHits();
    }

    private <T> CountRequestBuilder prepareCount(Query query, Class<T> clazz) {
        String indexName[] = CollectionUtils.isNotEmpty(query.getIndices()) ? query.getIndices().toArray(new String[query.getIndices().size()]) : retrieveIndexNameFromPersistentEntity(clazz);
        String types[] = CollectionUtils.isNotEmpty(query.getTypes()) ? query.getTypes().toArray(new String[query.getTypes().size()]) : retrieveTypeFromPersistentEntity(clazz);

        Preconditions.checkNotNull(indexName, "No index defined for Query");

        CountRequestBuilder countRequestBuilder = client.prepareCount(indexName);

        if (types != null) {
            countRequestBuilder.setTypes(types);
        }
        return countRequestBuilder;
    }

    @Override
    public <T> LinkedList<T> multiGet(SearchQuery searchQuery, Class<T> clazz) {
        return resultsMapper.mapResults(getMultiResponse(searchQuery, clazz), clazz);
    }

    /**
     * 将一个请求分为多个，到不同的node中进行请求，再将结果合并起来。
     * 如果某个node的请求查询失败了，那么这个请求仍然会返回数据，只是返回的数据只有请求成功的节点的查询数据集合
     * 
     * 并且spring.elasticsearch只给这一个方法加了路由
     * @param searchQuery
     * @param clazz
     * @return
     */
    private <T> MultiGetResponse getMultiResponse(Query searchQuery, Class<T> clazz) {
        String indexName = CollectionUtils.isNotEmpty(searchQuery.getIndices()) ? searchQuery.getIndices().get(0) : getPersistentEntityFor(clazz).getIndexName();
        String type = CollectionUtils.isNotEmpty(searchQuery.getTypes()) ? searchQuery.getTypes().get(0) : getPersistentEntityFor(clazz).getIndexType();

        Preconditions.checkNotNull(indexName, "No index defined for Query");
        Preconditions.checkNotNull(type, "No type define for Query");
        Preconditions.checkState(CollectionUtil.isEmpty(searchQuery.getIds()), "No Id define for Query");

        MultiGetRequestBuilder builder = client.prepareMultiGet();

        for (String id : searchQuery.getIds()) {
            MultiGetRequest.Item item = new MultiGetRequest.Item(indexName, type, id);
            if (searchQuery.getRoute() != null) {
                item = item.routing(searchQuery.getRoute());
            }

            if (searchQuery.getFields() != null && !searchQuery.getFields().isEmpty()) {
                item = item.fields(CollectionUtil.toArray(searchQuery.getFields()));
            }
            builder.add(item);
        }
        return builder.execute().actionGet();
    }

    @Override
    public <T> LinkedList<T> multiGet(SearchQuery searchQuery, Class<T> clazz, MultiGetResultMapper getResultMapper) {
        return getResultMapper.mapResults(getMultiResponse(searchQuery, clazz), clazz);
    }

    @Override
    public String index(IndexQuery query) {
        String documentId = prepareIndex(query).execute().actionGet().getId();
        // We should call this because we are not going through a mapper.
        if (query.getObject() != null) {
            logger.info(documentId);
            setPersistentEntityId(query.getObject(), documentId);
        }
        return documentId;
    }

    @Override
    public UpdateResponse update(UpdateQuery query) {
        return this.prepareUpdate(query).execute().actionGet();
    }

    private UpdateRequestBuilder prepareUpdate(UpdateQuery query) {
        String indexName = StringUtils.isNotBlank(query.getIndexName()) ? query.getIndexName() : getPersistentEntityFor(query.getClazz()).getIndexName();
        String type = StringUtils.isNotBlank(query.getType()) ? query.getType() : getPersistentEntityFor(query.getClazz()).getIndexType();
        Preconditions.checkNotNull(indexName, "No index defined for Query");
        Preconditions.checkNotNull(type, "No type define for Query");
        Preconditions.checkNotNull(query.getId(), "No Id define for Query");
        Preconditions.checkNotNull(query.getUpdateRequest(), "No IndexRequest define for Query");
        UpdateRequestBuilder updateRequestBuilder = client.prepareUpdate(indexName, type, query.getId());

        if (query.getUpdateRequest().script() == null) {
            // doc
            if (query.DoUpsert()) {
                updateRequestBuilder.setDocAsUpsert(true).setDoc(query.getUpdateRequest().doc());
            } else {
                updateRequestBuilder.setDoc(query.getUpdateRequest().doc());
            }
        } else {
            // or script
            updateRequestBuilder.setScript(query.getUpdateRequest().script(), query.getUpdateRequest().scriptType()).setScriptParams(query.getUpdateRequest().scriptParams()).setScriptLang(query.getUpdateRequest().scriptLang());
        }

        return updateRequestBuilder;
    }

    @Override
    public void bulkIndex(List<IndexQuery> queries) {
        BulkRequestBuilder bulkRequest = client.prepareBulk();
        for (IndexQuery query : queries) {
            bulkRequest.add(prepareIndex(query));
        }
        BulkResponse bulkResponse = bulkRequest.execute().actionGet();
        if (bulkResponse.hasFailures()) {
            Map<String, String> failedDocuments = new HashMap<String, String>();
            for (BulkItemResponse item : bulkResponse.getItems()) {
                if (item.isFailed())
                    failedDocuments.put(item.getId(), item.getFailureMessage());
            }
            throw new ElasticsearchException("Bulk indexing has failures. Use ElasticsearchException.getFailedDocuments() for detailed messages [" + failedDocuments + "]", failedDocuments);
        }
    }

    @Override
    public void bulkUpdate(List<UpdateQuery> queries) {
        BulkRequestBuilder bulkRequest = client.prepareBulk();
        for (UpdateQuery query : queries) {
            bulkRequest.add(prepareUpdate(query));
        }
        BulkResponse bulkResponse = bulkRequest.execute().actionGet();
        if (bulkResponse.hasFailures()) {
            Map<String, String> failedDocuments = new HashMap<String, String>();
            for (BulkItemResponse item : bulkResponse.getItems()) {
                if (item.isFailed())
                    failedDocuments.put(item.getId(), item.getFailureMessage());
            }
            throw new ElasticsearchException("Bulk indexing has failures. Use ElasticsearchException.getFailedDocuments() for detailed messages [" + failedDocuments + "]", failedDocuments);
        }
    }

    @Override
    public <T> boolean indexExists(Class<T> clazz) {
        return indexExists(getPersistentEntityFor(clazz).getIndexName());
    }

    @Override
    public boolean indexExists(String indexName) {
        return client.admin().indices().exists(Requests.indicesExistsRequest(indexName)).actionGet().isExists();
    }

    @Override
    public boolean typeExists(String index, String type) {
        return client.admin().cluster().prepareState().execute().actionGet().getState().metaData().index(index).mappings().containsKey(type);
    }

    @Override
    public <T> boolean deleteIndex(Class<T> clazz) {
        return deleteIndex(getPersistentEntityFor(clazz).getIndexName());
    }

    @Override
    public boolean deleteIndex(String indexName) {
        Preconditions.checkNotNull(indexName, "No index defined for delete operation");
        if (indexExists(indexName)) {
            return client.admin().indices().delete(new DeleteIndexRequest(indexName)).actionGet().isAcknowledged();
        }
        return false;
    }

    @Override
    public void deleteType(String index, String type) {
        ImmutableOpenMap<String, MappingMetaData> mappings = client.admin().cluster().prepareState().execute().actionGet().getState().metaData().index(index).mappings();
        if (mappings.containsKey(type)) {
            client.admin().indices().deleteMapping(new DeleteMappingRequest(index).types(type)).actionGet();
        }
    }

    @Override
    public String delete(String indexName, String type, String id) {
        DeleteRequestBuilder delerequest = client.prepareDelete(indexName, type, id);
        DeleteRequest request = delerequest.request();
        logger.info(JSON.toJSONString(request));
        return delerequest.execute().actionGet().getId();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public <T> String delete(Class<T> clazz, String id) {
        ElasticsearchPersistentEntity persistentEntity = getPersistentEntityFor(clazz);
        return delete(persistentEntity.getIndexName(), persistentEntity.getIndexType(), id);
    }

    @SuppressWarnings({ "deprecation", "rawtypes" })
    @Override
    public <T> void delete(DeleteQuery deleteQuery, Class<T> clazz) {
        ElasticsearchPersistentEntity persistentEntity = getPersistentEntityFor(clazz);
        logger.info(String.format("deletequery dsl:\n%s", deleteQuery.getQuery().toString()));
        client.prepareDeleteByQuery(persistentEntity.getIndexName()).setTypes(persistentEntity.getIndexType()).setQuery(deleteQuery.getQuery()).execute().actionGet();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void delete(DeleteQuery deleteQuery) {
        Preconditions.checkNotNull(deleteQuery.getIndex(), "No index defined for Query");
        Preconditions.checkNotNull(deleteQuery.getType(), "No type define for Query");
        client.prepareDeleteByQuery(deleteQuery.getIndex()).setTypes(deleteQuery.getType()).setQuery(deleteQuery.getQuery()).execute().actionGet();
    }

    //    @Override
    //    public <T> void delete(CriteriaQuery criteriaQuery, Class<T> clazz) {
    //        QueryBuilder elasticsearchQuery = new CriteriaQueryProcessor().createQueryFromCriteria(criteriaQuery.getCriteria());
    //        Preconditions.checkNotNull(elasticsearchQuery, "Query can not be null.");
    //        DeleteQuery deleteQuery = new DeleteQuery();
    //        deleteQuery.setQuery(elasticsearchQuery);
    //        delete(deleteQuery, clazz);
    //    }
    //
    //    @Override
    //    public String scan(CriteriaQuery criteriaQuery, long scrollTimeInMillis, boolean noFields) {
    //        Preconditions.checkNotNull(criteriaQuery.getIndices(), "No index defined for Query");
    //        Preconditions.checkNotNull(criteriaQuery.getTypes(), "No type define for Query");
    //        Preconditions.checkNotNull(criteriaQuery.getPageable(), "Query.pageable is required for scan & scroll");
    //
    //        QueryBuilder elasticsearchQuery = new CriteriaQueryProcessor().createQueryFromCriteria(criteriaQuery.getCriteria());
    //        FilterBuilder elasticsearchFilter = new CriteriaFilterProcessor().createFilterFromCriteria(criteriaQuery.getCriteria());
    //        SearchRequestBuilder requestBuilder = prepareScan(criteriaQuery, scrollTimeInMillis, noFields);
    //
    //        if (elasticsearchQuery != null) {
    //            requestBuilder.setQuery(elasticsearchQuery);
    //        } else {
    //            requestBuilder.setQuery(QueryBuilders.matchAllQuery());
    //        }
    //
    //        if (elasticsearchFilter != null) {
    //            requestBuilder.setPostFilter(elasticsearchFilter);
    //        }
    //
    //        return getSearchResponse(requestBuilder.execute()).getScrollId();
    //    }

    @Override
    public String scan(SearchQuery searchQuery, long scrollTimeInMillis, boolean noFields) {
        Preconditions.checkNotNull(searchQuery.getIndices(), "No index defined for Query");
        Preconditions.checkNotNull(searchQuery.getTypes(), "No type define for Query");
        Preconditions.checkNotNull(searchQuery.getPageable(), "Query.pageable is required for scan & scroll");

        SearchRequestBuilder requestBuilder = prepareScan(searchQuery, scrollTimeInMillis, noFields);

        if (searchQuery.getFilter() != null) {
            requestBuilder.setPostFilter(searchQuery.getFilter());
        }

        return getSearchResponse(requestBuilder.setQuery(searchQuery.getQuery()).execute()).getScrollId();
    }

    private SearchRequestBuilder prepareScan(Query query, long scrollTimeInMillis, boolean noFields) {
        SearchRequestBuilder requestBuilder = client.prepareSearch(CollectionUtil.toArray(query.getIndices())).setSearchType(SearchType.SCAN).setTypes(CollectionUtil.toArray(query.getTypes())).setScroll(TimeValue.timeValueMillis(scrollTimeInMillis)).setFrom(0).setSize(query.getPageable().getPageSize());

        if (CollectionUtils.isNotEmpty(query.getFields())) {
            requestBuilder.addFields(CollectionUtil.toArray(query.getFields()));
        }

        if (noFields) {
            requestBuilder.setNoFields();
        }
        return requestBuilder;
    }

    @Override
    public <T> Page<T> scroll(String scrollId, long scrollTimeInMillis, Class<T> clazz) {
        SearchResponse response = getSearchResponse(client.prepareSearchScroll(scrollId).setScroll(TimeValue.timeValueMillis(scrollTimeInMillis)).execute());
        return resultsMapper.mapResults(response, clazz, null);
    }

    @Override
    public <T> Page<T> scroll(String scrollId, long scrollTimeInMillis, SearchResultMapper mapper) {
        SearchResponse response = getSearchResponse(client.prepareSearchScroll(scrollId).setScroll(TimeValue.timeValueMillis(scrollTimeInMillis)).execute());
        return mapper.mapResults(response, null, null);
    }

    @SuppressWarnings({ "deprecation", "rawtypes" })
    @Override
    public <T> Page<T> moreLikeThis(MoreLikeThisQuery query, Class<T> clazz) {
        int startRecord = 0;
        ElasticsearchPersistentEntity persistentEntity = getPersistentEntityFor(clazz);
        String indexName = StringUtils.isNotBlank(query.getIndexName()) ? query.getIndexName() : persistentEntity.getIndexName();
        String type = StringUtils.isNotBlank(query.getType()) ? query.getType() : persistentEntity.getIndexType();

        Preconditions.checkNotNull(indexName, "No 'indexName' defined for MoreLikeThisQuery");
        Preconditions.checkNotNull(type, "No 'type' defined for MoreLikeThisQuery");
        Preconditions.checkNotNull(query.getId(), "No document id defined for MoreLikeThisQuery");

        MoreLikeThisRequestBuilder requestBuilder = new MoreLikeThisProcessor().creatMoreLikeThis(client, indexName, type, startRecord, query, clazz);

        SearchResponse response = getSearchResponse(requestBuilder.execute());
        return resultsMapper.mapResults(response, clazz, query.getPageable());
    }

    @SuppressWarnings("deprecation")
    private SearchResponse doSearch(SearchRequestBuilder searchRequest, SearchQuery searchQuery) {
        if (searchQuery.getFilter() != null) {
            searchRequest.setPostFilter(searchQuery.getFilter());
        }

        if (CollectionUtils.isNotEmpty(searchQuery.getElasticsearchSorts())) {
            for (SortBuilder sort : searchQuery.getElasticsearchSorts()) {
                searchRequest.addSort(sort);
            }
        }

        if (CollectionUtils.isNotEmpty(searchQuery.getFacets())) {
            for (FacetRequest facetRequest : searchQuery.getFacets()) {
                FacetBuilder facet = facetRequest.getFacet();
                if (facetRequest.applyQueryFilter() && searchQuery.getFilter() != null) {
                    facet.facetFilter(searchQuery.getFilter());
                }
                searchRequest.addFacet(facet);
            }
        }

        if (searchQuery.getHighlightFields() != null) {
            for (HighlightBuilder.Field highlightField : searchQuery.getHighlightFields()) {
                searchRequest.addHighlightedField(highlightField);
            }
        }

        if (CollectionUtils.isNotEmpty(searchQuery.getAggregations())) {
            for (AbstractAggregationBuilder aggregationBuilder : searchQuery.getAggregations()) {
                searchRequest.addAggregation(aggregationBuilder);
            }
        }
        searchRequest.setQuery(searchQuery.getQuery());
        logger.info("search type:" + searchQuery.getSearchType());
        logger.info(String.format("searchquery dsl:\n%s", searchRequest.toString()));
        return getSearchResponse(searchRequest.execute());
    }

    private SearchResponse getSearchResponse(ListenableActionFuture<SearchResponse> response) {
        return searchTimeout == null ? response.actionGet() : response.actionGet(searchTimeout);
    }

    private <T> CloseableIterator<T> doStream(final String initScrollId, final long scrollTimeInMillis, final Class<T> clazz, final SearchResultMapper mapper) {
        return new InnoCloseableIterator<T>(initScrollId, scrollTimeInMillis, clazz, mapper);
    }

    class InnoCloseableIterator<T> implements CloseableIterator<T> {
        /**
         * As we couldn't retrieve single result with scroll, store current
         * hits.
         */
        private volatile Iterator<T> currentHits;

        /** The scroll id. */
        private volatile String scrollId;

        /** If stream is finished (ie: cluster returns no results. */
        private volatile boolean finished;
        private Class<T> clazz;
        private SearchResultMapper mapper;
        private long scrollTimeInMillis;

        public InnoCloseableIterator(String initScrollId, long scrollTimeInMillis, Class<T> clazz, SearchResultMapper mapper) {
            this.scrollId = initScrollId;
            this.clazz = clazz;
            this.mapper = mapper;
            this.scrollTimeInMillis = scrollTimeInMillis;
        }

        @Override
        public void close() {
            try {
                // Clear scroll on cluster only in case of error (cause
                // elasticsearch auto clear scroll when it's done)
                if (!finished && scrollId != null && currentHits != null && currentHits.hasNext()) {
                    client.prepareClearScroll().addScrollId(scrollId).execute().actionGet();
                }
            } finally {
                currentHits = null;
                scrollId = null;
            }
        }

        @Override
        public boolean hasNext() {
            // Test if stream is finished
            if (finished) {
                return false;
            }
            // Test if it remains hits
            if (currentHits == null || !currentHits.hasNext()) {
                // Do a new request
                SearchResponse response = getSearchResponse(client.prepareSearchScroll(scrollId).setScroll(TimeValue.timeValueMillis(scrollTimeInMillis)).execute());
                // Save hits and scroll id
                currentHits = mapper.mapResults(response, clazz, null).iterator();
                finished = !currentHits.hasNext();
                scrollId = response.getScrollId();
            }
            return currentHits.hasNext();
        }

        @Override
        public T next() {
            if (hasNext()) {
                return currentHits.next();
            }
            throw new NoSuchElementException();
        }

        @Override
        public void remove() {

        }
    }

    private <T> boolean createIndexIfNotCreated(Class<T> clazz) {
        return indexExists(getPersistentEntityFor(clazz).getIndexName()) || createIndexWithSettings(clazz);
    }

    @SuppressWarnings("unchecked")
    private <T> boolean createIndexWithSettings(Class<T> clazz) {
        if (clazz.isAnnotationPresent(Setting.class)) {// 是否有setting配置
            String settingPath = clazz.getAnnotation(Setting.class).settingPath();
            if (StringUtils.isNotBlank(settingPath)) {
                String settings = readFileFromClasspath(settingPath);
                if (StringUtils.isNotBlank(settings)) {
                    return createIndex(getPersistentEntityFor(clazz).getIndexName(), settings);
                }
            } else {
                logger.info("settingPath in @Setting has to be defined. Using default instead.");
            }
        }
        return createIndex(getPersistentEntityFor(clazz).getIndexName(), getDefaultSettings(getPersistentEntityFor(clazz)));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public boolean createIndex(String indexName, Object settings) {
        CreateIndexRequestBuilder createIndexRequestBuilder = client.admin().indices().prepareCreate(indexName);
        if (settings instanceof String) {
            createIndexRequestBuilder.setSettings(String.valueOf(settings));
        } else if (settings instanceof Map) {
            createIndexRequestBuilder.setSettings((Map) settings);
        } else if (settings instanceof XContentBuilder) {
            createIndexRequestBuilder.setSettings((XContentBuilder) settings);
        }
        return createIndexRequestBuilder.execute().actionGet().isAcknowledged();
    }

    @Override
    public <T> boolean createIndex(Class<T> clazz, Object settings) {
        return createIndex(getPersistentEntityFor(clazz).getIndexName(), settings);
    }

    @SuppressWarnings("rawtypes")
    private <T> Map getDefaultSettings(ElasticsearchPersistentEntity<T> persistentEntity) {

        if (persistentEntity.isUseServerConfiguration())
            return Maps.newHashMap();

        return new MapBuilder<String, String>().put("index.number_of_shards", String.valueOf(persistentEntity.getShards())).put("index.number_of_replicas", String.valueOf(persistentEntity.getReplicas())).put("index.refresh_interval", persistentEntity.getRefreshInterval()).put("index.store.type", persistentEntity.getIndexStoreType()).map();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public <T> Map getSetting(Class<T> clazz) {
        return getSetting(getPersistentEntityFor(clazz).getIndexName());
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Map getSetting(String indexName) {
        Preconditions.checkNotNull(indexName, "No index defined for getSettings");
        return client.admin().indices().getSettings(new GetSettingsRequest()).actionGet().getIndexToSettings().get(indexName).getAsMap();
    }

    //
    private <T> SearchRequestBuilder prepareSearch(Query query, Class<T> clazz) {
        setPersistentEntityIndexAndType(query, clazz);
        return prepareSearch(query);
    }

    //组装查询条件
    private SearchRequestBuilder prepareSearch(Query query) {
        Preconditions.checkNotNull(query.getIndices(), "No index defined for Query");
        Preconditions.checkNotNull(query.getTypes(), "No type defined for Query");

        int startRecord = 0;
        SearchRequestBuilder searchRequestBuilder = client.prepareSearch(CollectionUtil.toArray(query.getIndices())).setSearchType(query.getSearchType()).setTypes(CollectionUtil.toArray(query.getTypes()));

        if (query.getPageable() != null) {
            startRecord = query.getPageable().getPageNumber() * query.getPageable().getPageSize();
            searchRequestBuilder.setSize(query.getPageable().getPageSize());
        }

        searchRequestBuilder.setFrom(startRecord);

        if (!query.getFields().isEmpty()) {
            searchRequestBuilder.addFields(CollectionUtil.toArray(query.getFields()));
        }

        if (query.getSort() != null) {
            for (Sort.Order order : query.getSort()) {
                searchRequestBuilder.addSort(order.getProperty(), order.getDirection() == Sort.Direction.DESC ? SortOrder.DESC : SortOrder.ASC);
            }
        }

        if (query.getMinScore() > 0) {
            searchRequestBuilder.setMinScore(query.getMinScore());
        }
        return searchRequestBuilder;
    }

    private IndexRequestBuilder prepareIndex(IndexQuery query) {
        try {
            String indexName = StringUtils.isBlank(query.getIndexName()) ? retrieveIndexNameFromPersistentEntity(query.getObject().getClass())[0] : query.getIndexName();
            String type = StringUtils.isBlank(query.getType()) ? retrieveTypeFromPersistentEntity(query.getObject().getClass())[0] : query.getType();

            IndexRequestBuilder indexRequestBuilder = null;

            if (query.getObject() != null) {
                String entityId = null;
                if (isDocument(query.getObject().getClass())) {
                    entityId = getPersistentEntityId(query.getObject());
                }
                // If we have a query id and a document id, do not ask ES to
                // generate one.
                if (query.getId() != null && entityId != null) {
                    indexRequestBuilder = client.prepareIndex(indexName, type, query.getId());
                } else {
                    indexRequestBuilder = client.prepareIndex(indexName, type);
                }
                indexRequestBuilder.setSource(resultsMapper.getEntityMapper().mapToString(query.getObject()));
            } else if (query.getSource() != null) {
                indexRequestBuilder = client.prepareIndex(indexName, type, query.getId()).setSource(query.getSource());
            } else {
                throw new ElasticsearchException("object or source is null, failed to index the document [id: " + query.getId() + "]");
            }
            if (query.getVersion() != null) {
                indexRequestBuilder.setVersion(query.getVersion());
                indexRequestBuilder.setVersionType(EXTERNAL);
            }

            if (query.getParentId() != null) {
                indexRequestBuilder.setParent(query.getParentId());
            }

            return indexRequestBuilder;
        } catch (IOException e) {
            throw new ElasticsearchException("failed to index the document [id: " + query.getId() + "]", e);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void refresh(String indexName, boolean waitForOperation) {
        client.admin().indices().refresh(Requests.refreshRequest(indexName).force(waitForOperation)).actionGet();
    }

    @SuppressWarnings({ "rawtypes", "deprecation" })
    @Override
    public <T> void refresh(Class<T> clazz, boolean waitForOperation) {
        ElasticsearchPersistentEntity persistentEntity = getPersistentEntityFor(clazz);
        client.admin().indices().refresh(Requests.refreshRequest(persistentEntity.getIndexName()).force(waitForOperation)).actionGet();
    }

    @Override
    public Boolean addAlias(AliasQuery query) {
        Preconditions.checkNotNull(query.getIndexName(), "No index defined for Alias");
        Preconditions.checkNotNull(query.getAliasName(), "No alias defined");
        AliasAction aliasAction = new AliasAction(ADD, query.getIndexName(), query.getAliasName());
        if (query.getFilterBuilder() != null) {
            aliasAction.filter(query.getFilterBuilder());
        } else if (query.getFilter() != null) {
            aliasAction.filter(query.getFilter());
        } else if (StringUtils.isNotBlank(query.getRouting())) {
            aliasAction.routing(query.getRouting());
        } else if (StringUtils.isNotBlank(query.getSearchRouting())) {
            aliasAction.searchRouting(query.getSearchRouting());
        } else if (StringUtils.isNotBlank(query.getIndexRouting())) {
            aliasAction.indexRouting(query.getIndexRouting());
        }
        return client.admin().indices().prepareAliases().addAliasAction(aliasAction).execute().actionGet().isAcknowledged();
    }

    @Override
    public Boolean removeAlias(AliasQuery query) {
        Preconditions.checkNotNull(query.getIndexName(), "No index defined for Alias");
        Preconditions.checkNotNull(query.getAliasName(), "No alias defined");
        return client.admin().indices().prepareAliases().removeAlias(query.getIndexName(), query.getAliasName()).execute().actionGet().isAcknowledged();
    }

    @Override
    public Set<String> queryForAlias(String indexName) {
        ClusterStateRequest clusterStateRequest = Requests.clusterStateRequest().routingTable(true).nodes(true).indices(indexName);
        Iterator<String> iterator = client.admin().cluster().state(clusterStateRequest).actionGet().getState().getMetaData().aliases().keysIt();
        return Sets.newHashSet(iterator);
    }

    // 获取一个类的持久化document配置
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private ElasticsearchPersistentEntity getPersistentEntityFor(Class clazz) {
        Preconditions.checkState(clazz.isAnnotationPresent(Document.class), "Unable to identify index name. " + clazz.getSimpleName() + " is not a Document. Make sure the document class is annotated with @Document(indexName=\"foo\")");
        return elasticsearchConverter.getMappingContext().getPersistentEntity(clazz);
    }

    @SuppressWarnings("rawtypes")
    private String getPersistentEntityId(Object entity) {
        PersistentProperty idProperty = getPersistentEntityFor(entity.getClass()).getIdProperty();
        if (idProperty != null) {
            Method getter = idProperty.getGetter();
            if (getter != null) {
                try {
                    Object id = getter.invoke(entity);
                    if (id != null) {
                        return String.valueOf(id);
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        } else {
            logger.info("has no id property");
        }
        return null;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void setPersistentEntityId(Object entity, String id) {
        PersistentProperty idProperty = getPersistentEntityFor(entity.getClass()).getIdProperty();
        // Only deal with String because ES generated Ids are strings !
        if (idProperty != null && idProperty.getType().isAssignableFrom(String.class)) {
            Method setter = idProperty.getSetter();
            if (setter != null) {
                try {
                    setter.invoke(entity, id);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private void setPersistentEntityIndexAndType(Query query, Class clazz) {
        if (query.getIndices().isEmpty()) {
            query.addIndices(retrieveIndexNameFromPersistentEntity(clazz));
        }
        if (query.getTypes().isEmpty()) {
            query.addTypes(retrieveTypeFromPersistentEntity(clazz));
        }
    }

    @SuppressWarnings("rawtypes")
    private String[] retrieveIndexNameFromPersistentEntity(Class clazz) {
        if (clazz != null) {
            return new String[] { getPersistentEntityFor(clazz).getIndexName() };
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    private String[] retrieveTypeFromPersistentEntity(Class clazz) {
        if (clazz != null) {
            return new String[] { getPersistentEntityFor(clazz).getIndexType() };
        }
        return null;
    }

    private List<String> extractIds(SearchResponse response) {
        List<String> ids = new ArrayList<String>();
        for (SearchHit hit : response.getHits()) {
            if (hit != null) {
                ids.add(hit.getId());
            }
        }
        return ids;
    }

    protected ResultsMapper getResultsMapper() {
        return resultsMapper;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private boolean isDocument(Class clazz) {
        return clazz.isAnnotationPresent(Document.class);
    }

    /**
     *  从class中读取文件 此方法未实现 
     * @param url
     * @return
     */
    public static String readFileFromClasspath(String url) {
        StringBuilder stringBuilder = new StringBuilder();

        return stringBuilder.toString();
    }

    public SuggestResponse suggest(SuggestBuilder.SuggestionBuilder<?> suggestion, String... indices) {
        SuggestRequestBuilder suggestRequestBuilder = client.prepareSuggest(indices);
        suggestRequestBuilder.addSuggestion(suggestion);
        return suggestRequestBuilder.execute().actionGet();
    }

    @SuppressWarnings("rawtypes")
    public SuggestResponse suggest(SuggestBuilder.SuggestionBuilder<?> suggestion, Class clazz) {
        return suggest(suggestion, retrieveIndexNameFromPersistentEntity(clazz));
    }

    @Override
    public <T> T queryForObject(DSLQuery query, Class<T> clazz) {
        return null;
    }
}
