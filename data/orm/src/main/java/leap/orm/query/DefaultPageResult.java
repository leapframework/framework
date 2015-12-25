/*
 * Copyright 2014 the original author or authors.
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
package leap.orm.query;

import java.util.List;

import leap.lang.value.Page;

public class DefaultPageResult<T> implements PageResult<T> {
	
	protected final Query<T>  query;
	protected final Page      page;
	protected final PageIndex pageInfo;
	
	private long 			totalCount = -1;
	private QueryResult<T>  result	   = null;
	
	public DefaultPageResult(Query<T> query,Page page) {
		this.query    = query;
		this.page     = page;
		this.pageInfo = new LazyPageIndex();
	}

	@Override
    public boolean isEmpty() {
	    return list().isEmpty();
    }

	@Override
    public int size() {
	    return list().size();
    }

	@Override
    public Page getPage() {
	    return page;
    }

	@Override
    public PageIndex getPageIndex() {
	    return pageInfo;
    }

	@Override
    public long getTotalCount() {
		if(-1 == totalCount){
			totalCount = query.count();
		}
	    return totalCount;
    }

	@Override
    public List<T> list() {
		if(null == result) {
			result = query.result(page);
		}
	    return result.list();
    }

	protected class LazyPageIndex implements PageIndex {

		private final int current;
		private final int size;
		
		private int totalPage = -1;
		
		public LazyPageIndex() {
			this.current = page.getIndex();
			this.size  = page.getSize();
        }

		@Override
        public int getCurrent() {
	        return current;
        }

		@Override
        public int getTotal() {
			if(-1 == totalPage){
				calcTotalCountAndPage();
			}
	        return totalPage;
        }
		
		@Override
        public boolean isFirst() {
	        return current == 1;
        }

		@Override
        public boolean isLast() {
	        return current == getTotal();
        }

		@Override
        public int getPrev() {
	        return current == 1 ? 1 : current - 1;
        }

		@Override
        public int getNext() {
			int totalPage = getTotal();
	        return current < totalPage ? current + 1 : totalPage;
        }

		private void calcTotalCountAndPage() {
			long totalCount = getTotalCount();
			
	        if(totalCount >=0){
	            if (totalCount % size == 0) {
	                totalPage = (int)(totalCount / size);
	            } else {
	                totalPage = (int)(totalCount / size) + 1;
	            }
	        }else{
	        	totalPage = -1;
	        }
		}
	}
}
