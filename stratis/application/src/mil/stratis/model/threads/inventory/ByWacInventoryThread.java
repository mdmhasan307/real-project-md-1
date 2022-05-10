package mil.stratis.model.threads.inventory;

import lombok.extern.slf4j.Slf4j;
import mil.usmc.mls2.stratis.core.utility.AdfDbCtxLookupUtils;
import oracle.jbo.server.DBTransaction;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * physical button on admin_inventorySchedule.jspx creates the thread
 */
@Slf4j
public class ByWacInventoryThread extends BaseInventoryThread {

  public ByWacInventoryThread(DBTransaction db, String dbConnectionForUser, AdfDbCtxLookupUtils adfDbCtxLookupUtils) {
    super(db, dbConnectionForUser, adfDbCtxLookupUtils);
  }

  List wacs;
  int iUserId;
  Object InventoryID;
  boolean last360;

  public void init(List Wacs, boolean last360, int iUserId, Object InventoryID) {
    this.setWacs(Wacs);

    this.last360 = last360;
    this.iUserId = iUserId;
    this.InventoryID = InventoryID.toString();

    super.kickOffProcess();
  }

  /**
   * If fleature flag to do performed threaded with object conversions (per stratis.xml context setting),
   * setWacs will convert the passed in list of Objects into a simple list of Strings
   * to ensure that there is no connection between the thread and the user's original request context.
   */
  private void setWacs(List Wacs) {
    log.info("Wacs count:  {}", Wacs.size());

    if (super.isFeatFlagThreadedWithObjectConversions()) {
      log.info("performing object conversions");
      this.wacs = new ArrayList(Wacs.size());

      for (Object item : Wacs) {
        log.debug("putting {} into disconnected list.", item);
        this.wacs.add(item.toString());
      }
    }
    else {
      //either running in foreground, or running threaded
      // with feature flag for threadedWithObjectConversions set to false.
      // simply use the list that was passed in.

      this.wacs = new ArrayList(Wacs);
    }
  }

  @Override
  protected void doRun() throws SQLException {
    PreparedStatement pstmt = null;
    PreparedStatement pstmtINS = null;
    try {
      log.info("wacs.size:  {}", wacs.size());

      int waccount = wacs.size();
      int wacid = 0;
      log.info("running...{} {}", waccount, InventoryID.toString());
      setStatus("RUNNING");
      String sql = "select niin_id, niin_loc_id, wac_id, qty, l.location_id " +
          "from niin_location n, location l " +
          "where nvl(n.locked,'N') ='N' and nvl(n.under_audit,'N') ='N' " +
          "and n.niin_loc_id not in (select distinct niin_loc_id from inventory_item where inv_type='INVENTORY') " +
          "and n.location_id=l.location_id and l.wac_id=? ";
      if (last360)
        sql += "and n.last_inv_date between (sysdate - 360) and sysdate ";

      pstmt = super.createPreparedStatement(sql, 0);

      String sqlINS = "insert into inventory_item (niin_id, niin_loc_id, inventory_id, num_counts, created_by, created_date, modified_date, inv_type, wac_id, priority, status, niin_loc_qty, location_id) values (?,?,?,0,?,sysdate,sysdate,'INVENTORY',?,1,'INVENTORYPENDING',?,?)";
      pstmtINS = super.createPreparedStatement(sqlINS, 0);
      for (int i = 0; i < waccount; i++) {
        wacid = Integer.parseInt(wacs.get(i).toString());
        pstmt.setInt(1, wacid);
        log.info("wac..  {}", wacs.get(i).toString());
        int r = 0;

        try (ResultSet rs = pstmt.executeQuery()) {
          while (rs.next()) {
            pstmtINS.setString(1, rs.getString(1));
            pstmtINS.setString(2, rs.getString(2));
            pstmtINS.setInt(3, Integer.parseInt(InventoryID.toString()));
            pstmtINS.setInt(4, iUserId);
            pstmtINS.setString(5, rs.getString(3));
            pstmtINS.setInt(6, rs.getInt(4));
            pstmtINS.setInt(7, rs.getInt(5));

            pstmtINS.executeUpdate();
            super.commit();
            log.info("committing...  {}", r++);
          }
        }
      }
      setStatus("READY");
    }
    catch (Exception e1) {
      log.warn("Exception during processing! ", e1);
      super.rollback();
      setStatus("FAILED");
    }
    finally {
      try {
        if (pstmt != null) pstmt.close();
      }
      catch (Exception e2) {
      }
      try {
        if (pstmtINS != null) pstmtINS.close();
      }
      catch (Exception e2) {
      }
    }
  }

  private void setStatus(String status) throws SQLException {
    String sql = "update inventory set status=? where inventory_id=?";
    try (PreparedStatement pstmt = super.createPreparedStatement(sql, 0)) {
      pstmt.setString(1, status);
      pstmt.setInt(2, Integer.parseInt(InventoryID.toString()));
      pstmt.executeUpdate();
      super.commit();
    }
    catch (Exception e) {
      super.rollback();
    }
  }
}
