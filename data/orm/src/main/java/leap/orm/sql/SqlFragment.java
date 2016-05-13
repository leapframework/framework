package leap.orm.sql;

import leap.lang.Sourced;

import java.util.List;

/**
 * Created by KAEL on 2016/5/8.
 */
public interface SqlFragment extends Sourced {
    public SqlClause[] sqlClause();
}
