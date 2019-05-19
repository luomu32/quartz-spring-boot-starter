# Spring Boot Quartz Integration Extension
this extension base on spring boot quartz autoconfiguration.so make sure add spring boot dependency into your project first.
## example
```java
@SchedulerJob(cron = "0 0/1 * * * ?")
public class FooJob implements Job {
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        //do some logic...
    }
}
```
```java
@SchedulerJob(repeatCount = 10,interval = 20)
public class FooJob implements Job {
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        //do some logic...
    }
}
```