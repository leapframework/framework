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
package leap.core.ioc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class CopyOnWriteArrayBeanList<T> extends CopyOnWriteArrayList<T> implements BeanList<T>{

	private static final long serialVersionUID = 4646561656576489416L;
	
	public CopyOnWriteArrayBeanList() {
	    super();
    }

	public CopyOnWriteArrayBeanList(Collection<? extends T> c) {
	    super(c);
    }

	public CopyOnWriteArrayBeanList(T[] toCopyIn) {
	    super(toCopyIn);
    }

	@Override
    public List<T> removeAll(Class<? extends T> type) {
		List<T> remove = new ArrayList<>();
		
		for(T bean : this){
			if(type.isAssignableFrom(bean.getClass())){
				remove.add(bean);
			}
		}
		
		if(!remove.isEmpty()){
			for(T bean : remove){
				remove(bean);
			}
		}
		
	    return remove;
    }
}
