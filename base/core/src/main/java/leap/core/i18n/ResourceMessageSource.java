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

import leap.core.AppResource;
import leap.core.annotation.Inject;
import leap.core.validation.annotations.NotNull;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;

public class ResourceMessageSource extends CachableMessageSource {
    private final Log log = LogFactory.get(this.getClass());

    protected @NotNull Map<String, Message> messages        = new HashMap<>();
    protected @NotNull Map<String, Message> defaultMessages = new HashMap<>();
    protected @Inject  MessageReader[]      readers;

    @Override
    protected Message getLocaleMessage(String key) {
        return messages.get(key);
    }

    @Override
    protected Message getDefaultMessage(String key) {
        return defaultMessages.get(key);
    }

    public ResourceMessageSource readFromResources(AppResource... resources) {
        if (resources.length > 0) {
            DefaultMessageContext context = new DefaultMessageContext(defaultLocale, false, messages, defaultMessages);
            for (AppResource resource : resources) {
                readFromResource(context, resource);
            }
        }
        return this;
    }

    protected void readFromResource(MessageContext context, AppResource ar) {
        if (null != ar) {
            context.setDefaultOverride(ar.isDefaultOverride());
            for (MessageReader reader : readers) {
                log.debug("Try read '{}' by '{}'", ar, reader.getClass().getSimpleName());
                if (reader.read(context, ar.getResource())) {
                    log.info("Read '{}' by '{}'", ar, reader.getClass().getSimpleName());
                    break;
                }
            }
            context.resetDefaultOverride();
        }
    }
}
