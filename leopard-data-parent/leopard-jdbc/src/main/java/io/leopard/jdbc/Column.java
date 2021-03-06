package io.leopard.jdbc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {

	int length() default 0;

	/**
	 * 字段名称
	 * 
	 * @return
	 */
	String name() default "";

}
