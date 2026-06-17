package com.example.service.impl;
import com.example.service.EmailAyncService;
import com.example.entity.LoginEmail;
import com.example.entity.UserEntity;
import cn.dev33.satoken.secure.SaSecureUtil;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.LoginUserEntity;
import lombok.extern.slf4j.Slf4j;
import com.example.mapper.UserMapper;
import com.example.service.UserService;
import com.example.utils.EmailCodeRedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

@Service
@Transactional
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements UserService{
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private EmailCodeRedisUtil emailCodeRedisUtil;
    @Autowired
    private EmailAyncService emailAsyncService;
    @Override
    public UserEntity getUserById(Long id) {
        UserEntity userEntity = userMapper.selectById(id);
//        //方式一
//        userEntity.setPassword(null);
        //for？
        return userEntity;
    }

    /**
     * 根据手机号查询用户ID
     * @param phone 手机号
     * @return 用户ID，如果不存在则返回null
     */
    @Override
    public Long selectByPhone(String phone) {
        // 参数校验
        if (StringUtils.isBlank(phone)) {
            log.warn("手机号为空");
            return null;
        }

        // 根据手机号查询用户
        LambdaQueryWrapper<UserEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserEntity::getPhone, phone);
        UserEntity userEntity = userMapper.selectOne(queryWrapper);

        // 返回用户ID，如果用户不存在则返回null
        return userEntity != null ? userEntity.getId() : null;
    }

    /**
     * 登录逻辑
     * @param loginUserEntity
     * @return
     */
    @Override
    public SaResult login(LoginUserEntity loginUserEntity) {
        //1.查询是否有这个电话号码
        LambdaQueryWrapper<UserEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserEntity::getPhone, loginUserEntity.getPhone());
        UserEntity userEntity = userMapper.selectOne(queryWrapper);
        if (userEntity == null){
            return SaResult.error("未查询到该用户");
        }
        if (userEntity.getStatus().equals("0")){
            return SaResult.error("该用户异常状态");
        }
        if (userEntity.getPassword().equals(loginUserEntity.getPassword())){
            StpUtil.login(loginUserEntity.getPhone());
            return SaResult.data(StpUtil.getTokenInfo());
        }
        return SaResult.error("密码不正确");
    }
    @Override
    public SaResult saveUser(UserEntity userEntity) {
        //1.参数校验完成后，进行下一步操作
        //2.检查电话号码时候有被注册过
        LambdaQueryWrapper<UserEntity> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(UserEntity::getPhone,userEntity.getPhone());
        UserEntity userEntity1 = userMapper.selectOne(queryWrapper);
        if(userEntity1!=null){
            return SaResult.error("该电话号码已被注册");
        }
        //3.没有被注册的情况下，走密码加密保存逻辑
        userEntity.setPassword(SaSecureUtil.md5(userEntity.getPassword()));
        //4.进行保存
        return userMapper.insert(userEntity)>0?SaResult.ok("保存成功"):SaResult.error("保存失败");
    }

    @Override
    public SaResult updateEntity(UserEntity userEntity) {
        int rows = userMapper.updateById(userEntity);
        return rows > 0 ? SaResult.ok("修改成功") : SaResult.error("修改失败");
    }

    @Override
    public SaResult deleteEntity(Long id) {
        int rows = userMapper.deleteById(id);
        return rows > 0 ? SaResult.ok("删除成功") : SaResult.error("删除失败");
    }

