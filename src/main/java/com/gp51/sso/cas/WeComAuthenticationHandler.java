package com.gp51.sso.cas;

import com.gp51.sso.PasswordUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.apereo.cas.authentication.AcceptUsersAuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.MessageDescriptor;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.security.auth.login.FailedLoginException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class WeComAuthenticationHandler extends AcceptUsersAuthenticationHandler
{
    private static final List<MessageDescriptor> warningList = new ArrayList<>();

    public WeComAuthenticationHandler(String name, ServicesManager servicesManager, PrincipalFactory principalFactory, Integer order) {
        super(name, servicesManager, principalFactory, order, null);
    }

    @Override
    protected AuthenticationHandlerExecutionResult authenticateUsernamePasswordInternal(
            UsernamePasswordCredential credential,
            String originalPassword) throws Throwable {

        HttpServletRequest requestObj = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        for (Map.Entry<String, String[]> obj : requestObj.getParameterMap().entrySet()) {
            System.out.println(obj);
        }
        // todo 定义自己的验证方法
        //$1$2cf88021c6d36b9a7d86373a3937cccc2444
        if (PasswordUtil.isWecomPassword(originalPassword) & PasswordUtil.isPseudoPassword(originalPassword)) {
            // decrypt password
            Map<String, List<Object>> attributes = new HashMap<>();
            attributes.put("authType", List.of("wecom"));
            Principal principal = principalFactory.createPrincipal(credential.getUsername(), attributes);
            return createHandlerResult(credential, principal, warningList);
        } else {
            // invoke super auth method
            return super.authenticateUsernamePasswordInternal(credential, originalPassword);
        }
    }

}
