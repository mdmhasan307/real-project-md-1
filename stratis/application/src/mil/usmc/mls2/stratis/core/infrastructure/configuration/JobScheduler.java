package mil.usmc.mls2.stratis.core.infrastructure.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.usmc.mls2.stratis.common.domain.exception.StratisRuntimeException;
import mil.usmc.mls2.stratis.core.infrastructure.jobs.ExportI009Job;
import mil.usmc.mls2.stratis.core.infrastructure.jobs.UCPStatsJob;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import static org.quartz.DateBuilder.futureDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobScheduler {

  @Value("${stratis.jobs.i009Export.frequency}")
  private int exportI009FrequencyInMillis;

  @Value("${stratis.jobs.ucp.frequency}")
  private int ucpFrequencyInMillis;

  private final Scheduler scheduler;

  @EventListener
  @SuppressWarnings("unused")
  public void onApplicationReady(ApplicationReadyEvent event) {
    log.info("initializing schedulers...");

    try {
      scheduleJobs();
    }
    catch (SchedulerException e) {
      log.error("failed to schedule a job during startup - raising exception", e);
      throw new StratisRuntimeException("Failed to schedule jobs", e);
    }
  }

  private void scheduleJobs() throws SchedulerException {
    scheduleUcpJob();
    scheduleI009Job();
  }

  public void scheduleUcpJob() throws SchedulerException {
    val jobDetail = JobBuilder.newJob(UCPStatsJob.class)
        .withIdentity("UCP Stats Quartz Job", "ucp-jobs")
        .withDescription("UCP Stats Quartz Job2")
        .build();

    val jobTrigger = TriggerBuilder.newTrigger()
        .forJob(jobDetail)
        .withIdentity(jobDetail.getKey().getName(), "ucp-triggers")
        .withDescription("UCP Job Trigger")
        .startAt(futureDate(2, DateBuilder.IntervalUnit.MINUTE))
        .withSchedule(SimpleScheduleBuilder.simpleSchedule()
            .withIntervalInMilliseconds(ucpFrequencyInMillis)
            .repeatForever()
            .withMisfireHandlingInstructionNextWithRemainingCount())
        .build();

    if (!scheduler.checkExists(jobTrigger.getJobKey())) {
      scheduler.scheduleJob(jobDetail, jobTrigger);
    }
  }

  public void scheduleI009Job() throws SchedulerException {
    val jobDetail = JobBuilder.newJob(ExportI009Job.class)
        .withIdentity("I009 Export Quartz Job", "i009-jobs")
        .withDescription("I009 Export Quartz Job2")
        .build();

    val jobTrigger = TriggerBuilder.newTrigger()
        .forJob(jobDetail)
        .withIdentity(jobDetail.getKey().getName(), "i009-triggers")
        .withDescription("i009 Job Trigger")
        .startAt(futureDate(2, DateBuilder.IntervalUnit.MINUTE))
        .withSchedule(SimpleScheduleBuilder.simpleSchedule()
            .withIntervalInMilliseconds(exportI009FrequencyInMillis)
            .repeatForever()
            .withMisfireHandlingInstructionNextWithRemainingCount())
        .build();

    if (!scheduler.checkExists(jobTrigger.getJobKey())) {
      scheduler.scheduleJob(jobDetail, jobTrigger);
    }
  }
}
