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
package leap.lang.io;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

import leap.lang.Charsets;
import leap.lang.Exceptions;
import leap.lang.annotation.Nullable;
import leap.lang.exception.NestedIOException;

/**
 * io utils
 */
//Most codes copied from apache commons io under Apache License 2.0.
public class IO {
	
	private static final int EOF 				 = -1;
	private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
	
	public static String tmpdir() {
		String path = System.getProperty("java.io.tmpdir");
		if(!path.endsWith(File.separator)) {
			return path + File.separator;
		}
		return path;
	}
	
	/**
	 * Creates a {@link Reader} for reading content of the file use the default charset {@link Charsets#defaultCharset()}.
	 * 
	 * <p>
	 * Note : 
	 * <strong>
	 * You must close the retured {@link Reader}.
	 * </strong>
	 * 
	 * @throws NestedIOException if an I/O error occurs.
	 */
	public static Reader createReader(File file) throws NestedIOException{
		return createReader(file,Charsets.defaultCharset());
	}
	
	/**
	 * Creates a {@link Reader} fo reading content of the file use the given charset.
	 * 
	 * <p>
	 * Note : 
	 * <strong>
	 * You must close the retured {@link Reader}.
	 * </strong>
	 * 
	 * @throws NestedIOException if an I/O error occurs.
	 */
	public static Reader createReader(File file,Charset charset) throws NestedIOException {
		try {
	        return new InputStreamReader(new FileInputStream(file),charset);
        } catch (IOException e) {
        	throw Exceptions.wrap(e);
        }
	}
	
	/**
	 * Creates a {@link Writer} for writing content to the file use default charset {@link Charsets#defaultCharset()}
	 * 
	 * <p>
	 * Note : 
	 * <strong>
	 * You must close the retured {@link Writer}.
	 * </strong>
	 * 
	 * @throws NestedIOException if an I/O error occurs.
	 */
	public static Writer createWriter(File file) throws NestedIOException {
		return createWriter(file,Charsets.defaultCharset());
	}
	
	/**
	 * Creates a {@link Writer} for writing content to the the file use the given charset.
	 * 
	 * <p>
	 * Note : 
	 * <strong>
	 * You must close the retured {@link Writer}.
	 * </strong>
	 * 
	 * @throws NestedIOException if an I/O error occurs.
	 */
	public static Writer createWriter(File file,Charset charset) throws NestedIOException {
		try {
	        return new OutputStreamWriter(new FileOutputStream(file), charset);
        } catch (IOException e) {
        	throw Exceptions.wrap(e);
        }
	}
	
	public static void close(Closeable closeable) {
		try {
			if (closeable != null) {
				closeable.close();
			}
		} catch (Throwable e) {
			// ignore
		}
	}
	
