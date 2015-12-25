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
package leap.orm.tested.model;

import java.util.List;

import leap.core.validation.annotations.Email;
import leap.core.validation.annotations.NotEmpty;
import leap.orm.annotation.Finder;
import leap.orm.model.Model;

public class Person extends Model {

	@Finder
	public static Person findByName(String name){
		return null;
	}
	
	public static List<Person> findAllByNameLike(String name){
		return null;
	}
	
	public static List<Person> findAllByNameOrNameLike(String name,String name1){
		return null;
	}
	
	public static Person findByName1(String name){
		return new Person();
	}
	
	public static void test(){

	}

	@NotEmpty
	private String name;
	
	@Email
	private String email;
	
	private String address;
	
	private boolean enabled;
	
	private int age;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}
}