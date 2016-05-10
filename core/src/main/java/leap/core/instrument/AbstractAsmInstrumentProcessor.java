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

import leap.lang.Classes;
import leap.lang.Exceptions;
import leap.lang.Strings;
import leap.lang.asm.ClassReader;
import leap.lang.io.IO;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.resource.Resource;
import leap.lang.resource.ResourceSet;
import leap.lang.resource.Resources;
import leap.lang.time.StopWatch;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractAsmInstrumentProcessor implements AppInstrumentProcessor {

    private final Log log = LogFactory.get(this.getClass());

    @Override
    public void instrument(final AppInstrumentContext context,final ResourceSet rs) {

        final AtomicInteger counter = new AtomicInteger();
        StopWatch sw = StopWatch.startNew();

        preInstrument(context, rs);

        rs.process((resource) -> {
            if(resource.exists()){
                String filename = resource.getFilename();

                if(null != filename &&
                        filename.endsWith(Classes.CLASS_FILE_SUFFIX)){

                    InputStream is = null;

                    try{
                        is = resource.getInputStream();

                        ClassReader cr = new ClassReader(is);

                        if(acceptsClass(context, rs, cr)) {
                            AppInstrumentClass ic = context.getInstrumentedClass(cr.getClassName());
                            if(null != ic) {
                                log.debug("Class '{}' already instrumented, use the instrumented class instead.", cr.getClassName());
                                cr = new ClassReader(ic.getClassData());
                            }

                            processClass(context, rs, resource, cr);
                            counter.incrementAndGet();
                        }
                    }catch(IOException e){
                        throw Exceptions.wrap(e);
                    }catch(Exception e){
                        throw new AppInstrumentException("Error instrument class '" + resource.getFilename() + "'",e);
                    }finally{
                        IO.close(is);
                    }
                }
            }
        });

        postInstrument(context, rs);

        log.debug("Process {} classes by '{}' used {}ms",counter.get(), this.getClass().getName(), sw.getElapsedMilliseconds());
    }

    protected void preInstrument(AppInstrumentContext context, ResourceSet rs) {

    }

    protected void postInstrument(AppInstrumentContext context, ResourceSet rs) {

    }

    protected boolean acceptsClass(AppInstrumentContext context, ResourceSet rs,  ClassReader cr) {
        return Modifier.isPublic(cr.getAccess()) && !Modifier.isAbstract(cr.getAccess());
    }

    protected abstract void processClass(AppInstrumentContext context, ResourceSet rs, Resource resource, ClassReader cr) ;

    /**
     * Reads the internal class of super class.
     */
    protected static String readSuperName(Resource base, String internalClassName){
        String classUrl = Strings.remove(base.getURLString(), base.getClasspath()) + internalClassName + Classes.CLASS_FILE_SUFFIX;

        Resource resource = Resources.getResource(classUrl);
        if(null != resource && resource.exists()){
            InputStream is = null;
            try{
                is = resource.getInputStream();

                return new ClassReader(is).getSuperName();
            }catch(IOException e) {
                throw Exceptions.wrap(e);
            }finally{
                IO.close(is);
            }
        }

        return null;
    }
}
