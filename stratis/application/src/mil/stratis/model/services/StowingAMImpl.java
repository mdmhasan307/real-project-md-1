package mil.stratis.model.services;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mil.stratis.model.view.stow.*;
import mil.usmc.mls2.stratis.core.utility.AdfLogUtility;
import oracle.jbo.Row;
import oracle.jbo.server.ApplicationModuleImpl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

@Slf4j
@NoArgsConstructor //ADF BackingHandlers all need a no args constructor
public class StowingAMImpl extends ApplicationModuleImpl {

  /**
   * Container's getter for ScannedStowLVO1
   */
  public ScannedStowLVOImpl getScannedStowLVO1() {
    return (ScannedStowLVOImpl) findViewObject("ScannedStowLVO1");
  }

  /**
   * Container's getter for StowingGenQryVO1
   */
  public StowingGenQryVOImpl getStowingGenQryVO1() {
    return (StowingGenQryVOImpl) findViewObject("StowingGenQryVO1");
  }

  /**
   * Set the scan indicator so that they show up in the stow queue on STOW READY.
   */
  public int updateStowScannedFlag(int stArea, String sidStr, int uid) {
    try {
      int r = findSID(sidStr, uid);
      if (r <= 0)
        return r; // either already scanned or not found

      int rs = 0;
      String sql = "select count(*) from stow inner join location on stow.location_ID = location.location_ID inner join wac on wac.wac_id = location.wac_id inner join equip on equip.wac_id = location.wac_id where status = 'STOW READY' and EQUIP.equipment_number = ? and stow.sid = ? order by sid";
      try (PreparedStatement selectCount = getDBTransaction().createPreparedStatement(sql, 0)) {
        selectCount.setInt(1, stArea);
        selectCount.setString(2, sidStr.trim());
        int count = 0;
        try (ResultSet rset = selectCount.executeQuery()) {
          if (rset.next()) {
            count = rset.getInt(1);
          }
        }
        if (count == 0) return 3;

        try (PreparedStatement updateStow = getDBTransaction().createPreparedStatement("begin update stow set scan_ind = 'Y', modified_by = ? where sid = ?; end;", 0)) {
          updateStow.setLong(1, uid);
          updateStow.setString(2, sidStr.trim());
          rs = updateStow.executeUpdate();
          getDBTransaction().commit();
        }
      }
      return rs;
    }
    catch (Exception e) {
      AdfLogUtility.logExceptionAsWarning(e);
    }
    return 0;
  }

  /**
   * Get the stows for a wac.
   */
  public void getScannedStowSIDs(int equipmentNumber, long userId) {
    try (PreparedStatement stR = getDBTransaction().createPreparedStatement("select wac_id from equip where equipment_number = ?", 0)) {
      int wacId = 0;
      stR.setInt(1, equipmentNumber);
      try (ResultSet rs = stR.executeQuery()) {
        if (rs.next()) {
          if (rs.getObject(1) != null) wacId = rs.getInt(1);
        }
      }
      ScannedStowLVOImpl vsr = getScannedStowLVO1();
      vsr.clearCache();
      vsr.executeQuery();
      vsr.setNamedWhereClauseParam("wacId", wacId);
      vsr.executeQuery();
    }
    catch (Exception e) {
      AdfLogUtility.logExceptionAsWarning(e);
    }
  }

  /**
   * Logic to check and avoid already scanned in stows.
   */
  public int findSID(String sidStr, int uid) {
    try {
      int ri = -3;
      try (PreparedStatement stR = getDBTransaction().createPreparedStatement("select scan_ind, nvl(p.status, 'PICKED'), nvl(s.assign_to_user, ''), s.status from stow s left join picking p on s.PID = p.PID where s.sid = ?", 0)) {
        stR.setString(1, sidStr.toUpperCase().trim());
        try (ResultSet rs = stR.executeQuery()) {
          if (rs.next()) {
            String status = rs.getString(4);
            if (rs.getString(1).equals("Y")) {
              if (status.equals("STOW BYPASS2") || status.equals("STOW LOSS") || status.equals("STOWLOSS47")) {
                ri = -4;
              }
              else ri = 0; // found but was already scanned
            }
            else if (!(rs.getString(2).equals("PICKED"))) //found but the SID for a rewarehouse that is not picked yet
              ri = -1;
            else ri = 1; // found and needs to be updated to Y

            if (ri == 1) {
              String assign_to = rs.getString(3);
              if (assign_to != null && !assign_to.isEmpty() && Integer.parseInt(assign_to) != uid) {
                ri = -2; //already assigned to handheld user.
              }
            }
          }
        }
      }
      return ri; // not found
    }
    catch (Exception e) {
      AdfLogUtility.logExceptionAsWarning(e);
    }
    return -2;
  }

