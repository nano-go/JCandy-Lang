package com.nano.candy.interpreter.cni;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NativeMethod {
	public String name();
	public int argc() default 0;
	public int varArgsIndex() default -1;
}

