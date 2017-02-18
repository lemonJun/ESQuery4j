package lemon.elastic4j.test.chew;

import lemon.elastic.query4j.provider.CriteriaEnum;
import lemon.elastic.query4j.provider.SearchAnno;

public class SplitCrit {

    @SearchAnno
    private String id;

    @SearchAnno(condition = CriteriaEnum.EQ, routing = true)
    private String cate;

    public String getCate() {
        return cate;
    }

    public void setCate(String cate) {
        this.cate = cate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
