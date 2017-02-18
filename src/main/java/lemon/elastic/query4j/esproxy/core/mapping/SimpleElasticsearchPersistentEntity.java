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
package lemon.elastic.query4j.esproxy.core.mapping;

import java.util.Locale;

import org.elasticsearch.common.Preconditions;

import lemon.elastic.query4j.esproxy.annotations.Document;
import lemon.elastic.query4j.esproxy.annotations.Parent;
import lemon.elastic.query4j.esproxy.annotations.Setting;
import lemon.elastic.query4j.esproxy.mapping.model.BasicPersistentEntity;
import lemon.elastic.query4j.util.SpringStringUtils;
import lemon.elastic.query4j.util.TypeInformation;

/**
 * Elasticsearch specific {@link lemon.elastic.query4j.esproxy.mapping.PersistentEntity} implementation holding
 * 由配置映射出来的实体参数民对象
 * @param <T>
 * @author Rizwan Idrees
 * @author Mohsin Husen
 */
public class SimpleElasticsearchPersistentEntity<T> extends BasicPersistentEntity<T, ElasticsearchPersistentProperty> implements ElasticsearchPersistentEntity<T> {

    //    private final StandardEvaluationContext context;
    //    private final SpelExpressionParser parser;

    private String indexName;
    private String indexType;
    private boolean useServerConfiguration;
    private short shards;
    private short replicas;
    private String refreshInterval;
    private String indexStoreType;
    private String parentType;
    private ElasticsearchPersistentProperty parentIdProperty;
    private String settingPath;

    public SimpleElasticsearchPersistentEntity(TypeInformation<T> typeInformation) {
        super(typeInformation);

        Class<T> clazz = typeInformation.getType();
        if (clazz.isAnnotationPresent(Document.class)) {
            Document document = clazz.getAnnotation(Document.class);
            Preconditions.checkState(SpringStringUtils.hasText(document.indexName()), " Unknown indexName. Make sure the indexName is defined. e.g @Document(indexName=\"foo\")");
            this.indexName = document.indexName();
            this.indexType = SpringStringUtils.hasText(document.type()) ? document.type() : clazz.getSimpleName().toLowerCase(Locale.ENGLISH);
            this.useServerConfiguration = document.useServerConfiguration();
            this.shards = document.shards();
            this.replicas = document.replicas();
            this.refreshInterval = document.refreshInterval();
            this.indexStoreType = document.indexStoreType();
        }
        if (clazz.isAnnotationPresent(Setting.class)) {
            this.settingPath = typeInformation.getType().getAnnotation(Setting.class).settingPath();
        }
    }

    //此处应该是一个过滤验证操作
    @Override
    public String getIndexName() {
        //        Expression expression = parser.parseExpression(indexName, ParserContext.TEMPLATE_EXPRESSION);
        //        return expression.getValue(context, String.class);
        return indexName;
    }

    @Override
    public String getIndexType() {
        //        Expression expression = parser.parseExpression(indexType, ParserContext.TEMPLATE_EXPRESSION);
        //        return expression.getValue(context, String.class);
        return indexType;
    }

    @Override
    public String getIndexStoreType() {
        return indexStoreType;
    }

    @Override
    public short getShards() {
        return shards;
    }

    @Override
    public short getReplicas() {
        return replicas;
    }

    @Override
    public boolean isUseServerConfiguration() {
        return useServerConfiguration;
    }

    @Override
    public String getRefreshInterval() {
        return refreshInterval;
    }

    @Override
    public String getParentType() {
        return parentType;
    }

    @Override
    public ElasticsearchPersistentProperty getParentIdProperty() {
        return parentIdProperty;
    }

    @Override
    public String settingPath() {
        return settingPath;
    }

    @Override
    public void addPersistentProperty(ElasticsearchPersistentProperty property) {
        super.addPersistentProperty(property);

        if (property.getField() != null) {
            Parent parent = property.getField().getAnnotation(Parent.class);
            if (parent != null) {
                Preconditions.checkNotNull(this.parentIdProperty, "Only one field can hold a @Parent annotation");
                Preconditions.checkNotNull(this.parentType, "Only one field can hold a @Parent annotation");
                Preconditions.checkState(property.getType() == String.class, "Parent ID property should be String");
                this.parentIdProperty = property;
                this.parentType = parent.type();
            }
        }

        //        if (property.isVersionProperty()) {
        //            Preconditions.checkState(property.getType() == Long.class, "Version property should be Long");
        //        }
    }
}
