package com.sucaiji.cjpan.dao;

import com.sucaiji.cjpan.model.Index;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface IndexDao {

    Index get(String uuid);

    void insertIndex(Index index);

    List<Index> selectIndex(Index index);

    List<Index> fuzzySelectIndex(String name);

    /**
     *
     * @param uuid
     */
    void deleteByUuid(String uuid);

    void updateIndex(Index index);
}
