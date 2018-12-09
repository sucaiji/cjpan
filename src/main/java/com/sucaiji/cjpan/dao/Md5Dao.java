package com.sucaiji.cjpan.dao;

import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface Md5Dao {

    void insert(@Param("md5") String md5,@Param("uuid") String uuid);

    List<String> selectUuidByMd5(@Param("md5") String md5);

    String selectMd5ByUuid(@Param("uuid")String uuid);

    void deleteByUuid(@Param("uuid")String uuid);


}
