/*
 * Copyright 2013-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package lemon.elastic.query4j.esproxy.core;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.elasticsearch.action.update.UpdateResponse;

import lemon.elastic.query4j.esproxy.core.query.AliasQuery;
import lemon.elastic.query4j.esproxy.core.query.CriteriaQuery;
import lemon.elastic.query4j.esproxy.core.query.DSLQuery;
import lemon.elastic.query4j.esproxy.core.query.DeleteQuery;
import lemon.elastic.query4j.esproxy.core.query.GetQuery;
import lemon.elastic.query4j.esproxy.core.query.IndexQuery;
import lemon.elastic.query4j.esproxy.core.query.MoreLikeThisQuery;
import lemon.elastic.query4j.esproxy.core.query.SearchQuery;
import lemon.elastic.query4j.esproxy.core.query.StringQuery;
import lemon.elastic.query4j.esproxy.core.query.UpdateQuery;
import lemon.elastic.query4j.esproxy.domain.Page;
import lemon.elastic.query4j.util.CloseableIterator;

/**
 * ElasticsearchOperations
 *
 * @author Rizwan Idrees
 * @author Mohsin Husen
 * @author Kevin Leturc
 */
public interface ElasticsearchOperations {

    /**
     * Create an index for a class
     *
     * @param clazz
     * @param <T>
     */
    <T> boolean createIndex(Class<T> clazz);

    /**
     * Create an index for given indexName
     *
     * @param indexName
     */
    boolean createIndex(String indexName);

    /**
     * Create an index for given indexName and Settings
     *
     * @param indexName
     * @param settings
     */
    boolean createIndex(String indexName, Object settings);

    /**
     * Create an index for given class and Settings
     *
     * @param clazz
     * @param settings
     */
    <T> boolean createIndex(Class<T> clazz, Object settings);

    /**
     * Create mapping for a class
     *
     * @param clazz
     * @param <T>
     */
    <T> boolean putMapping(Class<T> clazz);

    /**
     * Create mapping for a given indexName and type
     *
     * @param indexName
     * @param type
     * @param mappings
     */
    boolean putMapping(String indexName, String type, Object mappings);

    /**
     * Create mapping for a class
     *
     * @param clazz
     * @param mappings
     */
    <T> boolean putMapping(Class<T> clazz, Object mappings);

    /**
     * Get mapping for a class
     *
     * @param clazz
     * @param <T>
     */
    <T> Map getMapping(Class<T> clazz);

    /**
     * Get mapping for a given indexName and type
     *
     * @param indexName
     * @param type
     */
    Map getMapping(String indexName, String type);

    /**
     * Get settings for a given indexName
     *
     * @param indexName
     */
    Map getSetting(String indexName);

    /**
     * Get settings for a given class
     *
     * @param clazz
     */
    <T> Map getSetting(Class<T> clazz);

    /**
     * Execute the query against elasticsearch and return the first returned
     * object
     *
     * @param query
     * @param clazz
     * @return the first matching object
     */
    <T> T queryForObject(GetQuery query, Class<T> clazz);

    /**
     * Execute the query against elasticsearch and return the first returned
     * object using custom mapper
     *
     * @param query
     * @param clazz
     * @param mapper
     * @return the first matching object
     */
    <T> T queryForObject(GetQuery query, Class<T> clazz, GetResultMapper mapper);

    /**
     * Execute the query against elasticsearch and return the first returned
     * object
     *
     * @param query
     * @param clazz
     * @return the first matching object
     */
    <T> T queryForObject(CriteriaQuery query, Class<T> clazz);

    /**
     * Execute the query against elasticsearch and return the first returned
     * object
     *
     * @param query
     * @param clazz
     * @return the first matching object
     */
    <T> T queryForObject(DSLQuery query, Class<T> clazz);

    /**
     * Execute the query against elasticsearch and return the first returned object
     *
     * @param query
     * @param clazz
     * @return the first matching object
     */
    <T> T queryForObject(StringQuery query, Class<T> clazz);

    /**
     * Execute the query against elasticsearch and return result as {@link Page}
     *
     * @param query
     * @param clazz
     * @return
     */
    <T> FacetedPage<T> queryForPage(SearchQuery query, Class<T> clazz);

    /**
     * Execute the query against elasticsearch and return result as {@link Page}
     * using custom mapper
     *
     * @param query
     * @param clazz
     * @return
     */
    <T> FacetedPage<T> queryForPage(SearchQuery query, Class<T> clazz, SearchResultMapper mapper);

