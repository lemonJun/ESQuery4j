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
package lemon.elastic.query4j.esproxy.core.mapping;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;

import lemon.elastic.query4j.esproxy.mapping.context.AbstractMappingContext;
import lemon.elastic.query4j.esproxy.mapping.model.SimpleTypeHolder;
import lemon.elastic.query4j.util.TypeInformation;

/**
 * 凡是上下文信息 理论上应该都是线程级别的
 * 如果我们的每一次操作都带BEAN对象的CLASS的话，理论上应该是不需要CONTEXT了
 *
 * @author Rizwan Idrees
 * @author Mohsin Husen
 */
public class SimpleElasticsearchMappingContext extends AbstractMappingContext<SimpleElasticsearchPersistentEntity<?>, ElasticsearchPersistentProperty> {


    @Override
    protected <T> SimpleElasticsearchPersistentEntity<?> createPersistentEntity(TypeInformation<T> typeInformation) {
        final SimpleElasticsearchPersistentEntity<T> persistentEntity = new SimpleElasticsearchPersistentEntity<T>(typeInformation);
        return persistentEntity;
    }
    
    @Override
    protected ElasticsearchPersistentProperty createPersistentProperty(Field field, PropertyDescriptor descriptor, SimpleElasticsearchPersistentEntity<?> owner, SimpleTypeHolder simpleTypeHolder) {
        return new SimpleElasticsearchPersistentProperty(field, descriptor, owner, simpleTypeHolder);
    }
        
}
