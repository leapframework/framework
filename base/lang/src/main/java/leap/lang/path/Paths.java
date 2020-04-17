/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package leap.lang.path;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import leap.lang.Strings;

public class Paths {

    public static final char   EXTENSION_SEPARATOR 	    = '.';
    public static final String EXTENSION_SEPARATOR_STR  = ".";
    public static final char   NORMALIZED_SEPERATOR     = '/';
    public static final String NORMALIZED_SEPERATOR_STR = "/";
    public static final char   UNIX_SEPARATOR 	        = '/';
    public static final String UNIX_SEPARATOR_STR       = "/";
    public static final char   WINDOWS_SEPARATOR        = '\\';
    public static final String WINDOWS_SEPARATOR_STR    = "\\";
    public static final char   SYSTEM_SEPARATOR         = File.separatorChar;
    public static final String SYSTEM_SEPARATOR_STR     = Character.toString(File.separatorChar);
    
	public static final String PARENT_PATH = "..";
	public static final String CURRENT_PATH = ".";

    protected Paths() {
        super();
    }

    //-----------------------------------------------------------------------
    
	/**
	 * Normalize the path by suppressing sequences like "path/.." and
	 * inner simple dots.
	 * <p>The result is convenient for path comparison. For other uses,
	 * notice that Windows separators ("\") are replaced by simple slashes.
	 * @param path the original path
	 * @return the normalized path
	 */
	public static String normalize(String path) {
		if (path == null) {
			return Strings.EMPTY;
		}
		String pathToUse = Strings.replace(path, WINDOWS_SEPARATOR, UNIX_SEPARATOR);

		// Strip prefix from path to analyze, to not treat it as part of the
		// first path element. This is necessary to correctly parse paths like
		// "file:core/../core/io/Resource.class", where the ".." should just
		// strip the first "core" directory while keeping the "file:" prefix.
		int prefixIndex = pathToUse.indexOf(":");
		String prefix = "";
		if (prefixIndex != -1) {
			prefix = pathToUse.substring(0, prefixIndex + 1);
			pathToUse = pathToUse.substring(prefixIndex + 1);
		}
		if (pathToUse.startsWith(UNIX_SEPARATOR_STR)) {
			prefix = prefix + UNIX_SEPARATOR;
			pathToUse = pathToUse.substring(1);
		}

		String[] pathArray = Strings.split(pathToUse, UNIX_SEPARATOR_STR, false, false);
		List<String> pathElements = new LinkedList<String>();
		int tops = 0;

		for (int i = pathArray.length - 1; i >= 0; i--) {
			String element = pathArray[i];
			if (CURRENT_PATH.equals(element)) {
				// Points to current directory - drop it.
			}
			else if (PARENT_PATH.equals(element)) {
				// Registering top path found.
				tops++;
			}
			else {
				if (tops > 0) {
					// Merging path element with element corresponding to top path.
					tops--;
				}
				else {
					// Normal path element found.
					pathElements.add(0, element);
				}
			}
		}

		// Remaining top paths need to be retained.
		for (int i = 0; i < tops; i++) {
			pathElements.add(0, PARENT_PATH);
		}

		return prefix + Strings.join(pathElements, UNIX_SEPARATOR_STR);
	}
	
	/**
	 * Extract the filename from the given path,
	 * e.g. "mypath/myfile.txt" -> "myfile.txt".
	 * @param path the file path (may be <code>null</code>)
	 * @return the extracted filename, or <code>null</code> if none
	 */
	public static String getFileName(String path) {
		if (path == null) {
			return Strings.EMPTY;
		}
		int separatorIndex = indexOfLastSeparator(path);
		return (separatorIndex != -1 ? path.substring(separatorIndex + 1) : path);
	}

	/**
	 * Extract the filename extension from the given path,
	 * e.g. "mypath/myfile.txt" -> "txt".
	 * @param path the file path (may be <code>null</code>)
	 * @return the extracted filename extension, or <code>""</code> if none
	 */
	public static String getFileExtension(String path) {
		if (path == null) {
			return Strings.EMPTY;
		}
		
		int extIndex = path.lastIndexOf(EXTENSION_SEPARATOR);
		if (extIndex == -1) {
			return Strings.EMPTY;
		}
		
		int folderIndex = indexOfLastSeparator(path);
		if (folderIndex > extIndex) {
			return Strings.EMPTY;
		}
		return path.substring(extIndex + 1);
	}	
	
