package com.sucaiji.cjpan.service;


import com.sucaiji.cjpan.dao.UserDao;
import com.sucaiji.cjpan.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    private UserDao userDao;


    /**
     * 检查账号密码是否正确
     *
     * @param account
     * @param password
     * @return 登录成功则返回true
     */
    public boolean login(String account, String password) {
        Map<String, Object> user = new HashMap<>();
        user.put("account", account);
        List<User> list = userDao.select(user);
        if (list.size() == 0) {
            return false;
        }
        boolean wrongPassword = !list.get(0).getPassword().equals(password);
        if (wrongPassword) {
            return false;
        }
        return true;
    }

    /**
     * 注册
     *
     * @param account
     * @param password
     * @param name
     */
    public void regist(String account, String password, String name, String role) {
        User user=new User(account,password,name,role);
        userDao.insert(user);
    }

    /**
     * 改密码
     *
     * @param oldPassword
     * @param newPassword
     * @return 旧密码无误且新密码符合格式时返回true，反之返回false
     */
    public boolean changePassword(String account, String oldPassword, String newPassword) {
        User user = userDao.selectByAccount(account);
        if (oldPassword.equals(user.getPassword())) {
            userDao.updatePassword(account, newPassword);
            return true;
        }
        return false;
    }

    /**
     * 判断用户表是否为空
     *
     * @return 为空返回true
     */
    public boolean isEmpty() {
        List<User> list=userDao.select(new HashMap<>());
        if (list==null){
            return true;
        }
        if(list.size()==0){
            return true;
        }

        return false;
    }


}
