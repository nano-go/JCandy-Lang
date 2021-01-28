package com.nano.candy.interpreter.i1.builtin.func.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BuiltinMethod {
	// Built-in method name, if empty means initializer.
    String value() default "" ;
	
	// Built-in method argument count.
	int argc() default 0;
}