//    @Override
    public SaResult selectPage(Integer page, Integer size, Map<String, Object> params) {
        Page<UserEntity> page1 = new Page<>(page, size);
        String name = params.get("name") != null ? params.get("name").toString() : null;
        String startTime= params.get("startTime") != null ? params.get("startTime").toString() : null;
        String endTime= params.get("endTime") != null ? params.get("endTime").toString() : null;
        LambdaQueryWrapper<UserEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(name),
                        UserEntity::getName,
                        name)
                .between(StringUtils.isNotBlank(startTime)&&StringUtils.isNotBlank(endTime),UserEntity::getSubmitTime,startTime,endTime)
                .ge(StringUtils.isNotBlank(startTime),UserEntity::getSubmitTime,startTime)
                .le(StringUtils.isNotBlank(endTime),UserEntity::getSubmitTime,endTime);
        Page<UserEntity> userPage = userMapper.selectPage(page1, queryWrapper);
        return SaResult.data(userPage);
    }

    @Override
    public SaResult initializationPassword(Long id) {
        //1.先查询
        UserEntity userEntity = userMapper.selectById(id);
        if (userEntity == null) {
            return SaResult.error("未查询出要初始化的用户信息");
        }
        userEntity.setPassword(SaSecureUtil.md5("123456"));
        //2.再初始化
        return userMapper.updateById(userEntity)>0?SaResult.ok("初始化成功"):SaResult.error("初始化失败");
    }

    /**
     * 发送邮件验证码
     * @param phone
     * @return
     */
    @Override
    public SaResult sendEmail(String phone) {
        try {
            // 1. 查询用户
            LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserEntity::getPhone, phone);
            UserEntity user = userMapper.selectOne(wrapper);
            if (user == null) {
                return SaResult.error("没有该用户");
            }
            if (user.getEmail() == null || "".equals(user.getEmail())) {
                return SaResult.error("该用户未绑定邮箱");
            }
            String email = user.getEmail();
            // 2. 检查Redis中是否已有有效验证码
            if (emailCodeRedisUtil.hasValidCode(email)) {
                Long expireSeconds = emailCodeRedisUtil.getExpireTime(email);
                // 如果验证码还在有效期内（剩余时间>0），提示用户已发送
                if (expireSeconds > 0) {
                    return SaResult.ok("验证码已发送，请查收邮件。剩余有效期：" + (expireSeconds / 60) + "分钟");
                }
            }
            // 3. 检查是否正在发送中（占位符状态）
            if (emailCodeRedisUtil.isSendingPlaceholder(email)) {
                return SaResult.ok("邮件正在发送中，请稍后查看邮箱。若长时间未收到，请2分钟后重试");
            }
            // 4. 生成验证码
            String code = generateCode();
            // 5. 先创建占位符（防止重复提交）
            emailCodeRedisUtil.createSendingPlaceholder(email);
            // 6. 异步发送邮件（不等待结果）
            // 注意：这里先异步发送，邮件发送成功后会再保存到Redis
            // 这样设计的好处是：如果邮件发送失败，Redis中也不会有验证码，用户无法验证
            CompletableFuture<Boolean> future = emailAsyncService.sendVerificationCodeAsync(email, code);
            future.whenComplete((result, throwable) -> {
                if (throwable != null || !result) {
                    // 发送失败，删除占位符，允许用户重试
                    log.error("邮件发送失败，删除占位符：{}", email,throwable);
                    emailCodeRedisUtil.deleteEmailCode(email);
                }
            });
            // 6. 主线程立即返回成功
            return SaResult.ok("验证码发送请求已接收，请30秒后 查收邮件");
        } catch (Exception e) {
            e.printStackTrace();
            return SaResult.error("发送失败，请稍后重试");
        }
    }

    @Override
    public SaResult loginEmail(LoginEmail loginEmail) {
        //1.先判断是否有该用户
        //2.获取redis中的验证码
        //3.对比验证码是否正确
        //4.正确则删除redis中的验证码
        //5.登录
        LambdaQueryWrapper<UserEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserEntity::getPhone, loginEmail.getPhone());
        UserEntity user = userMapper.selectOne(queryWrapper);
        if(user==null){
            return SaResult.error("没有该账号");
        }
        String emailCode = emailCodeRedisUtil.getEmailCode(user.getEmail());
        if(StringUtils.isBlank(emailCode)){
            return SaResult.error("该用户未发生验证码");
        } else if (loginEmail.getCode().equals(emailCode)) {
            StpUtil.login(loginEmail.getPhone());
            emailCodeRedisUtil.deleteEmailCode(user.getEmail());
            return SaResult.data(StpUtil.getTokenInfo());
        }else{
            return SaResult.error("验证码不正确");
        }
    }
    /**
     * 生成6位数字验证码
     */
    private String generateCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

}