package com.gp51.sso;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import cn.hutool.crypto.symmetric.SymmetricAlgorithm;
import cn.hutool.crypto.symmetric.SymmetricCrypto;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

public class PasswordUtil
{
    private final static byte[] key ="24063f35a511a2f3d088cd2fe2143981".getBytes();
    private final static byte[] salt = "DYgjCEIKaJa2W9xN".getBytes();

    private final static String pseudo = "37617d22";

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
            password = password.substring(passPrefix.length(), password.length() - 3);
        }
        try {
            return aes.decryptStr(password, CharsetUtil.CHARSET_UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isWecomPassword(String password) {
        if (StringUtils.isBlank(password)) {
            return false;
        }
        return password.startsWith(passPrefix);
    }

    public static String getEncryptPassword() {
        return encryptPassword(pseudo);
    }

    public static boolean isPseudoPassword(String password) {
        if (StringUtils.isBlank(password)) {
            return false;
        }
        return password.startsWith(passPrefix) && Objects.equals(decryptPassword(password), pseudo);
    }
}