package com.supplychainx.common.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.util.Arrays;


@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Pointcut("within(com.supplychainx.*.controller..*)")
    public void controllerMethods() {}

    @Pointcut("within(com.supplychainx.*.service..*)")
    public void serviceMethods() {}

    @Before("controllerMethods()")
    public void logBeforeController(JoinPoint joinPoint) {
        log.info(">>> Entering Controller: {}.{}() with arguments: {}", 
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(),
                Arrays.toString(joinPoint.getArgs()));
    }

    @AfterReturning(pointcut = "controllerMethods()", returning = "result")
    public void logAfterController(JoinPoint joinPoint, Object result) {
        log.info("<<< Exiting Controller: {}.{}() with result: {}", 
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(),
                result != null ? result.getClass().getSimpleName() : "null");
    }

    @AfterThrowing(pointcut = "controllerMethods() || serviceMethods()", throwing = "exception")
    public void logException(JoinPoint joinPoint, Exception exception) {
        log.error("!!! Exception in {}.{}(): {}", 
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(),
                exception.getMessage(),
                exception);
    }

    @Around("serviceMethods()")
    public Object logAroundService(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        log.debug(">>> Entering Service: {}.{}()", 
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName());
        
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            
            log.debug("<<< Exiting Service: {}.{}() - Execution time: {} ms", 
                    joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName(),
                    executionTime);
            
            return result;
        } catch (Exception ex) {
            log.error("!!! Exception in Service: {}.{}() - {}", 
                    joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName(),
                    ex.getMessage());
            throw ex;
        }
    }
}
