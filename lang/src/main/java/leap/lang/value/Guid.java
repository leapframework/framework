/*
 * Copyright 2012 the original author or authors.
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
package leap.lang.value;

import java.util.UUID;

/**
 * A custom Guid class is necessary to interop with .net Guid strings incompatible with {@link UUID}.
 * <p>
 * Guids are equal if their string representations are equal.
 * </p>
 */
public class Guid {

	private final String value;

	private Guid(String value) {
		this.value = value;
	}

	/**
	 * Return a Guid for a given string.
	 * 
	 * @param value the guid's string representation
	 * @return a new Guid
	 */
	public static Guid fromString(String value) {
		return new Guid(value);
	}

	/**
	 * Return a Guid for a given UUID.
	 * 
	 * @param uuid an existing UUID
	 * @return a new Guid
	 */
	public static Guid fromUUID(UUID uuid) {
		return new Guid(uuid.toString());
	}

	/**
	 * Generate a new Guid.
	 * 
	 * @return a new Guid
	 */
	public static Guid randomGuid() {
		return new Guid(UUID.randomUUID().toString());
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}

	@Override
	public boolean equals(Object other) {
		return (other instanceof Guid) && ((Guid) other).value.equals(value);
	}

	@Override
	public String toString() {
		return value;
	}
}
