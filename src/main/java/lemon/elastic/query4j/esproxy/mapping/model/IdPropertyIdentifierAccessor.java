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
package lemon.elastic.query4j.esproxy.mapping.model;

import com.google.common.base.Preconditions;

import lemon.elastic.query4j.esproxy.mapping.IdentifierAccessor;
import lemon.elastic.query4j.esproxy.mapping.PersistentEntity;
import lemon.elastic.query4j.esproxy.mapping.PersistentProperty;
import lemon.elastic.query4j.esproxy.mapping.PersistentPropertyAccessor;

/**
 * Default implementation of {@link IdentifierAccessor}.
 * 
 * @author Christoph Strobl
 * @author Oliver Gierke
 * @since 1.10
 */
public class IdPropertyIdentifierAccessor implements IdentifierAccessor {

    private final PersistentPropertyAccessor accessor;
    private final PersistentProperty<?> idProperty;

    /**
     * Creates a new {@link IdPropertyIdentifierAccessor} for the given {@link PersistentEntity} and
     * {@link ConvertingPropertyAccessor}.
     * 
     * @param entity must not be {@literal null}.
     * @param target must not be {@literal null}.
     */

    public IdPropertyIdentifierAccessor(PersistentEntity<?, ?> entity, Object target) {

        Preconditions.checkNotNull(entity, "PersistentEntity must not be 'null'");
        Preconditions.checkState(entity.hasIdProperty(), "PersistentEntity does not have an identifier property!");

        this.idProperty = entity.getIdProperty();
        this.accessor = entity.getPropertyAccessor(target);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.keyvalue.core.IdentifierAccessor#getIdentifier()
     */
    public Object getIdentifier() {
        return accessor.getProperty(idProperty);
    }
}
