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

package leap.core.aop;

import leap.core.AppConfig;
import leap.core.annotation.Inject;
import leap.core.annotation.M;
import leap.core.aop.config.MethodInterceptorConfig;
import leap.core.aop.matcher.AsmMethodInfo;
import leap.core.instrument.AppInstrumentClass;
import leap.core.instrument.AppInstrumentContext;
import leap.core.instrument.AsmInstrumentProcessor;
import leap.lang.Arrays2;
import leap.lang.Strings;
import leap.lang.Try;
import leap.lang.asm.*;
import leap.lang.asm.commons.GeneratorAdapter;
import leap.lang.asm.commons.Method;
import leap.lang.asm.tree.AnnotationNode;
import leap.lang.asm.tree.ClassNode;
import leap.lang.asm.tree.MethodNode;

import java.io.InputStream;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.*;
import java.util.function.Supplier;

import static leap.lang.asm.Opcodes.*;

public class AopInstrumentation extends AsmInstrumentProcessor {

    private static final Type PROVIDER_TYPE            = Type.getType(AopProvider.class);
    private static final Type INTERCEPTOR_TYPE         = Type.getType(MethodInterceptor.class);
    private static final Type SIMPLE_INTERCEPTION_TYPE = Type.getType(SimpleMethodInterception.class);
    private static final Type RUNNABLE_TYPE            = Type.getType(Runnable.class);
    private static final Type SUPPLIER_TYPE            = Type.getType(Supplier.class);
    private static final Type INJECT_TYPE              = Type.getType(Inject.class);
    private static final Type MANDATORY_TYPE           = Type.getType(M.class);
    private static final Type LAMBDA_FACTORY_TYPE      = Type.getType(LambdaMetafactory.class);
    private static final Type LOOKUP_TYPE              = Type.getType(MethodHandles.Lookup.class);
    private static final Type HANDLES_TYPE             = Type.getType(MethodHandles.class);
    private static final Type METHOD_TYPE              = Type.getType(java.lang.reflect.Method.class);
    private static final Type CLASS_TYPE               = Type.getType(Class.class);

    private static final String LOOKUP_NAME              = "Lookup";
    private static final String PROVIDER_FIELD           = "$$aopProvider";
    private static final String INTERCEPTOR_FIELD_PREFIX = "$$aopInterceptor$";
    private static final String METHOD_FIELD_PREFIX      = "$$aopMethod$";

    private static final Method METHOD_METADATA_FACTORY;
    private static final Method METHOD_PROVIDER_RUN1;
    private static final Method METHOD_PROVIDER_RUN2;
    private static final Method INTERCEPTION_CONSTRUCTOR1;
    private static final Method INTERCEPTION_CONSTRUCTOR2;
    private static final Method INTERCEPTION_CONSTRUCTOR3;
    private static final Method INTERCEPTION_CONSTRUCTOR4;
    private static final Method METHOD_CLASS_GET_METHOD;

