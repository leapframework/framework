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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class FileFilters {
	
	private static final FileFilter2 FILE         = new FileFilter();
	private static final FileFilter2 DIR          = new DirectoryFileFilter();
	private static final FileFilter2 HIDDEN       = new HiddenFilter();
	private static final FileFilter2 VISIBLE      = new NotFilter(HIDDEN);
	private static final FileFilter2 CAN_READ     = new CanReadFilter();
	private static final FileFilter2 CANNOT_READ  = new NotFilter(new CanReadFilter());
	private static final FileFilter2 CAN_WRITE    = new CanWriteFilter();
	private static final FileFilter2 CANNOT_WRITE = new NotFilter(new CanWriteFilter());
	private static final FileFilter2 READONLY     = new AndFilter(CAN_READ,CANNOT_WRITE);

	protected FileFilters(){
		
	}
	
	public static FileFilter2 isFile(){
		return FILE;
	}
	
	public static FileFilter2 isDirectory(){
		return DIR;
	}
	
	public static FileFilter2 isReadonly(){
		return READONLY;
	}
	
	public static FileFilter2 canRead(){
		return CAN_READ;
	}
	
	public static FileFilter2 canNotRead(){
		return CANNOT_READ;
	}
	
	public static FileFilter2 canWrite(){
		return CAN_WRITE;
	}
	
	public static FileFilter2 canNotWrite(){
		return CANNOT_WRITE;
	}
	
	public static FileFilter2 endsWith(String... suffixes){
		return new EndsWithFilter(suffixes);
	}
	
	public static FileFilter2 nameEquals(String... names){
		return new NameEqualsFilter(names);
	}
	
	public static FileFilter2 isHidden(){
		return HIDDEN;
	}
	
	public static FileFilter2 isVisible(){
		return VISIBLE;
	}
	
	public static FileFilter2 and(FileFilter2... filters){
		return new AndFilter(filters);
	}
	
	public static FileFilter2 or(FileFilter2... filters){
		return new OrFilter(filters);
	}
	
	public static FileFilter2 not(FileFilter2 filter){
		return new NotFilter(filter);
	}
	
	static class CanReadFilter extends AbstractFileFilter{
	    @Override
	    public boolean accept(File file) {
	        return file.canRead();
	    }
	}
	
	static class CanWriteFilter extends AbstractFileFilter {
	    @Override
	    public boolean accept(File file) {
	        return file.canWrite();
	    }
	}
	
	static class HiddenFilter extends AbstractFileFilter{
	    
	    @Override
	    public boolean accept(File file) {
	        return file.isHidden();
	    }
	}
	
	static class DirectoryFileFilter extends AbstractFileFilter{
	    @Override
	    public boolean accept(File file) {
	        return file.isDirectory();
	    }
	}
	
	static class NameEqualsFilter extends AbstractFileFilter {
	    
	    private final String[] names;
	    private final IOCase   caseSensitivity;

	    public NameEqualsFilter(String name) {
	        this(name, null);
	    }

	    public NameEqualsFilter(String name, IOCase caseSensitivity) {
	        if (name == null) {
	            throw new IllegalArgumentException("The name must not be null");
	        }
	        this.names = new String[] {name};
	        this.caseSensitivity = caseSensitivity == null ? IOCase.SENSITIVE : caseSensitivity;
	    }

	    public NameEqualsFilter(String... names) {
	        this(names, null);
	    }

	    public NameEqualsFilter(String[] names, IOCase caseSensitivity) {
	        if (names == null) {
	            throw new IllegalArgumentException("The array of names must not be null");
	        }
	        this.names = new String[names.length];
	        System.arraycopy(names, 0, this.names, 0, names.length);
	        this.caseSensitivity = caseSensitivity == null ? IOCase.SENSITIVE : caseSensitivity;
	    }

	    @Override
	    public boolean accept(File file) {
	        String name = file.getName();
	        for (String name2 : this.names) {
	            if (caseSensitivity.checkEquals(name, name2)) {
	                return true;
	            }
	        }
	        return false;
	    }

	    @Override
	    public boolean accept(File dir, String name) {
	        for (String name2 : names) {
	            if (caseSensitivity.checkEquals(name, name2)) {
	                return true;
	            }
	        }
	        return false;
	    }

	    @Override
	    public String toString() {
	        StringBuilder buffer = new StringBuilder();
	        buffer.append(super.toString());
	        buffer.append("(");
	        if (names != null) {
	            for (int i = 0; i < names.length; i++) {
	                if (i > 0) {
	                    buffer.append(",");
	                }
	                buffer.append(names[i]);
	            }
	        }
	        buffer.append(")");
	        return buffer.toString();
	    }
	}	
	
	static class NotFilter extends AbstractFileFilter{
	    private final FileFilter2 filter;

	    public NotFilter(FileFilter2 filter) {
	        if (filter == null) {
	            throw new IllegalArgumentException("The filter must not be null");
	        }
	        this.filter = filter;
	    }

	    @Override
	    public boolean accept(File file) {
	        return ! filter.accept(file);
	    }
	    
	    @Override
	    public boolean accept(File file, String name) {
	        return ! filter.accept(file, name);
	    }

	    @Override
	    public String toString() {
	        return super.toString() + "(" + filter.toString()  + ")";
	    }
	}
	
	static class AndFilter extends AbstractFileFilter {

		private final List<FileFilter2> filters;

		public AndFilter() {
			this.filters = new ArrayList<FileFilter2>();
		}

		public AndFilter(final List<FileFilter2> filters) {
			if (filters == null) {
				this.filters = new ArrayList<FileFilter2>();
			} else {
				this.filters = new ArrayList<FileFilter2>(filters);
			}
		}
		
		public AndFilter(final FileFilter2... filters) {
			if (filters == null) {
				this.filters = new ArrayList<FileFilter2>();
			} else {
				this.filters = Arrays.asList(filters);
			}
		}		

		@Override
		public boolean accept(final File file) {
			if (this.filters.isEmpty()) {
				return false;
			}
			for (FileFilter2 fileFilter : filters) {
				if (!fileFilter.accept(file)) {
					return false;
				}
			}
			return true;
		}

		@Override
		public boolean accept(final File file, final String name) {
			if (this.filters.isEmpty()) {
				return false;
			}
			for (FileFilter2 fileFilter : filters) {
				if (!fileFilter.accept(file, name)) {
					return false;
				}
			}
			return true;
		}

		@Override
		public String toString() {
			StringBuilder buffer = new StringBuilder();
			buffer.append(super.toString());
			buffer.append("(");
			if (filters != null) {
				for (int i = 0; i < filters.size(); i++) {
					if (i > 0) {
						buffer.append(",");
					}
					Object filter = filters.get(i);
					buffer.append(filter == null ? "null" : filter.toString());
				}
			}
			buffer.append(")");
			return buffer.toString();
		}
	}	
	
	static class OrFilter extends AbstractFileFilter{

		private final List<FileFilter2> filters;

		public OrFilter() {
			this.filters = new ArrayList<FileFilter2>();
		}

		public OrFilter(final List<FileFilter2> fileFilters) {
			if (fileFilters == null) {
				this.filters = new ArrayList<FileFilter2>();
			} else {
				this.filters = new ArrayList<FileFilter2>(fileFilters);
			}
		}
		
		public OrFilter(final FileFilter2... filters) {
			if (filters == null) {
				this.filters = new ArrayList<FileFilter2>();
			} else {
				this.filters = Arrays.asList(filters);
			}
		}

		@Override
		public boolean accept(final File file) {
			for (FileFilter2 fileFilter : filters) {
				if (fileFilter.accept(file)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public boolean accept(final File file, final String name) {
			for (FileFilter2 fileFilter : filters) {
				if (fileFilter.accept(file, name)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public String toString() {
			StringBuilder buffer = new StringBuilder();
			buffer.append(super.toString());
			buffer.append("(");
			if (filters != null) {
				for (int i = 0; i < filters.size(); i++) {
					if (i > 0) {
						buffer.append(",");
					}
					Object filter = filters.get(i);
					buffer.append(filter == null ? "null" : filter.toString());
				}
			}
			buffer.append(")");
			return buffer.toString();
		}
	}
	
	static class FileFilter extends AbstractFileFilter {
	    @Override
	    public boolean accept(File file) {
	        return file.isFile();
	    }
	}
	
	static class EndsWithFilter extends AbstractFileFilter {
	    
	    private final String[] suffixes;

	    private final IOCase caseSensitivity;

	    public EndsWithFilter(String suffix) {
	        this(suffix, IOCase.SENSITIVE);
	    }

	    public EndsWithFilter(String suffix, IOCase caseSensitivity) {
	        if (suffix == null) {
	            throw new IllegalArgumentException("The suffix must not be null");
	        }
	        this.suffixes = new String[] {suffix};
	        this.caseSensitivity = caseSensitivity == null ? IOCase.SENSITIVE : caseSensitivity;
	    }

	    public EndsWithFilter(String[] suffixes) {
	        this(suffixes, IOCase.SENSITIVE);
	    }

	    public EndsWithFilter(String[] suffixes, IOCase caseSensitivity) {
	        if (suffixes == null) {
	            throw new IllegalArgumentException("The array of suffixes must not be null");
	        }
	        this.suffixes = new String[suffixes.length];
	        System.arraycopy(suffixes, 0, this.suffixes, 0, suffixes.length);
	        this.caseSensitivity = caseSensitivity == null ? IOCase.SENSITIVE : caseSensitivity;
	    }

	    @Override
	    public boolean accept(File file) {
	        String name = file.getName();
	        for (String suffix : this.suffixes) {
	            if (caseSensitivity.checkEndsWith(name, suffix)) {
	                return true;
	            }
	        }
	        return false;
	    }
	    
	    @Override
	    public boolean accept(File file, String name) {
	        for (String suffix : this.suffixes) {
	            if (caseSensitivity.checkEndsWith(name, suffix)) {
	                return true;
	            }
	        }
	        return false;
	    }

	    @Override
	    public String toString() {
	        StringBuilder buffer = new StringBuilder();
	        buffer.append(super.toString());
	        buffer.append("(");
	        if (suffixes != null) {
	            for (int i = 0; i < suffixes.length; i++) {
	                if (i > 0) {
	                    buffer.append(",");
	                }
	                buffer.append(suffixes[i]);
	            }
	        }
	        buffer.append(")");
	        return buffer.toString();
	    }
	}
	
	static abstract class AbstractFileFilter implements FileFilter2 {

	    public boolean accept(File file) {
	        return accept(file.getParentFile(), file.getName());
	    }

	    public boolean accept(File dir, String name) {
	        return accept(new File(dir, name));
	    }

	    @Override
	    public String toString() {
	        return getClass().getSimpleName();
	    }
	}
}
