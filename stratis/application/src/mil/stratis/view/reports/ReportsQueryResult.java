package mil.stratis.view.reports;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.common.util.Util;
import mil.stratis.model.services.SysAdminImpl;
import mil.stratis.view.admin.AdminBackingHandler;
import mil.usmc.mls2.stratis.common.domain.model.SortOrder;
import mil.usmc.mls2.stratis.core.domain.model.reports.*;
import mil.usmc.mls2.stratis.core.service.multitenancy.DateService;
import mil.usmc.mls2.stratis.core.service.reports.*;
import mil.usmc.mls2.stratis.core.utility.AdfLogUtility;
import mil.usmc.mls2.stratis.core.utility.ContextUtils;
import mil.usmc.mls2.stratis.core.utility.DateConstants;
import oracle.jdbc.OracleCallableStatement;
import oracle.jdbc.OracleTypes;
import org.apache.commons.lang3.StringUtils;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.application.Application;
import javax.faces.context.FacesContext;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@SuppressWarnings("Duplicates")
public class ReportsQueryResult extends AdminBackingHandler {

  private static final char DLTR = ',';

  // Access ReportInfo bean
  private ReportsInfo reportsInfoBean;

  //ArrayList to populate data in af:table
  private List<ReportColumnBean> reportColumnList = new ArrayList<>();
  private List<ReconReportColumnBean> reconReportColumnList = new ArrayList<>();
  private List<StdReportColumnBean> stdReportColumnList = new ArrayList<>();

  ReportsInfo reportsInfoInstance;

  private static final String FILE_IMPORT_LOGS_REPORT_MATS = "File Import Logs Report (MATS)";
  private static final String FILE_IMPORT_LOGS_REPORT_GABF = "File Import Logs Report (GABF)";
  private static final String FILE_IMPORT_LOGS_REPORT_GBOF = "File Import Logs Report (GBOF)";
  private static final String FILE_IMPORT_LOGS_REPORT_DASF = "File Import Logs Report (DASF)";
  private static final String FILE_IMPORT_LOGS_REPORT_MHIF = "File Import Logs Report (MHIF)";
  private static final String RECEIPT_HISTORY_REPORT = "Receipt History Report";
  private static final String INTERFACE_TRANSACTION_REPORT = "Interface Transactions";
  private static final String INVENTORY_HISTORY_REPORT = "Inventory History Report";
  private static final String EMPLOYEE_HISTORY_REPORT = "Employee Workload History";

  public ReportsQueryResult() {
    val service = getSysAdminModule();

    try {
      if (reportsInfoInstance == null) {
        FacesContext fctx = FacesContext.getCurrentInstance();
        Application application = fctx.getApplication();
        ExpressionFactory expressionFactory = application.getExpressionFactory();
        ELContext context = fctx.getELContext();
        ValueExpression createValueExpression =
            expressionFactory.createValueExpression(context, "#{ReportsInfoBean}", ReportsInfo.class);
        reportsInfoInstance = (ReportsInfo) createValueExpression.getValue(context);
      }

      val isReportConverted = processAsConvertedReport();
      if (!isReportConverted) {
        val sql = getSqlStatement();
        try (CallableStatement st = service.getDBTransaction().createCallableStatement(sql, service.getDBTransaction().DEFAULT)) {
          st.registerOutParameter(1, Types.INTEGER);
          st.registerOutParameter(2, OracleTypes.CURSOR);
          st.registerOutParameter(3, Types.VARCHAR);
          setInputParameters(st);
          st.execute();

          val result = st.getInt(1);
          if (result >= 0) {
            try (ResultSet rs = ((OracleCallableStatement) st).getCursor(2)) {
              populateResultSet(rs);
            }
          }
          else {
            log.error("Error code returned - {} ({})", result, st.getString(3));
          }
        }
      }
    }
    catch (SQLException e) {
      AdfLogUtility.logException(e);
    }
  }

  private boolean processAsConvertedReport() {
    switch (reportsInfoInstance.getTargetReportName()) {
      case FILE_IMPORT_LOGS_REPORT_MATS:
        processMatsReport();
        break;
      case FILE_IMPORT_LOGS_REPORT_MHIF:
        processMhifReport();
        break;
      case FILE_IMPORT_LOGS_REPORT_GBOF:
        processGbofReport();
        break;
      case FILE_IMPORT_LOGS_REPORT_GABF:
        processGabfReport();
        break;
      case FILE_IMPORT_LOGS_REPORT_DASF:
        processDasfReport();
        break;
      case RECEIPT_HISTORY_REPORT:
        processReceiptHistoryReport();
        break;
      case INTERFACE_TRANSACTION_REPORT:
        processInterfaceTransactionReport();
        break;
      case INVENTORY_HISTORY_REPORT:
        processInventoryHistoryReport();
        break;
      case EMPLOYEE_HISTORY_REPORT:
        processEmployeeHistoryReport();
        break;
      default:
        return false;
    }
    return true;
  }

  private void processEmployeeHistoryReport() {
    val employeeWorkloadHistoryRptViewService = ContextUtils.getBean(EmployeeWorkloadHistoryRptViewService.class);
    val criteria = EmployeeWorkloadHistoryRptViewSearchCriteria.builder()
        .startDate(getStartDate(reportsInfoInstance.getStartDate(), false))
        .endDate(getEndDate(reportsInfoInstance.getEndDate(), false))
        .user(reportsInfoInstance.getNiin())
        .build();

    criteria.setSort("id.user", SortOrder.ASC);
    stdReportColumnList = employeeWorkloadHistoryRptViewService.searchForAdfReport(criteria);
    reportsInfoInstance.setReportTotals(Integer.toString(stdReportColumnList.size()));
  }

  private void processInventoryHistoryReport() {
    val inventoryHistoryRptViewService = ContextUtils.getBean(InventoryHistoryRptViewService.class);
    val criteria = InventoryHistoryRptViewSearchCriteria.builder()
        .startDate(getStartDate(reportsInfoInstance.getStartDate(), false))
        .endDate(getEndDate(reportsInfoInstance.getEndDate(), false))
        .niin(reportsInfoInstance.getNiin())
        .wac(reportsInfoInstance.getWacNumber())
        .build();

    criteria.setSort("niin, nomenclature, locationLabel", SortOrder.ASC);
    reportColumnList = inventoryHistoryRptViewService.searchForAdfReport(criteria);
    reportsInfoInstance.setReportTotals(Integer.toString(reportColumnList.size()));
  }

  private void processInterfaceTransactionReport() {
    val interfaceTransactionRptViewService = ContextUtils.getBean(InterfaceTransactionRptViewService.class);
    val criteria = InterfaceTransactionRptViewSearchCriteria.builder()
        .cc(reportsInfoInstance.getFilterConditionCode())
        .docNumber(reportsInfoInstance.getDocNumber())
        .transactionType(reportsInfoInstance.getTransactionType())
        .route(reportsInfoInstance.getFilterRoute())
        .niin(reportsInfoInstance.getNiin())
        .qty(StringUtils.isEmpty(reportsInfoInstance.getFilterQty()) ? null : Integer.parseInt(reportsInfoInstance.getFilterQty()))
        .ui(reportsInfoInstance.getFilterUnitOfIssue())
        .startDate(getStartDate(reportsInfoInstance.getTransactionDate(), true))
        .endDate(getEndDate(reportsInfoInstance.getTransactionDate(), true))
        .build();

    criteria.setSort("timestamp", SortOrder.DESC);
    stdReportColumnList = interfaceTransactionRptViewService.searchForAdfReport(criteria);
    reportsInfoInstance.setReportTotals(Integer.toString(stdReportColumnList.size()));
  }

