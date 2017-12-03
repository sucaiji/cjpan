package com.sucaiji.cjpan.provider;

import com.sucaiji.cjpan.entity.Index;
import org.apache.ibatis.jdbc.SQL;

import java.util.Map;

public class IndexProvider {
    public String insertIndex(Index index){
        return new SQL(){
            {
                INSERT_INTO("INDEXS");
                VALUES("uuid","#{uuid}");
                if(index.getType()!=null) {
                    VALUES("type", "#{type}");
                }
                if(index.getSuffix()!=null){
                    VALUES("suffix","#{suffix}");
                }
                VALUES("name","#{name}");
                VALUES("is_dir","#{wasDir}");
                VALUES("l_update","#{lastUpdate}");
                VALUES("size","#{size}");
                if (index.getParentUuid()!=null){
                    VALUES("parent_uuid","#{parentUuid}");
                }
            }
        }.toString();
    }
    public String selectIndex(Map<String,Object> index){
        return new SQL(){
            {
                SELECT("*");
                FROM("INDEXS");

                //uuid为最优先，如果以uuid搜索的话就忽略其他所有条件
                if(index.get("uuid")!=null){
                    WHERE("uuid=#{uuid}");
                } else{

                    if(index.get("parentUuid")!=null){
                        WHERE("parent_uuid=#{parentUuid}");
                    } else {
                        WHERE("parent_uuid is NULL");
                    }
                    if(index.get("name")!=null){
                        WHERE("name=#{name}");
                    }
                    if(index.get("type")!=null){
                        WHERE("type=#{type}");
                    }
                }



            }
        }.toString();
    }

    public String deleteIndex(Map<String,Object> index){
        return new SQL(){
            {
                DELETE_FROM("INDEXS");
                if(index.get("uuid")!=null){
                    WHERE("uuid=#{uuid}");
                }

            }
        }.toString();
    }
}
