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
package leap.web;

import java.io.IOException;

import javax.servlet.ServletException;

import leap.core.web.RequestBase;
import leap.core.web.ResponseBase;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;

public abstract class FilterChainBase implements FilterChain {
	private static final Log log = LogFactory.get(FilterChainBase.class);
	
	private final FilterMappings filterMappings;
	
	private int index = -1;

	public FilterChainBase(FilterMappings filterMappings) {
		this.filterMappings = filterMappings;
	}

	@Override
	public void doFilter(Request request, Response response) throws ServletException, IOException {
		index++;

		while(index < filterMappings.size()){
			FilterMapping mapping = filterMappings.get(index);
			if(mapping.matches(request)){
				
				if(log.isDebugEnabled()){
					log.debug("Filtering request '{}' by filter '{}'...",request.getPath(),mapping.getFilter().getClass().getName());	
				}
				
				mapping.getFilter().doFilter(request, response, this);
				
				return;
			}else{
				index++;
			}
		}
		
		doNext(request, response);
	}
	
	public boolean filtered() {
		return index < filterMappings.size();
	}

	protected abstract void doNext(RequestBase request,ResponseBase response) throws ServletException, IOException;
}
