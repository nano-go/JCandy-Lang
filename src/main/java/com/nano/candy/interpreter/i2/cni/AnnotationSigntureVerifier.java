package com.nano.candy.interpreter.i2.cni;

import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.vm.VM;
import com.nano.candy.std.Names;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;

class AnnotationSigntureVerifier {
	
	protected static void verifyAnnotationPresent(Method m, 
	                                              Class<? extends Annotation> anno) {
		if (!m.isAnnotationPresent(anno)) {
			error("The method %s must be annotated with the %s.",
				m.getName(), anno.getSimpleName());
		}
	}
	
	protected static void verifyAnnotationPresent(Class c, 
	                                              Class<? extends Annotation> anno) {
		if (!c.isAnnotationPresent(anno)) {
			error("The class %s must be annotated with the %s.",
				  c.getName(), anno.getSimpleName());
		}
	}
	
	protected static void verifyNativeFunc(Method m, int arity, int varAgrsIndex,
	                                       String name) {
		if (!Modifier.isStatic(m.getModifiers())) {
			error("The method %s must be static.", m.getName());
		}
		verifyCallable(m, arity, varAgrsIndex, name);
	}
	
	protected static void verifyNativeMethod(Method m, int arity, int varAgrsIndex,
	                                         String name) {
		if (Modifier.isStatic(m.getModifiers())) {
			error("The method %s can't be static.", m.getName());
		}
		if (Modifier.isAbstract(m.getModifiers())) {
			error("Invalid method modifier: abstract.", m.getName());
		}
		verifyCallable(m, arity, varAgrsIndex, name);
	}
	
	protected static void verifyCallable(Method m, int arity, int varAgrsIndex,
	                                     String name) {
		if (arity < 0) {
			error("Invalid arity %d of the method %s.", arity, m.getName());
		}
		if (!Names.isCandyIdentifier(name)) {
			error("Invalid name %s of the method %s.", name, m.getName());
		}
		if (varAgrsIndex >= arity) {
			error("Invalid var args index %d of the method %s.", 
				varAgrsIndex, m.getName());
		}
		if (m.getParameterCount() != 2) {
			error("The method %s must have two paramters.", m.getName());
		}
		Parameter[] params = m.getParameters();
		if (params[0].getType() != VM.class) {
			error(
				"The first parameter of the method %s must receive a %s instance",
				m.getName(), VM.class.getSimpleName());
		}
		if (!params[1].getType().isArray() ||
			params[1].getType().getComponentType() != CandyObject.class) {
			error("The second parameter of the method %s must receive a"
				  + " %s array.",
				  m.getName(), CandyObject.class.getSimpleName());
		}
		if (m.getReturnType() != CandyObject.class) {
			error("The return type of the method '%s' must be %s",
				  m.getName(), CandyObject.class.getSimpleName());
		}
	}
	
	public static void verifyNativeClass(Class<? extends CandyObject> c, 
	                                     String nativeClassName, 
										 boolean isInheritable) {
		if (!Names.isCandyIdentifier(nativeClassName)) {
			error("Invalid name %s of the class %s.", 
				nativeClassName, c.getName());
		}	
		if (Modifier.isInterface(c.getModifiers())) {
			error("The class %s can't be a interface.", 
				nativeClassName);
		}
		if (!isInheritable) {
			return;
		}
		try {
			c.getDeclaredConstructor();
		} catch (Exception e) {
			error("The class %s must have a constructor with empty argument"
			      + " if it is an iheritable class.", 
				  nativeClassName);
		}
	}

	protected static void error(String fmt, Object... args) {
		throw new VerifyError(String.format(fmt, args));
	}
}
