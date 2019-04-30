/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package leap.core.config.dyna;

import leap.core.config.dyna.exception.UnsupportedBindDynaPropertyException;
import leap.core.config.dyna.exception.UnsupportedDynaPropertyException;
import leap.core.config.dyna.exception.UnsupportedRawPropertyException;

import java.lang.reflect.Type;

/**
 * Used by {@link leap.core.AppConfig} for dynamic {@link Property} configuration.
 */
public interface PropertyProvider {

    /**
     * Returns the raw property value or null if not exists.
     *
     * @throws if this raw property are unsupported by this provider
     */
    String getRawProperty(String name) throws UnsupportedRawPropertyException;

    /**
     * Returns the {@link Property} for the given type.
     *
     * @throws if this dyna property are unsupported by this provider
     */
    <T> Property<T> getDynaProperty(String name, Type type, Class<T> cls) throws UnsupportedDynaPropertyException;

    /**
     * Binding the property.
     *
     * @throws if this dyna property are unsupported bind by this provider
     */
    <T> void bindDynaProperty(String name, Class<T> type, Property<T> p) throws UnsupportedBindDynaPropertyException;

    /**
     * Returns the {@link StringProperty}.
     *
     * @throws if this dyna property are unsupported by this provider
     */
    default StringProperty getDynaProperty(String name) throws UnsupportedDynaPropertyException {
        return new WrappedStringProperty(getDynaProperty(name, String.class, String.class));
    }

    /**
     * Returns the {@link IntegerProperty}.
     * <p>
     * <p/>
     * The returned {@link Property} object cannot be null.
     *
     * @throws if this dyna property are unsupported by this provider
     */
    default IntegerProperty getDynaIntegerProperty(String name) throws UnsupportedDynaPropertyException {
        return new WrappedIntegerProperty(getDynaProperty(name, Integer.class, Integer.class));
    }

    /**
     * Returns the {@link LongProperty}.
     * <p>
     * <p/>
     * The returned {@link Property} object cannot be null.
     *
     * @throws if this dyna property are unsupported by this provider
     */
    default LongProperty getDynaLongProperty(String name) throws UnsupportedDynaPropertyException {
        return new WrappedLongProperty(getDynaProperty(name, Long.class, Long.class));
    }

    /**
     * Returns the {@link BooleanProperty}.
     * <p>
     * <p/>
     * The returned {@link Property} object cannot be null.
     *
     * @throws if this dyna property are unsupported by this provider
     */
    default BooleanProperty getDynaBooleanProperty(String name) throws UnsupportedDynaPropertyException {
        return new WrappedBooleanProperty(getDynaProperty(name, Boolean.class, Boolean.class));
    }

    /**
     * Returns the {@link DoubleProperty}.
     * <p>
     * <p/>
     * The returned {@link Property} object cannot be null.
     *
     * @throws if this dyna property are unsupported by this provider
     */
    default DoubleProperty getDynaDoubleProperty(String name) throws UnsupportedDynaPropertyException {
        return new WrappedDoubleProperty(getDynaProperty(name, Double.class, Double.class));
    }

}