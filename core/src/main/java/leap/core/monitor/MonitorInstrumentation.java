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

package leap.core.monitor;

import leap.core.AppConfig;
import leap.core.annotation.Bean;
import leap.core.annotation.Inject;
import leap.core.annotation.Monitored;
import leap.core.instrument.AbstractAsmInstrumentProcessor;
import leap.core.instrument.AppInstrumentContext;
import leap.lang.Try;
import leap.lang.asm.*;
import leap.lang.asm.commons.AdviceAdapter;
import leap.lang.asm.commons.Method;
import leap.lang.asm.tree.ClassNode;
import leap.lang.asm.tree.MethodNode;
import leap.lang.io.InputStreamSource;
import leap.lang.resource.Resource;
import leap.lang.resource.ResourceSet;

import java.io.InputStream;
import java.lang.reflect.Modifier;

public class MonitorInstrumentation extends AbstractAsmInstrumentProcessor {

    private static final Type NOP_PROVIDER_TYPE = Type.getType(NopMonitorProvider.class);
    private static final Type PROVIDER_TYPE     = Type.getType(MonitorProvider.class);
    private static final Type MONITOR_TYPE      = Type.getType(MethodMonitor.class);
    private static final Type INJECT_TYPE       = Type.getType(Inject.class);

    private static final String PROVIDER_FIELD = "$$monitorProvider";
    private static final String START_MONITOR  = "startMethodMonitor";
    private static final String ERROR          = "error";
    private static final String EXIT           = "exit";

    private static Method START_MONITOR_METHOD_NO_ARGS;
    private static Method START_MONITOR_METHOD_WITH_ARGS;

    static {
        Try.throwUnchecked(() -> {
            START_MONITOR_METHOD_WITH_ARGS =
                    Method.getMethod(MonitorProvider.class
                            .getMethod(START_MONITOR,String.class,String.class,Object[].class));

            START_MONITOR_METHOD_NO_ARGS =
                    Method.getMethod(MonitorProvider.class
                            .getMethod(START_MONITOR,String.class,String.class));

        });
    }

    private MonitorConfig mc;
    private String        basePackage;

    @Override
    public void init(AppConfig config) {
        this.mc          = config.getExtension(MonitorConfig.class);
        this.basePackage = config.getBasePackage().replace('.','/') + '/';
    }

    @Override
    protected boolean preInstrument(AppInstrumentContext context, ResourceSet rs) {
        if(null == mc || !mc.isEnabled()) {
            return false;
        }
        return true;
    }

    @Override
    protected void processClass(AppInstrumentContext context, Resource rs, InputStreamSource is, ClassReader cr, ClassNode cn) {
        boolean isBean = context.isBeanClass();
        if(!isBean && ASM.isAnnotationPresent(cn, Bean.class)) {
            isBean = true;
        }

        boolean isBasePackage = cr.getClassName().startsWith(basePackage);

        if(isBean || isBasePackage) {
            boolean isMonitored = ASM.isAnnotationPresent(cn, Monitored.class);

            if(!isMonitored) {
                boolean isFrameworkClass = cr.getClassName().startsWith("leap/");
                if(isFrameworkClass) {
                    return;
                }

                for(MethodNode mn : cn.methods) {

                    if(isMonitored(mn)) {
                        isMonitored = true;
                        break;
                    }

                }
            }

            if(isMonitored){
                Try.throwUnchecked(() -> {
                    try(InputStream in = is.getInputStream()) {
                        ClassReader newCr = new ClassReader(in);
                        context.addInstrumentedClass(this.getClass(), cr.getClassName(), instrumentClass(cn, newCr));
                    }
                });
            }
        }

    }

    protected static boolean isMonitored(MethodNode mn) {

        if(ASM.isAnnotationPresent(mn, Monitored.class)) {
            return true;
        }

        if(ASM.isConstructor(mn) || ASM.isStaticInit(mn)) {
            return false;
        }

        if(Modifier.isStatic(mn.access)) {
            return false;
        }

        String name = mn.name;
        if(name.startsWith("get") && name.length() > 3 && !ASM.hasArgument(mn)) {
            return false;
        }

        if(name.startsWith("is") && name.length() > 2 && !ASM.hasArgument(mn)) {
            return false;
        }

        if(name.startsWith("set") && name.length() > 3 && ASM.getArgumentSize(mn) == 1) {
            return false;
        }

        return true;
    }

    protected byte[] instrumentClass(ClassNode cn, ClassReader cr) {
        MonitoredClassVisitor visitor = new MonitoredClassVisitor(cn ,new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES));

        cr.accept(visitor, ClassReader.EXPAND_FRAMES);

