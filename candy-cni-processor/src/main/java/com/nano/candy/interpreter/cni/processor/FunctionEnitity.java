package com.nano.candy.interpreter.cni.processor;

import com.nano.candy.interpreter.cni.NativeFunc;
import com.nano.candy.interpreter.cni.NativeMethod;
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * This class represents a Java method.
 */
public class FunctionEnitity {
    
	private ExecutableElement annotatedElement;

	private String name;
	private int arity;
	private int varArgsIndex;

	private String annotatedJavaMethodName;
	private String qualifiedClassName;
	
	private Class annotationClass;

	public FunctionEnitity(ExecutableElement annotatedElement, Class annotationClass) {
		this.annotationClass = annotationClass;
		this.annotatedElement = annotatedElement;
		this.annotatedJavaMethodName = annotatedElement.getSimpleName().toString();
		
		TypeElement klass = (TypeElement) annotatedElement.getEnclosingElement();
		this.qualifiedClassName = klass.getQualifiedName().toString();
		
		if (annotationClass == NativeMethod.class) {
			NativeMethod javaMet = annotatedElement.getAnnotation(NativeMethod.class);
			this.name = javaMet.name();
			this.varArgsIndex = javaMet.varArgsIndex();
		} else {
			NativeFunc javaFunc = annotatedElement.getAnnotation(NativeFunc.class);
			this.name = javaFunc.name();
			this.varArgsIndex = javaFunc.varArgsIndex();
			checkValidateFunction();
		}
		this.arity = annotatedElement.getParameters().size()-1;
		checkValidateFunction();
	}
	
	private void checkValidateFunction() {
		List<? extends VariableElement> params = annotatedElement.getParameters();
		if (params.size() < 1) {
			Tools.throwArgException(
				"The method '%s' annotated with @%s requires 1 parameter at least.",
				annotationClass.getSimpleName() ,annotatedJavaMethodName);
		}
		if (!TypeNames.isCNIEnc(params.get(0).asType())) {
			Tools.throwArgException(
				"The first parameter of the method '%s' annotated with @%s" +
				" must be a CNIEnv.",
				annotatedJavaMethodName, annotationClass.getSimpleName());
		}
		if (varArgsIndex < -1 || (varArgsIndex != -1 && varArgsIndex >= arity)) {
			Tools.throwArgException(
				"Illegal varArgIndex(): %d. it must (= -1) or (< arity).", 
				varArgsIndex);
		} else if (varArgsIndex != -1) {
			TypeMirror vararg = params.get(varArgsIndex + 1).asType();
			if (!TypeNames.CANDY_ARRAY_TYPE.equals(vararg.toString())) {
				Tools.throwArgException(
					"Variable-arguments must be a %s, but %s.", 
					TypeNames.CANDY_ARRAY_TYPE, vararg.toString());
			}
		}
		for (int i = 1; i < params.size(); i ++) {
			VariableElement ve = params.get(i);
			checkValidArgType(ve);
		}
	}
	
	private void checkValidArgType(VariableElement ve) {
		TypeMirror type = ve.asType();
		if (TypeNames.isBaseCandyObjType(type)) {
			return;
		}
		if (TypeNames.isSupportedPriType(type)) {
			return;
		}
		Tools.throwArgException("Unsupported argument type: %s", type.toString());
	}
	
	public ExecutableElement getAnnotatedElement() {
		return annotatedElement;
	}

	public String getQualifiedClassName() {
		return qualifiedClassName;
	}

	public String getAnnotatedJavaMethodName() {
		return annotatedJavaMethodName;
	}

	public String getName() {
		return name;
	}

	public int getArity() {
		return arity;
	}

	public int getVarArgIndex() {
		return varArgsIndex;
	}
}
