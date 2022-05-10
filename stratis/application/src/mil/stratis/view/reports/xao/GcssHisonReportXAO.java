package mil.stratis.view.reports.xao;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.model.services.SysAdminImpl;
import mil.stratis.view.admin.AdminBackingHandler;
import mil.stratis.view.reports.vo.ReportGabfInfo;
import mil.stratis.view.reports.vo.ReportQuarterlyUsage;
import mil.stratis.view.reports.vo.ReportSpoolInfo;
import mil.stratis.view.util.HTMLUtils;
import mil.usmc.mls2.stratis.core.service.multitenancy.DateService;
import mil.usmc.mls2.stratis.core.utility.ContextUtils;
import mil.usmc.mls2.stratis.core.utility.DateConstants;
import oracle.jdbc.OracleCallableStatement;
import oracle.jdbc.OracleTypes;
import org.apache.commons.collections4.CollectionUtils;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;

/**
 * This class returns the HTML that represents a GCSS HISON report.
 */
@Slf4j
public class GcssHisonReportXAO extends AdminBackingHandler {

  private static final String CLASS_NAME = "GcssHisonReportXAO";

  private static final String STYLE_NO_MARGIN_NO_BOTTOM = "margin:0in;margin-bottom:.0001pt;";
  private static final String CLASS_HDR_TEXT = "HdrTxt";      // Indent 1
  private static final String CLASS_HDR_TEXT2 = "HdrTxt2";    // Indent 2
  private static final String CLASS_HDR_TEXT3 = "HdrTxt3";    // Indent 3
  private static final String CLASS_VAL_TEXT = "ValTxt";
  private static final String CLASS_NORMAL_TEXT = "TxtNormal";
  private static final int MAX_COLUMNS = 5;

  private static final String NIIN_LABEL = "NIIN:";
  private static final String DOC_NUMBER_LABEL = "Document Number:";
  private static final String LOCATION_LABEL = "Location:";
  private static final String PKG_HISON_REPORT_NUM_DAYS = "pkg_hison.f_get_report_num_days('HISON')";

  enum DataType {
    CURRENT,
    HISTORICAL
  }

  enum IssueType {
    NORMAL,
    WALKTHRU,
    REWAREHOUSE,
    CROSSDOCK
  }

  private static final String[] transCodeLabel =
      {"AE1", "AS_", "D6_", "D8_", "D9_", "DAC", "DWF_", "SRO", "STL", "STW", "YLL", "Z0_", "Z7K"};

  private static final String[] transCodeStartsWith =
      {"AE1", "AS", "D6", "D8", "D9", "DAC", "DWF", "SRO", "STL", "STW", "YLL", "Z0", "Z7K"};

  private transient SysAdminImpl service = null;

  private boolean isSerialControlled = false;
  private boolean isLotControlled = false;

  private boolean unReportedError = false;
  private String unReportedErrorString = "";

  private final transient HTMLUtils hutils;

  private String currentNiin = "";
  private long currentNiinId = 0L;

  public GcssHisonReportXAO() {
    if (service == null) {
      service = getSysAdminModule();
    }
    hutils = new HTMLUtils();
    hutils.setCurrentParagraphClass(CLASS_NORMAL_TEXT);
    hutils.setCurrentParagraphStyle(STYLE_NO_MARGIN_NO_BOTTOM);
    hutils.setCurrentSpanClass(CLASS_NORMAL_TEXT);
    hutils.setCurrentSpanStyle("");
    hutils.setCurrentSpanHeaderClass(CLASS_HDR_TEXT);
    hutils.setCurrentSpanHeaderStyle("");
    hutils.setCurrentSpanValueClass(CLASS_VAL_TEXT);
    hutils.setCurrentSpanValueStyle("");
  }

  public String buildDocumentReport(String documentNumber) {

    return "<tr><table border=\"0\" width=\"100%\"><td>\n\n" +
        hutils.addStyles() + // Add the fonts styles
        "\n<div>" +
        addAIinfo(documentNumber) +
        addPINsInfo(documentNumber) +
        addDocIssues(documentNumber) +
        "\n</div>" +
        "</td></table></tr>\n\n";
  }

  public String buildByNiinReport(String niin) {
    StringBuilder buf = new StringBuilder();

    buf.append("<tr><table border=\"0\" width=\"100%\"><td>\n\n");
    buf.append(hutils.addStyles()); // Add the fonts styles

    buf.append("\n<div>");
    buf.append(hutils.addBlankLine());

    long niinId = getNiinId(niin);

    if (niinId != 0) {
      buf.append(addNiinDataSection(niinId));
      buf.append(addReconciliationSection(niin));
      buf.append(addQuarterlyUsageDataSection(niin));
      buf.append(addIssueDataSection(niin));
      buf.append(addWalkThruSection(niin));
      buf.append(addCrossDockSection(niin));
      buf.append(addRewarehouseSection(niin));
      buf.append(addReceiveStowSection(niin, niinId));
      buf.append(addInventorySection(niin, niinId));
      buf.append(addTransactionsSection(niinId));
      buf.append("</div>\n\n\n");
    }
    buf.append("</td></table></tr>\n\n");
    return buf.toString();
  }

  private String showValueOrNA(String value) {
    String rv = "n/a";
    if (value != null) {
      rv = value;
    }
    return rv;
  }

  private String addNiinDataSection(long niinId) {

    return addNiinInfo(niinId) +
        addLocationInfo(niinId) +
        addLastMechNonMech(niinId);
  }

  private String addReconciliationSection(String niin) {
    ArrayList<ReportGabfInfo> gabfInfoData;
    StringBuilder buf = new StringBuilder();

    buf.append(hutils.startStyledParagraph());
    buf.append(hutils.addStyledText("<b>RECONCILIATION:</b>", CLASS_HDR_TEXT, STYLE_NO_MARGIN_NO_BOTTOM));
    buf.append(hutils.endParagraph());

    buf.append(hutils.addBlankLine());

    gabfInfoData = getGabfInfoData(niin);
    buf.append(checkAndReportQueryStatus());

    buf.append(addOnHandDataByCode(gabfInfoData, true));  // Cond A
    buf.append(addOnHandDataByCode(gabfInfoData, false)); // Cond F
    buf.append(hutils.addBlankLine());

    return buf.toString();
  }

  private String addQuarterlyUsageDataSection(String niin) {
    StringBuilder buf = new StringBuilder();
    ArrayList<ReportQuarterlyUsage> quarterlyData;

    buf.append(hutils.startStyledParagraph());
    buf.append(hutils.addStyledText("<b>Quarterly Usage Data:</b>", CLASS_HDR_TEXT, STYLE_NO_MARGIN_NO_BOTTOM));
    buf.append(hutils.endParagraph());

    buf.append(hutils.addBlankLine());

    quarterlyData = getQuarterlyUsageData(niin);
    buf.append(checkAndReportQueryStatus());

    buf.append(addQuarterlyDataByCode(quarterlyData, true));  // Code A

    buf.append(hutils.addBlankLine());

    buf.append(addQuarterlyDataByCode(quarterlyData, false)); // Code F

    buf.append(hutils.addBlankLine());

    return buf.toString();
  }

  private String addIssueDataSection(String niin) {
    StringBuilder buf = new StringBuilder();

    if (checkForIssueDataByType(niin, DataType.CURRENT, IssueType.NORMAL)) {
      buf.append(hutils.startStyledParagraph());
      buf.append(hutils.addStyledText("<b>MATS Issues:</b>", CLASS_HDR_TEXT, STYLE_NO_MARGIN_NO_BOTTOM));
      buf.append(hutils.endParagraph());
      buf.append(addIssueData(niin, DataType.CURRENT, IssueType.NORMAL));
      buf.append(hutils.addBlankLine());
    }

    if (checkForIssueDataByType(niin, DataType.HISTORICAL, IssueType.NORMAL)) {
      buf.append(hutils.startStyledParagraph());
      buf.append(hutils.addStyledText("<b>MATS History:</b>", CLASS_HDR_TEXT, STYLE_NO_MARGIN_NO_BOTTOM));
      buf.append(hutils.endParagraph());
      buf.append(addIssueData(niin, DataType.HISTORICAL, IssueType.NORMAL));
      buf.append(hutils.addBlankLine());
    }

    return buf.toString();
  }

  private String addWalkThruSection(String niin) {
    StringBuilder buf = new StringBuilder();

    if (checkForIssueDataByType(niin, DataType.CURRENT, IssueType.WALKTHRU)) {
      buf.append(hutils.startStyledParagraph());
      buf.append(hutils.addStyledText("<b>Walkthru Current:</b>", CLASS_HDR_TEXT, STYLE_NO_MARGIN_NO_BOTTOM));
      buf.append(hutils.endParagraph());
      buf.append(addIssueData(niin, DataType.CURRENT, IssueType.WALKTHRU));
      buf.append(hutils.addBlankLine());
    }

    if (checkForIssueDataByType(niin, DataType.HISTORICAL, IssueType.WALKTHRU)) {
      buf.append(hutils.startStyledParagraph());
      buf.append(hutils.addStyledText("<b>Walkthru History:</b>", CLASS_HDR_TEXT, STYLE_NO_MARGIN_NO_BOTTOM));
      buf.append(hutils.endParagraph());
      buf.append(addIssueData(niin, DataType.HISTORICAL, IssueType.WALKTHRU));
      buf.append(hutils.addBlankLine());
    }

    return buf.toString();
  }

  private String addCrossDockSection(String niin) {
    StringBuilder buf = new StringBuilder();

    if (checkForIssueDataByType(niin, DataType.CURRENT, IssueType.CROSSDOCK)) {
      buf.append(hutils.startStyledParagraph());
      buf.append(hutils.addStyledText("<b>Crossdock Issue:</b>", CLASS_HDR_TEXT, STYLE_NO_MARGIN_NO_BOTTOM));
      buf.append(hutils.endParagraph());
      buf.append(addIssueData(niin, DataType.CURRENT, IssueType.CROSSDOCK));
      buf.append(hutils.addBlankLine());
    }

    if (checkForIssueDataByType(niin, DataType.HISTORICAL, IssueType.CROSSDOCK)) {
      buf.append(hutils.startStyledParagraph());
      buf.append(hutils.addStyledText("<b>Crossdock History:</b>", CLASS_HDR_TEXT, STYLE_NO_MARGIN_NO_BOTTOM));
      buf.append(hutils.endParagraph());
      buf.append(addIssueData(niin, DataType.HISTORICAL, IssueType.CROSSDOCK));
      buf.append(hutils.addBlankLine());
    }

    return buf.toString();
  }

  private String addRewarehouseSection(String niin) {
    StringBuilder buf = new StringBuilder();

    if (checkForIssueDataByType(niin, DataType.CURRENT, IssueType.REWAREHOUSE)) {
      buf.append(hutils.startStyledParagraph());
      buf.append(hutils.addStyledText("<b>Rewarehouse Issue:</b>", CLASS_HDR_TEXT, STYLE_NO_MARGIN_NO_BOTTOM));
      buf.append(hutils.endParagraph());
      buf.append(addIssueData(niin, DataType.CURRENT, IssueType.REWAREHOUSE));
      buf.append(hutils.addBlankLine());
    }

    if (checkForIssueDataByType(niin, DataType.HISTORICAL, IssueType.REWAREHOUSE)) {
      buf.append(hutils.startStyledParagraph());
      buf.append(hutils.addStyledText("<b>Rewarehouse History:</b>", CLASS_HDR_TEXT, STYLE_NO_MARGIN_NO_BOTTOM));
      buf.append(hutils.endParagraph());
      buf.append(addIssueData(niin, DataType.HISTORICAL, IssueType.REWAREHOUSE));
      buf.append(hutils.addBlankLine());
    }

    return buf.toString();
  }

