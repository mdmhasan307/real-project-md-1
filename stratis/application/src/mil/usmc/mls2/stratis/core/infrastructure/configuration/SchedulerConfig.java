package mil.usmc.mls2.stratis.core.infrastructure.configuration;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.quartz.QuartzProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import javax.sql.DataSource;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Configuration of Quartz-based Scheduled Tasks (cluster friendly)
 * Integration Quartz jobs will be stored in the STRATIS COMMON database.
 */
@Slf4j
@Configuration
@Order(2)
@Profile(Profiles.INTEGRATION_ANY)
class SchedulerConfig {

  @Bean("stratisQuartzScheduler") // <-- so named to not conflict with other Quartz Schedulers within the same JVM (ex: clc2s, mls2feeds)
  public SchedulerFactoryBean quartzScheduler(AutowiringJobFactory autowiringJobFactory, QuartzProperties quartzProperties, @Qualifier("commonDataSource") DataSource dataSource) {
    val properties = new Properties();
    properties.putAll(quartzProperties.getProperties());

    log.info("*** quartzScheduler: Quartz Properties [{}]", properties.entrySet().stream().map(e -> e.getKey() + ":" + e.getValue()).collect(Collectors.joining(",")));

    val factory = new SchedulerFactoryBean();
    factory.setApplicationContextSchedulerContextKey("applicationContext");
    factory.setAutoStartup(quartzProperties.isAutoStartup());
    factory.setDataSource(dataSource);
    factory.setJobFactory(autowiringJobFactory);
    factory.setOverwriteExistingJobs(quartzProperties.isOverwriteExistingJobs());
    factory.setQuartzProperties(properties);
    factory.setSchedulerName(quartzProperties.getSchedulerName());
    factory.setWaitForJobsToCompleteOnShutdown(quartzProperties.isWaitForJobsToCompleteOnShutdown());

    return factory;
  }
}