        byte[] data = visitor.getClassData();

        return data;
    }

    protected static class MonitoredClassVisitor extends ClassVisitor {

        private final ClassNode   cn;
        private final ClassWriter cw;
        private final Type        type;

        public MonitoredClassVisitor(ClassNode cn, ClassWriter cw) {
            super(ASM.API, cw);
            this.cn = cn;
            this.cw = cw;
            this.type = Type.getObjectType(cn.name);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            MethodNode mn = ASM.getMethod(cn, name, desc);

            if(ASM.isConstructor(mn)) {
                MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
                return new InitMethodVisitor(type, mv, access, name, desc);
            } else if(isMonitored(mn)) {
                MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);

                return new MonitoredMethodVisitor(type, mn, mv , access, name, desc);
            }

            return super.visitMethod(access, name, desc, signature, exceptions);
        }

        @Override
        public void visitEnd() {
            FieldVisitor fv = cw.visitField(Opcodes.ACC_PRIVATE, PROVIDER_FIELD, PROVIDER_TYPE.getDescriptor(), null, null);
            {
                AnnotationVisitor av = fv.visitAnnotation(INJECT_TYPE.getDescriptor(), true);
                av.visitEnd();
            }
            fv.visitEnd();
            super.visitEnd();
        }

        public byte[] getClassData() {
            return cw.toByteArray();
        }
    }

    protected static class InitMethodVisitor extends AdviceAdapter {

        private final Type type;

        public InitMethodVisitor(Type type, MethodVisitor mv, int access, String name, String desc) {
            super(ASM.API, mv, access, name, desc);
            this.type = type;
        }

        @Override
        public void visitCode() {
            super.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            getStatic(NOP_PROVIDER_TYPE, "INSTANCE", PROVIDER_TYPE);
            putField(type, PROVIDER_FIELD, PROVIDER_TYPE);
        }
    }

    protected static class MonitoredMethodVisitor extends AdviceAdapter {

        private final Type       type;
        private final MethodNode mn;
        private final Type[]     argumentTypes;
        private final Label      tryLabel     = new Label();
        private final Label      finallyLabel = new Label();

        private int monitorLocal = 0;

        protected MonitoredMethodVisitor(Type type, MethodNode mn, MethodVisitor mv, int access, String name, String desc) {
            super(ASM.API, mv, access, name, desc);
            this.type = type;
            this.mn   = mn;
            this.argumentTypes = ASM.getArgumentTypes(mn);
        }

        @Override
        public void visitCode() {
            super.visitCode();

            startMonitor();
            visitLabel(tryLabel);
        }

        @Override
        public void visitMaxs(int maxStack, int maxLocals) {
            visitTryCatchBlock(tryLabel, finallyLabel, finallyLabel, null);

            visitLabel(finallyLabel);

            int errLocal = newLocal(Type.getType(Throwable.class));
            storeLocal(errLocal);

            notifyMonitorError(errLocal);
            exitMonitor();
            loadLocal(errLocal);
            visitInsn(Opcodes.ATHROW);

            super.visitMaxs(maxStack, maxLocals);
        }

        @Override
        protected void onMethodExit(int opcode) {
            if(opcode != Opcodes.ATHROW) {
                exitMonitor();
            }
        }

        protected void startMonitor() {
            //get the field of monitor provider.
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(GETFIELD,
                    type.getInternalName(),
                    PROVIDER_FIELD,
                    PROVIDER_TYPE.getDescriptor());

            //Call startMethodMonitor
            mv.visitLdcInsn(type.getClassName()); //classname
            mv.visitLdcInsn(mn.name);             //methodDesc

            if(argumentTypes.length > 0) {
                loadArgArray();
                invokeInterface(PROVIDER_TYPE, START_MONITOR_METHOD_WITH_ARGS);
            }else{
                invokeInterface(PROVIDER_TYPE, START_MONITOR_METHOD_NO_ARGS);
            }

            //store local variable.
            monitorLocal = newLocal(MONITOR_TYPE);
            storeLocal(monitorLocal);
        }

        protected void notifyMonitorError(int errLocal) {
            loadLocal(monitorLocal);
            loadLocal(errLocal);
            mv.visitMethodInsn(INVOKEINTERFACE,
                    MONITOR_TYPE.getInternalName(),
                    ERROR,
                    "(Ljava/lang/Throwable;)V", true);
        }

        protected void exitMonitor() {
            loadLocal(monitorLocal);
            mv.visitMethodInsn(INVOKEINTERFACE,
                    MONITOR_TYPE.getInternalName(),
                    EXIT,
                    "()V", true);
        }
    }

}