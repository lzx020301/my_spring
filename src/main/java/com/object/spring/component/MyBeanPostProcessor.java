package com.object.spring.component;

import com.object.spring.annotation.Component;
import com.object.spring.ioc.BeanPostProcessor;

/**
 * @author Object
 * @version 1.0
 * @date 2024/3/2
 */
@Component
public class MyBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        System.out.println("后置处理器 before 执行，当前类名为：" + bean.getClass());
        if(bean instanceof MonsterService){
            System.out.println("当前对象是MonsterService，修改其beanName");
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        System.out.println("后置处理器 after 执行，当前类名为：" + bean.getClass());
        return bean;
    }
}