    static {
        try {
            java.lang.reflect.Method metadataFactory =
                    LambdaMetafactory.class
                        .getMethod("metafactory",
                                MethodHandles.Lookup.class,
                                String.class,
                                MethodType.class,
                                MethodType.class,
                                MethodHandle.class,
                                MethodType.class);

            METHOD_METADATA_FACTORY = Method.getMethod(metadataFactory);

            java.lang.reflect.Constructor c1 =
                    SimpleMethodInterception.class
                        .getConstructor(Object.class, java.lang.reflect.Method.class,
                                        MethodInterceptor[].class, Runnable.class);

            java.lang.reflect.Constructor c2 =
                    SimpleMethodInterception.class
                            .getConstructor(Object.class, java.lang.reflect.Method.class, Object[].class,
                                            MethodInterceptor[].class, Runnable.class);

            java.lang.reflect.Constructor c3 =
                    SimpleMethodInterception.class
                            .getConstructor(Object.class, java.lang.reflect.Method.class,
                                            MethodInterceptor[].class, Supplier.class);

            java.lang.reflect.Constructor c4 =
                    SimpleMethodInterception.class
                            .getConstructor(Object.class, java.lang.reflect.Method.class, Object[].class,
                                            MethodInterceptor[].class, Supplier.class);


            INTERCEPTION_CONSTRUCTOR1 = Method.getMethod(c1);
            INTERCEPTION_CONSTRUCTOR2 = Method.getMethod(c2);
            INTERCEPTION_CONSTRUCTOR3 = Method.getMethod(c3);
            INTERCEPTION_CONSTRUCTOR4 = Method.getMethod(c4);

            java.lang.reflect.Method run1 =
                    AopProvider.class
                            .getMethod("run", MethodInterception.class);

            java.lang.reflect.Method run2 =
                    AopProvider.class
                            .getMethod("runWithResult", MethodInterception.class);

            METHOD_PROVIDER_RUN1 = Method.getMethod(run1);
            METHOD_PROVIDER_RUN2 = Method.getMethod(run2);

            java.lang.reflect.Method m = Class.class.getMethod("getMethod", String.class, Class[].class);
            METHOD_CLASS_GET_METHOD = Method.getMethod(m);

        }catch(NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    private AopConfig config;

    @Override
    public void init(AppConfig config) {
        this.config = config.getExtension(AopConfig.class);
    }

    @Override
    protected boolean preInstrument(AppInstrumentContext context) {
        if(null == config || !config.isEnabled()) {
            return false;
        }
        return true;
    }

    @Override
    protected void processClass(AppInstrumentContext context, AppInstrumentClass ic, ClassInfo ci, boolean methodBodyOnly) {
        ClassNode cn = ci.cn;

        boolean isIgnore = isFrameworkClass(ci);
        if (isIgnore) {
            return;
        }

        List<AopMethod> methods = new ArrayList<>();

        for (MethodNode mn : cn.methods) {

            //static method not supported.
            if(ASM.isStatic(mn)) {
                continue;
            }

            AopMethod method = new AopMethod(cn, mn);

            List<MethodInterceptorConfig> interceptors = config.getMethodInterceptors(method);
            if(null != interceptors) {
                method.interceptors = interceptors;
                methods.add(method);
            }

        }

        if (methods.isEmpty()) {
            log.trace("Ignore class '{}', can't found any intercepted method(s).", ic.getClassName());
            return;
        } else {

            if(cn.version < 52) {
                log.warn("Cannot instrument class '{}' of version '{}' for aop",ic.getClassName(), cn.version);
                return;
            }

            Try.throwUnchecked(() -> {
                try (InputStream in = ci.is.getInputStream()) {

                    byte[] bytes = instrumentClass(cn, new ClassReader(in), methods);

                    context.updateInstrumented(ic, this.getClass(), bytes, true);
                }
            });
        }
    }

    protected byte[] instrumentClass(ClassNode cn, ClassReader cr, List<AopMethod> methods) {
        AopClassVisitor visitor = new AopClassVisitor(cn ,new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES), methods);

        cr.accept(visitor, ClassReader.EXPAND_FRAMES);

        byte[] data = visitor.getClassData();

        return data;
    }

    protected static class AopClassVisitor extends ClassVisitor {

        private final ClassNode       cn;
        private final ClassWriter     cw;
        private final Type            type;
        private final List<AopMethod> methods;

        private final Map<String, String> nameInterceptorFields  = new HashMap<>();
        private final Map<String, String> classInterceptorFields = new HashMap<>();

        private boolean lambadaInnerClassVisited;
        private boolean visitStaticInit;

        public AopClassVisitor(ClassNode cn, ClassWriter cw, List<AopMethod> methods) {
            super(ASM.API, cw);
            this.cn = cn;
            this.cw = cw;
            this.type = Type.getObjectType(cn.name);
            this.methods = methods;

            init();
        }

        private void init() {

            int count = 1;
            for(int i=1;i<=methods.size();i++) {
                AopMethod am = methods.get(i-1);

                am.field    = METHOD_FIELD_PREFIX + i;
                am.argTypes = ASM.getArgumentTypes(am.getMethod());

                for(MethodInterceptorConfig interceptor : am.interceptors) {
                    if(!Strings.isEmpty(interceptor.getBeanName())) {
                        if(!nameInterceptorFields.containsKey(interceptor.getBeanName())) {
                            nameInterceptorFields.put(interceptor.getBeanName(), INTERCEPTOR_FIELD_PREFIX + count);
                            count++;
                        }
                    }else{
                        if(!classInterceptorFields.containsKey(interceptor.getClassName())) {
                            classInterceptorFields.put(interceptor.getClassName(), INTERCEPTOR_FIELD_PREFIX + count);
                            count++;
                        }
                    }
                }
            }

        }

        private String getFieldName(MethodInterceptorConfig interceptor) {
            if(!Strings.isEmpty(interceptor.getBeanName())) {
                return nameInterceptorFields.get(interceptor.getBeanName());
            }else{
                return classInterceptorFields.get(interceptor.getClassName());
            }
        }

        private Type getFieldType(MethodInterceptorConfig interceptor) {
            if(!Strings.isEmpty(interceptor.getBeanName())) {
                return INTERCEPTOR_TYPE;
            }else{
                return Type.getType(ASM.getObjectTypeDescriptor(interceptor.getClassName()));
            }
        }

        private AopMethod isIntercepted(MethodNode m) {
            for(AopMethod am : methods) {
                if(am.getMethod() == m) {
                    return am;
                }
            }
            return null;
        }

        public byte[] getClassData() {
            return cw.toByteArray();
        }

        @Override
        public void visitInnerClass(String name, String outerName, String innerName, int access) {
            if(name.equals(LOOKUP_TYPE.getInternalName()) &&
                    outerName.equals(HANDLES_TYPE.getInternalName()) &&
                    LOOKUP_NAME.equals(innerName)) {

                lambadaInnerClassVisited = true;

            }
            super.visitInnerClass(name, outerName, innerName, access);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            MethodNode mn = ASM.getMethod(cn, name, desc);

            if(ASM.isStaticInit(mn)) {
                visitStaticInit = true;
                MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
                return new ClinitMethodVisitor(mv, access, name, desc);
            }

            AopMethod am = isIntercepted(mn);

            if(null != am) {
                String newName = name + "$aop";

                visitInterceptedMethod(am, newName);

                return super.visitMethod(access, newName, desc, signature, exceptions);
            }else{
                return super.visitMethod(access, name, desc, signature, exceptions);
            }
        }

        protected void visitInterceptedMethod(AopMethod am, String newName) {
            MethodNode m = am.getMethod();

            MethodVisitor real = cw.visitMethod(m.access, m.name, m.desc, null,
                    m.exceptions == null ? null : m.exceptions.toArray(Arrays2.EMPTY_STRING_ARRAY));

            GeneratorAdapter mv = new GeneratorAdapter(real, m.access, m.name, m.desc);

            if(null != m.visibleAnnotations) {
                for (AnnotationNode an : m.visibleAnnotations) {
                    AnnotationVisitor av = mv.visitAnnotation(an.desc, true);
                    an.accept(av);
                }
            }
            if(null != m.invisibleAnnotations) {
                for (AnnotationNode an : m.invisibleAnnotations) {
                    AnnotationVisitor av = mv.visitAnnotation(an.desc, true);
                    an.accept(av);
                }
            }

            mv.visitCode();

            List<MethodInterceptorConfig> interceptors = am.interceptors;

            boolean hasReturnValue = ASM.hasReturnValue(m);
            Type[]  argumentTypes  = am.argTypes;

            mv.visitTypeInsn(NEW, SIMPLE_INTERCEPTION_TYPE.getInternalName());
            mv.visitInsn(DUP);

            //object
            mv.loadThis();

            //method
            mv.getStatic(type, am.field, METHOD_TYPE);

            //args
            if(argumentTypes.length > 0) {
                mv.loadArgArray();
            }

            //interceptors
            mv.push(interceptors.size());
            mv.newArray(INTERCEPTOR_TYPE);
            for(int i=0;i<interceptors.size();i++) {
                MethodInterceptorConfig interceptor = interceptors.get(i);

                mv.dup();
                mv.push(i);
                mv.loadThis();
                mv.getField(type, getFieldName(interceptor), getFieldType(interceptor));
                mv.arrayStore(INTERCEPTOR_TYPE);
            }

            //runnable with lambada
            mv.loadThis();
            if(argumentTypes.length > 0) {
                mv.loadArgs();
            }


            Handle bsm = new Handle(Opcodes.H_INVOKESTATIC,
                    LAMBDA_FACTORY_TYPE.getInternalName(),
                    METHOD_METADATA_FACTORY.getName(),
                    METHOD_METADATA_FACTORY.getDescriptor());

            List<Type> types = new ArrayList<>();
            types.add(type);
            for(Type at : argumentTypes) {
                types.add(at);
            }

            Object[] bsmArgs;

            if(hasReturnValue) {
                Method method = new Method("t", SUPPLIER_TYPE, types.toArray(new Type[0]));

                bsmArgs = new Object[]{
                        Type.getType("()Ljava/lang/Object;"),
                        new Handle(Opcodes.H_INVOKESPECIAL, type.getInternalName(), newName, m.desc),
                        Type.getType("()Ljava/lang/Object;")
                };

                mv.invokeDynamic("get", method.getDescriptor(), bsm, bsmArgs);
            }else{
                Method method = new Method("t", RUNNABLE_TYPE, types.toArray(new Type[0]));

                bsmArgs = new Object[]{
                        Type.getType("()V"),
                        new Handle(Opcodes.H_INVOKESPECIAL, type.getInternalName(), newName, m.desc),
                        Type.getType("()V")
                };

                mv.invokeDynamic("run", method.getDescriptor(), bsm, bsmArgs);
            }

            //call constructor
            if(!hasReturnValue) {
                if(argumentTypes.length == 0) {
                    mv.invokeConstructor(SIMPLE_INTERCEPTION_TYPE, INTERCEPTION_CONSTRUCTOR1);
                }else{
                    mv.invokeConstructor(SIMPLE_INTERCEPTION_TYPE, INTERCEPTION_CONSTRUCTOR2);
                }
            }else{
                if(argumentTypes.length == 0) {
                    mv.invokeConstructor(SIMPLE_INTERCEPTION_TYPE, INTERCEPTION_CONSTRUCTOR3);
                }else{
                    mv.invokeConstructor(SIMPLE_INTERCEPTION_TYPE, INTERCEPTION_CONSTRUCTOR4);
                }
            }

            //store the interception
            int local = mv.newLocal(INTERCEPTOR_TYPE);
            mv.storeLocal(local);

            //call provider.run or runWithResult if has return value.
            mv.getStatic(type, PROVIDER_FIELD, PROVIDER_TYPE);
            mv.loadLocal(local);
            mv.invokeInterface(PROVIDER_TYPE, !hasReturnValue ? METHOD_PROVIDER_RUN1 : METHOD_PROVIDER_RUN2);

            if(hasReturnValue) {
                mv.checkCast(ASM.getReturnType(m));
            }

            mv.returnValue();
            mv.visitMaxs(0,0);
            mv.visitEnd();
        }

        @Override
        public void visitEnd() {

            if(!lambadaInnerClassVisited) {
                cw.visitInnerClass(LOOKUP_TYPE.getInternalName(), HANDLES_TYPE.getInternalName(), LOOKUP_NAME, ACC_PUBLIC + ACC_FINAL + ACC_STATIC);
            }

            if(!visitStaticInit) {
                MethodVisitor real = cw.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
                ClinitMethodVisitor mv = new ClinitMethodVisitor(real, ACC_STATIC, "<clinit>", "()V");
                mv.visitCode();
                mv.returnValue();
                mv.visitMaxs(0,0);
                mv.visitEnd();
            }

            //visit fields
            FieldVisitor fv = cw.visitField(Opcodes.ACC_PRIVATE + Opcodes.ACC_FINAL + Opcodes.ACC_STATIC,
                                            PROVIDER_FIELD,
                                            PROVIDER_TYPE.getDescriptor(), null, null);
            fv.visitEnd();

            for(Map.Entry<String,String> field : classInterceptorFields.entrySet()) {

                fv = cw.visitField(Opcodes.ACC_PRIVATE, field.getValue(), ASM.getObjectTypeDescriptor(field.getKey()), null, null);
                {
                    AnnotationVisitor av = fv.visitAnnotation(INJECT_TYPE.getDescriptor(), true);
                    av.visitEnd();
                }
                {
                    AnnotationVisitor av = fv.visitAnnotation(MANDATORY_TYPE.getDescriptor(), true);
                    av.visitEnd();
                }
                fv.visitEnd();
            }

            for(Map.Entry<String,String> field : nameInterceptorFields.entrySet()) {

                fv = cw.visitField(Opcodes.ACC_PRIVATE, field.getValue(), INTERCEPTOR_TYPE.getDescriptor(), null, null);
                {
                    AnnotationVisitor av = fv.visitAnnotation(INJECT_TYPE.getDescriptor(), true);
                    av.visit("name", field.getKey());
                    av.visitEnd();
                }
                {
                    AnnotationVisitor av = fv.visitAnnotation(MANDATORY_TYPE.getDescriptor(), true);
                    av.visitEnd();
                }
                fv.visitEnd();
            }

            for(AopMethod am : methods) {
                fv = cw.visitField(ACC_PRIVATE + ACC_FINAL + ACC_STATIC, am.field, METHOD_TYPE.getDescriptor(), null, null);
                fv.visitEnd();
            }

            super.visitEnd();
        }

        protected final class ClinitMethodVisitor extends GeneratorAdapter {

            public ClinitMethodVisitor(MethodVisitor mv, int access, String name, String desc) {
                super(ASM.API, mv, access, name, desc);
            }

            @Override
            public void visitCode() {
                super.visitCode();

                mv.visitLdcInsn(PROVIDER_TYPE);
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, "leap/lang/Factory", "getInstance", "(Ljava/lang/Class;)Ljava/lang/Object;", false);
                checkCast(PROVIDER_TYPE);
                putStatic(type, PROVIDER_FIELD, PROVIDER_TYPE);

                for(AopMethod am : methods) {
                    MethodNode m     = am.getMethod();
                    String     field = am.field;

                    visitLdcInsn(type);
                    visitLdcInsn(m.name);

                    push(am.argTypes.length);
                    newArray(CLASS_TYPE);
                    for(int i=0;i<am.argTypes.length;i++) {
                        Type type = am.argTypes[i];
                        dup();
                        push(i);
                        push(type);
                        arrayStore(CLASS_TYPE);
                    }

                    invokeVirtual(CLASS_TYPE, METHOD_CLASS_GET_METHOD);
                    putStatic(type, field, METHOD_TYPE);
                }
            }
        }
    }

    protected static final class AopMethod extends AsmMethodInfo {

        private List<MethodInterceptorConfig> interceptors;

        private String field;
        private Type[] argTypes;

        public AopMethod(ClassNode c, MethodNode m) {
            super(c, m);
        }

    }

}