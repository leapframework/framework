/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package leap.orm.sql;

import leap.lang.Buildable;

public class SqlInfo {

    protected final Object      source;
    protected final String      desc;
    protected final String      dbType;
    protected final SqlLanguage lang;
    protected final String      content;
    protected final String      dataSourceName;

    public SqlInfo(Object source, String desc, String dbType, SqlLanguage lang, String content, String dataSourceName) {
        this.source = source;
        this.desc = desc;
        this.dbType = dbType;
        this.lang = lang;
        this.content = content;
        this.dataSourceName = dataSourceName;
    }

    public Object getSource() {
        return source;
    }

    public String getDesc() {
        return desc;
    }

    public String getDbType() {
        return dbType;
    }

    public SqlLanguage getLang() {
        return lang;
    }

    public String getContent() {
        return content;
    }

    public String getDataSourceName() {
        return dataSourceName;
    }

    public static class Builder implements Buildable<SqlInfo> {

        protected Object      source;
        protected String      desc;
        protected String      dbType;
        protected SqlLanguage lang;
        protected String      content;
        protected String      dataSourceName;

        public Builder() {

        }

        public Builder(Object source, String desc, String dbType, SqlLanguage lang, String content, String dataSourceName) {
            this.source = source;
            this.desc = desc;
            this.dbType = dbType;
            this.lang = lang;
            this.content = content;
            this.dataSourceName = dataSourceName;
        }

        public Object getSource() {
            return source;
        }

        public Builder setSource(Object source) {
            this.source = source;
            return this;
        }

        public String getDesc() {
            return desc;
        }

        public Builder setDesc(String desc) {
            this.desc = desc;
            return this;
        }

        public String getDbType() {
            return dbType;
        }

        public Builder setDbType(String dbType) {
            this.dbType = dbType;
            return this;
        }

        public SqlLanguage getLang() {
            return lang;
        }

        public Builder setLang(SqlLanguage lang) {
            this.lang = lang;
            return this;
        }

        public String getContent() {
            return content;
        }

        public Builder setContent(String content) {
            this.content = content;
            return this;
        }

        public String getDataSourceName() {
            return dataSourceName;
        }

        public Builder setDataSourceName(String dataSourceName) {
            this.dataSourceName = dataSourceName;
            return this;
        }

        @Override
        public SqlInfo build() {
            return new SqlInfo(source, desc, dbType, lang, content, dataSourceName);
        }
    }
}
