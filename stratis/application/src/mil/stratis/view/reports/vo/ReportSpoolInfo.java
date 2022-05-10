package mil.stratis.view.reports.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
public class ReportSpoolInfo {

  private OffsetDateTime timeStamp;
  private String transactionType;
  private String statusCode;
  private int qty;
  private String documentNumber;
  private String unitOfIssue;
  private String conditionCode;
  private String priority;
  private long spoolId;
  private long niinId;
  private String spoolDefMode;
  private OffsetDateTime timeStampSent;
  private long spoolBatchNum;
  private String rcn;
  private String scn;
}
