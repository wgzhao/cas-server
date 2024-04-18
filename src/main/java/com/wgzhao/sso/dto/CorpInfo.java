package com.wgzhao.sso.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CorpInfo
{
    private int id;
    private String name;
    private String agentid;
    private String appid;
    private String corpid;
    private String state;
}
