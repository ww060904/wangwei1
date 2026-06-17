package com.example.config;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.util.SaResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 统一异常拦截处理类
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    // 全局异常拦截（拦截项目中的NotLoginException异常）
    @ExceptionHandler(NotLoginException.class)
    public SaResult handlerNotLoginException(NotLoginException nle)
            throws Exception {

        // 打印堆栈，以供调试
        nle.printStackTrace();

        // 判断场景值，定制化异常信息
        String message = "";
        if(nle.getType().equals(NotLoginException.NOT_TOKEN)) {
            message = "未能读取到有效 token";
        }
        else if(nle.getType().equals(NotLoginException.INVALID_TOKEN)) {
            message = "token 无效";
        }
        else if(nle.getType().equals(NotLoginException.TOKEN_TIMEOUT)) {
            message = "token 已过期";
        }
        else if(nle.getType().equals(NotLoginException.BE_REPLACED)) {
            message = "token 已被顶下线";
        }
        else if(nle.getType().equals(NotLoginException.KICK_OUT)) {
            message = "token 已被踢下线";
        }
        else if(nle.getType().equals(NotLoginException.TOKEN_FREEZE)) {
            message = "token 已被冻结";
        }
        else if(nle.getType().equals(NotLoginException.NO_PREFIX)) {
            message = "未按照指定前缀提交 token";
        }
        else {
            message = "当前会话未登录";
        }

        // 返回给前端
        return SaResult.error(message);
    }
    @ExceptionHandler(NullPointerException.class)
    public SaResult handlerNullPointerException(NullPointerException nle){
        return SaResult.error(nle.getMessage());
    }


    //拦截Valid参数校验异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public SaResult handleValidException(MethodArgumentNotValidException e) {
        String errorMsg=e.getBindingResult().getFieldError().getDefaultMessage();
        return SaResult.error(errorMsg);
    }
    /// 根据项目扩展
}
