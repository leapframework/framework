/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package leap.db.platform;

import leap.core.config.ConfigUtils;
import leap.db.DbDialect;
import leap.db.DbVersion;
import leap.lang.resource.Resource;
import leap.lang.resource.ResourceSet;
import leap.lang.resource.Resources;

import java.util.*;

public abstract class GenericDbDialectBase implements DbDialect {

    //http://en.wikipedia.org/wiki/SQL
    public static final String SQL92   = "SQL-92";
    public static final String SQL99   = "SQL:1999";
    public static final String SQL2003 = "SQL:2003";

    /**
     * max identifier length defined in {@link #SQL92}
     */
    public static final int SQL92_MAX_IDENTIFIER_LENGTH = 128;

    /**
     * the reserved key words (upper case) defined in {@link #SQL92}
     */
    public static final String[] SQL92_RESERVED_WORDS = new String[]{
            "ABSOLUTE", "ACTION", "ADD", "ALL", "ALLOCATE", "ALTER", "AND",
            "ANY", "ARE", "AS", "ASC",
            "ASSERTION", "AT", "AUTHORIZATION", "AVG",
            "BEGIN", "BETWEEN", "BIT", "BIT_LENGTH", "BOTH", "BY",
            "CASCADE", "CASCADED", "CASE", "CAST", "CATALOG", "CHAR", "CHARACTER", "CHAR_LENGTH",
            "CHARACTER_LENGTH", "CHECK", "CLOSE", "COALESCE", "COLLATE", "COLLATION",
            "COLUMN", "COMMIT", "CONNECT", "CONNECTION", "CONSTRAINT",
            "CONSTRAINTS", "CONTINUE",
            "CONVERT", "CORRESPONDING", "COUNT", "CREATE", "CROSS", "CURRENT",
            "CURRENT_DATE", "CURRENT_TIME", "CURRENT_TIMESTAMP", "CURRENT_USER", "CURSOR",
            "DATE", "DAY", "DEALLOCATE", "DEC", "DECIMAL", "DECLARE", "DEFAULT", "DEFERRABLE",
            "DEFERRED", "DELETE", "DESC", "DESCRIBE", "DESCRIPTOR", "DIAGNOSTICS",
            "DISCONNECT", "DISTINCT", "DOMAIN", "DOUBLE", "DROP",
            "ELSE", "END", "END-EXEC", "ESCAPE", "EXCEPT", "EXCEPTION",
            "EXEC", "EXECUTE", "EXISTS",
            "EXTERNAL", "EXTRACT",
            "FALSE", "FETCH", "FIRST", "FLOAT", "FOR", "FOREIGN", "FOUND", "FROM", "FULL",
            "GET", "GLOBAL", "GO", "GOTO", "GRANT", "GROUP",
            "HAVING", "HOUR",
            "IDENTITY", "IMMEDIATE", "IN", "INDICATOR", "INITIALLY", "INNER", "INPUT",
            "INSENSITIVE", "INSERT", "INT", "INTEGER", "INTERSECT", "INTERVAL", "INTO", "IS",
            "ISOLATION",
            "JOIN",
            "KEY",
            "LANGUAGE", "LAST", "LEADING", "LEFT", "LEVEL", "LIKE", "LOCAL", "LOWER",
            "MATCH", "MAX", "MIN", "MINUTE", "MODULE", "MONTH",
            "NAMES", "NATIONAL", "NATURAL", "NCHAR", "NEXT", "NO", "NOT", "NULL",
            "NULLIF", "NUMERIC",
            "OCTET_LENGTH", "OF", "ON", "ONLY", "OPEN", "OPTION", "OR",
            "ORDER", "OUTER",
            "OUTPUT", "OVERLAPS",
            "PAD", "PARTIAL", "POSITION", "PRECISION", "PREPARE", "PRESERVE", "PRIMARY",
            "PRIOR", "PRIVILEGES", "PROCEDURE", "PUBLIC",
            "READ", "REAL", "REFERENCES", "RELATIVE", "RESTRICT", "REVOKE", "RIGHT",
            "ROLLBACK", "ROWS",
            "SCHEMA", "SCROLL", "SECOND", "SECTION", "SELECT", "SESSION", "SESSION_USER", "SET",
            "SIZE", "SMALLINT", "SOME", "SPACE", "SQL", "SQLCODE", "SQLERROR", "SQLSTATE",
            "SUBSTRING", "SUM", "SYSTEM_USER",
            "TABLE", "TEMPORARY", "THEN", "TIME", "TIMESTAMP", "TIMEZONE_HOUR", "TIMEZONE_MINUTE",
            "TO", "TRAILING", "TRANSACTION", "TRANSLATE", "TRANSLATION", "TRIM", "TRUE",
            "UNION", "UNIQUE", "UNKNOWN", "UPDATE", "UPPER", "USAGE", "USER", "USING",
            "VALUE", "VALUES", "VARCHAR", "VARYING", "VIEW",
            "WHEN", "WHENEVER", "WHERE", "WITH", "WORK", "WRITE",
            "YEAR",
            "ZONE"};

