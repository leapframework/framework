/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package leap.orm.sql.parser;

import leap.junit.contexual.Contextual;
import leap.lang.Strings;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.resource.Resource;
import leap.lang.resource.Resources;
import leap.orm.sql.Sql;
import org.junit.Test;

import java.util.List;

public class SqlParserDynaTest extends SqlParserTestCase {

    private static final Log log = LogFactory.get(SqlParserDynaTest.class);

    public void setup(){
        level = Sql.ParseLevel.DYNA;
    }

    @Test
    public void testSimpleUpdate() {
        assertEquals("update owner set address = ? where 1=1",
                parse("update owner set address = ? where 1=1;"));
    }

    @Test
    @Contextual
    public void testBug(){
        String text = Strings.trim(Resources.getContent("classpath:/test/sqls/parse/bug.sql"));
        if(!Strings.isEmpty(text)){
            List<String> sqls = split(text);

            log.info("Test {} sql statement(s)", sqls.size());

            for (int i = 0; i < sqls.size(); i++) {
                String sql = Strings.trim(sqls.get(i));
                log.debug("  Sql {} \n {}", (i + 1), sql);
                Sql result = assertParse(sql);
                log.info(result.toString());
            }
        }
    }

    @Test
    @Contextual
    public void testFromResources() throws Exception{
        for(Resource res : Resources.scan("classpath:/test/sqls/parse/**/*.sql")){
            String text = res.getContent();

            log.info("Split sqls in '{}'",res.getFilename());

            List<String> sqls = split(text);

            log.info("Test {} sql statement(s) in '{}'",sqls.size(),res.getFilename());

            for(int i=0;i<sqls.size();i++){
                String sql = Strings.trim(sqls.get(i));
                log.debug("  Sql {} \n {}",(i+1),sql);
                assertParse(sql);
            }
        }
    }

    @Test
    @Contextual
    public void testDynamicFromResources() throws Exception{
        for(Resource res : Resources.scan("classpath:/test/sqls/dynamic/**/*.sql")){
            String text = res.getContent();

            log.info("Split sqls in '{}'",res.getFilename());

            List<String> sqls = split(text);

            log.info("Test {} sql statement(s) in '{}'",sqls.size(),res.getFilename());

            for(int i=0;i<sqls.size();i++){
                String sql = Strings.trim(sqls.get(i));
                log.debug("  Sql {} \n {}",(i+1),sql);
                assertParse(sql);
            }
        }
    }
}
