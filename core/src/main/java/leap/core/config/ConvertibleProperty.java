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

import leap.lang.Args;
import leap.lang.convert.Converts;

import java.util.function.Function;

public class ConvertibleProperty<T> extends AbstractProperty<T> {

    public static final Function<String,String>  STRING_CONVERTER  = (s) -> s;
    public static final Function<String,Boolean> BOOLEAN_CONVERTER = (s) -> Converts.convert(s,Boolean.class);
    public static final Function<String,Integer> INTEGER_CONVERTER = (s) -> Converts.convert(s,Integer.class);

    private Function<String,T> converter;

    public ConvertibleProperty(Function<String, T> converter) {
        Args.notNull(converter, "converter");
        this.converter = converter;
    }

    public ConvertibleProperty(Function<String, T> converter, T defaultValue) {
        Args.notNull(converter, "converter");
        this.converter = converter;
        this.value = defaultValue;
    }

    @Override
    public void convert(String property) {
        set(converter.apply(property));
    }

}