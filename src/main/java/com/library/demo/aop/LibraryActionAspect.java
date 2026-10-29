package com.library.demo.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LibraryActionAspect {

    private static final Logger log =
            LoggerFactory.getLogger(LibraryActionAspect.class);

    @Before("@annotation(com.library.demo.aop.LogLibraryAction)")
    public void logAction(JoinPoint joinPoint) {
        log.info("Tracked action started: {}",
                joinPoint.getSignature().getName());
    }
}