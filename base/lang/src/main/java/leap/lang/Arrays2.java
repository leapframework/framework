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

import leap.lang.collection.ArrayIterable;
import leap.lang.collection.EmptyIterable;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Predicate;

/**
 * A <code>null</code> safe util class for array object.
 */
public class Arrays2 {
	public static final Object[]	EMPTY_OBJECT_ARRAY	       = new Object[0];
	public static final Class<?>[]	EMPTY_CLASS_ARRAY	       = new Class[0];
	public static final String[]	EMPTY_STRING_ARRAY	       = new String[0];
	public static final long[]	    EMPTY_LONG_ARRAY	       = new long[0];
	public static final Long[]	    EMPTY_LONG_OBJECT_ARRAY	   = new Long[0];
	public static final int[]	    EMPTY_INT_ARRAY	           = new int[0];
	public static final Integer[]	EMPTY_INT_OBJECT_ARRAY	   = new Integer[0];
	public static final short[]	    EMPTY_SHORT_ARRAY	       = new short[0];
	public static final Short[]	    EMPTY_SHORT_OBJECT_ARRAY   = new Short[0];
	public static final byte[]	    EMPTY_BYTE_ARRAY	       = new byte[0];
	public static final Byte[]	    EMPTY_BYTE_OBJECT_ARRAY	   = new Byte[0];
	public static final double[]	EMPTY_DOUBLE_ARRAY	       = new double[0];
	public static final Double[]	EMPTY_DOUBLE_OBJECT_ARRAY  = new Double[0];
	public static final float[]	    EMPTY_FLOAT_ARRAY	       = new float[0];
	public static final Float[]	    EMPTY_FLOAT_OBJECT_ARRAY   = new Float[0];
	public static final boolean[]	EMPTY_BOOLEAN_ARRAY	       = new boolean[0];
	public static final Boolean[]	EMPTY_BOOLEAN_OBJECT_ARRAY = new Boolean[0];
	public static final char[]	    EMPTY_CHAR_ARRAY	       = new char[0];
	public static final Character[]	EMPTY_CHAR_OBJECT_ARRAY	   = new Character[0];

	public static final int	        INDEX_NOT_FOUND	           = -1;
	
	@SuppressWarnings("unchecked")
    public static <T> T[] empty(){
		return (T[])EMPTY_OBJECT_ARRAY;
	}

	public static boolean isEmpty(Object[] array) {
		return (array == null || array.length == 0);
	}

	public static boolean isEmpty(long[] array) {
		return array == null || array.length == 0;
	}

	public static boolean isEmpty(int[] array) {
		return array == null || array.length == 0;
	}

	public static boolean isEmpty(short[] array) {
		return array == null || array.length == 0;
	}

	public static boolean isEmpty(char[] array) {
		return array == null || array.length == 0;
	}

	public static boolean isEmpty(byte[] array) {
		return array == null || array.length == 0;
	}

	public static boolean isEmpty(double[] array) {
		return array == null || array.length == 0;
	}

	public static boolean isEmpty(float[] array) {
		return array == null || array.length == 0;
	}

	public static boolean isEmpty(boolean[] array) {
		return array == null || array.length == 0;
	}

	public static <T> boolean isNotEmpty(T[] array) {
		return (array != null && array.length != 0);
	}

	public static boolean isNotEmpty(long[] array) {
		return (array != null && array.length != 0);
	}

	public static boolean isNotEmpty(int[] array) {
		return (array != null && array.length != 0);
	}

	public static boolean isNotEmpty(short[] array) {
		return (array != null && array.length != 0);
	}

	public static boolean isNotEmpty(char[] array) {
		return (array != null && array.length != 0);
	}

	public static boolean isNotEmpty(byte[] array) {
		return (array != null && array.length != 0);
	}

	public static boolean isNotEmpty(double[] array) {
		return (array != null && array.length != 0);
	}

	public static boolean isNotEmpty(float[] array) {
		return (array != null && array.length != 0);
	}

