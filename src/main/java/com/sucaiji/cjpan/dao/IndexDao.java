package com.sucaiji.cjpan.dao;

import com.sucaiji.cjpan.model.IndexModel;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface IndexDao {

    IndexModel get(String uuid);

    void insertIndex(IndexModel index);

    List<IndexModel> selectIndex(IndexModel index);

    List<IndexModel> fuzzySelectIndex(String name);

    /**
     *
     * @param uuid
     */
    void deleteByUuid(String uuid);

    void updateIndex(IndexModel index);
}
