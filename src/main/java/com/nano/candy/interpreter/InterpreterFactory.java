package com.nano.candy.interpreter;
import java.lang.reflect.Constructor;

public class InterpreterFactory {

	private static final String INTERPRETER_IMPLEMENTATION_CLASS_NAME = "InterpreterImpl" ;

	public static Interpreter newInterpreter(String type) {
		String packageName = Interpreter.class.getPackage().getName() + "." + type ;
		try {
			Class<?> interpreterImplClass = Class.forName(packageName + "." + INTERPRETER_IMPLEMENTATION_CLASS_NAME) ;
			Constructor<?> constructor = interpreterImplClass.getDeclaredConstructors()[0] ;
			if (!constructor.isAccessible()) {
				constructor.setAccessible(true) ;
			}
			return (Interpreter) constructor.newInstance() ;
		} catch (Exception e) {
			throw new InterpreterException("Unknown interpreter type: " + type);
		}
	}    
}
