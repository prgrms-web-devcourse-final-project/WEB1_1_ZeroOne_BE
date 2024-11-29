package com.palettee.global.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.lang.reflect.Proxy;
import java.sql.Connection;

@Aspect
@Component
@Slf4j
public class PerformanceAspect {

    private final ThreadLocal<QueryCounter> queryCounter = new ThreadLocal<>();

    @Pointcut("execution(* javax.sql.DataSource.getConnection(..))")
    public void performancePointcut() {
    }

    @Around("performancePointcut()")
    public Object start(ProceedingJoinPoint joinPoint) throws Throwable {
        final Connection connection = (Connection) joinPoint.proceed();
        queryCounter.set(new QueryCounter());
        final QueryCounter counter = this.queryCounter.get();

        final Connection proxyConnection = getProxyConnection(connection, counter);
        queryCounter.remove();
        return proxyConnection;
    }

    private Connection getProxyConnection(Connection connection, QueryCounter counter) {
        return (Connection) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{Connection.class},
                new ConnectionHandler(connection, counter)
        );
    }
}
