package com.object.spring.config;

import com.object.spring.annotation.ComponentScan;
import com.object.spring.ioc.Config;

/**
 * @author Object
 * @version 1.0
 * @date 2024/3/1
 */
@ComponentScan(value = {"com.object.spring.component"})
public class SpringConfig implements Config {
}
