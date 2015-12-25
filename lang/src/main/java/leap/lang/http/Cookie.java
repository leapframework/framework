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

public interface Cookie {

	/**
	 * Returns the name of the cookie. The name cannot be changed after
	 * creation.
	 *
	 * @return the name of the cookie
	 */
	String getName();

	/**
	 * Gets the current value of this Cookie.
	 *
	 * @return the current value of this Cookie
	 */
	String getValue();

	/**
	 * Gets the domain name of this Cookie.
	 *
	 * <p>Domain names are formatted according to RFC 2109.
	 *
	 * @return the domain name of this Cookie
	 *
	 * @see #setDomain
	 */
	String getDomain();

	/**
	 * Returns the path on the server 
	 * to which the browser returns this cookie. The
	 * cookie is visible to all subpaths on the server.
	 *
	 * @return a <code>String</code> specifying a path that contains
	 *			a servlet name, for example, <i>/catalog</i>
	 */
	String getPath();

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
	int getMaxAge();

	/**
	 * Returns the comment describing the purpose of this cookie, or
	 * <code>null</code> if the cookie has no comment.
	 *
	 * @return the comment of the cookie, or <code>null</code> if unspecified
	 */
	String getComment();

	/**
	 * Returns <code>true</code> if the browser is sending cookies
	 * only over a secure protocol, or <code>false</code> if the
	 * browser can send cookies using any protocol.
	 *
	 * @return <code>true</code> if the browser uses a secure protocol,
	 * <code>false</code> otherwise
	 */
	boolean isSecure();

	/**
	 * Checks whether this Cookie has been marked as <i>HttpOnly</i>.
	 *
	 * @return true if this Cookie has been marked as <i>HttpOnly</i>,
	 * false otherwise
	 * 
	 * @see <a href="http://www.owasp.org/index.php/HttpOnly">HttpOnly</a>
	 */
	boolean isHttpOnly();

}