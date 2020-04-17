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
package leap.lang.net;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Map.Entry;

import leap.lang.Charsets;
import leap.lang.Exceptions;
import leap.lang.New;
import leap.lang.Strings;
import leap.lang.path.Paths;

//some codes copy from spring framework, under Apache License 2.0
public class Urls {
	
	public static final String CLASSPATH_ONE_URL_PREFIX = "classpath:";  //Pseudo URL prefix for loading the class path resource
	public static final String CLASSPATH_ALL_URL_PREFIX = "classpath*:"; //Pseudo URL prefix for loading the class path resource
	
	public static final String FILE_URL_PREFIX    = "file:";
	public static final String JAR_URL_SEPARATOR  = "!/";
	public static final String PROTOCOL_SEPARATOR = "://";
	
	public static final String PROTOCOL_FILE        = "file";
	public static final String PROTOCOL_JAR         = "jar";
	public static final String PROTOCOL_ZIP         = "zip";
	public static final String PROTOCOL_VFSZIP      = "vfszip";
	public static final String PROTOCOL_VFS         = "vfs";
	public static final String PROTOCOL_WSJAR       = "wsjar";
	public static final String PROTOCOL_CODE_SOURCE = "code-source";
	
	public static String encode(String url) {
	    if(null == url) {
	        return url;
	    }
		try {
	        return URLEncoder.encode(url, Charsets.UTF_8_NAME);
        } catch (UnsupportedEncodingException e) {
        	throw Exceptions.wrap(e);
        }
	}
	
    public static String encode(String url, String charset) {
        if(null == url) {
            return url;
        }
        try {
            return URLEncoder.encode(url, charset);
        } catch (UnsupportedEncodingException e) {
            throw Exceptions.wrap(e);
        }
    }

	public static String decode(String s) {
	    if(null == s) {
	        return s;
	    }
        try {
            return URLDecoder.decode(s, Charsets.UTF_8_NAME);
        } catch (UnsupportedEncodingException e) {
            throw Exceptions.wrap(e);
        }
	}
	
    public static String decode(String s, String charset) {
        if(null == s) {
            return s;
        }
        try {
            return URLDecoder.decode(s, charset);
        } catch (UnsupportedEncodingException e) {
            throw Exceptions.wrap(e);
        }
    }
	
	public static boolean hasProtocolPrefix(String location) {
		return null != location && location.indexOf(':') > 0;
	}
	
	public static boolean isClasspath(String resourceLocation){
		return Strings.startsWith(resourceLocation, CLASSPATH_ONE_URL_PREFIX) || 
			   Strings.startsWith(resourceLocation, CLASSPATH_ALL_URL_PREFIX);
	}
	
	public static String getQueryString(String uri) {
	    if(null == uri) {
	        return null;
	    }
	    
	    int index = uri.indexOf('?');
	    if(index > 0) {
	        return uri.substring(index+1);
	    }
	    
	    return null;
	}
	
	public static String appendQueryString(String url, String queryString) {
	    if(null == url) {
	        return url;
	    }
	    
		int index = url.lastIndexOf('?');
		if(index < 0) {
			return url + "?" + queryString;
		}else{
			if(index == url.length() - 1) {
				return url + queryString;
			}else{
				return url + "&" + queryString;
			}
		}
	}
	
	public static String appendQueryParams(String url, String name, String value) {
	    return appendQueryString(url, name + "=" + encode(value));
	}
	
	public static String removeQueryString(String url) {
	    if(null == url) {
	        return url;
	    }
	    int index = url.indexOf('?');
	    if(index < 0) {
	        return url;
	    }else{
	        return url.substring(0, index);
	    }
	}
		
	/**
	 * Return whether the given resource location is a URL: either a special "classpath" pseudo URL or a standard URL.
	 */
	public static boolean isUrl(String resourceLocation) {
		if (resourceLocation == null) {
			return false;
		}
		if (resourceLocation.startsWith(CLASSPATH_ONE_URL_PREFIX)) {
			return true;
		}
		try {
			new URL(resourceLocation);
			return true;
		} catch (MalformedURLException ex) {
			return false;
		}
	}
	
	/**
	 * Determine whether the given URL points to a resource in the file system, that is, has protocol "file" or "vfs".
	 */
	public static boolean isFileUrl(URL url) {
		String protocol = url.getProtocol();
		return (PROTOCOL_FILE.equals(protocol) || protocol.startsWith(PROTOCOL_VFS));
	}	
	
	/**
	 * Determine whether the given URL points to a resource in a jar file, that is, has protocol "jar", "zip", "wsjar" or "code-source".
	 * <p>
	 * "zip" and "wsjar" are used by BEA WebLogic Server and IBM WebSphere, respectively, but can be treated like jar
	 * files. The same applies to "code-source" URLs on Oracle OC4J, provided that the path contains a jar separator.
	 */
	public static boolean isJarUrl(URL url) {
		String protocol = url.getProtocol();
		return (PROTOCOL_JAR.equals(protocol) || 
				PROTOCOL_ZIP.equals(protocol) || 
				PROTOCOL_WSJAR.equals(protocol) || 
				(PROTOCOL_CODE_SOURCE.equals(protocol) && url.getPath().contains(JAR_URL_SEPARATOR)));
	}	
	
	/**
	 * Create a URI instance for the given URL, replacing spaces with "%20" quotes first.
	 */
	public static URI toURI(URL url) throws URISyntaxException {
		return toURI(url.toString());
	}	
	
