package com.dayz.sapientiacloud_edupivot.auth.aspect;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.Objects;

@Aspect
@Component
@Slf4j
public class WebLogAspect {

    @Pointcut("@within(org.springframework.web.bind.annotation.RestController)")
    public void restControllerLogPointcut() {
    }

    @Around("restControllerLogPointcut()")
    public Object doAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

        long startTime = System.currentTimeMillis();
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        String requestUrl = "N/A";
        String requestMethod = "N/A";
        String remoteAddr = "N/A";

        if (Objects.nonNull(attributes)) {
            HttpServletRequest request = attributes.getRequest();
            requestUrl = request.getRequestURL().toString();
            requestMethod = request.getMethod();
            remoteAddr = request.getRemoteAddr();
        }

        String methodName = proceedingJoinPoint.getSignature().getDeclaringTypeName() + "." + proceedingJoinPoint.getSignature().getName();

        if (log.isDebugEnabled()) {
            log.debug("[AOP REQ_IN_DETAIL] URL: {}, Method: {}, IP: {}, ClassMethod: {}, Args: {}",
                    requestUrl,
                    requestMethod,
                    remoteAddr,
                    methodName,
                    Arrays.toString(proceedingJoinPoint.getArgs()));
        } else {
            log.info("[AOP REQ_IN] URL: {}, Method: {}, ClassMethod: {}",
                    requestUrl,
                    requestMethod,
                    methodName);
        }

        Object result;
        try {
            result = proceedingJoinPoint.proceed();
        } catch (Throwable e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[AOP ERROR] Method: {}, Duration: {}ms, Exception: {}",
                    methodName,
                    duration,
                    e.getMessage(),
                    e);
            throw e;
        }

        long duration = System.currentTimeMillis() - startTime;

        if (log.isDebugEnabled()) {
            log.debug("[AOP REQ_OUT_DETAIL] Method: {}, Duration: {}ms, Response: {}",
                    methodName,
                    duration,
                    result);
        } else {
            log.info("[AOP REQ_OUT] Method: {}, Duration: {}ms, ResponseType: {}",
                    methodName,
                    duration,
                    (result != null ? result.getClass().getSimpleName() : "null"));
        }

        return result;
    }
}