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
package leap.lang.http;

public class SimpleCookie implements Cookie {
	
	private String  name;
	private String  value;
	private String  domain;
	private String  path;
	private int	    maxAge = -1;
	private String  comment;
	private boolean secure;
	private boolean httpOnly;
	
	public SimpleCookie() {

	}

	public SimpleCookie(String name, String value) {
		this.name  = name;
		this.value = value;
	}

	public SimpleCookie(String name, String value, String domain, String path, int maxAge, String comment, boolean secure, boolean httpOnly) {
	    this.name = name;
	    this.value = value;
	    this.domain = domain;
	    this.path = path;
	    this.maxAge = maxAge;
	    this.comment = comment;
	    this.secure = secure;
	    this.httpOnly = httpOnly;
    }

	/**
     * Returns the name of the cookie. The name cannot be changed after
     * creation.
     *
     * @return the name of the cookie
     */
    @Override
    public String getName() {
    	return name;
    }

    /**
     * Gets the current value of this Cookie.
     *
     * @return the current value of this Cookie
     */
    @Override
    public String getValue() {
    	return value;
    }
    
    /**
     * Gets the domain name of this Cookie.
     *
     * <p>Domain names are formatted according to RFC 2109.
     *
     * @return the domain name of this Cookie
     *
     * @see #setDomain
     */ 
    @Override
    public String getDomain() {
    	return domain;
    }
    
    /**
     * Returns the path on the server 
     * to which the browser returns this cookie. The
     * cookie is visible to all subpaths on the server.
     *
     * @return a <code>String</code> specifying a path that contains
     *			a servlet name, for example, <i>/catalog</i>
     */ 
    @Override
    public String getPath() {
    	return path;
    }
    
    
    /**
     * Gets the maximum age in seconds of this Cookie.
     *
     * <p>By default, <code>-1</code> is returned, which indicates that
     * the cookie will persist until browser shutdown.
     *
     * @return	an integer specifying the maximum age of the
     *				cookie in seconds; if negative, means
     *				the cookie persists until browser shutdown
     */
    @Override
    public int getMaxAge() {
    	return maxAge;
    }
    
    /**
     * Returns the comment describing the purpose of this cookie, or
     * <code>null</code> if the cookie has no comment.
     *
     * @return the comment of the cookie, or <code>null</code> if unspecified
     */ 
    @Override
    public String getComment() {
    	return comment;
    }
    
    /**
     * Returns <code>true</code> if the browser is sending cookies
     * only over a secure protocol, or <code>false</code> if the
     * browser can send cookies using any protocol.
     *
     * @return <code>true</code> if the browser uses a secure protocol,
     * <code>false</code> otherwise
     */
    @Override
    public boolean isSecure() {
    	return secure;
    }
    
    /**
     * Checks whether this Cookie has been marked as <i>HttpOnly</i>.
     *
     * @return true if this Cookie has been marked as <i>HttpOnly</i>,
     * false otherwise
     * 
     * @see <a href="http://www.owasp.org/index.php/HttpOnly">HttpOnly</a>
     */
    @Override
    public boolean isHttpOnly() {
    	return httpOnly;
    }

	public void setName(String name) {
		this.name = name;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setMaxAge(int maxAge) {
		this.maxAge = maxAge;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public void setSecure(boolean secure) {
		this.secure = secure;
	}

	public void setHttpOnly(boolean httpOnly) {
		this.httpOnly = httpOnly;
	}
}
