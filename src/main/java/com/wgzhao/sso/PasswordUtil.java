package com.wgzhao.sso;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

@Slf4j
public class PasswordUtil
{
    private static final String passPrefix = "$1$2";

    // map each digital to its alphabet
    private static final Map<String, String> d2a = Map.of(
            "0", "c", "1", "p", "2", "m", "3", "E", "4", "j",
            "5", "A", "6", "n", "7", "_", "8", "x", "9", "z");
    // alphabetize to digital
    private static final Map<String, String> a2d = Map.of(
            "c", "0", "p", "1", "m", "2", "E", "3", "j", "4",
            "A", "5", "n", "6", "_", "7", "x", "8", "z", "9");
    private static final int MAX_PASSWORD_AGE = 5 * 60 * 1000;

    public static String encryptPassword()
    {
        long l = System.currentTimeMillis() * 3;
        log.info("current ts=" + l);
        // convert the l to string and reverse it
        String s = new StringBuilder(String.valueOf(l)).reverse().toString();
        StringBuilder sb = new StringBuilder();
        sb.append(passPrefix);
        for (int i = 0; i < s.length(); i++) {
            sb.append(d2a.get(s.charAt(i) + ""));
        }
        return sb.toString();
    }

    public static String decryptPassword(String password)
    {
        StringBuilder k = new StringBuilder();
        //skip passPrefix
        for (int i = 4; i < password.length(); i++) {
            k.append(a2d.get(password.charAt(i) + ""));
        }
        log.info("decrypt password=" + k.toString());
        return k.reverse().toString();
    }

    public static boolean isWecomPassword(String password)
    {
        if (StringUtils.isBlank(password)) {
            return false;
        }
        return password.startsWith(passPrefix);
    }

    public static String getEncryptPassword()
    {
        return encryptPassword();
    }

    public static boolean isPseudoPassword(String password)
    {
        return isWecomPassword(password) &
                System.currentTimeMillis() - Long.parseLong(decryptPassword(password)) / 3 < MAX_PASSWORD_AGE;
    }

    public static void main(String[] args)
    {
        String enc = encryptPassword();
        System.out.printf("enc=%s\n", enc);
        String dec = decryptPassword(enc);
        System.out.printf("dec=%s\n", dec);
        System.out.println(isPseudoPassword(enc));
    }
}