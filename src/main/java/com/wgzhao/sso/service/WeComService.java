package com.wgzhao.sso.service;

import com.wgzhao.sso.dto.CorpInfo;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


@Service
@Slf4j
public class WeComService
{
    @Getter
    private final String tokenUrl = "https://qyapi.weixin.qq.com/cgi-bin/gettoken";

    @Getter
    private final String userInfoUrl = "https://qyapi.weixin.qq.com/cgi-bin/user/getuserinfo";

    private static BasicDataSource dataSource = null;

    private final String profile;

    private final String encryptKey;

    @Autowired
    public WeComService(@Value("${cas.authn.jdbc.query[0].url}") String jdbcUrl,
            @Value("${cas.authn.jdbc.query[0].user}") String jdbcUser,
            @Value("${cas.authn.jdbc.query[0].password}") String password,
            @Value("${spring.profiles.include}") String activeProfile,
            @Value("${wecom.encrypt.key}") String encryptKey
    )
    {
        this.profile = activeProfile;
        this.encryptKey = encryptKey;

        dataSource = new BasicDataSource();
        dataSource.setUrl(jdbcUrl);
        dataSource.setUsername(jdbcUser);
        dataSource.setPassword(password);

        dataSource.setMinIdle(3);
        dataSource.setMaxIdle(5);
        dataSource.setMaxTotal(8);
    }

    public List<CorpInfo> getCorpInfo()
    {
        List<CorpInfo> result = new ArrayList<>();
        try (Connection connection = dataSource.getConnection()) {
            String sql = String.format(
                    "select * from cas_wecom_appliers u where u.profile= '%s' ", profile);
            ResultSet resultSet = connection.createStatement().executeQuery(sql);
            while (resultSet.next()) {
                result.add(new CorpInfo(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getString("agentid"),
                        resultSet.getString("appid"),
                        resultSet.getString("corpid"),
                        resultSet.getString("state")
                ));
            }
        }
        catch (SQLException e) {
            log.error("getCorpInfo error: ", e);
        }
        return result;
    }

    public String getSecret(String corpId)
    {
        try (Connection connection = dataSource.getConnection()) {
            String sql = String.format(
                    "select aes_decrypt(secret, '%s') as secret from cas_wecom_appliers where corpid = '%s' and profile = '%s'",
                    encryptKey, corpId, profile);
            ResultSet resultSet = connection.createStatement().executeQuery(sql);
            if (resultSet.next()) {
                return resultSet.getString("secret");
            }
        }
        catch (SQLException e) {
            log.error("getSecret error: ", e);
        }
        return null;
    }

    public String getStaffCode(String userId, String corpId)
    {
        String staffCode;
        String sql = """
                select staff_code from staff_bind_we_chat
                where we_chat_user_id  = '%s' and we_chat_corp_id = '%s'
                """.formatted(userId, corpId);

        try (Connection connection = dataSource.getConnection()) {
            ResultSet resultSet = connection.createStatement().executeQuery(sql);
            if (resultSet.next()) {
                staffCode = resultSet.getString("staff_code");
                resultSet.close();
            }
            else {
                staffCode = "";
            }
        }
        catch (SQLException e) {
            throw new RuntimeException("getStaffCode error: ", e);
        }
        return staffCode;
    }
}