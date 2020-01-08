package com.zj.demo.service.impl;

import com.zj.demo.service.DemoService;
import com.zj.spring.framework.annotation.Service;

/**
 * @author jian1.zhang
 * @date 2020/1/8  11:36
 */
@Service
public class DemoServiceImpl implements DemoService {
    @Override
    public String get(String name) {
        return "my name is " + name;
    }
}
