package com.supplychainx.common.aspect;

import com.supplychainx.common.annotation.Audited;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;


@Slf4j
@Aspect
@Component
public class AuditAspect {

    @Pointcut("@annotation(com.supplychainx.common.annotation.Audited)")
    public void auditedMethods() {}

    @AfterReturning(pointcut = "auditedMethods()", returning = "result")
    public void audit(JoinPoint joinPoint, Object result) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Audited audited = signature.getMethod().getAnnotation(Audited.class);
        
        String action = audited.action();
        String entityType = audited.entityType();
        
        log.info("[AUDIT] Action: {}, Entity: {}, Method: {}", 
                action, entityType, signature.getName());
        

    }
}
