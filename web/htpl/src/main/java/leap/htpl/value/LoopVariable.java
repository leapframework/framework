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
package leap.htpl.value;

public final class LoopVariable {
	
	private final int count;
	
	private int     index;
	private Object  item;
	
	public LoopVariable(int count){
		this.count = count;
	}
	
	public int getCount() {
		return count;
	}
	
	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public Object getItem() {
		return item;
	}

	public void setItem(Object item) {
		this.item = item;
	}

	public boolean isFirst() {
		return index == 1;
	}

	public boolean isLast() {
		return index == count;
	}
	
	public boolean isOdd() {
		return index % 2 == 1;
	}
	
	public boolean isEven() {
		return index % 2 == 0;
	}
}