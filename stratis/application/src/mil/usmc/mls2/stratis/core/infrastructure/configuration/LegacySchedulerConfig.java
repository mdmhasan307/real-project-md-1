package mil.usmc.mls2.stratis.core.infrastructure.configuration;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.autoconfigure.quartz.JobStoreType;
import org.springframework.boot.autoconfigure.quartz.QuartzProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Configuration of Quartz-based Scheduled Tasks
 * Legacy Quartz jobs will not be stored in the database.
 */
@Slf4j
@Configuration
@Order(2)
@Profile(Profiles.LEGACY)
class LegacySchedulerConfig {

  @Bean("stratisQuartzScheduler")
  public SchedulerFactoryBean quartzScheduler(AutowiringJobFactory autowiringJobFactory, QuartzProperties quartzProperties) {
    //remove the datasource related properties and update the jobStore.class back to the RamJobStore
    quartzProperties.getProperties().remove("org.quartz.jobStore.dataSource");
    quartzProperties.getProperties().remove("org.quartz.jobStore.driverDelegateClass");
    quartzProperties.getProperties().remove("org.quartz.jobStore.tablePrefix");
    quartzProperties.getProperties().remove("org.quartz.jobStore.class");
    quartzProperties.getProperties().remove("org.quartz.jobStore.isClustered");
    quartzProperties.getProperties().put("org.quartz.jobStore.class", "org.quartz.simpl.RAMJobStore");

    val properties = new Properties();
    properties.putAll(quartzProperties.getProperties());
    //this overrides the jobstoreType from application.yml to be MEMORY as Legacy runs quartz jobs in memory only.
    quartzProperties.setJobStoreType(JobStoreType.MEMORY);

    log.info("*** Quartz Properties: {}", properties.entrySet().stream().map(e -> e.getKey() + ":" + e.getValue()).collect(Collectors.joining(",")));

    val factory = new SchedulerFactoryBean();
    factory.setJobFactory(autowiringJobFactory);
    factory.setAutoStartup(quartzProperties.isAutoStartup());
    factory.setApplicationContextSchedulerContextKey("applicationContext");
    factory.setWaitForJobsToCompleteOnShutdown(quartzProperties.isWaitForJobsToCompleteOnShutdown());
    factory.setOverwriteExistingJobs(quartzProperties.isOverwriteExistingJobs());
    factory.setSchedulerName(quartzProperties.getSchedulerName());
    factory.setQuartzProperties(properties);

    return factory;
  }
}
