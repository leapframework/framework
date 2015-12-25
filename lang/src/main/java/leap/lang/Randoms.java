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
package leap.lang;

import java.util.Date;
import java.util.Random;

//from apache commons-lang3

public class Randoms {

    /**
     * <p>Random object used by random method. This has to be not local
     * to the random method so as to not return the same value in the 
     * same millisecond.</p>
     */
    private static final Random RANDOM = new Random();

    // Random String
    //-----------------------------------------------------------------------

    /**
     * <p>Creates a random string whose length is the number of characters
     * specified.</p>
     *
     * <p>Characters will be chosen from the set of characters whose
     * ASCII value is between {@code 33} and {@code 126} (inclusive).</p>
     *
     * @param length  the length of random string to create
     * @return the random string
     */
    public static String nextString(int length) {
        return nextString(length, 33, 127, false, false);
    }
    
    public static String nextString(int minLength,int maxLength) {
    	int len;
    	if(minLength == maxLength){
    		len = minLength;
    	}else{
    		len = nextInt(minLength,maxLength);
    	}
    	return nextString(len);
    }
    
    /**
     * <p>Creates a random chararacter</p>
     *
     * <p>Character will be chosen from the set of all characters.</p>
     *
     * @return the random character
     */
    public static char nextCharacter() {
        return nextString(1, true, true).charAt(0);
    }

    /**
     * <p>Creates a random string whose length is the number of characters
     * specified.</p>
     *
     * <p>Characters will be chosen from the set of alpha-numeric
     * characters as indicated by the arguments.</p>
     *
     * @param length  the length of random string to create
     * @param letters  if {@code true}, generated string will include
     *  alphabetic characters
     * @param numbers  if {@code true}, generated string will include
     *  numeric characters
     * @return the random string
     */
    public static String nextString(int length, boolean letters, boolean numbers) {
        return nextString(length, 0, 0, letters, numbers);
    }
    
    /**
     * <p>Creates a random string whose length is the number of characters
     * specified.</p>
     *
     * <p>Characters will be chosen from the set of characters
     * specified.</p>
     *
     * @param length  the length of random string to create
     * @param chars  the String containing the set of characters to use,
     *  may be null
     * @return the random string
     * @throws IllegalArgumentException if {@code count} &lt; 0.
     */
    public static String nextString(int length, String chars) {
        if (chars == null) {
            return nextString(length, 0, 0, false, false, null, RANDOM);
        }
        return nextString(length, chars.toCharArray());
    }

    /**
     * <p>Creates a random string whose length is the number of characters
     * specified.</p>
     *
     * <p>Characters will be chosen from the set of characters specified.</p>
     *
     * @param length  the length of random string to create
     * @param chars  the character array containing the set of characters to use,
     *  may be null
     * @return the random string
     * @throws IllegalArgumentException if {@code count} &lt; 0.
     */
    public static String nextString(int length, char... chars) {
        if (chars == null) {
            return nextString(length, 0, 0, false, false, null, RANDOM);
        }
        return nextString(length, 0, chars.length, false, false, chars, RANDOM);
    }
    
    /**
     * <p>Creates a random string whose length is the number of characters
     * specified.</p>
     *
     * <p>Characters will be chosen from the set of alphabetic
     * characters.</p>
     *
     * @param length  the length of random string to create
     * @return the random string
     */
    public static String nextStringAlphabetic(int length) {
        return nextString(length, true, false);
    }
    
    /**
     * <p>Creates a random string whose length is the number of characters
     * specified.</p>
     *
     * <p>Characters will be chosen from the set of alpha-numeric
     * characters.</p>
     *
     * @param length  the length of random string to create
     * @return the random string
     */
    public static String nextStringAlphanumeric(int length) {
        return nextString(length, true, true);
    }
    
    /**
     * <p>Creates a random string whose length is the number of characters
     * specified.</p>
     *
     * <p>Characters will be chosen from the set of numeric
     * characters.</p>
     *
     * @param count  the length of random string to create
     * @return the random string
     */
    public static String nextStringNumeric(int count) {
        return nextString(count, false, true);
    }
    
    // Random Date Time
    //----------------------------------------------------------------------- 
    
    public static Date nextDateTime(){
    	return new Date(nextLong());
    }
    
    public static java.sql.Date nextDate(){
    	return new java.sql.Date(nextLong());
    }
    
    public static java.sql.Time nextTime(){
    	return new java.sql.Time(nextLong());
    }
    
    public static java.sql.Timestamp nextTimestamp(){
    	return new java.sql.Timestamp(nextLong());
    }
    
    // Random Number
    //-----------------------------------------------------------------------    
    
    public static short nextShort() {
        return (short)(RANDOM.nextDouble() * Short.MAX_VALUE);
    }
    
    public static short nextShort(short max) {
        return (short)(RANDOM.nextDouble() * max);
    }    

    public static int nextInt() {
        return (int)(RANDOM.nextDouble() * Integer.MAX_VALUE);
    }
    
    public static int nextInt(int max) {
        return (int)(RANDOM.nextDouble() * max);
    }
    
    public static int nextInt(int min,int max) {
        return (int)(RANDOM.nextDouble() * (max - min)) + min;
    }
    
    public static long nextLong() {
        return (long)(RANDOM.nextDouble() * Long.MAX_VALUE);
    }
    
    public static long nextLong(long max) {
        return (long)(RANDOM.nextDouble() * max);
    }  
    
