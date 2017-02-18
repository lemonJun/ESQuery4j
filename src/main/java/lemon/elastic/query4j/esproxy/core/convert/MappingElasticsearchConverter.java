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
package lemon.elastic.query4j.esproxy.core.convert;

import com.google.common.base.Preconditions;

import lemon.elastic.query4j.esproxy.core.mapping.ElasticsearchPersistentEntity;
import lemon.elastic.query4j.esproxy.core.mapping.ElasticsearchPersistentProperty;
import lemon.elastic.query4j.esproxy.mapping.context.MappingContext;

/**
 * MappingElasticsearchConverter
 * 
 * @author Rizwan Idrees
 * @author Mohsin Husen
 */
public class MappingElasticsearchConverter implements ElasticsearchConverter {

    private final MappingContext<? extends ElasticsearchPersistentEntity<?>, ElasticsearchPersistentProperty> mappingContext;
    //    private final GenericConversionService conversionService;

    //ZTODO
    //    @SuppressWarnings("unused")
    //    private ApplicationContext applicationContext;

    public MappingElasticsearchConverter(MappingContext<? extends ElasticsearchPersistentEntity<?>, ElasticsearchPersistentProperty> mappingContext) {
        Preconditions.checkNotNull(mappingContext);
        this.mappingContext = mappingContext;
        //        this.conversionService = new DefaultConversionService();
    }

    @Override
    public MappingContext<? extends ElasticsearchPersistentEntity<?>, ElasticsearchPersistentProperty> getMappingContext() {
        return mappingContext;
    }

    //    @Override
    //    public ConversionService getConversionService() {
    //        return this.conversionService;
    //    }
    //
    //    @Override
    //    public void setApplicationContext(ApplicationContext applicationContext) throws Exception {
    //        this.applicationContext = applicationContext;
    //        if (mappingContext instanceof ApplicationContextAware) {
    //            ((ApplicationContextAware) mappingContext).setApplicationContext(applicationContext);
    //        }
    //    }
}
