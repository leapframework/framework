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

import java.util.Map;

/**
 * Standard End-User claim
 * 
 * @see <a href="http://openid.net/specs/openid-connect-core-1_0.html#StandardClaims">OpenId Connect</a>
 * 
 * @author kael.
 */
public interface AuthzUserInfo {
    
    String SUBJECT               = "sub";
    String NAME                  = "name";
    String GIVEN_NAME            = "given_name";
    String FAMILY_NAME           = "family_name";
    String MIDDLE_NAME           = "middle_name";
    String NICKNAME              = "nickname";
    String PREFERRED_USERNAME    = "preferred_username";
    String PROFILE               = "profile";
    String PICTURE               = "picture";
    String WEBSITE               = "website";
    String EMAIL                 = "email";
    String EMAIL_VERIFIED        = "email_verified";
    String GENDER                = "gender";
    String BIRTHDATE             = "birthdate";
    String ZONEINFO              = "zoneinfo";
    String LOCALE                = "locale";
    String PHONE_NUMBER          = "phone_number";
    String PHONE_NUMBER_VERIFIED = "phone_number_verified";
    String ADDRESS               = "address";
    String UPDATED_AT            = "updated_at";
    
    /**
     * Returns sub or <code>null</code>
     */
    String getSubject();
    /**
     * Returns name or <code>null</code>
     */
    String getFullName();
    /**
     * Returns given_name or <code>null</code>
     */
    String getGivenName();
    /**
     * Returns family_name or <code>null</code>
     */
    String getFamilyName();
    /**
     * Returns middle_name or <code>null</code>
     */
    String getMiddleName();
    /**
     * Returns nickname or <code>null</code>
     */
    String getNickname();
    /**
     * Returns preferred_username or <code>null</code>
     */
    String getPreferredUsername();

    /**
     * Returns profile or <code>null</code>
     * 
     * url format
     */
    String getProfile();
    /**
     * Returns picture or <code>null</code>
     * 
     * url format
     * 
     */
    String getPicture();

    /**
     * Returns website or <code>null</code>
     */
    String getWebsite();

    /**
     * Returns email or <code>null</code>
     */
    String getEmail();

    /**
     * Returns <code>trur</code> if this email has been verified.
     */
    boolean isEmailVerified();

    /**
     * Returns gender or <code>null</code>
     */
    String getGender();
    /**
     * Returns birthdate or <code>null</code>
     * 
     * <code>YYYY-MM-DD</code> format or <code>YYYY</code> format
     */
    String getBirthdate();
    /**
     * Returns zoneinfo or <code>null</code>
     */
    String getZoneinfo();
    /**
     * Returns locale or <code>null</code>
     */
    String getLocale();
    /**
     * Returns phone number or <code>null</code>
     */
    String getPhoneNumber();
    /**
     * Returns <code>true</code> if this phone number has been verified.
     */
    boolean isPhoneNumberVerified();
    /**
     * Returns address or <code>null</code>
     */
    AuthzAddress getAddress();

    /**
     * Returns updated_at or <code>null</code>
     */
    long getUpdatedAt();

    /**
     * Convert this object to a map
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
