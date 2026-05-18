package com.wgzhao.sso.cas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.MessageDescriptor;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.ServicesManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;


public class FastJdbcAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler
{

    private static final List<MessageDescriptor> warningList = new ArrayList<>();

    private final JdbcTemplate jdbcTemplate;

    private final String sql;

    private final String passwordField;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public FastJdbcAuthenticationHandler(String name,
            ServicesManager servicesManager,
            PrincipalFactory principalFactory,
            Integer order,
            JdbcTemplate jdbcTemplate,
            String sql,
            String passwordField)
    {
        super(name, servicesManager, principalFactory, order);
        this.jdbcTemplate = jdbcTemplate;
        this.sql = sql;
        this.passwordField = passwordField;
    }

    @Override
    protected AuthenticationHandlerExecutionResult doAuthentication(Credential credential, Service service) throws Throwable
    {
        if (!(credential instanceof UsernamePasswordCredential usernamePasswordCredential)) {
            throw new FailedLoginException("Unsupported credential type");
        }

        String username = usernamePasswordCredential.getUsername();
        String rawPassword = usernamePasswordCredential.toPassword();
        if (username == null || username.isBlank()) {
            throw new AccountNotFoundException("Username is empty");
        }
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new FailedLoginException("Password is empty");
        }

        Map<String, Object> dbFields = jdbcTemplate.queryForMap(sql, username);

        if (!dbFields.containsKey(passwordField)) {
            throw new FailedLoginException("Password field not found in query result");
        }

        String dbPassword = String.valueOf(dbFields.get(passwordField));
        boolean matched = passwordEncoder.matches(rawPassword, dbPassword);
        if (!matched) {
            throw new FailedLoginException("Password does not match value on record.");
        }

        Principal principal = principalFactory.createPrincipal(username, new HashMap<>());
        return createHandlerResult(credential, principal, warningList);
    }

    @Override
    public boolean supports(Credential credential)
    {
        return credential instanceof UsernamePasswordCredential;
    }
}
