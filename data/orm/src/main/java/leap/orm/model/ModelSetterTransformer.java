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
import leap.lang.asm.commons.AdviceAdapter;
import leap.lang.asm.commons.GeneratorAdapter;
import leap.lang.asm.tree.ClassNode;
import leap.lang.asm.tree.FieldNode;
import leap.lang.asm.tree.MethodNode;
import leap.lang.reflect.Reflection;

class ModelSetterTransformer implements ModelTransformer {
	
	private static final String PUT_METHOD_DESC;
	
	static {
		Method m = Reflection.findMethod(Model.class, "set", String.class,Object.class);
		PUT_METHOD_DESC = Type.getMethodDescriptor(m);
	}

	@Override
    public MethodVisitor transform(ClassNode cn, ClassWriter cw, MethodNode mn) {
		String name = mn.name;
		
		if(Beans.isSetterMethod(name) && !ASM.hasReturnValue(mn) && ASM.getArgumentSize(mn) == 1){
			String fieldName = Beans.extractPropertyFromSetter(name);

			FieldNode fn = ASM.tryGetField(cn, fieldName);
			
			if(null != fn){
				//return new SetterMethodTransformer(cn,method,field,ASM.visitMethod(cw, method));
				return doTransform(cn, cw, mn, fn);
			}
		}
		
	    return null;
    }
	
	@SuppressWarnings("deprecation")
    protected MethodVisitor doTransform(ClassNode cn,ClassWriter cw,MethodNode mn,FieldNode fn) {
		MethodVisitor    mv = cw.visitMethod(mn.access, mn.name, mn.desc, mn.signature,Collections2.toStringArray(mn.exceptions));
		GeneratorAdapter mg = new GeneratorAdapter(mv, mn.access, mn.name, mn.desc);
		
		mg.visitCode();
		
		/*
		 * put(field,value);
		 */
		/*
			mv.visitVarInsn(ALOAD, 0);
			mv.visitLdcInsn("address");
			mv.visitVarInsn(ALOAD, 1);
			mv.visitMethodInsn(INVOKEVIRTUAL, "leap/orm/tested/model/Person", "set", "(Ljava/lang/String;Ljava/lang/Object;)V");
		 */
		mg.loadThis();
		mg.visitLdcInsn(fn.name);
		mg.loadArg(0);
		mg.valueOf(ASM.getArgumentTypes(mn)[0]);
		mg.visitMethodInsn(Opcodes.INVOKEVIRTUAL, cn.name, "set", PUT_METHOD_DESC);
		
		mg.endMethod();
		return mg;
	}
	
	protected class SetterMethodTransformer extends AdviceAdapter {
		
		private final ClassNode  cn;
		private final MethodNode m;
		private final FieldNode  field;

		public SetterMethodTransformer(ClassNode cn, MethodNode m,FieldNode field, MethodVisitor mv) {
	        super(ASM.API, mv, m.access, m.name, m.desc);
	        this.cn    = cn;
	        this.m	   = m;
	        this.field = field;
        }

		@SuppressWarnings("deprecation")
        @Override
        protected void onMethodExit(int opcode) {
			/*
			 * put(field,value);
			 */
			/*
				mv.visitVarInsn(ALOAD, 0);
				mv.visitLdcInsn("address");
				mv.visitVarInsn(ALOAD, 1);
				mv.visitMethodInsn(INVOKEVIRTUAL, "leap/orm/tested/model/Person", "put", "(Ljava/lang/String;Ljava/lang/Object;)V");
			 */
			loadThis();
			visitLdcInsn(field.name);
			loadArg(0);
			valueOf(ASM.getArgumentTypes(m)[0]);
			visitMethodInsn(INVOKEVIRTUAL, cn.name, "put", PUT_METHOD_DESC);
		}
	}
}
