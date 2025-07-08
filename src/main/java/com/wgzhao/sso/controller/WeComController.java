package com.wgzhao.sso.controller;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.wgzhao.sso.cas.WeComAuthenticationHandler;
import com.wgzhao.sso.dto.CorpInfo;
import com.wgzhao.sso.dto.RspDto;
import com.wgzhao.sso.service.WeComService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.DefaultAuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.AuthenticationResultBuilder;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/wecom")
@CrossOrigin("*")
@Slf4j
public class WeComController
{
    private static final HashMap<String, Map<String, Object>> accessTokens = new HashMap<>();

    @Autowired
    WeComService weComService;

    @Value("${cas.server.name}")
    private String casServerName;

    @Autowired
    private CentralAuthenticationService centralAuthenticationService;

    @Autowired
    private PrincipalFactory principalFactory;

    @Autowired
    private ServicesManager servicesManager;

    @Autowired
    private AuthenticationServiceSelectionPlan authenticationServiceSelectionPlan;

    @Autowired
    private WebApplicationServiceFactory webApplicationServiceFactory;

    @Autowired
    private AuthenticationSystemSupport authenticationSystemSupport;

    @Autowired
    private CasCookieBuilder ticketGrantingTicketCookieGenerator;

    @GetMapping(value = "/callback")
    public RspDto callback(HttpServletRequest request, HttpServletResponse response, @RequestParam("code") String code, @RequestParam("corpId") String corpId)
            throws IOException
    {
        RspDto result = new RspDto();
        String queryString = request.getQueryString();
        String serviceUrl = request.getParameter("service");

        log.info("queryString from request: = <{}>", queryString);
        log.info("callback service url: = <{}>", serviceUrl);
        String corpSecret = weComService.getSecret(corpId);
        setAccessToken(corpId, corpSecret);
        String userId = getUserInfo(corpId, code);
        if (userId == null) {
            result.setCode(404);
            result.setMsg("UserId not found, please try again later");
            return result;
        }
        String st;
        try {
            st = grantServiceTicketInternal(userId, serviceUrl, request, response);
            if (st == null) {
                // Handle error - e.g., redirect to an error page or CAS login page
                log.error("Failed to grant Service Ticket for user {} and service {}", userId, serviceUrl);
                result.setCode(701);
                result.setMsg("Failed to grant Service Ticket, please try again later.");
                return result;
            }

            // *** Append ST and Redirect ***
            // Use the original service URL, not the encoded one, as CAS handles encoding during validation
//            redirectUrl = serviceUrl + (serviceUrl.contains("?") ? "&" : "?") + "ticket=" + st;
            result.setCode(200);
            result.setMsg("Service Ticket granted successfully");
            result.setData(Map.of("ticket", st));
            return result;
        }
        catch (Throwable e) {
            result.setCode(500);
            result.setMsg("Internal error during ticket granting: " + e.getMessage());
            return result;
        }
    }
    /**
     * Grants a Service Ticket internally using CentralAuthenticationService.
     *
     * @param username The authenticated username (staffCode).
     * @param serviceUrl The target service URL.
     * @return The Service Ticket ID, or null if granting fails.
     * @throws Throwable For underlying exceptions during ticket creation/granting.
     */
    private String grantServiceTicketInternal(String username, String serviceUrl, HttpServletRequest request, HttpServletResponse response)
            throws Throwable
    {
        // 1. Create Principal for the authenticated user
        // Add attributes if needed, like the authentication type
        Map<String, List<Object>> principalAttributes = new HashMap<>();
        principalAttributes.put("authType", List.of("wecom")); // Indicate WeCom authentication
        // Add other attributes if your services expect them
        Principal principal = this.principalFactory.createPrincipal(username, principalAttributes);
        log.debug("Created principal for user: {}", username);

        // 2. Build Authentication object
        // We represent the successful authentication via WeCom.
        ZonedDateTime authDate = ZonedDateTime.now(); // Use an injected clock
        String handlerName = WeComAuthenticationHandler.class.getSimpleName(); // Name of your handler
        AuthenticationHandlerExecutionResult handlerResult = getAuthenticationHandlerExecutionResult(username, handlerName, principal);

        // Use the builder
        Authentication authentication = DefaultAuthenticationBuilder.newInstance()
                .setPrincipal(principal)
                .setAuthenticationDate(authDate)
                .addAttribute(AuthenticationHandler.SUCCESSFUL_AUTHENTICATION_HANDLERS, List.of(handlerName)) // Standard attribute
                .addSuccess(handlerName, handlerResult) // Link the handler success
                // Add other optional authentication attributes if needed
                // .addAttribute("someAuthnAttribute", "value")
                .build();
        log.debug("Built authentication object for principal: {}", principal.getId());

        // 3. Build AuthenticationResult (required for TGT/ST granting)
        final AuthenticationResultBuilder builder = authenticationSystemSupport.getAuthenticationResultBuilderFactory().newBuilder();
        builder.collect(authentication); // Feed the built Authentication
        AuthenticationResult authenticationResult = builder.build(authenticationSystemSupport.getPrincipalElectionStrategy());

        // 4. Create TGT
        TicketGrantingTicket tgt = centralAuthenticationService.createTicketGrantingTicket(authenticationResult);
        String tgtId = tgt.getId();
        ticketGrantingTicketCookieGenerator.addCookie(request, response, tgtId);
        log.info("Successfully created TGT internally: {}", tgtId);

        // 5. Create and Validate a Service object
        Service service = this.webApplicationServiceFactory.createService(serviceUrl);
        var registeredService = this.servicesManager.findServiceBy(service);
        if (registeredService == null || !registeredService.getAccessStrategy().isServiceAccessAllowed(registeredService, service)) { // Check access strategy with TGT context
            log.error("Service [{}] is not registered, not enabled, or access denied by strategy.", serviceUrl);
            throw new RuntimeException("Service not allowed: " + serviceUrl); // More specific error if possible
        }
        log.info("Target service [{}] found and allowed.", serviceUrl);

        // 6. Grant ST
        ServiceTicket st = centralAuthenticationService.grantServiceTicket(tgtId, service, authenticationResult);
        String stId = st.getId();
        log.info("Successfully granted ST internally: {}", stId);

        return stId;
    }

