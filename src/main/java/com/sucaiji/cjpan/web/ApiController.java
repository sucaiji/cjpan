package com.sucaiji.cjpan.web;


import com.sucaiji.cjpan.config.Property;
import com.sucaiji.cjpan.config.Type;
import com.sucaiji.cjpan.entity.Index;
import com.sucaiji.cjpan.entity.Page;
import com.sucaiji.cjpan.service.IndexService;
import com.sucaiji.cjpan.service.UserService;
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

import static com.sucaiji.cjpan.config.Property.PARENT_UUID;
import static com.sucaiji.cjpan.config.Property.ROOT;
import static com.sucaiji.cjpan.config.Property.TYPE;

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
     *  获取一共有多少条数据
     *  如果tparent_uuid不为空，则查询parent_uuid文件夹下的type类型文件
     *  如果parentUuid为空，获取root目录下文件总条数
     * @param parentUuid
     * @return
     */
    @RequestMapping("/total")
    public Integer total(@RequestParam(value = "parent_uuid",required = false)String parentUuid) {
        if(parentUuid==null){
            parentUuid=ROOT;
        }
        return indexService.getTotal(parentUuid);
    }

    /**
     *  获取一共有多少条type类型的数据
     * @param type
     * @return
     */
    @RequestMapping("/total_with_type")
    public Integer totalWithType(@RequestParam(value="type")String type){

        return indexService.getTotalWithType(type);
    }



    /**
     * 访问文件列表
     * @param parentUuid 访问的文件夹的uuid，如果为空，则是访问根目录
     * @param limit 每页的个数,不填则是默认值
     * @param pg 第几页
     * @return
     */
    @RequestMapping("/visit")
    public Map<String, Object> visit(@RequestParam(value = "parent_uuid",required = false)String parentUuid,
                             @RequestParam(value = "limit",required = false)Integer limit,
                             @RequestParam(value = "pg",required = false)Integer pg){//带参数uuid就访问那个文件夹 不带的话就主页
        Map<String, Object> map = new HashMap<>();


        if(null == parentUuid){
            parentUuid = ROOT;
            Page page = indexService.getPage(pg, limit, parentUuid);
            List<Index> list = indexService.getIndexList(page, parentUuid);
            map.put("page", page);
            map.put("data", list);
            System.out.println(page);
            return map;
        }
        Page page = indexService.getPage(pg, limit, parentUuid);
        List<Index> list = indexService.getIndexList(page, parentUuid);
        map.put("page", page);
        map.put("data", list);
        return map;
    }


    /**
     * 访问文件列表
     * @param type 想要访问的文件类型，不填则是全部类型
     * @param limit 每页的个数,不填则是默认值
     * @param pg 第几页
     * @return
     */
    @RequestMapping("/visit_with_type")
    public Map<String, Object> visitWithType(@RequestParam(value = "type")String type,
                                     @RequestParam(value = "limit",required = false)Integer limit,
                                     @RequestParam(value = "pg",required = false)Integer pg){
        Map<String, Object> map = new HashMap<>();
        Type type1 = Type.getType(type);
        Page page = indexService.getPageWithType(pg, limit, type1);
        List<Index> list = indexService.getIndexList(page, type1);
        map.put("page", page);
        map.put("data", list);

        return map;

    }

    /**
     * 创建文件夹
     * @param name
     * @param parentUuid
     * @return 成功返回success 失败返回fail
     */
    @RequestMapping("/mkdir")
    public String createDir(@RequestParam("name")String name,
                            @RequestParam(value = "parent_uuid",required = false)String parentUuid){
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
     * 如果已经有该md5值对应的文件了，则告诉客户端已经存在，同时数据库里面添加文件记录
     * @param md5
     * @return
     */
    @RequestMapping(value = "/is_upload")
    public Map<String,Object> isUpload(@RequestParam("md5")String md5,
                                       @RequestParam(value = "parent_uuid",required = false)String parentUuid,
                                       @RequestParam(value = "name")String name){
        if(parentUuid==null){
            parentUuid=ROOT;
        }
        if(indexService.md5Exist(md5)){
            logger.debug("该文件已经存在，进入秒传分支");
            indexService.saveByMd5(md5,parentUuid,name);
            logger.debug("保存一个md5[{}],parentUuid[{}],name[{}]的文件",md5,parentUuid,name);
            Map<String,Object> map=new HashMap<>();
            map.put("flag",MD5_EXIST);
            logger.debug("将信息返回到客户端[{}]",map);
            return map;
        }
        Map<String,Object> map=new HashMap<>();
        map.put("flag",MD5_NOT_EXIST);
        return map;
    }


    @RequestMapping(value = "/upload")
    public void upload(HttpServletRequest request,
                      @RequestParam(value = "file",required = false)MultipartFile multipartFile,
//                      @RequestParam("action")String action,
//                      @RequestParam("md5")String md5,//分片的md5
                      @RequestParam("filemd5")String fileMd5,//文件的md5
                      @RequestParam("name")String name,//文件名称
                      @RequestParam(value = "parent_uuid",required = false)String parentUuid,//父uuid，不带此参数的话代表
                      @RequestParam(value = "index")Integer index,//文件第几片
                      @RequestParam(value = "total")Integer total,//总片数
                      @RequestParam(value = "finish",required = false)Boolean finish //是否完成
                        ){
        if(parentUuid == null){
            parentUuid = ROOT;
        }
        indexService.saveTemp(multipartFile, fileMd5, index);
        //判断传过来的包finish参数是不是true 如果是的话代表是最后一个包，这时开始执行合并校验操作
        if(finish){
            indexService.saveFile(parentUuid, fileMd5, name, total);
        }
    }

    @RequestMapping(value = "/checkUpload")
    public Map<String,Object> checkSuccess(@RequestParam("filemd5") String fileMd5) {
        boolean success = indexService.checkUpload(fileMd5);

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
        indexService.setIndexName(uuid, name);
        return "success";
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

        return "success";
    }
    @RequestMapping("/thumbnail")
    public void thumbnail(@RequestParam("uuid")String uuid,
                          HttpServletResponse response){
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
                            HttpServletRequest request,
                            HttpServletResponse response,
                            @RequestParam("account")String account,
                            @RequestParam("password")String password,
                            @RequestParam("name")String name){
        if(!userService.isEmpty()){
            try {
                response.sendRedirect("/index");
            } catch (IOException e) {
                e.printStackTrace();
            }
            //如果user表不是空的则什么也不做
            return "fail";
        }
        //魔法值admin后期改
        userService.regist(account,password,name,"admin");
        try {
            response.sendRedirect("/index");
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            IndexService.Range range=indexService.getRange(rangeStr,index.getSize());
            String total=String.valueOf(range.total);
            String length=String.valueOf(range.length);
            String start=String.valueOf(range.start);
            String end=String.valueOf(range.end);

            response.setHeader("Content-Range","bytes "+start+"-"+end+"/"+total);
            response.setHeader("Content-Length",length);

            System.out.println("range不为空");
            System.out.println("rangeStr="+rangeStr);
            System.out.println("start"+range.start);
            System.out.println("end"+range.end);
            System.out.println("length"+range.length);
            System.out.println("total"+range.total);
            response.setStatus(206);
            try {
                OutputStream os=response.getOutputStream();
                indexService.writeInOutputStream(index,os,range);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            Long end=index.getSize()-1L;
            IndexService.Range range=new IndexService.Range(0L,end,index.getSize());
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
            indexService.writeInOutputStream(uuid,os);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}









