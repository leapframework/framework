package leap.orm.sql;

import leap.orm.OrmTestCase;
import org.junit.Test;

/**
 * Created by KAEL on 2016/5/8.
 */
public class SqlFragmentTest extends OrmTestCase{
    @Test
    public void testSqlFragment(){
        dao.createNamedQuery("sql.fragment.clause.where").list();
    }
}
