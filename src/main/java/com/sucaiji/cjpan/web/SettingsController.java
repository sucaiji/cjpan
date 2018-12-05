package com.sucaiji.cjpan.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/settings")
public class SettingsController {

    @RequestMapping("/change_passwd")
    public String index() {
        return "settings/change_passwd";
    }

}
