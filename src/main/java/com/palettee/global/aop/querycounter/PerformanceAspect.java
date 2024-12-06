package com.palettee.global.aop.querycounter;

import java.lang.reflect.*;
import java.sql.*;
import lombok.extern.slf4j.*;
import org.aspectj.lang.*;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.*;

@Aspect
@Component
@Slf4j
public class PerformanceAspect {

    private final ThreadLocal<QueryCounter> queryCounter = new ThreadLocal<>();

    @Pointcut("execution(* javax.sql.DataSource.getConnection(..))")
    public void performancePointcut() {
    }

    @Pointcut("@within(org.springframework.stereotype.Service)")
    public void serviceExecutionPointcut() {
    }

//    @Around("performancePointcut()")
    public Object start(ProceedingJoinPoint joinPoint) throws Throwable {
        final Connection connection = (Connection) joinPoint.proceed();
        queryCounter.set(new QueryCounter());
        final QueryCounter counter = this.queryCounter.get();

        final Connection proxyConnection = getProxyConnection(connection, counter);
        queryCounter.remove();
        return proxyConnection;
    }

//    @Around("serviceExecutionPointcut()")
    public Object aroundServiceExecution(ProceedingJoinPoint joinPoint) throws Throwable {

        long startTime = System.currentTimeMillis();

        var joinPointSignature = joinPoint.getSignature();
        String serviceName = joinPointSignature.getDeclaringType().getSimpleName();
        String methodName = joinPointSignature.getName();
        String pointcutDesignator = serviceName + "#" + methodName;

        try {
            return joinPoint.proceed();
        } finally {
            long endTime = System.currentTimeMillis();
            log.debug("[{}] Executed in [{}] ms", pointcutDesignator, endTime - startTime);
        }
    }

    private Connection getProxyConnection(Connection connection, QueryCounter counter) {
        return (Connection) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{Connection.class},
                new ConnectionHandler(connection, counter)
        );
    }
}
