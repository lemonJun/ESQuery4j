package lemon.elastic4j.test.jobmvBM25;

import lemon.elastic.query4j.esproxy.annotations.Document;
import lemon.elastic.query4j.esproxy.annotations.Field;
import lemon.elastic.query4j.esproxy.annotations.FieldIndex;
import lemon.elastic.query4j.esproxy.annotations.FieldType;
import lemon.elastic.query4j.esproxy.annotations.Id;
import lemon.elastic.query4j.esproxy.annotations.Setting;

/**
 * 需要插入到ES中的实体
 * Created by luhe on 2016/5/18.
 */
@Setting(settingPath = "C:/Users/58/git/elastic-query4j/config/setting.txt")
@Document(indexName = "jobmovev2", type = "company", shards = 5, replicas = 1, refreshInterval = "1")
public class CompanyEsInfoV2 {

    //企业ID
    @Id
    @Field(type = FieldType.String, index = FieldIndex.analyzed)
    private String id;
    //企业名称

    @Field(type = FieldType.String, indexAnalyzer = "mmseg", searchAnalyzer = "mmseg", similarity = "BM25")
    private String comName;

    @Field(type = FieldType.Long)
    private long comnamehash;

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

    public long getComnamehash() {
        return comnamehash;
    }

    public void setComnamehash(long comnamehash) {
        this.comnamehash = comnamehash;
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
}
