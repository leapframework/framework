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

package leap.oauth2.as.userinfo;

import java.util.HashMap;
import java.util.Map;

/**
 * @author kael.
 */
public class SimpleAuthzAddress implements AuthzAddress {
    
    protected String formatted;
    protected String streetAddress;
    protected String locality;
    protected String region;
    protected String postalCode;
    protected String country;
    
    @Override
    public String getFormatted() {
        return formatted;
    }

    @Override
    public String getStreetAddress() {
        return streetAddress;
    }

    @Override
    public String getLocality() {
        return locality;
    }

    @Override
    public String getRegion() {
        return region;
    }

    @Override
    public String getPostalCode() {
        return postalCode;
    }

    @Override
    public String getCountry() {
        return country;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put(FORMATTED,getFormatted());
        map.put(STREET_ADDRESS,getStreetAddress());
        map.put(LOCALITY,getLocality());
        map.put(REGION,getRegion());
        map.put(POSTAL_CODE,getPostalCode());
        map.put(COUNTRY,getCountry());
        return map;
    }

    public void setFormatted(String formatted) {
        this.formatted = formatted;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
