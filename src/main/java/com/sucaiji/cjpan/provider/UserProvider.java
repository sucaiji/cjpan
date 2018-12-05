package com.sucaiji.cjpan.provider;

import com.sucaiji.cjpan.entity.User;
import org.apache.ibatis.jdbc.SQL;

import java.util.List;
import java.util.Map;

public class UserProvider  {
    public String selectUser(Map<String,Object> user){
        return new SQL(){
            {
                SELECT("*");
                FROM("USERS");
                if(user.get("id")!=null){
                    WHERE("id=#{id}");
                } else {
                    if(user.get("account")!=null){
                        WHERE("account=#{account}");
                    }
                    if(user.get("password")!=null){
                        WHERE("password=#{password}");
                    }
                }
            }
        }.toString();
    }

}
