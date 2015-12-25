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

import leap.lang.Collections2;
import leap.lang.asm.ASM;
import leap.lang.asm.ClassWriter;
import leap.lang.asm.MethodVisitor;
import leap.lang.asm.Opcodes;
import leap.lang.asm.Type;
import leap.lang.asm.commons.GeneratorAdapter;
import leap.lang.asm.tree.ClassNode;
import leap.lang.asm.tree.InsnList;
import leap.lang.asm.tree.InsnNode;
import leap.lang.asm.tree.MethodNode;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.orm.annotation.Finder;
import leap.orm.model.ModelRegistry.ModelContext;

public class ModelFinderTransformer implements ModelTransformer {
	
	private static final Log log = LogFactory.get(ModelFinderTransformer.class);
	
	@Override
	public MethodVisitor transform(ClassNode cn, ClassWriter cw, MethodNode method) {
		if(isFinderMethod(method)){
			InsnList instructions = method.instructions;

			InsnNode nullNode = ASM.nextInsnNode(instructions.getFirst(), Opcodes.ACONST_NULL);
			
			if(null != nullNode){
				InsnNode returnNode = ASM.nextInsnNode(nullNode.getNext(), Opcodes.ARETURN);
				
				if(null != returnNode){
					
					log.debug("Create dynamic finder '{}'",method.name);
					
					return doTransform(cn, cw, method);
				}
			}
		}
		
		return null;
	}

	protected MethodVisitor doTransform(ClassNode cn, ClassWriter cw, MethodNode mn){
		MethodVisitor    mv = cw.visitMethod(mn.access, mn.name, mn.desc, mn.signature,Collections2.toStringArray(mn.exceptions));
		GeneratorAdapter mg = new GeneratorAdapter(mv, mn.access, mn.name, mn.desc);
		
		//Used in ModelMapper for registering finder methods.
		if(null == ASM.getAnnotation(mn, Finder.class)){
			mg.visitAnnotation(Type.getDescriptor(Finder.class), true);	
		}
		
		mg.visitCode();
		
		mg.visitMethodInsn(Opcodes.INVOKESTATIC, cn.name, "context","()Lleap/orm/model/ModelRegistry$ModelContext;",false);
		mg.visitLdcInsn(mn.name);
		mg.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(ModelContext.class), "getFinder","(Ljava/lang/String;)Lleap/orm/model/ModelFinder;",false);
		
		mg.loadArgArray();
		
		Type returnType = ASM.getReturnType(mn);
		if(returnType.getInternalName().equals("java/util/List")){
			mg.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "leap/orm/model/ModelFinder", "findAll", "([Ljava/lang/Object;)Ljava/util/List;",false);
		}else if(returnType.getInternalName().equals(cn.name)){
			mg.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "leap/orm/model/ModelFinder", "find", "([Ljava/lang/Object;)Ljava/lang/Object;",false);
			mg.visitTypeInsn(Opcodes.CHECKCAST, cn.name);
		}else {
			throw new IllegalStateException("Return type '" + returnType.getInternalName() + "' not supported by dynamic finder method");
		}
		mg.returnValue();
		mg.endMethod();
		return mg;
	}
	
	private static boolean isFinderMethod(MethodNode m){
		if(!ASM.isStatic(m)){
			return false;
		}
		
		if(!ASM.hasReturnValue(m)){
			return false;
		}
		
		if(!ASM.hasArgument(m)){
			return false;
		}
		
		if(m.name.startsWith(ModelFinder.FIND_BY) && m.name.length() > ModelFinder.FIND_BY.length()){
			return true;
		}
		
		if(m.name.startsWith(ModelFinder.FIND_ALL_BY) && m.name.length() > ModelFinder.FIND_ALL_BY.length()){
			return true;
		}
		
		return false;
	}
}
