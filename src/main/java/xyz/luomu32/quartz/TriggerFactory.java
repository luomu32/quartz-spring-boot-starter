package xyz.luomu32.quartz;

import org.quartz.CronScheduleBuilder;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

public class TriggerFactory implements FactoryBean<Trigger>, InitializingBean {

    private Trigger trigger;

    private String cron;

    private int repeatCount;

    private int interval;

    private String jobName;

    @Override
    public Trigger getObject() throws Exception {
        return this.trigger;
    }

    @Override
    public Class<?> getObjectType() {
        return Trigger.class;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (null == jobName)
            throw new IllegalArgumentException("job name can not be null");

        TriggerBuilder builder = TriggerBuilder
                .newTrigger()
                .forJob(this.jobName)
                .withIdentity(this.jobName);

        if (null != cron) {
            builder.withSchedule(CronScheduleBuilder.cronSchedule(this.cron));
        } else {
            if (-1 == repeatCount) {
                builder.withSchedule(SimpleScheduleBuilder.repeatSecondlyForever(interval));
            } else {
                builder.withSchedule(SimpleScheduleBuilder.repeatSecondlyForTotalCount(repeatCount, interval));
            }
        }

        this.trigger = builder.build();
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public int getRepeatCount() {
        return repeatCount;
    }

    public void setRepeatCount(int repeatCount) {
        this.repeatCount = repeatCount;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }
}
