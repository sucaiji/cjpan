package com.sucaiji.cjpan.dao;

import com.sucaiji.cjpan.entity.Index;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface IndexDao {

    @InsertProvider(type=com.sucaiji.cjpan.provider.IndexProvider.class,method = "insertIndex")
    void insertIndex(Index index);

    @SelectProvider(type=com.sucaiji.cjpan.provider.IndexProvider.class,method = "selectIndex")
    @Results({
            @Result(column = "parent_uuid",property = "parentUuid"),
            @Result(column="is_dir",property = "wasDir"),
            @Result(column = "l_update",property = "lastUpdate")
    })
    List<Index> selectIndex(Map<String,Object> index);

    @DeleteProvider(type = com.sucaiji.cjpan.provider.IndexProvider.class,method = "deleteIndex")
    void deleteIndex(Map<String,Object> map);
}
