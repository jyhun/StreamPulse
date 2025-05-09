package com.streampulse.backend.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class LogExecutionAspect {

    @Around("@annotation(com.streampulse.backend.aop.LogExecution)")
    public Object logExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();
        Object[] args = joinPoint.getArgs();

        try {
            Object result = joinPoint.proceed();
            log.info("[{}] {} 메서드 정상 종료. 반환값: {}", className, methodName, result);
            return result;
        } catch (Throwable e) {
            log.error("[{}] {} 메서드 실행 중 예외 발생. 인자: {} 에러: {}", className, methodName, Arrays.toString(args), e.getMessage(), e);
            throw e;
        }
    }
}
