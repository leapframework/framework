package leap.orm.dao;

import leap.orm.OrmTestCase;
import leap.orm.tested.TestedEntity;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Created by KAEL on 2016/5/5.
 */
public class JDBCExecutorTest extends OrmTestCase {
    @Test
    public void executorTest(){
        String sql = "SELECT 1 FROM "+metadata.getEntityMapping(TestedEntity.class).getTableName();
        dao.getJdbcExecutor().execute((conn)->{
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            rs.next();
            int i = rs.getInt(1);
            assertEquals(1,i);
            rs.close();
            stmt.close();
        });
    }
}
