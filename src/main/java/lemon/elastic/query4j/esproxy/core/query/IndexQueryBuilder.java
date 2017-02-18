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
package lemon.elastic.query4j.esproxy.core.query;

/**
 * 流式风格的索引操作构建类
 * 
 * 
 * @author lemon
 * @version 1.0
 * @date  2016年4月8日 下午11:12:34
 * @see 
 * @since
 */
public class IndexQueryBuilder {

    private String id;
    private Object object;
    private Long version;
    private String indexName;
    private String type;
    private String source;
    private String parentId;

    public IndexQueryBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public IndexQueryBuilder withObject(Object object) {
        this.object = object;
        return this;
    }

    public IndexQueryBuilder withVersion(Long version) {
        this.version = version;
        return this;
    }

    public IndexQueryBuilder withIndexName(String indexName) {
        this.indexName = indexName;
        return this;
    }

    public IndexQueryBuilder withType(String type) {
        this.type = type;
        return this;
    }

    public IndexQueryBuilder withSource(String source) {
        this.source = source;
        return this;
    }

    public IndexQueryBuilder withParentId(String parentId) {
        this.parentId = parentId;
        return this;
    }

    public IndexQuery build() {
        IndexQuery indexQuery = new IndexQuery();
        indexQuery.setId(id);
        indexQuery.setIndexName(indexName);
        indexQuery.setType(type);
        indexQuery.setObject(object);
        indexQuery.setParentId(parentId);
        indexQuery.setSource(source);
        indexQuery.setVersion(version);
        return indexQuery;
    }
}