  private void processReceiptHistoryReport() {
    val receiptHistRptViewService = ContextUtils.getBean(ReceiptHistRptViewService.class);
    val criteria = ReceiptHistRptViewSearchCriteria.builder()
        .startDate(getStartDate(reportsInfoInstance.getStartDate(), false))
        .endDate(getEndDate(reportsInfoInstance.getEndDate(), false))
        .niin(reportsInfoInstance.getNiin())
        .build();
    if (StringUtils.isNotEmpty(reportsInfoInstance.getRcn())) criteria.setRcn(Integer.valueOf(reportsInfoInstance.getRcn()));

    criteria.setSort("id.rcn, id.sid", SortOrder.ASC);
    reportColumnList = receiptHistRptViewService.searchForAdfReport(criteria);
    reportsInfoInstance.setReportTotals(Integer.toString(reportColumnList.size()));
  }

  private void processMatsReport() {
    val importFileLogMatsRptViewService = ContextUtils.getBean(ImportFileLogMatsRptViewService.class);
    val criteria = ImportFileLogMatsRptViewSearchCriteria.builder()
        .startDate(getStartDate(reportsInfoInstance.getImportDate(), true))
        .endDate(getEndDate(reportsInfoInstance.getImportDate(), true))
        .build();
    criteria.setSort("createdDate, lineNumber", SortOrder.DESC);

    reportColumnList = importFileLogMatsRptViewService.searchForAdfReport(criteria);
    reportsInfoInstance.setReportTotals(Integer.toString(reportColumnList.size()));
  }

  private void processMhifReport() {
    val importFileLogMhifRptViewService = ContextUtils.getBean(ImportFileLogMhifRptViewService.class);
    val criteria = ImportFileLogMhifRptViewSearchCriteria.builder()
        .startDate(getStartDate(reportsInfoInstance.getImportDate(), true))
        .endDate(getEndDate(reportsInfoInstance.getImportDate(), true))
        .build();
    criteria.setSort("createdDate, lineNumber", SortOrder.DESC);

    reportColumnList = importFileLogMhifRptViewService.searchForAdfReport(criteria);
    reportsInfoInstance.setReportTotals(Integer.toString(reportColumnList.size()));
  }

  private void processGabfReport() {
    val importFileLogGabfRptViewService = ContextUtils.getBean(ImportFileLogGabfRptViewService.class);
    val criteria = ImportFileLogGabfRptViewSearchCriteria.builder()
        .startDate(getStartDate(reportsInfoInstance.getImportDate(), true))
        .endDate(getEndDate(reportsInfoInstance.getImportDate(), true))
        .build();
    criteria.setSort("createdDate, lineNumber", SortOrder.DESC);

    reportColumnList = importFileLogGabfRptViewService.searchForAdfReport(criteria);
    reportsInfoInstance.setReportTotals(Integer.toString(reportColumnList.size()));
  }

  private void processGbofReport() {
    val importFileLogGbofRptViewService = ContextUtils.getBean(ImportFileLogGbofRptViewService.class);
    val criteria = ImportFileLogGbofRptViewSearchCriteria.builder()
        .startDate(getStartDate(reportsInfoInstance.getImportDate(), true))
        .endDate(getEndDate(reportsInfoInstance.getImportDate(), true))
        .build();
    criteria.setSort("createdDate, lineNumber", SortOrder.DESC);

    reportColumnList = importFileLogGbofRptViewService.searchForAdfReport(criteria);
    reportsInfoInstance.setReportTotals(Integer.toString(reportColumnList.size()));
  }

  private void processDasfReport() {
    val importFileLogDasfRptViewService = ContextUtils.getBean(ImportFileLogDasfRptViewService.class);
    val criteria = ImportFileLogDasfRptViewSearchCriteria.builder()
        .startDate(getStartDate(reportsInfoInstance.getImportDate(), true))
        .endDate(getEndDate(reportsInfoInstance.getImportDate(), true))
        .build();
    criteria.setSort("createdDate, lineNumber", SortOrder.DESC);

    reportColumnList = importFileLogDasfRptViewService.searchForAdfReport(criteria);
    reportsInfoInstance.setReportTotals(Integer.toString(reportColumnList.size()));
  }

  public void setReportColumnList(List<ReportColumnBean> reportColumnList) {
    this.reportColumnList = reportColumnList;
  }

  public List<ReportColumnBean> getReportColumnList() {
    return reportColumnList;
  }

  public void setReconReportColumnList(List<ReconReportColumnBean> reconReportColumnList) {
    this.reconReportColumnList = reconReportColumnList;
  }

  public List<ReconReportColumnBean> getReconReportColumnList() {
    return reconReportColumnList;
  }

  public void setStdReportColumnList(List<StdReportColumnBean> stdReportColumnList) {
    this.stdReportColumnList = stdReportColumnList;
  }

  public List<StdReportColumnBean> getStdReportColumnList() {
    return stdReportColumnList;
  }

  public void setReportsInfoBean(ReportsInfo reportsInfoBean) {
    this.reportsInfoBean = reportsInfoBean;
  }

  public ReportsInfo getReportsInfoBean() {
    return reportsInfoBean;
  }

  private void setWorkloadDetailPickPackShipParameters(CallableStatement st) throws SQLException {
    st.setInt(4, 155);
    st.setString(5, reportsInfoInstance.getStatus() == null ? "" : reportsInfoInstance.getStatus());
    st.setString(6, reportsInfoInstance.getNiin() == null ? "" : reportsInfoInstance.getNiin());
    st.setString(7, reportsInfoInstance.getWacNumber() == null ? "" : reportsInfoInstance.getWacNumber());
    st.setString(8, reportsInfoInstance.getDocNumber() == null ? "" : reportsInfoInstance.getDocNumber());
    st.setString(9, reportsInfoInstance.getPriority() == null ? "" : reportsInfoInstance.getPriority());
  }

  private void setWorkloadDetailStowingParameters(CallableStatement st) throws SQLException {
    st.setInt(4, 156);
    st.setString(5, reportsInfoInstance.getWacNumber() == null ? "" : reportsInfoInstance.getWacNumber());
    st.setString(6, reportsInfoInstance.getPackArea() == null ? "" : reportsInfoInstance.getPackArea());
  }

  private void setWorkloadDetailExpiringNiinsParameters(CallableStatement st) throws SQLException {
    st.setInt(4, 157);
    st.setString(5, reportsInfoInstance.getNiin() == null ? "" : reportsInfoInstance.getNiin());
    st.setString(6, reportsInfoInstance.getWacNumber() == null ? "" : reportsInfoInstance.getWacNumber());
    st.setString(7,
        reportsInfoInstance.getExpirationDate() == null ? "" : reportsInfoInstance.getExpirationDate());
  }

  private void setWorkloadDetailNsnUpdatesParameters(CallableStatement st) throws SQLException {
    st.setInt(4, 158);
    st.setString(5, reportsInfoInstance.getWacNumber() == null ? "" : reportsInfoInstance.getWacNumber());
    st.setString(6, reportsInfoInstance.getNiin() == null ? "" : reportsInfoInstance.getNiin());
  }

  private void setWorkloadDetailPendingInventoriesParameters(CallableStatement st) throws SQLException {
    st.setInt(4, 159);
    st.setString(5, reportsInfoInstance.getWacNumber() == null ? "" : reportsInfoInstance.getWacNumber());
    st.setString(6, reportsInfoInstance.getNiin() == null ? "" : reportsInfoInstance.getNiin());
    st.setString(7, reportsInfoInstance.getPackArea() == null ? "" : reportsInfoInstance.getPackArea());
  }

