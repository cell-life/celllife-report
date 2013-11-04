package org.celllife.report.framework.logging;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * User: Kevin W. Sewell
 * Date: 2013-03-18
 * Time: 10h15
 */
@Aspect
@Component
public final class LoggingAspect {

    private ILoggerFactory loggerFactory = LoggerFactory.getILoggerFactory();

    @Before(value = "@annotation(loggable)", argNames = "joinPoint, loggable")
    public void before(JoinPoint joinPoint, Loggable loggable) {

        if (joinPoint == null) {
            return;
        }

        Object target = joinPoint.getTarget();
        if (target == null) {
            return;
        }

        Signature signature = joinPoint.getSignature();
        if (signature == null) {
            return;
        }
        String name = signature.getName();

        Object[] args = joinPoint.getArgs();

        Class clazz = target.getClass();

        LogLevel logLevel = loggable.value();

        String logMessage = buildLogMessage(name, args);

        log(clazz, logLevel, logMessage);
    }

    private void log(Class clazz, LogLevel logLevel, String logMessage) {
        Logger logger = loggerFactory.getLogger(clazz.getName());
        if (logLevel == LogLevel.DEBUG) {
            logger.debug(logMessage);
            return;
        }

        if (logLevel == LogLevel.ERROR) {
            logger.error(logMessage);
            return;
        }

        if (logLevel == LogLevel.INFO) {
            logger.info(logMessage);
            return;
        }

        if (logLevel == LogLevel.TRACE) {
            logger.trace(logMessage);
            return;
        }

        if (logLevel == LogLevel.WARN) {
            logger.warn(logMessage);
        }
    }

    @AfterThrowing(value = "@annotation(loggable)", argNames = "joinPoint, loggable, throwable", throwing = "throwable")
    public void afterThrowing(JoinPoint joinPoint, Loggable loggable, Throwable throwable) {

        if (joinPoint == null) {
            return;
        }

        Object target = joinPoint.getTarget();
        if (target == null) {
            return;
        }

        Class<? extends Throwable> throwableClass = throwable.getClass();

        if (Exception.class.isAssignableFrom(throwableClass)) {

            Class clazz = target.getClass();

            LogLevel logLevel = loggable.exception();

            String message = throwable.getMessage();

            log(clazz, logLevel, message, throwable);
        }
    }

    private void log(Class clazz, LogLevel logLevel, String logMessage, Throwable throwable) {
        Logger logger = loggerFactory.getLogger(clazz.getName());
        if (logLevel == LogLevel.DEBUG) {
            logger.debug(logMessage, throwable);
            return;
        }

        if (logLevel == LogLevel.ERROR) {
            logger.error(logMessage, throwable);
            return;
        }

        if (logLevel == LogLevel.INFO) {
            logger.info(logMessage, throwable);
            return;
        }

        if (logLevel == LogLevel.TRACE) {
            logger.trace(logMessage, throwable);
            return;
        }

        if (logLevel == LogLevel.WARN) {
            logger.warn(logMessage, throwable);
        }
    }

    private String buildLogMessage(String name, Object[] args) {

        StringBuilder stringBuilder = new StringBuilder(name);
        stringBuilder.append("(");

        if (args != null) {
            for (int i = 0; i < args.length; i++) {

                stringBuilder.append(args[i]);

                if (i != args.length - 1) {
                    stringBuilder.append(",");
                }
            }
        }

        stringBuilder.append(")");

        return stringBuilder.toString();
    }
}