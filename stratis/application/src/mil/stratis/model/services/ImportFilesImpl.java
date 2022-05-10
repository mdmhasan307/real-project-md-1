package mil.stratis.model.services;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.common.util.PadUtil;
import mil.stratis.common.util.Util;
import mil.stratis.common.util.ZipUtils;
import mil.stratis.model.util.ImportMetricsLogHelper;
import mil.stratis.model.view.wlm.*;
import mil.usmc.mls2.stratis.core.service.multitenancy.DateService;
import mil.usmc.mls2.stratis.core.utility.AdfLogUtility;
import mil.usmc.mls2.stratis.core.utility.AmsCmosUtils;
import mil.usmc.mls2.stratis.core.utility.ContextUtils;
import oracle.jbo.server.ApplicationModuleImpl;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;

@Slf4j
@SuppressWarnings("unused")
@NoArgsConstructor //ADF BackingHandlers all need a no args constructor
public class ImportFilesImpl extends ApplicationModuleImpl {

  public static final String XXXXXXXX = "XXXXXXXX";
  public static final String FIVE_SPACES = "     ";
  public static final String SIX_SPACES = "      ";
  public static final String SIX_ZEROS = "000000";
  private final ImportMetricsLogHelper metricsLogHelper = new ImportMetricsLogHelper("IMPORT");

