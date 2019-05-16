package xyz.luomu32.quartz;

import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import java.text.ParseException;
import java.util.Collection;

@Deprecated
public class TriggerBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

        Collection<Job> beans = beanFactory.getBeansOfType(Job.class).values();
        for (Job bean : beans) {
            Trigger trigger = bean.getClass().getAnnotation(Trigger.class);
            if (null != trigger) {

                String jobName = bean.getClass().getSimpleName();

                CronTriggerImpl cronTrigger = new CronTriggerImpl();
                cronTrigger.setName(jobName + "Trigger");
                cronTrigger.setJobName(jobName);
                try {
                    cronTrigger.setCronExpression(trigger.cron());
                } catch (ParseException e) {
                    throw new RuntimeException("quartz trigger cron " + trigger.cron() + " not valid");
                }
                beanFactory.registerSingleton(jobName, cronTrigger);

                JobDetail detail = JobBuilder.newJob()
                        .withIdentity(jobName)
                        .ofType(bean.getClass())
                        .storeDurably()
                        .build();

                beanFactory.registerSingleton(jobName + "Detail", detail);

            }
        }
    }
}
