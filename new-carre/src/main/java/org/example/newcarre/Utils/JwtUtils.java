package org.example.newcarre.Utils;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;


@Component
public class JwtUtils {

    public static final String secret = "8s9Fk2pX7zQaL5gB0nD6vH1jC4mR7tU9";
    public static final Long expire = 1000 * 60 * 60 * 24L;

    // 生成token（存 userId、username 等）
    public String createToken(Long userId, String username) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", userId);
        payload.put("username", username);
        // 过期时间
        payload.put("exp", System.currentTimeMillis() + expire);

        return JWTUtil.createToken(payload,secret.getBytes());
    }

    // 解析token，获取userId
    public Long getUserId(String token) {
        JWT jwt = JWTUtil.parseToken(token);
        jwt.setKey(secret.getBytes()); // 验签
        return Long.parseLong(jwt.getPayload("userId").toString());
    }

    // 验证token是否有效（签名+过期）
    public boolean verify(String token) {
        try {
            JWT jwt = JWTUtil.parseToken(token);
            jwt.setKey(secret.getBytes());
            jwt.verify(); // 验签+验时间
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
