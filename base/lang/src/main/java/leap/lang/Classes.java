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

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import leap.lang.exception.NestedClassNotFoundException;
import leap.lang.io.IO;

public class Classes {
	
	//Maps a primitive class name to its corresponding abbreviation used in array class names.
	private static final Map<String, String> abbreviationMap = new HashMap<String, String>();
	
	//Maps an abbreviation used in array class names to corresponding primitive class name.
	private static final Map<String, String> reverseAbbreviationMap = new HashMap<String, String>();
	
	static {
		addAbbreviation("int", "I");
		addAbbreviation("boolean", "Z");
		addAbbreviation("float", "F");
		addAbbreviation("long", "J");
		addAbbreviation("short", "S");
		addAbbreviation("byte", "B");
		addAbbreviation("double", "D");
		addAbbreviation("char", "C");
	}	
	
	public static final Annotation[] EMPTY_ANNOTATION_ARRAY = new Annotation[]{};
	
	public static final String CLASS_FILE_SUFFIX          = ".class";
	public static final char   PACKAGE_SEPARATOR_CHAR     = '.';
	public static final String PACKAGE_SEPARATOR          = ".";
	public static final char   INNER_CLASS_SEPARATOR_CHAR = '$';	
	
	/**
	 * Returns <code>true</code> if the given class name is present.
	 */
	public static boolean isPresent(String className) {
		return tryForName(className) != null;
	}
	
	/**
	 * Returns <code>true</code> if the given class name is present.
	 */
	public static boolean isPresent(ClassLoader classLoader,String className) {
		return tryForName(classLoader,className) != null;
	}
	
	/**
	 * Returns the (initialized) class represented by {@code className} using the current thread's context class loader.
	 * 
	 * @throws NestedClassNotFoundException if the class is not found
	 */
	public static Class<?> forName(String className) throws NestedClassNotFoundException {
		return forName(getClassLoader(),className,true);
	}
	
	/**
	 * Returns the (initialized) class represented by {@code className} using the current thread's context class loader.
	 * 
	 * <p/>
	 * 
	 * returns {@code null} if the class is not found
	 */
	public static Class<?> tryForName(String className) {
		try {
	        return forName(getClassLoader(),className,true);
        } catch (NestedClassNotFoundException e) {
        	return null;
        }
	}
	
	/**
	 * Returns the (initialized) class represented by {@code className} using the given class loader.
	 * 
	 * @throws NestedClassNotFoundException if the class is not found
	 */
	public static Class<?> forName(ClassLoader classLoader, String className) throws NestedClassNotFoundException {
		return forName(classLoader, className, true);
	}
	
	/**
	 * Returns the (initialized) class represented by {@code className} using the given class loader.
	 * 
	 * <p/>
	 * 
	 * returns {@code null} if the class is not found
	 */
	public static Class<?> tryForName(ClassLoader classLoader, String className) {
		try {
	        return forName(classLoader, className, true);
        } catch (NestedClassNotFoundException e) {
        	return null;
        }
	}	
	
	/**
	 * Returns the (initialized) class represented by {@code className} using the given class's loader.
	 * 
	 * <p/>
	 * 
	 * returns {@code null} if the class is not found
	 */	
	public static Class<?> tryForName(Class<?> loaderClass,String className) {
		return tryForName(getClassLoader(loaderClass),className);
	}
	
	/**
	 * Returns the (initialized) class represented by {@code className} using the given class's loader.
	 * 
	 * @throws NestedClassNotFoundException if the class is not found
	 */	
	public static Class<?> forName(Class<?> loaderClass,String className) throws NestedClassNotFoundException {
		return forName(getClassLoader(loaderClass),className);
	}
	
	/**
	 * returns <code>null</code> if the given class is not a primitive type.
	 * 
	 * <p/>
	 * 
	 * returns the not null default value if the given class is a primitive type.
	 */
	public static Object getDefaultValue(Class<?> type){
        if(Integer.TYPE == type){
            return 0;
        }
        
        if(Boolean.TYPE == type){
            return false;
        }
        
        if(Long.TYPE == type){
            return 0L;
        }
        
        if(Float.TYPE == type){
            return 0.0f;
        }
        
        if(Double.TYPE == type){
            return 0.0d;
        }
        
        if(Short.TYPE == type){
            return 0;
        }
        
        if(Byte.TYPE == type){
        	return 0;
        }
        
        if(Character.TYPE == type){
        	return '\u0000';
        }
        
        return null;
	}

