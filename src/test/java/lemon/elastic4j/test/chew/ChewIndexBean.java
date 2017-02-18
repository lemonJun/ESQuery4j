package lemon.elastic4j.test.chew;

import lemon.elastic.query4j.provider.CriteriaEnum;
import lemon.elastic.query4j.provider.SearchAnno;

public class ChewIndexBean {

    @SearchAnno
    private String id;

    @SearchAnno(condition = CriteriaEnum.EQ)
    private String cate;

}
