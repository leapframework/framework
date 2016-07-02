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
package leap.lang.value;

import leap.lang.Args;
import leap.lang.tostring.ToStringBuilder;

public class Limit {
	
	protected final int start;
	protected final int end;

	public Limit(int start,int end) {
		Args.assertTrue(start > 0 && end > 0 && end >= start, "Invalid start and end");
		this.start = start;
		this.end   = end;
	}

	/**
	 * Starts from 1.
	 */
	public int getStart() {
		return start;
	}

	/**
	 * Starts from 1.
	 */
	public int getEnd() {
		return end;
	}

	@Override
    public String toString() {
		return new ToStringBuilder(this)
					.append("start",start)
					.append("end",end)
					.toString();
    }
}