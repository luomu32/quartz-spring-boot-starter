package xyz.luomu32.quartz;

import org.quartz.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.core.io.Resource;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
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
        String basePackage = StringUtils.arrayToDelimitedString(AutoConfigurationPackages.get(this.beanFactory).toArray(), ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS);

        String classResourcePath = "classpath*:" + ClassUtils.convertClassNameToResourcePath(this.applicationContext.getEnvironment().resolveRequiredPlaceholders(basePackage)) + "/**/*.class";

        BeanNameGenerator beanNameGenerator = new AnnotationBeanNameGenerator();

        CachingMetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory();

        try {
            Resource[] resources = this.applicationContext.getResources(classResourcePath);
            for (Resource resource : resources) {
                MetadataReader reader = metadataReaderFactory.getMetadataReader(resource);

                String[] interfaces = reader.getClassMetadata().getInterfaceNames();

                if (reader.getAnnotationMetadata().hasAnnotation(SchedulerJob.class.getName())
                        && matchJobInterface(interfaces)) {

                    String beanClassName = reader.getClassMetadata().getClassName();

                    Map<String, ?> attr = reader.getAnnotationMetadata().getAnnotationAttributes(SchedulerJob.class.getName());

                    String name = StringUtils.isEmpty(attr.get("name")) ? beanClassName : attr.get("name").toString();

                    Object recover = attr.get("recover");
                    Object storeDurably = attr.get("storeDurably");
                    Object cron = attr.get("cron");
                    Object repeatCount = attr.get("repeatCount");
                    Object interval = attr.get("interval");

                    RootBeanDefinition jobDetailBd = new RootBeanDefinition(JobDetailFactory.class);
                    MutablePropertyValues propertyValues = new MutablePropertyValues();
                    propertyValues.add("jobClass", beanClassName);
                    propertyValues.add("jobName", name);
                    propertyValues.add("recovery", recover);
                    propertyValues.add("storeDurability", storeDurably);
                    jobDetailBd.setPropertyValues(propertyValues);

                    String jobBeanName = beanNameGenerator.generateBeanName(jobDetailBd, registry);
                    registry.registerBeanDefinition(jobBeanName, jobDetailBd);
                    LOGGER.info("register job,name: {},class:{},recover:{},durably:{}", name, beanClassName, recover, storeDurably);

                    RootBeanDefinition triggerDb = new RootBeanDefinition(TriggerFactory.class);
                    MutablePropertyValues triggerPropertyValues = new MutablePropertyValues();
                    triggerPropertyValues.add("cron", cron);
                    triggerPropertyValues.add("jobName", name);
                    triggerPropertyValues.add("repeatCount", repeatCount);
                    triggerPropertyValues.add("interval", interval);
                    triggerDb.setPropertyValues(triggerPropertyValues);

                    String triggerBeanName = beanNameGenerator.generateBeanName(triggerDb, registry);
                    registry.registerBeanDefinition(triggerBeanName, triggerDb);
                    LOGGER.info("register trigger for job:{},cron:{}", name, cron);
                }
            }

        } catch (IOException e) {
            throw new BeanDefinitionStoreException("I/O failure during classpath scanning", e);
        }
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
