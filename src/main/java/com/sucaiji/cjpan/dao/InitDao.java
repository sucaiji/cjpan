package com.sucaiji.cjpan.dao;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface InitDao{
    @Update("CREATE TABLE IF NOT EXISTS USERS(" +
            "  id int PRIMARY KEY AUTO_INCREMENT," +
            "  count varchar(32)," +
            "  password varchar(32)," +
            "  name varchar(20)," +
            "  role varchar(10)" +
            ")")
    void createTableUsers();

    @Update("CREATE TABLE IF NOT EXISTS INDEXS (" +
            "  uuid CHAR(32) PRIMARY KEY," +
            "  parent_uuid CHAR(32) REFERENCES INDEXS (uuid)," +
            "  name VARCHAR (254) NOT NULL," +
            "  type VARCHAR(10)," +
            "  suffix VARCHAR(10)," +
            "  is_dir BOOLEAN NOT NULL," +
            "  l_update DATETIME NOT NULL," +
            "  size INT," +
            "  CONSTRAINT not_same UNIQUE (parent_uuid,name,is_dir)" +
            ")")
    void createTableIndexs();

    @Update("CREATE TABLE IF NOT EXISTS MD5(" +
            "  md5 CHAR(32)," +
            "  uuid CHAR(32) PRIMARY KEY," +
            "  CONSTRAINT fk FOREIGN KEY(uuid) REFERENCES INDEXS(uuid)" +
            ")")
    void createTableMd5();




}