  private void setReconParameters(CallableStatement st) throws SQLException {
    st.setInt(4, 106);
    st.setString(5, reportsInfoInstance.getRunDate() == null ? "" : reportsInfoInstance.getRunDate());
  }

  private void setOnhandServiceableQtyParameters(CallableStatement st) throws SQLException {
    st.setInt(4, 109);
    st.setString(5, reportsInfoInstance.getRunDate() == null ? "" : reportsInfoInstance.getRunDate());
    st.setString(6, reportsInfoInstance.getNiin() == null ? "" : reportsInfoInstance.getNiin());
  }

  private void setOnhandUnserviceableQtyParameters(CallableStatement st) throws SQLException {
    st.setInt(4, 110);
    st.setString(5, reportsInfoInstance.getRunDate() == null ? "" : reportsInfoInstance.getRunDate());
    st.setString(6, reportsInfoInstance.getNiin() == null ? "" : reportsInfoInstance.getNiin());
  }

  private void setOnhandSerialControlledServiceableQtyParameters(CallableStatement st) throws SQLException {
    st.setInt(4, 115);
    st.setString(5, reportsInfoInstance.getRunDate() == null ? "" : reportsInfoInstance.getRunDate());
    st.setString(6, reportsInfoInstance.getNiin() == null ? "" : reportsInfoInstance.getNiin());
  }

  private void setOnhandSerialControlledUnserviceableQtyParameters(CallableStatement st) throws SQLException {
    st.setInt(4, 116);
    st.setString(5, reportsInfoInstance.getRunDate() == null ? "" : reportsInfoInstance.getRunDate());
    st.setString(6, reportsInfoInstance.getNiin() == null ? "" : reportsInfoInstance.getNiin());
  }

  //09-22-2020 Courier report not active in user interface.
  // But FSR's say Courier processing is still technically needed. So not removing this for the time being.
  private void setCourierFileImportLogParameters(CallableStatement st) throws SQLException {
    st.setInt(4, 34);
    st.setString(5,
        reportsInfoInstance.getImportDate() == null ? "" : reportsInfoInstance.getImportDate());
  }

  private void setDasfParameters(CallableStatement st) throws SQLException {
    st.setInt(4, 181);
    st.setString(5, reportsInfoInstance.getNiin() == null ? "" : reportsInfoInstance.getNiin());
    st.setString(6, reportsInfoInstance.getDocNumber() == null ? "" : reportsInfoInstance.getDocNumber());
  }

  private void setActiveReceiptParameters(CallableStatement st) throws SQLException {
    st.setInt(4, 104);
    st.setString(5, reportsInfoInstance.getRcn() == null ? "" : reportsInfoInstance.getRcn());
    st.setString(6, reportsInfoInstance.getNiin() == null ? "" : reportsInfoInstance.getNiin());
  }

  //if the date is null, or 01-JAN-9999 return either a null or "now" depending on returnNull
  private LocalDate getDateForReport(String date, boolean returnNull) {
    if (StringUtils.isEmpty(date) || date.equals(DateConstants.REPORT_CURRENT_DAY_DEFAULT_DISPLAY_VALUE)) {
      if (returnNull) return null;
      return LocalDate.now();
    }
    else {
      //The date comes in like 11-SEP-2020 (all caps for the month.  Standard date formatting is expecting 'Sep'.  So need to build
      //the formatter, and specify parseCaseInsensitive to allow for SEP to convert.
      DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
      builder.parseCaseInsensitive();
      builder.appendPattern(DateConstants.DB_REPORT_DATE_INPUT_FORMATTER_PATTERN);
      return LocalDate.parse(date, builder.toFormatter());
    }
  }

  private OffsetDateTime getStartDate(String date, boolean returnNull) {
    val dateService = ContextUtils.getBean(DateService.class);
    val startDate = getDateForReport(date, returnNull);
    if (startDate != null)
      return dateService.convertLocalDateTimeToDbTimezoneOffsetDateTime(startDate.atStartOfDay());
    return null;
  }

  private OffsetDateTime getEndDate(String date, boolean returnNull) {
    val dateService = ContextUtils.getBean(DateService.class);
    val endDate = getDateForReport(date, returnNull);
    if (endDate != null)
      return dateService.convertLocalDateTimeToDbTimezoneOffsetDateTime(endDate.atTime(LocalTime.MAX));
    return null;
  }

  private void setLocatorDeckParameters(CallableStatement st) throws SQLException {
    st.setInt(4, 201);
    st.setString(5,
        reportsInfoInstance.getFilterConditionCode() == null ? "" : reportsInfoInstance.getFilterConditionCode());
    st.setString(6,
        reportsInfoInstance.getAvailabilityFlag() == null ? "" : reportsInfoInstance.getAvailabilityFlag());
    st.setString(7, reportsInfoInstance.getWacNumber() == null ? "" : reportsInfoInstance.getWacNumber());
    st.setString(8, reportsInfoInstance.getNiin() == null ? "" : reportsInfoInstance.getNiin());
  }

  private void setSerialLotControlNumbersParameters(CallableStatement st) throws SQLException {
    st.setInt(4, 119);
    st.setString(5, reportsInfoInstance.getNiin() == null ? "" : reportsInfoInstance.getNiin());
  }

  private void setShelfLifeParameters(CallableStatement st) throws SQLException {
    st.setInt(4, 120);
    st.setString(5, reportsInfoInstance.getNiin() == null ? "" : reportsInfoInstance.getNiin());
    st.setString(6, reportsInfoInstance.getWacNumber() == null ? "" : reportsInfoInstance.getWacNumber());
    st.setString(7, reportsInfoInstance.getCc() == null ? "" : reportsInfoInstance.getCc());
  }

  private void setInputParameters(CallableStatement st) {
    try {
      switch (reportsInfoInstance.getTargetReportName()) {
        case "Workload Detail (Picking Packing Shipping)":
          setWorkloadDetailPickPackShipParameters(st);
          break;
        case "Workload Detail (Stowing)":
          setWorkloadDetailStowingParameters(st);
          break;
        case "Workload Detail (Expiring NIINs)":
          setWorkloadDetailExpiringNiinsParameters(st);
          break;
        case "Workload Detail (NSN Updates)":
          setWorkloadDetailNsnUpdatesParameters(st);
          break;
        case "Workload Detail (Pending Inventories)":
          setWorkloadDetailPendingInventoriesParameters(st);
          break;
        case "Recon Report Summary":
          setReconParameters(st);
          break;
        case "Onhand Serviceable Qty Details":
          setOnhandServiceableQtyParameters(st);
          break;
        case "Onhand Un-Serviceable Qty Details":
          setOnhandUnserviceableQtyParameters(st);
          break;
        case "Onhand Serial Controlled Serviceable Qty Details":
          setOnhandSerialControlledServiceableQtyParameters(st);
          break;
        case "Onhand Serial Controlled Un-Serviceable Qty Details":
          setOnhandSerialControlledUnserviceableQtyParameters(st);
          break;
        case "File Import Logs Report (COURIER)":
          setCourierFileImportLogParameters(st);
          break;
        case "DASF":
          setDasfParameters(st);
          break;
        case "Active Receipt Report":
          setActiveReceiptParameters(st);
          break;
        case "Locator Deck":
          setLocatorDeckParameters(st);
          break;
        case "Serial Lot Control Numbers Report":
          setSerialLotControlNumbersParameters(st);
          break;
        case "Shelf-Life Report":
          setShelfLifeParameters(st);
          break;
        default:
          break;
      }
    }
    catch (SQLException e) {
      AdfLogUtility.logException(e);
    }
  }

