package xyz.luomu32.quartz;

import org.quartz.Trigger;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

public class TriggerFactory implements FactoryBean<Trigger>, InitializingBean {

    private Trigger trigger;

    private String cron;

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
        if (null == cron)
            throw new IllegalArgumentException("cron can not be null");

        CronTriggerImpl trigger = new CronTriggerImpl();
        trigger.setCronExpression(this.cron);
        trigger.setJobName(this.jobName);
        trigger.setName(this.jobName);
        this.trigger = trigger;
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
}