	public static boolean isNotEmpty(boolean[] array) {
		return (array != null && array.length != 0);
	}

	public static int indexOf(Object[] array, Object objectToFind) {
		return indexOf(array, objectToFind, 0);
	}

	public static int indexOf(Object[] array, Object objectToFind, int startIndex) {
		if (array == null) {
			return INDEX_NOT_FOUND;
		}
		if (startIndex < 0) {
			startIndex = 0;
		}
		if (objectToFind == null) {
			for (int i = startIndex; i < array.length; i++) {
				if (array[i] == null) {
					return i;
				}
			}
		} else if (array.getClass().getComponentType().isInstance(objectToFind)) {
			for (int i = startIndex; i < array.length; i++) {
				if (objectToFind.equals(array[i])) {
					return i;
				}
			}
		}
		return INDEX_NOT_FOUND;
	}
	
	public static int indexOfObjectArray(Object array,Object objectToFind){
		return indexOfObjectArray(array, objectToFind, 0);
	}
	
	public static int indexOfObjectArray(Object array,Object objectToFind, int startIndex){
		if (array == null) {
			return INDEX_NOT_FOUND;
		}
		if (startIndex < 0) {
			startIndex = 0;
		}
		if (objectToFind == null) {
			for (int i = startIndex; i < Array.getLength(array); i++) {
				if (Array.get(array, i) == null) {
					return i;
				}
			}
		} else if (Primitives.wrap(array.getClass().getComponentType()).isInstance(objectToFind)) {
			for (int i = startIndex; i < Array.getLength(array); i++) {
				if (objectToFind.equals(Array.get(array, i))) {
					return i;
				}
			}
		}
		return INDEX_NOT_FOUND;
	}

	public static int lastIndexOf(Object[] array, Object objectToFind) {
		return lastIndexOf(array, objectToFind, Integer.MAX_VALUE);
	}

	public static int lastIndexOf(Object[] array, Object objectToFind, int startIndex) {
		if (array == null) {
			return INDEX_NOT_FOUND;
		}
		if (startIndex < 0) {
			return INDEX_NOT_FOUND;
		} else if (startIndex >= array.length) {
			startIndex = array.length - 1;
		}
		if (objectToFind == null) {
			for (int i = startIndex; i >= 0; i--) {
				if (array[i] == null) {
					return i;
				}
			}
		} else if (array.getClass().getComponentType().isInstance(objectToFind)) {
			for (int i = startIndex; i >= 0; i--) {
				if (objectToFind.equals(array[i])) {
					return i;
				}
			}
		}
		return INDEX_NOT_FOUND;
	}

	public static boolean contains(Object[] array, Object objectToFind) {
		return indexOf(array, objectToFind) != INDEX_NOT_FOUND;
	}
	
	public static boolean containsInObjectArray(Object array, Object objectToFind) {
		return indexOfObjectArray(array, objectToFind) != INDEX_NOT_FOUND;
	}

	public static boolean containsAny(String[] array, String... items) {
        if(null == array) {
            return false;
        }
        for(String item : items) {
            for(String itemInArray : array) {
                if(Strings.equals(item, itemInArray)) {
                    return true;
                }
            }
        }
        return false;
    }

	public static int indexOf(long[] array, long valueToFind) {
		return indexOf(array, valueToFind, 0);
	}

	public static int indexOf(long[] array, long valueToFind, int startIndex) {
		if (array == null) {
			return INDEX_NOT_FOUND;
		}
		if (startIndex < 0) {
			startIndex = 0;
		}
		for (int i = startIndex; i < array.length; i++) {
			if (valueToFind == array[i]) {
				return i;
			}
		}
		return INDEX_NOT_FOUND;
	}

	public static int lastIndexOf(long[] array, long valueToFind) {
		return lastIndexOf(array, valueToFind, Integer.MAX_VALUE);
	}

