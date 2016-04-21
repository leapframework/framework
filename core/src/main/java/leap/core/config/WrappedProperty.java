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

package leap.core.config;

import java.util.function.Consumer;

public class WrappedProperty<T> implements Property<T> {

    private final Property<T> wrapped;

    public WrappedProperty(Property<T> wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public T get() {
        return wrapped.get();
    }

    @Override
    public void set(T value) {
        wrapped.set(value);
    }

    @Override
    public void convert(String s) {
        wrapped.convert(s);
    }

    @Override
    public void onChanged(Consumer<T> callback) {
        wrapped.onChanged(callback);
    }
}