  /**
   * Builds a sorted list of stows for a WAC so that optimal order is used in stowing.
   */
  public int buildStowRS(long reqType, long equipmentNumber, long userId, String sortOrder) {
    PreparedStatement stR = null;
    ResultSet rs = null;
    try {
      int addCount = 0;
      String wacIdStr = "";
      String mechFlag = "";
      String sideStr = "A";
      this.deleteRowsFromVOForStowing();
      StowingGenQryVOImpl pNQ = getStowingGenQryVO1();
      pNQ.setNamedWhereClauseParam("wacIdStr", "-1");
      pNQ.executeQuery();
      StowingGenQryVOImpl pNQA = null;
      StowingGenQryVOImpl pNQB = null;
      stR = getDBTransaction().createPreparedStatement("select w.mechanized_flag, w.wac_id from wac w, equip e where e.wac_id = w.wac_id and e.equipment_number = ?", 0);
      stR.setLong(1, equipmentNumber);
      rs = stR.executeQuery();
      if (rs.next()) {
        mechFlag = rs.getString(1);
        wacIdStr = rs.getString(2);
      }
      rs.close();
      stR.close();
      //Build the Stow query
      if (mechFlag.compareTo("V") == 0) {
        pNQ = getStowingGenQryVO1();
        pNQ.setRangeSize(-1);
        pNQ.setOrderByClause(" LOC_LEVEL, BAY, SLOT ");
        pNQ.setNamedWhereClauseParam("wacIdStr", (Object) wacIdStr);
        // Build where clause for vertical
        sideStr = "A";
        pNQ.setWhereClause(" SIDE like NVL( '" + sideStr.trim() + "', '%') ");
        pNQ.executeQuery();
        addCount = pNQ.getRowCount();
        pNQ.first();
      }
      else if (mechFlag.compareTo("H") == 0) {

        pNQA = getStowingGenQryVOA();
        pNQA.setRangeSize(-1);
        sideStr = "A";
        pNQA.setOrderByClause("  QRSLT.BAY ASC, QRSLT.LOC_LEVEL ASC, QRSLT.SLOT ASC ");
        pNQA.setNamedWhereClauseParam("wacIdStr", (Object) wacIdStr);
        // Build where clause for Horizantal
        pNQA.setWhereClause(" SIDE like NVL( '" + sideStr.trim() + "', '%') ");
        pNQA.executeQuery();
        int aCount = pNQA.getRowCount();
        //For Side B
        pNQB = getStowingGenQryVOB();
        pNQB.setRangeSize(-1);
        sideStr = "B";
        pNQB.setOrderByClause("  QRSLT.BAY ASC, QRSLT.LOC_LEVEL ASC, QRSLT.SLOT ASC ");
        pNQB.setNamedWhereClauseParam("wacIdStr", (Object) wacIdStr);
        pNQB.setWhereClause(" SIDE like NVL( '" + sideStr.trim() + "', '%') ");
        pNQB.executeQuery();
        int bCount = pNQB.getRowCount();
        //Perform the merge of the rows
        int a = 0;
        int b = 0;

        Row ro = null;
        pNQ.setRangeSize(-1);
        while ((a < aCount) || (b < bCount)) {
          // Add the merge code
          if (a < aCount) {
            ro = pNQA.next();
            if (!validateSidAlreadyOnStowList(ro.getAttribute("Sid").toString())) {
              if (addCount == 0)
                pNQ.insertRow(ro);
              else
                pNQ.insertRowAtRangeIndex(addCount, ro);
              addCount++;
            }
          }
          if (b < bCount) {
            ro = pNQB.next();
            if (!validateSidAlreadyOnStowList(ro.getAttribute("Sid").toString())) {
              if (addCount == 0)
                pNQ.insertRow(ro);
              else
                pNQ.insertRowAtRangeIndex(addCount, ro);
              addCount++;
            }
          }
          a++;
          b++;
        }
        pNQA.clearCache();
        pNQB.clearCache();
        pNQ.first();
      }
      else {
        pNQ = getStowingGenQryVO1();
        pNQ.setRangeSize(-1);
        pNQ.setOrderByClause(sortOrder);

        pNQ.setNamedWhereClauseParam("wacIdStr", (Object) wacIdStr);

        pNQ.executeQuery();
        addCount = pNQ.getRowCount();
        pNQ.first();
      }
      return pNQ.getRowCount();
    }
    catch (Exception e) {
      AdfLogUtility.logExceptionAsWarning(e);
    }
    finally {
      try {
        if (rs != null) {
          rs.close();
        }
        if (stR != null) {
          stR.close();
        }
      }
      catch (Exception e) {
        AdfLogUtility.logException(e);
      }
    }
    return 0;
  }

