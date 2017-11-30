package com.sucaiji.cjpan.web;

import com.sucaiji.cjpan.entity.Index;
import com.sucaiji.cjpan.service.IndexService;
import org.apache.coyote.Request;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;


@Controller
public class MainController {
    @Autowired
    private IndexService indexService;



    @RequestMapping("/upload")
    public String upload(Response response,Request request,Model model){
        return "upload";
    }

    @RequestMapping("/")
    public String index(@RequestParam(value = "parent_uuid",required = false)String parentUuid){
        return "index";
    }

    @RequestMapping("/download")
    public String download(){
        return "download";
    }




}
