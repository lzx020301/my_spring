package com.object.spring.ioc;

/**
 * @author Object
 * @version 1.0
 * @date 2024/3/2
 */
public interface BeanPostProcessor {
    default Object postProcessBeforeInitialization(Object bean, String beanName){
        return bean;
    }

    default Object postProcessAfterInitialization(Object bean, String beanName){
        return bean;
    }
}
