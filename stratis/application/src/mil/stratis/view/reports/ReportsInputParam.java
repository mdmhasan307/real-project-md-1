package mil.stratis.view.reports;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.model.services.SysAdminImpl;
import mil.stratis.view.admin.AdminBackingHandler;
import mil.stratis.view.util.JSFUtils;
import mil.usmc.mls2.stratis.common.domain.exception.StratisRuntimeException;
import mil.usmc.mls2.stratis.core.service.multitenancy.DateService;
import mil.usmc.mls2.stratis.core.utility.AdfLogUtility;
import mil.usmc.mls2.stratis.core.utility.ContextUtils;
import mil.usmc.mls2.stratis.core.utility.DateConstants;
import oracle.adf.view.rich.component.rich.data.RichTable;
import oracle.adf.view.rich.component.rich.input.RichInputText;
import oracle.adf.view.rich.context.AdfFacesContext;
import oracle.adf.view.rich.event.DialogEvent;
import org.apache.commons.lang3.StringUtils;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.validator.ValidatorException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
public class ReportsInputParam extends AdminBackingHandler {

  private String selectedReportName;
  private ReportsInfo reportsInfoBean;
  private transient SysAdminImpl service = null;

  // Run Date Lookup
  private List<RunDateLOV> runDateList;
  private List<ImportDateLOV> importDateList;
  private List<TransactionDateLOV> transactionDateList;
  ReportsInfo reportsInfoInstance;

  //Component Binding of af:Table in bean
  private transient RichTable runDateTableBind;
  private transient RichTable importDateTableBind;
  private transient RichTable transactionDateTableBind;

  public ReportsInputParam() {
    super();
    if (reportsInfoInstance == null) {
      FacesContext fctx = FacesContext.getCurrentInstance();
      Application application = fctx.getApplication();
      ExpressionFactory expressionFactory = application.getExpressionFactory();
      ELContext context = fctx.getELContext();
      ValueExpression createValueExpression =
          expressionFactory.createValueExpression(context, "#{ReportsInfoBean}", ReportsInfo.class);
      reportsInfoInstance = (ReportsInfo) createValueExpression.getValue(context);
    }
    this.selectedReportName = reportsInfoInstance.getTargetReportName();

    switch (selectedReportName) {
      case "Recon Report Summary":
      case "Onhand Serviceable Qty Details":
      case "Onhand Un-Serviceable Qty Details":
      case "Onhand Serial Controlled Serviceable Qty Details":
      case "Onhand Serial Controlled Un-Serviceable Qty Details":
        runDateList = new ArrayList<>();
        populateRunDateLOVTable();
        break;
      case "File Import Logs Report (DASF)":
      case "File Import Logs Report (GABF)":
      case "File Import Logs Report (GBOF)":
      case "File Import Logs Report (MATS)":
      case "File Import Logs Report (MHIF)":
        importDateList = new ArrayList<>();
        populateImportDateLOVTable();
        break;
      case "Interface Transactions":
        transactionDateList = new ArrayList<>();
        populateTransactionDateLOVTable();
        break;
      default:
        break;
    }
  }

  public void setRunDateTableBind(RichTable runDateTableBind) {
    this.runDateTableBind = runDateTableBind;
  }

  public RichTable getRunDateTableBind() {
    return runDateTableBind;
  }

  public void setImportDateTableBind(RichTable importDateTableBind) {
    this.importDateTableBind = importDateTableBind;
  }

  public RichTable getImportDateTableBind() {
    return importDateTableBind;
  }

  public void setTransactionDateTableBind(RichTable transactionDateTableBind) {
    this.transactionDateTableBind = transactionDateTableBind;
  }

  public RichTable getTransactionDateTableBind() {
    return transactionDateTableBind;
  }

  public void setSelectedReportName(String selectedReportName) {
    this.selectedReportName = selectedReportName;
  }

