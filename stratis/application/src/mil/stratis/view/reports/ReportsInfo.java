package mil.stratis.view.reports;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

@Slf4j
@Data
public class ReportsInfo implements Serializable {

  private String reportNavigationFlag;
  private String reportTotals;
  private String targetReportName;
  private String wacNumber;
  private String niin;
  private String status;
  private String expirationDate;
  private String runDate;
  private String importDate;
  private String startDate;
  private String endDate;
  private String priority;
  private String packArea;
  private String availabilityFlag;
  private String lastName;
  private String rcn;
  private String cc;
  private String transactionType;
  private String docNumber;
  private String filterRoute;
  private String filterUnitOfIssue;
  private String filterQty;
  private String filterConditionCode;
  private String transactionDate;

  private Integer dbTimeShift;

  public ReportsInfo() {
    super();
  }

  public void resetInputParameters() {
    wacNumber = "";
    niin = "";
    status = "";
    expirationDate = "";
    runDate = "";
    importDate = "";
    startDate = "";
    endDate = "";
    priority = "";
    packArea = "";
    availabilityFlag = "";
    transactionType = "";
    docNumber = "";
    filterRoute = "";
    filterUnitOfIssue = "";
    filterQty = "";
    filterConditionCode = "";
    transactionDate = "";
    lastName = "";
  }

  public String getExportReportName() throws UnsupportedEncodingException {
    String s;
    if (targetReportName != null && !targetReportName.equals("")) {
      s = targetReportName.replace('(', '_');
      s = s.replace(")", "");
      s = s.replace(' ', '_');
      s = s + ".xls";
      s = URLDecoder.decode(targetReportName + ".xls", "UTF-8");
      log.debug("decoded file name:   {}", s);
    }
    else {
      s = "Report.xls";
    }
    return s;
  }
}
