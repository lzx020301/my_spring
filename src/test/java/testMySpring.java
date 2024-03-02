import com.object.spring.component.Dao;
import com.object.spring.component.MonsterDao;
import com.object.spring.component.MonsterService;
import com.object.spring.component.Service;
import com.object.spring.config.SpringConfig;
import com.object.spring.ioc.ApplicationContext;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.Arrays;

/**
 * @author Object
 * @version 1.0
 * @date 2024/3/1
 */
public class testMySpring {
    @Test
    public void test(){
        ApplicationContext context = new ApplicationContext(SpringConfig.class);
        Dao monster = context.getBean("monster", Dao.class);
        monster.hi();
        System.out.println("ok");
    }

    @Test
    public void test2() throws ClassNotFoundException {
        Class<?> cls = Class.forName("com.object.spring.component.MonsterService");

        Field[] declaredFields = cls.getDeclaredFields();
        for (Field field : declaredFields) {
            System.out.println(field.getType());
        }
    }
}
