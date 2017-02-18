package lemon.elastic4j.test.jobmv;

import lemon.elastic.query4j.esproxy.annotations.Document;
import lemon.elastic.query4j.esproxy.annotations.Field;
import lemon.elastic.query4j.esproxy.annotations.FieldIndex;
import lemon.elastic.query4j.esproxy.annotations.FieldType;
import lemon.elastic.query4j.esproxy.annotations.Id;

/**
 * 需要插入到ES中的实体
 * Created by luhe on 2016/5/18.
 */
@Document(indexName = "jobmoveproxy", type = "qdcompany", shards = 5, replicas = 1, refreshInterval = "1")
public class CompanyEsInfo {

    //企业ID
    @Id
    @Field(type = FieldType.String, index = FieldIndex.analyzed)
    private String id;

    //企业名称的simhash值,用来去重
    @Field(type = FieldType.Long)
    private long namesimhash;

    //企业名称
    @Field(type = FieldType.String, indexAnalyzer = "mmseg", searchAnalyzer = "mmseg")
    private String comName;

    @Field(type = FieldType.Integer)
    private int size;

    //添加时间
    @Field(type = FieldType.Long)
    private long addTime;

    //数据来源1-zl.2-51
    @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
    private String source;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getComName() {
        return comName;
    }

    public void setComName(String comName) {
        this.comName = comName;
    }

    public long getAddTime() {
        return addTime;
    }

    public void setAddTime(long addTime) {
        this.addTime = addTime;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public long getNamesimhash() {
        return namesimhash;
    }

    public void setNamesimhash(long namesimhash) {
        this.namesimhash = namesimhash;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

}
