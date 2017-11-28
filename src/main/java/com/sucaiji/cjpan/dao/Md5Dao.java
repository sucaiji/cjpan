package com.sucaiji.cjpan.dao;

import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface Md5Dao {

    @Insert("INSERT INTO MD5(md5,uuid) VALUES(#{md5},#{uuid})")
    void insert(@Param("md5") String md5,@Param("uuid") String uuid);

    @Select("SELECT uuid FROM MD5 WHERE md5=#{md5}")
    List<String> selectUuidByMd5(@Param("md5") String md5);

    @Select("SELECT md5 FROM MD5 WHERE uuid=#{uuid}")
    String selectMd5ByUuid(@Param("uuid")String uuid);

    @Delete("DELETE FROM MD5 WHERE uuid=#{uuid}")
    void deleteByUuid(@Param("uuid")String uuid);


}
