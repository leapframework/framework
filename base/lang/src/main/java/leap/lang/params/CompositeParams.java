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
package leap.lang.params;

import java.util.ArrayList;
import java.util.List;

public class CompositeParams extends NamedParamsBase {
	
	protected final List<Params> list = new ArrayList<Params>();

	public CompositeParams() {
		super();
	}
	
	public CompositeParams add(Params parameters){
		if(null != parameters){
			map.putAll(parameters.map());
			list.add(parameters);
		}
		return this;
	}

	@Override
    public Params set(String name, Object value) {
	    super.set(name, value);
	    
	    for(int i=0;i<list.size();i++){
	    	list.get(i).set(name, value);
	    }
	    
	    return this;
    }
}