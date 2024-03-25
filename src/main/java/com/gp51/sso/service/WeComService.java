package com.gp51.sso.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

@Service
public class WeComService
{
    private static final Logger LOG = LoggerFactory.getLogger(WeComService.class);
    @Value("${cas.authn.jdbc.query[0].url}")
    private String jdbcUrl;

    @Value("${cas.authn.jdbc.query[0].user}")
    private String jdbcUser;

    @Value("${cas.authn.jdbc.query[0].password}")
    private String password;

    public String getStaffCode(String userId, String corpId)
    {
        String staffCode = null;
        // todo
        String sql = """
                select staff_code from staff_bind_we_chat
                where we_chat_user_id  = '%s' and we_chat_corp_id = '%s'
                """.formatted(userId, corpId);
        try(Connection connect = DriverManager.getConnection(jdbcUrl, jdbcUser, password)) {
            ResultSet resultSet = connect.createStatement().executeQuery(sql);
            if (resultSet.next()) {
                staffCode = resultSet.getString("staff_code");
                resultSet.close();
            } else {
                LOG.error("getStaffCode error: " + userId + " not found");
                staffCode = "";
            }
        }
        catch (SQLException e) {
            LOG.error("getStaffCode error: ", e);
        }
        return staffCode;
    }
}
