package com.library.demo.aop;

import java.util.List;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LibrarySearchAspect {

    private static final Logger log =
            LoggerFactory.getLogger(LibrarySearchAspect.class);

    @Pointcut("execution(* com.library.demo.service.LibraryItemServiceImpl.search(..))")
    private void itemSearch() {
    }

    @Before("itemSearch()")
    public void beforeSearch(JoinPoint joinPoint) {
        log.info("Calling service method: {}",
                joinPoint.getSignature().getName());
    }

    @AfterReturning(pointcut = "itemSearch()", returning = "results")
    public void afterSearch(List<?> results) {
        log.info("Library search returned {} item(s)", results.size());
    }

    @AfterThrowing(pointcut = "execution(* com.library.demo.service.LibraryItemServiceImpl.findById(..))",
        throwing = "error"
    )

    public void logFailedLookup(JoinPoint joinPoint, Throwable error) {
        log.warn("{} failed: {}",
                joinPoint.getSignature().getName(),
                error.getMessage());
    }

    @After("execution(* com.library.demo.service.LibraryItemServiceImpl.findById(..))")
    public void afterLookup() {
        log.info("findById finished");
    }

    @AfterThrowing(pointcut = "execution(* com.library.demo.service.LibraryItemServiceImpl.deleteById(..))",
        throwing = "error")
    public void logDeleteFailure(Throwable error) {
        log.warn("deleteById failed: {}", error.getMessage());
    }   
} 