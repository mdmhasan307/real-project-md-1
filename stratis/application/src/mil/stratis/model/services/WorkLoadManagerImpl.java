package mil.stratis.model.services;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.common.dm.LocationSelectionOption;
import mil.stratis.common.util.RegUtils;
import mil.stratis.common.util.Util;
import mil.stratis.model.util.AppModuleDBTransactionGetterImpl;
import mil.stratis.model.util.DBTransactionGetter;
import mil.stratis.model.view.pack.ConsolidationViewImpl;
import mil.stratis.model.view.pack.PackingIssuesViewImpl;
import mil.stratis.model.view.pack.PickingViewImpl;
import mil.stratis.model.view.pick.SerialOrLotNumListImpl;
import mil.stratis.model.view.wlm.*;
import mil.usmc.mls2.stratis.core.utility.AdfLogUtility;
import mil.usmc.mls2.stratis.core.utility.ContextUtils;
import mil.usmc.mls2.stratis.modules.workload.service.WorkloadService;
import oracle.jbo.Row;
import oracle.jbo.ViewCriteria;
import oracle.jbo.ViewCriteriaRow;
import oracle.jbo.domain.DBSequence;
import oracle.jbo.domain.Date;
import oracle.jbo.server.ApplicationModuleImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MCPX - Single/Multi Station
 * CPCX - Consolidation Station
 */
@Slf4j
@NoArgsConstructor //ADF BackingHandlers all need a no args constructor
public class WorkLoadManagerImpl extends ApplicationModuleImpl {

  private int workstationId;
  private String textForDialog = ""; //Used by the Error message text builder

  /**
   * This function determines the appropriate routing for an item to a packing
   * station from a picking station.   Primarily, if it is suitable for consolidation.
   * it is assumed that by default, an item is destined for consolidation, and that the
   * class checks for every case where it can try to deny an item to go to a consolidation
   * station.    After it determines whether an item can be consolidated or not, it determines
   * which specific station to send the item to based on workload distribution and
   * customer criteria.
   * <p>
   * mcpxPref
   */
  public String getPackingStation(int pid, int userId, Integer qtyPicked, int mcpxPref) {
    //This function now calls the converted methods in the Spring controlled WorkloadService class.
    val workloadService = ContextUtils.getBean(WorkloadService.class);

    val result = workloadService.getPackingStation(pid, userId, qtyPicked, mcpxPref);
    return result.packingStationName();
  }

  /**
   * This function takes a consolidation id and adds
   * all related issues to the SHIPPING table.
   */
  public int sendToShipping(int consolId, int userId) {
    val shipView = getShippingView1();
    val stratsTrans = (TransactionsImpl) getTransactions1();

    //Get all of the SCNs for the given consolidation
    try (val stR = getDBTransaction().createPreparedStatement("select issue.scn, issue.packing_consolidation_id, issue.qty, issue.document_number, nvl(issue.suffix,''), nvl(issue.issue_type,''), sum(picking.qty_picked) from issue left join picking Picking on issue.scn = picking.scn where issue.packing_consolidation_id = ? group by issue.scn, issue.packing_consolidation_id, issue.qty, issue.document_number, issue.suffix, issue.issue_type", 0)) {
      stR.setInt(1, consolId);
      try (ResultSet rs = stR.executeQuery()) {
        //loop through all of the SCNs in the consolidation
        while (rs.next()) {
          //Create a new row in the SHIPPING table
          Row shipRow = shipView.createRow();
          shipRow.setAttribute("CreatedBy", userId);
          shipRow.setAttribute("CreatedDate", (new Date(new Timestamp(System.currentTimeMillis()))));
          shipRow.setAttribute("ModifiedBy", userId);
          shipRow.setAttribute("Quantity", rs.getInt(7));
          shipRow.setAttribute("Scn", rs.getString(1));
          shipRow.setAttribute("PackingConsolidationId", rs.getInt(2));
          String docNum = rs.getString(4);
          String suffixCode = "X";
          if (rs.getString(5) != null && !rs.getString(5).equals("") && !rs.getString(5).equals(" "))
            suffixCode = rs.getString(5);
          String tcn = docNum + suffixCode.charAt(0) + "XX";
          shipRow.setAttribute("Tcn", tcn);

          shipView.insertRow(shipRow);
          //Commit the transaction

          /* ********************************************
           * Create Z7K Transaction
           * ****************************************** */
          String issueType = rs.getString(6);
          if (issueType == null) issueType = "";
          if (!issueType.equals("B") && !issueType.equals("T") && !issueType.equals("W")) {
            //Only send for A5_ normal issues
            getGCSSMCTransactionsService().sendAE1GCSSMCTransaction(rs.getString(1));
          }
        }
        getDBTransaction().commit();
      }

      return 0;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      return -1;
    }
  }

