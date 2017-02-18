package lemon.elastic.query4j.esproxy.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.elasticsearch.common.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

import lemon.elastic.query4j.esproxy.annotations.CompletionField;
import lemon.elastic.query4j.esproxy.annotations.DateFormat;
import lemon.elastic.query4j.esproxy.annotations.Field;
import lemon.elastic.query4j.esproxy.annotations.FieldIndex;
import lemon.elastic.query4j.esproxy.annotations.FieldType;
import lemon.elastic.query4j.esproxy.annotations.GeoPointField;
import lemon.elastic.query4j.esproxy.annotations.MultiField;
import lemon.elastic.query4j.esproxy.annotations.NestedField;
import lemon.elastic.query4j.esproxy.annotations.RoutingField;
import lemon.elastic.query4j.esproxy.annotations.Transient;
import lemon.elastic.query4j.esproxy.core.completion.Completion;
import lemon.elastic.query4j.esproxy.core.geo.GeoPoint;
import lemon.elastic.query4j.esproxy.mapping.model.SimpleTypeHolder;
import lemon.elastic.query4j.util.ClassTypeInformation;
import lemon.elastic.query4j.util.GenericCollectionTypeResolver;
import lemon.elastic.query4j.util.NumberUtils;
import lemon.elastic.query4j.util.SpringStringUtils;
import lemon.elastic.query4j.util.TypeInformation;

/**
 * XContentBuilder不好的地方在于 需要手动指定开始符和结束符 很容易弄乱
 * https://www.elastic.co/guide/en/elasticsearch/reference/current/mapping-params.html
 * 
 * @author WangYazhou
 * @date 2016年4月7日 下午2:08:26
 * @see
 */
public class MappingJSONBuilder {

    private static final Logger logger = LoggerFactory.getLogger(MappingJSONBuilder.class);

    public static final String EMPTY = "";

    public static final String FIELD_STORE = "store";
    public static final String FIELD_TYPE = "type";
    public static final String FIELD_INDEX = "index";
    public static final String FIELD_FORMAT = "format";
    public static final String FIELD_SEARCH_ANALYZER = "search_analyzer";
    public static final String FIELD_INDEX_ANALYZER = "index_analyzer";
    public static final String FIELD_SIMILARITY = "similarity";
    public static final String FIELD_PROPERTIES = "properties";
    public static final String FIELD_PARENT = "_parent";

    public static final String COMPLETION_PAYLOADS = "payloads";
    public static final String COMPLETION_PRESERVE_SEPARATORS = "preserve_separators";
    public static final String COMPLETION_PRESERVE_POSITION_INCREMENTS = "preserve_position_increments";
    public static final String COMPLETION_MAX_INPUT_LENGTH = "max_input_length";

    public static final String INDEX_VALUE_NOT_ANALYZED = "not_analyzed";
    public static final String TYPE_VALUE_STRING = "string";
    public static final String TYPE_VALUE_GEO_POINT = "geo_point";
    public static final String TYPE_VALUE_COMPLETION = "completion";
    public static final String TYPE_VALUE_GEO_HASH_PREFIX = "geohash_prefix";
    public static final String TYPE_VALUE_GEO_HASH_PRECISION = "geohash_precision";

    private static SimpleTypeHolder SIMPLE_TYPE_HOLDER = new SimpleTypeHolder();

    //生成json的配置文件
    @SuppressWarnings("rawtypes")
    static String buildMapping(Class clazz, String indexType, String idFieldName, String parentType) throws IOException {
        JSONObject json = new JSONObject();
        if (SpringStringUtils.hasText(parentType)) {
            json.put(FIELD_PARENT, new JSONObject().put(FIELD_TYPE, parentType));
        }
        buildRouting(json, clazz);//生成路由信息
        //生成mapping信息
        mapEntity(json, clazz, true, idFieldName, EMPTY, false, FieldType.Auto, null);
        logger.info(json.toJSONString());

        return json.toJSONString();

    }

