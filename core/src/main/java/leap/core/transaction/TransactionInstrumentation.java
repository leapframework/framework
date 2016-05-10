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

package leap.core.transaction;

import leap.core.annotation.Inject;
import leap.core.annotation.Transactional;
import leap.core.instrument.AbstractAsmInstrumentProcessor;
import leap.core.instrument.AppInstrumentContext;
import leap.core.instrument.AppInstrumentProcessor;
import leap.lang.Try;
import leap.lang.asm.*;
import leap.lang.asm.commons.AdviceAdapter;
import leap.lang.asm.tree.ClassNode;
import leap.lang.asm.tree.MethodNode;
import leap.lang.io.InputStreamSource;
import leap.lang.resource.Resource;

import java.io.InputStream;

public class TransactionInstrumentation extends AbstractAsmInstrumentProcessor implements AppInstrumentProcessor {

    private static final Type MANAGER_TYPE = Type.getType(TransactionManager.class);
    private static final Type TRANS_TYPE   = Type.getType(ClosableTransaction.class);
    private static final Type INJECT_TYPE  = Type.getType(Inject.class);

    private static final String MANAGER_FIELD = "tm";

    @Override
    protected void processClass(AppInstrumentContext context, Resource rs, InputStreamSource is, ClassReader cr) {
        ClassNode cn = ASM.getClassNode(cr);

        if(null != cn.methods) {
            boolean hasTransactionalMethods = false;

            for(MethodNode mn : cn.methods) {
                if(ASM.isAnnotationPresent(mn, Transactional.class)) {
                    hasTransactionalMethods = true;
                    break;
                }
            }

            if(hasTransactionalMethods) {
                Try.throwUnchecked(() -> {
                    try(InputStream in = is.getInputStream()) {
                        ClassReader newCr = new ClassReader(in);
                        context.addInstrumentedClass(this.getClass(), cr.getClassName(), instrumentClass(cn, newCr));
                    }
                });
            }
        }
    }

    protected byte[] instrumentClass(ClassNode cn, ClassReader cr) {
        TxClassVisitor visitor = new TxClassVisitor(cn ,new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES));

        cr.accept(visitor, ClassReader.EXPAND_FRAMES);

        byte[] data = visitor.getClassData();

        ASM.printASMifiedCode(data);

        return data;
    }

    protected static class TxClassVisitor extends ClassVisitor {

        private final ClassNode   cn;
        private final ClassWriter cw;
        private final Type        type;

        public TxClassVisitor(ClassNode cn, ClassWriter cw) {
            super(ASM.API, cw);
            this.cn = cn;
            this.cw = cw;
            this.type = Type.getObjectType(cn.name);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            MethodNode mn = ASM.getMethod(cn, name, desc);

            if(ASM.isAnnotationPresent(mn, Transactional.class)) {
                MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);

                return new TxMethodVisitor(type, mn, mv , access, name, desc);
            }

            return super.visitMethod(access, name, desc, signature, exceptions);
        }

        @Override
        public void visitEnd() {
            FieldVisitor fv = cw.visitField(Opcodes.ACC_PRIVATE, MANAGER_FIELD, MANAGER_TYPE.getDescriptor(), null, null);
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

    protected static class TxMethodVisitor extends AdviceAdapter {

        private final Type       type;
        private final MethodNode mn;

        private final Label tryLabel     = new Label();
        private final Label finallyLabel = new Label();

        private int newLocal = 0;

        protected TxMethodVisitor(Type type, MethodNode mn, MethodVisitor mv, int access, String name, String desc) {
            super(ASM.API, mv, access, name, desc);

            this.type = type;
            this.mn   = mn;
        }

        @Override
        public void visitCode() {
            super.visitCode();

            beginTransaction();
            visitLabel(tryLabel);
        }

        @Override
        public void visitMaxs(int maxStack, int maxLocals) {
            visitTryCatchBlock(tryLabel, finallyLabel, finallyLabel, null);
            visitLabel(finallyLabel);
            closeTransaction();
            visitInsn(Opcodes.ATHROW);

            super.visitMaxs(maxStack, maxLocals);
        }

        @Override
        protected void onMethodExit(int opcode) {
            if(opcode != Opcodes.ATHROW) {
                closeTransaction();
            }
        }

        protected void beginTransaction() {
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(GETFIELD,
                              type.getInternalName(),
                              MANAGER_FIELD,
                              MANAGER_TYPE.getDescriptor());

            mv.visitMethodInsn(INVOKEINTERFACE,
                               MANAGER_TYPE.getInternalName(),
                               "begin",
                               "()" + TRANS_TYPE.getDescriptor(), true);

            newLocal = newLocal(TRANS_TYPE);
            storeLocal(newLocal);
        }

        protected void closeTransaction() {
            loadLocal(newLocal);
            mv.visitMethodInsn(INVOKEINTERFACE,
                    TRANS_TYPE.getInternalName(),
                    "close",
                    "()V", true);
        }
    }
}