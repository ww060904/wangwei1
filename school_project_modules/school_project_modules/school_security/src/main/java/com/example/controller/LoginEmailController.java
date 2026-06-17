package com.example.controller;

import cn.dev33.satoken.util.SaResult;
import com.example.entity.LoginEmail;
import com.example.service.UserService;
import com.example.validation.Valida;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/email")
@Api(tags = "邮件验证码登录模块")
public class LoginEmailController {

    @Autowired
    private UserService userService;

    /**
     * 获取电话号码, 根据电话号码绑定的邮箱发送验证码
     * @param phone
     * @return
     */
    @ApiOperation(value = "发送邮件")
    @GetMapping("/sendEmail")
    public SaResult sendEmail(@RequestParam("phone") String phone) {
        return userService.sendEmail(phone);
    }

    /**
     * 邮箱登录接口
     * @param loginEmail
     * @return
     */
    @ApiOperation(value = "邮件登录接口")
    @PostMapping("/login")
    public SaResult login(@Validated(Valida.Create.class) @RequestBody LoginEmail loginEmail) {
        return userService.loginEmail(loginEmail);
    }
}