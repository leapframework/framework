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
package leap.orm.tested.model;

import leap.orm.annotation.Column;
import leap.orm.annotation.Id;
import leap.orm.model.Model;

public class ModelWithId2 extends Model {

	@Id
	@Column(name="model_id")
	protected String modelId;
	
	@Column
	protected String field1;

	public String getModelId() {
		return modelId;
	}

	public void setModelId(String userId) {
		this.modelId = userId;
	}

	public String getField1() {
		return field1;
	}

	public void setField1(String field1) {
		this.field1 = field1;
	}

}