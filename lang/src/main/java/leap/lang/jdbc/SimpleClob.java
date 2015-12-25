/*
 * Copyright 2012 the original author or authors.
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
package leap.lang.jdbc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.sql.Clob;
import java.sql.SQLException;

import leap.lang.convert.StringConvertibleTo;
import leap.lang.io.IO;
import leap.lang.io.ReaderInputStream;

public class SimpleClob implements Clob,StringConvertibleTo {
	
	private String  string;
	private Reader  reader;
	private long    length;
	private boolean needsReset = false;
	
	public SimpleClob(String string){
		this.string = string;
		this.reader = new StringReader(string);
		this.length = string.length();
	}
	
	public SimpleClob(Reader reader, long length){
		this.reader = reader;
		this.length = length;
	}

	public long length() throws SQLException {
	    return length;
    }
	
	public InputStream getAsciiStream() throws SQLException {
		resetIfNeeded();
		return new ReaderInputStream(reader);
    }
	
	public Reader getCharacterStream() throws SQLException {
		resetIfNeeded();
		return reader;
    }

	public String getSubString(long pos, int length) throws SQLException {
		if ( string == null ) {
			throw new UnsupportedOperationException( "Clob was not created from string; cannot substring" );
		}
		// semi-naive implementation
		int startIndex = (int)(pos - 1);
		int endIndex = Math.min( startIndex + length, string.length());
		return string.substring(startIndex, endIndex );
    }

	public long position(String searchstr, long start) throws SQLException {
		unsupported();
	    return 0;
    }

	public long position(Clob searchstr, long start) throws SQLException {
		unsupported();
	    return 0;
    }

	public int setString(long pos, String str) throws SQLException {
		unsupported();
	    return 0;
    }

	public int setString(long pos, String str, int offset, int len) throws SQLException {
		unsupported();
	    return 0;
    }

	public OutputStream setAsciiStream(long pos) throws SQLException {
		unsupported();
	    return null;
    }

	public Writer setCharacterStream(long pos) throws SQLException {
		unsupported();
	    return null;
    }

	public void truncate(long len) throws SQLException {
		unsupported();
    }
	
	protected void resetIfNeeded() throws SQLException {
		try {
			if ( needsReset ) {
				reader.reset();
			}
		}
		catch ( IOException ioe ) {
			throw new SQLException("could not reset reader", ioe);
		}
		needsReset = true;
	}
	
	@Override
    public void free() throws SQLException {
	    string = null;
	    IO.close(reader);
    }

	@Override
    public Reader getCharacterStream(long pos, long length) throws SQLException {
		unsupported();
		return null;
	}

	private static void unsupported(){
		throw new UnsupportedOperationException("operation not supported in this clob");
	}

	public String convertToString() {
		return string;
    }
}
