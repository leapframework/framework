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

import leap.core.annotation.Transactional;
import leap.core.instrument.AbstractAsmInstrumentProcessor;
import leap.core.instrument.AppInstrumentContext;
import leap.core.instrument.AppInstrumentProcessor;
import leap.lang.asm.*;
import leap.lang.asm.commons.AdviceAdapter;
import leap.lang.asm.tree.ClassNode;
import leap.lang.asm.tree.MethodNode;
import leap.lang.resource.Resource;
import leap.lang.resource.ResourceSet;

public class TransactionInstrumentation extends AbstractAsmInstrumentProcessor implements AppInstrumentProcessor {

    @Override
    protected void processClass(AppInstrumentContext context, ResourceSet rs, Resource resource, ClassReader cr) {
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
                context.addInstrumentedClass(this.getClass(), cr.getClassName(), instrumentClass(cn, cr));
            }

        }
    }

    protected byte[] instrumentClass(ClassNode cn, ClassReader cr) {
        TxClassVisitor visitor = new TxClassVisitor(cn ,new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES));

        cr.accept(visitor, 0);

        byte[] data = visitor.getClassData();

        //ASM.printASMifiedCode(data, new PrintWriter(System.out));

        return data;
    }

    protected static class TxClassVisitor extends ClassVisitor {

        private final ClassNode   cn;
        private final ClassWriter cw;

        public TxClassVisitor(ClassNode cn, ClassWriter cw) {
            super(ASM.API, cw);
            this.cn = cn;
            this.cw = cw;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            MethodNode mn = ASM.getMethod(cn, name, desc);

            if(ASM.isAnnotationPresent(mn, Transactional.class)) {
                return new TxMethodVisitor(mn, super.visitMethod(access, name, desc, signature, exceptions), access, name, desc);
            }

            return super.visitMethod(access, name, desc, signature, exceptions);
        }

        public byte[] getClassData() {
            return cw.toByteArray();
        }
    }

    protected static class TxMethodVisitor extends AdviceAdapter {

        private final MethodNode mn;

        private final Label tryLabel     = new Label();
        private final Label finallyLabel = new Label();
        private final Label catchLabel   = new Label();

        protected TxMethodVisitor(MethodNode mn, MethodVisitor mv, int access, String name, String desc) {
            super(ASM.API, mv, access, name, desc);
            this.mn = mn;
        }

        @Override
        public void visitCode() {
            super.visitCode();
            mv.visitLabel(tryLabel);
            onTransactionBegin();
        }

        @Override
        public void visitMaxs(int maxStack, int maxLocals) {
            mv.visitTryCatchBlock(tryLabel, finallyLabel, catchLabel, null);
            mv.visitLabel(catchLabel);

            onTransactionEnd();

            mv.visitInsn(Opcodes.ATHROW);
            mv.visitMaxs(maxStack, maxLocals);
        }

        @Override
        protected void onMethodExit(int opcode) {
            if(opcode != Opcodes.ATHROW) {
                mv.visitLabel(finallyLabel);
                onTransactionEnd();
            }
        }

        protected void onTransactionBegin() {
            mv.visitMethodInsn(INVOKESTATIC, "leap/core/transaction/TransactionInstrumentation", "begin", "()V", false);
        }

        protected void onTransactionEnd() {
            mv.visitMethodInsn(INVOKESTATIC, "leap/core/transaction/TransactionInstrumentation", "end", "()V", false);
        }
    }

    public static void begin() {
        System.out.println("tx begin");
    }

    public static void end() {
        System.out.println("tx end");
    }
}