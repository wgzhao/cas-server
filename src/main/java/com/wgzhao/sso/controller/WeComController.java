package com.wgzhao.sso.controller;

import com.wgzhao.sso.dto.CorpInfo;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.wgzhao.sso.PasswordUtil;
import com.wgzhao.sso.service.WeComService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
    private static Date expireTime;
    private static String accessToken;

    @Value("${wecom.binding.url}")
    private String bindingUrl;

    @Autowired
    WeComService weComService;

    @Value("${cas.server.name}")
    private String casServerName;

    @GetMapping(value = "/callback")
    public void callback(HttpServletRequest request, HttpServletResponse response, @RequestParam("code") String code)
            throws IOException
    {
        String queryString = request.getQueryString();
        String redirectUrl;
        String corpId = request.getParameter("appid");
        String corpSecret = weComService.getSecret(corpId);
        setAccessToken(corpId, corpSecret);
        String userId = getUserInfo(code);
        if (userId == null) {
            log.error("get user info failed");
            return;
        }
        String staffCode = weComService.getStaffCode(userId, corpId);
        if (staffCode == null || staffCode.isEmpty()) {
            log.info("UserId({}) with corpId({}) not found ,prepare redirect to {}", userId, corpId, bindingUrl);
            redirectUrl = "%s?weChatCorpId=%s&weChatUserId=%s".formatted(bindingUrl, corpId, userId);
            response.sendRedirect(redirectUrl);
            return;
        }
        String service = request.getParameter("service");
        log.info("service: {}", service);
        String encodedService = encodeServiceUrl(service);
        log.info("encoded service: {}", encodedService);
        String st = getST(staffCode, encodedService);
        redirectUrl = encodedService + "&ticket=" + st;
        log.info("redirect url: {}", redirectUrl);
        response.sendRedirect(redirectUrl);

    }

    private void setAccessToken(String corpId, String corpSecret)
    {
        long curTs = System.currentTimeMillis();
        if (expireTime != null && curTs < expireTime.getTime()) {
            // token is still valid
            log.info("token is still valid");
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
                accessToken = jsonObject.getStr("access_token");
                expireTime = new Date(curTs + jsonObject.getInt("expires_in", 7200) * 1000 - 60);
                log.info("set expire time: {}", expireTime.toString());
            }
        }
        catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private String getUserInfo(String code)
    {
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("code", code);
        paramMap.put("access_token", accessToken);
        String result = HttpUtil.get(weComService.getUserInfoUrl(), paramMap);
        try {
            JSONObject jsonObject = JSONUtil.parseObj(result);
            if (jsonObject.getInt("errcode") != 0) {
                log.error("get user info failed: {}",  jsonObject.getStr("errmsg"));
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

    private String getST(String username, String service)
    {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("username", username);
        paramMap.put("password", PasswordUtil.getEncryptPassword());
        // step 1. auth and get TGT
        HttpResponse response =  HttpRequest.post(casServerName + "/cas/v1/tickets")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .form(paramMap)
                .execute();
        if (response.getStatus() != HttpStatus.HTTP_CREATED) {
            log.error("create tgt failed: {}", response.body());
            throw new RuntimeException("create tgt failed");
        }
        String[] locations = response.header("Location").split("/");
        String tgt = locations[locations.length - 1];
        // step 2. get ST
        response = HttpRequest.post(casServerName + "/cas/v1/tickets/" + tgt + "?service=" + service)
                .execute();
        if (response.getStatus() != HttpStatus.HTTP_OK) {
            log.error("get st failed: {}", response.body());
            throw new RuntimeException("get st failed");
        }
        return response.body();
    }

    private Map<String, String> getQueryMap(String query) {
        String[] params = query.split("&");
        Map<String, String> map = new HashMap<String, String>();

        for (String param : params) {
            String name = param.split("=")[0];
            String value = param.split("=")[1];
            map.put(name, value);
        }
        return map;
    }

    private String encodeServiceUrl(String s){
        URL url;
        try {
            url = new URL(s);
        } catch (Exception e) {
            log.error("invalid url: {}", s);
            return s;
        }
        String q = url.getQuery().split("redirect=")[1];

        StringBuilder sb = new StringBuilder();
        sb.append(url.getProtocol()).append("://").append(url.getHost());
        if (url.getPort() > 0) {
            sb.append(":").append(url.getPort());
        }
        sb.append(url.getPath());

        if (q.contains("?")) {
            sb.append("?redirect=").append(URLEncoder.encode(q, StandardCharsets.UTF_8));
        } else {
            if (q.contains("&")) {
                String[] split = q.split("&", 2);
                sb.append("?redirect=").append(URLEncoder.encode(split[0], StandardCharsets.UTF_8)).append("?").append(split[1]);
            } else {
                sb.append("?redirect=").append(URLEncoder.encode(q, StandardCharsets.UTF_8));
            }
        }

        return sb.toString();
    }
}
