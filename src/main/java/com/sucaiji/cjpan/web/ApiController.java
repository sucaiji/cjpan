package com.sucaiji.cjpan.web;


import com.sucaiji.cjpan.entity.Index;
import com.sucaiji.cjpan.service.IndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {
    @Autowired
    private IndexService indexService;

    /**
     * 相同文件已经存在，返回值告诉客户端秒传
     */
    public static final int MD5_EXIST=1;
    /**
     * 不存在相同文件
     */
    public static final int MD5_NOT_EXIST=2;
    /**
     * 分片已经存在
     */
    public static final int SLICE_EXIST=3;
    /**
     * 分片不存在
     */
    public static final int SLICE_NOT_EXIST=4;
    /**
     * 分片上传成功
     */
    public static final int SUCCESS_SLICE_UPLOAD=999;
    /**
     * 所有分片上传成功
     */
    public static final int SUCCESS_ALL_SLICE_UPLOAD=999;




    @RequestMapping("/visit")
    public List<Index> visit(@RequestParam(value = "uuid",required = false)String uuid){//带参数uuid就访问那个文件夹 不带的话就主页
        List<Index> list;

        list=indexService.visitDir(uuid);

        return list;
    }

    @RequestMapping("/mkdir")
    public String createDir(@RequestParam("name")String name,
                            @RequestParam(value = "parent_uuid",required = false)String parentUuid){
        //不对parent_uuid是否为空做判断 因为mybatis里面有动态sql的判断
        indexService.createDir(name, parentUuid);

        return "success";
    }

    /**
     * 如果已经有该md5值对应的文件了，则告诉客户端已经存在，同时数据库里面添加文件记录
     * @param md5
     * @return
     */
    @RequestMapping(value = "/is_upload")
    public Map<String,Object> isUpload(@RequestParam("md5")String md5,
                                       @RequestParam(value = "parent_uuid",required = false)String parentUuid,
                                       @RequestParam(value = "name")String name){
        if(indexService.md5Exist(md5)){
            indexService.saveByMd5(md5,parentUuid,name);

            Map<String,Object> map=new HashMap<>();
            map.put("flag",MD5_EXIST);
            return map;
        }
        Map<String,Object> map=new HashMap<>();
        map.put("flag",MD5_NOT_EXIST);
        return map;
    }

    @RequestMapping(value = "/upload")
    public Map<String,Object> upload(HttpServletRequest request,
                      @RequestParam(value = "file",required = false)MultipartFile multipartFile,
                      @RequestParam("action")String action,
                      @RequestParam("md5")String md5,//分片的md5
                      @RequestParam("filemd5")String fileMd5,//文件的md5
                      @RequestParam("name")String name,//文件名称
                      @RequestParam(value = "parent_uuid",required = false)String parentUuid,//父uuid，不带此参数的话代表
                      @RequestParam(value = "index")Integer index,//文件第几片
                      @RequestParam(value = "total")Integer total,//总片数
                      @RequestParam(value = "finish",required = false)Boolean finish //是否完成
                        ){
        if(action.equals("check")){
            Map<String,Object> map=new HashMap<>();
            map.put("flag",SLICE_NOT_EXIST);
            return map;
        }
        if(action.equals("upload")){
            indexService.saveTemp(multipartFile,fileMd5,md5,index);
            //判断传过来的包finish参数是不是true 如果是的话代表是最后一个包，这时开始执行合并校验操作
            if(finish){
                boolean success=indexService.saveFile(parentUuid, fileMd5,name,total);
                if(success) {
                    Map<String,Object> map=new HashMap<>();
                    map.put("flag",123123123);
                    return map;
                }else {
                    Map<String,Object> map=new HashMap<>();
                    map.put("flag",123188883);
                    return map;
                }
            }
            return new HashMap();
        }
        Map<String,Object> map=new HashMap<>();
        map.put("error","mmp你传错值了");
        return map;


    }

    /**
     * 根据传入的uuid，删除该文件或者文件夹，如果是文件夹的话，则删除文件夹下所有文件
     * 注意，删除决定的确定应该在客户端上完成，这里只负责完成删除
     * @param uuid
     * @return
     */
    @RequestMapping("/delete")
    public String delete(@RequestParam("uuid")String uuid){
        indexService.deleteByUuid(uuid);

        return "删完了";
    }
    @RequestMapping("/thumbnail")
    public void thumbnail(@RequestParam("uuid")String uuid,HttpServletResponse response){
        String md5=indexService.getMd5ByUuid(uuid);
        if(md5==null){
            return;
        }
        response.setContentType("image/jpeg");
        response.addHeader("Content-Disposition","attachment;filename="+md5+".jpg");
        File file=indexService.getThumbnailByMd5(md5);
        if(file==null){
            return;
        }
        try {
            OutputStream os=response.getOutputStream();
            indexService.writeInOutputStream(file,os);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping("/image")
    public void image(@RequestParam("uuid")String uuid,
                      HttpServletRequest request,HttpServletResponse response){
        Index index=indexService.getIndexByUuid(uuid);
        //测试用，测试完删掉
        response.setContentType("image/jpeg");//+index.getSuffix());
        try {
            response.addHeader("Content-Disposition","attachment;filename="+ URLEncoder.encode(index.getName(), "UTF-8"));//url这个是将文件名转码
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        try {
            OutputStream os=response.getOutputStream();
            indexService.writeInOutputStream(index,os);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping("/video")
    public void video(@RequestParam("uuid")String uuid,
                      HttpServletRequest request,HttpServletResponse response){
        Index index=indexService.getIndexByUuid(uuid);
        response.setContentType("video/"+index.getSuffix());
        try {
            response.addHeader("Content-Disposition","attachment;filename="+ URLEncoder.encode(index.getName(), "UTF-8"));//url这个是将文件名转码
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //response.setHeader("Accept-Ranges","bytes");
        try {
            OutputStream os=response.getOutputStream();
            indexService.writeInOutputStream(index,os);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    @RequestMapping("/download")
    public void download(HttpServletRequest request, HttpServletResponse response){
        String uuid=request.getParameter("uuid");
        if(uuid==null){
            return;
        }
        Index index=indexService.getIndexByUuid(uuid);
        String fileName=index.getName();
        Integer fileLength=index.getSize();
        response.setContentType("application/force-download");
        try {
            response.addHeader("Content-Disposition","attachment;filename="+ URLEncoder.encode(fileName, "UTF-8"));//url这个是将文件名转码
            //这句话有坑，数据库里面我记录的都是6 如果设置长度了的话 文件只会下载6b  等文件大小那里没问题了再加上这行代码
            //response.setHeader("Content-Length", String.valueOf(fileLength));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        try {
            //文件丢失时，这里会下载0kb的空文件
            //下次将writeInoutputStream函数修改一下
            OutputStream os=response.getOutputStream();
            indexService.writeInOutputStream(uuid,os);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}









