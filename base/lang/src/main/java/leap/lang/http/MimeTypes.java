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
package leap.lang.http;

import leap.lang.Args;
import leap.lang.Strings;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.resource.Resource;
import leap.lang.resource.ResourceSet;
import leap.lang.resource.Resources;

import javax.activation.FileTypeMap;
import javax.activation.MimetypesFileTypeMap;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.UnsupportedCharsetException;
import java.util.*;


public class MimeTypes {
	
	private static final Log log = LogFactory.get(MimeTypes.class);
	
	private static final FileTypeMap[] mimeTypeMaps;
	static {
		mimeTypeMaps = loadMimeTypeMaps();
	}
	
	public static final MimeType[] EMPTY_ARRAY = new MimeType[]{};
	
	public static final String   ALL = "*/*";
	public static final MimeType ALL_TYPE = parse(ALL);
	
	public static final String   TEXT_PLAIN      = "text/plain";
	public static final String   TEXT_PLAIN_UTF8 = "text/plain;charset=UTF-8";
	public static final MimeType TEXT_PLAIN_TYPE = parse(TEXT_PLAIN);

	public static final String   TEXT_HTML       = "text/html";
	public static final String   TEXT_HTML_UTF8  = "text/html;charset=UTF-8";
	public static final MimeType TEXT_HTML_TYPE  = parse(TEXT_HTML);
	
	public static final String   TEXT_XML        = "text/xml";
    public static final String   TEXT_XML_UTF8   = "text/xml;charset=UTF-8";
	public static final MimeType TEXT_XML_TYPE   = parse(TEXT_XML);
	
	public static final String   TEXT_CSS      = "text/css";
	public static final String   TEXT_CSS_UTF8 = "text/css;charset=UTF-8";
	public static final MimeType TEXT_CSS_TYPE = parse(TEXT_CSS);
	
	public static final String   TEXT_JAVASCRIPT      = "text/javascript";
	public static final String   TEXT_JAVASCRIPT_UTF8 = "text/javascript;charset=UTF-8";
	public static final MimeType TEXT_JAVASCRIPT_TYPE = parse(TEXT_JAVASCRIPT);
	
	public static final String   APPLICATION_JAVASCRIPT      = "application/javascript";
	public static final String   APPLICATION_JAVASCRIPT_UTF8 = "application/javascript;charset=UTF-8";
	public static final MimeType APPLICATION_JAVASCRIPT_TYPE = parse(APPLICATION_JAVASCRIPT);
	
	public static final String   APPLICATION_JSON      = "application/json";
	public static final String   APPLICATION_JSON_UTF8 = "application/json;charset=UTF-8";
	public static final MimeType APPLICATION_JSON_TYPE = parse(APPLICATION_JSON);
	
	public static final String   APPLICATION_XML      = "application/xml";
	public static final String   APPLICATION_XML_UTF8 = "application/xml;charset=UTF-8";
	public static final MimeType APPLICATION_XML_TYPE = parse(APPLICATION_XML);
	
	public static final String   APPLICATION_ATOM_XML      = "application/atom+xml";
	public static final MimeType APPLICATION_ATOM_XML_TYPE = parse(APPLICATION_ATOM_XML);
	
	public static final String   APPLICATION_XHTML_XML      = "application/xhtml+xml";
	public static final MimeType APPLICATION_XHTML_XML_TYPE = parse(APPLICATION_XHTML_XML);
	
	public static final String   APPLICATION_SVG_XML      = "application/svg+xml";
	public static final MimeType APPLICATION_SVG_XML_TYPE = parse(APPLICATION_SVG_XML);
	
	public static final String   APPLICATION_FORM_URLENCODED      = "application/x-www-form-urlencoded";
	public static final MimeType APPLICATION_FORM_URLENCODED_TYPE = parse(APPLICATION_FORM_URLENCODED);
	
	public static final String   APPLICATION_OCTET_STREAM      = "application/octet-stream";
	public static final MimeType APPLICATION_OCTET_STREAM_TYPE = parse(APPLICATION_OCTET_STREAM);
	
	public static final String   MULTIPART_FORM_DATA      = "multipart/form-data";
	public static final MimeType MULTIPART_FORM_DATA_TYPE = parse(MULTIPART_FORM_DATA);
	
