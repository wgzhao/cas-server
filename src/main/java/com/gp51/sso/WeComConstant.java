package com.gp51.sso;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class WeComConstant
{
    public final static String tokenUrl = "https://qyapi.weixin.qq.com/cgi-bin/gettoken";
    public final static String userInfoUrl = "https://qyapi.weixin.qq.com/cgi-bin/user/getuserinfo";

    public final static Map<String, Map<String, String>> corpIds = new HashMap<>();

    public final static Map<String, String> corpSecrets = new HashMap<>();

    static {
        corpSecrets.put("wwfbeaf1a16d7f77b4", "AxUVY2mWTeuwQNHjvdUaXViyvmACi2IzTUKTl9cWJsg");
        corpSecrets.put("wwd3d10c479c919b50", "cin6z0slckLb8WBuc4IRvj0ZNoZ5JDXIMnAt3CPM5qU");

        corpIds.put("entity2", Map.of(
                        "entityId", "entity2",
                        "appid", "wwfbeaf1a16d7f77b4",
                        "corpid", "wwfbeaf1a16d7f77b4",
                        "agentid", "1000087",
                        "state", "11019",
                        "name", "股掌柜证券2"
                )
        );
        corpIds.put("entity3", Map.of(
                "entityId", "entity3",
                "appid", "wwd3d10c479c919b50",
                "corpid", "wwd3d10c479c919b50",
                "agentid", "1000004",
                "state", "11019",
                "name", "股掌柜证券3"));
    }

}
