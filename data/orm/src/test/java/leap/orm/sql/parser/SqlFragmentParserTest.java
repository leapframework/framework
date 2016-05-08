package leap.orm.sql.parser;

import leap.lang.Strings;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.resource.Resource;
import leap.lang.resource.Resources;
import leap.orm.OrmTestCase;
import leap.orm.sql.Sql;
import org.junit.Test;

import java.util.List;

/**
 * Created by KAEL on 2016/5/8.
 */
public class SqlFragmentParserTest extends SqlParserTestCase {
    Log log = LogFactory.get(SqlFragmentParserTest.class);
    @Test
    public void testFratmentParse(){
        for(Resource res : Resources.scan("classpath:/test/sqls/fragment/**/*.sql")){
            String text = res.getContent();
            log.info("Split sqls in '{}'",res.getFilename());
            List<String> sqls = split(text);
            log.info("Test {} sql statement(s) in '{}'",sqls.size(),res.getFilename());
            for(int i=0;i<sqls.size();i++){
                String sql = Strings.trim(sqls.get(i));
                log.debug("  Sql {} \n {}",(i+1),sql);
                Sql s = assertParse(sql);
                System.out.println(s);
            }
        }
    }
}
