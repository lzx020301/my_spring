package com.object.spring.component;

import com.object.spring.annotation.Component;
import com.object.spring.annotation.Scope;

/**
 * @author Object
 * @version 1.0
 * @date 2024/3/1
 */
@Component(value = "monster")
@Scope
public class MonsterDao implements Dao{

    public void hi(){
        System.out.println("dao hi ...");
    }
}