	public static ClassLoader getClassLoader() {
		ClassLoader cl = null;
		try {
			cl = Thread.currentThread().getContextClassLoader();
		} catch (Throwable ex) {
			// Cannot access thread context ClassLoader - falling back to system class loader...
		}
		if (cl == null) {
			// No thread context class loader -> use class loader of this class.
			cl = Classes.class.getClassLoader();
		}
		return cl;
	}

	public static ClassLoader getClassLoader(Class<?> clazz) {
		ClassLoader cl = null;
		try {
			cl = Thread.currentThread().getContextClassLoader();
		} catch (Throwable ex) {
			// Cannot access thread context ClassLoader - falling back to system class loader...
		}
		if (cl == null) {
			// No thread context class loader -> use class loader of this class.
			cl = clazz.getClassLoader();
		}
		return cl;
	}
	
	public static String getResourceAsString(Class<?> loaderClass,String resource){
		return getResourceAsString(loaderClass, resource, Charsets.defaultCharset());
	}	
	
	public static String getResourceAsString(Class<?> loaderClass,String resource,Charset charset){
		InputStream is = null;
		try{
			is = loaderClass.getResourceAsStream(resource);
			
			if(null == is){
				return null;
			}
			
			return IO.readString(is,charset);
		}finally{
			IO.close(is);
		}
	}
	
	/**
	 * Determine the name of the package of the given class,
	 * e.g. "java.lang" for the {@code java.lang.String} class.
	 * @param clazz the class
	 * @return the package name, or the empty String if the class
	 * is defined in the default package
	 */
	public static String getPackageName(Class<?> clazz) {
		Args.notNull(clazz, "Class must not be null");
		return getPackageName(clazz.getName());
	}

	/**
	 * Determine the name of the package of the given fully-qualified class name,
	 * e.g. "java.lang" for the {@code java.lang.String} class name.
	 * @param fqClassName the fully-qualified class name
	 * @return the package name, or the empty String if the class
	 * is defined in the default package
	 */
	public static String getPackageName(String fqClassName) {
		Args.notNull(fqClassName, "Class name must not be null");
		int lastDotIndex = fqClassName.lastIndexOf(PACKAGE_SEPARATOR);
		return (lastDotIndex != -1 ? fqClassName.substring(0, lastDotIndex) : "");
	}
	
	/**
	 * get the resource path of class object's package.
	 * 
	 * <p/>
	 * 
	 * a resource path can be used as the input argument of {@link ClassLoader#getResource(String)}.
	 * 
	 * <p/>
	 * 
	 * e.g. all foo.bar.A.class -> foo/bar
	 * 
	 * @return return "" if clazz is null or no package, else return the resource path
	 */
	public static String getPackageResourcePath(Class<?> clazz){
		if (clazz == null) {
			return "";
		}
		String className = clazz.getName();
		int packageEndIndex = className.lastIndexOf('.');
		if (packageEndIndex == -1) {
			return "";
		}
		String packageName = className.substring(0, packageEndIndex);
		return packageName.replace('.', '/');
	}
	
	/**
	 * get the resource path of class object.
	 * 
	 * <p/>
	 * 
	 * a resource path can be used as the input argument of {@link ClassLoader#getResource(String)}.
	 * 
	 * <p/>
	 * 
	 * e.g. all foo.bar.A.class -> foo/bar/A.class
	 */
	public static String getClassResourcePath(Class<?> clazz){
		return clazz.getName().replace('.','/') + CLASS_FILE_SUFFIX;
	}
	
	/**
	 * get the file name of the class file relative to the class object's package.
	 * 
	 * <p/>
	 * 
	 * e.g java.lang.String.class -> String.class
	 */
	public static String getClassFileName(Class<?> clazz) {
		Args.notNull(clazz, "Class");
		String className = clazz.getName();
		int lastDotIndex = className.lastIndexOf(".");
		return className.substring(lastDotIndex + 1) + CLASS_FILE_SUFFIX;
	}
	
	/**
	 * <p>
	 * Gets the class name minus the package name from a {@code Class}.
	 * </p>
	 * 
	 * <p>
	 * Consider using the Java 5 API {@link Class#getSimpleName()} instead. The one known difference is that this code
	 * will return {@code "Map.Entry"} while the {@code java.lang.Class} variant will simply return {@code "Entry"}.
	 * </p>
	 * 
	 * @param cls the class to get the short name for.
	 * 
	 * @return the class name without the package name or an empty string
	 */
	//from apache commons lang
	public static String getShortName(Class<?> cls) {
		if (cls == null) {
			return Strings.EMPTY;
		}
		return getShortName(cls.getName());
	}	
	
