package com.sucaiji.cjpan.dao;

import com.sucaiji.cjpan.model.Index;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface IndexDao {

    @InsertProvider(type=com.sucaiji.cjpan.provider.IndexProvider.class,method = "insertIndex")
    void insertIndex(Index index);

    List<Index> selectIndex(Map<String,Object> index);

    @DeleteProvider(type = com.sucaiji.cjpan.provider.IndexProvider.class,method = "deleteIndex")
    void deleteIndex(Map<String,Object> map);

    @UpdateProvider(type = com.sucaiji.cjpan.provider.IndexProvider.class, method = "updateIndex")
    void updateIndex(Map<String,Object> map);
}