	public static int lastIndexOf(long[] array, long valueToFind, int startIndex) {
		if (array == null) {
			return INDEX_NOT_FOUND;
		}
		if (startIndex < 0) {
			return INDEX_NOT_FOUND;
		} else if (startIndex >= array.length) {
			startIndex = array.length - 1;
		}
		for (int i = startIndex; i >= 0; i--) {
			if (valueToFind == array[i]) {
				return i;
			}
		}
		return INDEX_NOT_FOUND;
	}

	public static boolean contains(long[] array, long valueToFind) {
		return indexOf(array, valueToFind) != INDEX_NOT_FOUND;
	}

	public static int indexOf(int[] array, int valueToFind) {
		return indexOf(array, valueToFind, 0);
	}

	public static int indexOf(int[] array, int valueToFind, int startIndex) {
		if (array == null) {
			return INDEX_NOT_FOUND;
		}
		if (startIndex < 0) {
			startIndex = 0;
		}
		for (int i = startIndex; i < array.length; i++) {
			if (valueToFind == array[i]) {
				return i;
			}
		}
		return INDEX_NOT_FOUND;
	}

	public static int lastIndexOf(int[] array, int valueToFind) {
		return lastIndexOf(array, valueToFind, Integer.MAX_VALUE);
	}

	public static int lastIndexOf(int[] array, int valueToFind, int startIndex) {
		if (array == null) {
			return INDEX_NOT_FOUND;
		}
		if (startIndex < 0) {
			return INDEX_NOT_FOUND;
		} else if (startIndex >= array.length) {
			startIndex = array.length - 1;
		}
		for (int i = startIndex; i >= 0; i--) {
			if (valueToFind == array[i]) {
				return i;
			}
		}
		return INDEX_NOT_FOUND;
	}

	public static boolean contains(int[] array, int valueToFind) {
		return indexOf(array, valueToFind) != INDEX_NOT_FOUND;
	}

	public static int indexOf(short[] array, short valueToFind) {
		return indexOf(array, valueToFind, 0);
	}

	public static int indexOf(short[] array, short valueToFind, int startIndex) {
		if (array == null) {
			return INDEX_NOT_FOUND;
		}
		if (startIndex < 0) {
			startIndex = 0;
		}
		for (int i = startIndex; i < array.length; i++) {
			if (valueToFind == array[i]) {
				return i;
			}
		}
		return INDEX_NOT_FOUND;
	}

	public static int lastIndexOf(short[] array, short valueToFind) {
		return lastIndexOf(array, valueToFind, Integer.MAX_VALUE);
	}

	public static int lastIndexOf(short[] array, short valueToFind, int startIndex) {
		if (array == null) {
			return INDEX_NOT_FOUND;
		}
		if (startIndex < 0) {
			return INDEX_NOT_FOUND;
		} else if (startIndex >= array.length) {
			startIndex = array.length - 1;
		}
		for (int i = startIndex; i >= 0; i--) {
			if (valueToFind == array[i]) {
				return i;
			}
		}
		return INDEX_NOT_FOUND;
	}

	public static boolean contains(short[] array, short valueToFind) {
		return indexOf(array, valueToFind) != INDEX_NOT_FOUND;
	}

	public static int indexOf(char[] array, char valueToFind) {
		return indexOf(array, valueToFind, 0);
	}

	public static int indexOf(char[] array, char valueToFind, int startIndex) {
		if (array == null) {
			return INDEX_NOT_FOUND;
		}
		if (startIndex < 0) {
			startIndex = 0;
		}
		for (int i = startIndex; i < array.length; i++) {
			if (valueToFind == array[i]) {
				return i;
			}
		}
		return INDEX_NOT_FOUND;
	}

	public static int lastIndexOf(char[] array, char valueToFind) {
		return lastIndexOf(array, valueToFind, Integer.MAX_VALUE);
	}

	public static int lastIndexOf(char[] array, char valueToFind, int startIndex) {
		if (array == null) {
			return INDEX_NOT_FOUND;
		}
		if (startIndex < 0) {
			return INDEX_NOT_FOUND;
		} else if (startIndex >= array.length) {
			startIndex = array.length - 1;
		}
		for (int i = startIndex; i >= 0; i--) {
			if (valueToFind == array[i]) {
				return i;
			}
		}
		return INDEX_NOT_FOUND;
	}

