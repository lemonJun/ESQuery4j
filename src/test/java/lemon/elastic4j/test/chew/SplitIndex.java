package lemon.elastic4j.test.chew;

import java.util.Date;

import lemon.elastic.query4j.esproxy.annotations.Document;
import lemon.elastic.query4j.esproxy.annotations.Field;
import lemon.elastic.query4j.esproxy.annotations.FieldIndex;
import lemon.elastic.query4j.esproxy.annotations.FieldType;
import lemon.elastic.query4j.esproxy.annotations.Id;
import lemon.elastic.query4j.esproxy.annotations.RoutingField;

@Document(indexName = "chew", type = "split", useServerConfiguration = false, shards = 5, replicas = 0, refreshInterval = "-1")
//对应的是配置文件@Setting()
public class SplitIndex {

    @Id
    private String id;//简历id 

    @Field(type = FieldType.String, index = FieldIndex.analyzed, store = false, indexAnalyzer = "standard")
    private String cvname;

    @Field(type = FieldType.String, index = FieldIndex.analyzed, store = false, indexAnalyzer = "standard")
    @RoutingField(required = true, path = "cate")
    private String cate;

    @Field(type = FieldType.Date)
    private Date date;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCvname() {
        return cvname;
    }

    public void setCvname(String cvname) {
        this.cvname = cvname;
    }

    public String getCate() {
        return cate;
    }

    public void setCate(String cate) {
        this.cate = cate;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

}
