package com.zj.spring.framework.context;

import com.zj.demo.mvc.action.DemoAction;
import com.zj.spring.framework.annotation.Autowired;
import com.zj.spring.framework.annotation.Controller;
import com.zj.spring.framework.beans.BeanDefinition;
import com.zj.spring.framework.beans.BeanPostProcessor;
import com.zj.spring.framework.beans.BeanWrapper;
import com.zj.spring.framework.context.support.BeanDefinitionReader;
import com.zj.spring.framework.core.BeanFactory;

import javax.xml.ws.Service;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author jian1.zhang
 * @date 2020/1/8  13:53
 */
public class ApplicationContext implements BeanFactory {

    private String[] configLocations;

    private BeanDefinitionReader reader;

    //beanDefinitionMap用来保存配置信息
    private Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();

    //用来保证注册式单例的容器
    private Map<String, Object> beanCacheMap = new ConcurrentHashMap<>();

    //用来存储所有的被代理过的对象
    private Map<String, BeanWrapper> beanWrapperMap = new ConcurrentHashMap<>();

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
        doAutowired();
        DemoAction demoAction = (DemoAction)beanWrapperMap.get("demoAction").getWrappedInstance();
        demoAction.query(null, null, "jake");
    }

    //自动化的依赖注入
    private void doAutowired() {

        for(Map.Entry<String, BeanDefinition> beanDefinitionEntry : this.beanDefinitionMap.entrySet()) {
            String beanName = beanDefinitionEntry.getKey();
            if(!beanDefinitionEntry.getValue().isLazyInit()) {
                getBean(beanName);
            }
        }

        for(Map.Entry<String, BeanWrapper> beanWrapperEntry : this.beanWrapperMap.entrySet()) {
            populateBean(beanWrapperEntry.getKey(), beanWrapperEntry.getValue().getWrappedInstance());
        }

    }

    //真正的注入
    public void populateBean(String beanName, Object instance) {
        Class clazz = instance.getClass();
        //不是所有牛奶都叫特仑苏
        if(!(clazz.isAnnotationPresent(Controller.class) || clazz.isAnnotationPresent(Service.class))) {
            return;
        }
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if(!field.isAnnotationPresent(Autowired.class)) {continue;}
            Autowired autowired = field.getAnnotation(Autowired.class);
            String autowiredBeanName = autowired.value().trim();
            if("".equals(autowiredBeanName)) {
                autowiredBeanName = field.getType().getName();
            }
            field.setAccessible(true);
            try {
                System.out.println("=============" + instance + "," + autowiredBeanName + "," + this.beanWrapperMap.get(autowiredBeanName).getWrappedInstance());
                field.set(instance, this.beanWrapperMap.get(autowiredBeanName).getWrappedInstance());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

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
    //通过读取BeanDefinition中的信息，然后通过反射创建一个实例，并返回
    //Spring的做法是不会把最原始的对象放出去，会用一个BeanWrapper来进行一次包装
    //装饰器模式：
    // 1、保留原来的OOP关系
    // 2、需要对它进行扩展，增强，为了以后的AOP打基础
    @Override
    public Object getBean(String beanName) {
        BeanDefinition beanDefinition = this.beanDefinitionMap.get(beanName);
        //生产通知事件
        BeanPostProcessor beanPostProcessor = new BeanPostProcessor();

        Object instance = instantionBean(beanDefinition);
        if(null == instance) {
            return null;
        }
        //初始化之前通知一次
        beanPostProcessor.postProcessBeforeInitialization(instance, beanName);

        BeanWrapper beanWrapper = new BeanWrapper(instance);
        beanWrapper.setBeanPostProcessor(beanPostProcessor);
        this.beanWrapperMap.put(beanName, beanWrapper);

        //初始化之后通知一次
        beanPostProcessor.postProcessAfterInitialization(instance, beanName);

//        this.populateBean(beanName, instance);

        //这样调用，留有可操作空间
        return this.beanWrapperMap.get(beanName).getWrappedInstance();
    }

    //传一个BeanDefinition,就返回一个实例
    private Object instantionBean(BeanDefinition beanDefinition) {
        Object instance = null;
        String className = beanDefinition.getBeanClassName();
        try {
            //根据class才能确定一个类是否有实例
            if(this.beanCacheMap.containsKey(className)) {
                instance = this.beanCacheMap.get(className);
            } else {
                Class<?> clazz = Class.forName(className);
                instance = clazz.newInstance();
                this.beanCacheMap.put(className, instance);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return instance;
    }
}
