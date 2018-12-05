package com.sucaiji.cjpan.dao;

import com.sucaiji.cjpan.entity.Index;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

import static com.sucaiji.cjpan.config.Property.IS_DIR;
import static com.sucaiji.cjpan.config.Property.LAST_UPDATE;
import static com.sucaiji.cjpan.config.Property.PARENT_UUID;

@Mapper
public interface IndexDao {

    @InsertProvider(type=com.sucaiji.cjpan.provider.IndexProvider.class,method = "insertIndex")
    void insertIndex(Index index);

    @SelectProvider(type=com.sucaiji.cjpan.provider.IndexProvider.class,method = "selectIndex")
    @Results({
            @Result(column = "parent_uuid",property = PARENT_UUID),
            @Result(column="is_dir",property = IS_DIR),
            @Result(column = "l_update",property = LAST_UPDATE)
    })
    List<Index> selectIndex(Map<String,Object> index);

    @DeleteProvider(type = com.sucaiji.cjpan.provider.IndexProvider.class,method = "deleteIndex")
    void deleteIndex(Map<String,Object> map);

    @UpdateProvider(type = com.sucaiji.cjpan.provider.IndexProvider.class, method = "updateIndex")
    void updateIndex(Map<String,Object> map);
}
