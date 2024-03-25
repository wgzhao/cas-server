package com.gp51.sso.controller;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.gp51.sso.PasswordUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.gp51.sso.WeComConstant.corpIds;
import static com.gp51.sso.WeComConstant.corpSecrets;
import static com.gp51.sso.WeComConstant.tokenUrl;
import static com.gp51.sso.WeComConstant.userInfoUrl;

@RestController
@RequestMapping("/v1/wecom")
@CrossOrigin("*")
public class WeComController
{
    private static final Logger LOG = LoggerFactory.getLogger(WeComController.class);

    private static Date expireTime;
    private static String accessToken;

    @GetMapping(value = "/callback")
    public void callback(HttpServletRequest request, HttpServletResponse response, @RequestParam("code") String code)
            throws IOException
    {
        String corpId = request.getParameter("appid");
        String corpSecret = corpSecrets.get(corpId);

        getAccessToken(corpId, corpSecret);
        String userId = getUserInfo(code);
        StringBuilder sb = new StringBuilder();
        String host = URI.create(request.getRequestURL().toString()).getHost();
        sb.append("https://").append(host).append(":8001/cas/login?");
        if (request.getParameter("service") != null) {
            sb.append("service=").append(request.getParameter("service")).append("&");
        }
        sb.append("username=").append(userId).append("&");
        sb.append("password=").append(PasswordUtil.getEncryptPassword());
        response.sendRedirect(sb.toString());
    }

    //    @GetMapping(value="/getAccessToken", produces = "application/json", consumes = "application/json")
    public void getAccessToken(String corpId, String corpSecret)
    {
        long curTs = System.currentTimeMillis();
        if (expireTime != null && curTs < expireTime.getTime()) {
            // token is still valid
            LOG.info("token is still valid");
        }
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("corpid", corpId);
        paramMap.put("corpsecret", corpSecret);
        String result = HttpUtil.get(tokenUrl, paramMap);
        try {
            JSONObject jsonObject = JSONUtil.parseObj(result);
            if (jsonObject.getInt("errcode") != 0) {
                LOG.error("get access token failed: {}", jsonObject.getStr("errmsg"));
            }
            else {
                LOG.info("get access token success");
                accessToken = jsonObject.getStr("access_token");
                expireTime = new Date(curTs + jsonObject.getInt("expires_in", 7200) * 1000 - 60);
                LOG.info("set expire time: {}", expireTime.toString());
            }
        }
        catch (Exception e) {
            LOG.error(e.getMessage());
        }
    }

    //    @GetMapping(value="/getUserInfo", produces = "application/json", consumes = "application/json")
    public String getUserInfo(String code)
    {
        LOG.info("access getUserInfo");
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("code", code);
        paramMap.put("access_token", accessToken);
        String result = HttpUtil.get(userInfoUrl, paramMap);
        try {
            JSONObject jsonObject = JSONUtil.parseObj(result);
            if (jsonObject.getInt("errcode") != 0) {
                LOG.error("get user info failed: " + jsonObject.getStr("errmsg"));
                return null;
            }
            else {
                String UserId = jsonObject.getStr("UserId");
                // TODO: detect the UserId if exists staff center
                return jsonObject.getStr("UserId");
            }
        }
        catch (Exception e) {
            LOG.error(e.getMessage());
            return null;
        }
    }

    @GetMapping(value = "/corpInfo", produces = "application/json", consumes = "application/json")
    public Map<String, Map<String, String>> getCorpInfo()
    {

        return corpIds;
    }

}
