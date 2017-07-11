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

import leap.lang.exception.ObjectExistsException;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleMBeanExporter implements MBeanExporter {

    public static final String DEFAULT_DOMAIN = "beans";

    private static final Log log = LogFactory.get(SimpleMBeanExporter.class);

    protected final MBeanServer server;
    protected final String      domain;
    protected final Map<ObjectName, Object> exportedBeans = new ConcurrentHashMap<>();

    public SimpleMBeanExporter() {
        this(ManagementFactory.getPlatformMBeanServer(), DEFAULT_DOMAIN);
    }

    public SimpleMBeanExporter(MBeanServer server) {
        this(server, DEFAULT_DOMAIN);
    }

    public SimpleMBeanExporter(MBeanServer server, String domain) {
        this.server = server;
        this.domain = domain;
    }

    @Override
    public MBeanServer getServer() {
        return server;
    }

    @Override
    public String getDomain() {
        return domain;
    }

    public ObjectName createObjectName(String name) {
        name = domain + ":name=" + name;
        try {
            return new ObjectName(name);
        } catch (MalformedObjectNameException e) {
            throw new MException(e);
        }
    }

    @Override
    public void export(String name, Object bean) {
        export(createObjectName(name), bean);
    }

    @Override
    public void export(ObjectName name, Object bean) {
        if(exportedBeans.containsKey(name)) {
            throw new ObjectExistsException("Jmx bean '" + name + "' already exported!");
        }

        MBean mbean = new MBeanBuilder(bean).build();

        try {
            server.registerMBean(mbean, name);
        } catch (MBeanRegistrationException e) {
            throw new MException(e.getMessage(), e);
        } catch (NotCompliantMBeanException e) {
            throw new IllegalStateException(e);
        } catch (InstanceAlreadyExistsException e) {
            // ignore when auto-redeploy.
        }

        exportedBeans.put(name, Boolean.TRUE);
    }

    @Override
    public void unexportAll() {
        for(ObjectName name : exportedBeans.keySet()) {
            unexport(name);
        }
    }

    public boolean unexport(ObjectName name) {
        try {
            server.unregisterMBean(name);
            return true;
        } catch (InstanceNotFoundException e) {
            log.info("The mbean '{}' not exists!", name);
        } catch (Exception e) {
            log.warn("Error unexport mbean '" + name + "'", e);
        }
        return false;
    }
}
