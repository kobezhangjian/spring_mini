package com.zj.demo.mvc.action;

import com.zj.demo.service.DemoService;
import com.zj.spring.framework.annotation.Autowired;
import com.zj.spring.framework.annotation.Controller;
import com.zj.spring.framework.annotation.RequestMapping;

/**
 * @author jian1.zhang
 * @date 2020/1/8  11:32
 */
@Controller
public class MyAction {

    @Autowired
    private DemoService demoService;

    @RequestMapping("/index.html")
    public void query() {

    }
}
