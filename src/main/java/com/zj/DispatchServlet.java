package com.zj;

import com.zj.demo.mvc.action.DemoAction;
import com.zj.spring.framework.annotation.Autowired;
import com.zj.spring.framework.annotation.Controller;
import com.zj.spring.framework.annotation.Service;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author jian1.zhang
 * @date 2020/1/6  17:05
 */
//Servlet只是作为一个MVC的启动入口
public class DispatchServlet extends HttpServlet {

    private Properties contextConfig = new Properties();

    private Map<String, Object> beanMap = new ConcurrentHashMap<>();

    private List<String> classNames = new ArrayList<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("--------调用post--------");
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        //开始初始化的进程

        //定位
        doLoadConfig(config.getInitParameter("contextConfigLocation"));

        //加载
        doScanner(contextConfig.getProperty("scanPackage"));

        //注册
        doRegistry();

        //自动依赖注入
        //在spring中是调用getBean方法来触发依赖注入的
        doAutowired();

        //demo 调用
        DemoAction demoAction = (DemoAction)beanMap.get("demoAction");
        demoAction.query(null, null, "jake");
        //springmvc会多出一个 handlerMapping

        //@RequestMapping中配置得url和method关联上
        //以便用户输入制定url找到具体方法， 通过反射注入
        initHandlerMapping();
    }

    private void doLoadConfig(String location) {
        //spring中是通过Reader去查找和定位的
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(location.replace("classpath:", ""));
        try {
            contextConfig.load(is);
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
    }

    private void doScanner(String packageName) {
        URL url = this.getClass().getClassLoader().getResource("/" + packageName.replaceAll("\\.", "/"));
        File classDir = new File(url.getFile());
        for (File file : classDir.listFiles()) {
            if(file.isDirectory()) {
                doScanner(packageName + "." + file.getName());
            } else {
                classNames.add(packageName + "." + file.getName().replace(".class", ""));
            }
        }
    }

    private void doRegistry() {
        if(classNames.isEmpty()) {return;}

        try {
            for (String className : classNames) {
                Class<?> clazz = Class.forName(className);
                //在spring中用的多个子方法来处理的
                if(clazz.isAnnotationPresent(Controller.class)) {
                    String beanName = lowerFirstCase(clazz.getSimpleName());
                    //在spring中，在这个阶段是不会直接put isntance， 这里put的是beanDefinition
                    beanMap.put(beanName, clazz.newInstance());
                } else if(clazz.isAnnotationPresent(Service.class)) {
                    Service service = clazz.getAnnotation(Service.class);

                    //默认用类名首字母注入
                    //如果自定义beanName，那么使用自己定义的beanName
                    //如果是接口，那么实例化实现类

                    //在spring中同样会分别调用不同的方法， autowiredByName / autowiredByType

                    String beanName = service.value();
                    if("".equals(beanName.trim())) {
                        beanName = lowerFirstCase(clazz.getSimpleName());
                    }
                    Object instance = clazz.newInstance();
                    beanMap.put(beanName, instance);

                    Class<?>[] interfaces = clazz.getInterfaces();
                    for (Class<?> i : interfaces) {
                        beanMap.put(i.getName(), instance);
                    }

                } else {
                    continue;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doAutowired() {
        if(beanMap.isEmpty()) {return;}

        for(Map.Entry<String, Object> entry : beanMap.entrySet()) {
            Field[] fields = entry.getValue().getClass().getDeclaredFields();

            for(Field field : fields) {
                if(field.isAnnotationPresent(Autowired.class)) {
                    Autowired autowired = field.getAnnotation(Autowired.class);
                    String beanName = autowired.value().trim();
                    if("".equals(beanName)) {
                        beanName = field.getType().getName();
                    }
                    field.setAccessible(true);

                    try {
                        field.set(entry.getValue(), beanMap.get(beanName));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }

    private void initHandlerMapping() {
    }

    private String lowerFirstCase(String str) {
        char[] chars = str.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }
}
