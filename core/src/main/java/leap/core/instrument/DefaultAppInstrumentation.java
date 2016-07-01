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

package leap.core.instrument;

import leap.core.AppConfig;
import leap.core.AppInitException;
import leap.lang.Factory;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.resource.Resource;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class DefaultAppInstrumentation implements AppInstrumentation {

    private static final Log log = LogFactory.get(DefaultAppInstrumentation.class);

    private final List<AppInstrumentProcessor> processors = Factory.newInstances(AppInstrumentProcessor.class);

    @Override
    public void init(AppConfig config) {
        for(AppInstrumentProcessor p : processors) {
            p.init(config);
        }
    }

    @Override
    public AppInstrumentClass tryInstrument(ClassLoader loader, Resource r, byte[] bytes, boolean methodBodyOnly) {
        DefaultAppInstrumentContext context = new DefaultAppInstrumentContext(loader);

        for(AppInstrumentProcessor p : processors){
            try {
                p.instrument(context, r, bytes, methodBodyOnly);
            } catch (Throwable e) {
                throw new AppInitException("Error calling instrument processor '" + p + "', " + e.getMessage(), e);
            }
        }

        AppInstrumentClass ic = null;
        if(!context.getAllInstrumentedClasses().isEmpty()){
            ic = context.getAllInstrumentedClasses().iterator().next();

            if(log.isDebugEnabled()) {
                log.debug("Instrument '{}' by [ {} ]",
                          ic.getClassName(),
                          getInstrumentedBy(ic.getAllInstrumentedBy()));
            }
        }
        return ic;
    }


    private String getInstrumentedBy(Set<AppInstrumentProcessor> classes) {
        StringBuilder s = new StringBuilder();

        final AtomicInteger i = new AtomicInteger(-1);
        classes.forEach(c -> {
            if(i.incrementAndGet() > 0) {
                s.append(" , ");
            }
            s.append(c.getClass().getSimpleName());
        });
        return s.toString();
    }

}