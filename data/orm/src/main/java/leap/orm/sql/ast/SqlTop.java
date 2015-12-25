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
package leap.orm.sql.ast;

import java.io.IOException;

public class SqlTop extends SqlNode {
	
	private String  token;
	private int     number;
	private boolean percent;
	
	public SqlTop(){
		
	}
	
	public SqlTop(String token){
		this.token = token;
	}
	
	public SqlTop(String token,int number){
		this.token  = token;
		this.number = number;
	}
	
	public int getNumber() {
		return number;
	}
	
	public void setNumber(int number) {
		this.number = number;
	}
	
	public boolean isPercent() {
		return percent;
	}

	public void setPercent(boolean percent) {
		this.percent = percent;
	}

	@Override
	protected void toString_(Appendable buf) throws IOException {
		buf.append(null == token ? "top" : token).append(" ").append(String.valueOf(number));
		if(percent){
			buf.append('%');
		}
	}
}