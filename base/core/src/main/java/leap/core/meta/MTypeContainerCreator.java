/*
 *
 *  * Copyright 2016 the original author or authors.
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
package leap.core.meta;

import leap.lang.Creatable;
import leap.lang.meta.MTypeFactory;
import leap.lang.meta.MTypeListener;
import leap.lang.meta.MTypeStrategy;

public interface MTypeContainerCreator extends Creatable<MTypeFactory> {

    /**
     * Sets the listener.
     */
	MTypeContainerCreator setListener(MTypeListener listener);

    /**
     * Sets the strategy.
     */
	MTypeContainerCreator setStrategy(MTypeStrategy strategy);

    /**
     * Sets true if always return {@link leap.lang.meta.MTypeRef} of complex type.
     */
    MTypeContainerCreator setAlwaysReturnComplexTypeRef(boolean b);

    /**
     * Creates the {@link MTypeContainer}.
     */
	MTypeContainer create();
	
}