  public int updateNiinLocForHardcard(int niinLocId, int qtyMoved, int userId) {
    int result = 0;
    String sql = "update niin_location set qty=qty-?, modified_by=?, modified_date=sysdate where niin_loc_id=?";
    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(sql, 0)) {
      pstmt.setInt(1, qtyMoved);
      pstmt.setInt(2, userId);
      pstmt.setInt(3, niinLocId);
      pstmt.execute();
      this.getDBTransaction().commit();
    }
    catch (Exception e) {
      result = -999;
    }
    return result;
  }

  /**
   * Used by walkthru to update the quantities back to previous state on error
   */
  public int undoNiinLocForHardcard(List<Map<String, Object>> rollbackList) {
    int result = 0;
    String sql = "update niin_location set qty=qty+?, modified_by=?, modified_date=sysdate where niin_loc_id=?";
    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(sql, 0)) {
      for (Map<String, Object> hm : rollbackList) {
        pstmt.setObject(1, hm.get("qty"));
        pstmt.setObject(2, hm.get("userId"));
        pstmt.setObject(3, hm.get("niinLocationId"));
        pstmt.addBatch();
      }
      pstmt.executeBatch();
      this.getDBTransaction().commit();
    }
    catch (Exception e) {
      result = -999;
      AdfLogUtility.logException(e);
    }
    return result;
  }

  /**
   * Used by walk thru to remove from issue, picking, issue_hist, and
   * picking_hist on error.
   */
  public void undoIssues(String scn) {
    try {
      try (PreparedStatement psDel = getDBTransaction().createPreparedStatement("delete from picking where scn = ?", 0)) {
        psDel.setString(1, scn);
        psDel.execute();
        this.getDBTransaction().commit();
      }
      try (PreparedStatement psDel2 = getDBTransaction().createPreparedStatement("delete from issue where scn = ?", 0)) {
        psDel2.setString(1, scn);
        psDel2.execute();
        this.getDBTransaction().commit();
      }

      try (PreparedStatement psDel3 = getDBTransaction().createPreparedStatement("delete from picking_hist where scn = ? and (created_date > sysdate - 1)", 0)) {
        psDel3.setString(1, scn);
        psDel3.execute();
        this.getDBTransaction().commit();
      }
      try (PreparedStatement psDel4 = getDBTransaction().createPreparedStatement("delete from issue_hist where scn = ? and (created_date > sysdate - 1)", 0)) {
        psDel4.setString(1, scn);
        psDel4.execute();
        this.getDBTransaction().commit();
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  /**
   * This function inserts all of the necessary rows in the database to take an issue directly from
   * a receipt state to a state in shipping, tricking the system to think it underwent necessary picks
   * and packs to do so.  This is done for CROSS-DOCKS and TRANS-SHIPMENTS.
   * PARMS    : issueSCN - The issue SCN from the ISSUE Table
   * pickList - the list of items that are undergoing a pick/pack "simulation"
   * userId - the user id from the USERS table (from the session bean)
   */
  public int hardcardWalkThru(String issueSCN, List<Map<String, Object>> pickList, int userId, boolean isSerial, boolean isLot, String niinId, List<String> serialTrackIds) {

    int retVal = -1;
    Map<String, Object> hmRollback;
    List<Map<String, Object>> rollbackList = new ArrayList<>();
    try {
      PickingViewImpl pickView = getPickingView1();
      Map<String, Object> pickMap;
      Iterator<Map<String, Object>> itr = pickList.iterator();
      int pickCount = 0;
      retVal = 0;

      while (itr.hasNext() && retVal >= 0) {
        //List is a list of hashmaps, get a hashmap for the next spot in the arraylist.
        pickMap = itr.next();
        Row pickRow = pickView.createRow();

        pickRow.setAttribute("Scn", issueSCN);
        String pinStr = issueSCN;
        if (pickCount > 0) pinStr += pickCount;
        pickRow.setAttribute("Pin", pinStr);

        int qty = Integer.parseInt(pickMap.get("qty").toString());
        int niinLocationId = Integer.parseInt(pickMap.get("niinlocid").toString());

        pickRow.setAttribute("SuffixCode", pickCount);
        pickRow.setAttribute("PickQty", qty);
        pickRow.setAttribute("QtyPicked", qty);
        pickRow.setAttribute("NiinLocId", niinLocationId);
        pickRow.setAttribute("CreatedBy", userId);
        pickRow.setAttribute("ModifiedBy", userId);
        pickRow.setAttribute("Status", "WALKTHRU");

        this.getDBTransaction().commit();

        retVal = updateNiinLocForHardcard(niinLocationId, qty, userId);
        if (retVal >= 0) {
          hmRollback = new HashMap<>();
          hmRollback.put("niinLocationId", niinLocationId);
          hmRollback.put("qty", qty);
          hmRollback.put("userId", userId);
          rollbackList.add(hmRollback);
        }
        else retVal = -888;

        pickCount++;
      }

      if (retVal >= 0) {
        if (isSerial || isLot) {
          updateIssuedFlag(niinId, serialTrackIds, isLot);
          updatePickSerialLotNum(issueSCN, serialTrackIds, isLot);
        }
        TransactionsImpl trans = (TransactionsImpl) this.getTransactions1();

        //MCF 11/16/2015: Walk-thru change, no Z0 Transactions anymore
        //Send walk-thru transaction
        retVal = this.getGCSSMCTransactionsService().sendZ0GCSSMCTransaction(issueSCN, issueSCN, null, 1);

        return retVal;
      }

      if (isSerial || isLot) {
        //* problems occurred, niin loc may have conflict with update qty to negative number
        undoIssuedFlag(niinId, serialTrackIds, isLot);
        undoPickSerialLotNum(issueSCN);
      }

      undoIssues(issueSCN);
      if (CollectionUtils.isNotEmpty(rollbackList)) undoNiinLocForHardcard(rollbackList);
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return retVal;
  }

  private void updatePickSerialLotNum(String scn, List<String> serialTrackIds, boolean isLot) {
    String pid;
    String serialnumber;
    String locationId;
    String qty;

    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement("insert into pick_serial_lot_num (serial_lot_num_track_id, pid, qty, timestamp, scn) values (?,?,?, sysdate,?)", 0)) {
      log.debug("insert into pick_serial_lot_num");
      for (String trackIdQty : serialTrackIds) {

        StringTokenizer tk = new StringTokenizer(trackIdQty, ",");
        serialnumber = tk.nextToken();

        if (isLot) qty = tk.nextToken();
        else qty = "1";

        locationId = tk.nextToken();

        try (PreparedStatement pstmtSEL = getDBTransaction().createPreparedStatement("select pid from picking p inner join niin_location nl on nl.niin_loc_id = p.niin_loc_id where scn=? and location_id =?", 0)) {
          pstmtSEL.setString(1, scn);
          pstmtSEL.setString(2, locationId);
          try (ResultSet rs = pstmtSEL.executeQuery()) {

            if (rs.next()) pid = rs.getString(1);
            else pid = "";
          }
        }

        pstmt.setString(1, serialnumber);
        pstmt.setString(2, pid);
        pstmt.setString(3, qty);
        pstmt.setString(4, scn);
        pstmt.addBatch();
      }
      pstmt.executeBatch();
      getDBTransaction().commit();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  /**
   * Update the issued flag for serial numbers and/or lot control numbers of a specific niin
   * The list used is SerialOrLotNumListImpl.
   * Used primarily by hardcard walk thru
   */
  private void undoIssuedFlag(String niinId, List<String> serialTrackIds, boolean isLot) {
    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement("update serial_lot_num_track set issued_flag='N', qty = qty+? where serial_lot_num_track_id=? and niin_id = ?", 0)) {

      for (String trackId : serialTrackIds) {
        StringTokenizer tk = new StringTokenizer(trackId, ",");
        if (isLot) {
          String undoTrackId = tk.nextToken();
          int undoQty = Integer.parseInt(tk.nextToken());
          pstmt.setInt(1, undoQty);
          pstmt.setString(2, undoTrackId);
        }
        else {
          pstmt.setInt(1, 1);
          pstmt.setString(2, tk.nextToken());
        }
        pstmt.setInt(3, Integer.parseInt(niinId));
        pstmt.addBatch();
      }
      pstmt.executeBatch();
      getDBTransaction().commit();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  private void undoPickSerialLotNum(String scn) {
    String pid = "";
    try (PreparedStatement pstmtSEL = getDBTransaction().createPreparedStatement("select distinct pid from picking where scn=?", 0)) {
      pstmtSEL.setString(1, scn);
      try (ResultSet rs = pstmtSEL.executeQuery()) {
        if (rs.next()) pid = rs.getString(1);
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }

    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement("delete from pick_serial_lot_num where pid =?", 0)) {

      pstmt.setString(1, pid);
      pstmt.executeUpdate();
      getDBTransaction().commit();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  /**
   * Update the issued flag for serial numbers and/or lot control numbers of a specific niin
   * The list used is SerialOrLotNumListImpl.
   * Used primarily by hardcard walk thru
   */
  private void updateIssuedFlag(String niinId, List<String> serialTrackIds, boolean isLot) {
    StringBuilder results = new StringBuilder();
    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement("update serial_lot_num_track set issued_flag=?, qty=qty-? where serial_lot_num_track_id=? and niin_id = ?", 0)) {

      for (String trackId : serialTrackIds) {
        if (isLot) {
          StringTokenizer tk = new StringTokenizer(trackId, ",");
          String pickTrackId = tk.nextToken();
          int pickQty = Integer.parseInt(tk.nextToken());
          pstmt.setString(1, "N");
          pstmt.setInt(2, pickQty);
          pstmt.setString(3, pickTrackId);
        }
        else {
          pstmt.setString(1, "Y");
          pstmt.setInt(2, 1);
          StringTokenizer tk = new StringTokenizer(trackId, ",");
          pstmt.setString(3, tk.nextToken());
        }

        pstmt.setInt(4, Integer.parseInt(niinId));
        pstmt.addBatch();
      }
      pstmt.executeBatch();
      getDBTransaction().commit();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      results.append("error ").append(e);
      getDBTransaction().rollback();
    }

    if (isLot && results.length() < 1) {
      try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement("update serial_lot_num_track set issued_flag='Y' where qty<1 and serial_lot_num_track_id=? and niin_id = ?", 0)) {

        for (String trackId : serialTrackIds) {
          StringTokenizer tk = new StringTokenizer(trackId, ",");
          pstmt.setString(1, tk.nextToken());
          pstmt.setInt(2, Integer.parseInt(niinId));
          pstmt.addBatch();
        }
        pstmt.executeBatch();
        getDBTransaction().commit();
      }
      catch (Exception e) {
        AdfLogUtility.logException(e);
        results.append("error ").append(e);
        getDBTransaction().rollback();
      }
    }
  }

  /**
   * This function generates a list of serial numbers. It assumes serial snumbers are subdivided into 2 pieces:
   * a prefix and a postfix. The prefix is alphanumerical, and the postfix is the number ending. Prefix must
   * be alphanumerical and the same across both serial numbers. The postfix must either be the same length or the same leading zeroes.
   */
  public String generateSerialRange(String startSerial, String endSerial, List<String> serialList) {
    String errStr = "";
    if ((startSerial != null) && (!startSerial.isEmpty()) && (endSerial != null) && (!endSerial.isEmpty())) {
      try {
        String begpart1;
        String begpart2;
        String numberpart1;
        String numberpart2;
        String padzeros = "";
        int digits1;
        int digits2;
        long numbers1;
        long numbers2;
        long smaller;
        long larger;
        boolean fixedsize = false;
        boolean goodrange = false;
        startSerial = startSerial.toUpperCase();
        endSerial = endSerial.toUpperCase();

        if (!RegUtils.isAlphaNumericDashes(startSerial)) return "Serial Numbers can contain alphanumeric, underscore, and/or hyphen only.";

        Pattern regex = Pattern.compile("\\d+$");
        Matcher matcher1 = regex.matcher(startSerial);
        Matcher matcher2 = regex.matcher(endSerial);

        //Regular expression: assumes there's a number at the end of the serial number
        //If it can't find a number at the end of the serial number, it quits
        if (matcher1.find() && matcher2.find()) {
          begpart1 = startSerial.substring(0, matcher1.start());
          begpart2 = endSerial.substring(0, matcher2.start());
          numberpart1 = startSerial.substring(matcher1.start(), matcher1.end());
          numberpart2 = endSerial.substring(matcher2.start(), matcher2.end());
          digits1 = matcher1.end() - matcher1.start();
          digits2 = matcher2.end() - matcher2.start();
          //Only proceed if first parts of the serial number are equal
          if (begpart1.equals(begpart2)) {
            if (digits1 == digits2) { //same size numerals
              fixedsize = true;
              goodrange = true;
            }
            else {
              regex = Pattern.compile("[1-9]");
              matcher1 = regex.matcher(numberpart1);
              matcher2 = regex.matcher(numberpart2);
              if (matcher1.find() && matcher2.find()) {
                if (matcher1.start() == matcher2.start()) { //same number of leading zeroes
                  digits1 = matcher1.start();
                  fixedsize = false;
                  goodrange = true;
                  char[] zeros = new char[digits1];
                  Arrays.fill(zeros, '0');
                  padzeros = String.valueOf(zeros);
                }
                //Different number of leading zeroes
                //return error
                else errStr = "Numbers aren't in the right format. Second numeral part not the same size.";
              }
              //One number is all zeroes. Not the same size, so leading zeroes doesn't matter either. Ignoring.
              //return error
              else errStr = "Numbers aren't in the right format. Second numeral part not the same size.";
            }
            //Numerals are either the same size or have the same number of leading zeroes, so create the numbers
            if (goodrange) {
              numbers1 = Long.parseLong(numberpart1);
              numbers2 = Long.parseLong(numberpart2);
              if (numbers1 > numbers2) {
                larger = numbers1;
                smaller = numbers2;
              }
              else {
                larger = numbers2;
                smaller = numbers1;
              }
              for (long i = smaller; i <= larger; i++) {
                if (fixedsize) {
                  numberpart1 = String.format("%0" + digits1 + "d", i);
                  serialList.add(begpart1 + numberpart1);
                }
                else {
                  serialList.add(begpart1 + padzeros + i);
                }
              }
            }
          }
          //Different prefixes
          else errStr = "Numbers in the wrong format. Prefix is not identical in both numbers.";
        }
        else errStr = "Serial numbers must end in a number to be used in a range.";
      }
      catch (Exception e) {
        AdfLogUtility.logException(e);
        errStr = "An error has occured. See error logs.";
      }
    }
    else errStr = "Serial Number is empty.";

    return errStr;
  }

  enum SimType {
    CROSS,
    TRANS,
    TRANSSHIP
  }

  /**
   * This function inserts all of the necessary rows in the database to take an issue directly from
   * a receipt state to a state in shipping, tricking the system to think it underwent necessary picks
   * and packs to do so.  This is done for CROSS-DOCKS and TRANS-SHIPMENTS.
   */
  private int simulatePackedItem(String issueSCN, String consolBarcode, int iQty, int userId, int wsId, SimType simType) {
    int retVal = 0;

    log.debug("{} {} {} {} {} {}", issueSCN, consolBarcode, iQty, userId, wsId, simType);

    PickingViewImpl pickView = null;
    ConsolidationViewImpl consolView = null;
    Row pickRow = null;
    Row consolRow = null;
    try {

      // get the packing station id
      int packingId = 0;
      try (PreparedStatement psLoc = getDBTransaction().createPreparedStatement("select unique packing_station_id, e2.name from packing_station ps, equip e, equip e2 where " + "  e.equipment_number = ? and " + "  e.warehouse_id = e2.warehouse_id and " + "  e2.equipment_number = ps.equipment_number and " + "  e2.description = 'Packing Station - Single' ", 0)) {
        psLoc.setInt(1, wsId);
        try (ResultSet rsLoc = psLoc.executeQuery()) {
          //Get the warehouseId from the resultset
          if (rsLoc.next()) packingId = rsLoc.getInt(1);
        }
      }
      //Error -2 = No rows returned from PACKING_STATION table
      if (packingId < 1) return -2;

      // get the number of picks for an issue
      int scnCount;
      try (PreparedStatement psSCNCount = getDBTransaction().createPreparedStatement("select count(*) from picking where scn = ?", 0)) {
        psSCNCount.setString(1, issueSCN);
        try (ResultSet rsSCNCount = psSCNCount.executeQuery()) {
          scnCount = 0;
          if (rsSCNCount.next()) scnCount = rsSCNCount.getInt(1);
        }
      }

      // refresh the row, just in case we have to remove it, to avoid RowInconsistentException
      // begin insert row into packing_consolidation table
      consolView = getConsolidationView1();
      consolRow = consolView.createRow();
      consolView.insertRow(consolRow);

      if (SimType.TRANS == simType) {
        try (val psIssue = getDBTransaction().createPreparedStatement("select customer_id from issue where scn = ?", 0)) {
          psIssue.setString(1, issueSCN);
          try (ResultSet rsIssue = psIssue.executeQuery()) {
            if (rsIssue.next()) {
              //There is no NIIN INFORMATION supplied with this trans-shipment, so there is no cube/weight information supplied.
              //So we only are entering values of 1 in the consolidation history.
              consolRow.setAttribute("ConsolidationCube", Double.parseDouble("1"));
              consolRow.setAttribute("ConsolidationWeight", Double.parseDouble("1"));
              consolRow.setAttribute("CustomerId", rsIssue.getObject(1));
            }
            else {
              //Error -3 = No rows returned from ISSUE/NIIN_INFO SQL Result Set
              // undo consolRow insertRow using a refresh, since it has not yet been committed
              consolRow.refresh(Row.REFRESH_REMOVE_NEW_ROWS);

              //Rollback inserted row from pickview using remove since it HAS been committed
              retVal = -3;
            }
          }
        }
      }
      else {
        //Now we have to insert some information about the issue into the PACKING_CONSOLIDATION table
        try (val psIssue = getDBTransaction().createPreparedStatement("select i.customer_id, n.cube, n.weight " + "from issue i, niin_info n where i.niin_id=n.niin_id and i.scn = ?", 0)) {
          psIssue.setString(1, issueSCN);
          try (val rsIssue = psIssue.executeQuery()) {
            if (rsIssue.next()) {
              consolRow.setAttribute("ConsolidationCube", rsIssue.getDouble(2) * iQty);
              consolRow.setAttribute("ConsolidationWeight", rsIssue.getDouble(3) * iQty);
              consolRow.setAttribute("CustomerId", rsIssue.getObject(1));
            }
            else {
              //Error -3 = No rows returned from ISSUE/NIIN_INFO SQL Result Set
              // undo consolRow insertRow using a refresh, since it has not yet been committed
              consolRow.refresh(Row.REFRESH_REMOVE_NEW_ROWS);

              //Rollback inserted row from pickview using remove since it HAS been committed
              retVal = -3;
            }
          }
        }
      }

      // errors found, return
      if (retVal < 0) return retVal;

      //Insert the rest of the PACKING_CONSOLIDATION information
      consolRow.setAttribute("PackingStationId", packingId);
      consolRow.setAttribute("ConsolidationBarcode", consolBarcode);
      //By default the carton is closed
      consolRow.setAttribute("CloseCarton", "Y");
      consolRow.setAttribute("CreatedBy", userId);
      consolRow.setAttribute("ModifiedBy", userId);

      // commit it to the database
      consolView.getDBTransaction().commit();

      //* refresh the row to get the Packing_consolidation_id generated by database insert
      // Retrieve the row back from the database so we can get the primary key value back into our view row.
      consolRow.refresh(Row.REFRESH_WITH_DB_FORGET_CHANGES);

      // Now we need to update the PICKING table to reflect the consolidation_id and that the item is now packed
      int conId = Integer.parseInt(consolRow.getAttribute("PackingConsolidationId").toString());

      // to avoid InConsistentException, refresh pickRow prior to setting attributes

      pickView = getPickingView1();
      //First we need to create a row in the PICKING table for our non-picked issue
      pickRow = pickView.createRow();
      pickRow.setAttribute("Scn", issueSCN);

      if (scnCount == 0) pickRow.setAttribute("Pin", issueSCN);
      else pickRow.setAttribute("Pin", issueSCN + scnCount);

      pickRow.setAttribute("PickQty", iQty);
      pickRow.setAttribute("QtyPicked", iQty);
      pickRow.setAttribute("CreatedBy", userId);
      pickRow.setAttribute("ModifiedBy", userId);

      //FUTURE Review status also set below, does this generate an OR in the query?
      if (SimType.CROSS == simType) {
        pickRow.setAttribute("Status", "CROSSDOCKED");
      }
      else if (SimType.TRANS == simType || SimType.TRANSSHIP == simType) {
        pickRow.setAttribute("Status", "TRANSSHIP");
      }

      pickRow.setAttribute("PackingConsolidationId", conId);

      //FUTURE Review status also set above, does this generate an OR in the query?
      pickRow.setAttribute("Status", "PACKED");

      pickRow.setAttribute("ModifiedBy", userId);
      //Insert our new record into the database and commit using pickView
      pickView.insertRow(pickRow);
      pickView.getDBTransaction().commit();

      // refresh the row, just in case we have to remove it, to avoid RowInconsistentException
      pickRow.refresh(Row.REFRESH_WITH_DB_FORGET_CHANGES);

      // Now we need to update the ISSUE table to reflect the consolidation_id
      try (val stR = getDBTransaction().createPreparedStatement("update issue set packing_consolidation_id = ?, qty_issued = ? where scn = ?", 0)) {
        stR.setInt(1, conId);
        stR.setInt(2, iQty);
        stR.setString(3, issueSCN);
        stR.executeUpdate();
        getDBTransaction().commit();
      }

      //Insert rows in the SHIPPING table
      retVal = sendToShipping(conId, userId);
      // if sendToShipping failed, undo all inserts to picking and packing_consolidation and updates to issue
      if (retVal < 0) {
        log.debug("send to shipping failed");
        //Everything is inserted into the database to simulate a completed pack except insert into SHIPPING Failed
        try (val stR = getDBTransaction().createPreparedStatement("update issue set packing_consolidation_id = null where scn = ?", 0)) {
          stR.setString(1, issueSCN);
          stR.executeUpdate();
          getDBTransaction().commit();
        }
        pickRow.remove();
        consolRow.remove();
        getDBTransaction().commit();
        return -4;
      }

      retVal = 0;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      retVal = -999;

      try {
        if (pickView != null && pickRow != null) {
          pickRow.remove();
          pickView.getDBTransaction().commit();
        }

        if (consolView != null && consolRow != null) {
          consolRow.remove();
          consolView.getDBTransaction().commit();
        }
      }
      catch (Exception e1) {
        log.error("Error closing and committing DB connection");
      }
    }
    return retVal;
  }

  /**
   * This function is used to create a consolidation barcode based on the database
   * sequence variable CONSOL_SEQ and pass it into a string.   Auto-generated
   * Consolidation Barcodes are usually only created for Consolidated Packing
   * Stations (CPCX) since Single Pack Stations use Pre-Printed Barcodes
   */
  public String createConsolidationBarcode() {
    val cBC = new StringBuilder();
    try (val st = getDBTransaction().createPreparedStatement("select CONSOL_SEQ.nextval from dual", 0)) {
      try (val rs = st.executeQuery()) {
        rs.next();
        int r = rs.getInt(1);
        for (int i = 1; i <= 10; i++) {
          val sqM = r % 36;
          if (sqM <= 9) {
            cBC.insert(0, (char) (sqM + 48));
          }
          else {
            cBC.insert(0, (char) (sqM + 55));
          }
          r = Math.abs(r / 36);
        }
      }
      return (cBC.toString());
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      return null;
    }
  }

  /**
   * This function is used to create a partial backorder record in the ISSUE table
   * when part of a backorder has already been crossdocked and there is a quantity still remaining
   * to be filled for that backorder.
   */
  public int insertPartialBackorder(String scn, int issueQty) {
    int retVal = -1;

    String nextscn = this.returnNextSCN(false);
    try (val stIssue = getDBTransaction().createPreparedStatement("select document_id, document_number, qty, issue_type, issue_priority_designator, issue_priority_group, customer_id, niin_id, rdd, fund_code, date_back_ordered, routing_id_from, supplementary_address, signal_code from issue where scn = ?", 0)) {
      stIssue.setString(1, scn);
      try (val rsIssue = stIssue.executeQuery()) {
        if (rsIssue.next()) {
          int qty = rsIssue.getInt(3) - issueQty;

          int iCount;
          try (PreparedStatement stC = getDBTransaction().createPreparedStatement("select count(*) from issue where document_number = ? and status <> 'BACKORDER'", 0)) {
            stC.setString(1, rsIssue.getString(2));
            try (ResultSet rsC = stC.executeQuery()) {
              iCount = 0;
              if (rsC.next()) iCount = rsC.getInt(1);
            }
          }

          try (val stR = getDBTransaction().createPreparedStatement("insert into issue (document_id, document_number, qty, issue_type, issue_priority_designator, issue_priority_group, customer_id, niin_id, status, created_by, rdd, fund_code, date_back_ordered, scn, routing_id_from,  supplementary_address, signal_code, suffix) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", 0)) {
            //original document number has no suffix, but additional inserts need to have suffixs starting with A -> Z

            stR.setObject(1, rsIssue.getObject(1));
            stR.setObject(2, rsIssue.getObject(2));
            stR.setInt(3, qty);
            stR.setString(4, "B");
            stR.setObject(5, rsIssue.getObject(5));
            stR.setObject(6, rsIssue.getObject(6));
            stR.setObject(7, rsIssue.getObject(7));
            stR.setObject(8, rsIssue.getObject(8));
            stR.setString(9, "BACKORDER");
            stR.setInt(10, 1);
            stR.setObject(11, rsIssue.getObject(9));
            stR.setObject(12, rsIssue.getObject(10));
            stR.setObject(13, rsIssue.getObject(11));
            stR.setString(14, nextscn);
            stR.setObject(15, rsIssue.getObject(12));
            stR.setString(16, rsIssue.getString(13));
            stR.setString(17, rsIssue.getString(14) == null ? "A" : rsIssue.getString(14));

            String suffix = "";
            if (iCount > 0) suffix = suffix + (char) (iCount + 64);

            stR.setString(18, suffix);
            stR.execute();

            this.getDBTransaction().commit();
            retVal = 0;
          }
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return retVal;
  }

  /**
   * This function initiates the Trans-Shipment process in the workload manager
   * module by calling all of the necessary functions and inserting all of
   * the necessary rows in the database to Trans-Ship the item that has arrived.
   */
  public String initiateTransShipment(int rcn, String consolBarcode, int userId, int wsId) {
    int retVal;
    String errorVal;
    NewIssueViewImpl issueView = null;
    Row issueRow = null;
    try {
      int iQty = 1;
      SimType simType = SimType.TRANS;
      String docNum = "";
      String docSuffix = null;
      String consignee = "";
      String rid = null;
      String projectCode = null;
      String signalCode = "A";
      String cc = "A";

      // get generated SCN to use in new and updated rows
      String issueSCN = this.returnNextSCN(false);

      // create an issue row
      issueView = getNewIssueView1();
      issueRow = issueView.createRow();
      issueView.insertRow(issueRow);

      try (val stR = getDBTransaction().createPreparedStatement("select quantity_invoiced, niin_id, document_id, document_number, consignee, suffix, rpd, routing_id, project_code, signal_code, cc from receipt where rcn = ?", 0)) {
        stR.setInt(1, rcn);

        try (val rs = stR.executeQuery()) {
          if (rs.next()) {
            iQty = rs.getInt(1);
            if (rs.getObject(2) != null) {
              issueRow.setAttribute("NiinId", rs.getInt(2));
              simType = SimType.TRANSSHIP;
              issueRow.setAttribute("IssuePriorityGroup", "1");
              issueRow.setAttribute("IssuePriorityDesignator", "3");
            }
            else {
              //* added to pass AS1 when issue.niin_id is empty, required issue_priority_designator='T'
              issueRow.setAttribute("IssuePriorityGroup", "1");
              issueRow.setAttribute("IssuePriorityDesignator", "3");
            }
            issueRow.setAttribute("DocumentId", rs.getString(3));
            docNum = rs.getString(4);
            consignee = rs.getString(5);
            if (rs.getObject(6) != null && rs.getString(6).length() > 0) docSuffix = rs.getString(6);
            if (rs.getObject(8) != null && rs.getString(8).length() > 0) rid = rs.getString(8);
            if (rs.getObject(9) != null && rs.getString(9).length() > 0) projectCode = rs.getString(9);
            if (rs.getObject(10) != null && rs.getString(10).length() > 0) signalCode = rs.getString(10);
            if (rs.getObject(11) != null && rs.getString(11).length() > 0) cc = rs.getString(11);
            //We append the word ship to the simulation type variable to let the simulate pack function know to look for weight information
          }
        }
      }

      issueRow.setAttribute("Scn", issueSCN);
      issueRow.setAttribute("DocumentNumber", docNum);

      if (docSuffix != null) issueRow.setAttribute("Suffix", docSuffix);

      issueRow.setAttribute("CreatedBy", userId);
      issueRow.setAttribute("ModifiedBy", userId);
      issueRow.setAttribute("Status", "TRANSSHIP");
      issueRow.setAttribute("IssueType", "T");
      issueRow.setAttribute("Cc", cc); //* 12/26/08 - remove hardcoded condition code
      issueRow.setAttribute("RoutingIdFrom", rid);
      issueRow.setAttribute("ProjectCode", projectCode);
      issueRow.setAttribute("SignalCode", signalCode == null ? "A" : signalCode);

      try (PreparedStatement stCust = getDBTransaction().createPreparedStatement("select customer_id from customer where aac = ?", 0)) {

        //If there is no value in the consignee field, make sure to use the first 6 characters of the document number.
        if (consignee != null) stCust.setString(1, consignee);
        else stCust.setString(1, docNum.substring(0, 6));

        try (ResultSet rsCust = stCust.executeQuery()) {
          if (rsCust.next()) issueRow.setAttribute("CustomerId", rsCust.getInt(1));
        }
      }

      issueRow.setAttribute("Qty", iQty);
      issueView.getDBTransaction().commit();
      // refresh the row, just in case we have to remove it, to avoid RowInconsistentException
      issueRow.refresh(Row.REFRESH_WITH_DB_FORGET_CHANGES);

      try (val stRI = getDBTransaction().createPreparedStatement("INSERT INTO RECEIPT_ISSUE(rcn, scn, qty) values(?, ?, ?)", 0)) {
        stRI.setInt(1, rcn);
        stRI.setString(2, issueSCN);
        stRI.setInt(3, iQty);
        stRI.execute();
        this.getDBTransaction().commit();
      }

      retVal = this.simulatePackedItem(issueSCN, consolBarcode, iQty, userId, wsId, simType);
      if (retVal < 0) {
        log.debug("simulate packed item failed  {}", retVal);
        // simulate pack item failed, remove the issue row just inserted
        try (val stRI = getDBTransaction().createPreparedStatement("delete from receipt_issue where rcn = ? and scn = ?", 0)) {
          stRI.setInt(1, rcn);
          stRI.setString(2, issueSCN);
          stRI.execute();
          this.getDBTransaction().commit();
        }

        issueRow.remove();
        issueView.getDBTransaction().commit();
        errorVal = "Error " + retVal;
      }
      else errorVal = "SCN " + issueSCN;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      errorVal = "Error -1";
      this.getDBTransaction().rollback();
      if (issueView != null && issueRow != null) {
        issueRow.remove();
        issueView.getDBTransaction().commit();
      }
    }
    return errorVal;
  }

  /**
   * This function initiates the cross dock process in the workload manager
   * module by calling all of the necessary functions and inserting all of
   * the necessary rows in the database to cross-dock the backordered issue.
   */
  public void initiateCrossDock(String crossSCN, String consolBarcode, int crossQty, int userId, int wsId) {
    try {
      simulatePackedItem(crossSCN, consolBarcode, crossQty, userId, wsId, SimType.CROSS);
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  public Object simpleNIINlookup(String niin, String fsc) {
    Object niinid = null;
    try (val psNiin = getDBTransaction().createPreparedStatement("select niin_id from niin_info where niin = ? and fsc = ?", 0)) {
      psNiin.setString(1, niin);
      psNiin.setString(2, fsc);
      try (val rsNiin = psNiin.executeQuery()) {
        if (rsNiin.next()) niinid = rsNiin.getInt(1);
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return niinid;
  }

  public Object getCustomerId(String aac) {
    Object customerid = null;
    // get the customer view
    try (val psCust = getDBTransaction().createPreparedStatement("select customer_id from customer where aac = ?", 0)) {
      psCust.setString(1, aac);
      try (val rsCust = psCust.executeQuery()) {

        // get the cust number
        if (rsCust.next()) customerid = rsCust.getObject(1);
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return customerid;
  }

  public void fillErrorList() {
    //Build the list of errors from ERROR table
    try {
      ErrorLVOImpl ve = getErrorLVO1();
      ve.setRangeSize(-1);
      ve.executeQuery();
      ve.getAllRowsInRange();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  public void createErrorQueueRecord(String eIdStr, String keyTyp, String keyStr, String uid, String notes) {
    try { // Checks if the row is already there in error_queue table.
      // Other wise creates a new row.
      int eId = Integer.parseInt(eIdStr);
      int userid = Integer.parseInt(uid);

      if (!this.findErrorQueueRecord(eIdStr, keyTyp, keyStr)) {
        val von = getErrorQueueVO1();
        val nnRow = von.createRow();
        von.insertRow(nnRow);

        nnRow.setAttribute("Status", "Open");
        nnRow.setAttribute("CreatedBy", userid);
        nnRow.setAttribute("CreatedDate", (new Date(new Timestamp(System.currentTimeMillis()))));
        nnRow.setAttribute("Eid", eId);
        nnRow.setAttribute("KeyType", keyTyp);
        nnRow.setAttribute("KeyNum", keyStr);
        nnRow.setAttribute("Notes", notes); // up to 200 chars
        von.getDBTransaction().commit();
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  /**
   * Delete a record form error queue
   */
  public void deleteErrorQueueRecord(String eIdStr, String keyTyp, String keyStr, int userID) {
    try {
      if (this.findErrorQueueRecord(eIdStr, keyTyp, keyStr)) {
        try (val stR = getDBTransaction().createPreparedStatement("begin update error_queue set modified_by = ?, modified_date = sysdate where eid = ? and key_type = ? and key_num = ?; end;", 0)) {
          stR.setInt(1, userID);
          stR.setLong(2, Long.parseLong(eIdStr));
          stR.setString(3, keyTyp);
          stR.setString(4, keyStr);
          stR.executeUpdate();
          getDBTransaction().commit();
        }

        try (val stR = getDBTransaction().createPreparedStatement("begin delete from error_queue where eid = ? and key_type = ? and key_num = ?; end;", 0)) {
          stR.setLong(1, Long.parseLong(eIdStr));
          stR.setString(2, keyTyp);
          stR.setString(3, keyStr);
          stR.executeUpdate();
          getDBTransaction().commit();
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  public boolean findErrorQueueRecord(String eIdStr, String keyTyp, String keyStr) {
    // Identifies the record in the error_queue table based on input parameters.
    // Returns false if it is not there.
    boolean qId = false;
    try (val stR = getDBTransaction().createPreparedStatement("select error_queue_id from error_queue where eid = ? and key_type = ? and key_num = ?", 0)) {
      stR.setLong(1, Long.parseLong(eIdStr));
      stR.setString(2, keyTyp);
      stR.setString(3, keyStr);
      try (val rs = stR.executeQuery()) {
        if (rs.next()) qId = true;
      }
      return qId;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return false;
  }

  /**
   * Identifies the error message row that needs to be used based on the error_code.
   */
  public Row getErrorRow(String eStr) {
    // Get the error message row for the error code
    try {
      ErrorLVOImpl ve = this.getErrorLVO1();
      ve.setRangeSize(-1);
      Row[] er = ve.getAllRowsInRange();
      int i = 0;
      while (i < er.length) {
        if (er[i].getAttribute("ErrorCode").toString().compareTo(eStr) == 0)
          return er[i];
        i++;
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return null;
  }

  public void clearLocationList() {
    try {
      val vo = getLocationList1();
      vo.clearCache();
      vo.executeQuery();

      val vollist = getNiinLocListVVOF1();
      vollist.clearCache();
      vollist.executeQuery();

      val vol = getLocationVO1();
      vol.clearCache();
      vol.executeQuery();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  public String isControlled(long niinId) {
    // Get the niin info details for receipt
    String isSecure = "N";
    try (val stR = getDBTransaction().createPreparedStatement("select ss.code_value from site_security ss inner join niin_info ni on ni.scc = ss.code_value where ss.code_name = 'SCC' and ni.niin_id = ?", 0)) {
      stR.setLong(1, niinId);
      try (val rs = stR.executeQuery()) {
        if (rs.next()) isSecure = "Y";
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return isSecure;
  }

  public List<LocationSelectionOption> locationListToSelectionOption() {
    List<LocationSelectionOption> locationSelectionOptionList = new ArrayList<>();
    val vo = getLocationList1();
    val iterator = vo.createRowSetIterator(null);

    while (iterator.hasNext()) {
      val option = new LocationSelectionOption();
      val item = iterator.next();
      val locationId = (oracle.jbo.domain.Number) item.getAttribute("LocationId");
      val locationQty = (oracle.jbo.domain.Number) item.getAttribute("LocQty");

      option.setLocationId(locationId.intValue());
      option.setLocationLabel((String) item.getAttribute("LocationLabel"));
      option.setLocationQuantity(locationQty.intValue());

      locationSelectionOptionList.add(option);
    }

    return locationSelectionOptionList;
  }

  /**
   * This is the main routine to initiate the building of best location list for stowing.
   * Builds a list of best three stow locations.
   */
  public String buildLocationList(String locLabel, String keyStr, String keyId, String whId, int qtySt) {
    try {
      log.debug("--------------------------  Build Location list for ::location {}::keyStr {} ::keyId(stowId) {} ::warehouseId {} ::qty  {}", locLabel, keyStr, keyId, whId, qtySt);
      StringBuilder locationIdList = new StringBuilder();
      String errStr = "";
      this.clearLocationList();
      LocationListImpl vo = getLocationList1();
      vo.setRangeSize(-1);

      //Get ExpirationDate, rcn from stow
      String rcnStr = "0";
      String expDateStr = "999901";
      String qtyToBeStowed = "0";

      try (val stR = getDBTransaction().createPreparedStatement(
          "select rcn, to_char(expiration_date,'YYYYMM'), qty_to_be_stowed from stow where stow_id = ?", 0)) {
        stR.setLong(1, Long.parseLong(keyId));

        try (val rs = stR.executeQuery()) {
          if (rs.next()) {
            rcnStr = rs.getString(1);
            expDateStr = rs.getString(2);
            qtyToBeStowed = rs.getString(3);

            log.debug("found rcnStr = {} ::expDateStr = {} ::qtyToBeStowed = {}", rcnStr, expDateStr, qtyToBeStowed);
          }
        }
      }
      catch (Exception e) {
        AdfLogUtility.logException(e);
      }

      if (qtySt != -1)
        qtyToBeStowed = String.valueOf(qtySt); //Use the sent value instead of what's on stow as the qty is not upto date
      //This is a case from receipt where expiration date was not yet comited to database
      if ((keyStr != null) && ((!keyStr.contains("STOW")) && (keyStr.length() >= 7))) {
        expDateStr = keyStr.substring(0, 4) + keyStr.substring(5, 7);
        log.debug("keyStr = {} ::expDateStr=  {}", keyStr, expDateStr);
      }

      //Get weight, cube, niinId, MechNonMechFlag from Receipt
      String niinIdStr = "0";
      String onlyBulkStr = "N";
      String rWeight = "0";
      String rLength = "0";
      String rHeight = "0";
      String rWidth = "0";
      String rMeasure = "0";
      String cc = "A";

      //Get weight, cube, scc from niin_info
      String nWeightStr = "0";
      String nCubeStr = "0";
      String scc = null;

      //variables for on-hand locations temporary. If there's more than 3 on-hand locations, should display 2 mech and 1 non-mech.
      boolean onhandfilled = false;
      String tmpLocID = "";
      String tmpLocLbl = "";
      String tmpqty = "";

      val stRSql = "select r.niin_id, nvl(mech_non_mech_flag,'N'), nvl(r.weight,1), " +
          "nvl(r.length,1), nvl(r.height,1), nvl(r.width,1), " +
          "nvl(quantity_measured,1), nvl(cc, 'A'), " +
          "n.weight, n.cube, n.scc, n.shelf_life_code, " +
          "nvl(n.serial_control_flag,'N'), nvl(n.lot_control_flag,'N')  " +
          "from receipt r, niin_info n where r.niin_id=n.niin_id and r.rcn = ?";

      try (val stR = getDBTransaction().createPreparedStatement(stRSql, 0)) {
        stR.setLong(1, Long.parseLong(rcnStr));

        try (val rs = stR.executeQuery()) {
          if (rs.next()) {
            niinIdStr = rs.getString(1);
            onlyBulkStr = rs.getString(2);
            rWeight = rs.getString(3);
            rLength = rs.getString(4);
            rHeight = rs.getString(5);
            rWidth = rs.getString(6);
            rMeasure = rs.getString(7);
            cc = rs.getString(8);

            nWeightStr = rs.getString(9);
            nCubeStr = rs.getString(10);
            scc = rs.getString(11);
            log.debug("The niin weighs {} and has a cube of {} ::scc= {}", nWeightStr, nCubeStr, scc);
          }
        }
      }
      catch (Exception e) {
        AdfLogUtility.logException(e);
      }

      //Check if it is secure
      if (scc != null) {
        try (val stR = getDBTransaction().createPreparedStatement(
            "select code_value from site_security where code_name = 'SCC' and code_value = ?", 0)) {
          stR.setString(1, scc);
          try (val rs = stR.executeQuery()) {
            //not a secure Item
            if (!rs.next()) scc = "N";
            else scc = "Y";
          }
        }
        catch (Exception e) {
          AdfLogUtility.logException(e);
        }
      }
      else scc = "N";

      //05-21-2014: Add new Secure Check for better error messages.
      //check for WAC or Location
      //10-14-2015: New Requirement. Sites would like to be able to stow non-secure items into secure locations if requested.
      //Will disable check for secure flag if it's a non-secure item in a secure WAC.
      boolean secureCheck = true;
      if (locLabel != null && !locLabel.isEmpty()) {
        try {
          boolean hasSameSecureFlag = false;
          String sql = "select distinct(l.wac_id), w.secure_flag from location l inner join wac w on l.wac_id = w.wac_id where location_label like ?";
          try (PreparedStatement pstmt = this.getDBTransaction().createPreparedStatement(sql, 0)) {
            pstmt.setString(1, locLabel.toUpperCase() + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
              while (rs.next()) {
                if (scc.equals(rs.getString(2))) hasSameSecureFlag = true;
              }
            }
          }
          //Secure mismatch
          if (!hasSameSecureFlag) {
            if (scc.equals("Y")) return "Can not store Secure Item on a Non-Secure location";
            else secureCheck = false;
          }
        }
        catch (Exception e) {
          AdfLogUtility.logException(e);
        }
      }

      //10-14-2015: New Requirement. Sites would like to be able to stow non-secure items into secure locations if requested.
      //Will disable check for secure flag if it's a non-secure item in a secure WAC.
      //we know that the scc = N and the wac secure_flag = Y, so we temporarily change the flag to match the wac
      if (!secureCheck) scc = "Y";

      double nWt = 0;
      double nCu = 0;

      double qMs = Double.parseDouble(rMeasure); // Get the quantity measured into qMs
      log.debug("quantity measured = receipt's quantity measured =  {}", qMs);

      // Get weight and cube from receipt, if not there then from niin_info
      if (qMs > 0) {
        nWt = (Double.parseDouble(rWeight) * Double.parseDouble(qtyToBeStowed)) / qMs;
        nCu = ((Double.parseDouble(rLength) * Double.parseDouble(rHeight) * Double.parseDouble(rWidth)) * Double.parseDouble(qtyToBeStowed)) / qMs;
      }
      if (nWt <= 0) {
        nWt = Double.parseDouble(nWeightStr) * Double.parseDouble(qtyToBeStowed);
      }

      if (nCu <= 0) {
        nCu = Double.parseDouble(nCubeStr) * Double.parseDouble(qtyToBeStowed);
      }

      int locationCount = 0;
      int maxLocationsToShow = 3;
      //--------------------ONHAND STOW LOCATIONS ---------------------------------------------
      String convertedLocLabel = (locLabel != null ? locLabel.toUpperCase() : "") + '%';
      if (onlyBulkStr.equalsIgnoreCase("N")) {
        log.debug("ONE.A.  checkMechOnHand ");
        /*Checks to see if the NIIN is on-hand for Mech locations*/
        try {
          val sql = "SELECT l.location_id, l.location_label, nl.qty, sum(lhb.weight) over (partition by w.wac_id) wacweight   " +
              "FROM location l inner join niin_location nl on l.location_id = nl.location_id  " +
              "inner join wac w on l.wac_id = w.wac_id   " +
              "inner join location_header_bin lhb on l.location_header_bin_id  = lhb.location_header_bin_id   " +
              "inner join location_classification lc on lhb.loc_classification_id = lc.loc_classification_id  " +
              "inner join location_header lh on lhb.loc_header_id = lh.loc_header_id  " +
              "where nl.niin_id=?   " +
              "and nl.cc LIKE NVL(?,'A')   " +
              "and (to_char(nvl(nl.expiration_date,to_date('01019999','DDMMYYYY')), 'YYYYMM') like nvl(?,'%'))   " +
              "and (l.location_label like nvl(?,'%'))    " +
              "and w.secure_flag = ?   " +
              "and l.mechanized_flag <> 'N'   " +
              "and NVL(nl.locked,'N') = 'N'   " +
              "order by wacweight asc, lhb.weight asc";

          try (val pstmt = getDBTransaction().createPreparedStatement(sql, 0)) {
            pstmt.setString(1, niinIdStr);
            pstmt.setString(2, cc);
            pstmt.setString(3, expDateStr);
            pstmt.setString(4, convertedLocLabel);
            pstmt.setString(5, scc);

            try (val rs = pstmt.executeQuery()) {
              while (rs.next() && (locationCount < maxLocationsToShow) && !onhandfilled) {
                if (!locationIdList.toString().contains(rs.getString(1))) {
                  //Temporary holding in case a nonMech is not found to fill up last spot
                  if (locationCount == maxLocationsToShow - 1) {
                    onhandfilled = true;
                    tmpLocID = rs.getString(1);
                    tmpLocLbl = rs.getString(2);
                    tmpqty = rs.getString(3);
                    //locationCount will remain at maxLocations -1 so the logic of non-mech will work properly
                  }
                  else {
                    val row = vo.createRow();
                    row.setAttribute("LocationId", rs.getString(1));
                    row.setAttribute("LocationLabel", rs.getString(2));
                    row.setAttribute("LocQty", rs.getString(3));
                    vo.insertRow(row);
                    locationIdList.append(',').append(rs.getString(1));
                    locationCount++;
                  }
                }
              }

              log.debug("Location Count: {}, Locations:  {}", locationCount, locationIdList);
            }
          }
        }
        catch (Exception e) {
          AdfLogUtility.logException(e);
        }
      }

      if (locationCount < maxLocationsToShow) {
        log.debug("ONE.B.  checkNonMechOnHand ");
        /*Checks to see if the NIIN is on-hand for Non-Mech locations*/
        try {
          val sql = "select l.location_id, l.location_label, nl.qty   " +
              "from niin_location nl inner join location l on nl.location_id = l.location_id   " +
              "inner join wac w on l.wac_id = w.wac_id   " +
              "where nvl(nl.locked,'N') = 'N'   " +
              "and (l.location_label like nvl(?,'%')) " +
              "and l.mechanized_flag = 'N'   " +
              "and nl.niin_id= ?   " +
              "and nl.cc like NVL(?,'A')   " +
              "and (to_char(nvl(nl.expiration_date,to_date('01019999','DDMMYYYY')), 'YYYYMM') like nvl(?,'%'))   " +
              "and w.secure_flag = ?   " +
              "order by nl.qty, l.location_label";

          try (val pstmt = this.getDBTransaction().createPreparedStatement(sql, 0)) {
            pstmt.setString(1, convertedLocLabel);
            pstmt.setString(2, niinIdStr);
            pstmt.setString(3, cc);
            pstmt.setString(4, expDateStr);
            pstmt.setString(5, scc);

            try (val rs = pstmt.executeQuery()) {
              while (rs.next() && locationCount < maxLocationsToShow) {
                if (!locationIdList.toString().contains(rs.getString(1))) {
                  val row = vo.createRow();
                  row.setAttribute("LocationId", rs.getString(1));
                  row.setAttribute("LocationLabel", rs.getString(2));
                  row.setAttribute("LocQty", rs.getString(3));
                  vo.insertRow(row);
                  locationIdList.append(',').append(rs.getString(1));
                  locationCount++;
                }
              }
            }
          }

          //No nonmech locations found, but a third mech was found.
          if ((locationCount + 1 == maxLocationsToShow) && onhandfilled) {
            val row = vo.createRow();
            row.setAttribute("LocationId", tmpLocID);
            row.setAttribute("LocationLabel", tmpLocLbl);
            row.setAttribute("LocQty", tmpqty);
            vo.insertRow(row);
            locationIdList.append(',').append(tmpLocID);
            locationCount++;
            //locationCount now up to max, should skip rest of checks.
          }
          log.debug("Location Count: {} Locations:  {}", locationCount, locationIdList.toString());
        }
        catch (Exception e) {
          AdfLogUtility.logException(e);
        }
      }
      //--------------------PENDING STOW LOCATIONS ---------------------------------------------

      if (onlyBulkStr.equalsIgnoreCase("N") && locationCount < maxLocationsToShow) {
        log.debug("TWO.A.  checkMechPendingStow");
        //Finds NIINs that are in the same WAC but have different CC or Expiration Dates
        try {
          String sql = "Select l.location_id, l.location_label, sum(decode(s.status,'STOW READY',qty_to_be_stowed,'STOW BYPASS1', qty_to_be_stowed, 'STOW BYPASS2',qty_to_be_stowed,0)) pending_stow_qty  " +
              "from location l inner join wac w on l.wac_id = w.wac_id   " +
              "inner join stow s on l.location_id = s.location_id   " +
              "inner join receipt r on r.rcn = s.rcn  " +
              "where r.niin_id = ?   " +
              "and (l.location_label like nvl(?,'%'))   " +
              "and w.secure_flag = ?   " +
              "and r.cc = ?   " +
              "and (to_char(nvl(s.expiration_date,to_date('01019999','DDMMYYYY')), 'YYYYMM') like nvl(?,'%'))   " +
              "and l.mechanized_flag <> 'N'   " +
              "and s.status in ('STOW READY', 'STOW BYPASS1', 'STOW BYPASS2')    " +
              "GROUP BY l.location_id, l.location_label";

          try (PreparedStatement pstmt = this.getDBTransaction().createPreparedStatement(sql, 0)) {

            pstmt.setString(1, niinIdStr);
            pstmt.setString(2, convertedLocLabel);
            pstmt.setString(3, scc);
            pstmt.setString(4, cc);
            pstmt.setString(5, expDateStr);

            try (ResultSet rs = pstmt.executeQuery()) {
              while (rs.next() && locationCount < maxLocationsToShow) {
                if (!locationIdList.toString().contains(rs.getString(1))) {
                  val row = vo.createRow();
                  row.setAttribute("LocationId", rs.getString(1));
                  row.setAttribute("LocationLabel", rs.getString(2));
                  row.setAttribute("LocQty", rs.getString(3));
                  vo.insertRow(row);
                  locationIdList.append(',').append(rs.getString(1));
                  locationCount++;
                }
              }
              log.debug("Location Count: {} Locations:  {}", locationCount, locationIdList.toString());
            }
          }
        }
        catch (Exception e) {
          AdfLogUtility.logException(e);
        }
      }

      final String upperCaseLocationLabel = locLabel != null ? locLabel.toUpperCase() + "%" : null;
      if (locationCount < maxLocationsToShow) {
        log.debug("TWO.B.  checkNonMechPendingStow ");
        //Finds NIINs that are in the same WAC but have different CC or Expiration Dates
        try {
          String sql = "Select l.location_id, l.location_label, sum(decode(s.status,'STOW READY',qty_to_be_stowed,'STOW BYPASS1', qty_to_be_stowed, 'STOW BYPASS2',qty_to_be_stowed,0)) pending_stow_qty  " +
              "from location l inner join wac w on l.wac_id = w.wac_id   " +
              "inner join stow s on l.location_id = s.location_id   " +
              "inner join receipt r on r.rcn = s.rcn  " +
              "where r.niin_id = ?   " +
              "and (l.location_label like nvl(?,'%'))   " +
              "and w.secure_flag = ?   " +
              "and r.cc = ?   " +
              "and (to_char(nvl(s.expiration_date,to_date('01019999','DDMMYYYY')), 'YYYYMM') like nvl(?,'%'))   " +
              "and l.mechanized_flag = 'N'   " +
              "and s.status in ('STOW READY', 'STOW BYPASS1', 'STOW BYPASS2')    " +
              "GROUP BY l.location_id, l.location_label";

          try (PreparedStatement pstmt = this.getDBTransaction().createPreparedStatement(sql, 0)) {
            pstmt.setString(1, niinIdStr);
            pstmt.setString(2, upperCaseLocationLabel);
            pstmt.setString(3, scc);
            pstmt.setString(4, cc);
            pstmt.setString(5, expDateStr);

            try (ResultSet rs = pstmt.executeQuery()) {
              while (rs.next() && locationCount < maxLocationsToShow) {

                if (!locationIdList.toString().contains(rs.getString(1))) {
                  val row = vo.createRow();
                  row.setAttribute("LocationId", rs.getString(1));
                  row.setAttribute("LocationLabel", rs.getString(2));
                  row.setAttribute("LocQty", rs.getString(3));
                  vo.insertRow(row);
                  locationIdList.append(',').append(rs.getString(1));
                  locationCount++;
                }
              }
              log.debug("Location Count: {} Locations:  {}", locationCount, locationIdList.toString());
            }
          }
        }
        catch (Exception e) {
          AdfLogUtility.logException(e);
        }
      }

      //--------------------Same WAC STOW LOCATIONS ---------------------------------------------
      if (onlyBulkStr.equalsIgnoreCase("N") && locationCount < maxLocationsToShow) {
        log.debug("THREE.A.  checkMechSameWAC");
        //Finds NIINs that are in the same WAC but have different CC or Expiration Dates
        try {
          String sql = "select l.location_id, l.location_label, 0   " +
              "from location l, wac w,   " +
              "location_header_bin lhb, location_header lh, location_classification lc  " +
              "where l.wac_id=w.wac_id   " +
              "and l.location_header_bin_id=lhb.location_header_bin_id(+)  " +
              "and lhb.loc_header_id=lh.loc_header_id(+)  " +
              "and lhb.loc_classification_id=lc.loc_classification_id(+)  " +
              "and l.location_id not in (select location_id from niin_location)  " +
              " AND l.location_id not in (select nvl(s.location_id,1) from receipt r,stow s where r.rcn=s.rcn and s.status <> 'STOWED' and r.niin_id <> ?)   " +
              "and w.wac_id in (  " +
              "select wac_id from niin_location nl, location lo  " +
              "where nl.location_id=lo.location_id  " +
              "and nl.niin_id=?  " +
              ")  " +
              "and (l.location_label like nvl(?,'%'))    " +
              "and l.mechanized_flag <> 'N'   " +
              "and l.weight   + ?    <= lc.usable_weight   " +
              "and l.cube     + ?      <= (lc.usable_cube/lc.slot_count)   " +
              "and lhb.weight + ?    <= lhb.max_weight   " +
              "and lh.weight  + ?   <= lh.max_weight    " +
              "AND w.secure_flag = ?   " +
              "and rownum < 10   " +
              "order by lhb.weight asc";

          try (PreparedStatement pstmt = this.getDBTransaction().createPreparedStatement(sql, 0)) {

            pstmt.setString(1, niinIdStr);
            pstmt.setString(2, niinIdStr);
            pstmt.setString(3, upperCaseLocationLabel);
            pstmt.setDouble(4, nWt);
            pstmt.setDouble(5, nCu);
            pstmt.setDouble(6, nWt);
            pstmt.setDouble(7, nWt);
            pstmt.setString(8, scc);

            try (ResultSet rs = pstmt.executeQuery()) {
              while (rs.next() && locationCount < maxLocationsToShow) {

                if (!locationIdList.toString().contains(rs.getString(1))) {
                  val row = vo.createRow();
                  row.setAttribute("LocationId", rs.getString(1));
                  row.setAttribute("LocationLabel", rs.getString(2));
                  row.setAttribute("LocQty", rs.getString(3));
                  vo.insertRow(row);
                  locationIdList.append(',').append(rs.getString(1));
                  locationCount++;
                }
              }
              log.debug("Location Count: {} Locations:  {}", locationCount, locationIdList.toString());
            }
          }
        }
        catch (Exception e) {
          AdfLogUtility.logException(e);
        }
      }

      if (locationCount < maxLocationsToShow) {
        log.debug("THREE.B.  checkNonMechSameWAC ");
        //Finds NIINs that are in the same WAC but have different CC or Expiration Dates
        try {
          String sql = "SELECT l.location_id, l.location_label, 0   " +
              "FROM location l, wac w   " +
              "where l.wac_id = w.wac_id   " +
              "and w.wac_id in (Select w.wac_id   " +
              "FROM location l inner join niin_location nl on l.location_id = nl.location_id  " +
              "inner join wac w on l.wac_id = w.wac_id   " +
              "WHERE nl.niin_id=?)   " +
              "      and (l.location_label like nvl(?,'%'))    " +
              "      and l.mechanized_flag = 'N'   " +
              "      AND w.secure_flag = ?   " +
              "      AND l.location_id not in (select location_id from niin_location)   " +
              " AND l.location_id not in (select nvl(s.location_id,1) from receipt r,stow s where r.rcn=s.rcn and s.status <> 'STOWED' and r.niin_id <> ?)   " +
              "      AND rownum < 10   " +
              " Order by location_label";

          try (PreparedStatement pstmt = this.getDBTransaction().createPreparedStatement(sql, 0)) {
            pstmt.setString(1, niinIdStr);
            pstmt.setString(2, upperCaseLocationLabel);
            pstmt.setString(3, scc);
            pstmt.setString(4, niinIdStr);

            try (ResultSet rs = pstmt.executeQuery()) {
              while (rs.next() && locationCount < maxLocationsToShow) {
                if (!locationIdList.toString().contains(rs.getString(1))) {
                  val row = vo.createRow();
                  row.setAttribute("LocationId", rs.getString(1));
                  row.setAttribute("LocationLabel", rs.getString(2));
                  row.setAttribute("LocQty", rs.getString(3));
                  vo.insertRow(row);
                  locationIdList.append(',').append(rs.getString(1));
                  locationCount++;
                }
              }
              log.debug("Location Count: {} Locations:  {}", locationCount, locationIdList.toString());
            }
          }
        }
        catch (Exception e) {
          AdfLogUtility.logException(e);
        }
      }

      //--------------------Last Known STOW LOCATIONS ---------------------------------------------
      if (onlyBulkStr.equalsIgnoreCase("N") && locationCount < maxLocationsToShow) {
        log.debug("FOUR.A.  checkMechLastKnownLocation");
        //Check last known location
        try {
          String sql = "select l.location_id, l.location_label, 0 from   " +
              "  niin_location_hist nh inner join   " +
              "  (select niin_Id , max(created_date) as maxdate from niin_location_hist   " +
              "    where niin_id = ?   " +
              "    group by niin_Id   " +
              "    ) nmh ON nmh.niin_id = nh.niin_id AND nmh.maxdate = nh.created_date   " +
              "    inner join location l on l.location_id = nh.location_id   " +
              "    left join stow s on l.location_id = s.location_id   " +
              "    left join niin_location nl on l.location_id = nl.location_id   " +
              "    inner join wac w on l.wac_id = w.wac_id   " +
              "    inner join location_header_bin lhb on l.location_header_bin_id  = lhb.location_header_bin_id   " +
              "    inner join location_classification lc on lhb.loc_classification_id = lc.loc_classification_id   " +
              "    inner join location_header lh on lhb.loc_header_id = lh.loc_header_id   " +
              "    WHERE (l.location_label like nvl(?,'%')) and   " +
              "          l.mechanized_flag <> 'N'                           " +
              "          AND ((l.weight   + ?) <= lc.usable_weight)   " +
              "          AND ((l.cube     + ?)   <= (lc.usable_cube/lc.SLOT_COUNT))   " +
              "          AND ((lhb.weight + ?) <= lhb.max_weight)   " +
              "          AND ((lh.weight  + ?) <= lh.max_weight)   " +
              "          AND (w.mechanized_flag <> 'N')   " +
              "          AND w.secure_flag = ?   " +
              "          AND s.stow_id IS NULL   " +
              "          AND nl.niin_id IS NULL";

          try (PreparedStatement pstmt = this.getDBTransaction().createPreparedStatement(sql, 0)) {
            pstmt.setString(1, niinIdStr);
            pstmt.setString(2, upperCaseLocationLabel);
            pstmt.setDouble(3, nWt);
            pstmt.setDouble(4, nCu);
            pstmt.setDouble(5, nWt);
            pstmt.setDouble(6, nWt);
            pstmt.setString(7, scc);

            try (ResultSet rs = pstmt.executeQuery()) {
              while (rs.next() && locationCount < maxLocationsToShow) {
                if (!locationIdList.toString().contains(rs.getString(1))) {
                  val row = vo.createRow();
                  row.setAttribute("LocationId", rs.getString(1));
                  row.setAttribute("LocationLabel", rs.getString(2));
                  row.setAttribute("LocQty", rs.getString(3));
                  vo.insertRow(row);
                  locationIdList.append(',').append(rs.getString(1));
                  locationCount++;
                }
              }
              log.debug("Location Count: {} Locations:  {}", locationCount, locationIdList.toString());
            }
          }
        }
        catch (Exception e) {
          AdfLogUtility.logException(e);
        }
      }

      if (locationCount < maxLocationsToShow) {
        log.debug("FOUR.B.  checkNonMechLastKnownLocation");
        //Check last known location
        try {
          String sql = "select l.location_id, l.location_label, 0 from    " +
              " niin_location_hist nh inner join    " +
              " (select niin_Id , max(created_date) as maxdate from niin_location_hist    " +
              "  where niin_id = ?    " +
              "  group by niin_Id    " +
              "  ) nmh ON nmh.niin_id = nh.niin_id AND nmh.maxdate = nh.created_date    " +
              "  inner join location l on l.location_id = nh.location_id    " +
              "  left join stow s on l.location_id = s.location_id    " +
              "  left join niin_location nl on l.location_id = nl.location_id    " +
              "  inner join wac w on l.wac_id = w.wac_id    " +
              "  WHERE (l.location_label like nvl(?,'%')) and    " +
              "  l.mechanized_flag = 'N'   " +
              "  AND w.secure_flag = ?    " +
              "  AND s.stow_id IS NULL    " +
              "  AND nl.niin_id IS NULL";

          try (PreparedStatement pstmt = this.getDBTransaction().createPreparedStatement(sql, 0)) {
            pstmt.setString(1, niinIdStr);
            pstmt.setString(2, upperCaseLocationLabel);
            pstmt.setString(3, scc);

            try (ResultSet rs = pstmt.executeQuery()) {
              while (rs.next() && locationCount < maxLocationsToShow) {
                if (!locationIdList.toString().contains(rs.getString(1))) {
                  val row = vo.createRow();
                  row.setAttribute("LocationId", rs.getString(1));
                  row.setAttribute("LocationLabel", rs.getString(2));
                  row.setAttribute("LocQty", rs.getString(3));
                  vo.insertRow(row);
                  locationIdList.append(',').append(rs.getString(1));
                  locationCount++;
                }
              }
              log.debug("Location Count: {} Locations:  {}", locationCount, locationIdList.toString());
            }
          }
        }
        catch (Exception e) {
          AdfLogUtility.logException(e);
        }
      }

      //--------------------AVAILABLE LOCATIONS---------------------------------------------
      if (onlyBulkStr.equalsIgnoreCase("N") && locationCount < maxLocationsToShow) {
        log.debug("FIVE.A.  checkMechAvailable");
        //Check last known location
        try {
          String sql = "SELECT l.location_id, l.location_label, 0, sum(lhb.weight) over (partition by w.wac_id) wacweight   " +
              "FROM location l   " +
              "inner join wac w on l.wac_id = w.wac_id   " +
              "inner join location_header_bin lhb on l.location_header_bin_id  = lhb.location_header_bin_id   " +
              "inner join location_classification lc on lhb.loc_classification_id = lc.loc_classification_id  " +
              "inner join location_header lh on lhb.loc_header_id = lh.loc_header_id  " +
              "WHERE (l.location_label like nvl(?,'%'))    " +
              "      AND l.location_id not in (select location_id from niin_location)   " +
              " AND l.location_id not in (select nvl(s.location_id,1) from receipt r,stow s where r.rcn=s.rcn and s.status <> 'STOWED' and r.niin_id <> ?)   " +
              "      and (l.mechanized_flag  <> 'N'   " +
              "      and (l.weight   + ?    <= lc.usable_weight   " +
              "      and l.cube     + ?      <= (lc.usable_cube/lc.slot_count)   " +
              "      and lhb.weight + ?    <= lhb.max_weight   " +
              "      and lh.weight  + ?   <= lh.max_weight))    " +
              "      AND w.secure_flag = ?   " +
              "      AND rownum < 10   " +
              " ORDER BY wacweight, lhb.weight asc";

          try (PreparedStatement pstmt = this.getDBTransaction().createPreparedStatement(sql, 0)) {
            pstmt.setString(1, upperCaseLocationLabel);
            pstmt.setString(2, niinIdStr);
            pstmt.setDouble(3, nWt);
            pstmt.setDouble(4, nCu);
            pstmt.setDouble(5, nWt);
            pstmt.setDouble(6, nWt);
            pstmt.setString(7, scc);

            try (ResultSet rs = pstmt.executeQuery()) {
              while (rs.next() && locationCount < maxLocationsToShow) {
                if (!locationIdList.toString().contains(rs.getString(1))) {
                  val row = vo.createRow();
                  row.setAttribute("LocationId", rs.getString(1));
                  row.setAttribute("LocationLabel", rs.getString(2));
                  row.setAttribute("LocQty", rs.getString(3));
                  vo.insertRow(row);
                  locationIdList.append(',').append(rs.getString(1));
                  locationCount++;
                }
              }
              log.debug("Location Count: {} Locations:  {}", locationCount, locationIdList.toString());
            }
          }
        }
        catch (Exception e) {
          AdfLogUtility.logException(e);
        }
      }

      if (locationCount < maxLocationsToShow) {
        log.debug("FIVE.B.  checkNonMechAvailable");
        //Check available non mech locations which are not a pending stow for the same niin
        try {
          String sql = "SELECT l.location_id, l.location_label, 0   " +
              "FROM location l, wac w " +
              "where l.wac_id = w.wac_id and (l.location_label like nvl(?,'%'))    " +
              "      and l.mechanized_flag = 'N'   " +
              "      AND w.secure_flag = ?   " +
              "      AND l.location_id not in (select location_id from niin_location)   " +
              " AND l.location_id not in (select nvl(s.location_id,1) from receipt r,stow s where r.rcn=s.rcn and s.status <> 'STOWED' and r.niin_id <> ?)   " +
              "      AND rownum < 10   " +
              " order by location_label";

          try (PreparedStatement pstmt = this.getDBTransaction().createPreparedStatement(sql, 0)) {
            pstmt.setString(1, upperCaseLocationLabel);
            pstmt.setString(2, scc);
            pstmt.setString(3, niinIdStr);

            try (ResultSet rs = pstmt.executeQuery()) {
              while (rs.next() && locationCount < maxLocationsToShow) {
                if (!locationIdList.toString().contains(rs.getString(1))) {
                  val row = vo.createRow();
                  row.setAttribute("LocationId", rs.getString(1));
                  row.setAttribute("LocationLabel", rs.getString(2));
                  row.setAttribute("LocQty", rs.getString(3));
                  vo.insertRow(row);
                  locationIdList.append(',').append(rs.getString(1));
                  locationCount++;
                }
              }
              log.debug("Location Count: {} Locations:  {}", locationCount, locationIdList.toString());
            }
          }
        }
        catch (Exception e) {
          AdfLogUtility.logException(e);
        }
      }

      //------------------------------------------------------------------------------------+++
      if (locationCount < 1) {

        if (scc.equals("Y")) errStr = "SECURE_ITEM-";
        if ((locLabel != null) && (locLabel.length() < 1)) {
          errStr = errStr + "WH_MSG Could not retrieve any locations. -Try filter by WAC or Location";
          return errStr;
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return "";
  }

  /**************************************************************************
   * This method is responsible for returning the last row of the locationlist view.
   * ***********************************************************************/
  public int getBestLocId(String location) {
    int returnLoc = 0;

    LocationListImpl view = this.getLocationList1();
    Row[] row = view.getAllRowsInRange();

    if (row.length > 0) {
      for (Row value : row) {
        if (!location.equalsIgnoreCase((value.getAttribute("LocationLabel")).toString())) {
          returnLoc = Integer.parseInt((value.getAttribute("LocationId")).toString());
          break;
        }
      }
    }

    return returnLoc;
  }

  /**************************************************************************
   * This function deletes the default STOW and RECEIPT columns after a failed rewarehouse,
   * or if no available locations can be found
   * ***********************************************************************/
  public void deleteLocation(int rcn, int stowId) {
    try {
      try (PreparedStatement deletion1 = this.getDBTransaction().createPreparedStatement("DELETE FROM STOW WHERE STOW_ID = ?", 0)) {
        deletion1.setInt(1, stowId);
        deletion1.execute();
        this.getDBTransaction().commit();
      }

      try (PreparedStatement deletion2 = this.getDBTransaction().createPreparedStatement("DELETE FROM RECEIPT WHERE RCN = ?", 0)) {
        deletion2.setInt(1, rcn);
        deletion2.execute();
        this.getDBTransaction().commit();
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  /**
   * This functions looks list of locations and applies if they meet stow item criteria.
   * It then adds as a potential stow location.
   */
  public String addToLocationList(LocationListImpl vo, String mfgH,
                                  String mfgV, String scc,
                                  String wareHouseId, String locLabel,
                                  int curRows, String wacNo, String sideStr,
                                  String bay, String locLevel, double nwt,
                                  double ncu, String niinIdStr,
                                  String locationIdList,
                                  String expDateStr) {
    try {
      // Picking new locations or matching locations
      Row row;
      int i = curRows;
      int maxLocationsToShow = 3;
      StringBuilder locList = new StringBuilder(locationIdList);

      LocationVOImpl vol = getLocationVO1();
      vol.setRangeSize(-1);
      if (wareHouseId == null) log.debug("rewarehouse debug - warehouseId is null");
      vol.setNamedWhereClauseParam("whId", wareHouseId);
      if (wacNo == null) log.debug("rewarehouse debug - wacNo is null");
      vol.setNamedWhereClauseParam("wacNumberStr", wacNo);
      if (sideStr == null) log.debug("rewarehouse debug - sideStr is null");
      vol.setNamedWhereClauseParam("sideStr", sideStr);
      if (bay == null) log.debug("rewarehouse debug - bayStr is null");
      vol.setNamedWhereClauseParam("bayStr", bay);
      if (locLevel == null) log.debug("rewarehouse debug - locLevel is null");
      vol.setNamedWhereClauseParam("locLevelStr", locLevel);
      if (locLabel == null) log.debug("rewarehouse debug - locLabel is null");
      vol.setNamedWhereClauseParam("locLabelStr", locLabel != null ? locLabel.toUpperCase() + "%" : null);
      if (niinIdStr == null) log.debug("rewarehouse debug - niinIdStr is null");
      vol.setNamedWhereClauseParam("niinIdStr", (niinIdStr == null || niinIdStr.length() <= 0) ? "0" : niinIdStr);

      if (!mfgH.equalsIgnoreCase("N")) { // For cases where we can choose any location
        vol.setNamedWhereClauseParam("newWeight", nwt);
        vol.setNamedWhereClauseParam("newCube", ncu);
      }
      else {
        vol.setNamedWhereClauseParam("newWeight", "1");
        vol.setNamedWhereClauseParam("newCube", "1");
      }
      vol.setNamedWhereClauseParam("mechFlagH", mfgH);
      vol.setNamedWhereClauseParam("mechFlagV", mfgV);
      //See if it can support only Secure locations
      if ((scc != null) && (scc.length() > 0)) vol.setNamedWhereClauseParam("secureFlag", "Y");
      else vol.setNamedWhereClauseParam("secureFlag", "N");

      if (expDateStr == null) log.debug("rewarehouse debug - expDtStr is null");
      vol.setNamedWhereClauseParam("expDtStr", (expDateStr == null || expDateStr.length() <= 0) ? "" : expDateStr);

      vol.executeQuery();
      Row vlr2 = vol.first();
      while ((vlr2 != null) && (i < maxLocationsToShow)) {
        if (!locList.toString().contains(vlr2.getAttribute("LocationId").toString())) {
          row = vo.createRow();
          row.setAttribute("WacId", vlr2.getAttribute("WacId"));
          row.setAttribute("LocationId", vlr2.getAttribute("LocationId"));
          row.setAttribute("LocationLabel", vlr2.getAttribute("LocationLabel"));
          row.setAttribute("LocQty", "0");
          //-------------------
          if (i == 0) vo.insertRow(row);
          else vo.insertRowAtRangeIndex(i, row);
          //-------------------

          locList.append(',').append(vlr2.getAttribute("LocationId"));
          i++;
        }
        vlr2 = vol.next();
      }
      vol.clearCache();
      vol.refreshWhereClauseParams();
      vol.reset();
      return i + "-" + locList + " ";
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return curRows + "-" + locationIdList + " ";
  }

  public void updateErrorQueueRecordStatus(String keyTyp, String keyStr, int userID) {
    try (val stR = getDBTransaction().createPreparedStatement("begin update error_queue set status = 'CLOSED', modified_by = ?, modified_date = sysdate where key_type = ? and key_num = ?; end;", 0)) {
      stR.setInt(1, userID);
      stR.setString(2, keyTyp);
      stR.setString(3, keyStr);
      stR.executeUpdate();
      getDBTransaction().commit();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  /**
   * This function creates all of the database records necessary for STRATIS to operate a Vertical Carousel
   */
  public int createVerticalCarousel(int wacId, int levels, Map<Integer, String> binMap, int userId) {
    try {
      //This function makes the assumption that the front end already checked if the WAC can support the carousel
      //that it is attempting to create.
      String wacLabel;
      String carouselId = "00";
      int carouselCount;

      // get the wac number for this wac_id
      try (PreparedStatement wacps = getDBTransaction().createPreparedStatement("select wac_number from wac where wac_id = ?", 0)) {
        wacps.setInt(1, wacId);
        try (ResultSet wacrs = wacps.executeQuery()) {

          //GET THE WAC NUMBER
          if (wacrs.next()) wacLabel = wacrs.getString(1);
          else return -1;
        }
      }

      //How many carousels exist for this location? Increment the count when done.
      try (PreparedStatement carPs = getDBTransaction().createPreparedStatement("select carousel_count from site_info", 0)) {
        try (ResultSet carRs = carPs.executeQuery()) {

          if (carRs.next()) {
            //GET THE NEXT CAROUSEL ID
            carouselCount = carRs.getInt(1);
            carouselId += (carouselCount + 1);
            carouselId = carouselId.substring(carouselId.length() - 2);
          }
          else return -2;
        }
      }

      for (int levelCount = 0; levelCount < levels; levelCount++) {
        //FOR EVERY LEVEL
        //CREATE LOC_HEADER RECORD
        String lvlStr = "000" + (levelCount + 1);
        long lMaxWeight;
        int iHeaderId;

        try (val headerPs = getDBTransaction().createPreparedStatement("insert into location_header(Header_type, weight, bay_or_level, aisle, side, max_weight, created_by, wac_id) values(?,?,?,?,?,?,?,?)", 0)) {
          headerPs.setString(1, "B");
          headerPs.setInt(2, 0);
          headerPs.setString(3, lvlStr.substring(lvlStr.length() - 3));
          if (wacLabel.length() > 5) headerPs.setString(4, wacLabel.substring(wacLabel.length() - 5));
          else headerPs.setString(4, wacLabel);
          headerPs.setString(5, "A");

          //CALCULATE MAXIMUM WEIGHT FOR LEVEL
          lMaxWeight = 0;
          for (int i = 0; i < levels; i++) {
            int iWidth;
            int iWeight;
            try (val iPs = getDBTransaction().createPreparedStatement("select width, usable_weight from location_classification where loc_classification_id = ?", 0)) {
              iPs.setObject(1, binMap.get(i + 1));

              try (val iRs = iPs.executeQuery()) {
                if (iRs.next()) {
                  iWidth = iRs.getInt(1);
                  iWeight = iRs.getInt(2);
                }
                else {
                  this.getDBTransaction().rollback();
                  return -3;
                }
              }
            }

            int bplvl = (112 / iWidth); //bins per level
            lMaxWeight += bplvl * iWeight;
          }
          headerPs.setLong(6, lMaxWeight);
          headerPs.setInt(7, userId);
          headerPs.setInt(8, wacId);
          headerPs.executeUpdate();
        }

        try (val headerPs = getDBTransaction().createPreparedStatement("select loc_header_id from location_header where wac_id = ? and bay_or_level = ? and aisle = ? and side = ?", 0)) {
          headerPs.setInt(1, wacId);
          headerPs.setString(2, lvlStr.substring(lvlStr.length() - 3));
          if (wacLabel.length() > 5) headerPs.setString(3, wacLabel.substring(wacLabel.length() - 5));
          else headerPs.setString(3, wacLabel);
          headerPs.setString(4, "A");

          try (val headerRs = headerPs.executeQuery()) {
            if (headerRs.next()) iHeaderId = headerRs.getInt(1);
            else {
              getDBTransaction().rollback();
              return -6;
            }
          }
        }

        int width;
        int dividerType;
        long cube;
        int weight;

        //GET BIN TYPE FOR THIS LEVEL AND RELATED INFO
        try (val binPs = getDBTransaction().createPreparedStatement("select width, divider_type_id, usable_cube, usable_weight from location_classification where loc_classification_id = ?", 0)) {
          binPs.setObject(1, binMap.get(levelCount + 1));

          try (val binRs = binPs.executeQuery()) {
            if (binRs.next()) {
              width = binRs.getInt(1);
              dividerType = binRs.getInt(2);
              cube = binRs.getLong(3);
              weight = binRs.getInt(4);
            }
            else {
              this.getDBTransaction().rollback();
              return -3;
            }
          }
        }

        //FIND OUT HOW MANY BINS FIT ON THIS LEVEL/BAY
        int binsPerBay = (112 / width);

        //Maximum number if bins per level exceeded
        if (binsPerBay > 26) {
          this.getDBTransaction().rollback();
          return -10;
        }

        //FIND OUT SLOTS PER BIN
        int slotCount;
        try (val countPs = getDBTransaction().createPreparedStatement("select count(*) from divider_slots where divider_type_id = ?", 0)) {
          countPs.setInt(1, dividerType);

          try (val countRs = countPs.executeQuery()) {
            if (countRs.next()) slotCount = countRs.getInt(1);
            else {
              this.getDBTransaction().rollback();
              return -4;
            }
          }
        }

        for (int binLoop = 0; binLoop < binsPerBay; binLoop++) {
          //ON EVERY LEVEL, MAX THE NUMBER OF BINS OF THE GIVEN BIN TYPE TO FIT ON THAT LEVEL
          //ON A VERTICAL CAROUSEL, EACH BIN TYPE IS A "BAY" SINCE THERE ARE NO FIXED COLUMNS ON A VERITCAL CAROUSEL

          int iHeaderBin;
          long lMaxCube;
          int iBinLabel = 65 + binLoop;
          char cBin = (char) iBinLabel;

          String bayStr = cBin + "A";

          //CREATE LOC_HEADER_BIN RECORD FOR EVERY BIN
          try (val headerbinPs = getDBTransaction().createPreparedStatement("insert into location_header_bin(Header_type, weight, bay, loc_level, aisle, side, loc_header_id, created_by, max_weight, max_cube, loc_classification_id) values(?,?,?,?,?,?,?,?,?,?,?)", 0)) {
            headerbinPs.setString(1, "D");
            headerbinPs.setInt(2, 0);
            headerbinPs.setString(3, bayStr.substring(bayStr.length() - 2));
            headerbinPs.setString(4, lvlStr.substring(lvlStr.length() - 3));
            if (wacLabel.length() > 5) headerbinPs.setString(5, wacLabel.substring(wacLabel.length() - 5));
            else headerbinPs.setString(5, wacLabel);
            headerbinPs.setString(6, "A");
            headerbinPs.setInt(7, iHeaderId);
            headerbinPs.setInt(8, userId);
            //CALCULATE MAXIMUM WEIGHT FOR LOC_HEADER_BIN
            lMaxWeight = (long) weight * binsPerBay;
            lMaxCube = cube * binsPerBay;

            headerbinPs.setLong(9, lMaxWeight);
            headerbinPs.setLong(10, lMaxCube);
            headerbinPs.setObject(11, binMap.get(levelCount + 1));

            // run the insert
            headerbinPs.executeUpdate();
          }

          try (val headerbinPs = getDBTransaction().createPreparedStatement("select location_header_bin_id from location_header_bin where bay = ? and loc_level = ? and aisle = ? and side = ?", 0)) {
            headerbinPs.setString(1, bayStr.substring(bayStr.length() - 2));
            headerbinPs.setString(2, lvlStr.substring(lvlStr.length() - 3));
            if (wacLabel.length() > 5) headerbinPs.setString(3, wacLabel.substring(wacLabel.length() - 5));
            else headerbinPs.setString(3, wacLabel);
            headerbinPs.setString(4, "A");

            try (val headerbinRs = headerbinPs.executeQuery()) {
              if (headerbinRs.next()) iHeaderBin = headerbinRs.getInt(1);
              else {
                this.getDBTransaction().rollback();
                return -7;
              }
            }
          }

          //FOR ALL DIVIDERS (IF THERE ARE ANY)
          for (int slotLoop = 0; slotLoop < slotCount; slotLoop++) {
            //GET INFO NEEDED FOR LOCATION RECORDS
            String displayPos;

            try (val slotPs = getDBTransaction().createPreparedStatement("select display_position from divider_slots where select_index = ? AND divider_type_id = ?", 0)) {
              slotPs.setInt(1, slotLoop);
              slotPs.setInt(2, dividerType);

              try (val slotRs = slotPs.executeQuery()) {
                if (slotRs.next()) displayPos = slotRs.getString(1);
                else {
                  this.getDBTransaction().rollback();
                  return -5;
                }
              }
            }

            //CREATE LOCATION LABEL
            String locLabel = wacLabel + carouselId;
            if ((levelCount + 1) < 10) locLabel += "0";
            int iSlotLabel = 65 + slotLoop;
            char cSlot = (char) iSlotLabel;
            locLabel += (levelCount + 1) + String.valueOf(cBin);

            if (slotLoop > 25) {
              if (slotLoop > 35) {
                //Too many locations per bay!   Bin creation / size error.
                this.getDBTransaction().rollback();
                return -9;
              }
              //Use numeric locations for slot values 27-36 "0" - "9"
              locLabel += Math.abs(26 - slotLoop);
            }
            else locLabel += String.valueOf(cSlot);

            //CREATE LOCATION RECORDS
            try (val locPs = getDBTransaction().createPreparedStatement("insert into location (WAC_ID, Availability_Flag, Mechanized_Flag, Divider_Index, Side, Location_Label, Bay, Loc_Level, Slot, Location_Header_Bin_ID, Aisle, created_by) values(?,?,?,?,?,?,?,?,?,?,?,?)", 0)) {
              locPs.setInt(1, wacId);
              locPs.setString(2, "A");
              locPs.setString(3, "V");
              locPs.setInt(4, slotLoop);
              locPs.setString(5, "A");
              locPs.setString(6, locLabel);
              locPs.setString(7, bayStr.substring(bayStr.length() - 2));
              locPs.setString(8, lvlStr.substring(lvlStr.length() - 3));
              locPs.setString(9, displayPos);
              locPs.setInt(10, iHeaderBin);
              if (wacLabel.length() > 5) locPs.setString(11, wacLabel.substring(wacLabel.length() - 5));
              else locPs.setString(11, wacLabel);
              locPs.setInt(12, userId);

              // run the insert
              locPs.execute();
            }
          }
        }
      }

      //CAROUSEL CREATED
      try (val siPs = getDBTransaction().createPreparedStatement("update site_info set carousel_count = ?", 0)) {
        //Increment Carousel Counter
        carouselCount++;
        siPs.setInt(1, carouselCount);
        siPs.execute();
      }

      getDBTransaction().commit();
    }
    catch (Exception e) {
      getDBTransaction().rollback();
      AdfLogUtility.logException(e);
    }
    return 0;
  }

  /**
   * This function creates all of the database records necessary for STRATIS to operate a Horizontal Carousel
   */
  public int createHorizontalCarousel(int wacId, int levels, int bays, Map<Integer, String> binMap, int userId) {
    try {
      //This function makes the assumption that the front end already checked if the WAC can support the carousel
      //that it is attempting to create.

      String side;
      String wacLabel;
      int slotCounter;
      String carouselId = "00";
      int carouselCount;

      // get the wac number for this wac_id
      try (PreparedStatement wacps = getDBTransaction().createPreparedStatement("select wac_number from wac where wac_id = ?", 0)) {
        wacps.setInt(1, wacId);
        try (ResultSet wacrs = wacps.executeQuery()) {

          //GET THE WAC NUMBER
          if (wacrs.next()) wacLabel = wacrs.getString(1);
          else return -1;
        }
      }

      // get the side for this wac_id
      int sideCount;
      try (PreparedStatement sideps = getDBTransaction().createPreparedStatement("select count(distinct(side)) from location where wac_id = ?", 0)) {
        sideps.setInt(1, wacId);
        try (ResultSet siders = sideps.executeQuery()) {
          sideCount = 0;
          if (siders.next()) {
            sideCount = siders.getInt(1);
          }
        }
      }

      if (sideCount == 0) side = "A";
      else {
        if (sideCount == 1) side = "B";
        else return -8;
      }

      //How many carousels exist for this location? Increment the count when done.
      try (PreparedStatement carPs = getDBTransaction().createPreparedStatement("select carousel_count from site_info", 0)) {
        try (ResultSet carRs = carPs.executeQuery()) {

          if (carRs.next()) {
            //GET THE NEXT CAROUSEL ID
            carouselCount = carRs.getInt(1);
            carouselId += (carouselCount + 1);
            carouselId = carouselId.substring(carouselId.length() - 2);
          }
          else return -2;
        }
      }

      for (int bayCount = 0; bayCount < bays; bayCount++) {
        //FOR EVERY BAY
        //CREATE LOC_HEADER RECORD
        String bayStr = "000" + (bayCount + 1);
        long lMaxWeight;
        int iHeaderId;
        try (PreparedStatement headerPs = getDBTransaction().createPreparedStatement("insert into location_header(Header_type, weight, bay_or_level, aisle, side, max_weight, created_by, wac_id) values(?,?,?,?,?,?,?,?)", 0)) {
          headerPs.setString(1, "B");
          headerPs.setInt(2, 0);
          headerPs.setString(3, bayStr.substring(bayStr.length() - 3));
          if (wacLabel.length() > 5) headerPs.setString(4, wacLabel.substring(wacLabel.length() - 5));
          else headerPs.setString(4, wacLabel);
          headerPs.setString(5, side);

          //CALCULATE MAXIMUM WEIGHT FOR BAY-COLUMN
          lMaxWeight = 0;
          for (int i = 0; i < levels; i++) {
            int iWidth;
            int iWeight;
            try (PreparedStatement iPs = getDBTransaction().createPreparedStatement("select width, usable_weight from location_classification where loc_classification_id = ?", 0)) {
              iPs.setObject(1, binMap.get(i + 1));
              try (ResultSet iRs = iPs.executeQuery()) {
                if (iRs.next()) {
                  iWidth = iRs.getInt(1);
                  iWeight = iRs.getInt(2);
                }
                else {
                  this.getDBTransaction().rollback();
                  return -3;
                }
              }
            }

            int bpb = (25 / iWidth); //bins per bay
            lMaxWeight += bpb * iWeight;
          }
          headerPs.setLong(6, lMaxWeight);
          headerPs.setInt(7, userId);
          headerPs.setInt(8, wacId);
          headerPs.executeUpdate();
        }

        try (val headerPs = getDBTransaction().createPreparedStatement("select loc_header_id from location_header where wac_id = ? and bay_or_level = ? and aisle = ? and side = ?", 0)) {
          headerPs.setInt(1, wacId);
          headerPs.setString(2, bayStr.substring(bayStr.length() - 3));
          if (wacLabel.length() > 5) headerPs.setString(3, wacLabel.substring(wacLabel.length() - 5));
          else headerPs.setString(3, wacLabel);
          headerPs.setString(4, side);
          try (ResultSet headerRs = headerPs.executeQuery()) {
            if (headerRs.next()) iHeaderId = headerRs.getInt(1);
            else {
              getDBTransaction().rollback();
              return -6;
            }
          }
        }

        for (int levelCount = 0; levelCount < levels; levelCount++) {
          //FOR EVERY LEVEL
          int width;
          int dividerType;
          long cube;
          int weight;

          //GET BIN TYPE FOR THIS LEVEL AND RELATED INFO
          try (val binPs = getDBTransaction().createPreparedStatement("select width, divider_type_id, usable_cube, usable_weight from location_classification where loc_classification_id = ?", 0)) {
            binPs.setObject(1, binMap.get(levelCount + 1));

            try (val binRs = binPs.executeQuery()) {
              if (binRs.next()) {
                width = binRs.getInt(1);
                dividerType = binRs.getInt(2);
                cube = binRs.getLong(3);
                weight = binRs.getInt(4);
              }
              else {
                this.getDBTransaction().rollback();
                return -3;
              }
            }
          }

          //FIND OUT HOW MANY BINS FIT ON THIS LEVEL/BAY
          int binsPerBay = (25 / width);
          if (binsPerBay > 5) {
            //Bin type too small for Horizontal Carousel.  Use standard bin type.
            this.getDBTransaction().rollback();
            return -10;
          }

          //FIND OUT SLOTS PER BIN
          int slotCount;

          try (PreparedStatement countPs = getDBTransaction().createPreparedStatement("select count(*) from divider_slots where divider_type_id = ?", 0)) {
            countPs.setInt(1, dividerType);
            try (ResultSet countRs = countPs.executeQuery()) {
              if (countRs.next()) slotCount = countRs.getInt(1);
              else {
                this.getDBTransaction().rollback();
                return -4;
              }
            }
          }

          String lvlStr = "00" + (levelCount + 1);
          int iHeaderBin;
          long lMaxCube;

          //CREATE LOC_HEADER_BIN RECORD
          try (PreparedStatement headerbinPs = getDBTransaction().createPreparedStatement("insert into location_header_bin(Header_type, weight, bay, loc_level, aisle, side, loc_header_id, created_by, max_weight, max_cube, loc_classification_id) values(?,?,?,?,?,?,?,?,?,?,?)", 0)) {
            headerbinPs.setString(1, "D");
            headerbinPs.setInt(2, 0);
            headerbinPs.setString(3, bayStr.substring(bayStr.length() - 3));
            headerbinPs.setString(4, lvlStr.substring(lvlStr.length() - 2));
            if (wacLabel.length() > 5) headerbinPs.setString(5, wacLabel.substring(wacLabel.length() - 5));
            else headerbinPs.setString(5, wacLabel);
            headerbinPs.setString(6, side);
            headerbinPs.setInt(7, iHeaderId);
            headerbinPs.setInt(8, userId);
            //CALCULATE MAXIMUM WEIGHT FOR LOC_HEADER_BIN
            lMaxWeight = (long) weight * binsPerBay;
            lMaxCube = cube * binsPerBay;

            headerbinPs.setLong(9, lMaxWeight);
            headerbinPs.setLong(10, lMaxCube);
            headerbinPs.setObject(11, binMap.get(levelCount + 1));

            // run the insert
            headerbinPs.executeUpdate();
          }

          try (PreparedStatement headerbinPs = getDBTransaction().createPreparedStatement("select location_header_bin_id from location_header_bin where bay = ? and loc_level = ? and aisle = ? and side = ?", 0)) {
            headerbinPs.setString(1, bayStr.substring(bayStr.length() - 3));
            headerbinPs.setString(2, lvlStr.substring(lvlStr.length() - 2));
            if (wacLabel.length() > 5) headerbinPs.setString(3, wacLabel.substring(wacLabel.length() - 5));
            else headerbinPs.setString(3, wacLabel);
            headerbinPs.setString(4, side);
            try (ResultSet headerbinRs = headerbinPs.executeQuery()) {
              if (headerbinRs.next()) iHeaderBin = headerbinRs.getInt(1);
              else {
                this.getDBTransaction().rollback();
                return -7;
              }
            }
          }

          //CREATE LOCATION RECORDS FOR ALL BINS ON THIS LEVEL/BAY AND ALL DIVIDERS IN BINS
          slotCounter = 0;

          for (int binLoop = 0; binLoop < binsPerBay; binLoop++) {
            for (int slotLoop = 0; slotLoop < slotCount; slotLoop++) {
              //GET INFO NEEDED FOR LOCATION RECORDS
              String displayPos;

              try (PreparedStatement slotPs = getDBTransaction().createPreparedStatement("select display_position from divider_slots where select_index = ? AND divider_type_id = ?", 0)) {
                slotPs.setInt(1, slotLoop);
                slotPs.setInt(2, dividerType);
                try (ResultSet slotRs = slotPs.executeQuery()) {
                  if (slotRs.next()) displayPos = slotRs.getString(1);
                  else {
                    this.getDBTransaction().rollback();
                    return -5;
                  }
                }
              }

              //CREATE LOCATION LABEL
              String locLabel = wacLabel + carouselId;
              if ((bayCount + 1) < 10) locLabel += "0";
              int iLevelLabel = 65 + levelCount;
              int iSlotLabel = 65 + slotCounter;
              char cLevel = (char) iLevelLabel;
              char cSlot = (char) iSlotLabel;
              locLabel += (bayCount + 1) + String.valueOf(cLevel).toUpperCase();
              if (slotCounter > 25) {
                if (slotCounter > 35) {
                  //Too many locations per bay!   Bin creation / size error.
                  this.getDBTransaction().rollback();
                  return -9;
                }
                //Use numeric locations for slot values 27-36 "0" - "9"
                locLabel += Math.abs(26 - slotCounter);
              }
              else locLabel += String.valueOf(cSlot).toUpperCase();

              //CREATE LOCATION RECORDS
              try (PreparedStatement locPs = getDBTransaction().createPreparedStatement("insert into location (WAC_ID, Availability_Flag, Mechanized_Flag, Divider_Index, Side, Location_Label, Bay, Loc_Level, Slot, Location_Header_bin_ID, Aisle, created_by) values(?,?,?,?,?,?,?,?,?,?,?,?)", 0)) {
                log.debug("insert into location (WAC_ID={}, Divider_Index={}, Side={}, Location_Label={}, Bay={}, Loc_Level={}, Slot={}, Location_Header_bin_ID={}, Aisle={})", wacId, slotLoop, side, locLabel, bayStr.substring(bayStr.length() - 3), lvlStr.substring(lvlStr.length() - 2), displayPos, iHeaderBin, ((wacLabel.length() > 5) ? wacLabel.substring(wacLabel.length() - 5) : wacLabel));
                locPs.setInt(1, wacId);
                locPs.setString(2, "A");
                locPs.setString(3, "H");
                locPs.setInt(4, slotLoop);
                locPs.setString(5, side);
                locPs.setString(6, locLabel);
                locPs.setString(7, bayStr.substring(bayStr.length() - 3));
                locPs.setString(8, lvlStr.substring(lvlStr.length() - 2));
                locPs.setString(9, displayPos);
                locPs.setInt(10, iHeaderBin);
                if (wacLabel.length() > 5) locPs.setString(11, wacLabel.substring(wacLabel.length() - 5));
                else locPs.setString(11, wacLabel);
                locPs.setInt(12, userId);

                // run the insert
                locPs.execute();
              }

              slotCounter++;
            }
          }
          //END LEVEL LOOP
        }
        //END BAY LOOP
      }

      //CAROUSEL CREATED

      try (PreparedStatement siPs = getDBTransaction().createPreparedStatement("update site_info set carousel_count = ?", 0)) {
        //Increment Carousel Counter
        carouselCount++;
        siPs.setInt(1, carouselCount);
        siPs.execute();
      }

      getDBTransaction().commit();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      getDBTransaction().rollback();
      return -999;
    }
    return 0;
  }

  /**
   * This function creates the appropriate checksum character for the White brand Horizontal Carousel
   */
  public char calculateCarouselChecksum(String inStr) {

    try {
      int csum = 0;
      for (int i = 0; i < inStr.length(); i++) {
        csum += Integer.parseInt(inStr.substring(i, i + 1));
      }
      int aVal = 'A';

      //Create Checksum
      aVal += (csum % 26);

      //Return Checksum
      return (char) aVal;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return 'A';
  }

  /**
   * This function creates the appropriate string to move a REMSTAR     brand
   * Horizontal carousel to a given bin (column) based on the side
   * (REMSTAR carousel's used in Okinawa)
   */
  public String moveRemstarHorizontalCarousel(int bin, char side,
                                              int offset, int ctlNum) {

    try {
      String outStr = "";
      char stx = 0x02;
      char etx = 0x03;
      char cZero = '0';
      if (side == 'A')
        side = '1';
      else
        side = '2';
      String sCtlNum = "00" + ctlNum;
      outStr += stx + sCtlNum.substring(sCtlNum.length() - 2);
      int sideVal = side;
      offset += (sideVal - cZero);
      offset = (offset % 2 == 0) ? 2 : 1; //* carousel can only ever be 1 (A or left) or 2 (B or right)
      String sOffset = "00" + offset;
      outStr += sOffset.substring(sOffset.length() - 2) + "f";
      String sBin = "000" + bin;
      outStr += sBin.substring(sBin.length() - 3);

      outStr += "05" + "AA" + "0" + "01" + "+000018" + " ";
      //14 Blank Spaces
      outStr += "              " + "**" + etx + '\0';
      outStr += outStr;
      log.debug("remstar  {}", outStr);
      return outStr;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return "";
  }

  /**
   * This function creates the appropriate string to move a WHITE brand
   * Horizontal carousel to a given bin (column) based on the side
   * (White carousel's used in Camp Pendleton)
   */
  public String moveWhiteHorizontalCarousel(int bin, char side) {
    try {
      String outStr = "";
      char stx = 0x02;
      char etx = 0x03;
      outStr += stx + "00";
      if (side == 'A')
        outStr += "1";
      else
        outStr += "2";
      outStr += "30000G";
      String binStr = "000" + bin;
      outStr += binStr.substring(binStr.length() - 3) + etx;
      String checkStr = "00";
      if (side == 'A')
        checkStr += "1";
      else
        checkStr += "2";
      checkStr += binStr.substring(binStr.length() - 3);
      outStr += this.calculateCarouselChecksum(checkStr);
      outStr += stx + "00130000SS0" + etx + 'o';
      outStr = stx + "00130000SS0" + etx + 'o' + outStr;
      return outStr;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return "";
  }

  /**
   * This function creates the appropriate string to move a WHITE brand
   * Horizontal carousel to a given bin (column) based on the side
   * (White carousel's used in Camp Pendleton)
   */
  public String moveWhiteHorizontalCarouselOld(int bin, char side) {

    try {
      String outStr = "";
      char stx = 0x02;
      char etx = 0x03;
      outStr += stx + "00";
      if (side == 'A') outStr += "1";
      else outStr += "2";
      outStr += "30000G";
      String binStr = "000" + bin;
      outStr += binStr.substring(binStr.length() - 3) + "B00" + etx;
      String checkStr = "00";
      if (side == 'A') checkStr += "1";
      else checkStr += "2";
      checkStr += binStr.substring(binStr.length() - 3);
      outStr += this.calculateCarouselChecksum(checkStr);
      outStr += stx + "00130000SS0" + etx + 'o';
      return outStr;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return "";
  }

  /**
   * This function creates the appropriate string to move a HANEL brand Vertical carousel to a given level
   */
  public String moveHanelVerticalCarousel(int level) {

    String carMsg = "";
    try {
      carMsg += "*G01$C1$ T";
      if (level != 0) {
        String sLevel = "000" + level;
        carMsg += sLevel.substring(sLevel.length() - 3);
      }
      carMsg += "$";
      //Not sure if the CR LF are necessary.  Depends on the function used to print the string to the serial port.
      carMsg += "\r\n";
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return carMsg;
  }

  public String getWareHouseId(String stowArea, int wrkId) {
    try {
      String stArea = stowArea.toUpperCase().trim() + "%";
      String whId = "NONE";
      try (PreparedStatement stR = getDBTransaction().createPreparedStatement("select unique w.warehouse_id from wac w, equip e, location l, wac w1 " + " where e.equipment_number = ? and l.location_label like ? and e.wac_id = w.wac_id and l.wac_id = w1.wac_id and w1.warehouse_id = w.warehouse_id", 0)) {
        stR.setInt(1, wrkId);
        stR.setString(2, stArea);
        try (ResultSet rs = stR.executeQuery()) {
          if (rs.next()) whId = rs.getString(1);
        }
      }
      return whId;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return "NONE";
  }

  // function to return the next scn in the sequence

  public String returnNextSCN(boolean isWalkThru) {
    // Get the next sequence from DB and build SCN and return
    val trans = (TransactionsImpl) this.getTransactions1();
    try {
      val scn = new StringBuilder();
      try (val st = getDBTransaction().createPreparedStatement("select ISSUE_SCN_SEQ.nextval from dual", 0)) {
        try (val rs = st.executeQuery()) {
          rs.next();
          int r = rs.getInt(1);
          for (int i = 1; i <= 4; i++) {
            val sqM = r % 36;
            if (sqM <= 9) {
              scn.insert(0, (char) (sqM + 48));
            }
            else {
              scn.insert(0, (char) (sqM + 55));
            }
            r = Math.abs(r / 36);
          }
        }
      }

      val prefix = isWalkThru ? "W" : "P";
      scn.insert(0, prefix + trans.getCurrentJulian(4));
      return (scn.toString());
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return null;
  }

  public void createException(int oldniinid, int newniinid, String documentnumber) {
    try {
      String sql = "begin INSERT INTO error_queue (status, created_date, created_by, notes, eid, key_type, key_num) " +
          " values (?,sysdate,1,?, 82, ?, ?); end;";
      try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(sql, 0)) {
        pstmt.setString(1, "OPEN");
        pstmt.setString(2, documentnumber);
        pstmt.setString(3, Integer.toString(oldniinid));
        pstmt.setString(4, Integer.toString(newniinid));
        pstmt.executeUpdate();
      }
      getDBTransaction().commit();
    }
    catch (Exception e) {
      getDBTransaction().rollback();
      AdfLogUtility.logException(e);
    }
  }

  //MCF:11/17/2015
  //For conversion of WalkThru, we need to change the DOC_ID to A5A instead of Z0A.
  //Move the thing to history, both Issue and Pick
  //Generate SRO, AE1BA, AS1 transactions
  public void convertWalkThru(String scn) {

    try {
      //Generate SRO.
      try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement("select pid from picking where scn = ?", 0)) {
        pstmt.setString(1, scn);
        try (ResultSet rs = pstmt.executeQuery()) {
          while (rs.next()) {
            this.getGCSSMCTransactionsService().sendSROGCSSMCTransaction(rs.getString(1));
          }
        }
      }

      //Send AE1
      this.getGCSSMCTransactionsService().sendAE1GCSSMCTransaction(scn);

      //Send AS1
      this.getGCSSMCTransactionsService().sendAsxTrans(scn, 3);

      //Set document_id to A5A instead of Z0A
      try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement("update issue set document_id = 'A5A' where scn = ?", 0)) {
        pstmt.setString(1, scn);
        pstmt.executeUpdate();
        getDBTransaction().commit();
      }

      //Clear the picking
      try (PreparedStatement pstmt = this.getDBTransaction().createPreparedStatement("delete from picking where scn=?", 0)) {
        pstmt.setObject(1, scn);
        pstmt.executeUpdate();
        this.getDBTransaction().commit();
      }

      //Clear the issue
      try (PreparedStatement pstmt = this.getDBTransaction().createPreparedStatement("delete from issue where scn=?", 0)) {
        pstmt.setObject(1, scn);
        pstmt.executeUpdate();
        this.getDBTransaction().commit();
      }
    }
    catch (SQLException e) {
      AdfLogUtility.logException(e);
    }
  }

  /**
   * function to create the issue record, return the scn generated
   */
  public Object createIssue(Object documentid, Object documentnumber,
                            Object qty, Object issuetype,
                            Object issueprioritydesignator,
                            Object issueprioritygroup, Object customerid,
                            Object niinid, Object suffix, Object supaddress,
                            Object fundcode, Object mediastatuscode,
                            Object ricFrom, Object costJon,
                            Object signalCode, Object adviceCode,
                            Object rdd, Object cc, Object demilCode, boolean isHardcard) {
    Object returno = null;

    try {
      if (Util.isEmpty(ricFrom)) ricFrom = getSiteRIC();

      String whereClause = "";
      String whereClauseSuffix = "";
      if (Util.isEmpty(issuetype))
        whereClause = " AND issue_type is NULL";
      if (!Util.isEmpty(suffix))
        whereClauseSuffix = " AND suffix=?";

      int issueCount = 0;
      int issueCountH = 0;
      try (PreparedStatement stIssue = this.getDBTransaction().createPreparedStatement("select count(*) from issue where document_number = ?" + whereClauseSuffix + whereClause, 0)) {
        stIssue.setString(1, documentnumber.toString());
        if (!Util.isEmpty(suffix)) stIssue.setString(2, suffix.toString());
        try (ResultSet rsIssue = stIssue.executeQuery()) {
          rsIssue.next();
          issueCount += rsIssue.getInt(1);
        }
      }
      if (issueCount > 0) log.debug("issue NON-ZERO COUNT for document_number  {}", documentnumber);

      try (PreparedStatement stHist = this.getDBTransaction().createPreparedStatement("select count(*) from issue_hist where document_number = ?" + whereClauseSuffix + whereClause, 0)) {
        stHist.setString(1, documentnumber.toString());
        if (!Util.isEmpty(suffix)) stHist.setString(2, suffix.toString());
        try (ResultSet rsHist = stHist.executeQuery()) {
          rsHist.next();
          issueCountH += rsHist.getInt(1);
        }
      }

      if (issueCountH > 0) log.debug("issue_hist NON-ZERO COUNT for document_number  {}", documentnumber);
      issueCount += issueCountH;

      if (issueCount == 0) {

        // get the issue view
        NewIssueViewImpl issueview = getNewIssueView1();
        issueview.executeQuery();

        // add a new row into issue
        Row nrow = issueview.createRow();

        // ADD CHECKS TO NOT INSERT SPACES INTO NULL FIELDS!
        nrow.setAttribute("DocumentId", documentid);
        nrow.setAttribute("DocumentNumber", documentnumber);
        nrow.setAttribute("Qty", qty);
        nrow.setAttribute("IssueType", issuetype);
        nrow.setAttribute("IssuePriorityDesignator", issueprioritydesignator);
        nrow.setAttribute("IssuePriorityGroup", issueprioritygroup);
        nrow.setAttribute("CustomerId", customerid);
        nrow.setAttribute("NiinId", niinid);

        if (!isHardcard) nrow.setAttribute("Status", "PICK READY");
        else nrow.setAttribute("Status", "WALKTHRU");

        nrow.setAttribute("CreatedBy", 1);
        nrow.setAttribute("Suffix", (suffix == null) ? "" : suffix.toString().trim().toUpperCase());
        nrow.setAttribute("SupplementaryAddress", supaddress);
        nrow.setAttribute("FundCode", fundcode);
        nrow.setAttribute("MediaAndStatusCode", mediastatuscode);
        nrow.setAttribute("RoutingIdFrom", ricFrom);
        //* added 12/11/08 - allow issue to use condition code F
        nrow.setAttribute("Cc", Util.isEmpty(cc) ? "A" : cc);
        nrow.setAttribute("QtyIssued", qty);
        nrow.setAttribute("CostJon", costJon);
        nrow.setAttribute("SignalCode", (signalCode == null) ? "A" : signalCode.toString().trim().toUpperCase());
        nrow.setAttribute("AdviceCode", adviceCode);
        nrow.setAttribute("DemilCode", demilCode);
        if (rdd == null) {
          TransactionsImpl trans = (TransactionsImpl) this.getTransactions1();
          rdd = trans.getCurrentJulian(3);
        }
        nrow.setAttribute("Rdd", rdd);
        boolean walkThru = false;
        if (issuetype != null && issuetype.toString().equals("W")) walkThru = true;

        String nextscn = returnNextSCN(walkThru);

        nrow.setAttribute("Scn", nextscn);

        issueview.insertRow(nrow);
        getDBTransaction().commit();

        // give the scn if we made it this far
        returno = nextscn;
      }
      else return returno;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }

    // return result
    return returno;
  }

  /**
   * function to create the issue record, return the scn generated
   */
  public Object createIssueHist(Object documentid, Object documentnumber,
                                Object qty, Object issuetype,
                                Object issueprioritydesignator,
                                Object issueprioritygroup, Object customerid,
                                Object niinid, Object suffix, Object supaddress,
                                Object fundcode, Object mediastatuscode,
                                Object ricFrom, Object costJon,
                                Object signalCode, Object adviceCode,
                                Object rdd, Object cc, Object demilCode) {

    try {
      String sql = "begin INSERT INTO Issue_HIST (document_id, document_number, qty, issue_type, issue_priority_group, customer_id, niin_id, suffix, SUPPLEMENTARY_ADDRESS, Fund_code, media_and_status_code, routing_id_from, signal_code, advice_code, rdd, cc, demil_code, scn, status) " +
          " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?); end;";
      String nextscn;
      try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(sql, 0)) {
        pstmt.setObject(1, documentid);
        pstmt.setObject(2, documentnumber);
        pstmt.setObject(3, qty);
        pstmt.setObject(4, issuetype);
        pstmt.setObject(5, issueprioritydesignator);
        pstmt.setObject(6, issueprioritygroup);
        pstmt.setObject(7, customerid);
        pstmt.setObject(8, niinid);
        pstmt.setObject(9, suffix);
        pstmt.setObject(10, supaddress);
        pstmt.setObject(11, fundcode);
        pstmt.setObject(12, mediastatuscode);
        pstmt.setObject(13, ricFrom);
        pstmt.setObject(14, costJon);
        pstmt.setObject(15, signalCode);
        pstmt.setObject(16, adviceCode);
        pstmt.setObject(17, rdd);
        pstmt.setObject(18, cc);
        pstmt.setObject(19, demilCode);
        nextscn = returnNextSCN(true);
        pstmt.setObject(20, nextscn);
        pstmt.setObject(21, "WREJECTED");
        pstmt.executeUpdate();
      }
      getDBTransaction().commit();
      return nextscn;
    }
    catch (Exception e) {
      getDBTransaction().rollback();
      AdfLogUtility.logException(e);
    }

    return null;
  }

  /**
   * This function takes a pick already in progress and will generate additional picks,
   * re-assign the pick to a different location, or create partial refusals. Provides
   * a DBTransaction so we can pass in test a object.
   */
  public boolean returnBestPickLocation(Object scn, Object niinid, Object pid, String cc, DBTransactionGetter dbTransactionGetter) {

    try {
      PickMetrics pickMetrics = new PickMetrics(scn, pid, niinid, cc);

      if (scn != null || pid != null) {
        pickMetrics.scnText = String.valueOf(scn);

        //Additional or partial pick generation
        log.debug("Before doFirstPickingStep PickMetrics is {}", pickMetrics);
        if (pid != null) {
          //This is a pick re-assignment.
          initializePickMetrics(pickMetrics.pid, pickMetrics, dbTransactionGetter);
          removePreviousPick(pickMetrics.pid, pickMetrics.qtyPicked, dbTransactionGetter);
        }

        log.debug("Before getRequestedQuantityForIssue PickMetrics is {}", pickMetrics);
        pickMetrics.issueQty = getRequestedQuantityForIssue(pickMetrics.scn, dbTransactionGetter);

        if (pickMetrics.issueQty == 0) {
          log.debug("PickMetrics.issueQty is 0 so we're leaving returnBestPickLocation before getTotalQuantityForIssueSoFar; PickMetrics is {}", pickMetrics);
          return false;
        }

        log.debug("Before getTotalQuantityForIssueSoFar PickMetrics is {}", pickMetrics);
        pickMetrics.sumPicks = getTotalQuantityForIssueSoFar(pickMetrics, dbTransactionGetter);

        log.debug("Before getTotalUnpickedQuantityForIssueSoFar PickMetrics is {}", pickMetrics);
        pickMetrics.sumNotPicked = getTotalUnpickedQuantityForIssueSoFar(pickMetrics, dbTransactionGetter);

        pickMetrics.qtyRemaining = pickMetrics.issueQty - pickMetrics.sumPicks - pickMetrics.sumNotPicked;

        if (pickMetrics.qtyRemaining <= 0) {
          log.debug("After getTotalUnpickedQuantityForIssueSoFar pickMetrics.qtyRemaining is <= 0; returning TRUE; PickMetrics is {}", pickMetrics);
          return true;
        }

        log.debug("Before getLocationsAndGeneratePickingRecords PickMetrics is {}", pickMetrics);
        pickMetrics = getLocationsAndGeneratePickingRecords(pickMetrics, dbTransactionGetter);

        log.debug("WorkLoadManagerImpl: After picks were made qtyRemaining = [{}]...", pickMetrics.qtyRemaining);

        if (pickMetrics.qtyRemaining <= 0) {
          return true;
        }

        //All locations checked and there are still items needed to be picked for this issue
        log.debug("WorkLoadManagerImpl: Since qtyRemaining [{}] is > 0; adding PICK REFUSED record.; pickMetrics is {}", pickMetrics.qtyRemaining, pickMetrics);

        deleteOldPickingRefused(scn, dbTransactionGetter);
        createNewPickingRefused(pickMetrics, dbTransactionGetter);

        log.debug("Leaving returnBestPickLocation with TRUE; PickMetrics is {}", pickMetrics);
        return true;
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    log.debug("Leaving returnBestPickLocation with FALSE");
    return false;
  }

  /**
   * Alias for returnBestPickLocation with default DBTransaction.
   */
  public boolean returnBestPickLocation(Object scn, Object niinid, Object pid, String cc) {
    return returnBestPickLocation(scn, niinid, pid, cc, new AppModuleDBTransactionGetterImpl(this));
  }

  static final String GET_STATS_FOR_PID_SQL = "select scn, qty_picked, pick_qty, status, niin_loc_id from picking where pid = ?";

  void initializePickMetrics(String pid, PickMetrics pickMetrics, DBTransactionGetter dbTransactionGetter) throws SQLException {

    if (pid != null) {
      try (PreparedStatement stScn = dbTransactionGetter.getDBTransaction().createPreparedStatement(GET_STATS_FOR_PID_SQL, 0)) {
        stScn.setObject(1, pid);
        try (ResultSet rsScn = stScn.executeQuery()) {
          if (rsScn.next()) {
            pickMetrics.scn = rsScn.getString(1);
            pickMetrics.qtyPicked = rsScn.getInt(2);
            pickMetrics.niinLocId = rsScn.getInt(5);
            pickMetrics.scnText = String.valueOf(pickMetrics.scn);
          }
        }
      }
    }
  }

  void removePreviousPick(String pid, int qtyPicked, DBTransactionGetter dbTransactionGetter) throws SQLException {
    if (pid != null && qtyPicked == 0) {
      //Need to remove the previous pick
      try (PreparedStatement rmPick = dbTransactionGetter.getDBTransaction().createPreparedStatement("delete from picking where pid = ?", 0)) {
        rmPick.setObject(1, pid);
        rmPick.execute();
        dbTransactionGetter.getDBTransaction().commit();
      }
    }
  }

  /**
   * Get the quantity requested for the given issue
   */
  static final String GET_REQUESTED_QTY_FOR_ISSUE_SQL = "select qty from issue where scn = ?";

  int getRequestedQuantityForIssue(Object scn, DBTransactionGetter dbTransactionGetter) throws SQLException {
    if (scn != null) {
      try (PreparedStatement stR = dbTransactionGetter.getDBTransaction().createPreparedStatement(GET_REQUESTED_QTY_FOR_ISSUE_SQL, 0)) {
        stR.setObject(1, scn);
        try (ResultSet rs = stR.executeQuery()) {
          if (rs.next()) {
            return rs.getInt(1);
          }
        }
      }
    }
    return 0;
  }

  /**
   * get the total quantity picked so far for this issue
   */
  static final String GET_TOTAL_PICKED_QTY_FOR_ISSUE_SQL = "select sum(qty_picked) from picking where scn = ?";

  int getTotalQuantityForIssueSoFar(PickMetrics pickMetrics, DBTransactionGetter dbTransactionGetter) throws SQLException {
    if (pickMetrics != null && pickMetrics.scn != null) {
      try (val stSum = dbTransactionGetter.getDBTransaction().createPreparedStatement(GET_TOTAL_PICKED_QTY_FOR_ISSUE_SQL, 0)) {
        stSum.setObject(1, pickMetrics.scn);
        try (val rsSum = stSum.executeQuery()) {
          if (rsSum.next()) {
            if (rsSum.getObject(1) != null) {
              return rsSum.getInt(1);
            }
          }
        }
      }
    }

    return pickMetrics != null ? pickMetrics.sumPicks : 0;
  }

  /**
   * Get the total quantity not picked so far for this issue
   */
  static final String GET_TOTAL_UNPICKED_QTY_FOR_ISSUE_SQL = "select sum(pick_qty) from picking where scn = ? and qty_picked = 0 and status <> 'PICK REFUSED'";

  int getTotalUnpickedQuantityForIssueSoFar(PickMetrics pickMetrics, DBTransactionGetter dbTransactionGetter) throws SQLException {
    try (PreparedStatement stSum2 = dbTransactionGetter.getDBTransaction().createPreparedStatement(GET_TOTAL_UNPICKED_QTY_FOR_ISSUE_SQL, 0)) {
      stSum2.setObject(1, pickMetrics.scn);
      try (ResultSet rsSum2 = stSum2.executeQuery()) {
        if (rsSum2.next()) {
          if (rsSum2.getObject(1) != null) {
            return rsSum2.getInt(1);
          }
        }
        else {
          return 0;
        }
      }
    }
    return pickMetrics.sumNotPicked;
  }

  /**
   * Find locations where the niin items exist, and generate PICKING records.
   */
  private static final String GET_NIIN_QTYS_BY_LOCATION_SQL = "select qty, niin_loc_id from niin_location where niin_id = ? and locked = 'N' and cc = ? and qty > 0 and niin_loc_id <> ? ORDER BY EXPIRATION_DATE ASC, qty DESC";
  private static final String GET_RESERVED_TOTALS_FOR_LOCATION_SQL = "select sum(pick_qty) from picking where qty_picked = 0 and niin_loc_id = ? and (status = 'PICK READY' or status like 'PICK B%')";

  private PickMetrics getLocationsAndGeneratePickingRecords(PickMetrics pickMetrics, DBTransactionGetter dbTransactionGetter) throws SQLException {
    try (PreparedStatement stLoc = dbTransactionGetter.getDBTransaction().createPreparedStatement(GET_NIIN_QTYS_BY_LOCATION_SQL, 0)) {
      stLoc.setObject(1, pickMetrics.niinid);
      stLoc.setString(2, pickMetrics.cc);
      stLoc.setInt(3, pickMetrics.niinLocId);
      try (ResultSet rsLoc = stLoc.executeQuery()) {
        while (rsLoc.next() && pickMetrics.qtyRemaining > 0) {

          log.debug("WorkLoadManagerImpl: qtyRemaining [{}] is > 0; so adding PICK READY record.", pickMetrics.qtyRemaining);
          //Continue to check all niin_locations as long as there is a qty Remaining to be picked
          int qtyAssigned = 0;

          pickMetrics.locationId = rsLoc.getLong(2); // niin_loc_id

          try (val stPicking = dbTransactionGetter.getDBTransaction().createPreparedStatement(GET_RESERVED_TOTALS_FOR_LOCATION_SQL, 0)) {
            stPicking.setLong(1, pickMetrics.locationId.longValue());

            try (val rsPicking = stPicking.executeQuery()) {
              //Get the total number of items already reserved for picks at this location
              if (rsPicking.next()) qtyAssigned = rsPicking.getInt(1);
            }
          }

          int qtyAvail = rsLoc.getInt(1) - qtyAssigned;

          if (qtyAvail > 0) {
            //Make sure to not assign picks to the same Location

            //There are still items available at this location to be picked.   Assign an additional pick for this location
            if (qtyAvail >= pickMetrics.qtyRemaining) {
              //Insert a pick for the full remaining amount
              int retPid = insertPicking(pickMetrics.locationId, pickMetrics.qtyRemaining, pickMetrics.scnText, "PICK READY", dbTransactionGetter);
              getPackingStation(retPid, 1, pickMetrics.qtyRemaining, 0);
              pickMetrics.qtyRemaining = 0;
            }
            else {
              //Insert a pick for the maximum amount this location can support
              int retPid = insertPicking(pickMetrics.locationId, qtyAvail, pickMetrics.scnText, "PICK READY", dbTransactionGetter);
              getPackingStation(retPid, 1, qtyAvail, 0);
              pickMetrics.qtyRemaining = pickMetrics.qtyRemaining - qtyAvail;
            }
          }
        }
      }
    }
    return pickMetrics;
  }

  void deleteOldPickingRefused(Object scn, DBTransactionGetter dbTransactionGetter) throws SQLException {
    try (PreparedStatement refPs = dbTransactionGetter.getDBTransaction().createPreparedStatement("delete from picking where scn = ? and status = 'PICK REFUSED'", 0)) {
      refPs.setObject(1, scn);
      refPs.execute();
      dbTransactionGetter.getDBTransaction().commit();
    }
  }

  void createNewPickingRefused(PickMetrics pickMetrics, DBTransactionGetter dbTransactionGetter) {
    if (pickMetrics.niinLocId != -1) {
      pickMetrics.locationId = pickMetrics.niinLocId;
      insertPicking(pickMetrics.locationId, pickMetrics.qtyRemaining, pickMetrics.scnText, "PICK REFUSED", dbTransactionGetter);
    }
    else {
      insertPicking(null, pickMetrics.qtyRemaining, pickMetrics.scnText, "PICK REFUSED", dbTransactionGetter);
    }
  }

  private static final String SELECT_SUFFIX_CODE_SQL = "select suffix_code from picking where scn = ?";

  private static final String INSERT_PICKING_RECORD_SQL =
      "insert into picking (scn, niin_loc_id, created_by, pick_qty, qty_picked, status, bypass_count, suffix_code, pin) values (?,?,?,?,?,?,?,?,?) ";

  private static final String SELECT_PID_SQL = "select pid from picking where pin = ?";

  public int insertPicking(Object niinlocid, int qty, String scn, String status, DBTransactionGetter dbTransactionGetter) {
    int pid = 0;

    try {
      int totalsuffix;
      try (PreparedStatement pstmt = dbTransactionGetter.getDBTransaction().createPreparedStatement(SELECT_SUFFIX_CODE_SQL, 0)) {
        pstmt.setString(1, scn);
        try (ResultSet rs = pstmt.executeQuery()) {
          totalsuffix = 0;
          boolean done = false;
          while (rs.next() && !done) {
            val tempSuffix = rs.getInt(1);
            if (tempSuffix == totalsuffix) {
              totalsuffix++;
            }
            else {
              done = true;
            }
          }
        }
      }

      val pin = totalsuffix == 0 ? scn : scn + convertTotalSuffix(totalsuffix);
      try (val pstmt = dbTransactionGetter.getDBTransaction().createPreparedStatement(INSERT_PICKING_RECORD_SQL, 0)) {
        pstmt.setString(1, scn);
        pstmt.setObject(2, niinlocid);
        pstmt.setInt(3, 1);
        pstmt.setInt(4, qty);
        pstmt.setInt(5, 0);
        pstmt.setString(6, status);
        pstmt.setInt(7, 0);
        pstmt.setInt(8, totalsuffix);
        pstmt.setString(9, pin);
        pstmt.executeUpdate();
        dbTransactionGetter.getDBTransaction().commit();
      }

      try (PreparedStatement pstmt = dbTransactionGetter.getDBTransaction().createPreparedStatement(SELECT_PID_SQL, 0)) {
        pstmt.setString(1, pin);
        try (ResultSet rs = pstmt.executeQuery()) {
          if (rs.next()) pid = rs.getInt(1);
        }
      }
    }
    catch (SQLException e) {
      AdfLogUtility.logException(e);
    }

    return pid;
  }

  public String convertTotalSuffix(int totalsuffix) {
    String convertedSuffix;
    if (totalsuffix < 10) convertedSuffix = Integer.toString(totalsuffix);
    else {
      switch (totalsuffix) {
        case 10:
          convertedSuffix = "A";
          break;
        case 11:
          convertedSuffix = "B";
          break;
        case 12:
          convertedSuffix = "C";
          break;
        case 13:
          convertedSuffix = "D";
          break;
        case 14:
          convertedSuffix = "E";
          break;
        case 15:
          convertedSuffix = "F";
          break;
        case 16:
          convertedSuffix = "G";
          break;
        case 17:
          convertedSuffix = "H";
          break;
        case 18:
          convertedSuffix = "I";
          break;
        case 19:
          convertedSuffix = "J";
          break;
        case 20:
          convertedSuffix = "K";
          break;
        case 21:
          convertedSuffix = "L";
          break;
        case 22:
          convertedSuffix = "M";
          break;
        case 23:
          convertedSuffix = "N";
          break;
        case 24:
          convertedSuffix = "O";
          break;
        case 25:
          convertedSuffix = "P";
          break;
        case 26:
          convertedSuffix = "Q";
          break;
        case 27:
          convertedSuffix = "R";
          break;
        case 28:
          convertedSuffix = "S";
          break;
        case 29:
          convertedSuffix = "T";
          break;
        case 30:
          convertedSuffix = "U";
          break;
        case 31:
          convertedSuffix = "V";
          break;
        case 32:
          convertedSuffix = "W";
          break;
        case 33:
          convertedSuffix = "X";
          break;
        case 34:
          convertedSuffix = "Y";
          break;
        case 35:
          convertedSuffix = "Z";
          break;
        default:
          convertedSuffix = null;
          break;
      }
    }
    return convertedSuffix;
  }

  /**
   * CreateIssueRequest method only called from MATS Import.
   * This method processes a mats row from the MATS Import.
   * <p>
   * - If the customer found on the mats row is not in our CUSTOMER table, it is inserted
   * else, return value of -888
   * - If a NIIN is not found, then a check is performed to see if in the issue or issue_history,
   * if found, then -333 is returned and no Z7K duplicates allowed,
   * otherwise a -999 is returned and a Z7K is inserted into spool.
   * <p>
   * - An issue row is created, if creation fails, the return value is 3
   * - if creation is successful, then picking row is created and assigned a packing station.
   * if the pick insert fails, return value is -555
   * - If any exception occurs (e.g. NullPointerException) then the return value is -1
   */
  public int createPicksForMATS(String scn, Object transQty, Object niintext, Object cc) {

    // the qty needed
    int qtyneeded = Integer.parseInt(transQty.toString());
    int qtyused = 0;
    int returnv = 0;
    int pid;
    String packingStation;

    try {
      // get the niin view
      NiinInfoMATSViewImpl niinview = getNiinInfoMATSView1();
      niinview.setNamedWhereClauseParam("CCSearch", cc);
      niinview.setNamedWhereClauseParam("NIINSearch", niintext);
      niinview.setRangeSize(-1);
      niinview.executeQuery();

      int niinlocs = niinview.getRowCount();

      // just add all the locations till we get the qty fullfilled
      boolean shouldBreak = false;
      for (int i = 0; i < niinlocs; i++) {
        if (qtyused < qtyneeded) {
          // use this location
          Number locid = getNumberFromRowObject(niinview.getRowAtRangeIndex(i).getAttribute("NiinLocId"));
          int locqty = getNumberFromRowObject(niinview.getRowAtRangeIndex(i).getAttribute("Avail")).intValue();

          // make the qty an int
          if (locqty < (qtyneeded - qtyused)) {
            pid = insertPicking(locid, locqty, scn, "PICK READY", new AppModuleDBTransactionGetterImpl(this));

            if (pid < 1) {
              returnv = -552;
              // end this loop
              shouldBreak = true;
            }
            else {
              packingStation = getPackingStation(pid, 1, locqty, 0);

              if (packingStation.contains("Error")) {
                returnv = -5521;
                // end this loop
                shouldBreak = true;
              }
              else qtyused += locqty;
            }
          }
          else {
            // only take what we need
            pid = insertPicking(locid, qtyneeded - qtyused, scn, "PICK READY", new AppModuleDBTransactionGetterImpl(this));

            if (pid < 1) {
              returnv = -553;
              // end this loop
              shouldBreak = true;
            }
            else {
              packingStation = getPackingStation(pid, 1, (qtyneeded - qtyused), 0);

              if (packingStation.contains("Error")) {
                returnv = -5531;
                // end this loop
                shouldBreak = true;
              }
              else qtyused += (qtyneeded - qtyused);
            }
          }
        }
        // end this loop
        else
          shouldBreak = true;

        if (shouldBreak) break;
      }

      if (qtyused < qtyneeded && returnv == 0) {
        int npid = insertPicking(null, (qtyneeded - qtyused), scn, "PICK REFUSED", new AppModuleDBTransactionGetterImpl(this));
        if (npid <= 0) returnv = -555;
        else returnv = 555;
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      returnv = -551;
    }

    return returnv;
  }

  /**
   * In some cases, a pick may fail during packing station assignment or issue creation.
   * In either case, rollback will not work, must delete from issue and issue_hist
   * table manually.  Deleting from issue table will delete child picking rows
   * automatically.
   */
  public void deletePickingIssueIssueHist(Object newSCN) {
    try {
      try (val pstmt = getDBTransaction().createPreparedStatement("delete from issue where scn=?", 0)) {
        pstmt.setObject(1, newSCN);
        pstmt.executeUpdate();
        getDBTransaction().commit();
      }

      try (val pstmt = getDBTransaction().createPreparedStatement("delete from issue_hist where scn=? and (created_date > sysdate -1)", 0)) {
        pstmt.setObject(1, newSCN);
        pstmt.executeUpdate();
        getDBTransaction().commit();
      }
    }
    catch (Exception e) {
      getDBTransaction().rollback();
      AdfLogUtility.logException(e);
    }
  }

  /**
   * Returns the priority designator group based on the issue's priority
   */
  private Integer getPriorityDesignatorGroup(int issuePriorityDesignator) {
    int priDesignatorGroup;
    if ((issuePriorityDesignator == 1) || (issuePriorityDesignator == 2) || (issuePriorityDesignator == 3) || (issuePriorityDesignator == 7) || (issuePriorityDesignator == 8)) {
      priDesignatorGroup = 1;
    }
    else if ((issuePriorityDesignator == 4) || (issuePriorityDesignator == 5) || (issuePriorityDesignator == 6) || (issuePriorityDesignator == 9) || (issuePriorityDesignator == 10)) {
      priDesignatorGroup = 2;
    }
    else {
      priDesignatorGroup = 3;
    }
    return priDesignatorGroup;
  }

  public Object findAndCreateCustomer(String aac, Object shipToAddress1, Object shipToAddress2, Object shipToAddress3, Object shipToAddress4, Object shipToAddressCity, Object shipToAddressState, Object shipToAddressZipCode, Object shipToAddressCountry) {
    Object customerid = null;
    try (PreparedStatement psCust = this.getDBTransaction().createPreparedStatement("select customer_id from customer where aac = ? and rownum =1 ", 0)) {
      psCust.setString(1, aac);
      try (ResultSet rsCust = psCust.executeQuery()) {

        // get the cust number
        if (rsCust.next()) customerid = rsCust.getObject(1);
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      customerid = null;
    }

    if (customerid == null) {
      try {
        CustomerViewImpl custView = getCustomerView1();
        Row nrow = custView.createRow();

        nrow.setAttribute("Aac", aac);
        nrow.setAttribute("Address1", (shipToAddress2 == null) ? "TBD" : shipToAddress2);
        nrow.setAttribute("Address2", shipToAddress3);

        nrow.setAttribute("City", shipToAddressCity);
        nrow.setAttribute("State", (shipToAddressCountry == null) ? shipToAddressState : shipToAddressCountry);
        nrow.setAttribute("ZipCode", shipToAddressZipCode);

        nrow.setAttribute("Name", (shipToAddress1 == null) ? "TBD" : shipToAddress1);
        nrow.setAttribute("RestrictShip", "N");

        custView.insertRow(nrow);
        this.getDBTransaction().commit();
        nrow.refresh(Row.REFRESH_CONTAINEES);

        // get the id
        customerid = nrow.getAttribute("CustomerId");
      }
      catch (Exception e) {
        AdfLogUtility.logException(e);
        customerid = null;
      }
    }
    return customerid;
  }

  //Checks if a WAC is a mechanized WAC
  public boolean isMechanized(String wacid) {
    boolean retVal = false;
    try {
      String sql = "select mechanized_flag from wac where wac.wac_id = ?";
      try (PreparedStatement pstmt = this.getDBTransaction().createPreparedStatement(sql, 0)) {
        pstmt.setString(1, wacid);
        try (ResultSet rs = pstmt.executeQuery()) {
          if (rs.next() && !(rs.getString(1).equalsIgnoreCase("N"))) retVal = true;
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return retVal;
  }

  /**
   * This method checks the signal code of the mats record.
   * If the signal code is J, K, M, or X, then the supplementary address
   * should be used as the customer to ship to.
   * Returns a customer id (or null if not found).
   */
  private Object checkSignalCode(String signalCode, Object customerId, Object suppAddr) {
    //* use supplementary address for customer
    if ((signalCode.equalsIgnoreCase("J")
        || signalCode.equalsIgnoreCase("K")
        || signalCode.equalsIgnoreCase("M")
        || signalCode.equalsIgnoreCase("X"))
        && !Util.isEmpty(suppAddr)) {

      String strSuppAddr = suppAddr.toString().toUpperCase().trim();
      if (!strSuppAddr.startsWith("Y") && strSuppAddr.length() == 6)
        customerId = findAndCreateCustomer(strSuppAddr, "TBD", "TBD", "TBD", "TBD", "TBD", "TB", "TBD", "TB");
    }
    return customerId;
  }

  /**
   * This method will lookup a niin and find any available locations for pick
   * if niinId is not null, then there are some available niin on hand
   */
  private Object lookupNiinLocation(Object niintext, Object cc) {
    Object niinId = null;
    // get the niin view
    NiinInfoMATSViewImpl niinview = getNiinInfoMATSView1();
    niinview.setNamedWhereClauseParam("CCSearch", cc);
    niinview.setNamedWhereClauseParam("NIINSearch", niintext);
    niinview.setRangeSize(-1);
    niinview.executeQuery();

    int niinlocs = niinview.getRowCount();
    if (niinlocs > 0) niinId = niinview.getRowAtRangeIndex(0).getAttribute("NiinId");

    return niinId;
  }

  public void reProcessMATSRow(int refMatsId) {
    try {
      String sql = "select ref_mats_id, document_number, " + "ship_to_address_1, ship_to_address_2, ship_to_address_3, ship_to_address_4, " + "issue_priority_designator, niin, fsc, " + "document_identifier, transaction_quantity, demand_suffix_code, " + "supplementary_address, fund_code, media_and_status_code, " + "routing_identifier_from, signal_code, required_delivery_date, unit_of_issue, " + "ship_to_address_city, ship_to_address_state, ship_to_address_zip_code,ship_to_address_country," + "condition_code, demil_code " + "from ref_mats where ref_mats_id = ?";
      try (PreparedStatement pstmt = this.getDBTransaction().createPreparedStatement(sql, 0)) {
        pstmt.setInt(1, refMatsId);
        try (ResultSet rs = pstmt.executeQuery()) {
          while (rs.next()) {
            boolean skip = false;
            try {

              Object documentNumber = rs.getObject(2);

              //* find the customer for this requisition, if not exist, then create
              String aac = documentNumber.toString().substring(0, 6);
              Object shipToAddress1 = rs.getObject(3);
              Object shipToAddress2 = rs.getObject(4);
              Object shipToAddress3 = rs.getObject(5);
              Object shipToAddress4 = rs.getObject(6);
              Object shipToAddressCity = rs.getObject(20);
              Object shipToAddressState = rs.getObject(21);
              Object shipToAddressZipCode = rs.getObject(22);
              Object shipToAddressCountry = rs.getObject(23);

              //* added 12/2/08 - if mats condition code (cc) not provided
              //* always default A5A to A
              //* BWA should also be for condition code A
              //* never default an A5J to F, create an exception
              Object cc = rs.getObject(24);
              Object docId = rs.getObject(10);
              Object demil = rs.getObject(25);
              if (Util.isEmpty(cc)) {
                cc = !Util.cleanString(docId).equalsIgnoreCase("A5J") ? "A" : "";
                if (Util.cleanString(docId).equalsIgnoreCase("A5J")) {
                  //* create an exception here - A5J missing condition code
                  createErrorQueueRecord("55", "ref_mats_id", String.valueOf(refMatsId), "1", null);
                  skip = true;
                }
              }

              if (!skip) {
                Object customerId = findAndCreateCustomer(aac, shipToAddress1, shipToAddress2, shipToAddress3, shipToAddress4, shipToAddressCity, shipToAddressState, shipToAddressZipCode, shipToAddressCountry);

                Object suppAddr = rs.getObject(13);
                Object signalCode = rs.getObject(17);
                //* added 8/27/08 to check signal code and assign customer id accurately
                if (!Util.isEmpty(signalCode)) {
                  customerId = checkSignalCode(signalCode.toString(), customerId, suppAddr);
                }

                if (customerId == null) {
                  //* customer not found and creation failed, error code -888
                  log.debug("return value is -888");
                }
                else {

                  //* compute the issue priority designator group based upon priority designator
                  int priDesignator;
                  try {
                    priDesignator = Integer.parseInt(rs.getString(7));
                  }
                  catch (Exception e) {
                    priDesignator = 1;
                  }
                  Integer priorityDesignatorGroup = getPriorityDesignatorGroup(priDesignator);

                  //* find the niin for this mats row, if not found in niin location, try niin_info,
                  //* if not found in niin_info, find in ref_mhif and create
                  Object niintext = rs.getObject(8);
                  Object niinId = lookupNiinLocation(niintext, cc);
                  boolean niinOnHand = true;

                  if (niinId == null) {
                    niinOnHand = false;
                    //* niin not found in niin location, try niin_info,
                    //* if not found in niin_info, find in ref_mhif and create
                    niinId = findAndCreateNiin(niintext);
                  }

                  Object transQty = rs.getObject(11);
                  Object demandSuffix = rs.getObject(12);
                  if (niinId != null) {

                    Object fundCode = rs.getObject(14);
                    Object mediaStatusCode = rs.getObject(15);
                    Object routingId = rs.getObject(16);
                    Object rdd = rs.getObject(18);

                    //* successfully found customer and niin, create the issue
                    //* added 12/11/08 - allow issue to use condition code F
                    Object newSCN = createIssue(docId, documentNumber, transQty, null, priDesignator, priorityDesignatorGroup, customerId, niinId, demandSuffix, suppAddr, fundCode, mediaStatusCode, routingId, null, signalCode, null, rdd, cc, demil, false);
                    if (newSCN != null) {
                      //* issue creation successful, create picks for mats
                      int returnv = createPicksForMATS(String.valueOf(newSCN), transQty, niintext, cc);

                      if (returnv < 0) {
                        //* some picks failed creation
                        log.debug("return value is  {}", returnv);
                        //error -555
                        //* undo insert picking, if one fails, all fail
                        deletePickingIssueIssueHist(newSCN);
                      }
                      else {
                        if (niinOnHand && returnv == 555) {
                          log.debug("return value is 555");
                          //* may be a partial refusal or full refusal
                        }
                      }
                    }
                    else {
                      log.debug("return value is 3");
                    }
                  }
                }
              }
            }
            catch (Exception e) {
              AdfLogUtility.logException(e);
            }
          }
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  //Redmine #45260
  //MCF 05/11/2015: Print in portrait alter to print portrait rather than landscape
  public String generatePrintAllSIDLabelUsingRcn(String sidStr, int prQty, int prDarkness, boolean isMobile) {
    val prnStr = new StringBuilder();
    log.debug("sidStr:  {}", sidStr);
    //Update 05-14-2014. Added STOW_HIST to printing because rewarehouses create a dummy SID for serialized stows which is deleted, so it needs to be retrieved.
    //Fix for ##24413: SID labels for Serial Rewarehouse.
    try (PreparedStatement pstmt = this.getDBTransaction().createPreparedStatement("select t.sid from stow t where t.status = 'STOW READY' " + " and t.rcn in (select s.rcn from stow s where s.sid = ? UNION select sh.rcn from stow_hist sh where sh.sid = ?)", 0)) {
      pstmt.setString(1, sidStr);
      pstmt.setString(2, sidStr);
      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          if (!isMobile) prnStr.append(this.generatePrintSIDLabel(rs.getString(1), prQty, prDarkness));
          else prnStr.append(this.generatePrintSIDLabelMobile(rs.getString(1), prQty, prDarkness));
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return prnStr.toString();
  }

  public String[] getAllSIDLabelUsingRcn(String sidStr) {
    ArrayList<String> sids = new ArrayList<>();
    try (PreparedStatement pstmt = this.getDBTransaction().createPreparedStatement("select t.sid from stow t where t.status = 'STOW READY' " + " and t.rcn in (select s.rcn from stow s where s.sid = ? UNION select sh.rcn from stow_hist sh where sh.sid = ?)", 0)) {
      pstmt.setString(1, sidStr);
      pstmt.setString(2, sidStr);
      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          sids.add(rs.getString(1));
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return sids.stream().toArray(String[]::new);
  }

  public String generatePrintSIDLabel(String sidStr, int prQty, int prDarkness) {
    try {
      // Generate the string for the SID label
      SiteInfoViewImpl siteview = getSiteInfoView1();
      siteview.executeQuery();
      Row si = siteview.first();
      String cityStr = (si.getAttribute("City") == null) ? "" : si.getAttribute("City").toString().trim();

      String prQtyL = "1";
      if (prQty > 1) prQtyL = String.valueOf(prQty);

      //Get the data for the SID label
      SIDLabelVOImpl svo = getSIDLabelVO1();
      svo.setNamedWhereClauseParam("sidStr", sidStr);
      svo.executeQuery();
      Row sro = svo.first();
      if (sro == null) return "";

      String mId = (sro.getAttribute("MechanizedFlag") == null) ? "" : sro.getAttribute("MechanizedFlag").toString().trim();
      if (!mId.equalsIgnoreCase("N")) mId = "MECH " + "(" + mId.trim() + ")";
      else mId = "NON MECH";

      String prnStr = "";
      String nl = "";
      String sDarkness = "0";
      if ((prDarkness > -30) && (prDarkness < 30)) sDarkness = String.valueOf(prDarkness);

      prnStr = prnStr + "^XA" + nl;
      prnStr = prnStr + "^PRC" + nl;
      prnStr = prnStr + "^LH0,0^FS" + nl;
      prnStr = prnStr + "^MD" + sDarkness + nl;
      prnStr = prnStr + "^MNW" + nl;
      prnStr = prnStr + "^LH0,0^FS" + nl;
      prnStr = prnStr + "^CWI,510S8_S2.FNT^FS" + nl;

      prnStr = prnStr + "^FO561,64^AIR,31,0^CI0^FR^FDSTRATIS ";
      prnStr = prnStr + mId + "   "; //Assign Mech or non Mech string
      prnStr = prnStr + "STOW:  " + cityStr + "^FS" + nl; // Assign the camp/city

      prnStr = prnStr + "^FO528,64^AIR,31,0^CI0^FR^FDLOCATION:  " + sro.getAttribute("LocationLabel").toString().trim() + "^FS" + nl;

      prnStr = prnStr + "^FO496,64^AIR,31,0^CI0^FR^FDFSC/NIIN:  " + sro.getAttribute("Fsc").toString().trim() + sro.getAttribute("Niin").toString().trim();
      prnStr = prnStr + "   U/I:  " + ((sro.getAttribute("Ui") == null) ? " " : sro.getAttribute("Ui").toString().trim());
      prnStr = prnStr + "   CC : " + ((sro.getAttribute("Cc") == null) ? " " : sro.getAttribute("Cc").toString().trim()) + "^FS" + nl;

      prnStr = prnStr + "^FO463,64^AIR,31,0^CI0^FR^FDDOC NO:  " + sro.getAttribute("DocumentNumber").toString().trim() + ((sro.getAttribute("Suffix") == null) ? "" : sro.getAttribute("Suffix").toString().trim());
      prnStr = prnStr + "    RCN:  " + sro.getAttribute("Rcn").toString().trim() + "^FS" + nl;

      prnStr = prnStr + "^FO430,64^AIR,31,0^CI0^FR^FDSID: " + sro.getAttribute("Sid").toString().trim();
      prnStr = prnStr + " SID QTY:  " + sro.getAttribute("QtyStowed").toString().trim();
      prnStr = prnStr + " EXP: " + ((sro.getAttribute("ExpirationDate") == null) ? "" : sro.getAttribute("ExpirationDate").toString().trim()) + "^FS" + nl;

      prnStr = prnStr + "^FO398,64^AIR,31,0^CI0^FR^FDLOT: " + ((sro.getAttribute("LotConNum") == null) ? "" : sro.getAttribute("LotConNum").toString().trim());
      prnStr = prnStr + " CASE QTY: " + ((sro.getAttribute("CaseWeightQty") == null) ? "" : sro.getAttribute("CaseWeightQty").toString().trim());
      prnStr = prnStr + " SERIAL NUM:" + ((sro.getAttribute("SerialNumber") == null) ? "" : sro.getAttribute("SerialNumber").toString().trim()) + "^FS" + nl;

      prnStr = prnStr + "^FO365,64^AIR,31,0^CI0^FR^FDUSER NAME:  " + ((sro.getAttribute("Username") == null) ? " " : sro.getAttribute("Username").toString().trim());
      prnStr = prnStr + " DATE/TIME:  " + sro.getAttribute("SidDate").toString().trim() + "^FS" + nl;

      prnStr = prnStr + "^FO332,64^AIR,31,0^CI0^FR^FDDESC:  " + sro.getAttribute("Nomenclature").toString().trim() + "^FS" + nl;
      prnStr = prnStr + "^CWJ,E20S8_S2.FNT^FS" + nl;
      prnStr = prnStr + "^FO150,712^AJR,61,0^CI0^FR^FDCC:" + ((sro.getAttribute("Cc") == null) ? "" : sro.getAttribute("Cc").toString().trim()) + "^FS" + nl;
      prnStr = prnStr + "^BY2,3.0^FO80,250^B3R,N,202,Y,N^FR^FD" + sro.getAttribute("Sid").toString().trim() + "^FS" + nl;
      prnStr = prnStr + "^CWJ,E20S8_S2.FNT^FS" + nl;
      prnStr = prnStr + "^FO150,61^AJR,61,0^CI0^FR^FD" + sro.getAttribute("WacNumber").toString().trim() + "^FS" + nl;
      prnStr = prnStr + "^PQ" + prQtyL + ",0,0,N" + nl;
      prnStr = prnStr + "^XZ" + nl;
      return prnStr;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return "";
  }

  //Redmine #45260
  //05/11/2015: MCF: add new portrait printing for mobile printers. Mobile printers can't print in landscape without smearing the barcode.
  public String generatePrintSIDLabelMobile(String sidStr, int prQty, int prDarkness) {

    try {
      // Generate the string for the SID label
      SiteInfoViewImpl siteview = getSiteInfoView1();
      siteview.executeQuery();
      Row si = siteview.first();
      String cityStr = (si.getAttribute("City") == null) ? "" : si.getAttribute("City").toString().trim();

      String prQtyL = "1";
      if (prQty > 1)
        prQtyL = String.valueOf(prQty);

      //Get the data for the SID label
      SIDLabelVOImpl svo = getSIDLabelVO1();
      svo.setNamedWhereClauseParam("sidStr", sidStr);
      svo.executeQuery();
      Row sro = svo.first();
      if (sro == null) return "";

      String mId = (sro.getAttribute("MechanizedFlag") == null) ? "" : sro.getAttribute("MechanizedFlag").toString().trim();
      if (!mId.equalsIgnoreCase("N")) mId = "MECH " + "(" + mId.trim() + ")";
      else mId = "NON MECH";

      String prnStr = "";
      String nl = "";
      String sDarkness = "0";
      if ((prDarkness > -30) && (prDarkness < 30)) sDarkness = String.valueOf(prDarkness);

      prnStr = prnStr + "^XA" + nl;
      prnStr = prnStr + "^PRC" + nl;
      prnStr = prnStr + "^LH0,0^FS" + nl;
      prnStr = prnStr + "^MD" + sDarkness + nl;
      prnStr = prnStr + "^MNW" + nl;
      prnStr = prnStr + "^LH0,0^FS" + nl;
      prnStr = prnStr + "^CWI,510S8_S2.FNT^FS" + nl;

      prnStr = prnStr + "^FO60,160^AIN,31,0^CI0^FR^FDSTRATIS ";
      prnStr = prnStr + mId + "^FS" + nl; //Assign Mech or non Mech string
      prnStr = prnStr + "^FO60,192^AIN,31,0^CI0^FR^FDSTOW:  " + cityStr + "^FS" + nl; // Assign the camp/city

      prnStr = prnStr + "^FO60,224^AIN,31,0^CI0^FR^FDLOCATION:  " + sro.getAttribute("LocationLabel").toString().trim() + "^FS" + nl;

      prnStr = prnStr + "^FO60,256^AIN,31,0^CI0^FR^FDFSC/NIIN:  " + sro.getAttribute("Fsc").toString().trim() + sro.getAttribute("Niin").toString().trim() + "^FS" + nl;
      prnStr = prnStr + "^FO60,288^AIN,31,0^CI0^FR^FDU/I:  " + ((sro.getAttribute("Ui") == null) ? " " : sro.getAttribute("Ui").toString().trim());
      prnStr = prnStr + "   CC : " + ((sro.getAttribute("Cc") == null) ? " " : sro.getAttribute("Cc").toString().trim()) + "^FS" + nl;

      prnStr = prnStr + "^FO60,320^AIN,31,0^CI0^FR^FDDOC NO:  " + sro.getAttribute("DocumentNumber").toString().trim() + ((sro.getAttribute("Suffix") == null) ? "" : sro.getAttribute("Suffix").toString().trim());
      prnStr = prnStr + "    RCN:  " + sro.getAttribute("Rcn").toString().trim() + "^FS" + nl;

      prnStr = prnStr + "^FO60,352^AIN,31,0^CI0^FR^FDSID: " + sro.getAttribute("Sid").toString().trim();
      prnStr = prnStr + " SID QTY:  " + sro.getAttribute("QtyStowed").toString().trim() + "^FS" + nl;
      prnStr = prnStr + "^FO60,384^AIN,31,0^CI0^FR^FDEXP: " + ((sro.getAttribute("ExpirationDate") == null) ? "" : sro.getAttribute("ExpirationDate").toString().trim()) + "^FS" + nl;

      prnStr = prnStr + "^FO60,416^AIN,31,0^CI0^FR^FDLOT: " + ((sro.getAttribute("LotConNum") == null) ? "" : sro.getAttribute("LotConNum").toString().trim());
      prnStr = prnStr + " CASE QTY: " + ((sro.getAttribute("CaseWeightQty") == null) ? "" : sro.getAttribute("CaseWeightQty").toString().trim()) + "^FS" + nl;
      prnStr = prnStr + "^FO60,448^AIN,31,0^CI0^FR^FDSERIAL NUM:" + ((sro.getAttribute("SerialNumber") == null) ? "" : sro.getAttribute("SerialNumber").toString().trim()) + "^FS" + nl;

      prnStr = prnStr + "^FO60,480^AIN,31,0^CI0^FR^FDUSER NAME:  " + ((sro.getAttribute("Username") == null) ? " " : sro.getAttribute("Username").toString().trim()) + "^FS" + nl;
      prnStr = prnStr + "^FO60,512^AIN,31,0^CI0^FR^FDDATE/TIME:  " + sro.getAttribute("SidDate").toString().trim() + "^FS" + nl;

      prnStr = prnStr + "^FO60,544^AIN,31,0^CI0^FR^FDDESC:  " + sro.getAttribute("Nomenclature").toString().trim() + "^FS" + nl;
      prnStr = prnStr + "^CWJ,E20S8_S2.FNT^FS^FO120,600^AJN,61,0^CI0^FR^FD" + sro.getAttribute("WacNumber").toString().trim() + "     " + "CC:" + ((sro.getAttribute("Cc") == null) ? "" : sro.getAttribute("Cc").toString().trim()) + "^FS" + nl;
      prnStr = prnStr + "^FO180,750^B3N,N,200,Y,N^FD" + sro.getAttribute("Sid").toString().trim() + "^FS" + nl;
      prnStr = prnStr + "^PQ" + prQtyL + ",0,0,N" + nl;
      prnStr = prnStr + "^XZ" + nl;
      return prnStr;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return "";
  }

  public String generatePrintPINLabel(String pinStr, int prQty,
                                      int prDarkness) {
    try {
      // Generate the string for the PIN label
      SiteInfoViewImpl siteview = getSiteInfoView1();
      siteview.executeQuery();
      Row si = siteview.first();
      String cityStr = "";
      if (si.getAttribute("City") != null) cityStr = si.getAttribute("City").toString().trim();

      String prQtyL = "1";
      if (prQty > 1)
        prQtyL = String.valueOf(prQty);

      //Get the data for the PIN label
      PINLabelVOImpl pvo = getPINLabelVO1();
      pvo.setNamedWhereClauseParam("pinStr", pinStr);
      pvo.executeQuery();
      Row sro = pvo.first();
      if (sro == null) return "";

      String mId = sro.getAttribute("MechanizedFlag").toString();
      if (!mId.equalsIgnoreCase("N")) mId = "MECH " + "(" + mId.trim() + ")";
      else mId = "NON MECH";

      String prnStr = "";
      String nl = "";
      String sDarkness = "0";
      if ((prDarkness > -30) && (prDarkness < 30))
        sDarkness = String.valueOf(prDarkness);

      prnStr = prnStr + "^XA" + nl;
      prnStr = prnStr + "^PRC" + nl;
      prnStr = prnStr + "^LH0,0^FS" + nl;
      prnStr = prnStr + "^MD" + sDarkness + nl;
      prnStr = prnStr + "^MNW" + nl;
      prnStr = prnStr + "^LH0,0^FS" + nl;
      prnStr = prnStr + "^CWI,510S8_S2.FNT^FS" + nl;

      prnStr = prnStr + "^FO549,59^AIR,31,0^CI0^FR^FDSTRATIS ";
      prnStr = prnStr + mId + "   "; //Assign Mech or non Mech string
      prnStr = prnStr + "ISSUE:  " + cityStr + "^FS" + nl; // Assign the camp/city

      prnStr = prnStr + "^FO516,59^AIR,31,0^CI0^FR^FDPACKING STATION:  " + sro.getAttribute("WorkStationName").toString().trim() + "^FS" + nl;

      prnStr = prnStr + "^FO484,59^AIR,31,0^CI0^FR^FDFSC/NIIN:  " + sro.getAttribute("Fsc").toString().trim() + sro.getAttribute("Niin").toString().trim();
      prnStr = prnStr + "   CC:  " + sro.getAttribute("Cc").toString().trim() + "^FS" + nl;

      prnStr = prnStr + "^FO451,59^AIR,31,0^CI0^FR^FDDOC NO:  " + sro.getAttribute("DocumentNumber").toString().trim() + ((sro.getAttribute("Suffix") == null) ? "" : sro.getAttribute("Suffix").toString().trim());
      prnStr = prnStr + "    SCN:  " + sro.getAttribute("Scn").toString().trim() + "^FS" + nl;

      prnStr = prnStr + "^FO418,59^AIR,31,0^CI0^FR^FDPIN:  " + sro.getAttribute("Pin").toString().trim();
      prnStr = prnStr + "    PIN QTY:  " + sro.getAttribute("QtyPicked").toString().trim() + "^FS" + nl;

      prnStr = prnStr + "^FO386,59^AIR,31,0^CI0^FR^FDUSER NAME:  " + sro.getAttribute("Username").toString().trim();
      prnStr = prnStr + "    DATE/TIME:  " + sro.getAttribute("PinDate").toString().trim() + "^FS" + nl;

      prnStr = prnStr + "^FO353,59^AIR,31,0^CI0^FR^FDDESC:  " + sro.getAttribute("Nomenclature").toString().trim() + "^FS" + nl;

      prnStr = prnStr + "^BY2,3.0^FO80,273^B3R,N,202,Y,N^FR^FD" + sro.getAttribute("Pin").toString().trim() + "^FS" + nl;

      prnStr = prnStr + "^PQ" + prQtyL + ",0,0,N" + nl;
      prnStr = prnStr + "^XZ" + nl;
      return prnStr;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return "";
  }

  //Redmine #45260
  //05/11/2015: MCF: add new portrait printing for mobile printers. Mobile printers can't print in landscape without smearing the barcode.
  public String generatePrintPINLabelMobile(String pinStr, int prQty,
                                            int prDarkness) {
    try {
      // Generate the string for the PIN label
      SiteInfoViewImpl siteview = getSiteInfoView1();
      siteview.executeQuery();
      Row si = siteview.first();
      String cityStr = "";
      if (si.getAttribute("City") != null) cityStr = si.getAttribute("City").toString().trim();

      String prQtyL = "1";
      if (prQty > 1) prQtyL = String.valueOf(prQty);

      //Get the data for the PIN label
      PINLabelVOImpl pvo = getPINLabelVO1();
      pvo.setNamedWhereClauseParam("pinStr", pinStr);
      pvo.executeQuery();
      Row sro = pvo.first();
      if (sro == null) return "";

      String mId = sro.getAttribute("MechanizedFlag").toString();
      if (!mId.equalsIgnoreCase("N")) mId = "MECH " + "(" + mId.trim() + ")";
      else mId = "NON MECH";

      String prnStr = "";
      String nl = "";
      String sDarkness = "0";
      if ((prDarkness > -30) && (prDarkness < 30)) sDarkness = String.valueOf(prDarkness);

      prnStr = prnStr + "^XA" + nl;
      prnStr = prnStr + "^PRC" + nl;
      prnStr = prnStr + "^LH0,0^FS" + nl;
      prnStr = prnStr + "^MD" + sDarkness + nl;
      prnStr = prnStr + "^MNW" + nl;
      prnStr = prnStr + "^LH0,0^FS" + nl;
      prnStr = prnStr + "^CWI,510S8_S2.FNT^FS" + nl;

      prnStr = prnStr + "^FO60,160^AIN,31,0^CI0^FR^FDSTRATIS ";
      prnStr = prnStr + mId + "^FS" + nl; //Assign Mech or non Mech string
      prnStr = prnStr + "^FO60,192^AIN,31,0^CI0^FR^FDISSUE:  " + cityStr + "^FS" + nl; // Assign the camp/city

      prnStr = prnStr + "^FO60,224^AIN,31,0^CI0^FR^FDPACKING STATION:  " + sro.getAttribute("WorkStationName").toString().trim() + "^FS" + nl;

      prnStr = prnStr + "^FO60,256^AIN,31,0^CI0^FR^FDFSC/NIIN:  " + sro.getAttribute("Fsc").toString().trim() + sro.getAttribute("Niin").toString().trim() + "^FS" + nl;
      prnStr = prnStr + "^FO60,288^AIN,31,0^CI0^FR^FDCC:  " + sro.getAttribute("Cc").toString().trim() + "^FS" + nl;

      prnStr = prnStr + "^FO60,320^AIN,31,0^CI0^FR^FDDOC NO:  " + sro.getAttribute("DocumentNumber").toString().trim() + ((sro.getAttribute("Suffix") == null) ? "" : sro.getAttribute("Suffix").toString().trim()) + "^FS" + nl;
      prnStr = prnStr + "^FO60,352^AIN,31,0^CI0^FR^FDSCN:  " + sro.getAttribute("Scn").toString().trim() + "^FS" + nl;

      prnStr = prnStr + "^FO60,384^AIN,31,0^CI0^FR^FDPIN:  " + sro.getAttribute("Pin").toString().trim();
      prnStr = prnStr + "    PIN QTY:  " + sro.getAttribute("QtyPicked").toString().trim() + "^FS" + nl;

      prnStr = prnStr + "^FO60,416^AIN,31,0^CI0^FR^FDUSER NAME:  " + sro.getAttribute("Username").toString().trim() + "^FS" + nl;
      prnStr = prnStr + "^FO60,448^AIN,31,0^CI0^FR^FDDATE/TIME:  " + sro.getAttribute("PinDate").toString().trim() + "^FS" + nl;

      prnStr = prnStr + "^FO60,480^AIN,31,0^CI0^FR^FDDESC:  " + sro.getAttribute("Nomenclature").toString().trim() + "^FS" + nl;

      prnStr = prnStr + "^FO180,640^B3N,N,200,Y,N^FD^FD" + sro.getAttribute("Pin").toString().trim() + "^FS" + nl;

      prnStr = prnStr + "^PQ" + prQtyL + ",0,0,N" + nl;
      prnStr = prnStr + "^XZ" + nl;
      return prnStr;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return "";
  }

  public String generatePrintFSCNIINLabel(String fscNiinStr, int prQty, int prDarkness, boolean nsn) {
    try {
      boolean flag = false;
      final String strSQL;
      if (nsn) {
        strSQL = "select nf.niin_id, nf.fsc, nf.niin from niin_info nf where (concat(nf.fsc,nf.niin) = ?)";
      }
      else {
        strSQL = "select nf.niin_id, nf.fsc, nf.niin from niin_info nf where nf.niin = ?";
      }

      try (val stR = getDBTransaction().createPreparedStatement(strSQL, 0)) {
        stR.setString(1, fscNiinStr.trim());
        try (ResultSet rset = stR.executeQuery()) {
          if (rset.next()) flag = true;
        }
      }
      if (!flag) return "";

      String prQtyL = "1";
      if (prQty > 1) prQtyL = String.valueOf(prQty);

      String prnStr = "";
      String nl = "";
      String sDarkness = "0";
      if ((prDarkness > -30) && (prDarkness < 30)) sDarkness = String.valueOf(prDarkness);

      prnStr = prnStr + "^XA" + nl;
      prnStr = prnStr + "^PRC" + nl;
      prnStr = prnStr + "^LH0,0^FS" + nl;
      prnStr = prnStr + "^MD" + sDarkness + nl;

      prnStr = prnStr + "^MNW" + nl;
      prnStr = prnStr + "^LH0,0^FS" + nl;
      prnStr = prnStr + "^CWI,U00D81_8.FNT^FS" + nl;
      prnStr = prnStr + "^FO364,5^AIN,21,0^CI0^FR^FDFSC/NIIN^FS" + nl;
      prnStr = prnStr + "^CWJ,510N04UX.FNT^FS" + nl;
      prnStr = prnStr + "^FO306,163^AJN,29,0^CI0^FR^FD" + fscNiinStr.trim() + "^FS" + nl;
      prnStr = prnStr + "^BY2,3.0^FO167,40^B3N,N,109,N,Y^FR^FD" + fscNiinStr.trim() + "^FS" + nl;

      prnStr = prnStr + "^PQ" + prQtyL + ",0,0,N" + nl;
      prnStr = prnStr + "^XZ" + nl;
      return prnStr;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return "";
  }

  public String generatePrintLocationLabel(String locStr, int prQty,
                                           int prDarkness) {
    try {
      boolean flag = false;
      try (PreparedStatement stR = getDBTransaction().createPreparedStatement("select l.location_id from location l where (l.location_label = ?)", 0)) {
        stR.setString(1, locStr.toUpperCase().trim());
        try (ResultSet rset = stR.executeQuery()) {
          if (rset.next()) flag = true;
        }
      }

      if (!flag) return "";

      String prQtyL = "1";
      if (prQty > 1) prQtyL = String.valueOf(prQty);

      String prnStr = "";
      String nl = "";
      String sDarkness = "0";
      if ((prDarkness > -30) && (prDarkness < 30)) sDarkness = String.valueOf(prDarkness);

      val locUpper = locStr.toUpperCase().trim();

      prnStr = prnStr + "^XA" + nl;
      prnStr = prnStr + "^PRC" + nl;
      prnStr = prnStr + "^LH0,0^FS" + nl;
      prnStr = prnStr + "^MD" + sDarkness + nl;

      prnStr = prnStr + "^MNW" + nl;
      prnStr = prnStr + "^LH0,0^FS" + nl;
      prnStr = prnStr + "^CWI,U00D81_8.FNT^FS" + nl;
      prnStr = prnStr + "^FO355,5^AIN,21,0^CI0^FR^FDLOCATION^FS" + nl;
      prnStr = prnStr + "^CWJ,A10N04UX.FNT^FS" + nl;
      prnStr = prnStr + "^FO319,140^AJN,32,0^CI0^FR^FD" + locUpper + "^FS" + nl;
      prnStr = prnStr + "^BY2,3.0^FO231,38^B3N,N,90,N,Y^FR^FD" + locUpper + "^FS" + nl;

      prnStr = prnStr + "^PQ" + prQtyL + ",0,0,N" + nl;
      prnStr = prnStr + "^XZ" + nl;
      return prnStr;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return "";
  }

  public String getWACforSID(String sid) {
    String sql = "select w.wac_number from stow s inner join location l on s.location_ID = l.location_ID inner join wac w on w.wac_id = l.wac_id where s.sid = ?";
    String wac = "";
    try (PreparedStatement stR = getDBTransaction().createPreparedStatement(sql, 0)) {
      stR.setString(1, sid);
      try (ResultSet rset = stR.executeQuery()) {
        if (rset.next()) wac = rset.getString(1);
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return wac;
  }

  /**
   * This method lists all the SIDs used for Re-Print based on given document number.
   */
  public String genrateStowListForDocNum(String docNumStr) {
    try {
      StringBuilder errLs = new StringBuilder();
      if (docNumStr == null || docNumStr.length() < 14) return "PRINT_NODOCNUM_ERR";

      try (PreparedStatement stR = getDBTransaction().createPreparedStatement("select s.sid from stow s, receipt r where (s.status = 'STOW READY' or s.status='STOWED') and r.rcn = s.rcn and concat(r.document_number,nvl(r.suffix,'')) = ? order by sid asc", 0)) {
        stR.setString(1, docNumStr);
        try (ResultSet rset = stR.executeQuery()) {
          while (rset.next()) {
            if (errLs.length() <= 0) errLs = new StringBuilder("SIDLIST,").append(rset.getString(1));
            else errLs.append(',').append(rset.getString(1));
          }
        }
      }
      if (errLs.length() <= 0) return "PRINT_NOSIDS_ERR";
      else return errLs.toString();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }

    return "";
  }

  public String getSCNQuantitiesForReprintBarcode(String barcode) {
    StringBuilder str = new StringBuilder();
    try {
      String sqlIssue = "select scn from issue where packing_consolidation_id in (select packing_consolidation_id from packing_consolidation where UPPER(consolidation_barcode)=?)";
      try (PreparedStatement pstmtIssue = getDBTransaction().createPreparedStatement(sqlIssue, 0)) {
        pstmtIssue.setString(1, barcode);
        try (ResultSet rsIssue = pstmtIssue.executeQuery()) {
          while (rsIssue.next()) {
            String scn = rsIssue.getString(1);
            if (scn != null) {
              if (!StringUtils.isEmpty(str)) str.append(',');
              str.append(getSCNQuantitiesForReprint(scn));
            }
          }
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return str.toString();
  }

  public String getSCNQuantitiesForReprintBarcodeHist(String barcode) {
    StringBuilder str = new StringBuilder();
    try {
      String sqlIssue = "select scn, document_number from issue_hist where packing_consolidation_id in (select packing_consolidation_id from packing_consolidation_hist where UPPER(consolidation_barcode)=?)";
      try (PreparedStatement pstmtIssue = getDBTransaction().createPreparedStatement(sqlIssue, 0)) {
        pstmtIssue.setString(1, barcode);
        try (ResultSet rsIssue = pstmtIssue.executeQuery()) {
          while (rsIssue.next()) {
            String scn = rsIssue.getString(1);
            String documentNumber = rsIssue.getString(2);
            if (scn != null) {
              if (!StringUtils.isEmpty(str)) str.append(',');
              str.append(getSCNQuantitiesForReprintHist(scn, documentNumber));
            }
          }
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return str.toString();
  }

  public String getSCNQuantitiesForReprintDocumentNumberHist(String documentNumber) {
    StringBuilder str = new StringBuilder();
    try {
      String sqlIssue = "select scn from issue_hist where status in ('PICKED', 'PACKED', 'SHIPPED', 'SHIPPING') and concat(document_number,nvl(suffix,'')) like ?";
      try (PreparedStatement pstmtIssue = getDBTransaction().createPreparedStatement(sqlIssue, 0)) {
        pstmtIssue.setString(1, documentNumber.trim() + "%");
        try (ResultSet rsIssue = pstmtIssue.executeQuery()) {
          while (rsIssue.next()) {
            String scn = rsIssue.getString(1);
            if (scn != null) {
              if (!StringUtils.isEmpty(str)) str.append(',');
              str.append(getSCNQuantitiesForReprintHist(scn, documentNumber));
            }
          }
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return str.toString();
  }

  public String getSCNQuantitiesForReprintDocumentNumber(String documentNumber) {
    StringBuilder str = new StringBuilder();
    try {
      String sqlIssue = "select scn from issue where  status in ('PICKED', 'PACKED', 'SHIPPED', 'SHIPPING') and concat(document_number,nvl(suffix,'')) like ?";
      try (PreparedStatement pstmtIssue = getDBTransaction().createPreparedStatement(sqlIssue, 0)) {
        pstmtIssue.setString(1, documentNumber.trim() + "%");
        try (ResultSet rsIssue = pstmtIssue.executeQuery()) {
          while (rsIssue.next()) {
            String scn = rsIssue.getString(1);
            if (scn != null) {
              if (!StringUtils.isEmpty(str)) str.append(',');
              str.append(getSCNQuantitiesForReprint(scn));
            }
          }
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return str.toString();
  }

  /**
   * Used by 1348 Reprint only
   */
  public String getSCNQuantitiesForReprint(String scn) {
    String str = "";
    try {

      //Changed SQL statement so it sums up the total quantities picked rather than just use the issue quantity
      //This will take into account any partial refusals that were issued - 6-12-08 JPO
      String sqlIssue = "select issue.issue_type, sum(picking.qty_picked) from issue left join picking Picking on issue.scn = picking.scn where issue.scn = ? group by issue.issue_type";
      try (PreparedStatement pstmtIssue = getDBTransaction().createPreparedStatement(sqlIssue, 0)) {
        pstmtIssue.setString(1, scn);
        try (ResultSet rsIssue = pstmtIssue.executeQuery()) {
          if (rsIssue.next()) {
            String issueType = rsIssue.getString(1);
            if (issueType != null && (issueType.equals("T") || issueType.equals("B"))) str += getQuantityForReprint(scn);
            if (Util.isEmpty(str)) str += scn + "," + rsIssue.getString(2);
          }
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return str;
  }

  /**
   * Used by 1348 Reprint only
   */
  public String getSCNQuantitiesForReprintHist(String scn, String documentNumber) {
    String str = "";
    try {

      //Changed SQL statement so it sums up the total quantities picked rather than just use the issue quantity
      //This will take into account any partial refusals that were issued - 6-12-08 JPO
      String sqlIssue = "select i.issue_type, sum(p.qty_picked) from issue_hist i, picking_hist p where i.scn=p.scn " +
          "and to_char(i.created_date ,'YYYY') = to_char(p.created_date,'YYYY') " +
          "and i.scn = ? and i.document_number=? group by i.issue_type";
      try (PreparedStatement pstmtIssue = getDBTransaction().createPreparedStatement(sqlIssue, 0)) {
        pstmtIssue.setString(1, scn);
        pstmtIssue.setString(2, documentNumber);
        try (ResultSet rsIssue = pstmtIssue.executeQuery()) {
          if (rsIssue.next()) {
            String issueType = rsIssue.getString(1);
            if (issueType != null && (issueType.equals("T") || issueType.equals("B"))) str += getQuantityForReprint(scn);
            if (Util.isEmpty(str)) str += scn + "," + documentNumber + "," + rsIssue.getString(2);
          }
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return str;
  }

  /**
   * Used by 1348 Reprint only
   */
  private String getQuantityForReprint(String scn) {
    StringBuilder str = new StringBuilder();
    try {
      String sql = "select qty from receipt_issue where scn=?";
      try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(sql, 0)) {
        pstmt.setString(1, scn);
        try (ResultSet rs = pstmt.executeQuery()) {
          while (rs.next()) {
            String foundQty = rs.getString(1);
            if (foundQty != null) {
              if (!StringUtils.isEmpty(str))
                str.append(',');
              str.append(scn).append(',').append(foundQty);
            }
          }
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return str.toString();
  }

  public String findBarcodeForPIN(String pin) {
    String str = "";
    try {
      String sql = "select nvl(consolidation_barcode,'') from packing_consolidation pc, picking p where p.packing_consolidation_id=pc.packing_consolidation_id and p.pin=?";
      try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(sql, 0)) {
        pstmt.setString(1, pin);
        try (ResultSet rs = pstmt.executeQuery()) {
          if (rs.next()) str = rs.getString(1);
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return str;
  }

  public boolean existSCNForReprint(String scn) {
    String sql = "select scn from issue " + "where status in ('PICKED', 'PACKED', 'SHIPPED', 'SHIPPING') and scn=?";
    return existForReprint(sql, scn);
  }

  public boolean existSCNForReprintBarcode(String barcode) {
    String sql = "select scn from issue " + "where status in ('PICKED', 'PACKED', 'SHIPPED', 'SHIPPING') and packing_consolidation_id in (select packing_consolidation_id from packing_consolidation where consolidation_barcode=?)";
    return existForReprint(sql, barcode);
  }

  public boolean existSCNForReprintBarcodeHist(String barcode) {
    String sql = "select scn from issue_hist " + "where status in ('PICKED', 'PACKED', 'SHIPPED', 'SHIPPING') and packing_consolidation_id in (select packing_consolidation_id from packing_consolidation_hist where consolidation_barcode=?)";
    return existForReprint(sql, barcode);
  }

  public boolean existSCNForReprintDocumentNumber(String documentNumber) {
    String sql = "select scn from issue " + "where status in ('PICKED', 'PACKED', 'SHIPPED', 'SHIPPING') and document_number=?";
    return existForReprint(sql, documentNumber);
  }

  public boolean existSCNForReprintDocumentNumberHist(String documentNumber) {
    String sql = "select scn from issue_hist " + "where status in ('PICKED', 'PACKED', 'SHIPPED', 'SHIPPING') and document_number=?";
    return existForReprint(sql, documentNumber);
  }

  public boolean existBarcodeForReprint(String manifest) {
    String sql = "select consolidation_barcode from packing_consolidation " + "where UPPER(consolidation_barcode)=?";
    return existForReprint(sql, manifest);
  }

  public boolean existManifestForReprint(String manifest) {
    String sql = "select manifest from shipping_manifest " + "where manifest=? and manifest_date is not null";
    boolean exist = existForReprint(sql, manifest);
    if (!exist) {
      sql = "select manifest from shipping_manifest_hist " + "where manifest=? and manifest_date is not null";
    }
    return existForReprint(sql, manifest);
  }

  public boolean existAACForReprint(String aac) {
    String sql = "select aac from customer " + "where aac=?";
    return existForReprint(sql, aac);
  }

  public boolean existFloorForReprint(String floor) {
    String sql = "select floor_location from floor_location " + "where floor_location=?";
    return existForReprint(sql, floor);
  }

  private boolean existForReprint(String sql, String selection) {
    boolean exist = false;
    try {
      try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(sql, 0)) {
        pstmt.setString(1, selection);
        try (ResultSet rs = pstmt.executeQuery()) {
          if (rs.next()) {
            String found = rs.getString(1);
            if (found != null) exist = true;
          }
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return exist;
  }

  /**
   * Update Location, Location Header, Location header bin weight and cubde
   */
  public void updateLocationWeightAndCubeDetails(long niinId, long locId, int subQty, int addQty, String uid) {
    // Update LOCATION, LOCATION_HEADER, LOCATION_HEADER_BIN
    try {
      // Get Unit Weight and Cube from NIIN_INFO, LOCATION_HEADER, LOCATION_HEADER_BIN
      NiinAndLocationWeightAndCubeByLocIdVOImpl nllvo = this.getNiinAndLocationWeightAndCubeByLocIdVO1();
      nllvo.setNamedWhereClauseParam("locIdStr", locId);
      nllvo.setNamedWhereClauseParam("niinIdStr", String.valueOf(niinId));
      nllvo.executeQuery();
      Row nllro = nllvo.first();

      if ((nllro != null) && (!nllro.getAttribute("MechanizedFlag").toString().equalsIgnoreCase("N"))) { // Take care of Cube and weights for only mechanized

        double lhbwt = //subtract the amout we thought we could stow
            Double.parseDouble(nllro.getAttribute("Lhbweight").toString()) - (Double.parseDouble(nllro.getAttribute("Weight").toString()) * subQty) + (Double.parseDouble(nllro.getAttribute("Weight").toString()) * addQty); // add the actual we stowed
        if (lhbwt < 0) lhbwt = 0;
        double lhbc = Double.parseDouble(nllro.getAttribute("Lhbcube").toString()) - (Double.parseDouble(nllro.getAttribute("Cube").toString()) * subQty) + (Double.parseDouble(nllro.getAttribute("Cube").toString()) * addQty);
        if (lhbc < 0) lhbc = 0;
        double lhwt = Double.parseDouble(nllro.getAttribute("Lhweight").toString()) - (Double.parseDouble(nllro.getAttribute("Weight").toString()) * subQty) + (Double.parseDouble(nllro.getAttribute("Weight").toString()) * addQty);
        // Update LOCATION weight and Cube

        try (PreparedStatement stR = getDBTransaction().createPreparedStatement("begin update location set  weight = weight + ?, cube = cube + ?, modified_by = ? where location_id = ?; end;", 0)) {
          stR.setDouble(1, (Double.parseDouble(nllro.getAttribute("Weight").toString()) * addQty) - (Double.parseDouble(nllro.getAttribute("Weight").toString()) * subQty)); //subtract the amout we thought we could stow
          stR.setDouble(2, (Double.parseDouble(nllro.getAttribute("Cube").toString()) * addQty) - (Double.parseDouble(nllro.getAttribute("Cube").toString()) * subQty));
          stR.setInt(3, Integer.parseInt(uid));
          stR.setLong(4, locId);
          stR.executeUpdate();
          getDBTransaction().commit();
        } // add the actual we stowed
        // Update LOCATION_HEADER_BIN weight and Cube
        try (PreparedStatement stR = getDBTransaction().createPreparedStatement("begin update location_header_bin set  weight = ?, cube = ?, modified_by = ? where location_header_bin_id = ?; end;", 0)) {
          stR.setDouble(1, lhbwt);
          stR.setDouble(2, lhbc);
          stR.setInt(3, Integer.parseInt(uid));
          stR.setLong(4, Long.parseLong(nllro.getAttribute("LocHeaderId").toString()));
          stR.executeUpdate();
          getDBTransaction().commit();
        }
        // Update LOCATION_HEADER weight
        try (PreparedStatement stR = getDBTransaction().createPreparedStatement("begin update location_header set  weight = ?, modified_by = ? where loc_header_id = ?; end;", 0)) {
          stR.setDouble(1, lhwt);
          stR.setInt(2, Integer.parseInt(uid));
          stR.setLong(3, Long.parseLong(nllro.getAttribute("LhbLocHeaderId").toString()));
          stR.executeUpdate();
          getDBTransaction().commit();
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  /**
   * Parses up to 6 parameters in a string and helps substitue each with a value
   * from data string. The values in data are ',' seperated.
   */
  public String buildDialogTxt(String data, String msgTxt) {
    //Replace the parameters in the Error string with actual values for display.
    //The values in the data are ',' seperated and msgtxt has parameter 1:, 2:, etc.,
    try {
      String[] d;
      if (data != null) {
        d = data.split(",");
        this.setTextForDialog(msgTxt);
        for (int i = 0; i < d.length; i++) {
          if (i == 0) this.setTextForDialog(this.getTextForDialog().replaceFirst(":0", d[i]));
          if (i == 1) this.setTextForDialog(this.getTextForDialog().replace(":1", d[i]));
          if (i == 2) this.setTextForDialog(this.getTextForDialog().replace(":2", d[i]));
          if (i == 3) this.setTextForDialog(this.getTextForDialog().replace(":3", d[i]));
          if (i == 4) this.setTextForDialog(this.getTextForDialog().replace(":4", d[i]));
          if (i == 5) this.setTextForDialog(this.getTextForDialog().replace(":5", d[i]));
        }
      }
      return this.getTextForDialog();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return "Sorry has no message.";
  }

  /**
   * AUTHOR      : KISHORE SONNAKUL
   * DESCRIPTION : Process for receipt U/I conversions, returns values that
   * are used in showing the re-pack message.
   */
  public String getUIDifferenceValues(long niinLocId) {
    try {
      //Get old_UI in the Niin_LOCation used by pops prompts for unpack
      String oldUI = "X";
      String newUI = "X";
      int actQty = 0;
      double convFac = -1;
      int oldQty = 0;
      try (PreparedStatement stR = getDBTransaction().createPreparedStatement("select nvl(nf.ui,'X'), nl.qty, nvl(nl.old_ui,'X'), nvl(nl.old_qty,0) from niin_location nl, niin_info nf where nf.niin_id = nl.niin_id and niin_loc_id = ?", 0)) {
        stR.setLong(1, niinLocId);
        try (ResultSet rs = stR.executeQuery()) {
          if (rs.next()) {
            newUI = rs.getString(1);
            actQty = Integer.parseInt(rs.getString(2));
            oldUI = rs.getString(3);
            oldQty = Integer.parseInt(rs.getString(4));
          }
        }
      }
      if (oldUI.trim().compareToIgnoreCase("X") == 0) return "NO_CONV";

      //Get the conversion factor
      try (PreparedStatement stR = getDBTransaction().createPreparedStatement("select conversion_factor from ref_ui where ui_conv_from = ? and ui_conv_to = ?", 0)) {
        stR.setString(1, oldUI);
        stR.setString(2, newUI);
        try (ResultSet rs = stR.executeQuery()) {
          if (rs.next()) convFac = Double.parseDouble(rs.getString(1));
        }
      }

      if (convFac < 0) return "CONV_ERROR";
      else if (convFac == 1) return "NO_CONV";

      //Reset the old_UI now that we are showing the repack dialog
      try (PreparedStatement stR = getDBTransaction().createPreparedStatement("begin update niin_location set  old_ui = null where niin_loc_id = ?; end;", 0)) {
        stR.setLong(1, niinLocId);
        stR.executeUpdate();
        getDBTransaction().commit();
      }

      return "NEED_REPACK" + "," + oldUI + "," + oldQty + "," + newUI + "," + actQty + "," + convFac;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return "";
  }

  /**
   * Generate SID for stow
   */
  public String assignSID() {
    // Get the next sequence from DB and build SID and return
    try {
      StringBuilder sid = new StringBuilder();
      TransactionsImpl trans = (TransactionsImpl) this.getTransactions1();
      try (PreparedStatement st = getDBTransaction().createPreparedStatement("select STOW_SID_SEQ.nextval from dual", 0)) {
        try (ResultSet rs = st.executeQuery()) {
          rs.next();
          int r = rs.getInt(1);
          for (int i = 1; i <= 4; i++) {
            val sqM = r % 36;
            if (sqM <= 9) {
              sid.insert(0, (char) (sqM + 48));
            }
            else {
              sid.insert(0, (char) (sqM + 55));
            }
            r = Math.abs(r / 36);
          }
        }
      }
      sid.insert(0, "R" + trans.getCurrentJulian(4));
      return (sid.toString());
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return null;
  }

  /**
   * Perfom the steps to generate the move string
   */
  public String performCarouselMove(Row ro) {
    // Check to find out the carousel type and call appropriate functions that will build the move command
    String commandStr = "";
    try {
      String mflg = "N";
      String cCtl = "";
      String cOfs = "";
      String cMdl = "";
      try (PreparedStatement st = getDBTransaction().createPreparedStatement("select mechanized_flag, carousel_number, carousel_controller, carousel_offset, carousel_model from wac where wac_id = ?", 0)) {
        st.setString(1, ro.getAttribute("WacId").toString());
        try (ResultSet rs = st.executeQuery()) {
          if (rs.next()) {
            mflg = rs.getString(1);
            cCtl = rs.getString(3);
            cOfs = rs.getString(4);
            cMdl = rs.getString(5);
          }
          if (!mflg.equalsIgnoreCase("N")) {
            if (mflg.equalsIgnoreCase("V")) {
              if (cMdl.equalsIgnoreCase("HANEL"))
                commandStr = this.moveHanelVerticalCarousel(Integer.parseInt(ro.getAttribute("LocLevel").toString()));
            }
            else if (cMdl != null) {
              if (cMdl.equalsIgnoreCase("REMSTAR"))
                commandStr = this.moveRemstarHorizontalCarousel(Util.cleanInt(ro.getAttribute("Bay").toString()), ro.getAttribute("Side").toString().charAt(0), Util.cleanInt(cOfs), Util.cleanInt(cCtl));
              if (cMdl.equalsIgnoreCase("WHITE"))
                commandStr = this.moveWhiteHorizontalCarousel(Integer.parseInt(ro.getAttribute("Bay").toString()), ro.getAttribute("Side").toString().charAt(0));
            }
          }
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return commandStr;
  }

  /**
   * Test method to retireve carosuel infor from WAC
   */
  public String getCarouselInfo(String wacStr) {
    // Check to find out the carousel type and call appropriate functions that will build the move command
    String commandStr = "";
    try {
      String mflg = "N";
      String cNo = "";
      String cCtl = "";
      String cOfs = "";
      String cMdl = "";
      try (PreparedStatement st = getDBTransaction().createPreparedStatement("select mechanized_flag, carousel_number, carousel_controller, carousel_offset, carousel_model from wac where wac_number = ?", 0)) {
        st.setString(1, wacStr);
        try (ResultSet rs = st.executeQuery()) {
          if (rs.next()) {
            mflg = rs.getString(1);
            cNo = rs.getString(2);
            cCtl = rs.getString(3);
            cOfs = rs.getString(4);
            cMdl = rs.getString(5);
          }
        }
      }
      commandStr = mflg + "," + ((cNo == null) ? "0" : cNo) + "," + ((cCtl == null) ? "0" : cCtl) + "," + ((cOfs == null) ? "0" : cOfs) + "," + ((cMdl == null) ? "0" : cMdl);
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return commandStr;
  }

  /**
   * Perfom the steps to generate the move string
   */
  public String performCarouselMoveOld(Row ro) {
    // Check to find out the carousel type and call appropriate functions that will build the move command
    String commandStr = "";
    try {
      String mflg = "N";
      String cCtl = "";
      String cOfs = "";
      String cMdl = "";
      try (PreparedStatement st = getDBTransaction().createPreparedStatement("select mechanized_flag, carousel_number, carousel_controller, carousel_offset, carousel_model from wac where wac_id = ?", 0)) {
        st.setString(1, ro.getAttribute("WacId").toString());
        try (ResultSet rs = st.executeQuery()) {
          if (rs.next()) {
            mflg = rs.getString(1);
            cCtl = rs.getString(3);
            cOfs = rs.getString(4);
            cMdl = rs.getString(5);
          }
          if (!mflg.equalsIgnoreCase("N")) {
            if (mflg.equalsIgnoreCase("V")) {
              if (cMdl.equalsIgnoreCase("HANEL"))
                commandStr = this.moveHanelVerticalCarousel(Integer.parseInt(ro.getAttribute("LocLevel").toString()));
            }
            else if (cMdl != null) {
              if (cMdl.equalsIgnoreCase("REMSTAR"))
                commandStr = this.moveRemstarHorizontalCarousel(Integer.parseInt(ro.getAttribute("Bay").toString()), ro.getAttribute("Side").toString().charAt(0), Integer.parseInt(cOfs), Integer.parseInt(cCtl));
              if (cMdl.equalsIgnoreCase("WHITE"))
                commandStr = this.moveWhiteHorizontalCarousel(Integer.parseInt(ro.getAttribute("Bay").toString()), ro.getAttribute("Side").toString().charAt(0));
            }
          }
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return commandStr;
  }

  /**************************************************************************
   * This function filters the NiinInfoView object by the NiinId.
   *************************************************************************/
  public void getNiinInfo(String niinIdStr) {

    try {
      NiinInfoViewImpl niinInfoView;
      //Instantiate the view object
      niinInfoView = this.getNiinInfoView1();

      //Clear the view criteria
      niinInfoView.applyViewCriteria(null);

      ViewCriteria crit;
      ViewCriteriaRow critRow;

      crit = niinInfoView.createViewCriteria();
      critRow = crit.createViewCriteriaRow();

      critRow.setUpperColumns(true);

      critRow.setAttribute("NiinId", "= " + niinIdStr);
      crit.addElement(critRow);
      //Apply the criteria above to the view
      niinInfoView.applyViewCriteria(crit);
      //Execute the query
      niinInfoView.executeQuery();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  public Object findAndCreateNiin(Object niin) {
    Object niinid = null;
    try {
      try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement("select niin_id from niin_info where niin = ? and rownum =1", 0)) {
        pstmt.setObject(1, niin);
        try (ResultSet rs = pstmt.executeQuery()) {
          if (rs.next()) niinid = rs.getObject(1);
        }
      }

      if (niinid == null) {
        boolean niinInserted = false;
        //* niin not found in niin_info table, get from mhif and insert into niin_info
        try (PreparedStatement statementMhif = getDBTransaction().createPreparedStatement("select record_niin, record_fsc, unit_of_issue, unit_price, item_name_nomenclature from ref_mhif where record_niin = ? and rownum =1 ", 0)) {
          statementMhif.setObject(1, niin);
          try (ResultSet resultSet = statementMhif.executeQuery()) {
            if (resultSet.next()) {
              try (PreparedStatement statementNiin = getDBTransaction().createPreparedStatement("insert into niin_info (niin, fsc, ui, price, nomenclature, inventory_threshold) values (?,?,?,?,?,?)", 0)) {

                statementNiin.setObject(1, niin);
                statementNiin.setObject(2, resultSet.getObject(2));
                statementNiin.setObject(3, resultSet.getObject(3));
                Object priceObj = resultSet.getObject(4);
                double price = 0.0;
                if (priceObj != null) price = Double.parseDouble(priceObj.toString()) / 100;

                statementNiin.setDouble(4, price);
                statementNiin.setObject(5, resultSet.getObject(5));
                statementNiin.setString(6, "N");
                statementNiin.executeUpdate();
              }
              getDBTransaction().commit();

              niinInserted = true;
            }
          }
        }
        catch (Exception e) {
          AdfLogUtility.logException(e);
          getDBTransaction().rollback();
        }

        if (niinInserted) {
          try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement("select niin_id from niin_info where niin = ? and rownum =1", 0)) {
            pstmt.setObject(1, niin);
            try (ResultSet rs = pstmt.executeQuery()) {
              if (rs.next()) niinid = rs.getObject(1);
            }
          }
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      niinid = null;
    }
    return niinid;
  }

  /**
   * Container's getter for PackingIssuesView2
   */
  public PackingIssuesViewImpl getPackingIssuesView2() {
    return (PackingIssuesViewImpl) findViewObject("PackingIssuesView2");
  }

  public String getAssignedPackingStationName(String pidIn) {
    try {
      int pidI = Integer.parseInt(pidIn);
      String stn = "ERROR";
      try (PreparedStatement stR = getDBTransaction().createPreparedStatement("select unique e.name from packing_consolidation pc, packing_station ps, equip e " + " where pc.packing_station_id = ps.packing_station_id and " + "      ps.equipment_number = e.equipment_number and " + "      pc.packing_consolidation_id = ? ", 0)) {
        stR.setInt(1, pidI);
        try (ResultSet rs = stR.executeQuery()) {
          if (rs.next()) stn = rs.getString(1);
        }
      }

      return stn;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return "ERROR";
  }

  //verifies if the NIIN exists, or attempts to create it from REF_MHIF.  If it can't it will return a boolean saying that we must call GCSS for the NIIN.
  public boolean createNiinInfoFromRefMhifOrCallGCSS(String niinStr) {
    try {
      String id = "ERROR";
      try (PreparedStatement stR = getDBTransaction().createPreparedStatement("select niin_id from niin_info where niin = ?  ", 0)) {
        stR.setString(1, niinStr.toUpperCase().trim());
        try (ResultSet rs = stR.executeQuery()) {
          if (rs.next()) return false;
        }
      }

      try (PreparedStatement stR = getDBTransaction().createPreparedStatement("select record_fsc, record_niin, record_smic, unit_of_issue, shelf_life_code, unit_price," + " nvl(physical_security_code,'U'), item_name_nomenclature from ref_mhif where record_niin = ?  ", 0)) {
        stR.setString(1, niinStr.toUpperCase().trim());
        try (ResultSet rs = stR.executeQuery()) {
          if (rs.next()) {
            try (PreparedStatement uQ = getDBTransaction().createPreparedStatement("begin INSERT INTO niin_info (fsc, niin, smic, ui, shelf_life_code, " + "  price, scc, nomenclature) " + "  values ('" + rs.getString(1) + "','" + rs.getString(2) + "','" + rs.getString(3) + "','" + rs.getString(4) + "','" + rs.getString(5) + "','" + rs.getDouble(6) / 100 + "','" + rs.getString(7) + "','" + rs.getString(8).trim() + "'); end;", 0)) {
              uQ.executeUpdate();
            }
            getDBTransaction().commit();
            id = "";
          }
        }
      }
      return (id.equalsIgnoreCase("ERROR"));
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return false;
  }

  /**
   * Container's getter for RefMatsView1
   */
  public RefMatsViewImpl getRefMatsView1() {
    return (RefMatsViewImpl) findViewObject("RefMatsView1");
  }

  public String getSiteRIC() {
    String ric = "";
    try (PreparedStatement stR = getDBTransaction().createPreparedStatement("select routing_id from site_info", 0)) {
      try (ResultSet rs = stR.executeQuery()) {
        if (rs.next()) ric = Util.cleanString(rs.getObject(1));
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return ric;
  }

  public String hasCubiScan(String wrkStnName) {
    String flag = "N";
    try (PreparedStatement stR = getDBTransaction().createPreparedStatement("select nvl(HAS_CUBISCAN,'N') from EQUIP where equipment_number = ?", 0)) {
      stR.setString(1, wrkStnName.toUpperCase().trim());
      try (ResultSet rs = stR.executeQuery()) {
        if (rs.next()) flag = rs.getString(1);
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return flag;
  }

  /**
   * METHOD      : createSrlOrLotAndPickXref
   * AUTHOR      : KISHORE SONNAKUL
   * DESCRIPTION : Creates ref rows for each pick.  (PICK_SERIAL_LOT_NUM)
   */
  public String createSrlOrLotAndPickXref(String pIdStr, String sUid) {
    try {
      String scn = "";
      StringBuilder id = new StringBuilder();
      StringBuilder num = new StringBuilder();
      StringBuilder qty = new StringBuilder();
      int locId = 1;

      try (val stR = getDBTransaction().createPreparedStatement("select location_id, scn from picking p, niin_location nl  where p.pid = ? and p.niin_loc_id = nl.niin_loc_id ", 0)) {
        stR.setString(1, pIdStr);
        try (ResultSet rs = stR.executeQuery()) {
          if (rs.next()) {
            locId = rs.getInt(1);
            scn = rs.getString(2);
          }
        }
      }
      SerialOrLotNumListImpl vo;
      vo = getSerialOrLotNumList1();
      int cou = 0;
      if (vo != null) {
        cou = vo.getRowCount();
      }

      //Existing list of serial/lot numbers
      try (val stR = getDBTransaction().createPreparedStatement("select pick_serial_lot_num, serial_lot_num_track_id, qty, location_id from pick_serial_lot_num  where pid = ? ", 0)) {
        stR.setInt(1, Integer.parseInt(pIdStr));
        try (ResultSet rs = stR.executeQuery()) {
          while (rs.next()) {
            if (num.length() <= 0) {
              num = new StringBuilder(rs.getString(1));
              id = new StringBuilder(rs.getString(2));
              qty = new StringBuilder(rs.getString(3));
            }
            else {
              num.append(',').append(rs.getString(1));
              id.append(',').append(rs.getString(2));
              qty.append(',').append(rs.getString(3));
            }
          }
        }
      }
      if (cou <= 0 && num.length() <= 0) return "";

      //Latest list of serial/lot numbers
      StringBuilder addL = new StringBuilder();
      StringBuilder addQ = new StringBuilder();
      StringBuilder addLL = new StringBuilder();
      int i = 0;
      Row r = null;

      if (cou > 0) {
        vo.first(); //FUTURE is this needed, when first is called 3 lines later?
        while (i < cou) {
          if (r == null) {
            r = vo.first();
            if (!id.toString().contains(r.getAttribute("SrlLotNumTrackId").toString())) {
              addL = new StringBuilder(r.getAttribute("SrlLotNumTrackId").toString());
              addQ = new StringBuilder(r.getAttribute("QtyLot").toString());
              addLL = new StringBuilder(r.getAttribute("LocationId").toString());
            }
          }
          else {
            r = vo.next();
            if (!id.toString().contains(r.getAttribute("SrlLotNumTrackId").toString())) {
              addL.append(',').append(r.getAttribute("SrlLotNumTrackId"));
              addQ.append(',').append(r.getAttribute("QtyLot"));
              addLL.append(',').append(r.getAttribute("LocationId"));
            }
          }
          i++;
        }
      }
      cou = id.toString().split(",").length;
      i = 0;
      StringBuilder delL = new StringBuilder();
      //Extracting the serial/lot numbers that are not there in latest list
      while (i < cou) {
        if (id.toString().split(",")[i] != null && !addL.toString().contains(id.toString().split(",")[i])) {
          if (delL.length() <= 0) delL = new StringBuilder("'").append(id.toString().split(",")[i]).append("'");
          else delL.append(",'").append(id.toString().split(",")[i]).append("'");
        }
        i++;
      }
      if (delL.length() > 0) {
        try (PreparedStatement stR = getDBTransaction().createPreparedStatement("select serial_lot_num_track_id, qty, location_id from pick_serial_lot_num  where pid = ? and serial_lot_num_track_id in (" + delL + ")", 0)) {
          stR.setString(1, pIdStr);
          try (ResultSet rs = stR.executeQuery()) {
            //Resetting the quantities that are not there in the latest pick serial/lot
            while (rs.next()) {
              try (PreparedStatement stR2 = getDBTransaction().createPreparedStatement("begin update serial_lot_num_track  set issued_flag = 'N', qty = qty + ?, timestamp = sysdate where serial_lot_num_track_id = ? and location_id = ?; end;", 0)) {
                stR2.setInt(1, rs.getInt(2));
                stR2.setString(2, rs.getString(1));
                stR2.setString(3, rs.getString(3));
                stR2.executeUpdate();
                this.getDBTransaction().commit();
              }
            }
          }
        }
        //Removing the rows that are no loger on the latest list
        try (PreparedStatement stR = getDBTransaction().createPreparedStatement("begin delete from pick_serial_lot_num  where pid = ? and serial_lot_num_track_id in (" + delL + "); end;", 0)) {
          stR.setString(1, pIdStr);
          stR.executeUpdate();
          this.getDBTransaction().commit();
        }
      }
      cou = addL.toString().split(",").length;
      i = 0;
      StringBuilder addL1 = new StringBuilder();
      if (cou > 0) {
        //Inserting the new list of serial/lot to pick
        while (i < cou) {
          try (PreparedStatement stR = getDBTransaction().createPreparedStatement("begin INSERT INTO pick_serial_lot_num (pid, serial_lot_num_track_id, timestamp, qty, location_id, scn) values (?,?, sysdate, ?, ?,?); end;", 0)) {
            stR.setString(1, pIdStr);
            stR.setString(2, addL.toString().split(",")[i]);
            stR.setString(3, addQ.toString().split(",")[i]);
            stR.setString(4, addLL.toString().split(",")[i]);
            stR.setString(5, scn);
            stR.executeUpdate();
          }

          int pickLocId = Integer.parseInt(addLL.toString().split(",")[i]);

          //Handheld allows serial numbers pulled from a separate location.
          if (pickLocId != locId) {
            Row er = getErrorRow("PICK_SRLOT_WARN");
            if (!findErrorQueueRecord(er.getAttribute("Eid").toString(), "SRL_ID", addL.toString().split(",")[i])) { // First time only
              createErrorQueueRecord(er.getAttribute("Eid").toString(), "SRL_ID", addL.toString().split(",")[i], sUid, null);
            }
          }

          //Subtracting the qty of the latest pick serial/lot
          try (PreparedStatement stR2 = getDBTransaction().createPreparedStatement("begin update serial_lot_num_track  set qty = qty - ?, timestamp = sysdate where serial_lot_num_track_id = ?; end;", 0)) {
            stR2.setInt(1, Integer.parseInt(addQ.toString().split(",")[i]));
            stR2.setString(2, addL.toString().split(",")[i]);
            stR2.executeUpdate();
          }
          if (addL1.length() <= 0) addL1 = new StringBuilder("'").append(addL.toString().split(",")[i]).append("'");
          else addL1.append(",'").append(addL.toString().split(",")[i]).append("'");

          i++;
        }
        getDBTransaction().commit();
        //Seeting the issued flag to Y if qty has reached zero
        try (PreparedStatement stR2 = getDBTransaction().createPreparedStatement("begin update serial_lot_num_track  set issued_flag = 'Y', timestamp = sysdate where qty = 0 and serial_lot_num_track_id in (" + addL1 + "); end;", 0)) {
          stR2.executeUpdate();
          getDBTransaction().commit();
        }
      }
      vo.clearCache();
      return "";
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return "Creation of Serial or Lot Con requirement failed.";
  }

  public GCSSMCTransactionsImpl getGCSSMCTransactions1() {
    return getGCSSMCTransactionsService();
  }

  public GCSSMCTransactionsImpl getGCSSMCTransactionsService() {
    try {
      return ((TransactionsImpl) this.getTransactions1()).getGCSSMCTransactions1();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return null;
  }

  /**
   * Container's getter for SerialOrLotNumList1
   */
  public SerialOrLotNumListImpl getSerialOrLotNumList1() {
    return (SerialOrLotNumListImpl) findViewObject("SerialOrLotNumList1");
  }

  /**
   * Container's getter for Transactions1
   */
  public ApplicationModuleImpl getTransactions1() {
    return (ApplicationModuleImpl) findApplicationModule("Transactions1");
  }

  /**
   * Container's getter for CustomerView1.
   */
  public CustomerViewImpl getCustomerView1() {
    return (CustomerViewImpl) findViewObject("CustomerView1");
  }

  public Number getNumberFromRowObject(Object keyObject) {
    Number value = 0;
    if (keyObject instanceof oracle.jbo.domain.DBSequence) {
      value = ((DBSequence) (keyObject)).getValue();
      log.trace("Using sequence.");
    }
    else if (keyObject instanceof oracle.jbo.domain.Number) {
      value = ((oracle.jbo.domain.Number) keyObject).longValue();
      log.trace("Using number.");
    }
    else {
      log.trace("Returning default of 0 - check this!");
    }
    return value;
  }

  public void setWorkstationId(int workstationId) {
    this.workstationId = workstationId;
  }

  public int getWorkstationId() {
    return workstationId;
  }

  public void setTextForDialog(String textForDialog) {
    this.textForDialog = textForDialog;
  }

  public String getTextForDialog() {
    return textForDialog;
  }

  /**
   * Container's getter for NewIssueView1
   */
  public NewIssueViewImpl getNewIssueView1() {
    return (NewIssueViewImpl) findViewObject("NewIssueView1");
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
   * Container's getter for ConsolidationView1
   */
  public ConsolidationViewImpl getConsolidationView1() {
    return (ConsolidationViewImpl) findViewObject("ConsolidationView1");
  }

  /**
   * Container's getter for PickingView1
   */
  public PickingViewImpl getPickingView1() {
    return (PickingViewImpl) findViewObject("PickingView1");
  }

  /**
   * Container's getter for SiteInfoView1
   */
  public SiteInfoViewImpl getSiteInfoView1() {
    return (SiteInfoViewImpl) findViewObject("SiteInfoView1");
  }

  /**
   * Container's getter for SIDLabelVO1
   */
  public SIDLabelVOImpl getSIDLabelVO1() {
    return (SIDLabelVOImpl) findViewObject("SIDLabelVO1");
  }

  /**
   * Container's getter for ShippingView1
   */
  public ShippingViewImpl getShippingView1() {
    return (ShippingViewImpl) findViewObject("ShippingView1");
  }

  /**
   * Container's getter for PINLabelVO1
   */
  public PINLabelVOImpl getPINLabelVO1() {
    return (PINLabelVOImpl) findViewObject("PINLabelVO1");
  }

  /**
   * Container's getter for ErrorQueueVO1
   */
  public ErrorQueueVOImpl getErrorQueueVO1() {
    return (ErrorQueueVOImpl) findViewObject("ErrorQueueVO1");
  }

  /**
   * Container's getter for ErrorLVO1
   */
  public ErrorLVOImpl getErrorLVO1() {
    return (ErrorLVOImpl) findViewObject("ErrorLVO1");
  }

  /**
   * Container's getter for LocationList1
   */
  public LocationListImpl getLocationList1() {
    return (LocationListImpl) findViewObject("LocationList1");
  }

  /**
   * Container's getter for LocationVO1
   */
  public LocationVOImpl getLocationVO1() {
    return (LocationVOImpl) findViewObject("LocationVO1");
  }

  /**
   * Container's getter for NiinLocListVVOF1
   */
  public NiinLocListVVOFImpl getNiinLocListVVOF1() {
    return (NiinLocListVVOFImpl) findViewObject("NiinLocListVVOF1");
  }

  /**
   * Container's getter for NiinAndLocationWeightAndCubeByLocIdVO1
   */
  public NiinAndLocationWeightAndCubeByLocIdVOImpl getNiinAndLocationWeightAndCubeByLocIdVO1() {
    return (NiinAndLocationWeightAndCubeByLocIdVOImpl) findViewObject("NiinAndLocationWeightAndCubeByLocIdVO1");
  }

  /**
   * A simple data structure to help consolidate the "loose" values that were being manipulated in the
   * returnBestPickLocation() function. This allows us to break that function apart and also log the state
   * more easily.
   */
  private static class PickMetrics {

    String scn;
    String pid;
    String niinid;
    String cc;
    Number locationId;
    String scnText;
    int qtyRemaining;
    int issueQty = 0;
    int sumPicks = 0;
    int sumNotPicked = 0;
    int qtyPicked = -1;
    int niinLocId = -1;

    public PickMetrics(Object scn, Object pid, Object niinid, String cc) {
      this.scn = Objects.toString(scn, null);
      this.pid = Objects.toString(pid, null);
      this.niinid = Objects.toString(niinid, null);
      this.cc = cc;
    }

    @Override
    public String toString() {
      return "PickMetrics{" +
          "pid=" + pid +
          ", niinid=" + niinid +
          ", cc='" + cc + '\'' +
          ", locationId=" + locationId +
          ", scnText='" + scnText + '\'' +
          ", scn=" + scn +
          ", qtyRemaining=" + qtyRemaining +
          ", issueQty=" + issueQty +
          ", sumPicks=" + sumPicks +
          ", sumNotPicked=" + sumNotPicked +
          ", qtyPicked=" + qtyPicked +
          ", niinLocId=" + niinLocId +
          '}';
    }
  }
}
