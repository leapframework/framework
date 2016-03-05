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

import java.util.zip.CRC32;

import leap.core.BeanFactory;
import leap.core.annotation.Inject;
import leap.core.annotation.M;
import leap.core.ioc.PostCreateBean;
import leap.lang.Randoms;
import leap.lang.Strings;
import leap.lang.codec.Digests;
import leap.lang.codec.Hex;
import leap.lang.path.Paths;

public class DefaultAssetStrategy implements AssetStrategy,PostCreateBean {
	
	protected @Inject @M AssetConfig config;
	
	private int fingerprintLength;
	
	public void setConfig(AssetConfig config) {
		this.config = config;
	}
	
	@Override
    public int getFingerprintLength() {
	    return fingerprintLength;
    }

	@Override
	public String getFingerprint(byte[] content) {
		String hash = Hex.encode(Digests.getDigest(config.getHashAlgorithm()).digest(content));
		return hashToCodeString(hash);
	}

	@Override
    public String getPathWithFingerprint(String filepath, String fingerprint) {
    	String dirpath  = Paths.getDirPath(filepath);
    	String filename = Paths.getFileName(filepath);
		
		int lastDotIndex = filename.lastIndexOf('.');
		
		if(lastDotIndex > 0){
			return dirpath + filename.substring(0,lastDotIndex) + "-" + fingerprint + filename.substring(lastDotIndex);
		}else{
			return dirpath + filename + "-" + fingerprint;
		}		
    }
	
	@Override
    public String getPathWithoutFingerprint(String filepath) {
		String[] pathAndFingerprint = splitPathAndFingerprint(filepath);
	    return null == pathAndFingerprint ? filepath : pathAndFingerprint[0];
    }

	@Override
    public String[] splitPathAndFingerprint(String filepath) {
    	String dirpath  = Paths.getDirPath(filepath);
    	String filename = Paths.getFileName(filepath);
    	
    	int lastFingerprintIndex = filename.lastIndexOf("-");
    	
    	if(lastFingerprintIndex > 0){
    		int lastDotIndex = filename.lastIndexOf(".");
    		String fingerprint = filename.substring(lastFingerprintIndex + 1, lastDotIndex > 0 ? lastDotIndex : filename.length());
    		String filepathWithoutFingerprint;
    		if(fingerprint.length() == fingerprintLength){
    			if(lastDotIndex > 0){
    				filepathWithoutFingerprint = dirpath + filename.substring(0,lastFingerprintIndex) + filename.substring(lastDotIndex);
    			}else{
    				filepathWithoutFingerprint = dirpath + filename.substring(0,lastFingerprintIndex);
    			}
    			return new String[]{filepathWithoutFingerprint,fingerprint};
    		}
    	}
		return null;
    }

	@Override
    public void postCreate(BeanFactory beanFactory) throws Exception {
		int len1 = getFingerprint(Randoms.nextString(10).getBytes()).length();
		int len2 = getFingerprint(Randoms.nextString(20).getBytes()).length();
		
		if(len1 != len2){
			throw new AssetException("The fingerprint's length must be fixed");
		}
		this.fingerprintLength = len1;
    }
	
	protected String hashToCodeString(String hash) {
		long hashCode = Math.abs(hash.hashCode());
		
		CRC32 alder32 = new CRC32();
		alder32.update(hash.getBytes());
		hashCode = hashCode + Math.abs(alder32.getValue());
		
		String hashCodeString = String.valueOf(hashCode);
		
		if(hashCodeString.length() < 10) {
			hashCodeString = Strings.repeat('0', 10 - hashCodeString.length()) + hashCodeString;
		}else if(hashCodeString.length() > 10) {
			hashCodeString = hashCodeString.substring(0, 10);
		}
		
		return hashCodeString;
	}

}
