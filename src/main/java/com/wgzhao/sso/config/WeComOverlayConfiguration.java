package com.wgzhao.sso.config;

import com.wgzhao.sso.cas.WeComAuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

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
}

