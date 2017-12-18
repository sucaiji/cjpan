package com.sucaiji.cjpan.service.impl;

import com.sucaiji.cjpan.dao.UserDao;
import com.sucaiji.cjpan.entity.User;
import com.sucaiji.cjpan.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserDao userDao;

    @Override
    public boolean login(String count, String password) {
        Map<String,Object> user=new HashMap<>();
        user.put("count",count);
        List<User> list=userDao.select(user);
        if(list.size()==0){
            return false;
        }
        boolean wrongPassword=!list.get(0).getPassword().equals(password);
        if (wrongPassword){
            return false;
        }
        return true;
    }

    @Override
    public void regist(String count, String password, String name, String role) {
        User user=new User(count,password,name,role);
        userDao.insert(user);
    }

    @Override
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