	public static boolean contains(char[] array, char valueToFind) {
		return indexOf(array, valueToFind) != INDEX_NOT_FOUND;
	}

	public static int indexOf(byte[] array, byte valueToFind) {
		return indexOf(array, valueToFind, 0);
	}

	public static int indexOf(byte[] array, byte valueToFind, int startIndex) {
		if (array == null) {
			return INDEX_NOT_FOUND;
		}
		if (startIndex < 0) {
			startIndex = 0;
		}
		for (int i = startIndex; i < array.length; i++) {
			if (valueToFind == array[i]) {
				return i;
			}
		}
		return INDEX_NOT_FOUND;
	}

	public static int lastIndexOf(byte[] array, byte valueToFind) {
		return lastIndexOf(array, valueToFind, Integer.MAX_VALUE);
	}

	public static int lastIndexOf(byte[] array, byte valueToFind, int startIndex) {
		if (array == null) {
			return INDEX_NOT_FOUND;
		}
		if (startIndex < 0) {
			return INDEX_NOT_FOUND;
		} else if (startIndex >= array.length) {
			startIndex = array.length - 1;
		}
		for (int i = startIndex; i >= 0; i--) {
			if (valueToFind == array[i]) {
				return i;
			}
		}
		return INDEX_NOT_FOUND;
	}

	public static boolean contains(byte[] array, byte valueToFind) {
		return indexOf(array, valueToFind) != INDEX_NOT_FOUND;
	}

	public static int indexOf(double[] array, double valueToFind) {
		return indexOf(array, valueToFind, 0);
	}

	public static int indexOf(double[] array, double valueToFind, int startIndex) {
		if (isEmpty(array)) {
			return INDEX_NOT_FOUND;
		}
		if (startIndex < 0) {
			startIndex = 0;
		}
		for (int i = startIndex; i < array.length; i++) {
			if (valueToFind == array[i]) {
				return i;
			}
		}
		return INDEX_NOT_FOUND;
	}

	public static int lastIndexOf(double[] array, double valueToFind) {
		return lastIndexOf(array, valueToFind, Integer.MAX_VALUE);
	}

	public static int lastIndexOf(double[] array, double valueToFind, int startIndex) {
		if (isEmpty(array)) {
			return INDEX_NOT_FOUND;
		}
		if (startIndex < 0) {
			return INDEX_NOT_FOUND;
		} else if (startIndex >= array.length) {
			startIndex = array.length - 1;
		}
		for (int i = startIndex; i >= 0; i--) {
			if (valueToFind == array[i]) {
				return i;
			}
		}
		return INDEX_NOT_FOUND;
	}

	public static boolean contains(double[] array, double valueToFind) {
		return indexOf(array, valueToFind) != INDEX_NOT_FOUND;
	}

	public static int indexOf(float[] array, float valueToFind) {
		return indexOf(array, valueToFind, 0);
	}

	public static int indexOf(float[] array, float valueToFind, int startIndex) {
		if (isEmpty(array)) {
			return INDEX_NOT_FOUND;
		}
		if (startIndex < 0) {
			startIndex = 0;
		}
		for (int i = startIndex; i < array.length; i++) {
			if (valueToFind == array[i]) {
				return i;
			}
		}
		return INDEX_NOT_FOUND;
	}

	public static int lastIndexOf(float[] array, float valueToFind) {
		return lastIndexOf(array, valueToFind, Integer.MAX_VALUE);
	}

	public static int lastIndexOf(float[] array, float valueToFind, int startIndex) {
		if (isEmpty(array)) {
			return INDEX_NOT_FOUND;
		}
		if (startIndex < 0) {
			return INDEX_NOT_FOUND;
		} else if (startIndex >= array.length) {
			startIndex = array.length - 1;
		}
		for (int i = startIndex; i >= 0; i--) {
			if (valueToFind == array[i]) {
				return i;
			}
		}
		return INDEX_NOT_FOUND;
	}

