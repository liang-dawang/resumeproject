package org.example.newcarre.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {


    /**
     * 登录成功后返回给前端的响应数据
     */



        // JWT 令牌（最重要）
        private String token;

        // 用户ID
        private Long userId;

        // 用户名
        private String username;

        // 角色（可选）
        private int role;

    public LoginResponseDTO(String token) {
        this.token = token;
    }

    public LoginResponseDTO(String token, int role) {
        this.token=token;
        this.role=role;
    }

    public LoginResponseDTO(String token, int role, Long userId) {
        this.token=token;
        this.role=role;
        this.userId=userId;
    }
}
