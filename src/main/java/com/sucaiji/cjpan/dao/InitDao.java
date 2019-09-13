package com.sucaiji.cjpan.dao;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface InitDao{

    void createTableUsers();

    void createTableIndexs();
}
