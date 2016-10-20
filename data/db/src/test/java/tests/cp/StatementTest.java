/*
 *
 *  * Copyright 2016 the original author or authors.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */
package tests.cp;

import leap.lang.jdbc.JDBC;
import org.junit.Test;
import tests.cp.mock.MockConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StatementTest extends PoolTestBase {

    @Test
    public void testCreateAndCloseStatement() throws SQLException {
        try(Connection conn = ds.getConnection()){

            try(Statement st = conn.createStatement()) {
                st.execute("select 1");
            }

            try(PreparedStatement ps = conn.prepareStatement("select 1")) {
                try(ResultSet rs = ps.executeQuery()){

                }
            }

            try(CallableStatement cs = conn.prepareCall("{? call test()}")){
                cs.executeUpdate();
            }
        }
    }

    @Test
    public void testOpenAndCloseManyStatement() throws SQLException {

        try(Connection conn = ds.getConnection()) {
            List<Statement> statements = new ArrayList<>();

            for(int i=0;i<100;i++) {
                statements.add(conn.createStatement());
            }

            for(int i=0;i<100;i++) {
                statements.add(conn.prepareStatement("select 1"));
            }

            for(int i=0;i<100;i++) {
                statements.add(conn.prepareCall("{? call test()}"));
            }

            MockConnection mc = conn.unwrap(MockConnection.class);
            assertEquals(300, mc.getOpeningStatements());

            statements.forEach(JDBC::closeStatementOnly);
            assertEquals(0, mc.getOpeningStatements());
        }

    }

    @Test
    public void testStatementLeak() throws SQLException {

        MockConnection mc;

        try(Connection conn = ds.getConnection()) {
            mc = conn.unwrap(MockConnection.class);
            Statement st = conn.createStatement();

            assertEquals(1, mc.getOpeningStatements());
        }

        assertEquals(0, mc.getOpeningStatements());
    }
}
