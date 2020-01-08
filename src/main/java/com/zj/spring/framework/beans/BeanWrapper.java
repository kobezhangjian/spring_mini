package com.zj.spring.framework.beans;

import com.zj.spring.framework.core.FactoryBean;
import lombok.Data;

/**
 * @author jian1.zhang
 * @date 2020/1/8  14:05
 */
public class BeanWrapper extends FactoryBean {

    //还会调用 观察者模式
    //1、支持事件响应， 会有一个监听
    private BeanPostProcessor beanPostProcessor;

    private Object wrappedInstance;
    //原生的，反射包装，要存储
    private Object originalInstance;

    public BeanWrapper(Object instance) {
        this.wrappedInstance = instance;
        this.originalInstance = instance;
    }

    public Object getWrappedInstance() {
        return wrappedInstance;
    }

    public Object getOriginalInstance() {
        return originalInstance;
    }

    //返回代理后的class
    //可能会是这个 $Proxy0
    public Class<?> getWrappedClass() {
        return this.wrappedInstance.getClass();
    }

    public void setBeanPostProcessor(BeanPostProcessor beanPostProcessor) {
        this.beanPostProcessor = beanPostProcessor;
    }

}
