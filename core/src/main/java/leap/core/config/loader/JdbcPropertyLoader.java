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
package leap.core.config.loader;

import leap.core.config.AppPropertyLoader;
import leap.core.config.AppPropertySetter;
import leap.core.validation.annotations.NotEmpty;
import leap.lang.Try;
import leap.lang.jdbc.JDBC;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class JdbcPropertyLoader implements AppPropertyLoader {

    private static final Log log = LogFactory.get(JdbcPropertyLoader.class);

    protected @NotEmpty String driverClassName;
    protected @NotEmpty String jdbcUrl;
    protected @NotEmpty String username;
    protected           String password;
    protected @NotEmpty String sql;

    @Override
    public void loadProperties(AppPropertySetter props) {
        Try.throwUnchecked(() -> {
            log.info("Load properties from db : {}", jdbcUrl);

            Class.forName(driverClassName);

            Statement  stmt = null;
            ResultSet  rs   = null;
            Connection conn = null;
            try {
                log.debug("Obtain db connection...");
                conn = DriverManager.getConnection(jdbcUrl, username, password);

                log.debug("Execute sql ->\n{}\n", sql);

                stmt = conn.createStatement();
                rs   = stmt.executeQuery(sql);

                int count = 0;

                while (rs.next()) {
                    String key   = rs.getString(1);
                    String value = rs.getString(2);
                    props.putProperty(this, key, value);

                    count++;
                }

                log.info("Load {} properties from db!", count);
            } finally {
                JDBC.closeResultSetOnly(rs);
                JDBC.closeStatementOnly(stmt);
                JDBC.closeConnection(conn);
            }
        });
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }
}
