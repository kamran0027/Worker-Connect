package com.workerconnect.ratelimiter;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ErrorController {

    @RequestMapping("/error/429")
    public String rateLimitPage() {

        return "error/429";

    }

}