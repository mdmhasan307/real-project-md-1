package mil.stratis.model.services;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.common.util.Util;
import mil.stratis.model.threads.imports.ImportCourierIORThread;
import mil.stratis.model.util.AppModuleDBTransactionGetterImpl;
import mil.stratis.model.view.sysadm.SiteInfoViewImpl;
import mil.stratis.model.view.sysadm.*;
import mil.stratis.model.view.wlm.*;
import mil.usmc.mls2.stratis.core.utility.AdfLogUtility;
import oracle.jbo.Row;
import oracle.jbo.SessionData;
import oracle.jbo.ViewCriteria;
import oracle.jbo.ViewCriteriaRow;
import oracle.jbo.server.ApplicationModuleImpl;
import oracle.jbo.server.ViewLinkImpl;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@NoArgsConstructor //ADF BackingHandlers all need a no args constructor
public class SysAdminImpl extends ApplicationModuleImpl {

  protected Object currentniinlist = null;

  public void refreshErrorList() {
    try {
      ExceptionViewImpl view = getExceptionView1();
      view.executeQuery();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  public void filterExceptionData(String errorLabel) {
    String filterString = "";

    log.trace("Error label: {}", errorLabel);

    if (errorLabel != null) {
      if (errorLabel.compareTo("NONE") != 0) {
        filterString = "'%" + errorLabel + "%'";
      }
      else {
        filterString = "'%'";
      }
    }

    log.trace("Filter string: {}", filterString);

    try {

      ExceptionViewImpl view = getExceptionView1();
      view.applyViewCriteria(null);

      // Apply the filter criteria if any (NONE indicates no filter).
      if (filterString.length() > 0) {
        log.trace("Applying view criteria.");
        ViewCriteria vc = view.createViewCriteria();
        ViewCriteriaRow vcr = vc.createViewCriteriaRow();
        vcr.setUpperColumns(true);
        vcr.setAttribute("Error", "like " + filterString);
        vc.addElement(vcr);
        view.applyViewCriteria(vc);
      }
      view.executeQuery();
    }
    catch (Exception e) {
      log.error("Exception in filter processing.", e);
    }
  }

  /**
   * FUNCTION : refreshRefusalBuffer
   * DESC     : This function populates the refusal buffer table in Exception Processing
   */
  public void refreshRefusalBuffer() {
    try {
      //Create the instance of the object that is on the page
      RefusalBufferImpl view = getRefusalBuffer1();
      view.executeQuery(); //clear any rows in it

      try (PreparedStatement refPs = getDBTransaction().createPreparedStatement("select issue.scn, issue.document_number, issue.qty, niin_info.niin,sum(picking.pick_qty),issue.issue_type,issue.advice_code,issue.cc " + "from issue, niin_info,picking where issue.niin_id = niin_info.niin_id and issue.scn = picking.scn and picking.status = 'PICK REFUSED' and picking.refused_flag='N' " + "group by issue.scn, issue.document_number, issue.qty, niin_info.niin, issue.issue_type, issue.advice_code, issue.cc", 0)) {
        try (ResultSet refRs = refPs.executeQuery()) {
          while (refRs.next()) {
            Row refRow = view.createRow();

            refRow.setAttribute("SCN", refRs.getString(1));
            refRow.setAttribute("DOCUMENT_NUMBER", refRs.getString(2));
            refRow.setAttribute("QTY", refRs.getInt(3));
            refRow.setAttribute("NIIN", refRs.getString(4));
            refRow.setAttribute("CC", refRs.getString(8));
            refRow.setAttribute("QTY_REFUSED", refRs.getInt(5));
            refRow.setAttribute("ISSUE_TYPE", refRs.getObject(6));
            refRow.setAttribute("ADVICE_CODE", refRs.getObject(7));
            view.insertRow(refRow);
          }
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  /**
   * FUNCTION : refuseIssue
   * DESC     : This function deletes an entire Issue given SCN when it is refused
   */
  public boolean refuseIssue(String rowSCN) {
    try {
      if (rowSCN == null)
        return false;

      String sIssue = null;
      int consolId = 0;
      try (PreparedStatement refPs = getDBTransaction().createPreparedStatement("select packing_consolidation_id from picking where scn = ? and status <> 'PICK REFUSED'", 0)) {
        refPs.setString(1, rowSCN);
        try (ResultSet refRs = refPs.executeQuery()) {
          if (refRs.next()) {
            consolId = refRs.getInt(1);
          }
        }
      }
      WorkLoadManagerImpl workload = getWorkloadManagerService();

      try (PreparedStatement refPs = getDBTransaction().createPreparedStatement("delete from picking where scn = ?", 0)) {
        refPs.setString(1, rowSCN);
        refPs.execute();
        this.getDBTransaction().commit();
      }

      String sDocNum = null;
      String sNiin = null;
      try (PreparedStatement refPs = getDBTransaction().createPreparedStatement("select i.issue_type, i.document_number, n.niin from issue i, niin_info n where i.scn = ? and i.niin_id = n.niin_id ", 0)) {
        refPs.setString(1, rowSCN);
        try (ResultSet refRs = refPs.executeQuery()) {
          if (refRs.next()) {
            sIssue = refRs.getString(1);
            sDocNum = refRs.getString(2);
            sNiin = refRs.getString(3);
          }
        }
      }

      if (sDocNum != null && sNiin != null) {
        String sql = "delete from spool where gcssmc_xml like ? and gcssmc_xml like ? and (transaction_type like 'Z0%' or transaction_type like 'SR%')";

        try (PreparedStatement refPs = getDBTransaction().createPreparedStatement(sql, 0)) {
          refPs.setString(1, "%" + sDocNum + "%");
          refPs.setString(2, "%" + sNiin + "%");
          refPs.execute();
          this.getDBTransaction().commit();
        }
      }

      if (sIssue == null || ((!sIssue.equals("W")) && (!sIssue.equals("R")))) {
        //Do not create a Z7K Transaction for a Walkthru
        log.debug("Sending out AE1 M5.");
        this.getGCSSMCTransactions1().sendAE1GCSSMCTransaction(rowSCN);
      }
      if (consolId > 0) {
        try (PreparedStatement refPs = getDBTransaction().createPreparedStatement("delete from packing_consolidation where packing_consolidation_id = ?", 0)) {
          refPs.setInt(1, consolId);
          refPs.execute();
          this.getDBTransaction().commit();
        }
      }

      try (PreparedStatement refPs2 = getDBTransaction().createPreparedStatement("delete from issue where scn = ?", 0)) {
        refPs2.setString(1, rowSCN);
        refPs2.execute();
        this.getDBTransaction().commit();
      }

      return true;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return false;
  }

  /**
   * FUNCTION : removeRewarehouse
   * DESC     : This function deletes all pick/stow/receipt/issue rows for a given Rewarehouse (pid)
   */
  public boolean removeRewarehouse(int pid) {
    boolean retVal = true;
    try {
      String sIssue = null, rowSCN = null;
      int consolId = 0, rcn = 0;

      try (PreparedStatement refPs = getDBTransaction().createPreparedStatement("select scn from picking where pid = ?", 0)) {
        refPs.setInt(1, pid);
        try (ResultSet refRs = refPs.executeQuery()) {
          if (refRs.next()) {
            rowSCN = refRs.getString(1);
          }
        }
      }

      try (PreparedStatement refPs = getDBTransaction().createPreparedStatement("select packing_consolidation_id, pid from picking where scn = ?", 0)) {
        refPs.setString(1, rowSCN);
        try (ResultSet refRs = refPs.executeQuery()) {
          while (refRs.next()) {
            consolId = refRs.getInt(1);
            pid = refRs.getInt(2);

            if (consolId > 0 && retVal == true) {
              try (PreparedStatement refPs2 = getDBTransaction().createPreparedStatement("delete from packing_consolidation where packing_consolidation_id = ?", 0)) {
                refPs2.setInt(1, consolId);
                refPs2.execute();
              }
            }

            if (retVal == true) {
              try (PreparedStatement refPs2 = getDBTransaction().createPreparedStatement("select rcn from stow where pid = ?", 0)) {
                refPs2.setInt(1, pid);
                try (ResultSet refRs2 = refPs2.executeQuery()) {
                  if (refRs2.next()) {
                    rcn = refRs2.getInt(1);
                  }
                  else retVal = false;
                }
              }
            }

            if (retVal == true) {
              try (PreparedStatement refPs2 = getDBTransaction().createPreparedStatement("delete from stow where pid = ?", 0)) {
                refPs2.setInt(1, pid);
                refPs2.execute();
              }
            }
          }
        }
      }
      if (retVal == true) {
        try (PreparedStatement refPs = getDBTransaction().createPreparedStatement("delete from picking where scn = ?", 0)) {
          refPs.setString(1, rowSCN);
          refPs.execute();
        }
      }

      if (retVal == true) {
        try (PreparedStatement refPs2 = getDBTransaction().createPreparedStatement("delete from issue where scn = ?", 0)) {
          refPs2.setString(1, rowSCN);
          refPs2.execute();
        }
      }

      if (retVal == true) {
        try (PreparedStatement refPs2 = getDBTransaction().createPreparedStatement("delete from receipt where rcn = ?", 0)) {
          refPs2.setInt(1, rcn);
          refPs2.execute();
        }
      }

      if (retVal == true)
        this.getDBTransaction().commit();
      else
        this.getDBTransaction().rollback();
      return retVal;
    }
    catch (Exception e) {
      this.getDBTransaction().rollback();
      AdfLogUtility.logException(e);
    }
    return false;
  }

  /**
   * FUNCTION : releasePartialIssue
   * DESC     : This function takes all picks for a given issue that have not been refused
   * and releases them from their holding bins to shipping, regardless if their full
   * issue quantity has been filled.
   */
  public boolean releasePartialIssue(String rowSCN) {
    try (PreparedStatement refPs = getDBTransaction().createPreparedStatement("update packing_consolidation set partial_release = 'Y' where packing_consolidation_id in (select packing_consolidation_id from picking where status <> 'PICK REFUSED' and scn = ?)", 0)) {
      if (rowSCN == null)
        return false;
      //Make sure there is an SCN for this issue
      refPs.setString(1, rowSCN);
      refPs.execute();
      this.getDBTransaction().commit();

      return true;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return false;
  }

  /**
   * FUNCTION : checkZ0Refusal
   * DESC     : This function checks partial picks for a given SCN to see if it is a walkthru and properly creates a Z0 record.
   */
  public boolean checkZ0Refusal(String rowSCN) {
    try (PreparedStatement refPs = getDBTransaction().createPreparedStatement("select issue_type from issue where scn = ?", 0)) {
      if (rowSCN == null)
        return false;
      //Make sure there is an SCN for this issue
      refPs.setString(1, rowSCN);
      String iType = "";
      try (ResultSet refRs = refPs.executeQuery()) {
        refRs.next();
        if (refRs.getString(1) != null)
          iType = refRs.getString(1);
      }

      return iType.equals("W");
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return false;
  }

  /**
   * FUNCTION : deleteRefusedPicks
   * DESC     : This function deletes all refused picks for a given SCN
   */
  public boolean deleteRefusedPicks(String rowSCN) {
    try (PreparedStatement refPs = getDBTransaction().createPreparedStatement("delete from picking where status = 'PICK REFUSED' and scn = ?", 0)) {
      if (rowSCN == null)
        return false;
      refPs.setString(1, rowSCN);
      refPs.execute();
      this.getDBTransaction().commit();
      return true;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return false;
  }

  /**
   * FUNCTION : deleteRefusedPicks
   * DESC     : This function deletes all refused picks for a given SCN
   */
  public boolean markRefusedPicks(String rowSCN) {
    try (PreparedStatement refPs = getDBTransaction().createPreparedStatement("update picking set refused_flag='Y' where status = 'PICK REFUSED' and scn = ?", 0)) {
      if (rowSCN == null)
        return false;
      refPs.setString(1, rowSCN);
      refPs.execute();
      this.getDBTransaction().commit();
      return true;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return false;
  }

  /**
   * FUNCTION : reassignRefusedPicks
   * DESC     : This function reassigns all refused picks for a given SCN to the workload for Picking.
   */
  public int reassignRefusedPicks(String rowSCN, int qtyneeded) {
    boolean successful = false;
    try {
      if (rowSCN == null)
        return -1;
      if (qtyneeded < 1)
        return -2;
      int qtyused = 0, pid = 0;

      String packingStation = "";
      WorkLoadManagerImpl workload = getWorkloadManagerService();

      String niintext = null;
      String cc = null;
      try (PreparedStatement refPs = getDBTransaction().createPreparedStatement("select niin_info.fsc, niin_info.niin, issue.cc from niin_info, issue where niin_info.niin_id = issue.niin_id and issue.scn = ?", 0)) {
        refPs.setString(1, rowSCN);
        try (ResultSet refRs = refPs.executeQuery()) {
          if (refRs.next()) {
            niintext = refRs.getString(2);
            cc = refRs.getString(3);
          }
        }
      }

      // get the niin id
      Object niinid = null;

      // get the niin view
      NiinInfoMATSViewImpl niinview = getNiinInfoMATSView1();

      // check for the niin
      niinview.setNamedWhereClauseParam("CCSearch", cc);
      niinview.setNamedWhereClauseParam("NIINSearch", niintext);

      // get all the hits
      niinview.setRangeSize(-1);

      // run the query
      niinview.executeQuery();

      int niinlocs = niinview.getRowCount();

      if (niinlocs > 0) {
        niinid = niinview.getRowAtRangeIndex(0).getAttribute("NiinId");
      }

      if (niinid != null) {

        // just add all the locations till we get the qty fullfilled
        for (int i = 0; i < niinlocs; i++) {
          if (qtyused < qtyneeded) {
            // use this location
            Number locid = workload.getNumberFromRowObject(niinview.getRowAtRangeIndex(i).getAttribute("NiinLocId"));
            int locqty = workload.getNumberFromRowObject(niinview.getRowAtRangeIndex(i).getAttribute("Avail")).intValue();
            // make the qty an int

            if (locqty < (qtyneeded - qtyused)) {
              pid = workload.insertPicking(locid, locqty, rowSCN, "PICK READY", new AppModuleDBTransactionGetterImpl(this));
              successful = true;
              packingStation = workload.getPackingStation(pid, 1, locqty, 0);

              if (!packingStation.contains("Error"))
                qtyused += locqty;
            }
            else {
              // only take what we need
              pid = workload.insertPicking(locid, qtyneeded - qtyused, rowSCN, "PICK READY", new AppModuleDBTransactionGetterImpl(this));
              successful = true;
              packingStation = workload.getPackingStation(pid, 1, (qtyneeded - qtyused), 0);

              if (!packingStation.contains("Error"))
                qtyused += (qtyneeded - qtyused);
            }
          }
          else {
            // end this loop
            i = niinlocs;
          }
        }
        if (qtyused < qtyneeded) {
          //Refuse the rest
          workload.insertPicking(null, qtyneeded - qtyused, rowSCN, "PICK REFUSED", new AppModuleDBTransactionGetterImpl(this));
        }
      }
      else {
        workload.insertPicking(null, qtyneeded, rowSCN, "PICK REFUSED", new AppModuleDBTransactionGetterImpl(this));
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    if (successful)
      return 0;
    else
      return -1;
  }

  /**
   * FUNCTION : sumNiinOnHand
   * DESC     : This function sums up the total quantity of a particular NIIN onhand
   */
  public int sumNiinOnHand(int niinId, String cc) {
    int retVal = 0;
    try (PreparedStatement st = this.getDBTransaction().createPreparedStatement("select sum(qty) from niin_location where niin_id = ? and locked = 'N' and cc = ?", 0)) {
      st.setInt(1, niinId);
      st.setString(2, cc);
      try (ResultSet rs = st.executeQuery()) {
        if (rs.next()) {
          retVal = rs.getInt(1);
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return retVal;
  }

  /**
   * FUNCTION : sumNiinOnHand
   * DESC     : This function sums up the total quantity of a particular NIIN onhand pending pick
   */
  public int sumNiinPendingPick(int niinId, String cc) {
    int retVal = 0;
    try (PreparedStatement st = this.getDBTransaction().createPreparedStatement("select sum(picking.pick_qty) from picking, niin_location where (picking.status = 'PICKING' or picking.status like 'PICK B%' or picking.status = 'PICK READY') AND qty_picked = 0 and picking.niin_loc_id = niin_location.niin_loc_id and niin_location.niin_id = ? and niin_location.cc = ?", 0)) {
      st.setInt(1, niinId);
      st.setString(2, cc);
      try (ResultSet rs = st.executeQuery()) {
        if (rs.next()) {
          retVal = rs.getInt(1);
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return retVal;
  }

  /**
   * FUNCTION : getRefusalDetails
   * DESC     : This function gets details about a particular refusal in Exception Processing
   */
  public boolean getRefusalDetails(String rowSCN) {
    boolean retVal = false;
    try {
      if (rowSCN == null)
        return false;
      RefusalBufferImpl view = getRefusalBuffer2();
      view.executeQuery();

      int iQty = 0;
      int iqtyOnhand = 0;
      int isumPending = 0;
      int iQtyRefused = 0;
      int iQtyPicked = 0;

      try (PreparedStatement refPs2 = this.getDBTransaction().createPreparedStatement("select picking.scn, picking.suffix_code, issue.document_number, niin_info.niin, issue.qty, picking.pick_qty, picking.qty_picked, picking.status, issue.advice_code, issue.issue_type, niin_info.niin_id, issue.cc, l.location_label from (issue inner join niin_info on issue.niin_id = niin_info.niin_id inner join picking on issue.scn = picking.scn) left join (niin_location nl inner join location l on nl.location_id = l.location_id) on nl.niin_loc_id = picking.niin_loc_id where picking.scn = ?", 0)) {
        refPs2.setString(1, rowSCN);
        try (ResultSet refRs = refPs2.executeQuery()) {
          while (refRs.next()) {
            Row row = view.createRow();

            row.setAttribute("SCN", refRs.getString(1));
            row.setAttribute("SUFFIX", refRs.getInt(2));
            row.setAttribute("DOCUMENT_NUMBER", refRs.getString(3));
            row.setAttribute("NIIN", refRs.getString(4));
            iQty = refRs.getInt(5);
            if (refRs.getString(8).equals("PICK REFUSED")) {
              iQtyRefused += refRs.getInt(6);
              row.setAttribute("QTY_REFUSED", refRs.getInt(6));
              row.setAttribute("QTY_PICKED", 0);
            }
            else {
              if (refRs.getInt(7) == 0) {
                iQtyPicked += refRs.getInt(6);
                row.setAttribute("QTY_PICKED", refRs.getInt(6));
              }
              else {
                iQtyPicked += refRs.getInt(7);
                row.setAttribute("QTY_PICKED", refRs.getInt(7));
              }
              row.setAttribute("QTY_REFUSED", 0);
            }
            row.setAttribute("STATUS", refRs.getString(8));
            row.setAttribute("ADVICE_CODE", refRs.getString(9));
            row.setAttribute("ISSUE_TYPE", refRs.getObject(10));
            row.setAttribute("CC", refRs.getString(12));
            row.setAttribute("Location_Label", refRs.getString(13));
            iqtyOnhand = this.sumNiinOnHand(refRs.getInt(11), refRs.getString(12));
            isumPending = this.sumNiinPendingPick(refRs.getInt(11), refRs.getString(12));

            view.insertRowAtRangeIndex(view.getRowCount(), row);
          }
        }
      }

      //Create Summary Row
      Row row = view.createRow();

      row.setAttribute("SCN", "SUMMARY");
      row.setAttribute("QTY", iQty);
      row.setAttribute("QTY_REFUSED", iQtyRefused);
      row.setAttribute("QTY_PICKED", iQtyPicked);
      row.setAttribute("QTY_ONHAND", iqtyOnhand);
      row.setAttribute("QTY_PENDING_PICK", isumPending);

      view.insertRowAtRangeIndex(view.getRowCount(), row);

      retVal = true;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return retVal;
  }

  public TransactionsImpl getTransactionManagerService() {
    try {
      TransactionsImpl service = (TransactionsImpl) getWorkloadManagerService().getTransactions1();
      return service;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return null;
  }

  /**
   * FUNCTION : runCouriorWalkThru
   * DESC     : This function initiates a thread that imports and processes a CouriorWalkThru File
   * PARMS    : fileName - the filepath + filename of the couriorWalkThru File
   */
  public void runCourierWalkThru(String fileName) {
    try {

      log.debug("go courier");
      ImportCourierIORThread task = new ImportCourierIORThread();
      task.init(fileName, this.getDBTransaction(), this);
      Thread.currentThread().sleep(5000); // 5 sec
      log.debug("courier wake up");
    }
    catch (Exception e1) {
      log.debug(e1.toString());
    }
  }

  public String getConfigValue(String key_name) {
    String retVal = "";
    try (PreparedStatement st = this.getDBTransaction().createPreparedStatement("select key_value from stratis_config where key_name = ?", 0)) {
      st.setString(1, key_name);
      try (ResultSet rs = st.executeQuery()) {
        if (rs.next()) {
          retVal = rs.getString(1);
          if (retVal == null) {
            retVal = "";
          }
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return retVal;
  }

  public void setConfigValue(String key_name, String key_value) {
    PreparedStatement st = null;
    try {
      st = this.getDBTransaction().createPreparedStatement("update stratis_config set key_value = ? where key_name = ?", 0);
      st.setString(1, key_value);
      st.setString(2, key_name);
      st.executeUpdate();
      getDBTransaction().commit();

      st.close();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      try {
        if (st != null)
          st.close();
      }
      catch (Exception e1) {
      }
    }
  }

  public void clearUserLogin(Object UserId) {
    try {
      // open the view and get the row that this user has and clear the values
      UsersViewImpl view = getUsersView1();
      view.applyViewCriteria(null);

      ViewCriteria vc = view.createViewCriteria();
      ViewCriteriaRow vcr = vc.createViewCriteriaRow();
      vcr.setAttribute("UserId", "= " + UserId);
      vc.addElement(vcr);

      view.applyViewCriteria(vc);
      view.executeQuery();

      if (view.getRowCount() > 0) {
        // get the first one and update the login and temp key
        view.getRowAtRangeIndex(0).setAttribute("LoggedIn", 'N');
        view.getRowAtRangeIndex(0).setAttribute("TempKey", "");
        view.getRowAtRangeIndex(0).setAttribute("LoggedInHh", 'N');
        view.getRowAtRangeIndex(0).setAttribute("TempKeyHh", "");
        this.getDBTransaction().commit();
      }
      // clear the last view and requery
      view.applyViewCriteria(null);
      view.executeQuery();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }

    //* also clear the users workstations
    PreparedStatement pstmt = null;
    try {
      String sqlClear = "update equip set current_user_id=null where current_user_id=?";

      //* clear current_user_id in the equip table
      pstmt = getDBTransaction().createPreparedStatement(sqlClear, 0);
      pstmt.setObject(1, UserId);
      pstmt.executeUpdate();
      getDBTransaction().commit();
      pstmt.close();
      pstmt = null;
    }
    catch (Exception e) {
      getDBTransaction().rollback();
      try {
        if (pstmt != null)
          pstmt.close();
      }
      catch (Exception e1) {
        log.error("Error closing preparedStatement");
      }

      log.warn("Exception while clearing user's equip", e);
    }

    //* clear the users assigned tasks
    try (PreparedStatement uQ = getDBTransaction().createPreparedStatement("update picking set assign_to_user = null where assign_to_user = ?", 0)) {
      uQ.setObject(1, UserId);
      uQ.executeUpdate();
      getDBTransaction().commit();
    }
    catch (Exception e) {
      log.warn("Exception while clearing user's picking", e);
    }

    try (PreparedStatement uQ = getDBTransaction().createPreparedStatement("update stow set assign_to_user = null where assign_to_user = ?", 0)) {
      uQ.setObject(1, UserId);
      uQ.executeUpdate();
      getDBTransaction().commit();
    }
    catch (Exception e) {
      log.warn("Exception while clearing user's stow", e);
    }

    try (PreparedStatement uQ = getDBTransaction().createPreparedStatement("update inventory_item set assign_to_user = null where assign_to_user = ?", 0)) {
      uQ.setObject(1, UserId);
      uQ.executeUpdate();
      getDBTransaction().commit();
    }
    catch (Exception e) {
      log.warn("Exception while clearing user's inventory", e);
    }
  }

  /**
   * FUNCTION : updatePick
   * DESC     : This function takes a pick and updates the status to the one passed
   */
  public boolean updatePick(int errId, String status) {
    try (PreparedStatement errorPs = getDBTransaction().createPreparedStatement("select key_num from error_queue where error_queue_id = ?", 0)) {
      errorPs.setInt(1, errId);
      int pid = -1;
      try (ResultSet errRs = errorPs.executeQuery()) {
        if (errRs.next()) {
          pid = errRs.getInt(1);
        }
      }
      if (pid == -1)
        return false;
      boolean resetBypassCount = false;
      String sqlRefused = "";
      if (status.equals("PICK REFUSED")) {
        try (PreparedStatement psCheck = getDBTransaction().createPreparedStatement("SELECT scn, nvl(issue_type,' ') from issue where scn in (select scn from picking where pid = ?)", 0)) {
          psCheck.setInt(1, pid);
          try (ResultSet rsCheck = psCheck.executeQuery()) {
            if (rsCheck.next() && rsCheck.getString(2).equals("R")) {
              return this.removeRewarehouse(pid);
            }
          }
        }
        sqlRefused = ", refused_by=nvl(refused_by,?), refused_date=nvl(refused_date,sysdate) ";
      }
      else {
        resetBypassCount = true;
      }
      String sqlResetBypass = ", bypass_count=0 ";
      String sql = "update picking set status = ?, modified_by = ? ";
      sql += sqlRefused;
      if (resetBypassCount)
        sql += sqlResetBypass;

      try (PreparedStatement PsUp = getDBTransaction().createPreparedStatement(sql + "where pid = ?", 0)) {
        int i = 1;
        PsUp.setString(i++, status);
        PsUp.setInt(i++, 1);
        if (Util.isNotEmpty(sqlRefused)) {
          PsUp.setInt(i++, 1);
        }
        PsUp.setInt(i++, pid);
        PsUp.execute();
        this.getDBTransaction().commit();
      }

      return true;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return false;
  }

  /**
   * FUNCTION : updateStow
   * DESC     : This function takes a pick and resets the status to pick ready
   */
  public boolean updateStow(int errId) {
    boolean retVal = false;
    try (PreparedStatement errorPs = getDBTransaction().createPreparedStatement("select key_num from error_queue where error_queue_id = ?", 0)) {
      errorPs.setInt(1, errId);
      String sid = null;
      try (ResultSet errRs = errorPs.executeQuery()) {
        if (errRs.next()) {
          sid = errRs.getString(1);
        }
      }
      if (sid == null)
        return false;

      try (PreparedStatement PsUp = getDBTransaction().createPreparedStatement("update stow set status = 'STOW READY', bypass_count=0, modified_by = 1 where sid = ?", 0)) {
        PsUp.setString(1, sid);
        PsUp.execute();
        this.getDBTransaction().commit();
      }
      return true;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return retVal;
  }

  /**
   * FUNCTION : createStow
   * DESC     : This function takes a stow loss and resends it out to be stowed.
   */
  public String createStow(int errId, String userId) {
    String newSID = "ERROR";
    try (PreparedStatement errorPs = getDBTransaction().createPreparedStatement("select key_num from error_queue where error_queue_id = ?", 0)) {
      errorPs.setInt(1, errId);
      String sid = null;
      try (ResultSet errRs = errorPs.executeQuery()) {
        if (errRs.next()) {
          sid = errRs.getString(1);
        }
      }
      if (sid == null)
        return "ERROR";

      try (PreparedStatement PsUp = getDBTransaction().createPreparedStatement("select rcn, expiration_date, date_of_manufacture, location_id, qty_to_be_stowed, stow_qty from stow where sid = ?", 0)) {
        PsUp.setString(1, sid);
        try (ResultSet stowInfo = PsUp.executeQuery()) {
          if (stowInfo.next()) {
            int qtytobestowed, stowedqty;
            qtytobestowed = stowInfo.getInt(5);
            stowedqty = stowInfo.getInt(6);

            newSID = getWorkloadManagerService().assignSID();
            try (PreparedStatement insertStow = getDBTransaction().createPreparedStatement("insert into stow (rcn, sid, qty_to_be_stowed, created_by, created_date, status, expiration_date, date_of_manufacture, location_id) values (?,?,?,?,sysdate,?,?,?,?)", 0)) {
              insertStow.setInt(1, stowInfo.getInt(1));
              insertStow.setString(2, newSID);
              insertStow.setInt(3, qtytobestowed - stowedqty);
              insertStow.setString(4, userId);
              insertStow.setString(5, "STOW READY");
              insertStow.setDate(6, stowInfo.getDate(2));
              insertStow.setDate(7, stowInfo.getDate(3));
              insertStow.setInt(8, stowInfo.getInt(4));
              insertStow.executeUpdate();
              this.getDBTransaction().commit();
            }
          }
        }
      }

      return newSID;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return "ERROR";
  }

  /**
   * FUNCTION : deleteError
   * DESC     : This function takes an error id in the error queue and
   * removes it.
   */
  public boolean deleteError(int errId, int userId) {
    try (PreparedStatement updateErrorPs = getDBTransaction().createPreparedStatement("update error_queue set modified_by = ?, modified_date = sysdate where error_queue_id = ?", 0);
         PreparedStatement deleteErrorPs = getDBTransaction().createPreparedStatement("delete from error_queue where error_queue_id = ?", 0)
    ) {
      updateErrorPs.setInt(1, userId);
      updateErrorPs.setInt(2, errId);
      updateErrorPs.execute();
      this.getDBTransaction().commit();

      deleteErrorPs.setInt(1, errId);
      deleteErrorPs.execute();
      this.getDBTransaction().commit();
      return true;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return false;
  }

  /**
   * FUNCTION : updateExpRemark
   * DESC     : This function takes an error id in the error queue and
   * resets the expRemarkField to N for the niin location stored for that exception error
   */
  public void updateExpRemark(int errId) {
    try (PreparedStatement errorPs = getDBTransaction().createPreparedStatement("update niin_location set exp_remark = 'N' where niin_loc_id in (select key_num from error_queue where error_queue_id = ?)", 0)) {
      errorPs.setInt(1, errId);
      errorPs.execute();
      this.getDBTransaction().commit();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  /**
   * FUNCTION : updateUnderAudit
   * DESC     : This function takes an error id in the error queue and
   * resets the under_audit to N for the niin location stored for that exception error
   */
  public void updateUnderAudit(int errId) {
    try (PreparedStatement errorPs = getDBTransaction().createPreparedStatement("update niin_location set under_audit = 'N' where niin_loc_id in (select key_num from error_queue where error_queue_id = ?)", 0)) {
      errorPs.setInt(1, errId);
      errorPs.execute();
      this.getDBTransaction().commit();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  /**
   * FUNCTION : getDetailType
   * DESC     : This function takes an error id in the error queue and
   * returns the DetailType screen to show.
   */
  public HashMap getDetailType(int errId) {
    HashMap errMap = new HashMap();
    try {
      MatsExceptionViewImpl matsView = null;
      StowDelayViewImpl delayView = null;
      PickExceptionViewImpl pickView = null;
      PickExceptionViewImpl stowView = null;
      ExceptionReceiptImpl receiptView = null;
      RefGbofViewImpl gbofView = null;
      SiteInterfacesViewImpl intfView = null;
      NiinUIchangeImpl niinUIview = null;
      SerialPickViewImpl serialView = null;
      SiteRemoteConnectionsViewImpl connView = getSiteRemoteConnectionsView1();
      NiinLocationWalkThruViewImpl nlView = null;
      ViewCriteria vc = null;
      ViewCriteriaRow vcr = null;
      String niin = null;
      try (PreparedStatement errorPs = getDBTransaction().createPreparedStatement("select eid, key_num, key_type, notes from error_queue where error_queue_id = ?", 0)) {
        errorPs.setInt(1, errId);
        try (ResultSet errRs = errorPs.executeQuery()) {
          int eid = -1;
          if (errRs.next()) {
            eid = errRs.getInt(1);
            errMap.put("keynum", errRs.getObject(2));
            switch (eid) {
              case 1:
                try (PreparedStatement rPs = getDBTransaction().createPreparedStatement("select receipt.rcn, receipt.document_number, niin_info.NOMENCLATURE, niin_info.NIIN, receipt.fsc, receipt.QUANTITY_INVOICED, receipt.QUANTITY_INDUCTED, receipt.price, receipt.ui, receipt.weight, receipt.cube, niin_info.fsc  from receipt left outer join niin_info on receipt.niin_id = niin_info.niin_id where receipt.rcn = ? ORDER BY receipt.rcn", 0)) {
                  rPs.setObject(1, errRs.getObject(2));
                  try (ResultSet rRs = rPs.executeQuery()) {
                    if (rRs.next()) {
                      errMap.put("eid", eid);

                      receiptView = getExceptionReceipt1();
                      receiptView.executeQuery();
                      Row recRow = receiptView.createRow();
                      recRow.setAttribute("Rcn", rRs.getObject(1).toString());
                      recRow.setAttribute("DocumentNumber", rRs.getString(2));
                      recRow.setAttribute("Nomenclature", rRs.getObject(3));
                      recRow.setAttribute("Niin", rRs.getObject(4));
                      recRow.setAttribute("Fsc", rRs.getObject(5));
                      recRow.setAttribute("Invoiced", rRs.getInt(6));
                      recRow.setAttribute("Received", rRs.getInt(7));
                      recRow.setAttribute("Price", rRs.getDouble(8));
                      recRow.setAttribute("UI", rRs.getString(9));
                      recRow.setAttribute("Weight", rRs.getDouble(10));
                      recRow.setAttribute("Cube", rRs.getDouble(11));
                      recRow.setAttribute("NiinFSC", rRs.getObject(12));
                      receiptView.insertRow(recRow);
                    }
                    else {
                      errMap.put("eid", -1);
                    }
                  }
                }
                break;
              case 2:
                try (PreparedStatement rPs = getDBTransaction().createPreparedStatement("select receipt.rcn, receipt.document_number, niin_info.NOMENCLATURE, niin_info.NIIN, niin_info.fsc, receipt.QUANTITY_INVOICED, receipt.QUANTITY_INDUCTED, receipt.price, receipt.ui, receipt.weight, receipt.cube from receipt left outer join niin_info on receipt.niin_id = niin_info.niin_id where receipt.rcn = ? ORDER BY receipt.rcn", 0)) {
                  rPs.setObject(1, errRs.getObject(2));
                  try (ResultSet rRs = rPs.executeQuery()) {
                    if (rRs.next()) {
                      errMap.put("eid", eid);

                      receiptView = getExceptionReceipt1();
                      receiptView.executeQuery();
                      Row recRow = receiptView.createRow();
                      recRow.setAttribute("Rcn", rRs.getObject(1).toString());
                      recRow.setAttribute("DocumentNumber", rRs.getString(2));
                      recRow.setAttribute("Nomenclature", rRs.getObject(3));
                      recRow.setAttribute("Niin", rRs.getObject(4));
                      recRow.setAttribute("Fsc", rRs.getObject(5));
                      recRow.setAttribute("Invoiced", rRs.getInt(6));
                      recRow.setAttribute("Received", rRs.getInt(7));
                      recRow.setAttribute("Price", rRs.getDouble(8));
                      recRow.setAttribute("UI", rRs.getString(9));
                      recRow.setAttribute("Weight", rRs.getDouble(10));
                      recRow.setAttribute("Cube", rRs.getDouble(11));
                      receiptView.insertRow(recRow);
                    }
                    else {
                      errMap.put("eid", -1);
                    }
                  }
                }
                break;
              case 3:
                try (PreparedStatement rPs = getDBTransaction().createPreparedStatement("select receipt.rcn, receipt.document_number, niin_info.NOMENCLATURE, niin_info.NIIN, niin_info.fsc, receipt.QUANTITY_INVOICED, receipt.QUANTITY_INDUCTED, receipt.price, receipt.ui, receipt.weight, receipt.cube from receipt left outer join niin_info on receipt.niin_id = niin_info.niin_id where receipt.rcn = ? ORDER BY receipt.rcn", 0)) {
                  rPs.setObject(1, errRs.getObject(2));
                  try (ResultSet rRs = rPs.executeQuery()) {
                    if (rRs.next()) {
                      errMap.put("eid", eid);

                      receiptView = getExceptionReceipt1();
                      receiptView.executeQuery();
                      Row recRow = receiptView.createRow();
                      recRow.setAttribute("Rcn", rRs.getObject(1).toString());
                      recRow.setAttribute("DocumentNumber", rRs.getString(2));
                      recRow.setAttribute("Nomenclature", rRs.getObject(3));
                      recRow.setAttribute("Niin", rRs.getObject(4));
                      recRow.setAttribute("Fsc", rRs.getObject(5));
                      recRow.setAttribute("Invoiced", rRs.getInt(6));
                      recRow.setAttribute("Received", rRs.getInt(7));
                      recRow.setAttribute("Price", rRs.getDouble(8));
                      recRow.setAttribute("UI", rRs.getString(9));
                      recRow.setAttribute("Weight", rRs.getDouble(10));
                      recRow.setAttribute("Cube", rRs.getDouble(11));
                      receiptView.insertRow(recRow);
                    }
                    else {
                      errMap.put("eid", -1);
                    }
                  }
                }
                break;
              case 4:
                try (PreparedStatement rPs = getDBTransaction().createPreparedStatement("select receipt.rcn, receipt.document_number, niin_info.NOMENCLATURE, niin_info.NIIN, niin_info.fsc, receipt.QUANTITY_INVOICED, receipt.QUANTITY_INDUCTED, receipt.price, receipt.ui, receipt.weight, receipt.cube from receipt left outer join niin_info on receipt.niin_id = niin_info.niin_id where receipt.rcn = ? ORDER BY receipt.rcn", 0)) {
                  rPs.setObject(1, errRs.getObject(2));
                  try (ResultSet rRs = rPs.executeQuery()) {
                    if (rRs.next()) {
                      errMap.put("eid", eid);

                      receiptView = getExceptionReceipt1();
                      receiptView.executeQuery();
                      Row recRow = receiptView.createRow();
                      recRow.setAttribute("Rcn", rRs.getObject(1).toString());
                      recRow.setAttribute("DocumentNumber", rRs.getString(2));
                      recRow.setAttribute("Nomenclature", rRs.getObject(3));
                      recRow.setAttribute("Niin", rRs.getObject(4));
                      recRow.setAttribute("Fsc", rRs.getObject(5));
                      recRow.setAttribute("Invoiced", rRs.getInt(6));
                      recRow.setAttribute("Received", rRs.getInt(7));
                      recRow.setAttribute("Price", rRs.getDouble(8));
                      recRow.setAttribute("UI", rRs.getString(9));
                      recRow.setAttribute("Weight", rRs.getDouble(10));
                      recRow.setAttribute("Cube", rRs.getDouble(11));
                      receiptView.insertRow(recRow);
                    }
                    else {
                      errMap.put("eid", -1);
                    }
                  }
                }
                break;
              case 5:
                try (PreparedStatement rPs = getDBTransaction().createPreparedStatement("select receipt.rcn, receipt.document_number, niin_info.NOMENCLATURE, niin_info.NIIN, niin_info.fsc, receipt.QUANTITY_INVOICED, receipt.QUANTITY_INDUCTED, receipt.price, receipt.ui, receipt.weight, receipt.cube from receipt left outer join niin_info on receipt.niin_id = niin_info.niin_id where receipt.rcn = ? ORDER BY receipt.rcn", 0)) {
                  rPs.setObject(1, errRs.getObject(2));
                  try (ResultSet rRs = rPs.executeQuery()) {
                    if (rRs.next()) {
                      errMap.put("eid", eid);

                      receiptView = getExceptionReceipt1();
                      receiptView.executeQuery();
                      Row recRow = receiptView.createRow();
                      recRow.setAttribute("Rcn", rRs.getObject(1).toString());
                      recRow.setAttribute("DocumentNumber", rRs.getString(2));
                      recRow.setAttribute("Nomenclature", rRs.getObject(3));
                      recRow.setAttribute("Niin", rRs.getObject(4));
                      recRow.setAttribute("Fsc", rRs.getObject(5));
                      recRow.setAttribute("Invoiced", rRs.getInt(6));
                      recRow.setAttribute("Received", rRs.getInt(7));
                      recRow.setAttribute("Price", rRs.getDouble(8));
                      recRow.setAttribute("UI", rRs.getString(9));
                      recRow.setAttribute("Weight", rRs.getDouble(10));
                      recRow.setAttribute("Cube", rRs.getDouble(11));
                      receiptView.insertRow(recRow);
                    }
                    else {
                      errMap.put("eid", -1);
                    }
                  }
                }
                break;
              case 6:
                try (PreparedStatement rPs = getDBTransaction().createPreparedStatement("select receipt.rcn, receipt.document_number, niin_info.NOMENCLATURE, niin_info.NIIN, niin_info.fsc, receipt.QUANTITY_INVOICED, receipt.QUANTITY_INDUCTED, receipt.price, receipt.ui, receipt.weight, receipt.cube, niin_info.ui from receipt left outer join niin_info on receipt.niin_id = niin_info.niin_id where receipt.rcn = ? ORDER BY receipt.rcn", 0)) {
                  rPs.setObject(1, errRs.getObject(2));
                  try (ResultSet rRs = rPs.executeQuery()) {
                    if (rRs.next()) {
                      errMap.put("eid", eid);

                      receiptView = getExceptionReceipt1();
                      receiptView.executeQuery();
                      Row recRow = receiptView.createRow();
                      recRow.setAttribute("Rcn", rRs.getObject(1).toString());
                      recRow.setAttribute("DocumentNumber", rRs.getString(2));
                      recRow.setAttribute("Nomenclature", rRs.getObject(3));
                      recRow.setAttribute("Niin", rRs.getObject(4));
                      recRow.setAttribute("Fsc", rRs.getObject(5));
                      recRow.setAttribute("Invoiced", rRs.getInt(6));
                      recRow.setAttribute("Received", rRs.getInt(7));
                      recRow.setAttribute("Price", rRs.getDouble(8));
                      recRow.setAttribute("UI", rRs.getString(9));
                      recRow.setAttribute("Weight", rRs.getDouble(10));
                      recRow.setAttribute("Cube", rRs.getDouble(11));
                      recRow.setAttribute("NiinUI", rRs.getObject(12));
                      receiptView.insertRow(recRow);
                    }
                    else {
                      errMap.put("eid", -1);
                    }
                  }
                }
                break;
              case 7:
                try (PreparedStatement rPs = getDBTransaction().createPreparedStatement("select receipt.rcn, receipt.document_number, niin_info.NOMENCLATURE, niin_info.NIIN, niin_info.fsc, receipt.QUANTITY_INVOICED, receipt.QUANTITY_INDUCTED, receipt.price, receipt.ui, receipt.weight, receipt.cube, niin_info.ui from receipt left outer join niin_info on receipt.niin_id = niin_info.niin_id where receipt.rcn = ? ORDER BY receipt.rcn", 0)) {
                  rPs.setObject(1, errRs.getObject(2));
                  try (ResultSet rRs = rPs.executeQuery()) {
                    if (rRs.next()) {
                      errMap.put("eid", eid);

                      receiptView = getExceptionReceipt1();
                      receiptView.executeQuery();
                      Row recRow = receiptView.createRow();
                      recRow.setAttribute("Rcn", rRs.getObject(1).toString());
                      recRow.setAttribute("DocumentNumber", rRs.getString(2));
                      recRow.setAttribute("Nomenclature", rRs.getObject(3));
                      recRow.setAttribute("Niin", rRs.getObject(4));
                      recRow.setAttribute("Fsc", rRs.getObject(5));
                      recRow.setAttribute("Invoiced", rRs.getInt(6));
                      recRow.setAttribute("Received", rRs.getInt(7));
                      recRow.setAttribute("Price", rRs.getDouble(8));
                      recRow.setAttribute("UI", rRs.getString(9));
                      recRow.setAttribute("Weight", rRs.getDouble(10));
                      recRow.setAttribute("Cube", rRs.getDouble(11));
                      recRow.setAttribute("NiinUI", rRs.getObject(12));
                      receiptView.insertRow(recRow);
                    }
                    else {
                      errMap.put("eid", -1);
                    }
                  }
                }
                break;
              case 8:
                try (PreparedStatement rPs = getDBTransaction().createPreparedStatement("select receipt.rcn, receipt.document_number, niin_info.NOMENCLATURE, niin_info.NIIN, niin_info.fsc, receipt.QUANTITY_INVOICED, receipt.QUANTITY_INDUCTED, receipt.price, receipt.ui, receipt.weight, receipt.cube from receipt left outer join niin_info on receipt.niin_id = niin_info.niin_id where receipt.rcn = ? ORDER BY receipt.rcn", 0)) {
                  rPs.setObject(1, errRs.getObject(2));
                  try (ResultSet rRs = rPs.executeQuery()) {
                    if (rRs.next()) {
                      errMap.put("eid", eid);

                      receiptView = getExceptionReceipt1();
                      receiptView.executeQuery();
                      Row recRow = receiptView.createRow();
                      recRow.setAttribute("Rcn", rRs.getObject(1).toString());
                      recRow.setAttribute("DocumentNumber", rRs.getString(2));
                      recRow.setAttribute("Nomenclature", rRs.getObject(3));
                      recRow.setAttribute("Niin", rRs.getObject(4));
                      recRow.setAttribute("Fsc", rRs.getObject(5));
                      recRow.setAttribute("Invoiced", rRs.getInt(6));
                      recRow.setAttribute("Received", rRs.getInt(7));
                      recRow.setAttribute("Price", rRs.getDouble(8));
                      recRow.setAttribute("UI", rRs.getString(9));
                      recRow.setAttribute("Weight", rRs.getDouble(10));
                      recRow.setAttribute("Cube", rRs.getDouble(11));
                      receiptView.insertRow(recRow);
                    }
                    else {
                      errMap.put("eid", -1);
                    }
                  }
                }
                break;
              case 9:
                try (PreparedStatement rPs = getDBTransaction().createPreparedStatement("select receipt.rcn, receipt.document_number, niin_info.NOMENCLATURE, niin_info.NIIN, niin_info.fsc, receipt.QUANTITY_INVOICED, receipt.QUANTITY_INDUCTED, receipt.price, receipt.ui, receipt.weight, receipt.cube from receipt left outer join niin_info on receipt.niin_id = niin_info.niin_id where receipt.rcn = ? ORDER BY receipt.rcn", 0)) {
                  rPs.setObject(1, errRs.getObject(2));
                  try (ResultSet rRs = rPs.executeQuery()) {
                    if (rRs.next()) {
                      errMap.put("eid", eid);

                      receiptView = getExceptionReceipt1();
                      receiptView.executeQuery();
                      Row recRow = receiptView.createRow();
                      recRow.setAttribute("Rcn", rRs.getObject(1).toString());
                      recRow.setAttribute("DocumentNumber", rRs.getString(2));
                      recRow.setAttribute("Nomenclature", rRs.getObject(3));
                      recRow.setAttribute("Niin", rRs.getObject(4));
                      recRow.setAttribute("Fsc", rRs.getObject(5));
                      recRow.setAttribute("Invoiced", rRs.getInt(6));
                      recRow.setAttribute("Received", rRs.getInt(7));
                      recRow.setAttribute("Price", rRs.getDouble(8));
                      recRow.setAttribute("UI", rRs.getString(9));
                      recRow.setAttribute("Weight", rRs.getDouble(10));
                      recRow.setAttribute("Cube", rRs.getDouble(11));
                      receiptView.insertRow(recRow);
                    }
                    else {
                      errMap.put("eid", -1);
                    }
                  }
                }
                break;
              //case 10-12 are Pick Refusals and dont get put in getDetails
              case 13: //Pick Bypass Exception
                try (PreparedStatement pickPs = getDBTransaction().createPreparedStatement("select niin_info.niin, location.location_label, picking.scn, picking.suffix_code, picking.pick_qty, picking.pid, issue.issue_type from niin_info, picking, niin_location, location, issue where picking.pid = ? and picking.niin_loc_id = niin_location.niin_loc_id and niin_location.location_id = location.location_id and niin_location.niin_id = niin_info.niin_id and issue.scn = picking.scn", 0)) {
                  pickPs.setObject(1, errRs.getObject(2));
                  try (ResultSet pickRs = pickPs.executeQuery()) {
                    if (pickRs.next()) {
                      errMap.put("eid", eid);
                      errMap.put("niin", pickRs.getString(1));
                      errMap.put("location", pickRs.getString(2));
                      errMap.put("scn", pickRs.getString(3));
                      errMap.put("suffix", pickRs.getInt(4));
                      errMap.put("pickqty", pickRs.getInt(5));
                      errMap.put("pid", pickRs.getInt(6));
                      errMap.put("issue_type", pickRs.getString(7));

                      pickView = getpickExceptionView1();
                      pickView.executeQuery();
                      Row pickRow = pickView.createRow();
                      pickRow.setAttribute("Scn", pickRs.getString(3));
                      pickRow.setAttribute("Niin", pickRs.getString(1));
                      pickRow.setAttribute("Suffix", pickRs.getInt(4));
                      pickRow.setAttribute("PickQty", pickRs.getInt(5));
                      pickRow.setAttribute("Location", pickRs.getString(2));
                      pickRow.setAttribute("issueType", (pickRs.getObject(7) == null ? "" : pickRs.getString(7)));
                      pickView.insertRow(pickRow);
                    }
                    else {
                      errMap.put("eid", -1);
                    }
                  }
                }
                break;
              case 14: //Pick Bypass Exception
                try (PreparedStatement pickPs2 = getDBTransaction().createPreparedStatement("select niin_info.niin, location.location_label, picking.scn, picking.suffix_code, picking.pick_qty, picking.pid, issue.issue_type from niin_info, picking, niin_location, location, issue where picking.pid = ? and picking.niin_loc_id = niin_location.niin_loc_id and niin_location.location_id = location.location_id and niin_location.niin_id = niin_info.niin_id and issue.scn = picking.scn", 0)) {
                  pickPs2.setObject(1, errRs.getObject(2));
                  try (ResultSet pickRs2 = pickPs2.executeQuery()) {
                    if (pickRs2.next()) {
                      errMap.put("eid", eid);
                      errMap.put("niin", pickRs2.getString(1));
                      errMap.put("location", pickRs2.getString(2));
                      errMap.put("scn", pickRs2.getString(3));
                      errMap.put("suffix", pickRs2.getInt(4));
                      errMap.put("pickqty", pickRs2.getInt(5));
                      errMap.put("pid", pickRs2.getInt(6));
                      errMap.put("issue_type", pickRs2.getString(7));

                      pickView = getpickExceptionView1();
                      pickView.executeQuery();
                      Row pickRow = pickView.createRow();
                      pickRow.setAttribute("Scn", pickRs2.getString(3));
                      pickRow.setAttribute("Niin", pickRs2.getString(1));
                      pickRow.setAttribute("Suffix", pickRs2.getInt(4));
                      pickRow.setAttribute("PickQty", pickRs2.getInt(5));
                      pickRow.setAttribute("Location", pickRs2.getString(2));
                      pickRow.setAttribute("issueType", (pickRs2.getObject(7) == null ? "" : pickRs2.getString(7)));
                      pickView.insertRow(pickRow);
                    }
                    else {
                      errMap.put("eid", -1);
                    }
                  }
                }
                break;
              case 15: //Pick Bypass Exception
                try (PreparedStatement pickPs3 = getDBTransaction().createPreparedStatement("select niin_info.niin, location.location_label, picking.scn, picking.suffix_code, picking.pick_qty, picking.pid, issue.issue_type from niin_info, picking, niin_location, location, issue where picking.pid = ? and picking.niin_loc_id = niin_location.niin_loc_id and niin_location.location_id = location.location_id and niin_location.niin_id = niin_info.niin_id and issue.scn = picking.scn", 0)) {
                  pickPs3.setObject(1, errRs.getObject(2));
                  try (ResultSet pickRs3 = pickPs3.executeQuery()) {
                    if (pickRs3.next()) {
                      errMap.put("eid", eid);
                      errMap.put("niin", pickRs3.getString(1));
                      errMap.put("location", pickRs3.getString(2));
                      errMap.put("scn", pickRs3.getString(3));
                      errMap.put("suffix", pickRs3.getInt(4));
                      errMap.put("pickqty", pickRs3.getInt(5));
                      errMap.put("pid", pickRs3.getInt(6));
                      errMap.put("issue_type", pickRs3.getString(7));

                      pickView = getpickExceptionView1();
                      pickView.executeQuery();
                      Row pickRow = pickView.createRow();
                      pickRow.setAttribute("Scn", pickRs3.getString(3));
                      pickRow.setAttribute("Niin", pickRs3.getString(1));
                      pickRow.setAttribute("Suffix", pickRs3.getInt(4));
                      pickRow.setAttribute("PickQty", pickRs3.getInt(5));
                      pickRow.setAttribute("Location", pickRs3.getString(2));
                      pickRow.setAttribute("issueType", (pickRs3.getObject(7) == null ? "" : pickRs3.getString(7)));
                      pickView.insertRow(pickRow);
                    }
                    else {
                      errMap.put("eid", -1);
                    }
                  }
                }
                break;
              case 16: //Pick Bypass Exception
                try (PreparedStatement pickPs4 = getDBTransaction().createPreparedStatement("select niin_info.niin, location.location_label, picking.scn, picking.suffix_code, picking.pick_qty, picking.pid, issue.issue_type from niin_info, picking, niin_location, location, issue where picking.pid = ? and picking.niin_loc_id = niin_location.niin_loc_id and niin_location.location_id = location.location_id and niin_location.niin_id = niin_info.niin_id and issue.scn = picking.scn", 0)) {
                  pickPs4.setObject(1, errRs.getObject(2));
                  try (ResultSet pickRs4 = pickPs4.executeQuery()) {
                    if (pickRs4.next()) {
                      errMap.put("eid", eid);
                      errMap.put("niin", pickRs4.getString(1));
                      errMap.put("location", pickRs4.getString(2));
                      errMap.put("scn", pickRs4.getString(3));
                      errMap.put("suffix", pickRs4.getInt(4));
                      errMap.put("pickqty", pickRs4.getInt(5));
                      errMap.put("pid", pickRs4.getInt(6));
                      errMap.put("issue_type", pickRs4.getString(7));

                      pickView = getpickExceptionView1();
                      pickView.executeQuery();
                      Row pickRow = pickView.createRow();
                      pickRow.setAttribute("Scn", pickRs4.getString(3));
                      pickRow.setAttribute("Niin", pickRs4.getString(1));
                      pickRow.setAttribute("Suffix", pickRs4.getInt(4));
                      pickRow.setAttribute("PickQty", pickRs4.getInt(5));
                      pickRow.setAttribute("Location", pickRs4.getString(2));
                      pickRow.setAttribute("issueType", (pickRs4.getObject(7) == null ? "" : pickRs4.getString(7)));
                      pickView.insertRow(pickRow);
                    }
                    else {
                      errMap.put("eid", -1);
                    }
                  }
                }
                break;
              case 17:
                try (PreparedStatement stowPs = getDBTransaction().createPreparedStatement("select stow.sid, location.location_label, stow.stow_qty from stow, location where sid = ? and location.location_id = stow.location_id", 0)) {
                  stowPs.setString(1, errRs.getString(2));
                  try (ResultSet stowRs = stowPs.executeQuery()) {
                    if (stowRs.next()) {
                      errMap.put("eid", eid);
                      try (PreparedStatement ns = getDBTransaction().createPreparedStatement("select niin_info.niin from niin_info, receipt, stow where stow.sid = ? and stow.rcn = receipt.rcn and receipt.niin_id = niin_info.niin_id", 0)) {
                        ns.setString(1, errRs.getString(2));
                        try (ResultSet rsns = ns.executeQuery()) {
                          if (rsns.next()) {
                            if (rsns.getObject(1) != null) niin = rsns.getString(1);
                          }
                        }
                      }

                      stowView = getPickExceptionView2();
                      stowView.executeQuery();
                      Row stowRow = stowView.createRow();
                      stowRow.setAttribute("Scn", stowRs.getString(1));
                      stowRow.setAttribute("Location", stowRs.getString(2));
                      stowRow.setAttribute("PickQty", stowRs.getInt(3));
                      if (niin == null) niin = "Not Avail";
                      stowRow.setAttribute("Niin", niin);
                      stowView.insertRow(stowRow);
                    }
                    else {
                      errMap.put("eid", -1);
                    }
                  }
                }
                break;
              case 18:
                try (PreparedStatement stowPs2 = getDBTransaction().createPreparedStatement("select stow.sid, location.location_label, stow.stow_qty from stow, location where sid = ? and location.location_id = stow.location_id", 0)) {
                  stowPs2.setString(1, errRs.getString(2));
                  try (ResultSet stowRs2 = stowPs2.executeQuery()) {
                    if (stowRs2.next()) {
                      errMap.put("eid", eid);
                      try (PreparedStatement ns2 = getDBTransaction().createPreparedStatement("select niin_info.niin from niin_info, receipt, stow where stow.sid = ? and stow.rcn = receipt.rcn and receipt.niin_id = niin_info.niin_id", 0)) {
                        ns2.setString(1, errRs.getString(2));
                        try (ResultSet rsns2 = ns2.executeQuery()) {
                          if (rsns2.next()) {
                            if (rsns2.getObject(1) != null) niin = rsns2.getString(1);
                          }
                        }
                      }

                      stowView = getPickExceptionView2();
                      stowView.executeQuery();
                      Row stowRow = stowView.createRow();
                      stowRow.setAttribute("Scn", stowRs2.getString(1));
                      stowRow.setAttribute("Location", stowRs2.getString(2));
                      stowRow.setAttribute("PickQty", stowRs2.getInt(3));
                      if (niin == null) niin = "Not Avail";
                      stowRow.setAttribute("Niin", niin);
                      stowView.insertRow(stowRow);
                    }
                    else {
                      errMap.put("eid", -1);
                    }
                  }
                }
                break;
              //case 19 is no longer used (Stow Refusal)
              //case 20 is no longer used (Inventory 1)
              //case 21 is no longer used (Inventory 2)
              //case 22 is no longer used (Stow Refusal)
              case 23:
                try (PreparedStatement stowPs3 = getDBTransaction().createPreparedStatement("select stow.sid, location.location_label, stow.stow_qty from stow, location where sid = ? and location.location_id = stow.location_id", 0)) {
                  stowPs3.setString(1, errRs.getString(2));
                  try (ResultSet stowRs3 = stowPs3.executeQuery()) {
                    if (stowRs3.next()) {
                      errMap.put("eid", eid);
                      try (PreparedStatement ns3 = getDBTransaction().createPreparedStatement("select niin_info.niin from niin_info, receipt, stow where stow.sid = ? and stow.rcn = receipt.rcn and receipt.niin_id = niin_info.niin_id", 0)) {
                        ns3.setString(1, errRs.getString(2));
                        try (ResultSet rsns3 = ns3.executeQuery()) {
                          if (rsns3.next()) {
                            if (rsns3.getObject(1) != null) niin = rsns3.getString(1);
                          }
                        }
                      }

                      stowView = getPickExceptionView2();
                      stowView.executeQuery();
                      Row stowRow = stowView.createRow();
                      stowRow.setAttribute("Scn", stowRs3.getString(1));
                      stowRow.setAttribute("Location", stowRs3.getString(2));
                      stowRow.setAttribute("PickQty", stowRs3.getInt(3));
                      if (niin == null) niin = "Not Avail";
                      stowRow.setAttribute("Niin", niin);
                      stowView.insertRow(stowRow);
                    }
                    else {
                      errMap.put("eid", -1);
                    }
                  }
                }
                break;
              //case 24 is no longer used (Stow Refusal)
              case 25:
                errMap.put("eid", eid);
                break;
              case 26:
                errMap.put("eid", eid);
                gbofView = getRefGbofView1();

                gbofView.applyViewCriteria(null);
                gbofView.executeQuery();

                vc = gbofView.createViewCriteria();
                vcr = vc.createViewCriteriaRow();

                vcr.setAttribute("RefGbofId", " = " + errRs.getObject(2));
                vc.addElement(vcr);

                gbofView.applyViewCriteria(vc);
                gbofView.setRangeSize(-1);
                gbofView.executeQuery();
                break;
              case 27:
                errMap.put("eid", eid);
                gbofView = getRefGbofView1();

                gbofView.applyViewCriteria(null);
                gbofView.executeQuery();

                vc = gbofView.createViewCriteria();
                vcr = vc.createViewCriteriaRow();

                vcr.setAttribute("RefGbofId", " = " + errRs.getObject(2));
                vc.addElement(vcr);

                gbofView.applyViewCriteria(vc);
                gbofView.setRangeSize(-1);
                gbofView.executeQuery();
                break;
              case 28:
                errMap.put("eid", eid);
                gbofView = getRefGbofView1();

                gbofView.applyViewCriteria(null);
                gbofView.executeQuery();

                vc = gbofView.createViewCriteria();
                vcr = vc.createViewCriteriaRow();

                vcr.setAttribute("RefGbofId", " = " + errRs.getObject(2));
                vc.addElement(vcr);

                gbofView.applyViewCriteria(vc);
                gbofView.setRangeSize(-1);
                gbofView.executeQuery();
                break;
              //case 29 no longer used
              case 30:
                errMap.put("eid", eid);
                connView = getSiteRemoteConnectionsView1();

                connView.applyViewCriteria(null);
                connView.executeQuery();

                vc = connView.createViewCriteria();
                vcr = vc.createViewCriteriaRow();

                vcr.setAttribute("HostName", " = " + errRs.getObject(2));
                vc.addElement(vcr);

                connView.applyViewCriteria(vc);
                connView.setRangeSize(-1);
                connView.executeQuery();
                break;
              case 31:
                errMap.put("eid", eid);
                connView = getSiteRemoteConnectionsView1();

                connView.applyViewCriteria(null);
                connView.executeQuery();

                vc = connView.createViewCriteria();
                vcr = vc.createViewCriteriaRow();

                vcr.setAttribute("HostName", " = " + errRs.getObject(2));
                vc.addElement(vcr);

                connView.applyViewCriteria(vc);
                connView.setRangeSize(-1);
                connView.executeQuery();
                break;
              case 32:
                errMap.put("eid", eid);

                gbofView = getRefGbofView1();
                gbofView.applyViewCriteria(null);
                gbofView.executeQuery();

                vc = gbofView.createViewCriteria();
                vcr = vc.createViewCriteriaRow();

                vcr.setAttribute("InterfaceName", " = 'DASF'");
                vc.addElement(vcr);

                gbofView.applyViewCriteria(vc);
                gbofView.setRangeSize(-1);
                gbofView.executeQuery();
                break;
              //33 no longer used
              case 34:
                errMap.put("eid", eid);

                gbofView = getRefGbofView1();
                gbofView.applyViewCriteria(null);
                gbofView.executeQuery();

                vc = gbofView.createViewCriteria();
                vcr = vc.createViewCriteriaRow();

                vcr.setAttribute("InterfaceName", " = 'DASF'");
                vc.addElement(vcr);

                gbofView.applyViewCriteria(vc);
                gbofView.setRangeSize(-1);
                gbofView.executeQuery();
                break;
              //35 no longer used
              case 36:
                errMap.put("eid", eid);

                gbofView = getRefGbofView1();
                gbofView.applyViewCriteria(null);
                gbofView.executeQuery();

                vc = gbofView.createViewCriteria();
                vcr = vc.createViewCriteriaRow();

                vcr.setAttribute("InterfaceName", " = 'MHIF'");
                vc.addElement(vcr);

                gbofView.applyViewCriteria(vc);
                gbofView.setRangeSize(-1);
                gbofView.executeQuery();
                break;
              //37 no longer used
              case 38:
                errMap.put("eid", eid);

                gbofView = getRefGbofView1();
                gbofView.applyViewCriteria(null);
                gbofView.executeQuery();

                vc = gbofView.createViewCriteria();
                vcr = vc.createViewCriteriaRow();

                vcr.setAttribute("InterfaceName", " = 'GBOF'");
                vc.addElement(vcr);

                gbofView.applyViewCriteria(vc);
                gbofView.setRangeSize(-1);
                gbofView.executeQuery();
                break;
              //39 no longer used
              case 40:
                errMap.put("eid", eid);

                gbofView = getRefGbofView1();
                gbofView.applyViewCriteria(null);
                gbofView.executeQuery();

                vc = gbofView.createViewCriteria();
                vcr = vc.createViewCriteriaRow();

                vcr.setAttribute("InterfaceName", " = 'MATS'");
                vc.addElement(vcr);

                gbofView.applyViewCriteria(vc);
                gbofView.setRangeSize(-1);
                gbofView.executeQuery();
                break;
              //41 no longer used
              case 42:
                try (PreparedStatement preparedStatement = getDBTransaction().createPreparedStatement("select receipt.rcn, receipt.document_number, niin_info.NOMENCLATURE, niin_info.NIIN, niin_info.fsc, receipt.QUANTITY_INVOICED, receipt.QUANTITY_INDUCTED, receipt.price, receipt.ui, receipt.weight, receipt.cube from receipt left outer join niin_info on receipt.niin_id = niin_info.niin_id where receipt.rcn = ? ORDER BY receipt.rcn", 0)) {
                  preparedStatement.setObject(1, errRs.getObject(2));
                  try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                      errMap.put("eid", eid);

                      receiptView = getExceptionReceipt1();
                      receiptView.executeQuery();
                      Row recRow = receiptView.createRow();
                      recRow.setAttribute("Rcn", resultSet.getObject(1).toString());
                      recRow.setAttribute("DocumentNumber", resultSet.getString(2));
                      recRow.setAttribute("Nomenclature", resultSet.getObject(3));
                      recRow.setAttribute("Niin", resultSet.getObject(4));
                      recRow.setAttribute("Fsc", resultSet.getObject(5));
                      recRow.setAttribute("Invoiced", resultSet.getInt(6));
                      recRow.setAttribute("Received", resultSet.getInt(7));
                      recRow.setAttribute("Price", resultSet.getDouble(8));
                      recRow.setAttribute("UI", resultSet.getString(9));
                      recRow.setAttribute("Weight", resultSet.getDouble(10));
                      recRow.setAttribute("Cube", resultSet.getDouble(11));
                      receiptView.insertRow(recRow);
                    }
                    else {
                      errMap.put("eid", -1);
                    }
                  }
                }
                break;
              case 43:
                try (PreparedStatement preparedStatement = getDBTransaction().createPreparedStatement("select receipt.rcn, receipt.document_number, niin_info.NOMENCLATURE, niin_info.NIIN, niin_info.fsc, receipt.QUANTITY_INVOICED, receipt.QUANTITY_INDUCTED, receipt.price, receipt.ui, receipt.weight, receipt.cube from receipt left outer join niin_info on receipt.niin_id = niin_info.niin_id where receipt.rcn = ? ORDER BY receipt.rcn", 0)) {
                  preparedStatement.setObject(1, errRs.getObject(2));
                  try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                      errMap.put("eid", eid);

                      receiptView = getExceptionReceipt1();
                      receiptView.executeQuery();
                      Row recRow = receiptView.createRow();
                      recRow.setAttribute("Rcn", resultSet.getObject(1).toString());
                      recRow.setAttribute("DocumentNumber", resultSet.getString(2));
                      recRow.setAttribute("Nomenclature", resultSet.getObject(3));
                      recRow.setAttribute("Niin", resultSet.getObject(4));
                      recRow.setAttribute("Fsc", resultSet.getObject(5));
                      recRow.setAttribute("Invoiced", resultSet.getInt(6));
                      recRow.setAttribute("Received", resultSet.getInt(7));
                      recRow.setAttribute("Price", resultSet.getDouble(8));
                      recRow.setAttribute("UI", resultSet.getString(9));
                      recRow.setAttribute("Weight", resultSet.getDouble(10));
                      recRow.setAttribute("Cube", resultSet.getDouble(11));
                      receiptView.insertRow(recRow);
                    }
                    else {
                      errMap.put("eid", -1);
                    }
                  }
                }
                break;
              case 44: //NIIN Expiration Exception
                try (PreparedStatement niinPs = getDBTransaction().createPreparedStatement("select niin_info.niin, location.location_label, cc, qty, to_char(expiration_date, 'YYYY-MM-DD') from niin_info, niin_location, location where niin_location.niin_loc_id = ? and niin_location.niin_id = niin_info.niin_id and niin_location.location_id = location.location_id", 0)) {
                  niinPs.setObject(1, errRs.getObject(2));
                  try (ResultSet niinRs = niinPs.executeQuery()) {
                    if (niinRs.next()) {
                      errMap.put("niin", niinRs.getString(1));
                      errMap.put("location", niinRs.getString(2));
                      errMap.put("eid", eid);
                      pickView = getExpirationView();
                      pickView.executeQuery();
                      Row pickRow = pickView.createRow();
                      pickRow.setAttribute("Niin", niinRs.getString(1));
                      pickRow.setAttribute("Location", niinRs.getString(2));
                      pickRow.setAttribute("Cc", niinRs.getString(3));
                      pickRow.setAttribute("PickQty", niinRs.getString(4));
                      pickRow.setAttribute("ExpirationDate", niinRs.getString(5));
                      pickView.insertRow(pickRow);
                    }
                    else {
                      errMap.put("eid", -1);
                    }
                  }
                }
                break;
              case 45:
                errMap.put("eid", eid);
                break;
              case 46:
                try (PreparedStatement Psloc = getDBTransaction().createPreparedStatement("select niin_info.niin, location.location_label, cc, qty, to_char(expiration_date, 'YYYY-MM-DD') from niin_info, niin_location, location where niin_location.niin_loc_id = ? and niin_location.niin_id = niin_info.niin_id and niin_location.location_id = location.location_id", 0)) {
                  Psloc.setObject(1, errRs.getObject(2));
                  try (ResultSet rsloc = Psloc.executeQuery()) {
                    if (rsloc.next()) {
                      errMap.put("location", rsloc.getString(2));
                      errMap.put("eid", eid);
                      pickView = getExpirationView();
                      pickView.executeQuery();
                      Row pickRow = pickView.createRow();
                      pickRow.setAttribute("Niin", rsloc.getString(1));
                      pickRow.setAttribute("Location", rsloc.getString(2));
                      pickRow.setAttribute("Cc", rsloc.getString(3));
                      pickRow.setAttribute("PickQty", rsloc.getString(4));
                      pickRow.setAttribute("ExpirationDate", rsloc.getString(5));
                      pickView.insertRow(pickRow);
                    }
                    else {
                      errMap.put("eid", -1);
                    }
                  }
                }
                break;
              case 47: //STOW LOSS Exception
                try (PreparedStatement psStow = getDBTransaction().createPreparedStatement("select rcn, qty_to_be_stowed, stow_qty from stow where sid = ?", 0)) {
                  psStow.setObject(1, errRs.getObject(2));
                  try (ResultSet rsStow = psStow.executeQuery()) {
                    if (rsStow.next()) {
                      try (PreparedStatement preparedStatement = getDBTransaction().createPreparedStatement("select receipt.rcn, receipt.document_number, niin_info.NOMENCLATURE, niin_info.NIIN, niin_info.fsc, receipt.QUANTITY_INVOICED, receipt.QUANTITY_INDUCTED, receipt.price, receipt.ui, receipt.weight, receipt.cube from receipt left outer join niin_info on receipt.niin_id = niin_info.niin_id where receipt.rcn = ? ORDER BY receipt.rcn", 0)) {
                        preparedStatement.setObject(1, rsStow.getObject(1));
                        try (ResultSet resultSet = preparedStatement.executeQuery()) {
                          if (resultSet.next()) {
                            errMap.put("eid", eid);

                            receiptView = getExceptionReceipt1();
                            receiptView.executeQuery();
                            Row recRow = receiptView.createRow();
                            recRow.setAttribute("Rcn", resultSet.getObject(1).toString());
                            recRow.setAttribute("DocumentNumber", resultSet.getString(2));
                            recRow.setAttribute("Nomenclature", resultSet.getObject(3));
                            recRow.setAttribute("Niin", resultSet.getObject(4));
                            recRow.setAttribute("Fsc", resultSet.getObject(5));
                            recRow.setAttribute("Invoiced", resultSet.getInt(6));
                            recRow.setAttribute("Received", resultSet.getInt(7));
                            recRow.setAttribute("Price", resultSet.getDouble(8));
                            recRow.setAttribute("UI", resultSet.getString(9));
                            recRow.setAttribute("Weight", resultSet.getDouble(10));
                            recRow.setAttribute("Cube", resultSet.getDouble(11));
                            recRow.setAttribute("Sid", errRs.getObject(2).toString());
                            recRow.setAttribute("Qty_To_Stow", rsStow.getInt(2));
                            recRow.setAttribute("Qty_Stowed", rsStow.getInt(3));
                            receiptView.insertRow(recRow);
                          }
                          else {
                            errMap.put("eid", -1);
                          }
                        }
                      }
                    }
                    else {
                      errMap.put("eid", -1);
                    }
                  }
                }
                break;
              case 48: //Pick Bypass Exception
                try (PreparedStatement pickPs5 = getDBTransaction().createPreparedStatement("select niin_info.niin, location.location_label, picking.scn, picking.suffix_code, picking.pick_qty, picking.pid, issue.issue_type from niin_info, picking, niin_location, location, issue where picking.pid = ? and picking.niin_loc_id = niin_location.niin_loc_id and niin_location.location_id = location.location_id and niin_location.niin_id = niin_info.niin_id and issue.scn = picking.scn", 0)) {
                  pickPs5.setObject(1, errRs.getObject(2));
                  try (ResultSet pickRs5 = pickPs5.executeQuery()) {
                    if (pickRs5.next()) {
                      errMap.put("eid", eid);
                      errMap.put("niin", pickRs5.getString(1));
                      errMap.put("location", pickRs5.getString(2));
                      errMap.put("scn", pickRs5.getString(3));
                      errMap.put("suffix", pickRs5.getInt(4));
                      errMap.put("pickqty", pickRs5.getInt(5));
                      errMap.put("pid", pickRs5.getInt(6));
                      errMap.put("issue_type", pickRs5.getString(7));

                      pickView = getpickExceptionView1();
                      pickView.executeQuery();
                      Row pickRow = pickView.createRow();
                      pickRow.setAttribute("Scn", pickRs5.getString(3));
                      pickRow.setAttribute("Niin", pickRs5.getString(1));
                      pickRow.setAttribute("Suffix", pickRs5.getInt(4));
                      pickRow.setAttribute("PickQty", pickRs5.getInt(5));
                      pickRow.setAttribute("Location", pickRs5.getString(2));
                      pickRow.setAttribute("issueType", (pickRs5.getObject(7) == null ? "" : pickRs5.getString(7)));
                      pickView.insertRow(pickRow);
                    }
                    else {
                      errMap.put("eid", -1);
                    }
                  }
                }
                break;
              case 49:
                try (PreparedStatement stowPs4 = getDBTransaction().createPreparedStatement("select stow.sid, location.location_label, stow.stow_qty from stow, location where sid = ? and location.location_id = stow.location_id", 0)) {
                  stowPs4.setString(1, errRs.getString(2));
                  try (ResultSet stowRs4 = stowPs4.executeQuery()) {
                    if (stowRs4.next()) {
                      errMap.put("eid", eid);
                      try (PreparedStatement ns4 = getDBTransaction().createPreparedStatement("select niin_info.niin from niin_info, receipt, stow where stow.sid = ? and stow.rcn = receipt.rcn and receipt.niin_id = niin_info.niin_id", 0)) {
                        ns4.setString(1, errRs.getString(2));
                        try (ResultSet rsns4 = ns4.executeQuery()) {
                          if (rsns4.next()) {
                            if (rsns4.getObject(1) != null) niin = rsns4.getString(1);
                          }
                        }
                      }

                      stowView = getPickExceptionView2();
                      stowView.executeQuery();
                      Row stowRow = stowView.createRow();
                      stowRow.setAttribute("Scn", stowRs4.getString(1));
                      stowRow.setAttribute("Location", stowRs4.getString(2));
                      stowRow.setAttribute("PickQty", stowRs4.getInt(3));
                      if (niin == null) niin = "Not Avail";
                      stowRow.setAttribute("Niin", niin);
                      stowView.insertRow(stowRow);
                    }
                    else {
                      errMap.put("eid", -1);
                    }
                  }
                }
                break;
              case 51: //NIIN Expiration Exception
                try (PreparedStatement niinPs2 = getDBTransaction().createPreparedStatement("select niin_info.niin, location.location_label from niin_info, niin_location, location where niin_location.niin_loc_id = ? and niin_location.niin_id = niin_info.niin_id and niin_location.location_id = location.location_id", 0)) {
                  niinPs2.setObject(1, errRs.getObject(2));
                  try (ResultSet niinRs2 = niinPs2.executeQuery()) {
                    if (niinRs2.next()) {
                      errMap.put("niin", niinRs2.getString(1));
                      errMap.put("location", niinRs2.getString(2));
                      errMap.put("eid", eid);
                    }
                    else {
                      errMap.put("eid", -1);
                    }
                  }
                }
                break;
              case 52:
                errMap.put("eid", eid);
                break;
              case 53:
                errMap.put("eid", eid);
                break;
              case 54:
                errMap.put("eid", eid);
                break;
              case 55:
                errMap.put("eid", eid);
                matsView = getMatsExceptionView1();

                matsView.applyViewCriteria(null);
                matsView.executeQuery();

                vc = matsView.createViewCriteria();
                vcr = vc.createViewCriteriaRow();

                vcr.setAttribute("RefMatsId", " = " + errRs.getObject(2));
                vc.addElement(vcr);

                matsView.applyViewCriteria(vc);
                matsView.setRangeSize(-1);
                matsView.executeQuery();

                break;
              case 56:
                errMap.put("eid", eid);
                niinUIview = getNiinUIchange1();
                niinUIview.setRangeSize(-1);
                niinUIview.setNamedWhereClauseParam("iLoc", errRs.getObject(2));
                niinUIview.executeQuery();

                break;
              case 57:
                errMap.put("eid", eid);
                niinUIview = getNiinUIchange1();
                niinUIview.setRangeSize(-1);
                niinUIview.setNamedWhereClauseParam("iLoc", errRs.getObject(2));
                niinUIview.executeQuery();

                break;
              case 58:
                errMap.put("eid", eid);
                //Serial Number Table - Pick Location Mismatch Exception
                try (PreparedStatement selectStatement = getDBTransaction().createPreparedStatement("SELECT pick_serial_lot_num.pid, location.location_label FROM pick_serial_lot_num, location where pick_serial_lot_num.serial_lot_num_track_id = ? AND pick_serial_lot_num.location_id = location.location_id UNION SELECT pick_serial_lot_num_hist.pid, location.location_label from pick_serial_lot_num_hist, location WHERE serial_lot_num_track_id = ? AND pick_serial_lot_num_hist.location_id = location.location_Id", 0)) {
                  selectStatement.setObject(1, errRs.getObject(2));
                  selectStatement.setObject(2, errRs.getObject(2));
                  try (ResultSet selectResultSet = selectStatement.executeQuery()) {
                    if (selectResultSet.next()) {
                      //Initialize the view object we are using on the Exception Page and create the single row.
                      serialView = getserialPickView1();
                      serialView.executeQuery();
                      Row recRow = serialView.createRow();

                      //First grab the pick row involved and the location it was actually picked from
                      int pid = selectResultSet.getInt(1);
                      recRow.setAttribute("PICK_LOCATION", selectResultSet.getObject(2));

                      try (PreparedStatement preparedStatement = getDBTransaction().createPreparedStatement("SELECT serial_lot_num_track.serial_number, serial_lot_num_track.lot_con_num, location.location_label, niin_info.niin FROM serial_lot_num_track, location, niin_info where serial_lot_num_track_id = ? AND location.location_id = serial_lot_num_track.location_id AND niin_info.niin_id = serial_lot_num_track.niin_id UNION SELECT serial_lot_num_track_hist.serial_number, serial_lot_num_track_hist.lot_con_num, location.location_label, niin_info.niin FROM serial_lot_num_track_hist, location, niin_info where serial_lot_num_track_id = ? AND location.location_id = serial_lot_num_track_hist.location_id AND niin_info.niin_id = serial_lot_num_track_hist.niin_id", 0)) {
                        preparedStatement.setObject(1, errRs.getObject(2));
                        preparedStatement.setObject(2, errRs.getObject(2));
                        try (ResultSet resultSet = preparedStatement.executeQuery()) {
                          if (resultSet.next()) {
                            //Get the data from the serial number tracking table, including the niin and the location STRATIS "thought" the item was in
                            recRow.setAttribute("SERIAL_NUMBER", resultSet.getObject(1));
                            recRow.setAttribute("LOT_NUMBER", resultSet.getObject(2));
                            recRow.setAttribute("INV_LOCATION", resultSet.getObject(3));
                            recRow.setAttribute("NIIN", resultSet.getObject(4));
                          }
                        }
                      }
                      try (PreparedStatement preparedStatement = getDBTransaction().createPreparedStatement("SELECT scn, pin, time_of_pick FROM picking WHERE pid = ?", 0)) {
                        preparedStatement.setInt(1, pid);
                        try (ResultSet resultSet = preparedStatement.executeQuery()) {
                          if (resultSet.next()) {
                            //Get additional information about the picked item
                            recRow.setAttribute("SCN", resultSet.getObject(1));
                            recRow.setAttribute("PIN", resultSet.getObject(2));
                            recRow.setAttribute("TIME_OF_PICK", resultSet.getTimestamp(3));
                          }
                        }
                      }
                      //Finally Insert the row into the table
                      serialView.insertRow(recRow);
                    }
                  }
                }
                break;
              case 78: //Courier Walk Thru
                errMap.put("eid", eid);
                break;
              case 79: //Pick Bypass Exception
                try (PreparedStatement Rwhos = getDBTransaction().createPreparedStatement("select niin_info.niin, location.location_label, picking.scn, picking.suffix_code, picking.pick_qty, picking.pid, issue.issue_type from niin_info, picking, niin_location, location, issue where picking.pid = ? and picking.niin_loc_id = niin_location.niin_loc_id and niin_location.location_id = location.location_id and niin_location.niin_id = niin_info.niin_id and issue.scn = picking.scn", 0)) {
                  Rwhos.setObject(1, errRs.getObject(2));
                  try (ResultSet Rwhos2 = Rwhos.executeQuery()) {
                    if (Rwhos2.next()) {
                      errMap.put("eid", eid);
                      errMap.put("niin", Rwhos2.getString(1));
                      errMap.put("location", Rwhos2.getString(2));
                      errMap.put("scn", Rwhos2.getString(3));
                      errMap.put("suffix", Rwhos2.getInt(4));
                      errMap.put("pickqty", Rwhos2.getInt(5));
                      errMap.put("pid", Rwhos2.getInt(6));
                      errMap.put("issue_type", Rwhos2.getString(7));

                      pickView = getpickExceptionView1();
                      pickView.executeQuery();
                      Row pickRow = pickView.createRow();
                      pickRow.setAttribute("Scn", Rwhos2.getString(3));
                      pickRow.setAttribute("Niin", Rwhos2.getString(1));
                      pickRow.setAttribute("Suffix", Rwhos2.getInt(4));
                      pickRow.setAttribute("PickQty", Rwhos2.getInt(5));
                      pickRow.setAttribute("Location", Rwhos2.getString(2));
                      pickRow.setAttribute("issueType", (Rwhos2.getObject(7) == null ? "" : Rwhos2.getString(7)));
                      pickView.insertRow(pickRow);
                    }
                    else {
                      errMap.put("eid", -1);
                    }
                  }
                }
                break;

              //STOW DELAY
              case 80:
                errMap.put("eid", eid);
                try (PreparedStatement preparedStatement = getDBTransaction().createPreparedStatement("SELECT a.SID, a.QTY_TO_BE_STOWED, a.STOW_QTY, b.KEY_NUM, b.ERROR_QUEUE_ID, r.document_number, ni.niin, ni.nomenclature, ni.ui FROM STOW a inner join ERROR_QUEUE b on b.KEY_NUM = TO_CHAR(a.RCN) inner join RECEIPT r on r.RCN = a.RCN inner join NIIN_INFO ni on ni.NIIN_ID = r.NIIN_ID WHERE b.EID = 80 AND a.RCN = ?", 0)) {
                  preparedStatement.setObject(1, errRs.getObject(2));
                  try (ResultSet resultSet = preparedStatement.executeQuery()) {

                    delayView = getStowDelayView1();
                    delayView.executeQuery();

                    while (resultSet.next()) {

                      Row delayRow = delayView.createRow();
                      delayRow.setAttribute("SID", resultSet.getObject(1));
                      delayRow.setAttribute("RCN", resultSet.getObject(4));
                      delayRow.setAttribute("QTY_TO_BE_STOWED", resultSet.getObject(2));
                      delayRow.setAttribute("STOWED_QTY", resultSet.getObject(3));
                      delayRow.setAttribute("DOCUMENT_NUMBER", resultSet.getObject(6));
                      delayRow.setAttribute("NIIN", resultSet.getObject(7));
                      delayRow.setAttribute("NOMENCLATURE", resultSet.getObject(8));
                      delayRow.setAttribute("UI", resultSet.getObject(9));
                      delayRow.setAttribute("QTY_TO_BE_STOWED", resultSet.getObject(2));
                      delayView.insertRow(delayRow);
                    }
                  }
                }
                break;
              //Courier Issue
              case 81:
                errMap.put("eid", eid);
                //Set up info Issue info
                receiptView = getExceptionReceipt1();
                receiptView.executeQuery();
                String courierInput = errRs.getString(4);
                HashMap courierIssue = convertCourier(courierInput);
                Row recRow = receiptView.createRow();
                recRow.setAttribute("DocumentNumber", hashValue(courierIssue, "docNum"));
                recRow.setAttribute("Niin", hashValue(courierIssue, "niin"));
                recRow.setAttribute("NiinFSC", hashValue(courierIssue, "AAC"));
                recRow.setAttribute("Qty_To_Stow", hashValue(courierIssue, "qty"));
                receiptView.insertRow(recRow);

                String errormessage = hashValue(courierIssue, "error");
                String niinid = hashValue(courierIssue, "niinid");
                if (errormessage.equals("N")) {
                  nlView = getNiinLocationWalkThruView1();
                  vc = nlView.createViewCriteria();
                  vcr = vc.createViewCriteriaRow();
                  vcr.setAttribute("NiinId", " = " + niinid);
                  vc.addElement(vcr);
                  nlView.applyViewCriteria(vc);
                  nlView.executeQuery();
                  errMap.put("error", "");
                  errMap.put("serialized", courierIssue.get("serialized").toString());
                }
                else {
                  errMap.put("error", errormessage);
                  errMap.put("serialized", "N");
                }

                break;
              //Walk-Thru Issue
              case 82:
                errMap.put("eid", eid);
                String oldNIIN = "";
                String newNIIN = "";
                String qty = "";

                try (PreparedStatement niinPs = getDBTransaction().createPreparedStatement("select niin from niin_info where niin_id = ?", 0)) {
                  niinPs.setObject(1, errRs.getObject(3));
                  try (ResultSet niinRs = niinPs.executeQuery()) {
                    if (niinRs.next()) oldNIIN = niinRs.getString(1);
                  }
                }

                try (PreparedStatement niinPs = getDBTransaction().createPreparedStatement("select niin from niin_info where niin_id = ?", 0)) {
                  niinPs.setObject(1, errRs.getObject(2));
                  try (ResultSet niinRs = niinPs.executeQuery()) {
                    if (niinRs.next()) newNIIN = niinRs.getString(1);
                  }
                }

                try (PreparedStatement preparedStatement = getDBTransaction().createPreparedStatement("select qty from issue where document_number = ?", 0)) {
                  preparedStatement.setString(1, errRs.getString(4));
                  try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) qty = resultSet.getString(1);
                  }
                }

                receiptView = getExceptionReceipt1();
                receiptView.executeQuery();
                recRow = receiptView.createRow();
                recRow.setAttribute("DocumentNumber", errRs.getString(4));
                recRow.setAttribute("Niin", oldNIIN);
                recRow.setAttribute("NiinFSC", newNIIN);
                recRow.setAttribute("Qty_To_Stow", qty);
                receiptView.insertRow(recRow);
                break;
              default:
                errMap.put("eid", -2);
            }
          }
          else {
            errMap.put("eid", -1);
          }
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      errMap.put("eid", -1);
    }

    return errMap;
  }

  public String hashValue(HashMap map, String key) {
    if (map.get(key) == null)
      return "";
    else
      return map.get(key).toString();
  }

  public HashMap<String, Object> convertCourier(String courierInput) {
    HashMap<String, Object> courierIssue = new HashMap();
    courierIssue.put("error", "N");
    try {
      WorkLoadManagerImpl workload = getWorkloadManagerService();
      courierIssue.put("docId", "Z0A");
      //Routing Identifier Code 4-6
      courierIssue.put("ric", courierInput.substring(3, 6));
      //PassControlCode 7
      courierIssue.put("msc", courierInput.substring(6, 7));
      //FSC 8 - 11
      String fsc = courierInput.substring(7, 11);
      courierIssue.put("fsc", fsc);
      //NIIN 12-20
      String niin = courierInput.substring(11, 20);
      courierIssue.put("niin", niin);

      Object niinid = null;
      //Make a call to workload manager to find the NIIN_ID for a given NIIN & FSC in the NIIN_INFO Table
      niinid = workload.simpleNIINlookup(niin, fsc);

      //Weapon Status Code 21-22
      //BLANK
      //Unit of Issue 23-24
      courierIssue.put("ui", courierInput.substring(22, 24));
      //Quantity 25-29
      courierIssue.put("qty", courierInput.substring(24, 29));
      //Document Number 30-43
      String docNum = courierInput.substring(29, 43);
      courierIssue.put("docNum", docNum);
      String AAC = docNum.substring(0, 6);

      //Demand Code 44
      courierIssue.put("sfx", courierInput.substring(43, 44));

      //Supplementary Address 45-50
      courierIssue.put("supAdd", courierInput.substring(44, 50));
      //Signal Code 51
      courierIssue.put("signalCode", courierInput.substring(50, 51));
      //Ero Number 52-56
      courierIssue.put("eroNum", courierInput.substring(51, 56).trim());
      //Blank filler 57-58-59

      //Priority Designator 60-61
      Integer issuePriorityDesignator = Integer.parseInt(courierInput.substring(59, 61));
      courierIssue.put("pridesignator", issuePriorityDesignator);
      if ((issuePriorityDesignator == 1) || (issuePriorityDesignator == 2) ||
          (issuePriorityDesignator == 3) || (issuePriorityDesignator == 7) ||
          (issuePriorityDesignator == 8)) {
        courierIssue.put("pri_designator_group", "1");
      }
      else if ((issuePriorityDesignator == 4) || (issuePriorityDesignator == 5) ||
          (issuePriorityDesignator == 6) || (issuePriorityDesignator == 9) || (issuePriorityDesignator == 10)) {
        courierIssue.put("pri_designator_group", "2");
      }
      else {
        courierIssue.put("pri_designator_group", "3");
      }

      //RDD 62-64
      courierIssue.put("rdd", courierInput.substring(61, 64));
      //Advice Code 65-66
      courierIssue.put("adviceCode", courierInput.substring(64, 66));
      //Control Code 67
      //BLANK
      //PRoject Code 68
      courierIssue.put("projectCode", courierInput.substring(67, 68));
      //Cost Jon 69-80
      courierIssue.put("costJon", courierInput.substring(68, 80));

      if (niinid == null) {
        courierIssue.put("error", "ERROR: NIIN not found");
      }
      else {
        try (PreparedStatement rPs = getDBTransaction().createPreparedStatement("SELECT serial_control_flag from NIIN_INFO where NIIN_ID = ?", 0)) {
          rPs.setObject(1, niinid);
          try (ResultSet rRs = rPs.executeQuery()) {
            if (rRs.next()) {
              courierIssue.put("serialized", rRs.getString(1));
            }
            else {
              courierIssue.put("serialized", "N");
            }
          }
        }
        courierIssue.put("niinid", niinid.toString());
      }

      //Make a call to workload manager to return the ID number for this customer's AAC
      Object customerid = workload.getCustomerId(AAC);

      if (customerid == null) {
        courierIssue.put("error", "ERROR: Customer not found");
      }
      else {
        courierIssue.put("customerid", customerid.toString());
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      courierIssue.put("error", "An error has occured. Please see log.");
    }
    return courierIssue;
  }

  public HashMap getCourierInformation(int error_queue_id) {
    HashMap<String, Object> courierIssue = null;
    try (PreparedStatement errorPs = getDBTransaction().createPreparedStatement("select notes from error_queue where error_queue_id = ?", 0)) {
      WorkLoadManagerImpl workload = getWorkloadManagerService();
      errorPs.setInt(1, error_queue_id);
      String courierInput = "";
      try (ResultSet errRs = errorPs.executeQuery()) {
        if (errRs.next()) {
          courierInput = errRs.getString(1);
        }
      }

      if (!courierInput.isEmpty()) {
        courierIssue = convertCourier(courierInput);
      }
      else {
        courierIssue = new HashMap();
        courierIssue.put("error", "Bad Courier Import Information");
      }
      return courierIssue;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return courierIssue;
  }

  public String rejectCourier(int error_queue_id, int uID) {
    String result = "";
    try (PreparedStatement errorPs = getDBTransaction().createPreparedStatement("select notes from error_queue where error_queue_id = ?", 0)) {
      WorkLoadManagerImpl workload = getWorkloadManagerService();
      errorPs.setInt(1, error_queue_id);
      String courierInput = "";
      try (ResultSet errRs = errorPs.executeQuery()) {
        if (errRs.next()) {
          courierInput = errRs.getString(1);
        }
      }

      if (!courierInput.isEmpty()) {
        HashMap courierIssue = convertCourier(courierInput);
        String errormessage = hashValue(courierIssue, "error");
        if (errormessage.equals("N")) {
          Object scn = workload.createIssueHist(hashValue(courierIssue, "documentid"), hashValue(courierIssue, "documentnumber"),
              hashValue(courierIssue, "qty"), hashValue(courierIssue, "issuetype"),
              hashValue(courierIssue, "issueprioritydesignator"),
              hashValue(courierIssue, "issueprioritygroup"), hashValue(courierIssue, "customerid"),
              hashValue(courierIssue, "niinid"), hashValue(courierIssue, "suffix"), hashValue(courierIssue, "supaddress"),
              hashValue(courierIssue, "fundcode"), hashValue(courierIssue, "mediastatuscode"),
              hashValue(courierIssue, "ricFrom"), hashValue(courierIssue, "costJon"),
              hashValue(courierIssue, "signalCode"), hashValue(courierIssue, "adviceCode"),
              hashValue(courierIssue, "rdd"), hashValue(courierIssue, "cc"), hashValue(courierIssue, "demilCode"));
          if (scn == null) {
            return "Unable to create issue history";
          }
        }
        else {
          return errormessage;
        }
      }
      else {
        result = "Cannot get issue from Exception";
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return result;
  }

  /**
   * FUNCTION : updateMatsA5J
   * DESC     : This function sets a condition code value to a MATS row that doesnt process
   * due to a null condition code, and re-submits for processing.
   */
  public boolean updateConvertedQuantity(int errId, int qty, int userId) {
    boolean retVal = false;
    try (PreparedStatement errorPs = getDBTransaction().createPreparedStatement("select key_num from error_queue where error_queue_id = ?", 0)) {
      //Retrieve niin_loc_id from error_queue
      errorPs.setInt(1, errId);
      int niinLocId = -1;
      try (ResultSet errRs = errorPs.executeQuery()) {
        if (errRs.next()) {
          niinLocId = errRs.getInt(1);
        }
      }
      if (niinLocId == -1) return false;

      //For the given niin_loc, find the niin_id and qty at that niin_loc
      int oldQty = -1;
      int niinId = -1;
      try (PreparedStatement niinPs = getDBTransaction().createPreparedStatement("select niin_id, qty from niin_location where niin_loc_id = ?", 0)) {
        niinPs.setInt(1, niinLocId);
        try (ResultSet niinRs = niinPs.executeQuery()) {
          if (niinRs.next()) {
            niinId = niinRs.getInt(1);
            oldQty = niinRs.getInt(2);
          }
        }
      }

      if (niinId == -1 || oldQty == -1) return false;

      long lQty = 0, lOld = 0;
      lQty += qty;
      lOld += oldQty;
      BigDecimal weightRatio = BigDecimal.valueOf(1);

      if (lOld != 0) {
        //Calculate the ratio of the old location qty to the new location quantity
        if (lQty < lOld) {
          //If the new quantity is smaller then the old quantity
          lQty = lOld / lQty;
          //Take the DIV factor and invert it to scale quantity down.  Leftovers discarded in conversion
          weightRatio = BigDecimal.valueOf(1).divide(BigDecimal.valueOf(lQty), 8, 0);
        }
        else {
          //
          lQty = lQty / lOld;
          //Take the DIV factor to scale the quantity up.  Leftovers discarded in conversion
          weightRatio = BigDecimal.valueOf(lQty);
        }

        log.debug("===> weightRatio {} : qty {} : errId {} : niinLocId {}  : niinId  {}", weightRatio, qty, errId, niinLocId, niinId);

        //Modify niin_info to adjust weight and cube based on the ratio.
        //NOTE:  Make sure to do this ONLY one time per niin.  Otherwise conversion will be wrong.  Check conversion dates! -JPO
        try (PreparedStatement niinPs = getDBTransaction().createPreparedStatement("update niin_info set weight = (weight * ?), cube = (cube * ?) where niin_id = ?", 0)) {
          niinPs.setBigDecimal(1, weightRatio);
          niinPs.setBigDecimal(2, weightRatio);
          niinPs.setInt(3, niinId);
          niinPs.execute();
          this.getDBTransaction().commit();
        }
      }

      //Update the qty, old_qty, and lock for just the location the exception was made for.  Exact qty entered is used.
      try (PreparedStatement niinPs = getDBTransaction().createPreparedStatement("update niin_location set qty = ?, old_qty = qty, locked = 'N' where niin_loc_id = ?", 0)) {
        niinPs.setInt(1, qty);
        niinPs.setInt(2, niinLocId);
        niinPs.execute();
        this.getDBTransaction().commit();
      }

      //Update the qty of ALL other locations based off of calculated conversion factor, put the previous qty into old_qty, and unlock it.
      //the trunc function returns a number truncated to a certain number of decimal places -
      //stratis requires round down
      try (PreparedStatement niinPs = getDBTransaction().createPreparedStatement("update niin_location set qty = trunc(qty * ?), old_qty = qty, locked = 'N' where niin_id = ? and niin_loc_id <> ?", 0)) {
        niinPs.setDouble(1, weightRatio.doubleValue());
        niinPs.setInt(2, niinId);
        niinPs.setInt(3, niinLocId);
        niinPs.execute();
        this.getDBTransaction().commit();
      }

      try (PreparedStatement niinPs = getDBTransaction().createPreparedStatement("update error_queue set modified_by = ?, modified_date = sysdate where (key_type = 'niin_loc_id' OR key_type = 'NIIN_LOC_ID') and error_queue_id <> ? and key_num in (select niin_loc_id from niin_location where niin_id in (select niin_id from niin_location where niin_loc_id = ?))", 0)) {
        niinPs.setInt(1, userId);
        niinPs.setInt(2, errId);
        niinPs.setInt(3, niinLocId);
        niinPs.execute();
        this.getDBTransaction().commit();
      }

      try (PreparedStatement niinPs = getDBTransaction().createPreparedStatement("delete from error_queue where (key_type = 'niin_loc_id' OR key_type = 'NIIN_LOC_ID') and error_queue_id <> ? and key_num in (select niin_loc_id from niin_location where niin_id in (select niin_id from niin_location where niin_loc_id = ?))", 0)) {
        niinPs.setInt(1, errId);
        niinPs.setInt(2, niinLocId);
        niinPs.execute();
        this.getDBTransaction().commit();
      }

      WorkLoadManagerImpl workload = getWorkloadManagerService();
      workload.createErrorQueueRecord("57", "niin_loc_id", String.valueOf(niinLocId), "1", null);
      retVal = true;
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
   * FUNCTION : updateDimensions
   * DESC     : This function updates dimensions for a given niin
   */
  public boolean updateDimensions(int errId, double length, double width,
                                  double height) {
    boolean retVal = false;
    try (PreparedStatement errorPs = getDBTransaction().createPreparedStatement("select key_num from error_queue where error_queue_id = ?", 0)) {
      //Retrieve niin_loc_id from error_queue
      errorPs.setInt(1, errId);
      int niinLocId = -1;
      try (ResultSet errRs = errorPs.executeQuery()) {
        if (errRs.next()) {
          niinLocId = errRs.getInt(1);
        }
      }
      if (niinLocId == -1)
        return false;

      //For the given niin_loc, find the niin_id and qty at that niin_loc
      int niinId = -1;
      try (PreparedStatement niinPs = getDBTransaction().createPreparedStatement("select niin_id, qty from niin_location where niin_loc_id = ?", 0)) {
        niinPs.setInt(1, niinLocId);
        try (ResultSet niinRs = niinPs.executeQuery()) {
          if (niinRs.next()) {
            niinId = niinRs.getInt(1);
          }
        }
      }
      if (niinId == -1)
        return false;

      //Modify niin_info to adjust weight and cube based on the ratio.
      try (PreparedStatement niinPs = getDBTransaction().createPreparedStatement("update niin_info set length = ?, width = ?, height = ? where niin_id = ?", 0)) {
        niinPs.setDouble(1, length);
        niinPs.setDouble(2, width);
        niinPs.setDouble(3, height);
        niinPs.setInt(4, niinId);
        niinPs.execute();
        this.getDBTransaction().commit();
      }

      retVal = true;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return retVal;
  }

  /**
   * FUNCTION : clearDimensionUpdate
   * DESC     : This function takes a given error (EID 57) and retrieves the niin_location in question
   * it finds the niin for which this error was removed, and makes sure to remove any other errors
   * eith EID-57 and that are for the same NIIN.  The dimensions only need adjusting once.
   */
  public boolean clearDimensionUpdates(int errId, int userID) {
    boolean retVal = false;
    try {
      //Retrieve niin_loc_id from error_queue
      int niinLocId = -1;
      try (PreparedStatement errorPs = getDBTransaction().createPreparedStatement("select key_num from error_queue where error_queue_id = ?", 0)) {
        errorPs.setInt(1, errId);
        try (ResultSet errRs = errorPs.executeQuery()) {
          if (errRs.next()) {
            niinLocId = errRs.getInt(1);
          }
        }
      }
      if (niinLocId == -1)
        return false;

      try (PreparedStatement delPs = getDBTransaction().createPreparedStatement("update error_queue set modified_by = ?, modified_date = sysdate where (key_type = 'niin_loc_id' OR key_type = 'NIIN_LOC_ID') and key_num in (select niin_loc_id from niin_location where niin_id in (select niin_id from niin_location where niin_loc_id = ?))", 0)) {
        delPs.setInt(1, userID);
        delPs.setInt(2, niinLocId);
        delPs.execute();
        this.getDBTransaction().commit();
      }

      //For the given niin_loc, find the niin_id at that niin_loc, and remove all EID 57 Exceptions for niin_loc_ids that have that niin.
      try (PreparedStatement delPs = getDBTransaction().createPreparedStatement("delete from error_queue where (key_type = 'niin_loc_id' OR key_type = 'NIIN_LOC_ID') and key_num in (select niin_loc_id from niin_location where niin_id in (select niin_id from niin_location where niin_loc_id = ?))", 0)) {
        delPs.setInt(1, niinLocId);
        delPs.execute();
        this.getDBTransaction().commit();
      }

      retVal = true;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return retVal;
  }

  /**
   * FUNCTION : updateMatsA5J
   * DESC     : This function sets a condition code value to a MATS row that doesnt process
   * due to a null condition code, and re-submits for processing.
   */
  public boolean updateMatsA5J(int errId, String cc) {
    boolean retVal = false;
    try (PreparedStatement errorPs = getDBTransaction().createPreparedStatement("select key_num from error_queue where error_queue_id = ?", 0)) {
      errorPs.setInt(1, errId);
      int refMatsId = -1;
      try (ResultSet errRs = errorPs.executeQuery()) {
        if (errRs.next()) {
          refMatsId = errRs.getInt(1);
        }
      }
      if (refMatsId == -1)
        return false;

      //Update mats table for the selected condition code
      try (PreparedStatement PsUp = getDBTransaction().createPreparedStatement("update ref_mats set condition_code = ? where ref_mats_id = ?", 0)) {
        PsUp.setString(1, cc);
        PsUp.setObject(2, refMatsId);
        PsUp.execute();
        this.getDBTransaction().commit();
      }

      //Re-Process Mats Transaction
      WorkLoadManagerImpl workload = getWorkloadManagerService();
      workload.reProcessMATSRow(refMatsId);

      retVal = true;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return retVal;
  }

  /**
   * FUNCTION : updateStowLoss
   * DESC     : This function takes a pick and resets the status to pick ready
   */
  public boolean updateStowLoss(int errId, String stowStatus, int userID) {
    GCSSMCTransactionsImpl transaction = this.getGCSSMCAMService();
    int iCount = 0;
    boolean retVal = false;
    try (PreparedStatement errorPs = getDBTransaction().createPreparedStatement("select key_num from error_queue where error_queue_id = ?", 0)) {
      errorPs.setInt(1, errId);
      String sid = null;
      try (ResultSet errRs = errorPs.executeQuery()) {
        if (errRs.next()) {
          sid = errRs.getString(1);
        }
      }
      if (sid == null)
        return false;

      try (PreparedStatement PsUp = getDBTransaction().createPreparedStatement("update stow set status = ? where sid = ?", 0)) {
        PsUp.setString(1, stowStatus);
        PsUp.setString(2, sid);
        PsUp.execute();
        this.getDBTransaction().commit();
      }

      int rcn = 0;
      try (PreparedStatement psRcn = getDBTransaction().createPreparedStatement("select rcn from stow where sid = ?", 0)) {
        psRcn.setString(1, sid);
        try (ResultSet rsRcn = psRcn.executeQuery()) {
          rsRcn.next();
          rcn = rsRcn.getInt(1);
        }
      }

      try (PreparedStatement ps = getDBTransaction().createPreparedStatement("select count(*) \n" +
          "from ref_dasf \n" +
          "where document_number in (select document_number from receipt where rcn = ?) and\n" +
          "      record_niin in (select niin from niin_info where niin_id in (select niin_id from receipt where rcn = ?))", 0)) {
        ps.setString(1, String.valueOf(rcn));
        ps.setString(2, String.valueOf(rcn));
        try (ResultSet rs3 = ps.executeQuery()) {
          if (rs3.next()) {
            iCount = rs3.getInt(1);
          }
        }
      }

      TransactionsImpl service = getTransactionManagerService();
      service.processReceiptTransaction(rcn, sid, null, userID);

      return true;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return retVal;
  }

  public boolean updateToStowLoss(int errId, String stowStatus, int userID) {
    boolean retVal = false;
    GCSSMCTransactionsImpl transaction = this.getGCSSMCAMService();
    try (PreparedStatement errorPs = getDBTransaction().createPreparedStatement("select key_num, key_type from error_queue where error_queue_id = ?", 0)) {
      errorPs.setInt(1, errId);
      int rcn = 0;
      String sid = null;
      try (ResultSet errRs = errorPs.executeQuery()) {
        if (errRs.next()) {
          if ((errRs.getString(2)).equalsIgnoreCase("SID")) sid = errRs.getString(1);
          else rcn = Integer.parseInt(errRs.getString(1));
        }
      }

      if (rcn != 0) {
        this.updateStowStatus(rcn, sid, stowStatus);

        try (PreparedStatement psRcn = getDBTransaction().createPreparedStatement("select sid, stow_qty from stow where rcn = ? and (status <> 'STOWED' and status not like '%CANCEL%')", 0)) {
          psRcn.setInt(1, rcn);
          try (ResultSet rsRcn = psRcn.executeQuery()) {

            while (rsRcn.next()) {
              sid = rsRcn.getString(1);

              if (stowStatus.equals("STOW LOSS1")) {

                try (PreparedStatement query = getDBTransaction().createPreparedStatement("INSERT INTO ERROR_QUEUE (STATUS, EID, KEY_TYPE, KEY_NUM, CREATED_BY, CREATED_DATE)" + "VALUES (?, ?, ?, ?, ?, sysdate)", 0)) {

                  query.setString(1, "OPEN");
                  query.setInt(2, 47);
                  query.setString(3, "SID");
                  query.setString(4, sid);
                  query.setInt(5, userID);
                  query.execute();
                  this.getDBTransaction().commit();
                }
              }
            }
          }
        }
      }
      else {
        int stowloss = 0;
        this.updateStowStatus(rcn, sid, stowStatus);
        try (PreparedStatement rcnQ = getDBTransaction().createPreparedStatement("select rcn from stow where sid = ?", 0)) {
          rcnQ.setString(1, sid);
          try (ResultSet rs = rcnQ.executeQuery()) {

            if (rs.next()) rcn = rs.getInt(1);
          }
        }
        if (stowStatus.equalsIgnoreCase("STOW LOSS1") && getSiteGCCSSMCFlag().equals("Y")) {
          stowloss = transaction.sendSTLGCSSMCTransaction(sid);
          try (PreparedStatement rec = getDBTransaction().createPreparedStatement("Update Receipt set qty_stowloss = (qty_stowloss + ?) where rcn = ? ", 0)) {
            rec.setInt(1, stowloss);
            rec.setInt(2, rcn);
            rec.executeUpdate();
            getDBTransaction().commit();
          }
          try (PreparedStatement up = getDBTransaction().createPreparedStatement("Update stow set status = ? where sid=?", 0)) {
            up.setString(1, "STOW LOSS");
            up.setString(2, sid);
            up.executeUpdate();
          }
        }
        else if (getSiteGCCSSMCFlag().equals("N")) {
          TransactionsImpl service = getTransactionManagerService();
          service.processReceiptTransaction(rcn, sid, null, userID);
        }
      }

      return true;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return retVal;
  }

  public boolean updateStowStatus(String status, int errId) {

    boolean retVal = false;

    try (PreparedStatement errorPs = getDBTransaction().createPreparedStatement("select  key_type from error_queue where error_queue_id = ?", 0)) {
      errorPs.setInt(1, errId);
      try (ResultSet errRs = errorPs.executeQuery()) {

        String sid = null;

        if (errRs.next()) sid = errRs.getString(1);

        try (PreparedStatement stwstmt = getDBTransaction().createPreparedStatement("update stow set status = 'STOW READY', assign_to_user = null  where sid = ?", 0)) {
          stwstmt.setString(1, sid);
          stwstmt.executeUpdate();
          getDBTransaction().commit();
        }
      }
      retVal = true;

      return retVal;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return retVal;
  }

  public void updateStowStatus(int rcn, String sid, String stowStatus) {
    log.debug("update stow status to  {}", stowStatus);
    String sql = null;

    try {
      if (rcn != 0) {
        sql = "update stow set status = ? where  rcn = ? and sid=?";
        try (PreparedStatement PsUp = getDBTransaction().createPreparedStatement(sql, 0)) {
          PsUp.setString(1, stowStatus);
          PsUp.setInt(2, rcn);
          PsUp.setString(3, sid);
          PsUp.execute();
          this.getDBTransaction().commit();
        }
      }
      else {
        sql = "update stow set status = ? where sid = ?";
        try (PreparedStatement PsUp = getDBTransaction().createPreparedStatement(sql, 0)) {
          PsUp.setString(1, stowStatus);
          PsUp.setString(2, sid);
          PsUp.execute();
          this.getDBTransaction().commit();
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  public String getNiinExpInfo(int niinLocId) {
    String retVal = "Error";
    try (PreparedStatement errorPs = getDBTransaction().createPreparedStatement("select niin_info.niin, location.location_label from niin_info, niin_location, location where niin_location.niin_loc_id = ? and niin_location.niin_id = niin_info.niin_id and niin_location.location_id = location.location_id", 0)) {
      errorPs.setInt(1, niinLocId);
      try (ResultSet errRs = errorPs.executeQuery()) {
        if (errRs.next()) {
          String niin = errRs.getString(1);
          String locLabel = errRs.getString(2);
          retVal = niin + "|" + locLabel;
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return retVal;
  }

  /**
   * Used by rewarehouse
   */
  public void clearTable() {
    RewarehouseTempViewImpl niinlocs = getRewarehouseTempView1();
    RewarehouseTempViewImpl niinlocs2 = getRewarehouseTempView2();
    NiinSearchViewImpl view = this.getNiinSearchView1();

    niinlocs.clearCache();
    niinlocs2.clearCache();
    view.clearCache();
  }

  /**
   * Create a receipt row for REWAREHOUSE only
   * Returns -1 if the receipt failed creation
   */
  public int createReceiptRow(int niinId, int quantity, int temp4,
                              String cc) {
    int returnVal = 0;

    try {
      ReceiptViewImpl view = this.getReceiptView1();
      Row row = view.createRow();

      row.setAttribute("QuantityStowed", quantity);
      row.setAttribute("QuantityInvoiced", quantity);
      row.setAttribute("QuantityInducted", quantity);
      row.setAttribute("QuantityMeasured", 0);
      row.setAttribute("QuantityReleased", 0);
      row.setAttribute("NiinId", niinId);
      row.setAttribute("MechNonMechFlag", "N");
      row.setAttribute("Status", "REWAREHOUSE");
      row.setAttribute("Cc", cc);
      row.setAttribute("CreatedBy", temp4);
      view.insertRowAtRangeIndex(0, row);

      this.getDBTransaction().commit();
      row.refresh(row.REFRESH_CONTAINEES);

      // get the id
      returnVal = Integer.parseInt(row.getAttribute("Rcn").toString());
      log.debug("createReceiptRow  {}", returnVal);
    }
    catch (Exception e) {
      this.getDBTransaction().rollback();
      AdfLogUtility.logException(e);
      returnVal = -1;
    }
    return returnVal;
  }

  /**
   * Returns 0 if there is already a stow row created for the niin location or
   * Returns -1 if the stow failed creation
   */
  public int createStowRow(int niin_loc_id, int niinId, int rcn,
                           int temp4) {

    int returnVal = 0;
    boolean _found = false;
    String dateOfManu = "";
    String expire = "";
    String caseWeight = "";
    String serialNumber = "";
    String packedDate = "";
    String sql = "select date_of_manufacture, expiration_date, case_weight_qty, serial_number, packed_date " + "from niin_location where niin_loc_id=? and niin_id=?";

    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(sql, 0)) {
      pstmt.setInt(1, niin_loc_id);
      pstmt.setInt(2, niinId);
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          dateOfManu = rs.getString(1);
          expire = rs.getString(2);
          caseWeight = rs.getString(3);
          serialNumber = rs.getString(4);
          packedDate = rs.getString(5);
          _found = true;
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }

    if (_found) {
      try {
        WorkLoadManagerImpl workload = getWorkloadManagerService();
        String nsid = workload.assignSID();

        StowViewImpl view = this.getStowView1();
        Row row = view.createRow();
        row.setAttribute("LocationId", 1);
        row.setAttribute("DateOfManufacture", dateOfManu);
        row.setAttribute("ExpirationDate", expire);
        row.setAttribute("CaseWeightQty", caseWeight);
        row.setAttribute("SerialNumber", serialNumber);
        row.setAttribute("PackedDate", packedDate);
        row.setAttribute("ScanInd", "N");
        row.setAttribute("Sid", nsid);
        row.setAttribute("Status", "STOW READY");
        row.setAttribute("Rcn", rcn);
        row.setAttribute("QtyToBeStowed", 1);

        row.setAttribute("CreatedBy", temp4);
        view.insertRowAtRangeIndex(0, row);
        this.getDBTransaction().commit();

        row.refresh(Row.REFRESH_CONTAINEES);

        // get the id
        returnVal = Integer.parseInt(row.getAttribute("StowId").toString());
        log.debug("createStowRow  {}", returnVal);
        view.clearCache();
      }
      catch (Exception e) {
        this.getDBTransaction().rollback();
        AdfLogUtility.logException(e);
        returnVal = -1;
      }
    }

    return returnVal;
  }

  /**
   * Returns the SID of the stow record
   */
  public String getSID(int stowId) {
    String retVal = "Error";
    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement("select sid from stow where stow_id=?", 0)) {
      pstmt.setInt(1, stowId);
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          retVal = rs.getString(1);
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return retVal;
  }

  /**
   * Given the niin, grab the niin location details
   * Populates temporary table RewarehouseTempView
   */
  public void getRewarehouseLocations(String niin, String sort, boolean ascending) {
    RewarehouseTempViewImpl niinlocs = getRewarehouseTempView1();
    for (Row row : niinlocs.getAllRowsInRange()) {
      row.remove();
    }

    if (Util.isEmpty(niin))
      return;
    try {
      //* 04-02-09 modify sql to not allow rewarehouse if no qty available due to pending picks
      String sqlQry = "select distinct nl.niin_loc_id, nl.qty, nl.CC, nl.expiration_date, l.location_label, nl.date_of_manufacture, " + "n.niin_id, n.weight, l.location_id, n.nomenclature, n.price, n.ui, nvl(nl.locked,'N') as locked, " + "nvl(w.sum_pick,0) as sum_pick, (nl.qty-nvl(sum_pick,0)) as Left " + "from niin_location nl, location l, niin_info n, picking p, " + "(Select Sum(b.Pick_qty) sum_pick,c.location_id " + "FROM NIIN_LOCATION a, PICKING b, LOCATION c " + "WHERE a.NIIN_LOC_ID = b.NIIN_LOC_ID AND " + "a.LOCATION_ID = c.LOCATION_ID AND " + "b.STATUS <> 'PACKED' AND b.STATUS <> 'PICK REFUSED' AND " + "b.STATUS <> 'PICKED' AND b.STATUS <> 'WALKTHRU' " + "group by c.location_id) w " + "where nl.location_id = l.location_id and nl.niin_id = n.niin_id and " + "nl.niin_loc_id = p.niin_loc_id (+) and l.location_id = w.location_id(+) " + "and (nl.qty-nvl(sum_pick,0)) >= 1 and n.niin = ?";
      if (sort != null) {
        //NOTE: Do the sort in the reverse order since it adds each row at the beginning of the sequence
        // example A,B,C adds A, then B before A, then C before B => C,B,A
        if (sort.equalsIgnoreCase("Location")) {
          sqlQry += " order by l.location_label " + (ascending ? "DESC" : "ASC");
        }
        else if (sort.equalsIgnoreCase("Locked")) {
          sqlQry += " order by locked " + (ascending ? "DESC" : "ASC");
        }
      }
      try (PreparedStatement nlps = getDBTransaction().createPreparedStatement(sqlQry, 0)) { //*  show rewarehouse rows with qty less than 1

        nlps.setString(1, niin);

        try (ResultSet nlrs = nlps.executeQuery()) {
          while (nlrs.next()) {
            // put all the results in the view
            Row nrow = niinlocs.createRow();
            nrow.setAttribute("Location", nlrs.getObject(5));
            nrow.setAttribute("LocationQTY", nlrs.getObject(15)); //2
            nrow.setAttribute("LocationCC", nlrs.getObject(3));
            nrow.setAttribute("Expiration", nlrs.getObject(4));
            nrow.setAttribute("NiinLocId", nlrs.getObject(1));
            nrow.setAttribute("DateofManufacture", nlrs.getObject(6));
            nrow.setAttribute("NiinId", nlrs.getObject(7));
            nrow.setAttribute("Weight", nlrs.getObject(8));
            nrow.setAttribute("LocationId", nlrs.getObject(9));
            nrow.setAttribute("Niin", niin);
            nrow.setAttribute("Locked", nlrs.getObject(13));

            // add the row
            niinlocs.insertRow(nrow);
          }
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  /**
   * Returns true if a specific niin location is locked.
   * Used by Rewarehouse
   */
  public boolean isNIINLocationLocked(int niinLocId) {

    boolean flag = false;
    try (PreparedStatement stR = getDBTransaction().createPreparedStatement("select nvl(locked,'N') from niin_location where niin_loc_id=?", 0)) {
      stR.setInt(1, niinLocId);
      try (ResultSet rs = stR.executeQuery()) {
        if (rs.next()) {
          String locked = rs.getString(1);
          if (locked.equalsIgnoreCase("Y")) flag = true;
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return flag;
  }

  /**
   * public boolean GenerateRewarehouse (Object fromniinlocid, Object stowid,
   * Object QTYMoved, Object niinid,
   * Object locId, Object rcn, Object cc,
   * int user_id) {
   **/
  public boolean generateRewarehouse(int fromniinlocid, int stowid,
                                     int QTYMoved, int niinid,
                                     int locId, int rcn, String cc,
                                     int user_id) {
    boolean returnvalue = true;
    String scn = "";

    try {
      // get the workload module
      WorkLoadManagerImpl workload = getWorkloadManagerService();
      String Docnum = getTransactionManagerService().returnNextAvailableDocumentNumber();
      log.debug("[generateRewarehouse] next available document number is  {}", Docnum);
      // create the issue first
      Object nscn;
      nscn =
          workload.createIssue("YLL", Docnum, QTYMoved, "R", 1, 1, null,
              niinid, null, null, null, null, null, null, null, null,
              null, cc, null, false);

      if (nscn != null && rcn != 0) {
        scn = String.valueOf(nscn);
        log.debug("new scn is  {}", scn);
        // create the receipt
        try (PreparedStatement addrecps = getDBTransaction().createPreparedStatement("BEGIN UPDATE RECEIPT SET DOCUMENT_NUMBER = ?, MODIFIED_DATE = sysdate, document_id='YLL' WHERE RCN = ?; END; ", 0)) {
          addrecps.setObject(1, Docnum);
          addrecps.setObject(2, rcn);
          addrecps.executeUpdate();
          getDBTransaction().commit();
        }
        // make the pick first

        Number locationId = fromniinlocid;
        Object npid = workload.insertPicking(locationId, QTYMoved, scn, "PICK READY", new AppModuleDBTransactionGetterImpl(this));

        // if we got our pick
        if (npid != null) {
          log.debug("new picking id is  {}", npid);
          try (PreparedStatement updater = getDBTransaction().createPreparedStatement("BEGIN UPDATE STOW SET LOCATION_ID = ?, QTY_TO_BE_STOWED = ?, PID = ?  WHERE STOW_ID = ? AND CREATED_BY = ? ; END;", 0)) {

            updater.setObject(1, locId);
            updater.setObject(2, QTYMoved);
            updater.setObject(3, npid);
            updater.setObject(4, stowid);
            updater.setObject(5, user_id);
            updater.executeUpdate();

            this.getDBTransaction().commit();
          }
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      returnvalue = false;
      this.getDBTransaction().rollback();
    }
    // return the status
    return returnvalue;
  }

  public boolean hasExistingSchedule(String scheduleType,
                                     String connectionId,
                                     String scheduleId) {
    boolean found = false;
    try {
      String sql = "";
      if (scheduleId.equals("")) {
        sql = "select schedule_type from site_schedules where connection_id=?";
      }
      else {
        sql = "select schedule_type from site_schedules where connection_id=? and schedule_id<>?";
      }
      try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(sql, 0)) {
        if (scheduleId.equals("")) {
          pstmt.setString(1, connectionId);
        }
        else {
          pstmt.setString(1, connectionId);
          pstmt.setString(2, scheduleId);
        }

        try (ResultSet rs = pstmt.executeQuery()) {
          while (rs.next()) {
            String importExport = rs.getString(1);
            if (importExport.equals(scheduleType)) found = true;
          }
        }
      }
    }
    catch (Exception e) {
      found = false;
    }

    return found;
  }

  public boolean checkWalkThruNiinOnMHIF(String niin) {
    boolean onMHIF = false;
    String sql = "select controlled_item_code from ref_mhif where record_niin=?";
    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(sql, 0)) {
      pstmt.setString(1, niin);

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          String controlled_item_code = rs.getString(1);
          if (!Util.isEmpty(controlled_item_code)) onMHIF = true;
        }
      }
    }
    catch (Exception e) {
      onMHIF = false;
    }
    return onMHIF;
  }

  /**
   * This function returns a new scn from newly created issue (may return null)
   *
   * @return Object
   */
  public Object createIssueForWalkThru(Object documentid,
                                       Object documentnumber, Object qty,
                                       Object issuetype,
                                       Object issueprioritydesignator,
                                       Object issueprioritygroup,
                                       Object customerid, Object niinid,
                                       Object suffix, Object supaddress,
                                       Object fundcode,
                                       Object mediastatuscode,
                                       Object ricFrom, Object costJon,
                                       Object rdd, Object niin, Object cc,
                                       boolean isHardcard) {
    if (Util.isEmpty(niin))
      return null;
    String signalCode = "A";
    String adviceCode = "2L";
    if (checkWalkThruNiinOnMHIF(niin.toString())) {
      signalCode = "W";
      adviceCode = "4A";
    }

    WorkLoadManagerImpl workload = getWorkloadManagerService();
    return workload.createIssue(documentid, documentnumber, qty, issuetype, issueprioritydesignator, issueprioritygroup, customerid, niinid, suffix, supaddress, fundcode, mediastatuscode, ricFrom, costJon, signalCode, adviceCode, rdd, cc, null, isHardcard);
  }

  public void commit() {
    WorkLoadManagerImpl workload = getWorkloadManagerService();
    workload.getDBTransaction().commit();
  }

  /**
   * Updates an existing NIIN Location.  Puts under audit flag to Y.
   * Sends to Exception Processing
   * Returns a negative number to indicate errors
   * This function is used primarily by the NIIN Inquiry backing bean.
   */
  public int updateNiinLoc(int niin_loc_id, int niin_id, String qty,
                           int user_id) {
    int result = 0;

    log.debug("updateNiinLoc {}, {},{}, {}", niin_loc_id, niin_id, qty, user_id);

    PreparedStatement pstmtUPD = null;
    try {

      String sqlUPD = "update niin_location set qty=?,under_audit='N', locked='N', modified_by=?, modified_date=sysdate where niin_loc_id=?";
      pstmtUPD = getDBTransaction().createPreparedStatement(sqlUPD, 0);

      pstmtUPD.setObject(1, qty);
      pstmtUPD.setObject(2, user_id);
      pstmtUPD.setInt(3, niin_loc_id);

      pstmtUPD.executeUpdate();
      getDBTransaction().commit();
      pstmtUPD.close();
      pstmtUPD = null;
    }
    catch (Exception e) {
      log.debug("an error occurred in SysAdminImpl.updateNiinLoc");
      AdfLogUtility.logException(e);
      this.getDBTransaction().rollback();
      result = -999;
    }
    finally {
      if (pstmtUPD != null) {
        try {
          pstmtUPD.close();
        }
        catch (Exception ps1) {
        }
      }
    }
    return result;
  }

  /**
   * Updates an existing NIIN Location.  Puts under audit flag to Y.
   * Sends to Exception Processing
   * Returns a negative number to indicate errors
   * This function is used primarily by the NIIN Inquiry backing bean.
   */
  public int updateNiinLocDates(int niin_loc_id, String dom, String exp) {
    int result = 0;

    log.debug("updateNiinLoc {}, {}, {}", niin_loc_id, exp, dom);

    PreparedStatement pstmtUPD = null;
    //date_of_manufacture=?
    try {

      String sqlUPD = "update niin_location set expiration_date=to_date(nvl(?,'9999-01-01'),'yyyy-MM-dd') where niin_loc_id=?";
      pstmtUPD = getDBTransaction().createPreparedStatement(sqlUPD, 0);

      pstmtUPD.setString(1, exp.toString());
      pstmtUPD.setInt(2, niin_loc_id);

      pstmtUPD.executeUpdate();
      getDBTransaction().commit();
      pstmtUPD.close();
      pstmtUPD = null;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      this.getDBTransaction().rollback();
      result = -999;
    }
    finally {
      if (pstmtUPD != null) {
        try {
          pstmtUPD.close();
        }
        catch (Exception ps1) {
        }
      }
    }
    return result;
  }

  /**
   * Used by NIIN Inquiry only
   */
  public void generateD(Integer niinId, int qty, String cc, String dNumber,
                        String transType, Integer userId) {
    val dicCode = (dNumber.equals("8") ? "D8" : "D9") + transType;
    this.getGCSSMCTransactions1().sendDxxGCSSMCTransaction(niinId, qty, dicCode, "0", cc, userId);
  }

  public int removeSerialFromNiin(int serialId, int niin_id,
                                  int niin_loc_id, int qty, int iUserId) {
    int result = 0;
    boolean success = true;

    log.debug("removeSerialFromNiin {}, {}, {}", serialId, qty, iUserId);

    PreparedStatement pstmtUPD = null;
    try {

      String sqlUPD = "delete from serial_lot_num_track where SERIAL_LOT_NUM_TRACK_ID=?";
      String sqlUPD2 = "delete from serial_lot_num_track_hist where SERIAL_LOT_NUM_TRACK_ID=?";
      pstmtUPD = getDBTransaction().createPreparedStatement(sqlUPD, 0);
      pstmtUPD.setInt(1, serialId);
      pstmtUPD.executeUpdate();
      getDBTransaction().commit();
      pstmtUPD.close();
      pstmtUPD = null;

      pstmtUPD = getDBTransaction().createPreparedStatement(sqlUPD2, 0);
      pstmtUPD.setInt(1, serialId);
      pstmtUPD.executeUpdate();
      getDBTransaction().commit();
      pstmtUPD.close();
      pstmtUPD = null;
      success = true;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      result = -999;
      success = false;
    }
    finally {
      if (pstmtUPD != null) {
        try {
          pstmtUPD.close();
        }
        catch (Exception ps1) {
        }
      }
    }

    if (success && qty > -1)
      updateNiinLoc(niin_loc_id, niin_id, String.valueOf(qty), iUserId);
    return result;
  }

  public int changeSerialForNiin(int serialId, int niin_id, int niin_loc_id,
                                 String newSerialNumber, int iUserId) {
    int result = 0;

    log.debug("changeSerialForNiin {}, {}, {}", serialId, newSerialNumber, iUserId);

    PreparedStatement pstmtUPD = null;
    try {

      String sqlUPD = "update serial_lot_num_track set serial_number=?, lot_con_num=? where SERIAL_LOT_NUM_TRACK_ID=?";
      pstmtUPD = getDBTransaction().createPreparedStatement(sqlUPD, 0);

      pstmtUPD.setString(1, newSerialNumber);
      pstmtUPD.setString(2, "");
      pstmtUPD.setInt(3, serialId);

      pstmtUPD.executeUpdate();
      getDBTransaction().commit();
      pstmtUPD.close();
      pstmtUPD = null;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      result = -999;
    }
    finally {
      if (pstmtUPD != null) {
        try {
          pstmtUPD.close();
        }
        catch (Exception ps1) {
        }
      }
    }

    return result;
  }

  public int addSerialToNiin(int niin_id, String serial, String cc,
                             int niin_loc_id, int qty, int user_id) {
    int result = 0;
    boolean success = true;

    log.debug("addSerialToNiin {}, {}, {}", niin_id, qty, user_id);
    try {

      String sqlUPD = "insert into serial_lot_num_track (niin_id, serial_number, lot_con_num, cc, expiration_date, issued_flag, timestamp, qty) values(?, ?, ?, ?, ?, 'N', sysdate, ?)";
      try (PreparedStatement pstmtUPD = getDBTransaction().createPreparedStatement(sqlUPD, 0)) {

        pstmtUPD.setInt(1, niin_id);
        pstmtUPD.setString(2, serial);
        pstmtUPD.setString(3, "");
        pstmtUPD.setString(4, cc);
        pstmtUPD.setString(5, "");
        pstmtUPD.setInt(6, 1);

        pstmtUPD.executeUpdate();
        getDBTransaction().commit();
      }

      String sql = "select serial_lot_num_track_id from serial_lot_num_track where niin_id=? and serial_number=?";
      try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(sql, 0)) {

        pstmt.setInt(1, niin_id);
        pstmt.setString(2, serial);
        try (ResultSet rs = pstmt.executeQuery()) {
          if (rs.next()) {
            result = rs.getInt(1);
          }
        }
      }
      success = true;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      result = -999;
      success = false;
    }

    if (success)
      updateNiinLoc(niin_loc_id, niin_id, String.valueOf(qty), user_id);
    return result;
  }

  /**
   * This function populates two lists, one list of niin location id and
   * another list of location labels (names).
   * The lists are used to build select option (af:selectOneChoice)
   *
   * @return HashMap
   */
  public Map<String, String> fillSerialLocationLists(int niinId,
                                                     boolean nonZero) {
    try {
      String sql = "select niin_loc_id, location_label from " + "niin_location n, location l " + "where n.location_id=l.location_id " + "and n.niin_id=?";
      if (nonZero)
        sql += " and qty > 0";

      HashMap<String, String> hm = new HashMap<String, String>();
      try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(sql, 0)) {
        pstmt.setInt(1, niinId);
        try (ResultSet rs = pstmt.executeQuery()) {
          //* populate the hashmap with the returned rows
          while (rs.next()) {
            Object nameObj = rs.getObject(2);
            Object idObj = rs.getObject(1);

            if ((nameObj != null) && (idObj != null)) hm.put(idObj.toString(), nameObj.toString());
          }
        }
      }
      return hm;
    }
    catch (Exception e) {
      log.debug("serial/lot location list error =  {}", e.toString());
    }

    return null;
  }

  /**
   * Container's getter for UsersView1
   */
  public UsersViewImpl getUsersView1() {
    return (UsersViewImpl) findViewObject("UsersView1");
  }

  /**
   * Container's getter for SiteInfoView1
   */
  public SiteInfoViewImpl getSiteInfoView1() {
    return (SiteInfoViewImpl) findViewObject("SiteInfoView1");
  }

  /**
   * Container's getter for RewarehouseTempView1
   */
  public RewarehouseTempViewImpl getRewarehouseTempView1() {
    return (RewarehouseTempViewImpl) findViewObject("RewarehouseTempView1");
  }

  /**
   * Container's getter for NiinSearchView1
   */
  public NiinSearchViewImpl getNiinSearchView1() {
    return (NiinSearchViewImpl) findViewObject("NiinSearchView1");
  }

  /**
   * Container's getter for SiteInterfacesView1
   */
  public SiteInterfacesViewImpl getSiteInterfacesView1() {
    return (SiteInterfacesViewImpl) findViewObject("SiteInterfacesView1");
  }

  /**
   * Container's getter for SiteRemoteConnectionsView1
   */
  public SiteRemoteConnectionsViewImpl getSiteRemoteConnectionsView1() {
    return (SiteRemoteConnectionsViewImpl) findViewObject("SiteRemoteConnectionsView1");
  }

  public void setCurrentniinlist(Object currentniinlist) {
    this.currentniinlist = currentniinlist;
  }

  public Object getCurrentniinlist() {
    return currentniinlist;
  }

  /**
   * Container's getter for NiinInfoWalkThruView1
   */
  public NiinInfoWalkThruViewImpl getNiinInfoWalkThruView1() {
    return (NiinInfoWalkThruViewImpl) findViewObject("NiinInfoWalkThruView1");
  }

  /**
   * Container's getter for NiinLocationWalkThruView1
   */
  public NiinLocationWalkThruViewImpl getNiinLocationWalkThruView1() {
    return (NiinLocationWalkThruViewImpl) findViewObject("NiinLocationWalkThruView1");
  }

  /**
   * Container's getter for WalkThruNIINViewLink1
   */
  public ViewLinkImpl getWalkThruNIINViewLink1() {
    return (ViewLinkImpl) findViewLink("WalkThruNIINViewLink1");
  }

  /**
   * Container's getter for NiinLocationWalkThruView2
   */
  public NiinLocationWalkThruViewImpl getNiinLocationWalkThruView2() {
    return (NiinLocationWalkThruViewImpl) findViewObject("NiinLocationWalkThruView2");
  }

  /**
   * Container's getter for ExceptionView1
   */
  public ExceptionViewImpl getExceptionView1() {
    return (ExceptionViewImpl) findViewObject("ExceptionView1");
  }

  /**
   * Container's getter for RewarehouseTempView2
   */
  public RewarehouseTempViewImpl getRewarehouseTempView2() {
    return (RewarehouseTempViewImpl) findViewObject("RewarehouseTempView2");
  }

  /**
   * Container's getter for StowView1
   */
  public StowViewImpl getStowView1() {
    return (StowViewImpl) findViewObject("StowView1");
  }

  /**
   * Container's getter for RefUiValidView1
   */
  public RefUiValidViewImpl getRefUiValidView1() {
    return (RefUiValidViewImpl) findViewObject("RefUiValidView1");
  }

  /**
   * Container's getter for RefusalBuffer1
   */
  public RefusalBufferImpl getRefusalBuffer1() {
    return (RefusalBufferImpl) findViewObject("RefusalBuffer1");
  }

  /**
   * Container's getter for ReceiptView1
   */
  public ReceiptViewImpl getReceiptView1() {
    return (ReceiptViewImpl) findViewObject("ReceiptView1");
  }

  /**
   * Container's getter for PickExceptionView1
   */
  public PickExceptionViewImpl getpickExceptionView1() {
    return (PickExceptionViewImpl) findViewObject("PickExceptionView1");
  }

  /**
   * Container's getter for PickExceptionView2
   */
  public PickExceptionViewImpl getPickExceptionView2() {
    return (PickExceptionViewImpl) findViewObject("PickExceptionView2");
  }

  /**
   * Container's getter for RefusalBuffer2
   */
  public RefusalBufferImpl getRefusalBuffer2() {
    return (RefusalBufferImpl) findViewObject("RefusalBuffer2");
  }

  /**
   * Container's getter for NiinInfoMATSView1
   */
  public NiinInfoMATSViewImpl getNiinInfoMATSView1() {
    return (NiinInfoMATSViewImpl) findViewObject("NiinInfoMATSView1");
  }

  /**
   * Container's getter for SiteRemoteConnectionsView2
   */
  public SiteRemoteConnectionsViewImpl getSiteRemoteConnectionsView2() {
    return (SiteRemoteConnectionsViewImpl) findViewObject("SiteRemoteConnectionsView2");
  }

  /**
   * Container's getter for exceptionReceipt1
   */
  public ExceptionReceiptImpl getExceptionReceipt1() {
    return (ExceptionReceiptImpl) findViewObject("ExceptionReceipt1");
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
   * Container's getter for RefGbofView1
   */
  public RefGbofViewImpl getRefGbofView1() {
    return (RefGbofViewImpl) findViewObject("RefGbofView1");
  }

  /**
   * Container's getter for RefMatsView1
   */
  public RefMatsViewImpl getRefMatsView1() {
    return (RefMatsViewImpl) findViewObject("RefMatsView1");
  }

  /**
   * Container's getter for RefMhifView1
   */
  public RefMhifViewImpl getRefMhifView1() {
    return (RefMhifViewImpl) findViewObject("RefMhifView1");
  }

  public String checkDuplicateDocNumInIssue(String docNumStr) {
    String retVal = "Error";
    int c = 0;
    String message = "";
    try {
      try (PreparedStatement st = getDBTransaction().createPreparedStatement("select count(*) from issue where document_number = ? ", 0)) {
        st.setString(1, docNumStr);
        try (ResultSet rs = st.executeQuery()) {
          if (rs.next()) {
            c = rs.getInt(1);
          }
        }
      }
      if (c > 0)
        message = "Document Number already in Issue.";
      c = 0;
      try (PreparedStatement st = getDBTransaction().createPreparedStatement("select count(*) from issue_hist where document_number = ? ", 0)) {
        st.setString(1, docNumStr);
        try (ResultSet rs = st.executeQuery()) {
          if (rs.next()) {
            c = rs.getInt(1);
          }
        }
      }
      if ((c > 0) && (message.length() <= 0))
        message = "Document Number already in Issue History.";
      if ((c > 0) && (message.length() > 0))
        message = "Document Number already in Issue and in Issue History.";
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return message;
  }

  public String getSiteGCCSSMCFlag() {
    String flag = "N";
    try (PreparedStatement stR = getDBTransaction().createPreparedStatement("select nvl(GCSS_MC,'N') from site_info", 0)) {
      try (ResultSet rs = stR.executeQuery()) {
        if (rs.next()) {
          flag = rs.getString(1);
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return flag;
  }

  public GCSSMCTransactionsImpl getGCSSMCTransactions1() {
    return getGCSSMCTransactionsService();
  }

  public GCSSMCTransactionsImpl getGCSSMCTransactionsService() {
    try {
      return ((TransactionsImpl) getWorkloadManagerService().getTransactions1()).getGCSSMCTransactions1();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return null;
  }

  public WorkLoadManagerImpl getWorkloadManagerService() {
    try {
      return (WorkLoadManagerImpl) getWorkLoadManager1();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return null;
  }

  /**
   * Container's getter for GCSSMCSiteInterfacesView1
   */
  public GCSSMCSiteInterfacesViewImpl getGCSSMCSiteInterfacesView1() {
    return (GCSSMCSiteInterfacesViewImpl) findViewObject("GCSSMCSiteInterfacesView1");
  }

  /**
   * Container's getter for RefDataloadLogView1
   */
  public RefDataloadLogViewImpl getRefDataloadLogView1() {
    return (RefDataloadLogViewImpl) findViewObject("RefDataloadLogView1");
  }

  /**
   * Container's getter for MatsExceptionView1
   */
  public MatsExceptionViewImpl getMatsExceptionView1() {
    return (MatsExceptionViewImpl) findViewObject("MatsExceptionView1");
  }

  /**
   * Container's getter for niinUIchange1
   */
  public NiinUIchangeImpl getNiinUIchange1() {
    return (NiinUIchangeImpl) findViewObject("NiinUIchange1");
  }

  /**
   * Container's getter for workloadmanager1
   */
  public ApplicationModuleImpl getWorkLoadManager1() {
    return (ApplicationModuleImpl) findApplicationModule("WorkLoadManager1");
  }

  public void generateMassD() {

    try (CallableStatement csOne = getDBTransaction().createCallableStatement("{call RE_POPULATE_SPOOL}", 1)) {
      csOne.execute();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  /**
   * Container's getter for StowDelayView1
   */
  public StowDelayViewImpl getStowDelayView1() {
    return (StowDelayViewImpl) findViewObject("StowDelayView1");
  }

  /**
   * Container's getter for serialPickView1
   */
  public SerialPickViewImpl getserialPickView1() {
    return (SerialPickViewImpl) findViewObject("SerialPickView1");
  }

  public GCSSMCTransactionsImpl getGCSSMCAMService() {
    return getTransactionManagerService().getGCSSMCTransactions1();
  }

  /**
   * Container's getter for PickExceptionView1.
   *
   * @return PickExceptionView1
   */
  public PickExceptionViewImpl getPickExceptionView1() {
    return (PickExceptionViewImpl) findViewObject("PickExceptionView1");
  }

  /**
   * Container's getter for SerialPickView1.
   *
   * @return SerialPickView1
   */
  public SerialPickViewImpl getSerialPickView1() {
    return (SerialPickViewImpl) findViewObject("SerialPickView1");
  }

  /**
   * Container's getter for ExpirationView.
   *
   * @return ExpirationView
   */
  public PickExceptionViewImpl getExpirationView() {
    return (PickExceptionViewImpl) findViewObject("ExpirationView");
  }
}
