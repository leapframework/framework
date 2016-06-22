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

import leap.lang.asm.*;
import leap.lang.asm.signature.SignatureReader;
import leap.lang.asm.signature.SignatureVisitor;
import leap.lang.resource.Resource;

import java.util.HashSet;
import java.util.Set;

public class AsmClassDependencyResolver implements ClassDependencyResolver {

    @Override
    public ClassDependency resolveDependentClassNames(Resource resource, byte[] bytes) {
        ClassReader cr = new ClassReader(bytes);

        DependencyVisitor visitor = new DependencyVisitor();

        cr.accept(visitor,0);

        String className = Type.getObjectType(cr.getClassName()).getClassName();
        String superName = className.equals("java.lang.Object") ?
                                null :
                                Type.getObjectType(cr.getSuperName()).getClassName();

        return new SimpleClassDependency(className, superName, visitor.classes);
    }

    protected static class SimpleClassDependency implements ClassDependency {

        private final String      className;
        private final String      superClassName;
        private final Set<String> depdendentClassNames;

        public SimpleClassDependency(String className, String superClassName, Set<String> depdendentClassNames) {
            this.className            = className;
            this.superClassName       = superClassName;
            this.depdendentClassNames = depdendentClassNames;
        }

        @Override
        public String getClassName() {
            return className;
        }

        @Override
        public String getSuperClassName() {
            return superClassName;
        }

        @Override
        public Set<String> getDependentClassNames() {
            return depdendentClassNames;
        }
    }

    /**
     * Inspired by <code>org.objectweb.asm.depend.DependencyVisitor</code> in the ASM dependencies example
     */
    protected static class DependencyVisitor extends ClassVisitor {

        final Set<String> classes = new HashSet<>();

        String className;

        public DependencyVisitor() {
            super(Opcodes.ASM5);
        }

        // ClassVisitor

        @Override
        public void visit(final int version, final int access, final String name,
                          final String signature, final String superName,
                          final String[] interfaces) {

            this.className = name;

            if (signature != null) {
                addSignature(signature);
            }
        }

        @Override
        public AnnotationVisitor visitAnnotation(final String desc,
                                                 final boolean visible) {
            addDesc(desc);
            return new AnnotationDependencyVisitor();
        }

        @Override
        public AnnotationVisitor visitTypeAnnotation(final int typeRef,
                                                     final TypePath typePath, final String desc, final boolean visible) {
            addDesc(desc);
            return new AnnotationDependencyVisitor();
        }

        @Override
        public FieldVisitor visitField(final int access, final String name,
                                       final String desc, final String signature, final Object value) {
            if (signature == null) {
                addDesc(desc);
            } else {
                addTypeSignature(signature);
            }
            if (value instanceof Type) {
                addType((Type) value);
            }
            return new FieldDependencyVisitor();
        }

        @Override
        public MethodVisitor visitMethod(final int access, final String name,
                                         final String desc, final String signature, final String[] exceptions) {
            if (signature == null) {
                addMethodDesc(desc);
            } else {
                addSignature(signature);
            }
            addInternalNames(exceptions);
            return new MethodDependencyVisitor();
        }

        class AnnotationDependencyVisitor extends AnnotationVisitor {

            public AnnotationDependencyVisitor() {
                super(Opcodes.ASM5);
            }

            @Override
            public void visit(final String name, final Object value) {
                if (value instanceof Type) {
                    addType((Type) value);
                }
            }

            @Override
            public void visitEnum(final String name, final String desc,
                                  final String value) {
                addDesc(desc);
            }

            @Override
            public AnnotationVisitor visitAnnotation(final String name,
                                                     final String desc) {
                addDesc(desc);
                return this;
            }

            @Override
            public AnnotationVisitor visitArray(final String name) {
                return this;
            }
        }

        class FieldDependencyVisitor extends FieldVisitor {

            public FieldDependencyVisitor() {
                super(Opcodes.ASM5);
            }

            @Override
            public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                addDesc(desc);
                return new AnnotationDependencyVisitor();
            }

            @Override
            public AnnotationVisitor visitTypeAnnotation(final int typeRef,
                                                         final TypePath typePath, final String desc,
                                                         final boolean visible) {
                addDesc(desc);
                return new AnnotationDependencyVisitor();
            }
        }

        class MethodDependencyVisitor extends MethodVisitor {

            public MethodDependencyVisitor() {
                super(Opcodes.ASM5);
            }

            @Override
            public AnnotationVisitor visitAnnotationDefault() {
                return new AnnotationDependencyVisitor();
            }

            @Override
            public AnnotationVisitor visitAnnotation(final String desc,
                                                     final boolean visible) {
                addDesc(desc);
                return new AnnotationDependencyVisitor();
            }

