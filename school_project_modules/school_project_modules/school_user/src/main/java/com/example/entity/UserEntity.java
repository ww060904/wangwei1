package com.example.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.validation.Valida;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("sys_user")
public class UserEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    @NotNull(message = "用户名不能为空",groups = {Valida.Create.class})
    private String name;
    @NotNull(message = "电话号码不能为空",groups = Valida.Create.class)
    @Pattern(regexp = "^1(3\\d|4[5-9]|5[0-35-9]|6[567]|7[0-8]|8\\d|9[0-35-9])\\d{8}$", message = "手机号格式错误",groups = Valida.Create.class)
    private String phone;
    @NotBlank(message = "密码不能为空",groups ={Valida.Create.class} )
    @Size(min = 6 ,max = 10,message = "密码长度需要6-10位",groups ={Valida.Create.class} )
    //方式二
    //@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    private String remark;
    private String status;
//    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")//接收时候的格式化
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")  //返回前端格式化
    private LocalDateTime submitTime;
    private String email;

}
