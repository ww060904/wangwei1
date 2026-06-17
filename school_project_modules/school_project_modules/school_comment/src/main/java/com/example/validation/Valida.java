package com.example.validation;

import javax.validation.groups.Default;  //注意一定导入的是Validation下的Default类
public interface Valida {
    interface Login extends Default{} //登录校验组
    interface Create extends Default{} //新增校验组
    interface Update extends Default{} //修改校验组
    interface Delete extends Default{} //删除校验组
    interface Query extends Default{}  //查询校验组
}
