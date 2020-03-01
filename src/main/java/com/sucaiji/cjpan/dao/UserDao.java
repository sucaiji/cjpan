package com.sucaiji.cjpan.dao;

import com.sucaiji.cjpan.model.User;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserDao {
    @SelectProvider(type = com.sucaiji.cjpan.provider.UserProvider.class,method = "selectUser")
    List<User> select(Map<String,Object> user);

    User selectByAccount(@Param("account") String account);

    void insert(User user);

    void update(User user);

    void updatePassword(@Param("account") String account, @Param("password") String password);


}
