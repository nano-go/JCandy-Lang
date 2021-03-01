package com.nano.candy.interpreter.i2.builtin.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface BuiltinClass {
	// Built-in class name.
	String value() default "";
	boolean isInheritable() default false;
}