    //生成路由信息
    @SuppressWarnings("rawtypes")
    private static void buildRouting(JSONObject json, Class clazz) {
        java.lang.reflect.Field[] fields = retrieveFields(clazz);
        for (java.lang.reflect.Field field : fields) {
            boolean isRoutingFiled = isRoutingField(field);
            if (isRoutingFiled) {
                try {
                    JSONObject routing = new JSONObject();
                    RoutingField singleField = field.getAnnotation(RoutingField.class);
                    routing.put("required", singleField.required());
                    routing.put("path", singleField.path());
                    json.put("_routing", routing);
                } catch (Exception e) {
                    logger.error("_routing error", e);
                }
            }
        }
    }

    //生成mapping信息
    @SuppressWarnings("rawtypes")
    private static void mapEntity(JSONObject json, Class clazz, boolean isRootObject, String idFieldName, String nestedObjectFieldName, boolean nestedOrObjectField, FieldType fieldType, Field fieldAnnotation) throws IOException {

        java.lang.reflect.Field[] fields = retrieveFields(clazz);
        if (!isRootObject && (isAnyPropertyAnnotatedAsField(fields) || nestedOrObjectField)) {
            String type = FieldType.Object.toString().toLowerCase();
            if (nestedOrObjectField) {
                type = fieldType.toString().toLowerCase();
            }
            logger.info(nestedObjectFieldName + ":" + type);
            JSONObject nested = new JSONObject();
            json.put(nestedObjectFieldName, nested.put(FIELD_TYPE, type));
            if (nestedOrObjectField && FieldType.Nested == fieldType && fieldAnnotation.includeInParent()) {
                nested.put("include_in_parent", fieldAnnotation.includeInParent());
            }
        }

        JSONObject properties = new JSONObject();
        for (java.lang.reflect.Field field : fields) {

            if (field.isAnnotationPresent(Transient.class) || isInIgnoreFields(field)) {
                continue;
            }
            boolean isGeoPointField = isGeoPointField(field);
            boolean isCompletionField = isCompletionField(field);
            Field singleField = field.getAnnotation(Field.class);
            if (!isGeoPointField && !isCompletionField && isEntity(field) && isAnnotated(field)) {
                if (singleField == null) {
                    continue;
                }
                boolean nestedOrObject = isNestedOrObjectField(field);
                mapEntity(json, getFieldType(field), false, EMPTY, field.getName(), nestedOrObject, singleField.type(), field.getAnnotation(Field.class));
                if (nestedOrObject) {
                    continue;
                }
            }

            MultiField multiField = field.getAnnotation(MultiField.class);
            if (isGeoPointField) {
                applyGeoPointFieldMapping(properties, field);
            }

            if (isCompletionField) {
                CompletionField completionField = field.getAnnotation(CompletionField.class);
                applyCompletionFieldMapping(properties, field, completionField);
            }

            if (isRootObject && singleField != null && isIdField(field, idFieldName)) {
                applyDefaultIdFieldMapping(properties, field);
            } else if (multiField != null) {
                addMultiFieldMapping(properties, field, multiField);
            } else if (singleField != null) {
                addSingleFieldMapping(properties, field, singleField);
            }
        }

        json.put(FIELD_PROPERTIES, properties);

        if (!isRootObject && isAnyPropertyAnnotatedAsField(fields) || nestedOrObjectField) {
        }
    }

    private static void applyGeoPointFieldMapping(JSONObject properties, java.lang.reflect.Field field) throws IOException {
        JSONObject geo = new JSONObject();
        geo.put(FIELD_TYPE, TYPE_VALUE_GEO_POINT);

        GeoPointField annotation = field.getAnnotation(GeoPointField.class);
        if (annotation != null) {
            if (annotation.geoHashPrefix()) {
                geo.put(TYPE_VALUE_GEO_HASH_PREFIX, true);
                if (StringUtils.isNotEmpty(annotation.geoHashPrecision())) {
                    if (NumberUtils.isNumber(annotation.geoHashPrecision())) {
                        geo.put(TYPE_VALUE_GEO_HASH_PRECISION, Integer.parseInt(annotation.geoHashPrecision()));
                    } else {
                        geo.put(TYPE_VALUE_GEO_HASH_PRECISION, annotation.geoHashPrecision());
                    }
                }
            }
        }
        properties.put(field.getName(), geo);

    }