	/**
	 * Extract the part without extension from the given filename,
	 * 
	 * e.g. "myfile.txt" -> "myfile"
	 */
	public static String getFileNameWithoutExtension(String filename){
		if(null == filename){
			return Strings.EMPTY;
		}
		
		int extIndex = filename.lastIndexOf(EXTENSION_SEPARATOR);
		if (extIndex == -1) {
			return filename;
		}
		
		int folderIndex = indexOfLastSeparator(filename);
		if (folderIndex > extIndex) {
			return filename;
		}
		
		return filename.substring(0,extIndex);
	}
	
    /**
     * Gets the full path from a full filename, which is the prefix + path.
     * <p>
     * This method will handle a file in either Unix or Windows format.
     * The method is entirely text based, and returns the text before and
     * including the last forward or backslash.
     * <pre>
     * C:\a\b\c.txt --> C:\a\b\
     * ~/a/b/c.txt  --> ~/a/b/
     * a.txt        --> ""
     * a/b/c        --> a/b/
     * a/b/c/       --> a/b/c/
     * C:           --> C:
     * C:\          --> C:\
     * ~            --> ~/
     * ~/           --> ~/
     * ~user        --> ~user/
     * ~user/       --> ~user/
     * </pre>
     * <p>
     * The output will be the same irrespective of the machine that the code is running on.
     *
     * @param filepath  the path to query, null returns null
     * @return the path of the file, an empty string if none exists, null if invalid
     */
    public static String getDirPath(String filepath) {
        return doGetFullPath(filepath, true);
    }
	
    /**
     * Gets the path from a full filename, which excludes the prefix.
     * <p>
     * This method will handle a file in either Unix or Windows format.
     * The method is entirely text based, and returns the text before and
     * including the last forward or backslash.
     * <pre>
     * C:\a\b\c.txt --> a\b\
     * ~/a/b/c.txt  --> a/b/
     * a.txt        --> ""
     * a/b/c        --> a/b/
     * a/b/c/       --> a/b/c/
     * </pre>
     * <p>
     * The output will be the same irrespective of the machine that the code is running on.
     * <p>
     * This method drops the prefix from the result.
     * See {@link #getDirPath(String)} for the method that retains the prefix.
     *
     * @param filepath  the path of full filename to query, null returns null
     * @return the path of the file, an empty string if none exists, null if invalid
     */
    public static String getDirPathWithoutPrefix(String filepath) {
        return doGetPath(filepath, 1);
    }

    /**
     * Returns the dir name of the file.
     *
     * <pre>
     *     a/b/c.txt -> b
     * </pre>
     */
    public static String getDirName(String filepath) {
        return getDirNameByDirPath(getDirPath(normalize(filepath)));
    }

    public static String getDirNameByDirPath(String dirPath) {
        dirPath = Paths.suffixWithoutSlash(dirPath);
        int lastIndex = dirPath.lastIndexOf('/');
        if(lastIndex >= 0) {
            return dirPath.substring(lastIndex+1);
        }else {
            return dirPath;
        }
    }
    
    /**
     * Gets the prefix from a full filename, such as <code>C:/</code>
     * or <code>~/</code>.
     * <p>
     * This method will handle a file in either Unix or Windows format.
     * The prefix includes the first slash in the full filename where applicable.
     * <pre>
     * Windows:
     * a\b\c.txt           --> ""          --> relative
     * \a\b\c.txt          --> "\"         --> current drive absolute
     * C:a\b\c.txt         --> "C:"        --> drive relative
     * C:\a\b\c.txt        --> "C:\"       --> absolute
     * \\server\a\b\c.txt  --> "\\server\" --> UNC
     *
     * Unix:
     * a/b/c.txt           --> ""          --> relative
     * /a/b/c.txt          --> "/"         --> absolute
     * ~/a/b/c.txt         --> "~/"        --> current user
     * ~                   --> "~/"        --> current user (slash added)
     * ~user/a/b/c.txt     --> "~user/"    --> named user
     * ~user               --> "~user/"    --> named user (slash added)
     * </pre>
     * <p>
     * The output will be the same irrespective of the machine that the code is running on.
     * ie. both Unix and Windows prefixes are matched regardless.
     *
     * @param filename  the filename to query, null returns null
     * @return the prefix of the file, null if invalid
     */
    public static String getPrefix(String filename) {
        if (filename == null) {
            return null;
        }
        int len = getPrefixLength(filename);
        if (len < 0) {
            return null;
        }
        if (len > filename.length()) {
            return filename + UNIX_SEPARATOR;  // we know this only happens for unix
        }
        return filename.substring(0, len);
    }
    
