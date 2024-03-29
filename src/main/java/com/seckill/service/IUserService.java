package com.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.seckill.dto.LoginFormDTO;
import com.seckill.dto.Result;
import com.seckill.entity.User;

import javax.servlet.http.HttpSession;

/**
 * <p>
 *  服务类
 * </p>
 */
public interface IUserService extends IService<User> {
    Result sendCode(String phone, HttpSession session);

    Result login(LoginFormDTO loginForm, HttpSession session);

    Result sign();

    Result signCount();

}
