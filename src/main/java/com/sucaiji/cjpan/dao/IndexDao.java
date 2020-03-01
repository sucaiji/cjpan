package com.sucaiji.cjpan.dao;

import com.sucaiji.cjpan.model.Index;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface IndexDao {

    void insertIndex(Index index);

    List<Index> selectIndex(Index index);

    void deleteIndex(Map<String,Object> map);

    void updateIndex(Index index);
}
