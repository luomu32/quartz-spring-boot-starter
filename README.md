# Spring Boot Quartz Integration Extension
this extension base on spring boot quartz autoconfiguration.so make sure add spring boot dependency into your project first.
## Example
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
- Spring Boot 1.5+