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

import leap.core.annotation.Bean;
import leap.lang.Classes;
import leap.lang.Exceptions;
import leap.lang.Strings;
import leap.lang.asm.ASM;
import leap.lang.asm.ClassReader;
import leap.lang.asm.tree.ClassNode;
import leap.lang.io.ByteArrayInputStreamSource;
import leap.lang.io.IO;
import leap.lang.io.InputStreamSource;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.resource.Resource;
import leap.lang.resource.Resources;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;

public abstract class AsmInstrumentProcessor implements AppInstrumentProcessor {

    protected final Log log = LogFactory.get(this.getClass());

    protected boolean isFrameworkClass(ClassInfo ci) {
        return ci.cr.getClassName().startsWith("leap/");
    }

    @Override
    public void instrument(AppInstrumentContext context, Resource resource, byte[] bytes, boolean methodBodyOnly) {
        if(!preInstrument(context)){
            return;
        }

        try{
            ClassReader cr = new ClassReader(bytes);

            if(acceptsClass(context, cr)) {
                if(context.isInstrumentedBy(cr.getClassName(), this.getClass())) {
                    return;
                }

                InputStreamSource  is = new ByteArrayInputStreamSource(bytes);
                AppInstrumentClass ic = context.getInstrumentedClass(cr.getClassName());
                if(null != ic) {
                    is = new ByteArrayInputStreamSource(ic.getClassData());
                    cr = new ClassReader(ic.getClassData());
                }else{
                    ic = context.newInstrumentedClass(cr.getClassName());
                }

                ClassNode cn = ASM.getClassNode(cr);

                ClassInfo ci = new ClassInfo();
                ci.rs = resource;
                ci.is = is;
                ci.cr = cr;
                ci.cn = cn;

                //todo : optimize performance?
                processClass(context, ic, ci, methodBodyOnly);

                if(null == ic) {
                    ic = context.getInstrumentedClass(cr.getClassName());
                }

                if(null != ic && !ic.isBeanDeclared()) {
                    ic.setBeanDeclared(ASM.isAnnotationPresent(cn, Bean.class));
                }
            }
        }catch(Exception e){
            throw new AppInstrumentException("Error instrument class '" + resource.getFilename() + "'",e);
        }

        postInstrument(context);
    }

    protected boolean preInstrument(AppInstrumentContext context) {
        return true;
    }

    protected void postInstrument(AppInstrumentContext context) {

    }

    protected boolean acceptsClass(AppInstrumentContext context, ClassReader cr) {
        return Modifier.isPublic(cr.getAccess()) && !Modifier.isAbstract(cr.getAccess());
    }

    protected abstract void processClass(AppInstrumentContext context, AppInstrumentClass ic, ClassInfo ci, boolean methodBodyOnly) ;

    /**
     * Reads the internal class of super class.
     */
    protected static String readSuperName(Resource base, String internalClassName){
        String classUrl = "classpath:" + internalClassName + Classes.CLASS_FILE_SUFFIX;

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

    protected static final class ClassInfo {
        public Resource          rs;
        public InputStreamSource is;
        public ClassReader       cr;
        public ClassNode         cn;
    }
}