	public static String applyRelative(String parentPath, String relativePath) {
		if(null == parentPath) {
			return relativePath;
		}
		
		parentPath = Strings.replace(parentPath, WINDOWS_SEPARATOR, UNIX_SEPARATOR);
		
		int separatorIndex = parentPath.lastIndexOf(Paths.UNIX_SEPARATOR_STR);
		if (separatorIndex != -1) {
			String newPath = parentPath.substring(0, separatorIndex);
			if (!relativePath.startsWith(Paths.UNIX_SEPARATOR_STR)) {
				newPath += Paths.UNIX_SEPARATOR_STR;
			}
			return newPath + relativePath;
		}
		else {
			return relativePath;
		}
	}
	
    /**
     * Converts the path provided to a slash-leading form, no matter what is provided.
     */
    public static String prefixWithSlash(final String path) {
        return null == path ? null : !Strings.startsWith(path, "/") ? "/" + path : path;
    }
    
    public static String prefixWithoutSlash(final String path){
    	return null == path ? null : Strings.startsWith(path, "/") ? path.substring(1) : path;
    }
    
    public static String prefixAndSuffixWithSlash(String path){
    	if(Strings.isEmpty(path)){
    		return path;
    	}
    	
    	if(path.startsWith("/")){
    		return suffixWithSlash(path);
    	}else{
    		return "/" + suffixWithSlash(path);
    	}
    }

    public static String prefixAndSuffixWithoutSlash(String path){
        if(Strings.isEmpty(path)){
            return path;
        }

        return suffixWithoutSlash(prefixWithoutSlash(path));
    }
    
    public static String prefixWithAndSuffixWithoutSlash(String path) {
    	if(Strings.isEmpty(path)){
    		return path;
    	}
    	
    	if(path.startsWith("/")){
    		return suffixWithoutSlash(path);
    	}else{
    		return "/" + suffixWithoutSlash(path);
    	}
    }
    
    public static String prefixWithoutAndSuffixWithSlash(String path){
    	if(Strings.isEmpty(path)){
    		return path;
    	}
    	
    	if(path.endsWith("/")){
    		return prefixWithoutSlash(path);
    	}else{
    		return prefixWithoutSlash(path) + "/";
    	}
    }
    
    /**
     * Converts the path provided to a slash-ending form, no matter what is provided.
     */
    public static String suffixWithSlash(final String path){
    	return !Strings.endsWith(path, "/") ? path + "/" : path;
    }
    
    public static String suffixWithoutSlash(final String path){
    	return Strings.endsWith(path, "/") ? path.substring(0, path.length() - 1) : path;
    }
    
    public static boolean isExplicitRelativePath(String path) {
    	return null != path && (path.startsWith("./") || path.startsWith("../") || 
    						    path.startsWith(".\\") || path.startsWith("..\\"));
    }
    
    /**
     * Does the work of getting the path.
     * 
     * @param filename  the filename
     * @param separatorAdd  0 to omit the end separator, 1 to return it
     * @return the path
     */
    private static String doGetPath(String filename, int separatorAdd) {
        if (filename == null) {
            return null;
        }
        int prefix = getPrefixLength(filename);
        if (prefix < 0) {
            return null;
        }
        int index = indexOfLastSeparator(filename);
        int endIndex = index+separatorAdd;
        if (prefix >= filename.length() || index < 0 || prefix >= endIndex) {
            return "";
        }
        return filename.substring(prefix, endIndex);
    }    
	
    /**
     * Does the work of getting the path.
     * 
     * @param filename  the filename
     * @param includeSeparator  true to include the end separator
     * @return the path
     */
    private static String doGetFullPath(String filename, boolean includeSeparator) {
        if (filename == null) {
            return null;
        }
        int prefix = getPrefixLength(filename);
        if (prefix < 0) {
            return null;
        }
        if (prefix >= filename.length()) {
            if (includeSeparator) {
                return getPrefix(filename);  // add end slash if necessary
            } else {
                return filename;
            }
        }
        int index = indexOfLastSeparator(filename);
        if (index < 0) {
            return filename.substring(0, prefix);
        }
        int end = index + (includeSeparator ?  1 : 0);
        if (end == 0) {
            end++;
        }
        return filename.substring(0, end);
    }
    
