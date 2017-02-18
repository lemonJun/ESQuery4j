/*
 * Copyright 2013 the original author or authors.
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

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.fuzzyQuery;
import static org.elasticsearch.index.query.QueryBuilders.queryString;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.apache.lucene.queryparser.flexible.core.util.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.BoostableQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import lemon.elastic.query4j.esproxy.core.query.Criteria;
import lemon.elastic.query4j.esproxy.core.query.Criteria.OperationKey;

/**
 * 把Criteria查询 转成 es的query查询
 * 
 * @author WangYazhou
 * @date 2016年4月7日 下午8:06:05
 * @see
 */
class CriteriaQueryProcessor implements Processor {

    private static final Logger logger = LoggerFactory.getLogger(CriteriaQueryProcessor.class);

    QueryBuilder createQueryFromCriteria(Criteria criteria) {
        try {
            Preconditions.checkNotNull(criteria, "cirteria can not be null");
            List<QueryBuilder> shouldQueryBuilderList = new LinkedList<QueryBuilder>();
            List<QueryBuilder> mustNotQueryBuilderList = new LinkedList<QueryBuilder>();
            List<QueryBuilder> mustQueryBuilderList = new LinkedList<QueryBuilder>();

            ListIterator<Criteria> chainIterator = criteria.getCriteriaChain().listIterator();

            QueryBuilder firstQuery = null;
            boolean negateFirstQuery = false;

            while (chainIterator.hasNext()) {
                Criteria chainedCriteria = chainIterator.next();
                QueryBuilder queryFragmentForCriteria = createQueryFragmentForCriteria(chainedCriteria);
                if (queryFragmentForCriteria != null) {
                    if (firstQuery == null) {
                        firstQuery = queryFragmentForCriteria;
                        negateFirstQuery = chainedCriteria.isNegating();
                        continue;
                    }
                    if (chainedCriteria.isOr()) {
                        shouldQueryBuilderList.add(queryFragmentForCriteria);
                    } else if (chainedCriteria.isNegating()) {
                        mustNotQueryBuilderList.add(queryFragmentForCriteria);
                    } else {
                        mustQueryBuilderList.add(queryFragmentForCriteria);
                    }
                }
            }

            if (firstQuery != null) {
                if (!shouldQueryBuilderList.isEmpty() && mustNotQueryBuilderList.isEmpty() && mustQueryBuilderList.isEmpty()) {
                    shouldQueryBuilderList.add(0, firstQuery);
                } else {
                    if (negateFirstQuery) {
                        mustNotQueryBuilderList.add(0, firstQuery);
                    } else {
                        mustQueryBuilderList.add(0, firstQuery);
                    }
                }
            }

            BoolQueryBuilder query = null;

            if (!shouldQueryBuilderList.isEmpty() || !mustNotQueryBuilderList.isEmpty() || !mustQueryBuilderList.isEmpty()) {

                query = boolQuery();

                for (QueryBuilder qb : shouldQueryBuilderList) {
                    query.should(qb);
                }
                for (QueryBuilder qb : mustNotQueryBuilderList) {
                    query.mustNot(qb);
                }
                for (QueryBuilder qb : mustQueryBuilderList) {
                    query.must(qb);
                }
            }
            return query;
        } catch (Exception e) {
            logger.error("", e);
            return null;
        }

    }

    private QueryBuilder createQueryFragmentForCriteria(Criteria chainedCriteria) {
        if (chainedCriteria.getQueryCriteriaEntries().isEmpty())
            return null;

        Iterator<Criteria.CriteriaEntry> it = chainedCriteria.getQueryCriteriaEntries().iterator();
        boolean singeEntryCriteria = (chainedCriteria.getQueryCriteriaEntries().size() == 1);

        String fieldName = chainedCriteria.getField().getName();
        Preconditions.checkNotNull(fieldName, "Unknown field");
        QueryBuilder query = null;

        if (singeEntryCriteria) {
            Criteria.CriteriaEntry entry = it.next();
            query = processCriteriaEntry(entry, fieldName);
        } else {
            query = boolQuery();
            while (it.hasNext()) {
                Criteria.CriteriaEntry entry = it.next();
                ((BoolQueryBuilder) query).must(processCriteriaEntry(entry, fieldName));
            }
        }

        addBoost(query, chainedCriteria.getBoost());
        return query;
    }

    private QueryBuilder processCriteriaEntry(Criteria.CriteriaEntry entry, /* OperationKey key, Object value,*/ String fieldName) {
        Object value = entry.getValue();
        if (value == null) {
            return null;
        }
        OperationKey key = entry.getKey();
        QueryBuilder query = null;

        String searchText = StringUtils.toString(value);

        Iterable<Object> collection = null;

        switch (key) {
            case EQUALS:
                query = queryString(searchText).field(fieldName).defaultOperator(QueryStringQueryBuilder.Operator.AND);
                break;
            case CONTAINS:
                query = queryString("*" + searchText + "*").field(fieldName).analyzeWildcard(true);
                break;
            case STARTS_WITH:
                query = queryString(searchText + "*").field(fieldName).analyzeWildcard(true);
                break;
            case ENDS_WITH:
                query = queryString("*" + searchText).field(fieldName).analyzeWildcard(true);
                break;
            case EXPRESSION:
                query = queryString(searchText).field(fieldName);
                break;
            case LESS_EQUAL:
                query = rangeQuery(fieldName).lte(value);
                break;
            case GREATER_EQUAL:
                query = rangeQuery(fieldName).gte(value);
                break;
            case BETWEEN:
                Object[] ranges = (Object[]) value;
                query = rangeQuery(fieldName).from(ranges[0]).to(ranges[1]);
                break;
            case LESS:
                query = rangeQuery(fieldName).lt(value);
                break;
            case GREATER:
                query = rangeQuery(fieldName).gt(value);
                break;
            case FUZZY:
                query = fuzzyQuery(fieldName, searchText);
                break;
            case IN:
                query = boolQuery();
                collection = (Iterable<Object>) value;
                for (Object item : collection) {
                    ((BoolQueryBuilder) query).should(queryString(item.toString()).field(fieldName));
                }
                break;
            case NOT_IN:
                query = boolQuery();
                collection = (Iterable<Object>) value;
                for (Object item : collection) {
                    ((BoolQueryBuilder) query).mustNot(queryString(item.toString()).field(fieldName));
                }
                break;
        }
        return query;
    }

    private void addBoost(QueryBuilder query, float boost) {
        if (Float.isNaN(boost)) {
            return;
        }
        if (query instanceof BoostableQueryBuilder) {
            ((BoostableQueryBuilder) query).boost(boost);
        }
    }
}
