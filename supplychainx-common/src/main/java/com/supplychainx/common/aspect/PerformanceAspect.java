package com.supplychainx.common.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class PerformanceAspect {

    private static final long SLOW_METHOD_THRESHOLD_MS = 1000;

    @Pointcut("within(com.supplychainx.*.service..*)")
    public void serviceMethods() {}

    @Pointcut("within(com.supplychainx.*.repository..*)")
    public void repositoryMethods() {}

    @Around("serviceMethods() || repositoryMethods()")
    public Object monitorPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        try {
            return joinPoint.proceed();
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;
            
            if (executionTime > SLOW_METHOD_THRESHOLD_MS) {
                log.warn("[PERFORMANCE] Slow method detected: {}.{}() took {} ms", 
                        joinPoint.getSignature().getDeclaringTypeName(),
                        joinPoint.getSignature().getName(),
                        executionTime);
            } else {
                log.trace("[PERFORMANCE] {}.{}() executed in {} ms", 
                        joinPoint.getSignature().getDeclaringTypeName(),
                        joinPoint.getSignature().getName(),
                        executionTime);
            }
        }
    }
}
