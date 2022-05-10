package mil.stratis.model.services;

import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.common.util.Util;
import mil.stratis.model.datatype.pack.PackingIssue;
import mil.stratis.model.services.common.PackingModule;
import mil.stratis.model.util.DBUtil;
import mil.stratis.model.view.pack.*;
import mil.stratis.model.view.wlm.ShippingViewImpl;
import mil.usmc.mls2.stratis.core.utility.AdfLogUtility;
import mil.usmc.mls2.stratis.core.utility.ContextUtils;
import mil.usmc.mls2.stratis.modules.workload.domain.model.NewCartonParams;
import mil.usmc.mls2.stratis.modules.workload.domain.model.PackingStationType;
import mil.usmc.mls2.stratis.modules.workload.service.WorkloadService;
import oracle.jbo.Row;
import oracle.jbo.SessionData;
import oracle.jbo.ViewCriteria;
import oracle.jbo.ViewCriteriaRow;
import oracle.jbo.server.ApplicationModuleImpl;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

@Slf4j
@NoArgsConstructor //ADF BackingHandlers all need a no args constructor
public class PackingModuleImpl extends ApplicationModuleImpl implements PackingModule {

  public static final String PACKED = "PACKED";

  /**
   * This function clears the PIN list of all current items waiting to be packed
   * in the PIN Table on the bottom left corner of the screen.
   */
  public void clearPINList() {
    try {
      PinListImpl view = getpinlist1();
      view.executeQuery();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  /**
   * Build packingIssue object given a valid PIN
   *
   * @return PackingIssue
   * @see mil.stratis.model.datatype.pack.PackingIssue
   */
  private PackingIssue buildPackingIssue(String pin) {
    String sql = "select p.status, p.pin, nvl(p.suffix_code,''), i.scn, c.aac, i.document_number, p.qty_picked from picking p, issue i, customer c where p.pin=? and p.scn=i.scn and i.customer_id=c.customer_id";
    PackingIssue issue = null;

    try {
      try (PreparedStatement psPack = getDBTransaction().createPreparedStatement(sql, 0)) {
        psPack.setString(1, pin);
        try (ResultSet rsPack = psPack.executeQuery()) {
          if (rsPack.next()) {
            issue = new PackingIssue();
            issue.setStatus(rsPack.getString(1));
            issue.setPin(rsPack.getString(2));
            issue.setSuffixCode(rsPack.getString(3));
            issue.setScn(rsPack.getString(4));
            issue.setAac(rsPack.getString(5));
            issue.setDocumentNumber(rsPack.getString(6));
            issue.setQtyPicked(rsPack.getString(7));
          }
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }

    return issue;
  }

  /**
   * Change the status of the item to notify STRATIS
   */
  private void updatePickingStatus(String pin, String status) throws Exception {
    String sqlUpd = "update picking set status = '" + status + "' where pin = ?";
    DBUtil.updateSingleRow(sqlUpd, pin, getDBTransaction());
  }

  /**
   * Set the packing station value of the packing row
   */
  private void assignPackingStation(String packingConsolidationId,
                                    String packingStationId) throws Exception {
    String sqlUpd = "update packing_consolidation set packing_station_id = " + packingStationId + " where packing_consolidation_id = ?";
    DBUtil.updateSingleRow(sqlUpd, packingConsolidationId, getDBTransaction());
  }

  /**
   * validate PIN is at correct packing station
   * if the workstation currently logged into doesn't match the PIN's intended workstation, then return
   * <p>
   * If PIN is intended for a single packing station and there are pack columns and levels available, then
   * automatically reassign this PIN to the workstation currently logged into
   */
  private int validatePackingWorkstation(String pin,
                                         int iWorkstationId) throws Exception {
    String sql = "select e.equipment_number, e.description, pc.packing_consolidation_id, e.name, pc.pack_column, pc.pack_level from equip e, packing_station ps, packing_consolidation pc, picking p where p.pin=? and e.equipment_number = ps.equipment_number and ps.packing_station_id = pc.packing_station_id and pc.packing_consolidation_id = p.packing_consolidation_id";
    int wId;
    int newWorkstationId = 0;
    try (PreparedStatement stR = getDBTransaction().createPreparedStatement(sql, 0)) {
      stR.setString(1, pin);
      try (ResultSet rs = stR.executeQuery()) {
        if (rs.next()) {
          wId = rs.getInt(1);

          //If the workstation identifier for the pick is different than this workstation, send back
          //and error message telling the user to route the item to a different workstation
          if (wId != iWorkstationId) {
            //if (wDesc.equals("Packing Station - Single") && rs.getInt(5) > 0 && rs.getInt(6) > 0) {
            //We need to re-assign the single packing station to this packing station.
                    /*PreparedStatement psPack = getDBTransaction().createPreparedStatement("select packing_station_id from packing_station where equipment_number = ?", 0);
                    psPack.setInt(1, iWorkstationId);
                    ResultSet rsPack = psPack.executeQuery();
                    String pId = "";
                    if (rsPack.next()) {
                        pId = rsPack.getString(1);
                    }
                    rsPack.close();
                    psPack.close();

                    assignPackingStation(pConsolId, pId);
                    wId = -999;
                    newWorkstationId = iWorkstationId;

                }*/
            newWorkstationId = wId;
          }
          else {
            newWorkstationId = 0;
          }
        }
      }
    }
    return newWorkstationId;
  }

  /**
   * This function adds a PIN and it's associated values to the PIN list
   * which is displayed on the bottom left table of the packing screen.
   */
  public int addPIN(String pin, int iWorkstationId) {
    PackingIssue issue = buildPackingIssue(pin);
    if (issue == null)
      return -1;
    String status = issue.getStatus().trim();
    if (status.equalsIgnoreCase(PACKED)) {
      //Item has been packed already
      return -3;
    }

    if (!status.equalsIgnoreCase("PICKED") && !status.equalsIgnoreCase("PACKING")) {
      //* status not ready to be packed
      return -4;
    }

    try {
      PinListImpl view = getpinlist1();
      view.setRangeSize(-1);
      Row[] pinRows = view.getAllRowsInRange();
      for (Row pinRow : pinRows) {
        //If this pin has already been scanned at this station and is already
        //waiting in the PIN list.
        if (Util.cleanString(pinRow.getAttribute("PIN")).equals(pin))
          return -2;
      }

      //* validate PIN is at correct packing station
      int workstationRetVal = validatePackingWorkstation(pin, iWorkstationId);
      if (workstationRetVal > 0)
        return workstationRetVal;

      //Change the status of the item to Packing to notify STRATIS that the item
      //has arrived at a packing station
      updatePickingStatus(pin, "PACKING");

      Row row = view.createRow();
      row.setAttribute("PIN", pin);
      row.setAttribute("AAC", Util.cleanString(issue.getAac()));
      row.setAttribute("Document_Number", Util.cleanString(issue.getDocumentNumber()));
      row.setAttribute("Qty", Util.cleanInt(issue.getQtyPicked()));
      row.setAttribute("SCN", Util.cleanString(issue.getScn()));

      String pickCount;
      if (Util.isEmpty(issue.getSuffixCode())) {
        pickCount = "1 of 1";
      }
      else {
        //Find out the maximum suffix code for each SCN on insert
        String sqlCount = "select max(suffix_code) from picking where scn = '" + issue.getScn() + "'";
        int suffixMax = DBUtil.getCountValue(sqlCount, getDBTransaction()) + 1;
        int suffixCode = Util.cleanInt(issue.getSuffixCode()) + 1;

        //Add 1 to the suffix code and maximum suffix code since they both start at 0.
        pickCount = suffixCode + " of " + suffixMax;
      }

      row.setAttribute("pickNo", pickCount);
      view.insertRow(row);

      return 0;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return 0;
  }

  /**
   * This function gets the workstation name for a given equipment number
   */
  public String getWorkstationName(int wId) {
    String sql = "select name from equip where equipment_number = ?";
    return DBUtil.getSingleReturnValue(sql, String.valueOf(wId), getDBTransaction());
  }

  //************************************ end ADD PIN ***********************************//

  /**
   * AUTHOR   : John P. Owens
   * DATE     : Aug 8, 2007
   * DESC     : This function clears the Pack list of items currently being processed from PIN list
   */
  public void clearPackList(int iWorkstationId, int isCancel) {
    try {
      PinListImpl packList = getpinlist2();
      if (packList.getRowCount() > 0 && isCancel == 1) {

        Row[] packRow = packList.getAllRowsInRange();

        for (int i = 0; i < packList.getRowCount(); i++) {
          //For each row in the packing list update the PACKING_CONSOLIDATION view
          try (PreparedStatement st = getDBTransaction().createPreparedStatement("update picking set status = 'PACKING' where pin = ?", 0)) {
            st.setString(1, packRow[i].getAttribute("PIN").toString());
            st.execute();
          }

          try (PreparedStatement st2 = getDBTransaction().createPreparedStatement("update issue set status = 'PACKING' where scn = ? and status = 'PACKED'", 0)) {
            st2.setString(1, packRow[i].getAttribute("SCN").toString());
            st2.execute();
          }

          this.getDBTransaction().commit();
          this.addPIN(packRow[i].getAttribute("PIN").toString(), iWorkstationId);
        }
      }
      packList.executeQuery();
    }
    catch (Exception e) {
      this.getDBTransaction().rollback();
      AdfLogUtility.logException(e);
    }
  }

  /**
   * AUTHOR   : John P. Owens
   * DATE     : Nov 26, 2007
   * DESC     : This function clears the list of open cartons
   */
  public void clearCartonList() {

    try {
      CartonViewImpl view = getCartonView1();
      view.executeQuery();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  /**
   * AUTHOR   : John P. Owens
   * DATE     : August 30, 2007
   * DESC     : This function is used to create a consolidation barcode based on the database
   * sequence variable CONSOL_SEQ and pass it into a string.   Auto-generated
   * Consolidation Barcodes are usually only created for Consolidated Packing
   * Stations (CPCX) since Single Pack Stations use Pre-Printed Barcodes
   */
  public String createConsolidationBarcode() {
    try {
      StringBuilder cBC = new StringBuilder();
      WorkLoadManagerImpl workload = (WorkLoadManagerImpl) this.getWorkLoadManager1();
      TransactionsImpl service = (TransactionsImpl) workload.getTransactions1();
      try (PreparedStatement st = getDBTransaction().createPreparedStatement("select CONSOL_SEQ.nextval from dual", 0)) {
        try (ResultSet rs = st.executeQuery()) {
          rs.next();
          int r = rs.getInt(1);
          int sqM;
          int i = 1;
          while (i++ <= 4) {
            sqM = r % 36;
            if (sqM <= 9)
              cBC.insert(0, (char) (sqM + 48));
            else
              cBC.insert(0, (char) (sqM + 55));
            r = Math.abs(r / 36);
          }
        }
      }
      cBC.insert(0, "SB" + service.getCurrentJulian(4));
      return (cBC.toString());
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return null;
  }

  /**
   * AUTHOR   : John P. Owens
   * DATE     : September 24, 2007
   * DESC     : This function is used to re-assign all items assigned to a given carton (consolId)
   * to a new carton (newConsolId).  This should happen after a carton is closed manually
   * and a new carton is opened so that all items still pending consolidation are routed
   * to the correct Bin.
   */
  public int reAssignCarton(int consolId, int newConsolId, int userId) {
    int result = 1;

    try {
      ConsolidationViewWithBindImpl consolList = getConsolidationViewWithBind1();

      consolList.setNamedWhereClauseParam("consolId", newConsolId);
      consolList.executeQuery();

      if (consolList.getRowCount() > 0) {
        Row consolRow = consolList.getRowAtRangeIndex(0);

        double dCube, dWeight;
        String partialRelease = "";

        //Get all pending rows in the Picking table to be packed at the old consolidation container
        try (PreparedStatement st = getDBTransaction().createPreparedStatement(
            "select picking.qty_picked, niin_info.weight, niin_info.cube, packing_consolidation.partial_release, " +
                "picking.pick_qty from picking, issue, niin_info, packing_consolidation " +
                "where picking.status <> 'PACKED' and picking.status <> 'SHIPPING' and picking.status <> 'SHIPPED' " +
                "and " + "issue.niin_id = niin_info.niin_id AND issue.scn = picking.scn " +
                "AND picking.packing_consolidation_id = packing_consolidation.packing_consolidation_id " +
                "AND picking.packing_consolidation_id = ?", 0)) {

          st.setInt(1, consolId);
          try (ResultSet rs = st.executeQuery()) {

            //Get the initial cube and weight from the first item in the carton.
            dCube = Double.parseDouble(consolRow.getAttribute("ConsolidationCube").toString());
            dWeight = Double.parseDouble(consolRow.getAttribute("ConsolidationWeight").toString());

            while (rs.next()) {
              //For each row that we found still in route to the old consolidation continer,
              //assign to the new consolidation container and roll-up weight.
              if (rs.getInt(1) == 0) {
                //Roll up cube and weight (qtyPicked * value)
                dCube = dCube + rs.getDouble(3) * rs.getDouble(5);
                dWeight = dWeight + rs.getDouble(2) * rs.getDouble(5);
              }
              else {
                //Roll up cube and weight (qtyPicked * value)
                dCube = dCube + rs.getDouble(3) * rs.getDouble(1);
                dWeight = dWeight + rs.getDouble(2) * rs.getDouble(1);
              }
              partialRelease = rs.getString(4);
            }
          }
        }

        consolRow.setAttribute("ConsolidationCube", dCube);
        consolRow.setAttribute("ConsolidationWeight", dWeight);
        consolRow.setAttribute("ModifiedBy", userId);
        consolRow.setAttribute("PartialRelease", partialRelease);

        //Number of issues isn't one-to-one to rows returned
        //roll up number of issues.
        try (PreparedStatement st2 = getDBTransaction().createPreparedStatement(
            "select count(unique scn) from picking  where status <> 'SHIPPING' and status <> 'SHIPPED' and " +
                "packing_consolidation_id = ? ", 0)) {
          st2.setInt(1, newConsolId);
          try (ResultSet rs2 = st2.executeQuery()) {
            if (rs2.next()) {
              consolRow.setAttribute("NumberOfIssues", rs2.getInt(1));
            }
          }
        }

        //commit our changes
        this.getDBTransaction().commit();

        try (PreparedStatement stPick = getDBTransaction().createPreparedStatement("update picking set packing_consolidation_id = ? where packing_consolidation_id = ? and (status = 'PACKING' or status like 'PICK%')", 0)) {
          stPick.setInt(1, newConsolId);
          stPick.setInt(2, consolId);
          stPick.execute();
        }
        try (PreparedStatement stConsol = getDBTransaction().createPreparedStatement("update packing_consolidation set number_of_issues = (select count(unique scn) from picking  where status <> 'SHIPPING' and status <> 'SHIPPED' and packing_consolidation_id = ?) where packing_consolidation_id = ?", 0)) {
          stConsol.setInt(1, consolId);
          stConsol.setInt(2, consolId);
        }
        //commit our changes
        this.getDBTransaction().commit();

        result = 0;
      }
      else {
        //The new consolidation id is not found in the PACKING_CONSOLIDATION table.

        result = -1;
      }
    }
    catch (Exception e) {
      this.getDBTransaction().rollback();
      AdfLogUtility.logException(e);
    }
    if (result == 1) {
      result = 0;
    }
    return result;
  }

  /**
   * AUTHOR   : John P. Owens
   * DATE     : September 05, 2007
   * DESC     : This function is used to close a specific consolidation bin
   * On CLOSE CARTON:
   * #1 set close_carton to Y in PACKING_CONSOLIDATION
   * #2 update modified_by in PACKING_CONSOLIDATION
   * #3 Roll up cube and weight of all items contained in PICKING for this bin that have status of PACKED
   * #4 update number of issues in PACKING_CONSOLIDATION for this bin
   * #5 assign a consolidation barcode in PACKING_CONSOLIDATION for the bin
   */
  public int closeCarton(int consolId, int userId) {

    int retVal = -3;
    try (PreparedStatement pickPs = getDBTransaction().createPreparedStatement(
        "select niin_info.cube, niin_info.weight, picking.qty_picked, picking.scn from picking, issue, niin_info " +
            "where picking.packing_consolidation_id = ? and picking.status = 'PACKED' " +
            "and picking.scn = issue.scn and issue.niin_id = niin_info.niin_id", 0)) {
      //#3 This should show all picked issues for this consolidation bin that have
      //a status of PACKED

      pickPs.setInt(1, consolId);

      String scn = null;
      double dCube = 0, dWeight = 0;
      try (ResultSet pickRs = pickPs.executeQuery()) {

        //#4 Roll up all cubes and weights for the bin.  Otherwise bust
        while (pickRs.next()) {
          dCube += dCube + pickRs.getDouble(3) * pickRs.getDouble(1);
          dWeight += dWeight + pickRs.getDouble(3) * pickRs.getDouble(2);
          scn = pickRs.getString(4);
        }
      }

      if (scn == null || scn.equals("")) {
        retVal = -4;
      }
      else {
        try (PreparedStatement stR = getDBTransaction().createPreparedStatement(
            "select count(*) from picking where ((status = 'PICK READY' OR status = 'PICKED' OR status = 'PACKING' " +
                "OR status like 'PICK BYPASS%') and packing_consolidation_id = ?) OR (status = 'PICK REFUSED' AND scn = ?)", 0)) {
          stR.setInt(1, consolId);
          stR.setString(2, scn);
          try (ResultSet rs = stR.executeQuery()) {
            if (rs.next()) {
              retVal = rs.getInt(1);
            }
          }
        }
        try (PreparedStatement conPs = getDBTransaction().createPreparedStatement(
            "update packing_consolidation set consolidation_cube = ?, consolidation_weight = ?, " +
                "consolidation_barcode = ?, modified_by = ?, close_carton = ? where packing_consolidation_id = ?", 0)) {
          conPs.setDouble(1, dCube);
          conPs.setDouble(2, dWeight);

          //#2 Set the modified_by attribute in the packing_consolidation table

          //#5 Create a consolidation Barcode
          conPs.setString(3, this.createConsolidationBarcode());
          conPs.setInt(4, userId);
          //#1 Set closed carton to Y
          conPs.setString(5, "Y");

          conPs.setInt(6, consolId);
          conPs.execute();

          //commit changes.
          this.getDBTransaction().commit();
        }
      }
    }

    catch (Exception e) {
      this.getDBTransaction().rollback();
      AdfLogUtility.logException(e);
    }
    return retVal;
  }

  /**
   * AUTHOR   : John P. Owens
   * DATE     : Nov 26, 2007
   * DESC     : This function is used to manually close a carton
   */
  public boolean manualCloseCarton(int consolId, int userId) {

    try {
      WorkLoadManagerImpl workload = (WorkLoadManagerImpl) this.getWorkLoadManager1();
      int newConsolId = -1;
      int ccRet = closeCarton(consolId, userId);

      if (ccRet >= 0) {
        //Carton Closed Succesfully.
        if (ccRet > 0) {
          //Need to redirect remainder of items coming to this carton
          try (PreparedStatement stR = getDBTransaction().createPreparedStatement(
              "select packing_station.packing_station_id, equip.name, packing_station.levels, " +
                  "packing_station.columns, packing_consolidation.customer_id, packing_consolidation.issue_priority_group " +
                  "from packing_consolidation, packing_station, equip " +
                  "where packing_consolidation.packing_station_id = packing_station.packing_station_id " +
                  "and packing_station.equipment_number = equip.equipment_number " +
                  "and packing_consolidation.packing_consolidation_id = ?", 0)) {
            stR.setInt(1, consolId);
            try (ResultSet rs = stR.executeQuery()) {
              if (rs.next()) {

                val workloadService = ContextUtils.getBean(WorkloadService.class);

                val stationId = rs.getObject(1);
                val stationLevels = rs.getObject(3);
                val stationColumns = rs.getObject(4);

                val params = NewCartonParams.builder()
                    .stationId(Util.cleanInt(stationId, -1))
                    .stationLevels(Util.cleanInt(stationLevels, 1))
                    .stationColumns(Util.cleanInt(stationColumns, 1))
                    .build();

                val customerId = Integer.getInteger(rs.getObject(5).toString());
                val cube = new BigDecimal(0);
                val weight = new BigDecimal(0);
                val pQty = 0;
                val iQty = 0;
                val priorityGroup = rs.getString(6);
                val info = workloadService.openNewCarton(params, customerId, weight, cube, pQty, userId, PackingStationType.CONSOLIDATION, iQty, priorityGroup);
                newConsolId = info.packingConsolidationId();
              }
            }
          }
          if (newConsolId == -1) {
            return false;
          }
          else {
            //New consolidation Bin opened successfully
            //Now redirect remaining PINs to the new consolidation station
            int iRetVal = reAssignCarton(consolId, newConsolId, userId);
            return iRetVal >= 0;
          }
        }
        return true;
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return false;
  }

  /**
   * This function is used to automatically check if it is
   * necessary to close a specific consolidation bin and if
   * it is, to do so.
   */
  public int autoCloseCarton(int consolId, int userId) {
    int retVal = -1;
    try {

      String scn = null;
      try (PreparedStatement stPick = getDBTransaction().createPreparedStatement("select scn from picking where (status = 'PICK REFUSED') and packing_consolidation_id = ?", 0)) {
        stPick.setInt(1, consolId);
        try (ResultSet rsPick = stPick.executeQuery()) {
          if (rsPick.next()) {
            scn = rsPick.getString(1);
          }
        }
      }

      try (PreparedStatement stR = getDBTransaction().createPreparedStatement(
          "select count(*) from picking where ((status = 'PICK READY' OR status like 'PICK BYPASS%') " +
              "and packing_consolidation_id = ?) OR (scn IS NOT NULL AND status = 'PICK REFUSED' AND scn = ?)", 0)) {

        stR.setInt(1, consolId);
        stR.setString(2, Util.cleanString(scn));
        try (ResultSet rs = stR.executeQuery()) {
          if (rs.next()) {
            int countVal = rs.getInt(1);
            if (countVal == 0) {
              //All PINs are packed for this consolidation bin!
              retVal = closeCarton(consolId, userId);
            }
            else {
              //return the number of PINS still expected to show up for the bin
              retVal = countVal;
            }
          }
          else {
            //ResultSet Error
            retVal = -2;
          }
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return retVal;
  }

  public void prepareSession(SessionData sessionData) {
    super.prepareSession(sessionData);
  }

  /**
   * AUTHOR   : John P. Owens
   * DATE     : Sept 05, 2007
   * DESC     : This function completes the packing process for a consolidated
   * issue and sets its status to PACKED in the database.
   */
  public boolean packConsolidatedIssue(int consolId, int userId) {
    boolean result = false;
    boolean finished = false;

    try {

      PickingViewWithBindImpl pickView = getPickingViewWithBind1();
      PinListImpl packList = getpinlist2();

      if (packList.getRowCount() > 0) {
        Row packRow = packList.getRowAtRangeIndex(0);

        //For each row in the packing list update the PACKING_CONSOLIDATION view
        pickView.setNamedWhereClauseParam("pinVal", packRow.getAttribute("PIN").toString());
        pickView.executeQuery();

        Row pickRow = pickView.getRowAtRangeIndex(0);

        if (Util.cleanString(pickRow.getAttribute("Pin")) == null) {
          finished = true;
        }
        else {
          pickRow.setAttribute("Status", PACKED);
          pickRow.setAttribute("ModifiedBy", userId);
          String scn = pickRow.getAttribute("Scn").toString();
          this.getDBTransaction().commit();
          try (PreparedStatement stR = this.getDBTransaction().createPreparedStatement("update issue set packing_consolidation_id = ?, modified_by = ? where scn = ?", 0)) {
            stR.setInt(1, consolId);
            stR.setInt(2, userId);
            stR.setString(3, scn);
            stR.execute();

            //Add the consolidation barcode to the PACKING_CONSOLIDATION table and commit.
            this.getDBTransaction().commit();
          }
        }
        if (!finished) {
          result = true;
        }
      }
    }
    catch (Exception e) {
      this.getDBTransaction().rollback();
      AdfLogUtility.logException(e);
    }
    return result;
  }

  /**
   * This function is used to pass the backing bean a boolean return value
   * to determine if the logged in workstation is a consolidation packing
   * station or not CPCX vs MCPX, which controls the graphical switcher
   * control.
   */
  public boolean isConsolStation(int workstationId) {
    String sql = "select description from equip where equipment_number=?";
    String desc = DBUtil.getSingleReturnValue(sql, workstationId, getDBTransaction());
    return (desc.equals("Packing Station - Consolidation"));
  }

  /**
   * AUTHOR   : John P. Owens
   * DATE     : Aug 31, 2007
   * DESC     : This function is used to retrieve the Location Barcode for a consolidation
   * packing station based on the packing consolidation id
   */
  public String getConsolidationBarcode(int consolId) {

    try (PreparedStatement stR = getDBTransaction().createPreparedStatement(
        "select consolidation_barcode from packing_consolidation where packing_consolidation_id = ?", 0)) {
      stR.setInt(1, consolId);
      try (ResultSet rs = stR.executeQuery()) {
        if (rs.next()) {
          return rs.getString(1);
        }
        else {
          //ERROR
          return "Error -1";
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return null;
  }

  /**
   * AUTHOR   : John P. Owens
   * DATE     : Aug 31, 2007
   * DESC     : This function is used to retrieve the Location Barcode for a consolidation
   * packing station based on the packing consolidation id
   */
  public String getLocationBarcode(int consolId) {

    try (PreparedStatement stR = getDBTransaction().createPreparedStatement("select pack_location_barcode from packing_consolidation where packing_consolidation_id = ?", 0)) {
      String retVal;
      stR.setInt(1, consolId);
      {
        try (ResultSet rs = stR.executeQuery()) {
          if (rs.next()) {
            retVal = rs.getString(1);
            return retVal;
          }
          else {
            //ERROR
            return "Error -1";
          }
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return null;
  }

  /**
   * AUTHOR   : John P. Owens
   * DATE     : May 5, 2008
   * DESC     : This function is used to check if there are any partial releases pending at this workstation
   * PARMS    : iWorkstationId (the workstation we are checking)
   */
  public boolean isPartialsPending(int iWorkstationId) {

    boolean retVal = false;
    try {
      int consolId = 0;
      try (PreparedStatement stR = getDBTransaction().createPreparedStatement(
          "select packing_consolidation.packing_consolidation_id from packing_consolidation, packing_station " +
              "where packing_consolidation.packing_station_id = packing_station.packing_station_id " +
              "and packing_consolidation.partial_release = 'Y' and packing_consolidation.close_carton = 'N' " +
              "and packing_station.equipment_number = ?", 0)) {
        stR.setInt(1, iWorkstationId);
        try (ResultSet rs = stR.executeQuery()) {
          if (rs.next()) {
            if (rs.getObject(1) == null)
              retVal = false;
            else {
              consolId = rs.getInt(1);
            }
          }
        }
      }

      int otherCount = 0;
      if (consolId > 0) {
        log.debug("partial releases found +  {}", consolId);
        try (PreparedStatement StCount = getDBTransaction().createPreparedStatement("select status from picking where packing_consolidation_id = ? and status <> 'PICK REFUSED'", 0)) {
          StCount.setInt(1, consolId);
          try (ResultSet rsCount = StCount.executeQuery()) {
            while (rsCount.next()) {
              if (!rsCount.getString(1).equals(PACKED))
                otherCount++;
            }
          }
        }

        if (otherCount == 0) {
          int testRet = this.releasePartialIssue(consolId);
          if (testRet == 0)
            retVal = true;
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return retVal;
  }

  /**
   * AUTHOR   : John P. Owens
   * DATE     : May 5, 2008
   * DESC     : This function takes a given consolidation bin and releases all of the PINs associated to it and releases
   * them to shipping, regardless if the entire qty for a given issue has been fullfilled or not.
   * PARMS    : consolId - the packing_consolidation_id of the consolidation bin
   */
  public int releasePartialIssue(int consolId) {

    try {
      PinListImpl packList = getpinlist2();
      packList.executeQuery();

      try (PreparedStatement stR = getDBTransaction().createPreparedStatement(
          "select picking.pin, customer.aac, issue.document_number, picking.qty_picked, picking.scn from picking, " +
              "issue, customer where picking.scn = issue.scn and issue.customer_id = customer.customer_id " +
              "and picking.packing_consolidation_id = ?", 0)) {
        stR.setInt(1, consolId);
        try (ResultSet rs = stR.executeQuery()) {
          if (rs.next()) {
            //Add this PIN to the packing list
            Row row = packList.createRow();
            row.setAttribute("PIN", rs.getString(1));
            row.setAttribute("AAC", rs.getString(2));
            row.setAttribute("Document_Number", rs.getString(3));
            row.setAttribute("Qty", rs.getInt(4));
            row.setAttribute("SCN", rs.getString(5));

            packList.insertRow(row);
          }
        }
      }
      return 0;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return -1;
  }

  /**
   * AUTHOR   : John P. Owens
   * DATE     : March 7, 2008
   * DESC     : This function is used to check if this is the last pin arriving at an MCPX station for a given issue (scn)
   * PARMS    : scn - the issue we are checking
   */
  public boolean isLastPin(String scn) {

    boolean retVal = true;
    try (PreparedStatement stR = getDBTransaction().createPreparedStatement("select status, pin, nvl(packing_consolidation_id,0) from picking where scn = ?", 0)) {
      int refuseCount = 0, otherCount = 0, consolId = 0;
      PinListImpl packList = getpinlist2();
      Row packRow = packList.getRowAtRangeIndex(0);

      stR.setString(1, scn);
      try (ResultSet rs = stR.executeQuery()) {
        while (rs.next()) {
          if (rs.getString(1).equals(PACKED)) {
            consolId = rs.getInt(3);
          }
          else {
            if (rs.getString(1).equals("PICK REFUSED"))
              refuseCount++;
            else {
              otherCount++;
              consolId = rs.getInt(3);
              if (packRow.getAttribute("PIN").toString().equals(rs.getString(2)))
                otherCount--;
            }
          }
        }
      }
      if (otherCount > 0) {
        retVal = false;
      }
      else if (refuseCount > 0) {
        if (consolId != 0) {
          try (PreparedStatement stC = getDBTransaction().createPreparedStatement("select partial_release from packing_consolidation where packing_consolidation_id = ?", 0)) {
            stC.setInt(1, consolId);
            try (ResultSet rsC = stC.executeQuery()) {
              if (rsC.next()) {
                retVal = rsC.getString(1).equals("Y");
              }
            }
          }
        }
        else {
          retVal = false;
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return retVal;
  }

  /**
   * This function is used to retrieve the Location Barcode for a consolidation
   * packing station based on the packing consolidation id
   */
  public int getConsolIdByPin(String pin) {

    try {
      if (pin.equals("packlist")) {
        //Do not access by a PIN directly, just get the first pin from the packing list
        PinListImpl packList = getpinlist2();
        if (packList.getRowCount() > 0) {
          Row packRow = packList.getRowAtRangeIndex(0);
          //Set the pin to the pin of the first row
          pin = packRow.getAttribute("PIN").toString();
        }
        else {
          //No rows in the packlist
          return -2;
        }
      }

      String sql = "select packing_consolidation_id from picking where pin = ?";
      return Util.cleanInt(DBUtil.getSingleReturnValue(sql, pin, getDBTransaction()));
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return -1;
  }

  /**
   * AUTHOR   : John P. Owens
   * DATE     : Aug 31, 2007
   * DESC     : This function is used to retrieve the Consolidation Bin Level and Column for
   * a consolidation packing station based on the packing consolidation id
   */
  public String getBinColAndLev(int consolId) {

    try (PreparedStatement stR = getDBTransaction().createPreparedStatement("select pack_level, pack_column from packing_consolidation where packing_consolidation_id = ?", 0)) {
      String retVal;
      int iLev = 0, iCol = 0;
      stR.setInt(1, consolId);
      try (ResultSet rs = stR.executeQuery()) {
        if (rs.next()) {
          iLev = rs.getInt(1);
          iCol = rs.getInt(2);
        }
      }

      retVal = "";
      if (iCol < 10)
        retVal = "0";
      if (iLev == 0)
        retVal = retVal + "0";
      else
        retVal = retVal + iCol + (char) (iLev + 64);

      return retVal;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return "Error -1";
  }

  /**
   * AUTHOR   : John P. Owens
   * DATE     : August 07, 2007
   * DESC     : This function takes a scanned consolidation barcode and adds
   * it to the database for all items in the current issue.
   */
  public int addConsolBarcode(String consolBarcode, int userId) {

    if (consolBarcode == null)
      return -1;
    if (inUseBarcode(consolBarcode))
      return -4;

    try {
      PickingViewWithBindImpl pickView = getPickingViewWithBind1();
      String scn = "";

      PinListImpl packList = getpinlist2();

      if (packList.getRowCount() > 0) {

        Row[] packRow = packList.getAllRowsInRange();
        int consolId = 0;

        for (int i = 0; i < packList.getRowCount(); i++) {
          //For each row in the packing list update the PACKING_CONSOLIDATION view
          pickView.setNamedWhereClauseParam("pinVal", packRow[i].getAttribute("PIN").toString());
          pickView.executeQuery();

          Row pickRow = pickView.getRowAtRangeIndex(0);

          if (pickRow.getAttribute("Pin").toString() != null) {
            if (i == 0) {
              consolId = Integer.parseInt(pickRow.getAttribute("PackingConsolidationId").toString());
              scn = pickRow.getAttribute("Scn").toString();
            }
            pickRow.setAttribute("Status", PACKED);
            log.debug("set to PACKED pin  {}", pickRow.getAttribute("Pin").toString());
          }
          else {
            this.getDBTransaction().rollback();
            return -2;
          }
        }
        ConsolidationViewWithBindImpl consolList = getConsolidationViewWithBind1();
        consolList.setNamedWhereClauseParam("consolId", consolId);
        consolList.executeQuery();

        if (consolList.getRowCount() > 0) {
          //There is a row to update! Yay!
          Row row = consolList.getRowAtRangeIndex(0);
          row.setAttribute("ConsolidationBarcode", consolBarcode);
          row.setAttribute("ModifiedBy", userId);
          row.setAttribute("CloseCarton", "Y");
          this.getDBTransaction().commit();

          try (PreparedStatement stR = getDBTransaction().createPreparedStatement("update issue set packing_consolidation_id = ?, modified_by = ? where scn = ?", 0)) {
            stR.setInt(1, consolId);
            stR.setInt(2, userId);
            stR.setString(3, scn);
            stR.execute();
          }
        }
        this.getDBTransaction().commit();
        return 0;
      }
      else {
        return -3;
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      getDBTransaction().rollback();
    }
    return -6;
  }

  public boolean inUseBarcode(String barcode) {
    boolean inuse = false;
    try {
      try (PreparedStatement refPs = getDBTransaction().createPreparedStatement("select consolidation_barcode from packing_consolidation where consolidation_barcode = ?", 0)) {
        refPs.setString(1, barcode.trim().toUpperCase());
        try (ResultSet refRs = refPs.executeQuery()) {
          if (refRs.next()) {
            inuse = true;
          }
        }
      }
      if (!inuse) {
        try (PreparedStatement refPs = getDBTransaction().createPreparedStatement("select consolidation_barcode from packing_consolidation_hist where consolidation_barcode=?", 0)) {
          refPs.setString(1, barcode.trim().toUpperCase());
          try (ResultSet refRs = refPs.executeQuery()) {
            if (refRs.next()) {
              inuse = true;
            }
          }
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return inuse;
  }

  /**
   * AUTHOR   : John P. Owens
   * DATE     : Nov 26, 2007
   * DESC     : This function displays all of the current open cartons
   */
  public void displayCartons(int iWorkstationId) {

    try (PreparedStatement stR = getDBTransaction().createPreparedStatement(
        "select packing_consolidation.pack_column, packing_consolidation.pack_level, packing_consolidation.number_of_issues, " +
            "packing_consolidation.consolidation_cube, packing_consolidation.consolidation_weight, " +
            "packing_consolidation.pack_location_barcode, customer.aac, packing_consolidation.packing_consolidation_id " +
            "from packing_consolidation, customer, packing_station where packing_consolidation.close_carton = 'N' " +
            "and packing_consolidation.customer_id = customer.customer_id " +
            "and packing_station.packing_station_id = packing_consolidation.packing_station_id " +
            "and packing_station.equipment_number = ?", 0)) {
      CartonViewImpl view = getCartonView1();
      view.executeQuery();
      stR.setInt(1, iWorkstationId);
      try (ResultSet rs = stR.executeQuery()) {
        while (rs.next()) {
          try (PreparedStatement stR2 = getDBTransaction().createPreparedStatement(
              "select count(*) from issue where issue.status = 'PACKED' and packing_consolidation_id = ?", 0)) {
            stR2.setInt(1, rs.getInt(8));
            try (ResultSet rs2 = stR2.executeQuery()) {
              if (rs2.next()) {
                if (rs2.getObject(1) != null) {
                  if (rs2.getInt(1) > 0) {
                    Row row = view.createRow();
                    row.setAttribute("BinLocation", this.getBinColAndLev(rs.getInt(8)));
                    row.setAttribute("PackLocationBarcode", rs.getString(6));
                    row.setAttribute("NumberOfIssues", rs.getInt(3));
                    row.setAttribute("ConsolidationCube", rs.getDouble(4));
                    row.setAttribute("ConsolidationWeight", rs.getDouble(5));
                    row.setAttribute("PackingConsolidationId", rs.getInt(8));
                    row.setAttribute("CustomerAac", rs.getString(7));
                    view.insertRow(row);
                  }
                }
              }
            }
          }
        }
      }
      if (view.getRowCount() > 0) {
        //Set the selection to the first row.
        view.setCurrentRowAtRangeIndex(0);
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  /**
   * AUTHOR   : John P. Owens
   * DATE     : August 3, 2007
   * DESC     : This function takes the passed SCN and sees how many PINs were scanned
   * for it at the current packing station based on the scanList passed to this function.
   */
  public int getPINcountForSCN(ArrayList scanList, String scn) {

    try {
      int pinCount = 0;
      HashMap pinMap;

      //Loop through every item in the list of Scanned PINs
      for (Object o : scanList) {
        //List is a list of hashmaps, get a hashmap for the next spot in the arraylist.
        pinMap = (HashMap) o;
        if (scn.equals(pinMap.get("scn"))) {
          pinCount++;
        }
      }
      return pinCount;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return 0;
  }

  /**
   * AUTHOR   : John P. Owens
   * DATE     : August 2, 2007
   * DESC     : This function takes the current PIN list and checks if any PINs in the list
   * were part of issues picked from multi-location picks.  If so, it will put them
   * into a rejection list if they have not all arrived at this packing station.
   */
  public HashSet processPINList(ArrayList scanList, HashSet scnSet) {

    try {
      PickingViewImpl pickView = getPickingView1();
      HashMap pinMap;
      Iterator itr = scanList.iterator();

      ViewCriteria vc = pickView.createViewCriteria();
      ViewCriteriaRow vcr = vc.createViewCriteriaRow();
      vcr.setUpperColumns(true);

      //Loop through every item in the list of Scanned PINs
      while (itr.hasNext()) {
        //List is a list of hashmaps, get a hashmap for the next spot in the arraylist.
        pinMap = (HashMap) itr.next();

        //Filter picking table for this SCN
        vcr.setAttribute("Scn", " = '" + pinMap.get("scn") + "'");
        vc.addElement(vcr);

        pickView.applyViewCriteria(vc);
        pickView.executeQuery();

        //Check if there is more than one pick location for the given SCN Issue
        if (pickView.getRowCount() > 1) {
          if (pickView.getRowCount() == getPINcountForSCN(scanList, pinMap.get("scn").toString())) {
            //If all of the planned picks in the PICKING table arent in the pick list, add to the reject list.
            //Add to the set of SCNs to reject
            scnSet.add(pinMap.get("scn").toString());
          }
        }
      }

      return scnSet;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return scnSet;
  }

  /**
   * AUTHOR   : John P. Owens
   * DATE     : August 28, 2007
   * DESC     : This function displays the PINLoad Detail report on the
   * Packing_Home page and refrshes it based on the workstationId
   * in the user bean.
   */
  public void refreshPINLoadDetail(int wId) {

    try {
      PinLoadDetailImpl pinView = getpinLoadDetail1();
      pinView.setNamedWhereClauseParam("equipNum", wId);
      pinView.executeQuery();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  /**
   * This function takes the current PIN array list in memory and
   * finds the first one that doesnt have a SCN in the rejectSet.
   * Once it does that it adds every PIN with that SCN into the PackingList
   * Group so that a 1348 can be printed for that Issue grouping and the
   * picked items are now grouped by Issue.
   */
  public ArrayList fillPackingList(ArrayList scanList) {
    try {
      HashMap pinMap;
      Iterator itr = scanList.iterator();
      ArrayList newList = new ArrayList();
      PinListImpl pinList = getpinlist1();
      PinListImpl packList = getpinlist2();

      if (itr.hasNext()) {
        //List is a list of hashmaps, get a hashmap for the next spot in the arraylist.
        pinMap = (HashMap) itr.next();

        //Add this PIN to the packing list
        Row row = packList.createRow();
        row.setAttribute("PIN", pinMap.get("pin").toString());
        row.setAttribute("AAC", pinMap.get("aac").toString());
        row.setAttribute("Document_Number", pinMap.get("doc").toString());
        row.setAttribute("Qty", pinMap.get("qty").toString());
        row.setAttribute("SCN", pinMap.get("scn").toString());

        packList.insertRow(row);

        //Remove this PIN record from the PIN list
        if (pinList.getRowCount() > 0) {
          Row[] pinRows = pinList.getAllRowsInRange();
          for (int i = 0; i < pinList.getRowCount(); i++) {
            if (pinRows[i].getAttribute("PIN").toString().equals(pinMap.get("pin").toString())) {
              pinList.setCurrentRowAtRangeIndex(i);
              pinList.removeCurrentRow();
            }
          }
        }
      }

      log.debug("packing - populate the pinlist2iterator");

      return newList;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return scanList;
  }

  private int getPackingConsolidationId(String barcode) {
    String sql = "select packing_consolidation_id from packing_consolidation where consolidation_barcode=?";
    return Util.cleanInt(DBUtil.getSingleReturnValue(sql, barcode, getDBTransaction()));
  }

  private String getHtmlEscapedShipToString(String aac, String address1, String address2, String city, String state, String zip) {
    String shipTo = escapeHtml(aac) + "<br/>" + escapeHtml(address1);

    if (address2 != null && !address2.isEmpty())
      shipTo += "<br/>" + escapeHtml(address2);
    shipTo += "<br/>";

    if (city != null && !city.isEmpty())
      shipTo += escapeHtml(city);

    if (state != null && !state.isEmpty())
      shipTo += " " + escapeHtml(state);

    if (zip != null && !zip.isEmpty())
      shipTo += " " + escapeHtml(zip);

    return shipTo;
  }

  /**
   * Returns a full string formatted as html.
   * This is the actual manifest to be printed.
   *
   * @author Veronica M. Berryman
   * @date 01/16/2008
   */
  public String printContainerSummary(String barcodeString, String urlContextPath) {

    log.debug("printing container summary  {}", barcodeString);
    StringBuilder html = new StringBuilder(4000);

    PackingLabelValues packingLabelValues = getPackingConsolidationInfo(barcodeString);

    if (packingLabelValues.packingConsolidationId != null) {

      packingLabelValues = getFullPackingLabelValues(packingLabelValues);

      packingLabelValues.slot = getBinColAndLev(getPackingConsolidationId(barcodeString.toUpperCase()));

      if (packingLabelValues.slot.equals("Error -1"))
        packingLabelValues.slot = "";

      List<PackingLineItemValues> packingLineItems = getPackingLineItems(packingLabelValues.packingConsolidationId);
      return assembleContainerSummaryHtml(barcodeString, urlContextPath, packingLabelValues, packingLineItems);
    }

    return "";
  }

  /**
   * Given the packing label values, generate HTML representation of the container summary.
   */
  String assembleContainerSummaryHtml(String barcodeString, String urlContextPath, PackingLabelValues packingLabelValues, List<PackingLineItemValues> packingLineItems) {

    StringBuilder htmlStringBuffer = new StringBuilder(getFirstPartHtmlBlock(urlContextPath, barcodeString, packingLabelValues));
    packingLineItems.forEach(it -> htmlStringBuffer.append(getPackingLineItemHtmlBlock(it)));
    htmlStringBuffer.append(getFooterHtmlBlock());
    return htmlStringBuffer.toString();
  }

  /**
   *
   */
  private PackingLabelValues getPackingConsolidationInfo(String barcodeString) {
    PackingLabelValues packingLabelValues = new PackingLabelValues();

    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
        "select c.packing_consolidation_id, p.packing_station_id, p.number_of_slots_in_use, e.name, e.description from " +
            "packing_consolidation c, packing_station p, equip e " +
            "where c.packing_station_id=p.packing_station_id and p.equipment_number=e.equipment_number " +
            "and consolidation_barcode=?", 0)) {

      pstmt.setString(1, barcodeString.toUpperCase());
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          packingLabelValues.packingConsolidationId = escapeHtml(rs.getString(1));
          packingLabelValues.station = escapeHtml(rs.getString(4));
        }
      }
    }
    catch (Exception e) {log.trace("Exception caught and ignored", e);}
    return packingLabelValues;
  }

  /**
   *
   */
  List<PackingLineItemValues> getPackingLineItems(String packing_consolidation_id) {

    List<PackingLineItemValues> packingLineItems = new ArrayList<>();
    try (PreparedStatement pstmt1 = getDBTransaction().createPreparedStatement(
        "select p.pin, i.scn, i.document_number, n.niin, n.fsc, n.scc, n.ui, p.qty_picked, i.supplementary_address " +
            "from picking p INNER JOIN issue i LEFT JOIN niin_info n on i.niin_id=n.niin_id on p.scn=i.scn " +
            "where i.packing_consolidation_id=?", 0)) {
      pstmt1.setString(1, packing_consolidation_id);

      try (ResultSet rs = pstmt1.executeQuery()) {
        while (rs.next()) {
          String pin = rs.getString(1);
          String document_number = rs.getString(3);
          String niin = rs.getString(4);
          String fsc = rs.getString(5);
          String ui = rs.getString(7);
          String quantity = rs.getString(8);
          String supp_add = rs.getString(9);
          String nsn = normalizeValue(fsc) + normalizeValue(niin);

          // Add each packing line item for the given packing consolidation id
          packingLineItems.add(
              PackingLineItemValues.builder()
                  .pin(pin)
                  .documentNumber(document_number)
                  .nsn(nsn)
                  .ui(normalizeValue(ui))
                  .quantity(normalizeValue(quantity))
                  .suppAdd(normalizeValue(supp_add))
                  .build());
        }
      }
    }
    catch (Exception e) {
      log.trace("Exception caught and ignored", e);
    }
    return packingLineItems;
  }

  private PackingLabelValues getFullPackingLabelValues(PackingLabelValues oldValues) {
    PackingLabelValues packingLabelValues = new PackingLabelValues(oldValues);

    try (PreparedStatement pstmtTcn = getDBTransaction().createPreparedStatement(
        "select tcn, aac, address_1, address_2, city, state, zip_code, route_name from " +
            "shipping s INNER JOIN packing_consolidation p INNER JOIN customer c LEFT JOIN shipping_route r " +
            "on c.shipping_route_id=r.route_id on p.customer_id=c.customer_id " +
            "on s.packing_consolidation_id=p.packing_consolidation_id where p.packing_consolidation_id=?", 0)) {

      pstmtTcn.setString(1, packingLabelValues.packingConsolidationId);
      try (ResultSet rs = pstmtTcn.executeQuery()) {
        if (rs.next()) {
          packingLabelValues.tcn = rs.getString(1);

          String aac = rs.getString(2);
          String address1 = rs.getString(3);
          String address2 = rs.getString(4);
          String city = rs.getString(5);
          String state = rs.getString(6);
          String zip = rs.getString(7);

          packingLabelValues.escapedShipTo = getHtmlEscapedShipToString(aac, address1, address2, city, state, zip);
          packingLabelValues.routeTo = normalizeValue(rs.getString(8));
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return packingLabelValues;
  }

  /**
   *
   */
  StringBuffer getFirstPartHtmlBlock(String urlContextPath, String barcodeString, PackingLabelValues labelValues) {
    StringBuffer html = new StringBuffer();

    html.append("<html>");
    html.append("<head>");

    html.append("<style>");
    html.append("table {font-size:14px}");
    html.append(".summaryTable {font-weight:bold;border-bottom: 1px solid black}");
    html.append(".boldBarcode {font-size:14px; font-weight:bold}");
    html.append(".boldLine {font-size:14px; font-weight:bold}");
    html.append("</style>");
    html.append("</head>");
    html.append("<body onload='javascript:window.print();'>");

    html.append("<table border='0' cellpadding='2' cellspacing='0' width='100%'>");
    html.append("<tr><td align='center' colspan='11'><h3>STRATIS</h3></td></tr>");
    html.append("<tr><td align='center' colspan='11'><h3>Container Summary</h3></td></tr>");
    html.append("<tr><td colspan='11'>&nbsp;</td></tr>");

    html.append("<tr><td colspan='11'><table>");

    html.append("<tr>");
    html.append("<td class='boldBarcode' colspan='6' valign='top'>");
    html.append("Shipping Barcode:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
    html.append("<img src=\"").append(escapeHtml(urlContextPath)).append("/SlotImage?type=BARCODE&bc=");
    html.append(Util.encodeUTF8(barcodeString).toUpperCase()).append("&bt=BAR39\"/></td>");
    html.append("</tr>");
    html.append("<tr>");
    html.append("<td class='boldBarcode' align='right'>( Slot:&nbsp;&nbsp;</td>");

    html.append("<td class='boldBarcode' align='left'>").append(escapeHtml(labelValues.slot)).append("</td>");
    html.append("<td>&nbsp;</td>");
    html.append("<td class='boldBarcode'>Station:&nbsp;&nbsp;").append(escapeHtml(labelValues.station)).append("&nbsp;)</td>");
    html.append("<td>&nbsp;</td>");
    html.append("<td>&nbsp;</td>");
    html.append("</tr>");
    html.append("<tr>");
    html.append("<td class='boldBarcode' colspan='3'>TCN:&nbsp;&nbsp;").append(escapeHtml(labelValues.tcn)).append("</td>");
    html.append("<td class='boldBarcode' colspan='3' align='left'>Route To:&nbsp;&nbsp;").append(escapeHtml(labelValues.routeTo)).append("</td>");
    html.append("</tr>");
    html.append("<tr>");
    html.append("<td class='boldBarcode' colspan='3'><table><tr><td valign='top' class='boldBarcode'>SHIP TO:&nbsp;&nbsp;</td><td valign='top' class='boldBarcode'>").append(labelValues.escapedShipTo).append("</td></tr></table></td>");
    html.append("<td class='boldBarcode' colspan='3'>&nbsp;</td>");
    html.append("</tr>");

    html.append("</table></td></tr>");
    html.append("<tr><td colspan='11'>&nbsp;</td></tr>");
    html.append("<tr>");
    html.append("<td class='summaryTable'>PIN</td>");
    html.append("<td class='summaryTable'>&nbsp;</td>");
    html.append("<td class='summaryTable'>DOCUMENT #</td>");
    html.append("<td class='summaryTable'>&nbsp;</td>");
    html.append("<td class='summaryTable'>NSN</td>");
    html.append("<td class='summaryTable'>&nbsp;</td>");
    html.append("<td class='summaryTable'>UI</td>");
    html.append("<td class='summaryTable'>&nbsp;</td>");
    html.append("<td class='summaryTable'>Qty</td>");
    html.append("<td class='summaryTable'>&nbsp;</td>");
    html.append("<td class='summaryTable'>SupAdd</td>");
    html.append("</tr>");
    return html;
  }

  String normalizeValue(String in) {
    if (in == null || in.equals("null"))
      return "";
    return in;
  }

  private StringBuilder getFooterHtmlBlock() {
    StringBuilder html = new StringBuilder();
    html.append("</table>");
    html.append("</body>");
    html.append("</html>");
    return html;
  }

  StringBuilder getPackingLineItemHtmlBlock(PackingLineItemValues values) {
    StringBuilder htmlBuilder = new StringBuilder();
    htmlBuilder.append("<tr>");
    htmlBuilder.append("<td>&nbsp;&nbsp;").append(escapeHtml(values.pin)).append("</td>");
    htmlBuilder.append("<td>&nbsp;</td>");
    htmlBuilder.append("<td>").append(escapeHtml(values.documentNumber)).append("</td>");
    htmlBuilder.append("<td>&nbsp;</td>");
    htmlBuilder.append("<td>").append(escapeHtml(values.nsn)).append("</td>");
    htmlBuilder.append("<td>&nbsp;</td>");
    htmlBuilder.append("<td>").append(escapeHtml(values.ui)).append("</td>");
    htmlBuilder.append("<td>&nbsp;</td>");
    htmlBuilder.append("<td>").append(escapeHtml(values.quantity)).append("</td>");
    htmlBuilder.append("<td>&nbsp;</td>");
    htmlBuilder.append("<td>").append(escapeHtml(values.suppAdd)).append("</td>");
    htmlBuilder.append("</tr>");

    return htmlBuilder;
  }

  /**
   * Container's getter for PickingView1
   */
  public PickingViewImpl getPickingView1() {
    return (PickingViewImpl) findViewObject("PickingView1");
  }

  /**
   * Container's getter for PackingStation1
   */
  public PackingStationImpl getPackingStation1() {
    return (PackingStationImpl) findViewObject("PackingStation1");
  }

  /**
   * Container's getter for PackingIssuesView1
   */
  public PackingIssuesViewImpl getPackingIssuesView1() {
    return (PackingIssuesViewImpl) findViewObject("PackingIssuesView1");
  }

  /**
   * Container's getter for isSecure1
   */
  public IsSecureImpl getisSecure1() {
    return (IsSecureImpl) findViewObject("IsSecure1");
  }

  /**
   * Container's getter for ConsolidationView1
   */
  public ConsolidationViewImpl getConsolidationView1() {
    return (ConsolidationViewImpl) findViewObject("ConsolidationView1");
  }

  /**
   * Container's getter for bypassThresholdView1
   */
  public BypassThresholdViewImpl getbypassThresholdView1() {
    return (BypassThresholdViewImpl) findViewObject("BypassThresholdView1");
  }

  /**
   * Container's getter for pinlist1
   */
  public PinListImpl getpinlist1() {
    return (PinListImpl) findViewObject("PinList1");
  }

  /**
   * Container's getter for pinlist2
   */
  public PinListImpl getpinlist2() {
    return (PinListImpl) findViewObject("PinList2");
  }

  /**
   * Container's getter for ConsolidationViewWithBind1
   */
  public ConsolidationViewWithBindImpl getConsolidationViewWithBind1() {
    return (ConsolidationViewWithBindImpl) findViewObject("ConsolidationViewWithBind1");
  }

  /**
   * Container's getter for PickingViewWithBind1
   */
  public PickingViewWithBindImpl getPickingViewWithBind1() {
    return (PickingViewWithBindImpl) findViewObject("PickingViewWithBind1");
  }

  /**
   * Container's getter for workloadmanager1
   */
  public ApplicationModuleImpl getWorkLoadManager1() {
    return (ApplicationModuleImpl) findApplicationModule("WorkLoadManager1");
  }

  /**
   * Container's getter for PinLoadReport1
   */
  public PinLoadReportImpl getPinLoadReport1() {
    return (PinLoadReportImpl) findViewObject("PinLoadReport1");
  }

  /**
   * Container's getter for pinLoadDetail1
   */
  public PinLoadDetailImpl getpinLoadDetail1() {
    return (PinLoadDetailImpl) findViewObject("PinLoadDetail1");
  }

  /**
   * Container's getter for EquipView1
   */
  public EquipViewImpl getEquipView1() {
    return (EquipViewImpl) findViewObject("EquipView1");
  }

  /**
   * Container's getter for ShippingView1
   */
  public ShippingViewImpl getShippingView1() {
    return (ShippingViewImpl) findViewObject("ShippingView1");
  }

  /**
   * Container's getter for cartonView1
   */
  public CartonViewImpl getCartonView1() {
    return (CartonViewImpl) findViewObject("CartonView1");
  }
}

// Simple data class for packing line item values
@Builder
class PackingLineItemValues {

  public String pin;
  public String documentNumber;
  public String nsn;
  public String ui;
  public String quantity;
  public String suppAdd;
}

// Simple data class for packing label values
class PackingLabelValues {

  public String packingConsolidationId;
  public String tcn;
  public String escapedShipTo;
  public String routeTo;
  public String slot;
  public String station;

  public PackingLabelValues() {}

  public PackingLabelValues(PackingLabelValues oldValues) {
    this.packingConsolidationId = oldValues.packingConsolidationId;
    this.tcn = oldValues.tcn;
    this.escapedShipTo = oldValues.escapedShipTo;
    this.routeTo = oldValues.routeTo;
    this.slot = oldValues.slot;
    this.station = oldValues.station;
  }
}