  /**
   * Used to remove rows from the queue.
   */
  public void deleteRowsFromVOForStowing() {
    // Check if a stow is already in the stow list returns true if it exists else false NOT USED
    try {
      StowingGenQryVOImpl pVO = getStowingGenQryVO1();
      Row ro = pVO.first();
      if (ro != null)
        pVO.removeCurrentRow();
      while (pVO.hasNext()) {
        pVO.next();
        pVO.removeCurrentRow();
      }
      ro = null;
      StowingGenQryVOImpl pVOA = getStowingGenQryVOA();
      ro = pVOA.first();
      if (ro != null)
        pVOA.removeCurrentRow();
      while (pVOA.hasNext()) {
        pVOA.next();
        pVOA.removeCurrentRow();
      }
      ro = null;
      StowingGenQryVOImpl pVOB = getStowingGenQryVOB();
      ro = pVOB.first();
      if (ro != null)
        pVOB.removeCurrentRow();
      while (pVOB.hasNext()) {
        pVOB.next();
        pVOB.removeCurrentRow();
      }
      getDBTransaction().commit();
    }
    catch (Exception e) {
      AdfLogUtility.logExceptionAsWarning(e);
    }
  }

  /**
   * Container's getter for StowingGenQryVOA
   */
  public StowingGenQryVOImpl getStowingGenQryVOA() {
    return (StowingGenQryVOImpl) findViewObject("StowingGenQryVOA");
  }

  /**
   * Container's getter for StowingGenQryVOB
   */
  public StowingGenQryVOImpl getStowingGenQryVOB() {
    return (StowingGenQryVOImpl) findViewObject("StowingGenQryVOB");
  }

