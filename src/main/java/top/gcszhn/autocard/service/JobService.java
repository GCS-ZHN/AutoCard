/* 
 * Copyright © 2022 <a href="mailto:zhang.h.n@foxmail.com">Zhang.H.N</a>.
 * Release under GPL License
 */
package top.gcszhn.autocard.service;

import java.io.IOException;
import java.util.Date;

import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import org.springframework.stereotype.Service;

import top.gcszhn.autocard.utils.LogUtils;

/**
 * 定时任务的服务
 * @author Zhang.H.N
 * @version 1.1
 */
@Service
public class JobService implements AppService {
    /**任务调度器 */
    private Scheduler scheduler;
    /**初始化任务服务，创建任务调度器 */
    public JobService() throws SchedulerException  {
        SchedulerFactory factory = new StdSchedulerFactory();
        scheduler = factory.getScheduler();
    }
    /**
     * 添加任务，并采用默认定时策略
     * @param jobClass Job接口实现类
     * @param cronExpression cron表达式定时
     * @param jobDataMap 任务参数
     */
    public void addJob(Class<? extends Job> jobClass,String cronExpression, JobDataMap jobDataMap) {
        addJob(jobClass, cronExpression, new Date(), jobDataMap);
    }
    /**
     * 添加指定定时策略的任务
     * @param jobClass Job接口实现类
     * @param cronExpression cron表达式，决定定时规律
     * @param triggerStartTime 定时启动时间，指的是从什么时候开始定时，并非定时任务执行的时间
     * @param jobDataMap 任务参数
     */
    public void addJob(Class<? extends Job> jobClass, String cronExpression, Date triggerStartTime, JobDataMap jobDataMap) {
        try {
            JobDetail jobDetail = JobBuilder.newJob(jobClass)
                .usingJobData(jobDataMap)
                .build();
            Trigger trigger = TriggerBuilder.newTrigger()
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                .startAt(triggerStartTime)
                .build();
            scheduler.scheduleJob(jobDetail, trigger);
            LogUtils.printMessage("Timed job has been injected with cron expression " + cronExpression);
        } catch (Exception e) {
            LogUtils.printMessage(null, e, LogUtils.Level.ERROR);
        }
    }
    /**
     * 开始所有任务
     */
    public void start() {
        try {
            scheduler.start();
            LogUtils.printMessage("Start job scheduler...");
        } catch (SchedulerException e) {
            LogUtils.printMessage(null, e, LogUtils.Level.ERROR);
        }
    }
    @Override
    public void close() throws IOException {
        try {
            if (!scheduler.isShutdown()) scheduler.shutdown();
            LogUtils.printMessage("Shutdown job scheduler...");
        } catch (Exception e) {
            LogUtils.printMessage(null, e, LogUtils.Level.ERROR);
        }
    }
}
