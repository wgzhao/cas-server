package com.gp51.sso;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import cn.hutool.crypto.symmetric.SymmetricAlgorithm;
import cn.hutool.crypto.symmetric.SymmetricCrypto;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.UUID;

public  class PasswordUtil
{
    //随机生成密钥
//    public final static byte[] key = SecureUtil.generateKey(SymmetricAlgorithm.AES.getValue()).getEncoded();

    private final static byte[] key ="24063f35a511a2f3d088cd2fe2143981".getBytes();
    private final static byte[] salt = "DYgjCEIKaJa2W9xN".getBytes();
    //构建
//    private final static SymmetricCrypto aes = new SymmetricCrypto(SymmetricAlgorithm.AES, key);

    private static final AES aes = new AES("CBC", "PKCS7Padding", key, salt);

    private static final String passPrefix = "$1$2";

    public static String encryptPassword(String password)
    {
        return passPrefix + aes.encryptHex(password) + generateSuffix();
    }

    // generate random 3 hex chars
    public static String generateSuffix()
    {
        return UUID.randomUUID().toString().substring(0, 3);
    }

    public static String decryptPassword(String password)
    {
        if (password.startsWith(passPrefix))
        {
            password = password.substring(passPrefix.length(), 36);
        }
        try {
            return aes.decryptStr(password, CharsetUtil.CHARSET_UTF_8);
        } catch (Exception e) {
            return null;
        }

    }
}