	// read toByteArray
	//-----------------------------------------------------------------------
	/**
	 * Get the contents of an <code>InputStream</code> as a <code>byte[]</code>.
	 * <p>
	 * This method buffers the input internally, so there is no need to use a <code>BufferedInputStream</code>.
	 * 
	 * @param input the <code>InputStream</code> to read from
	 * @return the requested byte array
	 * @throws NullPointerException if the input is null
	 * @throws NestedIOException if an I/O error occurs
	 */
	public static byte[] readByteArray(InputStream input) throws NestedIOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		copy(input, output);
		return output.toByteArray();
	}
	
	public static byte[] readByteArray(File file) {
		FileInputStream in = null;
		try{
			in = new FileInputStream(file);
			return readByteArray(in);
		}catch(IOException e){
			throw Exceptions.wrap(e);
		}finally{
			close(in);
		}
	}

	public static byte[] readByteArray(Reader reader) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		OutputStreamWriter writer = new OutputStreamWriter(out);
		try{
			copy(reader,writer);
			writer.flush();
			return out.toByteArray();
		}catch(IOException e){
			throw Exceptions.wrap(e);
		}finally{
			close(writer);
		}
	}
	
	/**
	 * Get contents of an <code>InputStream</code> as a <code>byte[]</code>. Use this method instead of
	 * <code>toByteArray(InputStream)</code> when <code>InputStream</code> size is known. <b>NOTE:</b> the method checks
	 * that the length can safely be cast to an int without truncation before using
	 * {@link IO#readByteArray(java.io.InputStream, int)} to read into the byte array. (Arrays can have no more than
	 * Integer.MAX_VALUE entries anyway)
	 * 
	 * @param input the <code>InputStream</code> to read from
	 * @param size the size of <code>InputStream</code>
	 * @return the requested byte array
	 * @throws NestedIOException if an I/O error occurs or <code>InputStream</code> size differ from parameter size
	 * @throws IllegalArgumentException if size is less than zero or size is greater than Integer.MAX_VALUE
	 * @see IO#readByteArray(java.io.InputStream, int)
	 * @since 2.1
	 */
	public static byte[] readByteArray(InputStream input, long size) throws NestedIOException {

		if (size > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Size cannot be greater than Integer max value: " + size);
		}

		return readByteArray(input, (int) size);
	}

	/**
	 * Get the contents of an <code>InputStream</code> as a <code>byte[]</code>. Use this method instead of
	 * <code>toByteArray(InputStream)</code> when <code>InputStream</code> size is known
	 * 
	 * @param input the <code>InputStream</code> to read from
	 * @param size the size of <code>InputStream</code>
	 * @return the requested byte array
	 * @throws NestedIOException if an I/O error occurs or <code>InputStream</code> size differ from parameter size
	 * @throws IllegalArgumentException if size is less than zero
	 */
	public static byte[] readByteArray(InputStream input, int size) throws NestedIOException {

		if (size < 0) {
			throw new IllegalArgumentException("Size must be equal or greater than zero: " + size);
		}

		if (size == 0) {
			return new byte[0];
		}

		byte[] data = new byte[size];
		int offset = 0;
		int readed;

		try {
	        while (offset < size && (readed = input.read(data, offset, size - offset)) != EOF) {
	        	offset += readed;
	        }

	        if (offset != size) {
	        	throw new IOException("Unexpected readed size. current: " + offset + ", excepted: " + size);
	        }
        } catch (IOException e) {
        	throw Exceptions.wrap(e);
        }

		return data;
	}
	
	/**
	 * Get the contents of an {@link File} as a String using the specified character encoding.
	 */
	public static String readString(File file,Charset encoding) throws NestedIOException {
		FileInputStream fis = null;
		try{
			fis = new FileInputStream(file);
			return readString(fis, encoding);
		}catch(IOException e){
			throw Exceptions.wrap(e);
		}finally{
			close(fis);
		}
	}

	/**
	 * Get the contents of an <code>InputStream</code> as a String using the specified character encoding.
	 * <p>
	 * This method buffers the input internally, so there is no need to use a <code>BufferedInputStream</code>.
	 * </p>
	 * 
	 * @param input the <code>InputStream</code> to read from
	 * @param encoding the encoding to use, null means platform default
	 * @return the requested String
	 * @throws NullPointerException if the input is null
	 * @throws NestedIOException if an I/O error occurs
	 */
	public static String readString(InputStream input, Charset encoding) throws NestedIOException {
		StringWriter sw = new StringWriter();
		copy(input, sw, encoding);
		return sw.toString();
	}

	/**
	 * Get the contents of a <code>Reader</code> as a String.
	 * <p>
	 * This method buffers the input internally, so there is no need to use a <code>BufferedReader</code>.
	 * 
	 * @param input the <code>Reader</code> to read from
	 * @return the requested String
	 * @throws NullPointerException if the input is null
	 * @throws NestedIOException if an I/O error occurs
	 */
	public static String readString(Reader input) throws IOException {
		StringWriter sw = new StringWriter();
		copy(input, sw);
		return sw.toString();
	}
	
	public static void writeString(File file,String string, Charset encoding){
		FileOutputStream out = null;
		try{
			out = new FileOutputStream(file);
			writeString(out,string,encoding);
		}catch(IOException e){
			throw Exceptions.wrap(e);
		}finally{
			close(out);
		}
	}
	
	public static void writeString(OutputStream out,String string,Charset encoding) {
		byte[] data = string.getBytes(encoding);
		try {
	        out.write(data);
        } catch (IOException e) {
        	throw Exceptions.wrap(e);
        }
	}
	
	// copy from InputStream
	//-----------------------------------------------------------------------
	/**
	 * Copy bytes from an <code>InputStream</code> to an <code>OutputStream</code>.
	 * <p>
	 * This method buffers the input internally, so there is no need to use a <code>BufferedInputStream</code>.
	 * <p>
	 * The buffer size is given by {@link #DEFAULT_BUFFER_SIZE}.
	 * 
	 * @param input the <code>InputStream</code> to read from
	 * @param output the <code>OutputStream</code> to write to
	 * @return the number of bytes copied
	 * @throws NullPointerException if the input or output is null
	 * @throws NestedIOException if an I/O error occurs
	 */
	public static long copy(InputStream input, OutputStream output) throws NestedIOException {
		return copy(input, output, new byte[DEFAULT_BUFFER_SIZE]);
	}

	/**
	 * Copy bytes from an <code>InputStream</code> to an <code>OutputStream</code>.
	 * <p>
	 * This method uses the provided buffer, so there is no need to use a <code>BufferedInputStream</code>.
	 * <p>
	 * 
	 * @param input the <code>InputStream</code> to read from
	 * @param output the <code>OutputStream</code> to write to
	 * @param buffer the buffer to use for the copy
	 * @return the number of bytes copied
	 * @throws NullPointerException if the input or output is null
	 * @throws NestedIOException if an I/O error occurs
	 * @since 2.2
	 */
	public static long copy(InputStream input, OutputStream output, byte[] buffer) throws NestedIOException {
		long count = 0;
		int n = 0;
		try {
	        while (EOF != (n = input.read(buffer))) {
	        	output.write(buffer, 0, n);
	        	count += n;
	        }
        } catch (IOException e) {
        	throw Exceptions.wrap(e);
        }
		return count;
	}

	/**
	 * Copy bytes from an <code>InputStream</code> to chars on a <code>Writer</code> using the default character
	 * encoding of the platform.
	 * <p>
	 * This method buffers the input internally, so there is no need to use a <code>BufferedInputStream</code>.
	 * <p>
	 * This method uses {@link InputStreamReader}.
	 * 
	 * @param input the <code>InputStream</code> to read from
	 * @param output the <code>Writer</code> to write to
	 * @throws NullPointerException if the input or output is null
	 * @throws NestedIOException if an I/O error occurs
	 * @since 1.1
	 */
	public static void copy(InputStream input, Writer output) throws NestedIOException {
		copy(input, output, Charset.defaultCharset());
	}

	/**
	 * Copy bytes from an <code>InputStream</code> to chars on a <code>Writer</code> using the specified character
	 * encoding.
	 * <p>
	 * This method buffers the input internally, so there is no need to use a <code>BufferedInputStream</code>.
	 * <p>
	 * This method uses {@link InputStreamReader}.
	 * 
	 * @param input the <code>InputStream</code> to read from
	 * @param output the <code>Writer</code> to write to
	 * @param encoding the encoding to use, null means platform default
	 * @throws NullPointerException if the input or output is null
	 * @throws NestedIOException if an I/O error occurs
	 * @since 2.3
	 */
	public static void copy(InputStream input, Writer output,@Nullable Charset encoding) throws NestedIOException {
		InputStreamReader in = new InputStreamReader(input,Charsets.get(encoding));
		copy(in, output);
	}

	/**
	 * Copy bytes from an <code>InputStream</code> to chars on a <code>Writer</code> using the specified character
	 * encoding.
	 * <p>
	 * This method buffers the input internally, so there is no need to use a <code>BufferedInputStream</code>.
	 * <p>
	 * Character encoding names can be found at <a href="http://www.iana.org/assignments/character-sets">IANA</a>.
	 * <p>
	 * This method uses {@link InputStreamReader}.
	 * 
	 * @param input the <code>InputStream</code> to read from
	 * @param output the <code>Writer</code> to write to
	 * @param encoding the encoding to use, null means platform default
	 * @throws NullPointerException if the input or output is null
	 * @throws NestedIOException if an I/O error occurs
	 * @throws UnsupportedCharsetException thrown instead of {@link UnsupportedEncodingException} in version 2.2 if the
	 *             encoding is not supported.
	 * @since 1.1
	 */
	public static void copy(InputStream input, Writer output, String encoding) throws NestedIOException {
		copy(input, output, Charsets.get(encoding));
	}

	// copy from Reader
	//-----------------------------------------------------------------------	
	/**
	 * Copy chars from a <code>Reader</code> to a <code>Writer</code>.
	 * <p>
	 * This method buffers the input internally, so there is no need to use a <code>BufferedReader</code>.
	 * <p>
	 * The buffer size is given by {@link #DEFAULT_BUFFER_SIZE}.
	 * 
	 * @param input the <code>Reader</code> to read from
	 * @param output the <code>Writer</code> to write to
	 * @return the number of characters copied
	 * @throws NullPointerException if the input or output is null
	 * @throws NestedIOException if an I/O error occurs
	 */
	public static long copy(Reader input, Writer output) throws NestedIOException {
		return copy(input, output, new char[DEFAULT_BUFFER_SIZE]);
	}

	/**
	 * Copy chars from a <code>Reader</code> to a <code>Writer</code>.
	 * <p>
	 * This method uses the provided buffer, so there is no need to use a <code>BufferedReader</code>.
	 * <p>
	 * 
	 * @param input the <code>Reader</code> to read from
	 * @param output the <code>Writer</code> to write to
	 * @param buffer the buffer to be used for the copy
	 * @return the number of characters copied
	 * @throws NullPointerException if the input or output is null
	 * @throws IOException if an I/O error occurs
	 */
	public static long copy(Reader input, Writer output, char[] buffer) throws NestedIOException {
		long count = 0;
		int n = 0;
		try {
	        while (EOF != (n = input.read(buffer))) {
	        	output.write(buffer, 0, n);
	        	count += n;
	        }
        } catch (IOException e) {
        	throw Exceptions.wrap(e);
        }
		return count;
	}
	
	protected IO(){
		
	}
}
