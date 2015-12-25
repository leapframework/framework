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

import java.lang.reflect.Method;

import leap.lang.Beans;
import leap.lang.Collections2;
import leap.lang.asm.ASM;
import leap.lang.asm.ClassWriter;
import leap.lang.asm.MethodVisitor;
import leap.lang.asm.Opcodes;
import leap.lang.asm.Type;
import leap.lang.asm.commons.GeneratorAdapter;
import leap.lang.asm.tree.ClassNode;
import leap.lang.asm.tree.FieldNode;
import leap.lang.asm.tree.MethodNode;
import leap.lang.reflect.Reflection;

@Deprecated
class ModelGetterTransformer implements ModelTransformer {
	
	private static final String GET_METHOD_DESC;
	
	static {
		Method m = Reflection.findMethod(Model.class, "get", String.class);
		GET_METHOD_DESC = Type.getMethodDescriptor(m);
	}

	@Override
    public MethodVisitor transform(ClassNode cn, ClassWriter cw, MethodNode mn) {
		String name = mn.name;
		
		if(Beans.isGetterMethod(name) && ASM.hasReturnValue(mn) && ASM.getArgumentSize(mn) == 0){
			String fieldName = Beans.extraPropertyFromGetter(name);

			FieldNode fn = ASM.tryGetField(cn, fieldName);
			
			if(null != fn){
				return doTransform(cn, cw, mn, fn);
			}
		}
		
	    return null;
    }
	
	protected MethodVisitor doTransform(ClassNode cn,ClassWriter cw,MethodNode mn,FieldNode fn) {
		MethodVisitor    mv = cw.visitMethod(mn.access, mn.name, mn.desc, mn.signature,Collections2.toStringArray(mn.exceptions));
		GeneratorAdapter mg = new GeneratorAdapter(mv, mn.access, mn.name, mn.desc);
		
		mg.visitCode();
		mg.loadThis();
		mg.visitLdcInsn(fn.name);
		mg.visitMethodInsn(Opcodes.INVOKEVIRTUAL, cn.name, "get", GET_METHOD_DESC);
		mv.visitLdcInsn(ASM.getReturnType(mn));
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, "leap/lang/convert/Converts", "convert", "(Ljava/lang/Object;Ljava/lang/Class;)Ljava/lang/Object;");
		mg.unbox(ASM.getReturnType(mn));
		mg.returnValue();
		mg.endMethod();
		return mg;
	}
}
