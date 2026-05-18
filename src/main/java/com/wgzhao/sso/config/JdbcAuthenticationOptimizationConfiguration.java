package com.wgzhao.sso.config;

import java.lang.reflect.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;


@AutoConfiguration
public class JdbcAuthenticationOptimizationConfiguration
{
    private static final Logger log = LoggerFactory.getLogger(JdbcAuthenticationOptimizationConfiguration.class);

    @Bean
    public static BeanPostProcessor jdbcPasswordEncoderOptimizationBeanPostProcessor()
    {
        return new BeanPostProcessor()
        {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName)
            {
                if (!isQueryDatabaseAuthenticationHandler(bean) && !isUsernamePasswordHandler(bean)) {
                    return bean;
                }

                PasswordEncoder current = resolvePasswordEncoder(bean);
                if (current == null) {
                    return bean;
                }

                String encoderType = current.getClass().getName();
                if (!encoderType.contains("BCryptPasswordEncoder")) {
                    return bean;
                }

                setPasswordEncoder(bean, new SkipPreEncodePasswordEncoder(current));
                log.warn("Applied JDBC auth optimization on bean={} encoder={}: skip pre-encode, keep matches", beanName, encoderType);
                return bean;
            }
        };
    }

    private static boolean isQueryDatabaseAuthenticationHandler(Object bean)
    {
        Class<?> type = bean.getClass();
        while (type != null && type != Object.class) {
            if ("org.apereo.cas.adaptors.jdbc.QueryDatabaseAuthenticationHandler".equals(type.getName())) {
                return true;
            }
            type = type.getSuperclass();
        }
        return false;
    }

    private static boolean isUsernamePasswordHandler(Object bean)
    {
        Class<?> type = bean.getClass();
        while (type != null && type != Object.class) {
            if ("org.apereo.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler".equals(type.getName())) {
                return true;
            }
            type = type.getSuperclass();
        }
        return false;
    }

    private static PasswordEncoder resolvePasswordEncoder(Object bean)
    {
        try {
            Method method = bean.getClass().getMethod("getPasswordEncoder");
            Object result = method.invoke(bean);
            return result instanceof PasswordEncoder ? (PasswordEncoder) result : null;
        } catch (Exception e) {
            log.warn("Unable to resolve password encoder from bean type={}", bean.getClass().getName(), e);
            return null;
        }
    }

    private static void setPasswordEncoder(Object bean, PasswordEncoder passwordEncoder)
    {
        try {
            Method method = bean.getClass().getMethod("setPasswordEncoder", PasswordEncoder.class);
            method.invoke(bean, passwordEncoder);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to set optimized password encoder on bean type=" + bean.getClass().getName(), e);
        }
    }

    private static class SkipPreEncodePasswordEncoder implements PasswordEncoder
    {
        private final PasswordEncoder delegate;

        SkipPreEncodePasswordEncoder(PasswordEncoder delegate)
        {
            this.delegate = delegate;
        }

        @Override
        public String encode(CharSequence rawPassword)
        {
            return rawPassword == null ? null : rawPassword.toString();
        }

        @Override
        public boolean matches(CharSequence rawPassword, String encodedPassword)
        {
            return delegate.matches(rawPassword, encodedPassword);
        }

        @Override
        public boolean upgradeEncoding(String encodedPassword)
        {
            return delegate.upgradeEncoding(encodedPassword);
        }
    }
}
