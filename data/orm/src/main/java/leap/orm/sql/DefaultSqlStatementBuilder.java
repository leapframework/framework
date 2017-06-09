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
package leap.orm.sql;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import leap.lang.Arrays2;
import leap.lang.Strings;

public class DefaultSqlStatementBuilder implements SqlStatementBuilder {
	
	private final SqlContext    context;
    private final Sql           sql;
	private final boolean		query;
	private final StringBuilder buf  = new StringBuilder(150);
	private final List<Object>  args = new ArrayList<Object>();
	
	private int pi = -1;


	public DefaultSqlStatementBuilder(SqlContext context, Sql sql, boolean query){
		this.context = context;
        this.sql     = sql;
		this.query   = query;
	}
	
	@Override
    public SqlContext context() {
	    return context;
    }

	public boolean isQuery() {
		return query;
	}
	
	public int increaseAndGetParameterIndex(){
		return ++pi;
	}
	
	@Override
    public int currentParameterIndex() {
	    return pi;
    }

    @Override
    public StringBuilder getText() {
        return buf;
    }

    @Override
    public List<Object> getArgs() {
		return args;
	}

    @Override
    public SavePoint createSavePoint() {
        return new SavePointImpl();
    }

    @Override
    public Appendable append(char c) throws IOException {
		buf.append(c);
	    return buf;
    }

	@Override
    public Appendable append(CharSequence cs, int start, int end) throws IOException {
		buf.append(cs,start,end);
	    return buf;
    }

	@Override
    public Appendable append(CharSequence cs) throws IOException {
		buf.append(cs);
		return buf;
    }
	
	public DefaultSqlStatementBuilder appendText(String text){
		if(!Strings.isEmpty(text)){
			int len = text.length();
			for(int i=0;i<len;i++){
				char c = text.charAt(i);
				
				if(c == '\r' || c == '\n'){
					c = ' ';
				}
				buf.append(c);
				
				if(skip(c)){
					i++;
					for(;i < len;i++){
						c = text.charAt(i);
						if(!skip(c)){
							buf.append(c);
							break;
						}
					}
				}
			}
		}
		return this;
	}
	
	public DefaultSqlStatementBuilder addParameter(Object value){
		args.add(value);	
		return this;
	}
	
	public boolean removeLastEqualsOperator(){
		int len = buf.length();
		
		for(int i=len-1;i > 0;i--){
			char c = buf.charAt(i);
			
			if(c == '=' && i > 1 && Character.isWhitespace(buf.charAt(i-1))){
				buf.delete(i-1,len);
				return true;
			}			
			
			if(!Character.isWhitespace(c)){
				return false;
			}
		}
		
		return false;
	}
	
	public boolean isLastInOperator(){
		for(int i = buf.length() - 1; i>2; i--) {
			char c = buf.charAt(i);
			
			if(Character.isWhitespace(c)) {
				continue;
			}
			
			if(c == '(') {
				continue;
			}
			
			if(c == 'n' || c == 'N') {
				char c1 = buf.charAt(i-1);
				if((c1 == 'i' || c1 == 'I') && Character.isWhitespace(buf.charAt(i-2))) {
					return true;
				}
			}
			
			return false;
		}
		
		return false;
	}
	
	private static boolean skip(char c){
		return Character.isWhitespace(c);
	}
	
	@Override
    public DefaultSqlStatement build() {
	    return new DefaultSqlStatement(context,
                                        sql,
                                        buf.toString(),
                                        args.toArray(new Object[args.size()]),
                                        Arrays2.EMPTY_INT_ARRAY);
    }

    protected class SavePointImpl implements SavePoint {
        private final int len;
        private final int size;

        public SavePointImpl() {
            this.len = buf.length();
            this.size = args.size();
        }

        @Override
        public void restore() {
            if(buf.length() > len) {
                buf.delete(len, buf.length());
            }

            while(args.size() > size) {
                args.remove(args.size() - 1);
            }
        }

        @Override
        public boolean hasChanges() {
            return buf.length() > len || args.size() > size;
        }
    }
}