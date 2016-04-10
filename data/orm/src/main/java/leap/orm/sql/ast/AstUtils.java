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

import leap.orm.sql.parser.Token;

public class AstUtils {
	
    @SuppressWarnings("unchecked")
    public static <T extends AstNode> T findFirstNode(AstNode[] nodes, Class<T> type){
		for(int i=0;i<nodes.length;i++){
			AstNode node = nodes[i];
			
			if(type.equals(node.getClass())){
				return (T)node;
			}
			
			if(node instanceof AstNodeContainer){
				AstNode found = ((AstNodeContainer) node).findLastNode(type);
				if(null != found){
					return (T)found;
				}
			}
		}
		return null;
	}
	
    @SuppressWarnings("unchecked")
    public static <T extends AstNode> T findLastNode(AstNode[] nodes, Class<T> type){
		for(int i=nodes.length-1;i>=0;i--){
			AstNode node = nodes[i];
			
			if(type.equals(node.getClass())){
				return (T)node;
			}
			
			if(node instanceof AstNodeContainer){
				AstNode found = ((AstNodeContainer) node).findLastNode(type);
				if(null != found){
					return (T)found;
				}
			}
		}
		return null;
	}
    
    public static SqlObjectName findFirstObjectName(AstNode[] nodes,String lastName){
		for(int i=0;i<nodes.length;i++){
			AstNode node = nodes[i];
			
			if(node instanceof SqlObjectName && ((SqlObjectName) node).getLastName().equalsIgnoreCase(lastName)){
				return (SqlObjectName)node;
			}
			
			if(node instanceof AstNodeContainer){
				SqlObjectName found = findFirstObjectName(((AstNodeContainer) node).getNodes(), lastName);
				if(null != found){
					return found;
				}
				
			}
		}
		return null;
    }
    
    public static SqlTableName findFirstTableName(AstNode[] nodes,String lastName){
		for(int i=0;i<nodes.length;i++){
			AstNode node = nodes[i];
			
			if(node instanceof SqlTableName && ((SqlTableName) node).getLastName().equalsIgnoreCase(lastName)){
				return (SqlTableName)node;
			}
			
			if(node instanceof AstNodeContainer){
				SqlTableName found = findFirstTableName(((AstNodeContainer) node).getNodes(), lastName);
				if(null != found){
					return found;
				}
				
			}
		}
		return null;
    }

    public static AstNode prevNode(AstNode[] nodes, int currIndex) {
        int i = currIndex - 1;

        if( i < 0) {
            return null;
        }else{
            return nodes[i];
        }

    }

    public static <T extends AstNode> T prevNode(AstNode[] nodes, int currIndex,Class<T> type) {
        AstNode node = prevNode(nodes, currIndex);

        if(type.isAssignableFrom(node.getClass())) {
            return (T)node;
        }else{
            return null;
        }
    }

    public static boolean prevToken(AstNode[] nodes, int currIndex, Token token) {
        SqlToken node = prevNode(nodes, currIndex, SqlToken.class);
        if(null == node) {
            return false;
        }

        if(node.getToken() == token) {
            return true;
        }

        return false;
    }

    public static AstNode nextNode(AstNode[] nodes, int currIndex) {
        int i = currIndex + 1;

        if( i >= nodes.length) {
            return null;
        }else{
            return nodes[i];
        }

    }

    public static <T extends AstNode> T nextNode(AstNode[] nodes, int currIndex,Class<T> type) {
        AstNode node = nextNode(nodes, currIndex);

        if(type.isAssignableFrom(node.getClass())) {
            return (T)node;
        }else{
            return null;
        }
    }

    public static boolean nextToken(AstNode[] nodes, int currIndex, Token token) {
        SqlToken node = nextNode(nodes, currIndex, SqlToken.class);
        if(null == node) {
            return false;
        }

        if(node.getToken() == token) {
            return true;
        }

        return false;
    }

    protected AstUtils(){
    	
    }
}