    /**
     * Execute the query against elasticsearch and return result as {@link Page}
     *
     * @param query
     * @param clazz
     * @return
     */
    <T> Page<T> queryForPage(CriteriaQuery query, Class<T> clazz);

    /**
     * Execute the query against elasticsearch and return result as {@link Page}
     *
     * @param query
     * @param clazz
     * @return
     */
    <T> FacetedPage<T> queryForPage(StringQuery query, Class<T> clazz);

    /**
     * Execute the query against elasticsearch and return result as {@link Page}
     * using custom mapper
     *
     * @param query
     * @param clazz
     * @return
     */
    <T> FacetedPage<T> queryForPage(StringQuery query, Class<T> clazz, SearchResultMapper mapper);

    /**
     * Executes the given {@link CriteriaQuery} against elasticsearch and return
     * result as {@link CloseableIterator}. <p> Returns a
     * {@link CloseableIterator} that wraps an Elasticsearch scroll context that
     * needs to be closed in case of error.
     *
     * @param <T>
     *        element return type
     * @param query
     * @param clazz
     * @return
     * @since 1.3
     */
    <T> CloseableIterator<T> stream(CriteriaQuery query, Class<T> clazz);

    /**
     * Executes the given {@link SearchQuery} against elasticsearch and return
     * result as {@link CloseableIterator}. <p> Returns a
     * {@link CloseableIterator} that wraps an Elasticsearch scroll context that
     * needs to be closed in case of error.
     *
     * @param <T>
     *        element return type
     * @param query
     * @param clazz
     * @return
     * @since 1.3
     */
    <T> CloseableIterator<T> stream(SearchQuery query, Class<T> clazz);

    /**
     * Executes the given {@link SearchQuery} against elasticsearch and return
     * result as {@link CloseableIterator} using custom mapper. <p> Returns a
     * {@link CloseableIterator} that wraps an Elasticsearch scroll context that
     * needs to be closed in case of error.
     *
     * @param <T>
     *        element return type
     * @param query
     * @param clazz
     * @param mapper
     * @return
     * @since 1.3
     */
    <T> CloseableIterator<T> stream(SearchQuery query, Class<T> clazz, SearchResultMapper mapper);

    /**
     * Execute the criteria query against elasticsearch and return result as
     * {@link List}
     *
     * @param query
     * @param clazz
     * @param <T>
     * @return
     */
    <T> List<T> queryForList(CriteriaQuery query, Class<T> clazz);

    /**
     * Execute the string query against elasticsearch and return result as
     * {@link List}
     *
     * @param query
     * @param clazz
     * @param <T>
     * @return
     */
    <T> List<T> queryForList(StringQuery query, Class<T> clazz);

    /**
     * Execute the search query against elasticsearch and return result as
     * {@link List}
     *
     * @param query
     * @param clazz
     * @param <T>
     * @return
     */
    <T> List<T> queryForList(SearchQuery query, Class<T> clazz);

    /**
     * Execute the query against elasticsearch and return ids
     *
     * @param query
     * @return
     */
    <T> List<String> queryForIds(SearchQuery query);

    /**
     * return number of elements found by given query
     *
     * @param query
     * @param clazz
     * @return
     */
    <T> long count(CriteriaQuery query, Class<T> clazz);

    /**
     * return number of elements found by given query
     *
     * @param query
     * @return
     */
    <T> long count(CriteriaQuery query);

    /**
     * return number of elements found by given query
     *
     * @param query
     * @param clazz
     * @return
     */
    <T> long count(SearchQuery query, Class<T> clazz);

    /**
     * return number of elements found by given query
     *
     * @param query
     * @return
     */
    <T> long count(SearchQuery query);

    /**
     * Execute a multiGet against elasticsearch for the given ids
     *
     * @param searchQuery
     * @param clazz
     * @return
     */
    <T> LinkedList<T> multiGet(SearchQuery searchQuery, Class<T> clazz);

    /**
     * Execute a multiGet against elasticsearch for the given ids with
     * MultiGetResultMapper
     *
     * @param searchQuery
     * @param clazz
     * @param multiGetResultMapper
     * @return
     */
    <T> LinkedList<T> multiGet(SearchQuery searchQuery, Class<T> clazz, MultiGetResultMapper multiGetResultMapper);

