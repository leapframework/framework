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
package leap.lang.asm;

import leap.lang.*;
import leap.lang.asm.tree.*;
import leap.lang.asm.util.*;
import leap.lang.exception.ObjectNotFoundException;
import leap.lang.resource.Resource;
import leap.lang.resource.Resources;

import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * This class is not from original asm sources.
 */
public class ASM {
	
	public static final int    API 				= Opcodes.ASM5;
	public static final String CONSTRUCTOR_NAME = "<init>";
	public static final String STATIC_INIT_NAME = "<clinit>"; 
	
	public static String opcodeName(int opcode) {
		return Printer.OPCODES[opcode];
	}
	
	public static MethodVisitor visitMethod(ClassVisitor cv,MethodNode m){
		return cv.visitMethod(m.access, m.name, m.desc, m.signature, Collections2.toStringArray(m.exceptions));
	}
	
	public static boolean isConstructor(MethodNode m){
		return m.name.equals(CONSTRUCTOR_NAME);
	}
	
	public static boolean isStaticInit(MethodNode m){
		return m.name.equals(STATIC_INIT_NAME);
	}
	
	public static boolean isStatic(MethodNode m){
		return Modifier.isStatic(m.access);
	}
	
	public static boolean isAnnotationPresent(MethodNode m,Class<? extends Annotation> annotationType){
		if(null == m.visibleAnnotations){
			return false;
		}
		
		String desc = Type.getDescriptor(annotationType);
		for(AnnotationNode an : m.visibleAnnotations){
			if(an.desc.equals(desc)){
				return true;
			}
		}
		return false;
	}
	
	public static ClassNode getClassNode(ClassReader cr){
		ClassNode cn = new ClassNode();
		cr.accept(cn, 0);
		return cn;
	}
	
	public static MethodNode getMethod(ClassNode cn,String name,String desc){
		MethodNode m = tryGetMethod(cn, name, desc);
		
		if(null == m){
			throw new ObjectNotFoundException("MethodNode '" + name + " " + desc + "' not found in class '" + cn.name + "'");
		}
		
		return m;
	}
	
	public static MethodNode tryGetMethod(ClassNode cn,String name,String desc){
		if(null == cn.methods){
			return null;
		}
		for(MethodNode m : cn.methods){
			if(m.name.equals(name) && m.desc.equals(desc)){
				return m;
			}
		}
		return null;
	}
	
	public static FieldNode getField(ClassNode cn,String name){
		FieldNode f = tryGetField(cn, name);
		
		if(null == f){
			throw new ObjectNotFoundException("FieldNode '" + name + "' not found in class '" + cn.name + "'");
		}
		
		return f;
	}
	
	public static FieldNode tryGetField(ClassNode cn,String name){
		if(null == cn.fields){
			return null;
		}
		for(FieldNode f : cn.fields){
			if(f.name.equals(name)){
				return f;
			}
		}
		return null;
	}
	
	public static MethodNode[] getMethods(ClassNode cn,Predicate<MethodNode> predicate){
		final List<MethodNode> methods = new ArrayList<MethodNode>();
		for(MethodNode m : cn.methods){
			if(predicate.test(m)){
				methods.add(m);
			}
		}
		return methods.toArray(new MethodNode[methods.size()]);
	}
	
	public static AnnotationNode getAnnotation(MethodNode m,Class<? extends Annotation> annotationType) {
		AnnotationNode a = getAnnotation(m.visibleAnnotations, annotationType);
		
		if(null == a){
			a = getAnnotation(m.invisibleAnnotations, annotationType);
		}
		
		return a;
	}

    public static Map<String,Object> getAnnotationValues(AnnotationNode a) {
        Map<String,Object> map = new LinkedHashMap<>();

        if(null != a.values) {

            for(int i=0;i<a.values.size();i++) {
                String name  = (String)a.values.get(i);
                Object value = a.values.get(i+1);

                if(value instanceof String[]) {
                    //enum value
                    String[] array = (String[])value;
                    Class<? extends Enum> enumType = (Class<? extends Enum>)Classes.forName(Type.getType(array[0]).getClassName());

                    map.put(name, Enums.nameOf(enumType, array[1]));
                }else{
                    map.put(name, value);
                }

                i+=1;
            }
        }

        return map;
    }
	
