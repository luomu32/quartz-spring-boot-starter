# Spring Boot Quartz Integration Extension
this extension base on spring boot quartz autoconfiguration.so make sure add spring boot dependency into your project first.
## Example
base on CronTrigger,will be executed by every minute.
```java
@SchedulerJob(cron = "0 0/1 * * * ?")
public class FooJob implements Job {
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        //do some logic...
    }
}
```
base on SimpleTrigger,will be executed 10 times and each time interval 20 second.
```java
@SchedulerJob(repeatCount = 10,interval = 20000)
public class FooJob implements Job {
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        //do some logic...
    }
}
```
## Requirements
- Java 6+
- Spring Boot 2.0.0+
- Quartz 2.0.0+