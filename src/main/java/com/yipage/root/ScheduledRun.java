package com.yipage.root;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class ScheduledRun {
    @Autowired
    StringRedisTemplate redisTemplate;

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledRun.class);

    private static final String LOCK = "job-lock";
    private static final String KEY = "task:" + ScheduledRun.class.getName() + ".key";

    /*
        @Scheduled(cron = "0 0/10 * * * ?")     // 每隔10分钟
        @Scheduled(cron = "0 10 0 * * ?")       // 每天凌晨00:00
        @Scheduled(cron = "0 0 9 * * ?")        // 每天 9：00
        @Scheduled(cron = "0 0 9 ? * MON")      // 每周一 9：00
        @Scheduled(cron = "0 0 9 1 * ?")        // 每月1号 9：00
        @Scheduled(cron = "0 0 0 1 1 ?")        // 每年1月1日 00:00
     */


    /**
     * 分布式锁示例
     */
//    @Scheduled(cron = "0 0 0 * * ?")
//    private void cron() {
//        boolean lock = false;
//        BoundValueOperations<String, String> valueOperations = null;
//        try {
//            valueOperations = redisTemplate.boundValueOps(KEY);
//            lock = valueOperations.setIfAbsent(LOCK);
//            if (!lock) {
//                LOGGER.info("没有获取到锁，不执行任务!");
//                return;
//            }
//            // 这里放业务代码
//        } catch (Exception e) {
//            LOGGER.error("方法执行失败！" + e);
//        } finally {
//            if (lock) {
//                valueOperations.expire(60, TimeUnit.MINUTES);
//                LOGGER.info("任务结束，释放锁!");
//            } else {
//                LOGGER.info("没有获取到锁，无需释放锁!");
//            }
//        }
//    }

}
