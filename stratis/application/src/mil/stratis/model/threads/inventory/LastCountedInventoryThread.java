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
public class LastCountedInventoryThread extends BaseInventoryThread {

  public LastCountedInventoryThread(DBTransaction db, String dbConnectionForUser, AdfDbCtxLookupUtils adfDbCtxLookupUtils) {
    super(db, dbConnectionForUser, adfDbCtxLookupUtils);
  }

  List newcounts;
  int iUserId;
  Object InventoryID;

  public void init(List inputNewCounts, int iUserId, Object InventoryID) {
    setNewCounts(inputNewCounts);
    this.iUserId = iUserId;
    this.InventoryID = InventoryID.toString();

    super.kickOffProcess();
  }

  /**
   * If fleature flag to do performed threaded with object conversions (per stratis.xml context setting),
   * setNewCounts will convert the passed in list of Objects into a simple list of Strings
   * to ensure that there is no connection between the thread and the user's original request context.
   */
  private void setNewCounts(List inputNewCounts) {
    if (super.isFeatFlagThreadedWithObjectConversions()) {
      log.info("performing object conversions");

      this.newcounts = new ArrayList(inputNewCounts.size());

      for (Object item : inputNewCounts) {
        log.debug("putting {} into disconnected list.", item);
        this.newcounts.add(item.toString());
      }
    }
    else {
      //either running in foreground, or running threaded
      // with feature flag for threadedWithObjectConversions set to false.
      // simply use the list that was passed in.

      this.newcounts = new ArrayList(inputNewCounts);
    }
  }

  //* Last Counted returns items which have not been counted in the last x number of days.
  @Override
  protected void doRun() throws SQLException {
    PreparedStatement pstmt = null;
    PreparedStatement pstmtINS = null;
    try {
      log.info("running...Count: {} InventoryId: {}", newcounts.size(), InventoryID.toString());
      setStatus("RUNNING");
      String sql = "select niin_loc_id, niin_id, wac_id, qty, l.location_id " +
          "from niin_location n, location l " +
          "where nvl(n.locked,'N') ='N' and nvl(n.under_audit,'N') ='N' " +
          "and n.qty > 0 " +
          "and n.niin_loc_id not in (select distinct niin_loc_id from inventory_item where inv_type='INVENTORY') " +
          "and n.last_inv_date < (sysdate - ?) " +
          "and n.location_id=l.location_id order by n.last_inv_date ASC";
      pstmt = super.createPreparedStatement(sql, 0);

      String sqlINS = "insert into inventory_item (niin_id, niin_loc_id, inventory_id, num_counts, created_by, created_date, modified_date, inv_type, wac_id, priority, status, niin_loc_qty, location_id) values (?,?,?,0,?,sysdate,sysdate,'INVENTORY',?,1,'INVENTORYPENDING',?,?)";
      pstmtINS = super.createPreparedStatement(sqlINS, 0);
      for (int i = 0; i < newcounts.size(); i++) {
        pstmt.setObject(1, newcounts.get(i));
        try (ResultSet rs = pstmt.executeQuery()) {
          int r = 0;
          log.info("last counted ..  {}", newcounts.get(i).toString());
          while (rs.next()) {
            pstmtINS.setString(1, rs.getString(2));
            pstmtINS.setString(2, rs.getString(1));
            pstmtINS.setInt(3, Integer.parseInt(InventoryID.toString()));
            pstmtINS.setInt(4, iUserId);
            pstmtINS.setString(5, rs.getString(3));
            //* added 1/22/09 to support "freezing" niin location qty
            pstmtINS.setInt(6, rs.getInt(4));
            pstmtINS.setInt(7, rs.getInt(5));

            pstmtINS.execute();
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
      catch (Exception e3) {
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


