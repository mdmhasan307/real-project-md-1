package mil.usmc.mls2.stratis.core.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;

@Slf4j
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Spool {

  private Integer spoolId;
  private String spoolDefMode;
  private String priority;
  private OffsetDateTime timestamp;
  private String transactionType;
  private String recData;
  private String xml;
  private String status;
  private Integer spoolBatchNum;
  private OffsetDateTime timestampSent;
  private Integer rcn;
  private String recallFlag;
  private OffsetDateTime recallDate;
  private String correctionFlag;
  private Integer niinId;
  private Long refSpoolId;
  private Integer modById;
  private String documentNumber;
  private String suffix;
  private String sid;

  public void updateToInProgress() {
    status = "INPROGRESS";
  }

  public void updateToComplete(Integer spoolBatch) {
    status = "COMPLETE";
    timestampSent = OffsetDateTime.now();
    spoolBatchNum = spoolBatch;
  }

  public void updateToReady(Long referencedSpoolId, String updatedXml) {
    status = "READY";
    refSpoolId = referencedSpoolId;
    xml = updatedXml;
  }
}
