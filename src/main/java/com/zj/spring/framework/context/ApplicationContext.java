package com.zj.spring.framework.context;

import com.zj.spring.framework.beans.BeanDefinition;
import com.zj.spring.framework.context.support.BeanDefinitionReader;
import com.zj.spring.framework.core.BeanFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author jian1.zhang
 * @date 2020/1/8  13:53
 */
public class ApplicationContext implements BeanFactory {

    private String[] configLocations;

    private BeanDefinitionReader reader;

    private Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();

    public ApplicationContext(String... configLocations) {
        this.configLocations = configLocations;
        this.refresh();
    }

    public void refresh() {

        //1、定位
        this.reader = new BeanDefinitionReader(configLocations);
        //2、加载
        List<String> beanDefinitions =  reader.loadBeanDefinitions();
        //3、注册
        doRegistry(beanDefinitions);
        //4、依赖注入 （lazy-init=false） 要执行依赖注入， 自动调用getBean方法

    }

    //将真正的beanDefinition注册到BeanDefinitionMap中
    private void doRegistry(List<String> beanDefinitions) {
        beanDefinitions.forEach(className -> {
            //beanName三种情况
            //1、默认类名，首字母小写
            //2、自定义beanName
            //3、接口注入
            try {
                Class<?> beanClass = Class.forName(className);

                //如果是一个接口，是不能实例化的
                //用它的实现类来实例化
                if(beanClass.isInterface()){return;}
                BeanDefinition beanDefinition = reader.registerBean(className);
                if(beanDefinition != null) {
                    this.beanDefinitionMap.put(beanDefinition.getFactoryBeanName(), beanDefinition);
                }

                Class<?>[] interfaces = beanClass.getInterfaces();
                for(Class<?> i : interfaces) {
                    //如果是多个实现类，只能覆盖
                    //因为spring没有那么智能，可以通过自定义名字解决
                    this.beanDefinitionMap.put(i.getName(), beanDefinition);
                }

                //容器初始化完毕
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        });
    }

    //依赖注入从这里开始
    //通过读取beanDefinition中的信息，然后通过反射创建一个实例，并返回
    //Spring的做法是不会把最原始的对象放出去，会用一个BeanWrapper来进行一次包装
    //装饰器模式：
    // 1、保留原来的OOP关系
    // 2、需要对它进行扩展，增强，为了以后的AOP打基础
    @Override
    public Object getBean(String name) {
        return null;
    }
}
