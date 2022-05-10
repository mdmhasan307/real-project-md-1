package mil.stratis.model.services;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.common.util.RegUtils;
import mil.stratis.common.util.Util;
import mil.stratis.model.util.DBUtil;
import mil.stratis.model.view.rcv.*;
import mil.usmc.mls2.stratis.core.domain.model.Location;
import mil.usmc.mls2.stratis.core.domain.model.Receipt;
import mil.usmc.mls2.stratis.core.processor.ReceiptProcessor;
import mil.usmc.mls2.stratis.core.processor.ReceiptSuffixProcessor;
import mil.usmc.mls2.stratis.core.service.LocationService;
import mil.usmc.mls2.stratis.core.service.ReceiptService;
import mil.usmc.mls2.stratis.core.utility.AdfLogUtility;
import mil.usmc.mls2.stratis.core.utility.ContextUtils;
import mil.usmc.mls2.stratis.core.utility.DateConstants;
import oracle.jbo.Row;
import oracle.jbo.ViewCriteria;
import oracle.jbo.ViewCriteriaRow;
import oracle.jbo.domain.Date;
import oracle.jbo.server.ApplicationModuleImpl;
import oracle.jbo.server.ViewLinkImpl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.TreeSet;

@Slf4j
@NoArgsConstructor //ADF BackingHandlers all need a no args constructor
public class ReceiptAMImpl extends ApplicationModuleImpl {

  public static final String NIIN_ID = "NiinId";
  public static final String QUANTITY_INDUCTED = "QuantityInducted";
  public static final String WAC_ID = "WacId";
  public static final String NIIN_ID_STR = "niinIdStr";
  public static final String LOCATION_LABEL = "LocationLabel";
  public static final String LOCATION_ID = "LocationId";
  public static final String NIIN_STR = "niinStr";
  public static final String QUANTITY_MEASURED = "QuantityMeasured";

