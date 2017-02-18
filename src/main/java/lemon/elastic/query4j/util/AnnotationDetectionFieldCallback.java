/*
 * Copyright 2012-2014 the original author or authors.
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
package lemon.elastic.query4j.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import com.google.common.base.Preconditions;

import lemon.elastic.query4j.util.ReflectionUtils.FieldCallback;

/**
 * A {@link FieldCallback} that will inspect each field for a given annotation. This field's type can then be accessed
 * afterwards.
 * 
 * @author Oliver Gierke
 */
public class AnnotationDetectionFieldCallback implements FieldCallback {

    private final Class<? extends Annotation> annotationType;
    private Field field;

    /**
     * Creates a new {@link AnnotationDetectionFieldCallback} scanning for an annotation of the given type.
     * 
     * @param annotationType must not be {@literal null}.
     */
    public AnnotationDetectionFieldCallback(Class<? extends Annotation> annotationType) {

        Preconditions.checkNotNull(annotationType);
        this.annotationType = annotationType;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.util.ReflectionUtils.FieldCallback#doWith(java.lang.reflect.Field)
     */
    public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {

        if (this.field != null) {
            return;
        }

        Annotation annotation = field.getAnnotation(annotationType);

        if (annotation != null) {

            this.field = field;
            lemon.elastic.query4j.util.ReflectionUtils.makeAccessible(this.field);
        }
    }

    /**
     * Returns the type of the field.
     * 
     * @return
     */
    public Class<?> getType() {
        return field == null ? null : field.getType();
    }

    /**
     * Retrieves the value of the field by reflection.
     * 
     * @param source must not be {@literal null}.
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T getValue(Object source) {

        Preconditions.checkNotNull(source);
        return field == null ? null : (T) lemon.elastic.query4j.util.ReflectionUtils.getField(field, source);
    }
}
