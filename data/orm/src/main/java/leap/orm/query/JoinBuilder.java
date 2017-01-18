package leap.orm.query;

/**
 * Created by kael on 2016/11/17.
 */
public interface JoinBuilder {
    void build(StringBuilder sqlBuilder,JoinContext context);
}