  /**
   * Validation method for Cube checking
   * Validates the given data from RECEIPT and
   * Updates the cube and dimensions only on to NIIN_INFO if they are all null.
   */
  public String validateNiinMeasurementsCube(Row row) {
    try {
      NiinInfoLVOImpl niinInfoView = getNiinInfoLVO2();
      NiinInfoLVORowImpl niinRow = (NiinInfoLVORowImpl) niinInfoView.getCurrentRow();

      //* capture the dimensional data entered on the receipt form
      double receiptLength = Util.cleanDouble(row.getAttribute("Length"));
      double receiptWidth = Util.cleanDouble(row.getAttribute("Width"));
      double receiptHeight = Util.cleanDouble(row.getAttribute("Height"));

      if (receiptLength <= 0 || receiptWidth <= 0 || receiptHeight <= 0) {
        return "RECEIPT_CUBE_REQ";
      }

      double receiptCube = receiptLength * receiptWidth * receiptHeight;
      int quantityMeasured = Util.cleanInt(row.getAttribute(QUANTITY_MEASURED));
      if (quantityMeasured < 1)
        quantityMeasured = 1;
      String niinId = Util.cleanString(niinRow.getNiinId());

      //* the NIIN is stored in STRATIS with dimensional data
      double niinCube = Util.cleanDouble(niinRow.getCube());

      //* Compare entered Length, Width, Height to Cube value in STRATIS
      //* return false if different. Also set the length, width, height of RECEIPT table
      if (receiptLength > 0 && receiptWidth > 0 && receiptHeight > 0 && receiptCube > 0) {
        //* Update NIIN_INFO and receipt Cube field
        String sql = "update niin_info set length = ?, width = ?, height = ?, cube = ? where niin_id = ?";
        try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(sql, 0)) {
          pstmt.setDouble(1, receiptLength / quantityMeasured);
          pstmt.setDouble(2, receiptWidth / quantityMeasured);
          pstmt.setDouble(3, receiptHeight / quantityMeasured);
          pstmt.setDouble(4, receiptCube / quantityMeasured);
          pstmt.setString(5, niinId);
          pstmt.executeUpdate();
          getDBTransaction().commit();
        }
        return "";
      }

      //* receipt dimensions are empty, and the niin has no dimensions
      if (niinCube <= 0)
        return "RECEIPT_CUBE";

      //* receipt dimensions are empty, however, the niin does have dimensions
    }
    catch (Exception e) {
      getDBTransaction().rollback();
      AdfLogUtility.logException(e);
    }
    return "";
  }

  /**
   * Validation method for Weight checking
   * Validates the given data from RECEIPT and
   * Updates the weight on to NIIN_INFO if they are all null.
   */
  public String validateNiinMeasurementsWeight(Row row) {
    // Compare weight entered on Receipt to that of the values in NIIN_INFO
    // Return false if different
    try {
      NiinInfoLVOImpl niinInfoView = getNiinInfoLVO2();
      NiinInfoLVORowImpl niinRow = (NiinInfoLVORowImpl) niinInfoView.getCurrentRow();

      double weightNiin = Util.cleanDouble(niinRow.getWeight());
      double weightReceipt = Util.cleanDouble(row.getAttribute("Weight"));
      int quantityMeasured = Util.cleanInt(row.getAttribute(QUANTITY_MEASURED));
      if (quantityMeasured == 0)
        quantityMeasured = 1;

      if ((weightNiin <= 0) && (weightReceipt <= 0)) {
        //Error message to enter Weight b/c niin has zero as weight
        return "RECEIPT_WEIGHT_REQ";
      }
      if ((weightNiin > 0) && (weightReceipt <= 0)) {
        //NIIN_INFO has weight the user did not enter any new values so we are fine
        return "";
      }
      if ((weightNiin <= 0) && (weightReceipt > 0)) {
        //Do NIIN_INFO update with this new values
        try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement("update niin_info set weight = ? where niin_id = ?", 0)) {
          pstmt.setDouble(1, weightReceipt / quantityMeasured);
          pstmt.setString(2, Util.cleanString(row.getAttribute(NIIN_ID)));
          pstmt.executeUpdate();
          getDBTransaction().commit();
        }
      }
    }
    catch (Exception e) {
      getDBTransaction().rollback();
      AdfLogUtility.logException(e);
    }
    return "";
  }

  /**
   * Validation method for Supplementary Address checking
   * Return true if invalid.
   */
  public boolean validateSupplementaryAddress(Row row) {
    String suppAddr = Util.cleanString(row.getAttribute("SupplementaryAddress"));
    return (suppAddr.length() < 7);
  }

  /**
   * Validation method for Quantity Inducted checking
   * Return true if invalid.
   */
  public boolean validateQuantityInducted(Row row) {
    int qtyInducted = Util.cleanInt(row.getAttribute(QUANTITY_INDUCTED));
    return (qtyInducted < 1);
  }

  /**
   * Validation method for COG checking
   * Return true if valid.
   */
  public boolean validateCOG(String data) {
    // Check master_dropdown if value exists for COG
    String value = Util.trimUpperCaseClean(data);
    String sql = "select count(*) from master_dropdown where domain = 'COG' and trim(value) = ?";
    int count = DBUtil.getCountValueBy(sql, value, getDBTransaction());
    return (count > 0);
  }

  /**
   * Validation method for CC checking
   * Return true if valid.
   */
  public boolean validateConditionCode(String data) {
    // Check master_dropdown if value exists for CC
    String value = Util.trimUpperCaseClean(data);
    String sql = "select count(*) from master_dropdown where domain = 'CC' and trim(value) = ?";
    int count = DBUtil.getCountValueBy(sql, value, getDBTransaction());
    return (count > 0);
  }

  /**
   * Validation method for Project Code (PJC) checking
   * Return true if valid.
   */
  public boolean validateProjectCode(String data) {
    // Check master_dropdown if value exists for Project code
    String value = Util.trimUpperCaseClean(data);
    String sql = "select count(*) from master_dropdown where domain = 'PJC' and trim(value) = ?";
    int count = DBUtil.getCountValueBy(sql, value, getDBTransaction());
    return (count > 0);
  }

  /**
   * Validation method for Purpose Code (PC) checking
   * Return true if valid.
   */
  public boolean validatePurposeCode(String data) {
    // Check master_dropdown if value exists for Purpose code
    String value = Util.trimUpperCaseClean(data);
    String sql = "select count(*) from master_dropdown where domain = 'PC' and trim(value) = ?";
    int count = DBUtil.getCountValueBy(sql, value, getDBTransaction());
    return (count > 0);
  }

  /**
   * Validation method for Shelf Life Code (SLC) checking
   */
  public int validateShelfLifeCode(String data) {
    String value = Util.trimUpperCaseClean(data);
    String sql = "select trim(span_days) from ref_slc where trim(shelf_life_code) = ?";
    int spanDays = Util.cleanInt(DBUtil.getSingleReturnValue(sql, value, getDBTransaction()));
    val convertedValue = value.equalsIgnoreCase("0") ? 0 : -1;
    return (spanDays > 0) ? spanDays : convertedValue;
  }

  /**
   * Validation method for UI checking
   * Return true if valid
   */
  public boolean validateUi(String data) {
    log.debug("validate ui +  {}", data);
    String value = Util.trimUpperCaseClean(data);
    String sql = "select count(*) from ref_ui where trim(ui_conv_from) = ?";
    int count = DBUtil.getCountValueBy(sql, value, getDBTransaction());
    return (count > 0);
  }

  /**
   * Validation method for FSC checking
   * Return true if valid
   */
  public boolean validateFsc(String data) {
    // Check if Receipt FSC matches NIIN_INFO FSC. Return false if mismatch.
    boolean valid = true;
    String fsc = Util.trimUpperCaseClean(data);
    try {
      NiinInfoLVOImpl niinInfoView = getNiinInfoLVO2();
      NiinInfoLVORowImpl niinInfoRow = (NiinInfoLVORowImpl) niinInfoView.getCurrentRow();
      if (!niinInfoRow.getFsc().equalsIgnoreCase(fsc))
        valid = false;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      valid = false;
    }
    return valid;
  }

  /**
   * Validation method for DocumentNumber Route checking
   * Check if given document number and route name belongs to local SMU
   * also validate if this docuement number is on REF_DASF
   * (if it is not a customer of current SMU).
   * <p>
   * Return false if invalid
   */
  public boolean validateDocumentNumberRoute(String data, String unused) {
    // Validate if the receipt is for this AAC; return false for if it does not
    String value = Util.trimUpperCaseClean(data);
    if (value.length() < 6)
      return false;

    String smu = value.substring(0, 6);
    String sql = "select count(*) from SITE_INFO where trim(aac) = ?";
    int count = DBUtil.getCountValueBy(sql, smu, getDBTransaction());
    if (count > 0) return false; // this is for this SMU

    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement("select aac, nvl(restrict_ship,'N'), nvl(supported,'Y') from customer where (trim(aac) = ?)", 0)) {
      String restrictShp = "N";
      String deliInd = "Y";
      pstmt.setString(1, smu);
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          restrictShp = rs.getString(2);
          deliInd = rs.getString(3);
        }
      }

      //* flag f is true at this stage means AAC or Route belongs to this SMU
      //* If this not a customer who belongs to this SMU but we do Transshipment to them
      //* If customer is there and is restricted then do not do transhipment
      if (restrictShp.equalsIgnoreCase("Y") || deliInd.equalsIgnoreCase("N")) return false;

      return validateDocumentNumberDASFCheck(value.substring(0, 14));
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return false;
  }

  /**
   * Validation method for RoutingId
   * Check if route is in the database ROUTE table
   * Return true if valid
   */
  public boolean validateRoutingId(String data) {
    // Check Route table if Route_name exists. Return false if it does not exist else true.
    String value = Util.trimUpperCaseClean(data);
    String sql = "select count(*) from route where route_name = ?";
    int count = DBUtil.getCountValueBy(sql, value, getDBTransaction());
    return (count > 0);
  }

  /**
   * Validation method for ConsolidationBarcode checking (checks only current table)
   * Return true if exists
   */
  public boolean validateConsolidationBarcode(String data) {
    log.debug("ReceiptAMImpl.validateConsolidationBarcode for barcode  {}", data);
    //* Check PACKING_CONSOLIDATION if that value already used
    String sql = "select count(*) from packing_consolidation where consolidation_barcode = ?";
    return (DBUtil.getCountValueBy(sql, Util.trimUpperCaseClean(data), getDBTransaction()) > 0);
  }

  /**
   * Validates that the qtyRecived is less than or equal to the quantity due on the dasf for the document number
   *
   * @param docNumber   Document number to check
   * @param qtyReceived quantity received
   */
  public boolean validateDASFQty(String docNumber, String qtyReceived) {
    String sql = "select quantity_due from ref_dasf where document_number = ?";
    try (PreparedStatement statement = getDBTransaction().createPreparedStatement(sql, 0)) {
      statement.setString(1, docNumber);
      try (ResultSet result = statement.executeQuery()) {
        if (result.next()) {
          String qtyStr = Util.trimClean(result.getString(1));
          if (!Util.isEmpty(qtyStr) && !Util.isEmpty(qtyReceived) && (Integer.parseInt(qtyReceived) > Integer.parseInt(qtyStr)))
            return false;
        }
      }
    }
    catch (SQLException e) {
      log.error("Error in validateDASFQty", e);
    }
    return true;
  }

  /**
   * Validates that the document number and suffix will not cause a duplicate D6 issue
   *
   * @param documentNumber   Document number to check
   * @param suffix           Document number suffix
   * @param quantityInducted quantity inducted
   */
  public boolean checkPreviousTransactions(String documentNumber, String suffix, int quantityInducted) {
    try {

      GCSSMCTransactionsImpl service = getWorkloadManagerService().getGCSSMCTransactionsService();
      GCSSMCTransactionsImpl.DasfItem dasfItem = service.isDASF(documentNumber, "", quantityInducted);
      if (Util.isNotEmpty(dasfItem.refDasfId)) return service.checkDASFDuplicate(documentNumber, suffix);

      return service.checkNonDASFDuplicate(documentNumber, suffix);
    }
    catch (SQLException e) {
      log.error("Error in checkPreviousTransactions", e);
    }
    return true;
  }

  /**
   * Validation method for DocumentNumber Route checking
   * Check if the given document number is in REF_DASF
   * Return true if valid
   */
  public boolean validateDocumentNumberDASFCheck(String data) {
    String value = Util.trimUpperCaseClean(data);
    String sql = "select count(*) from ref_dasf where trim(document_number) = ?";
    int count = DBUtil.getCountValueBy(sql, value, getDBTransaction());
    return (count > 0);
  }

  /**
   * Validation method for UI difference
   * Check if the Unit of Issue is convertable and returns the conversion factor.
   */
  public double validateUICheckAndReturnConvFactor(String receiptUI) {
    // Validate if the receipt has UI that can be converted or not
    // Check REF_UI table
    NiinInfoLVOImpl niinInfoView = getNiinInfoLVO2();
    String niinUI = niinInfoView.getCurrentRow().getAttribute("Ui").toString();
    double r = -1;
    if (niinUI.compareToIgnoreCase(receiptUI) == 0) return 1;

    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement("select trim(ui_conv_to), trim(conversion_factor) from ref_ui where trim(ui_conv_from) = ?", 0)) {
      pstmt.setString(1, receiptUI);
      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          if (niinUI.compareToIgnoreCase(rs.getString(1)) == 0) r = rs.getDouble(2);
        }
      }
      return r;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return -1;
  }

  /**
   * Copy NIIN_INFO measurements to Receipt.
   */
  public String copyNiinInfoMesurementsToReceipt(Row rr) {
    // Copy NIIN_INFO measurements to Receipt to
    // show the current values for dimensions and weight.
    try {
      if (rr != null) {
        NiinInfoLVOImpl niinInfoView = getNiinInfoLVO2();
        NiinInfoLVORowImpl niinRow = (NiinInfoLVORowImpl) niinInfoView.getCurrentRow();
        rr.setAttribute(QUANTITY_MEASURED, "1");

        rr.setAttribute("Length", Util.cleanString(niinRow.getLength()));
        rr.setAttribute("Width", Util.cleanString(niinRow.getWidth()));
        rr.setAttribute("Height", Util.cleanString(niinRow.getHeight()));
        rr.setAttribute("Weight", Util.cleanString(niinRow.getWeight()));
        rr.setAttribute("Cube", Util.cleanString(niinRow.getCube()));
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return "";
  }

  /**
   * Builds WAC list.
   */
  public void fillWacList(List valueList, List displayList) {
    try {
      NiinInfoReceiptVViewImpl rvo = getNiinInfoReceiptVView1();
      String m = "";
      WacListQryImpl vo = getWacListQry1();
      vo.setRangeSize(-1);
      vo.executeQuery();
      Row[] ro = vo.getAllRowsInRange();
      if (rvo.getCurrentRow() != null && rvo.getCurrentRow().getAttribute("MechNonMechFlag") != null)
        m = rvo.getCurrentRow().getAttribute("MechNonMechFlag").toString();
      for (Row row : ro) {
        Object wacLoc = row.getAttribute("WacLoc");
        Object wacId = row.getAttribute(WAC_ID);
        Object bldg = row.getAttribute("Building");
        Object mId = row.getAttribute("MechInd");
        if ((wacLoc != null) && (bldg != null) && m.compareTo(mId.toString()) == 0) {
          valueList.add(wacId);
          String wb = bldg + "-" + wacLoc;
          displayList.add(wb);
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  /**
   * Builds Location label list.
   */
  public void fillLocList(List valueList, List displayList, Object wacId) {
    try {
      NiinInfoReceiptVViewImpl rvo = getNiinInfoReceiptVView1();

      NiinLocListVViewImpl vo = getNiinLocListVView1();
      vo.setRangeSize(-1);
      vo.setNamedWhereClauseParam("wacIdStr", wacId);
      if (rvo.getCurrentRow() != null) {
        if (rvo.getCurrentRow().getAttribute(NIIN_ID) != null)
          vo.setNamedWhereClauseParam(NIIN_ID_STR, rvo.getCurrentRow().getAttribute(NIIN_ID).toString());
        else
          vo.setNamedWhereClauseParam(NIIN_ID_STR, "");
      }
      else
        vo.setNamedWhereClauseParam(NIIN_ID_STR, "");
      vo.executeQuery();
      Row[] ro = vo.getAllRowsInRange();
      for (Row row : ro) {
        Object locLabel = row.getAttribute(LOCATION_LABEL);
        Object locId = row.getAttribute(LOCATION_ID);
        if ((locLabel != null) && (locId != null)) {

          valueList.add(locId);
          String wb = locLabel.toString();
          displayList.add(wb);
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  /**
   * Get NIIN_INFO row for a given NIIN string.
   */
  public long getNiinInfoFromDB(String nnStr) {
    // Get the niin info details for receipt
    try {
      String niin = null;
      try (PreparedStatement stR = getDBTransaction().createPreparedStatement("select niin from niin_info where niin = ?", 0)) {
        stR.setString(1, nnStr);
        try (ResultSet rs = stR.executeQuery()) {
          if (rs.next()) niin = rs.getString(1);
        }
      }

      if (Util.isEmpty(niin)) {
        //* NIIN not found in STRATIS, Find NIIN in MHIF and insert into STRATIS

        //* niin not found in niin_info table, get from mhif and insert into niin_info
        try (PreparedStatement pstmtREFMHIF = getDBTransaction().createPreparedStatement("select record_niin, record_fsc, unit_of_issue, unit_price, item_name_nomenclature from ref_mhif where record_niin = ? and rownum =1 ", 0)) {
          pstmtREFMHIF.setObject(1, nnStr);
          try (ResultSet rsrefMhif = pstmtREFMHIF.executeQuery()) {
            if (rsrefMhif.next()) {
              try (PreparedStatement pstmtnewNiin = getDBTransaction().createPreparedStatement("insert into niin_info (niin, fsc, ui, price, nomenclature, inventory_threshold) values (?,?,?,?,?,?)", 0)) {

                pstmtnewNiin.setObject(1, nnStr);
                pstmtnewNiin.setObject(2, rsrefMhif.getObject(2));
                pstmtnewNiin.setObject(3, rsrefMhif.getObject(3));
                Object priceObj = rsrefMhif.getObject(4);
                double price = 0.0;
                if (priceObj != null) price = Double.parseDouble(priceObj.toString()) / 100;

                pstmtnewNiin.setDouble(4, price);
                pstmtnewNiin.setObject(5, rsrefMhif.getObject(5));
                pstmtnewNiin.setString(6, "N");
                pstmtnewNiin.executeUpdate();
                getDBTransaction().commit();
              }
            }
          }
        }
        catch (Exception e) {
          AdfLogUtility.logException(e);
          getDBTransaction().rollback();
        }
      }

      NiinInfoLVOImpl vnf = getNiinInfoLVO2();
      vnf.setRangeSize(-1);
      vnf.setNamedWhereClauseParam(NIIN_STR, nnStr);
      vnf.executeQuery();
      vnf.setNamedWhereClauseParam(NIIN_STR, "x"); //So that it does not return any rows on screen refresh
      if (vnf.getRowCount() > 0) return Long.parseLong(vnf.first().getAttribute(NIIN_ID).toString());
      else return 0;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return 0;
  }

  /**
   * Get NIIN_INFO row for a given NIIN_ID string.
   */
  public long getNiinInfoFromDBNiinId(long niinId) {
    // Get the niin info details for receipt
    try {
      String niinStr = null;
      try (PreparedStatement stR = getDBTransaction().createPreparedStatement("select niin from niin_info where niin_id = ?", 0)) {
        stR.setLong(1, niinId);
        try (ResultSet rs = stR.executeQuery()) {
          if (rs.next()) niinStr = rs.getString(1);
        }
      }

      if (niinStr != null) {
        NiinInfoLVOImpl vnf = getNiinInfoLVO2();
        vnf.setRangeSize(-1);
        vnf.setNamedWhereClauseParam(NIIN_STR, niinStr);
        vnf.executeQuery();
        vnf.setNamedWhereClauseParam(NIIN_STR, "x");
        if (vnf.getRowCount() > 0) return Long.parseLong(vnf.first().getAttribute(NIIN_ID).toString());
        else return 0;
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return 0;
  }

  /**
   * Create a stow for a given receipt.
   */
  public void createStowForAReceipt(long uId, Object serialNumber) {
    //Create Stow record if there are any items to be stowed.
    //Set default for location to NONE, set status to Receip InProgress,
    //Set SerialNumber, Set CreateUser, Set CreateDate,
    //Set ExpirationDate iff ShelfLifeCode is zero or empty
    try {
      NiinInfoReceiptVViewImpl vor = getNiinInfoReceiptVView1();
      StowViewImpl vos = getStowView2();
      Row nsRow = null;

      // get the receipt info
      Row crRow = vor.first();
      // get the location list
      int inducted = Util.cleanInt(crRow.getAttribute(QUANTITY_INDUCTED));
      int released = Util.cleanInt(crRow.getAttribute("QuantityReleased"));
      int stowed = Util.cleanInt(crRow.getAttribute("QuantityStowed"));
      int stQty = this.getStowQtyTotalForAReceipt();
      // Based on Receipt and Stow table
      if (((inducted - released - stowed) > 0) && ((inducted - released - stQty) > 0)) {
        // Create row only if there are quantity to be stowed
        nsRow = vos.createRow();
        vos.insertRow(nsRow);
        // Establish the link
        nsRow.setAttribute("Rcn", crRow.getAttribute("Rcn"));
        nsRow.setAttribute("Sid", this.getWorkloadManagerService().assignSID());
        nsRow.setAttribute("QtyToBeStowed", Util.cleanInt(crRow.getAttribute(QUANTITY_INDUCTED)) - Util.cleanInt(crRow.getAttribute("QuantityReleased")) - Util.cleanInt(crRow.getAttribute("QuantityStowed")));
        nsRow.setAttribute("CreatedBy", uId);
        nsRow.setAttribute("CreatedDate", (new Date(new Timestamp(System.currentTimeMillis()))));
        nsRow.setAttribute("Status", "STOW");
        if (crRow.getAttribute("RserialNumber") != null)
          nsRow.setAttribute("SerialNumber", crRow.getAttribute("RserialNumber").toString());
        else if (serialNumber != null)
          nsRow.setAttribute("SerialNumber", serialNumber);
        if (crRow.getAttribute("SerialControlFlag").toString().equalsIgnoreCase("Y"))
          nsRow.setAttribute("QtyToBeStowed", 1);
        if (crRow.getAttribute("ShelfLifeCode") != null) {
          if ((crRow.getAttribute("ShelfLifeCode").toString().trim().length() <= 0) || (crRow.getAttribute("ShelfLifeCode").toString().trim().compareTo("0") == 0))
            nsRow.setAttribute("ExpirationDate", DateConstants.DEFAULT_EXPIRATION_DATE);
        }
        else
          nsRow.setAttribute("ExpirationDate", DateConstants.DEFAULT_EXPIRATION_DATE);
        nsRow.setAttribute(LOCATION_ID, "1");
        getDBTransaction().commit();
      }

      vor.setRangeSize(-1);
      vor.setNamedWhereClauseParam("rcnStr", crRow.getAttribute("Rcn").toString());
      vor.executeQuery();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  /**
   * Get NIIN_INFO row for a given NIIN_ID string.
   */
  public String getStowQtyTotalForAReceipt(String rcnStr, int secondRCN) {
    //Go throught all the rows of stow for current receipt and return the total
    String curStowQtyStr = "0";
    try (PreparedStatement stR = this.getDBTransaction().createPreparedStatement("select sum(qty_to_be_stowed) from stow  where rcn in (?, ?)", 0)) {
      stR.setString(1, rcnStr);
      stR.setInt(2, secondRCN);
      try (ResultSet rst = stR.executeQuery()) {
        if (rst.next()) curStowQtyStr = rst.getString(1);

        if (curStowQtyStr == null) curStowQtyStr = "0";
      }
      return curStowQtyStr;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return "0";
  }

  /**
   * Get total qty used in stows.
   */
  public int getStowQtyTotalForAReceipt() {
    //Go through all the rows of stow for current receipt and return the total
    try {
      StowViewImpl vos2 = getStowView2();
      int totalQty = 0;

      Row[] ros = vos2.getAllRowsInRange();
      for (int i = 0; i < ros.length; i++) {
        totalQty = totalQty + Integer.parseInt(ros[i].getAttribute("QtyToBeStowed").toString());
      }
      return totalQty;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return 0;
  }

  /**
   * Change status to RECEIPT INERROR in RECEIPT table
   */
  public String saveReceiptError() {
    //Change the status to Receipt InError
    try {
      // ### Save the Receipt record for initial capture of receipt information (1348 doc)
      ReceiptViewImpl vor = getReceiptView1();
      Row crRow = vor.getCurrentRow();

      crRow.setAttribute("Status", "RECEIPT INERROR");
      vor.getDBTransaction().commit();
      crRow.refresh(Row.REFRESH_CONTAINEES);
      vor.setCurrentRow(crRow);
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return "";
  }

  public void addSuffixToReceipt() {
    ReceiptViewImpl vor = getReceiptView1();
    Row crRow = vor.getCurrentRow();

    ReceiptSuffixProcessor receiptSuffixProcessor = ContextUtils.getBean(ReceiptSuffixProcessor.class);
    String suffix = receiptSuffixProcessor.generateSuffix(crRow.getAttribute("DocumentNumber").toString());
    crRow.setAttribute("Suffix", suffix);
  }

  /**
   * Initial status in RECEIPT table
   */
  public String saveReceipt(long uId) {
    try {
      // ### Save the Receipt record for initial capture of receipt information (1348 doc)
      ReceiptViewImpl vor = getReceiptView1();
      Row crRow = vor.getCurrentRow();

      crRow.setAttribute("CreatedBy", uId);
      crRow.setAttribute("Status", "RECEIPT DRAFT");
      vor.getDBTransaction().commit();
      crRow.refresh(Row.REFRESH_CONTAINEES);
      vor.setCurrentRow(crRow);
    }
    catch (Exception e) {
      this.getDBTransaction().rollback();
      AdfLogUtility.logException(e);
    }
    return "";
  }

  /**
   * Update the RECEIPT and check conditions to
   * descide on forwarding page of STOW DETAIL or CROSS DOCK or TRANSSHIPMENT.
   */
  public String moveOnToOthersFromReceipt(long uId, String docNumStr) {
    log.debug("begin ReceiptAMImpl.MoveOnToOthersFromReceipt");
    try {
      // ### Save the Receipt record for initail capture of receipt information (1348 doc)
      // ### Also forward to the next web page depending on processing cross dock
      this.getDBTransaction().commit();

      NiinInfoLVOImpl vnf = getNiinInfoLVO2();
      vnf.setRangeSize(-1);
      vnf.setNamedWhereClauseParam(NIIN_STR, "x");
      vnf.executeQuery();
      if (docNumStr.length() > 0) {
        log.debug("go to GetExistingReceipt for docNumStr  {}", docNumStr);

        this.getExistingReceipt(docNumStr);
      }
      ReceiptViewImpl vor = getReceiptView1();
      Row crRow = vor.getCurrentRow();
      boolean found = true;

      if (crRow == null) found = false;

      if (found) {
        String docNo = crRow.getAttribute("DocumentNumber").toString();
        docNo = docNo + ((crRow.getAttribute("Suffix") == null) ? "" : crRow.getAttribute("Suffix").toString());
        //Check for TRANSSHIPMENT
        if (this.validateDocumentNumberRoute(docNo, crRow.getAttribute("RoutingId").toString())) {
          ReceiptTransshipmentVOImpl vox = getReceiptTransshipmentVO1();
          vox.setRangeSize(-1);
          vox.setNamedWhereClauseParam("rcnStr", crRow.getAttribute("Rcn").toString());
          vox.executeQuery();
          if (vox.getRowCount() > 0) return "GoReceivingGeneralTransshipment";
        }
      }

      // Check for CROSS DOCK for DUE IN ONLY GCSS mode and take steps
      //FIX FOR HANDHELD. Handheld does a save with stow, released, and inducted = 0 before checking.

      boolean handheld = false;

      if (found) {
        val quantityStowed = Long.valueOf(crRow.getAttribute("QuantityStowed").toString());
        val quantityReleased = Long.valueOf(crRow.getAttribute("QuantityReleased").toString());
        val quantityInducted = Long.valueOf(crRow.getAttribute(QUANTITY_INDUCTED).toString());
        if ((quantityStowed == 0 && quantityReleased == 0) && quantityInducted == 0) {
          handheld = true;
        }

        if ((((crRow.getAttribute("QuantityStowed") == null) || handheld || ((quantityStowed + quantityReleased) < quantityInducted)) && (crRow.getAttribute("Cc") != null && crRow.getAttribute("Cc").toString().equalsIgnoreCase("A")))) {
          IssueBackordersVOImpl vob = getIssueBackordersVO1();
          vob.setRangeSize(-1);
          vob.setNamedWhereClauseParam("rcnStr", crRow.getAttribute("Rcn").toString());
          vob.executeQuery();
          if (vob.getRowCount() > 0) {
            vob.next();
            Row boRow = vob.getCurrentRow();
            String scn = boRow.getAttribute("Scn").toString();
            String rcn = crRow.getAttribute("Rcn").toString();
            // assign first crossdock
            boolean success = assignFirstCrossdock(scn, rcn);

            if (success) {
              log.debug("end (returns 'GoReceivingGeneralCrossDock' ReceiptAMImpl.MoveOnToOthersFromReceipt");
              return "GoReceivingGeneralCrossDock";
            }
          }
        }
        //Other get the details of the RECEIPT
        NiinInfoReceiptVViewImpl nir = getNiinInfoReceiptVView1();
        nir.setRangeSize(-1);
        nir.setNamedWhereClauseParam("rcnStr", crRow.getAttribute("Rcn").toString());
        nir.executeQuery();
        if (docNumStr.length() <= 0) {
          log.debug("go to CreateStowForAReceipt for docNumStr  {}", docNumStr);
          this.createStowForAReceipt(uId, null);
        }
        nir.executeQuery();
        log.debug("end (returns 'GoReceivingGeneralDetail' ReceiptAMImpl.MoveOnToOthersFromReceipt");
        //Check if serialized item.

        Row r = nir.first();
        if (r.getAttribute("SerialControlFlag").equals("Y")) return "GoReceivingEnterSerial";
        else return "GoReceivingGeneralDetail";
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    log.debug("end (returns null) ReceiptAMImpl.MoveOnToOthersFromReceipt");

    return "";
  }

  /**
   * Update the STOW ROWS with the new get locations location.
   */
  public void updateAllRows(Object locId) {
    StowViewImpl vos = getStowView2();
    Row cs = vos.getCurrentRow();
    Row[] rows = vos.getAllRowsInRange();
    vos.setRangeSize(-1);
    NiinInfoReceiptVViewImpl vor = getNiinInfoReceiptVView1();
    Object shelflife = vor.getCurrentRow().getAttribute("ShelfLifeCode");

    //Calculating expiration date
    int d1 = 0;
    if (shelflife != null) d1 = this.validateShelfLifeCode(shelflife.toString());
    if (d1 > 0) {
      //For date
      Calendar c1 = Calendar.getInstance();
      if (cs.getAttribute("DateOfManufacture") != null) {
        String dom = cs.getAttribute("DateOfManufacture").toString();
        String[] arrD = dom.split("-"); //Split YYYY-MM-DD
        c1.set(Integer.parseInt(arrD[0]), Integer.parseInt(arrD[1]) - 1, Integer.parseInt(arrD[2]));
        c1.add(Calendar.DATE, d1);
        Date d = new Date(new Timestamp(c1.getTimeInMillis()));
        cs.setAttribute("ExpirationDate", d);
      }
    }
    else if ((cs.getAttribute("ExpirationDate") == null) || (cs.getAttribute("ExpirationDate").toString().length() <= 0))
      cs.setAttribute("ExpirationDate", DateConstants.DEFAULT_EXPIRATION_DATE);

    Object manuDate = cs.getAttribute("DateOfManufacture");
    Object expDate = cs.getAttribute("ExpirationDate");

    for (Row row : rows) {
      row.setAttribute(LOCATION_ID, locId);
      row.setAttribute("DateOfManufacture", manuDate);
      row.setAttribute("ExpirationDate", expDate);
    }
  }

  /**
   * Update the STOW ROWS with serial numbers
   */
  public String updateAllSerials(TreeSet<String> serialSet, long userID) {
    StowViewImpl vos = getStowView2();
    vos.setRangeSize(-1);
    Row[] rows = vos.getAllRowsInRange();
    String rcnStr = "";
    String errStr = "";

    NiinInfoReceiptVViewImpl vor = getNiinInfoReceiptVView1();
    Row receiptRow = vor.first();
    rcnStr = vor.getCurrentRow().getAttribute("Rcn").toString();
    int qtytobestowed = (Integer.parseInt(receiptRow.getAttribute(QUANTITY_INDUCTED).toString()) - Integer.parseInt(receiptRow.getAttribute("QuantityReleased").toString()));
    if (serialSet.size() != qtytobestowed) return "ERROR: # of Serial Numbers = " + serialSet.size() + "; # to stow = " + qtytobestowed;
    else {
      Object[] serialArray = serialSet.toArray();

      for (Object o : serialArray) {
        //Check validity of serialNumbers
        errStr = checkSerial(rcnStr, (String) o, "", vor.getCurrentRow().getAttribute(NIIN_ID).toString());
        if (errStr.length() > 0) return errStr;
      }

      int rowLength = 0;

      for (Row r : rows) {
        if (rowLength < qtytobestowed) {
          r.setAttribute("SerialNumber", serialArray[rowLength]);
          rowLength++;
        }
      }
      for (int i = rowLength; i < qtytobestowed; i++) {
        createStowForAReceipt(userID, serialArray[i]);
      }

      //Need to update Quantity Stowed

      try (PreparedStatement stR = getDBTransaction().createPreparedStatement("begin update receipt set quantity_stowed = ?, modified_by = ? where rcn = ?; end;", 0)) {
        int quantityStowed = Util.cleanInt(getStowQtyTotalForAReceipt(rcnStr, -1));
        log.debug("this.GetStowQtyTotalForAReceipt() quantity_stowed= {}", quantityStowed);
        stR.setInt(1, quantityStowed);
        stR.setLong(2, userID);
        stR.setLong(3, Long.parseLong(rcnStr));
        stR.executeUpdate();
        getDBTransaction().commit();
        vor.setRangeSize(-1);
        vor.setNamedWhereClauseParam("rcnStr", rcnStr);
        vor.executeQuery();
      }
      catch (Exception e) {
        AdfLogUtility.logException(e);
      }

      return "";
    }
  }

  /**
   * Return all Serial Numbers from stows
   */
  public String getAllSerialsfromStows() {
    StowViewImpl vos = getStowView2();
    vos.setRangeSize(-1);
    Row[] rows = vos.getAllRowsInRange();
    StringBuilder allSerials = new StringBuilder();
    for (Row r : rows) {
      allSerials.append(r.getAttribute("SerialNumber")).append('\n');
    }
    return allSerials.toString();
  }

  /**
   * Update the RECEIPT with manufacture date and also calculate expiration date
   * based on the shelf life code.
   */
  public String updateStowForAReceipt(Object locId, long uId,
                                      String fromLoc) {
    //Update Manufacture Date, Expiration Date, Location after validation of
    //all the information on Stow. fromLoc inidicates location needs to be validated or not.
    try {
      StowViewImpl vos = getStowView2();
      String errLs = "";
      Row cs = vos.getCurrentRow();
      NiinInfoReceiptVViewImpl vor = getNiinInfoReceiptVView1();
      String niinid = vor.getCurrentRow().getAttribute(NIIN_ID).toString();
      Object shelflife = vor.getCurrentRow().getAttribute("ShelfLifeCode");
      Object rcn = vor.getCurrentRow().getAttribute("Rcn");

      //Build Expiration date
      // Days for expiration from Shelf Life
      if (cs != null) {
        if (this.getSerialControlledFlag(niinid).equalsIgnoreCase("Y") && cs.getAttribute("SerialNumber") != null)
          cs.setAttribute("SerialNumber", cs.getAttribute("SerialNumber").toString().toUpperCase());

        if (this.getLotControlledFlag(niinid).equalsIgnoreCase("Y") && cs.getAttribute("LotConNum") != null)
          cs.setAttribute("LotConNum", cs.getAttribute("LotConNum").toString().toUpperCase());

        int d1 = 0;
        String preExpDt = (cs.getAttribute("ExpirationDate") == null ? DateConstants.DEFAULT_EXPIRATION_DATE : cs.getAttribute("ExpirationDate").toString().substring(0, 9));
        if (shelflife != null) d1 = this.validateShelfLifeCode(shelflife.toString());

        if (d1 > 0) {
          //For date
          Calendar c1 = Calendar.getInstance();
          if (cs.getAttribute("DateOfManufacture") != null) {
            String dom = cs.getAttribute("DateOfManufacture").toString();
            String[] arrD = dom.split("-"); //Split YYYY-MM-DD
            c1.set(Integer.parseInt(arrD[0]), Integer.parseInt(arrD[1]) - 1, Integer.parseInt(arrD[2]));
            Calendar nowDate = Calendar.getInstance();
            if (nowDate.before(c1)) {
              cs.setAttribute(LOCATION_ID, "1"); //failed because DoM is after current date.
              errLs = "MANUFACTURE_AFTER";
            }
            else {
              c1.add(Calendar.DATE, d1);
              Date d = new Date(new Timestamp(c1.getTimeInMillis()));
              cs.setAttribute("ExpirationDate", d);
            }
          }
        }
        else if ((cs.getAttribute("ExpirationDate") == null) || (cs.getAttribute("ExpirationDate").toString().length() <= 0))
          cs.setAttribute("ExpirationDate", DateConstants.DEFAULT_EXPIRATION_DATE);

        if ((d1 > 0) && ((cs.getAttribute("ExpirationDate") != null) && (!cs.getAttribute("ExpirationDate").toString().substring(0, 9).equalsIgnoreCase(preExpDt))))
          cs.setAttribute(LOCATION_ID, "1"); //Reset date if the expiration date got changed
        else if (locId != null) cs.setAttribute(LOCATION_ID, locId);

        //Validate Stow data
        if (errLs.isEmpty()) errLs = this.validateStowRow(fromLoc);
        if ((errLs.length() <= 0) && (fromLoc.equalsIgnoreCase("N"))) {
          getDBTransaction().commit();
          try (PreparedStatement stR = getDBTransaction().createPreparedStatement("begin update receipt set quantity_stowed = ?, modified_by = ? where rcn = ?; end;", 0)) {
            int quantityStowed = Util.cleanInt(getStowQtyTotalForAReceipt(vor.getCurrentRow().getAttribute("Rcn").toString(), -1));
            log.debug("this.GetStowQtyTotalForAReceipt() quantity_stowed= {}", quantityStowed);
            stR.setInt(1, quantityStowed);
            stR.setLong(2, uId);
            stR.setLong(3, Long.parseLong(rcn.toString()));
            stR.executeUpdate();
            getDBTransaction().commit();
          }
          vor.setRangeSize(-1);
          vor.setNamedWhereClauseParam("rcnStr", rcn.toString());
          vor.executeQuery();

          this.getWorkloadManagerService().clearLocationList();
          this.createStowForAReceipt(uId, null);
          return "";
        }
        return errLs;
      }
      return "";
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return "";
  }

  /**
   * Used to clear the location list.
   */
  public void editStowForAReceipt() {
    //Stow selection changed so clear location list
    try {
      this.clearLocationList();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  /**
   * Deletes selected stow record and updates the quantity on the RECEIPT.
   */
  public void deleteStowForAReceipt(long uId, String stowIdStr, String rcnStr) {
    //Delete the selected stow record
    try {
      if (stowIdStr.length() <= 0) {
        StowViewImpl vos = getStowView2();
        vos.removeCurrentRow();
        this.getDBTransaction().commit();
      }
      else {
        try (PreparedStatement stRd = getDBTransaction().createPreparedStatement("begin delete from stow  where stow_id = ?; end;", 0)) {
          stRd.setString(1, stowIdStr);
          stRd.executeUpdate();
          this.getDBTransaction().commit();
        }
      }

      String curStowQtyStr = "0";
      if (rcnStr.length() <= 0) {
        try (PreparedStatement stRe = this.getDBTransaction().createPreparedStatement("select sum(qty_to_be_stowed) from stow  where rcn = ?", 0)) {
          stRe.setString(1, rcnStr);
          try (ResultSet rst = stRe.executeQuery()) {
            if (rst.next()) curStowQtyStr = rst.getString(1);
          }
        }
      }

      NiinInfoReceiptVViewImpl vor = getNiinInfoReceiptVView1();

      try (PreparedStatement stR = getDBTransaction().createPreparedStatement("begin update receipt set quantity_stowed = ?, status = ?, modified_by = ? where rcn = ?; end;", 0)) {
        if (rcnStr.length() <= 0) stR.setInt(1, this.getStowQtyTotalForAReceipt());
        else stR.setString(1, curStowQtyStr);

        stR.setString(2, "RECEIPT INPROGRESS");
        stR.setLong(3, uId);
        if (rcnStr.length() <= 0) stR.setLong(4, Long.parseLong(vor.getCurrentRow().getAttribute("Rcn").toString()));
        else stR.setLong(4, Long.parseLong(rcnStr));

        stR.executeUpdate();
        getDBTransaction().commit();
      }
      vor.getCurrentRow().refresh(Row.REFRESH_CONTAINEES);
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  /*function to return if any SIDs are stowed for a RCN. Needed for error checking Cancel SID
   */
  public int getStowedNumberForAReceipt(String rcnStr) {
    //Go throught all the rows of stow for current receipt and return the total
    int curStowQtyStr = 0;
    try (PreparedStatement stR = this.getDBTransaction().createPreparedStatement("select count(sid) from stow where rcn = ? and status = 'STOWED'", 0)) {
      stR.setString(1, rcnStr);
      try (ResultSet rst = stR.executeQuery()) {
        if (rst.next()) curStowQtyStr = rst.getInt(1);
      }
      return curStowQtyStr;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return 0;
  }

  /**
   * Performs all the validations before setting the status on the RECEIPT.
   */
  public String finalUpdateOnReceiptAndStow(long userId, boolean DASFOverride) {
    //Update the quantity on Receipt and Set the status
    //to Receipt Complete on  Stow and Receipt records.
    try {
      // ### Set the status to Receipt Complete on  Stow and Receipt records validate the all view objects of Stow
      StowViewImpl stowView = getStowView2();
      StringBuilder errors = new StringBuilder(validateStow());
      if (errors.length() > 0) {
        log.error("An error occurred during pre-stow validation.");
        return errors.toString();
      }
      NiinInfoReceiptVViewImpl niinInfoReceiptVView = getNiinInfoReceiptVView1();
      niinInfoReceiptVView.getCurrentRow();
      Row currentRow = niinInfoReceiptVView.getCurrentRow();

      String quantityInducted = currentRow.getAttribute(QUANTITY_INDUCTED).toString();
      String quantityInvoiced = currentRow.getAttribute("QuantityInvoiced").toString();
      Integer quantityReleased = Integer.valueOf(currentRow.getAttribute("QuantityReleased").toString());
      Integer quantityStowed = Integer.valueOf(currentRow.getAttribute("QuantityStowed").toString());
      String rcn = currentRow.getAttribute("Rcn").toString();

      log.debug(String.format("QI%s QR %d QS %d", quantityInducted, quantityReleased, quantityStowed));

      int secondReceiptId = -1;
      if ((Integer.parseInt(quantityInducted) - quantityReleased - quantityStowed) == 0) {
        log.debug(String.format("Receipt complete via location screen with RCN [%s]", rcn));

        // * GCSS CR 05-01-2011

        if (getWorkloadManagerService().getGCSSMCTransactionsService().checkDuplicate(rcn)) {
          return "D6_DUPLICATE";
        }

        try (PreparedStatement receiptStatement = getDBTransaction().createPreparedStatement("begin update receipt set status = ?, modified_by = ? where rcn in (?, ?); end;", 0)) {
          receiptStatement.setString(1, "RECEIPT COMPLETE");
          receiptStatement.setLong(2, userId);
          receiptStatement.setLong(3, Long.parseLong(rcn));
          receiptStatement.setInt(4, secondReceiptId);
          receiptStatement.executeUpdate();
          getDBTransaction().commit();
        }

        if (stowView.getRowCount() > 0) {
          try (PreparedStatement stowStatement = getDBTransaction().createPreparedStatement("begin update stow set serial_number = upper(serial_number), lot_con_num = upper(lot_con_num), status = ?, modified_by = ? where rcn in (?, ?) and status='STOW'  and location_id<>1; end;", 0)) {
            stowStatement.setString(1, "STOW READY");
            stowStatement.setLong(2, userId);
            stowStatement.setLong(3, Long.parseLong(rcn));
            stowStatement.setInt(4, secondReceiptId);
            stowStatement.executeUpdate();
            getDBTransaction().commit();
          }
        }

        if (!quantityInducted.equalsIgnoreCase(quantityInvoiced) && !getWorkloadManagerService().findErrorQueueRecord("4", "RCN", rcn))
          getWorkloadManagerService().createErrorQueueRecord("4", "RCN", rcn, String.valueOf(userId), null);

        errors = new StringBuilder(validateStowAfterSave(secondReceiptId));
        if (errors.length() > 0) {
          log.error("An error occurred during post-stow validation.");
          return errors.toString();
        }
      }

      log.debug("Sending D6 transaction via location screen.");
      if (DASFOverride) {
        ReceiptProcessor receiptProcessor = ContextUtils.getBean(ReceiptProcessor.class);
        LocationService locationService = ContextUtils.getBean(LocationService.class);
        ReceiptService receiptService = ContextUtils.getBean(ReceiptService.class);
        val stowRow = stowView.getCurrentRow();
        int locId = Integer.parseInt(stowRow.getAttribute("LocationId1").toString());
        Location location = locationService.findById(locId).orElse(null);
        Receipt receipt = receiptService.findById(Integer.parseInt(rcn)).orElse(null);
        val secondReceipt = receiptProcessor.copyAndUpdateToD6AReceipt(receipt, location);
        secondReceiptId = secondReceipt.getRcn();
      }
      int d6ReturnCode = getWorkloadManagerService().getGCSSMCTransactionsService().sendD6GCSSMCTransaction(rcn);
      if (d6ReturnCode == -2) return "D6_DUPLICATE";
      if (DASFOverride) {
        int d6AReturnCode = getWorkloadManagerService().getGCSSMCTransactionsService().sendD6GCSSMCTransaction(Integer.toString(secondReceiptId), true);
        if (d6AReturnCode == -2) return "D6_DUPLICATE";
      }

      //Get the SIDLIST
      errors = new StringBuilder();
      try (PreparedStatement receiptStatement = getDBTransaction().createPreparedStatement("select sid from stow where (status = 'STOW READY') and rcn in (?, ?) order by sid asc", 0)) {
        receiptStatement.setLong(1, Long.parseLong(rcn));
        receiptStatement.setInt(2, secondReceiptId);

        try (ResultSet resultSet = receiptStatement.executeQuery()) {
          while (resultSet.next()) {
            if (errors.length() <= 0)
              errors = new StringBuilder("SIDLIST,").append(resultSet.getString(1));
            else
              errors.append(',').append(resultSet.getString(1));
          }
        }
      }
      // Clear NiinInfoReceiptVViewImpl
      this.clearLocationList();
      niinInfoReceiptVView.clearCache();
      getReceiptView1().clearCache();
      stowView.clearCache();
      return errors.toString();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return "";
  }

  /**
   * Change the status of a STOW to STOW CANCEL.
   */
  public String stowCancelVOUpdate(long uId) {
    //Update the status to Stow Cancel and cancel reason on stow.
    try {
      StowCancelVOImpl vos = getStowCancelVO1();
      Row csRow = vos.getCurrentRow();

      if (csRow == null) return "EMPTY_ROW";

      if ((csRow.getAttribute("CancelReason") == null) || (csRow.getAttribute("CancelReason").toString().length() <= 0)) {
        vos.clearCache();
        return "EMPTY_REASON";
      }
      try (PreparedStatement stR = getDBTransaction().createPreparedStatement("begin update stow set status = ?, cancel_reason = ?, modified_by = ? where sid = ?; delete from stow where sid = ?; end;", 0)) {
        stR.setString(1, "STOW CANCEL");
        stR.setString(2, csRow.getAttribute("CancelReason").toString());
        stR.setLong(3, uId);
        stR.setString(4, csRow.getAttribute("Sid").toString());
        stR.setString(5, csRow.getAttribute("Sid").toString());
        stR.executeUpdate();
        getDBTransaction().commit();
      }
      vos.clearCache();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return "";
  }

  /**
   * Update the location_id on the selected STOW.
   */
  public boolean setStowLocation(Object l) {
    try {
      if (l != null) {
        if (l.toString().length() <= 0)
          return true;
        boolean f = false;
        try (PreparedStatement stR = getDBTransaction().createPreparedStatement("select location_id, location_label from location where (availability_flag = 'A') and (trim(location_label) like ?) order by location_label asc", 0)) {
          stR.setString(1, l.toString().toUpperCase().trim() + "%");
          try (ResultSet rs = stR.executeQuery()) {
            if (rs.next()) {
              StowViewImpl vos = getStowView2();
              vos.getCurrentRow().setAttribute(LOCATION_ID, rs.getString(1));
              f = true;
            }
          }
        }
        return f;
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return true;
  }

  /**
   * Get the Receipt object for a given document number + suffix.
   * <p>
   * Returns null if no receipt row found
   */
  public Row getExistingReceipt(String data) {
    //Retrieve information from DB if the receipt was already created.
    try {

      ReceiptViewImpl view = getReceiptView1();
      view.setNamedWhereClauseParam("docNumStr", data);
      view.executeQuery();

      Row r = null;
      if (view.getRowCount() > 0) r = view.first();
      view.setNamedWhereClauseParam("docNumStr", "x");

      return r;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return null;
  }

  public Row getExistingReceiptInDraft(String data) {
    //Retrieve information from DB if the receipt was already created.
    try {

      ReceiptViewImpl view = getReceiptView1();

      // clear the last filter
      view.applyViewCriteria(null);

      view.setNamedWhereClauseParam("docNumStr", data);
      ViewCriteria vc = view.createViewCriteria();
      ViewCriteriaRow vcr = vc.createViewCriteriaRow();
      vcr.setUpperColumns(true);
      vcr.setAttribute("Status", " like '%DRAFT%'");
      vc.addElement(vcr);
      view.applyViewCriteria(vc);
      view.executeQuery();

      Row r = null;
      if (view.getRowCount() > 0) r = view.first();
      view.setNamedWhereClauseParam("docNumStr", "x");

      return r;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return null;
  }

  /**
   * Get Niin location list.
   */
  public void getStowLocationList(Object data) {
    //Gets the stowable location where NIIN already exists.
    try {
      NiinInfoReceiptVViewImpl vor = getNiinInfoReceiptVView1();
      NiinLocListVVOFRImpl vollist = getNiinLocListVVOF1();
      vollist.setRangeSize(3);
      vollist.setNamedWhereClauseParam(NIIN_ID_STR, vor.getCurrentRow().getAttribute(NIIN_ID).toString());
      if (data != null) vollist.setNamedWhereClauseParam("expDtStr", data);
      else vollist.setNamedWhereClauseParam("expDtStr", DateConstants.DEFAULT_EXPIRATION_DATE);
      vollist.executeQuery();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  /**
   * Clears location list.
   */
  public void clearLocationList() {
    try {
      LocationListRImpl vo = getLocationList1();
      vo.clearCache();
      NiinLocListVVOFRImpl vollist = getNiinLocListVVOF1();
      vollist.clearCache();
      LocationVORImpl vol = getLocationVO1();
      vol.clearCache();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  /**
   * builds location list.
   */
  public void buildLocationList(String l) {
    //Builds a list of  best three locations for stow.
    try {
      this.clearLocationList();
      LocationListRImpl vo = getLocationList1();
      NiinInfoReceiptVViewImpl vor = getNiinInfoReceiptVView1();
      StowViewImpl vos = getStowView2();
      NiinInfoLVOImpl vnf = getNiinInfoLVO2();
      Row row = null;
      Row vlr = null;
      int i = 0;
      double nWt = 0;
      double nCu = 0;
      if ((l == null) || (l.length() <= 0)) {
        NiinLocListVVOFRImpl vollist = getNiinLocListVVOF1();
        vollist.setRangeSize(3);
        vollist.setNamedWhereClauseParam(NIIN_ID_STR, vor.getCurrentRow().getAttribute(NIIN_ID).toString());
        if (vos.getCurrentRow().getAttribute("ExpirationDate") != null)
          vollist.setNamedWhereClauseParam("expDtStr", vos.getCurrentRow().getAttribute("ExpirationDate").toString());
        else vollist.setNamedWhereClauseParam("expDtStr", DateConstants.DEFAULT_EXPIRATION_DATE);

        nWt = (Double.parseDouble(vor.getCurrentRow().getAttribute("Weight").toString()) / Double.parseDouble(vor.getCurrentRow().getAttribute(QUANTITY_MEASURED).toString())) * Double.parseDouble(vos.getCurrentRow().getAttribute("QtyToBeStowed").toString());
        nCu = (Double.parseDouble(vor.getCurrentRow().getAttribute("Cube").toString()) / Double.parseDouble(vor.getCurrentRow().getAttribute(QUANTITY_MEASURED).toString())) * Double.parseDouble(vos.getCurrentRow().getAttribute("QtyToBeStowed").toString());
        vollist.setNamedWhereClauseParam("newWeight", nWt);
        vollist.setNamedWhereClauseParam("newCube", nCu);

        vollist.executeQuery();
        vlr = vollist.first();
        i = 0;
        while ((vlr != null) && (i++ < 3)) {
          row = vo.createRow();
          row.setAttribute(WAC_ID, vlr.getAttribute(WAC_ID));
          row.setAttribute(LOCATION_ID, vlr.getAttribute(LOCATION_ID));
          row.setAttribute(LOCATION_LABEL, vlr.getAttribute(LOCATION_LABEL));
          row.setAttribute("LocQty", vlr.getAttribute("Qty"));
          vo.insertRow(row);
          vlr = vollist.next();
        }
      }
      if (i < 3) {
        LocationVORImpl vol = getLocationVO1();
        vol.setRangeSize(3 - i);
        if (l != null) vol.setNamedWhereClauseParam("locLabelStr", l.toUpperCase() + '%');
        String mfg = "";
        if (vor.getCurrentRow().getAttribute("MechNonMechFlag") != null && vor.getCurrentRow().getAttribute("MechNonMechFlag").toString().compareTo("Y") == 0)
          mfg = "N"; // Set mechanized to N to retrieve only bulk

        if (mfg.length() <= 0) { // For cases where we can choose any location
          vol.setNamedWhereClauseParam("newWeight", nWt);
          vol.setNamedWhereClauseParam("newCube", nCu);
        }
        vol.setNamedWhereClauseParam("mechFlag", mfg);
        //See if it can support only Secure locations
        if (vnf.getCurrentRow().getAttribute("Scc") != null)
          vol.setNamedWhereClauseParam("secureFlag", "Y");
        vol.executeQuery();
        Row vlr2 = vol.first();
        while ((vlr2 != null) && (i++ < 3)) {
          row = vo.createRow();
          row.setAttribute(WAC_ID, vlr2.getAttribute(WAC_ID));
          row.setAttribute(LOCATION_ID, vlr2.getAttribute(LOCATION_ID));
          row.setAttribute(LOCATION_LABEL, vlr2.getAttribute(LOCATION_LABEL));
          row.setAttribute("LocQty", "0");
          vo.insertRow(row);
          vlr2 = vol.next();
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  /**
   * Get shelf life code of  a given niin_id.
   */
  public String getShelfLifeCode(Row ro) {
    //Return shelf_life_code from NIIN_INFO
    try {
      String slC = "-1";
      if (ro != null && ro.getAttribute(NIIN_ID) != null) {
        try (PreparedStatement stR = getDBTransaction().createPreparedStatement("select shelf_life_code from niin_info where niin_id = ?", 0)) {
          stR.setLong(1, Long.parseLong(ro.getAttribute(NIIN_ID).toString()));
          try (ResultSet rs = stR.executeQuery()) {
            if (rs.next()) slC = rs.getString(1);
          }
        }
        if (slC != null) return slC;
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return "-1";
  }

  /**
   * Validate manufacture date, location_id  for all stow rows.
   */
  public String validateStow() {
    //Performs validations of stow quantity, manufacture date,
    //location assignment for a stow.
    String errLs = "";
    try {
      errLs = this.validateStowRow("N");
      if (errLs.length() > 0) return errLs;

      NiinInfoReceiptVViewImpl vor = getNiinInfoReceiptVView1();
      String sidStrForDate = "";
      int sQty = 0;
      StringBuilder sidStrForLoc = new StringBuilder();
      String slfStr = "N";
      // Check if all locations are assigned
      StowViewImpl vos = getStowView2();
      int r1 = vos.getRowCount();
      int cRInd = 0;
      Row rowS = null;
      if (r1 > 0) {
        cRInd = vos.getCurrentRowIndex();
        rowS = vos.first();
        int r2C = 1;

        Row r = vor.getCurrentRow();
        if (r.getAttribute("ShelfLifeCode") != null
            && (r.getAttribute("ShelfLifeCode").toString().trim().length() > 0)
            && (r.getAttribute("ShelfLifeCode").toString().trim().compareTo("0") != 0)) {

          rowS = vos.first();
          r2C = 1;
          sidStrForLoc = new StringBuilder();
          while (r2C++ <= r1) {
            if (rowS != null) {
              //Removed DoM requirement since users can now directly add Expiration Date
              if (rowS.getAttribute("ExpirationDate") == null) sidStrForLoc.append(", ").append(rowS.getAttribute("Sid"));

              sQty = sQty + ((rowS.getAttribute("QtyToBeStowed") == null) ? 0 : Integer.parseInt(rowS.getAttribute("QtyToBeStowed").toString()));
            }
            rowS = vos.next();
          }
          if (sidStrForDate.length() > 0) errLs = errLs + "MANUFACTURE_DATE " + sidStrForDate;

          slfStr = "Y";
        }
        if (Integer.parseInt(this.getStowQtyTotalForAReceipt(vor.getCurrentRow().getAttribute("Rcn").toString(), -1)) != (Integer.parseInt(r.getAttribute(QUANTITY_INDUCTED).toString()) - Integer.parseInt(r.getAttribute("QuantityReleased").toString()))) {
          errLs = errLs + "STOW_QTY_INV" + ';';
        }
        rowS = vos.first();
        r2C = 1;
        sidStrForLoc = new StringBuilder();
        while (r2C++ <= r1) {
          if (rowS != null && ((rowS.getAttribute(LOCATION_ID) == null)
              || (rowS.getAttribute(LOCATION_ID).toString().equalsIgnoreCase("1"))))
            sidStrForLoc.append(", ").append(rowS.getAttribute("Sid"));

          rowS = vos.next();
        }
      }
      if (sidStrForLoc.length() > 0) errLs = errLs + "STOW_LOC_INV " + sidStrForLoc + ';';
      else if (slfStr.equalsIgnoreCase("Y")) {
        sidStrForLoc = new StringBuilder();
        try (PreparedStatement stR = getDBTransaction().createPreparedStatement("select s.sid, nl.niin_loc_id from niin_location nl, stow s " + " where s.location_id = nl.location_id and " + " (((to_char(s.expiration_date,'YYYYMM') != to_char(nl.expiration_date,'YYYYMM')) " + " or (s.expiration_date is not null and nl.expiration_date is  null))) and s.rcn = ? and nl.niin_id = ?", 0)) {
          stR.setLong(1, Long.parseLong(vor.getCurrentRow().getAttribute("Rcn").toString()));
          stR.setLong(2, Long.parseLong(vor.getCurrentRow().getAttribute(NIIN_ID).toString()));
          try (ResultSet rs = stR.executeQuery()) {
            while (rs.next()) {
              sidStrForLoc.append(", ").append(rs.getString(1));
            }
          }
        }
        if (sidStrForLoc.length() > 0)
          errLs = errLs + "STOW_LOCEXP_INV " + sidStrForLoc + ';';
      }
      if (sidStrForLoc.length() <= 0) {
        sidStrForLoc = new StringBuilder();
        try (PreparedStatement stR = getDBTransaction().createPreparedStatement("select s.sid, nl.niin_loc_id from niin_location nl, stow s " + " where s.location_id = nl.location_id and nl.cc != ? " + " and s.rcn = ? and nl.niin_id = ?", 0)) {
          stR.setString(1, vor.getCurrentRow().getAttribute("Cc").toString());
          stR.setLong(2, Long.parseLong(vor.getCurrentRow().getAttribute("Rcn").toString()));
          stR.setLong(3, Long.parseLong(vor.getCurrentRow().getAttribute(NIIN_ID).toString()));
          try (ResultSet rs = stR.executeQuery()) {
            while (rs.next()) {
              sidStrForLoc.append(", ").append(rs.getString(1));
            }
          }
        }
        if (sidStrForLoc.length() > 0)
          errLs = errLs + "STOW_CC_MIS " + sidStrForLoc + ';';
      }
      if ((sidStrForLoc.length() <= 0) && (this.getSerialControlledFlag(vor.getCurrentRow().getAttribute(NIIN_ID).toString()).equalsIgnoreCase("Y"))) {
        sidStrForLoc = new StringBuilder();
        try (PreparedStatement stR = getDBTransaction().createPreparedStatement("select s.sid from  stow s " + " where s.rcn = ? and s.qty_to_be_stowed != 1", 0)) {
          stR.setLong(1, Long.parseLong(vor.getCurrentRow().getAttribute("Rcn").toString()));
          try (ResultSet rs = stR.executeQuery()) {
            while (rs.next()) {
              sidStrForLoc.append(", ").append(rs.getString(1));
            }
          }
        }
        if (sidStrForLoc.length() > 0)
          errLs = errLs + "STOW_SRL_QTY_INV " + sidStrForLoc + ';';
      }
      if ((sidStrForLoc.length() <= 0) && (this.getSerialControlledFlag(vor.getCurrentRow().getAttribute(NIIN_ID).toString()).equalsIgnoreCase("Y"))) {
        sidStrForLoc = new StringBuilder();
        try (PreparedStatement stR = getDBTransaction().createPreparedStatement("select s.sid from  stow s " + " where s.rcn = ? and s.serial_number is null", 0)) {
          stR.setLong(1, Long.parseLong(vor.getCurrentRow().getAttribute("Rcn").toString()));
          try (ResultSet rs = stR.executeQuery()) {
            while (rs.next()) {
              sidStrForLoc.append(", ").append(rs.getString(1));
            }
          }
        }
        if (sidStrForLoc.length() > 0)
          errLs = errLs + "STOW_SRL_MIS " + sidStrForLoc + ';';
        else {
          //--Validate Serial and/or lot num-----------------------
          try (PreparedStatement stR = getDBTransaction().createPreparedStatement("select unique s.sid from receipt r, stow s, serial_lot_num_track sr\n" + "where (r.rcn = ? and r.rcn = s.rcn and s.serial_number is not null and upper(s.serial_number) = upper(sr.serial_number) and r.niin_id = sr.niin_id) \n" + "union (select unique s2.sid from stow s2 where s2.rcn = ? and s2.serial_number is not null and upper(s2.serial_number) in (select upper(st.serial_number) from stow st, receipt rc \n" + "where ( rc.niin_id = ?  and rc.rcn = st.rcn and st.serial_number is not null) \n" + "group by st.serial_number having count(st.serial_number) > 1))", 0)) {
            stR.setLong(1, Long.parseLong(vor.getCurrentRow().getAttribute("Rcn").toString()));
            stR.setLong(2, Long.parseLong(vor.getCurrentRow().getAttribute("Rcn").toString()));
            stR.setLong(3, Long.parseLong(vor.getCurrentRow().getAttribute(NIIN_ID).toString()));
            try (ResultSet rs = stR.executeQuery()) {
              while (rs.next()) {
                sidStrForLoc.append(", ").append(rs.getString(1));
              }
            }
          }
          if (sidStrForLoc.length() > 0) errLs = errLs + "DUPLICATE_SERIAL_NUM " + sidStrForLoc + ';';
        }
      }
      else if ((sidStrForLoc.length() <= 0) && (this.getLotControlledFlag(vor.getCurrentRow().getAttribute(NIIN_ID).toString()).equalsIgnoreCase("Y"))) {
        sidStrForLoc = new StringBuilder();
        try (PreparedStatement stR = getDBTransaction().createPreparedStatement("select s.sid from  stow s " + " where s.rcn = ? and s.lot_con_num is null", 0)) {
          stR.setLong(1, Long.parseLong(vor.getCurrentRow().getAttribute("Rcn").toString()));
          try (ResultSet rs = stR.executeQuery()) {
            while (rs.next()) {
              sidStrForLoc.append(", ").append(rs.getString(1));
            }
          }
        }
        if (sidStrForLoc.length() > 0)
          errLs = errLs + "STOW_LOT_MIS " + sidStrForLoc + ';';
      }

      vos.setCurrentRowAtRangeIndex(cRInd); //Make sure pointing back to same row.
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return errLs;
  }

  /**
   * Validate manufacture date, location_id  for astow row.
   * fromLoc indicates Y to check for locatio_id other then 1 or null
   */
  public String validateStowRow(String fromLoc) {
    //Performs validations of stow quantity, manufacture date,
    //location assignment for a stow.
    String errLs = "";
    try {
      StowViewImpl vos = getStowView2();
      NiinInfoReceiptVViewImpl vor = getNiinInfoReceiptVView1();
      Row s = vos.getCurrentRow();
      Row r = vor.getCurrentRow();
      if (s == null)
        return errLs;
      String slfStr = "N";
      if (r.getAttribute("ShelfLifeCode") != null && (r.getAttribute("ShelfLifeCode").toString().trim().length() > 0) && (r.getAttribute("ShelfLifeCode").toString().trim().compareTo("0") != 0)) {
        if (s.getAttribute("DateOfManufacture") == null && s.getAttribute("ExpirationDate") == null)
          errLs = errLs + "MANUFACTURE_DATE" + s.getAttribute("Sid") + ';';
        else if (s.getAttribute("ExpirationDate") == null) errLs = errLs + "STOW_UPDATE_MIS" + s.getAttribute("Sid") + ';';
        else slfStr = "Y";
      }
      if (this.getStowQtyTotalForAReceipt() > (Integer.parseInt(r.getAttribute(QUANTITY_INDUCTED).toString()) - Integer.parseInt(r.getAttribute("QuantityReleased").toString())))
        errLs = errLs + "STOW_QTY_INV" + s.getAttribute("Sid") + ';';
      if (((s.getAttribute(LOCATION_ID) == null) || (s.getAttribute(LOCATION_ID).toString().equalsIgnoreCase("1"))) && (fromLoc.equalsIgnoreCase("N")))
        errLs = errLs + "STOW_LOC_INV" + s.getAttribute("Sid") + ';';
      else if ((slfStr.equalsIgnoreCase("Y")) && (fromLoc.equalsIgnoreCase("N"))) {
        try (PreparedStatement stR = getDBTransaction().createPreparedStatement("select nl.niin_loc_id from niin_location nl, stow s " + " where nl.location_id = ? and s.sid = ? and nl.niin_id = ? and " + " (((to_char(s.expiration_date,'YYYYMM') != to_char(nl.expiration_date,'YYYYMM')) " + " or (s.expiration_date is not null and nl.expiration_date is  null))) ", 0)) {
          stR.setString(1, s.getAttribute(LOCATION_ID).toString());
          stR.setString(2, s.getAttribute("Sid").toString());
          stR.setLong(3, Long.parseLong(r.getAttribute(NIIN_ID).toString()));
          try (ResultSet rs = stR.executeQuery()) {
            if (rs.next()) errLs = errLs + "STOW_LOCEXP_INV" + s.getAttribute("Sid") + ';';
          }
        }
      }
      //-----------------------------
      if (this.getSerialControlledFlag(vor.getCurrentRow().getAttribute(NIIN_ID).toString()).equalsIgnoreCase("Y")
          && (s.getAttribute("SerialNumber") == null || s.getAttribute("SerialNumber").toString().length() <= 0)) {
        errLs = errLs + "MISSING_SERIAL_NUM" + s.getAttribute("Sid") + ';';
      }
      //-----------------------------
      if (this.getLotControlledFlag(vor.getCurrentRow().getAttribute(NIIN_ID).toString()).equalsIgnoreCase("Y")
          && (s.getAttribute("LotConNum") == null || s.getAttribute("LotConNum").toString().length() <= 0)) {
        errLs = errLs + "MISSING_LOT_CON_NUM" + s.getAttribute("Sid") + ';';
      }
      //--Validate Serial and/or lot num-----------------------
      if (errLs.isEmpty() && s.getAttribute("SerialNumber") != null) {
        errLs = checkSerial(vor.getCurrentRow().getAttribute("Rcn").toString(), s.getAttribute("SerialNumber").toString().toUpperCase(), s.getAttribute("Sid").toString(), vor.getCurrentRow().getAttribute(NIIN_ID).toString());
        if (Integer.parseInt(s.getAttribute("QtyToBeStowed").toString()) != 1)
          errLs = errLs + "INV_QTY_WITH_SERIAL_NUM" + s.getAttribute("Sid") + ';';
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return errLs;
  }

  public String checkSerial(String rcnStr, String serialNumber, String sid, String niinID) {
    try {
      if (!RegUtils.isAlphaNumericDashes(serialNumber)) return "INVALID_FORMAT: " + serialNumber + " is not alphanumeric.";
      String errLs = "";
      try (PreparedStatement stR = getDBTransaction().createPreparedStatement("select count(*) from receipt r, serial_lot_num_track sr where r.rcn = ? and sr.serial_number = ? and r.niin_id = sr.niin_id ", 0)) {
        stR.setLong(1, Long.parseLong(rcnStr));
        stR.setString(2, serialNumber);
        try (ResultSet rs = stR.executeQuery()) {
          if (rs.next() && rs.getInt(1) > 0) {
            if (sid.equals("")) errLs = errLs + "DUPLICATE_SERIAL_NUM: " + serialNumber + ';';
            else errLs = errLs + "DUPLICATE_SERIAL_NUM: " + sid + ';';
          }
        }
      }

      //check Picking Serial Numbers
      if (!errLs.contains("DUPLICATE_SERIAL_NUM")) {
        try (PreparedStatement stR = getDBTransaction().createPreparedStatement("select count(*) from pick_serial_lot_num psln inner join issue i on i.scn = psln.scn where i.niin_id = ? and psln.serial_number = ? ", 0)) {
          stR.setLong(1, Long.parseLong(niinID));
          stR.setString(2, serialNumber);
          try (ResultSet rs = stR.executeQuery()) {
            if (rs.next() && rs.getInt(1) > 0) {
              if (sid.equals("")) errLs = errLs + "DUPLICATE_SERIAL_NUM: " + serialNumber + ';';
              else errLs = errLs + "DUPLICATE_SERIAL_NUM: " + sid + ';';
            }
          }
        }
      }

      if (!errLs.contains("DUPLICATE_SERIAL_NUM")) {
        if (sid.length() > 0) {
          try (PreparedStatement stR = getDBTransaction().createPreparedStatement("select count(st.serial_number) from receipt r , stow st where (st.rcn = r.rcn and r.niin_id = ? and upper(st.serial_number) = ? and st.sid != ? )", 0)) {
            stR.setLong(1, Long.parseLong(niinID));
            stR.setString(2, serialNumber);
            stR.setString(3, sid);
            try (ResultSet rs = stR.executeQuery()) {
              if (rs.next() && rs.getInt(1) > 0) errLs = errLs + "DUPLICATE_SERIAL_NUM: " + sid + ';';
            }
          }
        }
        else {
          try (PreparedStatement stR = getDBTransaction().createPreparedStatement("select count(st.serial_number) from receipt r , stow st where (st.rcn = r.rcn and r.niin_id = ? and upper(st.serial_number) = ? and st.rcn != ? )", 0)) {
            stR.setLong(1, Long.parseLong(niinID));
            stR.setString(2, serialNumber);
            stR.setString(3, rcnStr);
            try (ResultSet rs = stR.executeQuery()) {
              if (rs.next() && rs.getInt(1) > 0) errLs = errLs + "DUPLICATE_SERIAL_NUM: " + sid + ';';
            }
          }
        }
      }
      return errLs;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      return "ERROR " + e.getMessage();
    }
  }

  /**
   * Validate manufacture date, location_id  for all stow rows.
   */
  public String validateStowAfterSave(int secondRCN) {
    //Performs validations of stow quantity, manufacture date,
    //location assignment for a stow.
    String errLs = "";
    try {
      NiinInfoReceiptVViewImpl vor = getNiinInfoReceiptVView1();
      StringBuilder sidStrForDate = new StringBuilder();
      StringBuilder sidStrForLoc = new StringBuilder();
      String slfStr = "N";
      StringBuilder sidStrForSrl = new StringBuilder();
      StringBuilder sidStrForSrlQty = new StringBuilder();
      StringBuilder sidStrForSrlNum = new StringBuilder();
      StringBuilder sidStrForLotCtl = new StringBuilder();

      Row r = vor.getCurrentRow();
      if (r.getAttribute("ShelfLifeCode") != null &&
          (r.getAttribute("ShelfLifeCode").toString().trim().length() > 0) && (r.getAttribute("ShelfLifeCode").toString().trim().compareTo("0") != 0)) {
        try (PreparedStatement stR = getDBTransaction().createPreparedStatement("select sid from stow where date_of_manufacture is null and expiration_date is null and rcn = ?", 0)) {
          stR.setLong(1, Long.parseLong(vor.getCurrentRow().getAttribute("Rcn").toString()));
          try (ResultSet rs = stR.executeQuery()) {
            while (rs.next()) {
              sidStrForDate.append(", ").append(rs.getString(1));
            }
          }
        }

        if (sidStrForDate.length() > 0) errLs = errLs + "MANUFACTURE_DATE " + sidStrForDate;
        slfStr = "Y";
      }
      int qInv = Integer.parseInt(r.getAttribute(QUANTITY_INDUCTED).toString());
      int qRel = Integer.parseInt(r.getAttribute("QuantityReleased").toString());
      if (Integer.parseInt(this.getStowQtyTotalForAReceipt(vor.getCurrentRow().getAttribute("Rcn").toString(), secondRCN)) != (qInv - qRel))
        errLs = errLs + "STOW_QTY_INV" + ';';
      if (slfStr.equalsIgnoreCase("Y")) {
        sidStrForLoc = new StringBuilder();
        try (PreparedStatement stR = getDBTransaction().createPreparedStatement("select s.sid, nl.niin_loc_id from niin_location nl, stow s " +
            " where s.location_id = nl.location_id and " +
            " (((to_char(s.expiration_date,'YYYYMM') != to_char(nl.expiration_date,'YYYYMM')) " +
            " or (s.expiration_date is not null and nl.expiration_date is  null))) and s.rcn in (?, ?) and nl.niin_id = ?", 0)) {
          stR.setLong(1, Long.parseLong(vor.getCurrentRow().getAttribute("Rcn").toString()));
          stR.setInt(2, secondRCN);
          stR.setLong(3, Long.parseLong(vor.getCurrentRow().getAttribute(NIIN_ID).toString()));
          try (ResultSet rs = stR.executeQuery()) {
            while (rs.next()) {
              sidStrForLoc.append(", ").append(rs.getString(1));
            }
          }
        }
        if (sidStrForLoc.length() > 0)
          errLs = errLs + "STOW_LOCEXP_INV " + sidStrForLoc + ';';
      }
      if (sidStrForLoc.length() <= 0) {
        sidStrForLoc = new StringBuilder();
        try (PreparedStatement stR = getDBTransaction().createPreparedStatement("select s.sid, nl.niin_loc_id from niin_location nl, stow s " +
            " where s.location_id = nl.location_id and nl.cc != ? " +
            " and s.rcn in (?, ?) and nl.niin_id = ?", 0)) {
          stR.setString(1, vor.getCurrentRow().getAttribute("Cc").toString());
          stR.setLong(2, Long.parseLong(vor.getCurrentRow().getAttribute("Rcn").toString()));
          stR.setInt(3, secondRCN);
          stR.setLong(4, Long.parseLong(vor.getCurrentRow().getAttribute(NIIN_ID).toString()));
          try (ResultSet rs = stR.executeQuery()) {
            while (rs.next()) {
              sidStrForLoc.append(", ").append(rs.getString(1));
            }
          }
        }

        if (sidStrForLoc.length() > 0) errLs = errLs + "STOW_CC_MIS " + sidStrForLoc + ';';
      }
      //--Validate Serial and/or lot num-----------------------
      try (PreparedStatement stR = getDBTransaction().createPreparedStatement("select unique s.sid from receipt r, stow s, serial_lot_num_track sr " +
          "where (r.rcn in (?, ?) and r.rcn = s.rcn " +
          "and s.serial_number is not null " +
          "and upper(s.serial_number) = upper(sr.serial_number) and r.niin_id = sr.niin_id) " +
          "union (select unique s2.sid from stow s2 where s2.rcn in (?, ?) " +
          "and s2.serial_number is not null " +
          "and upper(s2.serial_number) in (select upper(st.serial_number) " +
          "from stow st, receipt rc " +
          "where ( rc.niin_id = ?  and rc.rcn = st.rcn and st.serial_number is not null) " +
          "group by st.serial_number having count(st.serial_number) > 1))", 0)) {
        stR.setLong(1, Long.parseLong(vor.getCurrentRow().getAttribute("Rcn").toString()));
        stR.setInt(2, secondRCN);
        stR.setLong(3, Long.parseLong(vor.getCurrentRow().getAttribute("Rcn").toString()));
        stR.setInt(4, secondRCN);
        stR.setLong(5, Long.parseLong(vor.getCurrentRow().getAttribute(NIIN_ID).toString()));
        try (ResultSet rs = stR.executeQuery()) {
          while (rs.next()) {
            sidStrForSrl.append(", ").append(rs.getString(1));
          }
        }
      }

      if (sidStrForSrl.length() > 0)
        errLs = errLs + "DUPLICATE_SERIAL_NUM " + sidStrForSrl + ';';

      //--Validate Qty with Serial Num-----------------------
      try (PreparedStatement stR = getDBTransaction().createPreparedStatement("select s.sid from receipt r, stow s where s.serial_number is not null and s.qty_to_be_stowed != 1 and r.rcn in (?,?) and r.rcn = s.rcn", 0)) {
        stR.setLong(1, Long.parseLong(vor.getCurrentRow().getAttribute("Rcn").toString()));
        stR.setInt(2, secondRCN);
        try (ResultSet rs = stR.executeQuery()) {
          while (rs.next()) {
            sidStrForSrlQty.append(", ").append(rs.getString(1));
          }
        }
      }
      if (sidStrForSrlQty.length() > 0) errLs = errLs + "INV_QTY_WITH_SERIAL_NUM " + sidStrForSrlQty + ';';

      //--Validate Serial Num-----------------------
      if (this.getSerialControlledFlag(vor.getCurrentRow().getAttribute(NIIN_ID).toString()).equalsIgnoreCase("Y")) {
        try (PreparedStatement stR = getDBTransaction().createPreparedStatement("select s.sid from receipt r, stow s where s.serial_number is null and r.rcn in (?,?) and r.rcn = s.rcn", 0)) {
          stR.setLong(1, Long.parseLong(vor.getCurrentRow().getAttribute("Rcn").toString()));
          stR.setInt(2, secondRCN);
          try (ResultSet rs = stR.executeQuery()) {
            while (rs.next()) {
              sidStrForSrlNum.append(", ").append(rs.getString(1));
            }
          }
        }
        if (sidStrForSrlNum.length() > 0) errLs = errLs + "MISSING_SERIAL_NUM " + sidStrForSrlNum + ';';
      }
      //--Validate Lot ctlr Num-----------------------
      if (this.getLotControlledFlag(vor.getCurrentRow().getAttribute(NIIN_ID).toString()).equalsIgnoreCase("Y")) {
        try (PreparedStatement stR = getDBTransaction().createPreparedStatement("select s.sid from receipt r, stow s where s.lot_con_num is null and r.rcn in (?,?) and r.rcn = s.rcn", 0)) {
          stR.setLong(1, Long.parseLong(vor.getCurrentRow().getAttribute("Rcn").toString()));
          stR.setInt(2, secondRCN);
          try (ResultSet rs = stR.executeQuery()) {
            while (rs.next()) {
              sidStrForLotCtl.append(", ").append(rs.getString(1));
            }
          }
        }
        if (sidStrForLotCtl.length() > 0) errLs = errLs + "MISSING_LOT_CON_NUM " + sidStrForLotCtl + ';';
      }
    }
    catch (
        Exception e) {
      AdfLogUtility.logException(e);
    }
    return errLs;
  }

  /**
   * Validation method for Document already Received check
   * Check if the document was already received the string has document number and suffix.
   */
  public boolean validateDocumentInHistory(String data) {
    if (data == null) return false;
    // Check if the document was already received the string has document number and suffix
    try {
      boolean r = false;
      if (data.length() <= 0)
        return true;
      try (PreparedStatement stR = getDBTransaction().createPreparedStatement("select rcn from receipt_hist where document_number = ? and nvl(suffix,'#') = ? and status like '%COMPLETE%' and nvl(document_id,'#') <> 'YLL' " +
          " union select rcn from receipt where document_number = ? and nvl(suffix,'#') = ? and status like '%COMPLETE%' and nvl(document_id,'#') <> 'YLL'", 0)) {
        if (data.contains(",")) {
          stR.setString(1, data.split(",")[0]);
          stR.setString(2, data.split(",")[1]);
          stR.setString(3, data.split(",")[0]);
          stR.setString(4, data.split(",")[1]);
        }
        else {
          stR.setString(1, data.substring(0, 14));
          if (data.trim().length() < 15) stR.setString(2, "#");
          else stR.setString(2, data.substring(14, 15));

          stR.setString(3, data.substring(0, 14));
          if (data.trim().length() < 15) stR.setString(4, "#");
          else stR.setString(4, data.substring(14, 15));
        }
        try (ResultSet rs = stR.executeQuery()) {
          if (rs.next()) r = true;
        }
      }

      return r;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return false;
  }

  /**
   * Builds the Serial Or Lot Con Num VO list.
   */
  public String addSerialOrLotNumVOList(String slNum, String llNum, String qtyStr, String actQty, Date expDt1) {
    try {
      boolean flag = false;
      String qtyL = qtyStr;
      if (slNum.length() > 0) qtyL = "1";
      else if (qtyL.length() <= 0) return "Invalid lot qty.";

      SerialOrLoNumRImpl vo;
      Row row;
      int curQ = 0;
      vo = this.getSerialOrLoNumR1();
      int cou = 0;
      if (vo != null) cou = vo.getRowCount();

      int i = 0;
      while ((i < cou) && (!flag)) {
        if (i == 0) row = vo.first();
        else row = vo.next();
        if ((slNum.length() > 0)
            && (row != null
            && (row.getAttribute("SerialOrLoNum") != null
            && row.getAttribute("SerialOrLoNum").toString().equalsIgnoreCase(slNum))))
          flag = true;

        if ((llNum.length() > 0)
            && (row != null
            && ((row.getAttribute("SerialOrLoNum") == null
            && row.getAttribute("LotConNum") != null)
            && row.getAttribute("LotConNum").toString().equalsIgnoreCase(llNum))))
          flag = true;

        boolean isQtyLotNull = (row != null ? row.getAttribute("QtyLot") : null) == null;
        boolean isQtyLotLessThanZero = (row != null ? row.getAttribute("QtyLot").toString().length() : 0) <= 0;

        curQ = curQ + Integer.parseInt(isQtyLotNull || isQtyLotLessThanZero ? "1" : row.getAttribute("QtyLot").toString());
        i++;
      }
      //Already scanned in
      if (flag && slNum.length() > 0) return slNum + " is already entered.";
      else if (flag && llNum.length() > 0) return llNum + " is already entered.";

      if (Integer.parseInt(actQty) >= curQ + Integer.parseInt(qtyL)) {
        row = vo.createRow();
        row.setAttribute("SerialOrLoNum", slNum);
        row.setAttribute("LotConNum", llNum);
        row.setAttribute("QtyLot", qtyL);
        if (expDt1 != null) row.setAttribute("ExpirationDate", expDt1.dateValue());

        vo.insertRow(row);
      }
      else return "Too many serial numbers.";

      if (Integer.parseInt(actQty) != curQ + Integer.parseInt(qtyL)) return "Quantity did not match.";
      else return "";
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return "Row addition to Serial or Lot Con List failed.";
  }

  /**
   * Checks to see if the niin needs serial num or lot num or if it is missing any.
   */
  public String checkNiinNeedsAnySrlOrLotInfo(String niinIdStr, int qty) {
    try {
      int err = 0;
      String id = "";
      if (qty == 0) return ""; // No pick qty so just ignore

      try (PreparedStatement stR = getDBTransaction().createPreparedStatement("select niin_id, nvl(serial_control_flag,'N'), nvl(lot_control_flag,'N') from niin_info  where niin_id = ? and rownum = 1 ", 0)) {
        stR.setInt(1, Integer.parseInt(niinIdStr));
        try (ResultSet rs = stR.executeQuery()) {
          if (rs.next()) {
            id = rs.getString(1);
            if (rs.getString(2).equalsIgnoreCase("Y")) err = 1;
            if ((err <= 0) && (rs.getString(3).equalsIgnoreCase("Y"))) err = 2;
            else if ((err == 1) && (rs.getString(3).equalsIgnoreCase("Y"))) err = 3;
          }
        }
      }
      if (id.length() <= 0) return "false";

      SerialOrLoNumRImpl vo;
      Row row;
      vo = this.getSerialOrLoNumR1();
      int cou = 0;
      if (vo != null) cou = vo.getRowCount();

      int i = 0;
      int currQty = 0;
      while (i < cou) {
        if (i == 0) row = vo.first();
        else row = vo.next();

        currQty = currQty + Integer.parseInt(row.getAttribute("QtyLot") == null ? "1" : row.getAttribute("QtyLot").toString());
        i++;
      }
      if (cou == 0) {
        if (err == 1) return "Serial number entry required.";
        if (err == 2) return id + " STRATIS indicates item(s) needs lot con num (s).";
        if (err == 3) return id + " STRATIS indicates item(s) needs serial and lot con num (s).";
      }
      else if (currQty < qty) {
        if (err == 1) return id + " Please enter serial number(s) of all the items.";
        if (err == 2) return id + " Please enter lot control number of all the items.";
        if (err == 3) return id + " Please enter serial and lot control number of all the items.";
      }
      else if (currQty > qty) {
        if (err == 1) return id + " Entered serial number(s) of items are more than the quantity.";
        if (err == 2)
          return id + " Entered lot control number of items picked are more than the quantity to be picked. (can only have one lot number)";
        if (err == 3) return id + " Entered serial number(s) and lot number(s) of items are more than the quantity.";
      }
      return "";
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return "Check for Serial or Lot Con requirement failed.";
  }

  /**
   * Creates ref rows for each pick.  (PICK_SERIAL_LOT_NUM)
   */
  public String createSrlOrLotAndPickXref(String scnStr) {
    try {
      int locId = 1;
      try (PreparedStatement stR = getDBTransaction().createPreparedStatement("select location_id from picking p, niin_location nl  where p.scn = ? and p.niin_loc_id = nl.niin_loc_id ", 0)) {
        stR.setString(1, scnStr);
        try (ResultSet rs = stR.executeQuery()) {
          if (rs.next())
            locId = rs.getInt(1);
        }
      }
      SerialOrLoNumRImpl vo = null;
      vo = getSerialOrLoNumR1();
      int cou = 0;
      if (vo != null)
        cou = vo.getRowCount();
      //Existing list of serial/lot numbers
      Row ro;
      int i = 0;
      if (cou > 0) {
        ro = null;
        try (PreparedStatement stR = getDBTransaction().createPreparedStatement("begin INSERT INTO pick_serial_lot_num (scn, serial_number,lot_con_num, timestamp, qty, location_id, expiration_date) values (?,?, ?,sysdate, ?, ?,to_date(?,'YYYY-MM-DD')); end;", 0)) {
          while (i < cou) {
            if (ro == null) ro = vo.first();
            else ro = vo.next();
            stR.setString(1, scnStr);
            stR.setString(2, ro.getAttribute("SerialOrLoNum") == null ? "" : ro.getAttribute("SerialOrLoNum").toString());
            stR.setString(3, ro.getAttribute("LotConNum") == null ? "" : ro.getAttribute("LotConNum").toString());
            stR.setString(4, ro.getAttribute("QtyLot") == null ? "1" : ro.getAttribute("QtyLot").toString());
            stR.setInt(5, locId);
            stR.setString(6, ro.getAttribute("ExpirationDate").toString());
            stR.executeUpdate();
            i++;
          }
        }
        getDBTransaction().commit();
      }
      if (vo != null) vo.clearCache();

      return "";
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return "Creation of Serial or Lot Con requirement failed.";
  }

  /**
   * Checks if there are any stowed records, so that we do not allow any edit happen
   */
  public String checkAnyStowedForTheReceipt(String rcn) {
    String sql = "select count(*) from stow where rcn = ? and status = 'STOWED'";
    int count = DBUtil.getCountValueBy(sql, rcn, getDBTransaction());
    return String.valueOf(count);
  }

  /**
   * Checks if error queue has non convertable UI with error code 7
   */
  public int checkNonConversionUIOnReceipt(String rcn) {
    String sql = "select count(*) from error_queue where key_type = 'RCN' and key_num = ? and eid = 7";
    return DBUtil.getCountValueBy(sql, rcn, getDBTransaction());
  }

  private boolean assignFirstCrossdock(String scn, String rcn) {
    boolean success = false;

    try {
      String sql1 = "update issue set rcn=null where rcn=? and status='BACKORDER' and issue_type='B'";
      try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(sql1, 0)) {
        pstmt.setString(1, rcn);
        pstmt.executeUpdate();
        getDBTransaction().commit();
      }

      String sql = "update issue set rcn=? where scn=? and status='BACKORDER' and issue_type='B' and rcn is null";
      try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(sql, 0)) {
        pstmt.setString(1, rcn);
        pstmt.setString(2, scn);
        int rs = pstmt.executeUpdate();
        getDBTransaction().commit();
        log.debug("assignFirstCrossdock :: {} : {} : RS =  {}", scn, rcn, rs);
        if (rs > 0) success = true;
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      getDBTransaction().rollback();
    }

    return success;
  }

  public String cancelMyCrossdock(String rcn) {
    try {
      String sql = "update issue set rcn=null where rcn=? and status='BACKORDER' and issue_type='B'";
      try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(sql, 0)) {
        pstmt.setString(1, rcn);
        pstmt.executeUpdate();
        getDBTransaction().commit();
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      getDBTransaction().rollback();
    }

    //Other get the details of the RECEIPT
    NiinInfoReceiptVViewImpl nir = getNiinInfoReceiptVView1();
    nir.setRangeSize(-1);
    nir.setNamedWhereClauseParam("rcnStr", rcn);
    nir.executeQuery();
    Row r = nir.first();
    if (r.getAttribute("SerialControlFlag").equals("Y")) return "GoReceivingEnterSerial";
    else return "GoReceivingGeneralDetail";
  }

  /**
   * Check validations and update quantity used up for Cross Dock. Send
   * next forwarding action depending on the conditions.
   */
  public String saveCrossDock(long uId) {
    //Update the quantities on Receipt and Issue based on quantity issued.
    //Perform action forward to next cross dock or stow or new recept.
    try {
      // Get the VO of IssueBackorder
      NiinInfoReceiptVViewImpl nir = getNiinInfoReceiptVView1();
      IssueBackordersVOImpl vob = getIssueBackordersVO1();
      Row crRow = vob.getCurrentRow();
      String rcnS = crRow.getAttribute("Rcn").toString();
      int issQty = 0;
      int curQty = Integer.parseInt(crRow.getAttribute(QUANTITY_INDUCTED).toString()) - Integer.parseInt(crRow.getAttribute("QuantityReleased").toString());
      int actIssQty = Integer.parseInt(crRow.getAttribute("Iqty").toString());

      if (curQty > actIssQty) issQty = actIssQty;
      else issQty = curQty;

      //Deduct released qty from the Receipt
      try (PreparedStatement stR = getDBTransaction().createPreparedStatement("begin update receipt set  quantity_released = (quantity_released + ?), modified_by = ? where rcn = ?; end;", 0)) {
        stR.setInt(1, issQty);
        stR.setLong(2, uId);
        stR.setLong(3, Long.parseLong(crRow.getAttribute("Rcn").toString()));
        stR.executeUpdate();
        getDBTransaction().commit();
      }

      try (PreparedStatement stR = getDBTransaction().createPreparedStatement("begin update issue set status = ?, modified_by = ?, rcn = ? where scn = ?; end;", 0)) {
        stR.setString(1, "PICKED");
        stR.setLong(2, uId);
        stR.setLong(3, Long.parseLong(crRow.getAttribute("Rcn").toString()));
        stR.setString(4, crRow.getAttribute("Scn").toString());

        stR.executeUpdate();
        getDBTransaction().commit();
      }
      try (PreparedStatement stR = getDBTransaction().createPreparedStatement("begin INSERT INTO RECEIPT_ISSUE (rcn, scn, qty) values (?,?,?); end;", 0)) {
        stR.setString(1, crRow.getAttribute("Rcn").toString());
        stR.setString(2, crRow.getAttribute("Scn").toString());
        stR.setInt(3, issQty);
        stR.executeUpdate();
        getDBTransaction().commit();
      }
      if (issQty < actIssQty) this.getWorkloadManagerService().insertPartialBackorder(crRow.getAttribute("Scn").toString(), issQty);

      this.createSrlOrLotAndPickXref(crRow.getAttribute("Scn").toString()); //For serial or lot control create the pick_serial_lot rows

      // Check for next CROSS DOCK and take steps
      vob = getIssueBackordersVO1();
      vob.setRangeSize(-1);
      vob.setNamedWhereClauseParam("rcnStr", crRow.getAttribute("Rcn").toString());
      vob.executeQuery();
      crRow = vob.getCurrentRow();
      // get the latest available
      curQty = curQty - issQty;
      if ((vob.getRowCount() > 0) && (curQty > 0)) return "GoReceivingGeneralCrossDock";
      if (curQty > 0) {
        //Other get the details of the RECEIPT
        nir.setRangeSize(-1);
        nir.setNamedWhereClauseParam("rcnStr", rcnS);
        nir.executeQuery();
        this.createStowForAReceipt(uId, null);
        nir.executeQuery();
        vob.clearCache();
        Row r = nir.first();
        if (r.getAttribute("SerialControlFlag").equals("Y")) return "GoReceivingEnterSerial";
        else return "GoReceivingGeneralDetail";
      }
      // Otherwise you are all done
      vob.clearCache();
      ReceiptViewImpl vor = getReceiptView1();
      vor.clearCache();
      //Receipt has nothing to process in stow so mark it complete
      log.debug(String.format("Receipt complete via cross dock with RCN [%s]", rcnS));
      try (PreparedStatement stR = getDBTransaction().createPreparedStatement("begin update receipt set  status = ?, modified_by = ? where rcn = ?; end;", 0)) {
        stR.setString(1, "RECEIPT COMPLETE");
        stR.setLong(2, uId);
        stR.setString(3, rcnS);
        stR.executeUpdate();
        getDBTransaction().commit();
      }

      log.debug("Sending D6 transaction via cross dock.");
      int d6ReturnCode = getWorkloadManagerService().getGCSSMCTransactionsService().sendD6GCSSMCTransaction(rcnS);
      if (d6ReturnCode == -2) return "D6_DUPLICATE";

      try (PreparedStatement stR = getDBTransaction().createPreparedStatement("begin update stow set serial_number = upper(serial_number), lot_con_num = upper(lot_con_num), status = ?, modified_by = ? where rcn = ? and status='STOW' and location_id<>1; end;", 0)) {
        stR.setString(1, "STOW READY");
        stR.setLong(2, uId);
        stR.setString(3, rcnS);
        stR.executeUpdate();
        getDBTransaction().commit();
      }

      return "GoReceivingGeneral";
    }
    catch (Exception e) {
      getDBTransaction().rollback();
      AdfLogUtility.logException(e);
    }
    return null;
  }

  /**
   * Update status of transshimpment.
   */
  public String saveTransshipment(int rcn, long uId) {
    //Update Status on receipt.
    //Perform action forward to new receipt.
    try {
      // Get the VO of ReceiptTransshipment

      //Deduct released qty from the Receipt
      try (PreparedStatement stR = getDBTransaction().createPreparedStatement("begin update receipt set  status = ?, modified_by = ? where rcn = ?; end;", 0)) {
        stR.setString(1, "TRANSSHIPPING");
        stR.setLong(2, uId);
        stR.setInt(3, rcn);
        stR.executeUpdate();
        getDBTransaction().commit();
      }
      //Call to generate DWF
      // Otherwise you are all done
      ReceiptTransshipmentVOImpl vob = getReceiptTransshipmentVO1();
      vob.clearCache();
      ReceiptViewImpl vor = getReceiptView1();
      vor.clearCache();
      return "GoReceivingGeneral";
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return null;
  }

  public String updateCubiScanStatus(String wrkStnName, String statusStr) {
    String flag = "";

    try {
      try (PreparedStatement stR = getDBTransaction().createPreparedStatement("update cubiscan_spool set status = ? where rec_data = ? and status = 'READY'", 0)) {
        stR.setString(1, statusStr.toUpperCase().trim());
        stR.setString(2, "CUBI-" + wrkStnName.toUpperCase().trim());
        stR.execute();
        this.getDBTransaction().commit();
      }
    }
    catch (Exception e) {
      getDBTransaction().rollback();
      AdfLogUtility.logException(e);
    }
    return flag;
  }

  public String getCubiScanData(String wrkStnName) {
    String sql = "select nvl(GCSSMC_XML,'') from CUBISCAN_SPOOL where REC_DATA = ? order by SPOOL_ID DESC";
    return DBUtil.getSingleReturnValue(sql, "CUBI-" + wrkStnName.toUpperCase().trim(), getDBTransaction());
  }

  public String getSerialControlledFlag(String niinId) {
    String sql = "select nvl(serial_control_flag,'N') from niin_info  where niin_id = ?";
    return DBUtil.getSingleReturnValue(sql, niinId, getDBTransaction());
  }

  public boolean isSerialControlled(String niinId) {
    return getSerialControlledFlag(niinId).equalsIgnoreCase("Y");
  }

  public String getLotControlledFlag(String niinId) {
    String sql = "select nvl(lot_control_flag,'N') from niin_info  where niin_id = ?";
    return DBUtil.getSingleReturnValue(sql, niinId, getDBTransaction());
  }

  public boolean isLotControlled(String niinId) {
    return getLotControlledFlag(niinId).equalsIgnoreCase("Y");
  }

  public TransactionsImpl getTransactionManagerService() {
    try {
      WorkLoadManagerImpl workload = (WorkLoadManagerImpl) getWorkLoadManager1();
      return (TransactionsImpl) workload.getTransactions1();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return null;
  }

  public WorkLoadManagerImpl getWorkloadManagerService() {
    try {
      return (WorkLoadManagerImpl) this.getWorkLoadManager1();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return null;
  }

  /**
   * Container's getter for workloadmanager1
   */
  public ApplicationModuleImpl getWorkLoadManager1() {
    return (ApplicationModuleImpl) findApplicationModule("WorkLoadManager1");
  }

  /**
   * Container's getter for ReceiptTransshipmentVO1
   */
  public ReceiptTransshipmentVOImpl getReceiptTransshipmentVO1() {
    return (ReceiptTransshipmentVOImpl) findViewObject("ReceiptTransshipmentVO1");
  }

  /**
   * Container's getter for MasterDropDownPCVO1
   */
  public MasterDropDownPCVOImpl getMasterDropDownPCVO1() {
    return (MasterDropDownPCVOImpl) findViewObject("MasterDropDownPCVO1");
  }

  /**
   * Container's getter for MasterDropDownPJCVO1
   */
  public MasterDropDownPJCVOImpl getMasterDropDownPJCVO1() {
    return (MasterDropDownPJCVOImpl) findViewObject("MasterDropDownPJCVO1");
  }

  /**
   * Container's getter for MasterDropDownCOGVO1
   */
  public MasterDropDownCOGVOImpl getMasterDropDownCOGVO1() {
    return (MasterDropDownCOGVOImpl) findViewObject("MasterDropDownCOGVO1");
  }

  /**
   * Container's getter for MasterDropDownCCVO1
   */
  public MasterDropDownCCVOImpl getMasterDropDownCCVO1() {
    return (MasterDropDownCCVOImpl) findViewObject("MasterDropDownCCVO1");
  }

  /**
   * Container's getter for WACList1
   */
  public WACListImpl getWACList1() {
    return (WACListImpl) findViewObject("WACList1");
  }

  /**
   * Container's getter for NiinLocationView1
   */
  public NiinLocationViewImpl getNiinLocationView1() {
    return (NiinLocationViewImpl) findViewObject("NiinLocationView1");
  }

  /**
   * Container's getter for NiinInfoReceiptVView1
   */
  public NiinInfoReceiptVViewImpl getNiinInfoReceiptVView1() {
    return (NiinInfoReceiptVViewImpl) findViewObject("NiinInfoReceiptVView1");
  }

  /**
   * Container's getter for LocationList1
   */
  public LocationListRImpl getLocationList1() {
    return (LocationListRImpl) findViewObject("LocationList1");
  }

  /**
   * Container's getter for NiinLocListVView1
   */
  public NiinLocListVViewImpl getNiinLocListVView1() {
    return (NiinLocListVViewImpl) findViewObject("NiinLocListVView1");
  }

  /**
   * Container's getter for StowView1
   */
  public StowViewImpl getStowView1() {
    return (StowViewImpl) findViewObject("StowView1");
  }

  /**
   * Container's getter for StowCancelVO1
   */
  public StowCancelVOImpl getStowCancelVO1() {
    return (StowCancelVOImpl) findViewObject("StowCancelVO1");
  }

  /**
   * Container's getter for NiinLocListVVOF1
   */
  public NiinLocListVVOFRImpl getNiinLocListVVOF1() {
    return (NiinLocListVVOFRImpl) findViewObject("NiinLocListVVOF1");
  }

  /**
   * Container's getter for IssueBackordersVO1
   */
  public IssueBackordersVOImpl getIssueBackordersVO1() {
    return (IssueBackordersVOImpl) findViewObject("IssueBackordersVO1");
  }

  /**
   * Container's getter for StowView2
   */
  public StowViewImpl getStowView2() {
    return (StowViewImpl) findViewObject("StowView2");
  }

  /**
   * Container's getter for NiinInfoReceiptVViewStowViewVO1
   */
  public ViewLinkImpl getNiinInfoReceiptVViewStowViewVO1() {
    return (ViewLinkImpl) findViewLink("NiinInfoReceiptVViewStowViewVO1");
  }

  /**
   * Container's getter for LocationVO1
   */
  public LocationVORImpl getLocationVO1() {
    return (LocationVORImpl) findViewObject("LocationVO1");
  }

  /**
   * Container's getter for LocationWacVO1
   */
  public LocationWacVOImpl getLocationWacVO1() {
    return (LocationWacVOImpl) findViewObject("LocationWacVO1");
  }

  /**
   * Container's getter for WacListQry1
   */
  public WacListQryImpl getWacListQry1() {
    return (WacListQryImpl) findViewObject("WacListQry1");
  }

  /**
   * Container's getter for NiinInfoLVO2
   */
  public NiinInfoLVOImpl getNiinInfoLVO2() {
    return (NiinInfoLVOImpl) findViewObject("NiinInfoLVO2");
  }

  /**
   * Container's getter for ReceiptView1
   */
  public ReceiptViewImpl getReceiptView1() {
    return (ReceiptViewImpl) findViewObject("ReceiptView1");
  }

  /**
   * Container's getter for SerialOrLoNumR1
   */
  public SerialOrLoNumRImpl getSerialOrLoNumR1() {
    return (SerialOrLoNumRImpl) findViewObject("SerialOrLoNumR1");
  }
}
