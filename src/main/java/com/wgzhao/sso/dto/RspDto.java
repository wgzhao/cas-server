package com.wgzhao.sso.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RspDto
{
    private int code;
    private String msg;
    private Map<String, Object> data;
}
