package xyz.luomu32.quartz;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SchedulerJob {

    String name() default "";

    String cron() default "";

    int repeatCount() default -1;

    /**
     * unit milliseconds
     */
    int interval() default -1;

    boolean storeDurably() default false;

    /**
     * only work for cluster mode
     */
    boolean recover() default false;
}
