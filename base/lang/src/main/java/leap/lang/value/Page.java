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
package leap.lang.value;

import leap.lang.tostring.ToStringBuilder;

public class Page extends Limit {
	
	/**
	 * Creates a new {@link Page} instance of the given limit size.
	 * 
	 * @param size the limit size, starts from 1.
	 * 
	 * @see #startFrom(int, int)
	 */
    public static Page limit(int size){
    	return startFrom(1, size);
    }

    /**
     * Creates a new {@link Page} instance of the given limit size and starts from offset.
     *
     * <p/>
     * The offset is starts from 1.
     */
    public static Page limit(int limit, int offset) {
       return startFrom(offset, limit);
    }
	
    /**
     * Creates a new {@link Page} instance from the given start index.
     * 
     * @param start the start index , starts from 1.
     * @param size the page size, starts from 1.
     */
    public static Page startFrom(int start,int size){
    	int index = start < size ? 1 : start / size;
    	int end   = start + size - 1;
    	
    	return new Page(index,size,start,end);
    }
    
    public static Page startEnd(int start,int end) {
    	return startFrom(start, end - start + 1);
    }
    
    /**
     * Creates a new {@link Page} instance of the given page index and size.
     * 
     * @see #Page(int, int)
     */
    public static Page indexOf(int index, int size){
    	return new Page(index,size);
    }
    
    protected final int index;
    protected final int size;
    
    /**
     * Creates a new {@link Page} instance use the given page index and page size.
     * 
     * @param index page index,start from 1
     * @param size page size,start from 1
     */
    public Page(int index,int size){
    	super((index - 1) * size + 1,size * index);
        this.index     = index;
        this.size      = size;
    }
    
    protected Page(int index,int size,int start,int end){
    	super(start,end);
        this.index     = index;
        this.size      = size;
    }
    
    /**
     * Returns page index, starts from 1.
     */
    public int getIndex(){
        return index;
    }
    
    /**
     * Returns page size, starts form 1.
     */
    public int getSize(){
        return size;
    }
    
	@Override
    public String toString() {
		return new ToStringBuilder(this)
					.append("index", index)
					.append("size",size)
					.append("start",start)
					.append("end",end)
					.toString();
    }
}
