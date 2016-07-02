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
package leap.lang.io;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class IOComparators {

    /** Case-sensitive name comparator instance (see {@link IOCase#SENSITIVE}) */
    private static final Comparator<File> NAME_COMPARATOR = new NameFileComparator();

    /** Reverse case-sensitive name comparator instance (see {@link IOCase#SENSITIVE}) */
    private static final Comparator<File> NAME_REVERSE = new ReverseComparator(NAME_COMPARATOR);

    /** Case-insensitive name comparator instance (see {@link IOCase#INSENSITIVE}) */
    private static final Comparator<File> NAME_INSENSITIVE_COMPARATOR = new NameFileComparator(IOCase.INSENSITIVE);

    /** Reverse case-insensitive name comparator instance (see {@link IOCase#INSENSITIVE}) */
    private static final Comparator<File> NAME_INSENSITIVE_REVERSE = new ReverseComparator(NAME_INSENSITIVE_COMPARATOR);

    /** System sensitive name comparator instance (see {@link IOCase#SYSTEM}) */
    private static final Comparator<File> NAME_SYSTEM_COMPARATOR = new NameFileComparator(IOCase.SYSTEM);

    /** Reverse system sensitive name comparator instance (see {@link IOCase#SYSTEM}) */
    private static final Comparator<File> NAME_SYSTEM_REVERSE = new ReverseComparator(NAME_SYSTEM_COMPARATOR);
    
    public static Comparator<File> nameSensitive(){
    	return NAME_COMPARATOR;
    }
    
    public static Comparator<File> nameSensitiveReverse(){
    	return NAME_REVERSE;
    }
    
    public static Comparator<File> nameInsensitive(){
    	return NAME_INSENSITIVE_COMPARATOR;
    }
    
    public static Comparator<File> nameInsensitiveReverse(){
    	return NAME_INSENSITIVE_REVERSE;
    }
    
    public static Comparator<File> nameSystem(){
    	return NAME_SYSTEM_COMPARATOR;
    }
    
    public static Comparator<File> nameSystemReverse(){
    	return NAME_SYSTEM_REVERSE;
    }
    
	/**
	 * Compare the <b>names</b> of two files for order (see {@link File#getName()}).
	 * <p>
	 * This comparator can be used to sort lists or arrays of files
	 * by their name either in a case-sensitive, case-insensitive or
	 * system dependant case sensitive way. A number of singleton instances
	 * are provided for the various case sensitivity options (using {@link IOCase})
	 * and the reverse of those options.
	 * <p>
	 * Example of a <i>case-sensitive</i> file name sort using the
	 * {@link #NAME_COMPARATOR} singleton instance:
	 * <pre>
	 *       List&lt;File&gt; list = ...
	 *       NameFileComparator.NAME_COMPARATOR.sort(list);
	 * </pre>
	 * <p>
	 * Example of a <i>reverse case-insensitive</i> file name sort using the
	 * {@link #NAME_INSENSITIVE_REVERSE} singleton instance:
	 * <pre>
	 *       File[] array = ...
	 *       NameFileComparator.NAME_INSENSITIVE_REVERSE.sort(array);
	 * </pre>
	 * <p>
	 */
	static class NameFileComparator extends AbstractFileComparator {
	    /** Whether the comparison is case sensitive. */
	    private final IOCase caseSensitivity;

	    /**
	     * Construct a case sensitive file name comparator instance.
	     */
	    public NameFileComparator() {
	        this.caseSensitivity = IOCase.SENSITIVE;
	    }

	    /**
	     * Construct a file name comparator instance with the specified case-sensitivity.
	     *
	     * @param caseSensitivity  how to handle case sensitivity, null means case-sensitive
	     */
	    public NameFileComparator(IOCase caseSensitivity) {
	        this.caseSensitivity = caseSensitivity == null ? IOCase.SENSITIVE : caseSensitivity;
	    }

	    /**
	     * Compare the names of two files with the specified case sensitivity.
	     * 
	     * @param file1 The first file to compare
	     * @param file2 The second file to compare
	     * @return a negative value if the first file's name
	     * is less than the second, zero if the names are the
	     * same and a positive value if the first files name
	     * is greater than the second file.
	     */
	    public int compare(File file1, File file2) {
	        return caseSensitivity.checkCompareTo(file1.getName(), file2.getName());
	    }

	    /**
	     * String representation of this file comparator.
	     *
	     * @return String representation of this file comparator
	     */
	    @Override
	    public String toString() {
	        return super.toString() + "[caseSensitivity=" + caseSensitivity + "]";
	    }
	}
	
	static class ReverseComparator extends AbstractFileComparator {

	    private final Comparator<File> delegate;

	    public ReverseComparator(Comparator<File> delegate) {
	        if (delegate == null) {
	            throw new IllegalArgumentException("Delegate comparator is missing");
	        }
	        this.delegate = delegate;
	    }

	    public int compare(File file1, File file2) {
	        return delegate.compare(file2, file1); // parameters switched round
	    }

	    @Override
	    public String toString() {
	        return super.toString() + "[" + delegate.toString() + "]";
	    }
	}
	
	static abstract class AbstractFileComparator implements Comparator<File> {

	    public File[] sort(File... files) {
	        if (files != null) {
	            Arrays.sort(files, this);
	        }
	        return files;
	    }

	    public List<File> sort(List<File> files) {
	        if (files != null) {
	            Collections.sort(files, this);
	        }
	        return files;
	    }

	    @Override
	    public String toString() {
	        return getClass().getSimpleName();
	    }
	}	
}
