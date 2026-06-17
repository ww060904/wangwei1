package com.example.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginUserEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    @ApiModelProperty(value = "账号",example="17723892703")
    private String phone;
    @ApiModelProperty(value = "密码",example="41220639150619245471206ca42b1535041fb22651d6c462")
    private String password;
}