  private void populateWorkloadDetailPickPackShipReport(ResultSet rs) throws SQLException {
    while (rs.next()) {
      ReportColumnBean rcb = new ReportColumnBean();
      rcb.setWac(rs.getString("WAC_NUMBER"));
      rcb.setPickLocation(rs.getString("LOCATION_LABEL"));
      rcb.setColumn(rs.getString("SUM(O225003.PACK_COLUMN)"));
      rcb.setPac(rs.getString("SUM(O225003.PACK_LEVEL)"));
      rcb.setAac(rs.getString("AAC"));
      rcb.setNiin(" " + rs.getString("NIIN"));  // ZSL  10/2017  add blank space to preserve leading zero when export to excel
      rcb.setNomenclature(rs.getString("NOMENCLATURE"));
      rcb.setSupplementaryAddress(rs.getString("SUPPLEMENTARY_ADDRESS"));
      rcb.setDocumentNumber(rs.getString("DOCUMENT_NUMBER"));
      rcb.setPin(rs.getString("PIN"));
      rcb.setStatus(rs.getString("STATUS"));
      rcb.setPickQuantity(rs.getString("PICK_QTY"));
      rcb.setScn(rs.getString("SCN"));
      rcb.setUi(rs.getString("UI"));
      rcb.setPickedBy(rs.getString("PICKED_BY"));
      rcb.setDatePicked(getFormattedDateWithTime(rs.getString("TIME_OF_PICK")));
      rcb.setDatePacked(getFormattedDateWithTime(rs.getString("PACKED_DATE")));
      rcb.setPackedBy(rs.getString("PACKED_BY"));
      rcb.setPackingStation(rs.getString("EQUIP_NAME"));
      rcb.setPriority(rs.getString("PRIORITY"));

      reportColumnList.add(rcb);
    }
    reportsInfoInstance.setReportTotals(Integer.toString(reportColumnList.size()));
  }

  private void populateWorkloadDetailStowingReport(ResultSet rs) throws SQLException {
    while (rs.next()) {
      ReportColumnBean rcb = new ReportColumnBean();
      rcb.setLocation(rs.getString("LOCATION_LABEL"));
      rcb.setSid(rs.getString("SID"));
      rcb.setNomenclature(rs.getString("NOMENCLATURE"));
      rcb.setStatus(rs.getString("STATUS"));
      rcb.setWac(rs.getString("WAC_NUMBER"));
      rcb.setStowedQty(rs.getString("SUM(O225004.STOW_QTY)"));
      rcb.setQtyToBeStowed(rs.getString("SUM(O225004.QTY_TO_BE_STOWED)"));
      rcb.setPackingStation(rs.getString("PACK_AREA"));
      rcb.setStowedBy(rs.getString("STOWED_BY"));

      reportColumnList.add(rcb);
    }
    reportsInfoInstance.setReportTotals(Integer.toString(reportColumnList.size()));
  }

  private void populateWorkloadDetailExpiringNiinsReport(ResultSet rs) throws SQLException {
    while (rs.next()) {
      ReportColumnBean rcb = new ReportColumnBean();
      rcb.setExpirationDate(getFormattedDateNoShift(rs.getString("EXPIRATION_DATE")));
      rcb.setLocation(rs.getString("LOCATION_LABEL"));
      rcb.setNiin(" " + rs.getString("NIIN"));
      rcb.setNomenclature(rs.getString("NOMENCLATURE"));
      rcb.setPickQuantity(rs.getString("QTY"));

      reportColumnList.add(rcb);
    }
    reportsInfoInstance.setReportTotals(Integer.toString(reportColumnList.size()));
  }

  private void populateWorkloadDetailNsnUpdatesReport(ResultSet rs) throws SQLException {
    while (rs.next()) {
      ReportColumnBean rcb = new ReportColumnBean();
      rcb.setExpirationDate(getFormattedDateNoShift(rs.getString("EXPIRATION_DATE")));
      rcb.setLocation(rs.getString("LOCATION_LABEL"));
      rcb.setNiin(rs.getString("NIIN"));
      rcb.setNomenclature(rs.getString("NOMENCLATURE"));
      rcb.setNsnRemark(rs.getString("NSN_REMARK"));
      rcb.setWac(rs.getString("WAC_NUMBER"));

      reportColumnList.add(rcb);
    }
    reportsInfoInstance.setReportTotals(Integer.toString(reportColumnList.size()));
  }

  private void populateWorkloadDetailPendingInventoriesReport(ResultSet rs) throws SQLException {
    while (rs.next()) {
      ReportColumnBean rcb = new ReportColumnBean();
      rcb.setInventoryType(rs.getString("INV_TYPE"));
      rcb.setPickLocation(rs.getString("LOCATION_LABEL"));
      rcb.setNiin(rs.getString("NIIN"));
      rcb.setNomenclature(rs.getString("NOMENCLATURE"));
      rcb.setQty(rs.getString("QTY"));
      rcb.setStatus(rs.getString("STATUS"));
      rcb.setWac(rs.getString("WAC_NUMBER"));
      rcb.setAssignedToUser(rs.getString("ASSIGN_TO_USER"));

      reportColumnList.add(rcb);
    }
    reportsInfoInstance.setReportTotals(Integer.toString(reportColumnList.size()));
  }

  private void populateReconReport(ResultSet rs) throws SQLException {
    while (rs.next()) {
      ReconReportColumnBean rcb = new ReconReportColumnBean();
      rcb.setNiin(rs.getString("E136127"));
      rcb.setMatchingNum(rs.getString("C_1"));
      rcb.setUnmatchingNum(rs.getString("C_3"));

      reconReportColumnList.add(rcb);
    }
    reportsInfoInstance.setReportTotals(Integer.toString(reconReportColumnList.size()));
  }

  private void populateOnHandServiceableQtyReport(ResultSet rs) throws SQLException {
    while (rs.next()) {
      ReconReportColumnBean rcb = new ReconReportColumnBean();
      rcb.setNiin(rs.getString("E116432"));
      rcb.setStratisBalance(rs.getString("E116419"));
      rcb.setHostBalance(rs.getString("E116420"));
      rcb.setBalanceDiff(rs.getString("C_3"));
      rcb.setPrice(rs.getString("C_1"));
      rcb.setAdjustment(rs.getString("C_4"));
      rcb.setLocation(rs.getString("E103010"));
      rcb.setQtyOnHand(rs.getString("C_2"));

      reconReportColumnList.add(rcb);
    }
    reportsInfoInstance.setReportTotals(Integer.toString(reconReportColumnList.size()));
  }

  private void populateOnHandUnserviceableQtyReport(ResultSet rs) throws SQLException {
    while (rs.next()) {
      ReconReportColumnBean rcb = new ReconReportColumnBean();
      rcb.setNiin(rs.getString("E116432"));
      rcb.setStratisBalance(rs.getString("E116433"));
      rcb.setHostBalance(rs.getString("E116434"));
      rcb.setBalanceDiff(rs.getString("C_3"));
      rcb.setPrice(rs.getString("C_1"));
      rcb.setAdjustment(rs.getString("C_4"));
      rcb.setLocation(rs.getString("E103010"));
      rcb.setQtyOnHand(rs.getString("C_2"));

      reconReportColumnList.add(rcb);
    }
    reportsInfoInstance.setReportTotals(Integer.toString(reconReportColumnList.size()));
  }

  private void populateOnHandSerialControlledUnserviceableQtyReport(ResultSet rs) throws SQLException {
    while (rs.next()) {
      ReconReportColumnBean rcb = new ReconReportColumnBean();
      rcb.setNiin(rs.getString("E116432"));
      rcb.setSystem(rs.getString("E131241"));
      rcb.setQtyBySerial(rs.getString("E131240"));
      rcb.setSerialNum(rs.getString("E131237"));
      rcb.setLocation(rs.getString("E131239"));
      rcb.setUnServStratisBalance(rs.getString("E116433"));
      rcb.setUnServHostBalance(rs.getString("E116434"));

      reconReportColumnList.add(rcb);
    }
    reportsInfoInstance.setReportTotals(Integer.toString(reconReportColumnList.size()));
  }

