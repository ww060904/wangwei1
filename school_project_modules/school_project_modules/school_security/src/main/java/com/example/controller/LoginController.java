package com.example.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.example.entity.LoginUserEntity;
import com.example.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/login")
@Api(tags="登录模块")
public class LoginController {
    @Autowired
    private UserService userService;

    /**
     * 登录逻辑
     * @param loginUserEntity
     * @return
     */
    @PostMapping("/in")
    @ApiOperation(value = "登录接口")
    public SaResult in(@RequestBody LoginUserEntity loginUserEntity){
        return userService.login(loginUserEntity);
    }

    @GetMapping("/out")
    public SaResult out(){
        StpUtil.logout();
        return SaResult.ok("退出成功");
    }
}
