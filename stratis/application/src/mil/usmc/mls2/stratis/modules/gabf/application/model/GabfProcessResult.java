package mil.usmc.mls2.stratis.modules.gabf.application.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(fluent = true)

public class GabfProcessResult {

  private int gcssmcDataImportsId;
  private int totalRecordsFromGcss;
  private int refGabfRecordsCreated;
  private int refGabfRecordsUpdated;
  private int refGabfRecordsFailed;
  private int recordsSkipped;

  private int refGabfSerialRecordsCreated;
  private int refGabfSerialRecordsFailed;

  private boolean success;

  private List<String> messages = new ArrayList<>();

  public void incrementRefGabfSerialRecordsCreated() {
    refGabfSerialRecordsCreated++;
  }

  public void incrementRefGabfSerialRecordsFailed() {
    refGabfSerialRecordsFailed++;
  }

  public void incrementRefGabfRecordsCreated() {
    refGabfRecordsCreated++;
  }

  public void incrementRefGabfRecordsUpdated() {
    refGabfRecordsUpdated++;
  }

  public void incrementRefGabfRecordsFailed() {
    refGabfRecordsFailed++;
  }

  public void incrementRecordSkipped() {
    recordsSkipped++;
  }

  public void addMessage(String message) {
    messages.add(message);
  }

  public String returnStatisticsAlways() {
    return String.format(" Results for GCSSMC_DATA_IMPORT_ID: %s [GABF File Records: %s Records Created: %s Records Updated: %s Records Failed: %s Records Skipped: %s Serial Records Created: %s Serial Records Failed: %s]",
        gcssmcDataImportsId(), totalRecordsFromGcss(), refGabfRecordsCreated(), refGabfRecordsUpdated(), refGabfRecordsFailed(), recordsSkipped(), refGabfSerialRecordsCreated(), refGabfSerialRecordsFailed());
  }

  public String returnStatistics() {
    if (success) return returnStatisticsAlways();

    return String.format("GABF Processing Failed for GCSSMC_DATA_IMPORT_ID: %s, see logs for details", gcssmcDataImportsId());
  }
}
