package xyz.luomu32.quartz;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SchedulerJob {

    String name() default "";

    //TODO 支持简单的触发机制
    String cron() default "";

    //是否持久化
    boolean storeDurably() default false;

    //故障转移，仅当集群模式有效
    boolean recover() default false;
}