  public boolean isValidEvent(int scheduleId) {
    boolean rc = false;

    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement("select connection_id from site_schedules where schedule_id = ?", 0)) {
      pstmt.setInt(1, scheduleId);

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          rc = true;
        }
      }
    }
    catch (SQLException sqle) {
      log.warn("isValidEvent: ", sqle);
    }

    return rc;
  }

  /**
   * DESC     : This function exports the AMS-TAC Interface FIles
   * PARAMS   : manifestId - the shipping manifestId to create ITEM File for.
   * filePath - the path the fileName is appended to.
   */

  public boolean exportITEM(int manifestId, int workstationId, String filePath) {
    String fileName;
    boolean retVal = false;
    String ldCon = "";
    String leadTCN = "";
    String scn;
    String lineNum;
    String lineItem;
    String docId;
    String nsn = "";
    String ui = "";
    String issueQty;
    String docNum;
    String supAdd;
    String signalCode;
    String fundCode;
    String ipd;
    String adviceCode;
    StringBuilder price = new StringBuilder();
    String nomen = "";
    String shipQty;
    String masCode;
    String rdd;
    String cc;
    try {

      try (PreparedStatement psImports =
               getDBTransaction().createPreparedStatement("select c.aac, m.lead_tcn, m.manifest, m.mode_of_shipment, f.floor_location from customer c, shipping_manifest m, floor_location f where m.shipping_manifest_id = ? AND m.customer_id = c.customer_id AND m.floor_location_id = f.floor_location_id " +
                   "union select c.aac, m.lead_tcn, m.manifest, m.mode_of_shipment, m.floor_location from customer c, shipping_manifest_hist m where m.shipping_manifest_id = ? AND m.customer_id = c.customer_id", 0)) {

        psImports.setInt(1, manifestId);
        psImports.setInt(2, manifestId);
        try (ResultSet rsImports = psImports.executeQuery()) {
          if (rsImports.next()) {
            if (rsImports.getObject(2) == null) leadTCN = "                 ";
            else leadTCN = rsImports.getString(2);
            ldCon = rsImports.getString(3) + "        ";
          }
        }

        val fileLdConName = ldCon.concat(XXXXXXXX).substring(3, 8);
        fileName = filePath + "ITEM_" + fileLdConName + ".TXT";
        String fileNameAMS = filePath + "PDC" + fileLdConName + ".AMS";

        try (BufferedWriter outAMS = new BufferedWriter(new FileWriter(fileNameAMS))) {
          outAMS.write("\n");
        }

        String sql = "select s.scn, p.consolidation_barcode, s.line_number, " +
            "i.document_id, i.niin_id, i.qty, i.document_number, " +
            "i.supplementary_address, i.signal_code, i.fund_code," +
            "i.issue_priority_designator, i.advice_code, s.quantity, " +
            "i.media_and_status_code, i.rcn, " +
            "nvl(i.rdd, ''), nvl(i.routing_id_from,''),  nvl(i.cc, 'A'), i.qty_issued " +
            "from shipping s, packing_consolidation p, issue i " +
            "where s.shipping_manifest_id = ? and s.scn = i.scn " +
            "and s.packing_consolidation_id = p.packing_consolidation_id " +
            "union " +
            "select s.scn, p.consolidation_barcode, s.line_number, " +
            "i.document_id, i.niin_id, i.qty, i.document_number, " +
            "i.supplementary_address, i.signal_code, i.fund_code," +
            "i.issue_priority_designator, i.advice_code, s.quantity, " +
            "i.media_and_status_code, i.rcn, " +
            "nvl(i.rdd, ''), nvl(i.routing_id_from,''),  nvl(i.cc, 'A'), i.qty_issued " +
            "from shipping_hist s, packing_consolidation_hist p, issue_hist i " +
            "where s.shipping_manifest_id = ? and s.scn = i.scn " +
            "and s.packing_consolidation_id = p.packing_consolidation_id ";
        try (PreparedStatement psItems =
                 getDBTransaction().createPreparedStatement(sql, 0)) {

          psItems.setInt(1, manifestId);
          psItems.setInt(2, manifestId);
          try (ResultSet rsItems = psItems.executeQuery()) {
            try (BufferedWriter out = new BufferedWriter(new FileWriter(fileName))) {
              while (rsItems.next()) {

                String outString;
                //SCN
                if (rsItems.getObject(1) == null) {
                  log.debug("return false from export cmos item");
                  return false;
                }
                else scn = rsItems.getString(1) + "             ";
                //Shipping Barcode
                if (rsItems.getObject(2) == null) lineItem = FIVE_SPACES;
                else lineItem = rsItems.getString(2);
                //Line Number
                if (rsItems.getObject(3) == null) lineNum = FIVE_SPACES;
                else lineNum = rsItems.getString(3);
                //DocId
                docId = rsItems.getString(4);
                if (rsItems.getObject(5) != null) {
                  try (PreparedStatement psNiin =
                           getDBTransaction().createPreparedStatement("select fsc, niin, ui, price, nomenclature, shelf_life_code, weight, cube " +
                               "from niin_info where niin_id = ?", 0)
                  ) {
                    psNiin.setObject(1, rsItems.getObject(5));
                    try (ResultSet rsNiin = psNiin.executeQuery()) {
                      if (rsNiin.next()) {
                        //NSN
                        nsn = rsNiin.getString(1) + rsNiin.getString(2) + "       ";
                        //Unit of Issue
                        if (rsNiin.getObject(3) == null) ui = "  ";
                        else ui = rsNiin.getString(3);
                        //Unit Price
                        price = new StringBuilder(String.valueOf(rsNiin.getDouble(4)));
                        int priceInd = price.indexOf(".");
                        if (priceInd < 0) {
                          price.insert(0, SIX_ZEROS).append(".00");
                        }
                        else if (priceInd == price.length() - 2) {
                          price.insert(0, SIX_ZEROS).append('0');
                        }
                        else price.insert(0, SIX_ZEROS);
                        price.delete(0, price.length() - 10);
                        //Nomenclature
                        if (rsNiin.getObject(5) != null) nomen = rsNiin.getString(5) + "          ";
                        else nomen = "          ";
                      }
                    }
                  }
                }
                else {
                  //Get Data from Receipt table
                  if (rsItems.getObject(15) != null) {
                    try (PreparedStatement psNiin =
                             getDBTransaction().createPreparedStatement("select fsc, niin, ui, price from receipt where rcn = ?", 0)) {

                      psNiin.setObject(1, rsItems.getObject(15));
                      try (ResultSet rsNiin = psNiin.executeQuery()) {
                        if (rsNiin.next()) {
                          //NSN
                          if (rsNiin.getObject(1) == null) nsn = StringUtils.rightPad(nsn, 4);
                          else nsn = rsNiin.getString(1);
                          if (rsNiin.getObject(2) == null) nsn = StringUtils.rightPad(nsn, 9);
                          else nsn += rsNiin.getString(2);
                          nsn = StringUtils.rightPad(nsn, 16);
                          //Unit of Issue
                          if (rsNiin.getObject(3) == null) ui = "  ";
                          else ui = rsNiin.getString(3);
                          //Unit Price
                          if (rsNiin.getObject(4) == null) {
                            price = new StringBuilder("0000000.00");
                          }
                          else {
                            price = new StringBuilder(String.valueOf(rsNiin.getDouble(4)));
                            int priceInd = price.indexOf(".");
                            if (priceInd < 0) {
                              price.insert(0, SIX_ZEROS).append(".00");
                            }
                            else if (priceInd == price.length() - 2) {
                              price.insert(0, SIX_ZEROS).append('0');
                            }
                            price.insert(0, SIX_ZEROS);
                            price.delete(0, price.length() - 10);
                          }
                          //Nomenclature
                          nomen = StringUtils.rightPad("", 11);
                        }
                      }
                    }
                  }
                  else {
                    nsn = StringUtils.rightPad("", 16);
                    ui = StringUtils.rightPad("", 2);
                    price = new StringBuilder("0000000.00");
                    nomen = StringUtils.rightPad("", 11);
                  }
                }

                //Issue Qty
                issueQty = "00000" + rsItems.getInt(6);
                issueQty = issueQty.substring(issueQty.length() - 5);
                //Document Number
                docNum = rsItems.getString(7) + "  ";
                //Supplementary Address
                if (rsItems.getObject(8) == null) supAdd = SIX_SPACES;
                else supAdd = rsItems.getString(8) + SIX_SPACES;
                //Signal Code
                if (rsItems.getObject(9) == null) signalCode = "A";
                else signalCode = rsItems.getString(9);
                //Fund Code
                if (rsItems.getObject(10) == null) fundCode = "  ";
                else fundCode = rsItems.getString(10);
                //IPD (Priority Des)
                if (rsItems.getObject(11) == null) ipd = "  ";
                else ipd = rsItems.getString(11);
                //Advice Code
                if (rsItems.getObject(12) == null) adviceCode = "  ";
                else adviceCode = rsItems.getString(12);
                //Shipping Quantity
                if (rsItems.getObject(13) == null) shipQty = issueQty;
                else {
                  shipQty = "00000" + rsItems.getInt(13);
                  shipQty = shipQty.substring(shipQty.length() - 5);
                }
                //Media and Status
                if (rsItems.getObject(14) == null) masCode = " ";
                else masCode = rsItems.getString(14);

                //Put together the output string
                outString = ldCon.substring(0, 8) + leadTCN.substring(0, 17) + scn.substring(0, 13) + "             ";
                if (lineItem.length() > 5) {
                  lineItem = StringUtils.leftPad(lineItem, 10);
                  outString += lineItem.substring(lineItem.length() - 10);
                }
                else {
                  outString += lineNum.substring(0, 5) + lineItem.substring(0, 5);
                }

                rdd = rsItems.getString(16) + FIVE_SPACES;
                cc = rsItems.getString(18);

                ipd = PadUtil.padIt(ipd, 2); // workaround when priority_designator is T
                outString += docId + "   " + masCode + nsn.substring(0, 15) + "  " + ui.substring(0, 2) + issueQty.substring(0, 5) + docNum.substring(0, 15);
                outString += supAdd.substring(0, 6) + signalCode + fundCode + "   " + "   " + ipd.substring(0, 2) + rdd.substring(0, 3) + adviceCode + "   " + "A" + cc + " " + "  ";
                outString += price.substring(0, 10) + FIVE_SPACES + FIVE_SPACES + nomen.substring(0, 10) + "   " + FIVE_SPACES + "00000.00" + "0000.000" + shipQty.substring(0, 5);
                outString += "0000000000" + "0000.0" + SIX_SPACES;

                out.write(outString);
                out.write("\n");

                retVal = true;
              }
            }
          }
        }
      }
    }
    catch (Exception e) {
      log.error("exportITEM [THIS REALLY NEEDS WORK]: ", e);
    }

    return retVal;
  }

  /**
   * DESC     : This function exports the AMS-TAC Interface FIles
   * PARAMS   : manifestId - the shipping manifestId to create SHIP File for.
   * filePath - the path the fileName is appended to.
   */
  public boolean exportSHIP(int manifestId, int workstationId, String filePath) {
    String fileName = "";
    boolean retVal = false;

    String sql = "select c.aac, m.lead_tcn, m.manifest, m.mode_of_shipment, f.floor_location, " +
        "nvl(m.delivered_date, sysdate) from customer c, shipping_manifest m, floor_location f where " +
        "m.shipping_manifest_id = ? AND m.customer_id = c.customer_id and " +
        "m.floor_location_id = f.floor_location_id " +
        "union select c.aac, m.lead_tcn, m.manifest, m.mode_of_shipment, m.floor_location, " +
        "nvl(m.delivered_date, sysdate) from customer c, shipping_manifest_hist m " +
        "where m.shipping_manifest_id = ? AND m.customer_id = c.customer_id ";
    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(sql, 0)) {
      pstmt.setInt(1, manifestId);
      pstmt.setInt(2, manifestId);
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          String leadTCN;
          String totalCon = "00001";
          String totalCube = "0000001.00";
          String totalWeight = "000001.0";
          String floorLocation;
          String custAAC = rs.getString(1) + SIX_SPACES;
          if (rs.getObject(2) == null) leadTCN = "                 ";
          else leadTCN = rs.getString(2);
          String ldCon = rs.getString(3) + "        ";
          if (rs.getObject(5) == null) floorLocation = FIVE_SPACES;
          else floorLocation = rs.getString(5) + FIVE_SPACES;
          //Get the site AAC

          String aac = "";
          try (PreparedStatement pstmt2 = getDBTransaction().createPreparedStatement("select aac from site_info", 0)) {
            try (ResultSet rs2 = pstmt2.executeQuery()) {
              if (rs2.next()) {
                aac = rs2.getString(1) + SIX_SPACES;
              }
            }
          }

          String outString;
          val custAACSubstring = custAAC.substring(0, 6);
          //LD-CON Number, Lead-TCN, Shipment Unit Num, Ship-To AAC, Ship-From AAC, Total Containers, Total Cube, Total Weight
          outString = ldCon.substring(0, 8) + leadTCN.substring(0, 17) + "             " + custAACSubstring + aac.substring(0, 6) + totalCon + totalCube + totalWeight;
          //TAC, POE, POD, Water Commodity Code, HAZ Storage Cap, Oversize Len, Oversize Wid, Oversize Height, Mark for DODAAC, Priority, CCN, Stop Off Ind, Type_Pack, Air Pallet ID (Floor Location)
          outString += "    " + "   " + "   " + FIVE_SPACES + "   " + "000" + "000" + "   " + custAACSubstring + "3" + FIVE_SPACES + " " + "  " + floorLocation.substring(0, 5);
          fileName = filePath + "SHIP_" + ldCon.concat(XXXXXXXX).substring(3, 8) + ".TXT";
          try (BufferedWriter out = new BufferedWriter(new FileWriter(fileName))) {
            out.write(outString);
          }

          retVal = true;
        }
      }
    }
    catch (SQLException e) {
      log.error("Unable to execute queries. ", e);
    }
    catch (IOException ioe) {
      log.error("Unable to write to file: {} error:", fileName, ioe);
    }
    return retVal;
  }

  /**
   * DESC     : This function exports the AMS-TAC Interface FIles
   * PARAMS   : manifestId - the shipping manifestId to create GBL File for.
   * filePath - the path the fileName is appended to.
   */
  public boolean exportGBL(int manifestId, int workstationId, String filePath) {

    String fileName = "";
    boolean retVal = false;
    String sql =
        "select c.aac, m.lead_tcn, m.manifest, m.mode_of_shipment, nvl(m.delivered_date, sysdate) " +
            "from customer c, shipping_manifest m where m.shipping_manifest_id = ? AND " +
            "m.customer_id = c.customer_id " + "union select c.aac, m.lead_tcn, m.manifest, m.mode_of_shipment, " +
            "nvl(m.delivered_date, sysdate) from customer c, shipping_manifest_hist m " +
            "where m.shipping_manifest_id = ? AND m.customer_id = c.customer_id ";

    try {

      String modeOfShip = "";
      String leadTCN = "";
      String carrierName = "SMU                          ";
      String custAAC = "";
      String ldCon = "";

      try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(sql, 0)) {

        pstmt.setInt(1, manifestId);
        pstmt.setInt(2, manifestId);
        try (ResultSet rs = pstmt.executeQuery()) {
          if (rs.next()) {
            custAAC = rs.getString(1);
            if (rs.getObject(2) == null)
              leadTCN = "                 ";
            else
              leadTCN = rs.getString(2);
            ldCon = rs.getString(3);
            if (rs.getObject(4) == null)
              modeOfShip = "9";
            else
              modeOfShip = rs.getString(4);
          }

          sql = "select w.carrier_name from warehouse w, equip e where e.warehouse_id = w.warehouse_id " +
              "and e.equipment_number = ? ";
          try (PreparedStatement pstmt2 = getDBTransaction().createPreparedStatement(sql, 0)) {
            pstmt2.setInt(1, workstationId);
            try (ResultSet rs2 = pstmt2.executeQuery()) {
              if (rs2.next() && rs2.getObject(1) != null) {
                carrierName = StringUtils.rightPad(rs2.getString(1), 29);
                carrierName = carrierName.substring(0, 29);
              }
            }
          }
        }

        //Get the site AAC
        String aac = "";
        try (PreparedStatement pstmt2 = getDBTransaction().createPreparedStatement("select aac from site_info", 0)) {
          try (ResultSet rs = pstmt2.executeQuery()) {
            if (rs.next()) {
              aac = rs.getString(1);
            }
          }
        }

        //this is the server timezone, we need to switch it to the users timezone...
        val offsetDateTime = OffsetDateTime.now();
        val dateService = ContextUtils.getBean(DateService.class);
        val convertedDate = dateService.shiftAndFormatDate(offsetDateTime, "yyyyMMddHHmm");

        //Build the GBL File
        String outString = ldCon.substring(0, 8) + aac.substring(0, 4) + carrierName + modeOfShip + convertedDate;
        outString += "  " + leadTCN + custAAC + "        ";

        fileName = filePath + "GBL_" + ldCon.concat(XXXXXXXX).substring(3, 8) + ".TXT";
        try (BufferedWriter out = new BufferedWriter(new FileWriter(fileName))) {
          out.write(outString);
        }

        retVal = true;
      }
    }
    catch (SQLException e) {
      log.error("Unable to execute queries. ", e);
    }
    catch (IOException ioe) {
      log.error("Unable to write to file: {} error:", fileName, ioe);
    }
    return retVal;
  }

  /**
   * DESC     : This function exports the AMS-TAC Interface FIles
   * PARAMS   : manifestId - the shipping manifestId to create AMS-TAC / CMOS Files for.
   */
  public String exportAMSCMOS(int manifestId, int workstationId) {

    String retVal = "Error";
    try {
      val amsUtils = ContextUtils.getBean(AmsCmosUtils.class);
      val filePath = amsUtils.getAmsCmosPath();
      if (filePath == null) return "Error - Failed to find AMS-CMOS path.";

      //We have a file path.   Now we need to export the 3 different file types
      retVal = "Error";

      //EXPORT GBL FILE TO SERVER
      if (this.exportGBL(manifestId, workstationId, filePath)) {
        //EXPORT SHIP FILE TO SERVER
        if (this.exportSHIP(manifestId, workstationId, filePath)) {
          //EXPORT ITEM FILE TO SERVER
          if (this.exportITEM(manifestId, workstationId, filePath)) {
            //All Files Exported Correctly
            String ldCon = null;
            try (PreparedStatement psManifest = getDBTransaction().createPreparedStatement("select manifest from shipping_manifest where shipping_manifest_id = ? union select manifest from shipping_manifest_hist where shipping_manifest_id=?", 0)) {
              psManifest.setInt(1, manifestId);
              psManifest.setInt(2, manifestId);
              try (ResultSet rsManifest = psManifest.executeQuery()) {
                if (rsManifest.next() && rsManifest.getObject(1) != null)
                  ldCon = rsManifest.getString(1).concat(XXXXXXXX).substring(3, 8);
              }
            }
            String[] fileStr = {"", "", "", ""};
            fileStr[0] = "GBL_" + ldCon + ".TXT";
            fileStr[1] = "SHIP_" + ldCon + ".TXT";
            fileStr[2] = "ITEM_" + ldCon + ".TXT";
            fileStr[3] = "PDC" + ldCon + ".AMS";

            SysAdminImpl service = getSysAdminImpl();
            String classText = "";
            if (service != null) {
              classText = service.getConfigValue("STRATIS_CLASS_TEXT");
              if (classText == null || Util.isEmpty(classText)) {
                classText = "UNCLASSIFIED, FOR OFFICIAL USE ONLY";
              }
            }
            retVal = Util.trimClean(classText) + "_AMS" + ldCon + ".ZIP";

            if (!ZipUtils.zipFiles(fileStr, retVal, filePath)) {
              retVal = "Error - Failed to ZIP AMS-CMOS Files";
            }
          }
          else
            retVal = "Error - Failed to create ITEM File";
        }
        else
          retVal = "Error - Failed to create SHIP File";
      }
      else
        retVal = "Error - Failed to create GBL File";
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return retVal;
  }

  // FUTURE Legacy always called with numChars 4.  Should we remove the param?
  private String getCurrentJulian(int numChars) {
    String retVal = "";

    Calendar cal = Calendar.getInstance();
    SimpleDateFormat simpleDF = new SimpleDateFormat("yyyyDDD");
    String formattedDate = simpleDF.format(cal.getTime());

    if (numChars == 5)
      retVal += formattedDate.charAt(2);
    if (numChars == 4 || numChars == 5)
      retVal += formattedDate.charAt(3);

    if (numChars >= 3 && numChars <= 5) {
      retVal += formattedDate.charAt(4);
      retVal += formattedDate.charAt(5);
      retVal += formattedDate.charAt(6);
      return retVal;
    }
    return "   ";
  }

  public boolean exportGCSSMCXML(int gcssmcImportsDataId) {
    try (PreparedStatement psA = getDBTransaction().createPreparedStatement("select interface_name, rec_data from gcssmc_imports_data where gcssmc_imports_data_id = ?", 0)) {
      String longFile;
      Calendar cal = new GregorianCalendar();
      int hour24 = cal.get(Calendar.HOUR_OF_DAY); // 0..23
      int min = cal.get(Calendar.MINUTE); // 0..59
      int sec = cal.get(Calendar.SECOND); // 0..59

      psA.setInt(1, gcssmcImportsDataId);
      Clob c;
      try (ResultSet rsA = psA.executeQuery()) {
        if (rsA.next()) {
          String fileName;
          try (PreparedStatement psImports = getDBTransaction().createPreparedStatement("select file_name, file_path FROM SITE_INTERFACES where interface_name = ?", 0)) {
            psImports.setString(1, rsA.getString("interface_name"));
            try (ResultSet rsImports = psImports.executeQuery()) {
              if (rsImports.next()) {
                String filePath = rsImports.getString(2);
                fileName = gcssmcImportsDataId + "_" + rsA.getString(1);
                c = rsA.getClob(2);
                longFile = fileName + "_" + this.getCurrentJulian(4) + "_" + hour24 + min + sec + fileName.substring(fileName.length() - 4);

                if (c != null) {
                  try (InputStream clobInputStream = c.getAsciiStream();
                       BufferedWriter backupOut = new BufferedWriter(new FileWriter(filePath + longFile + ".xml"));
                       ByteArrayOutputStream outs = new ByteArrayOutputStream()) {
                    int chunkSize = 1024;
                    byte[] buffer = new byte[chunkSize];
                    int count;
                    while ((count = clobInputStream.read(buffer)) > 0) {
                      outs.write(buffer, 0, count);
                    }
                    backupOut.write(outs.toString(StandardCharsets.UTF_8.toString()));
                  }
                }
                return true;
              }
            }
          }
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return false;
  }

  public String getSiteRoutingId() {
    String routingId = "N";
    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement("select UPPER(Routing_Id) from site_info", 0)) {
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          routingId = rs.getString(1);
        }
      }
    }
    catch (SQLException e) {
      log.error("Error selecting routing id from site_info. ", e);
    }
    return routingId;
  }

  public String getSiteInterfaceStatus(String name) {
    String status = "";
    String sql = "select status from site_interfaces where interface_name=?";
    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(sql, 0)) {
      pstmt.setString(1, name);
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next())
          status = rs.getString(1);
      }
    }
    catch (SQLException e) {
      log.error("Error querying interface status. ", e);
    }

    if (status == null)
      status = "";

    return status;
  }

  private boolean isSiteInterfaceComplete(String name) {
    metricsLogHelper.logStartTimestamp("isSiteInterfaceComplete(" + name + ")");
    String status = "";
    String sql = "select status from site_interfaces where interface_name=?";
    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(sql, 0)) {
      pstmt.setString(1, name);
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          status = rs.getString(1);
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      if (!getDBTransaction().isConnected())
        getDBTransaction().reconnect();
    }
    if (status == null)
      status = "";

    metricsLogHelper.logEndTimestamp("isSiteInterfaceComplete(" + name + ")");
    return (!status.equals("RUNNING") && !status.equals("PROCESSING"));
  }

  public boolean checkDASFFinished() {
    return isSiteInterfaceComplete("DASF");
  }

  public boolean checkGABFFinished() {
    return isSiteInterfaceComplete("GABF");
  }

  public boolean checkMHIFFinished() {
    return isSiteInterfaceComplete("MHIF");
  }

  public boolean checkGBOFFinished() {
    return isSiteInterfaceComplete("GBOF");
  }

  public boolean checkMATSFinished() {
    return isSiteInterfaceComplete("MATS");
  }

  /**
   * Container's getter for RefDasfView1
   */
  public RefDasfViewImpl getRefDasfView1() {
    return (RefDasfViewImpl) findViewObject("RefDasfView1");
  }

  /**
   * Container's getter for RefGabfView1
   */
  public RefGabfViewImpl getRefGabfView1() {
    return (RefGabfViewImpl) findViewObject("RefGabfView1");
  }

  /**
   * Container's getter for RefMhifView1
   */
  public RefMhifViewImpl getRefMhifView1() {
    return (RefMhifViewImpl) findViewObject("RefMhifView1");
  }

  /**
   * Container's getter for RefMatsView1
   */
  public RefMatsViewImpl getRefMatsView1() {
    return (RefMatsViewImpl) findViewObject("RefMatsView1");
  }

  /**
   * Container's getter for workloadmanager1
   */
  public ApplicationModuleImpl getWorkLoadManager1() {
    return (ApplicationModuleImpl) findApplicationModule("WorkLoadManager1");
  }

  /**
   * Container's getter for SysAdminImpl
   */
  public SysAdminImpl getSysAdminImpl() {
    return (SysAdminImpl) findApplicationModule("SysAdmin1");
  }

  /**
   * Container's getter for RefGbofView1
   */
  public RefGbofViewImpl getRefGbofView1() {
    return (RefGbofViewImpl) findViewObject("RefGbofView1");
  }

  /**
   * Container's getter for NiinInfoView1
   */
  public NiinInfoViewImpl getNiinInfoView1() {
    return (NiinInfoViewImpl) findViewObject("NiinInfoView1");
  }

  /**
   * Container's getter for NiinInfoMATSView1
   */
  public NiinInfoMATSViewImpl getNiinInfoMATSView1() {
    return (NiinInfoMATSViewImpl) findViewObject("NiinInfoMATSView1");
  }

  /**
   * Container's getter for customerView1
   */
  public CustomerViewImpl getCustomerView1() {
    return (CustomerViewImpl) findViewObject("CustomerView1");
  }
}
