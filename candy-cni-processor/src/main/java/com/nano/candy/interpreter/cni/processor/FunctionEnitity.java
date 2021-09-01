package com.nano.candy.interpreter.cni.processor;

import com.nano.candy.interpreter.cni.NativeFunc;
import com.nano.candy.interpreter.cni.NativeMethod;
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

/**
 * This class represents a function or a method defined in a class.
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
			this.arity = javaMet.arity();
			this.varArgsIndex = javaMet.varArgsIndex();
		} else {
			NativeFunc javaFunc = annotatedElement.getAnnotation(NativeFunc.class);
			this.name = javaFunc.name();
			this.arity = javaFunc.arity();
			this.varArgsIndex = javaFunc.varArgsIndex();
		}

		checkValidateFunction();
	}

	private void checkValidateFunction() {
		if (!Tools.isCandyIdentifier(this.name)) {
			Tools.throwArgException(
				"It is not allow that the name() of the %s is not a" +
				" Candy identifier: %s", 
				annotationClass.getSimpleName(), name);
		}

		if (arity < 0) {
			Tools.throwArgException(
				"The arity() of the %s must be greater than zero, but " +
				"the arity() is %d.",
				annotationClass.getSimpleName(), arity);
		}

		// varArgsIndex is -1 or < arity
		if (varArgsIndex < -1 || (varArgsIndex != -1 && varArgsIndex >= arity)) {
			Tools.throwArgException(
				"Illegal varArgIndex(): %d. it must (= -1) or (< arity).", 
				varArgsIndex);
		}
		checkJavaMethodType();
	}

	/**
	 * Requires the annotated Java method has two parameters.
	 *
	 * The first parameter is a `CNIEnv` type and the secondary parameter 
	 * is a `CandyObject[]`.
	 *
	 * And requires the return type is a `CandyObject`. 
	 */
	private void checkJavaMethodType() {
		List<? extends VariableElement> params = annotatedElement.getParameters();
		if (params.size() != 2) {
			Tools.throwArgException(
				"The method '%s' annotated with @%s requires three parameters.",
				annotationClass.getSimpleName() ,annotatedJavaMethodName);
		}

		if (!TypeNames.isCNIEnc(params.get(0).asType())) {
			Tools.throwArgException(
				"The first parameter of the method '%s' annotated with @%s" +
				" must be a CNIEnv.",
				annotatedJavaMethodName, annotationClass.getSimpleName());
		}

		if (!TypeNames.isCandyObjectArray(params.get(1).asType())) {
			Tools.throwArgException(
				"The secondary parameter of the method '%s' annotated with @%s" +
				" must be a CandyObject array.",
				annotatedJavaMethodName, annotationClass.getSimpleName());
		}

		if (!TypeNames.isCandyObject(annotatedElement.getReturnType())) {
			Tools.throwArgException(
				"The return type of the method '%s' annotated with @%s" +
				" must be a CandyObject.",
				annotatedJavaMethodName, annotationClass.getSimpleName());
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
}
