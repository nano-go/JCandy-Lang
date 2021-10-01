package com.nano.candy.ast.dumper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Fields in the AST node which are annotated with this will be renamed
 * to the {@code value()}.
 *
 * <p> This is parsed by {@code SerializableDumper}.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldName {
	String value() default "" ;
}
