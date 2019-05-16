package xyz.luomu32.quartz;

import org.quartz.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.*;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.StringUtils;

import java.util.LinkedHashSet;
import java.util.Set;

public class SchedulerJobAnnotationBeanPostProcessor implements BeanDefinitionRegistryPostProcessor, BeanFactoryAware {


    private BeanFactory beanFactory;

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {

        TriggerBeanDefinitionScanner scanner = new TriggerBeanDefinitionScanner(registry);
        String basePackage = StringUtils.arrayToDelimitedString(AutoConfigurationPackages.get(this.beanFactory).toArray(), ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS);


        scanner.addIncludeFilter(new AnnotationTypeFilter(SchedulerJob.class));

//        scanner.addIncludeFilter(new TypeFilter() {
//            @Override
//            public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
//
//                return metadataReader.getAnnotationMetadata().isAnnotated(Trigger.class.getName()) &&
//                        implJobInterface(metadataReader.getClassMetadata().getInterfaceNames());
//            }
//        });

        scanner.scan(basePackage);
    }

    private boolean implJobInterface(String[] interfaces) {
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

    public class TriggerBeanDefinitionScanner extends ClassPathBeanDefinitionScanner {

        private final Logger log = LoggerFactory.getLogger(TriggerBeanDefinitionScanner.class);

        private ScopeMetadataResolver scopeMetadataResolver = new AnnotationScopeMetadataResolver();
        private BeanNameGenerator beanNameGenerator = new AnnotationBeanNameGenerator();

        private BeanDefinitionRegistry registry;

        public TriggerBeanDefinitionScanner(BeanDefinitionRegistry registry) {
            super(registry);
            this.registry = registry;
        }

        @Override
        protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
            Set<BeanDefinitionHolder> beanDefinitions = new LinkedHashSet<>();
            for (String basePackage : basePackages) {
                Set<BeanDefinition> candidates = findCandidateComponents(basePackage);
                for (BeanDefinition candidate : candidates) {
//                    System.out.println(candidate);
                    ScopeMetadata scopeMetadata = this.scopeMetadataResolver.resolveScopeMetadata(candidate);
                    candidate.setScope(scopeMetadata.getScopeName());
                    String beanName = this.beanNameGenerator.generateBeanName(candidate, registry);
                    if (candidate instanceof AbstractBeanDefinition) {
                        postProcessBeanDefinition((AbstractBeanDefinition) candidate, beanName);
                    }
                    if (candidate instanceof AnnotatedBeanDefinition) {
                        AnnotationConfigUtils.processCommonDefinitionAnnotations((AnnotatedBeanDefinition) candidate);
                    }
                    if (checkCandidate(beanName, candidate)) {

//                        System.out.println(beanName);
//                        System.out.println(candidate.getBeanClassName());
//                        System.out.println(candidate.getSource());
//                        System.out.println(candidate.getClass());
//                        BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(candidate, beanName);
//                        System.out.println(definitionHolder);
//                        definitionHolder =
//                                AnnotationConfigUtils.applyScopedProxyMode(scopeMetadata, definitionHolder, this.registry);
//                        beanDefinitions.add(definitionHolder);
//                        registerBeanDefinition(definitionHolder, this.registry);

                        SchedulerJob jobDesc = null;
                        try {
                            jobDesc = ((AbstractBeanDefinition) candidate).resolveBeanClass(this.getResourceLoader().getClassLoader())
                                    .getAnnotation(SchedulerJob.class);
                        } catch (ClassNotFoundException e) {
                            log.warn("job class '" + candidate.getBeanClassName() + "' not found");
                            continue;
                        }
                        String name = StringUtils.isEmpty(jobDesc.name()) ? candidate.getBeanClassName() : jobDesc.name();

                        RootBeanDefinition jobDetailBd = new RootBeanDefinition(JobDetailFactory.class);
                        MutablePropertyValues propertyValues = new MutablePropertyValues();
                        propertyValues.add("jobClass", candidate.getBeanClassName());
                        propertyValues.add("jobName", name);
                        propertyValues.add("recovery", jobDesc.recover());
                        propertyValues.add("storeDurability", jobDesc.storeDurably());
                        jobDetailBd.setPropertyValues(propertyValues);
                        BeanDefinitionHolder holder = new BeanDefinitionHolder(jobDetailBd, beanName);
                        registerBeanDefinition(holder, this.registry);
                        beanDefinitions.add(holder);


                        RootBeanDefinition triggerDb = new RootBeanDefinition(TriggerFactory.class);
                        MutablePropertyValues triggerPropertyValues = new MutablePropertyValues();
                        triggerPropertyValues.add("cron", jobDesc.cron());
                        triggerPropertyValues.add("jobName", name);

                        triggerDb.setPropertyValues(triggerPropertyValues);

                        BeanDefinitionHolder triggerHolder = new BeanDefinitionHolder(triggerDb, "trigger");
                        registerBeanDefinition(triggerHolder, this.registry);
                        beanDefinitions.add(triggerHolder);

                    }
                }
            }
            return beanDefinitions;
        }
    }
}
