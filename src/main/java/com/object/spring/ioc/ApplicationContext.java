package com.object.spring.ioc;

import com.object.spring.annotation.*;
import com.object.spring.config.SpringConfig;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Object
 * @version 1.0
 * @date 2024/3/1
 */
public class ApplicationContext {


    private final ConcurrentHashMap<String, Object> singletonObjects =
            new ConcurrentHashMap<>(16);

    private final ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap =
            new ConcurrentHashMap<>(16);

    private final ArrayList<BeanPostProcessor> postProcessors = new ArrayList<>(16);

    private final ArrayList<Class<?>> aspects = new ArrayList<>(16);

    public ApplicationContext(Class<? extends Config> configClass){
        beanDefinitionScan(configClass);
        //初始化单例池
        initSingletonObjects();
    }

    public void initSingletonObjects(){
        ConcurrentHashMap.KeySetView<String, BeanDefinition> set = beanDefinitionMap.keySet();
        for (String beanName : set) {
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            String scope = beanDefinition.getScope();
            if ("singleton".equals(scope)) {
                //存入单例池
                singletonObjects.put(beanName, this.createBean(beanName, beanDefinition));
            }
        }
    }

    private void beanDefinitionScan(Class<? extends Config> configClass) {
        //获取配置类上的注解
        ComponentScan componentScan = configClass.getDeclaredAnnotation(ComponentScan.class);
        //获取注解上的参数
        String[] value = componentScan.value();
        //获取全路径
        ClassLoader classLoader = ApplicationContext.class.getClassLoader();
        for (String packages : value) {
            URL resource = classLoader.getResource(packages.replace(".", "/"));
            //获取到全路径后，扫描出下面的文件
            File file = new File(resource.getFile());
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (File clazz : files) {
                    //判断文件是否是class结尾的
                    String path = clazz.getAbsolutePath();
                    if (path.substring(path.lastIndexOf(".") + 1).equals("class")) {
                        //通过反射判断该类上是否有对应的注解
                        String className = path.substring(path.lastIndexOf('\\') + 1, path.lastIndexOf('.'));
                        String classPath = packages + "." + className;
                        try {
                            Class<?> cls = classLoader.loadClass(classPath);
                            //如果存在对应注解 加入单例池
                            //判断其上面是否有scope注解以及是否设置为单例
                            boolean isSingleton = true;
                            if(cls.isAnnotationPresent(Component.class)){
                                Component component = cls.getDeclaredAnnotation(Component.class);
                                //封装beanDefinition对象
                                String classLowerName = StringUtils.uncapitalize(className);
                                if(!"".equals(component.value())){
                                    classLowerName = component.value();
                                }
                                boolean present = cls.isAnnotationPresent(Scope.class);
                                BeanDefinition beanDefinition = new BeanDefinition(present ? cls.getDeclaredAnnotation(Scope.class).value() : "singleton"
                                        , cls
                                        , component.value().isEmpty() ? classLowerName : component.value());

                                //存入到beanDefinitionMap
                                beanDefinitionMap.put(classLowerName, beanDefinition);

                                //判断是否实现了BeanPostProcessor接口，是否是一个后处理器
                                if(BeanPostProcessor.class.isAssignableFrom(cls)){
                                    postProcessors.add(((BeanPostProcessor)cls.newInstance()));
                                }

                                if(cls.isAnnotationPresent(Aspect.class)){
                                    aspects.add(cls);
                                }
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public Object createBean(String beanName, BeanDefinition beanDefinition){
        Class cls = beanDefinition.getClazz();

        try {
            Object instance = cls.getDeclaredConstructor().newInstance();
            setAttributesByAuto(cls, instance);
            Method beforeMethod = null;
            Method afterMethod = null;
            boolean haveBefore = false;
            boolean haveAfter = false;
            Class aimClass = null;
            Object proxyInstance = null;

            //进行后处理器处理
            for (BeanPostProcessor processor : postProcessors) {
                Object currentBean = processor.postProcessBeforeInitialization(instance, beanName);
                if(!Objects.isNull(currentBean)){
                    instance = currentBean;
                }
            }
            //初始化方法的执行
            System.out.println("这里是初始化方法的执行");

            //进行aop的过程
            String clsName = cls.getName();
            for (Class<?> aspect : aspects) {
                Method[] methods = aspect.getDeclaredMethods();
                for (Method method : methods) {
                    if (method.isAnnotationPresent(Before.class)) {
                        String aimClassName = method.getDeclaredAnnotation(Before.class).value();
                        //如果类名匹配上了，那么匹配其中的方法名
                        if (clsName.equals(aimClassName.substring(0, aimClassName.lastIndexOf('.')))) {
                            Method[] methods1 = cls.getDeclaredMethods();
                            for (Method method1 : methods1) {
                                if (method1.getName().equals(aimClassName.substring(aimClassName.lastIndexOf('.') + 1))) {
                                    //将此方法进行保存，后续一起执行
                                    beforeMethod = method;
                                    haveBefore = true;
                                    aimClass = aspect;
                                    break;
                                }
                            }
                        }
                    }else if(method.isAnnotationPresent(After.class)){
                        String aimClassName = method.getDeclaredAnnotation(After.class).value();
                        //如果类名匹配上了，那么匹配其中的方法名
                        if (clsName.equals(aimClassName.substring(0, aimClassName.lastIndexOf('.')))) {
                            Method[] methods1 = cls.getDeclaredMethods();
                            for (Method method1 : methods1) {
                                if (method1.getName().equals(aimClassName.substring(aimClassName.lastIndexOf('.') + 1))) {
                                    //将此方法进行保存，后续一起执行
                                    afterMethod = method;
                                    haveAfter = true;
                                    aimClass = aspect;
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            //生成代理对象进行返回
            if(haveBefore || haveAfter){
                boolean finalHaveBefore = haveBefore;
                Method finalBeforeMethod = beforeMethod;
                Class finalAimClass = aimClass;
                Object finalInstance = instance;
                boolean finalHaveAfter = haveAfter;
                Method finalAfterMethod = afterMethod;
                proxyInstance = Proxy.newProxyInstance(cls.getClassLoader(), cls.getInterfaces(), new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if (finalHaveBefore) {
                            finalBeforeMethod.invoke(finalAimClass.newInstance(), null);
                        }
                        Object invoke = method.invoke(finalInstance, args);
                        if (finalHaveAfter) {
                            finalAfterMethod.invoke(finalAimClass.newInstance(), null);
                        }
                        return invoke;
                    }
                });
            }
            for (BeanPostProcessor processor : postProcessors) {

                Object currentBean = processor.postProcessAfterInitialization(instance, beanName);
                if(!Objects.isNull(currentBean)){
                    instance = currentBean;
                }
            }

            if(proxyInstance != null){
                return proxyInstance;
            }
            return instance;
        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

    public Object getBean(String className){
        return this.getBean(className, Object.class);
    }

    public <T> T getBean(String beanName, Class<T> cls){
        BeanDefinition beanDefinition = this.beanDefinitionMap.get(beanName);
        if (Objects.isNull(beanDefinition)){
            throw new RuntimeException("找不到bean");
        }else if("singleton".equals(beanDefinition.getScope())){
            return (T)getBeanFromSingletonObjects(beanName);
        }else{
            return (T)createBean(beanName, beanDefinition);
        }
    }

    public Object getBeanFromSingletonObjects(String beanName){
        return this.singletonObjects.get(beanName);
    }

    public void setAttributesByAuto(Class cls, Object obj){
        Field[] fields = cls.getDeclaredFields();
        Object bean = null;
        for (Field field : fields) {
            //注意要进行暴力反射
            field.setAccessible(true);
            if (field.isAnnotationPresent(Autowired.class)) {
                //拿到字段的名字
                String fieldName = field.getName();
                //与beanDefinitionMap中的bean进行匹配
                BeanDefinition beanDefinition = beanDefinitionMap.get(fieldName);
                if(Objects.isNull(beanDefinition)){
                    //名字匹配不上，进行类型匹配
                    ConcurrentHashMap.KeySetView<String, BeanDefinition> keySet = beanDefinitionMap.keySet();
                    for (String beanName : keySet) {
                        BeanDefinition definition = beanDefinitionMap.get(beanName);
                        Class clazz = definition.getClazz();
                        boolean hasOk = false;
                        if(clazz.equals(field.getType())){
                            bean = this.createBean(beanName, definition);
                            hasOk = true;
                        }
                        if(!hasOk){
                            for (Class<?> aClass : clazz.getInterfaces()) {
                                if(field.getType().equals(aClass)){
                                    bean = this.createBean(beanName, definition);
                                    break;
                                }
                            }
                        }
                    }
                    if(bean == null){
                        //无法自动装配，抛出异常
                        throw new RuntimeException("找不到属性进行自动装配");
                    }
                }else if("singleton".equals(beanDefinition.getScope())){
                    bean = getBeanFromSingletonObjects(fieldName);
                }else{
                    bean = this.createBean(fieldName, beanDefinition);
                }
                try {
                    field.set(obj, bean);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
}
