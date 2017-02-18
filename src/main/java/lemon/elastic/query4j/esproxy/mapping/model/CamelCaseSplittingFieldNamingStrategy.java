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

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;

import lemon.elastic.query4j.esproxy.mapping.PersistentProperty;
import lemon.elastic.query4j.util.ParsingUtils;
import lemon.elastic.query4j.util.SpringStringUtils;

/**
 * Configurable {@link FieldNamingStrategy} that splits up camel-case property names and reconcatenates them using a
 * configured delimiter. Individual parts of the name can be manipulated using {@link #preparePart(String)}.
 * 
 * @author Oliver Gierke
 * @since 1.9
 */
public class CamelCaseSplittingFieldNamingStrategy implements FieldNamingStrategy {

    private final String delimiter;

    /**
     * Creates a new {@link CamelCaseSplittingFieldNamingStrategy}.
     * 
     * @param delimiter must not be {@literal null}.
     */
    public CamelCaseSplittingFieldNamingStrategy(String delimiter) {

        Preconditions.checkNotNull(delimiter, "Delimiter must not be null!");
        this.delimiter = delimiter;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.mapping.model.FieldNamingStrategy#getFieldName(org.springframework.data.mapping.PersistentProperty)
     */
    @Override
    public String getFieldName(PersistentProperty<?> property) {

        List<String> parts = ParsingUtils.splitCamelCaseToLower(property.getName());
        List<String> result = new ArrayList<String>();

        for (String part : parts) {

            String candidate = preparePart(part);

            if (SpringStringUtils.hasText(candidate)) {
                result.add(candidate);
            }
        }

        return SpringStringUtils.collectionToDelimitedString(result, delimiter);
    }

    /**
     * Callback to prepare the uncapitalized part obtained from the split up of the camel case source. Default
     * implementation returns the part as is.
     * 
     * @param part
     * @return
     */
    protected String preparePart(String part) {
        return part;
    }
}
