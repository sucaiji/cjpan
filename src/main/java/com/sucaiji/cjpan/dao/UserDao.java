package com.sucaiji.cjpan.dao;

import com.sucaiji.cjpan.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserDao {
    @SelectProvider(type = com.sucaiji.cjpan.provider.UserProvider.class,method = "selectUser")
    List<User> select(Map<String,Object> user);

    @Insert("INSERT INTO USERS(count,password,name,role) VALUES(#{count},#{password},#{name},#{role})")
    void insert(User user);


}