  public String getSelectedReportName() {
    return selectedReportName;
  }

  public void setReportsInfoBean(ReportsInfo reportsInfoBean) {
    this.reportsInfoBean = reportsInfoBean;
  }

  public ReportsInfo getReportsInfoBean() {
    return reportsInfoBean;
  }

  public void validateInputParameters(@SuppressWarnings("all") ActionEvent actionEvent) {
    if (reportsInfoBean.getTargetReportName().equals("Recon Report Summary") ||
        reportsInfoBean.getTargetReportName().equals("Onhand Serviceable Qty Details") ||
        reportsInfoBean.getTargetReportName().equals("Onhand Un-Serviceable Qty Details") ||
        reportsInfoBean.getTargetReportName().equals("Onhand Serial Controlled Serviceable Qty Details") ||
        reportsInfoBean.getTargetReportName().equals("Onhand Serial Controlled Un-Serviceable Qty Details")) {
      if (reportsInfoBean.getRunDate() == null || reportsInfoBean.getRunDate().equals("")) {
        log.debug("=========  ZSL ============== input parameter: Run Date  is null");
        reportsInfoBean.setReportNavigationFlag("GoReports_InputParam");
        this.showErrorMessage("Run Date");
      }
      else {
        reportsInfoBean.setReportNavigationFlag("GoReports_QueryResult");
      }
    }
    else {
      reportsInfoBean.setReportNavigationFlag("GoReports_QueryResult");
    }
  }

  public String showErrorMessage(String inputField) {
    String messageText = inputField + " is required";
    FacesMessage fm = new FacesMessage(messageText);
    fm.setSeverity(FacesMessage.SEVERITY_ERROR);
    FacesContext context = FacesContext.getCurrentInstance();
    context.addMessage(null, fm);
    return null;
  }

  public String goToNextPage() {
    return reportsInfoBean.getReportNavigationFlag();
  }

  public void setRunDateList(List<RunDateLOV> runDateList) {
    this.runDateList = Collections.unmodifiableList(runDateList);
  }

  public List<RunDateLOV> getRunDateList() {
    return Collections.unmodifiableList(runDateList);
  }

  public void setImportDateList(List<ImportDateLOV> importDateList) {
    this.importDateList = Collections.unmodifiableList(importDateList);
  }

  public List<ImportDateLOV> getImportDateList() {
    return Collections.unmodifiableList(importDateList);
  }

  public void setTransactionDateList(List<TransactionDateLOV> transactionDateList) {
    this.transactionDateList = Collections.unmodifiableList(transactionDateList);
  }

  public List<TransactionDateLOV> getTransactionDateList() {
    return Collections.unmodifiableList(transactionDateList);
  }

  public void getRunDateSelected(DialogEvent event) {
    if (event.getOutcome().equals(DialogEvent.Outcome.ok)) {
      String comId;
      switch (selectedReportName) {
        case "Recon Report Summary":
          comId = "it6";
          break;
        case "Onhand Serviceable Qty Details":
          comId = "it7";
          break;
        case "Onhand Un-Serviceable Qty Details":
          comId = "it8";
          break;
        case "Onhand Serial Controlled Serviceable Qty Details":
          comId = "it9";
          break;
        case "Onhand Serial Controlled Un-Serviceable Qty Details":
          comId = "it10";
          break;
        default:
          comId = null;
          break;
      }
      String rDate = ((RunDateLOV) runDateTableBind.getSelectedRowData()).getRunDate();
      log.debug("RunDateLOV selected -   {}", rDate);
      reportsInfoBean.setRunDate(rDate);
      AdfFacesContext.getCurrentInstance().addPartialTarget(FacesContext.getCurrentInstance().getViewRoot().findComponent(comId));
    }
  }

