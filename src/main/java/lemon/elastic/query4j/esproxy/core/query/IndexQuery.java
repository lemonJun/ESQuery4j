
package lemon.elastic.query4j.esproxy.core.query;

/**
 * 索引对象
 * 对应的是索引更新对象
 *
 *
 * @author lemon
 * @version 1.0
 * @date  2016年4月8日 下午11:12:15
 * @see   
 * @since
 */
public class IndexQuery {

    private String id;
    private Object object;
    private Long version;
    private String indexName;
    private String type;
    private String source;
    private String parentId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }
}
