package lemon.elastic.query4j.esproxy.core.query;

import com.google.common.base.Preconditions;

import lemon.elastic.query4j.esproxy.domain.Pageable;

/**
 * es中主要有三种主要的查询方式：
 * criteriaquery   统一的spring模式封装 
 * searchquery     原生客户端的支持
 * stringquery     原生restful支持
 *
 * 由此看来 因为CRITERIA太过于兼容SPRIG，因此并不是一个很好的方式，且IS CONTAINS等方法虽然易于理解
 * 但是并不利于对LUCENE的理解   更无法知道语法之间的对应关系；
 * @author WangYazhou
 * @date  2016年6月24日 下午2:22:54
 * @see
 */
public class CriteriaQuery extends AbstractQuery {

    private Criteria criteria;

    private CriteriaQuery() {
    }

    public CriteriaQuery(Criteria criteria) {
        this(criteria, null);
    }

    public CriteriaQuery(Criteria criteria, Pageable pageable) {
        this.criteria = criteria;
        this.pageable = pageable;
        if (pageable != null) {
            this.addSort(pageable.getSort());
        }
    }

    public static final Query fromQuery(CriteriaQuery source) {
        return fromQuery(source, new CriteriaQuery());
    }

    public static <T extends CriteriaQuery> T fromQuery(CriteriaQuery source, T destination) {
        if (source == null || destination == null) {
            return null;
        }

        if (source.getCriteria() != null) {
            destination.addCriteria(source.getCriteria());
        }

        if (source.getSort() != null) {
            destination.addSort(source.getSort());
        }

        return destination;
    }
    
    @SuppressWarnings("unchecked")
    public final <T extends CriteriaQuery> T addCriteria(Criteria criteria) {
        Preconditions.checkNotNull(criteria, "Cannot add null criteria.");
        if (this.criteria == null) {
            this.criteria = criteria;
        } else {
            this.criteria.and(criteria);
        }
        return (T) this;
    }

    public Criteria getCriteria() {
        return this.criteria;
    }
}