    private static void applyCompletionFieldMapping(JSONObject properties, java.lang.reflect.Field field, CompletionField annotation) throws IOException {
        JSONObject completion = new JSONObject();
        completion.put(FIELD_TYPE, TYPE_VALUE_COMPLETION);
        if (annotation != null) {
            completion.put(COMPLETION_MAX_INPUT_LENGTH, annotation.maxInputLength());
            completion.put(COMPLETION_PAYLOADS, annotation.payloads());
            completion.put(COMPLETION_PRESERVE_POSITION_INCREMENTS, annotation.preservePositionIncrements());
            completion.put(COMPLETION_PRESERVE_SEPARATORS, annotation.preserveSeparators());
            if (StringUtils.isNotBlank(annotation.searchAnalyzer())) {
                completion.put(FIELD_SEARCH_ANALYZER, annotation.searchAnalyzer());
            }
            if (StringUtils.isNotBlank(annotation.indexAnalyzer())) {
                completion.put(FIELD_INDEX_ANALYZER, annotation.indexAnalyzer());
            }
        }
        properties.put(field.getName(), completion);
    }

    private static void applyDefaultIdFieldMapping(JSONObject properties, java.lang.reflect.Field field) throws IOException {
        JSONObject d = new JSONObject();
        d.put(FIELD_TYPE, TYPE_VALUE_STRING);
        d.put(FIELD_INDEX, INDEX_VALUE_NOT_ANALYZED);
        properties.put(field.getName(), d);
    }

    /**
     * Apply mapping for a single @Field annotation
     *
     * @throws IOException
     */
    private static void addSingleFieldMapping(JSONObject properties, java.lang.reflect.Field field, Field fieldAnnotation) throws IOException {
        JSONObject single = new JSONObject();
        single.put(FIELD_STORE, fieldAnnotation.store());//是否存储
        if (FieldType.Auto != fieldAnnotation.type()) {
            single.put(FIELD_TYPE, fieldAnnotation.type().name().toLowerCase());
            if (FieldType.Date == fieldAnnotation.type() && DateFormat.none != fieldAnnotation.format()) {
                single.put(FIELD_FORMAT, DateFormat.custom == fieldAnnotation.format() ? fieldAnnotation.pattern() : fieldAnnotation.format());
            }
        }
        if (FieldIndex.not_analyzed == fieldAnnotation.index() || FieldIndex.no == fieldAnnotation.index()) {
            single.put(FIELD_INDEX, fieldAnnotation.index().name().toLowerCase());//字段是否分析
        }
        if (StringUtils.isNotBlank(fieldAnnotation.searchAnalyzer())) {//归回分词
            single.put(FIELD_SEARCH_ANALYZER, fieldAnnotation.searchAnalyzer());
        }
        if (StringUtils.isNotBlank(fieldAnnotation.indexAnalyzer())) {//索引分词
            single.put(FIELD_INDEX_ANALYZER, fieldAnnotation.indexAnalyzer());
        }
        if (StringUtils.isNotBlank(fieldAnnotation.similarity())) {//打分算法
            single.put(FIELD_SIMILARITY, fieldAnnotation.similarity());
        }
        //设置索引每个字段的权重
        logger.info(field.getName() + "-->" + single.toJSONString());
        properties.put(field.getName(), single);
    }

    /**
     * Apply mapping for a single nested @Field annotation
     *
     * @throws IOException
     */
    private static void addNestedFieldMapping(JSONObject properties, java.lang.reflect.Field field, NestedField annotation) throws IOException {
        JSONObject nested = new JSONObject();
        nested.put(FIELD_STORE, annotation.store());
        if (FieldType.Auto != annotation.type()) {
            nested.put(FIELD_TYPE, annotation.type().name().toLowerCase());
        }
        if (FieldIndex.not_analyzed == annotation.index()) {
            nested.put(FIELD_INDEX, annotation.index().name().toLowerCase());
        }
        if (StringUtils.isNotBlank(annotation.searchAnalyzer())) {
            nested.put(FIELD_SEARCH_ANALYZER, annotation.searchAnalyzer());
        }
        if (StringUtils.isNotBlank(annotation.indexAnalyzer())) {
            nested.put(FIELD_INDEX_ANALYZER, annotation.indexAnalyzer());
        }
        properties.put(field.getName() + "." + annotation.dotSuffix(), nested);
    }

