package com.workpilot.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class FrontendController {

    @RequestMapping(value = {
            "/",
            "/{x:[\\w\\-]+}",
            "/{x:^(?!api$).*$}/**/{y:[\\w\\-]+}"
    })
    public String redirect() {
        return "forward:/index.html";
    }
}
