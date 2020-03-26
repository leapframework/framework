package leap.db.platform.mysql;

import java.util.Arrays;

public class MySql8Dialect extends MySql5Dialect {

    /**
     * the reserved sql keywords not defined in {@link leap.db.platform.mysql.MySql5Dialect#SQL92_RESERVED_WORDS}. <p/>
     *
     * <p>
     * see：https://dev.mysql.com/doc/refman/8.0/en/keywords.html.
     */
    private static final String[] NONMYSQL5_RESERVED_WORDS = new String[] {
            "ACTIVE", "ADMIN", "ARRAY", "BUCKETS", "CLONE", "COMPONENT", "CUME_DIST", "DEFINITION", "DENSE_RANK",
            "DESCRIPTION", "EMPTY", "ENFORCED", "EXCLUDE", "FAILED_LOGIN_ATTEMPTS", "FIRST_VALUE", "FOLLOWING",
            "GEOMCOLLECTION", "GET_MASTER_PUBLIC_KEY", "GROUPING", "GROUPS", "HISTOGRAM", "HISTORY", "INACTIVE",
            "JSON_TABLE", "JSON_VALUE", "LAG", "LAST_VALUE", "LATERAL", "LEAD", "LOCKED", "MASTER_COMPRESSION_ALGORITHMS",
            "MASTER_PUBLIC_KEY_PATH", "MASTER_TLS_CIPHERSUITES", "MASTER_ZSTD_COMPRESSION_LEVEL", "MEMBER",
            "NESTED", "NETWORK_NAMESPACE", "NOWAIT", "NTH_VALUE", "NTILE", "NULLS", "OF", "OFF", "OJ", "OLD",
            "OPTIONAL", "ORDINALITY", "ORGANIZATION", "OTHERS", "OVER", "PASSWORD_LOCK_TIME", "PATH", "PERCENT_RANK",
            "PERSIST", "PERSIST_ONLY", "PRECEDING", "PRIVILEGE_CHECKS_USER", "PROCESS", "RANDOM", "RANK", "RECURSIVE",
            "REFERENCE", "REQUIRE_ROW_FORMATg", "RESOURCE", "RESPECT", "RESTART", "RETAIN", "RETURNING", "REUSE",
            "ROLE", "ROW_NUMBER", "SECONDARY", "SECONDARY_ENGINE", "SECONDARY_LOAD", "SECONDARY_UNLOAD", "SKIP", "SRID",
            "STREAM", "SYSTEM", "THREAD_PRIORITY", "TIES", "UNBOUNDED", "VCPU", "VISIBLE", "WINDOW"
    };

    @Override
    protected void registerSQLKeyWords() {
        super.registerSQLKeyWords();
        this.sqlKeyWords.addAll(Arrays.asList(NONMYSQL5_RESERVED_WORDS));
    }

}
