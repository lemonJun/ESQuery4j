package com.bj58.chr.scf.cvsearch.entity;

import java.util.Date;

import lemon.elastic.query4j.esproxy.annotations.Document;
import lemon.elastic.query4j.esproxy.annotations.Field;
import lemon.elastic.query4j.esproxy.annotations.FieldType;
import lemon.elastic.query4j.esproxy.annotations.GeoPointField;
import lemon.elastic.query4j.esproxy.annotations.Id;

/**
 * 
 * @author WangYazhou
 * @date   2015年12月5日 下午5:17:51
 * @see    
 */
@Document(indexName = "chrdata", type = "cvall", shards = 5, replicas = 1, refreshInterval = "-1")
public class CVIndexBean {

    @Id
    private String cvid;//简历id 

    @GeoPointField
    private String latlng;//纬度经度 受限于es的表述

    @Field(type = FieldType.Integer)
    private Integer gender;//性别  1男  2女  同CvEnums中的gender的值

    @Field(type = FieldType.Date)
    private Date birthday;//出生日期

    @Field(type = FieldType.Integer)
    private Integer degreeid;//学历,取最高的那个

    @Field(type = FieldType.Date)
    private Date worktime;// 参加工作时间

    @Field(type = FieldType.Integer)
    private Integer photo;//是否有照片 1-是   0-无

    @Field(type = FieldType.Integer)
    private Integer marry;//是否结婚   1未  0已婚  cvenums

    @Field(type = FieldType.Integer)
    private Integer workstatus;//工作状态  在职：0, 离职：1  在Expectation中

    @Field(type = FieldType.Double)
    private Double expsalary;//期望薪资

    @Field(type = FieldType.Integer)
    private Integer negotiation;//是否接受面议   0=否 1=是

    @Field(type = FieldType.Integer)
    private Integer iscompleted;//是否完整     0 不完整   1 完整
    
    @Field(type = FieldType.Date)
    private Date reftime;//简历刷新时间   

    @Field(type = FieldType.String, indexAnalyzer = "mmseg", searchAnalyzer = "mmseg")
    private String living;//现居住地，全路径id

    @Field(type = FieldType.String, indexAnalyzer = "mmseg", searchAnalyzer = "mmseg")
    private String explocal;//期望工作地,全路径id

    //    @Field(type = FieldType.String)
    //    private String fulltext;//全文本

    @Field(type = FieldType.Integer)
    private Integer openstate;// 简历是否公开   1公开 2隐藏 3部分公开

    //    @Field(type = FieldType.String)
    //    private String expindname;//期望行业名称，多个值  fullname 中英文都有

    @Field(type = FieldType.String)
    private String expindcate;//期望行业类别,fullpath

    @Field(type = FieldType.String)
    private String expjobname;//期望职位名称，多个值,fullname,中英文值都存

    @Field(type = FieldType.String)
    private String expjobcate;//期望职位类别,fullpath

    @Field(type = FieldType.String)
    private String language;//语言能力，多个值

    @Field(type = FieldType.Integer)
    private Integer expjobtype;//求职性质  1全职，2兼职，3实习

    @Field(type = FieldType.Integer)
    private Integer graduate;//是否是应届  1-是    0-不是

    @Field(type = FieldType.String)
    private String college;//大学  多个值 以,分开

    @Field(type = FieldType.String)
    private String major;//专业   多个值

    //    @Field(type = FieldType.Long)
    //    private Long idlongext;//预留业务字段

    @Field(type = FieldType.Integer)
    private Float percent; //简历完整度的百分比

    @Field(type = FieldType.String)
    private String recentjob;//最近工作

    @Field(type = FieldType.String, indexAnalyzer = "mmseg", searchAnalyzer = "mmseg")
    private String recentcomp;//最近工作单位

    @Field(type = FieldType.String)
    private String allexp;//全部工作经历

    @Field(type = FieldType.String)
    private String proskills;//专业技能

    //    @Field(type = FieldType.String)
    //    private String training;//培训

