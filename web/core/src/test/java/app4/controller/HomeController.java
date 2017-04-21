/*
 * Copyright 2016 the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *       http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
package app4.controller;

import leap.core.annotation.Bean;
import leap.lang.New;
import leap.lang.json.JsonProcessResult;
import leap.lang.json.JsonProcessor;
import leap.lang.naming.NamingStyles;
import leap.web.json.JsonSerialize;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HomeController {
	@JsonSerialize(namingStyle = NamingStyles.NAME_LOWER_UNDERSCORE)
	public Map<String, Object> map(){
		Map<String, Object> map = New.hashMap("userId","userId");
		map.put("userProperty",New.hashMap("userName","userName"));
		return map;
	}

	public Object bean1origin(){
		return new DefinedProcessBean().setProp1("test").setProp2(null);
	}

	@JsonSerialize(processors = {Processor.class})
	public Object bean1(){
		return new DefinedProcessBean().setProp1("test").setProp2(null);
	}

	@JsonSerialize(processors = {Processor.class})
	public Object bean1array(){
		DefinedProcessBean bean =  new DefinedProcessBean().setProp1("test").setProp2(null);
		List<DefinedProcessBean> list = new ArrayList<>();
		list.add(bean);
		return list;
	}

	@JsonSerialize(processors = {Processor.class})
	public Object map1(){
		Map<String, Object> map = New.hashMap("prop1", "test", "prop2", null);
		return map;
	}

	final static class DefinedProcessBean {
		private String prop1;
		private String prop2;

		public String getProp1() {
			return prop1;
		}

		public DefinedProcessBean setProp1(String prop1) {
			this.prop1 = prop1;
			return this;
		}

		public String getProp2() {
			return prop2;
		}

		public DefinedProcessBean setProp2(String prop2) {
			this.prop2 = prop2;
			return this;
		}
	}

	@Bean
	class Processor implements JsonProcessor {

		@Override
		public void process(JsonProcessResult result) {
			if(null == result.getValue()) result.setValue("");
		}
	}
}
