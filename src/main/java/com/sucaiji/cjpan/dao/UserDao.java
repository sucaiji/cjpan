package com.sucaiji.cjpan.dao;

import com.sucaiji.cjpan.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserDao {
    @SelectProvider(type = com.sucaiji.cjpan.provider.UserProvider.class,method = "selectUser")
    List<User> select(Map<String,Object> user);

    @Select("SELECT * FROM USERS WHERE account = #{account}")
    User selectByAccount(@Param("account") String account);

    @Insert("INSERT INTO USERS(account,password,name,role) VALUES(#{account},#{password},#{name},#{role})")
    void insert(User user);

    @Update("UPDATE USERS" +
            "  SET password=#{password}," +
            "      name=#{name}," +
            "      role=#{role}" +
            "  WHERE id=#{id}")
    void update(User user);

    @Update("UPDATE USERS SET password=#{password} WHERE account=#{account}")
    void updatePassword(@Param("account") String account, @Param("password") String password);


}
