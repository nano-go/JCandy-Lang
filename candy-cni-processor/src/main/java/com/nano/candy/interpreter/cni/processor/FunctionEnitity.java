package com.nano.candy.interpreter.cni.processor;

import com.nano.candy.interpreter.cni.NativeFunc;
import com.nano.candy.interpreter.cni.NativeMethod;
import java.lang.annotation.Annotation;
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
	private int optionalArgFlags;

	private String annotatedJavaMethodName;
	private String qualifiedClassName;
	
	private Class<? extends Annotation> annotationClass;

	public FunctionEnitity(ExecutableElement annotatedElement, 
	                       Class<? extends Annotation> annotationClass) {
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
		}
		this.arity = annotatedElement.getParameters().size()-1;
		this.optionalArgFlags = getOptionalArgFlagsValue();
		checkValidateFunction();
	}

	private int getOptionalArgFlagsValue() {
		List<? extends VariableElement> params = annotatedElement.getParameters();
		int flags = 0;
		for (int i = 1; i < params.size(); i ++) {
			TypeMirror paramType = params.get(i).asType();
			if (TypeNames.isOptionalArg(paramType)) {
				flags |= (1<<(i-1));
			}
		}
		return flags;
	}
	
	private void checkValidateFunction() {
		List<? extends VariableElement> params = annotatedElement.getParameters();
		checkParameters(params);
		checkVaargIndexAndOptionalArgBits(params);	
		checkReturnType();
	}

	private void checkParameters(List<? extends VariableElement> params) {
		if (params.size() < 1) {
			Tools.throwArgException(
				"The method '%s' annotated with @%s requires 1 parameter at least.",
				annotationClass.getSimpleName(), annotatedJavaMethodName);
		}
		if (params.size() > 33) {
			Tools.throwArgException(
				"The method '%s' annotated with @%s can not declare parameters" 
				+ " greater than 32.",
				annotationClass.getSimpleName(), annotatedJavaMethodName);
			
		}
		if (!TypeNames.isCNIEnc(params.get(0).asType())) {
			Tools.throwArgException(
				"The first parameter of the method '%s' annotated with @%s" +
				" must be a CNIEnv.",
				annotatedJavaMethodName, annotationClass.getSimpleName());
		}
		for (int i = 1; i < params.size(); i ++) {
			VariableElement ve = params.get(i);
			checkValidArgType(ve);
		}
	}
	
	private void checkValidArgType(VariableElement ve) {
		TypeMirror type = ve.asType();
		if (TypeNames.isBaseCandyObjType(type) ||
		    TypeNames.isSupportedPriType(type) ||
		    TypeNames.isOptionalArg(type)) {
			return;
		}
		Tools.throwArgException("Unsupported parameter type: %s", type.toString());
	}
	
	private void checkVaargIndexAndOptionalArgBits(List<? extends VariableElement> params) {
		if (varArgsIndex < -1 || (varArgsIndex != -1 && varArgsIndex >= arity)) {
			Tools.throwArgException(
				"Illegal varArgIndex(): %d. it must (= -1) or (< arity).", 
				varArgsIndex);
		} else if (varArgsIndex != -1) {
			TypeMirror vaargType = params.get(varArgsIndex + 1).asType();
			if (!TypeNames.isArrayObj(vaargType)) {
				Tools.throwArgException(
					"The parameter that receives any number of arguments" + 
					" must be %s.", TypeNames.CANDY_ARRAY_TYPE);
			}
			if (Tools.getOptionalArgCount(optionalArgFlags) != 0) {
				int highestOptionalArgIndex = Integer.highestOneBit(optionalArgFlags) - 1;
				if (highestOptionalArgIndex >= varArgsIndex) {
					Tools.throwArgException(
						"The parameter that receives any number of arguments" +
						" must be declared after all optional arguments.");
				}
			}
		}
	}
	
	private void checkReturnType() {
		TypeMirror retType = annotatedElement.getReturnType();
		if (!TypeNames.isBaseCandyObjType(retType)) {
			Tools.throwArgException("The return-type must be a CandyObject.");
		}
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
	
	public int getOptionalArgFlags() {
		return optionalArgFlags;
	}
}
