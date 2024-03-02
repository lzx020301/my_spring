package com.object.spring.component;

import com.object.spring.annotation.After;
import com.object.spring.annotation.Aspect;
import com.object.spring.annotation.Before;
import com.object.spring.annotation.Component;

import java.util.Date;

/**
 * @author Object
 * @version 1.0
 * @date 2024/3/2
 */
@Aspect
@Component
public class PrintLog implements LogUtils{

    @Before(value = "com.object.spring.component.MonsterService.m1")
    public void doLog(){
        System.out.println("当前时间为" + new Date());
    }

    @After(value = "com.object.spring.component.MonsterDao.hi")
    public void after(){
        System.out.println("当前方法执行结束");
    }
}
