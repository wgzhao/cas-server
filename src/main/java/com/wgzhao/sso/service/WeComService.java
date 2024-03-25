package com.wgzhao.sso.service;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static cn.hutool.core.util.ClassLoaderUtil.getClassLoader;

@Service
public class WeComService
{
    private final String tokenUrl = "https://qyapi.weixin.qq.com/cgi-bin/gettoken";
    private final String userInfoUrl = "https://qyapi.weixin.qq.com/cgi-bin/user/getuserinfo";

    private final Map<String, Map<String, String>> corpIds = new HashMap<>();

    private final Map<String, String> corpSecrets = new HashMap<>();

    private final Connection connection;

    @Autowired
    public WeComService(@Value("${cas.authn.jdbc.query[0].url}") String jdbcUrl,
                        @Value("${cas.authn.jdbc.query[0].user}") String jdbcUser,
                        @Value("${cas.authn.jdbc.query[0].password}") String password) {
        try {
            this.connection = DriverManager.getConnection(jdbcUrl, jdbcUser, password);
        }
        catch (SQLException e) {
            throw new RuntimeException("init WeComService error: ", e);
        }
        // load json file
        try {
            Object data = Objects.requireNonNull(getClassLoader().getResource("wecom.json")).getContent();
            JSONObject object = JSONUtil.parseObj(data);

            for (Map.Entry<String, Object> entry : object.getJSONObject("secrets")) {
                corpSecrets.put(entry.getKey(), entry.getValue().toString());
            }
            for (Map.Entry<String, Object> entry : object.getJSONObject("entity")) {
                corpIds.put(entry.getKey(), (Map<String, String>) entry.getValue());
            }
        }
        catch (Exception e) {
            throw new RuntimeException("parse wecom.json error: ", e);
        }
    }

    public Map<String, Map<String, String>> getCorpInfo()
    {
        return corpIds;
    }

    public String getSecret(String corpId) {
        return corpSecrets.getOrDefault(corpId, null);
    }

    public String getTokenUrl()  {
        return tokenUrl;
    }

    public String getUserInfoUrl() {
        return userInfoUrl;
    }

    public String getStaffCode(String userId, String corpId)
    {
        String staffCode = null;
        // todo
        String sql = """
                select staff_code from staff_bind_we_chat
                where we_chat_user_id  = '%s' and we_chat_corp_id = '%s'
                """.formatted(userId, corpId);
        try {
            ResultSet resultSet = connection.createStatement().executeQuery(sql);
            if (resultSet.next()) {
                staffCode = resultSet.getString("staff_code");
                resultSet.close();
            } else {
                staffCode = "";
            }
        }
        catch (SQLException e) {
            throw new RuntimeException("getStaffCode error: ", e);
        }
        return staffCode;
    }
}
