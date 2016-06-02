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
package leap.lang.jdbc;

import leap.lang.Strings;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.net.Urls;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class JDBC {
	private static final Log log = LogFactory.get(JDBC.class);
	
	public static final char   PARAMETER_PLACEHOLDER_CHAR   = '?';
	public static final String PARAMETER_PLACEHOLDER_STRING = "?";

	public static void closeConnection(Connection connection){
		if(null != connection){
			try {
	            connection.close();
            } catch (SQLException e) {
            	log.warn("Error closing connection : " + e.getMessage(),e);
            }
		}
	}
	
    public static void closeStatementOnly(Statement statement){
    	if(null != statement){
    		try{
    			statement.close();	
    		}catch(Throwable e){
    			log.warn("Error closing statement : {} ",e.getMessage(),e);
    		}
    	}
    }
	
    public static void closeStatementAndConnection(Statement statement){
        if(null != statement){
        	Connection conn = null;
        	try{
        		conn = statement.getConnection();
        	}catch(Throwable e) {
        		;
        	}
        	
            try {
                statement.close();
            } catch (Throwable e) {
                log.warn("Error closing statement : {} ",e.getMessage(),e);
            }finally{
            	closeConnection(conn);
            }
        }
    }
    
    public static void closeResultSetOnly(ResultSet rs){
    	if(null != rs){
    		try{
    			rs.close();	
    		}catch(Throwable e){
    			log.warn("Error closing result set : {} ",e.getMessage(),e);
    		}
    	}
    }
    
    public static void closeResultSetAndConnection(ResultSet rs) {
        if(null != rs){
        	Connection conn = null;
        	Statement  stmt = null;
        	try{
        		stmt = rs.getStatement();
        		conn = stmt.getConnection();
        	}catch(Throwable e){
        		;
        	}
        	
            try {
                rs.close();
            } catch (Throwable e) {
                log.warn("Error closing result set : {} ",e.getMessage(),e);
            } finally {
        		closeStatementOnly(stmt);
        		closeConnection(conn);
            }
        }
    }
    
    public static void closeResultSetAndStatement(ResultSet rs) {
    	Statement stmt = null;
        if(null != rs){
			try {
				stmt = rs.getStatement();
			} catch (Throwable e) {
				;
			}
            try {
                rs.close();
            } catch (Throwable e) {
                log.warn("Error closing result set : {} ",e.getMessage(),e);
            } finally {
            	closeStatementOnly(stmt);
            }
        }
    }

    /**
     * Try extracts the server info like server:port from the given jdbc url.
     *
     * <p/>
     * The method does not promise that the result is correct.
     */
    public static String tryExtractServerString(String url) {
        /*
            jdbc:microsoft:sqlserver://<server_name>:<port> -> port is optional
            jdbc:sqlserver://<server_name>:<port>
            jdbc:oracle:thin:@//<host>:<port>/ServiceName
            jdbc:oracle:thin:@<host>:<port>:<SID>
            jdbc:db2://<host>[:<port>]/<database_name>
            jdbc:mysql://<host>:<port>/<database_name>
            jdbc:postgresql://<host>:<port>/<database_name>
            jdbc:h2:tcp://<server>[:<port>]/[<path>]<databaseName>
            jdbc:h2:[file:][<path>]<databaseName>
            jdbc:h2:<url>[;USER=<username>][;PASSWORD=<value>]

         */
        if(!Strings.startsWith(url, "jdbc:", true)) {
            return null;
        }

        url = Urls.removeQueryString(url);

        //removes jdbc:dbtype:
        int index = url.indexOf(':',5);
        if(index < 0) {
            //unknown url
            return null;
        }else{
            url = url.substring(index+1);
        }

        //remove parameters like jdbc url : jdbc:h2:<url>[;USER=<username>][;PASSWORD=<value>]
        index = url.indexOf(';');
        if(index > 0) {
            url = url.substring(0, index);
        }

        index = url.indexOf("//");
        if(index >= 0) {
            url = url.substring(index + 2);
        }else {
            index = url.indexOf('@');
            if(index >= 0) {
                url = url.substring(index + 1);
            }
        }

        index = url.indexOf('/');
        if(index > 0) {
            url = url.substring(0, index);
        }

        String[] parts = Strings.split(url, ':');
        if(parts.length >= 2) {
            return parts[0] + ":" + parts[1];
        }else{
            //hostname or ip address only
            if(url.length() <= 5){
                return null;
            }
            return url;
        }
    }
    
	protected JDBC(){
		
	}
}
