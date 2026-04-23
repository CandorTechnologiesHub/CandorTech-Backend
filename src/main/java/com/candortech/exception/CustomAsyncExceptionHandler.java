package com.candortech.exception;

import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

import java.lang.reflect.Method;
import java.util.Arrays;

public class CustomAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(CustomAsyncExceptionHandler.class);

    @Override
    public void handleUncaughtException(@Nonnull Throwable ex, Method method, @Nonnull Object... params) {
        log.atError()
                .setMessage("Uncaught async exception — method: {}.{}(), params: {}")
                .addArgument(method.getDeclaringClass()::getSimpleName)
                .addArgument(method::getName)
                .addArgument(() -> Arrays.toString(params))
                .setCause(ex)
                .log();
    }
}
