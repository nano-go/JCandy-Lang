package com.nano.candy.interpreter.cni;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface NativeClass {
	// Built-in class name.
	String name();
	boolean isInheritable() default false;
}

