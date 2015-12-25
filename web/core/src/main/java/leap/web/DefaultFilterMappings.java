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
package leap.web;

import java.util.concurrent.CopyOnWriteArrayList;

import leap.core.web.RequestBase;
import leap.lang.Args;
import leap.lang.collection.ListEnumerable;

public class DefaultFilterMappings extends ListEnumerable<FilterMapping> implements FilterMappings {
	
	public DefaultFilterMappings() {
	    super(new CopyOnWriteArrayList<FilterMapping>());
    }
	
	@Override
    public FilterMappings add(FilterMapping m) {
		Args.notNull(m,"filter mapping");
		l.add(m);
	    return this;
    }

	@Override
    public FilterMappings addAll(Iterable<FilterMapping> i) {
		if(null != i){
			for(FilterMapping m : i){
				add(m);
			}
		}
	    return this;
    }

	@Override
    public FilterMappings add(String path, Filter filter) {
		return add(new DefaultFilterMapping(path, filter));
    }

	@Override
    public FilterMappings add(Filter filter) {
		Args.notNull(filter, "filter");
	    return add(new AllFilterMapping(filter));
    }
	
	public static final class AllFilterMapping implements FilterMapping {
		
		private final Filter filter;
		
		public AllFilterMapping(Filter filter) {
			this.filter = filter;
		}

		@Override
        public boolean matches(RequestBase request) {
	        return true;
        }

		@Override
        public Filter getFilter() {
	        return filter;
        }
		
	}
}