package mil.stratis.model.services;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mil.stratis.common.util.Util;
import mil.stratis.model.services.common.WarehouseSetup;
import mil.stratis.model.util.DBUtil;
import mil.stratis.model.view.loc.WarehouseViewAllImpl;
import mil.stratis.model.view.whsetup.*;
import mil.usmc.mls2.stratis.core.utility.AdfLogUtility;
import oracle.jbo.ViewCriteria;
import oracle.jbo.ViewCriteriaRow;
import oracle.jbo.server.ApplicationModuleImpl;
import oracle.jbo.server.DBTransaction;
import oracle.jbo.server.ViewObjectImpl;
import oracle.jdbc.OracleCallableStatement;
import oracle.jdbc.OracleTypes;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
@NoArgsConstructor //ADF BackingHandlers all need a no args constructor
@SuppressWarnings("Duplicates")
public class WarehouseSetupImpl extends ApplicationModuleImpl implements WarehouseSetup {

  boolean rowflag;

  public boolean isAlphaNumeric(String str) {
    return Pattern.matches("^[\\w]*$", str);
  }

  /**
   * This function returns all the wacs for a given warehouse by the equip id
   */
  public void buildWACList(int equipid, List<String> answerlist) {
    String wacId;
    String wacNumber;

    try (PreparedStatement equipps = getDBTransaction().createPreparedStatement("select wac_number, wac_id from wac where warehouse_id=(select warehouse_id from equip where equipment_number = ?)", 0)) {
      equipps.setInt(1, equipid);
      try (ResultSet equiprs = equipps.executeQuery()) {

        while (equiprs.next()) {
          // find all the info about this wac
          wacNumber = equiprs.getString(1);
          wacId = equiprs.getString(2);

          int totalJobs = 0;
          if (wacId != null && !wacId.equals("")) {
            String sql = "select sum(total) from (";
            sql += "SELECT count(*) as total FROM PICKING p, NIIN_LOCATION n, LOCATION l WHERE ((p.NIIN_LOC_ID = n.NIIN_LOC_ID) AND (n.LOCATION_ID = l.LOCATION_ID)) AND (l.WAC_ID = ?) AND ((p.STATUS = 'PICK READY') OR (p.STATUS = 'PICK BYPASS1'))";
            sql += " UNION ";
            sql += "SELECT count(*) as total FROM STOW s, LOCATION l WHERE (s.LOCATION_ID = l.LOCATION_ID) AND (l.WAC_ID = ?) AND ((s.STATUS = 'STOW READY') OR (s.STATUS = 'STOW BYPASS1'))";
            sql += " UNION ";
            sql += "SELECT count(*) as total FROM INVENTORY_ITEM i WHERE ((i.WAC_ID = ?) AND (i.STATUS = 'INVENTORYPENDING'))";
            sql += " UNION ";
            sql += "SELECT count(*) as total FROM INVENTORY_ITEM i WHERE ((i.WAC_ID = ?) AND (i.STATUS = 'LOCSURVEYPENDING') )";
            sql += ")";

            try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(sql, 0)) {
              pstmt.setString(1, wacId);
              pstmt.setString(2, wacId);
              pstmt.setString(3, wacId);
              pstmt.setString(4, wacId);
              try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                  totalJobs = rs.getInt(1);
                }
              }
            }
            if (totalJobs > 0) {
              answerlist.add(wacNumber);
              answerlist.add(String.valueOf(totalJobs));
            }
          }
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  /**
   * This method returns list of selected wac table
   */
  public void buildSelectedWACFull(int wacId, String wacLabel,
                                   List<String> answerlist) {
    if (wacId > 0) {
      answerlist.add(wacLabel);

      // get the rest of the counts

      // stow - not stowed (1)
      String stowSQL = "SELECT count(*) FROM STOW Stow, LOCATION Location WHERE (Stow.LOCATION_ID = Location.LOCATION_ID) AND (Location.WAC_ID = ?) AND ((Stow.STATUS = 'STOW READY') OR (Stow.STATUS = 'STOW BYPASS1'))";
      int stowCount = DBUtil.getCountValueBy(stowSQL, String.valueOf(wacId), getDBTransaction());
      answerlist.add(String.valueOf(stowCount));

      // pick - available to pick (2)
      String pickSQL = "SELECT count(*) FROM PICKING Picking, NIIN_LOCATION NiinLocation, LOCATION Location WHERE ((Picking.NIIN_LOC_ID = NiinLocation.NIIN_LOC_ID) AND (NiinLocation.LOCATION_ID = Location.LOCATION_ID)) AND (Location.WAC_ID = ?) AND ((Picking.STATUS = 'PICK READY') OR (Picking.STATUS = 'PICK BYPASS1'))";
      int pickCount = DBUtil.getCountValueBy(pickSQL, String.valueOf(wacId), getDBTransaction());
      answerlist.add(String.valueOf(pickCount));

      // need slots

      // PACKING - 3
      String packSQL = "SELECT count(*) FROM PICKING Picking, NIIN_LOCATION NiinLocation, LOCATION Location, ISSUE i WHERE ((Picking.NIIN_LOC_ID = NiinLocation.NIIN_LOC_ID) AND (NiinLocation.LOCATION_ID = Location.LOCATION_ID)) AND (Location.WAC_ID = ?) AND (Picking.STATUS='PICKED' OR Picking.STATUS='PACKING') and (i.scn = Picking.SCN) and (nvl(i.issue_type,' ') not in ('R'))";
      int packCount = DBUtil.getCountValueBy(packSQL, String.valueOf(wacId), getDBTransaction());
      answerlist.add(String.valueOf(packCount));

      // SHIPPING - 4
      String shipSQL = "SELECT count(*) FROM PICKING Picking, NIIN_LOCATION NiinLocation, LOCATION Location, ISSUE i WHERE ((Picking.NIIN_LOC_ID = NiinLocation.NIIN_LOC_ID) AND (NiinLocation.LOCATION_ID = Location.LOCATION_ID)) AND (Location.WAC_ID = ?) AND (Picking.STATUS='PACKED' or Picking.STATUS='SHIPPING') and (i.scn = Picking.SCN) and (nvl(i.issue_type,' ') not in ('R'))";
      int shipCount = DBUtil.getCountValueBy(shipSQL, String.valueOf(wacId), getDBTransaction());
      answerlist.add(String.valueOf(shipCount));

      // inventory (5)
      String invSQL = "SELECT count(*) FROM INVENTORY_ITEM InventoryItem WHERE (InventoryItem.WAC_ID = ?) AND (InventoryItem.STATUS = 'INVENTORYPENDING')";
      int invCount = DBUtil.getCountValueBy(invSQL, String.valueOf(wacId), getDBTransaction());
      answerlist.add(String.valueOf(invCount));

      // shelf life (6)
      String slcSQL = "SELECT count(*) FROM INVENTORY_ITEM InventoryItem, Niin_Location nl WHERE InventoryItem.niin_loc_id=nl.niin_loc_id and ((InventoryItem.WAC_ID = ?) AND ( nl.NUM_EXTENTS is null and nl.EXP_REMARK = 'Y') )";
      int slcCount = DBUtil.getCountValueBy(slcSQL, String.valueOf(wacId), getDBTransaction());
      answerlist.add(String.valueOf(slcCount));

      // location survey (7)
      String locSurveySQL = "SELECT count(*) FROM INVENTORY_ITEM InventoryItem WHERE ((InventoryItem.WAC_ID = ?) AND (InventoryItem.STATUS = 'LOCSURVEYPENDING') )";
      int locSurveyCount = DBUtil.getCountValueBy(locSurveySQL, String.valueOf(wacId), getDBTransaction());
      answerlist.add(String.valueOf(locSurveyCount));

      // walkthru queue (8)
      String wtqSQL = "SELECT count(*) FROM  walkthru_queue w, issue i, PICKING p, NIIN_LOCATION n, LOCATION l WHERE (((p.NIIN_LOC_ID = n.NIIN_LOC_ID) AND (w.document_number = i.document_number) AND (i.scn = p.scn) AND (n.LOCATION_ID = l.LOCATION_ID)) AND (l.WAC_ID = ?) AND ((p.STATUS = 'PICK READY') OR (p.STATUS = 'PICK BYPASS1')))";
      int wtqCount = DBUtil.getCountValueBy(wtqSQL, String.valueOf(wacId), getDBTransaction());
      answerlist.add(String.valueOf(wtqCount));
    }
  }

  /**
   * This function filters the change location tables
   */
  public void filterChangeLocationTables(Object fromLoc,
                                         Object searchNiin) {
    try {
      ChangeLocationFromViewImpl fromview = getChangeLocationFromView1();

      // clear the last criteria's
      fromview.applyViewCriteria(null);

      // set up the next filter
      ViewCriteria vc = fromview.createViewCriteria();
      ViewCriteriaRow vcr = vc.createViewCriteriaRow();
      vcr.setUpperColumns(true);

      // update the search vars
      if (searchNiin != null) {
        String niin = Util.trimUpperCaseClean(searchNiin);
        if (isAlphaNumeric(niin))
          vcr.setAttribute("Niin", "like '%" + niin + "%'");
      }

      if (fromLoc != null) {
        String location = Util.trimUpperCaseClean(fromLoc);
        if (isAlphaNumeric(location))
          vcr.setAttribute("LocationLabel", "like '%" + location + "%'");
      }
      vcr.setAttribute("Locked", "N");
      vc.addElement(vcr);
      fromview.applyViewCriteria(vc);

      // update the queries
      fromview.executeQuery();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  /**
   * Rollback Change Location - dangling row with qty 0
   */
  public void removeChangeLocation(int toNiinLocId) {
    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
        "delete from niin_location n " + "where niin_loc_id=? and qty=0", 0)) {

      pstmt.setInt(1, toNiinLocId);
      pstmt.executeUpdate();
      getDBTransaction().commit();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  /**
   * Rewritten 09/18/08 to include more error handling
   * Rewritten 05/4/2012 to use a DB stored procedure
   */
  public String changeLocation(String fromLocation, String toLocation, String niin, int qty, boolean dac, int iUserId) {

    String result = "";
    String message;
    String fromCC = "";
    String secureWAC = "";

    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
        "select cc, qty, ni.niin_id, nvl(w.secure_flag, 'N') from niin_location n inner join location l " +
            "on n.location_id = l.location_id inner join wac w on l.wac_id = w.wac_id inner join niin_info ni " +
            "on n.niin_id = ni.niin_id where l.location_label = ? and ni.niin = ?", 0)) {
      pstmt.setString(1, fromLocation);
      pstmt.setString(2, niin);

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          fromCC = rs.getString(1);
          secureWAC = rs.getString(4);
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }

    try {
      try (CallableStatement st = getDBTransaction().createCallableStatement(
          "begin ? := pkg_niin.f_change_location_qty(?, ?, ?, ?, ?, ?, null); end;", DBTransaction.DEFAULT)) {
        st.registerOutParameter(1, Types.VARCHAR);
        st.setString(2, niin);
        st.setObject(3, fromLocation);
        st.setObject(4, toLocation);
        st.setObject(5, qty);
        st.registerOutParameter(6, Types.VARCHAR);
        st.setObject(7, iUserId);
        st.execute();
        result = st.getString(1);
        message = st.getString(6);
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      message = "ERROR - An Error Has Occured. Please Try Again.";
    }

    if (Util.cleanInt(result) == 1) {
      message = "";
      if (dac) {
        //* if CCs do not match, send DAC
        int result2 = changeLocSendDAC(toLocation, niin, fromCC, qty);
        log.debug("sendDAC result2 =  {}", result2); // - 3 means DAC failed
        if (result2 < 0) {
          message = "ERROR - An error occurred while attempting to send DAC.";
        }
      }
    }
    else {
      message = "ERROR - " + message;
    }

    if (message.length() < 1 && secureWAC.equals("Y")) {
      message = "NOTE:  Moved from a secure WAC location.";
    }
    return message;
  }

  public int changeLocSendDAC(String toLocation, String niin, Object fromCCObj,
                              int qtyChanged) {

    int result = 0;
    boolean success = true;
    int niinId = 0;
    int toNiinLocId = 0;

    try {
      //* compare the from CC with the to CC, if they differ, send a DAC
      String fromCC = "";
      String cc = "";

      try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
          "select n.cc, n.niin_id, n.niin_loc_id from niin_location n inner join location l " +
              "on n.location_id = l.location_id inner join niin_info ni on n.niin_id = ni.niin_id " +
              "where l.location_label = ? and ni.niin = ?", 0)) {
        pstmt.setString(1, toLocation);
        pstmt.setString(2, niin);

        try (ResultSet rs = pstmt.executeQuery()) {
          if (rs.next()) {
            Object ccObj = rs.getObject(1);
            niinId = rs.getInt(2);
            toNiinLocId = rs.getInt(3);
            if (!Util.isEmpty(ccObj))
              cc = ccObj.toString();
            if (!Util.isEmpty(fromCCObj))
              fromCC = fromCCObj.toString();
          }
          else {
            result = -1;
            success = false;
          }
        }
      }

      if (success && !cc.equalsIgnoreCase(fromCC)) {
        //* Create the DAC
        // get the workload module
        WorkLoadManagerImpl workload = (WorkLoadManagerImpl) this.getWorkLoadManager1();
        int resultTrans = 0;
        resultTrans = workload.getGCSSMCTransactions1().sendDACGCSSMCTransaction(niinId, toNiinLocId, fromCC, null, null, null);
        if (resultTrans != 0) {
          //* only zero is success
          result = -3;
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return result;
  }

  /**
   * Used by Change Location to return the location id of a given location label
   */
  public int findLocation(String locLabel) {
    int locationId = 0;

    try (PreparedStatement selecter = getDBTransaction().createPreparedStatement(
        "select location_id from location " + "where location_label = ?", 0)) {

      selecter.setString(1, locLabel);
      try (ResultSet results = selecter.executeQuery()) {
        if (results.next())
          locationId = results.getInt(1);
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      locationId = 0;
    }
    return locationId;
  }

  public int findNiin(String niin) {
    int niinId = 0;

    try (PreparedStatement selecter = getDBTransaction().createPreparedStatement("select niin_id from niin_info " + "where niin = ?", 0)) {
      selecter.setString(1, niin);
      try (ResultSet results = selecter.executeQuery()) {
        if (results.next())
          niinId = results.getInt(1);
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      niinId = 0;
    }
    return niinId;
  }

  /**
   * Used by Change Location to determine the condition code of a given niin location id
   */
  public String getNiinLocationCC(int niinLocationId) {
    String cc = "";

    try (PreparedStatement selecter = getDBTransaction().createPreparedStatement(
        "select nvl(cc,'A') from niin_location where niin_loc_id=? ", 0)) {

      selecter.setInt(1, niinLocationId);
      try (ResultSet results = selecter.executeQuery()) {
        if (results.next())
          cc = results.getString(1);
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      cc = null;
    }
    return cc;
  }

  /**
   * Used by Change Location to determine whether a given niin id is serial
   * or lot controlled
   */
  public boolean isLocationNew(int locationId) {
    boolean found = false;
    try (PreparedStatement selecter = getDBTransaction().createPreparedStatement(
        "select count(*) from niin_location where location_id=?", 0)) {

      selecter.setInt(1, locationId);

      try (ResultSet results = selecter.executeQuery()) {
        if (results.next())
          found = results.getInt(1) > 0;
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      found = false;
    }
    return !found;
  }

  /**
   * Used by Change Location to determine whether a given niin id is serial
   * or lot controlled
   */
  public boolean isNiinSerialOrLot(int niinId, boolean serialOrLot) {
    boolean serial = false;

    String which = (serialOrLot) ? "serial_control_flag" : "lot_control_flag";
    String sql = "select nvl(" + which + ",'N') from niin_info " + "where niin_id=?";

    try (PreparedStatement selecter = getDBTransaction().createPreparedStatement(sql, 0)) {
      selecter.setInt(1, niinId);

      try (ResultSet results = selecter.executeQuery()) {
        if (results.next())
          serial = results.getString(1).equals("Y");
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return serial;
  }

  public boolean isNiinSerialOrLot(String niin, boolean serialOrLot) {
    boolean serial = false;
    String which = (serialOrLot) ? "serial_control_flag" : "lot_control_flag";
    try {
      String sql = "select nvl(" + which + ",'N') from niin_info " + "where niin=?";
      try (PreparedStatement selecter = getDBTransaction().createPreparedStatement(sql, 0)) {
        selecter.setString(1, niin);

        try (ResultSet results = selecter.executeQuery()) {
          if (results.next())
            serial = results.getString(1).equals("Y");
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return serial;
  }

  /**
   * Used by Change Location to determine whether a given location label
   * and niin id is locked
   */
  public boolean isNiinLocationLocked(String locLabel, int niinId) {
    boolean locked = false;
    try {
      try (PreparedStatement selecter = getDBTransaction().createPreparedStatement(
          "select nvl(locked,'N') from niin_location n, location l where n.LOCATION_ID = l.LOCATION_ID AND " +
              "l.LOCATION_LABEL = ? and n.niin_id=?", 0)) {
        selecter.setString(1, locLabel);
        selecter.setInt(2, niinId);

        try (ResultSet results = selecter.executeQuery()) {
          if (results.next())
            locked = results.getString(1).equals("Y");
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return locked;
  }

  /**
   * Used by Change Location to get the niin location id
   * for a given location label and niin id
   */
  public int getNiinLocId(String locLabel, int niinId) {
    int niinloc = 0;

    try {
      String sql = "SELECT a.NIIN_LOC_ID  FROM NIIN_LOCATION a, LOCATION b " + "WHERE a.LOCATION_ID = b.LOCATION_ID AND " + "b.LOCATION_LABEL = ?";

      if (niinId != 0)
        sql += " and a.niin_id=?";
      try (PreparedStatement selecter = getDBTransaction().createPreparedStatement(sql, 0)) {
        selecter.setString(1, locLabel);
        if (niinId != 0)
          selecter.setInt(2, niinId);

        try (ResultSet results = selecter.executeQuery()) {
          if (results.next())
            niinloc = results.getInt(1);
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      niinloc = 0;
    }
    return niinloc;
  }

  public int getSiteInfoId() {
    int siteInfoId = 0;

    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
        "select site_id from site_info where rownum < 2", 0)) {
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          siteInfoId = rs.getInt(1);
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return siteInfoId;
  }

  /**
   * This method is used to delete a row from the niin_info table given its id,
   * after a successful check is completed ensuring there are no existing row dependencies.
   */
  public String deleteNiin(int id) {
    String message = "";
    try {
      if (inUseNIIN(id)) {
        message = "NIIN currently in use.";
      }
      else {
        deleteRow("delete from niin_info where niin_id=?", id);
      }
    }
    catch (Exception e) {
      message = "NIIN Deletion failed.";
      log.debug("NIIN deletion failed", e);
    }
    return message;
  }

  /**
   * This method is used to determine if a row from the niin_info table given its id,
   * is in use by another table.  Returns false if there are no existing row dependencies.
   */
  public boolean inUseNIIN(int id) {
    int count = checkDependencies("select count(*) from inventory_item where niin_id=?", id);
    count += checkDependencies("select count(*) from inventory_item_hist where niin_id=?", id);
    count += checkDependencies("select count(*) from issue where niin_id=?", id);
    count += checkDependencies("select count(*) from issue_hist where niin_id=?", id);
    count += checkDependencies("select count(*) from niin_location where niin_id=?", id);
    count += checkDependencies("select count(*) from niin_location_hist where niin_id=?", id);
    count += checkDependencies("select count(*) from receipt where niin_id=?", id);
    count += checkDependencies("select count(*) from receipt_hist where niin_id=?", id);
    return (count > 0);
  }

  /**
   * This method is used to delete a row from the com_port table given its id,
   * after a successful check is completed ensuring there are no existing row dependencies.
   */
  public String deleteComPort(int id) {
    String message = "";
    try {
      if (inUseComPort(id)) {
        message = "Unable to delete Com Port due to Workstation dependencies.";
      }
      else {
        deleteRow("delete from com_port where com_port_id=?", id);
      }
    }
    catch (Exception e) {
      message = "Com Port Deletion failed.";
      log.debug("Com Port deletion failed", e);
    }
    return message;
  }

  /**
   * This method is used to determine if a row from the com_port table given its id,
   * is in use by another table.  Returns false if there are no existing row dependencies.
   */
  public boolean inUseComPort(int id) {
    int count = checkDependencies("select count(*) from equip where com_port_printer_id=?", id);
    count += checkDependencies("select count(*) from equip where com_port_equipment_id=?", id);
    return (count > 0);
  }

  /**
   * This method is used to delete a row from the packing_station table given its id,
   * after a successful check is completed ensuring there are no existing row dependencies.
   */
  public String deletePackingStation(int id) {
    String message = "";
    try {
      if (inUsePackingStation(id)) {
        message = "Consolidation Triwall currently in use and cannot be deleted.";
      }
      else {
        deleteRow("begin delete from packing_station where packing_station_id=?; end;", id);
      }
    }
    catch (Exception e) {
      message = "Consolidation Workstation Deletion failed.";
      log.debug("Consolidation Workstation deletion failed", e);
    }
    return message;
  }

  /**
   * This method is used to determine if a row from the packing_station table given its id,
   * is in use by another table.  Returns false if there are no existing row dependencies.
   */
  public boolean inUsePackingStation(int id) {
    int count = checkDependencies("select count(*) from packing_consolidation where packing_station_id=?", id);
    count += checkDependencies("select count(*) from packing_consolidation_hist where packing_station_id=?", id);
    return (count > 0);
  }

  /**
   * This method is used to delete a row from the floor_location table given its id,
   * after a successful check is completed ensuring there are no existing row dependencies.
   */
  public String deleteFloorLocation(int id) {
    String message = "";
    try {
      if (inUseFloorLocation(id)) {
        message = "Shipping Floor Location currently has assigned tasks and cannot be deleted.";
      }
      else {
        deleteRow("delete from floor_location where floor_location_id=?", id);
      }
    }
    catch (Exception e) {
      message = "Shipping Floor Location currently has assigned tasks and cannot be deleted.";
      log.debug("Shipping Floor Location currently has assigned tasks and cannot be deleted.", e);
    }
    return message;
  }

  public String deleteFloorLocationByName(String floor) {
    StringBuilder message = new StringBuilder();
    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
        "select floor_location_id from floor_location where floor_location=?", 0)) {
      pstmt.setString(1, floor);
      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          message.append(deleteFloorLocation(rs.getInt(1)));
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return message.toString();
  }

  /**
   * This method is used to determine if a row from the floor_location table given its id,
   * is in use by another table.  Returns false if there are no existing row dependencies.
   */
  public boolean inUseFloorLocation(int id) {
    int count = checkDependencies("select count(*) from shipping_manifest where floor_location_id=?", id);
    return (count > 0);
  }

  /**
   * This method is used to delete a row from the equip table given its id,
   * after a successful check is completed ensuring there are no existing row dependencies.
   */
  public String deleteWorkstation(int id, Object wacId) {
    String message;
    try {
      message = inUseWorkstation(id, wacId);
      if (message.equals("")) {
        deleteRow("begin delete from packing_station where equipment_number=?; end;", id);
        deleteRow("begin delete from equip where equipment_number=?; end;", id);
      }
      return message;
    }
    catch (Exception e) {
      message = "Workstation has assigned tasks and cannot be deleted.";
      log.debug("Workstation deletion failed", e);
    }
    return message;
  }

  /**
   * This method is used to determine if a row from the equip table given its id,
   * is in use by another table.  Returns false if there are no existing row dependencies.
   */
  public String inUseWorkstation(int id, Object wacId) {
    int count = checkDependencies("select count(*) from packing_consolidation where packing_station_id in (select packing_station_id from packing_station where equipment_number=?)", id);
    count += checkDependencies("select count(*) from packing_consolidation_hist where packing_station_id in (select packing_station_id from packing_station where equipment_number=?)", id);
    count += checkDependencies("select count(*) from packing_station where equipment_number=? and (number_of_slots_in_use > 0 or total_issues > 0)", id);
    if (count > 0) {
      return "Workstation has assigned tasks and cannot be deleted.";
    }

    if (wacId != null) {
      int wacnum = Integer.parseInt(wacId.toString());
      count = checkDependencies("select count(*) from stow s inner join location l on s.location_id = l.location_id where l.wac_id = ?", wacnum);
      if (count > 0) {
        return "Workstation has assigned tasks and cannot be deleted.";
      }

      count = checkDependencies("select count(*) from inventory_item ii inner join location l on ii.location_id = l.location_id where l.wac_id = ?", wacnum);
      if (count > 0) {
        return "Inventory in Progress on Locations in Workstation";
      }

      count = checkDependencies("select count(*) from issue i inner join picking p on i.scn = p.scn inner join niin_location nl on nl.niin_loc_id = p.niin_loc_id inner join location l on nl.location_id = l.location_id where l.wac_id = ?", wacnum);
      if (count > 0) {
        return "Issues Still Active with Locations in Workstation";
      }
    }

    return "";
  }

  /**
   * This method is used to delete a row from the warehouse table given its id,
   * after a successful check is completed ensuring there are no existing row dependencies.
   */
  public String deleteBuilding(int id) {
    String message = "";
    try {
      if (inUseBuilding(id)) {
        message = "Building currently in use and cannot be deleted.";
      }
      else {
        deleteRow("begin delete from warehouse where warehouse_id=?; end;", id);
      }
    }
    catch (Exception e) {
      message = "Warehouse Building Deletion failed.";
      log.debug("Warehouse Building deletion failed", e);
    }
    return message;
  }

  /**
   * This method is used to determine if a row from the warehouse table given its id,
   * is in use by another table.  Returns false if there are no existing row dependencies.
   */
  public boolean inUseBuilding(int id) {
    int count = checkDependencies("select count(*) from wac where warehouse_id=?", id);
    count += checkDependencies("select count(*) from equip where warehouse_id=?", id);
    return (count > 0);
  }

  /**
   * This method is used to delete a row from the wac table given its id,
   * after a successful check is completed ensuring there are no existing row dependencies.
   */
  public String deleteWAC(int id) {
    String message;
    try {
      message = inUseWAC(id);
      if (message.equals("")) {
        deleteRow("begin delete from wac where wac_id=?; end;", id);
      }
      else
        return message;
    }
    catch (Exception e) {
      message = "WAC Deletion failed.";
      log.debug("WAC deletion failed", e);
    }
    return message;
  }

  /**
   * This method is used to determine if a row from the wac table given its id,
   * is in use by another table.  Returns false if there are no existing row dependencies.
   */
  public String inUseWAC(int id) {
    int count;
    count = checkDependencies("select count(*) from niin_location nl inner join location l on nl.location_id = l.location_id where l.wac_id = ?", id);
    if (count > 0) {
      return "NIIN on Locations in WAC";
    }

    count = checkDependencies("select count(*) from stow s inner join location l on s.location_id = l.location_id where l.wac_id = ?", id);
    if (count > 0) {
      return "Stowing in Progress on Locations in WAC";
    }

    count = checkDependencies("select count(*) from inventory_item ii inner join location l on ii.location_id = l.location_id where l.wac_id = ?", id);
    if (count > 0) {
      return "Inventory in Progress on Locations in WAC";
    }

    count = checkDependencies("select count(*) from serial_lot_num_track slnt inner join location l on slnt.location_id = l.location_id where l.wac_id = ?", id);
    if (count > 0) {
      return "Serial Numbers on Locations in WAC";
    }

    count = checkDependencies("select count(*) from issue i inner join picking p on i.scn = p.scn inner join niin_location_hist nl on nl.niin_loc_id = p.niin_loc_id inner join location l on nl.location_id = l.location_id where l.wac_id = ?", id);
    if (count > 0) {
      return "Issues Still Active with Locations in WAC";
    }

    count = checkDependencies("select count(*) from error_queue eq inner join niin_location_hist nl on cast(nl.niin_loc_id as varchar2(20)) = eq.key_num inner join location l on nl.location_id = l.location_id where eq.key_type = 'NIIN_LOC_ID' and l.wac_id = ?", id);
    if (count > 0) {
      return "Errors Still Active with Locations in WAC";
    }

    count = checkDependencies("select count(*) from equip where wac_id=?", id);
    if (count > 0) {
      return "Workstations still in WAC";
    }

    return "";
  }

  /**
   * This method is used to delete a row from the divider_type table given its id,
   * after a successful check is completed ensuring there are no existing row dependencies.
   */
  public String deleteDivider(int id) {
    String message = "";
    try {
      if (inUseDivider(id)) {
        message = "Divider Type currently in use and cannot be deleted.";
      }
      else {
        deleteRow("begin delete from divider_slots where divider_type_id=?; end;", id);
        deleteRow("begin delete from divider_type where divider_type_id=?; end;", id);
      }
    }
    catch (Exception e) {
      message = "Divider Type Deletion failed.";
      log.debug("Divider Type deletion failed", e);
    }
    return message;
  }

  /**
   * This method is used to determine if a row from the divider_type table given its id,
   * is in use by another table.  Returns false if there are no existing row dependencies.
   */
  public boolean inUseDivider(int id) {
    int count = checkDependencies("select count(*) from location_classification where divider_type_id=?", id);
    return (count > 0);
  }

  /**
   * This method is used to delete a row from the location_classification table given its id,
   * after a successful check is completed ensuring there are no existing row dependencies.
   */
  public String deleteStorageBin(int id) {
    String message = "";
    try {
      if (inUseStorageBin(id)) {
        message = "Storage Bin currently in use and cannot be deleted.";
      }
      else {
        deleteRow("delete from location_classification where loc_classification_id=?", id);
      }
    }
    catch (Exception e) {
      message = "Storage Bin Type Deletion failed.";
      log.debug("Storage Bin Type deletion failed", e);
    }
    return message;
  }

  /**
   * This method is used to determine if a row from the location_classification table given its id,
   * is in use by another table.  Returns false if there are no existing row dependencies.
   */
  public boolean inUseStorageBin(int id) {
    int count = checkDependencies("select count(*) from location where loc_classification_id=?", id);
    count += checkDependencies("select count(*) from location_header_bin where loc_classification_id=?", id);
    return (count > 0);
  }

  public int checkDependencies(String sqlCheck, int id) {
    int count = 0;
    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(sqlCheck, 0)) {
      pstmt.setInt(1, id);
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          count = rs.getInt(1);
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      count = 1;
    }
    return count;
  }

  /**
   * This method is used to delete a row from the route table given its id,
   * after a successful check is completed ensuring there are no existing row dependencies.
   */
  public String deleteRoute(int id) {
    String message = "";
    try {
      if (inUseRoute(id)) {
        message = "Unable to delete Route due to database table (Site Info, Receipt, Issue) dependencies.";
      }
      else {
        deleteRow("delete from route where route_id=?", id);
      }
    }
    catch (Exception e) {
      message = "Route Deletion failed.";
      log.debug("Route deletion failed", e);
    }
    return message;
  }

  /**
   * This method is used to determine if a row from the route table given its id,
   * is in use by another table.  Returns false if there are no existing row dependencies.
   */
  public boolean inUseRoute(int id) {
    int count = checkDependencies("select count(*) from site_info where routing_id=(select route_name from route where route_id=?)", id);
    count += checkDependencies("select count(*) from issue where routing_id_from=(select route_name from route where route_id=?)", id);
    count += checkDependencies("select count(*) from receipt where ri=(select route_name from route where route_id=?)", id);
    count += checkDependencies("select count(*) from receipt where routing_id=(select route_name from route where route_id=?)", id);
    count += checkDependencies("select count(*) from customer where route_id=?", id);
    return (count > 0);
  }

  /**
   * This method is used to delete a row from the shipping_route table given its id,
   * after a successful check is completed ensuring there are no existing row dependencies.
   */
  public String deleteShippingRoute(int id) {
    String message = "";
    try {
      if (inUseShippingRoute(id)) {
        message = "Unable to delete Shipping Route due to database table Floor Location and/or Customer dependencies.";
      }
      else {
        deleteRow("delete from shipping_route where route_id=?", id);
      }
    }
    catch (Exception e) {
      message = "Shipping Route Deletion failed.";
      log.debug("Shipping Route deletion failed", e);
    }
    return message;
  }

  /**
   * This method is used to determine if a row from the shipping_route table given its id,
   * is in use by another table.  Returns false if there are no existing row dependencies.
   */
  public boolean inUseShippingRoute(int id) {
    int count = checkDependencies("select count(*) from floor_location where route_id=?", id);
    count += checkDependencies("select count(*) from customer where shipping_route_id=?", id);
    return (count > 0);
  }

  /**
   * This method is used to delete a row from the customer table given its id,
   * after a successful check is completed ensuring there are no existing row dependencies.
   */
  public String deleteCustomer(int id) {
    String message = "";
    try {
      if (inUseCustomer(id))
        message = "Unable to delete customer due to database dependencies";
      else
        deleteRow("delete from customer where customer_id=?", id);
    }
    catch (Exception e) {
      message = "Customer Deletion failed.";
      log.debug("Customer deletion failed", e);
    }
    return message;
  }

  /**
   * This method is used to determine if a row from the customer table given its id,
   * is in use by another table.  Returns false if there are no existing row dependencies.
   */
  public boolean inUseCustomer(int id) {
    int count = checkDependencies("select count(*) from issue where customer_id = ?", id);
    count += checkDependencies("select count(*) from issue_hist where customer_id = ?", id);
    count += checkDependencies("select count(*) from packing_consolidation where customer_id = ?", id);
    count += checkDependencies("select count(*) from packing_consolidation_hist where customer_id = ?", id);
    count += checkDependencies("select count(*) from shipping_manifest where customer_id = ?", id);
    count += checkDependencies("select count(*) from shipping_manifest_hist where customer_id = ?", id);
    count += checkDependencies("select count(*) from receipt where consignee = (select aac from customer where customer_id=?)", id);
    count += checkDependencies("select count(*) from receipt_hist where consignee = (select aac from customer where customer_id=?)", id);
    return (count > 0);
  }

  /**
   * This function is responsible for creating new groups
   */
  public void createGroup(int uid) {
    try (CallableStatement st = getDBTransaction().createCallableStatement(
        "begin ? := pkg_user_mgmt.f_create_group(?, ?, ?, ?); end;", DBTransaction.DEFAULT)) {
      st.registerOutParameter(1, Types.INTEGER);
      st.registerOutParameter(2, Types.VARCHAR);
      st.setString(3, "TEMP");
      st.setInt(4, uid);
      st.setString(5, "");
      st.executeUpdate();
    }
    catch (SQLException e) {
      AdfLogUtility.logException(e);
    }
  }

  /**
   * This method is used to delete a row from the group table given its id,
   * after a successful check is completed ensuring there are no existing row dependencies.
   */
  public String deleteGroup(long id, int uid) {
    String message = "";

    try (CallableStatement st = getDBTransaction().createCallableStatement(
        "begin ? := pkg_user_mgmt.f_delete_group(?, ?, ?); end;", DBTransaction.DEFAULT)) {
      st.registerOutParameter(1, Types.INTEGER);
      st.registerOutParameter(2, Types.VARCHAR);
      st.setLong(3, id);
      st.setInt(4, uid);
      st.executeUpdate();
      if (st.getInt(1) < 0) {
        message = st.getString(2);
      }
      getDBTransaction().commit();
    }
    catch (SQLException e) {
      message = "Group Deletion failed.";
      AdfLogUtility.logException(e);
    }
    return message;
  }

  /**
   * This method is used to save a group.
   */
  public String saveGroup(long groupID, String groupName, String groupDescription, int uid) {
    String message = "";

    try (CallableStatement st = getDBTransaction().createCallableStatement(
        "begin ? := pkg_user_mgmt.f_update_group_lu(?, ?, ?, ?, sysdate, ?); end;", DBTransaction.DEFAULT)) {
      st.registerOutParameter(1, Types.INTEGER);
      st.registerOutParameter(2, Types.VARCHAR);
      st.setLong(3, groupID);
      st.setString(4, groupName);
      st.setString(5, groupDescription);
      st.setInt(6, uid);
      st.executeUpdate();
      if (st.getInt(1) < 0) {
        message = st.getString(2);
      }
    }
    catch (SQLException e) {
      message = "Group Save failed.";
      AdfLogUtility.logException(e);
    }
    return message;
  }

  /**
   * This method is used to save a group priv.
   */
  public void saveGroupPriv(long groupID, int privID, int uid) {

    try (CallableStatement st = getDBTransaction().createCallableStatement(
        "begin ? := pkg_user_mgmt.f_assign_priv_2_group(?, ?, ?, ?); end;", DBTransaction.DEFAULT)) {
      st.registerOutParameter(1, Types.INTEGER);
      st.registerOutParameter(2, Types.VARCHAR);
      st.setLong(3, groupID);
      st.setInt(4, privID);
      st.setInt(5, uid);
      st.executeUpdate();
    }
    catch (SQLException e) {
      AdfLogUtility.logException(e);
    }
  }

  /**
   * This method is used to delete a group priv.
   */
  public void deleteGroupPriv(long groupID, int privID, int uid) {
    String sql = "begin ? := pkg_user_mgmt.f_remove_priv_from_group(?, ?, ?, ?); end;";
    try (CallableStatement st = getDBTransaction().createCallableStatement(sql, DBTransaction.DEFAULT)) {
      st.registerOutParameter(1, Types.INTEGER);
      st.registerOutParameter(2, Types.VARCHAR);
      st.setLong(3, groupID);
      st.setInt(4, privID);
      st.setInt(5, uid);
      st.executeUpdate();
    }
    catch (SQLException e) {
      AdfLogUtility.logException(e);
    }
  }

  /**
   * This method is used to commit the database.
   */

  public void dbCommit() {
    try (CallableStatement st = getDBTransaction().createCallableStatement("{call pkg_stratis_common.p_commit}", DBTransaction.DEFAULT)) {
      st.execute();
    }
    catch (SQLException e) {
      AdfLogUtility.logException(e);
    }
  }

  private void deleteRow(String sql, int id) throws SQLException {
    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(sql, 0)) {
      pstmt.setInt(1, id);
      pstmt.executeUpdate();
      getDBTransaction().commit();
    }
    catch (SQLException e) {
      getDBTransaction().rollback();
      throw e;
    }
  }

  /**
   * This method is used to determine if a row from the niin_info table given its id,
   * is in use by another table.  Returns false if there are no existing row dependencies.
   */
  public boolean inUseBulkLocation(int id) {
    int count = checkDependencies("select count(*) from niin_location where location_id=?", id);
    count += checkDependencies("select count(*) from serial_lot_num_track where location_id=?", id);
    count += checkDependencies("select count(*) from stow where location_id=?", id);
    return (count > 0);
  }

  public boolean submitRemoveBulkLocations(String locationId) {
    boolean success;

    try {

      if (Util.isEmpty(locationId))
        return false;

      if (inUseBulkLocation(Util.cleanInt(locationId))) {
        return false;
      }

      log.debug("delete  {}", locationId);

      //* remove the row by location id
      try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
          "delete from inv_serial_lot_num where location_id=?", 0)) {
        pstmt.setString(1, locationId);
        pstmt.executeUpdate();
        getDBTransaction().commit();
      }

      try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
          "delete from inventory_item where location_id=?", 0)) {
        pstmt.setString(1, locationId);
        pstmt.executeUpdate();
        getDBTransaction().commit();
      }

      try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
          "delete from location where location_id=? and availability_flag='A' and mechanized_flag='N'", 0)) {
        pstmt.setString(1, locationId);
        pstmt.executeUpdate();
        getDBTransaction().commit();
      }

      success = true;
    }
    catch (Exception e) {
      log.debug("remove bulk location error", e);

      success = false;
      AdfLogUtility.logException(e);
    }
    return success;
  }

  /**
   * This method is used by the NonMechanized (bulk) locations screen
   * to filter the table.
   */
  @Override
  public void filterNonMechLocationData(String wacID,
                                        String locationLabel) {
    try {
      LocationNonMechViewImpl view = getLocationNonMechView1();

      // clear the last ones
      view.applyViewCriteria(null);

      ViewCriteria vc = view.createViewCriteria();
      ViewCriteriaRow vcr = vc.createViewCriteriaRow();
      vcr.setUpperColumns(true);

      // If the Wac ID provided by the user is not null or empty
      if ((wacID != null) && (!wacID.equals(""))) {
        log.debug("filtering wac id  {}", wacID);
        vcr.setAttribute("WacId", "= " + Integer.parseInt(wacID));
      }

      // if the location isn't null add to filter
      if (locationLabel != null) {
        String location = Util.trimUpperCaseClean(locationLabel);
        if (isAlphaNumeric(location))
          vcr.setAttribute("LocationLabel", "like '%" + location + "%'");
      }

      vc.addElement(vcr);

      view.applyViewCriteria(vc);
      view.executeQuery();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  /**
   * This method is used by the NonMechanized (bulk) locations screen
   * to filter the table.
   */
  @Override
  public void filterNonMechRemovalData(String wacID, String locationLabel) {
    try {
      RemoveNonMechViewImpl view = getRemoveNonMechView1();

      // clear the last ones
      view.applyViewCriteria(null);

      ViewCriteria vc = view.createViewCriteria();
      ViewCriteriaRow vcr = vc.createViewCriteriaRow();
      vcr.setUpperColumns(true);

      // If the Wac ID provided by the user is not null or empty
      if ((wacID != null) && (!wacID.equals(""))) {
        log.debug("filtering wac id  {}", wacID);
        vcr.setAttribute("WacId", "= " + Integer.parseInt(wacID));
      }

      // if the location isn't null add to filter
      if (locationLabel != null) {
        String location = Util.trimUpperCaseClean(locationLabel);
        if (isAlphaNumeric(location))
          vcr.setAttribute("LocationLabel", "like '%" + location + "%'");
      }

      vc.addElement(vcr);

      view.applyViewCriteria(vc);
      view.executeQuery();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  public int checkAAC(String aac) {
    int customerid = -1;

    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement("select customer_id from customer where aac = ? and rownum =1", 0)) {
      pstmt.setString(1, aac);
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          customerid = rs.getInt(1);
        }
      }
    }
    catch (SQLException e) {
      customerid = -1;
    }
    return customerid;
  }

  public int checkBuilding(String building) {
    int buildingid = -1;

    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
        "select warehouse_id from warehouse where building = ? and rownum =1", 0)) {
      pstmt.setString(1, building);
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          buildingid = rs.getInt(1);
        }
      }
    }
    catch (SQLException e) {
      buildingid = -1;
    }
    return buildingid;
  }

  public void addDefaultFloorLocations(List<String> idList,
                                       int customerId) {
    try {
      try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
          "delete from cust_floor_location where customer_id=?", 0)) {
        pstmt.setInt(1, customerId);
        pstmt.executeUpdate();
        getDBTransaction().commit();
      }

      if (idList != null) {
        try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
            "insert into cust_floor_location (floor_location_id, customer_id) values (?, ?)", 0)) {
          for (String id : idList) {
            pstmt.setString(1, id);
            pstmt.setInt(2, customerId);
            pstmt.addBatch();
          }
          pstmt.executeBatch();
          getDBTransaction().commit();
        }
      }
    }
    catch (Exception e) {
      getDBTransaction().rollback();
      AdfLogUtility.logException(e);
    }
  }

