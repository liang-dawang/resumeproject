package org.example.newcarre.mapper;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserInfoMapperTest {

    @Autowired
    private UserInfoMapper userInfoMapper;
    @Test
    public void tsxt(){
        String a="admin";
        System.out.println(userInfoMapper.selectByUsername(a));
    }


}