  private void populateOnHandSerialControlledServiceableQtyReport(ResultSet rs) throws SQLException {
    while (rs.next()) {
      ReconReportColumnBean rcb = new ReconReportColumnBean();
      rcb.setNiin(rs.getString("E116432"));
      rcb.setSystem(rs.getString("E131241"));
      rcb.setQtyBySerial(rs.getString("E131240"));
      rcb.setSerialNum(rs.getString("E131237"));
      rcb.setLocation(rs.getString("E131239"));
      rcb.setServStratisBalance(rs.getString("E116419"));
      rcb.setServHostBalance(rs.getString("E116420"));

      reconReportColumnList.add(rcb);
    }
    reportsInfoInstance.setReportTotals(Integer.toString(reconReportColumnList.size()));
  }

  private void populateFileImportLogsReport(ResultSet rs) throws SQLException {
    while (rs.next()) {
      ReportColumnBean rcb = new ReportColumnBean();
      rcb.setInterfaceName(rs.getString("INTERFACE_NAME"));
      rcb.setCreatedDate(getFormattedDate(rs.getString("CREATED_DATE")));
      rcb.setDescription(rs.getString("DESCRIPTION"));
      rcb.setLineNumber(rs.getString("LINE_NO"));
      rcb.setDataRow(rs.getString("DATALOAD_STATUS"));

      reportColumnList.add(rcb);
    }
    reportsInfoInstance.setReportTotals(Integer.toString(reportColumnList.size()));
  }

  private void populateDasfReport(ResultSet rs) throws SQLException {
    while (rs.next()) {
      StdReportColumnBean rcb = new StdReportColumnBean();
      rcb.setDocNumber(rs.getString("DOCUMENT_NUMBER"));
      rcb.setUnitOfIssue(rs.getString("UNIT_OF_ISSUE"));
      rcb.setQtyDue(rs.getString("QUANTITY_DUE"));
      rcb.setQtyInvoiced(rs.getString("QUANTITY_INVOICED"));
      rcb.setRecordFsc(rs.getString("RECORD_FSC"));
      rcb.setRecordNiin(rs.getString("RECORD_NIIN"));
      rcb.setSignalCode(rs.getString("SIGNAL_CODE"));
      rcb.setSupplAddr(rs.getString("SUPPLEMENTARY_ADDRESS"));
      rcb.setUnitPriceSum(rs.getString("UNIT_PRICE"));

      stdReportColumnList.add(rcb);
    }
    reportsInfoInstance.setReportTotals(Integer.toString(stdReportColumnList.size()));
  }

  private void populateActiveReceiptReport(ResultSet rs) throws SQLException {
    while (rs.next()) {
      ReportColumnBean rcb = new ReportColumnBean();
      rcb.setRcn(rs.getString(9)); // correction query - SUM(O225105.RCN)?
      rcb.setDateReceived(getFormattedDateWithTime(rs.getString("CREATED_DATE_CHAR")));
      rcb.setNiin(rs.getString("NIIN"));
      rcb.setQtyInducted(rs.getString("SUM(O225105.TOT_QUANTITY_INDUCTED)"));
      rcb.setStowedQty(rs.getString(7)); // SUM(O225105.TOT_QUANTITY_STOWED)
      rcb.setDocumentNumber(rs.getString("DOCUMENT_NUMBER"));
      rcb.setStatus(rs.getString("STATUS"));
      rcb.setSid(rs.getString("SID"));
      rcb.setLocation(rs.getString("LOCATION_LABEL"));

      reportColumnList.add(rcb);
    }
    reportsInfoInstance.setReportTotals(Integer.toString(reportColumnList.size()));
  }

  private void populateLocatorDeckReport(ResultSet rs) throws SQLException {
    while (rs.next()) {
      StdReportColumnBean rcb = new StdReportColumnBean();
      rcb.setLocationlabel(rs.getString("location_label"));
      rcb.setNiin(rs.getString("niin"));
      rcb.setCc(rs.getString("cc"));
      rcb.setNomenclature(rs.getString("item_name"));
      rcb.setUi(rs.getString("ui"));
      rcb.setQty(rs.getString("qty"));
      rcb.setPackArea(rs.getString("pack_area"));
      rcb.setPriceSum(rs.getString("price"));
      rcb.setShelfLifeCode(rs.getString("shelf_life_code"));
      rcb.setScc(rs.getString("scc"));
      rcb.setManufactureDate(getFormattedDateNoShift(rs.getString("date_of_manufacture")));
      rcb.setExpirationDate(getFormattedDateNoShift(rs.getString("expiration_date")));
      rcb.setLastInvDate(getFormattedDateWithTime(rs.getString("last_inv_date")));
      rcb.setAvailabilityFlag(rs.getString("availability_flag"));

      stdReportColumnList.add(rcb);
    }
    reportsInfoInstance.setReportTotals(Integer.toString(stdReportColumnList.size()));
  }

  private void populateSerialLotControlNumReport(ResultSet rs) throws SQLException {
    while (rs.next()) {
      ReportColumnBean rcb = new ReportColumnBean();
      rcb.setNiin(rs.getString("NIIN"));
      rcb.setNomenclature(rs.getString("NOMENCLATURE"));
      rcb.setCc(rs.getString("CC"));
      rcb.setQty(rs.getString("NIIN_LOC_QTY"));
      rcb.setLocation(rs.getString("LOCATION_LABEL"));
      rcb.setSerialNumber(rs.getString("SERIAL_NUMBER"));
      rcb.setLotControlNumber(rs.getString("LOT_CON_NUM"));
      rcb.setIssuedFlag(rs.getString("ISSUED_FLAG"));
      rcb.setSerialLotQty(rs.getString("SERIAL_LOT_QTY"));

      reportColumnList.add(rcb);
    }
    reportsInfoInstance.setReportTotals(Integer.toString(reportColumnList.size()));
  }

  private void populateShelfLifeReport(ResultSet rs) throws SQLException {
    while (rs.next()) {
      ReportColumnBean rcb = new ReportColumnBean();
      rcb.setLocation(rs.getString("LOCATION_LABEL"));
      rcb.setNiin(rs.getString("NIIN"));
      rcb.setCc(rs.getString("CC"));
      rcb.setDom(getFormattedDateNoShift(rs.getString("DATE_OF_MANUFACTURE")));
      rcb.setExpirationDate(getFormattedDateNoShift(rs.getString("EXPIRATION_DATE")));
      rcb.setNomenclature(rs.getString("NOMENCLATURE"));
      rcb.setQty(rs.getString("QTY"));

      reportColumnList.add(rcb);
    }
    reportsInfoInstance.setReportTotals(Integer.toString(reportColumnList.size()));
  }

  private String getFormattedDate(String dateToFormat) {
    return getFormattedDate(dateToFormat, DateConstants.SITE_DATE_FORMATTER_PATTERN, true);
  }

  private String getFormattedDateNoShift(String dateToFormat) {
    return getFormattedDate(dateToFormat, DateConstants.SITE_DATE_FORMATTER_PATTERN, false);
  }

  private String getFormattedDateWithTime(String dateToFormat) {
    return getFormattedDate(dateToFormat, DateConstants.SITE_DATE_WITH_TIME_FORMATTER_PATTERN, true);
  }

  private String getFormattedDate(String dateToFormat, String toFormat, boolean shiftZone) {
    val dateService = ContextUtils.getBean(DateService.class);
    return dateService.formatForAdfDisplay(dateToFormat, DateConstants.ADF_ROW_DATE_RETURN_PATTERN, toFormat, shiftZone, false);
  }