	private static AnnotationNode getAnnotation(List<AnnotationNode> annotations,Class<? extends Annotation> annotationType){
		String desc = Type.getDescriptor(annotationType);

		if(null != annotations){
			for(AnnotationNode a : annotations){
				if(a.desc.equals(desc)){
					return a;
				}
			}
		}
		return null;
	}
	
	public static Class<?> getClassType(String internalName) {
		return getClassType(Type.getObjectType(internalName));
	}
	
	public static Class<?> getClassType(Type type) {
		switch (type.getSort()) {
			case Type.BOOLEAN:
				return Boolean.TYPE;
			case Type.CHAR:
				return Character.TYPE;
			case Type.BYTE:
				return Byte.TYPE;
			case Type.SHORT:
				return Short.TYPE;
			case Type.INT:
				return Integer.TYPE;
			case Type.FLOAT:
				return Float.TYPE;
			case Type.LONG:
				return Long.TYPE;
			case Type.DOUBLE:
				return Double.TYPE;
			case Type.VOID:
				return Void.TYPE;
		}
		String cn = type.getInternalName();
		cn = cn != null ? cn.replace('/', '.') : type.getClassName();
		return Classes.forName(cn);
	}

	public static Class<?>[] getArgumentClassTypes(MethodNode m) {
		Type[] argTypes = Type.getArgumentTypes(m.desc);
		
		if(argTypes == null || argTypes.length == 0) {
			return Arrays2.EMPTY_CLASS_ARRAY;
		}
		
		Class<?>[] classTypes = new Class<?>[argTypes.length];
		for(int i=0;i<argTypes.length;i++) {
			classTypes[i] = getClassType(argTypes[i]);
		}
		return classTypes;
	}
	
	public static Type[] getArgumentTypes(MethodNode m){
		return Type.getArgumentTypes(m.desc);
	}
	
	public static int getArgumentSize(MethodNode m){
		return Type.getArgumentTypes(m.desc).length;
	}
	
	public static Type getReturnType(MethodNode m){
		return Type.getReturnType(m.desc);
	}
	
	public static boolean hasArgument(MethodNode m){
		return getArgumentSize(m) > 0;
	}
	
	public static boolean hasReturnValue(MethodNode m){
		return Type.getReturnType(m.desc).getSort() != Type.VOID;
	}
	
	public static InsnNode nextInsnNode(AbstractInsnNode node,int opcode){
		if(null == node){
			return null;
		}
		do{
			if(node.getType() == AbstractInsnNode.LABEL || node.getType() == AbstractInsnNode.LINE){
				continue;
			}
			
			if(node.getType() != AbstractInsnNode.INSN){
				return null;
			}
			
			InsnNode insn = (InsnNode)node;
			
			if(opcode != insn.getOpcode()){
				return null;
			}
			
			return insn;
			
		}while((node = node.getNext()) != null);
		return null;
	}
	
	public static void printASMifiedCodes(MethodNode m) {
		ASMifier1 p = new ASMifier1();
		PrintWriter pw = new PrintWriter(System.out);
		m.accept(new TraceMethodVisitor(p));
		p.print(pw);
		pw.flush();
	}
	
	public static void printASMifiedCode(byte[] data,PrintWriter out) {
        ClassReader cr = new ClassReader(data);
        cr.accept(new TraceClassVisitor(null, new ASMifier(),out),ClassReader.EXPAND_FRAMES);
	}

    public static void printASMifiedCode(byte[] data) {
        printASMifiedCode(data, new PrintWriter(System.out));
    }

    public static void pintASMifiedCode(Class cls) {
        Resource r = Resources.getResource(cls);

        Try.throwUnchecked(() -> {

            try(InputStream is = r.getInputStream()) {
                ClassReader cr = new ClassReader(is);
                ClassWriter cw = new ClassWriter(cr,ClassWriter.COMPUTE_FRAMES);

                cr.accept(cw, 0);

                ASM.printASMifiedCode(cw.toByteArray(), new PrintWriter(System.out));
            }

        });
    }
	
	protected ASM(){
		
	}
}
