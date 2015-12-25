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
package leap.orm.mapping;

import java.util.List;

import leap.lang.Args;

public class RelationMapping {

	protected final String 		       name; 		 	 //relation's name
	protected final RelationType       type;			 //relation's type
	protected final String			   targetEntityName; //target entity's name
	protected final String			   joinEntityName;	 //join entity's name
	protected final boolean		       optional;	 	 //is the relation optional ?
	protected final JoinFieldMapping[] joinFields;

	public RelationMapping(String name, RelationType type, String targetEntityName, String joinEntityName,
						   boolean optional, List<JoinFieldMapping> joinFields) {
		Args.notEmpty(name,"name");
		Args.notNull(type,"type");
		Args.notEmpty(targetEntityName,"targetEntityName");
		
		if(type == RelationType.MANY_TO_MANY){
			Args.notEmpty(joinEntityName,"joinEntityName");
		}
		
		this.name       	  = name;
		this.type       	  = type;
		this.targetEntityName = targetEntityName;
		this.joinEntityName   = joinEntityName;
		this.optional    	  = optional;
		this.joinFields  	  = null == joinFields ? new JoinFieldMapping[]{} : joinFields.toArray(new JoinFieldMapping[joinFields.size()]);
    }

	public String getName() {
		return name;
	}
	
	public RelationType getType() {
		return type;
	}
	
	public String getTargetEntityName() {
		return targetEntityName;
	}
	
	/**
	 * Be appropriate for many-to-many relation only.
	 */
	public String getJoinEntityName() {
		return joinEntityName;
	}

	public boolean isOptional() {
		return optional;
	}
	
	/**
	 * Be appropriate for many-to-one relation only.
	 */
	public JoinFieldMapping[] getJoinFields() {
		return joinFields;
	}

}