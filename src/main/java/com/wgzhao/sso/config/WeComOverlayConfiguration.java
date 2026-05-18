package com.wgzhao.sso.config;

import com.wgzhao.sso.cas.FastJdbcAuthenticationHandler;
import com.wgzhao.sso.cas.WeComAuthenticationHandler;
import org.aopalliance.intercept.MethodInterceptor;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

@AutoConfiguration
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class WeComOverlayConfiguration
{

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Bean
    public AuthenticationHandler weComAuthenticationHandler()
    {
        return new WeComAuthenticationHandler(WeComAuthenticationHandler.class.getSimpleName(), servicesManager, new DefaultPrincipalFactory(), 1);
    }

    @Bean
    public AuthenticationEventExecutionPlanConfigurer weComAuthenticationEventExecutionPlanConfigurer(@Qualifier("weComAuthenticationHandler") final AuthenticationHandler weComAuthenticationHandler)
    {
        return plan -> plan.registerAuthenticationHandler(weComAuthenticationHandler);
    }
    @Bean
    @ConditionalOnProperty(name = {"cas.authn.jdbc.query[0].url", "cas.authn.jdbc.query[0].sql"})
    public AuthenticationHandler fastJdbcAuthenticationHandler(
        @Value("${cas.authn.jdbc.query[0].url:}") String jdbcUrl,
        @Value("${cas.authn.jdbc.query[0].user:}") String jdbcUser,
        @Value("${cas.authn.jdbc.query[0].password:}") String jdbcPassword,
        @Value("${cas.authn.jdbc.query[0].sql:}") String sql,
        @Value("${cas.authn.jdbc.query[0].field-password:user_pwd}") String fieldPassword)
    {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setUrl(jdbcUrl);
        dataSource.setUsername(jdbcUser);
        dataSource.setPassword(jdbcPassword);
        dataSource.setMinIdle(3);
        dataSource.setMaxIdle(8);
        dataSource.setMaxTotal(16);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return new FastJdbcAuthenticationHandler(FastJdbcAuthenticationHandler.class.getSimpleName(),
            servicesManager,
            new DefaultPrincipalFactory(),
            -100,
            jdbcTemplate,
            sql,
            fieldPassword);
    }

    @Bean
    @ConditionalOnBean(name = "fastJdbcAuthenticationHandler")
    public AuthenticationEventExecutionPlanConfigurer fastJdbcAuthenticationEventExecutionPlanConfigurer(
        @Qualifier("fastJdbcAuthenticationHandler") final AuthenticationHandler fastJdbcAuthenticationHandler)
    {
        return plan -> plan.registerAuthenticationHandler(fastJdbcAuthenticationHandler);
    }

    @Bean
    public static BeanPostProcessor authenticationHandlerTimingBeanPostProcessor()
    {
        return new BeanPostProcessor()
        {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName)
            {
                if (!(bean instanceof AuthenticationHandler)) {
                    return bean;
                }
                if (bean instanceof AuthenticationManager) {
                    return bean;
                }
                return createTimingProxy(bean, beanName);
            }
        };
    }

    private static Object createTimingProxy(Object bean, String beanName)
    {
        ProxyFactory proxyFactory = new ProxyFactory(bean);
        proxyFactory.setProxyTargetClass(true);
        proxyFactory.addAdvice((MethodInterceptor) invocation -> {
            if (!"authenticate".equals(invocation.getMethod().getName())) {
                return invocation.proceed();
            }
            return invocation.proceed();
        });
        return proxyFactory.getProxy();
    }
}
