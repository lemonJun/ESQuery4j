package lemon.elastic4j.test.chew;

import java.util.Date;

import lemon.elastic.query4j.esproxy.core.query.IndexQuery;

public class ElasticResumeBuilder {

    private ElasticResume result;

    public ElasticResumeBuilder() {
        result = new ElasticResume();
    }

    public ElasticResumeBuilder id(long id) {
        result.set_id(id);
        return this;
    }

    public ElasticResumeBuilder uid(long uid) {
        result.setUid(uid);
        return this;
    }

    public ElasticResumeBuilder fcate(String fcate) {
        result.setFcate(fcate);
        return this;
    }

    public ElasticResumeBuilder farea(String farea) {
        result.setFarea(farea);
        return this;
    }

    public ElasticResumeBuilder age(int age) {
        result.setAge(age);
        return this;
    }

    public ElasticResumeBuilder exp(int experience) {
        result.setExp(experience);
        return this;
    }

    public ElasticResumeBuilder edu(int edu) {
        result.setEdu(edu);
        return this;
    }

    public ElasticResumeBuilder sal(int salary) {
        result.setSalary(salary);
        return this;
    }

    public ElasticResumeBuilder date(Date date) {
        result.setDate(date);
        return this;
    }

    public ElasticResumeBuilder location(String location) {
        result.setLocation(location);
        return this;
    }

    public ElasticResumeBuilder complete(int complete) {
        result.setComplete(complete);
        return this;
    }

    public ElasticResumeBuilder gender(int gender) {
        result.setGender(gender);
        return this;
    }

    public ElasticResumeBuilder title(String title) {
        result.setTitle(title);
        return this;
    }

    public ElasticResume build() {
        return result;
    }

    public IndexQuery buildIndex() {
        IndexQuery indexQuery = new IndexQuery();
        indexQuery.setId(String.valueOf(result.get_id()));
        indexQuery.setObject(result);
        return indexQuery;
    }
}
