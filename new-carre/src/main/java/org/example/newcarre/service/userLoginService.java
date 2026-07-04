package org.example.newcarre.service;

import jakarta.annotation.Resource;
import org.example.newcarre.Utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.example.newcarre.dto.CareerData;
import org.example.newcarre.dto.LoginRequestDTO;
import org.example.newcarre.dto.UserInfo;
import org.example.newcarre.mapper.UserInfoMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class userLoginService {

    private final AuthenticationManager authenticationManager;
    @Resource
    private final JwtUtils jwtUtils;
    @Resource
    private final UserInfoMapper userInfoMapper;
    private final PasswordEncoder passwordEncoder;


//    注册功能
    public  void register(LoginRequestDTO request) {
        UserInfo exist = userInfoMapper.selectByUsername(request.getUsername());
        if (exist != null) {
            throw new RuntimeException("用户名已存在");
        }
        // 2. 创建用户对象
        UserInfo user = new UserInfo();
        user.setUserName(request.getUsername());

        // 3. BCrypt 加密密码（关键）
        String encodePassword = passwordEncoder.encode(request.getPassword());
        user.setPassWord(encodePassword);
        user.setRole(Integer.valueOf("0"));
        user.setStatus(Integer.valueOf("1"));
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        // 4. 存入数据库
        userInfoMapper.insert(user);

    }

    /**
     * 登录：校验用户名密码 → 签发JWT
     */
    public String login(String username, String rawPassword) {

        // 1. 查询用户
        UserInfo user = userInfoMapper.selectByUsername(username);

        System.out.println("数据库密码：" + user.getPassWord());
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 2. 校验密码（关键！不会死循环）
        if (!passwordEncoder.matches(rawPassword, user.getPassWord())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 3. 生成 token
        return jwtUtils.createToken(user.getUserId(), username);
    }

    public int selsctrole(String username) {
        return userInfoMapper.selectRoleByUserName(username);

    }

    public Long selsctuserId(String username) {
        return userInfoMapper.getUserIdByUsername(username);

    }
}
