/*
 *  Copyright 2019 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package leap.db;

import leap.core.security.annotation.Ignore;
import leap.db.model.DbColumnBuilder;
import leap.db.model.DbTableBuilder;
import leap.junit.contexual.Contextual;
import leap.lang.Randoms;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.time.StopWatch;
import org.junit.Test;

import java.sql.PreparedStatement;
import java.util.UUID;

public class DbBatchTest extends DbTestCase {

    private static final Log log = LogFactory.get(DbBatchTest.class);

    @Ignore
    @Test
    @Contextual("mysql")
    public void testMySQLBatchInsert() {
        if(!db.checkTableExists("t_batch")) {
            DbTableBuilder table = new DbTableBuilder("t_batch");
            table.addColumn(DbColumnBuilder.guid("id").primaryKey());
            for(int i=1;i<=10;i++) {
                table.addColumn(DbColumnBuilder.varchar("c" + i, 100));
            }
            db.cmdCreateTable(table.build()).execute();
        }
        final int count = 1000;

        //add ?rewriteBatchedStatements=true at jdbc url to improves performance

        Object[][] rows = new Object[count][];
        for(int i=0;i<count;i++) {
            Object[] row = new Object[11];
            row[0] = UUID.randomUUID().toString();
            for(int j=1;j<=10;j++) {
                row[j] = Randoms.nextString(5, 50);
            }
            rows[i] = row;
        }

        final String sql = "insert into t_batch(id, c1, c2, c3, c4, c5, c6, c7, c8, c9, c10) values(?,?,?,?,?,?,?,?,?,?,?)";
        StopWatch sw = StopWatch.startNew();
        db.executeBatchUpdate(sql, rows);
        log.info("Batch insert {} rows use {}ms", count, sw.getElapsedMilliseconds());
        db.executeUpdate("delete from t_batch");

        sw = StopWatch.startNew();
        db.execute(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql);
            for(Object[] row : rows) {
                for(int i=0;i<row.length;i++) {
                    ps.setString(i+1, (String)row[i]);
                }
                ps.addBatch();
            }
            ps.executeBatch();
        });
        log.info("Batch insert {} rows use {}ms", count, sw.getElapsedMilliseconds());
    }

}
