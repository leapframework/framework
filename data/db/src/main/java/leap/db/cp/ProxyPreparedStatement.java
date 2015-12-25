/*
 * Copyright 2015 the original author or authors.
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
package leap.db.cp;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

public class ProxyPreparedStatement extends ProxyStatement implements PreparedStatement {
	
	private final PreparedStatement ps;

	public ProxyPreparedStatement(PooledConnection conn, PreparedStatement ps, String sql) {
		super(conn, ps);
		this.ps 			  = ps;
		this.lastExecutingSql = sql;
	}

	@Override
    public ResultSet executeQuery() throws SQLException {
		try{
			beginExecute(null);
			return createResultSetProxy(ps.executeQuery());
		}finally{
			endExecute();
		}
    }

	@Override
    public int executeUpdate() throws SQLException {
		try{
			beginExecute(null);
			return ps.executeUpdate();
		}finally{
			endExecute();
		}
    }
	
	@Override
    public boolean execute() throws SQLException {
		try{
			beginExecute(null);
			return ps.execute();
		}finally{
			endExecute();
		}
    }

	@Override
    public void addBatch() throws SQLException {
		ps.addBatch();
    }

	@Override
    public void setNull(int parameterIndex, int sqlType) throws SQLException {
		ps.setNull(parameterIndex, sqlType);
    }

	@Override
    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
		ps.setBoolean(parameterIndex, x);
    }

	@Override
    public void setByte(int parameterIndex, byte x) throws SQLException {
		ps.setByte(parameterIndex, x);
    }

	@Override
    public void setShort(int parameterIndex, short x) throws SQLException {
		ps.setShort(parameterIndex, x);
    }

	@Override
    public void setInt(int parameterIndex, int x) throws SQLException {
		ps.setInt(parameterIndex, x);
    }

	@Override
    public void setLong(int parameterIndex, long x) throws SQLException {
		ps.setLong(parameterIndex, x);
    }

	@Override
    public void setFloat(int parameterIndex, float x) throws SQLException {
		ps.setFloat(parameterIndex, x);
    }

	@Override
    public void setDouble(int parameterIndex, double x) throws SQLException {
		ps.setDouble(parameterIndex, x);
    }

	@Override
    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
		ps.setBigDecimal(parameterIndex, x);
    }

	@Override
    public void setString(int parameterIndex, String x) throws SQLException {
		ps.setString(parameterIndex, x);
    }

	@Override
    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
		ps.setBytes(parameterIndex, x);
    }

	@Override
    public void setDate(int parameterIndex, Date x) throws SQLException {
		ps.setDate(parameterIndex, x);
    }

	@Override
    public void setTime(int parameterIndex, Time x) throws SQLException {
		ps.setTime(parameterIndex, x);
    }

	@Override
    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
		ps.setTimestamp(parameterIndex, x);
    }

	@Override
    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
		ps.setAsciiStream(parameterIndex, x, length);
    }

	@Override
	@Deprecated
    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
		ps.setUnicodeStream(parameterIndex, x, length);
	}

	@Override
    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
	    ps.setBinaryStream(parameterIndex, x, length);
    }

	@Override
    public void clearParameters() throws SQLException {
		ps.clearParameters();
    }

	@Override
    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
		ps.setObject(parameterIndex, x, targetSqlType);
    }

	@Override
    public void setObject(int parameterIndex, Object x) throws SQLException {
		ps.setObject(parameterIndex, x);
    }

	@Override
    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
		ps.setCharacterStream(parameterIndex, reader, length);
    }

	@Override
    public void setRef(int parameterIndex, Ref x) throws SQLException {
		ps.setRef(parameterIndex, x);
    }

	@Override
    public void setBlob(int parameterIndex, Blob x) throws SQLException {
		ps.setBlob(parameterIndex, x);
    }

	@Override
    public void setClob(int parameterIndex, Clob x) throws SQLException {
		ps.setClob(parameterIndex, x);
    }

	@Override
    public void setArray(int parameterIndex, Array x) throws SQLException {
		ps.setArray(parameterIndex, x);
    }

	@Override
    public ResultSetMetaData getMetaData() throws SQLException {
	    return ps.getMetaData();
    }

	@Override
    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
		ps.setDate(parameterIndex, x, cal);
    }

	@Override
    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
		ps.setTime(parameterIndex, x, cal);
    }

	@Override
    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
		ps.setTimestamp(parameterIndex, x, cal);
    }

	@Override
    public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
		ps.setNull(parameterIndex, sqlType, typeName);
    }

	@Override
    public void setURL(int parameterIndex, URL x) throws SQLException {
		ps.setURL(parameterIndex, x);
    }

	@Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
	    return ps.getParameterMetaData();
    }

	@Override
    public void setRowId(int parameterIndex, RowId x) throws SQLException {
		ps.setRowId(parameterIndex, x);
    }

	@Override
    public void setNString(int parameterIndex, String value) throws SQLException {
		ps.setNString(parameterIndex, value);
    }

	@Override
    public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
		ps.setNCharacterStream(parameterIndex, value, length);
    }

	@Override
    public void setNClob(int parameterIndex, NClob value) throws SQLException {
		ps.setNClob(parameterIndex, value);
    }

	@Override
    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
		ps.setClob(parameterIndex, reader, length);
    }

	@Override
    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
		ps.setBlob(parameterIndex, inputStream, length);
    }

	@Override
    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
		ps.setNClob(parameterIndex, reader, length);
    }

	@Override
    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
		ps.setSQLXML(parameterIndex, xmlObject);
    }

	@Override
    public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {
		ps.setObject(parameterIndex, x, targetSqlType, scaleOrLength);
    }

	@Override
    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
		ps.setAsciiStream(parameterIndex, x, length);
    }

	@Override
    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
		ps.setBinaryStream(parameterIndex, x, length);
    }

	@Override
    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
		ps.setCharacterStream(parameterIndex, reader, length);
    }

	@Override
    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
		ps.setAsciiStream(parameterIndex, x);
    }

	@Override
    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
		ps.setBinaryStream(parameterIndex, x);
    }

	@Override
    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
		ps.setCharacterStream(parameterIndex, reader);
    }

	@Override
    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
		ps.setNCharacterStream(parameterIndex, value);
    }

	@Override
    public void setClob(int parameterIndex, Reader reader) throws SQLException {
		ps.setClob(parameterIndex, reader);
    }

	@Override
    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
		ps.setBlob(parameterIndex, inputStream);
    }

	@Override
    public void setNClob(int parameterIndex, Reader reader) throws SQLException {
		ps.setNClob(parameterIndex, reader);
    }
}