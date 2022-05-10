package mil.usmc.mls2.stratis.modules.dasf.application.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * NiinsProcessed: represents successfully processed niins.
 */
@Data
@Accessors(fluent = true)
public class DasfProcessResult {

  private int gcssmcDataImportsId;
  private int totalRecordsFromGcss;
  private int refDasfRecordsCreated;
  private int refDasfRecordsFailed;
  private int recordFailures;
  private int niinsLookedUpInGcssMhif;
  private int niinsLoadedFromGcssMhif;

  private boolean success;
  private boolean seeLog;

  private List<String> messages = new ArrayList<>();

  public void incrementRefDasfRecordsCreated() {
    refDasfRecordsCreated++;
  }

  public void incrementRefDasfRecordsFailed() {
    refDasfRecordsFailed++;
  }

  public void incrementRecordFailures() {
    recordFailures++;
  }

  public void incrementNiinsLoadedFromGcssMhif() {
    niinsLoadedFromGcssMhif++;
  }

  public void addMessage(String message) {
    messages.add(message);
  }

  public String returnStatisticsAlways() {
    return String.format(" Results for GCSSMC_DATA_IMPORT_ID: %s [DASF file records: %s DASF records Processed: %s " +
        "Record Processing Failures: %s NIINs Looked up via MHIF %s NIINs found via MHIF %s]", gcssmcDataImportsId(), totalRecordsFromGcss(), refDasfRecordsCreated(), recordFailures(), niinsLookedUpInGcssMhif(), niinsLoadedFromGcssMhif());
  }

  public String returnStatistics() {
    if (success) return returnStatisticsAlways();
    return String.format("DASF Processing Failed for GCSSMC_DATA_IMPORT_ID: %s, see logs for details", gcssmcDataImportsId());
  }
}
