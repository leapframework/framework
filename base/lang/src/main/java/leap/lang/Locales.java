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
package leap.lang;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import leap.lang.path.Paths;

public class Locales {
	
	public static final Locale DEFAULT_LOCALE = Locale.getDefault();

	/**
	 * Returns <code>null</code> if empty or invalid locale format.
	 */
	public static Locale tryForName(String localName) {
		if(Strings.isEmpty(localName)) {
			return null;
		}else {
			try {
				return forName(localName);
			}catch (Exception e) {
				return null;
			}
		}
	}
	
    /**
     * <p>Converts a String to a Locale.</p>
     *
     * <p>This method takes the string format of a locale and creates the
     * locale object from it.</p>
     *
     * <pre>
     *   Locales.forName("en")         = new Locale("en", "")
     *   Locales.forName("en_GB")      = new Locale("en", "GB")
     *   Locales.forName("en_GB_xxx")  = new Locale("en", "GB", "xxx")   (#)
     * </pre>
     *
     * <p>(#) The behaviour of the JDK variant constructor changed between JDK1.3 and JDK1.4.
     * In JDK1.3, the constructor upper cases the variant, in JDK1.4, it doesn't.
     * Thus, the result from getVariant() may vary depending on your JDK.</p>
     *
     * <p>This method validates the input strictly.
     * The language code must be lowercase.
     * The country code must be uppercase.
     * The separator must be an underscore.
     * The length must be correct.
     * </p>
     *
     * @param localeName  the locale String to convert
     * @return a Locale
     * @throws IllegalArgumentException if the string is empty or an invalid format
     */
	//from apache commons lang
	public static Locale forName(String localeName) {
		Args.notEmpty(localeName,"locale name");
        int len = localeName.length();
        if (len != 2 && len != 5 && len < 7) {
            throw new IllegalArgumentException("Invalid locale format: " + localeName);
        }
        char ch0 = localeName.charAt(0);
        char ch1 = localeName.charAt(1);
        if (ch0 < 'a' || ch0 > 'z' || ch1 < 'a' || ch1 > 'z') {
            throw new IllegalArgumentException("Invalid locale format: " + localeName);
        }
        if (len == 2) {
            return new Locale(localeName, "");
        } else {
            if (localeName.charAt(2) != '_') {
                throw new IllegalArgumentException("Invalid locale format: " + localeName);
            }
            char ch3 = localeName.charAt(3);
            if (ch3 == '_') {
                return new Locale(localeName.substring(0, 2), "", localeName.substring(4));
            }
            char ch4 = localeName.charAt(4);
            if (ch3 < 'A' || ch3 > 'Z' || ch4 < 'A' || ch4 > 'Z') {
                throw new IllegalArgumentException("Invalid locale format: " + localeName);
            }
            if (len == 5) {
                return new Locale(localeName.substring(0, 2), localeName.substring(3, 5));
            } else {
                if (localeName.charAt(5) != '_') {
                    throw new IllegalArgumentException("Invalid locale format: " + localeName);
                }
                return new Locale(localeName.substring(0, 2), localeName.substring(3, 5), localeName.substring(6));
            }
        }
	}
	
	/**
	 * Extracts the locale string in the filename, i.e. message_en.xml, message_zh_CN.xml
	 * 
	 * <p>
	 * Returns empty string "" if no locale string in the filename;
	 * 
	 * <p>
	 * This method does not validate the locale string, use {@link #forName(String)} to test it.
	 */
	public static String extractFromFilename(String filename){
		filename = Paths.getFileNameWithoutExtension(filename);
		
		String localeName;
		
		String[] parts = Strings.split(filename,"_");
		
		if(parts.length == 2){
			localeName = parts[1];
		}else if(parts.length >= 3){
			localeName = parts[1] + "_" + parts[2];
		}else{
			localeName = Strings.EMPTY;
		}
		
		if(!Strings.isEmpty(localeName)){
			try {
	            forName(localeName);
            } catch (Exception e) {
            	return Strings.EMPTY;
            }
		}
		
		return localeName;
	}
	
	public static String[] getLocalePaths(Locale locale, String path){
		List<String> paths = new ArrayList<String>();
		
		String lang    = null == locale ? null : locale.getLanguage();
		String country = null == locale ? null : locale.getCountry();
		
		//{path}_{lang}_{COUNTRY}
		if(!Strings.isEmpty(country)){
			paths.add(path + "_" + lang + "_" + country);
		}
		
		//{path}_{lang}
		if(!Strings.isEmpty(lang)){
			paths.add(path + "_" + lang);
		}
		
		//{path}
		paths.add(path);
		
		return paths.toArray(new String[paths.size()]);
	}
	
	public static String[] getLocalePaths(Locale locale, String path,String suffix){
		List<String> paths = new ArrayList<String>();
		
		if(null == suffix){
			suffix = "";
		}else if(path.endsWith(suffix)){
			path = Strings.removeEnd(path,suffix);
		}
		
		String lang    = null == locale ? null : locale.getLanguage();
		String country = null == locale ? null : locale.getCountry();
		
		//{path}_{lang}_{COUNTRY}{suffix}
		if(!Strings.isEmpty(country)){
			paths.add(path + "_" + lang + "_" + country + suffix);
		}
		
		//{path}_{lang}{suffix}
		if(!Strings.isEmpty(lang)){
			paths.add(path + "_" + lang + suffix);
		}
		
		//{path}{suffix}
		paths.add(path + suffix);
		
		return paths.toArray(new String[paths.size()]);
	}
	
	public static String[] getLocaleFilePaths(Locale locale, String filePath) {
		String ext  = Paths.getFileExtension(filePath);
		String path = Strings.isEmpty(ext) ? filePath : filePath.substring(0,filePath.length() - ext.length() - 1);
		return getLocalePaths(locale, path, "." + ext);
	}
	
	protected Locales(){
		
	}
}