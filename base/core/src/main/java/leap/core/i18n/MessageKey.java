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

package leap.core.i18n;

import java.util.Locale;

/**
 * Created by kael on 2017/3/1.
 */
public class MessageKey {
    private Locale locale;
    private String key;
    private Object[] args;
    
    public MessageKey(String key) {
        this(null,key,null);
    }

    public MessageKey(String key, Object[] args) {
        this(null,key,args);
    }

    public MessageKey(Locale locale, String key, Object[] args) {
        if(key == null){
            throw new NullPointerException("message key can not be null!");
        }
        this.locale = locale;
        this.key = key;
        this.args = args;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }
}
