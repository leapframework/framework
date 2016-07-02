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
import leap.core.annotation.Monitored;
import leap.core.instrument.AppInstrumentClass;
import leap.core.instrument.AppInstrumentContext;
import leap.core.instrument.AsmInstrumentProcessor;
import leap.lang.Try;
import leap.lang.asm.*;
import leap.lang.asm.commons.AdviceAdapter;
import leap.lang.asm.commons.GeneratorAdapter;
import leap.lang.asm.commons.Method;
import leap.lang.asm.tree.AnnotationNode;
import leap.lang.asm.tree.ClassNode;
import leap.lang.asm.tree.MethodNode;

import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.Map;

public class MonitorInstrumentation extends AsmInstrumentProcessor {

    private static final Type PROVIDER_TYPE = Type.getType(MonitorProvider.class);
    private static final Type MONITOR_TYPE  = Type.getType(MethodMonitor.class);

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

    @Override
    public boolean isMethodBodyOnly() {
        return true;
    }

    @Override
    public void init(AppConfig config) {
        this.mc = config.getExtension(MonitorConfig.class);
    }

    @Override
    protected boolean preInstrument(AppInstrumentContext context) {
        if(null == mc || !mc.isEnabled()) {
            return false;
        }
        return true;
    }

    @Override
    protected void processClass(AppInstrumentContext context, AppInstrumentClass ic, ClassInfo ci, boolean methodBodyOnly) {
        ClassNode cn = ci.cn;

        if(cn.version < 51) {
            log.trace("Skip class '{}', version < 51", ic.getClassName());
            return;
        }

        boolean isMonitorDeclared = false;
        AnnotationNode a = ASM.getAnnotation(cn, Monitored.class);
        if(null != a) {
            isMonitorDeclared = true;
            Map<String,Object> params = ASM.getAnnotationValues(a);
            if(!params.isEmpty()) {
                Boolean value = (Boolean)params.get("value");
                if(null != value && !value) {
                    log.trace("Skip class '{}', disabled", ic.getClassName());
                    return;
                }
            }
        }

        if(!isMonitorDeclared) {
            boolean isIgnore = isFrameworkClass(ci);
            if(isIgnore) {
                return;
            }

            //todo : only bean class will be monitored.
//            boolean isBean = ASM.isAnnotationPresent(ci.cn, Bean.class) || AppClassLoader.isBeanClass(ic.getClassName());
//            if(!isBean) {
//                return;
//            }

            for(MethodNode mn : cn.methods) {

                if(isMonitored(mn)) {
                    isMonitorDeclared = true;
                    break;
                }

            }

            if(!isMonitorDeclared) {
                log.trace("Ignore class '{}', can't found any monitored method(s).", ic.getClassName());
            }
        }

        if(isMonitorDeclared){
            Try.throwUnchecked(() -> {
                try(InputStream in = ci.is.getInputStream()) {
                    byte[] bytes = instrumentClass(cn, new ClassReader(in), true);

                    context.updateInstrumented(ic, this, bytes , false);
                }
            });
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

    protected byte[] instrumentClass(ClassNode cn, ClassReader cr, boolean methodBodyOnly) {
        MonitoredClassVisitor visitor =
                new MonitoredClassVisitor(cn ,new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES), methodBodyOnly);

        cr.accept(visitor, ClassReader.EXPAND_FRAMES);

        byte[] data = visitor.getClassData();

        return data;
    }

    protected static class MonitoredClassVisitor extends ClassVisitor {

        private final ClassNode   cn;
        private final ClassWriter cw;
        private final Type        type;

        private boolean visitStaticInit;
        private boolean methodBodyOnly;

        public MonitoredClassVisitor(ClassNode cn, ClassWriter cw, boolean methodBodyOnly) {
            super(ASM.API, cw);
            this.cn = cn;
            this.cw = cw;
            this.type = Type.getObjectType(cn.name);
            this.methodBodyOnly = methodBodyOnly;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            MethodNode mn = ASM.getMethod(cn, name, desc);

            if(!methodBodyOnly && ASM.isStaticInit(mn)) {
                visitStaticInit = true;
                MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
                return new ClinitMethodVisitor(mv, access, name, desc);
            }

            if(isMonitored(mn)) {
                MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
                return new MonitoredMethodVisitor(type, mn, mv , access, name, desc);
            }

            return super.visitMethod(access, name, desc, signature, exceptions);
        }

        @Override
        public void visitEnd() {
            if(!methodBodyOnly) {
                FieldVisitor fv = cw.visitField(Opcodes.ACC_PRIVATE + Opcodes.ACC_FINAL + Opcodes.ACC_STATIC,
                        PROVIDER_FIELD,
                        PROVIDER_TYPE.getDescriptor(), null, null);
                fv.visitEnd();

                if (!visitStaticInit) {
                    int access = Opcodes.ACC_STATIC;

                    MethodVisitor real = cw.visitMethod(access, "<clinit>", "()V", null, null);
                    ClinitMethodVisitor mv = new ClinitMethodVisitor(real, access, "<clinit>", "()V");
                    mv.visitCode();
                    mv.returnValue();
                    mv.visitMaxs(0, 0);
                    mv.visitEnd();
                }
            }
            super.visitEnd();
        }

        public byte[] getClassData() {
            return cw.toByteArray();
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
            }
        }

        protected class MonitoredMethodVisitor extends AdviceAdapter {

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
                if(!methodBodyOnly) {
                    getStatic(type,PROVIDER_FIELD,PROVIDER_TYPE);
                }else{
                    getStatic(MonitorInst.TYPE,"PROVIDER",PROVIDER_TYPE);
                }

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


}