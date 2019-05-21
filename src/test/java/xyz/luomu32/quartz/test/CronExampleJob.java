package xyz.luomu32.quartz.test;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import xyz.luomu32.quartz.SchedulerJob;

@SchedulerJob(cron = "0 0/1 * * * ?", storeDurably = true)
public class CronExampleJob implements Job {
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

    }
}
