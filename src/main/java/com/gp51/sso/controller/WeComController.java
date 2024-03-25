package com.gp51.sso.controller;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.gp51.sso.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;

@RestController
@RequestMapping("/v1/wecom")
@CrossOrigin("*")
public class WeComController
{
    private static final Logger LOG = LoggerFactory.getLogger(WeComController.class);

    @Value("${wecom.token.url}")
    private String tokenUrl;

    @Value("${wecom.userinfo.url}")
    private String userInfoUrl;

    @Value("${wecom.corpid}")
    private String corpid;

    @Value("${wecom.corpsecret}")
    private String corpsecret;

    private static Date expireTime ;
    private static JSONObject token;

    @GetMapping(value="/getAccessToken", produces = "application/json", consumes = "application/json")
    public JSONObject getAccessToken() {
        long curTs = System.currentTimeMillis();
        if (expireTime != null && curTs < expireTime.getTime()) {
            // token is still valid
            LOG.info("token is still valid");
            return token;
        }

        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("corpid", corpid);
        paramMap.put("corpsecret", corpsecret);
        String result = HttpUtil.get(tokenUrl, paramMap);
        try {
            JSONObject jsonObject = JSONUtil.parseObj(result);
            if (jsonObject.getInt("errcode") != 0) {
                LOG.error("get access token failed: {}", jsonObject.getStr("errmsg"));
            } else {
                LOG.info("get access token success");
                token = jsonObject;
                expireTime = new Date(curTs + jsonObject.getInt("expires_in", 7200) * 1000 - 60);
                LOG.info("set expire time: {}", expireTime.toString());
                return token;
            }
        } catch (Exception e) {
            LOG.error(e.getMessage());
            return null;
        }
        return null;
    }

    @GetMapping(value="/getUserInfo", produces = "application/json", consumes = "application/json")
    public JSONObject getUserInfo(@RequestParam("code") String code, @RequestParam("access_token") String accessToken) {
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
            } else {
                jsonObject.set("password", PasswordUtil.getEncryptPassword());
                return jsonObject;
            }
        } catch (Exception e) {
            LOG.error(e.getMessage());
            return null;
        }
    }
}