    private AuthenticationHandlerExecutionResult getAuthenticationHandlerExecutionResult(String username, String handlerName, Principal principal)
    {
        WeComAuthenticationHandler weComAuthenticationHandler = new WeComAuthenticationHandler(handlerName, servicesManager, principalFactory, 1);
        // Create a minimal placeholder credential.
        // This isn't used for validation here; it's just to satisfy the constructor.
        // Using UsernamePasswordCredential as a concrete, known type.
        Credential placeholderCredential = new UsernamePasswordCredential(username, "dummyPasswordFromWeComCallback");

        // Create the result using the placeholder credential
        // Assuming the constructor (String, Credential, Principal) exists.
        // If not, check available constructors in CAS 7.0.2 for DefaultAuthenticationHandlerExecutionResult.
        // Another common signature might involve warnings/errors lists.
        return new DefaultAuthenticationHandlerExecutionResult(weComAuthenticationHandler, placeholderCredential, principal);
    }

    private void setAccessToken(String corpId, String corpSecret)
    {
        long curTs = System.currentTimeMillis();
        Map<String, Object> tokenMap = accessTokens.getOrDefault(corpId, null);
        if (tokenMap != null) {
            long expireTime = (long) tokenMap.get("expireTime");
            if (curTs < expireTime) {
                // the token is still valid
                log.info("token is still valid");
                return;
            }
        }
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("corpid", corpId);
        paramMap.put("corpsecret", corpSecret);
        String result = HttpUtil.get(weComService.getTokenUrl(), paramMap);
        try {
            JSONObject jsonObject = JSONUtil.parseObj(result);
            if (jsonObject.getInt("errcode") != 0) {
                log.error("get access token failed: {}", jsonObject.getStr("errmsg"));
            }
            else {
                log.info("get access token success");

                String token = jsonObject.getStr("access_token");
                long expireTime = new Date(curTs + jsonObject.getInt("expires_in", 7200) * 1000 - 300 * 1000).getTime();
                // set token and expire time
                tokenMap = new HashMap<>();
                tokenMap.put("token", token);
                tokenMap.put("expireTime", expireTime);
                accessTokens.put(corpId, tokenMap);
                log.info("set expire time {} with corpId: {}", expireTime, corpId);
            }
        }
        catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private String getUserInfo(String corpId, String code)
    {
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("code", code);
        paramMap.put("access_token", accessTokens.get(corpId).get("token"));
        String result = HttpUtil.get(weComService.getUserInfoUrl(), paramMap);
        try {
            JSONObject jsonObject = JSONUtil.parseObj(result);
            if (jsonObject.getInt("errcode") != 0) {
                log.error("get user info failed: {}", jsonObject.getStr("errmsg"));
                return null;
            }
            else {
                return jsonObject.getStr("UserId");
            }
        }
        catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }

    @GetMapping(value = "/corpInfo", produces = "application/json", consumes = "application/json")
    public List<CorpInfo> getCorpInfo()
    {
        return weComService.getCorpInfo();
    }
}
