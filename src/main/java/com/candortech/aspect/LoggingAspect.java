package com.candortech.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Around("execution(* com.candortech.service..*(..)) || execution(* com.candortech.controller..*(..))")
    public Object logAroundMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        String method = joinPoint.getSignature().toShortString();
        log.debug("Enter: {}", method);
        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        log.debug("Exit: {} in {} ms", method, System.currentTimeMillis() - startTime);
        return result;
    }
}