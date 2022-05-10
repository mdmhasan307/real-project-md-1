package mil.usmc.mls2.stratis.modules.mats.application.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * NiinsProcessed: represents successfully processed niins.
 */
@Data
@Accessors(fluent = true)
public class MatsProcessResult {

  private int gcssmcDataImportsId;
  private int totalRecordsFromGcss;
  private int refMatsRecordsCreated;
  private int refMatsRecordsFailed;
  private int errorQueueCreated;
  private int customersCreated;
  private int issuesCreated;
  private int picksCreated;
  private int pickRefusalsCreated;
  private int recordFailures;

  private boolean success;
  private boolean seeLog;

  private List<String> messages = new ArrayList<>();

  public void incrementCustomersCreated() {
    customersCreated++;
  }

  public void incrementErrorQueueCreated() {
    errorQueueCreated++;
  }

  public void incrementRefMatsRecordsCreated() {
    refMatsRecordsCreated++;
  }

  public void incrementRefMatsRecordsFailed() {
    refMatsRecordsFailed++;
  }

  public void incrementIssuesCreated() {
    issuesCreated++;
  }

  public void incrementPicksCreated() {
    picksCreated++;
  }

  public void incrementPickRefusalsCreated() {
    pickRefusalsCreated++;
  }

  public void incrementRecordFailures() {
    recordFailures++;
  }

  public void addMessage(String message) {
    messages.add(message);
  }

  public String returnStatisticsAlways() {
    return String.format(" Results for GCSSMC_DATA_IMPORT_ID: %s [MATS file records: %s MATS records Processed: %s Error Queue Created: %s Customers Created: %s Issues Created: %s Picks Created: %s Pick Refusals Created: %s Record Processing Failures: %s]", gcssmcDataImportsId(), totalRecordsFromGcss(), refMatsRecordsCreated(), errorQueueCreated(), customersCreated(), issuesCreated(), picksCreated(), pickRefusalsCreated(), recordFailures());
  }

  public String returnStatistics() {
    if (success) return returnStatisticsAlways();
    return String.format("MATS Processing Failed for GCSSMC_DATA_IMPORT_ID: %s, see logs for details", gcssmcDataImportsId());
  }
}
