/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package leap.core.i18n;

import leap.core.AppConfig;
import leap.core.AppConfigAware;
import leap.lang.Locales;
import leap.lang.Strings;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class CachableMessageSource extends AbstractMessageSource implements AppConfigAware {

    protected static final Message UNRESOLVED_MESSAGE = new Message("", null, "");

    protected Locale                            defaultLocale        = Locales.DEFAULT_LOCALE;
    protected Map<Locale, Map<String, Message>> cachedLocaleMessages = new ConcurrentHashMap<>();

    @Override
    public void setAppConfig(AppConfig config) {
        this.defaultLocale = config.getDefaultLocale();
    }

    @Override
    public String tryGetMessage(Locale locale, String key, Map<String, Object> vars, Object... args) {
        if (null == locale) {
            locale = defaultLocale;
        }

        Map<String, Message> localeMessages = cachedLocaleMessages.get(locale);
        if (null == localeMessages) {
            synchronized (this.cachedLocaleMessages) {
                localeMessages = new ConcurrentHashMap<>();
                cachedLocaleMessages.put(locale, localeMessages);
            }
        }

        Message message = localeMessages.get(key);
        if (message == UNRESOLVED_MESSAGE) {
            return null;
        }

        if (null == message) {
            message = doGetMessage(key, locale);
            if (null == message) {
                localeMessages.put(key, UNRESOLVED_MESSAGE);
                return null;
            } else {
                localeMessages.put(key, message);
            }
        }

        return formatMessage(message, vars, args);
    }

    protected String formatMessage(Message message, Map<String, Object> vars, Object... args) {
        if (args == null || args.length == 0) {
            return message.getString(vars);
        } else {
            return Strings.format(message.getString(vars), args);
        }
    }

    protected Message doGetMessage(String key, Locale locale) {
        String lang    = locale.getLanguage();
        String country = locale.getCountry();

        Message message;

        if (!Strings.isEmpty(country)) {
            if (null != (message = getLocaleMessage(key + "_" + lang + "_" + country))) {
                return message;
            }
        }

        if (null != (message = getLocaleMessage(key + "_" + lang))) {
            return message;
        }

        message = getLocaleMessage(key);
        if (null != message) {
            return message;
        }

        if (!locale.equals(defaultLocale)) {
            message = doGetMessage(key, defaultLocale);
            if (null != message) {
                return message;
            }
        }

        return getDefaultMessage(key);
    }

    /**
     * Returns the message of the key with locale
     */
    protected abstract Message getLocaleMessage(String key);

    /**
     * Returns the message of key without locale.
     */
    protected abstract Message getDefaultMessage(String key);
}
