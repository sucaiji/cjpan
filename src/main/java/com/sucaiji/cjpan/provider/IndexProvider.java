package com.sucaiji.cjpan.provider;

import com.sucaiji.cjpan.model.Index;
import org.apache.ibatis.jdbc.SQL;

import java.util.Map;

import static com.sucaiji.cjpan.config.Property.*;

public class IndexProvider {
    public String insertIndex(Index index) {
        return new SQL() {
            {
                INSERT_INTO("INDEXS");
                VALUES("uuid", "#{uuid}");
                if (index.getType() != null) {
                    VALUES("type", "#{type}");
                }
                if (index.getSuffix() != null) {
                    VALUES("suffix", "#{suffix}");
                }
                VALUES("name", "#{name}");
                VALUES("is_dir", "#{wasDir}");
                VALUES("l_update", "#{lastUpdate}");
                VALUES("size", "#{size}");
                if (index.getParentUuid() != null) {
                    VALUES("parent_uuid", "#{parentUuid}");
                }
            }
        }.toString();
    }

    public String selectIndex(Map<String, Object> index) {
        return new SQL() {
            {
                SELECT("*");
                FROM("INDEXS");


                if (index.get(UUID) != null) {
                    WHERE("uuid=#{uuid}");
                }
                if (index.get(PARENT_UUID) != null) {
                    WHERE("parent_uuid=#{parentUuid}");
                }
                if (index.get(NAME) != null) {
                    WHERE("name=#{name}");
                }
                if (index.get(TYPE) != null) {
                    WHERE("type=#{type}");
                }
            }
        }.toString();
    }

    public String deleteIndex(Map<String, Object> index) {
        return new SQL() {
            {
                DELETE_FROM("INDEXS");
                if (index.get("uuid") != null) {
                    WHERE("uuid=#{uuid}");
                }
            }
        }.toString();
    }

    public String updateIndex(Map<String, Object> index) {
        return new SQL() {
            {
                UPDATE("INDEXS");
                if (index.get("type") != null) {
                    SET("type=#{type}");
                }
                if (index.get("name") != null) {
                    SET("name=#{name}");
                }
                if (index.get("lastUpdate") != null) {
                    SET("l_update=#{lastUpdate}");
                }
                WHERE("uuid=#{uuid}");
            }
        }.toString();
    }

}
