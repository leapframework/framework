package leap.orm.sql;

import java.util.List;

/**
 * Created by KAEL on 2016/5/8.
 */
public class DefaultSqlFragment implements SqlFragment {
    protected Object source;
    protected final SqlClause[] clauses;

    public DefaultSqlFragment(Object source, SqlClause[] clauses) {
        this.source = source;
        this.clauses = clauses;
    }

    @Override
    public Object getSource() {
        return this.source;
    }

    @Override
    public SqlClause[] sqlClause() {
        return clauses;
    }
}
