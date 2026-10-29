package com.library.demo.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(2)
public class BorrowingTraceAspect {

    private static final Logger log =
            LoggerFactory.getLogger(BorrowingTraceAspect.class);

    @Around("execution(* com.library.demo.service.BorrowingServiceImpl.borrowItem(..))")
    public Object traceBorrowing(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("TRACE enter borrowItem");

        try {
            return joinPoint.proceed();
        } finally {
            log.info("TRACE exit borrowItem");
        }
    }
}
