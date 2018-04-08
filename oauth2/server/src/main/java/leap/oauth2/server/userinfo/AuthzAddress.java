/*
 *
 *  * Copyright 2013 the original author or authors.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  
 */

package leap.oauth2.server.userinfo;

import java.util.Map;

/**
 * 
 * Address Claim
 * 
 * @see <a href="http://openid.net/specs/openid-connect-core-1_0.html#AddressClaim">OpenId Connect</a>
 * 
 * @author kael.
 */
public interface AuthzAddress {
    
    String FORMATTED      = "formatted";
    String STREET_ADDRESS = "street_address";
    String LOCALITY       = "locality";
    String REGION         = "region";
    String POSTAL_CODE    = "postal_code";
    String COUNTRY        = "country";
    
    /**
     * Returns formatted or <code>null</code>
     */
    String getFormatted();

    /**
     * Returns street_address or <code>null</code>
     */
    String getStreetAddress();

    /**
     * Returns locality or <code>null</code>
     */
    String getLocality();
    /**
     * Returns region or <code>null</code>
     */
    String getRegion();
    /**
     * Returns postal_code or <code>null</code>
     */
    String getPostalCode();
    /**
     * Returns country or <code>null</code>
     */
    String getCountry();

    /**
     * convert this object to a map
     */
    Map<String, Object> toMap();

    /**
     * Returns an unmodifiable extend properties map
     */
    Map<String, Object> getExtProperties();

    /**
     * put an extend property
     */
    void putExtProperty(String name, Object value);
    
}