  public void getImportDateSelected(DialogEvent event) {
    if (event.getOutcome().equals(DialogEvent.Outcome.ok)) {
      String comId;
      switch (selectedReportName) {
        case "File Import Logs Report (DASF)":
          comId = "it11";
          break;
        case "File Import Logs Report (GABF)":
          comId = "it12";
          break;
        case "File Import Logs Report (GBOF)":
          comId = "it13";
          break;
        case "File Import Logs Report (MATS)":
          comId = "it14";
          break;
        case "File Import Logs Report (MHIF)":
          comId = "it15";
          break;
        default:
          comId = null;
          break;
      }

      String iDate = ((ImportDateLOV) importDateTableBind.getSelectedRowData()).getImportDate();
      log.debug("ImportDateLOV selected -   {}", iDate);
      reportsInfoBean.setImportDate(iDate);

      RichInputText input = (RichInputText) JSFUtils.findComponentInRoot(comId);
      input.resetValue();
      input.setValue(reportsInfoBean.getImportDate());

      AdfFacesContext.getCurrentInstance().addPartialTarget(input);
    }
  }

  public void getTransactionDateSelected(DialogEvent event) {
    if (event.getOutcome().equals(DialogEvent.Outcome.ok)) {
      String comId = "it16_8";

      String tDate = ((TransactionDateLOV) transactionDateTableBind.getSelectedRowData()).getTransactionDate();
      log.debug("TransactionDateLOV selected -   {}", tDate);
      reportsInfoBean.setTransactionDate(tDate);

      RichInputText input = (RichInputText) JSFUtils.findComponentInRoot(comId);
      input.resetValue();
      input.setValue(reportsInfoBean.getTransactionDate());

      AdfFacesContext.getCurrentInstance().addPartialTarget(input);
    }
  }

