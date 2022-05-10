package mil.stratis.model.services;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.common.util.RegUtils;
import mil.stratis.common.util.Util;
import mil.stratis.model.view.pick.*;
import mil.usmc.mls2.stratis.core.utility.AdfLogUtility;
import oracle.jbo.Row;
import oracle.jbo.server.ApplicationModuleImpl;
import org.apache.commons.lang3.StringUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Slf4j
@NoArgsConstructor //ADF BackingHandlers all need a no args constructor
public class PickingAMImpl extends ApplicationModuleImpl {

  public static final String WAC_ID_STR = "wacIdStr";
  public static final String SERIAL_OR_LO_NUM = "SerialOrLoNum";
  public static final String SRL_LOT_NUM_TRACK_ID = "SrlLotNumTrackId";
  public static final String QTY_LOT = "QtyLot";
  public static final String LOT_CON_NUM = "LotConNum";

  /**
   * Container's getter for PickingGenQryVO1
   */
  public PickingGenQryVOImpl getPickingGenQryVO1() {
    return (PickingGenQryVOImpl) findViewObject("PickingGenQryVO1");
  }

  /**
   * Container's getter for PickRefuseQryVO1
   */
  public PickRefuseQryVOImpl getPickRefuseQryVO1() {
    return (PickRefuseQryVOImpl) findViewObject("PickRefuseQryVO1");
  }

