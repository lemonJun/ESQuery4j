package lemon.elastic.query4j.esproxy.core.query;

/**
 * Defines a IndexBoost to be applied on the "indices_boost" query clause
 *
 * @author Thiago Locatelli
 */
public class IndexBoost {

    private String indexName;
    private float boost;

    public IndexBoost(String indexName, float boost) {
        this.indexName = indexName;
        this.boost = boost;
    }

    public String getIndexName() {
        return indexName;
    }

    public float getBoost() {
        return boost;
    }

}
