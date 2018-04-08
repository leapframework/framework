package leap.core.jdbc;

public class SqlExecutionContext {

    private static ThreadLocal<String> sql = new ThreadLocal<>();

    private static ThreadLocal<SqlExcutionType> type = new ThreadLocal<>();

    public static SqlExcutionType getType() {
        SqlExcutionType t = type.get();

        if(null == t) t = SqlExcutionType.unknown;

        return t;
    }

    public static void setType(SqlExcutionType t) {
        type.set(t);
    }

    public static String getSql() {
        return sql.get();
    }

    public static void setSql(String s) {
        sql.set(s);
    }

    public static void setup(SqlExcutionType type, String sql) {
        setType(type);
        setSql(sql);
    }

    public static void clean() {
        sql.remove();
        type.remove();
    }
}
