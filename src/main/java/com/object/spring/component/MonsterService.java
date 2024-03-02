package com.object.spring.component;

import com.object.spring.annotation.Autowired;
import com.object.spring.annotation.Component;

/**
 * @author Object
 * @version 1.0
 * @date 2024/3/1
 */
@Component(value = "service100")
public class MonsterService implements Service{
    @Autowired
    private Dao monsterDao;

    public void m1(){
        monsterDao.hi();
    }
}
