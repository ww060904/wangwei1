package com.example.controller;

import cn.dev33.satoken.util.SaResult;
import com.example.entity.UserEntity;
import com.example.service.UserService;
import com.example.service.UserService;
import com.example.validation.Valida;
import io.swagger.annotations.Api;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user")
@Api(tags="用户模块")
public class UserController {
    @Autowired
    private UserService userService;

    /**
     * 根据id查询用户信息
     *
     * @param id
     * @return
     */
    @GetMapping("/id")
    public SaResult getUserById(@RequestParam Long id) {
        return SaResult.data(userService.getById(id));
    }

    @PostMapping("/save")
    @ApiOperation("保存用户")
    public SaResult save(@Validated(Valida.Create.class) @RequestBody
                         UserEntity user) {
        return userService.saveUser(user);
    }

    /**
     * 修改用户信息
     */
    @PostMapping("/update")
    @ApiOperation("修改用户信息")
    public SaResult update(@Validated(Valida.Update.class) @RequestBody UserEntity userEntity) {
        return userService.updateEntity(userEntity);
    }

    /**
     * 删除用户信息
     */
    @GetMapping("/delete")
    @ApiOperation("删除用户")
    @ApiImplicitParam(name = "id", value = "用户ID", required = true, dataType = "Long")
    public SaResult delete(@Validated(Valida.Delete.class) @RequestParam("id") Long id) {
        return userService.deleteEntity(id);
    }

    /**
     * 用户分页查询
     *
     * @param page   页码
     * @param size   每页大小
     * @param params 查询参数（name、startTime、endTime等）
     * @return 分页结果
     */
    @ApiOperation(value = "分页查询")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "页码", defaultValue = "1", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "size", value = "每页大小", defaultValue = "10", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "name", value = "用户名", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "startTime", value = "开始时间", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "endTime", value = "结束时间", dataType = "string", paramType = "query")
    })
    @GetMapping("/page")
    public SaResult selectPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam Map<String, Object> params) {
        return userService.selectPage(page, size, params);
    }

    @GetMapping("initialization")
    @ApiOperation(value = "初始化密码")
    public SaResult initializationPassword(@RequestParam Long id) {
        return userService.initializationPassword(id);
    }

}
