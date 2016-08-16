package leap.orm.sql;

public interface SqlValue {

    SqlValue NULL = () -> null;

    static SqlValue of(Object value) {
        return null == value ? NULL : () -> value;
    }

    static SqlValue generated(Object value) {
        if(null == value) {
            return NULL;
        }

        return new SqlValue() {
            @Override
            public Object get() {
                return value;
            }

            @Override
            public boolean isGenerated() {
                return true;
            }
        };
    }

    /**
     * Returns the value.
     */
    Object get();

    /**
     * Returns true if the value is not null.
     */
    default boolean isPresent() {
        return null != get();
    }

    /**
     * Returns true if the value is a generated value.
     */
    default boolean isGenerated() {
        return false;
    }

}