  private void populateResultSet(ResultSet rs) {
    try {
      switch (reportsInfoInstance.getTargetReportName()) {
        case "Workload Detail (Picking Packing Shipping)":
          populateWorkloadDetailPickPackShipReport(rs);
          break;
        case "Workload Detail (Stowing)":
          populateWorkloadDetailStowingReport(rs);
          break;
        case "Workload Detail (Expiring NIINs)":
          populateWorkloadDetailExpiringNiinsReport(rs);
          break;
        case "Workload Detail (NSN Updates)":
          populateWorkloadDetailNsnUpdatesReport(rs);
          break;
        case "Workload Detail (Pending Inventories)":
          populateWorkloadDetailPendingInventoriesReport(rs);
          break;
        case "Recon Report Summary":
          populateReconReport(rs);
          break;
        case "Onhand Serviceable Qty Details":
          populateOnHandServiceableQtyReport(rs);
          break;
        case "Onhand Un-Serviceable Qty Details":
          populateOnHandUnserviceableQtyReport(rs);
          break;
        case "Onhand Serial Controlled Serviceable Qty Details":
          populateOnHandSerialControlledServiceableQtyReport(rs);
          break;
        case "Onhand Serial Controlled Un-Serviceable Qty Details":
          populateOnHandSerialControlledUnserviceableQtyReport(rs);
          break;
        case "File Import Logs Report (COURIER)":
          populateFileImportLogsReport(rs);
          break;
        case "DASF":
          populateDasfReport(rs);
          break;
        case "Active Receipt Report":
          populateActiveReceiptReport(rs);
          break;
        case "Locator Deck":
          populateLocatorDeckReport(rs);
          break;
        case "Serial Lot Control Numbers Report":
          populateSerialLotControlNumReport(rs);
          break;
        case "Shelf-Life Report":
          populateShelfLifeReport(rs);
          break;
        default:
          break;
      }
    }
    catch (SQLException e) {
      AdfLogUtility.logException(e);
    }
  }

  String getSqlStatement() {
    String sql = "";
    switch (reportsInfoInstance.getTargetReportName()) {
      case "Workload Detail (Picking Packing Shipping)":
        sql = "begin ? := pkg_reports.f_get_rpt_data(?, ?, ?, ?, ?, ?, ?, ?); end;";
        break;
      case "Workload Detail (Stowing)":
      case "Workload Detail (NSN Updates)":
      case "Onhand Serviceable Qty Details":
      case "Onhand Un-Serviceable Qty Details":
      case "Onhand Serial Controlled Serviceable Qty Details":
      case "Onhand Serial Controlled Un-Serviceable Qty Details":
      case "DASF":
      case "Active Receipt Report":
        sql = "begin ? := pkg_reports.f_get_rpt_data(?, ?, ?, ?, ?); end;";
        break;
      case "Workload Detail (Expiring NIINs)":
      case "Workload Detail (Pending Inventories)":
      case "Shelf-Life Report":
        sql = "begin ? := pkg_reports.f_get_rpt_data(?, ?, ?, ?, ?, ?); end;";
        break;
      case "Recon Report Summary":
      case "File Import Logs Report (COURIER)":
      case "Serial Lot Control Numbers Report":
        sql = "begin ? := pkg_reports.f_get_rpt_data(?, ?, ?, ?); end;";
        break;
      case "Locator Deck":
        sql = "begin ? := pkg_reports.f_get_rpt_data(?, ?, ?, ?, ?, ?, ?); end;";
        break;
      default:
        break;
    }

    return sql;
  }

