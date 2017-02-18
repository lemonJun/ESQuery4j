package lemon.elastic.query4j.util;

import static java.lang.String.format;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.common.base.Preconditions;

/**
 * {@link LinkedHashMap} subclass representing annotation attribute key/value pairs
 * as read by Spring's reflection- or ASM-based {@link
 * org.springframework.core.type.AnnotationMetadata AnnotationMetadata} implementations.
 * Provides 'pseudo-reification' to avoid noisy Map generics in the calling code as well
 * as convenience methods for looking up annotation attributes in a type-safe fashion.
 *
 * @author Chris Beams
 * @since 3.1.1
 */
@SuppressWarnings("serial")
public class AnnotationAttributes extends LinkedHashMap<String, Object> {

    /**
     * Create a new, empty {@link AnnotationAttributes} instance.
     */
    public AnnotationAttributes() {
    }

    /**
     * Create a new, empty {@link AnnotationAttributes} instance with the given initial
     * capacity to optimize performance.
     * @param initialCapacity initial size of the underlying map
     */
    public AnnotationAttributes(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * Create a new {@link AnnotationAttributes} instance, wrapping the provided map
     * and all its key/value pairs.
     * @param map original source of annotation attribute key/value pairs to wrap
     * @see #fromMap(Map)
     */
    public AnnotationAttributes(Map<String, Object> map) {
        super(map);
    }

    /**
     * Return an {@link AnnotationAttributes} instance based on the given map; if the map
     * is already an {@code AnnotationAttributes} instance, it is casted and returned
     * immediately without creating any new instance; otherwise create a new instance by
     * wrapping the map with the {@link #AnnotationAttributes(Map)} constructor.
     * @param map original source of annotation attribute key/value pairs
     */
    public static AnnotationAttributes fromMap(Map<String, Object> map) {
        if (map == null) {
            return null;
        }

        if (map instanceof AnnotationAttributes) {
            return (AnnotationAttributes) map;
        }

        return new AnnotationAttributes(map);
    }

    public String getString(String attributeName) {
        return doGet(attributeName, String.class);
    }

    public String[] getStringArray(String attributeName) {
        return doGet(attributeName, String[].class);
    }

    public boolean getBoolean(String attributeName) {
        return doGet(attributeName, Boolean.class);
    }

    @SuppressWarnings("unchecked")
    public <N extends Number> N getNumber(String attributeName) {
        return (N) doGet(attributeName, Integer.class);
    }

    @SuppressWarnings("unchecked")
    public <E extends Enum<?>> E getEnum(String attributeName) {
        return (E) doGet(attributeName, Enum.class);
    }

    @SuppressWarnings("unchecked")
    public <T> Class<? extends T> getClass(String attributeName) {
        return (Class<T>) doGet(attributeName, Class.class);
    }

    public Class<?>[] getClassArray(String attributeName) {
        return doGet(attributeName, Class[].class);
    }

    public AnnotationAttributes getAnnotation(String attributeName) {
        return doGet(attributeName, AnnotationAttributes.class);
    }

    public AnnotationAttributes[] getAnnotationArray(String attributeName) {
        return doGet(attributeName, AnnotationAttributes[].class);
    }

    @SuppressWarnings("unchecked")
    private <T> T doGet(String attributeName, Class<T> expectedType) {
        Preconditions.checkState(SpringStringUtils.hasText(attributeName), "attributeName must not be null or empty");
        Object value = this.get(attributeName);
        Preconditions.checkNotNull(value, format("Attribute '%s' not found", attributeName));
        Preconditions.checkState(ClassUtils.isAssignable(expectedType, value.getClass()), format("Attribute '%s' is of type [%s], but [%s] was expected. Cause: ", attributeName, value.getClass().getSimpleName(), expectedType.getSimpleName()));
        return (T) value;
    }
}