	/**
	 * <p>
	 * Gets the class name minus the package name from a String.
	 * </p>
	 * 
	 * <p>
	 * The string passed in is assumed to be a class name - it is not checked.
	 * </p>
	 * 
	 * <p>
	 * Note that this method differs from Class.getSimpleName() in that this will return {@code "Map.Entry"} whilst the
	 * {@code java.lang.Class} variant will simply return {@code "Entry"}.
	 * </p>
	 * 
	 * @param className the className to get the short name for
	 * @return the class name of the class without the package name or an empty string
	 */
	//from apache commons lang
	public static String getShortName(String className) {
		if (className == null) {
			return Strings.EMPTY;
		}
		if (className.length() == 0) {
			return Strings.EMPTY;
		}

		StringBuilder arrayPrefix = new StringBuilder();

		// Handle array encoding
		if (className.startsWith("[")) {
			while (className.charAt(0) == '[') {
				className = className.substring(1);
				arrayPrefix.append("[]");
			}
			// Strip Object type encoding
			if (className.charAt(0) == 'L' && className.charAt(className.length() - 1) == ';') {
				className = className.substring(1, className.length() - 1);
			}
		}

		if (reverseAbbreviationMap.containsKey(className)) {
			className = reverseAbbreviationMap.get(className);
		}

		int lastDotIdx = className.lastIndexOf(PACKAGE_SEPARATOR_CHAR);
		int innerIdx = className.indexOf(INNER_CLASS_SEPARATOR_CHAR, lastDotIdx == -1 ? 0 : lastDotIdx + 1);
		String out = className.substring(lastDotIdx + 1);
		if (innerIdx != -1) {
			out = out.replace(INNER_CLASS_SEPARATOR_CHAR, PACKAGE_SEPARATOR_CHAR);
		}
		return out + arrayPrefix;
	}	
	
    public static <T extends Annotation> T getAnnotation(Annotation[] annotations,Class<T> annotationType){
		return getAnnotation(annotations, annotationType, false);
	}
	
	@SuppressWarnings("unchecked")
    public static <T extends Annotation> T getAnnotation(Annotation[] annotations,Class<T> annotationType, boolean meta){
		if(null == annotations || annotations.length == 0 || null == annotationType){
			return null;
		}
		
		for(int i=0;i<annotations.length;i++){
			Annotation a = annotations[i];
			
			if(a.annotationType().equals(annotationType)){
				return (T)a;
			}
			
			if(meta){
				Annotation metaAnnotation = a.annotationType().getAnnotation(annotationType);
				if(null != metaAnnotation){
					return (T)metaAnnotation;
				}
			}
		}
		
		return null;
	}
	
	public static Annotation getAnnotationByMetaType(Annotation[] annotations,Class<? extends Annotation> metaAnnotationType) {
		for(Annotation a : annotations){
			if(a.annotationType().isAnnotationPresent(metaAnnotationType)){
				return a;
			}
		}
		return null;
	}
	
	public static boolean isAnnotatioinPresent(Annotation[] annotations,Class<? extends Annotation> annotationType){
		return null != getAnnotation(annotations, annotationType);
	}
	
	public static boolean isAnnotatioinPresent(Annotation[] annotations,Class<? extends Annotation> annotationType, boolean meta){
		return null != getAnnotation(annotations, annotationType, meta);
	}
	
