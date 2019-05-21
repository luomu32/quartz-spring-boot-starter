package xyz.luomu32.quartz.test;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import xyz.luomu32.quartz.SchedulerJob;

@SchedulerJob(storeDurably = true)
public class SimpleExampleJob implements Job {

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

    }
}