    /**
     * Returns the length of the filename prefix, such as <code>C:/</code> or <code>~/</code>.
     * <p>
     * This method will handle a file in either Unix or Windows format.
     * <p>
     * The prefix length includes the first slash in the full filename
     * if applicable. Thus, it is possible that the length returned is greater
     * than the length of the input string.
     * <pre>
     * Windows:
     * a\b\c.txt           --> ""          --> relative
     * \a\b\c.txt          --> "\"         --> current drive absolute
     * C:a\b\c.txt         --> "C:"        --> drive relative
     * C:\a\b\c.txt        --> "C:\"       --> absolute
     * \\server\a\b\c.txt  --> "\\server\" --> UNC
     *
     * Unix:
     * a/b/c.txt           --> ""          --> relative
     * /a/b/c.txt          --> "/"         --> absolute
     * ~/a/b/c.txt         --> "~/"        --> current user
     * ~                   --> "~/"        --> current user (slash added)
     * ~user/a/b/c.txt     --> "~user/"    --> named user
     * ~user               --> "~user/"    --> named user (slash added)
     * </pre>
     * <p>
     * The output will be the same irrespective of the machine that the code is running on.
     * ie. both Unix and Windows prefixes are matched regardless.
     *
     * @param filename  the filename to find the prefix in, null returns -1
     * @return the length of the prefix, -1 if invalid or null
     */
    static int getPrefixLength(String filename) {
        if (filename == null) {
            return -1;
        }
        int len = filename.length();
        if (len == 0) {
            return 0;
        }
        char ch0 = filename.charAt(0);
        if (ch0 == ':') {
            return -1;
        }
        if (len == 1) {
            if (ch0 == '~') {
                return 2;  // return a length greater than the input
            }
            return isSeparator(ch0) ? 1 : 0;
        } else {
            if (ch0 == '~') {
                int posUnix = filename.indexOf(UNIX_SEPARATOR, 1);
                int posWin = filename.indexOf(WINDOWS_SEPARATOR, 1);
                if (posUnix == -1 && posWin == -1) {
                    return len + 1;  // return a length greater than the input
                }
                posUnix = posUnix == -1 ? posWin : posUnix;
                posWin = posWin == -1 ? posUnix : posWin;
                return Math.min(posUnix, posWin) + 1;
            }
            char ch1 = filename.charAt(1);
            if (ch1 == ':') {
                ch0 = Character.toUpperCase(ch0);
                if (ch0 >= 'A' && ch0 <= 'Z') {
                    if (len == 2 || isSeparator(filename.charAt(2)) == false) {
                        return 2;
                    }
                    return 3;
                }
                return -1;
                
            } else if (isSeparator(ch0) && isSeparator(ch1)) {
                int posUnix = filename.indexOf(UNIX_SEPARATOR, 2);
                int posWin = filename.indexOf(WINDOWS_SEPARATOR, 2);
                if (posUnix == -1 && posWin == -1 || posUnix == 2 || posWin == 2) {
                    return -1;
                }
                posUnix = posUnix == -1 ? posWin : posUnix;
                posWin = posWin == -1 ? posUnix : posWin;
                return Math.min(posUnix, posWin) + 1;
            } else {
                return isSeparator(ch0) ? 1 : 0;
            }
        }
    }    
	
	/**
	 * Returns the index of the last directory separator character.
	 * <p>
	 * This method will handle a file in either Unix or Windows format. The position of the last forward or backslash is
	 * returned.
	 * <p>
	 * The output will be the same irrespective of the machine that the code is running on.
	 * 
	 * @param filename the filename to find the last path separator in, null returns -1
	 * @return the index of the last separator character, or -1 if there is no such character
	 */
    public static int indexOfLastSeparator(String filename) {
        if (filename == null) {
            return -1;
        }
        int lastUnixPos = filename.lastIndexOf(UNIX_SEPARATOR);
        int lastWindowsPos = filename.lastIndexOf(WINDOWS_SEPARATOR);
        return Math.max(lastUnixPos, lastWindowsPos);
    }

    /**
     * Determines if Windows file system is in use.
     * 
     * @return true if the system is Windows
     */
    public static boolean isSystemWindows() {
        return SYSTEM_SEPARATOR == WINDOWS_SEPARATOR;
    }
    
    /**
     * Checks if the character is a separator.
     * 
     * @param ch  the character to check
     * @return true if it is a separator character
     */
    public static boolean isSeparator(char ch) {
        return ch == UNIX_SEPARATOR || ch == WINDOWS_SEPARATOR;
    }
}
