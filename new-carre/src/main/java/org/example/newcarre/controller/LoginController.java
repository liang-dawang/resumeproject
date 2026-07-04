package org.example.newcarre.controller;


import org.example.newcarre.Utils.JwtUtils;
import org.example.newcarre.Utils.Result;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.example.newcarre.dto.LoginResponseDTO;
import org.example.newcarre.service.userLoginService;
import org.example.newcarre.dto.LoginRequestDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController

@RequestMapping("/api")
public class LoginController {

    @Resource
    private userLoginService userloginswrvice;
    @Resource
    private  JwtUtils jwtUtils;





    @PostMapping("/login")
    public Result<LoginResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO request) {
        // 只拿 username、password，干净、安全
        try {
            String token = userloginswrvice.login(request.getUsername(), request.getPassword());
            int role=userloginswrvice.selsctrole(request.getUsername());
            Long userId=userloginswrvice.selsctuserId(request.getUsername());
            System.out.println("JWT token"+token+"-----"+"role"+role);
            return Result.ok(new LoginResponseDTO(token,role,userId));
        } catch (IllegalArgumentException e) {
            return Result.fail(401, e.getMessage());
        }
    }

    @PostMapping("/login/register")
    public Result<String> register(@Valid @RequestBody LoginRequestDTO request) {
        // 你的注册逻辑
        userloginswrvice.register(request);

        // 只返回成功信息
        return Result.ok("注册成功");
    }
}
