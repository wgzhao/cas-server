package com.gp51.sso.cas;

import org.apereo.cas.authentication.AcceptUsersAuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.MessageDescriptor;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WeComAuthenticationHandler extends AcceptUsersAuthenticationHandler
{
    @Value("${wecom.user.pseudo}")
    private String pseudo;

    private static final List<MessageDescriptor> warningList = new ArrayList<>();

    private ServicesManager servicesManager;
    public WeComAuthenticationHandler(String name, ServicesManager servicesManager, PrincipalFactory principalFactory, Integer order) {
        super(name);
    }

    @Override
    protected AuthenticationHandlerExecutionResult authenticateUsernamePasswordInternal(
            UsernamePasswordCredential credential,
            String originalPassword) throws Throwable {
        char[] magicPassword = pseudo.toCharArray();
        // todo 定义自己的验证方法
        String username = credential.getUsername();
        char[] password = credential.getPassword();
        if (Arrays.equals(password, magicPassword)) {
            return createHandlerResult(credential,
                    principalFactory.createPrincipal(username), warningList);
        } else {
            // invoke super auth method
            return super.authenticateUsernamePasswordInternal(credential, originalPassword);
        }
    }

}