  public final void populateRunDateLOVTable() {
    if (service == null) {
      service = getSysAdminModule();
    }

    String sql =
        "SELECT TO_CHAR (TIMESTAMP, 'DD-MON-YYYY (DDD), hh24:mi:ss') run_dt FROM recon_run_date ORDER BY TO_DATE (run_dt, 'DD-MON-YYYY (DDD), hh24:mi:ss') DESC";

    try (PreparedStatement pstmt = service.getDBTransaction().createPreparedStatement(sql, 0)) {
      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          RunDateLOV rd = new RunDateLOV();
          rd.setRunDate(rs.getString(1));
          rd.setRunDate(rs.getString(1));
          runDateList.add(rd);
        }
      }
    }
    catch (SQLException e) {
      AdfLogUtility.logException(e);
    }
  }

  /**
   * The queries to the view uses a timeshift parameter now to shift the dates in the database to match the display dates of the system
   * so that the dates will match the users timezone.  This allows dates that are actually stored in the DB in a different timezone such as UTC
   * to timeshift and potentially show a different date on screen if the timeshift would cause the value to be a different day.
   * Example: Database in UTC with a value of 10-1-2020 02:45:00 and the user is in eastern.  Eastern the date would actually be 9-30-2020 22:45:00
   * This time shift would allow the user to see 9-30-2020 as an option instead of 10-1-2020.
   */
  public final void populateImportDateLOVTable() {
    if (service == null) {
      service = getSysAdminModule();
    }
    val dateService = ContextUtils.getBean(DateService.class);
    val timeshiftForDb = dateService.getDatabaseVsDisplayTimeZoneShiftInSecondsForOracle();
    String sql;
    switch (selectedReportName) {
      case "File Import Logs Report (DASF)":
        sql =
            "select distinct to_char(CREATED_DATE + ?,'dd-MON-yyyy') FROM SGA.V_IMPORTS_FILE_LOG_DASF_RPT order by 1 desc";
        break;
      case "File Import Logs Report (GABF)":
        sql =
            "select distinct to_char(CREATED_DATE + ?,'dd-MON-yyyy') FROM SGA.V_IMPORTS_FILE_LOG_GABF_RPT order by 1 desc";
        break;
      case "File Import Logs Report (GBOF)":
        sql =
            "select distinct to_char(CREATED_DATE + ?,'dd-MON-yyyy') FROM SGA.V_IMPORTS_FILE_LOG_GBOF_RPT order by 1 desc";
        break;
      case "File Import Logs Report (MATS)":
        sql =
            "select distinct to_char(CREATED_DATE + ?,'dd-MON-yyyy') FROM SGA.V_IMPORTS_FILE_LOG_MATS_RPT order by 1 desc";
        break;
      case "File Import Logs Report (MHIF)":
        sql =
            "select distinct to_char(CREATED_DATE + ?,'dd-MON-yyyy') FROM SGA.V_IMPORTS_FILE_LOG_MHIF_RPT order by 1 desc";
        break;
      default:
        throw new StratisRuntimeException("Invalid report selected {}", selectedReportName);
    }
    try (PreparedStatement pstmt = service.getDBTransaction().createPreparedStatement(sql, 0)) {
      pstmt.setDouble(1, timeshiftForDb);
      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          ImportDateLOV rd = new ImportDateLOV();
          rd.setImportDate(rs.getString(1));
          importDateList.add(rd);
        }
      }
    }
    catch (SQLException e) {
      AdfLogUtility.logException(e);
    }
  }

  public final void populateTransactionDateLOVTable() {
    if (service == null) {
      service = getSysAdminModule();
    }
    //this will return a distinct list of Dates there are spool records for all shifted to the display timezone.
    String sql = "select distinct to_char(gcss_date at time zone (select site_timezone from site_info), 'dd-MON-yyyy') as convertedDate from V_SUMOUT_SPOOL_DETAIL_GCSS order by convertedDate desc";

    try (PreparedStatement pstmt = service.getDBTransaction().createPreparedStatement(sql, 0)) {
      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          TransactionDateLOV rd = new TransactionDateLOV();
          rd.setTransactionDate(rs.getString(1));
          transactionDateList.add(rd);
        }
      }
    }
    catch (SQLException e) {
      AdfLogUtility.logException(e);
    }
  }

  public void endDateValidator(@SuppressWarnings("all") FacesContext facesContext,
                               @SuppressWarnings("all") UIComponent uiComponent, Object object) {
    String transactionDate = object.toString();
    if (StringUtils.isNotEmpty(transactionDate)) {
      validateDate(transactionDate, "End Date");
    }
  }

  public void startDateValidator(@SuppressWarnings("all") FacesContext facesContext,
                                 @SuppressWarnings("all") UIComponent uiComponent, Object object) {
    String transactionDate = object.toString();
    if (StringUtils.isNotEmpty(transactionDate)) {
      validateDate(transactionDate, "Start Date");
    }
  }

  public void transactionDateValidator(@SuppressWarnings("all") FacesContext facesContext,
                                       @SuppressWarnings("all") UIComponent uiComponent, Object object) {
    String transactionDate = object.toString();
    if (StringUtils.isNotEmpty(transactionDate)) {
      validateDate(transactionDate, "Transaction Date");
    }
  }

  public void importDateValidator(@SuppressWarnings("all") FacesContext facesContext,
                                  @SuppressWarnings("all") UIComponent uiComponent, Object object) {
    String importDate = object.toString();
    if (StringUtils.isNotEmpty(importDate)) {
      validateDate(importDate, "Import Date");
    }
  }

  private void validateDate(String date, String message) {
    try {
      DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
      builder.parseCaseInsensitive();
      builder.appendPattern(DateConstants.DB_REPORT_DATE_INPUT_FORMATTER_PATTERN);
      LocalDate.parse(date, builder.toFormatter());
    }
    catch (DateTimeParseException e) {
      String msg = message + " is not in Proper Format\nDate Format: " + DateConstants.DB_REPORT_DATE_INPUT_FORMATTER_PATTERN;
      throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null));
    }
  }
}
