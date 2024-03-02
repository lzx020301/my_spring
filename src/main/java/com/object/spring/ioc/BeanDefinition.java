package com.object.spring.ioc;

/**
 * @author Object
 * @version 1.0
 * @date 2024/3/1
 */
public class BeanDefinition {

    private String scope;
    private Class clazz;
    private String beanName;

    public BeanDefinition(String scope, Class clazz) {
        this.scope = scope;
        this.clazz = clazz;
    }

    public BeanDefinition(String scope, Class clazz, String beanName) {
        this.scope = scope;
        this.clazz = clazz;
        this.beanName = beanName;
    }

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }
}
