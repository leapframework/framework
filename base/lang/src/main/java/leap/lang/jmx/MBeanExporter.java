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

package leap.lang.jmx;

import leap.lang.Strings;
import leap.lang.exception.ObjectExistsException;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MBeanExporter {

    private static final Log log = LogFactory.get(MBeanExporter.class);

    protected final MBeanServer server;
    protected final String      namePrefix;
    protected final Map<ObjectName, Object> exportedBeans = new ConcurrentHashMap<>();

    public MBeanExporter() {
        this(ManagementFactory.getPlatformMBeanServer(), "app");
    }

    public MBeanExporter(MBeanServer server) {
        this(server, "app");
    }

    public MBeanExporter(MBeanServer server, String namePrefix) {
        this.server = server;
        this.namePrefix = namePrefix;
    }

    public ObjectName objectName(String name) {
        if(!Strings.isEmpty(namePrefix)) {
            name = namePrefix + ":name=" + name;
        }
        try {
            return new ObjectName(name);
        } catch (MalformedObjectNameException e) {
            throw new MException(e);
        }
    }

    public void export(String name, Object bean) {
        export(objectName(name), bean);
    }

    public void export(ObjectName name, Object bean) {
        if(exportedBeans.containsKey(name)) {
            throw new ObjectExistsException("Jmx bean '" + name + "' already exported!");
        }

        MBean mbean = new MBeanBuilder(bean).build();

        try {
            server.registerMBean(mbean, name);
        } catch (InstanceAlreadyExistsException | MBeanRegistrationException e) {
            throw new MException(e.getMessage(), e);
        } catch (NotCompliantMBeanException e) {
            throw new IllegalStateException(e);
        }

        exportedBeans.put(name, Boolean.TRUE);
    }

    public void unexportAll() {
        for(ObjectName name : exportedBeans.keySet()) {
            try {
                server.unregisterMBean(name);
            } catch (Exception e) {
                log.error("Error unexport mbean '" + name + "'", e);
            }
        }
    }
}
