package com.sucaiji.cjpan.web;

import com.sucaiji.cjpan.config.Property;
import com.sucaiji.cjpan.config.Type;
import com.sucaiji.cjpan.model.Index;
import com.sucaiji.cjpan.model.vo.PageVo;
import com.sucaiji.cjpan.service.IndexService;
import com.sucaiji.cjpan.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


import static com.sucaiji.cjpan.config.Property.*;


@Controller
public class MainController {
    final static Logger logger= LoggerFactory.getLogger(MainController.class);
    @Autowired
    private IndexService indexService;
    @Autowired
    private UserService userService;

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login() {
        if (userService.isEmpty()) {
            return "init";
        }
        return "login";
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String loginForm(@RequestParam("account")String account,
                            @RequestParam("password")String password) {
        return "login";
    }

    @RequestMapping(value = {"/", "index", "/index"})
    public String index(@RequestParam(value = "parentUuid", defaultValue = ROOT, required = false) String parentUuid,
                        @RequestParam(value = "pg", required = false, defaultValue = "1") Integer pageNumber,
                        @RequestParam(value = "limit", required = false, defaultValue = Property.DEFAULT_PAGE_SIZE_STR) Integer limit,
                        Model model) {
        Index queryIndex = new Index();
        queryIndex.setParentUuid(parentUuid);
        PageVo vo = indexService.getPageVo(pageNumber, limit, queryIndex);
        Index index = indexService.getIndexByUuid(parentUuid);
        model.addAttribute("vo", vo);
        model.addAttribute("parentIndex", index);
        return "index";
    }

    @RequestMapping(value = "/search")
    public String search(@RequestParam(value = "name") String name,
                         @RequestParam(value = "pg", required = false, defaultValue = "1") Integer pageNumber,
                        @RequestParam(value = "limit", required = false, defaultValue = Property.DEFAULT_PAGE_SIZE_STR) Integer limit,
                        Model model) {
        PageVo vo = indexService.search(pageNumber, limit, name);
        model.addAttribute("vo", vo);
        return "search";
    }


    @RequestMapping("/file/{str}")
    public String type(@PathVariable("str") String type,
                       @RequestParam(value = "pg" , required = false, defaultValue = "1") Integer pageNumber,
                       @RequestParam(value = "limit" , required = false, defaultValue = Property.DEFAULT_PAGE_SIZE_STR) Integer limit,
                       Model model) {
//        Type type = Type.getType(str);
        Index index = new Index();
        index.setType(type);
        PageVo vo = indexService.getPageVo(pageNumber, limit, index);
        model.addAttribute("vo",vo);
        return "type";
    }

    @RequestMapping("/video/{uuid}")
    public String video(@PathVariable("uuid") String uuid,
                        Model model) {
        Index index = indexService.getIndexByUuid(uuid);
        model.addAttribute("index", index);
        return "video";
    }

}
