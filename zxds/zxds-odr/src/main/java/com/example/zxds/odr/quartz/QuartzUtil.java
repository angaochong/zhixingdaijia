package com.example.zxds.odr.quartz;

import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

@Component
@Slf4j
public class QuartzUtil {

    @Resource
    private Scheduler scheduler;

    /**
     * 添加定时器
     *
     * JobDetail：要执行的任务内容（做什么）
     * Trigger：任务什么时候执行（何时做）
     * Quartz 的工作就是：把 JobDetail 和 Trigger 绑定在一起，交给 Scheduler 去调度。
     *
     * jobName:任务名字
     * jobGroupName:任务组名字（可以理解为分类）
     * start：开始日期时间
     */
    public void addJob(JobDetail jobDetail, String jobName, String jobGroupName, Date start) {
        try {
            // 封装触发器对象，它代表了任务什么时候执行
            Trigger trigger = TriggerBuilder.newTrigger() // 开始构建一个触发器（相当于开始设置闹钟）
                    .withIdentity(jobName, jobGroupName) // 给触发器起名字
                    // 设置触发器的调度策略，一个简单的调度策略（不是Cron表达式那种复杂的），以及设置"错过执行"时的处理策略:错过就立即执行一次
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
                    .startAt(start)
                    .build();
            // 指定什么时候执行什么任务。
            scheduler.scheduleJob(jobDetail, trigger);
            log.debug("成功添加" + jobName + "定时器");
        } catch (SchedulerException e) {
            log.error("定时器添加失败", e);
        }
    }

    /**
     * 查询是否存在定时器
     *
     * @param jobName      任务名字
     * @param jobGroupName 任务组名字
     * @return
     */
    public boolean checkExists(String jobName, String jobGroupName) {
        // 创建一个触发器键对象，Quartz 里用 TriggerKey 来定位一个触发器
        TriggerKey triggerKey = new TriggerKey(jobName, jobGroupName);
        try {
            // 检查是否存在定时器
            boolean bool = scheduler.checkExists(triggerKey);
            return bool;
        } catch (Exception e) {
            log.error("定时器查询失败", e);
            return false;
        }
    }

    /**
     * 删除定时器
     *
     * @param jobName      任务名字
     * @param jobGroupName 任务组名字
     */
    public void deleteJob(String jobName, String jobGroupName) {
        // 创建触发器键对象，和上面的new同等效果，可互换。
        TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroupName);
        try {
            scheduler.resumeTrigger(triggerKey);
            // 取消定时器
            scheduler.unscheduleJob(triggerKey);
            // 删除定时器
            scheduler.deleteJob(JobKey.jobKey(jobName, jobGroupName));
            log.debug("成功删除" + jobName + "定时器");
        } catch (SchedulerException e) {
            log.error("定时器删除失败", e);
        }

    }
}