    public static long nextLong(long min, long max) {
        return (long)(RANDOM.nextDouble() * (max - min)) + min;
    }
    
    public static float nextFloat() {
        return (float)(RANDOM.nextFloat() * Float.MAX_VALUE);
    }
    
    public static float nextFloat(float max) {
        return (float)(RANDOM.nextFloat() * max);
    }
    
    public static double nextDouble() {
        return RANDOM.nextDouble() * Double.MAX_VALUE;
    }  
    
    public static double nextDouble(double max) {
        return RANDOM.nextDouble() * max;
    }
    
    public static boolean nextBoolean() {
        return RANDOM.nextDouble() > 0.5;
    }
    
    // Internal Methods
    //-----------------------------------------------------------------------

    /**
     * <p>Creates a random string whose length is the number of characters
     * specified.</p>
     *
     * <p>Characters will be chosen from the set of alpha-numeric
     * characters as indicated by the arguments.</p>
     *
     * @param count  the length of random string to create
     * @param start  the position in set of chars to start at
     * @param end  the position in set of chars to end before
     * @param letters  if {@code true}, generated string will include
     *  alphabetic characters
     * @param numbers  if {@code true}, generated string will include
     *  numeric characters
     * @return the random string
     */
    static String nextString(int count, int start, int end, boolean letters, boolean numbers) {
        return nextString(count, start, end, letters, numbers, null, RANDOM);
    }
    
    /**
     * <p>Creates a random string based on a variety of options, using
     * default source of randomness.</p>
     *
     * <p>This method has exactly the same semantics as
     * {@link #nextString(int,int,int,boolean,boolean,char[],Random)}, but
     * instead of using an externally supplied source of randomness, it uses
     * the internal static {@link Random} instance.</p>
     *
     * @param count  the length of random string to create
     * @param start  the position in set of chars to start at
     * @param end  the position in set of chars to end before
     * @param letters  only allow letters?
     * @param numbers  only allow numbers?
     * @param chars  the set of chars to choose randoms from.
     *  If {@code null}, then it will use the set of all chars.
     * @return the random string
     * @throws ArrayIndexOutOfBoundsException if there are not
     *  {@code (end - start) + 1} characters in the set array.
     */
    static String nextString(int count, int start, int end, boolean letters, boolean numbers, char... chars) {
        return nextString(count, start, end, letters, numbers, chars, RANDOM);
    }    

    /**
     * <p>Creates a random string based on a variety of options, using
     * supplied source of randomness.</p>
     *
     * <p>If start and end are both {@code 0}, start and end are set
     * to {@code ' '} and {@code 'z'}, the ASCII printable
     * characters, will be used, unless letters and numbers are both
     * {@code false}, in which case, start and end are set to
     * {@code 0} and {@code Integer.MAX_VALUE}.
     *
     * <p>If set is not {@code null}, characters between start and
     * end are chosen.</p>
     *
     * <p>This method accepts a user-supplied {@link Random}
     * instance to use as a source of randomness. By seeding a single 
     * {@link Random} instance with a fixed seed and using it for each call,
     * the same random sequence of strings can be generated repeatedly
     * and predictably.</p>
     *
     * @param count  the length of random string to create
     * @param start  the position in set of chars to start at
     * @param end  the position in set of chars to end before
     * @param letters  only allow letters?
     * @param numbers  only allow numbers?
     * @param chars  the set of chars to choose randoms from.
     *  If {@code null}, then it will use the set of all chars.
     * @param random  a source of randomness.
     * @return the random string
     * @throws ArrayIndexOutOfBoundsException if there are not
     *  {@code (end - start) + 1} characters in the set array.
     * @throws IllegalArgumentException if {@code count} &lt; 0.
     * @since 2.0
     */
    static String nextString(int count, int start, int end, boolean letters, boolean numbers,
                          char[] chars, Random random) {
        if (count == 0) {
            return "";
        } else if (count < 0) {
            throw new IllegalArgumentException("Requested random string length " + count + " is less than 0.");
        }
        if (start == 0 && end == 0) {
            end = 'z' + 1;
            start = ' ';
            if (!letters && !numbers) {
                start = 0;
                end = Integer.MAX_VALUE;
            }
        }

        char[] buffer = new char[count];
        int gap = end - start;

        while (count-- != 0) {
            char ch;
            if (chars == null) {
                ch = (char) (random.nextInt(gap) + start);
            } else {
                ch = chars[random.nextInt(gap) + start];
            }
            if (letters && Character.isLetter(ch)
                    || numbers && Character.isDigit(ch)
                    || !letters && !numbers) {
                if(ch >= 56320 && ch <= 57343) {
                    if(count == 0) {
                        count++;
                    } else {
                        // low surrogate, insert high surrogate after putting it in
                        buffer[count] = ch;
                        count--;
                        buffer[count] = (char) (55296 + random.nextInt(128));
                    }
                } else if(ch >= 55296 && ch <= 56191) {
                    if(count == 0) {
                        count++;
                    } else {
                        // high surrogate, insert low surrogate before putting it in
                        buffer[count] = (char) (56320 + random.nextInt(128));
                        count--;
                        buffer[count] = ch;
                    }
                } else if(ch >= 56192 && ch <= 56319) {
                    // private high surrogate, no effing clue, so skip it
                    count++;
                } else {
                    buffer[count] = ch;
                }
            } else {
                count++;
            }
        }
        return new String(buffer);
    }
    
    protected Randoms(){
    	
    }
}
