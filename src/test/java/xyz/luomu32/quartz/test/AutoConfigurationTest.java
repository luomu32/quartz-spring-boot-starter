package xyz.luomu32.quartz.test;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.core.QuartzScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;
import xyz.luomu32.quartz.SchedulerJobAnnotationBeanPostProcessor;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Config.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class QuartTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private SimpleTrigger trigger;


    @Test
    public void test() {
        Assert.assertNotNull(applicationContext.getBean(SchedulerJobAnnotationBeanPostProcessor.class));
    }


    @Test
    public void testJob() throws SchedulerException {
        Assert.assertNotNull(trigger);

    }



}
