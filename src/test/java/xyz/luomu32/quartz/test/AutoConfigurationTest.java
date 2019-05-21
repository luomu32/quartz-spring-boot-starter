package xyz.luomu32.quartz.test;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import xyz.luomu32.quartz.QuartzJobAutoConfiguration;
import xyz.luomu32.quartz.SchedulerJobAnnotationBeanPostProcessor;

import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ImportAutoConfiguration(classes = QuartzJobAutoConfiguration.class)
@TestPropertySource(properties = "spring.quartz.basePackage=xyz.luomu32.quartz.test")
public class AutoConfigurationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private SimpleTrigger simpleTrigger;
    private CronTrigger cronTrigger;


    @Test
    public void test() {
        Assert.assertNotNull(applicationContext.getBean(SchedulerJobAnnotationBeanPostProcessor.class));
    }


    @Test
    public void testSimpleTrigger() throws SchedulerException {
        Assert.assertNotNull(simpleTrigger);
        Assert.assertEquals(0, simpleTrigger.getRepeatCount());
        Assert.assertEquals(0, simpleTrigger.getRepeatInterval());
    }

    public void testCronTrigger() {
        Assert.assertNotNull(cronTrigger);
        Assert.assertEquals("0 0/1 * * * ?", cronTrigger.getCronExpression());
    }

    @Test
    public void testJob() {
        Map<String, JobDetail> jobs = this.applicationContext.getBeansOfType(JobDetail.class);
        Assert.assertEquals(2, jobs.size());

    }


}