            @Override
            public AnnotationVisitor visitTypeAnnotation(final int typeRef,
                                                         final TypePath typePath, final String desc,
                                                         final boolean visible) {
                addDesc(desc);
                return new AnnotationDependencyVisitor();
            }

            @Override
            public AnnotationVisitor visitParameterAnnotation(final int parameter,
                                                              final String desc, final boolean visible) {
                addDesc(desc);
                return new AnnotationDependencyVisitor();
            }

            @Override
            public void visitTypeInsn(final int opcode, final String type) {
                addType(Type.getObjectType(type));
            }

            @Override
            public void visitFieldInsn(final int opcode, final String owner,
                                       final String name, final String desc) {
                addInternalName(owner);
                addDesc(desc);
            }

            @Override
            public void visitMethodInsn(final int opcode, final String owner,
                                        final String name, final String desc, final boolean itf) {
                addInternalName(owner);
                addMethodDesc(desc);
            }

            @Override
            public void visitInvokeDynamicInsn(String name, String desc,
                                               Handle bsm, Object... bsmArgs) {
                addMethodDesc(desc);
                addConstant(bsm);
                for (int i = 0; i < bsmArgs.length; i++) {
                    addConstant(bsmArgs[i]);
                }
            }

            @Override
            public void visitLdcInsn(final Object cst) {
                addConstant(cst);
            }

            @Override
            public void visitMultiANewArrayInsn(final String desc, final int dims) {
                addDesc(desc);
            }

            @Override
            public AnnotationVisitor visitInsnAnnotation(int typeRef,
                                                         TypePath typePath, String desc, boolean visible) {
                addDesc(desc);
                return new AnnotationDependencyVisitor();
            }

            @Override
            public void visitLocalVariable(final String name, final String desc,
                                           final String signature, final Label start, final Label end,
                                           final int index) {
                addTypeSignature(signature);
            }

            @Override
            public AnnotationVisitor visitLocalVariableAnnotation(int typeRef,
                                                                  TypePath typePath, Label[] start, Label[] end, int[] index,
                                                                  String desc, boolean visible) {
                addDesc(desc);
                return new AnnotationDependencyVisitor();
            }

            @Override
            public void visitTryCatchBlock(final Label start, final Label end,
                                           final Label handler, final String type) {
                if (type != null) {
                    addInternalName(type);
                }
            }

            @Override
            public AnnotationVisitor visitTryCatchAnnotation(int typeRef,
                                                             TypePath typePath, String desc, boolean visible) {
                addDesc(desc);
                return new AnnotationDependencyVisitor();
            }
        }

        class SignatureDependencyVisitor extends SignatureVisitor {

            String signatureClassName;

            public SignatureDependencyVisitor() {
                super(Opcodes.ASM5);
            }

            @Override
            public void visitClassType(final String name) {
                signatureClassName = name;
                addInternalName(name);
            }

            @Override
            public void visitInnerClassType(final String name) {
                signatureClassName = signatureClassName + "$" + name;
                addInternalName(signatureClassName);
            }
        }

        // ---------------------------------------------

        private void addName(final String name) {
            if (name == null) {
                return;
            }

            if (name.startsWith("java/") || name.startsWith("javax/")) {
                return;
            }

            if(name.equals(className)) {
                return;
            }

            classes.add(Type.getObjectType(name).getClassName());
        }

        void addInternalName(final String name) {
            addType(Type.getObjectType(name));
        }

        private void addInternalNames(final String[] names) {
            for (int i = 0; names != null && i < names.length; i++) {
                addInternalName(names[i]);
            }
        }

        void addDesc(final String desc) {
            addType(Type.getType(desc));
        }

        void addMethodDesc(final String desc) {
            addType(Type.getReturnType(desc));
            Type[] types = Type.getArgumentTypes(desc);
            for (int i = 0; i < types.length; i++) {
                addType(types[i]);
            }
        }

        void addType(final Type t) {
            switch (t.getSort()) {
                case Type.ARRAY:
                    addType(t.getElementType());
                    break;
                case Type.OBJECT:
                    addName(t.getInternalName());
                    break;
                case Type.METHOD:
                    addMethodDesc(t.getDescriptor());
                    break;
            }
        }

        private void addSignature(final String signature) {
            if (signature != null) {
                new SignatureReader(signature)
                        .accept(new SignatureDependencyVisitor());
            }
        }

        void addTypeSignature(final String signature) {
            if (signature != null) {
                new SignatureReader(signature)
                        .acceptType(new SignatureDependencyVisitor());
            }
        }

        void addConstant(final Object cst) {
            if (cst instanceof Type) {
                addType((Type) cst);
            } else if (cst instanceof Handle) {
                Handle h = (Handle) cst;
                addInternalName(h.getOwner());
                addMethodDesc(h.getDesc());
            }
        }
    }

}
