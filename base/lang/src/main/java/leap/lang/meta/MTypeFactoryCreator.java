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

import leap.lang.Creatable;

public interface MTypeFactoryCreator extends Creatable<MTypeFactory> {

    /**
     * Sets the listener.
     */
	MTypeFactoryCreator setListener(MTypeListener listener);

    /**
     * Sets the strategy.
     */
	MTypeFactoryCreator setStrategy(MTypeStrategy strategy);

    /**
     * Creates the {@link MTypeFactory}.
     */
	MTypeFactory create();
	
}