  private String addReceiveStowSection(String niin, long niinId) {
    StringBuilder buf = new StringBuilder();

    if (checkForReceiptDataByType(niin, DataType.CURRENT)) {
      buf.append(hutils.startStyledParagraph());
      buf.append(hutils.addStyledText("<b>Receive / Stow Current:</b>", CLASS_HDR_TEXT, STYLE_NO_MARGIN_NO_BOTTOM));
      buf.append(hutils.endParagraph());
      buf.append(addReceiptStowData(niin, niinId, DataType.CURRENT));
      buf.append(hutils.addBlankLine());
    }

    if (checkForReceiptDataByType(niin, DataType.HISTORICAL)) {
      buf.append(hutils.startStyledParagraph());
      buf.append(hutils.addStyledText("<b>Receive / Stow History:</b>", CLASS_HDR_TEXT, STYLE_NO_MARGIN_NO_BOTTOM));
      buf.append(hutils.endParagraph());
      buf.append(addReceiptStowData(niin, niinId, DataType.HISTORICAL));
      buf.append(hutils.addBlankLine());
    }

    return buf.toString();
  }

  private String addInventorySection(String niin, long niinId) {
    StringBuilder buf = new StringBuilder();
    if (checkForInventoryDataByType(niinId, DataType.CURRENT)) {
      buf.append(hutils.startStyledParagraph());
      buf.append(hutils.addStyledText("<b>Inventory:</b>", CLASS_HDR_TEXT, STYLE_NO_MARGIN_NO_BOTTOM));
      buf.append(hutils.endParagraph());
      buf.append(addInventoryData(niin, niinId, DataType.CURRENT));
      buf.append(hutils.addBlankLine());
    }

    if (checkForInventoryDataByType(niinId, DataType.HISTORICAL)) {
      buf.append(hutils.startStyledParagraph());
      buf.append(hutils.addStyledText("<b>Inventory History:</b>", CLASS_HDR_TEXT, STYLE_NO_MARGIN_NO_BOTTOM));
      buf.append(hutils.endParagraph());
      buf.append(addInventoryData(niin, niinId, DataType.HISTORICAL));
      buf.append(hutils.addBlankLine());
    }

    return buf.toString();
  }

  private String addTransactionsSection(long niinId) {
    StringBuilder buf = new StringBuilder();
    ArrayList<ReportSpoolInfo> reportSpoolInfo;

    reportSpoolInfo = getSpoolInfoData(niinId);
    buf.append(checkAndReportQueryStatus());

    if (CollectionUtils.isNotEmpty(reportSpoolInfo)) {
      buf.append(hutils.startStyledParagraph());
      buf.append(hutils.addStyledText("<b>Transaction History:</b>", CLASS_HDR_TEXT, STYLE_NO_MARGIN_NO_BOTTOM));
      buf.append(hutils.endParagraph());

      for (int i = 0; i < transCodeStartsWith.length; i++) {
        buf.append(addSpoolInfoByCode(reportSpoolInfo, transCodeStartsWith[i], transCodeLabel[i]));
      }
    }

    return buf.toString();
  }

  private static final String GET_AI_INFO_SQL =
      "select document_number, scn, issue_type, created_date, ui, cc, " +
          "issue_priority_designator, niin, qty, qty_issued, supplementary_address, " +
          "cost_jon, refused_qty " +
          "from v_ai_info where document_number = ? ";

  public String addAIinfo(String docNumber) {

    StringBuilder buf = new StringBuilder();

    try (PreparedStatement pstmt = service.getDBTransaction().createPreparedStatement(GET_AI_INFO_SQL, 0)) {

      pstmt.setString(1, docNumber);
      try (ResultSet rs = pstmt.executeQuery()) {

        if (rs.next()) {
          buf.append(hutils.startStyledParagraph());
          buf.append(hutils.addStyledValuePair("Document Number: ", rs.getString(1)));
          buf.append(hutils.addStyledValuePair("SCN:", rs.getString(2)));
          buf.append(hutils.addStyledValuePair("Issue Type: ", rs.getString(3)));
          buf.append(hutils.endParagraph());
          buf.append(hutils.startStyledParagraph());
          buf.append(hutils.addStyledValuePair("Created Date: ", getFormattedDate(rs.getString(4))));
          buf.append(hutils.addStyledValuePair("UI:", rs.getString(5)));
          buf.append(hutils.addStyledValuePair("CC:", rs.getString(6)));
          buf.append(hutils.addStyledValuePair("Priority:", rs.getString(7)));
          buf.append(hutils.endParagraph());

          buf.append(hutils.startStyledParagraph());
          buf.append(hutils.addStyledValuePair(NIIN_LABEL, rs.getString(8)));
          buf.append(hutils.addStyledValuePair("Issue Qty:", String.valueOf(rs.getLong(9))));
          buf.append(hutils.addStyledValuePair("Issued Qty:", String.valueOf(rs.getLong(10))));
          buf.append(hutils.addStyledValuePair("Refused Qty:", String.valueOf(rs.getLong(13))));
          buf.append(hutils.endParagraph());

          buf.append(hutils.startStyledParagraph());
          buf.append(hutils.addStyledValuePair("Supp. Address:", rs.getString(11)));
          buf.append(hutils.addStyledValuePair("Cost Jon: ", rs.getString(12)));
          buf.append(hutils.endParagraph());

          buf.append(hutils.addBlankLine());
        }
      }
    }
    catch (SQLException e) {
      buf.append(showSqlErrorMessageInResponse(e, "getAIinfo"));
    }

    return buf.toString();
  }

  /**
   * select scn from issue where document_number = ?
   * union select scn from issue_hist where document_number = ? ;
   */

  private static final String GET_DOC_BACKORDER_SCN_SQL =
      "select scn from issue where document_number = ? and TRUNC (SYSDATE) - TRUNC (ISSUE.created_date) <= " + PKG_HISON_REPORT_NUM_DAYS +
          " union " +
          "select scn from issue_hist where document_number = ? and TRUNC (SYSDATE) - TRUNC (ISSUE_HIST.created_date) <= " + PKG_HISON_REPORT_NUM_DAYS;

  private String getDocBackorder(String docNumber) {
    StringBuilder buf = new StringBuilder();

    try (PreparedStatement pstmt = service.getDBTransaction().createPreparedStatement(GET_DOC_BACKORDER_SCN_SQL, 0)) {
      pstmt.setString(1, docNumber);
      pstmt.setString(2, docNumber);

      try (ResultSet rs = pstmt.executeQuery()) {

        while (rs.next()) {
          String scn = rs.getString(1);
          buf.append(addShippingDataByScn(scn, DataType.CURRENT));
          buf.append(addShippingDataByScn(scn, DataType.HISTORICAL));
        }
      }
    }
    catch (SQLException e) {
      buf.append(showSqlErrorMessageInResponse(e, "getDocBackorder"));
    }
    return buf.toString();
  }

  private static final String GET_CURRENT_SHIPPING_DATA_BY_SCN_SQL =
      "select consolidation_barcode, name, floor_location, created_by, " +
          "tcn, manifest, manifested_by, manifest_date, lead_tcn, modified_by, " +
          "delivered_date, driver, scn " +
          "from v_current_shipping where scn = ? ";

  private static final String GET_HISTORICAL_SHIPPING_DATA_BY_SCN_SQL =
      "select consolidation_barcode, name, floor_location, created_by, " +
          "tcn, manifest, manifested_by, manifest_date, lead_tcn, modified_by, " +
          "delivered_date, driver, scn " +
          "from v_historical_shipping where scn = ? ";

  private String addShippingDataByScn(String scn, DataType dt) {
    StringBuilder buf = new StringBuilder();
    String sql = GET_CURRENT_SHIPPING_DATA_BY_SCN_SQL;
    if (dt == DataType.HISTORICAL) {
      sql = GET_HISTORICAL_SHIPPING_DATA_BY_SCN_SQL;
    }

    try (PreparedStatement pstmt = service.getDBTransaction().createPreparedStatement(sql, 0)) {
      pstmt.setString(1, scn);
      try (ResultSet rs = pstmt.executeQuery()) {

        while (rs.next()) {
          buf.append(hutils.addBlankLine());

          buf.append(hutils.startStyledParagraph());
          buf.append(hutils.addStyledValuePair("Shipping Barcode:", rs.getString(1), CLASS_HDR_TEXT2));
          buf.append(hutils.endParagraph());

          buf.append(hutils.startStyledParagraph());
          buf.append(hutils.addStyledValuePair("Floor Location:", rs.getString(3), CLASS_HDR_TEXT2));
          buf.append(hutils.addStyledValuePair("Assigned By:", rs.getString(4)));
          buf.append(hutils.endParagraph());

          buf.append(hutils.startStyledParagraph());
          buf.append(hutils.addStyledValuePair("Manifest#:", rs.getString(6), CLASS_HDR_TEXT2));
          buf.append(hutils.addStyledValuePair("Manifested By:", rs.getString(10))); // Was (7)

          buf.append(hutils.endParagraph());

          buf.append(hutils.startStyledParagraph());

          buf.append(hutils.addStyledValuePair("Manifested Date:", showValueOrNA(getFormattedDate(rs.getString(8))), CLASS_HDR_TEXT2));
          buf.append(hutils.endParagraph());

          buf.append(hutils.startStyledParagraph());
          buf.append(hutils.addStyledValuePair("TCN:", rs.getString(5), CLASS_HDR_TEXT2));
          buf.append(hutils.addStyledValuePair("Lead TCN:", rs.getString(9)));
          buf.append(hutils.endParagraph());

          buf.append(hutils.startStyledParagraph());
          buf.append(hutils.addStyledValuePair("Acknowledge Delivery By:", rs.getString(10), CLASS_HDR_TEXT2));
          buf.append(hutils.endParagraph());

          buf.append(hutils.startStyledParagraph());

          buf.append(hutils.addStyledValuePair("Deliver Date:", getFormattedDate(rs.getString(11)), CLASS_HDR_TEXT2));
          buf.append(hutils.addStyledValuePair("Driver:", rs.getString(12)));

          buf.append(hutils.endParagraph());
        }
      }
    }
    catch (SQLException e) {
      buf.append(showSqlErrorMessageInResponse(e, "addCurrentShippingDataByScn"));
    }
    return buf.toString();
  }

  private static final String GET_V_DOC_ISSUES_SQL =
      "select rcn, document_number, suffix, cc, ui, created_date, " +
          "quantity_invoiced, quantity_inducted, created_by, " +
          "remain_due_in, niin_id, niin " +
          "from v_doc_issues where document_number = ? ";

