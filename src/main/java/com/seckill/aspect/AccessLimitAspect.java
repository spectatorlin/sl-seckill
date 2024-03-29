package com.seckill.aspect;

import com.seckill.annotation.AccessLimit;
import io.lettuce.core.internal.LettuceLists;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * @author 邹松林
 * @version 1.0
 * @Title: AccessLimitAspect
 * @Description: TODO
 * @date 2023/10/29 1:15
 */
@Slf4j
@Aspect
@Component
public class AccessLimitAspect {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private HttpServletResponse response;


    @Pointcut("@annotation(com.seckill.annotation.AccessLimit)")
    private void check() {

    }

    private DefaultRedisScript<Long> redisScript;

    @PostConstruct
    public void init() {
        redisScript = new DefaultRedisScript<>();
        redisScript.setResultType(Long.class);
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("limit.lua")));
    }


    @Around("check()")
    public Object process(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) proceedingJoinPoint.getSignature();
        AccessLimit accessLimit = signature.getMethod().getDeclaredAnnotation(AccessLimit.class);
        if (accessLimit==null){
            //无限流注解，则正常执行原来方法
            return proceedingJoinPoint.proceed();
        }
        //获取注解的内容
        int value = accessLimit.value();
        //设置key，用ip：拼接当前时间（秒）
        String key = "ip:" + System.currentTimeMillis() / 1000;
        List<String> keyList = LettuceLists.newList(key);
        //执行lua
        Long result = stringRedisTemplate.execute(redisScript, keyList, String.valueOf(value), "2");

        if (result==0){
            fullfack();
            return null;
        }
        return proceedingJoinPoint.proceed();

    }
    /*
     * 服务降级方法
     */
    private void fullfack() {
        response.setHeader("Content-type","text/html;charset=UTF-8");
        PrintWriter writer=null;
        try {
            writer= response.getWriter();
            writer.println("服务开小差了，请重试");
            writer.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (writer!=null){
                writer.close();
            }
        }
    }

}