  /**
   * Checks to see if the stow is already on the stow list.
   */
  public boolean validateSidAlreadyOnStowList(String sidS) {
    // Check if a stow is already in the stow list returns true if it exists else false
    try {
      boolean r = false;
      StowingGenQryVOImpl pVO = getStowingGenQryVO1();
      pVO.first();
      if ((pVO.getCurrentRow() != null) && (pVO.getCurrentRow().getAttribute("Sid").toString().equals(sidS)))
        return true;
      while (pVO.hasNext()) {
        if (pVO.next().getAttribute("Sid").toString().equals(sidS)) {
          return true;
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
   * Container's getter for StowRefuseQryVO1
   */
  public StowRefuseQryVOImpl getStowRefuseQryVO1() {
    return (StowRefuseQryVOImpl) findViewObject("StowRefuseQryVO1");
  }

  /**
   * Calls methods to update niin_location along with updating of stow data.  Update the stow location and/or qty
   */
  public String updateStowLocationOnly(String uid) {
    // Assign stow location used in both Reassign SID and NIIN verification assigning new location
    try {
      Row ro = this.getStowingGenQryVO1().getCurrentRow();
      // Update the stow with the location
      try (PreparedStatement stS = getDBTransaction().createPreparedStatement("begin update stow set location_id = ?, modified_by = ?  where stow_id = ?; end;", 0)) {
        stS.setString(1, (ro.getAttribute("LocationId") == null ? "1" : ro.getAttribute("LocationId").toString()));
        stS.setString(2, uid);
        stS.setLong(3, Long.parseLong(ro.getAttribute("StowId").toString()));
        stS.executeUpdate();
        getDBTransaction().commit();
      }
      return "";
    }
    catch (Exception e) {
      log.warn("STOW_ERR_OTHERS1 on uid  {}", uid, e);
      return "STOW_ERR_OTHERS " + e.getMessage();
    }
  }

  /**
   * Calls methods to update niin_location along with updating of stow data.
   * Update the stow location and/or qty
   */
  public String updateStowLocation(Object locId, String uid, long selInd2,
                                   int stowedQty) {
    // Assign stow location used in both Reassign SID and NIIN verification assigning new location
    try {
      if (locId == null)
        return "STOW_LOC_INV";
      String errStr = "";
      Row ro = null;
      if (selInd2 == 0) { // This is when using the STOW window
        ro = this.getStowingGenQryVO1().getCurrentRow();
      }
      else { // this is when using Re-assign SID window
        ro = getStowLocVO1().getCurrentRow();
      }
      //Update the Qty and other details on NIIN_LOCATION
      if (selInd2 == 0)
        errStr = createOrUpdateNiinLocation(ro.getAttribute("LocationId"), uid, stowedQty); // Call this only for the stow and not for re-assign
      if (errStr.length() > 0)
        return errStr;

      // Update the stow with the added stow qty and location
      try (PreparedStatement stS = getDBTransaction().createPreparedStatement("begin update stow set location_id = ?, modified_by = ?, stow_qty = ?  where stow_id = ?; end;", 0)) {
        stS.setLong(1, Long.parseLong(locId.toString()));
        stS.setInt(2, Integer.parseInt(uid));
        if (selInd2 == 0)
          stS.setInt(3, stowedQty + Integer.parseInt(ro.getAttribute("StowQty") == null ? "0" : ro.getAttribute("StowQty").toString()));
        else stS.setInt(3, Integer.parseInt(ro.getAttribute("StowQty") == null ? "0" : ro.getAttribute("StowQty").toString()));
        stS.setLong(4, Long.parseLong(ro.getAttribute("StowId").toString()));
        stS.executeUpdate();
        getDBTransaction().commit();
      }
      return "";
    }
    catch (Exception e) {
      log.warn("STOW_ERR_OTHERS2 on locId {}, uid {}, selInd2 {}, stowedQty  {}", locId, uid, selInd2, stowedQty, e);
      return "STOW_ERR_OTHERS " + e.getMessage();
    }
  }

  /**
   * Create Update the niin_location qty
   */
  public String createOrUpdateNiinLocation(Object locId, String uid,
                                           int stowQty) {
    // Assign stow location used in both Reassign SID and NIIN verification assigning new location
    try {
      // This is when using the STOW window
      Row ro = this.getStowingGenQryVO1().getCurrentRow();
      long niinId = 0;
      // Get needed information from RECEIPT
      String cc = "A";
      String prjCd = "";
      String pc = "A";
      String cog = "AA";
      try (PreparedStatement stR = getDBTransaction().createPreparedStatement("select niin_id, nvl(cc,'A'), project_code, pc, cognizance_code, rcn from receipt where rcn = ?", 0)) {
        stR.setLong(1, Long.parseLong(ro.getAttribute("Rcn").toString()));
        try (ResultSet rst = stR.executeQuery()) {

          if (rst.next()) {
            niinId = Long.parseLong(rst.getString(1));
            cc = rst.getString(2);
            prjCd = rst.getString(3);
            pc = rst.getString(4);
            cog = rst.getString(5);
          }
        }
      }

      long niinLocId = 0;
      String lock = "N";
      String cCStr = "A";
      String expDtStr = "019999";
      String underAudit = "N";
      try (PreparedStatement stR = getDBTransaction().createPreparedStatement("select niin_loc_id, nvl(locked,'N'), nvl(under_audit,'N'), nvl(cc,'A'), to_char(expiration_date,'MMYYYY') from niin_location where location_id = ? and niin_id = ? and cc=?", 0)) {
        stR.setLong(1, Long.parseLong(locId.toString()));
        stR.setLong(2, niinId);
        stR.setString(3, cc);
        //Expiration date has a format of 01-JAN-99 so conver to MONYY format

        try (ResultSet rst = stR.executeQuery()) {
          if (rst.next()) {
            niinLocId = Long.parseLong(rst.getString(1));
            lock = rst.getString(2);
            underAudit = rst.getString(3);
            cCStr = rst.getString(4);
            expDtStr = rst.getString(5);
          }
          else { //If it is a new location then copy the values that are there on the stow so that they match
            if (ro.getAttribute("Rcc") != null && ro.getAttribute("Rcc").toString().length() > 0) cCStr = ro.getAttribute("Rcc").toString();
            if (ro.getAttribute("SexpDate") != null && ro.getAttribute("SexpDate").toString().length() > 0)
              expDtStr = (ro.getAttribute("SexpDate").toString().split("-")[1] + ro.getAttribute("SexpDate").toString().split("-")[0]);
          }
        }
      }

      if ((niinLocId == 0) && (this.getWorkloadManagerService().isMechanized(ro.getAttribute("WacId").toString()))) {

        // Check if some other NIIN existis at that location (FOR MECH ONLY)
        String nlIdStr = "0";
        try (PreparedStatement stR = getDBTransaction().createPreparedStatement("select nl.niin_loc_id from niin_location nl where nl.niin_id <> ? and nl.location_id = ?", 0)) {
          stR.setLong(1, niinId);
          stR.setLong(2, Long.parseLong(locId.toString()));
          try (ResultSet rst = stR.executeQuery()) {
            if (rst.next()) {
              nlIdStr = rst.getString(1);
            }
          }
        }
        //Check this building has any empty locations
        int nLoc = 0;
        try (PreparedStatement stR = getDBTransaction().createPreparedStatement("select count(location_id) from location l, wac w1 where l.availability_flag = 'A' and l.wac_id = w1.wac_id and w1.warehouse_id in (select w.warehouse_id from  wac w where w.wac_id = ?)", 0)) {
          stR.setString(1, ro.getAttribute("WacId").toString());
          try (ResultSet rst = stR.executeQuery()) {
            if (rst.next()) {
              nLoc = rst.getInt(1);
            }
          }
        }
        if ((!nlIdStr.equalsIgnoreCase("0")) && (nLoc > 0))
          return "STOW_ERR_LOCATION";
      }

      String expAtStow = ((ro.getAttribute("SexpDate") == null) ? "019999" : (ro.getAttribute("SexpDate").toString().split("-")[1] + ro.getAttribute("SexpDate").toString().split("-")[0]));

      if ((!expAtStow.equalsIgnoreCase(expDtStr)) || (!cCStr.equalsIgnoreCase((ro.getAttribute("Rcc") == null ? "A" : ro.getAttribute("Rcc").toString()))))
        return "STOW_ERR_CCEXPDIFF";
      // Return error message when trying to stow in locked or under audit locations
      if ((lock.equalsIgnoreCase("Y")) || (underAudit.equalsIgnoreCase("Y")))
        return "STOW_ERR_LOCKEDORAUDIT";

      if (niinLocId == 0) {
        // Create a new NIIN_LOCATION row
        try (PreparedStatement stR = getDBTransaction().createPreparedStatement("begin INSERT INTO NIIN_LOCATION (niin_id, location_id, qty, expiration_date, date_of_manufacture, cc, locked," + "                  created_by,  project_code, pc, " + "                  case_weight_qty, lot_con_num, serial_number, packed_date, num_extents, num_counts, under_audit  ) VALUES " + "                  (?, ?, ?, to_date(?,'YYYY-MM-DD'), to_date(?,'YYYY-MM-DD'), ?, ?, ?, ?, ?,?, ?, ?, to_date(?,'YYYY-MM-DD'), ?,?, ?); end;", 0)) {
          stR.setLong(1, niinId);
          stR.setLong(2, Long.parseLong(locId.toString()));
          stR.setInt(3, stowQty);
          stR.setString(4, ((ro.getAttribute("SexpDate") == null) ? "9999-01-01" : ro.getAttribute("SexpDate").toString().substring(0, 10)));
          stR.setString(5, ((ro.getAttribute("DateOfManufacture") == null) ? "" : ro.getAttribute("DateOfManufacture").toString().substring(0, 10)));
          stR.setString(6, cc);
          stR.setString(7, "N");
          stR.setLong(8, Long.parseLong(uid));
          stR.setString(9, prjCd);
          stR.setString(10, pc);
          stR.setInt(11, Integer.parseInt((ro.getAttribute("CaseWeightQty") == null) ? "0" : ro.getAttribute("CaseWeightQty").toString()));
          stR.setString(12, ((ro.getAttribute("LotConNum") == null) ? null : ro.getAttribute("LotConNum").toString()));
          stR.setString(13, ((ro.getAttribute("SerialNumber") == null) ? null : ro.getAttribute("SerialNumber").toString()));
          stR.setString(14, ((ro.getAttribute("PackedDate") == null) ? "" : ro.getAttribute("PackedDate").toString().substring(0, 10)));
          stR.setInt(15, 0);
          stR.setInt(16, 0);
          stR.setString(17, "N");
          stR.executeUpdate();
          getDBTransaction().commit();
        }
      }
      else {
        //Update qty on NIIN_LOCATION
        try (PreparedStatement stR = getDBTransaction().createPreparedStatement("begin update niin_location set  qty = (qty + ?), old_ui = null, nsn_remark = 'N',  modified_by = ? where niin_loc_id = ?; end;", 0)) {
          stR.setInt(1, stowQty);
          stR.setInt(2, Integer.parseInt(uid));
          stR.setLong(3, niinLocId);
          stR.executeUpdate();
          getDBTransaction().commit();
        }
      }

      //---------------------
      //create serial_lot_num_track row
      int srlTrackId = 0;
      if ((ro.getAttribute("SerialNumber") != null) || (ro.getAttribute("LotConNum") != null)) {
        if ((ro.getAttribute("SerialNumber") == null || ro.getAttribute("SerialNumber").toString().length() <= 0) && (ro.getAttribute("LotConNum") != null)) {
          try (PreparedStatement stR = getDBTransaction().createPreparedStatement("select serial_lot_num_track_id from serial_lot_num_track where lot_con_num = ? and niin_id = ? and serial_number is null and cc = ? and location_id = ?", 0)) {
            stR.setString(1, ro.getAttribute("LotConNum").toString());
            stR.setLong(2, niinId);
            stR.setString(3, cc);
            stR.setString(4, locId.toString());
            try (ResultSet rst = stR.executeQuery()) {
              if (rst.next()) {
                srlTrackId = Integer.parseInt(rst.getString(1));
              }
            }
          }

          if (srlTrackId > 0) { //Case of existing lot number
            try (PreparedStatement stR = getDBTransaction().createPreparedStatement("begin update serial_lot_num_track set qty = (qty +?), timestamp = sysdate where serial_lot_num_track_id = ?; end;", 0)) {
              stR.setInt(1, stowQty);
              stR.setInt(2, srlTrackId);
              stR.executeUpdate();
              getDBTransaction().commit();
            }
          }
        }
        if (srlTrackId <= 0) { //Case of serial or new lot number
          try (PreparedStatement stR = getDBTransaction().createPreparedStatement("begin insert into serial_lot_num_track (niin_id, serial_number, lot_con_num, cc, expiration_date, issued_flag, timestamp, qty, location_id) values " + " (?,?,?,?,to_date(?,'YYYY-MM-DD'), 'N',sysdate,?,?); end;", 0)) {
            stR.setLong(1, niinId);
            stR.setString(2, ((ro.getAttribute("SerialNumber") == null) ? null : ro.getAttribute("SerialNumber").toString()));
            stR.setString(3, ((ro.getAttribute("LotConNum") == null) ? null : ro.getAttribute("LotConNum").toString()));
            stR.setString(4, cc);
            stR.setString(5, ((ro.getAttribute("SexpDate") == null) ? "9999-01-01" : ro.getAttribute("SexpDate").toString().substring(0, 10)));
            stR.setInt(6, stowQty);
            stR.setString(7, locId.toString());
            stR.executeUpdate();
            getDBTransaction().commit();
          }
        }
      }

      //---------------------
      //Update last stow date on the Location and make sure the location is not available as new
      try (PreparedStatement stR = getDBTransaction().createPreparedStatement("begin update location set last_stow_date = sysdate, availability_flag = 'U', modified_by = ? where location_id = ?; end;", 0)) {
        stR.setInt(1, Integer.parseInt(uid));
        stR.setString(2, locId.toString());
        stR.executeUpdate();
        getDBTransaction().commit();
      }

      //Call update for the new Location with the remaining Stows NOT NEEDED AS IT IS HANDLED BY TRIGGER
      return "";
    }
    catch (Exception e) {
      log.warn("STOW_ERR_OTHERS3", e);
      return "STOW_ERR_OTHERS " + e.getMessage();
    }
  }

  /**
   * Container's getter for StowLocVO1
   */
  public StowLocVOImpl getStowLocVO1() {
    return (StowLocVOImpl) findViewObject("StowLocVO1");
  }

  /**
   * Get Stow details for reassign location
   */
  public String getScannedSIDDetails(String sidStr) {
    // Assign stow location
    try {
      String message = "";
      StowLocVOImpl vos = getStowLocVO1();
      vos.setNamedWhereClauseParam("sidStr", (Object) sidStr);
      vos.executeQuery();
      if (vos.getRowCount() > 0)
        return "";
      else { //Need to check if STOW is missing because of status or does not exist
        String sql = "select status from stow where sid = ?";
        String status = "";
        try (PreparedStatement stS = getDBTransaction().createPreparedStatement(sql, 0)) {
          stS.setString(1, sidStr.trim());
          try (ResultSet rset = stS.executeQuery()) {
            if (rset.next()) {
              status = rset.getString(1);
              if (status.equals("STOW BYPASS2") || status.equals("STOW LOSS") || status.equals("STOWLOSS47")) message = "STOW_ERR_BYPASS";
              else message = "STOW_ERR_STOWED";
            }
            else {
              message = "STOW_ERR_NOROW";
            }
          }
        }
        log.debug("MESSAGE:  {}", message);
        return message;
      }
    }
    catch (Exception e) {
      AdfLogUtility.logExceptionAsWarning(e);
    }
    return "";
  }

  /**
   * Update stow status
   */
  public String stowRowStatusChange(String sidL, int uid, int qtyStowed,
                                    int sQty, String lossInd) {

    log.debug("{} {} {} {} {}", sidL, uid, qtyStowed, sQty, lossInd);

    try {
      String statusStr = "STOWED";
      Row ro = this.getStowingGenQryVO1().getCurrentRow();
      GCSSMCTransactionsImpl transaction = this.getGCSSMCAMService();

      //Update the Qty and other details on NIIN_LOCATION
      String errStr = createOrUpdateNiinLocation(ro.getAttribute("LocationId").toString(), String.valueOf(uid), qtyStowed);
      if (errStr.length() > 0)
        return errStr;

      log.debug("User [{}] updating SID [{}] to status [{}]", uid, sidL, statusStr);
      try (PreparedStatement updateStatement = getDBTransaction().createPreparedStatement("begin update stow set stow_qty = ?, modified_by = ? where sid = ?; end;", 0)) {
        updateStatement.setInt(1, qtyStowed + sQty);
        updateStatement.setInt(2, uid);
        updateStatement.setString(3, sidL);
        updateStatement.executeUpdate();
        getDBTransaction().commit();
      }

      //Update the stow status only if all the quantity is stowed
      if (((sQty + qtyStowed) == Integer.parseInt(ro.getAttribute("QtyToBeStowed").toString())) || (lossInd.equalsIgnoreCase("7"))) {
        if (lossInd.equalsIgnoreCase("7"))
          statusStr = "STOW LOSS";

        log.debug("User [{}] updating SID [{}] to status [{}]", uid, sidL, statusStr);
        try (PreparedStatement updateStatement = getDBTransaction().createPreparedStatement("begin update stow set status = ?, modified_by = ? where sid = ?; end;", 0)) {
          updateStatement.setString(1, statusStr);
          updateStatement.setInt(2, uid);
          updateStatement.setString(3, sidL);
          updateStatement.executeUpdate();
          getDBTransaction().commit();
        }

        if (!(ro.getAttribute("DocumentId").equals("YLL"))) {
          int returnCode = transaction.sendSTWGCSSMCTransaction(sidL);
          if (returnCode == -2) {
            return "DUPLICATE_STW";
          }
        }

        //YLL transaction call for REWAREHOUSE
        if (ro.getAttribute("Pin") == null) {
          getTransactionManagerService().processReceiptTransaction(Integer.parseInt(ro.getAttribute("Rcn").toString()), ro.getAttribute("Sid").toString(), null, uid);
        }

        //need to clean up Picking_lot_num_track and the serial_lot_num_track
        if (ro.getAttribute("SerialNumber") != null && ro.getAttribute("Pid") != null && ro.getAttribute("DocumentId").equals("YLL")) {
          int serialid = 0;
          try (PreparedStatement serialSelect = getDBTransaction().createPreparedStatement("select plnt.serial_lot_num_track_id from serial_lot_num_track slnt inner join pick_serial_lot_num plnt on slnt.serial_lot_num_track_id = plnt.serial_lot_num_track_id where slnt.serial_number = ? and slnt.niin_id = ? and plnt.pid = ?", 0)) {
            serialSelect.setString(1, ro.getAttribute("SerialNumber").toString());
            serialSelect.setInt(2, Integer.parseInt(ro.getAttribute("NiinId").toString()));
            serialSelect.setInt(3, Integer.parseInt(ro.getAttribute("Pid").toString()));

            try (ResultSet rSet = serialSelect.executeQuery()) {
              if (rSet.next()) {
                serialid = rSet.getInt(1);
              }
            }
          }
          if (serialid != 0) {
            try (PreparedStatement stR1 = getDBTransaction().createPreparedStatement("begin delete from pick_serial_lot_num where serial_lot_num_track_id = ?; delete from serial_lot_num_track where serial_lot_num_track_id = ?; end;", 0)) {
              stR1.setInt(1, serialid);
              stR1.setInt(2, serialid);
              stR1.executeUpdate();
              this.getDBTransaction().commit();
            }
          }
        }
      }
      return "";
    }
    catch (Exception e) {
      AdfLogUtility.logExceptionAsWarning(e);
    }
    return "";
  }

  public WorkLoadManagerImpl getWorkloadManagerService() {
    try {
      WorkLoadManagerImpl workloadManagerService = (WorkLoadManagerImpl) this.getWorkLoadManager1();
      return workloadManagerService;
    }
    catch (Exception e) {
      AdfLogUtility.logExceptionAsWarning(e);
    }
    return null;
  }

  /**
   * Sets the status of stow bypass
   */
  public int stowRowByPass(String statusStr, int byPassCount, Object sidS,
                           int uid) {
    PreparedStatement stR = null;
    try {
      int bc = byPassCount;
      stR = getDBTransaction().createPreparedStatement("begin update stow set status = ?, bypass_count = ?, modified_by = ? where sid = ?; end;", 0);
      if (bc <= 1)
        stR.setString(1, statusStr + "1");
      else
        stR.setString(1, statusStr + "2");
      stR.setInt(2, bc);
      stR.setInt(3, uid);

      stR.setString(4, sidS.toString());
      stR.executeUpdate();
      getDBTransaction().commit();
      stR.close();
      return bc;
    }
    catch (Exception e) {
      AdfLogUtility.logExceptionAsWarning(e);
    }
    finally {
      try {
        if (stR != null) {
          stR.close();
          stR = null;
        }
      }
      catch (Exception e) {
        AdfLogUtility.logException(e);
      }
    }
    return 0;
  }

  /**
   * Sets the status of stow refuse
   */
  public int stowRowRefused(String statusStr, int refCount, Object sidS,
                            int uid) {
    PreparedStatement stR = null;
    try {
      int bc = refCount;
      stR = getDBTransaction().createPreparedStatement("begin update stow set status = ?,  modified_by = ? where sid = ?; end;", 0);
      stR.setString(1, statusStr);
      stR.setInt(2, uid);

      stR.setString(3, sidS.toString());
      stR.executeUpdate();
      getDBTransaction().commit();
      stR.close();
      return bc;
    }
    catch (Exception e) {
      AdfLogUtility.logExceptionAsWarning(e);
    }
    finally {
      try {
        if (stR != null) {
          stR.close();
          stR = null;
        }
      }
      catch (Exception e) {
        AdfLogUtility.logException(e);
      }
    }
    return 0;
  }

  /**
   * Checks if the location is empty and not same as choosen one.
   */
  public String validateStowLocation(String lStr) {
    try {
      String msgStr = "";
      try (PreparedStatement stR = getDBTransaction().createPreparedStatement("select location_id, location_label, side, bay, loc_level, slot, divider_index from location where location_label = ? and availability_flag = 'A' " + " and location_id not in (select distinct location_id from niin_location  where niin_id != ?) ", 0)) {
        stR.setString(1, lStr);
        stR.setString(2, this.getStowingGenQryVO1().getCurrentRow().getAttribute("NiinId").toString());
        try (ResultSet rs = stR.executeQuery()) {
          if (rs.next()) {
            msgStr = rs.getString(1);
            this.getStowingGenQryVO1().getCurrentRow().setAttribute("LocationId", rs.getString(1));
            this.getStowingGenQryVO1().getCurrentRow().setAttribute("LocationLabel", rs.getString(2));
            this.getStowingGenQryVO1().getCurrentRow().setAttribute("Side", rs.getString(3));
            this.getStowingGenQryVO1().getCurrentRow().setAttribute("Bay", rs.getString(4));
            this.getStowingGenQryVO1().getCurrentRow().setAttribute("LocLevel", rs.getString(5));
            this.getStowingGenQryVO1().getCurrentRow().setAttribute("Slot", rs.getString(6));
            this.getStowingGenQryVO1().getCurrentRow().setAttribute("DividerIndex", rs.getString(7));
          }
        }
      }
      return msgStr;
    }
    catch (Exception e) {
      AdfLogUtility.logExceptionAsWarning(e);
    }
    return "";
  }

  /**
   * Container's getter for StowByPassQryVO1
   */
  public StowByPassQryVOImpl getStowByPassQryVO1() {
    return (StowByPassQryVOImpl) findViewObject("StowByPassQryVO1");
  }

  public TransactionsImpl getTransactionManagerService() {
    try {
      TransactionsImpl service = (TransactionsImpl) getWorkloadManagerService().getTransactions1();
      return service;
    }
    catch (Exception e) {
      AdfLogUtility.logExceptionAsWarning(e);
    }
    return null;
  }

  public GCSSMCTransactionsImpl getGCSSMCAMService() {
    return getTransactionManagerService().getGCSSMCTransactions1();
  }

  /**
   * Container's getter for WorkLoadManager1
   */
  public ApplicationModuleImpl getWorkLoadManager1() {
    return (ApplicationModuleImpl) findApplicationModule("WorkLoadManager1");
  }
}
