package com.nano.candy.interpreter.i2.builtin.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BuiltinMethod {
	// Built-in method name.
	String name() default "";
	int argc() default 0;
}