    /**
	 * <p>
	 * Checks if one {@code Class} can be assigned to a variable of another {@code Class}.
	 * </p>
	 * 
	 * <p>
	 * Unlike the {@link Class#isAssignableFrom(java.lang.Class)} method, this method takes into account widenings of
	 * primitive classes and {@code null}s.
	 * </p>
	 * 
	 * <p>
	 * Primitive widenings allow an int to be assigned to a long, float or double. This method returns the correct
	 * result for these cases.
	 * </p>
	 * 
	 * <p>
	 * {@code Null} may be assigned to any reference type. This method will return {@code true} if {@code null} is
	 * passed in and the toClass is non-primitive.
	 * </p>
	 * 
	 * <p>
	 * Specifically, this method tests whether the type represented by the specified {@code Class} parameter can be
	 * converted to the type represented by this {@code Class} object via an identity conversion widening primitive or
	 * widening reference conversion. See
	 * <em><a href="http://java.sun.com/docs/books/jls/">The Java Language Specification</a></em>, sections 5.1.1, 5.1.2
	 * and 5.1.4 for details.
	 * </p>
	 * 
	 * @param cls the Class to check, may be null
	 * @param toClass the Class to try to assign into, returns false if null
	 * @return {@code true} if assignment possible
	 */
    public static boolean isAssignable(Class<?> cls, Class<?> toClass) {
		if (toClass == null) {
			return false;
		}
		// have to check for null, as isAssignableFrom doesn't
		if (cls == null) {
			return !toClass.isPrimitive();
		}
		//autoboxing:
		if (cls.isPrimitive() && !toClass.isPrimitive()) {
			cls = Primitives.wrap(cls);
			if (cls == null) {
				return false;
			}
		}
		if (toClass.isPrimitive() && !cls.isPrimitive()) {
			cls = Primitives.unwrap(cls);
			if (cls == null) {
				return false;
			}
		}
		if (cls.equals(toClass)) {
			return true;
		}
		if (cls.isPrimitive()) {
			if (toClass.isPrimitive() == false) {
				return false;
			}
			if (Integer.TYPE.equals(cls)) {
				return Long.TYPE.equals(toClass) || Float.TYPE.equals(toClass) || Double.TYPE.equals(toClass);
			}
			if (Long.TYPE.equals(cls)) {
				return Float.TYPE.equals(toClass) || Double.TYPE.equals(toClass);
			}
			if (Boolean.TYPE.equals(cls)) {
				return false;
			}
			if (Double.TYPE.equals(cls)) {
				return false;
			}
			if (Float.TYPE.equals(cls)) {
				return Double.TYPE.equals(toClass);
			}
			if (Character.TYPE.equals(cls)) {
				return Integer.TYPE.equals(toClass) || Long.TYPE.equals(toClass) || Float.TYPE.equals(toClass) || Double.TYPE.equals(toClass);
			}
			if (Short.TYPE.equals(cls)) {
				return Integer.TYPE.equals(toClass) || Long.TYPE.equals(toClass) || Float.TYPE.equals(toClass) || Double.TYPE.equals(toClass);
			}
			if (Byte.TYPE.equals(cls)) {
				return Short.TYPE.equals(toClass) || Integer.TYPE.equals(toClass) || Long.TYPE.equals(toClass) || Float.TYPE.equals(toClass)
				        || Double.TYPE.equals(toClass);
			}
			// should never get here
			return false;
		}
		return toClass.isAssignableFrom(cls);
    }		
    
    /**
     * <p>Is the specified class an inner class or static nested class.</p>
     */
    public static boolean isInnerClass(Class<?> clazz) {
        return clazz != null && clazz.getEnclosingClass() != null;
    }
    
	/**
	 * Checks if given class is a concrete one; that is, not an interface or abstract class.
	 */
	public static boolean isConcreteClass(Class<?> clazz) {
		if(null == clazz){
			return false;
		}
		return ! (Modifier.isInterface(clazz.getModifiers()) || Modifier.isAbstract(clazz.getModifiers())); 
	}    
	
	/**
	 * Check if the given class represents a primitive (i.e. boolean, byte,
	 * char, short, int, long, float, or double) or a primitive wrapper
	 * (i.e. Boolean, Byte, Character, Short, Integer, Long, Float, or Double).
	 * @param clazz the class to check
	 * @return whether the given class is a primitive or primitive wrapper class
	 */
	public static boolean isPrimitiveOrWrapper(Class<?> clazz) {
		if(null == clazz){
			return false;
		}
		return (clazz.isPrimitive() || Primitives.isWrapperType(clazz));
	}
	
	/**
	 * Check if the given type represents a "simple" value type:
	 * a primitive, a String or other CharSequence, a Number, a Date, a Caleandar, a Charset, 
	 * a URI, a URL, a Locale or a Class.
	 * @param clazz the type to check
	 * @return whether the given type represents a "simple" value type
	 */
	public static boolean isSimpleValueType(Class<?> clazz) {
		if(null == clazz){
			return false;
		}
		return isPrimitiveOrWrapper(clazz) || clazz.isEnum() ||
			   CharSequence.class.isAssignableFrom(clazz) ||
			   Number.class.isAssignableFrom(clazz) ||
			   Date.class.isAssignableFrom(clazz) ||
			   clazz.equals(Calendar.class) || 
			   clazz.equals(Charset.class) || 
			   clazz.equals(URI.class) || clazz.equals(URL.class) ||
			   clazz.equals(Locale.class) || clazz.equals(Class.class);
	}
    
