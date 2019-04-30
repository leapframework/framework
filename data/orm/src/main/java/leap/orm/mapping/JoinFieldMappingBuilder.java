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
package leap.orm.mapping;

import leap.lang.Buildable;

public class JoinFieldMappingBuilder implements Buildable<JoinFieldMapping> {

	protected String  localFieldName;
	protected String  localColumnName;
    protected boolean localPrimaryKey;
    protected String  referencedEntityName;
    protected String  referencedFieldName;

    public String getLocalFieldName() {
		return localFieldName;
	}

	public void setLocalFieldName(String localFieldName) {
		this.localFieldName = localFieldName;
	}

    public String getLocalColumnName() {
        return localColumnName;
    }

    public void setLocalColumnName(String column) {
        this.localColumnName = column;
    }

    public boolean isLocalPrimaryKey() {
        return localPrimaryKey;
    }

    public void setLocalPrimaryKey(boolean localPrimaryKey) {
        this.localPrimaryKey = localPrimaryKey;
    }

    public String getReferencedEntityName() {
        return referencedEntityName;
    }

    public void setReferencedEntityName(String referencedEntityName) {
        this.referencedEntityName = referencedEntityName;
    }

    public String getReferencedFieldName() {
		return referencedFieldName;
	}

	public void setReferencedFieldName(String referencedFieldName) {
		this.referencedFieldName = referencedFieldName;
	}

    @Override
	public JoinFieldMapping build() {
		return new JoinFieldMapping(localFieldName, localPrimaryKey, referencedEntityName, referencedFieldName);
	}
}
