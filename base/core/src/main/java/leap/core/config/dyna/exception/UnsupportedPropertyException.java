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

package leap.core.config.dyna.exception;

import leap.core.config.dyna.PropertyProvider;

/**
 * Top exception for {@link PropertyProvider}
 * @author kael.
 */
public abstract class UnsupportedPropertyException extends Exception{
    
    protected final String property;

    public UnsupportedPropertyException(String property) {
        this.property = property;
    }

    public UnsupportedPropertyException(String property,String message, Throwable cause) {
        super(message, cause);
        this.property = property;
    }

    public UnsupportedPropertyException(String property,Throwable cause) {
        super(cause);
        this.property = property;
    }

    protected UnsupportedPropertyException(String property,String message, Throwable cause, boolean enableSuppression,
                                           boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.property = property;
    }
}
