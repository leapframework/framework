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
package leap.orm.tested;

import leap.orm.annotation.Domain;
import leap.orm.annotation.Entity;
import leap.orm.tested.domains.TestDomain;
import leap.orm.tested.domains.TestDomain1;
import leap.orm.tested.domains.TestDomain2;

import java.sql.Timestamp;

@Entity
public class DomainEntity {

	@TestDomain
	private String test;
	
	@TestDomain1
	private String test1;

	@TestDomain2
	private String test2;

	@Domain(autoMapping = false)
	private Timestamp createdAt;

	private Timestamp updatedAt;
	
	public String getTest() {
		return test;
	}

	public void setTest(String test1) {
		this.test = test1;
	}

	public String getTest1() {
		return test1;
	}

	public void setTest1(String test1) {
		this.test1 = test1;
	}

	public String getTest2() {
		return test2;
	}

	public void setTest2(String test2) {
		this.test2 = test2;
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
}