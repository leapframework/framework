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

import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;

import leap.lang.Args;
import leap.lang.Strings;


public class AssetFile {
	
	protected final String		assetPath;
	protected final AssetType   assetType;
	protected final Charset     charset;
	protected final String      sourceFilename;
	protected final byte[]      sourceContent;
	protected final Set<String> refernecedFiles= new HashSet<String>();
	
	protected String targetFilename;
	protected byte[] targetContent;
	
	protected String minifiedFilename;
	protected byte[] minifiedContent;
	
	protected String gzippedFilename;
	protected byte[] gzippedContent;
	
	public AssetFile(String assetPath, AssetType assetType, Charset charset, String sourceFilename,  byte[] sourceContent){
		this.assetPath      = assetPath;
		this.assetType      = assetType;
		this.charset        = charset;
		this.sourceFilename = sourceFilename;
		this.sourceContent  = sourceContent;
		this.targetFilename = sourceFilename;
		this.targetContent  = sourceContent;
	}
	
	public String getAssetPath() {
		return assetPath;
	}

	public AssetType getAssetType() {
		return assetType;
	}
	
	public Charset getCharset() {
		return charset;
	}

	public String getSourceFilename() {
		return sourceFilename;
	}
	
	public byte[] getSourceContent() {
		return sourceContent;
	}
	
	public String getSourceContentAsString(){
		return Strings.newString(sourceContent, charset.name());
	}
	
	public String getTargetFilename() {
		return targetFilename;
	}

	public void setTargetFilename(String targetFilename) {
		Args.notEmpty(targetFilename,"targetFilename");
		this.targetFilename = targetFilename;
	}

	public byte[] getTargetContent() {
		return targetContent;
	}

	public void setTargetContent(byte[] targetContent) {
		Args.notNull(targetContent,"targetContent");
		this.targetContent = targetContent;
	}

	public boolean isMinified() {
		return null != minifiedContent;
	}
	
    public String getMinifiedFilename() {
		return minifiedFilename;
	}

	public void setMinifiedFilename(String minifiedFilename) {
		this.minifiedFilename = minifiedFilename;
	}

	public byte[] getMinifiedContent() {
		return minifiedContent;
	}
	
	public void setMinifiedContent(byte[] minimizedContent) {
		this.minifiedContent = minimizedContent;
	}
	
	public boolean isGzipped(){
		return null != gzippedContent;
	}
	
	public String getGzippedFilename() {
		return gzippedFilename;
	}

	public void setGzippedFilename(String gzippedFilename) {
		this.gzippedFilename = gzippedFilename;
	}

	public byte[] getGzippedContent() {
		return gzippedContent;
	}

	public void setGzippedContent(byte[] gzippedContent) {
		this.gzippedContent = gzippedContent;
	}

	public Set<String> getRefernecedFiles() {
		return refernecedFiles;
	}
	
	public void addReferencedFile(String filename){
		refernecedFiles.add(filename);
	}
}
