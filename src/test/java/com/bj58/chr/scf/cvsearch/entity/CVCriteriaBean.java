//package com.bj58.chr.scf.cvsearch.entity;
//
//import lemon.elastic.query4j.provider.CriteriaEnum;
//import lemon.elastic.query4j.provider.RangeValue;
//import lemon.elastic.query4j.provider.SearchAnno;
//
///**
// * 此为查询对象   之所以对建索引对象分开是因为属性值的要求不同
// * 
// * 注意：与建索引对象的字段必须一一对应
// * @author WangYazhou
// * @date  2016年3月28日 下午4:50:57
// * @see
// */
//public class CVCriteriaBean {
//
//    public CVCriteriaBean() {//默认只取公开的简历
//        this.openstate = "1";
//        this.iscompleted = "1";
//    }
//
//    @SearchAnno
//    private String cvid;
//
//    @SearchAnno(condition = CriteriaEnum.CONTAINS)
//    private String cvname;
//
//    @SearchAnno
//    private String gender;
//
//    @SearchAnno(condition = CriteriaEnum.KEYWORD)
//    private String keyword;
//
//    @SearchAnno(condition = CriteriaEnum.DATE)
//    private String birthday;
//    @SearchAnno
//    private String degreeId;
//    @SearchAnno(condition = CriteriaEnum.DATE)
//    private String worktime;
//    @SearchAnno
//    private String photo;
//    @SearchAnno
//    private String marry;
//    @SearchAnno
//    private String workstatus;
//
//    @SearchAnno(condition = CriteriaEnum.RANGE)
//    private RangeValue expsalary;
//    @SearchAnno
//    private String negotiation;
//    @SearchAnno
//    private String iscompleted;
//    @SearchAnno(condition = CriteriaEnum.DATE)
//    private String reftime;
//    @SearchAnno
//    private String living;
//    @SearchAnno
//    private String explocal;
//    @SearchAnno
//    private String openstate;
//    @SearchAnno
//    private String expindname;
//    @SearchAnno
//    private String expindcate;
//    @SearchAnno
//    private String expjobname;
//    @SearchAnno
//    private String expjobcate;
//    @SearchAnno
//    private String language;
//    @SearchAnno
//    private String expjobtype;
//    @SearchAnno
//    private String graduate;
//
//    @SearchAnno(condition = CriteriaEnum.GEO)
//    private String latlng;
//
//    @SearchAnno(condition = CriteriaEnum.CONTAINS)
//    private String college;
//    @SearchAnno
//    private String major;
//
//    @SearchAnno(condition = CriteriaEnum.RANGE)
//    private RangeValue percent;
//    @SearchAnno
//    private String recentjob;
//    @SearchAnno
//    private String recentcomp;
//    @SearchAnno
//    private String allexp;
//    @SearchAnno
//    private String proskills;
//    @SearchAnno
//    private String training;
//    @SearchAnno
//    private String certs;
//    @SearchAnno
//    private String hasfujian;
//
//    public String getCvid() {
//        return cvid;
//    }
//
//    public void setCvid(String cvid) {
//        this.cvid = cvid;
//    }
//
//    public String getCvname() {
//        return cvname;
//    }
//
//    public void setCvname(String cvname) {
//        this.cvname = cvname;
//    }
//
//    public String getGender() {
//        return gender;
//    }
//
//    public void setGender(String gender) {
//        this.gender = gender;
//    }
//
//    public String getKeyword() {
//        return keyword;
//    }
//
//    public void setKeyword(String keyword) {
//        this.keyword = keyword;
//    }
//
//    public String getBirthday() {
//        return birthday;
//    }
//
//    public void setBirthday(String birthday) {
//        this.birthday = birthday;
//    }
//
//    public String getDegreeId() {
//        return degreeId;
//    }
//
//    public void setDegreeId(String degreeId) {
//        this.degreeId = degreeId;
//    }
//
//    public String getWorktime() {
//        return worktime;
//    }
//
//    public void setWorktime(String worktime) {
//        this.worktime = worktime;
//    }
//
//    public String getPhoto() {
//        return photo;
//    }
//
//    public void setPhoto(String photo) {
//        this.photo = photo;
//    }
//
//    public String getMarry() {
//        return marry;
//    }
//
//    public void setMarry(String marry) {
//        this.marry = marry;
//    }
//
//    public String getWorkstatus() {
//        return workstatus;
//    }
//
//    public void setWorkstatus(String workstatus) {
//        this.workstatus = workstatus;
//    }
//
//    public RangeValue getExpsalary() {
//        return expsalary;
//    }
//
//    public void setExpsalary(RangeValue expsalary) {
//        this.expsalary = expsalary;
//    }
//
//    public String getNegotiation() {
//        return negotiation;
//    }
//
//    public void setNegotiation(String negotiation) {
//        this.negotiation = negotiation;
//    }
//
//    public String getIscompleted() {
//        return iscompleted;
//    }
//
//    public void setIscompleted(String iscompleted) {
//        this.iscompleted = iscompleted;
//    }
//
//    public String getReftime() {
//        return reftime;
//    }
//
//    public void setReftime(String reftime) {
//        this.reftime = reftime;
//    }
//
//    public String getLiving() {
//        return living;
//    }
//
//    public void setLiving(String living) {
//        this.living = living;
//    }
//
//    public String getExplocal() {
//        return explocal;
//    }
//
//    public void setExplocal(String explocal) {
//        this.explocal = explocal;
//    }
//
//    public String getOpenstate() {
//        return openstate;
//    }
//
//    public void setOpenstate(String openstate) {
//        this.openstate = openstate;
//    }
//
//    public String getExpindname() {
//        return expindname;
//    }
//
//    public void setExpindname(String expindname) {
//        this.expindname = expindname;
//    }
//
//    public String getExpindcate() {
//        return expindcate;
//    }
//
//    public void setExpindcate(String expindcate) {
//        this.expindcate = expindcate;
//    }
//
//    public String getExpjobname() {
//        return expjobname;
//    }
//
//    public void setExpjobname(String expjobname) {
//        this.expjobname = expjobname;
//    }
//
//    public String getExpjobcate() {
//        return expjobcate;
//    }
//
//    public void setExpjobcate(String expjobcate) {
//        this.expjobcate = expjobcate;
//    }
//
//    public String getLanguage() {
//        return language;
//    }
//
//    public void setLanguage(String language) {
//        this.language = language;
//    }
//
//    public String getExpjobtype() {
//        return expjobtype;
//    }
//
//    public void setExpjobtype(String expjobtype) {
//        this.expjobtype = expjobtype;
//    }
//
//    public String getGraduate() {
//        return graduate;
//    }
//
//    public void setGraduate(String graduate) {
//        this.graduate = graduate;
//    }
//
//    public String getLatlng() {
//        return latlng;
//    }
//
//    public void setLatlng(String latlng) {
//        this.latlng = latlng;
//    }
//
//    public String getCollege() {
//        return college;
//    }
//
//    public void setCollege(String college) {
//        this.college = college;
//    }
//
//    public String getMajor() {
//        return major;
//    }
//
//    public void setMajor(String major) {
//        this.major = major;
//    }
//
//    public RangeValue getPercent() {
//        return percent;
//    }
//
//    public void setPercent(RangeValue percent) {
//        this.percent = percent;
//    }
//
//    public String getRecentjob() {
//        return recentjob;
//    }
//
//    public void setRecentjob(String recentjob) {
//        this.recentjob = recentjob;
//    }
//
//    public String getRecentcomp() {
//        return recentcomp;
//    }
//
//    public void setRecentcomp(String recentcomp) {
//        this.recentcomp = recentcomp;
//    }
//
//    public String getAllexp() {
//        return allexp;
//    }
//
//    public void setAllexp(String allexp) {
//        this.allexp = allexp;
//    }
//
//    public String getProskills() {
//        return proskills;
//    }
//
//    public void setProskills(String proskills) {
//        this.proskills = proskills;
//    }
//
//    public String getTraining() {
//        return training;
//    }
//
//    public void setTraining(String training) {
//        this.training = training;
//    }
//
//    public String getCerts() {
//        return certs;
//    }
//
//    public void setCerts(String certs) {
//        this.certs = certs;
//    }
//
//    public String getHasfujian() {
//        return hasfujian;
//    }
//
//    public void setHasfujian(String hasfujian) {
//        this.hasfujian = hasfujian;
//    }
//
//}
