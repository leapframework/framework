/*
 * Copyright 2012 the original author or authors.
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
package leap.lang.reflect;

import static leap.lang.asm.Opcodes.AALOAD;
import static leap.lang.asm.Opcodes.ACC_PUBLIC;
import static leap.lang.asm.Opcodes.ACC_VARARGS;
import static leap.lang.asm.Opcodes.ACONST_NULL;
import static leap.lang.asm.Opcodes.ALOAD;
import static leap.lang.asm.Opcodes.ANEWARRAY;
import static leap.lang.asm.Opcodes.ARETURN;
import static leap.lang.asm.Opcodes.ASTORE;
import static leap.lang.asm.Opcodes.ATHROW;
import static leap.lang.asm.Opcodes.BIPUSH;
import static leap.lang.asm.Opcodes.CHECKCAST;
import static leap.lang.asm.Opcodes.DUP;
import static leap.lang.asm.Opcodes.GETFIELD;
import static leap.lang.asm.Opcodes.ILOAD;
import static leap.lang.asm.Opcodes.INVOKESPECIAL;
import static leap.lang.asm.Opcodes.INVOKESTATIC;
import static leap.lang.asm.Opcodes.INVOKEVIRTUAL;
import static leap.lang.asm.Opcodes.NEW;
import static leap.lang.asm.Opcodes.PUTFIELD;
import static leap.lang.asm.Opcodes.RETURN;
import static leap.lang.asm.Opcodes.V1_6;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import leap.lang.Classes;
import leap.lang.Primitives;
import leap.lang.Strings;
import leap.lang.asm.ClassWriter;
import leap.lang.asm.Label;
import leap.lang.asm.MethodVisitor;
import leap.lang.asm.Opcodes;
import leap.lang.asm.Type;

@SuppressWarnings("deprecation")
public abstract class ASMReflectAccessor implements ReflectAccessor {
    
    private static final String CLASS_NAME   = ASMReflectAccessor.class.getName().replaceAll("\\.", "/");
    private static final String	A_CLASS_NAME = "L" + CLASS_NAME + ";";
    
	Field[]  	   fields;
	Method[] 	   methods;
	Constructor<?> constructor;
	
	@Override
    public boolean canNewInstance() {
		return null != constructor;
	}
	
    public int getMethodIndex(Method method) {
        for (int i = 0, n = methods.length; i < n; i++) {
            if(method.equals(methods[i])){
                return i;
            }
        }
        return -1;
    }
    
    public int getFieldIndex(Field field){
        for (int i = 0, n = fields.length; i < n; i++) {
            if(field.equals(fields[i])){
                return i;
            }
        }
        return -1;
    }
    
    public int getFieldIndex(String name){
        for (int i = 0, n = fields.length; i < n; i++) {
            if(name.equals(fields[i].getName())){
                return i;
            }
        }
        return -1;
    }
    
    static ASMReflectAccessor createFor(Class<?> clazz){
    	synchronized (clazz) {
            ReflectLoader loader = new ReflectLoader(clazz.getClassLoader());
            
            String typeClassName      = clazz.getName();
            String accessorClassName  = getAccessorClassNameFor(clazz);
            Class<?> accessorClass    = null;
            
            try {
                accessorClass = loader.loadClass(accessorClassName);
            } catch (ClassNotFoundException ignored) {
            }
            
            Method[] methods = getAccessibleMethods(clazz);
            Field[]  fields  = getAccessibleFields(clazz);
            Constructor<?> c = getDefaultConstructor(clazz,accessorClassName.startsWith(typeClassName));
            
            if (accessorClass == null) {
                String accessorClassNameInternal = accessorClassName.replace('.', '/');
                String typeClassNameInternal     = typeClassName.replace('.', '/');

                ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                
                defineAccessorConstructor(accessorClassNameInternal,cw);
                
                defineNewInstance(typeClassNameInternal,cw);
                
                defineNewArray(clazz,typeClassNameInternal,cw);
                
                defineGetArrayLength(clazz, cw);
                
                defineGetArrayItem(clazz, cw);
                
                defineSetArrayItem(clazz, cw);
                
                defineInvokeMethod(typeClassNameInternal,methods, cw);
                
                defineSetField(typeClassNameInternal,fields, cw);
                
                defineGetField(typeClassNameInternal,fields, cw);

                cw.visitEnd();
                
                byte[] data   = cw.toByteArray();
                accessorClass = loader.defineClass(accessorClassName, data);
            }
            
            try {
                ASMReflectAccessor accessor = (ASMReflectAccessor)accessorClass.newInstance();
                accessor.methods     = methods;
                accessor.fields      = fields;
                accessor.constructor = c;
                return accessor;
            } catch (Exception ex) {
                throw new ReflectException(Strings.format("Error constructing reflect accessor class: {0}",accessorClassName),ex);
            }
        }
    }
    
    private static void defineAccessorConstructor(String accessorClassNameInternal,ClassWriter cw){
        cw.visit(V1_6, ACC_PUBLIC, accessorClassNameInternal, null, CLASS_NAME, null);
        
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, CLASS_NAME, "<init>", "()V");
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
    
    private static void defineNewInstance(String classNameInternal,ClassWriter cw){
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "newInstance", "()Ljava/lang/Object;", null, null);
        
        mv.visitCode();
        mv.visitTypeInsn(NEW, classNameInternal);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL,classNameInternal, "<init>", "()V");  
        mv.visitInsn(ARETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }
    
    private static void defineNewArray(Class<?> clazz, String classNameInternal,ClassWriter cw){
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "newArray", "(I)Ljava/lang/Object;", null, null);
        
        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitVarInsn(ILOAD, 1);
        
        if(clazz.isPrimitive()){
        	mv.visitIntInsn(Opcodes.NEWARRAY,getPrimitiveTypeCode(clazz));
        }else{
        	mv.visitTypeInsn(ANEWARRAY, classNameInternal);	
        }
        
        mv.visitInsn(ARETURN);
        Label l1 = new Label();
        mv.visitLabel(l1);
        mv.visitLocalVariable("this", A_CLASS_NAME, null, l0, l1, 0);
        mv.visitLocalVariable("length", "I", null, l0, l1, 1);
        mv.visitMaxs(1, 2);
        mv.visitEnd();
    }  
    
    private static void defineGetArrayLength(Class<?> clazz, ClassWriter cw){
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "getArrayLength", "(Ljava/lang/Object;)I", null, null);

        Type type = Type.getType(clazz);
        if(clazz.isPrimitive()){
        	
        	String primitiveInternalName  = null;
        	
            switch (type.getSort()) {
                case Type.BOOLEAN:
                	primitiveInternalName = "Z";
                    break;
                case Type.BYTE:
                	primitiveInternalName = "B";
                    break;
                case Type.CHAR:
                	primitiveInternalName = "C";
                    break;
                case Type.SHORT:
                	primitiveInternalName = "S";
                    break;
                case Type.INT:
                	primitiveInternalName = "I";
                    break;
                case Type.FLOAT:
                	primitiveInternalName = "F";
                    break;
                case Type.LONG:
                	primitiveInternalName = "J";
                    break;
                case Type.DOUBLE:
                	primitiveInternalName = "D";
                    break;
                default :
                	throw new IllegalStateException("??? unknow primitive type '" + clazz.getName() + "'");
            }    
            
            mv.visitCode();
            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitTypeInsn(CHECKCAST, "[" + primitiveInternalName);
            mv.visitInsn(Opcodes.ARRAYLENGTH);
            mv.visitInsn(Opcodes.IRETURN);
            Label l1 = new Label();
            mv.visitLabel(l1);
            mv.visitLocalVariable("this", A_CLASS_NAME, null, l0, l1, 0);
            mv.visitLocalVariable("array", "Ljava/lang/Object;", null, l0, l1, 1);
            mv.visitMaxs(1, 2);
            mv.visitEnd();
        }else{
        	mv.visitCode();
        	Label l0 = new Label();
        	mv.visitLabel(l0);
        	mv.visitVarInsn(ALOAD, 1);
        	mv.visitTypeInsn(CHECKCAST, "[Ljava/lang/Object;");
        	mv.visitInsn(Opcodes.ARRAYLENGTH);
        	mv.visitInsn(Opcodes.IRETURN);
        	Label l1 = new Label();
        	mv.visitLabel(l1);
        	mv.visitLocalVariable("this", A_CLASS_NAME, null, l0, l1, 0);
        	mv.visitLocalVariable("array", "Ljava/lang/Object;", null, l0, l1, 1);
        	mv.visitMaxs(1, 2);
        	mv.visitEnd();
        }
    }     
    
    private static void defineGetArrayItem(Class<?> clazz, ClassWriter cw){
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "getArrayItem", "(Ljava/lang/Object;I)Ljava/lang/Object;", null, null);

        Type type = Type.getType(clazz);
        if(clazz.isPrimitive()){
        	
        	int    opcode                 = -1;
        	String primitiveInternalName  = null;
        	String wraperTypeInternalName = Type.getType(Primitives.wrap(clazz)).getInternalName();
        	
            switch (type.getSort()) {
                case Type.BOOLEAN:
                	opcode = Opcodes.BALOAD;
                	primitiveInternalName = "Z";
                    break;
                case Type.BYTE:
                	opcode = Opcodes.BALOAD;
                	primitiveInternalName = "B";
                    break;
                case Type.CHAR:
                	opcode = Opcodes.CALOAD;
                	primitiveInternalName = "C";
                    break;
                case Type.SHORT:
                	opcode = Opcodes.SALOAD;
                	primitiveInternalName = "S";
                    break;
                case Type.INT:
                	opcode = Opcodes.IALOAD;
                	primitiveInternalName = "I";
                    break;
                case Type.FLOAT:
                	opcode = Opcodes.FALOAD;
                	primitiveInternalName = "F";
                    break;
                case Type.LONG:
                	opcode = Opcodes.BALOAD;
                	primitiveInternalName = "J";
                    break;
                case Type.DOUBLE:
                	opcode = Opcodes.DALOAD;
                	primitiveInternalName = "D";
                    break;
                default :
                	throw new IllegalStateException("??? unknow primitive type '" + clazz.getName() + "'");
            }        	
        	
            mv.visitCode();
            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitTypeInsn(CHECKCAST, "[" + primitiveInternalName);
            mv.visitVarInsn(ILOAD, 2);
            mv.visitInsn(opcode);
            mv.visitMethodInsn(INVOKESTATIC, wraperTypeInternalName, "valueOf", "(" + primitiveInternalName + ")L" + wraperTypeInternalName + ";");
            mv.visitInsn(ARETURN);
            Label l1 = new Label();
            mv.visitLabel(l1);
            mv.visitLocalVariable("this", A_CLASS_NAME, null, l0, l1, 0);
            mv.visitLocalVariable("array", "Ljava/lang/Object;", null, l0, l1, 1);
            mv.visitLocalVariable("index", "I", null, l0, l1, 2);
            mv.visitMaxs(2, 3);
            mv.visitEnd();
        }else{
        	mv.visitCode();
        	Label l0 = new Label();
        	mv.visitLabel(l0);
        	mv.visitVarInsn(ALOAD, 1);
        	mv.visitTypeInsn(CHECKCAST, "[Ljava/lang/Object;");
        	mv.visitVarInsn(ILOAD, 2);
        	mv.visitInsn(AALOAD);
        	mv.visitInsn(ARETURN);
        	Label l1 = new Label();
        	mv.visitLabel(l1);
        	mv.visitLocalVariable("this", A_CLASS_NAME, null, l0, l1, 0);
        	mv.visitLocalVariable("array", "Ljava/lang/Object;", null, l0, l1, 1);
        	mv.visitLocalVariable("index", "I", null, l0, l1, 2);
        	mv.visitMaxs(2, 3);
        	mv.visitEnd(); 
        }
    }  
    
    private static void defineSetArrayItem(Class<?> clazz, ClassWriter cw){
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "setArrayItem", "(Ljava/lang/Object;ILjava/lang/Object;)V", null, null);

        Type type = Type.getType(clazz);
        if(clazz.isPrimitive()){
        	
        	int    opcode                 = -1;
        	String primitiveInternalName  = null;
        	String unboxMethodName        = null;
        	String wraperTypeInternalName = Type.getType(Primitives.wrap(clazz)).getInternalName();
        	
            switch (type.getSort()) {
                case Type.BOOLEAN:
                	opcode = Opcodes.BASTORE;
                	primitiveInternalName = "Z";
                	unboxMethodName       = "booleanValue";
                    break;
                case Type.BYTE:
                	opcode = Opcodes.BASTORE;
                	primitiveInternalName = "B";
                	unboxMethodName       = "byteValue";
                    break;
                case Type.CHAR:
                	opcode = Opcodes.CASTORE;
                	primitiveInternalName = "C";
                	unboxMethodName       = "charValue";
                    break;
                case Type.SHORT:
                	opcode = Opcodes.SASTORE;
                	primitiveInternalName = "S";
                	unboxMethodName       = "shortValue";
                    break;
                case Type.INT:
                	opcode = Opcodes.IASTORE;
                	primitiveInternalName = "I";
                	unboxMethodName       = "intValue";
                    break;
                case Type.FLOAT:
                	opcode = Opcodes.FASTORE;
                	primitiveInternalName = "F";
                	unboxMethodName       = "floatValue";
                    break;
                case Type.LONG:
                	opcode = Opcodes.BASTORE;
                	primitiveInternalName = "J";
                	unboxMethodName       = "longValue";
                    break;
                case Type.DOUBLE:
                	opcode = Opcodes.DASTORE;
                	primitiveInternalName = "D";
                	unboxMethodName       = "doubleValue";
                    break;
                default :
                	throw new IllegalStateException("??? unknow primitive type '" + clazz.getName() + "'");
            }     
            
            mv.visitCode();
            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitTypeInsn(CHECKCAST, "[" + primitiveInternalName);
            mv.visitVarInsn(ILOAD, 2);
            mv.visitVarInsn(ALOAD, 3);
            mv.visitTypeInsn(CHECKCAST, wraperTypeInternalName);
            mv.visitMethodInsn(INVOKEVIRTUAL, wraperTypeInternalName, unboxMethodName, "()" + primitiveInternalName);
            mv.visitInsn(opcode);
            Label l1 = new Label();
            mv.visitLabel(l1);
            mv.visitInsn(RETURN);
            Label l2 = new Label();
            mv.visitLabel(l2);
            mv.visitLocalVariable("this", A_CLASS_NAME, null, l0, l2, 0);
            mv.visitLocalVariable("array", "Ljava/lang/Object;", null, l0, l2, 1);
            mv.visitLocalVariable("index", "I", null, l0, l2, 2);
            mv.visitLocalVariable("value", "Ljava/lang/Object;", null, l0, l2, 3);
            mv.visitMaxs(3, 4);
            mv.visitEnd();
        }else{
        	mv.visitCode();
        	Label l0 = new Label();
        	mv.visitLabel(l0);
        	mv.visitVarInsn(ALOAD, 1);
        	mv.visitTypeInsn(CHECKCAST, "[Ljava/lang/Object;");
        	mv.visitVarInsn(ILOAD, 2);
        	mv.visitVarInsn(ALOAD, 3);
        	mv.visitInsn(Opcodes.AASTORE);
        	Label l1 = new Label();
        	mv.visitLabel(l1);
        	mv.visitInsn(RETURN);
        	Label l2 = new Label();
        	mv.visitLabel(l2);
        	mv.visitLocalVariable("this",  A_CLASS_NAME, null, l0, l2, 0);
        	mv.visitLocalVariable("array", "Ljava/lang/Object;", null, l0, l2, 1);
        	mv.visitLocalVariable("index", "I", null, l0, l2, 2);
        	mv.visitLocalVariable("value", "Ljava/lang/Object;", null, l0, l2, 3);
        	mv.visitMaxs(3, 4);
        	mv.visitEnd();
        }
    }     
    
    private static void defineInvokeMethod(String classNameInternal,Method[] methods, ClassWriter cw){
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_VARARGS, 
                                          "invokeMethod", 
                                          "(Ljava/lang/Object;I[Ljava/lang/Object;)Ljava/lang/Object;", null, null);
        mv.visitCode();

        if (methods.length > 0) {
            mv.visitVarInsn(ALOAD, 1);
            mv.visitTypeInsn(CHECKCAST, classNameInternal);
            mv.visitVarInsn(ASTORE, 4);

            mv.visitVarInsn(ILOAD, 2);
            Label[] labels = new Label[methods.length];
            for (int i = 0;i < labels.length; i++){
                labels[i] = new Label();
            }
            Label defaultLabel = new Label();
            mv.visitTableSwitchInsn(0, labels.length - 1, defaultLabel, labels);

            StringBuilder buffer = new StringBuilder(128);
            for (int i = 0;i < labels.length; i++) {
                mv.visitLabel(labels[i]);
                
                if (i == 0){
                    mv.visitFrame(Opcodes.F_APPEND, 1, new Object[] {classNameInternal}, 0, null);
                }else{
                    mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
                }
                mv.visitVarInsn(ALOAD, 4);

                buffer.setLength(0);
                buffer.append('(');

                Method method = methods[i];
                Class<?>[] paramTypes = method.getParameterTypes();
                for (int paramIndex = 0; paramIndex < paramTypes.length; paramIndex++) {
                    mv.visitVarInsn(ALOAD, 3);
                    mv.visitIntInsn(BIPUSH, paramIndex);
                    mv.visitInsn(AALOAD);
                    Type paramType = Type.getType(paramTypes[paramIndex]);
                    switch (paramType.getSort()) {
                        case Type.BOOLEAN:
                            mv.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
                            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z");
                            break;
                        case Type.BYTE:
                            mv.visitTypeInsn(CHECKCAST, "java/lang/Byte");
                            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Byte", "byteValue", "()B");
                            break;
                        case Type.CHAR:
                            mv.visitTypeInsn(CHECKCAST, "java/lang/Character");
                            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Character", "charValue", "()C");
                            break;
                        case Type.SHORT:
                            mv.visitTypeInsn(CHECKCAST, "java/lang/Short");
                            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Short", "shortValue", "()S");
                            break;
                        case Type.INT:
                            mv.visitTypeInsn(CHECKCAST, "java/lang/Integer");
                            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I");
                            break;
                        case Type.FLOAT:
                            mv.visitTypeInsn(CHECKCAST, "java/lang/Float");
                            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Float", "floatValue", "()F");
                            break;
                        case Type.LONG:
                            mv.visitTypeInsn(CHECKCAST, "java/lang/Long");
                            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J");
                            break;
                        case Type.DOUBLE:
                            mv.visitTypeInsn(CHECKCAST, "java/lang/Double");
                            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Double", "doubleValue", "()D");
                            break;
                        case Type.ARRAY:
                            mv.visitTypeInsn(CHECKCAST, paramType.getDescriptor());
                            break;
                        case Type.OBJECT:
                            mv.visitTypeInsn(CHECKCAST, paramType.getInternalName());
                            break;
                    }
                    buffer.append(paramType.getDescriptor());
                }

                buffer.append(')');
                buffer.append(Type.getDescriptor(method.getReturnType()));
                mv.visitMethodInsn(INVOKEVIRTUAL, classNameInternal, method.getName(), buffer.toString());

                switch (Type.getType(method.getReturnType()).getSort()) {
                    case Type.VOID:
                        mv.visitInsn(ACONST_NULL);
                        break;
                    case Type.BOOLEAN:
                        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;");
                        break;
                    case Type.BYTE:
                        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;");
                        break;
                    case Type.CHAR:
                        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;");
                        break;
                    case Type.SHORT:
                        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;");
                        break;
                    case Type.INT:
                        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
                        break;
                    case Type.FLOAT:
                        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;");
                        break;
                    case Type.LONG:
                        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;");
                        break;
                    case Type.DOUBLE:
                        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;");
                        break;
                }

                mv.visitInsn(ARETURN);
            }

            mv.visitLabel(defaultLabel);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        }
        
        mv.visitTypeInsn(NEW, "java/lang/IllegalArgumentException");
        mv.visitInsn(DUP);
        mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
        mv.visitInsn(DUP);
        mv.visitLdcInsn("Method not found: ");
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V");
        mv.visitVarInsn(ILOAD, 2);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(I)Ljava/lang/StringBuilder;");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;");
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/IllegalArgumentException", "<init>", "(Ljava/lang/String;)V");
        mv.visitInsn(ATHROW);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
    
    private static void defineSetField(String classNameInternal,Field[] fields, ClassWriter cw){
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "setFieldValue", "(Ljava/lang/Object;ILjava/lang/Object;)V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ILOAD, 2);

        if (fields.length > 0) {
            Label[] labels = new Label[fields.length];
            for (int i = 0, n = labels.length; i < n; i++)
                labels[i] = new Label();
            Label defaultLabel = new Label();
            mv.visitTableSwitchInsn(0, labels.length - 1, defaultLabel, labels);

            for (int i = 0, n = labels.length; i < n; i++) {
                Field field = fields[i];
                Type fieldType = Type.getType(field.getType());

                mv.visitLabel(labels[i]);
                mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
                mv.visitVarInsn(ALOAD, 1);
                mv.visitTypeInsn(CHECKCAST, classNameInternal);
                mv.visitVarInsn(ALOAD, 3);

                switch (fieldType.getSort()) {
                    case Type.BOOLEAN:
                        mv.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
                        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z");
                        break;
                    case Type.BYTE:
                        mv.visitTypeInsn(CHECKCAST, "java/lang/Byte");
                        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Byte", "byteValue", "()B");
                        break;
                    case Type.CHAR:
                        mv.visitTypeInsn(CHECKCAST, "java/lang/Character");
                        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Character", "charValue", "()C");
                        break;
                    case Type.SHORT:
                        mv.visitTypeInsn(CHECKCAST, "java/lang/Short");
                        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Short", "shortValue", "()S");
                        break;
                    case Type.INT:
                        mv.visitTypeInsn(CHECKCAST, "java/lang/Integer");
                        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I");
                        break;
                    case Type.FLOAT:
                        mv.visitTypeInsn(CHECKCAST, "java/lang/Float");
                        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Float", "floatValue", "()F");
                        break;
                    case Type.LONG:
                        mv.visitTypeInsn(CHECKCAST, "java/lang/Long");
                        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J");
                        break;
                    case Type.DOUBLE:
                        mv.visitTypeInsn(CHECKCAST, "java/lang/Double");
                        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Double", "doubleValue", "()D");
                        break;
                    case Type.ARRAY:
                        mv.visitTypeInsn(CHECKCAST, fieldType.getDescriptor());
                        break;
                    case Type.OBJECT:
                        mv.visitTypeInsn(CHECKCAST, fieldType.getInternalName());
                        break;
                }

                if(Modifier.isStatic(field.getModifiers())){
                	mv.visitFieldInsn(Opcodes.PUTSTATIC, classNameInternal, field.getName(), fieldType.getDescriptor());
                }else{
                	mv.visitFieldInsn(PUTFIELD, classNameInternal, field.getName(), fieldType.getDescriptor());	
                }
                
                mv.visitInsn(RETURN);
            }

            mv.visitLabel(defaultLabel);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        }
        mv.visitTypeInsn(NEW, "java/lang/IllegalArgumentException");
        mv.visitInsn(DUP);
        mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
        mv.visitInsn(DUP);
        mv.visitLdcInsn("Field not found: ");
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V");
        mv.visitVarInsn(ILOAD, 2);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(I)Ljava/lang/StringBuilder;");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;");
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/IllegalArgumentException", "<init>", "(Ljava/lang/String;)V");
        mv.visitInsn(ATHROW);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
    
    private static void defineGetField(String classNameInternal,Field[] fields, ClassWriter cw){
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "getFieldValue", "(Ljava/lang/Object;I)Ljava/lang/Object;", null, null);
        mv.visitCode();
        mv.visitVarInsn(ILOAD, 2);

        if (fields.length > 0) {
            Label[] labels = new Label[fields.length];
            for (int i = 0, n = labels.length; i < n; i++){
                labels[i] = new Label();
            }
            Label defaultLabel = new Label();
            mv.visitTableSwitchInsn(0, labels.length - 1, defaultLabel, labels);

            for (int i = 0, n = labels.length; i < n; i++) {
                Field field = fields[i];

                mv.visitLabel(labels[i]);
                mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
                mv.visitVarInsn(ALOAD, 1);
                mv.visitTypeInsn(CHECKCAST, classNameInternal);
                
                if(Modifier.isStatic(field.getModifiers())){
                	mv.visitFieldInsn(Opcodes.GETSTATIC, classNameInternal, field.getName(), Type.getDescriptor(field.getType()));
                }else{
                	mv.visitFieldInsn(GETFIELD, classNameInternal, field.getName(), Type.getDescriptor(field.getType()));	
                }

                Type fieldType = Type.getType(field.getType());
                switch (fieldType.getSort()) {
                case Type.BOOLEAN:
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;");
                    break;
                case Type.BYTE:
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;");
                    break;
                case Type.CHAR:
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;");
                    break;
                case Type.SHORT:
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;");
                    break;
                case Type.INT:
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
                    break;
                case Type.FLOAT:
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;");
                    break;
                case Type.LONG:
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;");
                    break;
                case Type.DOUBLE:
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;");
                    break;
                }

                mv.visitInsn(ARETURN);
            }

            mv.visitLabel(defaultLabel);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        }
        mv.visitTypeInsn(NEW, "java/lang/IllegalArgumentException");
        mv.visitInsn(DUP);
        mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
        mv.visitInsn(DUP);
        mv.visitLdcInsn("Field not found: ");
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V");
        mv.visitVarInsn(ILOAD, 2);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(I)Ljava/lang/StringBuilder;");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;");
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/IllegalArgumentException", "<init>", "(Ljava/lang/String;)V");
        mv.visitInsn(ATHROW);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
    
    private static Constructor<?> getDefaultConstructor(Class<?> type,boolean isPackageAccessible){
    	for(Constructor<?> c : type.getDeclaredConstructors()){
    		
    		int modifiers = c.getModifiers();
    		
    		if(c.getParameterTypes().length == 0){
    			
    			if(Modifier.isPrivate(modifiers)){
    				return null;
    			}
    			
    			if(Modifier.isPublic(modifiers) || Modifier.isProtected(modifiers)){
    				return c;
    			}
    			
    			if(isPackageAccessible){
    				return c;
    			}
    			
    			return null;
    		}
    	}
    	
    	return null;
    }
    
    private static Method[] getAccessibleMethods(Class<?> type){
        ArrayList<Method> methods = new ArrayList<Method>();
        Class<?> nextClass = type;
        
        while (nextClass != null && nextClass != Object.class) {
            Method[] declaredMethods = nextClass.getDeclaredMethods();
            
            for (int i = 0, n = declaredMethods.length; i < n; i++) {
                Method method = declaredMethods[i];

                if (Modifier.isPrivate(method.getModifiers())) {
                	continue;
                }
                
                methods.add(method);
            }
            
            nextClass = nextClass.getSuperclass();
        }
        
        return methods.toArray(new Method[methods.size()]);
    }
    
    private static Field[] getAccessibleFields(Class<?> type){
        ArrayList<Field> fields = new ArrayList<Field>();
        Class<?> nextClass = type;
        String typePackageName = Classes.getPackageName(type);
        
        Set<String> processed = new HashSet<String>();
        
        while (nextClass != null && nextClass != Object.class) {
            Field[] declaredFields = nextClass.getDeclaredFields();
            
            for (int i = 0, n = declaredFields.length; i < n; i++) {
                Field field = declaredFields[i];
                
                if(processed.contains(field.getName())){
                	continue;
                }else{
                	processed.add(field.getName());
                }
                
                if (Modifier.isPrivate(field.getModifiers())) {
                	continue;
                }
                
                if(Modifier.isFinal(field.getModifiers())){
                	continue;
                }
                
                if(field.isSynthetic()){
                	continue;
                }
                
                if(!Modifier.isPublic(field.getModifiers())){
                	if(type.getPackage() == null){
                		continue;
                	}

                	if(type != nextClass){
                		String nextClassPackageName = Classes.getPackageName(nextClass);
                		if(!Strings.equals(typePackageName, nextClassPackageName)){
                			continue;
                		}
                	}
                }
                
                fields.add(field);
            }
            
            nextClass = nextClass.getSuperclass();
        }
        
        return fields.toArray(new Field[fields.size()]);
    }
    
    private static int getPrimitiveTypeCode(Class<?> primitiveType){
    	Type type = Type.getType(primitiveType);
    	
        int typeCode;
        switch (type.getSort()) {
            case Type.BOOLEAN:
                typeCode = Opcodes.T_BOOLEAN;
                break;
            case Type.CHAR:
                typeCode = Opcodes.T_CHAR;
                break;
            case Type.BYTE:
                typeCode = Opcodes.T_BYTE;
                break;
            case Type.SHORT:
                typeCode = Opcodes.T_SHORT;
                break;
            case Type.INT:
                typeCode = Opcodes.T_INT;
                break;
            case Type.FLOAT:
                typeCode = Opcodes.T_FLOAT;
                break;
            case Type.LONG:
                typeCode = Opcodes.T_LONG;
                break;
            case Type.DOUBLE:
                typeCode = Opcodes.T_DOUBLE;
                break;
            default : 
            	throw new IllegalStateException("not a primitive type");
        }
        return typeCode;
    }
    
    private static String getAccessorClassNameFor(Class<?> clazz){
    	String className = clazz.getName();
    	
    	if(className.startsWith("java.") || className.startsWith("javax.")){
    		className = "leap." + className;
    	}
    	
    	return className + "$LeapReflectAccessor";
    }
    
    static final class ReflectLoader extends ClassLoader {
    	
    	ReflectLoader (ClassLoader parent) {
    		super(parent);
    	}

    	protected synchronized java.lang.Class<?> loadClass (String name, boolean resolve) throws ClassNotFoundException {
    	    if (name.equals(ASMReflectAccessor.class.getName())) {
    		    return ASMReflectAccessor.class;
    		}
    		
    		return super.loadClass(name, resolve);
    	}

    	Class<?> defineClass (String name, byte[] bytes) throws ClassFormatError {
    		try {
    			Method method = ClassLoader.class.getDeclaredMethod("defineClass", 
    			                                                    new Class[] {String.class, byte[].class, int.class,int.class});
    			
    			method.setAccessible(true);
    			
    			return (Class<?>)method.invoke(getParent(), new Object[] {name, bytes, new Integer(0), new Integer(bytes.length)});
    		} catch (Exception ignored) {
    		    //do nothing
    		}
    		
    		return defineClass(name, bytes, 0, bytes.length);
    	}
    }
}