  /**
   * Get the Pick rows
   */
  public void buildPickRS(long reqType, long equipmentNumber, String userId, String pramStr1, String iorStr, String sortOrder) {
    //PreparedStatement stR = null;
    try {
      int addCount;
      int currRow;
      String wacIdStr = "";
      String mechFlag = "";
      String tasksPerTrip = "";
      this.deleteRowsFromVOForPicking();
      PickingGenQryVOImpl pNQ = getPickingGenQryVO1();
      pNQ.setNamedWhereClauseParam(WAC_ID_STR, "-1");
      pNQ.setNamedWhereClauseParam("userIdStr", (Object) userId);
      pNQ.setRangeSize(-1);
      pNQ.executeQuery();
      PickingGenQryVOImpl pNQA = null;
      PickingGenQryVOImpl pNQB = null;
      try (PreparedStatement stR = this.getDBTransaction().createPreparedStatement(
          "select w.mechanized_flag, w.wac_id, w.tasks_per_trip from wac w, equip e where e.wac_id = w.wac_id " +
              "and e.equipment_number = ?", 0)) {
        stR.setLong(1, equipmentNumber);
        try (ResultSet rs = stR.executeQuery()) {
          if (rs.next()) {
            mechFlag = rs.getString(1);
            wacIdStr = rs.getString(2);
            tasksPerTrip = (rs.getString(3) == null) ? "" : rs.getString(3);
          }
        }
      }
      //Build the pick query
      if (pramStr1.length() <= 0) reqType = 0;
      if (mechFlag.compareTo("V") == 0) {
        pNQ = getPickingGenQryVO1();
        pNQ.setRangeSize(-1);
        pNQ.setOrderByClause(" LOC_LEVEL, BAY, SLOT ");
        pNQ.setNamedWhereClauseParam(WAC_ID_STR, wacIdStr);
        pNQ.setNamedWhereClauseParam("issueTypeStr", iorStr);
        pNQ.setNamedWhereClauseParam("userIdStr", userId);
        // Build where clause for vertical
        if (reqType == 1)
          // By AAC
          pNQ.setWhereClause(" AAC in (" + pramStr1 + ") " + " AND SIDE like 'A' ");
        else if (reqType == 2)
          // By Route Name
          pNQ.setWhereClause(" ROUTE_NAME in (" + pramStr1.trim() + ") " + " AND SIDE like 'A' ");
        else if (reqType == 3)
          // By priority group
          pNQ.setWhereClause(" ISSUE_PRIORITY_GROUP in (" + pramStr1.trim() + ") " + " AND SIDE like 'A' ");
        else if (reqType == 4)
          // By Walk Thru
          pNQ.setWhereClause(" DOCUMENT_NUMBER in (" + pramStr1.trim() + ") " + "AND SIDE like 'A' ");
        else pNQ.setWhereClause(" SIDE like 'A' ");
        pNQ.executeQuery();
        currRow = pNQ.getRowCount();
        pNQ.first();
      }
      else if (mechFlag.compareTo("H") == 0) {

        pNQA = getPickingGenQryVOA();
        pNQA.clearCache();
        pNQA.setRangeSize(-1);
        pNQA.setOrderByClause("  QRSLT.BAY ASC, QRSLT.LOC_LEVEL ASC, QRSLT.SLOT ASC ");
        pNQA.setNamedWhereClauseParam(WAC_ID_STR, wacIdStr);
        pNQA.setNamedWhereClauseParam("issueTypeStr", iorStr);
        pNQA.setNamedWhereClauseParam("userIdStr", userId);
        // Build where clause for Horizantal
        if (reqType == 1)
          // By AAC
          pNQA.setWhereClause(" AAC in (" + pramStr1.trim() + ") " + " AND SIDE like 'A' ");
        else if (reqType == 2)
          // By Route Name
          pNQA.setWhereClause(" ROUTE_NAME in (" + pramStr1.trim() + ") " + " AND SIDE like 'A' ");
        else if (reqType == 3)
          // By priority group
          pNQA.setWhereClause(" ISSUE_PRIORITY_GROUP in (" + pramStr1.trim() + ") " + " AND SIDE like 'A' ");
        else if (reqType == 4)
          // By Walk Thru
          pNQA.setWhereClause(" DOCUMENT_NUMBER in (" + pramStr1.trim() + ") " + "AND SIDE like 'A' ");
        else pNQA.setWhereClause(" SIDE like 'A' ");
        pNQA.executeQuery();
        int aCount = pNQA.getRowCount();

        pNQB = getPickingGenQryVOB();
        pNQB.clearCache();
        pNQB.setRangeSize(-1);

        //For Side B
        pNQB.setOrderByClause("  QRSLT.BAY ASC, QRSLT.LOC_LEVEL ASC, QRSLT.SLOT ASC ");
        pNQB.setNamedWhereClauseParam(WAC_ID_STR, wacIdStr);
        pNQB.setNamedWhereClauseParam("issueTypeStr", iorStr);
        pNQB.setNamedWhereClauseParam("userIdStr", userId);
        if (reqType == 1)
          // By AAC
          pNQB.setWhereClause(" AAC in (" + pramStr1.trim() + ") " + " AND SIDE like 'B' ");
        else if (reqType == 2)
          // By Route Name
          pNQB.setWhereClause(" ROUTE_NAME in (" + pramStr1.trim() + ") " + " AND SIDE like 'B' ");
        else if (reqType == 3)
          // By priority group
          pNQB.setWhereClause(" ISSUE_PRIORITY_GROUP in (" + pramStr1.trim() + ") " + " AND SIDE like 'B' ");
        else if (reqType == 4)
          // By Walk Thru
          pNQB.setWhereClause(" DOCUMENT_NUMBER in (" + pramStr1.trim() + ") " + "AND SIDE like 'B'");
        else pNQB.setWhereClause(" SIDE like 'B' ");
        pNQB.executeQuery(); // They all may be listed in one view
        int bCount = pNQB.getRowCount();
        currRow = 0;
        //Perform the merge of the rows
        if (iorStr.length() <= 0)
          // Clear cache if it is first time trying to get the list or when all pick rows are processed
          pNQ.clearCache();
        currRow = pNQ.getCurrentRowIndex();
        int a = 0;
        int b = 0;
        addCount = 0;
        Row ro;
        while ((a < aCount) || (b < bCount)) {
          // Add the merge code
          if (a < aCount) {
            ro = pNQA.next();
            if (!validatePidAlreadyOnPickList(Long.valueOf(ro.getAttribute("Pid").toString()))) {
              if (addCount == 0) pNQ.insertRow(ro);
              else pNQ.insertRowAtRangeIndex(addCount, ro);
              addCount++;
            }
          }
          if (b < bCount) {
            ro = pNQB.next();
            if (!validatePidAlreadyOnPickList(Long.valueOf(ro.getAttribute("Pid").toString()))) {
              if (addCount == 0) pNQ.insertRow(ro);
              else pNQ.insertRowAtRangeIndex(addCount, ro);
              addCount++;
            }
          }
          a++;
          b++;
        }
        pNQA.clearCache();
        pNQB.clearCache();
        if (iorStr.length() <= 0) {
          pNQ.first();
        }
        else {
          pNQ.setCurrentRowAtRangeIndex(currRow + addCount);
        }
      }
      else {
        pNQ = getPickingGenQryVO1();
        pNQ.setRangeSize(-1);
        int fetchSize = (tasksPerTrip.equals("")) ? 12 : Integer.parseInt(tasksPerTrip);
        pNQ.setMaxFetchSize(fetchSize); // ZSL  set the fetched row based on tasks_per_trip

        pNQ.setOrderByClause(sortOrder);

        pNQ.setNamedWhereClauseParam(WAC_ID_STR, wacIdStr);
        pNQ.setNamedWhereClauseParam("issueTypeStr", iorStr);
        pNQ.setNamedWhereClauseParam("userIdStr", userId);
        // Build where clause for vertical

        if (reqType == 1)
          // By AAC
          pNQ.setWhereClause(" AAC in (" + pramStr1 + ") ");
        else if (reqType == 2)
          // By Route Name
          pNQ.setWhereClause(" ROUTE_NAME in (" + pramStr1.trim() + ") ");
        else if (reqType == 3)
          // By priority group
          pNQ.setWhereClause(" ISSUE_PRIORITY_GROUP in (" + pramStr1.trim() + ") ");
        else if (reqType == 4)
          // By Walk Thru
          pNQ.setWhereClause(" DOCUMENT_NUMBER in (" + pramStr1.trim() + ") ");
        pNQ.executeQuery();
        currRow = pNQ.getRowCount();
        pNQ.first();
        // == ZLIM start == Print pick rows
        if (currRow > 0) {
          int i = 0;
          Row ro = null;
          log.trace("== Pick Assignment ==  # of rows= {}", currRow);
          String pidsStr = "";
          while (i < currRow) {
            if (i == 0) ro = pNQ.getCurrentRow();
            if (ro != null) {
              log.trace("== Pick Assignment ==  Pid({})={}", i, ro.getAttribute("Pid"));

              //Build the PID string list
              if (pidsStr.length() > 0) pidsStr = pidsStr + ", " + ro.getAttribute("Pid");
              else pidsStr = ro.getAttribute("Pid").toString();
            }
            i++;
            ro = pNQ.next();
          }
          pNQ.first();

          //call here
          checkPidsForOtherUserAssigned(pidsStr, userId);

          // Assign user for fetched rows
          log.trace("== Pick Assignment ==  pidsStr={}", pidsStr);
          log.trace("== Pick Assignment ==  userid={}", userId);
          try (PreparedStatement stR = getDBTransaction().createPreparedStatement(
              "begin update picking set  assign_to_user = ?, modified_by = ? where pid in (" + pidsStr + ") " +
                  "and assign_to_user is null; end;", 0);) {
            stR.setString(1, userId);
            stR.setString(2, userId);
            stR.executeUpdate();
            getDBTransaction().commit();
          }
        }
        // == ZLIM end ==
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  public boolean checkPidsForOtherUserAssigned(String pids, String uid) {
    boolean noOverlap = true;

    try (PreparedStatement checkStatement = getDBTransaction().createPreparedStatement("SELECT i.DOCUMENT_NUMBER FROM picking p, issue i where i.SCN = p.SCN and p.pid in (" + pids + ") and p.assign_to_user is not null and p.assign_to_user <> ? ", 0)) {
      //check if any pids are assigned to another user
      checkStatement.setString(1, uid);
      try (ResultSet checkSet = checkStatement.executeQuery()) {
        String docString = "";
        while (checkSet.next()) {
          docString += (docString.length() > 0 ? ", " : "") + checkSet.getString(1);
        }
        if (!docString.isEmpty()) {
          log.warn("Pre assigned document numbers  {}", docString);
          noOverlap = false;
        }
      }
    }
    catch (SQLException e) {
      log.error("Error checking pids for other user assignment", e);
    }
    return noOverlap;
  }

  /**
   * Delete rows
   */
  public void deleteRowsFromVOForPicking() {
    // Check if a stow is already in the Pick list returns true if it exists else false
    try {
      boolean r = false;
      PickingGenQryVOImpl pVO = getPickingGenQryVO1();
      Row ro = pVO.first();
      if (ro != null) pVO.removeCurrentRow();
      while (pVO.hasNext()) {
        pVO.next();
        pVO.removeCurrentRow();
      }
      pVO.clearCache();
      ro = null;
      PickingGenQryVOImpl pVOA = getPickingGenQryVOA();
      ro = pVOA.first();
      if (ro != null) pVOA.removeCurrentRow();
      while (pVOA.hasNext()) {
        pVOA.next();
        pVOA.removeCurrentRow();
      }
      pVOA.clearCache();
      ro = null;
      PickingGenQryVOImpl pVOB = getPickingGenQryVOB();
      ro = pVOB.first();
      if (ro != null) pVOB.removeCurrentRow();
      while (pVOB.hasNext()) {
        pVOB.next();
        pVOB.removeCurrentRow();
      }
      pVOB.clearCache();
      getDBTransaction().commit();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  /**
   * Container's getter for PickingGenQryVOA
   */
  public PickingGenQryVOImpl getPickingGenQryVOA() {
    return (PickingGenQryVOImpl) findViewObject("PickingGenQryVOA");
  }

  /**
   * Container's getter for PickingGenQryVOB
   */
  public PickingGenQryVOImpl getPickingGenQryVOB() {
    return (PickingGenQryVOImpl) findViewObject("PickingGenQryVOB");
  }

  public void pickUpdatePid(long pidL, int uid, int suffix, String scnStr) {
    PreparedStatement stR = null;
    try { // Update the PIN
      String scn = scnStr.trim();
      if (suffix > 0) scn = scn + String.valueOf(suffix);
      stR = this.getDBTransaction().createPreparedStatement("begin update picking set pin = ?,  modified_by = ?  where pid = ?; end;", 0);
      stR.setString(1, scn);
      stR.setInt(2, uid);
      stR.setLong(3, pidL);
      int rs = stR.executeUpdate();
      this.getDBTransaction().commit();
      stR.close();
      Row r = this.getPickingGenQryVO1().getCurrentRow();
      r.refresh(r.REFRESH_CONTAINEES);
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    finally {
      try {
        if (stR != null) {
          stR.close();
        }
      }
      catch (Exception e) {
        AdfLogUtility.logException(e);
      }
    }
  }

  public void pickRowStatusChange(long pidL, int uid, int pQty, int qtyPicked, int curQtyPicked, long niinLocId) {
    PreparedStatement stR = null;
    try {
      String statusStr = "PICKED";
      stR = this.getDBTransaction().createPreparedStatement("begin update picking set status = ?, pick_qty = ?, qty_picked = ?, modified_by = ?, time_of_pick = sysdate where pid = ?; end;", 0);
      stR.setString(1, statusStr);
      stR.setInt(2, pQty);
      stR.setInt(3, qtyPicked + curQtyPicked);
      stR.setInt(4, uid);
      stR.setLong(5, pidL);
      int rs = stR.executeUpdate();
      this.getDBTransaction().commit();
      stR.close();

      stR = null;
      //Deduct the qunatity from the niin_location
      //Update qty on NIIN_LOCATION
      stR = this.getDBTransaction().createPreparedStatement("begin update niin_location set  qty = (qty - ?), old_ui = null, nsn_remark = 'N',  modified_by = ?, modified_date = sysdate where niin_loc_id = ?; end;", 0);
      stR.setInt(1, curQtyPicked);
      stR.setInt(2, uid);
      stR.setLong(3, niinLocId);
      rs = stR.executeUpdate();
      this.getDBTransaction().commit();
      stR.close();
      stR = null;
      //Create or delete rows for serial or lot number tracking in PICK_SERIAL_LOT_NUM
      this.createSrlOrLotAndPickXref(String.valueOf(pidL));
      // Get Unit Weight and Cube from NIIN_INFO, LOCATION_HEADER, LOCATION_HEADER_BIN -- This is not needed as it is handled by
      // STATEMENT_LEVEL_HEADER TRIGGER
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    finally {
      try {
        if (stR != null) {
          stR.close();
        }
      }
      catch (Exception e) {
        AdfLogUtility.logException(e);
      }
    }
  }

  // @ZSL  To Test HH incorporation 06/2018
  public void pickTestStatusChange(long pidL) {
    PreparedStatement stR = null;
    try {
      String statusStr = "ZLIM";
      stR = this.getDBTransaction().createPreparedStatement("begin update picking set status = ? where pid = ?; end;", 0);
      stR.setString(1, statusStr);
      stR.setLong(2, pidL);
      int rs = stR.executeUpdate();
      this.getDBTransaction().commit();
      stR.close();
      stR = null;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    finally {
      try {
        if (stR != null) {
          stR.close();
        }
      }
      catch (Exception e) {
        AdfLogUtility.logException(e);
      }
    }
  }

  public void clearAssignedUserInPicking(int userId) {
    PreparedStatement pstmt = null;
    try {
      //* clear assign_to_user in the picking table @ZSL 6/2018
      String sqlClear = "update picking set assign_to_user = null where assign_to_user = ?";
      pstmt = getDBTransaction().createPreparedStatement(sqlClear, 0);
      pstmt.setInt(1, userId);
      pstmt.executeUpdate();
      getDBTransaction().commit();
    }
    catch (SQLException e) {
      getDBTransaction().rollback();
      AdfLogUtility.logException(e);
    }
    finally {
      if (pstmt != null) { try { pstmt.close(); } catch (SQLException e) { pstmt = null; } }
    }
  }

  public void changeWalkThruStatus(String documentNumber, int userId) {
    String statusStr = "PICKED";
    PreparedStatement stR = null;
    // Replace modified_by to MOD_BY_ID
    stR = this.getDBTransaction().createPreparedStatement("begin update WALKTHRU_QUEUE set status = ?, MOD_BY_ID = ? where DOCUMENT_NUMBER = ?; end;", 0);
    try {
      stR.setString(1, statusStr);
      stR.setInt(2, userId);
      stR.setString(3, documentNumber);
      int rs = stR.executeUpdate();
      this.getDBTransaction().commit();
      stR.close();
      stR = null;
    }
    catch (SQLException e) {
      AdfLogUtility.logException(e);
    }
    finally {
      try {
        if (stR != null) {
          stR.close();
        }
      }
      catch (Exception e) {
        AdfLogUtility.logException(e);
      }
    }
  }

  public int pickRowByPass(String statusStr, int byPassCount, Object pidL, int uid) {
    PreparedStatement stR = null;
    try {
      int bc = byPassCount;
      stR = this.getDBTransaction().createPreparedStatement("begin update picking set status = ?, bypass_count = ?, modified_by = ? where pid = ?; end;", 0);
      if (bc == 1) stR.setString(1, statusStr + "1");
      else stR.setString(1, statusStr + "2");
      stR.setInt(2, bc);
      stR.setInt(3, uid);
      stR.setString(4, pidL.toString());
      int rs = stR.executeUpdate();
      this.getDBTransaction().commit();
      stR.close();
      return bc;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
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
   * Validate if row already in the list
   */
  public boolean validatePidAlreadyOnPickList(long pidL) {
    // Check if a pick is already in the pick list returns true if it exists else false
    try {
      boolean r = false;
      PickingGenQryVOImpl pVO = getPickingGenQryVO1();
      pVO.first();
      if ((pVO.getCurrentRow() != null) && (Long.valueOf(pVO.getCurrentRow().getAttribute("Pid").toString()) == pidL)) return true;
      while (pVO.hasNext()) {
        if (Long.valueOf(pVO.next().getAttribute("Pid").toString()) == pidL) {
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
   * Container's getter for PickByAACQryVO1
   */
  public PickByAACQryVOImpl getPickByAACQryVO1() {
    return (PickByAACQryVOImpl) findViewObject("PickByAACQryVO1");
  }

  /**
   * Container's getter for PickingByPriorityQryVO1
   */
  public PickingByPriorityQryVOImpl getPickingByPriorityQryVO1() {
    return (PickingByPriorityQryVOImpl) findViewObject("PickingByPriorityQryVO1");
  }

  /**
   * Container's getter for PickingByRouteName1
   */
  public PickingByRouteNameImpl getPickingByRouteName1() {
    return (PickingByRouteNameImpl) findViewObject("PickingByRouteName1");
  }

  /**
   * Container's getter for PickByWalkThruVO1
   */
  public PickByWalkThruImpl getPickingByWalkThruVO1() {
    return (PickByWalkThruImpl) findViewObject("PickingByWalkThruVO1");
  }

  /**
   * Container's getter for PickByPassQryVO1
   */
  public PickByPassQryVOImpl getPickByPassQryVO1() {
    return (PickByPassQryVOImpl) findViewObject("PickByPassQryVO1");
  }

  public WorkLoadManagerImpl getWorkloadManagerService() {
    try {
      WorkLoadManagerImpl workloadManagerService = (WorkLoadManagerImpl) getWorkLoadManager1();
      return workloadManagerService;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return null;
  }

  /**
   * Container's getter for WorkLoadManager1
   */
  public ApplicationModuleImpl getWorkLoadManager1() {
    return (ApplicationModuleImpl) findApplicationModule("WorkLoadManager1");
  }

  /**
   * Get the Pick AAC rows
   */
  public void buildCustomerRS(long reqType, long equipmentNumber, String userId, String pramStr1, String iorStr) {
    try {
      boolean r = false;
      String wacIdStr = "";
      String mechFlag = "";
      PickByAACQryVOImpl cNQ = getPickByAACQryVO1();
      try (PreparedStatement stR = this.getDBTransaction().createPreparedStatement("select w.mechanized_flag, w.wac_id from wac w, equip e where e.wac_id = w.wac_id and e.equipment_number = ?", 0)) {
        stR.setLong(1, equipmentNumber);
        try (ResultSet rs = stR.executeQuery()) {
          if (rs.next()) {
            mechFlag = rs.getString(1);
            wacIdStr = rs.getString(2);
          }
        }
      }
      //Build the customer query
      cNQ.setNamedWhereClauseParam(WAC_ID_STR, wacIdStr);
      cNQ.setNamedWhereClauseParam("userIdStr", userId);
      cNQ.executeQuery();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  /**
   * Get the Pick Route rows
   */
  public void buildRouteRS(long reqType, long equipmentNumber, String userId, String pramStr1, String iorStr) {
    try {
      boolean r = false;
      String wacIdStr = "";
      String mechFlag = "";
      if (getPickingByRouteName1() != null) getPickingByRouteName1().clearCache();
      PickingByRouteNameImpl rNQ = getPickingByRouteName1();
      try (PreparedStatement stR = this.getDBTransaction().createPreparedStatement("select w.mechanized_flag, w.wac_id from wac w, equip e where e.wac_id = w.wac_id and e.equipment_number = ?", 0)) {
        stR.setLong(1, equipmentNumber);
        try (ResultSet rs = stR.executeQuery()) {
          if (rs.next()) {
            mechFlag = rs.getString(1);
            wacIdStr = rs.getString(2);
          }
        }
      }
      //Build the Route query
      rNQ.setNamedWhereClauseParam(WAC_ID_STR, (Object) wacIdStr);
      rNQ.setNamedWhereClauseParam("userIdStr", (Object) userId);
      rNQ.executeQuery();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  /**
   * Get the Pick Priority rows
   */
  public void buildPriorityRS(long reqType, long equipmentNumber, String userId, String pramStr1, String iorStr) {
    try {
      boolean r = false;
      String wacIdStr = "";
      String mechFlag = "";
      if (getPickingByPriorityQryVO1() != null) getPickingByPriorityQryVO1().clearCache();
      PickingByPriorityQryVOImpl pNQ = getPickingByPriorityQryVO1();
      try (PreparedStatement stR = this.getDBTransaction().createPreparedStatement("select w.mechanized_flag, w.wac_id from wac w, equip e where e.wac_id = w.wac_id and e.equipment_number = ?", 0)) {
        stR.setLong(1, equipmentNumber);
        try (ResultSet rs = stR.executeQuery()) {
          if (rs.next()) {
            mechFlag = rs.getString(1);
            wacIdStr = rs.getString(2);
          }
        }
      }
      //Build the Priority query
      pNQ.setNamedWhereClauseParam(WAC_ID_STR, (Object) wacIdStr);
      pNQ.executeQuery();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  /**
   * Get the Pick Walk Thru rows
   */
  public void buildWalkThruRS(long reqType, long equipmentNumber, String userId, String pramStr1, String iorStr) {
    try {
      boolean r = false;
      String wacIdStr = "";
      String mechFlag = "";
      if (getPickingByWalkThruVO1() != null) getPickingByWalkThruVO1().clearCache();
      PickByWalkThruImpl rNQ = getPickingByWalkThruVO1();
      try (PreparedStatement stR = this.getDBTransaction().createPreparedStatement("select w.mechanized_flag, w.wac_id from wac w, equip e where e.wac_id = w.wac_id and e.equipment_number = ?", 0)) {
        stR.setLong(1, equipmentNumber);
        try (ResultSet rs = stR.executeQuery()) {
          if (rs.next()) {
            mechFlag = rs.getString(1);
            wacIdStr = rs.getString(2);
          }
        }
      }
      //Build the Walk Thru query
      rNQ.setNamedWhereClauseParam(WAC_ID_STR, (Object) wacIdStr);
      rNQ.setNamedWhereClauseParam("userIdStr", (Object) userId);
      rNQ.executeQuery();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  public int pickRowRefused(String statusStr, int refCount, Object pinS, int uid) {
    PreparedStatement stR = null;
    try {
      int bc = refCount;
      stR = this.getDBTransaction().createPreparedStatement("begin update picking set status = ?,  modified_by = ?, refused_by=?, refused_date=sysdate where pid = ?; end;", 0);
      stR.setString(1, statusStr);
      stR.setInt(2, uid);
      stR.setInt(3, uid);
      stR.setInt(4, Integer.parseInt(pinS.toString()));
      int rs = stR.executeUpdate();
      this.getDBTransaction().commit();
      stR.close();
      return bc;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    finally {
      try {
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
   * Skip the picks in queue if the location and/or status got changed
   */
  public void skipPicksForLocationOrStatusChange() {
    try {
      boolean flag = false;
      PickingGenQryVOImpl pVOx = getPickingGenQryVO1();
      while ((flag == false) && (pVOx.hasNext())) {
        pVOx.next(); //Keep going for next until we find a pick that has with no status and niin location changes
        try (PreparedStatement stR = this.getDBTransaction().createPreparedStatement("select p.niin_loc_id, p.status from picking p where p.pid = ?", 0)) {
          stR.setString(1, (pVOx.getCurrentRow().getAttribute("Pid") == null ? "0" : pVOx.getCurrentRow().getAttribute("Pid").toString()));
          try (ResultSet rs = stR.executeQuery()) {
            if (rs.next() && (rs.getString(1).equalsIgnoreCase((pVOx.getCurrentRow().getAttribute("NiinLocId") == null ? "0" : pVOx.getCurrentRow().getAttribute("NiinLocId").toString()))) && (rs.getString(2).equalsIgnoreCase((pVOx.getCurrentRow().getAttribute("Status") == null ? "0" : pVOx.getCurrentRow().getAttribute("Status").toString())))) {
              flag = true; //No change in location and status so we can process this row item for pick
            }
          }
        }
      }
      if (flag == true) pVOx.previous(); //Move back pick pointer so that the row item can be picked
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  public int getNiinLocationQuantity(long niinLocId) {
    String sql = "select qty from niin_location where niin_loc_id = ?";
    try (PreparedStatement stR = this.getDBTransaction().createPreparedStatement(sql, 0)) {
      int bc = 0;
      stR.setLong(1, niinLocId);
      try (ResultSet rs = stR.executeQuery()) {
        if (rs.next()) {
          bc = Integer.parseInt(rs.getString(1));
        }
      }
      return bc;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return 0;
  }

  /**
   * Builds the serial or lot con num VO list
   */
  public String addSerialOrLotNumVOList(String slNum, String niinIdStr, String qtyStr, String ccStr, String niinLocId, String niinStr, int pickqty) {
    PreparedStatement stR = null;
    ResultSet rs = null;
    String sqlStr;
    String locIdStr = "1";
    try {
      boolean flag = false;
      int err = 0;
      String id = "";
      int serialFlag = 0;
      int qtyl = 0;
      boolean found = false;
      int storQty = 0;
      String warnMsg = "";
      if (StringUtils.isEmpty(slNum)) return "Needs serial or lot con number."; //Invalid value
      stR = getDBTransaction().createPreparedStatement("select sr.serial_number, sr.lot_con_num, nl.location_id, sr.qty " + " from  serial_lot_num_track sr, niin_location nl " + " where nl.niin_id=sr.niin_id and  nl.location_id = sr.location_id and " + "        nl.niin_loc_id = ? and " + "        (sr.serial_number = ? or sr.lot_con_num = ?)", 0);
      stR.setString(1, niinLocId);
      stR.setString(2, (slNum.length() <= 0 ? "NONE" : slNum));
      stR.setString(3, (slNum.length() <= 0 ? "NONE" : slNum));
      rs = stR.executeQuery();
      if (!rs.next()) {
        warnMsg = "Warning: Srl# " + slNum + " of niin " + niinStr + " does't belong to this location. NIIN inventory is recommended for this item.";
      }
      else {
        locIdStr = rs.getString(3);
        qtyl = rs.getInt(4);
      }
      rs.close();
      rs = null;
      stR.close();
      stR = null;

      if ((warnMsg.length() > 0) || (Integer.parseInt(qtyStr) > qtyl)) {
        sqlStr = " select serial_lot_num_track_id, nvl(serial_number,'') serial_number, nvl(lot_con_num,'') lot_con_num, " + "              nvl(cc,'A'), to_char(expiration_date,'YYYYMM') exp_dt, to_char(sysdate,'YYYYMM') cur_dt, " + "              nvl(issued_flag,'N'), qty, location_id from serial_lot_num_track where ((nvl(serial_number,'NONE') =  ?) or (nvl(lot_con_num,'NONE')= ?)) and niin_id = ? and cc = ? and location_id = " + locIdStr + " order by qty desc ";
        if (Integer.parseInt(qtyStr) > qtyl)
          warnMsg = "Warning: Qty is less then expected for this serial/lot num at the selected location.";
      }
      else {
        sqlStr = " select serial_lot_num_track_id, nvl(serial_number,'') serial_number, nvl(lot_con_num,'') lot_con_num, " + "              nvl(cc,'A'), to_char(expiration_date,'YYYYMM') exp_dt, to_char(sysdate,'YYYYMM') cur_dt, " + "              nvl(issued_flag,'N'), qty, location_id from serial_lot_num_track where ((nvl(serial_number,'NONE') =  ?) or (nvl(lot_con_num,'NONE')= ?)) and niin_id = ? and cc = ? and location_id = " + locIdStr;
      }
      stR = getDBTransaction().createPreparedStatement(sqlStr, 0);
      stR.setString(1, (slNum.length() <= 0 ? "NONE" : slNum));
      stR.setString(2, (slNum.length() <= 0 ? "NONE" : slNum));
      stR.setInt(3, Integer.parseInt(niinIdStr));
      stR.setString(4, ccStr);
      rs = stR.executeQuery();
      if (rs.next()) {
        id = rs.getString(1);
        if (rs.getString(2) != null && rs.getString(7).equalsIgnoreCase("Y")) err = 7;
        if (((err <= 0) && (rs.getString(2) != null)) && ((qtyStr.length() > 0) && (Integer.parseInt(qtyStr) > 1)))
          err = 8;
        if (rs.getString(2) != null && rs.getString(2).length() > 0) serialFlag = 1;
        storQty = rs.getInt(8);
        locIdStr = rs.getString(9);
        found = true;
      }
      rs.close();
      rs = null;
      stR.close();
      stR = null;
      if (!found) return id + "Serial Number (" + slNum + ") is invalid. Re-enter or Bypass.";
      if (err == 7) return id + " STRATIS indicates item with serial number (" + slNum + ") is already issued.";
      if (err == 8) return " Qty is 1 for serial.";
      if (Integer.parseInt(qtyStr) > storQty) return " Qty not supported by this serial/lot.";

      if (serialFlag != 1 && qtyStr.length() <= 0) return id + "Lot Qty required.";

      if (id.length() <= 0)
        return slNum + " " + niinIdStr + " " + "STRATIS does not carry item with this serial number. Please correct it.";
      SerialOrLotNumListImpl vo;
      Row row;
      vo = this.getSerialOrLotNumList1();
      int cou = 0;
      if (vo != null) {
        cou = vo.getRowCount();
        if (pickqty <= cou) {
          return "Enough Serial Numbers already entered.";
        }
        int i = 0;
        while ((i < cou) && (!flag)) {
          if (i == 0) row = vo.first();
          else row = vo.next();
          if (row != null && row.getAttribute(SERIAL_OR_LO_NUM).toString().equalsIgnoreCase(slNum)) flag = true;
          i++;
        }
        if (flag) return slNum + " is already entered."; //Already scanned in
        row = vo.createRow();
        row.setAttribute(SERIAL_OR_LO_NUM, slNum);
        row.setAttribute(SRL_LOT_NUM_TRACK_ID, id);
        row.setAttribute(QTY_LOT, qtyStr);
        row.setAttribute("LocationId", locIdStr);
        vo.insertRow(row);
      }
      if (warnMsg.length() > 0)
        this.getWorkloadManagerService().createErrorQueueRecord("58", "SRL_LOT_NUM_TRACK_ID", id, "1", null);
      return warnMsg;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    finally {
      try {
        if (stR != null) {
          stR.close();
        }
        if (rs != null) {
          rs.close();
        }
      }
      catch (Exception e) {
        AdfLogUtility.logException(e);
      }
    }
    return "Row addition to Serial or Lot Con List failed.";
  }

  /**
   * Validate the serial number and lot control numbers submitted on a
   * hardcard walk thru.
   * <p>
   * - Do we have the correct number of serial numbers/lot control numbers to fulfill the walk thru
   * - Are there any duplication of serial numbers/lot control numbers in the list submitted
   * - Validate the serial numbers/lot control numbers in the database for the niin
   * (not already issued, condition code A, not expired, etc.)
   * - Change made to proper serial number to location mappings for hard cards to different locations
   * Used primarily in hardcard walk thru
   *
   * @return String
   * @date 10/10/08
   */
  public String validateSerialOrLotNumVOList(String niinId, String niin, String expectedListSize, boolean isSerial, boolean isLot, String cc, String locationIds, boolean multipleLocations, List<Map<String, Object>> listScans) {
    HashMap<String, String> dupLot = new HashMap<String, String>();
    HashMap<String, String> dupSerial = new HashMap<String, String>();
    HashMap<Integer, Integer> locationQty = null;
    HashMap<Integer, Integer> locationCount = null;
    HashMap<Integer, String> locationLabels = null;
    HashMap<Integer, String> locationSerials = null;
    SerialOrLotNumListImpl view = null;
    try {

      //Set up if a hardcard walk-thru with multiple locations
      if (multipleLocations) {
        locationQty = new HashMap<Integer, Integer>();
        locationLabels = new HashMap<Integer, String>();
        locationSerials = new HashMap<Integer, String>();
        locationCount = new HashMap<Integer, Integer>();

        HashMap temp;
        int locationId = 0;
        int qty = 0;
        String locationLabel = "";

        for (int i = 0; i < listScans.size(); i++) {
          temp = (HashMap) listScans.get(i);
          locationId = Integer.parseInt(temp.get("locationid").toString());
          qty = Integer.parseInt(temp.get("qty").toString());
          locationLabel = temp.get("locationlabel").toString();
          locationQty.put(locationId, qty);
          locationLabels.put(locationId, locationLabel);
          locationSerials.put(locationId, "");
          locationCount.put(locationId, 0);
        }
      }

      view = this.getSerialOrLotNumList1();
      int v = 0;
      while (v < view.getEstimatedRowCount()) {
        Row row = (v == 0) ? view.first() : view.next();

        v++;
        if (isSerial)
          if (Util.isEmpty(row.getAttribute(SERIAL_OR_LO_NUM))) return "Missing Serial Number and/or Lot Control Number(s). " + v;
        if (isLot) if (Util.isEmpty(row.getAttribute(LOT_CON_NUM))) return "Missing Lot Control Number and/or Serial Number(s).";
        if (Util.isEmpty(row.getAttribute(QTY_LOT))) return "Missing Quantity, Serial, and/or Lot Control Number(s)";
      }

      if (isSerial) {
        // serial only or serial and lot controlled
        if (view.getEstimatedRowCount() < Long.parseLong(expectedListSize)) {
          return "Missing Serial Number.";
        }
        if (view.getEstimatedRowCount() > Long.parseLong(expectedListSize)) {
          return "Too many Serial Number, expected " + expectedListSize + ".";
        }
      }
      else {
        // lot controlled only
        if (view.getEstimatedRowCount() < 1) {
          return "Missing Lot Control Number.";
        }
        if (view.getEstimatedRowCount() > Long.parseLong(expectedListSize)) {
          return "Too many Lot Control Number, should not exceed quantity of pick " + expectedListSize + ".";
        }

        int w = 0;
        int countUp = 0;
        while (w < view.getEstimatedRowCount()) {
          Row row = (w == 0) ? view.first() : view.next();
          if (row != null) {
            //* every lot quantity is required
            int qtyLot = Util.cleanInt(row.getAttribute(QTY_LOT));
            if (qtyLot < 1) {
              return "Every Lot Control Number requires a lot quantity greater than 0.";
            }

            //* is the total sum of the lots greater than the walkthru request
            countUp += qtyLot;
          }
          w++;
        }

        if (countUp != Integer.parseInt(expectedListSize)) {
          return "Total Sum of Lot Control Number Quantity, should be exact quantity of pick " + expectedListSize + ".";
        }
      }

      //* check for duplicates
      boolean flag = false;
      int i = 0;
      while (!flag && i < view.getEstimatedRowCount()) {
        Row row = (i == 0) ? view.first() : view.next();
        if (row != null) {
          if (isSerial) {
            if (dupSerial.get(row.getAttribute(SERIAL_OR_LO_NUM).toString()) == null)
              dupSerial.put(row.getAttribute(SERIAL_OR_LO_NUM).toString(), row.getAttribute(SERIAL_OR_LO_NUM).toString());
            else flag = true;
          }
          ///* lot rows can have duplicates if item is both serial and lot
          if (isLot && !isSerial) {
            if (dupLot.get(row.getAttribute(LOT_CON_NUM).toString()) == null)
              dupLot.put(row.getAttribute(LOT_CON_NUM).toString(), row.getAttribute(LOT_CON_NUM).toString());
            else flag = true;
          }
        }
        i++;
      }
      if (flag) return "Serial Number list should NOT contain duplicates.";
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      return "A System Error has occurred";
    }

    //* validate that not already in use and/or exist
    int err = 0;
    PreparedStatement pstmt = null;
    try {
      String sql = "select serial_lot_num_track_id, nvl(serial_number,'') serial_number, nvl(lot_con_num,'') lot_con_num, " + "nvl(cc,'A'), to_char(expiration_date,'YYYYMM') exp_dt, to_char(sysdate,'YYYYMM') cur_dt, " + "nvl(issued_flag,'N'), qty, location_id from serial_lot_num_track where niin_id = ? ";
      int commaPlace = locationIds.indexOf(',');

      if (isSerial) {
        sql += "and (nvl(serial_number,'') = ?) order by serial_lot_num_track_id desc";
      }

      if (isLot) {
        //* there can be multiple rows with same lot number and niin_id
        sql += " and cc=? and (nvl(lot_con_num,'') = ?)";
      }

      String title = (isSerial && isLot) ? "Serial Number and Lot Control Number" : ((isSerial) ? "Serial Number" : "Lot Control Number");

      pstmt = getDBTransaction().createPreparedStatement(sql, 0);
      StringBuilder results = new StringBuilder();

      int i = 0;
      while (i < view.getEstimatedRowCount()) {
        Row row = (i == 0) ? view.first() : view.next();
        String serialNum = (isSerial) ? row.getAttribute(SERIAL_OR_LO_NUM).toString().trim().toUpperCase() : "";
        String lotNum = (isLot) ? row.getAttribute(LOT_CON_NUM).toString().trim().toUpperCase() : "";
        String qtyLot = row.getAttribute(QTY_LOT).toString();
        pstmt.setInt(1, Integer.parseInt(niinId));
        int s = 1;

        if (isSerial) pstmt.setString(++s, serialNum);
        if (isLot) pstmt.setString(++s, cc);
        if (isLot) pstmt.setString(++s, lotNum);
        try (ResultSet rs = pstmt.executeQuery()) {
          if (rs.next()) {
            String id = rs.getString(1);
            String qty = rs.getString(8);

            if (rs.getString(7).equalsIgnoreCase("Y")) {
              err = 7; // check issued flag
              results.append('\n');
              if (isSerial) results.append(serialNum).append(" - ");
              if (isLot) results.append(lotNum).append(" - ");
              results.append("STRATIS indicates item with this ").append(title).append(" is already issued.");
            }
            //** hardcard walkthru should be able to pick condition code F
            if (isSerial) {
              if ((err <= 0) && !(rs.getString(4).equalsIgnoreCase(cc))) {
                err = 4; // check condition code
                results.append('\n');
                if (isSerial) results.append(serialNum).append(" - ");
                if (isLot) results.append(lotNum).append(" - ");
                results.append(" - STRATIS indicates item with this ").append(title).append(" is of wrong condition code.");
              }
            }

            //** hardcard walkthru and change CC should ignore expiration date - 12/18/08

            if (!isSerial) {
              if ((err <= 0) && Integer.parseInt(qtyLot) > Integer.parseInt(qty)) {
                err = 9; //* check if qty exceeds amount in tracking table
                results.append('\n');
                if (isSerial) results.append(serialNum).append(" - ");
                if (isLot) results.append(lotNum).append(" - ");
                results.append("STRATIS indicates item with this ").append(title).append(" exceeds lot quantity amount of ").append(qty).append(" in STRATIS.");
              }
            }

            if (err <= 0) {

              //* added 3/27/09 to support location tracking
              boolean foundLocation = false;
              StringTokenizer st = new StringTokenizer(locationIds, ",");
              while (st.hasMoreTokens() && !foundLocation) {
                String locationId = st.nextToken();
                if (locationId.equalsIgnoreCase(rs.getString(9))) {
                  foundLocation = true;
                }
              }
              if (!foundLocation) {
                results.append('\n');
                if (isSerial) results.append(serialNum).append(" - ");
                if (isLot) results.append(lotNum).append(" - ");
                results.append("STRATIS indicates item with this ").append(title).append(" is not at this location,").append(id);
              }
              else {
                if (multipleLocations && locationSerials != null && locationCount != null) {
                  int locationcheck, count;
                  String locationSerial;
                  locationcheck = rs.getInt(9);
                  count = locationCount.get(locationcheck) + 1;
                  locationCount.put(locationcheck, count);
                  if ("".equals(locationSerials.get(locationcheck))) {
                    locationSerial = serialNum;
                  }
                  else {
                    locationSerial = locationSerials.get(locationcheck) + ", " + serialNum;
                  }
                  locationSerials.put(locationcheck, locationSerial);
                }
              }

              row.setAttribute(SRL_LOT_NUM_TRACK_ID, id);
            }
          }
          else {
            results.append('\n');
            if (isSerial) results.append(serialNum).append(" - ");
            if (isLot) results.append(lotNum).append(" - ");
            results.append("STRATIS does not carry item ").append(niin).append(" with this ").append(title).append(" for this condition code ").append(cc).append(".");
          }
        }
        i++;
      }

      if (multipleLocations && locationCount != null && locationQty != null) {
        boolean flag = false;
        for (int key : locationQty.keySet()) {
          log.trace("locationId: {} Expected Qty: {} Counted Qty: {}", key, locationQty.get(key), locationCount.get(key));
          if (!locationQty.get(key).equals(locationCount.get(key))) {
            flag = true;
          }
        }

        if (flag && locationLabels != null && locationSerials != null) {
          results.append("Serial Qty/Location Qty mismatch |");
          for (int key : locationQty.keySet()) {
            results.append("Location").append(locationLabels.get(key)).append(" Expected Qty:").append(locationQty.get(key)).append(" Counted Qty: ").append(locationCount.get(key)).append(" Serials (").append(locationSerials.get(key)).append(") |");
          }
        }
      }

      if (pstmt != null) pstmt.close();
      pstmt = null;

      return (results.length() > 0) ? results.toString() : "";
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    finally {
      try {
        if (pstmt != null) pstmt.close();
      }
      catch (Exception e) {
        AdfLogUtility.logException(e);
      }
    }

    return "error.";
  }

  /**
   * Created 03-30-09 to support tracking serials by location
   */
  public String updateLocationForSerial(String niinId, String fromLocationId, String toLocationId, String qtyToBeChanged, String ccChangeTo, boolean isSerial, boolean isLot, int iUserId) {
    PreparedStatement pstmt = null;
    StringBuilder results = new StringBuilder();
    SerialOrLotNumListImpl view = null;

    String order = (ccChangeTo.equals("F")) ? " DESC" : " ASC";

    try {
      boolean stop = false;
      //* special cases for lot only
      if (!isSerial) {
        String sqlSEL = "select serial_lot_num_track_id, qty, cc, to_char(expiration_date,'YYYY-MM-DD HH24:MI:SS'), location_id from serial_lot_num_track where lot_con_num = ? and niin_id=? and location_id=? and issued_flag='N' order by cc" + order;
        try (PreparedStatement pstmtSEL = getDBTransaction().createPreparedStatement(sqlSEL, 0)) {
          view = this.getSerialOrLotNumList1();
          int i = 0;
          while (i < view.getEstimatedRowCount() && !stop) {
            int r = 0;
            boolean error = false;
            boolean match = false;
            String matchId = "";
            String deductId = "";
            String addId = "";
            String expDate = "";
            boolean secondRowFound = false;

            Row row = (i == 0) ? view.first() : view.next();
            int lotQty = Util.cleanInt(row.getAttribute(QTY_LOT));
            String lot_con_num = row.getAttribute(LOT_CON_NUM).toString();
            pstmtSEL.setString(1, lot_con_num);
            pstmtSEL.setInt(2, Integer.parseInt(niinId));
            pstmtSEL.setInt(3, Integer.parseInt(fromLocationId));
            try (ResultSet rs = pstmtSEL.executeQuery()) {
              //* inspect the actual database rows

              while (rs.next() && !error && !secondRowFound) {
                String trackId = rs.getString(1);
                String cc = rs.getString(3);
                expDate = rs.getString(4);
                if (cc.equals(ccChangeTo)) {
                  //* inspect this row first
                  if (r == 0) {

                    //* if lotqty match, then just change the cc if there is no other row
                    //* if lotqty greater than availqty, then deduct the lotqty
                    int availQty = rs.getInt(2);
                    if (lotQty == availQty) {
                      //* first verify if there is no other row
                      match = true;
                      matchId = trackId;
                    }
                    else if (lotQty > availQty) {
                      //* invalid
                      error = true;
                    }
                    else if (lotQty < availQty) {
                      //* deduct qty
                      match = true;
                      deductId = trackId;
                    }
                  }
                  else {
                    if (!match) error = true;
                  }
                }
                else {
                  if (r == 0) {
                    error = true;
                  }

                  //* inspect the second row, only should ever have 2 max rows,
                  //* one for A and one for F
                  if (r == 1) {
                    secondRowFound = true;
                    //* deduct from the deduct row and add to this new CC row
                    addId = trackId;
                  }
                }
                r++;
              }
            }

            if (error) stop = true;

            if ((match || secondRowFound) && !error) {
              if (secondRowFound) {
                //* deduct lotqty from the match row and add lotqty to this new add CC row
                log.trace("subtract from {} and add to {} lotQty of {}", (Util.isEmpty(deductId) ? matchId : deductId), addId, lotQty);

                //* update the quantity in the serial table to move from a location to another location
                pstmt = getDBTransaction().createPreparedStatement("update serial_lot_num_track set qty=qty-? where serial_lot_num_track_id=? and niin_id = ? and location_id=?", 0);

                pstmt.setInt(1, lotQty);
                pstmt.setString(2, Util.isEmpty(deductId) ? matchId : deductId);
                pstmt.setInt(3, Integer.parseInt(niinId));
                pstmt.setString(4, fromLocationId);

                pstmt.executeUpdate();

                pstmt.close();
                pstmt = null;

                pstmt = getDBTransaction().createPreparedStatement("update serial_lot_num_track set qty=qty+? where serial_lot_num_track_id=? and niin_id = ? and location_id=?", 0);

                pstmt.setInt(1, lotQty);
                pstmt.setString(2, addId);
                pstmt.setInt(3, Integer.parseInt(niinId));
                pstmt.setString(4, toLocationId);

                pstmt.executeUpdate();

                pstmt.close();
                pstmt = null;
              }
              else {
                if (Util.isEmpty(deductId)) {
                  //* change the cc on the row only
                  log.trace("change the location on the row only {}", matchId);

                  //* update the condition code in the serial table
                  pstmt = getDBTransaction().createPreparedStatement("update serial_lot_num_track set location_id=? where serial_lot_num_track_id=? and niin_id = ?", 0);

                  pstmt.setString(1, toLocationId);
                  pstmt.setString(2, matchId);
                  pstmt.setInt(3, Integer.parseInt(niinId));

                  pstmt.executeUpdate();
                  pstmt.close();
                  pstmt = null;
                }
                else {
                  //* deduct qty and add new row

                  //* deduct lotqty from the deduct row and add lotqty to this new add CC row
                  log.trace("subtract from {} and add new row lotQty of {} with condition code of {}", deductId, lotQty, ccChangeTo);

                  //* update the quantity in the serial table
                  pstmt = getDBTransaction().createPreparedStatement("update serial_lot_num_track set qty=qty-? where serial_lot_num_track_id=? and niin_id = ? and location_id=?", 0);

                  pstmt.setInt(1, lotQty);
                  pstmt.setString(2, deductId);
                  pstmt.setInt(3, Integer.parseInt(niinId));
                  pstmt.setString(4, fromLocationId);

                  pstmt.executeUpdate();
                  pstmt.close();
                  pstmt = null;

                  String sql = "insert into serial_lot_num_track (lot_con_num, niin_id, qty, cc,location_id, ";
                  sql += (!Util.isEmpty(expDate)) ? "expiration_date, " : "";
                  sql += "issued_flag, timestamp) values (?,?,?,?,?,";
                  sql += (!Util.isEmpty(expDate)) ? "to_date(?,'YYYY-MM-DD HH24:MI:SS')," : "";
                  sql += "'N',sysdate)";

                  pstmt = getDBTransaction().createPreparedStatement(sql, 0);
                  pstmt.setString(1, lot_con_num);
                  pstmt.setInt(2, Integer.parseInt(niinId));
                  pstmt.setInt(3, lotQty);
                  pstmt.setString(4, ccChangeTo);
                  pstmt.setString(5, toLocationId);
                  if (!Util.isEmpty(expDate)) pstmt.setString(6, expDate);

                  pstmt.executeUpdate();
                  pstmt.close();
                  pstmt = null;
                }
              }
            }

            i++;
          }
        }
      }
      else {
        //* this case is for serial and for serial and lot both

        //* update the location only in the serial table
        view = this.getSerialOrLotNumList1();
        pstmt = getDBTransaction().createPreparedStatement("update serial_lot_num_track set location_id=? where serial_lot_num_track_id=? and niin_id = ?", 0);

        int i = 0;
        while (i < view.getEstimatedRowCount()) {
          Row row = (i == 0) ? view.first() : view.next();
          String trackId = row.getAttribute(SRL_LOT_NUM_TRACK_ID).toString();
          pstmt.setString(1, toLocationId);
          pstmt.setString(2, trackId);
          pstmt.setInt(3, Integer.parseInt(niinId));
          pstmt.addBatch();
          i++;
        }
        pstmt.executeBatch();
        pstmt.close();
        pstmt = null;
      }

      if (!stop) {
        getDBTransaction().commit();
        if (pstmt != null) pstmt.close();
        pstmt = null;
      }
      else {
        results.append("error - update serial/lot numbers");
        getDBTransaction().rollback();
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      results.append("error ").append(e);
      getDBTransaction().rollback();
    }
    finally {
      try {
        if (pstmt != null) pstmt.close();
      }
      catch (Exception e) {
        AdfLogUtility.logException(e);
      }
    }

    return results.toString();
  }
  
  public String addToSerialLotList(String num, String niinIdStr, String qtyStr, boolean isSerial, String locationIds, boolean ignoreLocation) {
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    try {

      String id = "";
      if (Util.isEmpty(num)) return "Needs serial or lot con number.";

      if (!RegUtils.isAlphaNumericDashes(num)) {
        return "Serial Numbers can contain alphanumeric, underscore, and/or hyphen only.";
      }

      Row row = null;
      boolean flag = false;
      SerialOrLotNumListImpl vo = this.getSerialOrLotNumList1();
      int cou = vo.getRowCount();
      int i = 0;
      if (isSerial) {
        while ((i < cou) && !flag) {
          row = (i == 0) ? vo.first() : vo.next();
          if (row != null && row.getAttribute(SERIAL_OR_LO_NUM) != null && row.getAttribute(SERIAL_OR_LO_NUM).toString().equalsIgnoreCase(num))
            flag = true;
          i++;
        }
      }
      else {
        while ((i < cou) && !flag) {
          row = (i == 0) ? vo.first() : vo.next();
          if (row != null && row.getAttribute(LOT_CON_NUM).toString().equalsIgnoreCase(num)) flag = true;
          i++;
        }
      }
      if (flag) return num + " is already entered."; //Already scanned in
      log.trace("locationIds= {}", locationIds);
      //* added 3/27/09 - to track locations
      String location_tracking_message = "";
      String location_id = "";
      String sql = "select serial_lot_num_track_id, location_id from serial_lot_num_track ";
      if (isSerial) sql += "where serial_number=? and niin_id=? and issued_flag = 'N'";
      else sql += "where lot_con_num=? and niin_id=? and issued_flag = 'N'";
      try (PreparedStatement selectSerial = getDBTransaction().createPreparedStatement(sql, 0)) {
        selectSerial.setString(1, num);
        selectSerial.setInt(2, Integer.parseInt(niinIdStr));
        try (ResultSet serialResult = selectSerial.executeQuery()) {
          if (serialResult.next()) {
            id = serialResult.getString(1);
            location_id = serialResult.getString(2);
          }
        }
      }

      boolean foundLocation = true;

      //* added 3/27/09 - to track locations
      if (!ignoreLocation) {
        StringTokenizer st = new StringTokenizer(locationIds, ",");
        foundLocation = false;
        while (st.hasMoreTokens() && !foundLocation) {
          String locationId = st.nextToken();
          if (locationId.equalsIgnoreCase(location_id)) foundLocation = true;
        }
        if (!foundLocation) location_tracking_message = "Not available in location";
      }

      if (foundLocation) {
        row = vo.createRow();
        if (isSerial) row.setAttribute(SERIAL_OR_LO_NUM, num);
        else row.setAttribute(LOT_CON_NUM, num);
        row.setAttribute(SRL_LOT_NUM_TRACK_ID, id);
        row.setAttribute(QTY_LOT, 1);
        row.setAttribute("LocationId", location_id);
        vo.insertRow(row);
      }
      return location_tracking_message;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    finally {
      try {

        if (rs != null) {
          rs.close();
        }
        if (pstmt != null) {
          pstmt.close();
        }
      }
      catch (Exception e) {
        AdfLogUtility.logException(e);
      }
    }
    return "Row addition to Serial or Lot Con List failed.";
  }

  public String addToSerialLotListInquiry(String num, long niinId, long locationId) {
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    String location_tracking_message = "";
    try {

      int id = 0;
      int maxid = 0;
      if (Util.isEmpty(num)) return "Needs serial number.";

      if (!RegUtils.isAlphaNumericDashes(num)) {
        return "Serial Numbers can contain alphanumeric, underscore, and/or hyphen only";
      }

      Row row = null;
      boolean flag = false;
      SerialOrLotNumListImpl vo = this.getSerialOrLotNumList1();

      int cou = vo.getRowCount();
      row = vo.first();

      String rowSerial = "";
      for (int i = 0; i < cou; i++) {
        if (row != null && row.getAttribute(SERIAL_OR_LO_NUM) != null) {
          rowSerial = row.getAttribute(SERIAL_OR_LO_NUM).toString();
          if (rowSerial.equalsIgnoreCase(num)) flag = true;
        }
        //We're trying to get a unique identifier.
        if (row != null && row.getAttribute(SRL_LOT_NUM_TRACK_ID) != null) {
          if (Integer.parseInt(row.getAttribute(SRL_LOT_NUM_TRACK_ID).toString()) > maxid) {
            maxid = Integer.parseInt(row.getAttribute(SRL_LOT_NUM_TRACK_ID).toString());
          }
        }

        row = vo.next();
      }

      id = maxid + 1;
      if (flag) return num + " is already entered."; //Already scanned in

      String sql = "select location_label from serial_lot_num_track slnt inner join location l on slnt.location_id = l.location_id where serial_number=? and niin_id=? and l.location_id != ?";
      pstmt = getDBTransaction().createPreparedStatement(sql, 0);
      pstmt.setString(1, num);
      pstmt.setLong(2, niinId);
      pstmt.setLong(3, locationId);
      rs = pstmt.executeQuery();
      String location_label = "";
      if (rs.next()) {
        flag = true;
        location_label = rs.getString(1);
      }
      rs.close();
      rs = null;
      pstmt.close();
      pstmt = null;

      if (flag) //row found
      {
        location_tracking_message = "Serial Number " + num + " already exists on  " + location_label + ".";
      }
      else {
        row = vo.createRow();
        row.setAttribute(SERIAL_OR_LO_NUM, num);
        row.setAttribute(SRL_LOT_NUM_TRACK_ID, id);
        row.setAttribute(QTY_LOT, 1);
        vo.insertRow(row);
      }

      return location_tracking_message;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    finally {
      try {

        if (rs != null) {
          rs.close();
        }
        if (pstmt != null) {
          pstmt.close();
        }
      }
      catch (Exception e) {
        AdfLogUtility.logException(e);
      }
    }
    return "Row addition to Serial List failed.";
  }

  public void createSerialInquiry(int niin_id, int niin_loc_id, String cc, String expDate) {

    ArrayList<String> listofSerialNumbers = new ArrayList<String>();
    ArrayList<String> listtoAdd = new ArrayList<String>();

    try {
      String sql = "select slnt.serial_number from serial_lot_num_track slnt inner join niin_location nl on slnt.location_id = nl.location_id where nl.niin_loc_id =? and issued_flag = 'N'";
      String serial;
      try (PreparedStatement selectSerial = getDBTransaction().createPreparedStatement(sql, 0)) {
        selectSerial.setInt(1, niin_loc_id);
        try (ResultSet selectSerialResult = selectSerial.executeQuery()) {
          while (selectSerialResult.next()) {
            serial = selectSerialResult.getString(1);
            listofSerialNumbers.add(serial);
          }
        }
      }

      sql = "select location_id from niin_location where niin_loc_id = ?";
      int location_id = 0;
      try (PreparedStatement pstmtselectLocId = getDBTransaction().createPreparedStatement(sql, 0)) {
        pstmtselectLocId.setInt(1, niin_loc_id);
        try (ResultSet locIdResult = pstmtselectLocId.executeQuery()) {
          if (locIdResult.next()) {
            location_id = locIdResult.getInt(1);
          }
        }
      }

      SerialOrLotNumListImpl vo = this.getSerialOrLotNumList1();
      Row row = null;
      int cou = vo.getRowCount();
      row = vo.first();

      String rowSerial = "";
      String newSerial = "";
      int i = 0, j = 0;
      boolean flag = false;
      //Compare all the new serial numbers with the old. If we find a match, we can safely remove it from the old list and move on. If no match is found, it's put into a list to be added.
      while ((i < cou)) {
        rowSerial = row.getAttribute(SERIAL_OR_LO_NUM).toString();
        flag = false;
        j = 0;
        while (j < listofSerialNumbers.size()) {
          if (rowSerial.equalsIgnoreCase(listofSerialNumbers.get(j))) {
            flag = true;
            listofSerialNumbers.remove(j);
          }
          j++;
        }

        //If no match, we need to add
        if (!flag) {
          newSerial = new String(rowSerial);
          listtoAdd.add(newSerial);
        }
        row = vo.next();
        i++;
      }

      //These are the serial numbers with no matches and need to be added.
      for (i = 0; i < listtoAdd.size(); i++) {

        sql = "insert into serial_lot_num_track (serial_number, niin_id, qty, cc, location_id, expiration_date, issued_flag) VALUES (?, ?, ?, ?, ?, to_date(nvl(?,'9999-01-01'),'yyyy-MM-dd'), 'N')";

        try (PreparedStatement insertSerial = getDBTransaction().createPreparedStatement(sql, 0)) {
          insertSerial.setString(1, listtoAdd.get(i));
          insertSerial.setInt(2, niin_id);
          insertSerial.setInt(3, 1);
          insertSerial.setString(4, cc);
          insertSerial.setInt(5, location_id);
          if (!Util.isEmpty(expDate)) insertSerial.setString(6, expDate);
          else insertSerial.setString(6, "9999-01-01");
          insertSerial.executeUpdate();
        }
      }
      this.getDBTransaction().commit();

      //Any serial numbers left in the list of Serial Numbers has no match in the new list and must be removed.
      for (i = 0; i < listofSerialNumbers.size(); i++) {
        sql = "delete from serial_lot_num_track where niin_id = ? and location_id = ? and serial_number = ?";
        try (PreparedStatement deleteSerial = getDBTransaction().createPreparedStatement(sql, 0)) {
          deleteSerial.setInt(1, niin_id);
          deleteSerial.setInt(2, location_id);
          deleteSerial.setString(3, listofSerialNumbers.get(i));
          deleteSerial.executeUpdate();
        }
      }
      this.getDBTransaction().commit();
      vo.clearCache();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  public int hardcardWalkThru(String issueSCN, List<Map<String, Object>> pickList, int userId, boolean isSerial, boolean isLot, String niinId) {
    WorkLoadManagerImpl workload = (WorkLoadManagerImpl) getWorkLoadManager1();

    List<String> trackIds = null;
    if (isSerial || isLot) {
      trackIds = new ArrayList<>();
      SerialOrLotNumListImpl view = this.getSerialOrLotNumList1();
      int i = 0;
      while (i < view.getEstimatedRowCount()) {
        Row row = (i == 0) ? view.first() : view.next();
        String trackId = row.getAttribute(SRL_LOT_NUM_TRACK_ID).toString();

        //* group any lot numbers and qty which are duplicate
        if (isLot) trackId += "," + row.getAttribute(QTY_LOT);

        trackId += "," + row.getAttribute("LocationId");

        trackIds.add(trackId);
        i++;
      }
    }
    return workload.hardcardWalkThru(issueSCN, pickList, userId, isSerial, isLot, niinId, trackIds);
  }

  //* used for NIIN Inquiry

  public String buildSerialLotList(int niinId, boolean isSerial, int locationId) {
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    try {

      Row row = null;
      SerialOrLotNumListImpl vo = this.getSerialOrLotNumList1();
      String sql = "select serial_lot_num_track_id, serial_number, lot_con_num, qty, cc from serial_lot_num_track " + "where niin_id=? and location_id = ? and issued_flag = 'N' order by lot_con_num, serial_number";
      pstmt = getDBTransaction().createPreparedStatement(sql, 0);
      pstmt.setInt(1, niinId);
      pstmt.setInt(2, locationId);

      try (ResultSet serialLotTrackResults = pstmt.executeQuery()) {
        while (serialLotTrackResults.next()) {
          String id = serialLotTrackResults.getString(1);

          row = vo.createRow();
          String serialNum = Util.cleanString(serialLotTrackResults.getObject(2));
          String lotConNum = Util.cleanString(serialLotTrackResults.getObject(3));
          row.setAttribute(SERIAL_OR_LO_NUM, serialNum);
          row.setAttribute(LOT_CON_NUM, lotConNum);
          row.setAttribute(SRL_LOT_NUM_TRACK_ID, id);
          row.setAttribute(QTY_LOT, serialLotTrackResults.getInt(4));
          vo.insertRow(row);
          log.trace("inserted row");
        }
      }
      pstmt.close();
      pstmt = null;

      return "";
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    finally {
      try {
        if (pstmt != null) {
          pstmt.close();
        }
      }
      catch (Exception e) {
        AdfLogUtility.logException(e);
      }
    }
    return "Row addition to Serial or Lot Con List failed.";
  }

  //* end NIIN Inquiry

  /**
   * Checks to see if the niin needs serial num or lot num or if it is missing any.
   */
  public String checkNiinNeedsAnySrlOrLotInfo(String niinIdStr, int qty) {
    PreparedStatement stR = null;
    ResultSet rs = null;
    try {
      boolean flag = false;
      int err = 0;
      String id = "";
      if (qty == 0) return ""; // No pick qty so just ignore
      stR = getDBTransaction().createPreparedStatement("select niin_id, nvl(serial_control_flag,'N'), nvl(lot_control_flag,'N') from niin_info  where niin_id = ? and rownum = 1 ", 0);
      stR.setInt(1, Integer.parseInt(niinIdStr));
      rs = stR.executeQuery();
      if (rs.next()) {
        id = rs.getString(1);
        if (rs.getString(2).equalsIgnoreCase("Y")) {
          err = 1;
        }
        if ((err <= 0) && (rs.getString(3).equalsIgnoreCase("Y"))) err = 2;
        else if ((err == 1) && (rs.getString(3).equalsIgnoreCase("Y"))) err = 3;
      }
      if (rs != null) rs.close();
      rs = null;
      if (stR != null) stR.close();
      stR = null;
      if (id.length() <= 0) return "false";
      SerialOrLotNumListImpl vo = null;
      Row row = null;
      flag = false;
      vo = this.getSerialOrLotNumList1();
      int cou = 0;
      if (vo != null) cou = vo.getRowCount();
      int i = 0;
      int currQty = 0;
      boolean f1 = false;
      String ln = "";
      while ((i < cou) && (!f1)) {
        if (i == 0) row = vo.first();
        else row = vo.next();
        currQty = currQty + Integer.parseInt(row.getAttribute(QTY_LOT) == null ? "0" : row.getAttribute(QTY_LOT).toString());
        try (PreparedStatement serialNumSelect = getDBTransaction().createPreparedStatement("select nvl(serial_number,'NONE'),nvl(lot_con_num,'NONE'),qty from serial_lot_num_track  where serial_lot_num_track_id = ? ", 0)) {
          serialNumSelect.setString(1, row.getAttribute(SRL_LOT_NUM_TRACK_ID).toString());
          try (ResultSet serialNumResult = serialNumSelect.executeQuery()) {
            if (serialNumResult.next()) {
              if (serialNumResult.getInt(3) < Integer.parseInt(row.getAttribute(QTY_LOT) == null ? "0" : row.getAttribute(QTY_LOT).toString())) {
                f1 = true;
                ln = "Srl: " + serialNumResult.getString(1) + "Lot#: " + serialNumResult.getString(2) + "Qty: " + serialNumResult.getInt(3);
              }
            }
          }
        }
        i++;
      }
      if (f1) return ln + "- Qty assigned is more.";
      if (cou == 0) {
        if (err == 1) return "Serial number entry required.";
        if (err == 2) return id + " STRATIS indicates item(s) needs lot con num (s).";
        if (err == 3) return id + " STRATIS indicates item(s) needs Serial and lot con num (s).";
      }
      else if (currQty < qty) {
        if (err == 1) return id + " Please enter serial number(s) of all the items.";
        if (err == 2) return id + " Please enter lot control number of all the items.";
        if (err == 3) return id + " Please enter Serial and lot control number of all the items.";
      }
      else if (currQty > qty) {
        if (err == 1) return id + " Entered serial number(s) of items  are more then the quantity.";
        if (err == 2)
          return id + " Entered lot control number of items picked are more then the quantity to be picked. (can only have one lot number)";
        if (err == 3) return id + " Entered serial number(s)and lot number(s) of items  are more then the quantity.";
      }
      return "";
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    finally {
      try {
        if (stR != null) {
          stR.close();
        }
        if (rs != null) {
          rs.close();
        }
      }
      catch (Exception e) {
        AdfLogUtility.logException(e);
      }
    }
    return "Check for Serial or Lot Con requirement failed.";
  }

  /**
   * Creates ref rows for each pick.  (PICK_SERIAL_LOT_NUM)
   */
  public String createSrlOrLotAndPickXref(String pIdStr) {
    PreparedStatement stR = null;
    ResultSet rs = null;
    //place holder
    int rs2 = 0;
    try {
      boolean flag = false;
      int err = 0;
      String id = "";
      String num = "";
      String qty = "";
      int locId = 1;
      String issueType = "";
      String scn = "";
      stR = getDBTransaction().createPreparedStatement("select nl.location_id, i.issue_type, i.scn from picking p, niin_location nl, issue i  where p.pid = ? and p.niin_loc_id = nl.niin_loc_id and i.scn = p.scn ", 0);
      stR.setString(1, pIdStr);
      rs = stR.executeQuery();
      if (rs.next()) {
        locId = rs.getInt(1);
        issueType = rs.getString(2);
        scn = rs.getString(3);
      }
      stR.close();
      rs.close();
      //Resetting the quantities that are not there in the latest pick serial/lot

      SerialOrLotNumListImpl vo = null;
      flag = false;
      vo = getSerialOrLotNumList1();
      int cou = 0;
      if (vo != null) cou = vo.getRowCount();
      //Existing list of serial/lot numbers
      try (PreparedStatement existingSerial = getDBTransaction().createPreparedStatement("select pick_serial_lot_num, serial_lot_num_track_id, qty from pick_serial_lot_num  where pid = ? ", 0)) {
        existingSerial.setInt(1, Integer.parseInt(pIdStr));
        try (ResultSet existingSerialResult = existingSerial.executeQuery()) {
          while (existingSerialResult.next()) {
            if (num.length() <= 0) {
              num = existingSerialResult.getString(1);
              id = existingSerialResult.getString(2);
              qty = existingSerialResult.getString(3);
            }
            else {
              num = num + "," + existingSerialResult.getString(1);
              id = id + "," + existingSerialResult.getString(2);
              qty = qty + "," + existingSerialResult.getString(3);
            }
          }
        }
      }
      if (cou <= 0 && num.length() <= 0) return "";
      Row r = null;
      vo.first();
      //Latest list of serial/lot numbers
      StringBuilder addL = new StringBuilder();
      int i = 0;
      StringBuilder addQ = new StringBuilder();
      if (cou > 0) {
        while (i < cou) {
          if (r == null) {
            r = vo.first();
            if (!id.contains(r.getAttribute(SRL_LOT_NUM_TRACK_ID).toString())) {
              addL.append(r.getAttribute(SRL_LOT_NUM_TRACK_ID));
              addQ.append(r.getAttribute(QTY_LOT));
            }
          }
          else {
            r = vo.next();
            if (!id.contains(r.getAttribute(SRL_LOT_NUM_TRACK_ID).toString())) {
              addL.append(',').append(r.getAttribute(SRL_LOT_NUM_TRACK_ID));
              addQ.append(',').append(r.getAttribute(QTY_LOT));
            }
          }
          i++;
        }
      }
      val addLString = addL.toString();
      cou = id.split(",").length;
      i = 0;
      String delL = "";
      //Extracting the serial/lot numbers that are not there in latest list
      while (i < cou) {
        if (id.split(",")[i] != null) if (!addLString.contains(id.split(",")[i])) {
          if (delL.length() <= 0) {
            delL = "'" + id.split(",")[i] + "'";
          }
          else {
            delL = delL + ",'" + id.split(",")[i] + "'";
          }
        }
        i++;
      }
      int rs1 = 0;
      if (delL.length() > 0) {
        try (PreparedStatement selectSerialLot = getDBTransaction().createPreparedStatement("select  psr.serial_lot_num_track_id, psr.qty, sr.serial_number, sr.lot_con_num from pick_serial_lot_num psr, serial_lot_num_track sr  where psr.pid = ? and psr.serial_lot_num_track_id in (" + delL + ") and psr.serial_lot_num_track_id = sr.serial_lot_num_track_id ", 0)) {
          selectSerialLot.setString(1, pIdStr);
          try (ResultSet serialLotResults = selectSerialLot.executeQuery()) {
            //Resetting the quantities that are not there in the latest pick serial/lot
            while (serialLotResults.next()) {
              String sql = "begin update serial_lot_num_track  set issued_flag = 'N', qty = 1, timestamp = sysdate where serial_lot_num_track_id = ?; end;";
              if (serialLotResults.getString(3) == null || serialLotResults.getString(3).length() <= 0) {
                sql = "begin update serial_lot_num_track  set issued_flag = 'N', qty = qty + ?, timestamp = sysdate where serial_lot_num_track_id = ?; end;";
              }

              try (PreparedStatement updateSerial = getDBTransaction().createPreparedStatement(sql, 0)) {
                if (serialLotResults.getString(3) == null || serialLotResults.getString(3).length() <= 0) {
                  updateSerial.setInt(1, serialLotResults.getInt(2));
                  updateSerial.setString(2, serialLotResults.getString(1));
                }
                else {
                  updateSerial.setString(1, serialLotResults.getString(1));
                }
                updateSerial.executeUpdate();
                this.getDBTransaction().commit();
              }
            }
          }
        }
        //Removing the rows that are no loger on the latest list
        try (PreparedStatement removeRows = getDBTransaction().createPreparedStatement("begin delete from pick_serial_lot_num  where pid = ? and serial_lot_num_track_id in (" + delL + "); end;", 0)) {
          removeRows.setString(1, pIdStr);
          removeRows.executeUpdate();
          this.getDBTransaction().commit();
        }
      }
      cou = addLString.split(",").length;
      i = 0;
      String addL1 = "";
      if (cou > 0) {
        val addQString = addQ.toString();
        //Inserting the new list of serial/lot to pick
        while (i < cou) {
          try (PreparedStatement serialInsert = getDBTransaction().createPreparedStatement("begin INSERT INTO pick_serial_lot_num (pid, serial_lot_num_track_id, timestamp, qty, location_id, scn) values (?,?, sysdate, ?,?,?); end;", 0)) {
            serialInsert.setString(1, pIdStr);
            serialInsert.setString(2, addLString.split(",")[i]);
            serialInsert.setString(3, addQString.split(",")[i]);
            serialInsert.setInt(4, locId);
            serialInsert.setString(5, scn);
            serialInsert.executeUpdate();
          }
          //Subtracting the qty of the latest pick serial/lot
          try (PreparedStatement subtractQty = getDBTransaction().createPreparedStatement("begin update serial_lot_num_track  set qty = qty - ?, timestamp = sysdate where serial_lot_num_track_id = ?; end;", 0)) {
            subtractQty.setInt(1, Integer.parseInt((addQString.split(",")[i] == null || addQString.split(",")[i].length() <= 0) ? "0" : addQString.split(",")[i]));
            subtractQty.setString(2, addLString.split(",")[i]);
            //ReUpdate the location
            subtractQty.executeUpdate();
          }
          if (addL1.length() <= 0) addL1 = "'" + addLString.split(",")[i] + "'";
          else addL1 = addL1 + ",'" + addLString.split(",")[i] + "'";
          i++;
        }
        getDBTransaction().commit();
        //Setting the issued flag to Y if qty has reached zero
        try (PreparedStatement stR2 = getDBTransaction().createPreparedStatement("begin update serial_lot_num_track  set issued_flag = 'Y', timestamp = sysdate where qty = 0 and serial_lot_num_track_id in (" + addL1 + "); end;", 0)) {
          stR2.executeUpdate();
          getDBTransaction().commit();
        }
      }
      vo.clearCache();
      vo = null;
      //Process for ReWarehouse to create stow rows in serial/lot cases
      int totQty = 0;
      String oldSid = "";
      String rcn = "";
      try (PreparedStatement selectSerial = getDBTransaction().createPreparedStatement("select sr.serial_number, sr.lot_con_num, sr.cc, to_char(sr.expiration_date,'YYYY-MM-DD'), psr.qty, s.location_id, " + " s.sid, s.rcn, s.qty_to_be_stowed, s.pid, s.status, to_char(s.date_of_manufacture,'YYYY-MM-DD'), s.case_weight_qty, to_char(s.packed_date,'YYYY-MM-DD'), s.stow_qty \n" + "              from pick_serial_lot_num psr, serial_lot_num_track sr, stow s  \n" + "              where psr.pid = ? and " + "                    psr.serial_lot_num_track_id = sr.serial_lot_num_track_id and " + "                    s.pid = psr.pid", 0)) {
        selectSerial.setInt(1, Integer.parseInt(pIdStr));
        rs = selectSerial.executeQuery();
        try (PreparedStatement insertStow = getDBTransaction().createPreparedStatement("begin INSERT INTO stow (sid, qty_to_be_stowed, rcn, created_by, pid, status, expiration_date, location_id," + " lot_con_num, case_weight_qty, packed_date, serial_number, date_of_manufacture, stow_qty, created_date) values " + " (?,?,?,?,?,?,to_date(?,'YYYY-MM-DD'),?,?,?,to_date(?,'YYYY-MM-DD'),?,to_date(?,'YYYY-MM-DD'), 0, sysdate); end;", 0)) {
          while (rs.next()) {
            insertStow.setString(1, this.getWorkloadManagerService().assignSID());
            insertStow.setString(2, rs.getString(5));
            insertStow.setString(3, rs.getString(8));
            insertStow.setString(4, "1");
            insertStow.setString(5, pIdStr);
            insertStow.setString(6, rs.getString(11));
            insertStow.setString(7, rs.getString(4));
            insertStow.setString(8, rs.getString(6));
            insertStow.setString(9, rs.getString(2));
            insertStow.setString(10, rs.getString(13));
            insertStow.setString(11, rs.getString(14));
            insertStow.setString(12, rs.getString(1));
            insertStow.setString(13, rs.getString(12));
            rs1 = insertStow.executeUpdate();
            totQty = totQty + rs.getInt(5);
            if (oldSid == null || oldSid.length() <= 0) oldSid = rs.getString(7);
            if (rcn == null || rcn.length() <= 0) rcn = rs.getString(8);
          }
          getDBTransaction().commit();
        }
        if (rs != null) {
          rs.close();
          rs = null;
        }
      }
      //Update Receipt
      if (rcn != null && rcn.length() > 0) {
        try (PreparedStatement updateReceipt = getDBTransaction().createPreparedStatement("begin update receipt set quantity_stowed = ?, modified_by = ? where rcn = ?; end;", 0)) {
          updateReceipt.setInt(1, totQty);
          updateReceipt.setInt(2, 1);
          updateReceipt.setString(3, rcn);
          updateReceipt.executeUpdate();
        }
        try (PreparedStatement updateStow = getDBTransaction().createPreparedStatement("begin update stow set status = 'STOW CANCEL', modified_by = 1 where sid = ?; delete from stow where sid = ?; end;", 0)) {
          updateStow.setString(1, oldSid);
          updateStow.setString(2, oldSid);
          updateStow.executeUpdate();
          getDBTransaction().commit();
        }
      }
      return "";
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    finally {
      try {
        if (stR != null) {
          stR.close();
        }
        if (rs != null) {
          rs.close();
        }
      }
      catch (Exception e) {
        AdfLogUtility.logException(e);
      }
    }
    return "Creation of Serial or Lot Con requirement failed.";
  }

  /**
   * Container's getter for SerialOrLotNumList1
   */
  public SerialOrLotNumListImpl getSerialOrLotNumList1() {
    return (SerialOrLotNumListImpl) findViewObject("SerialOrLotNumList1");
  }
}
