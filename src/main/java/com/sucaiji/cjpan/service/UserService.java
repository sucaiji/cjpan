package com.sucaiji.cjpan.service;



public interface UserService {
    /**
     * 检查账号密码是否正确
     * @param count
     * @param password
     * @return 登录成功则返回true
     */
    boolean login(String count,String password);

    /**
     * 注册
     * @param count
     * @param password
     * @param name
     */
    void regist(String count,String password,String name,String role);

    /**
     * 判断用户表是否为空
     * @return 为空返回true
     */
    boolean isEmpty();


}
