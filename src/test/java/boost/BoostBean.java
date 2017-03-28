package boost;

import lemon.elastic.query4j.esproxy.annotations.Document;
import lemon.elastic.query4j.esproxy.annotations.Field;
import lemon.elastic.query4j.esproxy.annotations.FieldIndex;
import lemon.elastic.query4j.esproxy.annotations.FieldType;

@Document(indexName = "test_boost", type = "tb", shards = 5, replicas = 1, refreshInterval = "1")
public class BoostBean {
    @Field(type = FieldType.String, index = FieldIndex.analyzed)
    private String id;

    @Field(type = FieldType.String, index = FieldIndex.analyzed, indexAnalyzer = "ik", searchAnalyzer = "ik")
    private String name;

    @Field(type = FieldType.Float)
    private float _boost;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float get_boost() {
        return _boost;
    }
    
    public void set_boost(float _boost) {
        this._boost = _boost;
    }

}