  /**
   * function to build a list of the floor locations to use in a filter list.
   */
  public void buildFloorLocationFilterList(List<String> display,
                                           List<String> id,
                                           int customerId) {
    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
        "select floor_location, floor_location_id, building from floor_location f, warehouse w " +
            "where f.warehouse_id=w.warehouse_id and floor_location_id not in (select floor_location_id " +
            "from cust_floor_location where customer_id <> ?) " + "order by floor_location", 0)) {

      pstmt.setInt(1, customerId);
      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          String nameStr = rs.getString(1);
          String idStr = rs.getString(2);
          nameStr += " - " + rs.getString(3);

          if (idStr != null) {
            display.add(nameStr);
            id.add(idStr);
          }
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  /**
   * function to build a list of the floor locations to use in a filter list.
   */
  public void buildFloorLocationSelectedFilterList(List<String> display,
                                                   List<String> id,
                                                   int customerId) {
    if (customerId == 0)
      return;

    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
        "select floor_location, c.floor_location_id, building " +
            "from cust_floor_location c, floor_location f, warehouse w " +
            "where c.floor_location_id=f.floor_location_id and f.warehouse_id=w.warehouse_id " +
            "and customer_id=? " + "order by cust_floor_location_id", 0)) {
      pstmt.setInt(1, customerId);
      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          String nameStr = rs.getString(1);
          String idStr = rs.getString(2);

          if ((nameStr != null) && (idStr != null)) {
            display.add(nameStr);
            id.add(idStr);
          }
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  /**
   * function to build a list of the buildings to use in a filter list.
   */
  public void buildBuildingSelectedFilterList(List<String> display,
                                              List<String> id,
                                              String floorOrWarehouseId) {
    if (Util.isEmpty(floorOrWarehouseId))
      return;

    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
        "select f.warehouse_id, building from floor_location f, warehouse w " +
            "where f.warehouse_id=w.warehouse_id and floor_location=? and floor_location " +
            "not in (select floor_location from shipping_manifest m, floor_location f " +
            "where m.floor_location_id=f.floor_location_id) " + "order by building", 0)) {

      pstmt.setString(1, floorOrWarehouseId);
      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          String idStr = rs.getString(1);
          String nameStr = rs.getString(2);

          if ((nameStr != null) && (idStr != null)) {
            display.add(nameStr);
            id.add(idStr);
          }
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  /**
   * function to build a list of the buildings to use in a filter list.
   */
  public void buildBuildingFilterList(List<String> display, List<String> id,
                                      String floorOrWarehouseId) {
    try {
      String sql;
      sql = "select building, warehouse_id from warehouse ";
      if (!Util.isEmpty(floorOrWarehouseId)) {
        sql += "where warehouse_id not in (select warehouse_id from shipping_manifest m, floor_location f where m.floor_location_id=f.floor_location_id and floor_location=?) ";
      }
      sql += "order by building";

      try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(sql, 0)) {
        if (!Util.isEmpty(floorOrWarehouseId)) {
          pstmt.setString(1, floorOrWarehouseId);
        }
        try (ResultSet rs = pstmt.executeQuery()) {
          while (rs.next()) {
            String nameStr = rs.getString(1);
            String idStr = rs.getString(2);

            if ((nameStr != null) && (idStr != null)) {
              display.add(nameStr);
              id.add(idStr);
            }
          }
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  /**
   * function to build a list of the buildings to use in a filter list.
   */
  public void buildBuildingInUseFilterList(List<String> display,
                                           List<String> id,
                                           String floorOrWarehouseId) {
    if (!Util.isEmpty(floorOrWarehouseId)) {
      try {
        try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
            "select building, f.warehouse_id from shipping_manifest m, floor_location f, warehouse w " +
                "where m.floor_location_id=f.floor_location_id and f.warehouse_id=w.warehouse_id " +
                "and m.floor_location_id in (select floor_location_id from floor_location where floor_location=?) ", 0)) {
          pstmt.setString(1, floorOrWarehouseId);
          try (ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
              String nameStr = rs.getString(1);
              String idStr = rs.getString(2);

              if ((nameStr != null) && (idStr != null)) {
                display.add(nameStr);
                id.add(idStr);
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

  public void saveFloorLocation(String floor, List<String> idList) {

    try {
      List<String> existIdList = new ArrayList<>();

      deleteFloorsNoLongerInUse(floor);

      try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
          "select floor_location_id, floor_location, warehouse_id from floor_location where floor_location=?", 0)) {
        pstmt.setString(1, floor);
        try (ResultSet rs = pstmt.executeQuery()) {
          while (rs.next()) {
            existIdList.add(rs.getString(3));
          }
        }
      }

      for (String id : idList) {
        boolean found = false;
        for (String exist : existIdList) {
          if (id.equals(exist)) {
            found = true;
            break;
          }
        }

        if (!found)
          createFloorLocation(floor, id);
      }
    }
    catch (Exception e) {
      getDBTransaction().rollback();
      AdfLogUtility.logException(e);
    }
  }

  public void createFloorLocation(String floor,
                                  String warehouseId) throws SQLException {

    //* does the floor exist

    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
        "insert into floor_location (floor_location, warehouse_id) values (?,?)", 0)) {
      pstmt.setString(1, floor);
      pstmt.setString(2, warehouseId);
      pstmt.executeUpdate();
      getDBTransaction().commit();
    }
  }

  public void deleteFloorsNoLongerInUse(String floor) throws SQLException {

    //* does the floor exist
    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
        "delete from floor_location where floor_location=? and floor_location_id " +
            "not in (select floor_location_id from shipping_manifest union select floor_location_id " +
            "from cust_floor_location)", 0)) {
      pstmt.setString(1, floor);
      pstmt.executeUpdate();
      getDBTransaction().commit();
    }
  }

  /**
   * Container's getter for WacTableView1
   */
  public WacTableViewImpl getWacTableView1() {
    return (WacTableViewImpl) findViewObject("WacTableView1");
  }

  /**
   * Container's getter for WarehouseTableView1
   */
  public WarehouseTableViewImpl getWarehouseTableView1() {
    return (WarehouseTableViewImpl) findViewObject("WarehouseTableView1");
  }

  /**
   * Container's getter for EquipTableView1
   */
  public EquipTableViewImpl getEquipTableView1() {
    return (EquipTableViewImpl) findViewObject("EquipTableView1");
  }

  /**
   * Container's getter for PackingStationTableView1
   */
  public PackingStationTableViewImpl getPackingStationTableView1() {
    return (PackingStationTableViewImpl) findViewObject("PackingStationTableView1");
  }

  /**
   * Container's getter for SiteRemoteConnectionsView1
   */
  public SiteRemoteConnectionsViewImpl getSiteRemoteConnectionsView1() {
    return (SiteRemoteConnectionsViewImpl) findViewObject("SiteRemoteConnectionsView1");
  }

  /**
   * Container's getter for PickingView1
   */
  public PickingViewImpl getPickingView1() {
    return (PickingViewImpl) findViewObject("PickingView1");
  }

  /**
   * Container's getter for ChangeLocationFromView1
   */
  public ChangeLocationFromViewImpl getChangeLocationFromView1() {
    return (ChangeLocationFromViewImpl) findViewObject("ChangeLocationFromView1");
  }

  /**
   * Container's getter for ChangeLocationToView1
   */
  public ChangeLocationToViewImpl getChangeLocationToView1() {
    return (ChangeLocationToViewImpl) findViewObject("ChangeLocationToView1");
  }

  /**
   * Container's getter for LocationNonMechView1
   */
  public LocationNonMechViewImpl getLocationNonMechView1() {
    return (LocationNonMechViewImpl) findViewObject("LocationNonMechView1");
  }

  /**
   * Container's getter for WacTableMechView1
   */
  public WacTableMechViewImpl getWacTableMechView1() {
    return (WacTableMechViewImpl) findViewObject("WacTableMechView1");
  }

  /**
   * Container's getter for EquipTableConsolidationView1
   */
  public EquipTableConsolidationViewImpl getEquipTableConsolidationView1() {
    return (EquipTableConsolidationViewImpl) findViewObject("EquipTableConsolidationView1");
  }

  /**
   * Container's getter for WacTableMechView1_Workstation
   */
  @SuppressWarnings("java:S100") //(FUTURE) to rename this, iterator xml would need updated.  and unsure of ADF impact.
  public WacTableMechViewImpl getWacTableMechView1_Workstation() {
    return (WacTableMechViewImpl) findViewObject("WacTableMechView1_Workstation");
  }

  /**
   * Container's getter for PackingStationTableView1Consolidation
   */
  public PackingStationTableViewImpl getPackingStationTableView1Consolidation() {
    return (PackingStationTableViewImpl) findViewObject("PackingStationTableView1Consolidation");
  }

  /**
   * Container's getter for WarehouseViewAll1_Workstation
   */
  public WarehouseViewAllImpl getWarehouseViewAll1Workstation() {
    return (WarehouseViewAllImpl) findViewObject("WarehouseViewAllWorkstation");
  }

  /********
   * used by ChangeLocationBacking only
   */

  public void setRowflag(boolean rowflag) {
    this.rowflag = rowflag;
  }

  public boolean isRowflag() {
    return rowflag;
  }

  public int getPickingCount(int equipid, int userId) {

    try {
      int ctn = 0;
      int wacId = 0;
      try (PreparedStatement stR = getDBTransaction().createPreparedStatement(
          "select e.wac_id from equip e, wac w where e.wac_id=w.wac_id and equipment_number =?", 0)) {
        stR.setInt(1, equipid);
        try (ResultSet rs = stR.executeQuery()) {
          if (rs.next()) {
            wacId = rs.getInt(1);
          }
        }
      }

      // Add conditional Picking.ASSIGN_TO_USER = ? @ZSL 6/2018
      try (PreparedStatement stR = getDBTransaction().createPreparedStatement(
          "SELECT count(*) FROM PICKING Picking, NIIN_LOCATION NiinLocation, LOCATION Location " +
              "WHERE ((Picking.NIIN_LOC_ID = NiinLocation.NIIN_LOC_ID) AND (NiinLocation.LOCATION_ID = Location.LOCATION_ID)) " +
              "AND (Location.WAC_ID = ?) AND ((Picking.ASSIGN_TO_USER is NULL) OR (Picking.ASSIGN_TO_USER = ?)) " +
              "AND ((Picking.STATUS = 'PICK READY') OR (Picking.STATUS = 'PICK BYPASS1'))", 0)) {
        stR.setInt(1, wacId);
        stR.setInt(2, userId);
        try (ResultSet rs = stR.executeQuery()) {
          if (rs.next()) {
            ctn = rs.getInt(1);
          }
        }
      }
      return ctn;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return 0;
  }

  public int getInventoryItemCount(int equipid) {
    try {
      int ctn = 0;
      int wacId = 0;
      try (PreparedStatement stR = getDBTransaction().createPreparedStatement(
          "select e.wac_id, w.wac_number from equip e, wac w where e.wac_id=w.wac_id and equipment_number =?", 0)) {
        stR.setInt(1, equipid);
        try (ResultSet rs = stR.executeQuery()) {
          if (rs.next()) {
            wacId = rs.getInt(1);
          }
        }
      }

      try (PreparedStatement stR = getDBTransaction().createPreparedStatement(
          "SELECT count(*) as total FROM INVENTORY_ITEM i " +
              "WHERE ((i.niin_loc_ID in (select unique nl.niin_loc_id from niin_location nl, location l " +
              "where nl.location_id = l.location_id and l.wac_id = ?)) AND (i.STATUS = 'INVENTORYPENDING') " +
              "AND (i.ASSIGN_TO_USER is NULL))", 0)) {
        stR.setInt(1, wacId);
        try (ResultSet rs = stR.executeQuery()) {
          if (rs.next()) {
            ctn = rs.getInt(1);
          }
        }
      }
      return ctn;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return 0;
  }

  public int getInventoryItemCount(int equipid, int userId) {

    int ctn = 0;
    int wacId = 0;
    try {
      try (PreparedStatement stR = getDBTransaction().createPreparedStatement("select e.wac_id, w.wac_number from equip e, wac w where e.wac_id=w.wac_id and equipment_number =?", 0)) {
        stR.setInt(1, equipid);
        try (ResultSet rs = stR.executeQuery()) {
          if (rs.next()) {
            wacId = rs.getInt(1);
          }
        }
      }

      try (PreparedStatement stR = getDBTransaction().createPreparedStatement("SELECT count(*) as total FROM INVENTORY_ITEM i WHERE nvl(i.MODIFIED_BY,'-1') != nvl(?,'0') and ((i.niin_loc_ID in (select unique nl.niin_loc_id from niin_location nl, location l where nl.location_id = l.location_id and l.wac_id = ?)) AND (i.STATUS = 'INVENTORYPENDING') AND (i.ASSIGN_TO_USER is null))", 0)) {
        stR.setInt(1, userId);
        stR.setInt(2, wacId);
        try (ResultSet rs = stR.executeQuery()) {
          if (rs.next()) {
            ctn = rs.getInt(1);
          }
        }
      }
      return ctn;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return 0;
  }

  public int getLocSurveyCount(int equipid) {
    try {
      int ctn = 0;
      int wacId = 0;
      try (PreparedStatement stR = getDBTransaction().createPreparedStatement(
          "select e.wac_id from equip e, wac w where e.wac_id=w.wac_id and equipment_number =?", 0)) {
        stR.setInt(1, equipid);
        try (ResultSet rs = stR.executeQuery()) {
          if (rs.next()) {
            wacId = rs.getInt(1);
          }
        }
      }

      try (PreparedStatement stR = getDBTransaction().createPreparedStatement(
          "SELECT count(*) FROM INVENTORY_ITEM InventoryItem, location l " +
              "WHERE (((InventoryItem.location_ID = l.location_id) and (l.wac_id = ?)) " +
              "AND (InventoryItem.STATUS = 'LOCSURVEYPENDING') AND (InventoryItem.ASSIGN_TO_USER is NULL) )", 0)) {
        stR.setInt(1, wacId);
        try (ResultSet rs = stR.executeQuery()) {
          if (rs.next()) {
            ctn = rs.getInt(1);
          }
        }
      }
      return ctn;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return 0;
  }

  public int getShelfLifeCount(int equipid) {
    try {
      int ctn = 0;
      int wacId = 0;
      try (PreparedStatement stR = getDBTransaction().createPreparedStatement(
          "select e.wac_id from equip e, wac w where e.wac_id=w.wac_id and equipment_number =?", 0)) {
        stR.setInt(1, equipid);
        try (ResultSet rs = stR.executeQuery()) {
          if (rs.next()) {
            wacId = rs.getInt(1);
          }
        }
      }

      try (PreparedStatement stR = getDBTransaction().createPreparedStatement(
          "SELECT count(*) FROM NIIN_LOCATION nl, LOCATION l WHERE (nl.LOCATION_ID = l.LOCATION_ID) " +
              "AND ( nl.NUM_EXTENTS is null and nl.EXP_REMARK = 'Y') AND l.WAC_ID=?", 0)) {
        stR.setInt(1, wacId);
        try (ResultSet rs = stR.executeQuery()) {
          if (rs.next()) {
            ctn = rs.getInt(1);
          }
        }
      }
      return ctn;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return 0;
  }

  public Map<Integer, String> getGroupPrivs(long groupID) {
    HashMap<Integer, String> privMap = new HashMap<>();
    try (CallableStatement st = getDBTransaction().createCallableStatement(
        "begin ? := pkg_user_mgmt.f_get_privs_4_group(?, ?, ?); end;", DBTransaction.DEFAULT)) {
      st.registerOutParameter(1, Types.INTEGER);
      st.registerOutParameter(2, OracleTypes.CURSOR);
      st.registerOutParameter(3, Types.VARCHAR);
      st.setLong(4, groupID);
      fillPrivMap(privMap, st);
    }
    catch (SQLException e) {
      AdfLogUtility.logException(e);
    }
    return privMap;
  }

  public Map<Integer, String> getAllPrivs() {
    HashMap<Integer, String> privMap = new HashMap<>();
    try (CallableStatement st = getDBTransaction().createCallableStatement(
        "begin ? := pkg_user_mgmt.f_get_all_privs(?, ?); end;", DBTransaction.DEFAULT)) {
      st.registerOutParameter(1, Types.INTEGER);
      st.registerOutParameter(2, OracleTypes.CURSOR);
      st.registerOutParameter(3, Types.VARCHAR);
      fillPrivMap(privMap, st);
    }
    catch (SQLException e) {
      AdfLogUtility.logException(e);
    }
    return privMap;
  }

  private void fillPrivMap(HashMap<Integer, String> privMap, CallableStatement st) throws SQLException {
    int result;
    st.execute();
    result = st.getInt(1);
    if (result == 0) {
      try (ResultSet rs = ((OracleCallableStatement) st).getCursor(2)) {
        while (rs.next()) {
          privMap.put(rs.getInt(1), rs.getString(2));
        }
      }
    }
  }

  /**
   * Container's getter for WorkLoadManager1
   */
  public ApplicationModuleImpl getWorkLoadManager1() {
    return (ApplicationModuleImpl) findApplicationModule("WorkLoadManager1");
  }

  /**
   * Container's getter for GCSSMCSiteRemoteConnectionsView1
   */
  public GCSSMCSiteRemoteConnectionsViewImpl getGCSSMCSiteRemoteConnectionsView1() {
    return (GCSSMCSiteRemoteConnectionsViewImpl) findViewObject("GCSSMCSiteRemoteConnectionsView1");
  }

  /**
   * Container's getter for RemoveNonMechView1
   */
  public RemoveNonMechViewImpl getRemoveNonMechView1() {
    return (RemoveNonMechViewImpl) findViewObject("RemoveNonMechView1");
  }

  /**
   * Container's getter for WarehouseViewAllWorkstation.
   *
   * @return WarehouseViewAllWorkstation
   */
  public WarehouseViewAllImpl getWarehouseViewAllWorkstation() {
    return (WarehouseViewAllImpl) findViewObject("WarehouseViewAllWorkstation");
  }

  /**
   * Container's getter for WarehouseViewAll1_Workstation.
   *
   * @return WarehouseViewAll1_Workstation
   */
  @SuppressWarnings("java:S100") //(FUTURE) to rename this, ViewUsage xml would need updated.  and unsure of ADF impact.
  public mil.stratis.model.view.loc.WarehouseViewAllImpl getWarehouseViewAll1_Workstation() {
    return (mil.stratis.model.view.loc.WarehouseViewAllImpl) findViewObject("WarehouseViewAll1_Workstation");
  }

  /**
   * Container's getter for WacByBuildingView1.
   *
   * @return WacByBuildingView1
   */
  public ViewObjectImpl getWacByBuildingView1() {
    return (ViewObjectImpl) findViewObject("WacByBuildingView1");
  }

  /**
   * Container's getter for WorkstationTypeView1.
   *
   * @return WorkstationTypeView1
   */
  public ViewObjectImpl getWorkstationTypeView1() {
    return (ViewObjectImpl) findViewObject("WorkstationTypeView1");
  }

  /**
   * Container's getter for YesNoView1.
   *
   * @return YesNoView1
   */
  public ViewObjectImpl getYesNoView1() {
    return (ViewObjectImpl) findViewObject("YesNoView1");
  }
}
