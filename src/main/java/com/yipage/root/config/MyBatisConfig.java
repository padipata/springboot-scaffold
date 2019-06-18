package com.yipage.root.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis配置类
 * Created by root on 2019/4/8.
 */
@Configuration
@MapperScan({"com.yipage.root.mbg.mapper","com.yipage.root.dao"})
public class MyBatisConfig {
}
