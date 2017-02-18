package lemon.elastic4j.test.chew;

import java.util.Date;

import lemon.elastic.query4j.esproxy.annotations.Document;
import lemon.elastic.query4j.esproxy.annotations.Field;
import lemon.elastic.query4j.esproxy.annotations.FieldIndex;
import lemon.elastic.query4j.esproxy.annotations.FieldType;
import lemon.elastic.query4j.esproxy.annotations.GeoPointField;
import lemon.elastic.query4j.esproxy.annotations.Id;

/**
 * {"index.refresh_interval":"-1","index.creation_date":"1436266178118","index.uuid":"2RD-pNv-TU6F0u9l9wJIZA","index.version.created":"1050299","index.number_of_replicas":"0","index.number_of_shards":"5","index.store.type":"fs"}
 * {"properties":{"date":{"type":"date","format":"dateOptionalTime"},"uid":{"type":"long"},"edu":{"type":"integer"},"farea":{"type":"string"},"location":{"type":"geo_point"},"exp":{"type":"integer"},"salary":{"type":"integer"},"title":{"type":"string"},"fcate":{"type":"string"}}}
 *
 * @author wangyazhou
 * @version 1.0
 * @date  2015年7月7日 下午6:47:56
 * @see 
 * @since
 */
@Document(indexName = "daitouresume", type = "resume", shards = 5, replicas = 1, refreshInterval = "-1")
public class ElasticResume {

    @Id
    private Long _id;//简历ID

    @Field(type = FieldType.Long)
    private long uid;//用户ID

    @Field(type = FieldType.Integer)
    private int age;//年龄

    @Field(type = FieldType.Integer)
    private int exp;//工作经验

    @Field(type = FieldType.Integer)
    private int edu;//教育经历

    @Field(type = FieldType.Integer)
    private int salary;//薪水

    @Field(type = FieldType.String)
    private String fcate;//归属类别全路径 以分隔

    @Field(type = FieldType.String)
    private String farea;//归属区域全路径 以分隔

    @Field(type = FieldType.Integer)
    private int Complete;

    @Field(type = FieldType.Integer)
    private int gender;

    @GeoPointField
    private String location;//纬度经度 受限于es的表述

    @Field(type = FieldType.Date)
    private Date date;//日期

    @Field(type = FieldType.String, index = FieldIndex.analyzed)
    private String title;//简历名称

    public Long get_id() {
        return _id;
    }

    public void set_id(Long _id) {
        this._id = _id;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public int getEdu() {
        return edu;
    }

    public void setEdu(int edu) {
        this.edu = edu;
    }

    public int getSalary() {
        return salary;
    }

    public void setSalary(int salary) {
        this.salary = salary;
    }

    public String getFcate() {
        return fcate;
    }

    public void setFcate(String fcate) {
        this.fcate = fcate;
    }

    public String getFarea() {
        return farea;
    }

    public void setFarea(String farea) {
        this.farea = farea;
    }

    public int getComplete() {
        return Complete;
    }

    public void setComplete(int complete) {
        Complete = complete;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

}