	public static boolean isVoid(Class<?> clazz){
		return null != clazz && (Void.TYPE.equals(clazz) || Void.class.equals(clazz));
	}
	
	public static boolean isString(Class<?> clazz){
		return null == clazz ? false : clazz.equals(String.class);
	}
	
	public static boolean isBoolean(Class<?> clazz){
		return null == clazz ? false : clazz.equals(Boolean.TYPE) || clazz.equals(Boolean.class);
	}
	
	public static boolean isDouble(Class<?> clazz){
		return null == clazz ? false : clazz.equals(Double.TYPE) || clazz.equals(Double.class);
	}
	
	public static boolean isInteger(Class<?> clazz){
		return null == clazz ? false : clazz.equals(Integer.TYPE) || clazz.equals(Integer.class);
	}
	
	public static boolean isLong(Class<?> clazz){
		return null == clazz ? false : clazz.equals(Long.TYPE) || clazz.equals(Long.class);
	}
	
	public static boolean isShort(Class<?> clazz){
		return null == clazz ? false : clazz.equals(Short.TYPE) || clazz.equals(Short.class);
	}
	
	public static boolean isFloat(Class<?> clazz){
		return null == clazz ? false : clazz.equals(Float.TYPE) || clazz.equals(Float.class);
	}
	
	public static boolean isBigDecimal(Class<?> clazz){
		return null == clazz ? false : clazz.equals(BigDecimal.class);
	}
	
	public static boolean isBigInteger(Class<?> clazz){
		return null == clazz ? false : clazz.equals(BigInteger.class);
	}
	
	public static boolean isCharacter(Class<?> clazz){
		return null == clazz ? false : clazz.equals(Character.TYPE) || clazz.equals(Character.class);
	}    
	
	//--------------------private methods---------------------------------------------------------------------------------
	
	/**
	 * Returns the class represented by {@code className} using the {@code classLoader}. 
	 * 
	 * <p/>
	 * 
	 * This implementation supports the syntaxes : 
	 * 
	 * <pre>
	 * "{@code int}",
	 * "{@code java.util.Map.Entry[]}", 
	 * "{@code java.util.Map$Entry[]}",
	 * "{@code [Ljava.util.Map.Entry;}", 
	 * "{@code [Ljava.util.Map$Entry;}".
	 * </pre>
	 * 
	 * @throws NestedClassNotFoundException if the class is not found
	 */
	private static Class<?> forName(ClassLoader classLoader, String className, boolean initialize) throws NestedClassNotFoundException {
		try {
			Class<?> clazz;
			if (abbreviationMap.containsKey(className)) {
				String clsName = "[" + abbreviationMap.get(className);
				clazz = Class.forName(clsName, initialize, classLoader).getComponentType();
			} else {
				clazz = Class.forName(toCanonicalName(className), initialize, classLoader);
			}
			return clazz;
		} catch (NoClassDefFoundError|ClassNotFoundException ex) {
			// allow path separators (.) as inner class name separators
			int lastDotIndex = className.lastIndexOf(PACKAGE_SEPARATOR_CHAR);

			if (lastDotIndex != -1) {
				try {
					return forName(classLoader, className.substring(0, lastDotIndex) + INNER_CLASS_SEPARATOR_CHAR
					        + className.substring(lastDotIndex + 1), initialize);
				} catch (NestedClassNotFoundException ex2) { // NOPMD
					// ignore exception
				}
			}
			
			throw new NestedClassNotFoundException("Class not found : " + ex.getMessage(),ex);
		}
	}	
	
	/**
	 * Converts a class name to a JLS style class name.
	 */
	private static String toCanonicalName(String className) {
		className = Strings.trim(className);

		if (className.endsWith("[]")) {
			StringBuilder classNameBuffer = new StringBuilder();
			while (className.endsWith("[]")) {
				className = className.substring(0, className.length() - 2);
				classNameBuffer.append("[");
			}
			String abbreviation = abbreviationMap.get(className);
			if (abbreviation != null) {
				classNameBuffer.append(abbreviation);
			} else {
				classNameBuffer.append("L").append(className).append(";");
			}
			className = classNameBuffer.toString();
		}
		return className;
	}	
	
	/**
	 * Add primitive type abbreviation to maps of abbreviations.
	 */
	private static void addAbbreviation(String primitive, String abbreviation) {
		abbreviationMap.put(primitive, abbreviation);
		reverseAbbreviationMap.put(abbreviation, primitive);
	}
	
	protected Classes(){
		
	}
}
