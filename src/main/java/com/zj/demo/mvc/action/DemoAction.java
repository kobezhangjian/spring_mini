package com.zj.demo.mvc.action;

import com.zj.demo.service.DemoService;
import com.zj.spring.framework.annotation.Autowired;
import com.zj.spring.framework.annotation.Controller;
import com.zj.spring.framework.annotation.RequestMapping;
import com.zj.spring.framework.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author jian1.zhang
 * @date 2020/1/8  11:31
 */
@Controller
@RequestMapping("/demo")
public class DemoAction {

    @Autowired
    private DemoService demoService;

    @RequestMapping("/query.json")
    public void query(HttpServletRequest request, HttpServletResponse response,
                      @RequestParam("name") String name) {
        String result = demoService.get(name);
        System.out.println(result);
        /*try {
            response.getWriter().write(result);
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    @RequestMapping("/edit.json")
    public void edit(HttpServletRequest request, HttpServletResponse response,
                      @RequestParam("name") String name) {

    }
}
