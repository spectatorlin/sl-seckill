package com.seckill.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.seckill.dto.LoginFormDTO;
import com.seckill.dto.Result;
import com.seckill.dto.UserDTO;
import com.seckill.entity.User;
import com.seckill.mapper.UserMapper;
import com.seckill.service.IUserService;
import com.seckill.utils.RegexUtils;
import com.seckill.utils.SystemConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.seckill.utils.RedisConstants.*;


/**
 * 服务实现类
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * @Author 邹松林
     * @Description 发送验证码
     * @param phone
     * @param session
     * @Return com.seckill.dto.Result
     * @Date 15:30 2023/10/14
     */
    @Override
    public Result sendCode(String phone, HttpSession session) {
        //1.检查参数
        if (!RegexUtils.isCodeInvalid(phone)){
            return Result.fail("手机号格式错误，请重新输入");
        }
        System.out.println("aaaaaaaaa"+session.getAttribute("code"));
        //2.生成验证码
        String code = RandomUtil.randomNumbers(6);
//        //3.保存验证码到session
//        session.setAttribute("code",code);
        //3.保存验证码到redis
        stringRedisTemplate.opsForValue().set(LOGIN_CODE_KEY + phone, code, LOGIN_CODE_TTL, TimeUnit.MINUTES);

        //4.发送验证码
        log.info("验证码：{}",code);
        return Result.ok(code);
    }

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        // 1.校验手机号
        String phone = loginForm.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)) {
            // 2.如果不符合，返回错误信息
            return Result.fail("手机号格式错误！");
        }
//        //3.从session获取验证码并校验
//        Object cacheCode = session.getAttribute("code");
//        String code = loginForm.getCode();
//        if (cacheCode == null || !cacheCode.toString().equals(code)){
//            return Result.fail("验证码错误");
//        }
        // 3.从redis获取验证码并校验
        String cacheCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone);
        String code = loginForm.getCode();
        if (cacheCode == null || !cacheCode.equals(code)) {
            // 不一致，报错
            return Result.fail("验证码错误");
        }

        // 4.一致，根据手机号查询用户 select * from tb_user where phone = ?
        User user = query().eq("phone", phone).one();

        // 5.判断用户是否存在
        if (user == null) {
            // 6.不存在，创建新用户并保存
            user = createUserWithPhone(phone);
        }
        session.setAttribute("user",BeanUtil.copyProperties(user,UserDTO.class));

        // 7.保存用户信息到 redis中
        // 7.1.随机生成token，作为登录令牌
        String token = UUID.randomUUID().toString(true);
        // 7.2.将User对象转为HashMap存储
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString()));
        // 7.3.存储
        String tokenKey = LOGIN_USER_KEY + token;
        stringRedisTemplate.opsForHash().putAll(tokenKey, userMap);
        // 7.4.设置token有效期
        stringRedisTemplate.expire(tokenKey, LOGIN_USER_TTL, TimeUnit.MINUTES);

        // 8.返回token
        return Result.ok(token);
    }

    private User createUserWithPhone(String phone) {
        // 1.创建用户
        User user = new User();
        user.setPhone(phone);
        user.setNickName(SystemConstants.USER_NICK_NAME_PREFIX + RandomUtil.randomString(10));
        // 2.保存用户
        save(user);
        return user;
    }

    @Override
    public Result sign() {
        return null;
    }

    @Override
    public Result signCount() {
        return null;
    }
}
