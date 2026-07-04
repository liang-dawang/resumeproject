package org.example.newcarre.Config;

import lombok.extern.slf4j.Slf4j;
import org.example.newcarre.Utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {


    private final JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        System.out.println("[DEBUG] JWT Filter - Authorization Header: " + header); // ⭐ 1. 打印完整的 Header

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                boolean isValid = jwtUtils.verify(token);
                System.out.println("[DEBUG] JWT Filter - Token Verify Result: " + isValid); // ⭐ 2. 打印校验结果

                if (isValid) {
                    Long userId = jwtUtils.getUserId(token);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    System.out.println("[DEBUG] JWT Filter - 认证成功, UserId: " + userId); // ⭐ 3. 确认认证成功
                }
            } catch (Exception e) {
                System.err.println("[ERROR] JWT Filter - Token 解析异常: " + e.getMessage()); // ⭐ 4. 捕获并打印异常
                e.printStackTrace();
            }
        } else {
            System.out.println("[DEBUG] JWT Filter - 未找到合法的 Bearer Token"); // ⭐ 5. 没带 Token 的情况
        }

        filterChain.doFilter(request, response);
    }
}