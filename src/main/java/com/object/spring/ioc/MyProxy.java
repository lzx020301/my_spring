package com.object.spring.ioc;

import java.lang.reflect.Method;

/**
 * @author Object
 * @version 1.0
 * @date 2024/3/2
 */
public class MyProxy {
    private Class cls;

    private Method[] methods;

    public MyProxy(Class cls, Method[] methods) {
        this.cls = cls;
        this.methods = methods;
    }


}
