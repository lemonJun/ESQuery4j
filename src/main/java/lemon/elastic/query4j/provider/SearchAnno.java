package lemon.elastic.query4j.provider;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 可用于业务的索引查询是可以枚举的 在javabean对象上加上些注解，便可以自生成查询的query
 *
 *
 * @author wangyazhou
 * @version 1.0
 * @date 2016年2月14日 下午3:59:12
 * @see
 * @since
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@Documented
public @interface SearchAnno {

    public CriteriaEnum condition() default CriteriaEnum.EQ;

    public String setFuncName() default "setField";

    public String getFuncName() default "getField";

    public boolean routing() default false;

}