	public static boolean contains(float[] array, float valueToFind) {
		return indexOf(array, valueToFind) != INDEX_NOT_FOUND;
	}

	public static int indexOf(boolean[] array, boolean valueToFind) {
		return indexOf(array, valueToFind, 0);
	}

	public static int indexOf(boolean[] array, boolean valueToFind, int startIndex) {
		if (isEmpty(array)) {
			return INDEX_NOT_FOUND;
		}
		if (startIndex < 0) {
			startIndex = 0;
		}
		for (int i = startIndex; i < array.length; i++) {
			if (valueToFind == array[i]) {
				return i;
			}
		}
		return INDEX_NOT_FOUND;
	}

	public static int lastIndexOf(boolean[] array, boolean valueToFind) {
		return lastIndexOf(array, valueToFind, Integer.MAX_VALUE);
	}

	public static int lastIndexOf(boolean[] array, boolean valueToFind, int startIndex) {
		if (isEmpty(array)) {
			return INDEX_NOT_FOUND;
		}
		if (startIndex < 0) {
			return INDEX_NOT_FOUND;
		} else if (startIndex >= array.length) {
			startIndex = array.length - 1;
		}
		for (int i = startIndex; i >= 0; i--) {
			if (valueToFind == array[i]) {
				return i;
			}
		}
		return INDEX_NOT_FOUND;
	}

	public static boolean contains(boolean[] array, boolean valueToFind) {
		return indexOf(array, valueToFind) != INDEX_NOT_FOUND;
	}
	
	/**
	 * Copies the specified array to an new array.
	 * 
	 * @param original the array to be copied
	 * 
	 * @throws NullPointerException if <tt>original</tt> is null
	 */
	public static <T> T[] copyOf(T[] original) {
		return Arrays.copyOf(original,original.length);
	}
	
	/**
	 * Converts a T[] array to a Iterable<T>.
	 */
	@SuppressWarnings("unchecked")
	public static <E,T extends E> Iterable<E> toIterable(T... elements){
		return null == elements || elements.length == 0 ? EmptyIterable.<E>instance() : new ArrayIterable<E>(elements);
	}

	/**
	 * Converts a T[] array to a {@link Set}<T>.
	 */
	@SuppressWarnings("unchecked")
    public static <E,T extends E> Set<E> toSet(T... elements){
		return New.<E>linkedHashSet(elements);
	}

	/**
	 * Converts a T[] array to a String[] array.
	 * 
	 * @param <T> elements type
	 * 
	 * @param array object array to convert.
	 * 
	 * @return String[] array contains all the elements in T[] array, {@link #EMPTY_STRING_ARRAY} if input is null.
	 */
	public static final <T> String[] toStringArray(T[] array) {
		if (null == array) {
			return EMPTY_STRING_ARRAY;
		}

		String[] strings = new String[array.length];

		for (int i = 0; i < array.length; i++) {
			Object object = array[i];

			if (null == object) {
				strings[i] = null;
			} else {
				strings[i] = Objects.toString(object,Strings.EMPTY);
			}
		}

		return strings;
	}
	
	/**
	 * cast the supplied array to an array which it's type is the type of the specific to array.
	 * 
	 * @param array the array to be cast.
	 * @param to specify the type of the array after cast.
	 * @return an array after cast.
	 */
	@SuppressWarnings("unchecked")
	public static final <T> T[] cast(Object[] array, T[] to) {
		if (to.length == array.length) {
			for (int i = 0; i < array.length; i++) {
				to[i] = (T) array[i];
			}
			return to;
		} else {
			return Arrays.asList(array).toArray(to);
		}
	}
	
