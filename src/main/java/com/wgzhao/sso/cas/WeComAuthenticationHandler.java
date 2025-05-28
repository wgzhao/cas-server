package com.wgzhao.sso.cas;

import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.MessageDescriptor;
import org.apereo.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.ServicesManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WeComAuthenticationHandler
        extends AbstractPreAndPostProcessingAuthenticationHandler
{
    private static final List<MessageDescriptor> warningList = new ArrayList<>();

    public WeComAuthenticationHandler(String name, ServicesManager servicesManager, PrincipalFactory principalFactory, Integer order)
    {
        super(name, servicesManager, principalFactory, order);
    }

    @Override
    protected AuthenticationHandlerExecutionResult doAuthentication(Credential credential, Service service)
            throws Throwable
    {
        Principal principal = principalFactory.createPrincipal(credential.getId(), new HashMap<>());
        return createHandlerResult(credential, principal, warningList);
    }
}