    /**
     * the reserved key words (upper case) defined in {@link #SQL99}
     */
    public static final String[] SQL99_RESERVED_WORDS = new String[]{
            "ABSOLUTE", "ACTION", "ADD", "AFTER", "ALL", "ALLOCATE", "ALTER", "AND",
            "ANY", "ARE", "ARRAY", "AS", "ASC", "ASENSITIVE",
            "ASSERTION", "ASYMMETRIC", "AT", "ATOMIC", "AUTHORIZATION",
            "BEFORE", "BEGIN", "BETWEEN", "BINARY", "BIT", "BLOB", "BOOLEAN",
            "BOTH", "BREADTH", "BY",
            "CALL", "CALLED", "CASCADE", "CASCADED", "CASE", "CAST", "CATALOG",
            "CHAR", "CHARACTER", "CHECK", "CLOB", "CLOSE", "COLLATE",
            "COLLATION", "COLUMN", "COMMIT", "CONDITION", "CONNECT",
            "CONNECTION", "CONSTRAINT", "CONSTRAINTS", "CONSTRUCTOR",
            "CONTINUE", "CORRESPONDING", "CREATE", "CROSS", "CUBE", "CURRENT",
            "CURRENT_DATE", "CURRENT_DEFAULT_TRANSFORM_GROUP",
            "CURRENT_PATH", "CURRENT_ROLE", "CURRENT_TIME", "CURRENT_TIMESTAMP",
            "CURRENT_TRANSFORM_GROUP_FOR_TYPE", "CURRENT_USER", "CURSOR", "CYCLE",
            "DATA", "DATE", "DAY", "DEALLOCATE", "DEC", "DECIMAL", "DECLARE", "DEFAULT",
            "DEFERRABLE", "DEFERRED", "DELETE", "DEPTH", "DEREF", "DESC", "DESCRIBE",
            "DESCRIPTOR", "DETERMINISTIC", "DIAGNOSTICS", "DISCONNECT",
            "DISTINCT", "DO", "DOMAIN", "DOUBLE", "DROP", "DYNAMIC",
            "EACH", "ELSE", "ELSEIF", "END", "EQUALS", "ESCAPE", "EXCEPT", "EXCEPTION",
            "EXEC", "EXECUTE", "EXISTS", "EXIT", "EXTERNAL",
            "FALSE", "FETCH", "FILTER", "FIRST", "FLOAT", "FOR", "FOREIGN",
            "FOUND", "FREE", "FROM", "FULL", "FUNCTION",
            "GENERAL", "GET", "GLOBAL", "GO", "GOTO", "GRANT", "GROUP", "GROUPING",
            "HANDLER", "HAVING", "HOLD", "HOUR", "I" +
            "DENTITY", "IF", "IMMEDIATE", "IN", "INDICATOR", "INITIALLY",
            "INNER", "INOUT", "INPUT", "INSENSITIVE", "INSERT", "INT", "INTEGER",
            "INTERSECT", "INTERVAL", "INTO", "IS", "ISOLATION", "ITERATE",
            "JOIN",
            "KEY",
            "LANGUAGE", "LARGE", "LAST", "LATERAL", "LEADING", "LEAVE", "LEFT",
            "LEVEL", "LIKE", "LOCAL", "LOCALTIME", "LOCALTIMESTAMP", "LOCATOR", "LOOP",
            "MAP", "MATCH", "METHOD", "MINUTE", "MODIFIES", "MODULE", "MONTH",
            "NAMES", "NATIONAL", "NATURAL", "NCHAR", "NCLOB", "NEW", "NEXT",
            "NO", "NONE", "NOT", "NULL", "NUMERIC",
            "OBJECT", "OF", "OLD", "ON", "ONLY", "OPEN", "OPTION", "OR", "ORDER",
            "ORDINALITY", "OUT", "OUTER", "OUTPUT", "OVER", "OVERLAPS",
            "PAD", "PARAMETER", "PARTIAL", "PARTITION", "PATH", "PRECISION",
            "PREPARE", "PRESERVE", "PRIMARY", "PRIOR", "PRIVILEGES",
            "PROCEDURE", "PUBLIC",
            "RANGE", "READ", "READS", "REAL", "RECURSIVE", "REF", "REFERENCES",
            "REFERENCING", "RELATIVE", "RELEASE", "REPEAT", "RESIGNAL",
            "RESTRICT", "RESULT", "RETURN", "RETURNS", "REVOKE", "RIGHT",
            "ROLE", "ROLLBACK", "ROLLUP", "ROUTINE", "ROW", "ROWS",
            "SAVEPOINT", "SCHEMA", "SCOPE", "SCROLL", "SEARCH", "SECOND",
            "SECTION", "SELECT", "SENSITIVE", "SESSION", "SESSION_USER",
            "SET", "SETS", "SIGNAL", "SIMILAR", "SIZE", "SMALLINT", "SOME",
            "SPACE", "SPECIFIC", "SPECIFICTYPE", "SQL", "SQLEXCEPTION",
            "SQLSTATE", "SQLWARNING", "START", "STATE", "STATIC", "SYMMETRIC",
            "SYSTEM", "SYSTEM_USER",
            "TABLE", "TEMPORARY", "THEN", "TIME",
            "TIMESTAMP", "TIMEZONE_HOUR", "TIMEZONE_MINUTE", "TO", "TRAILING",
            "TRANSACTION", "TRANSLATION", "TREAT", "TRIGGER", "TRUE",
            "UNDER", "UNDO", "UNION", "UNIQUE", "UNKNOWN", "UNNEST",
            "UNTIL", "UPDATE", "USAGE", "USER", "USING",
            "VALUE", "VALUES", "VARCHAR", "VARYING", "VIEW",
            "WHEN", "WHENEVER", "WHERE", "WHILE", "WINDOW", "WITH", "WITHIN",
            "WITHOUT", "WORK", "WRITE",
            "YEAR",
            "ZONE"};

