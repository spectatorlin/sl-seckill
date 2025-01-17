package com.seckill.utils;

import com.seckill.dto.UserDTO;

public class UserThreadLocal {
    private static final ThreadLocal<UserDTO> tl = new ThreadLocal<>();

    public static void setUser(UserDTO user){
        tl.set(user);
    }

    public static UserDTO getUser(){
        return tl.get();
    }

    public static void removeUser(){
        tl.remove();
    }
}
