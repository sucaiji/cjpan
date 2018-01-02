package com.sucaiji.cjpan.provider;

import com.sucaiji.cjpan.entity.Index;
import org.apache.ibatis.jdbc.SQL;

import java.util.Map;

import static com.sucaiji.cjpan.config.Property.*;

public class IndexProvider {
    public String insertIndex(Index index){
        String str= new SQL(){
            {
                INSERT_INTO("INDEXS");
                VALUES("uuid","#{"+UUID+"}");
                if(index.getType()!=null) {
                    VALUES("type", "#{"+TYPE+"}");
                }
                if(index.getSuffix()!=null){
                    VALUES("suffix","#{"+SUFFIX+"}");
                }
                VALUES("name","#{"+NAME+"}");
                VALUES("is_dir","#{"+IS_DIR+"}");
                VALUES("l_update","#{"+LAST_UPDATE+"}");
                VALUES("size","#{size}");
                if (index.getParentUuid()!=null){
                    VALUES("parent_uuid","#{"+PARENT_UUID+"}");
                }
            }
        }.toString();
        System.out.println(str);
        return str;
    }
    public String selectIndex(Map<String,Object> index){
        return new SQL(){
            {
                SELECT("*");
                FROM("INDEXS");

                //uuid为最优先，如果以uuid搜索的话就忽略其他所有条件
                if(index.get(UUID)!=null){
                    WHERE("uuid=#{"+UUID+"}");
                } else{

                    if(index.get(PARENT_UUID)!=null){
                        WHERE("parent_uuid=#{"+PARENT_UUID+"}");
                    } else {
                        WHERE("parent_uuid is NULL");
                    }
                    if(index.get("name")!=null){
                        WHERE("name=#{"+NAME+"}");
                    }
                    if(index.get("type")!=null){
                        WHERE("type=#{"+TYPE+"}");
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
                    WHERE("uuid=#{"+UUID+"}");
                }

            }
        }.toString();
    }
}
