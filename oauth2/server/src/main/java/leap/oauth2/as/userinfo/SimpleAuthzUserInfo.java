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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author kael.
 */
public class SimpleAuthzUserInfo implements AuthzUserInfo {
    
    protected String subject;
    protected String fullName;
    protected String givenName;
    protected String familyName;
    protected String middleName;
    protected String nickname;
    protected String preferredUsername;
    protected String profile;
    protected String picture;
    protected String website;
    protected String email;
    protected boolean emailVerified;
    protected String gender;
    protected String birthdate;
    protected String zoneinfo;
    protected String locale;
    protected String phoneNumber;
    protected boolean phoneNumberVerified;
    protected AuthzAddress address;
    protected long updatedAt;
    
    protected Map<String, Object> ext = new HashMap<>();
    
    @Override
    public String getSubject() {
        return subject;
    }

    @Override
    public String getFullName() {
        return fullName;
    }

    @Override
    public String getGivenName() {
        return givenName;
    }

    @Override
    public String getFamilyName() {
        return familyName;
    }

    @Override
    public String getMiddleName() {
        return middleName;
    }

    @Override
    public String getNickname() {
        return nickname;
    }

    @Override
    public String getPreferredUsername() {
        return preferredUsername;
    }

    @Override
    public String getProfile() {
        return profile;
    }

    @Override
    public String getPicture() {
        return picture;
    }

    @Override
    public String getWebsite() {
        return website;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public boolean isEmailVerified() {
        return emailVerified;
    }

    @Override
    public String getGender() {
        return gender;
    }

    @Override
    public String getBirthdate() {
        return birthdate;
    }

    @Override
    public String getZoneinfo() {
        return zoneinfo;
    }

    @Override
    public String getLocale() {
        return locale;
    }

    @Override
    public String getPhoneNumber() {
        return phoneNumber;
    }

    @Override
    public boolean isPhoneNumberVerified() {
        return phoneNumberVerified;
    }

    @Override
    public AuthzAddress getAddress() {
        return address;
    }

    @Override
    public long getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put(SUBJECT,getSubject());
        map.put(NAME,getFullName());
        map.put(GIVEN_NAME,getGivenName());
        map.put(FAMILY_NAME,getFamilyName());
        map.put(MIDDLE_NAME,getMiddleName());
        map.put(NICKNAME,getNickname());
        map.put(PREFERRED_USERNAME,getPreferredUsername());
        map.put(PROFILE,getProfile());
        map.put(PICTURE,getPicture());
        map.put(WEBSITE,getWebsite());
        map.put(EMAIL,getEmail());
        map.put(EMAIL_VERIFIED,isEmailVerified());
        map.put(GENDER,getGender());
        map.put(BIRTHDATE,getBirthdate());
        map.put(ZONEINFO,getZoneinfo());
        map.put(LOCALE,getLocale());
        map.put(PHONE_NUMBER,getPhoneNumber());
        map.put(PHONE_NUMBER_VERIFIED,isPhoneNumberVerified());
        map.put(ADDRESS,getAddress());
        map.put(UPDATED_AT,getUpdatedAt());
        ext.forEach((s, o) -> {
            if (!map.containsKey(s)){
                map.put(s,o);
            }
        });
        return map;
    }

    @Override
    public Map<String, Object> getExtProperties() {
        return Collections.unmodifiableMap(ext);
    }

    @Override
    public void putExtProperty(String name, Object value) {
        ext.put(name,value);
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setPreferredUsername(String preferredUsername) {
        this.preferredUsername = preferredUsername;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }

    public void setZoneinfo(String zoneinfo) {
        this.zoneinfo = zoneinfo;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setPhoneNumberVerified(boolean phoneNumberVerified) {
        this.phoneNumberVerified = phoneNumberVerified;
    }

    public void setAddress(AuthzAddress address) {
        this.address = address;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
}
