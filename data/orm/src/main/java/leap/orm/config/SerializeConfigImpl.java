package leap.orm.config;

import leap.lang.jdbc.JdbcType;
import leap.orm.OrmConfig;

public class SerializeConfigImpl implements OrmConfig.SerializeConfig {

    private JdbcType defaultColumnType;
    private Integer  defaultColumnLength;

    @Override
    public JdbcType getDefaultColumnType() {
        return defaultColumnType;
    }

    public void setDefaultColumnType(JdbcType defaultColumnType) {
        this.defaultColumnType = defaultColumnType;
    }

    @Override
    public Integer getDefaultColumnLength() {
        return defaultColumnLength;
    }

    public void setDefaultColumnLength(Integer defaultColumnLength) {
        this.defaultColumnLength = defaultColumnLength;
    }

}