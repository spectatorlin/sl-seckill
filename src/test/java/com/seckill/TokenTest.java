package com.seckill;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.seckill.dto.LoginFormDTO;
import com.seckill.dto.Result;
import com.seckill.entity.User;
import com.seckill.service.IUserService;
import com.seckill.service.impl.UserServiceImpl;
import com.seckill.utils.RandomPhoneNumber;
import com.seckill.utils.RegexUtils;
import com.seckill.utils.SystemConstants;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import javax.annotation.Resource;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
class TokenTest {

    @Resource
    private IUserService userService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserServiceImpl userServiceI;
    @Test
    public void createUserBy1000(){
        List<String> phones = RandomPhoneNumber.randomCreatePhone(1000);
        phones.stream().forEach(phone -> {
            if(!RegexUtils.isPhoneInvalid(phone)){
                User login_user = new User();
                login_user.setPhone(phone);
                login_user.setCreateTime(LocalDateTime.now());
                login_user.setUpdateTime(LocalDateTime.now());
                String nickName_suf = RandomUtil.randomString(10);
                login_user.setNickName(SystemConstants.USER_NICK_NAME_PREFIX + nickName_suf);
                userServiceI.save(login_user);
            }
        });
    }

    @Test
    public void tokenBy1000() throws Exception {
        String phone = "";
        String code = "";
        //注意！这里的绝对路径设置为自己想要的地方
        OutputStreamWriter osw = null;
        osw = new OutputStreamWriter(new FileOutputStream("D:\\token.txt"));
        //先模拟10个用户的登录
        for (int i = 10; i < 1010; i++) {
            User user = userService.getById(i);
            phone = user.getPhone();
            //创建虚拟请求，模拟通过手机号，发送验证码
            ResultActions perform1 = mockMvc.perform(MockMvcRequestBuilders
                    .post("/user/code?phone=" + phone));
            //获得Response的body信息
            String resultJson1 = perform1.andReturn().getResponse().getContentAsString();
            //将结果转换为result对象
            Result result = JSONUtil.toBean(resultJson1, Result.class);
            //获得验证码
            code = result.getData().toString();
            //创建登录表单
            LoginFormDTO loginFormDTO = new LoginFormDTO();
            loginFormDTO.setCode(code);
            loginFormDTO.setPhone(phone);
            //将表单转换为json格式的字符串
            String loginFormDtoJson = JSONUtil.toJsonStr(loginFormDTO);
            //创建虚拟请求，模拟登录
            ResultActions perform2 = mockMvc.perform(MockMvcRequestBuilders.post("/user/login")
                    //设置contentType表示为json信息
                    .contentType(MediaType.APPLICATION_JSON)
                    //放入json对象
                    .content(loginFormDtoJson));
            String resultJson2 = perform2.andReturn().getResponse().getContentAsString();
            Result result2 = JSONUtil.toBean(resultJson2, Result.class);
            //获得token
            String token = result2.getData().toString();
            //写入
            osw.write(token+"\n");
        }
        //关闭输出流
        osw.close();
    }
}