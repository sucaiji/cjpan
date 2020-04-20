package com.sucaiji.cjpan.web;


import com.sucaiji.cjpan.config.Type;
import com.sucaiji.cjpan.model.Index;
import com.sucaiji.cjpan.model.Range;
import com.sucaiji.cjpan.service.IndexService;
import com.sucaiji.cjpan.service.UserService;
import com.sucaiji.cjpan.util.Utils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sucaiji.cjpan.config.Property.ROOT;

@RestController
@RequestMapping("/api")
public class ApiController {
    final static Logger logger = LoggerFactory.getLogger(ApiController.class);
    @Autowired
    private IndexService indexService;
    @Autowired
    private UserService userService;

    /**
     * 相同文件已经存在，返回值告诉客户端秒传
     */
    public static final int MD5_EXIST=1;
    /**
     * 不存在相同文件
     */
    public static final int MD5_NOT_EXIST=2;

    @RequestMapping("/exit")
    public void exit(){
        Subject subject=SecurityUtils.getSubject();
        subject.logout();
    }


    /**
     * 创建文件夹
     * @param name
     * @param parentUuid
     * @return 成功返回success 失败返回fail
     */
    @RequestMapping("/mkdir")
    public String createDir(@RequestParam("name")String name,
                            @RequestParam(value = "parentUuid",required = false)String parentUuid){
        if(parentUuid==null){
            parentUuid=ROOT;
        }
        boolean success = indexService.createDir(name, parentUuid);
        if (success) {
            return "success";
        }
        return "fail";
    }

    /**
     * 获取一个uuid
     * @return
     */
    @RequestMapping(value = "/getUuid")
    public String getUUID() {
        return Utils.UUID();
    }

    /**
     * 如果已经有该md5值对应的文件了，则告诉客户端已经存在，同时数据库里面添加文件记录
     * @return
     */
    @RequestMapping(value = "/is_upload")
    public Map<String,Object> isUpload(@RequestParam(value = "parentUuid",required = false)String parentUuid,
                                       @RequestParam(value = "name")String name){
        Map<String,Object> map=new HashMap<>();
        map.put("flag",MD5_NOT_EXIST);
        return map;
    }


    @RequestMapping(value = "/upload")
    public void upload(HttpServletRequest request,
                      @RequestParam(value = "file",required = false) MultipartFile multipartFile,
                      @RequestParam("uuid") String uuid,
                      @RequestParam("name") String name,//文件名称
                      @RequestParam(value = "parentUuid",required = false) String parentUuid,//父uuid，不带此参数的话代表
                      @RequestParam(value = "index") Integer index,//文件第几片
                      @RequestParam(value = "total") Integer total,//总片数
                      @RequestParam(value = "finish",required = false) Boolean finish //是否完成
                        ){
        if(parentUuid == null){
            parentUuid = ROOT;
        }
        indexService.saveTemp(multipartFile, uuid, index);
        //判断传过来的包finish参数是不是true 如果是的话代表是最后一个包，这时开始执行合并校验操作
        if(finish){
            indexService.saveFile(parentUuid, uuid, name, total);
        }
    }

    @RequestMapping(value = "/checkUpload")
    public Map<String,Object> checkSuccess(@RequestParam("uuid") String uuid) {
        boolean success = indexService.checkUpload(uuid);

        Map<String,Object> map=new HashMap<>();
        map.put("success", success);
        return map;

    }

    //获取全部正在校验md5的list 暂时是测试用
    @RequestMapping(value = "/getUploadList")
    public List<String> uploadQueue() {
        List<String> list = new ArrayList<>();
        for (Map.Entry<String, Object> entry: indexService.getCheckMap().entrySet()) {
            list.add(entry.getKey());
        }
        return list;
    }


    @RequestMapping("/rename")
    public String rename(@RequestParam("uuid")String uuid,
                         @RequestParam("name")String name) {
        Index updateIndex = new Index();
        updateIndex.setUuid(uuid);
        updateIndex.setName(name);
        indexService.updateIndex(updateIndex);
        return "success";
    }

