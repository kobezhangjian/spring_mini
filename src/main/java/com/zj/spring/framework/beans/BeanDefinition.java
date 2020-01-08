package com.zj.spring.framework.beans;

import com.sun.istack.internal.Nullable;
import lombok.Data;

/**
 * @author jian1.zhang
 * @date 2020/1/8  14:05
 */
//用来存储配置文件中的信息
//相当于是保存在内存中的配置
@Data
public class BeanDefinition {

    private String beanClassName;
    private boolean lazyInit = false;
    private String factoryBeanName;



    public void setBeanClassName(@Nullable String beanClassName){
        this.beanClassName = beanClassName;
    }

    public String getBeanClassName() {
        return beanClassName;
    }

    public String getFactoryBeanName() {
        return factoryBeanName;
    }

    public void setFactoryMethodName(@Nullable String factoryMethodName) {
        //this.factoryBeanName = factoryMethodName;
    }

    public void setLazyInit(boolean lazyInit) {

    }

    public boolean isLazyInit(){
        return false;
    }
}
