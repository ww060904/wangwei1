package com.example.service;

import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.LoginEmail;
import com.example.entity.LoginUserEntity;
import com.example.entity.UserEntity;
import org.springframework.stereotype.Service;

import java.util.Map;

public interface UserService extends IService<UserEntity> {
    UserEntity getUserById(Long id);

    SaResult login(LoginUserEntity loginUserEntity);

    SaResult saveUser(UserEntity userEntity);
    SaResult updateEntity(UserEntity userEntity);
    SaResult deleteEntity(Long id);
    SaResult selectPage(Integer page, Integer size, Map<String, Object> params);
    SaResult initializationPassword(Long id);
    SaResult sendEmail(String phone);
    SaResult loginEmail(LoginEmail loginEmail);
    Long selectByPhone(String phone);
}