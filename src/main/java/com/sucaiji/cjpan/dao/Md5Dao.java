package com.sucaiji.cjpan.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface Md5Dao {

    @Insert("INSERT INTO MD5(md5,uuid) VALUES(#{md5},#{uuid})")
    void insert(@Param("md5") String md5,@Param("uuid") String uuid);

    @Select("SELECT uuid FROM MD5 WHERE md5=#{md5}")
    List<String> selectByMd5(@Param("md5") String md5);

}
