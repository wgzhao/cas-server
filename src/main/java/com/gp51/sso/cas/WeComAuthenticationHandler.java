package com.gp51.sso.cas;

import com.gp51.sso.PasswordUtil;
import org.apereo.cas.authentication.AcceptUsersAuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.MessageDescriptor;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;
import org.springframework.beans.factory.annotation.Value;

import javax.security.auth.login.FailedLoginException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class WeComAuthenticationHandler extends AcceptUsersAuthenticationHandler
{
    private static final List<MessageDescriptor> warningList = new ArrayList<>();

    private ServicesManager servicesManager;
    public WeComAuthenticationHandler(String name, ServicesManager servicesManager, PrincipalFactory principalFactory, Integer order) {
        super(name);
    }

    @Override
    protected AuthenticationHandlerExecutionResult authenticateUsernamePasswordInternal(
            UsernamePasswordCredential credential,
            String originalPassword) throws Throwable {
        // todo 定义自己的验证方法
        String username = credential.getUsername();
        if (PasswordUtil.isWecomPassword(originalPassword)) {
            // decrypt password
            //$1$2cf88021c6d36b9a7d86373a3937cccc2444
            if (PasswordUtil.isPseudoPassword(originalPassword)) {
                return createHandlerResult(credential,
                        principalFactory.createPrincipal(username), warningList);
            } else {
                throw new FailedLoginException("Sorry, you are a failure!");
            }
        } else {
            // invoke super auth method
            return super.authenticateUsernamePasswordInternal(credential, originalPassword);
        }
    }

}
