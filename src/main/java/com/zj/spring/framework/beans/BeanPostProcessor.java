package com.zj.spring.framework.beans;

/**
 * @author jian1.zhang
 * @date 2020/1/8  15:39
 */
//用作事件监听
public class BeanPostProcessor {

    public Object postProcessBeforeInitialization(Object bean, String beanName){
        return bean;
    }

    public Object postProcessAfterInitialization(Object bean, String beanName) {
        return bean;
    }

}
