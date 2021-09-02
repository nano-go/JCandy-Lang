package com.nano.candy.interpreter.cni.processor;

import com.google.auto.service.AutoService;
import com.nano.candy.interpreter.cni.NativeFunc;
import com.nano.candy.interpreter.cni.NativeMethod;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
public class FunctionsProcessor extends AbstractProcessor {
	protected Messager messager;
	protected Elements elementUtils;
	protected Filer filer;
	
	@Override
	public void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		this.messager = processingEnv.getMessager();
		this.elementUtils = processingEnv.getElementUtils();
		this.filer = processingEnv.getFiler();
	}

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		Set<String> annotationTypes = new HashSet<>();
		annotationTypes.add(NativeFunc.class.getCanonicalName());
		annotationTypes.add(NativeMethod.class.getCanonicalName());
		return annotationTypes;
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		processElements(roundEnv, NativeFunc.class, new FunctionsCodeGenerator(), (e) -> {
			if (!Tools.isPublicStaticMethod(e)) {
				error(e, "Only public and static method can be annotated with @%s.",
					NativeFunc.class.getSimpleName());
				return false;
			}
			return true;
		});
		
		processElements(roundEnv, NativeMethod.class, new MethodsCodeGenerator(), (e) -> {
			if (!Tools.isPublicOrProtectedMethod(e)) {
				error(e, "Only public method can be annotated with @%s.",
					  NativeMethod.class.getSimpleName());
				return false;
			}
			return true;
		});
		return true;
	}
	
	private void processElements(RoundEnvironment env,
	                             Class<? extends Annotation> annotation,                          
								 CodeFileGenerator codeFileGenerator,
								 Predicate<Element> filter) {
		HashMap<String, List<FunctionEnitity>> methods = new HashMap<>();
		for (Element element : env.getElementsAnnotatedWith(annotation)) {
			if (!filter.test(element)) {
				continue;
			}
			addElement(methods, element, annotation);
		}
		methods.forEach((k, v) -> {
			try {
				codeFileGenerator.generateCode(elementUtils, filer, k, v);
			} catch (IOException e) {
				error(v.get(0).getAnnotatedElement().getEnclosingElement(), 
					  "Unable to write: %s.", e.getMessage());
			}
		});					 
	}
	
	private void addElement(HashMap<String, List<FunctionEnitity>> functions,
	                        Element element, 
	                        Class<? extends Annotation> annotation) {
		try {
			FunctionEnitity func = new FunctionEnitity((ExecutableElement) element, annotation);
			List<FunctionEnitity> list = functions.get(func.getQualifiedClassName());
			if (list == null) {
				list = new ArrayList<>();
				functions.put(func.getQualifiedClassName(), list);
			}
			list.add(func);
		} catch (IllegalArgumentException e) {
			error(element, e.getMessage());
		}
	}
	
	protected void message(Element e, String msg, Object... args) {
		messager.printMessage(Diagnostic.Kind.NOTE, String.format(msg, args), e);
	}

	protected void error(Element e, String msg, Object... args) {
		messager.printMessage(Diagnostic.Kind.ERROR, String.format(msg, args), e);
	}
}
