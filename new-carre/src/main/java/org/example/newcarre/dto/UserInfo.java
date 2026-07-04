package org.example.newcarre.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_info")
public class UserInfo {

    private Long userId;

    @TableField("user_name")  // 绑定数据库列
    private String userName;

    @TableField("pass_word")  // 绑定密码列
    private String passWord;

    private Integer role;
    private String email;
    private String phone;
    private Integer gender;
    private Integer age;
    private Integer status;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}