	/**
	 * Create a URI instance for the given location String, replacing spaces with "%20" quotes first.
	 */
	public static URI toURI(String location) throws URISyntaxException {
		return new URI(Strings.replace(location, " ", "%20"));
	}
	
	/**
	 * get the URL for the actual jar file from the given URL (which may point to a resource in a jar file or to a
	 * jar file itself).
	 * 
	 * @param jarUrl the original URL
	 * @return the URL for the actual jar file
	 * @throws MalformedURLException if no valid jar file URL could be extracted
	 */
	public static URL getJarFileURL(URL jarUrl) throws MalformedURLException {
		String urlFile = jarUrl.getFile();
		int separatorIndex = urlFile.indexOf(JAR_URL_SEPARATOR);
		if (separatorIndex != -1) {
			String jarFile = urlFile.substring(0, separatorIndex);
			//war:file:/Users/../app.jar*/BOOT-INF/lib/a.jar
            if(jarFile.startsWith("war:")) {
                jarFile = Strings.removeStart(jarFile, "war:");
                separatorIndex = jarFile.indexOf("*/BOOT-INF/");
                if(separatorIndex != -1) {
                    jarFile = jarFile.substring(0, separatorIndex);
                }
            }
			try {
				return new URL(jarFile);
			} catch (MalformedURLException ex) {
				// Probably no protocol in original jar URL, like "jar:C:/mypath/myjar.jar".
				// This usually indicates that the jar file resides in the file system.
				if (!jarFile.startsWith("/")) {
					jarFile = "/" + jarFile;
				}
				return new URL(FILE_URL_PREFIX + jarFile);
			}
		} else {
			return jarUrl;
		}
	}
	
	/**
	 * Set the {@link URLConnection#setUseCaches "useCaches"} flag on the given connection, preferring
	 * <code>false</code> but leaving the flag at <code>true</code> for JNLP based resources.
	 */
	public static void setUseCachesIfNecessary(URLConnection con) {
		con.setUseCaches(con.getClass().getName().startsWith("JNLP"));
	}
	
	public static String getQueryString(Map<String, String> map) {
		return getQueryStringBuilder(map).toString();
	}
	
	public static StringBuilder getQueryStringBuilder(Map<String, String> map) {
		StringBuilder sb = new StringBuilder();
		
		if(null != map) {
			int i=0;
			for(Entry<String, String> entry : map.entrySet()){
				if(i > 0){
					sb.append("&");
				}
				
				try {
	                sb.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(),Charsets.defaultCharset().name()));
                } catch (UnsupportedEncodingException e) {
                	throw new IllegalStateException(e);
                }
					
				i++;
			}
		}
		
		return sb;
	}
	
	public static Map<String, String> queryStringToMap(String queryString){
		Map<String, String> map = New.hashMap();
		if(Strings.isNotEmpty(queryString)){
			String[] kvs = Strings.split(queryString,'&');
			for(String kvStr : kvs){
				int idx = kvStr.indexOf('=');
				if(idx < 0){
					map.put(kvStr.trim(),null);
				}else if(idx >= kvStr.length()){
					String k = kvStr.substring(0,idx);
					map.put(k,null);
				}else {
					String k = kvStr.substring(0,idx);
					String v = kvStr.substring(idx+1);
					map.put(k,v);
				}
			}
		}
		return map;
	}

	/**
	 * uri = http://127.0.0.1/ctx
	 * 
	 * expression will be:
	 * <ul>
	 *     <li><code>@{~/}path1</code> 	-> <code>http://127.0.0.1/ctx/path1</code></li>
	 *     <li><code>@{~}path1</code> 	-> <code>http://127.0.0.1/ctxpath1</code></li>
	 *     <li><code>@{/}path1</code> 	-> <code>/ctx/path1</code></li>
	 *     <li><code>@{^/}path1</code> 	-> <code>/path1</code></li>
	 *     <li><code>@{^}path1</code> 	-> <code>path1</code></li>
	 * </ul>
	 * 
	 * @param expression
	 * @param uri
	 * @return
	 */
	public static String resolveUrlExpr(String expression, URI uri){
		int i = 0;
		StringBuilder source = new StringBuilder(expression);
		StringBuilder builder = new StringBuilder();
		while (source.length()>0){
			char c = source.charAt(i);
			source.deleteCharAt(i);
			if(c == '@'){
				if(source.charAt(i) == '{'){
					source.deleteCharAt(i);
					int index = source.indexOf("}");
					if(-1 == index){
						throw new IllegalArgumentException("error server url expression:"+expression);
					}
					String exp = source.substring(0,index);
					source.delete(0,index+1);
					builder.append(getExpressionValue(exp,uri));
				}
			}else {
				if(c != '/' || builder.length() == 0){
					builder.append(c);
				}else if(builder.charAt(builder.length()-1)!='/'){
					builder.append(c);
				}
			}
		}
		return builder.toString();
	}

	protected static String getExpressionValue(String expression, URI uri){
		if(Strings.startsWith(expression,"/")){
			return Paths.suffixWithoutSlash(uri.getPath()) + expression;
		}
		if(Strings.startsWith(expression,"~")){
			return Paths.suffixWithoutSlash(uri.toString()) + expression.substring(1);
		}
		if(Strings.startsWith(expression,"^")){
			return expression.substring(1);
		}
		return expression;
	}
	
	protected Urls(){
		
	}
}
