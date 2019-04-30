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
package leap.core.i18n;

import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class DefaultMessageContext implements MessageContext {

    private final Set<String> resourceUrls = new HashSet<>();

    private boolean originalDefaultOverride;
    private boolean defaultOverride;

    private final Locale                      defaultLocale;
    private final Map<String, Message>        messages;
    private final Map<String, Message> defaultMessages;

    public DefaultMessageContext(Locale defaultLocale, boolean defaultOverride, Map<String, Message> messages, Map<String, Message> defaultMessages) {
        this.defaultLocale = defaultLocale;
        this.defaultOverride = defaultOverride;
        this.originalDefaultOverride = defaultOverride;
        this.messages = messages;
        this.defaultMessages = defaultMessages;
    }

    @Override
    public boolean isDefaultOverride() {
        return defaultOverride;
    }

    @Override
    public void setDefaultOverride(boolean b) {
        this.defaultOverride = b;
    }

    @Override
    public void resetDefaultOverride() {
        this.defaultOverride = originalDefaultOverride;
    }

    @Override
    public boolean containsResourceUrl(String url) {
        return resourceUrls.contains(url);
    }

    @Override
    public void addResourceUrl(String url) {
        resourceUrls.add(url);
    }

    @Override
    public Message tryGetMessage(Locale locale, String name) {
        return messages.get(getKey(locale, name));
    }

    @Override
    public void addMessage(Locale locale, String name, Message message) {
        messages.put(getKey(locale, name), message);

        if (!defaultMessages.containsKey(name) ||
                (isDefaultLocale(locale) && !isDefaultLocale(message.getLocale()))) {
            defaultMessages.put(name, message);
        }
    }

    protected boolean isDefaultLocale(Locale locale) {
        return null == locale || locale.equals(defaultLocale);
    }

    protected String getKey(Locale locale, String name) {
        return null == locale ? name : name + "_" + locale.toString();
    }

}