	//concat
	@SuppressWarnings("unchecked")
	public static <T> T[] concat(T[] a, T[] b) {
	    final int alen = a.length;
	    final int blen = b.length;
	    if (alen == 0) {
	        return b;
	    }
	    if (blen == 0) {
	        return a;
	    }
	    final T[] result = (T[]) Array.newInstance(a.getClass().getComponentType(), alen + blen);
	    System.arraycopy(a, 0, result, 0, alen);
	    System.arraycopy(b, 0, result, alen, blen);
	    return result;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T[] prepend(T item , T[] array) {
	    final T[] result = (T[]) Array.newInstance(array.getClass().getComponentType(), array.length + 1);
	    result[0] = item;
	    System.arraycopy(array, 0, result, 1, array.length);
	    return result;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T[] append(T[] array, T item) {
	    final T[] result = (T[]) Array.newInstance(array.getClass().getComponentType(), array.length + 1);
	    System.arraycopy(array, 0, result, 0, array.length);
	    result[array.length] = item;
	    return result;
	}
	
	//equals
	public static <T> boolean equals(T[] a, T[] b){
		if((null == a && null == b) || (a == b)){
			return true;
		}
		
		if(null == a || null == b){
			return false;
		}
		
		if(a.length != b.length){
			return false;
		}
		
		for(int i=0;i<a.length;i++){
			if(!Objects.equals(a[i], b[i])){
				return false;
			}
		}
		
		return true;
	}
	
	public static boolean equals(String[] a,String[] b,boolean ignoreCase){
		if((null == a && null == b) || (a == b)){
			return true;
		}
		
		if(null == a || null == b){
			return false;
		}
		
		if(a.length != b.length){
			return false;
		}
		
		for(int i=0;i<a.length;i++){
			if(!Strings.equals(a[i], b[i],ignoreCase)){
				return false;
			}
		}
		
		return true;
	}
	
	public static boolean equalsIgnoreOrder(String[] a,String[] b,boolean ignoreCase){
		if((null == a && null == b) || (a == b)){
			return true;
		}
		
		if(null == a || null == b){
			return false;
		}
		
		if(a.length != b.length){
			return false;
		}
		
		for(int i=0;i<a.length;i++){
			String v1 = a[i];
			
			boolean found = false;
			for(String v2 : b){
				if(Strings.equals(v1,v2,ignoreCase)){
					found = true;
					break;
				}
			}
			
			if(!found){
				return false;
			}
		}
		return true;
	}

    public static int[] toIntArray(List<Integer> list) {
        if(null == list) {
            return EMPTY_INT_ARRAY;
        }

        int[] a = new int[list.size()];
        for(int i=0;i<a.length;i++) {
            a[i] = list.get(i);
        }
        return a;
    }

	/**
	 * Returns the first element in {@code iterable} that satisfies the given predicate.
	 * 
	 * <p/>
	 * 
	 * if such an element exists.If no such element is found, an null will be returned.
	 */
	public static <T> T firstOrNull(T[] array, Predicate<? super T> predicate) {
		Args.notNull(array);
		Args.notNull(predicate);
		
		for(int i=0;i<array.length;i++){
			T element = array[i];
			if(predicate.test(element)){
				return element;
			}
		}
		return null;
	}
	
	/**
	 * Returns the elements of {@code unfiltered} that satisfy a predicate. 
	 * 
	 * <p/>
	 */
    public static <T> List<T> filter(final T[] unfiltered, final Predicate<? super T> predicate) {
		Args.notNull(unfiltered);
		Args.notNull(predicate);
		
		List<T> filtered = new ArrayList<T>();
		
		for(int i=0;i<unfiltered.length;i++){
			T element = unfiltered[i];
			if(predicate.test(element)){
				filtered.add(element);
			}
		}
		
		return filtered;
	}

    public static <T extends Named> Map<String, T> toMap(T[] a) {
        Map map = new LinkedHashMap();
        if(null != a) {
            for(T item : a) {
                map.put(item.getName(), item);
            }
        }
        return map;
    }

    public static <T extends Named> T[] sort(T[] a) {
        Arrays.sort(a, Comparators.NAMED_COMPARATOR);
        return a;
    }
	
	protected Arrays2() {

	}	
}