    /**
     * the reserved key words (upper case) defined in {@link #SQL2003}
     */
    public static final String[] SQL2003_RESERVED_WORDS = new String[]{
            "ADD", "ALL", "ALLOCATE", "ALTER", "AND", "ANY", "ARE", "ARRAY",
            "AS", "ASENSITIVE", "ASYMMETRIC", "AT", "ATOMIC", "AUTHORIZATION",
            "BEGIN", "BETWEEN", "BIGINT", "BINARY", "BLOB", "BOOLEAN", "BOTH", "BY",
            "CALL", "CALLED", "CASCADED", "CASE", "CAST", "CHAR", "CHARACTER",
            "CHECK", "CLOB", "CLOSE", "COLLATE", "COLUMN", "COMMIT", "CONDITION",
            "CONNECT", "CONSTRAINT", "CONTINUE", "CORRESPONDING", "CREATE",
            "CROSS", "CUBE", "CURRENT", "CURRENT_DATE",
            "CURRENT_DEFAULT_TRANSFORM_GROUP", "CURRENT_PATH",
            "CURRENT_ROLE", "CURRENT_TIME", "CURRENT_TIMESTAMP",
            "CURRENT_TRANSFORM_GROUP_FOR_TYPE", "CURRENT_USER",
            "CURSOR", "CYCLE",
            "DATE", "DAY", "DEALLOCATE", "DEC", "DECIMAL", "DECLARE", "DEFAULT",
            "DELETE", "DEREF", "DESCRIBE", "DETERMINISTIC", "DISCONNECT",
            "DISTINCT", "DO", "DOUBLE", "DROP", "DYNAMIC",
            "EACH", "ELEMENT", "ELSE", "ELSEIF", "END", "ESCAPE", "EXCEPT", "EXEC",
            "EXECUTE", "EXISTS", "EXIT", "EXTERNAL",
            "FALSE", "FETCH", "FILTER", "FLOAT", "FOR", "FOREIGN", "FREE", "FROM",
            "FULL", "FUNCTION",
            "GET", "GLOBAL", "GRANT", "GROUP", "GROUPING",
            "HANDLER", "HAVING", "HOLD", "HOUR",
            "IDENTITY", "IF", "IMMEDIATE", "IN", "INDICATOR", "INNER", "INOUT",
            "INPUT", "INSENSITIVE", "INSERT", "INT", "INTEGER", "INTERSECT",
            "INTERVAL", "INTO", "IS", "ITERATE",
            "JOIN",
            "LANGUAGE", "LARGE", "LATERAL", "LEADING", "LEAVE", "LEFT", "LIKE",
            "LOCAL", "LOCALTIME", "LOCALTIMESTAMP", "LOOP",
            "MATCH", "MEMBER", "MERGE", "METHOD", "MINUTE", "MODIFIES", "MODULE",
            "MONTH", "MULTISET",
            "NATIONAL", "NATURAL", "NCHAR", "NCLOB", "NEW", "NO", "NONE", "NOT", "NULL",
            "NUMERIC",
            "OF", "OLD", "ON", "ONLY", "OPEN", "OR", "ORDER", "OUT", "OUTER", "OUTPUT", "OVER",
            "OVERLAPS",
            "PARAMETER", "PARTITION", "PRECISION", "PREPARE", "PRIMARY",
            "PROCEDURE",
            "RANGE", "READS", "REAL", "RECURSIVE", "REF", "REFERENCES", "REFERENCING",
            "RELEASE", "REPEAT", "RESIGNAL", "RESULT", "RETURN", "RETURNS", "REVOKE",
            "RIGHT", "ROLLBACK", "ROLLUP", "ROW", "ROWS",
            "SAVEPOINT", "SCOPE", "SCROLL", "SEARCH", "SECOND", "SELECT", "SENSITIVE",
            "SESSION_USER", "SET", "SIGNAL", "SIMILAR", "SMALLINT", "SOME", "SPECIFIC",
            "SPECIFICTYPE", "SQL", "SQLEXCEPTION", "SQLSTATE", "SQLWARNING", "START",
            "STATIC", "SUBMULTISET", "SYMMETRIC", "SYSTEM", "SYSTEM_USER",
            "TABLE", "TABLESAMPLE", "THEN", "TIME", "TIMESTAMP", "TIMEZONE_HOUR",
            "TIMEZONE_MINUTE", "TO", "TRAILING", "TRANSLATION", "TREAT", "TRIGGER", "TRUE",
            "UNDO", "UNION", "UNIQUE", "UNKNOWN", "UNNEST", "UNTIL", "UPDATE", "USER", "USING",
            "VALUE", "VALUES", "VARCHAR", "VARYING",
            "WHEN", "WHENEVER", "WHERE", "WHILE", "WINDOW", "WITH", "WITHIN", "WITHOUT",
            "YEAR"};

