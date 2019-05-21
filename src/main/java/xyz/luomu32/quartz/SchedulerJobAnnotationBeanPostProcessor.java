package xyz.luomu32.quartz;

import org.quartz.Job;
import org.quartz.JobKey;
import org.quartz.impl.JobDetailImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.*;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Map;

public class SchedulerJobAnnotationBeanPostProcessor implements BeanDefinitionRegistryPostProcessor, BeanFactoryAware, ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerJobAnnotationBeanPostProcessor.class);

    private BeanFactory beanFactory;
    private ApplicationContext applicationContext;

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        String basePackage = this.applicationContext.getEnvironment().getProperty("spring.quartz.basePackage");
        if (StringUtils.isEmpty(basePackage))
            basePackage = StringUtils.arrayToDelimitedString(AutoConfigurationPackages.get(this.beanFactory).toArray(), ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS);


        String classResourcePath = "classpath*:" + ClassUtils.convertClassNameToResourcePath(this.applicationContext.getEnvironment().resolveRequiredPlaceholders(basePackage)) + "/**/*.class";

        BeanNameGenerator beanNameGenerator = new DefaultBeanNameGenerator();

        CachingMetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory();

        try {
            Resource[] resources = this.applicationContext.getResources(classResourcePath);
            for (Resource resource : resources) {
                MetadataReader reader = metadataReaderFactory.getMetadataReader(resource);

                String[] interfaces = reader.getClassMetadata().getInterfaceNames();

                if (reader.getAnnotationMetadata().hasAnnotation(SchedulerJob.class.getName())
                        && matchJobInterface(interfaces)) {

                    String jobClassName = reader.getClassMetadata().getClassName();

                    Map<String, ?> attr = reader.getAnnotationMetadata().getAnnotationAttributes(SchedulerJob.class.getName());

                    String jobName = StringUtils.isEmpty(attr.get("name")) ? jobClassName : attr.get("name").toString();

                    Object recover = attr.get("recover");
                    Object storeDurably = attr.get("storeDurably");
                    String cron = attr.get("cron").toString();
                    int repeatCount = Integer.parseInt(attr.get("repeatCount").toString());
                    int interval = Integer.parseInt(attr.get("interval").toString());

                    registerJob(jobClassName, jobName, recover, storeDurably, registry, beanNameGenerator);

                    registerTrigger(jobName, jobClassName, cron, interval, repeatCount, beanNameGenerator, registry);
                }
            }

        } catch (IOException e) {
            throw new BeanDefinitionStoreException("I/O failure during classpath scanning", e);
        }
    }

    private void registerJob(String jobClassName,
                             String jobName,
                             Object recover,
                             Object storeDurably,
                             BeanDefinitionRegistry registry,
                             BeanNameGenerator beanNameGenerator) {
        RootBeanDefinition jobDefinition = new RootBeanDefinition(JobDetailFactoryBean.class);

        MutablePropertyValues propertyValues = new MutablePropertyValues();
        propertyValues.add("jobClass", jobClassName);
        propertyValues.add("name", jobName);
        propertyValues.add("requestsRecovery", recover);
        propertyValues.add("durability", storeDurably);

        jobDefinition.setPropertyValues(propertyValues);

        String jobBeanName = beanNameGenerator.generateBeanName(jobDefinition, registry);

        registry.registerBeanDefinition(jobBeanName, jobDefinition);

        LOGGER.info("register job,name: {},class:{},recover:{},durably:{}", jobBeanName, jobClassName, recover, storeDurably);
    }

    private void registerTrigger(String jobName,
                                 String jobClassName,
                                 String cron,
                                 int interval,
                                 int repeatCount,
                                 BeanNameGenerator beanNameGenerator,
                                 BeanDefinitionRegistry registry) {
        RootBeanDefinition triggerDefinition = new RootBeanDefinition();

        MutablePropertyValues triggerPropertyValues = new MutablePropertyValues();
        triggerPropertyValues.add("name", jobName + "Trigger");

        JobDetailImpl jobDetail = new JobDetailImpl();
        jobDetail.setKey(new JobKey(jobName));
        triggerPropertyValues.add("jobDetail", jobDetail);
        if (StringUtils.isEmpty(cron)) {
            triggerDefinition.setBeanClass(SimpleTriggerFactoryBean.class);
            triggerPropertyValues.add("repeatInterval", interval);
            triggerPropertyValues.add("repeatCount", repeatCount);
        } else {
            triggerDefinition.setBeanClass(CronTriggerFactoryBean.class);
            triggerPropertyValues.add("cronExpression", cron);
        }

        triggerDefinition.setPropertyValues(triggerPropertyValues);

        String triggerBeanName = beanNameGenerator.generateBeanName(triggerDefinition, registry);

        registry.registerBeanDefinition(triggerBeanName, triggerDefinition);

        LOGGER.info("register trigger for job:{},cron:{},interval:{},repeatCount:{}", jobClassName, cron, interval, repeatCount);
    }

    private boolean matchJobInterface(String[] interfaces) {
        for (String interfaceClass : interfaces) {
            if (interfaceClass.equals(Job.class.getName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
