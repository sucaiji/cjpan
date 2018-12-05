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
     * @param account
     * @param password
     * @param name
     */
    void regist(String account,String password,String name,String role);

    /**
     * 改密码
     * @param oldPassword
     * @param newPassword
     * @return 旧密码无误且新密码符合格式时返回true，反之返回false
     */
    boolean changePassword(String account, String oldPassword, String newPassword);

    /**
     * 判断用户表是否为空
     * @return 为空返回true
     */
    boolean isEmpty();


}
