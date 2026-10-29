package com.library.demo.aop;

import com.library.demo.entity.Borrowing;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.JoinPoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order (3)
public class BorrowingTimingAspect {

    private static final Logger log = LoggerFactory.getLogger(BorrowingTimingAspect.class);

    @Pointcut("execution(* com.library.demo.service.BorrowingServiceImpl.*(..))")
    private void anyBorrowingServiceMethod() {
    }

    @Pointcut("execution(* com.library.demo.service.BorrowingServiceImpl.find*(..))")
    private void findMethod() {
    }

    @Pointcut("anyBorrowingServiceMethod() && !findMethod()")
    private void borrowingAction() {
    }

    @Before( "execution(* com.library.demo.service.BorrowingServiceImpl.borrowItem(..))"
        + " && args(memberId, itemId, durationDays)")
    public void logBorrowRequest(int memberId, int itemId, int durationDays) {
        log.info("Borrow requested: member {}, item {}, {} days",
                memberId, itemId, durationDays);
    }

    @Around("borrowingAction()")
    public Object timeOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.nanoTime();

        try {
            return joinPoint.proceed();
        } finally {
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;
            log.info("{} took {} ms",
                    joinPoint.getSignature().getName(),
                    elapsedMs);
        }
    }

    @AfterReturning(pointcut = "execution(* com.library.demo.service.BorrowingServiceImpl.borrowItem(..))",
        returning = "borrowing")
    public void logCreatedBorrowing(Borrowing borrowing) {
        log.info("borrowItem returned borrowing ID {}", borrowing.getId());
    }

    @AfterThrowing(pointcut = "borrowingAction()", throwing = "error")
    public void logFailedAction(JoinPoint joinPoint, Throwable error) {
        log.warn("{} failed: {}",
                joinPoint.getSignature().getName(),
                error.getMessage());
    }

}