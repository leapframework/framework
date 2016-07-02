/*
 * Copyright 2012 the original author or authors.
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
package leap.lang.value;

/**
 * Unsigned 8-bit integer (0-255)
 */
public class UnsignedByte extends Number implements Comparable<UnsignedByte> {

    private static final long serialVersionUID = 698449810630429786L;

    public static final UnsignedByte MIN_VALUE = new UnsignedByte(0);
    public static final UnsignedByte MAX_VALUE = new UnsignedByte(255);
    
    public static UnsignedByte valueOf(int value) {
        return new UnsignedByte(value);
    }

    public static UnsignedByte parseUnsignedByte(String value) {
        return valueOf(Integer.parseInt(value));
    }    

    private final int value;

    public UnsignedByte(int value) {
        this.value = checkBounds(value);
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }

    private static int checkBounds(int value) {
        if (value < 0 || value > 255){
        	throw new IllegalArgumentException("Value must be between 0 and 255");
        }
        return value;
    }

    @Override
    public int intValue() {
        return value;
    }

    @Override
    public long longValue() {
        return value;
    }

    @Override
    public float floatValue() {
        return value;
    }

    @Override
    public double doubleValue() {
        return value;
    }

    public int compareTo(UnsignedByte other) {
        return new Integer(value).compareTo(new Integer(other.value));
    }

    @Override
    public int hashCode() {
        return new Integer(value).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof UnsignedByte && ((UnsignedByte) obj).value == value;
    }
}
