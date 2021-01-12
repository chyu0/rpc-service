package com.cy.rpc.register.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;

import java.security.MessageDigest;

@Slf4j
public class Base64Utils {

    /**
     * zk digest进行加密处理
     * @param usernameAndPassword
     * @return
     */
    public static String getDigest(String usernameAndPassword) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA1").digest(usernameAndPassword.getBytes());
            Base64 base64 = new Base64();
            return base64.encodeToString(digest);
        }catch (Exception e){
            log.error("加密失败, key : {}", usernameAndPassword);
            return null;
        }
    }


    public static void main(String[] args){
        System.out.println(getDigest("test:123456"));
    }
}
