package com.seckill.interceptor;


import com.seckill.utils.UserThreadLocal;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author 邹松林
 * @version 1.0
 * @Title: LoginInterceptor
 * @Description: 拦截请求
 * @date 2023/10/14 21:37
 */

public class LoginInterceptor implements HandlerInterceptor {


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1.判断ThreadLocal是否有用户
        if (UserThreadLocal.getUser()!=null){
            return true;
        }
        return false;
    }

}
