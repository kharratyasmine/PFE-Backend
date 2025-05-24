package com.workpilot.auditing;

import com.workpilot.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;


import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditLogService auditLogService;

    @Pointcut("execution(* com.workpilot.service..*(..)) && !@within(com.workpilot.auditing.NoAuditLog)")
    public void serviceMethods() {}


    @AfterReturning(pointcut = "serviceMethods()", returning = "result")
    public void logServiceMethod(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        String username = Optional.ofNullable(
                SecurityContextHolder.getContext().getAuthentication()
        ).map(Authentication::getName).orElse("anonymous");

        String entity = extractEntityName(className);
        String action = extractActionFromMethod(methodName);

        String params = Arrays.stream(joinPoint.getArgs())
                .map(arg -> arg != null ? arg.toString() : "null")
                .collect(Collectors.joining(", "));

        auditLogService.log(username, action, entity, methodName, params);
    }

    private String extractActionFromMethod(String methodName) {
        if (methodName.startsWith("create")) return "CREATE";
        if (methodName.startsWith("update")) return "UPDATE";
        if (methodName.startsWith("delete")) return "DELETE";
        if (methodName.startsWith("get")) return "READ";
        return "ACTION";
    }

    private String extractEntityName(String className) {
        // Ex: ProjectServiceImpl → Project
        return className.replace("ServiceImpl", "");
    }


    @AfterThrowing(pointcut = "serviceMethods()", throwing = "ex")
    public void logExceptions(JoinPoint joinPoint, Throwable ex) {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        String username = Optional.ofNullable(
                SecurityContextHolder.getContext().getAuthentication()
        ).map(Authentication::getName).orElse("anonymous");

        String entity = extractEntityName(className);
        String action = "EXCEPTION";

        String params = Arrays.stream(joinPoint.getArgs())
                .map(arg -> arg != null ? arg.toString() : "null")
                .collect(Collectors.joining(", "));

        String errorMessage = ex.getClass().getSimpleName() + ": " + ex.getMessage();

        auditLogService.log(username, action, entity, methodName,
                "Params: " + params + "\nException: " + errorMessage);
    }

}

