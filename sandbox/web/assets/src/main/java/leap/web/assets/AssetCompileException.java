/*
 * Copyright 2014 the original author or authors.
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
package leap.web.assets;


public class AssetCompileException extends AssetException {

	private static final long serialVersionUID = -6188278836042287459L;
	
	private String filename;
	private String source;
	private int    line;
	private int    column;

	public AssetCompileException() {
	}

	public AssetCompileException(String message) {
		super(message);
	}

	public AssetCompileException(Throwable cause) {
		super(cause);
	}

	public AssetCompileException(String message, Throwable cause) {
		super(message, cause);
	}

	public String getFilename() {
		return filename;
	}
	
	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getSource() {
		return source;
	}
	
	public void setSource(String source) {
		this.source = source;
	}

	public int getLine() {
		return line;
	}

	public void setLine(int line) {
		this.line = line;
	}

	public int getColumn() {
		return column;
	}
	
	public void setColumn(int column) {
		this.column = column;
	}
	
	public AssetCompileException setErrorInfo(String filename,String source){
		this.filename = filename;
		this.source   = source;
		return this;
	}

	public AssetCompileException setErrorInfo(String filename,String source,int line,int column){
		this.filename = filename;
		this.source   = source;
		this.line     = line;
		this.column   = column;
		return this;
	}
}
