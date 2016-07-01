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

package leap.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;

public class Agent {

    private static Map<Class<?>,? extends InstrumentClass> retransformClasses;
    private static boolean                                 retranformed;

    public static boolean retransform(Map<Class<?>,? extends InstrumentClass> classes) {
        retranformed = false;
        retransformClasses = classes;

        if(!AgentLoader.load()){
            return false;
        }

        return retranformed;
    }

    public static void agentmain(String args, Instrumentation inst) throws Exception {
        if(null != retransformClasses) {

            System.out.println();
            System.out.println("Retransform " + retransformClasses.size() + " classes!!!");
            System.out.println();

            final ClassTransformer transformer = new ClassTransformer(retransformClasses);
            inst.addTransformer(transformer, true);

            for(Class<?> c : retransformClasses.keySet()) {
                inst.retransformClasses(c);
            }

            inst.removeTransformer(transformer);
            retranformed = true;
        }
    }

    private static final class ClassTransformer implements ClassFileTransformer {

        private final Map<String,byte[]> classes;

        public ClassTransformer(Map<Class<?>,? extends InstrumentClass> classes) {
            this.classes = new HashMap<>(classes.size());
            for(Map.Entry<Class<?>,? extends InstrumentClass> entry : classes.entrySet()) {
                this.classes.put(entry.getKey().getName().replace('.','/'), entry.getValue().getClassData());
            }
        }

        @Override
        public byte[] transform(ClassLoader loader,
                                String className,
                                Class<?> classBeingRedefined,
                                ProtectionDomain protectionDomain,
                                byte[] classfileBuffer) throws IllegalClassFormatException {

            System.out.println("  do retransform '" + className + "'");
            return classes.get(className);
        }
    }

}