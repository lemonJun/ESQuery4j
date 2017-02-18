package lemon.elastic.query4j.esproxy.core.query;

import lemon.elastic.query4j.esproxy.domain.Pageable;
import lemon.elastic.query4j.esproxy.domain.Sort;

/**
 * 原生restful查询支持
 * 如果简历查询的话   用这个还是不错的 
 * 迁移性好
 *
 * @author WangYazhou
 * @date  2016年6月24日 下午2:24:06
 * @see
 */
public class StringQuery extends AbstractQuery {

    private String source;

    public StringQuery(String source) {
        this.source = source;
    }

    public StringQuery(String source, Pageable pageable) {
        this.source = source;
        this.pageable = pageable;
    }

    public StringQuery(String source, Pageable pageable, Sort sort) {
        this.pageable = pageable;
        this.sort = sort;
        this.source = source;
    }

    public String getSource() {
        return source;
    }
}
