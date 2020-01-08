package com.zj.spring.framework.core;

public interface BeanFactory {
    /**
     *  从IOC容器中获取实例bean 根据beanName
     * @param beanName
     * @return
     */
    Object getBean(String beanName);
}
