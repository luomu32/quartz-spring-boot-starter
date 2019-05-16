package xyz.luomu32.quartz;

import org.quartz.Scheduler;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

@Configuration
@ConditionalOnClass({Scheduler.class, SchedulerFactoryBean.class, QuartzAutoConfiguration.class})
@AutoConfigureAfter({QuartzAutoConfiguration.class})
public class QuartzJobAutoConfiguration {

    @Bean
    public BeanDefinitionRegistryPostProcessor jobAnnoattionBeanPostProcessor() {
        return new SchedulerJobAnnotationBeanPostProcessor();
    }
}
