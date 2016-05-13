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
package tested.beans;

import java.util.ArrayList;
import java.util.List;

import leap.core.annotation.Inject;
import leap.lang.Lazy;

public class TAutoInjectBean {

	public @Inject Lazy<TPrimaryBean1>       lazyPrimaryBean;
	public @Inject Lazy<List<TPrimaryBean1>> lazyPrimaryBeans;
	
	protected @Inject List<TAnnotationBeanType> abeans = new ArrayList<TAnnotationBeanType>();
	
	private @Inject TPrimaryBean1 privateInjectPrimaryBean;
	private         TPrimaryBean1 privateNotInjectPrimaryBean;
	
	public TPrimaryBean1 nonGetterGetPrivateInjectPrimaryBean() {
		return privateInjectPrimaryBean;
	}
	
	public TPrimaryBean1 nonGetterGetNotInjectPrimaryBean() {
		return privateNotInjectPrimaryBean;
	}
	
	public List<TAnnotationBeanType> abeans() {
	    return abeans;
	}
}
