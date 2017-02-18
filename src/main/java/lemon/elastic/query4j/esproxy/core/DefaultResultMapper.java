/*
 * Copyright 2014 the original author or authors.
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.base.Strings;
import org.elasticsearch.common.jackson.core.JsonEncoding;
import org.elasticsearch.common.jackson.core.JsonFactory;
import org.elasticsearch.common.jackson.core.JsonGenerator;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.elasticsearch.search.facet.Facet;

import lemon.elastic.query4j.esproxy.annotations.Document;
import lemon.elastic.query4j.esproxy.core.facet.DefaultFacetMapper;
import lemon.elastic.query4j.esproxy.core.facet.FacetResult;
import lemon.elastic.query4j.esproxy.core.mapping.ElasticsearchPersistentEntity;
import lemon.elastic.query4j.esproxy.core.mapping.ElasticsearchPersistentProperty;
import lemon.elastic.query4j.esproxy.domain.Pageable;
import lemon.elastic.query4j.esproxy.mapping.PersistentProperty;
import lemon.elastic.query4j.esproxy.mapping.context.MappingContext;

/**
 * 把结果转成具体的类
 * 
 *
 * @author WangYazhou
 * @date 2016年4月8日 下午5:02:03
 * @see
 */
public class DefaultResultMapper extends AbstractResultMapper {

    private MappingContext<? extends ElasticsearchPersistentEntity<?>, ElasticsearchPersistentProperty> mappingContext;

    public DefaultResultMapper() {
        super(new DefaultEntityMapper());
    }

    public DefaultResultMapper(MappingContext<? extends ElasticsearchPersistentEntity<?>, ElasticsearchPersistentProperty> mappingContext) {
        super(new DefaultEntityMapper());
        this.mappingContext = mappingContext;
    }

    public DefaultResultMapper(EntityMapper entityMapper) {
        super(entityMapper);
    }

    public DefaultResultMapper(MappingContext<? extends ElasticsearchPersistentEntity<?>, ElasticsearchPersistentProperty> mappingContext, EntityMapper entityMapper) {
        super(entityMapper);
        this.mappingContext = mappingContext;
    }

    @Override
    public <T> FacetedPage<T> mapResults(SearchResponse response, Class<T> clazz, Pageable pageable) {
        long totalHits = response.getHits().totalHits();
        List<T> results = new ArrayList<T>();
        for (SearchHit hit : response.getHits()) {
            if (hit != null) {
                T result = null;
                if (!Strings.isNullOrEmpty(hit.sourceAsString())) {
                    result = mapEntity(hit.sourceAsString(), clazz);
                } else {
                    result = mapEntity(hit.getFields().values(), clazz);
                }
                setPersistentEntityId(result, hit.getId(), clazz);
                results.add(result);
            }
        }
        List<FacetResult> facets = new ArrayList<FacetResult>();
        if (response.getFacets() != null) {
            for (Facet facet : response.getFacets()) {
                FacetResult facetResult = DefaultFacetMapper.parse(facet);
                if (facetResult != null) {
                    facets.add(facetResult);
                }
            }
        }

        return new FacetedPageImpl<T>(results, pageable, totalHits, facets);
    }

    private <T> T mapEntity(Collection<SearchHitField> values, Class<T> clazz) {
        return mapEntity(buildJSONFromFields(values), clazz);
    }

    private String buildJSONFromFields(Collection<SearchHitField> values) {
        JsonFactory nodeFactory = new JsonFactory();
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            JsonGenerator generator = nodeFactory.createGenerator(stream, JsonEncoding.UTF8);
            generator.writeStartObject();
            for (SearchHitField value : values) {
                if (value.getValues().size() > 1) {
                    generator.writeArrayFieldStart(value.getName());
                    for (Object val : value.getValues()) {
                        generator.writeObject(val);
                    }
                    generator.writeEndArray();
                } else {
                    generator.writeObjectField(value.getName(), value.getValue());
                }
            }
            generator.writeEndObject();
            generator.flush();
            return new String(stream.toByteArray(), Charset.forName("UTF-8"));
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public <T> T mapResult(GetResponse response, Class<T> clazz) {
        T result = mapEntity(response.getSourceAsString(), clazz);
        if (result != null) {
            setPersistentEntityId(result, response.getId(), clazz);
        }
        return result;
    }

    @Override
    public <T> LinkedList<T> mapResults(MultiGetResponse responses, Class<T> clazz) {
        LinkedList<T> list = new LinkedList<T>();
        for (MultiGetItemResponse response : responses.getResponses()) {
            if (!response.isFailed() && response.getResponse().isExists()) {
                T result = mapEntity(response.getResponse().getSourceAsString(), clazz);
                setPersistentEntityId(result, response.getResponse().getId(), clazz);
                list.add(result);
            }
        }
        return list;
    }

    private <T> void setPersistentEntityId(T result, String id, Class<T> clazz) {
        if (mappingContext != null && clazz.isAnnotationPresent(Document.class)) {
            PersistentProperty<ElasticsearchPersistentProperty> idProperty = mappingContext.getPersistentEntity(clazz).getIdProperty();
            // Only deal with String because ES generated Ids are strings !
            if (idProperty != null && idProperty.getType().isAssignableFrom(String.class)) {
                Method setter = idProperty.getSetter();
                if (setter != null) {
                    try {
                        setter.invoke(result, id);
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            }
        }
    }
}