    /**
     * 根据传入的uuid，删除该文件或者文件夹，如果是文件夹的话，则删除文件夹下所有文件
     * @param uuid
     * @return
     */
    @RequestMapping("/delete")
    public String delete(@RequestParam("uuid")String uuid){
        indexService.deleteByUuid(uuid);
        return "success";
    }
    @RequestMapping("/thumbnail")
    public void thumbnail(@RequestParam("uuid")String uuid,
                          HttpServletResponse response){
        response.setContentType("image/jpeg");
        response.addHeader("Content-Disposition","attachment;filename="+uuid+".jpg");
        try {
            OutputStream os=response.getOutputStream();
            indexService.writeThumbnailInOutputStream(uuid, os);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping("/image")
    public void image(@RequestParam("uuid")String uuid,
                      HttpServletRequest request,HttpServletResponse response){
        Index index=indexService.getIndexByUuid(uuid);
        response.setContentType("image/jpeg");
        try {
            OutputStream os=response.getOutputStream();
            indexService.writeInOutputStream(index,os);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping("/login")
    public String login(HttpSession session,
                        @RequestParam("account")String account,
                        @RequestParam("password")String password){
        Subject subject=SecurityUtils.getSubject();
        UsernamePasswordToken token=new UsernamePasswordToken(account,password);
        try {
            subject.login(token);
        } catch (Exception e){
            return "failure";
        }
        subject.getSession().setAttribute("account", account);
        return "success";
    }

    @RequestMapping("/change_password")
    public String changePassword(@RequestParam("old_pwd")String oldPassword,
                                 @RequestParam("new_pwd")String newPassword) {
        Subject subject = SecurityUtils.getSubject();
        String account = subject.getSession().getAttribute("account").toString();
        boolean success = userService.changePassword(account, oldPassword, newPassword);
        if (success) {
            return "success";
        }
        return "fail";
    }

    @RequestMapping("/init_regist")
    public String initRegister(
                            @RequestParam("account")String account,
                            @RequestParam("password")String password,
                            @RequestParam("name")String name){
        //user表是空的代表第一次用
        if(!userService.isEmpty()){
            return "fail";
        }
        //魔法值admin后期改
        //改个屁就这样吧
        userService.regist(account, password, name,"admin");
        return "success";
    }

    @RequestMapping("/video")
    public void video(@RequestParam("uuid")String uuid,
                      HttpServletRequest request,HttpServletResponse response){
        Index index=indexService.getIndexByUuid(uuid);
        response.setContentType("video/mp4");//+index.getSuffix());


        try {
            response.addHeader("Content-Disposition","attachment;filename="+ URLEncoder.encode(index.getName(), "UTF-8"));//url这个是将文件名转码
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        response.setHeader("Accept-Ranges","bytes");


        String rangeStr=request.getHeader("range");
        if(rangeStr!=null){
            Range range = Range.getRange(rangeStr,index.getSize());
            String total=String.valueOf(range.getTotal());
            String length=String.valueOf(range.getLength());
            String start=String.valueOf(range.getStart());
            String end=String.valueOf(range.getEnd());

            response.setHeader("Content-Range","bytes "+start+"-"+end+"/"+total);
            response.setHeader("Content-Length",length);

            System.out.println("range不为空");
            System.out.println("rangeStr="+rangeStr);
            System.out.println("start"+range.getStart());
            System.out.println("end"+range.getEnd());
            System.out.println("length"+range.getLength());
            System.out.println("total"+range.getTotal());
            response.setStatus(206);
            try {
                OutputStream os=response.getOutputStream();
                indexService.writeInOutputStream(index,os,range);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            Long end=index.getSize()-1L;
            Range range=new Range(0L,end,index.getSize());
            String length=String.valueOf(index.getSize());
            System.out.println("range为空!");
            response.setStatus(200);
            response.setHeader("Content-Range", "bytes 0-"+end+"/"+index.getSize());
            response.setHeader("Content-Length", length);

            try {
                OutputStream os=response.getOutputStream();
                indexService.writeInOutputStream(index,os,range);
            } catch (IOException e) {
                e.printStackTrace();
            }
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
        Long fileLength=index.getSize();
        response.setContentType("application/force-download");
        try {
            response.addHeader("Content-Disposition","attachment;filename="+ URLEncoder.encode(fileName, "UTF-8"));//url这个是将文件名转码
            response.setHeader("Content-Length", String.valueOf(fileLength));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        try {
            //文件丢失时，这里会下载0kb的空文件
            //下次将writeInoutputStream函数修改一下
            OutputStream os=response.getOutputStream();
            indexService.writeInOutputStream(index,os);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}