	public static boolean isText(String mimeType){
		//TODO : 
		return Strings.startsWith(mimeType, "text/") || 
			   Strings.endsWith(mimeType, "javascript") ||
			   Strings.endsWith(mimeType, "json") ||
			   Strings.endsWith(mimeType, "xml");
	}
	
	//some codes from spring framework
	public static MimeType parse(String mimeType) {
		Args.notEmpty(mimeType,"mimeType");

		String[] parts = Strings.split(mimeType, ';');

		String fullType = parts[0].trim();
		// java.net.HttpURLConnection returns a *; q=.2 Accept header
		if (MimeType.WILDCARD_TYPE.equals(fullType)) {
			fullType = "*/*";
		}
		
		int subIndex = fullType.indexOf('/');
		if (subIndex == -1) {
			throw new IllegalArgumentException("mime type '" + mimeType + "' : does not contain '/'");
		}
		if (subIndex == fullType.length() - 1) {
			throw new IllegalArgumentException("mime type '" + mimeType + "' : does not contain subtype after '/'");
		}
		
		String type = fullType.substring(0, subIndex);
		String subtype = fullType.substring(subIndex + 1, fullType.length());
		if (MimeType.WILDCARD_TYPE.equals(type) && !MimeType.WILDCARD_TYPE.equals(subtype)) {
			throw new IllegalArgumentException("mime type '" + mimeType + "' : wildcard type is legal only in '*/*' (all mime types)");
		}

		Map<String, String> parameters = null;
		if (parts.length > 1) {
			parameters = new LinkedHashMap<String, String>(parts.length - 1);
			for (int i = 1; i < parts.length; i++) {
				String parameter = parts[i];
				int eqIndex = parameter.indexOf('=');
				if (eqIndex != -1) {
					String attribute = parameter.substring(0, eqIndex);
					String value = parameter.substring(eqIndex + 1, parameter.length());
					parameters.put(attribute, value);
				}
			}
		}

		try {
			return new MimeType(type, subtype, parameters);
		} catch (UnsupportedCharsetException ex) {
			throw new IllegalArgumentException("mime type '" + mimeType + "' : unsupported charset '" + ex.getCharsetName() + "'");
		} catch (IllegalArgumentException ex) {
			throw new IllegalArgumentException("mime type '" + mimeType + "' : " + ex.getMessage());
		}
	}
	
	/**
	 * Parse the given, comma-separated string into a list of {@code MimeType} objects.
	 * @param mimeTypes the string to parse
	 * @return the list of mime types
	 * @throws IllegalArgumentException if the string cannot be parsed
	 */
	public static List<MimeType> parseList(String mimeTypes) {
		if (Strings.isEmpty(mimeTypes)) {
			return Collections.emptyList();
		}
		String[] tokens = mimeTypes.split(",\\s*");
		List<MimeType> result = new ArrayList<MimeType>(tokens.length);
		for (String token : tokens) {
			result.add(parse(token));
		}
		return result;
	}	
	
	/**
	 * @see FileTypeMap#getContentType(String)
	 */
	public static String getMimeType(String filename){
		String mimeType = null;
		for(FileTypeMap map : mimeTypeMaps){
			if(!(mimeType = map.getContentType(filename)).equals(APPLICATION_OCTET_STREAM)){
				return mimeType;
			}
		}
		return APPLICATION_OCTET_STREAM;
	}
	
	private static FileTypeMap[] loadMimeTypeMaps() {
		LinkedList<FileTypeMap> list = new LinkedList<FileTypeMap>();
		
		FileTypeMap defaultFileTypeMap = FileTypeMap.getDefaultFileTypeMap();
		if(null != defaultFileTypeMap){
			list.add(defaultFileTypeMap);
		}

		InputStream internalFileTypeMapStream = MimeTypes.class.getResourceAsStream("mime.types");
		if(null != internalFileTypeMapStream){
            list.addFirst(new MimetypesFileTypeMap(internalFileTypeMapStream));
		}
		
		ResourceSet externalMimeTypesResources = Resources.scan("classpath*:/META-INF/mime.types");
		for(Resource r : externalMimeTypesResources){
			try {
	            list.addFirst(new MimetypesFileTypeMap(r.getInputStream()));
            } catch (IOException e) {
            	log.warn("Error reading mime types resource '{}'",r.getURLString(),e);
            }
		}
		
		return list.toArray(new FileTypeMap[]{});
	}
	
	protected MimeTypes(){
		
	}
}
