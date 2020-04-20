package com.sucaiji.cjpan.dao;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

/**
 * 初始化表相关的sql
 */
@Mapper
public interface InitDao{

    void createTableUsers();

    void createTableIndexs();
}