  private String addDocIssues(String docNumber) {
    StringBuilder buf = new StringBuilder();

    try (PreparedStatement pstmt = service.getDBTransaction().createPreparedStatement(GET_V_DOC_ISSUES_SQL, 0)) {
      pstmt.setString(1, docNumber);
      try (ResultSet rs = pstmt.executeQuery()) {

        while (rs.next()) {
          buf.append(hutils.startStyledParagraph());
          buf.append(hutils.addStyledValuePair("RCN:", rs.getString(1)));
          buf.append(hutils.addStyledValuePair(DOC_NUMBER_LABEL, rs.getString(2)));
          buf.append(hutils.addStyledValuePair("Suffix: ", rs.getString(3)));
          buf.append(hutils.addStyledValuePair("CC:", rs.getString(4)));
          buf.append(hutils.addStyledValuePair("UI:", rs.getString(5)));
          buf.append(hutils.endParagraph());

          buf.append(hutils.startStyledParagraph());
          buf.append(hutils.addStyledValuePair("Created Date:", getFormattedDate(rs.getString(6))));
          buf.append(hutils.addStyledValuePair("Invoice Qty:", String.valueOf(rs.getLong(7))));
          buf.append(hutils.addStyledValuePair("Qty Received:", String.valueOf(rs.getLong(8))));
          buf.append(hutils.endParagraph());

          buf.append(hutils.startStyledParagraph());
          buf.append(hutils.addStyledValuePair(NIIN_LABEL, rs.getString(12)));
          buf.append(hutils.endParagraph());

          buf.append(hutils.startStyledParagraph());
          buf.append(hutils.addStyledValuePair("Received By: ", rs.getString(9)));
          buf.append(hutils.endParagraph());

          //05-06-2014: Added Stow Data to HISON by Document Number
          String rcn = rs.getString(1);
          buf.append(addStowData(Integer.parseInt(rcn), DataType.CURRENT));
          buf.append(addStowData(Integer.parseInt(rcn), DataType.HISTORICAL));
          buf.append(addDocXdoc(docNumber, rs.getLong(1)));

          buf.append(addDocSpoolInfo(rs.getInt(11), docNumber));
        }
      }
    }
    catch (SQLException e) {
      buf.append(showSqlErrorMessageInResponse(e, "addDocIssues"));
    }
    return buf.toString();
  }

  private static final String GET_DOC_SPOOL_INFO_SQL =
      "SELECT nvl(TRANSACTION_TYPE, 'n/a'), SPOOL_BATCH_NUM,  TIMESTAMP_SENT " +
          "FROM SPOOL WHERE NIIN_ID = ? AND (GCSSMC_XML LIKE ? ) AND (TRANSACTION_TYPE = 'D6T' OR " +
          "TRANSACTION_TYPE = 'D6A' OR TRANSACTION_TYPE = 'STW' OR TRANSACTION_TYPE = 'STL') " +
          "AND TRUNC(sysdate) - TRUNC(timestamp) <= pkg_hison.f_get_spool_num_days('HISON') " +
          "UNION SELECT nvl(TRANSACTION_TYPE, 'n/a'), SPOOL_BATCH_NUM, TIMESTAMP_SENT " +
          "FROM SPOOL_HIST WHERE NIIN_ID = ? AND (GCSSMC_XML LIKE ?) AND (TRANSACTION_TYPE = 'D6T' OR " +
          "TRANSACTION_TYPE = 'D6A' OR TRANSACTION_TYPE = 'STW' OR TRANSACTION_TYPE = 'STL') " +
          "AND TRUNC(sysdate) - TRUNC(timestamp) <= pkg_hison.f_get_spool_num_days('HISON') ";

  private String addDocSpoolInfo(long niinId, String docNumber) {

    StringBuilder buf = new StringBuilder();

    try (PreparedStatement pstmt = service.getDBTransaction().createPreparedStatement(GET_DOC_SPOOL_INFO_SQL, 0)) {
      StringBuilder docNumberWildCard =
          new StringBuilder("%").append(docNumber).append("%");

      pstmt.setLong(1, niinId);
      pstmt.setString(2, docNumberWildCard.toString());
      pstmt.setLong(3, niinId);
      pstmt.setString(4, docNumberWildCard.toString());

      try (ResultSet rs = pstmt.executeQuery()) {

        while (rs.next()) {
          buf.append(hutils.startStyledParagraph());
          buf.append(hutils.addStyledValuePair("Transaction Type:", rs.getString(1)));
          buf.append(hutils.addStyledValuePair("Date Sent:", getFormattedDate(rs.getString(3))));
          buf.append(hutils.endParagraph());

          buf.append(hutils.addBlankLine());
        }
      }
    }
    catch (SQLException e) {
      buf.append(showSqlErrorMessageInResponse(e, "getDocSpoolInfo"));
    }
    return buf.toString();
  }

  private String getFormattedDate(String dateToFormat) {
    return getFormattedDate(dateToFormat, DateConstants.ADF_ROW_DATE_RETURN_PATTERN, true, true);
  }

  private String getFormattedDate(String dateToFormat, String fromFormat, boolean showTime, boolean shiftZone) {
    if (dateToFormat == null) return "n/a";
    if (dateToFormat.equalsIgnoreCase("n/a")) return dateToFormat;
    val displayFormat = showTime ? DateConstants.SITE_DATE_WITH_TIME_FORMATTER_PATTERN : DateConstants.SITE_DATE_FORMATTER_PATTERN;
    val dateService = ContextUtils.getBean(DateService.class);
    return dateService.formatForAdfDisplay(dateToFormat, fromFormat, displayFormat, shiftZone, false);
  }

  private static final String GET_DOC_XDOC_SQL =
      "select scn, document_number, suffix, qty_issued, serial_number, qty, " +
          "lot_con_num, rcn, niin_id " +
          "from v_xdoc where rcn = ? and document_number = ? ";

  private String addDocXdoc(String docNumber, long rcn) {
    StringBuilder buf = new StringBuilder();

    try (PreparedStatement pstmt = service.getDBTransaction().createPreparedStatement(GET_DOC_XDOC_SQL, 0)) {
      pstmt.setLong(1, rcn);
      pstmt.setString(2, docNumber);
      try (ResultSet rs = pstmt.executeQuery()) {

        boolean needHeader = true;

        while (rs.next()) {

          if (needHeader) {
            buf.append(hutils.startStyledParagraph());
            buf.append(hutils.addStyledText("<b>Crossdock:</b>", CLASS_HDR_TEXT, STYLE_NO_MARGIN_NO_BOTTOM));
            buf.append(hutils.endParagraph());
            needHeader = false;
          }

          buf.append(hutils.startStyledParagraph());
          buf.append(hutils.addStyledValuePair("SCN:", rs.getString(1), CLASS_HDR_TEXT));
          buf.append(hutils.addStyledValuePair(DOC_NUMBER_LABEL, rs.getString(2)));
          buf.append(hutils.addStyledValuePair("Suffix:", rs.getString(3)));
          buf.append(hutils.addStyledValuePair("Issue Qty:", String.valueOf(rs.getLong(4))));
          buf.append(hutils.endParagraph());

          buf.append(hutils.startStyledParagraph());
          buf.append(hutils.startStyledParagraph());
          buf.append(hutils.addStyledValuePair("Serial#:", rs.getString(5), CLASS_HDR_TEXT2));
          buf.append(hutils.addStyledValuePair("Lot#:", rs.getString(7)));
          buf.append(hutils.addStyledValuePair("Qty:", String.valueOf(rs.getLong(6))));
          buf.append(hutils.endParagraph());

          buf.append(addDocumentCrossdockSpoolInfo(rs.getInt(9), docNumber));
          buf.append(hutils.addBlankLine());
        }
      }
    }
    catch (SQLException e) {
      buf.append(showSqlErrorMessageInResponse(e, "getDocXdoc"));
    }
    return buf.toString();
  }

  /**
   * 1 - timestamp
   * 2 - transaction_type
   * 3 - document_number
   * 4 - timestamp_sent
   * 5 - spool_batch_num
   * 6 - niin_id
   */

  private static final String GET_DOC_CROSSDOCK_SPOOL_INFO_SQL =
      "select timestamp, transaction_type, document_number, timestamp_sent, " +
          "spool_batch_num, niin_id " +
          "from v_doc_spool_info where niin_id = ? and document_number = ? " +
          "and transaction_type = 'DWF' " +
          "order by timestamp DESC";

  private String addDocumentCrossdockSpoolInfo(long niinId, String docNumber) {
    StringBuilder buf = new StringBuilder();

    try (PreparedStatement pstmt = service.getDBTransaction().createPreparedStatement(GET_DOC_CROSSDOCK_SPOOL_INFO_SQL, 0)) {
      pstmt.setLong(1, niinId);
      pstmt.setString(2, docNumber);

      try (ResultSet rs = pstmt.executeQuery()) {

        boolean needHeader = true;

        while (rs.next()) {
          if (needHeader) {
            buf.append(addTransactionHeader("CROSSDOCK"));
            needHeader = false;
          }

          buf.append(hutils.startStyledParagraph());
          buf.append(hutils.addStyledValuePair("Timestamp:", rs.getString(1), CLASS_HDR_TEXT2));
          buf.append(hutils.addStyledValuePair("Transaction Type:", rs.getString(2)));  // Always DWF
          buf.append(hutils.addStyledValuePair("Batch#:", String.valueOf(rs.getLong(5))));
          buf.append(hutils.addStyledValuePair("Date Sent:", showValueOrNA(rs.getString(4))));
          buf.append(hutils.endParagraph());

          buf.append(hutils.addBlankLine());
        }
      }
    }
    catch (SQLException e) {
      buf.append(showSqlErrorMessageInResponse(e, "getDocXdocSpoolInfo"));
    }
    return buf.toString();
  }

  private static final String GET_NIIN_INFO_SQL =
      "select distinct fsc, niin niin, ui, cube, weight,  " +
          "scc, smic, shelf_life_code, " +
          "activity_date, " +
          "tamcn, price, substr(new_nsn,5) new_nsn, niin_id, " +
          "nomenclature, serial_control_flag, lot_control_flag, " +
          "ro_threshold " +
          "from niin_info  where niin_id = ? ";

  public String addNiinInfo(long niinId) {
    StringBuilder buf = new StringBuilder();

    try (PreparedStatement pstmt = service.getDBTransaction().createPreparedStatement(GET_NIIN_INFO_SQL, 0)) {
      pstmt.setLong(1, niinId);
      try (ResultSet rs = pstmt.executeQuery()) {

        if (rs.next()) {

          buf.append(hutils.startStyledParagraph());
          buf.append(hutils.addStyledText("<b>NIIN DATA:</b>", CLASS_HDR_TEXT, STYLE_NO_MARGIN_NO_BOTTOM));
          buf.append(hutils.endParagraph());

          buf.append(hutils.addBlankLine());

          buf.append(hutils.startStyledParagraph());
          buf.append(hutils.addStyledValuePair("FSC:", rs.getString(1)));
          buf.append(hutils.addStyledValuePair(NIIN_LABEL, rs.getString(2)));
          buf.append(hutils.addStyledValuePair("Nomenclature:", rs.getString(14)));
          buf.append(hutils.endParagraph());

          buf.append(hutils.startStyledParagraph());
          buf.append(hutils.addStyledValuePair("UI:", rs.getString(3), CLASS_HDR_TEXT2));
          buf.append(hutils.addStyledValuePair("Cube:", rs.getString(4)));
          buf.append(hutils.addStyledValuePair("WT:", Double.toString(rs.getDouble(5))));
          buf.append(hutils.addStyledValuePair("SCC:", rs.getString(6)));
          buf.append(hutils.addStyledValuePair("SLC:", rs.getString(8)));
          buf.append(hutils.addStyledValuePair("Price:", Double.toString(rs.getDouble(11))));
          buf.append(hutils.endParagraph());

          buf.append(hutils.startStyledParagraph());
          buf.append(hutils.addStyledValuePair("Last-Activity-Date:", getFormattedDate(rs.getString(9)), CLASS_HDR_TEXT2));
          String serialControled = rs.getString(15);
          String lotControlled = rs.getString(16);
          isSerialControlled = serialControled.compareToIgnoreCase("n") != 0;
          isLotControlled = lotControlled.compareToIgnoreCase("n") != 0;
          if ((isLotControlled) || (isSerialControlled)) {
            buf.append(hutils.addStyledValuePair("Controlled:", "Y"));
          }
          else {
            buf.append(hutils.addStyledValuePair("Controlled:", "N"));
          }
          buf.append(hutils.endParagraph());
          buf.append(hutils.addBlankLine());
        }
      }
    }
    catch (SQLException e) {
      buf.append(showSqlErrorMessageInResponse(e, "getNiinInfo"));
    }
    return buf.toString();
  }

