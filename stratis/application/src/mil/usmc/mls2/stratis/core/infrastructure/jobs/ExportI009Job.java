package mil.usmc.mls2.stratis.core.infrastructure.jobs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mil.usmc.mls2.stratis.core.service.gcss.I009ShipmentReceiptService;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@DisallowConcurrentExecution
public class ExportI009Job implements Job {

  @Value("${stratis.jobs.i009Export.enabled}")
  private boolean i009ExportJobEnabled;

  private final I009ShipmentReceiptService i009ShipmentReceiptService;

  @Override
  public void execute(JobExecutionContext context) {
    if (!i009ExportJobEnabled) {
      log.debug("I009 Export Job Disabled");
      return;
    }

    try {
      i009ShipmentReceiptService.processShipmentReceipt();
    }
    catch (Exception e) {
      log.error("Error occurred while processing ExportI009StandardJob", e);
      try {
        log.debug("Sleep 30s");
        Thread.sleep(30000); //an error occurred - sleep 30 seconds before re-trying.
      }
      catch (InterruptedException ie) {
        //noop
      }
    }
  }
}
