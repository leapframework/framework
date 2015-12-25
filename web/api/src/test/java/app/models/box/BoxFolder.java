/*
 * Copyright 2015 the original author or authors.
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
package app.models.box;

import java.util.Date;

public class BoxFolder extends BoxObject {

	protected String   id;
	protected String   name;
	protected String   description;
	protected int	   size;
	protected Date     createdAt;
	protected BoxUser  createdBy;
	protected BoxUser  modifiedBy;
	protected BoxUser  ownedBy;
	protected String[] tags;
	
	public BoxFolder() {
		super(BoxObjectTypes.FOLER);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public BoxUser getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(BoxUser createdBy) {
		this.createdBy = createdBy;
	}

	public BoxUser getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(BoxUser modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public BoxUser getOwnedBy() {
		return ownedBy;
	}

	public void setOwnedBy(BoxUser ownedBy) {
		this.ownedBy = ownedBy;
	}

	public String[] getTags() {
		return tags;
	}

	public void setTags(String[] tags) {
		this.tags = tags;
	} 
}
