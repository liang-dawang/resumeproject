package org.example.newcarre.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.example.newcarre.dto.CareerData;
import org.example.newcarre.dto.UserInfo;

@Mapper
public interface UserInfoMapper extends BaseMapper<UserInfo> {
    @Select("SELECT * from user_info where user_name = #{username}")
    UserInfo selectByUsername(String username) ;


    @Select("SELECT user_id FROM user_info WHERE user_name = #{username} LIMIT 1")
    Long getUserIdByUsername(String username);

    @Select("select role from user_info WHERE user_name = #{username}")
    int selectRoleByUserName(String username);


}
