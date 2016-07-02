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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.SQLException;

import leap.lang.exception.NestedIOException;
import leap.lang.io.IO;

public class SimpleBlob implements Blob {

	private InputStream stream;
	private long 	    length;
	private boolean     needsReset = false;
	
	private byte[] bytes;

	public SimpleBlob(byte[] bytes) {
		this.bytes  = bytes;
		this.stream = new ByteArrayInputStream(bytes);
		this.length = bytes.length;
	}

	public SimpleBlob(InputStream stream, long length) {
		this.stream = stream;
		this.length = length;
	}
	
	public long length() throws SQLException {
	    return length;
    }

	public byte[] getBytes(long pos, int length) throws SQLException {
		if(null == bytes){
			try {
	            bytes = IO.readByteArray(stream);
            } catch (NestedIOException e) {
            	throw new SQLException("Could not get bytes from stream");
            }
		}
		
		byte[] data = new byte[length];
		
		System.arraycopy(bytes,(int)pos-1,(Object)data, 0, length);
		
		return data;
    }

	public InputStream getBinaryStream() throws SQLException {
		try {
			if (needsReset) {
				stream.reset();
				bytes = null;
			}
		}
		catch ( IOException ioe) {
			throw new SQLException("Could not reset reader");
		}
		needsReset = true;
		return stream;
    }

	public long position(byte[] pattern, long start) throws SQLException {
		unsupported();
	    return 0;
    }

	public long position(Blob pattern, long start) throws SQLException {
		unsupported();
	    return 0;
    }

	public int setBytes(long pos, byte[] bytes) throws SQLException {
		unsupported();
	    return 0;
    }

	public int setBytes(long pos, byte[] bytes, int offset, int len) throws SQLException {
		unsupported();
	    return 0;
    }

	public OutputStream setBinaryStream(long pos) throws SQLException {
		unsupported();
	    return null;
    }

	public void truncate(long len) throws SQLException {
		unsupported();
    }
	
	@Override
    public void free() throws SQLException {
		bytes = null;
		IO.close(stream);
    }

	@Override
    public InputStream getBinaryStream(long pos, long length) throws SQLException {
		unsupported();
		return null;
    }

	private static void unsupported(){
		throw new UnsupportedOperationException("Operation not supported in this blob");
	}	
}
