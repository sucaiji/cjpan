package com.sucaiji.cjpan.dao;

import com.sucaiji.cjpan.model.Index;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface IndexDao {

    @InsertProvider(type=com.sucaiji.cjpan.provider.IndexProvider.class,method = "insertIndex")
    void insertIndex(Index index);

    List<Index> selectIndex(Index index);

    @DeleteProvider(type = com.sucaiji.cjpan.provider.IndexProvider.class,method = "deleteIndex")
    void deleteIndex(Map<String,Object> map);

    void updateIndex(Index index);
}