    protected abstract DbVersion getVersion();

    protected Map<String, Object> loadProperties(String name) {
        final Map<String, Object> props = new LinkedHashMap<>();

        load(props, Resources.scan("classpath*:leap/db/dialects/" + name + ".*"));
        load(props, Resources.scan("classpath*:META-INF/db/dialects/" + name + ".*"));
        load(props, Resources.scan("classpath*:db/dialects/" + name + ".*"));

        return extractCurrentDialect(props);
    }

    private void load(Map<String, Object> all, ResourceSet rs) {
        for (Resource resource : rs) {
            if (!ConfigUtils.isJsonOrYaml(resource)) {
                continue;
            }
            Map<String, Object> map = ConfigUtils.decodeMap(resource);
            if (!map.isEmpty()) {
                all.putAll(ConfigUtils.toObjectProperties(map));
            }
        }
    }

    private Map<String, Object> extractCurrentDialect(Map<String, Object> props) {
        if (props.isEmpty()) {
            return props;
        }

        final DbVersion currVer = getVersion();

        final Map<String, Object> currMap  = new LinkedHashMap<>();
        final Map<String, VerMap> versions = new HashMap<>();

        //extract defaults
        props.forEach((key, value) -> {
            final String prefix = ConfigUtils.extractKeyPrefix(key);
            if (null == prefix) {
                return;
            }

            final String name = ConfigUtils.removeKeyPrefix(key, prefix);
            if (prefix.equals("defaults")) {
                currMap.put(name, value);
                return;
            }

            if(prefix.startsWith("v")) {
                DbVersion v = DbVersion.parseUnderscore(prefix.substring(1));
                if(!currVer.ge(v)) {
                    return;
                }
                VerMap verMap = versions.get(v.getDotExpr());
                if(null == verMap) {
                    verMap = new VerMap(v);
                    versions.put(v.getDotExpr(), verMap);
                }
                verMap.put(name, value);
            }
        });

        final List<VerMap> verMaps = new ArrayList<>(versions.values());
        Collections.sort(verMaps);

        verMaps.forEach(m -> {
            currMap.putAll(m);
        });

        return currMap;
    }

    private static class VerMap extends LinkedHashMap<String, Object> implements Comparable<VerMap> {
        private final DbVersion version;

        public VerMap(DbVersion version) {
            this.version = version;
        }

        @Override
        public int compareTo(VerMap o) {
            return DbVersion.SORT_COMPARATOR.compare(version, o.version);
        }
    }
}
