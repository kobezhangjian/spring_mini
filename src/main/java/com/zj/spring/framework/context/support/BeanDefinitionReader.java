package com.zj.spring.framework.context.support;

import com.zj.spring.framework.beans.BeanDefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

/**
 * @author jian1.zhang
 * @date 2020/1/8  14:03
 */

//用来对配置文件进行查找、读取，解析
public class BeanDefinitionReader {

    private Properties config = new Properties();

    private List<String> registryBeanClasses = new ArrayList<>();

    //在配置文件中用来获取自动扫描的包名的key
    private final String SCAN_PACKAGE = "scanPackage";

    public BeanDefinitionReader(String... locations) {
        //spring中是通过Reader去查找和定位的
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(locations[0].replace("classpath:", ""));
        try {
            config.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(null != is) {
                try {
                    is.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        doScanner(config.getProperty(SCAN_PACKAGE));
    }


    public List<String> loadBeanDefinitions() {
        return this.registryBeanClasses;
    }

    //没注册一个className，就返回一个beanDefinition
    //beanDefinition只是为了对配置信息进行一个包装
    public BeanDefinition registerBean(String className) {
        if(this.registryBeanClasses.contains(className)) {
            BeanDefinition beanDefinition = new BeanDefinition();
            beanDefinition.setBeanClassName(className);
            beanDefinition.setFactoryBeanName(lowerFirstCase(className.substring(className.lastIndexOf(".") + 1)));
            return beanDefinition;
        }
        return null;
    }

    public Properties getConfig() {
        return this.config;
    }

    //递归扫描所有的相关联的class，并且保存到一个list中
    private void doScanner(String packageName) {
        URL url = this.getClass().getClassLoader().getResource("/" + packageName.replaceAll("\\.", "/"));
        File classDir = new File(url.getFile());
        for (File file : classDir.listFiles()) {
            if(file.isDirectory()) {
                doScanner(packageName + "." + file.getName());
            } else {
                registryBeanClasses.add(packageName + "." + file.getName().replace(".class", ""));
            }
        }
    }

    private String lowerFirstCase(String str) {
        char[] chars = str.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

}
