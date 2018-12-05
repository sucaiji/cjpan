package com.sucaiji.cjpan.web;

import com.sucaiji.cjpan.config.Property;
import com.sucaiji.cjpan.config.Type;
import com.sucaiji.cjpan.entity.Index;
import com.sucaiji.cjpan.entity.Page;
import com.sucaiji.cjpan.service.IndexService;
import com.sucaiji.cjpan.service.UserService;
import org.apache.coyote.Request;
import org.apache.coyote.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sucaiji.cjpan.config.Property.*;


@Controller
public class MainController {
    final static Logger logger= LoggerFactory.getLogger(MainController.class);
    @Autowired
    private IndexService indexService;
    @Autowired
    private UserService userService;

    @RequestMapping("/login")
    public String login() {
        if (userService.isEmpty()) {
            return "init";
        }
        return "login";
    }

    @RequestMapping("/init")
    public String init() {
        return "init";
    }



    @RequestMapping(value = {"/", "index", "/index"})
    public String index(@RequestParam(value = "parent_uuid", required = false) String parentUuid,
                        @RequestParam(value = "pg", required = false) Integer pageNumber,
                        @RequestParam(value = "limit", required = false) Integer limit,
                        Model model) {
        if (parentUuid == null) {
            parentUuid = ROOT;
        }
        logger.debug("用户请求的parentUuid为[{}]",parentUuid);
        if (pageNumber == null) {
            pageNumber = 1;
        }
        logger.debug("用户请求的pageNumber为[{}]",pageNumber+"");

        Page page = indexService.getPage(pageNumber, limit, parentUuid);
        List<Index> list = indexService.getIndexList(page, parentUuid);

        model.addAttribute("indexList", list);

        int total = indexService.getTotal(parentUuid);
        int pageAmount = (int) Math.ceil((double) total/(double) indexService.DEFAULT_PAGE_SIZE);
        model.addAttribute("currentPage",pageNumber);
        model.addAttribute("pageAmount",pageAmount);

        Index index = indexService.getIndexByUuid(parentUuid);
        model.addAttribute("parentIndex", index);
        return "index";
    }

    @RequestMapping("/file/{str}")
    public String type(@PathVariable("str") String str,
                       @RequestParam(value = "pg" , required = false) Integer pageNumber,
                       @RequestParam(value = "limit" , required = false) Integer limit,
                       Model model) {
        if (pageNumber == null) {
            pageNumber = 1;
        }
        Type type = Type.getType(str);

        Page page = indexService.getPageWithType(pageNumber, limit, type);
        List<Index> list = indexService.getIndexList(page, type);

        model.addAttribute("indexList", list);

        int total = indexService.getTotalWithType(type.toString());
        int pageAmount = (int) Math.ceil((double) total/(double)indexService.DEFAULT_PAGE_SIZE);
        System.out.println(total+"+"+pageAmount);
        model.addAttribute("currentPage", pageNumber);
        model.addAttribute("pageAmount", pageAmount);

        return "type";

    }

    @RequestMapping("/video/{uuid}")
    public String video(@PathVariable("uuid") String uuid,
                        Model model) {
        Index index = indexService.getIndexByUuid(uuid);
        model.addAttribute("index", index);
        return "video";
    }




    @RequestMapping("/test")
    public String test(HttpServletRequest request, Model model) {


        return "test";
    }


}
