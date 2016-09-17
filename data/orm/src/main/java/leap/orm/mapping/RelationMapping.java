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

import leap.lang.Args;
import leap.orm.enums.CascadeDeleteAction;

import java.util.List;

public class RelationMapping {

    protected final String              name;             //relation's name
    protected final RelationType        type;             //relation's type
    protected final String              inverseRelationName;
    protected final String              targetEntityName; //target entity's name
    protected final String              joinEntityName;     //join entity's name
    protected final boolean             optional;         //is the relation optional ?
    protected final CascadeDeleteAction onCascadeDelete;
    protected final boolean             virtual;
    protected final JoinFieldMapping[]  joinFields;

	public RelationMapping(String name, RelationType type, String inverseRelationName, String targetEntityName, String joinEntityName,
						   boolean optional, CascadeDeleteAction onCascadeDelete,
                           boolean virtual, List<JoinFieldMapping> joinFields) {
		Args.notEmpty(name,"name");
		Args.notNull(type,"type");
        Args.notEmpty(inverseRelationName, "inverseRelationName");
		Args.notEmpty(targetEntityName,"targetEntityName");
        Args.notNull(onCascadeDelete, "onCascadeDelete");
		
		if(type == RelationType.MANY_TO_MANY){
			Args.notEmpty(joinEntityName,"joinEntityName");
		}

		this.name       	  = name;
		this.type       	  = type;
        this.inverseRelationName = inverseRelationName;
		this.targetEntityName = targetEntityName;
		this.joinEntityName   = joinEntityName;
		this.optional    	  = optional;
        this.onCascadeDelete  = onCascadeDelete;
        this.virtual          = virtual;
		this.joinFields  	  = null == joinFields ? new JoinFieldMapping[]{} : joinFields.toArray(new JoinFieldMapping[joinFields.size()]);
    }

    /**
     * Required. Returns the name of relation.
     */
	public String getName() {
		return name;
	}

    /**
     * Required. Returns the relation type.
     */
	public RelationType getType() {
		return type;
	}

    /**
     * Returns true if the type is {@link RelationType#MANY_TO_ONE}
     */
    public boolean isManyToOne() {
        return RelationType.MANY_TO_ONE.equals(type);
    }

    /**
     * Returns true if the type is {@link RelationType#MANY_TO_MANY}
     */
    public boolean isManyToMany() {
        return RelationType.MANY_TO_MANY.equals(type);
    }

    /**
     * Returns true if the type is {@link RelationType#ONE_TO_MANY}
     */
    public boolean isOneToMany() {
        return RelationType.ONE_TO_MANY.equals(type);
    }

    public String getInverseRelationName() {
        return inverseRelationName;
    }

    /**
     * Required. Returns the target entity's namne.
     */
	public String getTargetEntityName() {
		return targetEntityName;
	}
	
    /**
     * Returns true if the relation is optional. Valid on in *-to-one relation.
     */
	public boolean isOptional() {
		return optional;
	}

    public CascadeDeleteAction getOnCascadeDelete() {
        return onCascadeDelete;
    }

    /**
     * Returns true if the relation is not user defined (auto created by system).
     */
    public boolean isVirtual() {
        return virtual;
    }

    /**
	 * Valid for *-to-one relation only.
	 */
	public JoinFieldMapping[] getJoinFields() {
		return joinFields;
	}

    /**
     * Valid for many-to-many relation only.
     */
    public String getJoinEntityName() {
        return joinEntityName;
    }

    public boolean isSetNullOnCascadeDelete() {
        return null != onCascadeDelete && onCascadeDelete == CascadeDeleteAction.SET_NULL;
    }
}