    /**
     * Index an object. Will do save or update
     *
     * @param query
     * @return returns the document id
     */
    String index(IndexQuery query);

    /**
     * Partial update of the document
     *
     * @param updateQuery
     * @return
     */
    UpdateResponse update(UpdateQuery updateQuery);

    /**
     * Bulk index all objects. Will do save or update
     *
     * @param queries
     */
    void bulkIndex(List<IndexQuery> queries);

    /**
     * Bulk update all objects. Will do update
     *
     * @param queries
     */
    void bulkUpdate(List<UpdateQuery> queries);

    /**
     * Delete the one object with provided id
     *
     * @param indexName
     * @param type
     * @param id
     * @return documentId of the document deleted
     */
    String delete(String indexName, String type, String id);

    /**
     * Delete all records matching the criteria
     *
     * @param clazz
     * @param criteriaQuery
     */
    <T> void delete(CriteriaQuery criteriaQuery, Class<T> clazz);

    /**
     * Delete the one object with provided id
     *
     * @param clazz
     * @param id
     * @return documentId of the document deleted
     */
    <T> String delete(Class<T> clazz, String id);

    /**
     * Delete all records matching the query
     *
     * @param clazz
     * @param query
     */
    <T> void delete(DeleteQuery query, Class<T> clazz);

    /**
     * Delete all records matching the query
     *
     * @param query
     */
    void delete(DeleteQuery query);

    /**
     * Deletes an index for given entity
     *
     * @param clazz
     * @param <T>
     * @return
     */
    <T> boolean deleteIndex(Class<T> clazz);

    /**
     * Deletes an index for given indexName
     *
     * @param indexName
     * @return
     */
    boolean deleteIndex(String indexName);

    /**
     * Deletes a type in an index
     *
     * @param index
     * @param type
     */
    void deleteType(String index, String type);

    /**
     * check if index is exists
     *
     * @param clazz
     * @param <T>
     * @return
     */
    <T> boolean indexExists(Class<T> clazz);

    /**
     * check if index is exists for given IndexName
     *
     * @param indexName
     * @return
     */
    boolean indexExists(String indexName);

    /**
     * check if type is exists in an index
     *
     * @param index
     * @param type
     * @return
     */
    boolean typeExists(String index, String type);

    /**
     * refresh the index
     *
     * @param indexName
     * @param waitForOperation
     */
    void refresh(String indexName, boolean waitForOperation);

    /**
     * refresh the index
     *
     * @param clazz
     * @param waitForOperation
     */
    <T> void refresh(Class<T> clazz, boolean waitForOperation);

    /**
     * Returns scroll id for criteria query
     *
     * @param query
     * @param scrollTimeInMillis
     * @param noFields
     * @return
     */
    String scan(CriteriaQuery query, long scrollTimeInMillis, boolean noFields);

    /**
     * Returns scroll id for scan query
     *
     * @param query
     * @param scrollTimeInMillis
     * @param noFields
     * @return
     */
    String scan(SearchQuery query, long scrollTimeInMillis, boolean noFields);

    /**
     * Scrolls the results for give scroll id
     *
     * @param scrollId
     * @param scrollTimeInMillis
     * @param clazz
     * @param <T>
     * @return
     */
    <T> Page<T> scroll(String scrollId, long scrollTimeInMillis, Class<T> clazz);

    /**
     * Scrolls the results for give scroll id using custom result mapper
     *
     * @param scrollId
     * @param scrollTimeInMillis
     * @param mapper
     * @param <T>
     * @return
     */
    <T> Page<T> scroll(String scrollId, long scrollTimeInMillis, SearchResultMapper mapper);

    /**
     * more like this query to search for documents that are "like" a specific
     * document.
     *
     * @param query
     * @param clazz
     * @param <T>
     * @return
     */
    <T> Page<T> moreLikeThis(MoreLikeThisQuery query, Class<T> clazz);

    /**
     * adding new alias
     *
     * @param query
     * @return
     */
    Boolean addAlias(AliasQuery query);

    /**
     * removing previously created alias
     *
     * @param query
     * @return
     */
    Boolean removeAlias(AliasQuery query);

    /**
     * get all the alias pointing to specified index
     *
     * @param indexName
     * @return
     */
    Set<String> queryForAlias(String indexName);

    <T> T query(SearchQuery query, ResultsExtractor<T> resultsExtractor);
}
