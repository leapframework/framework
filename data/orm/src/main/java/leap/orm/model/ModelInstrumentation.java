/*
 * Copyright 2013 the original author or authors.
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
package leap.orm.model;

import leap.core.instrument.AbstractAsmInstrumentProcessor;
import leap.core.instrument.AppInstrumentContext;
import leap.core.instrument.AppInstrumentProcessor;
import leap.lang.Classes;
import leap.lang.Exceptions;
import leap.lang.Factory;
import leap.lang.asm.*;
import leap.lang.asm.commons.GeneratorAdapter;
import leap.lang.asm.tree.ClassNode;
import leap.lang.asm.tree.MethodNode;
import leap.lang.io.IO;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.resource.Resource;
import leap.lang.resource.ResourceSet;
import leap.orm.annotation.Instrument;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class ModelInstrumentation extends AbstractAsmInstrumentProcessor implements AppInstrumentProcessor {
	
	private static final Log log = LogFactory.get(ModelInstrumentation.class);
	
	protected static final Type   MODEL_TYPE       = Type.getType(Model.class);
	protected static final String MODEL_CLASS_NAME = MODEL_TYPE.getInternalName();
	//protected static final String OC_CLASS_NAME    = Type.getInternalName(OrmContext.class);
	//protected static final String EM_CLASS_NAME    = Type.getInternalName(EntityMapping.class);
	
	protected static final ClassNode	MODEL_CLASS;
	protected static final MethodNode[] DELEGATE_METHODS;
	
	static {
		InputStream is = null;
		
		try{
			is = Model.class.getClassLoader().getResourceAsStream(Classes.getClassResourcePath(Model.class));

			MODEL_CLASS = ASM.getClassNode(new ClassReader(is));
			DELEGATE_METHODS = ASM.getMethods(MODEL_CLASS, (m) -> {
				if(ASM.isStaticInit(m) || ASM.isConstructor(m)){
					return false;
				}
				
				if(ASM.isAnnotationPresent(m, Instrument.class)){
					return true;
				}
				
                return false;
			});
		}catch(IOException e){
			throw Exceptions.wrap(e);
		}finally{
			IO.close(is);
		}
	}

    protected Set<InstrumentModelClass> instrumentModelClasses;
	protected List<ModelTransformer>    transformers;

    @Override
    protected void preInstrument(AppInstrumentContext context, ResourceSet rs) {
        this.transformers = Factory.newInstances(ModelTransformer.class);
        this.instrumentModelClasses = new TreeSet<>();
    }

    @Override
    protected void processClass(AppInstrumentContext context, ResourceSet rs, Resource resource, ClassReader cr) {
        int inheritLevels = 0;
        boolean isModel   = false;

        String superName = cr.getSuperName();
        if(MODEL_CLASS_NAME.equals(superName)){
            isModel = true;
        }else if(!superName.equals("java/lang/Object")){
            for(;;){
                superName = readSuperName(resource, superName);

                if(null == superName || superName.equals("java/lang/Object")){
                    break;
                }

                inheritLevels++;

                if(superName.equals(MODEL_CLASS_NAME)){
                    isModel = true;
                    break;
                }
            }
        }

        if(isModel){
            instrumentModelClasses.add(new InstrumentModelClass(inheritLevels, cr));
        }
    }

    @Override
    protected void postInstrument(AppInstrumentContext context, ResourceSet rs) {
        //Sorts the model class by inherit levels
        for(InstrumentModelClass mc : instrumentModelClasses){
            ClassReader cr = mc.getClassReader();

            if(context.isInstrumentedBy(cr.getClassName(), this.getClass())) {
                log.info("Model class '{}' already instrumented",cr.getClassName());
                continue;
            }

            log.debug("Instrument model class '{}'",cr.getClassName());
            ClassWriter cw = new ClassWriter(cr,ClassWriter.COMPUTE_FRAMES);
            instrument(context, null, cr,cw);
        }
    }

	protected void instrument(AppInstrumentContext context, ModelClassLoader cl, ClassReader cr, ClassWriter cw) {
		ClassNode cn = ASM.getClassNode(cr);
		
		transformClass(cr,cw, cn);
		instrumentDelegates(cr,cw);
		
		cw.visitEnd();
		byte[] data = cw.toByteArray();
		
        context.addInstrumentedClass(this.getClass(), cr.getClassName(), data);
	}
	
	protected void transformClass(ClassReader cr,ClassWriter cw, ClassNode cn){
		ClassVisitor cv = new ModelTransformVisitor(cn, cw, transformers);
		cr.accept(cv,0);
	}
	
	protected void instrumentDelegates(ClassReader cr,ClassWriter cw) {
		MethodVisitor    mv;
		GeneratorAdapter mg;
		
		for(MethodNode method : DELEGATE_METHODS){
			Type[] argumentTypes = ASM.getArgumentTypes(method);
			
			mv = ASM.visitMethod(cw, method);
			mg = new GeneratorAdapter(mv, method.access, method.name, method.desc);
			
			mg.visitCode();
			
			Label l0 = new Label(); //try label
			Label l1 = new Label(); //finally label
			Label l2 = new Label(); //catch label
			
			mg.visitTryCatchBlock(l0, l1, l2, null);
			
			//start try
			mg.visitLabel(l0);
			
			//--invoke : className.set(...)--
			instrumentSetClassName(cr, mg);
			
			//--invoke delegate method--
			
			//load 'this' variable
			if(!ASM.isStatic(method)){
				mg.loadThis();
			}
			
			//load argument variables
			for(int i=0;i<argumentTypes.length;i++){
				mg.loadArg(i);
			}
			
			//invoke method
			if(ASM.isStatic(method)){
				mg.visitMethodInsn(Opcodes.INVOKESTATIC, MODEL_CLASS_NAME, method.name, method.desc,false);
			}else{
				mg.visitMethodInsn(Opcodes.INVOKESPECIAL, MODEL_CLASS_NAME, method.name, method.desc,false);
			}
			
			//store return value
			int returnValueIndex = -1;
			if(ASM.hasReturnValue(method)){
				returnValueIndex = mg.newLocal(ASM.getReturnType(method));
				mg.storeLocal(returnValueIndex);
			}
			
			//--finally--
			mg.visitLabel(l1);
			
			//className.remove()
			instrumentRemoveClassName(cr, mg);
			
			//return value
			if(returnValueIndex >= 0){
				mg.loadLocal(returnValueIndex);
			}
			mg.returnValue();
			
			//--catch throwable--
			mv.visitLabel(l2);
			mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {"java/lang/Throwable"});
			
			int exceptionIndex = mg.newLocal(Type.getType(Throwable.class));
			mg.storeLocal(exceptionIndex);

			//className.remove
			instrumentRemoveClassName(cr, mg);

			mg.loadLocal(exceptionIndex);
			mg.visitInsn(Opcodes.ATHROW);

			mg.endMethod();
		}
	}
	
	protected void instrumentSetClassName(ClassReader cr,MethodVisitor mv){
		mv.visitFieldInsn(Opcodes.GETSTATIC, cr.getClassName(), "className", "Ljava/lang/ThreadLocal;");
		mv.visitLdcInsn(cr.getClassName().replace('/', '.'));
		mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/ThreadLocal", "set", "(Ljava/lang/Object;)V",false);
	}
	
	protected void instrumentRemoveClassName(ClassReader cr,MethodVisitor mv){
		mv.visitFieldInsn(Opcodes.GETSTATIC, cr.getClassName(), "className", "Ljava/lang/ThreadLocal;");
		mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/ThreadLocal", "remove", "()V",false);
	}
	
	protected static final class ModelClassLoader extends ClassLoader {
		
		private ClassLoader parent;
		private Method 	    parentDefineClassMethod;
		
		protected ModelClassLoader(ClassLoader parent){
			super(parent);
			
			this.parent = parent;
			
			try {
				parentDefineClassMethod = ClassLoader.class.getDeclaredMethod("defineClass", 
				                                                    new Class[] {String.class, byte[].class, int.class,int.class});
				
				parentDefineClassMethod.setAccessible(true);
			} catch (Exception ignored) {
			    //do nothing
			}
		}

		
		Class<?> defineClass (String name, byte[] bytes) throws ClassFormatError {
			try {
				return (Class<?>)parentDefineClassMethod.invoke(parent, new Object[] {name, bytes, new Integer(0), new Integer(bytes.length)});
			} catch (InvocationTargetException e){
				throw new RuntimeException("Error instrument class '" + name + "', " + e.getTargetException().getMessage(), e.getTargetException());
			} catch (Exception e){
				throw new RuntimeException("Error instrument class '" + name + "', " + e.getMessage(), e);
			}
		}
	}
	
	protected static final class ModelTransformVisitor extends ClassVisitor {

		private final ClassNode				 cn;
		private final ClassWriter		     cw;
		private final List<ModelTransformer> transformers;

		public ModelTransformVisitor(ClassNode cn, ClassWriter cw,List<ModelTransformer> transformers) {
	        super(ASM.API, cw);
	        this.cn			  = cn;
	        this.cw			  = cw;
	        this.transformers = transformers;
        }

		@Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
			MethodNode method = ASM.getMethod(cn, name, desc);
			
			MethodVisitor mv;
			
			for(ModelTransformer transformer : transformers){
				if((mv = transformer.transform(cn, cw, method)) != null){
					return mv;
				}
			}
			
	        return super.visitMethod(access, name, desc, signature, exceptions);
        }
	}
	
	private static class InstrumentModelClass implements Comparable<InstrumentModelClass> {
		
		private final int 		  inheritLevels;
		private final ClassReader classReader;
		
		private InstrumentModelClass(int ineritLevels,ClassReader classReader){
			this.inheritLevels = ineritLevels;
			this.classReader   = classReader;
		}
		
		public ClassReader getClassReader() {
			return classReader;
		}

		@Override
        public int compareTo(InstrumentModelClass o) {
	        if(this.inheritLevels < o.inheritLevels){
	        	return -1;
	        }
	        return 1;
        }
		
	}
}