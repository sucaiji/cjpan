package com.sucaiji.cjpan.dao;

import com.sucaiji.cjpan.model.UserModel;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserDao {
    @SelectProvider(type = com.sucaiji.cjpan.provider.UserProvider.class,method = "selectUser")
    List<UserModel> select(Map<String,Object> user);

    UserModel selectByAccount(@Param("account") String account);

    void insert(UserModel user);

    void update(UserModel user);

    void updatePassword(@Param("account") String account, @Param("password") String password);


}
