package com.sucaiji.cjpan.web;

import com.sucaiji.cjpan.entity.Index;
import com.sucaiji.cjpan.service.IndexService;
import com.sucaiji.cjpan.service.UserService;
import org.apache.coyote.Request;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


@Controller
public class MainController {
    @Autowired
    private IndexService indexService;
    @Autowired
    private UserService userService;

    @RequestMapping("/login")
    public String login(){
        if(userService.isEmpty()){
            return "init";
        }
        return "login";
    }

    @RequestMapping("/init")
    public String init(){
        return "init";
    }

    @RequestMapping("/upload")
    public String upload(Response response,Request request,Model model){
        return "upload";
    }

    @RequestMapping(value = {"/","index","/index"})
    public String index(@RequestParam(value = "parent_uuid",required = false)String parentUuid,
                        Model model){
        List<Index> list;
        list=indexService.visitDir(parentUuid);
        model.addAttribute("indexList",list);

        if(parentUuid==null){
            model.addAttribute("parentIndex",null);
            return "index";
        }
        Index index=indexService.getIndexByUuid(parentUuid);
        model.addAttribute("parentIndex",index);
        return "index";
    }

    @RequestMapping("/download")
    public String download(){
        return "download";
    }

    @RequestMapping("/video/{uuid}")
    public String video(@PathVariable("uuid")String uuid,
                        Model model){
        Index index=indexService.getIndexByUuid(uuid);
        model.addAttribute("index",index);
        return "video";
    }

    /**
     * 画廊
     * @return
     */
    @RequestMapping("/image")
    public String image(){
        return "image";
    }


    @RequestMapping("/test")
    public String test(HttpServletRequest request,Model model){

        String parentUuid=request.getParameter("parent_uuid");
        List<Index> list;
        list=indexService.visitDir(parentUuid);
        model.addAttribute("indexList",list);


        return "test";
    }




}
