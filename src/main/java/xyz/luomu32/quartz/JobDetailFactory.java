package xyz.luomu32.quartz;

import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import javax.annotation.PostConstruct;

public class JobDetailFactory implements FactoryBean<JobDetail>, InitializingBean {

    private JobDetail jobDetail;

    private Class<? extends Job> jobClass;

    private String jobName;

    private boolean storeDurability;

    private boolean recovery;


    @Override
    public JobDetail getObject() throws Exception {
        return this.jobDetail;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (null == jobClass)
            throw new IllegalArgumentException("job class can not be null");

        if (null == jobName)
            throw new IllegalArgumentException("job name can not be null");

        this.jobDetail = JobBuilder
                .newJob()
                .withIdentity(this.jobName)
                .storeDurably(this.storeDurability)
                .ofType(this.jobClass)
                .requestRecovery(this.recovery) //任务支持故障转移
                .build();
    }
    
    @Override
    public Class<?> getObjectType() {
        return JobDetail.class;
    }

    public Class<? extends Job> getJobClass() {
        return jobClass;
    }

    public void setJobClass(Class<? extends Job> jobClass) {
        this.jobClass = jobClass;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public boolean isStoreDurability() {
        return storeDurability;
    }

    public void setStoreDurability(boolean storeDurability) {
        this.storeDurability = storeDurability;
    }

    public boolean isRecovery() {
        return recovery;
    }

    public void setRecovery(boolean recovery) {
        this.recovery = recovery;
    }
}
