/*
 * Copyright 2015 the original author or authors.
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
package leap.lang.meta;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class SimpleMTypeFactoryCreator implements MTypeFactoryCreator {
	
	protected BiConsumer<Class<?>, MComplexType>         complextTypeCreatedListener;
	protected Function<Class<?>, String> 				 complextTypeLocalNamer;
	protected BiFunction<Class<?>, MComplexType, String> complextTypeFqNamer;
	
	@Override
    public MTypeFactoryCreator setComplexTypeCreatedListener(BiConsumer<Class<?>, MComplexType> listener) {
		this.complextTypeCreatedListener = listener;
        return this;
    }

	@Override
    public MTypeFactoryCreator setComplexTypeLocalNamer(Function<Class<?>, String> namer) {
		this.complextTypeLocalNamer = namer;
        return this;
    }

	@Override
    public MTypeFactoryCreator setComplexTypeFqNamer(BiFunction<Class<?>, MComplexType, String> fqNamer) {
		this.complextTypeFqNamer = fqNamer;
        return this;
    }

	@Override
    public MTypeFactory create() {
        return new SimpleMTypeFactory(complextTypeCreatedListener, complextTypeLocalNamer, complextTypeFqNamer);
    }
	
}