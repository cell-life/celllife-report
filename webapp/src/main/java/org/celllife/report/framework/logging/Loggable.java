package org.celllife.report.framework.logging;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * User: Kevin W. Sewell
 * Date: 2013-03-18
 * Time: 10h15
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Loggable {

    LogLevel value();

    LogLevel exception() default LogLevel.NONE;

}