    @Field(type = FieldType.String)
    private String certs;//证书

    //    @Field(type = FieldType.String)
    //    private String verified;//简历状态

    @Field(type = FieldType.Integer)
    private Integer hasfujian;//是否有附件 1有 0无

    public String getCvid() {
        return cvid;
    }

    public void setCvid(String cvid) {
        this.cvid = cvid;
    }

    public String getLatlng() {
        return latlng;
    }

    public void setLatlng(String latlng) {
        this.latlng = latlng;
    }

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public Integer getDegreeid() {
        return degreeid;
    }

    public void setDegreeid(Integer degreeid) {
        this.degreeid = degreeid;
    }

    public Date getWorktime() {
        return worktime;
    }

    public void setWorktime(Date worktime) {
        this.worktime = worktime;
    }

    public Integer getPhoto() {
        return photo;
    }

    public void setPhoto(Integer photo) {
        this.photo = photo;
    }

    public Integer getMarry() {
        return marry;
    }

    public void setMarry(Integer marry) {
        this.marry = marry;
    }

    public Integer getWorkstatus() {
        return workstatus;
    }

    public void setWorkstatus(Integer workstatus) {
        this.workstatus = workstatus;
    }

    public Double getExpsalary() {
        return expsalary;
    }

    public void setExpsalary(Double expsalary) {
        this.expsalary = expsalary;
    }

    public Integer getNegotiation() {
        return negotiation;
    }

    public void setNegotiation(Integer negotiation) {
        this.negotiation = negotiation;
    }

    public Integer getIscompleted() {
        return iscompleted;
    }

    public void setIscompleted(Integer iscompleted) {
        this.iscompleted = iscompleted;
    }

    public Date getReftime() {
        return reftime;
    }

    public void setReftime(Date reftime) {
        this.reftime = reftime;
    }

    public String getLiving() {
        return living;
    }

    public void setLiving(String living) {
        this.living = living;
    }

    public String getExplocal() {
        return explocal;
    }

    public void setExplocal(String explocal) {
        this.explocal = explocal;
    }

    public Integer getOpenstate() {
        return openstate;
    }

    public void setOpenstate(Integer openstate) {
        this.openstate = openstate;
    }

    public String getExpindcate() {
        return expindcate;
    }

    public void setExpindcate(String expindcate) {
        this.expindcate = expindcate;
    }

    public String getExpjobname() {
        return expjobname;
    }

    public void setExpjobname(String expjobname) {
        this.expjobname = expjobname;
    }

    public String getExpjobcate() {
        return expjobcate;
    }

    public void setExpjobcate(String expjobcate) {
        this.expjobcate = expjobcate;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Integer getExpjobtype() {
        return expjobtype;
    }

    public void setExpjobtype(Integer expjobtype) {
        this.expjobtype = expjobtype;
    }

    public Integer getGraduate() {
        return graduate;
    }

    public void setGraduate(Integer graduate) {
        this.graduate = graduate;
    }

    public String getCollege() {
        return college;
    }

    public void setCollege(String college) {
        this.college = college;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public Float getPercent() {
        return percent;
    }

    public void setPercent(Float percent) {
        this.percent = percent;
    }

    public String getRecentjob() {
        return recentjob;
    }

    public void setRecentjob(String recentjob) {
        this.recentjob = recentjob;
    }

    public String getRecentcomp() {
        return recentcomp;
    }

    public void setRecentcomp(String recentcomp) {
        this.recentcomp = recentcomp;
    }

    public String getAllexp() {
        return allexp;
    }

    public void setAllexp(String allexp) {
        this.allexp = allexp;
    }

    public String getProskills() {
        return proskills;
    }

    public void setProskills(String proskills) {
        this.proskills = proskills;
    }

    public String getCerts() {
        return certs;
    }

    public void setCerts(String certs) {
        this.certs = certs;
    }

    public Integer getHasfujian() {
        return hasfujian;
    }

    public void setHasfujian(Integer hasfujian) {
        this.hasfujian = hasfujian;
    }

}
