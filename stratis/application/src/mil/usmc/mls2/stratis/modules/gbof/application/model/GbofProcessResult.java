package mil.usmc.mls2.stratis.modules.gbof.application.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(fluent = true)

public class GbofProcessResult {

  private int gcssmcDataImportsId;
  private int totalRecordsFromGcss;
  private int refGbofRecordsCreated;
  private int refGbofRecordsFailed;
  private int recordsSkipped;

  private int refGbofSerialRecordsCreated;
  private int refGbofSerialRecordsFailed;

  private boolean success;

  private List<String> messages = new ArrayList<>();

  public void incrementRefGbofSerialRecordsCreated() {
    refGbofSerialRecordsCreated++;
  }

  public void incrementRefGbofSerialRecordsFailed() {
    refGbofSerialRecordsFailed++;
  }

  public void incrementRefGbofRecordsCreated() {
    refGbofRecordsCreated++;
  }

  public void incrementRefGbofRecordsFailed() {
    refGbofRecordsFailed++;
  }

  public void incrementRecordSkipped() {
    recordsSkipped++;
  }

  public void addMessage(String message) {
    messages.add(message);
  }

  public String returnStatisticsAlways() {
    return String.format(" Results for GCSSMC_DATA_IMPORT_ID: %s [Gbof File Records: %s Records Created: %s Records Failed: %s Records Skipped: %s]",
        gcssmcDataImportsId(), totalRecordsFromGcss(), refGbofRecordsCreated(), refGbofRecordsFailed(), recordsSkipped());
  }

  public String returnStatistics() {
    if (success) return returnStatisticsAlways();
    return String.format("GBOF Processing Failed for GCSSMC_DATA_IMPORT_ID: %s, see logs for details", gcssmcDataImportsId());
  }
}