  public void createCsvFile(@SuppressWarnings("all") FacesContext context, OutputStream out) {
    try (
        OutputStreamWriter w = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {

      SysAdminImpl sysAdminModule = getSysAdminModule();
      String classText = "";
      if (sysAdminModule != null) {
        classText = sysAdminModule.getConfigValue("STRATIS_CLASS_TEXT");
        if (classText == null || Util.isEmpty(classText)) {
          classText = "UNCLASSIFIED, FOR OFFICIAL USE ONLY";
        }
      }
      w.write(classText + "\n");

      switch (reportsInfoInstance.getTargetReportName()) {
        case "Workload Detail (Picking Packing Shipping)":
          writeWorkloadDetailPickPackShipReport(w);
          break;
        case "Workload Detail (Stowing)":
          writeWorkloadDetailStowingReport(w);
          break;
        case "Workload Detail (Expiring NIINs)":
          writeWorkloadDetailExpiredNiinsReport(w);
          break;
        case "Workload Detail (NSN Updates)":
          writeWorkloadDetailNsnUpdatesReport(w);
          break;
        case "Workload Detail (Pending Inventories)":
          writeWorkloadDetailPendingReport(w);
          break;
        case "Recon Report Summary":
          writeReconReport(w);
          break;
        case "Onhand Serviceable Qty Details":
          writeOnhandServiceableQtyReport(w);
          break;
        case "Onhand Un-Serviceable Qty Details":
          writeOnhandUnserviceableQtyReport(w);
          break;
        case "Onhand Serial Controlled Serviceable Qty Details":
          writeOnhandSerialControlledServiceableQtyReport(w);
          break;
        case "Onhand Serial Controlled Un-Serviceable Qty Details":
          writeOnhandSerialControlledUnserviceableQtyReport(w);
          break;
        case FILE_IMPORT_LOGS_REPORT_DASF:
        case FILE_IMPORT_LOGS_REPORT_GABF:
        case FILE_IMPORT_LOGS_REPORT_GBOF:
        case FILE_IMPORT_LOGS_REPORT_MATS:
        case FILE_IMPORT_LOGS_REPORT_MHIF:
        case "File Import Logs Report (COURIER)":
          writeImportLogReport(w);
          break;
        case "DASF":
          writeDasf(w);
          break;
        case INTERFACE_TRANSACTION_REPORT:
          writeInterfaceTransactions(w);
          break;
        case "Active Receipt Report":
          writeActiveReceiptReport(w);
          break;
        case RECEIPT_HISTORY_REPORT:
          writeReceiptHistoryReport(w);
          break;
        case "Locator Deck":
          writeLocatorDeck(w);
          break;
        case "Serial Lot Control Numbers Report":
          writeSerialLotControlNumReport(w);
          break;
        case "Shelf-Life Report":
          writeShelflLifeReport(w);
          break;
        case INVENTORY_HISTORY_REPORT:
          writeInventoryHistoryReport(w);
          break;
        case EMPLOYEE_HISTORY_REPORT:
          writeEmployeeWorkloadHistory(w);
          break;
        default:
          break;
      }
      if (StringUtils.isNotEmpty(classText)) {
        w.write(classText + "\n");
      }
    }
    catch (
        Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  private void writeWorkloadDetailPickPackShipReport(OutputStreamWriter w) throws IOException {
    w.write(Objects.requireNonNull(concatenateColumns(new String[]{"WAC", "Pick Location", "Packing Station", "Column", "Pack",
        "AAC", "NIIN", "Nomenclature", "Supplementary Address", "Document Number", "PIN", "Status",
        "Qty to Be Picked", "SCN", "UI", "Picked By", "Date Picked",
        "Packed By", "Date Packed", "Priority"})));
    for (ReportColumnBean rptColumn : reportColumnList) {
      w.write(Objects.requireNonNull(concatenateColumns(new String[]{rptColumn.getWac(), rptColumn.getPickLocation(),
          rptColumn.getPackingStation(), rptColumn.getColumn(),
          rptColumn.getPac(), rptColumn.getAac(),
          rptColumn.getNiin(), rptColumn.getNomenclature(),
          rptColumn.getSupplementaryAddress(), rptColumn.getDocumentNumber(),
          rptColumn.getPin(), rptColumn.getStatus(),
          rptColumn.getPickQuantity(), rptColumn.getScn(),
          rptColumn.getUi(), rptColumn.getPickedBy(),
          rptColumn.getDatePicked(),
          rptColumn.getPackedBy(),
          rptColumn.getDatePacked(),
          rptColumn.getPriority()
      })));
    }
  }

  private void writeWorkloadDetailStowingReport(OutputStreamWriter w) throws IOException {
    w.write(Objects.requireNonNull(concatenateColumns(new String[]{"WAC", "Location", "SID", "Nomenclature", "Qty To Be Stowed", "Stowed Qty", "Status",
        "Pack Area", "Stowed By"})));
    for (ReportColumnBean rptColumn : reportColumnList) {
      w.write(Objects.requireNonNull(concatenateColumns(new String[]{rptColumn.getWac(), rptColumn.getLocation(),
          rptColumn.getSid(), rptColumn.getNomenclature(),
          rptColumn.getQtyToBeStowed(), rptColumn.getStowedQty(),
          rptColumn.getStatus(), rptColumn.getPackingStation(),
          rptColumn.getStowedBy()
      })));
    }
  }

  private void writeWorkloadDetailExpiredNiinsReport(OutputStreamWriter w) throws IOException {
    w.write(Objects.requireNonNull(concatenateColumns(new String[]{"Location", "Expiration Date", "NIIN", "Nomenclature", "Qty"})));
    for (ReportColumnBean rptColumn : reportColumnList) {
      w.write(Objects.requireNonNull(concatenateColumns(new String[]{rptColumn.getLocation(),
          rptColumn.getExpirationDate(),
          rptColumn.getNiin(), rptColumn.getNomenclature(), rptColumn.getPickQuantity()
      })));
    }
  }

  private void writeWorkloadDetailNsnUpdatesReport(OutputStreamWriter w) throws IOException {
    w.write(Objects.requireNonNull(concatenateColumns(new String[]{"WAC", "Location", "Expiration Date", "NIIN", "Nomenclature",
        "NSN Remark"})));
    for (ReportColumnBean rptColumn : reportColumnList) {
      w.write(Objects.requireNonNull(concatenateColumns(new String[]{rptColumn.getWac(), rptColumn.getLocation(),
          rptColumn.getExpirationDate(),
          rptColumn.getNiin(), rptColumn.getNomenclature(),
          rptColumn.getNsnRemark()
      })));
    }
  }

  private void writeWorkloadDetailPendingReport(OutputStreamWriter w) throws IOException {
    w.write(Objects.requireNonNull(concatenateColumns(new String[]{"WAC", "Location", "Qty", "NIIN", "Nomenclature", "Inventory Type", "Status",
        "Assigned To User"})));
    for (ReportColumnBean rptColumn : reportColumnList) {
      w.write(Objects.requireNonNull(concatenateColumns(new String[]{rptColumn.getWac(), rptColumn.getPickLocation(),
          rptColumn.getQty(), rptColumn.getNiin(), rptColumn.getNomenclature(),
          rptColumn.getInventoryType(), rptColumn.getStatus(),
          rptColumn.getAssignedToUser()
      })));
    }
  }

  private void writeReconReport(OutputStreamWriter w) throws IOException {
    w.write(Objects.requireNonNull(concatenateColumns(new String[]{"NIIN Field", "# Matching", "# Unmatching"})));
    for (ReconReportColumnBean rptColumn : reconReportColumnList) {
      w.write(Objects.requireNonNull(concatenateColumns(new String[]{rptColumn.getNiin(), rptColumn.getMatchingNum(),
          rptColumn.getUnmatchingNum()
      })));
    }
  }

  private void writeOnhandUnserviceableQtyReport(OutputStreamWriter w) throws IOException {
    //currently this report does the same thing as the OnhandServiceableQtyReport so just call it
    writeOnhandServiceableQtyReport(w);
  }

  private void writeOnhandServiceableQtyReport(OutputStreamWriter w) throws IOException {
    w.write(Objects.requireNonNull(concatenateColumns(new String[]{"NIIN", "STRATIS Balance", "Host Balance",
        "Balance Differences", "Price", "Adjustment", "Location", "QTY On Hand"})));
    for (ReconReportColumnBean rptColumn : reconReportColumnList) {
      w.write(Objects.requireNonNull(concatenateColumns(new String[]{rptColumn.getNiin(), rptColumn.getStratisBalance(),
          rptColumn.getHostBalance(), rptColumn.getBalanceDiff(),
          rptColumn.getPrice(), rptColumn.getAdjustment(),
          rptColumn.getLocation(), rptColumn.getQtyOnHand()
      })));
    }
  }

  private void writeOnhandSerialControlledServiceableQtyReport(OutputStreamWriter w) throws IOException {
    w.write(Objects.requireNonNull(concatenateColumns(new String[]{"NIIN", "System", "Qty by Serial", "Serial No.", "Location",
        "Stratis Balance Servicable", "Host Balance Servicable"})));
    for (ReconReportColumnBean rptColumn : reconReportColumnList) {
      w.write(Objects.requireNonNull(concatenateColumns(new String[]{rptColumn.getNiin(), rptColumn.getSystem(),
          rptColumn.getQtyBySerial(), rptColumn.getSerialNum(),
          rptColumn.getLocation(),
          rptColumn.getServStratisBalance(),
          rptColumn.getServHostBalance()
      })));
    }
  }

  private void writeOnhandSerialControlledUnserviceableQtyReport(OutputStreamWriter w) throws IOException {
    w.write(Objects.requireNonNull(concatenateColumns(new String[]{"NIIN", "System", "Qty by Serial", "Serial No.", "Location",
        "Stratis Balance UnServicable", "Host Balance UnServicable"})));
    for (ReconReportColumnBean rptColumn : reconReportColumnList) {
      w.write(Objects.requireNonNull(concatenateColumns(new String[]{rptColumn.getNiin(), rptColumn.getSystem(),
          rptColumn.getQtyBySerial(), rptColumn.getSerialNum(),
          rptColumn.getLocation(),
          rptColumn.getUnServStratisBalance(),
          rptColumn.getUnServHostBalance()
      })));
    }
  }

  private void writeImportLogReport(OutputStreamWriter w) throws IOException {
    w.write(Objects.requireNonNull(concatenateColumns(new String[]{"Interface", "Created Date", "Description", "Line No.",
        "Data Row"})));
    for (ReportColumnBean rptColumn : reportColumnList) {
      w.write(Objects.requireNonNull(concatenateColumns(new String[]{rptColumn.getInterfaceName(),
          rptColumn.getCreatedDate(),
          rptColumn.getDescription(), rptColumn.getLineNumber(),
          rptColumn.getDataRow()
      })));
    }
  }

  private void writeDasf(OutputStreamWriter w) throws IOException {
    w.write(Objects.requireNonNull(concatenateColumns(new String[]{"Document Number", "Unit of Issue", "Quantity Due",
        "Quantity Invoiced", "Record FSC", "Record NIIN",
        "Signal Code", "Supplementary Address", "Unit Price SUM"})));
    for (StdReportColumnBean rptColumn : stdReportColumnList) {
      w.write(Objects.requireNonNull(concatenateColumns(new String[]{rptColumn.getDocNumber(), rptColumn.getUnitOfIssue(),
          rptColumn.getQtyDue(), rptColumn.getQtyInvoiced(),
          rptColumn.getRecordFsc(), rptColumn.getRecordNiin(),
          rptColumn.getSignalCode(), rptColumn.getSupplAddr(),
          rptColumn.getUnitPriceSum()
      })));
    }
  }

  private void writeInterfaceTransactions(OutputStreamWriter w) throws IOException {
    w.write(Objects.requireNonNull(concatenateColumns(new String[]{"Priority", "Output Date", "Transaction Type",
        "Document Number", "Route", "NIIN", "UI", "QTY", "CC",
        "Transaction Date"})));
    for (StdReportColumnBean rptColumn : stdReportColumnList) {
      w.write(Objects.requireNonNull(concatenateColumns(new String[]{rptColumn.getPriority(),
          rptColumn.getOutputDate(),
          rptColumn.getTransType(), rptColumn.getDocNumber(),
          rptColumn.getRoute(), rptColumn.getNiin(),
          rptColumn.getUi(), rptColumn.getQty(), rptColumn.getCc(),
          rptColumn.getTransDate(),
      })));
    }
  }

  private void writeActiveReceiptReport(OutputStreamWriter w) throws IOException {
    w.write(Objects.requireNonNull(concatenateColumns(new String[]{"RCN", "Date Received", "NIIN", "Qty Inducted", "Qty Stowed",
        "Document Number", "Status", "SID", "Location"})));
    for (ReportColumnBean rptColumn : reportColumnList) {
      w.write(Objects.requireNonNull(concatenateColumns(new String[]{rptColumn.getRcn(),
          rptColumn.getDateReceived(),
          rptColumn.getNiin(), rptColumn.getQtyInducted(),
          rptColumn.getStowedQty(), rptColumn.getDocumentNumber(),
          rptColumn.getStatus(), rptColumn.getSid(),
          rptColumn.getLocation()
      })));
    }
  }

  private void writeReceiptHistoryReport(OutputStreamWriter w) throws IOException {
    w.write(Objects.requireNonNull(concatenateColumns(new String[]{"RCN", "SID", "NIIN", "Date Received", "Document Number",
        "Qty Received", "Qty Stowed", "Qty Backordered", "Location",
        "Received By", "Stowed By", "Date Stowed"})));
    for (ReportColumnBean rptColumn : reportColumnList) {
      w.write(Objects.requireNonNull(concatenateColumns(new String[]{rptColumn.getRcn(), rptColumn.getSid(),
          rptColumn.getNiin(),
          rptColumn.getDateReceived(),
          rptColumn.getDocumentNumber(),
          rptColumn.getQtyReceived(), rptColumn.getStowedQty(),
          rptColumn.getQtyBackordered(), rptColumn.getLocation(),
          rptColumn.getReceivedBy(), rptColumn.getStowedBy(),
          rptColumn.getDateStowed()
      })));
    }
  }

  private void writeLocatorDeck(OutputStreamWriter w) throws IOException {
    w.write(Objects.requireNonNull(concatenateColumns(new String[]{"Location", "NIIN", "CC", "Nomenclature", "UI", "Qty",
        "Pack Area", "Price SUM", "Shelf Life Code", "SCC",
        "Manufacturer's Date", "Expiration Date", "Last Inv Date",
        "Availability Flag"})));
    for (StdReportColumnBean rptColumn : stdReportColumnList) {
      w.write(Objects.requireNonNull(concatenateColumns(new String[]{rptColumn.getLocationlabel(), rptColumn.getNiin(),
          rptColumn.getCc(), rptColumn.getNomenclature(),
          rptColumn.getUi(), rptColumn.getQty(),
          rptColumn.getPackArea(), rptColumn.getPriceSum(),
          rptColumn.getShelfLifeCode(), rptColumn.getScc(),
          rptColumn.getManufactureDate(),
          rptColumn.getExpirationDate(),
          rptColumn.getLastInvDate(),
          rptColumn.getAvailabilityFlag()
      })));
    }
  }

  private void writeSerialLotControlNumReport(OutputStreamWriter w) throws IOException {
    w.write(Objects.requireNonNull(concatenateColumns(new String[]{"NIIN", "Nomenclature", "CC", "NIIN Location Qty",
        "Location", "Serial Number", "Lot Control Number",
        "Issued Flag", "Serial Lot Qty"})));
    for (ReportColumnBean rptColumn : reportColumnList) {
      w.write(Objects.requireNonNull(concatenateColumns(new String[]{rptColumn.getNiin(), rptColumn.getNomenclature(),
          rptColumn.getCc(), rptColumn.getQty(),
          rptColumn.getLocation(), rptColumn.getSerialNumber(),
          rptColumn.getLotControlNumber(),
          rptColumn.getIssuedFlag(),
          rptColumn.getSerialLotQty()
      })));
    }
  }

  private void writeShelflLifeReport(OutputStreamWriter w) throws IOException {
    w.write(Objects.requireNonNull(concatenateColumns(new String[]{"Location", "NIIN", "CC", "Date of Manufacture",
        "Expiration Date", "Nomenclature", "Qty"})));
    for (ReportColumnBean rptColumn : reportColumnList) {
      w.write(Objects.requireNonNull(concatenateColumns(new String[]{rptColumn.getLocation(), rptColumn.getNiin(),
          rptColumn.getCc(),
          rptColumn.getDom(),
          rptColumn.getExpirationDate(),
          rptColumn.getNomenclature(), rptColumn.getQty()})));
    }
  }

  private void writeInventoryHistoryReport(OutputStreamWriter w) throws IOException {
    w.write(Objects.requireNonNull(concatenateColumns(new String[]{"Completed Date", "NIIN", "Nomenclature", "Location", "CC",
        "Qty", "Pos. Adj.", "Neg. Adj.", "Employee", "Status",
        "Price", "Total"})));
    for (ReportColumnBean rptColumn : reportColumnList) {
      w.write(Objects.requireNonNull(concatenateColumns(new String[]{
          rptColumn.getCompletedDate(),
          rptColumn.getNiin(),
          rptColumn.getNomenclature(), rptColumn.getLocation(),
          rptColumn.getCc(), rptColumn.getQty(),
          rptColumn.getPosAdj(), rptColumn.getNegAdj(),
          rptColumn.getEmployee(), rptColumn.getStatus(),
          rptColumn.getPrice(), rptColumn.getPriceTotal()
      })));
    }
  }

  private void writeEmployeeWorkloadHistory(OutputStreamWriter w) throws IOException {
    w.write(Objects.requireNonNull(concatenateColumns(new String[]{"Employee", "Receipts", "Receipts Dollar Value", "Stows",
        "Stows Dollar Value", "Picks", "Picks Dollar Value", "Packs",
        "Packs Dollar Value", "Invs", "Invs Dollar Value",
        "Totals"})));
    for (StdReportColumnBean rptColumn : stdReportColumnList) {
      w.write(Objects.requireNonNull(concatenateColumns(new String[]{rptColumn.getEmployee(), rptColumn.getReceipts(),
          rptColumn.getReceiptsDollarValue(), rptColumn.getStows(),
          rptColumn.getStowsDollarValue(), rptColumn.getPicks(),
          rptColumn.getPicksDollarValue(), rptColumn.getPacks(),
          rptColumn.getPacksDollarValue(), rptColumn.getInvs(),
          rptColumn.getInvsDollarValue(),
          rptColumn.getTotals()
      })));
    }
  }

  private String concatenateColumns(String[] columns) {
    try {
      if (columns.length < 1)
        return null;

      StringBuilder row = new StringBuilder(printColumn(columns[0]));

      for (int i = 1; i < columns.length; i++)
        row.append(DLTR).append(printColumn(columns[i]));

      row.append('\n');

      return row.toString();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      return null;
    }
  }

  private String printColumn(String field) {
    return (field == null) ? "\"\"" : "\"" + field + "\"";
  }
}