    /**
     * Multi field mappings for string type fields, support for sorts and facets
     *
     * @throws IOException
     */
    private static void addMultiFieldMapping(JSONObject properties, java.lang.reflect.Field field, MultiField annotation) throws IOException {
        JSONObject mult = new JSONObject();
        mult.put(FIELD_TYPE, "multi_field");

        JSONObject fields = new JSONObject();
        addSingleFieldMapping(fields, field, annotation.mainField());
        for (NestedField nestedField : annotation.otherFields()) {
            addNestedFieldMapping(fields, field, nestedField);
        }
        mult.put("fields", fields);
        properties.put(field.getName(), mult);
    }

    @SuppressWarnings("rawtypes")
    protected static boolean isEntity(java.lang.reflect.Field field) {
        TypeInformation typeInformation = ClassTypeInformation.from(field.getType());
        Class<?> clazz = getFieldType(field);
        boolean isComplexType = !SIMPLE_TYPE_HOLDER.isSimpleType(clazz);
        return isComplexType && !Map.class.isAssignableFrom(typeInformation.getType());
    }

    @SuppressWarnings("rawtypes")
    protected static Class<?> getFieldType(java.lang.reflect.Field field) {
        Class<?> clazz = field.getType();
        TypeInformation typeInformation = ClassTypeInformation.from(clazz);
        if (typeInformation.isCollectionLike()) {
            clazz = GenericCollectionTypeResolver.getCollectionFieldType(field) != null ? GenericCollectionTypeResolver.getCollectionFieldType(field) : typeInformation.getComponentType().getType();
        }
        return clazz;
    }

    //有field注解
    private static boolean isAnyPropertyAnnotatedAsField(java.lang.reflect.Field[] fields) {
        if (fields != null) {
            for (java.lang.reflect.Field field : fields) {
                if (field.isAnnotationPresent(Field.class)) {
                    return true;
                }
            }
        }
        return false;
    }

    @SuppressWarnings("rawtypes")
    private static java.lang.reflect.Field[] retrieveFields(Class clazz) {
        // Create list of fields.
        List<java.lang.reflect.Field> fields = new ArrayList<java.lang.reflect.Field>();

        // Keep backing up the inheritance hierarchy.
        Class targetClass = clazz;
        do {
            fields.addAll(Arrays.asList(targetClass.getDeclaredFields()));
            targetClass = targetClass.getSuperclass();
        } while (targetClass != null && targetClass != Object.class);

        return fields.toArray(new java.lang.reflect.Field[fields.size()]);
    }

    private static boolean isAnnotated(java.lang.reflect.Field field) {
        return field.getAnnotation(Field.class) != null || field.getAnnotation(MultiField.class) != null || field.getAnnotation(GeoPointField.class) != null || field.getAnnotation(CompletionField.class) != null;
    }

    private static boolean isIdField(java.lang.reflect.Field field, String idFieldName) {
        return idFieldName.equals(field.getName());
    }

    private static boolean isInIgnoreFields(java.lang.reflect.Field field) {
        Field fieldAnnotation = field.getAnnotation(Field.class);
        if (null != fieldAnnotation) {
            String[] ignoreFields = fieldAnnotation.ignoreFields();
            return Arrays.asList(ignoreFields).contains(field.getName());
        }
        return false;
    }

    private static boolean isNestedOrObjectField(java.lang.reflect.Field field) {
        Field fieldAnnotation = field.getAnnotation(Field.class);
        return fieldAnnotation != null && (FieldType.Nested == fieldAnnotation.type() || FieldType.Object == fieldAnnotation.type());
    }

    private static boolean isGeoPointField(java.lang.reflect.Field field) {
        return field.getType() == GeoPoint.class || field.getAnnotation(GeoPointField.class) != null;
    }

    private static boolean isCompletionField(java.lang.reflect.Field field) {
        return field.getType() == Completion.class;
    }

    private static boolean isRoutingField(java.lang.reflect.Field field) {
        return field.getAnnotation(RoutingField.class) != null;
    }

}
