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
package leap.orm.tested.model.team;

import java.sql.Timestamp;

import leap.orm.annotation.Column;
import leap.orm.annotation.Id;
import leap.orm.annotation.JoinField;
import leap.orm.annotation.ManyToOne;
import leap.orm.annotation.NonColumn;
import leap.orm.model.Model;
import leap.orm.tested.model.sec.Role;
import leap.orm.tested.model.sec.User;
import static leap.lang.enums.Bool.*;

@ManyToOne(target =Team.class,fields={
	@JoinField(name="teamId")
})
@ManyToOne(target =User.class,fields={
	@JoinField(name="userId")
})
@ManyToOne(target =Role.class,fields={
	@JoinField(name="roleId")
})
public class TeamMember extends Model {
	
	@Id
	@Column(nullable=FALSE)
	private long teamId;
	
	@Id
	@Column(nullable=FALSE,name="user_id")
	private String userId;
	
	@NonColumn
	private String userName;
	
	@Column(nullable=FALSE)
	private int roleId;
	
	@Column(nullable=FALSE)
	private Timestamp createdAt;
	
	@Column(nullable=FALSE)
	private Timestamp updatedAt;
	
	@Column(nullable=TRUE)
	private String creatorId;
	
	@Column(nullable=TRUE)
	private String updatorId;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public long getTeamId() {
		return teamId;
	}

	public void setTeamId(long teamId) {
		this.teamId = teamId;
	}

	public int getRoleId() {
		return roleId;
	}

	public void setRoleId(int roleId) {
		this.roleId = roleId;
	}

	public Timestamp getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Timestamp createdAt) {
		this.createdAt = createdAt;
	}

	public Timestamp getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Timestamp updatedAt) {
		this.updatedAt = updatedAt;
	}

	public String getCreatorId() {
		return creatorId;
	}

	public void setCreatorId(String creatorId) {
		this.creatorId = creatorId;
	}

	public String getUpdatorId() {
		return updatorId;
	}

	public void setUpdatorId(String updatorId) {
		this.updatorId = updatorId;
	}
}