  private static final String GET_NIIN_ID_SQL =
      "select niin_id, serial_control_flag, lot_control_flag from niin_info where niin = ? ";

  /**
   * Get the niin_id.
   */
  private long getNiinId(String niin) {
    if (niin == null || niin.length() < 9) {
      return 0;
    }

    long niinId = 0;
    String lotControlFlag;
    String serialControlFlag;
    try (PreparedStatement pstmt = service.getDBTransaction().createPreparedStatement(GET_NIIN_ID_SQL, 0)) {
      pstmt.setString(1, niin);
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          niinId = rs.getLong(1);
          serialControlFlag = rs.getString(2);
          lotControlFlag = rs.getString(3);
          if (serialControlFlag.trim().compareTo("Y") == 0) {
            isSerialControlled = true;
          }
          if (lotControlFlag.trim().compareTo("Y") == 0) {
            isLotControlled = true;
          }
        }
      }
    }
    catch (SQLException sqle) {
      niinId = 0;
    }
    return niinId;
  }

  private static final String GET_NIIN_SQL =
      "select niin from niin_info where niin_id = ? ";

  private String getNiin(long niinId) {
    String niin = "n/a";

    if (currentNiinId == niinId) {
      return currentNiin;
    }

    try (PreparedStatement pstmt = service.getDBTransaction().createPreparedStatement(GET_NIIN_SQL, 0)) {
      pstmt.setLong(1, niinId);
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          niin = rs.getString(1);
          currentNiin = niin;
          currentNiinId = niinId;
        }
        else {
          currentNiin = "";
          currentNiinId = 0;
        }
      }
    }
    catch (SQLException sqle) {
      niin = "n/a";
      currentNiin = "";
      currentNiinId = 0;
    }
    return niin;
  }

  private static final String GET_PINS_INFO_SQL =
      "select pin, location_label, qty_picked, picked_by, time_of_pick, " +
          "scn, packing_consolidation_id, serial_number, qty, lot_con_num, " +
          "refused_qty, document_number, niin_id " +
          "from v_docpins_info where document_number = ? ";

  private String addPINsInfo(String docNumber) {
    StringBuilder buf = new StringBuilder();
    boolean backorderflag = true;

    try (PreparedStatement pstmt = service.getDBTransaction().createPreparedStatement(GET_PINS_INFO_SQL, 0)) {
      pstmt.setString(1, docNumber);
      try (ResultSet rs = pstmt.executeQuery()) {

        while (rs.next()) {
          long packingConsolidationId = rs.getLong(7);

          buf.append(hutils.startStyledParagraph());
          buf.append(hutils.addStyledValuePair("PIN#:", rs.getString(1)));
          buf.append(hutils.startStyledParagraph());
          buf.append(hutils.addStyledValuePair(LOCATION_LABEL, rs.getString(2)));
          buf.append(hutils.startStyledParagraph());
          buf.append(hutils.addStyledValuePair("Picked Qty:", String.valueOf(rs.getLong(3))));
          buf.append(hutils.startStyledParagraph());
          buf.append(hutils.addStyledValuePair("Refused Qty:", String.valueOf(rs.getLong(11))));
          buf.append(hutils.endParagraph());

          buf.append(hutils.startStyledParagraph());
          buf.append(hutils.addStyledValuePair("Picked By:", rs.getString(4)));

          buf.append(hutils.addStyledValuePair("Date Picked:", getFormattedDate(rs.getString(5))));
          buf.append(hutils.endParagraph());

          buf.append(addPackingInfoByConsolidationId(packingConsolidationId));

          buf.append(hutils.startStyledParagraph());
          buf.append(hutils.addStyledValuePair("Issued Serial#:", rs.getString(8)));
          buf.append(hutils.startStyledParagraph());
          buf.append(hutils.addStyledValuePair("Lot#:", rs.getString(10)));
          buf.append(hutils.startStyledParagraph());
          buf.append(hutils.addStyledValuePair("Qty:", String.valueOf(rs.getLong(9))));
          buf.append(hutils.endParagraph());

          String scn = rs.getString(5);

          buf.append(addShippingDataByScn(scn, DataType.CURRENT));
          buf.append(addShippingDataByScn(scn, DataType.HISTORICAL));

          buf.append(addPINsSpoolInfo(rs.getLong(13), rs.getString(12))); // niin_id, document number

          backorderflag = false;
        }
      }
    }
    catch (SQLException e) {
      buf.append(showSqlErrorMessageInResponse(e, "addPINsInfo"));
    }

    // If there are no records above then show back order info
    if (backorderflag) {
      buf.append(getDocBackorder(docNumber));
    }

    return buf.toString();
  }

  private static final String GET_PACKING_INFO_BY_CONSOLIDATION_ID_SQL =
      "select name, packed_date, packed_by, packing_consolidation_id " +
          "from v_packing_info where packing_consolidation_id = ? ";

  private String addPackingInfoByConsolidationId(long packingConsolidationId) {
    StringBuilder buf = new StringBuilder();
    try (PreparedStatement pstmt = service.getDBTransaction().createPreparedStatement(GET_PACKING_INFO_BY_CONSOLIDATION_ID_SQL, 0)) {
      pstmt.setLong(1, packingConsolidationId);
      try (ResultSet rs = pstmt.executeQuery()) {

        while (rs.next()) {
          buf.append(hutils.startStyledParagraph());
          buf.append(hutils.addStyledValuePair("Pack STN:", rs.getString(1)));
          buf.append(hutils.addStyledValuePair("Packed Date:", showValueOrNA(getFormattedDate(rs.getString(2)))));
          buf.append(hutils.endParagraph());

          buf.append(hutils.startStyledParagraph());
          buf.append(hutils.addStyledValuePair("Packed By:", rs.getString(3)));
          buf.append(hutils.endParagraph());
        }
      }
    }
    catch (SQLException e) {
      buf.append(showSqlErrorMessageInResponse(e, "addPackingInfoByConsolidationId"));
    }
    return buf.toString();
  }

  private static final String GET_PINS_DOC_SPOOL_INFO_SQL =
      "select timestamp, transaction_type, document_number, timestamp_sent, " +
          "spool_batch_num, niin_id " +
          "from v_doc_spool_info where niin_id = ? and document_number = ? " +
          "and ( transaction_type = 'SRO' or transaction_type = 'AE1' or " +
          "transaction_type = 'AS1' or transaction_type = 'Z7K' ) " +
          "order by timestamp desc ";

  private String addPINsSpoolInfo(long niinId, String docNumber) {
    StringBuilder buf = new StringBuilder();

    try (PreparedStatement pstmt = service.getDBTransaction().createPreparedStatement(GET_PINS_DOC_SPOOL_INFO_SQL, 0)) {
      pstmt.setLong(1, niinId);
      pstmt.setString(2, docNumber);

      try (ResultSet rs = pstmt.executeQuery()) {

        while (rs.next()) {
          buf.append(hutils.startStyledParagraph());
          buf.append(hutils.addStyledValuePair("Timestamp:", rs.getString(1), CLASS_HDR_TEXT2));
          buf.append(hutils.addStyledValuePair("Transaction Type:", rs.getString(2)));
          buf.append(hutils.addStyledValuePair("Batch#:", String.valueOf(rs.getLong(5))));
          buf.append(hutils.addStyledValuePair("Date Sent:", showValueOrNA(rs.getString(4))));
          buf.append(hutils.endParagraph());

          buf.append(hutils.addBlankLine());
        }
      }
    }
    catch (SQLException e) {
      buf.append(showSqlErrorMessageInResponse(e, "addPINsSpoolInfo"));
    }
    return buf.toString();
  }

  private static final String GET_LOCATION_INFO_SQL =
      "select v.niin_id, v.location_id, v.location_label, v.qty, v.cc, v.date_of_manufacture, v.expiration_date, " +
          "nl.LAST_INV_DATE as real_last_inv_date, v.last_inv " +
          "from v_get_location v, niin_location nl " +
          "where v.niin_id = nl.niin_id and v.location_id = nl.location_id  and v.NIIN_ID = ? ";

  private String addLocationInfo(long niinId) {
    StringBuilder buf = new StringBuilder();
    try (PreparedStatement pstmt = service.getDBTransaction().createPreparedStatement(GET_LOCATION_INFO_SQL, 0)) {
      pstmt.setLong(1, niinId);
      try (ResultSet rs = pstmt.executeQuery()) {

        while (rs.next()) {
          long locationId = rs.getLong(2);

          buf.append(hutils.startStyledParagraph());
          buf.append(hutils.addStyledValuePair(LOCATION_LABEL, rs.getString(3), CLASS_HDR_TEXT2));
          buf.append(hutils.addStyledValuePair("Qty:", Integer.toString(rs.getInt(4))));
          buf.append(hutils.addStyledValuePair("CC:", rs.getString(5)));
          buf.append(hutils.addStyledValuePair("Exp Date:", showValueOrNA(getFormattedDate(rs.getString(7), DateConstants.ADF_ROW_DATE_RETURN_PATTERN,
              false, false))));
          buf.append(hutils.endParagraph());

          buf.append(hutils.startStyledParagraph());
          buf.append(hutils.addStyledValuePair("Last Inv Date:", showValueOrNA(getFormattedDate(rs.getString(8))), CLASS_HDR_TEXT2));
          buf.append(hutils.endParagraph());

          if (isSerialControlled) buf.append(getStockDataRowsByLocationId(niinId, locationId, true));

          if (isLotControlled) buf.append(getStockDataRowsByLocationId(niinId, locationId, false));

          buf.append(hutils.addBlankLine());
        }
      }
    }
    catch (SQLException e) {
      buf.append(showSqlErrorMessageInResponse(e, "addLocationInfo"));
    }
    return buf.toString();
  }

  private String addLastMechNonMech(long niinId) {
    StringBuilder buf = new StringBuilder();
    String lastMechLocation = getLastMechLocation(niinId, true);
    buf.append(checkAndReportQueryStatus());
    String lastNonMechLocation = getLastMechLocation(niinId, false);
    buf.append(checkAndReportQueryStatus());

    buf.append(hutils.startStyledParagraph());
    buf.append(hutils.addStyledValuePair("Last_NonMech_Location:", lastNonMechLocation, CLASS_HDR_TEXT2));
    buf.append(hutils.addStyledValuePair("Last_Mech_Location:", lastMechLocation, CLASS_HDR_TEXT2));
    buf.append(hutils.endParagraph());
    buf.append(hutils.addBlankLine());

    return buf.toString();
  }

  private static final String GET_LAST_MECH_SQL =
      "select z.location_label from " +
          "niin_location_hist x, " +
          "(select niin_id,max(created_date) created_date from niin_location_hist " +
          "group by niin_Id ) y, location z, niin_Info w " +
          "where w.niin_id = ? and " +
          "x.niin_Id = y.niin_id and " +
          "x.created_date = y.created_date and " +
          "x.location_id = z.location_Id and " +
          "z.mechanized_flag <> 'N'  and " +
          "y.niin_Id = w.niin_id ";

  private static final String GET_LAST_NON_MECH_SQL =
      "select z.location_label from " +
          "niin_location_hist x, " +
          "(select niin_id,max(created_date) created_date from niin_location_hist " +
          "group by niin_Id ) y, " +
          "location z, niin_Info w " +
          "where w.niin_id = ? and " +
          "x.niin_Id = y.niin_id and " +
          "x.created_date = y.created_date and " +
          "x.location_id = z.location_Id and " +
          "z.mechanized_flag = 'N'  and " +
          "y.niin_Id = w.niin_id ";

  private String getLastMechLocation(long niinId, boolean mechanized) {
    String lastLocation = "n/a";
    PreparedStatement pstmt = null;

    try {
      if (mechanized) {
        pstmt = service.getDBTransaction().createPreparedStatement(GET_LAST_MECH_SQL, 0);
      }
      else {
        pstmt = service.getDBTransaction().createPreparedStatement(GET_LAST_NON_MECH_SQL, 0);
      }
      pstmt.setLong(1, niinId);
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          lastLocation = rs.getString(1);
        }
      }
    }
    catch (SQLException e) {
      unReportedError = true;
      unReportedErrorString = "getLastMechLocation() " + e.getLocalizedMessage();
    }
    finally {
      if (pstmt != null) {
        try { pstmt.close(); }
        catch (SQLException sqle) {
          //NO-OP
        }
      }
    }
    return lastLocation;
  }

  private String addOnHandDataByCode(ArrayList<ReportGabfInfo> gabfInfoData, boolean condA) {
    StringBuilder buf = new StringBuilder();
    StringBuilder diffBuf = new StringBuilder();
    String label = "GABF_ON_Hand_Serviceable:";
    String cc = "A";
    int serviceQty = 0;
    int onHandQty = 0;
    int diff = 0;

    // Loop over what we have and fill in the blanks.

    for (ReportGabfInfo info : gabfInfoData) {
      if (condA) {
        if (info.getCc().compareToIgnoreCase("A") == 0) {
          serviceQty += info.getServiceableQty();
          onHandQty += info.getQty();
          diff += info.getServiceableDiff();
        }
      }
      else {
        if (info.getCc().compareToIgnoreCase("F") == 0) {
          serviceQty += info.getUnserviceableQty();
          onHandQty += info.getQty();
          diff += info.getUnserviceableDiff();
        }
      }
    }

    if (!condA) {
      cc = "F";
      label = "GABF_ON_Hand_UnServiceable:";
    }
    if (diff < 0) {
      diff *= -1;
      diffBuf.append('(').append(diff).append(')');
    }
    else {
      diffBuf.append(diff);
    }

    buf.append(hutils.startStyledParagraph());
    buf.append(hutils.addStyledValuePair(label, String.valueOf(serviceQty), CLASS_HDR_TEXT2));
    buf.append(hutils.addStyledValuePair("STRATIS_ON_Hand_Qty:", String.valueOf(onHandQty)));
    buf.append(hutils.addStyledValuePair("CC:", cc));
    buf.append(hutils.addStyledValuePair("Diff:", diffBuf.toString()));
    buf.append(hutils.endParagraph());

    return buf.toString();
  }

  private static final String GET_GABF_INFO_SQL =
      "select serviceable_qty, unserviceable_qty, niin, ro_threshold, " +
          "operation_code, requirement_code, qty, activity_address_code, " +
          "cc, serviceable_diff, unserviceable_diff " +
          "from v_gabf_info where niin = ? ";

  private ArrayList<ReportGabfInfo> getGabfInfoData(String niin) {
    ArrayList<ReportGabfInfo> gabfData = new ArrayList<>();

    boolean dataFound = false;

    try (PreparedStatement pstmt = service.getDBTransaction().createPreparedStatement(GET_GABF_INFO_SQL, 0)) {
      pstmt.setString(1, niin);
      try (ResultSet rs = pstmt.executeQuery()) {

        while (rs.next()) {
          gabfData.add(new ReportGabfInfo(rs.getInt(1), rs.getInt(2), rs.getInt(7), rs.getString(9), rs.getInt(10), rs.getInt(11)));
          dataFound = true;
        }
      }
    }
    catch (SQLException e) {
      unReportedError = true;
      unReportedErrorString = "getGabfInfoData() " + e.getLocalizedMessage();
    }

    // If there was no data for this niin then add the info from our records.
    if (!dataFound) {
      getGabfInfoFromLocal(niin, gabfData);
    }
    return gabfData;
  }

  private static final String GET_GABF_INFO_LOCAL_SQL =
      "select qty, cc from niin_location where niin_id = ? ";

  private void getGabfInfoFromLocal(String niin, ArrayList<ReportGabfInfo> gabfData) {
    long niinId = getNiinId(niin);
    int serviceableDiff;
    int unserviceableDiff;

    try (PreparedStatement pstmt = service.getDBTransaction().createPreparedStatement(GET_GABF_INFO_LOCAL_SQL, 0)) {
      pstmt.setLong(1, niinId);
      try (ResultSet rs = pstmt.executeQuery()) {

        while (rs.next()) {
          int qty = rs.getInt(1);
          String cc = rs.getString(2);

          if (cc.compareTo("A") == 0) {
            serviceableDiff = qty;
            unserviceableDiff = 0;
          }
          else {
            serviceableDiff = 0;
            unserviceableDiff = qty;
          }
          gabfData.add(new ReportGabfInfo(0, 0, qty, cc, serviceableDiff, unserviceableDiff));
        }
      }
    }
    catch (SQLException e) {
      unReportedError = true;
      unReportedErrorString = "getGabfInfoFromLocal() " + e.getLocalizedMessage();
    }
  }

  private String addQuarterlyDataByCode(ArrayList<ReportQuarterlyUsage> quarterlyData, boolean condA) {
    StringBuilder buf = new StringBuilder();
    String range;
    // These should come back in order.
    int rowCount = 0;
    for (ReportQuarterlyUsage vo : quarterlyData) {
      buf.append(hutils.startStyledParagraph());
      if (condA) {
        range = vo.getDateRange();
        buf.append(hutils.addStyledValuePair(range, " CC: A", CLASS_HDR_TEXT2));
        buf.append(hutils.addStyledValuePair("Total Qty Received:", String.valueOf(vo.getQtyReceivedCodeA())));
        buf.append(hutils.addStyledValuePair("Total Qty Issued:", String.valueOf(vo.getQtyIssuedCodeA())));
      }
      else {
        range = vo.getDateRange();
        buf.append(hutils.addStyledValuePair(range, " CC: F", CLASS_HDR_TEXT2));
        buf.append(hutils.addStyledValuePair("Total Qty Received:", String.valueOf(vo.getQtyReceivedCodeF())));
        buf.append(hutils.addStyledValuePair("Total Qty Issued:", String.valueOf(vo.getQtyIssuedCodeF())));
      }
      buf.append(hutils.endParagraph());
      rowCount++;
      if (rowCount > 3) {  // Procedure is returning 5 lines so only show 4
        break;
      }
    }
    return buf.toString();
  }

  private static final String QUARTERLY_USAGE_BY_NIIN_SQL =
      "BEGIN pkg_hison.p_quarterly_usage_by_niin( ?, ?, ?); END;";

  private ArrayList<ReportQuarterlyUsage> getQuarterlyUsageData(String niin) {
    ArrayList<ReportQuarterlyUsage> quarterlyData = new ArrayList<>();

    try (CallableStatement stmt = service.getDBTransaction().createCallableStatement(QUARTERLY_USAGE_BY_NIIN_SQL, 0)) {
      stmt.setString(1, niin);
      stmt.setDate(2, null);
      stmt.registerOutParameter(3, OracleTypes.CURSOR);
      stmt.execute();
      try (ResultSet rs = ((OracleCallableStatement) stmt).getCursor(3)) {

        while (rs.next()) {
          quarterlyData.add(new ReportQuarterlyUsage(rs.getString(1), rs.getInt(2), rs.getInt(3), rs.getInt(4), rs.getInt(5), rs.getInt(6)));
        }
      }
    }
    catch (SQLException e) {
      unReportedError = true;
      unReportedErrorString = "getQuarterlyUsageData() " + e.getLocalizedMessage();
    }
    return quarterlyData;
  }

  private static final String GET_STOCK_DATA_BY_LOCATION_ID_SQL =
      "select c.serial_number, c.lot_con_num, c.qty, " +
          "b.recall_flag, " +
          "nvl(to_char(b.recall_date), '          ' ) " +
          "from location a, niin_location b, serial_lot_num_track c " +
          "where " +
          "b.location_id=a.location_id " +
          "AND b.location_id=c.location_id AND a.location_id=c.location_id " +
          "AND b.niin_id=c.niin_id " +
          "AND c.issued_flag='N' " +
          "AND b.niin_id = ? AND a.location_id = ? " +
          "order by c.serial_number, c.lot_con_num asc ";

  private void updateBuilderWithSerialLotData(StringBuilder buf, boolean isSerialRow, ResultSet rs) throws SQLException {
    int columnPosition = 0;
    while (rs.next()) {

      if (columnPosition == 0) {
        buf.append(hutils.startStyledParagraph());
        if (isSerialRow) {
          buf.append(hutils.addStyledValuePair("Serial#(s):", rs.getString(1), CLASS_HDR_TEXT2));
        }
        else {
          buf.append(hutils.addStyledValuePair("Lot#:(s)", rs.getString(2), CLASS_HDR_TEXT2));
        }
      }
      else {
        if (isSerialRow) {
          buf.append(hutils.addStyledValuePair(" ", rs.getString(1)));
        }
        else {
          buf.append(hutils.addStyledValuePair(" ", rs.getString(2)));
        }
      }
      columnPosition++;
      if (columnPosition >= MAX_COLUMNS) {
        buf.append(hutils.endParagraph());
        columnPosition = 0;
      }
    }
    if (columnPosition != 0) {
      buf.append(hutils.endParagraph());
    }
  }

  private String getStockDataRowsByLocationId(long niinId, long locationId, boolean isSerialRow) {
    StringBuilder buf = new StringBuilder();

    try (PreparedStatement pstmt = service.getDBTransaction().createPreparedStatement(GET_STOCK_DATA_BY_LOCATION_ID_SQL, 0)) {
      pstmt.setLong(1, niinId);
      pstmt.setLong(2, locationId);

      try (ResultSet rs = pstmt.executeQuery()) {
        updateBuilderWithSerialLotData(buf, isSerialRow, rs);
      }
    }
    catch (SQLException e) {
      buf.append(showSqlErrorMessageInResponse(e, "getStockDataRowsByLocationId()"));
    }
    return buf.toString();
  }

  private static final String CHECK_FOR_CURRENT_ISSUE_DATA_BY_TYPE_SQL =
      "select count(*) from issue i, niin_info ni " +
          "where trunc(sysdate) - trunc(i.created_date) <= " +
          PKG_HISON_REPORT_NUM_DAYS +
          " and i.niin_id = ni.niin_id and ni.niin = ? ";

  private static final String CHECK_FOR_HISTORICAL_ISSUE_DATA_BY_TYPE_SQL =
      "select count(*) from issue_hist ih, niin_info ni " +
          "where trunc(sysdate) - trunc(ih.created_date) <= " +
          PKG_HISON_REPORT_NUM_DAYS +
          " and ih.niin_id = ni.niin_id and ni.niin = ? ";

  private boolean checkForIssueDataByType(String niin, DataType dt, IssueType issueType) {
    boolean rc = false;
    StringBuilder sql = new StringBuilder();

    if (dt == DataType.CURRENT) {
      sql.append(CHECK_FOR_CURRENT_ISSUE_DATA_BY_TYPE_SQL);
    }
    else {
      sql.append(CHECK_FOR_HISTORICAL_ISSUE_DATA_BY_TYPE_SQL);
    }

    // Add issue type filter.
    sql.append(getIssueTypeFilter(issueType));

    try (PreparedStatement pstmt = service.getDBTransaction().createPreparedStatement(sql.toString(), 0)) {
      pstmt.setString(1, niin);

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          int count = rs.getInt(1);
          if (count > 0) {
            rc = true;
          }
        }
      }
    }
    catch (SQLException sqle) {
      log.error("checkForIssueDataByType", sqle);
    }
    return rc;
  }

  private static final String CHECK_FOR_CURRENT_RECEIPT_DATA_SQL =
      "select count(*) from receipt r, niin_info ni " +
          "where trunc(sysdate) - trunc(r.created_date) <= " +
          PKG_HISON_REPORT_NUM_DAYS +
          " and r.niin_id = ni.niin_id and ni.niin = ? order by r.created_date desc ";

  private static final String CHECK_FOR_HISTORICAL_RECEIPT_DATA_SQL =
      "select count(*) from receipt_hist r, niin_info ni " +
          "where trunc(sysdate) - trunc(r.created_date) <= " +
          PKG_HISON_REPORT_NUM_DAYS +
          " and r.niin_id = ni.niin_id and ni.niin = ? order by r.created_date desc ";

  private boolean checkForReceiptDataByType(String niin, DataType dt) {
    boolean rc = false;
    String sql = CHECK_FOR_CURRENT_RECEIPT_DATA_SQL;

    if (dt == DataType.HISTORICAL) {
      sql = CHECK_FOR_HISTORICAL_RECEIPT_DATA_SQL;
    }

    try (PreparedStatement pstmt = service.getDBTransaction().createPreparedStatement(sql, 0)) {
      pstmt.setString(1, niin);

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          int count = rs.getInt(1);
          if (count > 0) {
            rc = true;
          }
        }
      }
    }
    catch (SQLException sqle) {
      log.error("checkForReceiptDataByType", sqle);
    }

    return rc;
  }

  private static final String CHECK_FOR_CURRENT_INVENTORY_DATA_SQL =
      "select count(*) from inventory_item " +
          "where trunc(sysdate) - trunc( created_date) <= " +
          PKG_HISON_REPORT_NUM_DAYS +
          " and status <> 'INVENTORYPENDING' and niin_id = ? order by created_date desc";

  private static final String CHECK_FOR_HISTORICAL_INVENTORY_DATA_SQL =
      "select count(*) from inventory_item_hist " +
          "where trunc(sysdate) - trunc( created_date) <= " +
          PKG_HISON_REPORT_NUM_DAYS +
          " and status <> 'INVENTORYPENDING' and niin_id = ? order by created_date desc";

  private boolean checkForInventoryDataByType(long niinId, DataType dt) {
    boolean rc = false;
    String sql = CHECK_FOR_CURRENT_INVENTORY_DATA_SQL;

    if (dt == DataType.HISTORICAL) {
      sql = CHECK_FOR_HISTORICAL_INVENTORY_DATA_SQL;
    }

    try (PreparedStatement pstmt = service.getDBTransaction().createPreparedStatement(sql, 0)) {
      pstmt.setLong(1, niinId);
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          int count = rs.getInt(1);
          if (count > 0) {
            rc = true;
          }
        }
      }
    }
    catch (SQLException sqle) {
      log.error("checkForInventoryDataByType", sqle);
    }
    return rc;
  }

  private static final String GET_CURRENT_ISSUE_DATA_SQL =
      "select scn, document_number, suffix, created_date, ui, cc, " +
          "issue_priority_designator, qty, niin_id, niin " +
          "from v_current_issue_data where niin = ? ";

  private static final String GET_HISTORICAL_ISSUE_DATA_SQL =
      "select scn, document_number, suffix, created_date, ui, cc, " +
          "issue_priority_designator, qty, niin_id, niin " +
          "from v_historical_issue_data where niin = ? ";

  private String addIssueData(String niin, DataType dt, IssueType issueType) {
    StringBuilder buf = new StringBuilder();
    StringBuilder sql = new StringBuilder();

    if (dt == DataType.HISTORICAL) {
      sql.append(GET_HISTORICAL_ISSUE_DATA_SQL);
    }
    else {
      sql.append(GET_CURRENT_ISSUE_DATA_SQL);
    }

    sql.append(getIssueTypeFilter(issueType));
    sql.append(" order by created_date desc");

    try (PreparedStatement pstmt = service.getDBTransaction().createPreparedStatement(sql.toString(), 0)) {
      pstmt.setString(1, niin);

      try (ResultSet rs = pstmt.executeQuery()) {

        while (rs.next()) {

          buf.append(hutils.addBlankLine());

          // Need these for picks so cull them out here.
          String scn = rs.getString(1);
          String documentNumber = rs.getString(2);

          // SCN,  Document Number, Suffix, Niin, Issue Qty
          buf.append(hutils.startStyledParagraph());
          buf.append(hutils.addStyledValuePair("SCN:", scn, CLASS_HDR_TEXT));
          buf.append(hutils.addStyledValuePair(DOC_NUMBER_LABEL, documentNumber));
          buf.append(hutils.addStyledValuePair("Suffix:", rs.getString(3)));
          buf.append(hutils.addStyledValuePair(NIIN_LABEL, niin));
          buf.append(hutils.addStyledValuePair("Issue Qty:", String.valueOf(rs.getInt(8))));
          buf.append(hutils.endParagraph());

          // Created Date, cc, ui, priority
          buf.append(hutils.startStyledParagraph());
          buf.append(hutils.addStyledValuePair("Created Date:", getFormattedDate(rs.getString(4)), CLASS_HDR_TEXT2));
          buf.append(hutils.addStyledValuePair("CC:", rs.getString(6)));
          buf.append(hutils.addStyledValuePair("UI:", rs.getString(5)));
          buf.append(hutils.addStyledValuePair("Priority:", rs.getString(7)));
          buf.append(hutils.endParagraph());
          buf.append(hutils.addBlankLine());

          buf.append(addPickData(scn, dt));
        }
      }
    }
    catch (SQLException sqle) {
      buf.append(showSqlErrorMessageInResponse(sqle, "addIssueData"));
    }

    return buf.toString();
  }

  private static final String GET_CURRENT_PICK_DATA_SQL =
      "select pin, " +
          "created_date," +
          "niin_loc_id, " +
          "qty_picked, " +
          "NVL(pkg_strat_func.f_get_user(picked_by), 'n/a') picked_by, " +
          "time_of_pick, " +
          "NVL( pick_qty, 0 ), " +
          "status, " +
          "pid, " +
          "packing_consolidation_id,  " +
          "refused_flag, " +
          "NVL(pkg_strat_func.f_get_user(refused_by), 'n/a') refused_by, " +
          "refused_date " +
          "FROM picking " +
          "WHERE trunc(sysdate) - trunc(created_date) <= " +
          PKG_HISON_REPORT_NUM_DAYS +
          " and scn = ? order by created_date desc";

  private static final String GET_HISTORICAL_PICK_DATA_SQL =
      "select pin, " +
          "created_date," +
          "niin_loc_id, " +
          "qty_picked, " +
          "NVL(pkg_strat_func.f_get_user(picked_by), 'n/a') picked_by, " +
          "time_of_pick, " +
          "NVL( pick_qty, 0 ), " +
          "status, " +
          "pid, " +
          "packing_consolidation_id, " +
          "refused_flag, " +
          "NVL(pkg_strat_func.f_get_user(refused_by), 'n/a') refused_by, " +
          "refused_date " +
          "FROM picking_hist " +
          "WHERE trunc(sysdate) - trunc(created_date) <= " +
          PKG_HISON_REPORT_NUM_DAYS +
          " and scn = ? order by created_date desc";

  private String addPickData(String scn, DataType dt) {
    StringBuilder buf = new StringBuilder();
    boolean pickRefused;
    int pickedQty;
    int refusedQty;

    String sql = GET_CURRENT_PICK_DATA_SQL;

    if (dt == DataType.HISTORICAL) {
      sql = GET_HISTORICAL_PICK_DATA_SQL;
    }

    try (PreparedStatement pstmt = service.getDBTransaction().createPreparedStatement(sql, 0)) {
      pstmt.setString(1, scn);

      try (ResultSet rs = pstmt.executeQuery()) {

        while (rs.next()) {

          pickedQty = rs.getInt(4);
          refusedQty = rs.getInt(7);

          pickRefused = checkPickRefusalState(rs.getString(8), rs.getString(11));

          if (!pickRefused) {
            refusedQty = 0;
          }

          buf.append(hutils.addBlankLine());
          buf.append(hutils.startStyledParagraph());
          buf.append(hutils.addStyledValuePair("PIN#:", rs.getString(1), CLASS_HDR_TEXT2));
          String locationLabel = getLocationLabelByNiinLocId(rs.getLong(3));
          buf.append(checkAndReportQueryStatus());
          buf.append(hutils.addStyledValuePair(LOCATION_LABEL, locationLabel));
          buf.append(hutils.addStyledValuePair("Picked Qty:", String.valueOf(pickedQty)));
          buf.append(hutils.addStyledValuePair("Refused Qty:", String.valueOf(refusedQty)));
          buf.append(hutils.endParagraph());

          buf.append(hutils.startStyledParagraph());
          buf.append(hutils.addStyledValuePair("Picked By:", rs.getString(5), CLASS_HDR_TEXT2));
          buf.append(hutils.addStyledValuePair("Date Picked:", getFormattedDate(rs.getString(6)))); // Time
          buf.append(hutils.endParagraph());

          if (pickRefused) {
            buf.append(hutils.startStyledParagraph());
            buf.append(hutils.addStyledValuePair("Refused By:", rs.getString(12), CLASS_HDR_TEXT2));
            buf.append(hutils.addStyledValuePair("Date Refused:", getFormattedDate(rs.getString(13)))); // Time
            buf.append(hutils.endParagraph());
          }

          buf.append(addPackingData(rs.getLong(10), dt));

          if (!pickRefused) {
            if (isSerialControlled) {
              buf.append(addSerialLotData(rs.getLong(9), dt, true));
            }
            if (isLotControlled) {
              buf.append(addSerialLotData(rs.getLong(9), dt, false));
            }
            buf.append(addShippingDataByScn(scn, dt));
          }
        }
      }
    }
    catch (SQLException sqle) {
      buf.append(showSqlErrorMessageInResponse(sqle, "addPickData"));
    }

    return buf.toString();
  }

  private boolean checkPickRefusalState(String status, String refusalFlag) {
    return (status != null && status.compareToIgnoreCase("PICK REFUSED") == 0) ||
        (refusalFlag != null && refusalFlag.compareToIgnoreCase("Y") == 0);
  }

  private static final String GET_LOCATION_LABEL_SQL =

      "select location_label, nl.location_id from niin_location nl, location loc " +
          "where nl.location_id = loc.location_id and nl.niin_loc_id = ? " +
          "union " +
          "select location_label, nl_h.location_id from niin_location_hist nl_h, location loc_h " +
          "where nl_h.location_id = loc_h.location_id and nl_h.niin_loc_id = ? ";

  private String getLocationLabelByNiinLocId(long niinLocId) {
    String label = "n/a";

    if (niinLocId > 0L) {

      try (PreparedStatement pstmt = service.getDBTransaction().createPreparedStatement(GET_LOCATION_LABEL_SQL, 0)) {

        pstmt.setLong(1, niinLocId);
        pstmt.setLong(2, niinLocId);

        try (ResultSet rs = pstmt.executeQuery()) {

          if (rs.next()) {
            label = rs.getString(1);
          }
        }
      }
      catch (SQLException e) {
        unReportedError = true;
        unReportedErrorString = "getLocationLabelByNiinLocId() " + e.getLocalizedMessage();
      }
    }
    return label;
  }

  private static final String GET_CURRENT_PACKING_DATA_SQL =

      "SELECT nvl(e.name, 'n/a') equip_name, " +
          "p.packed_date, " +
          "NVL( PKG_STRAT_FUNC.F_get_user(p.packed_by), 'n/a' ) packed_by " +
          "FROM packing_consolidation p, packing_station ps, equip e " +
          "WHERE p.packing_station_id = ps.packing_station_id " +
          "AND ps.equipment_number = e.equipment_number " +
          "AND p.packing_consolidation_id = ? order by p.packed_date desc ";

  private static final String GET_HISTORICAL_PACKING_DATA_SQL =

      "SELECT nvl(e.name, 'n/a') equip_name, " +
          "p.packed_date, " +
          "NVL( PKG_STRAT_FUNC.F_get_user(p.packed_by), 'n/a' ) packed_by " +
          "FROM packing_consolidation_hist p, packing_station ps, equip e " +
          "WHERE p.packing_station_id = ps.packing_station_id " +
          "AND ps.equipment_number = e.equipment_number " +
          "AND p.packing_consolidation_id = ? order by p.packed_date desc";

  private String addPackingData(long packingId, DataType dt) {
    StringBuilder buf = new StringBuilder();
    String sql = GET_CURRENT_PACKING_DATA_SQL;

    if (packingId > 0L) {
      if (dt == DataType.HISTORICAL) {
        sql = GET_HISTORICAL_PACKING_DATA_SQL;
      }

      try (PreparedStatement pstmt = service.getDBTransaction().createPreparedStatement(sql, 0)) {
        pstmt.setLong(1, packingId);
        try (ResultSet rs = pstmt.executeQuery()) {

          if (rs.next()) {
            buf.append(hutils.startStyledParagraph());
            buf.append(hutils.addStyledValuePair("Packed By:", rs.getString(3), CLASS_HDR_TEXT2));
            buf.append(hutils.addStyledValuePair("Packed Date:", getFormattedDate(rs.getString(2))));
            buf.append(hutils.endParagraph());
          }
        }
      }
      catch (SQLException sqle) {
        buf.append(showSqlErrorMessageInResponse(sqle, "addPackingData"));
      }
    }
    return buf.toString();
  }

  private static final String GET_CURRENT_SERIAL_LOT_NUM_DATA_SQL =
      "SELECT NVL( slnt.serial_number, 'n/a' ), " +
          "NVL( slnt.lot_con_num, 'n/a' ) " +
          "FROM pick_serial_lot_num psln, serial_lot_num_track slnt " +
          "WHERE psln.serial_lot_num_track_id = slnt.serial_lot_num_track_id " +
          "AND psln.pid = ? ";

  private static final String GET_HISTORICAL_SERIAL_LOT_NUM_DATA_SQL =
      "SELECT NVL( slnt.serial_number, 'n/a' ), " +
          "NVL( slnt.lot_con_num, 'n/a' ) " +
          "FROM pick_serial_lot_num_hist psln, serial_lot_num_track_hist slnt " +
          "WHERE psln.serial_lot_num_track_id = slnt.serial_lot_num_track_id " +
          "AND psln.pid = ? ";

  private String addSerialLotData(long pid, DataType dt, boolean isSerialRow) {
    StringBuilder buf = new StringBuilder();
    String sql = GET_CURRENT_SERIAL_LOT_NUM_DATA_SQL;

    if (dt == DataType.HISTORICAL) {
      sql = GET_HISTORICAL_SERIAL_LOT_NUM_DATA_SQL;
    }

    try (PreparedStatement pstmt = service.getDBTransaction().createPreparedStatement(sql, 0)) {
      pstmt.setLong(1, pid);
      try (ResultSet rs = pstmt.executeQuery()) {
        updateBuilderWithSerialLotData(buf, isSerialRow, rs);
      }
    }
    catch (SQLException sqle) {
      buf.append(showSqlErrorMessageInResponse(sqle, "addSerialLotData"));
    }
    return buf.toString();
  }

  private String getIssueTypeFilter(IssueType issueType) {
    String filterString;

    switch (issueType) {
      case WALKTHRU:
        filterString = "  AND issue_type = 'W' ";
        break;
      case REWAREHOUSE:
        filterString = " AND issue_type = 'R' ";
        break;
      case CROSSDOCK:
        filterString = " AND issue_type = 'B' ";
        break;
      default:
        filterString = " AND issue_type is null ";
        break;
    }
    return filterString;
  }

  private static final String GET_CURRENT_RECEIPT_DATA_SQL =
      "select rcn, document_number, suffix, cc, ui, created_date, " +
          "quantity_invoiced, quantity_inducted, created_by, " +
          "remaining_due_in, quantity_xdock, niin_id " +
          "from v_current_receipt_data where niin_id = ? order by created_date desc ";

  private static final String GET_HISTORICAL_RECEIPT_DATA_SQL =
      "select rcn, document_number, suffix, cc, ui, created_date, " +
          "quantity_invoiced, quantity_inducted, created_by, " +
          "remaining_due_in, quantity_xdock, niin_id " +
          "from v_historical_receipt_data where niin_id = ? order by created_date desc ";

  private String addReceiptStowData(String niin, long niinId, DataType dt) {
    StringBuilder buf = new StringBuilder();
    String sql = GET_CURRENT_RECEIPT_DATA_SQL;

    if (dt == DataType.HISTORICAL) {
      sql = GET_HISTORICAL_RECEIPT_DATA_SQL;
    }

    try (PreparedStatement pstmt = service.getDBTransaction().createPreparedStatement(sql, 0)) {
      pstmt.setLong(1, niinId);

      try (ResultSet rs = pstmt.executeQuery()) {

        while (rs.next()) {
          int rcn = rs.getInt(1);
          String documentNumber = rs.getString(2);

          buf.append(hutils.addBlankLine());
          buf.append(hutils.startStyledParagraph());
          buf.append(hutils.addStyledValuePair("RCN:", String.valueOf(rcn), CLASS_HDR_TEXT));
          buf.append(hutils.addStyledValuePair(DOC_NUMBER_LABEL, documentNumber));
          buf.append(hutils.addStyledValuePair("Suffix:", rs.getString(3)));
          buf.append(hutils.addStyledValuePair(NIIN_LABEL, niin));
          buf.append(hutils.addStyledValuePair("CC:", rs.getString(4)));
          buf.append(hutils.addStyledValuePair("UI:", rs.getString(5)));
          buf.append(hutils.endParagraph());

          buf.append(hutils.startStyledParagraph());
          buf.append(hutils.addStyledValuePair("Created Date:", getFormattedDate(rs.getString(6)), CLASS_HDR_TEXT2));
          buf.append(hutils.endParagraph());

          buf.append(hutils.startStyledParagraph());
          buf.append(hutils.addStyledValuePair("Invoice Qty:", String.valueOf(rs.getInt(7)), CLASS_HDR_TEXT2));
          buf.append(hutils.addStyledValuePair("Qty Received:", String.valueOf(rs.getInt(8))));
          buf.append(hutils.addStyledValuePair("Qty XDock:", String.valueOf(rs.getInt(11))));
          buf.append(hutils.endParagraph());

          buf.append(hutils.startStyledParagraph());
          buf.append(hutils.addStyledValuePair("Received By:", rs.getString(9), CLASS_HDR_TEXT2));
          buf.append(hutils.addStyledValuePair("DASF Remain Due In:", String.valueOf(rs.getInt(10))));
          buf.append(hutils.endParagraph());

          buf.append(addStowData(rcn, dt));
        }
      }
    }
    catch (SQLException sqle) {
      buf.append(showSqlErrorMessageInResponse(sqle, "addReceiptStowData"));
    }

    return buf.toString();
  }

  private static final String GET_CURRENT_STOW_DATA =
      "select sid, location_id, location_label, status, cc, " +
          "stowed_date, stowed_by, serial_number, lot_con_num, " +
          "stow_qty, rcn from v_current_stow_data " +
          "where rcn = ? order by stowed_date desc ";

  private static final String GET_HISTORICAL_STOW_DATA =
      "select sid, location_id, location_label, status, cc, " +
          "stowed_date, stowed_by, serial_number, lot_con_num, " +
          "stow_qty, rcn from v_historical_stow_data " +
          "where rcn = ? order by stowed_date desc ";

  private String addStowData(int rcn, DataType dt) {
    StringBuilder buf = new StringBuilder();
    String sql = GET_CURRENT_STOW_DATA;

    if (dt == DataType.HISTORICAL) {
      sql = GET_HISTORICAL_STOW_DATA;
    }
    try (PreparedStatement pstmt = service.getDBTransaction().createPreparedStatement(sql, 0)) {
      pstmt.setInt(1, rcn);
      try (ResultSet rs = pstmt.executeQuery()) {

        while (rs.next()) {
          buf.append(hutils.addBlankLine());

          buf.append(hutils.startStyledParagraph());
          buf.append(hutils.addStyledValuePair("SID:", rs.getString(1), CLASS_HDR_TEXT3));
          buf.append(hutils.addStyledValuePair("Status:", rs.getString(4)));
          buf.append(hutils.endParagraph());

          buf.append(hutils.startStyledParagraph());
          buf.append(hutils.addStyledValuePair(LOCATION_LABEL, rs.getString(3), CLASS_HDR_TEXT3));
          buf.append(hutils.addStyledValuePair("Qty Stowed:", String.valueOf(rs.getInt(10))));
          buf.append(hutils.endParagraph());

          buf.append(hutils.startStyledParagraph());
          buf.append(hutils.addStyledValuePair("Stowed By:", rs.getString(7), CLASS_HDR_TEXT3));
          buf.append(hutils.endParagraph());

          buf.append(hutils.startStyledParagraph());
          buf.append(hutils.addStyledValuePair("Stowed Date:", showValueOrNA(getFormattedDate(rs.getString(6))), CLASS_HDR_TEXT3));
          buf.append(hutils.endParagraph());

          if (isSerialControlled) {
            buf.append(hutils.startStyledParagraph());
            buf.append(hutils.addStyledValuePair("Serial#:", rs.getString(8), CLASS_HDR_TEXT3));
            buf.append(hutils.endParagraph());
          }

          if (isLotControlled) {
            buf.append(hutils.startStyledParagraph());
            buf.append(hutils.addStyledValuePair("Lot Number:", rs.getString(9), CLASS_HDR_TEXT3));
            buf.append(hutils.endParagraph());
          }
        }
      }
    }
    catch (SQLException sqle) {
      buf.append(showSqlErrorMessageInResponse(sqle, "addStowData"));
    }
    return buf.toString();
  }

  private static final String GET_CURRENT_INVENTORY_DATA_SQL =
      "select created_date, created_by_nvl, location_label_nvl, cum_pos_adj, " +
          "cum_neg_adj, inv_type_nvl, last_name, first_name, " +
          "niin, ui_nvl, cc_nvl, location_label, " +
          "qty, status_nvl, counted_by_nvl, modified_date_nvl, " +
          "num_counted, niin_id, modified_by_nvl, modified_date, " +
          "location_id, inventory_item_id, niin_loc_qty " +
          "from v_current_inventory_data where niin_id = ? order by created_date desc ";

  private static final String GET_HISTORICAL_INVENTORY_DATA_SQL =
      "select created_date, created_by_nvl, location_label_nvl, cum_pos_adj, " +
          "cum_neg_adj, inv_type_nvl, last_name, first_name, " +
          "niin, ui_nvl, cc_nvl, location_label, " +
          "qty, status_nvl, counted_by_nvl, modified_date_nvl, " +
          "num_counted, niin_id, modified_by_nvl, modified_date, " +
          "location_id, inventory_item_id, niin_loc_qty " +
          "from v_historical_inventory_data where niin_id = ? order by created_date desc ";

  private String addInventoryData(String niin, long niinId, DataType dt) {
    StringBuilder buf = new StringBuilder();
    String sql = GET_CURRENT_INVENTORY_DATA_SQL;

    if (dt == DataType.HISTORICAL) {
      sql = GET_HISTORICAL_INVENTORY_DATA_SQL;
    }

    try (PreparedStatement pstmt = service.getDBTransaction().createPreparedStatement(sql, 0)) {
      pstmt.setLong(1, niinId);
      try (ResultSet rs = pstmt.executeQuery()) {

        while (rs.next()) {
          buf.append(hutils.addBlankLine());

          buf.append(hutils.startStyledParagraph());
          buf.append(hutils.addStyledValuePair(NIIN_LABEL, niin, CLASS_HDR_TEXT2));
          buf.append(hutils.addStyledValuePair("Created Date:", getFormattedDate(rs.getString(1))));
          buf.append(hutils.addStyledValuePair("Created By:", rs.getString(2)));
          buf.append(hutils.endParagraph());

          buf.append(hutils.startStyledParagraph());
          buf.append(hutils.addStyledValuePair("Description:", rs.getString(6), CLASS_HDR_TEXT3));
          buf.append(hutils.addStyledValuePair("UI:", rs.getString(10)));
          buf.append(hutils.addStyledValuePair("CC:", rs.getString(11)));
          buf.append(hutils.endParagraph());

          buf.append(hutils.startStyledParagraph());
          buf.append(hutils.addStyledValuePair(LOCATION_LABEL, rs.getString(3), CLASS_HDR_TEXT3));
          buf.append(hutils.addStyledValuePair("Location Qty:", String.valueOf(rs.getLong(23))));  // Was 13
          buf.append(hutils.addStyledValuePair("Status:", rs.getString(14)));
          buf.append(hutils.endParagraph());

          buf.append(hutils.startStyledParagraph());
          buf.append(hutils.addStyledValuePair("Counted By:", rs.getString(15), CLASS_HDR_TEXT3));
          buf.append(hutils.addStyledValuePair("Date Counted:", getFormattedDate(rs.getString(16), DateConstants.ADF_REPORT_DATE_TIME_WITH_JULIAN_IN_THE_MIDDLE_WITH_DASHES, true, true)));

          buf.append(hutils.endParagraph());

          buf.append(hutils.startStyledParagraph());
          buf.append(hutils.addStyledValuePair("Pos. Adj:", rs.getString(4), CLASS_HDR_TEXT3));
          buf.append(hutils.addStyledValuePair("Neg. Adj:", rs.getString(5)));
          buf.append(hutils.addStyledValuePair("Num Counted:", String.valueOf(rs.getLong(17))));
          buf.append(hutils.endParagraph());

          buf.append(hutils.startStyledParagraph());
          buf.append(hutils.addStyledValuePair("Released By:", rs.getString(19), CLASS_HDR_TEXT3));
          buf.append(hutils.addStyledValuePair("Released Date:", getFormattedDate(rs.getString(20), DateConstants.ADF_REPORT_DATE_TIME_WITH_JULIAN_IN_THE_MIDDLE_WITH_DASHES, true, true)));
          buf.append(hutils.endParagraph());

          if (isSerialControlled || isLotControlled) {
            buf.append(addInventorySerialLotNumItems(rs.getLong(22), isSerialControlled));
            buf.append(checkAndReportQueryStatus());
          }
        }
      }
    }
    catch (SQLException sqle) {
      buf.append(showSqlErrorMessageInResponse(sqle, "addInventoryData() (niin: " + niin + " niin_id: " + niinId + " type: " + dt + ")"));
    }
    return buf.toString();
  }

  private static final String GET_INV_ITEM_SQL =
      "select " +
          "serial_number, " +
          "lot_con_num " +
          "from v_inv_serial_lot_num where inventory_item_id = ? " +
          "order by timestamp desc ";

  private String addInventorySerialLotNumItems(long inventoryItemId, boolean isSerialRow) {
    StringBuilder buf = new StringBuilder();

    try (PreparedStatement pstmt = service.getDBTransaction().createPreparedStatement(GET_INV_ITEM_SQL, 0)) {
      pstmt.setLong(1, inventoryItemId);
      try (ResultSet rs = pstmt.executeQuery()) {
        updateBuilderWithSerialLotData(buf, isSerialRow, rs);
      }
    }
    catch (SQLException sqle) {
      unReportedError = true;
      unReportedErrorString = "addInventorySerialLotNumItems inventory_item_id: " + inventoryItemId + " errror: " + sqle.getLocalizedMessage();
    }

    return buf.toString();
  }

  private static final String GET_SPOOL_INFO_SQL =
      "select timestamp, transaction_type, " +
          "status, qty, document_number, " +
          "ui, cc, pri, spool_id, niin_id,  " +
          "spool_def_mode, timestamp_sent, spool_batch_num, rcn, scn " +
          "from v_spool_info where niin_id = ? order by timestamp DESC";

  private ArrayList<ReportSpoolInfo> getSpoolInfoData(long niinId) {
    val dateService = ContextUtils.getBean(DateService.class);
    ArrayList<ReportSpoolInfo> spoolData = new ArrayList<>();
    long lastSpoolId = 0L;

    try (PreparedStatement pstmt = service.getDBTransaction().createPreparedStatement(GET_SPOOL_INFO_SQL, 0)) {
      pstmt.setLong(1, niinId);
      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          lastSpoolId = rs.getLong(9);
          val timestamp = dateService.getOffsetDateTimeFromResultSet(rs, "timestamp");
          OffsetDateTime timestampSent = null;
          if (rs.getObject("timestamp_sent") != null) {
            timestampSent = dateService.getOffsetDateTimeFromResultSet(rs, "timestamp_sent");
          }

          spoolData.add(new ReportSpoolInfo(timestamp, rs.getString(2),
              rs.getString(3), rs.getInt(4), rs.getString(5), rs.getString(6),
              rs.getString(7), rs.getString(8), rs.getLong(9), rs.getLong(10),
              rs.getString(11), timestampSent, rs.getLong(13),
              rs.getString(14), rs.getString(15)));
        }
      }
    }
    catch (SQLException e) {
      unReportedError = true;
      unReportedErrorString = "getSpoolInfoData() last spool_id(" + lastSpoolId + ") " + e.getLocalizedMessage();
    }
    return spoolData;
  }

  private String addSpoolInfoByCode(ArrayList<ReportSpoolInfo> reportSpoolInfo, String codeString, String label) {
    val dateService = ContextUtils.getBean(DateService.class);

    StringBuilder buf = new StringBuilder();
    boolean needHeader = true;

    for (ReportSpoolInfo info : reportSpoolInfo) {
      if (info.getTransactionType().startsWith(codeString)) {
        if (needHeader) {
          buf.append(addTransactionHeader(label));
          needHeader = false;
        }

        String niin = getNiin(info.getNiinId());

        buf.append(hutils.addBlankLine());
        buf.append(hutils.startStyledParagraph());
        buf.append(hutils.addStyledValuePair("Transaction Type:", info.getTransactionType()));
        buf.append(hutils.endParagraph());

        buf.append(hutils.startStyledParagraph());
        if (info.getRcn().compareTo("n/a") != 0) {
          buf.append(hutils.addStyledValuePair("RCN:", info.getRcn(), CLASS_HDR_TEXT2));
          buf.append(hutils.addStyledValuePair(DOC_NUMBER_LABEL, info.getDocumentNumber()));
        }
        else if (info.getScn().compareTo("n/a") != 0) {
          buf.append(hutils.addStyledValuePair("SCN:", info.getScn(), CLASS_HDR_TEXT2));
          buf.append(hutils.addStyledValuePair(DOC_NUMBER_LABEL, info.getDocumentNumber()));
        }
        else {
          buf.append(hutils.addStyledValuePair(DOC_NUMBER_LABEL, info.getDocumentNumber(), CLASS_HDR_TEXT2));
        }
        buf.append(hutils.addStyledValuePair(NIIN_LABEL, niin));
        buf.append(hutils.addStyledValuePair("Qty:", String.valueOf(info.getQty())));
        buf.append(hutils.endParagraph());

        buf.append(hutils.startStyledParagraph());
        buf.append(hutils.addStyledValuePair("Timestamp:", dateService.shiftAndFormatDate(info.getTimeStamp(), DateConstants.SITE_DATE_WITH_TIME_FORMATTER_PATTERN), CLASS_HDR_TEXT2));
        buf.append(hutils.addStyledValuePair("Spool id:", String.valueOf(info.getSpoolId())));
        buf.append(hutils.endParagraph());

        buf.append(hutils.startStyledParagraph());
        buf.append(hutils.addStyledValuePair("Status Code:", info.getStatusCode(), CLASS_HDR_TEXT2));
        buf.append(hutils.addStyledValuePair("UI:", info.getUnitOfIssue()));
        buf.append(hutils.addStyledValuePair("CC:", info.getConditionCode()));
        buf.append(hutils.addStyledValuePair("Priority:", info.getPriority()));
        buf.append(hutils.endParagraph());

        // Removed serial info from spool not to overwhelm the user
        // this information can be cross referenced elsewhere in the report

        buf.append(hutils.startStyledParagraph());
        buf.append(hutils.addStyledValuePair("Batch Number:", String.valueOf(info.getSpoolBatchNum()), CLASS_HDR_TEXT2));
        String dateSent = "n/a";
        if (info.getTimeStampSent() != null) {
          dateSent = dateService.shiftAndFormatDate(info.getTimeStampSent(), DateConstants.SITE_DATE_WITH_TIME_FORMATTER_PATTERN);
        }
        buf.append(hutils.addStyledValuePair("Date Sent: ", dateSent));
        buf.append(hutils.endParagraph());
      }
    }

    return buf.toString();
  }

  private String addTransactionHeader(String transactionCode) {
    return hutils.addBlankLine() +
        hutils.startStyledParagraph() +
        hutils.addStyledText("<b>" + transactionCode + " Transactions:" + "</b>", CLASS_HDR_TEXT, STYLE_NO_MARGIN_NO_BOTTOM) +
        hutils.endParagraph();
  }

  private String showSqlErrorMessageInResponse(SQLException e, String method) {
    return "<p>SQL Error: " +
        e.getMessage() +
        " in " +
        CLASS_NAME + ":" + method +
        "</p>";
  }

  private String checkAndReportQueryStatus() {
    StringBuilder buf = new StringBuilder();
    if (unReportedError) {
      buf.append("<p><b>Error: ");
      buf.append(unReportedErrorString);
      buf.append("</b></p>");
    }
    unReportedError = false;
    unReportedErrorString = "";
    return buf.toString();
  }
}


