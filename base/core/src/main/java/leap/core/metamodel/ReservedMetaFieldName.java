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
package leap.core.metamodel;

import java.util.HashMap;
import java.util.Map;

public enum ReservedMetaFieldName {
	NONE(null),
	TITLE("title"),
	SUMMARY("summary"),
	DESCRIPTION("description"),
	CREATED_AT("createdAt"),
	UPDATED_AT("updatedAt"),
	PUBLISHED_AT("publishedAt");
	
	private static final Map<String, ReservedMetaFieldName> mapping = new HashMap<>();

	static {
		mapping.put(TITLE.fieldName.toLowerCase(),        TITLE);
		mapping.put(SUMMARY.fieldName.toLowerCase(),      SUMMARY);
		mapping.put(DESCRIPTION.fieldName.toLowerCase(),  DESCRIPTION);
		mapping.put(CREATED_AT.fieldName.toLowerCase(),   CREATED_AT);
		mapping.put(UPDATED_AT.fieldName.toLowerCase(),   UPDATED_AT);
		mapping.put(PUBLISHED_AT.fieldName.toLowerCase(), PUBLISHED_AT);
	}
	
	public static ReservedMetaFieldName tryForName(String name) {
		if(name == null) {
			return null;
		}
		return mapping.get(name.toLowerCase());
	}
	
	private final String fieldName;
	
	private ReservedMetaFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getFieldName() {
		return fieldName;
	}
	
	public boolean isTitle() {
		return this == TITLE;
	}
	
	public boolean isSummary() {
		return this == SUMMARY;
	}
	
	public boolean isDescription() {
		return this == DESCRIPTION;
	}
	
	public boolean isCreatedAt() {
		return this == CREATED_AT;
	}
	
	public boolean isUpdatedAt() {
		return this == UPDATED_AT;
	}
	
	public boolean isPublishedAt() {
		return this == PUBLISHED_AT;
	}
}