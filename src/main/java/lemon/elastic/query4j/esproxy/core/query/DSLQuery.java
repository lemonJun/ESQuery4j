package lemon.elastic.query4j.esproxy.core.query;

import lemon.elastic.query4j.esproxy.domain.Pageable;
import lemon.elastic.query4j.esproxy.domain.Sort;

/**
 * StringQuery
 *
 * @author Rizwan Idrees
 * @author Mohsin Husen
 */
public class DSLQuery extends AbstractQuery {

    private String source;

    public DSLQuery(String source) {
        this.source = source;
    }

    public DSLQuery(String source, Pageable pageable) {
        this.source = source;
        this.pageable = pageable;
    }

    public DSLQuery(String source, Pageable pageable, Sort sort) {
        this.pageable = pageable;
        this.sort = sort;
        this.source = source;
    }

    public String getSource() {
        return source;
    }
}
