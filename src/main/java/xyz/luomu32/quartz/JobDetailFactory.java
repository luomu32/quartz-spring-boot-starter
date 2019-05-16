package xyz.luomu32.quartz;

import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.springframework.beans.factory.FactoryBean;

import javax.annotation.PostConstruct;

public class JobDetailFactory implements FactoryBean<JobDetail> {

    private JobDetail jobDetail;

    private Class<? extends Job> jobClass;

    private String jobName;

    private boolean storeDurability;

    private boolean recovery;


    @Override
    public JobDetail getObject() throws Exception {
        return this.jobDetail;
    }

    @PostConstruct
    public void init() {
        if (null == jobClass)
            throw new IllegalArgumentException("JobClass can not be null");

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
