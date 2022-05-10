package mil.stratis.model.threads.inventory;

import lombok.extern.slf4j.Slf4j;
import mil.usmc.mls2.stratis.core.utility.AdfDbCtxLookupUtils;
import oracle.jbo.server.DBTransaction;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * physical button on admin_inventorySchedule.jspx creates the thread
 */
@Slf4j
public class ByNiinInventoryThread extends BaseInventoryThread {

  public ByNiinInventoryThread(DBTransaction db, String dbConnectionForUser, AdfDbCtxLookupUtils adfDbCtxLookupUtils) {
    super(db, dbConnectionForUser, adfDbCtxLookupUtils);
  }

  List inventories;
  int iUserId;

  public void init(List inventories, int iUserId) {
    this.inventories = inventories;
    this.iUserId = iUserId;

    super.kickOffProcess();
  }

  @Override
  protected void doRun() throws SQLException {
    PreparedStatement pstmt = null;
    PreparedStatement pstmtINS = null;
    int count = inventories.size();
    log.info("running...Count:  {}", count);

    try {
      String sql = "select niin_id, niin_loc_id, wac_id, qty, l.location_id " +
          "from niin_location n, location l " +
          " where n.location_id=l.location_id and n.niin_id=?";
      pstmt = super.createPreparedStatement(sql, 0);

      String sqlINS = "insert into inventory_item (niin_id, niin_loc_id, inventory_id, num_counts, created_by, created_date, modified_date, inv_type, wac_id, priority, status, transaction_type, niin_loc_qty, location_id) values (?,?,?,0,?,sysdate,sysdate,'INVENTORY',?,1,'INVENTORYPENDING','SUP',?,?)";
      pstmtINS = super.createPreparedStatement(sqlINS, 0);
      for (int i = 0; i < count; i++) {
        String niin_id = inventories.get(i).toString().split(",")[0];
        String inventory_id = inventories.get(i).toString().split(",")[1];

        setStatus("RUNNING", inventory_id);
        pstmt.setObject(1, niin_id);
        try (ResultSet rs = pstmt.executeQuery()) {
          log.info("niin..  {}", niin_id);
          int r = 0;
          while (rs.next()) {
            pstmtINS.setString(1, rs.getString(1));
            pstmtINS.setString(2, rs.getString(2));
            pstmtINS.setString(3, inventory_id);
            pstmtINS.setInt(4, iUserId);
            pstmtINS.setString(5, rs.getString(3));
            //* added 1/22/09 to support "freezing" niin location qty
            pstmtINS.setInt(6, rs.getInt(4));
            pstmtINS.setInt(7, rs.getInt(5));

            pstmtINS.executeUpdate();

            super.commit();
            log.info("committing...  {}", r++);
          }
        }
        setStatus("READY", inventory_id);
      }
    }
    catch (Exception e1) {
      super.rollback();
      log.warn("Exception during processing! ", e1);
      for (int m = 0; m < count; m++) {
        setStatus("FAILED", inventories.get(m).toString().split(",")[1]);
      }
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

  private void setStatus(String status, String InventoryID) throws SQLException {
    String sql = "update inventory set status=? where inventory_id=?";
    try (PreparedStatement pstmt = super.createPreparedStatement(sql, 0)) {
      pstmt.setString(1, status);
      pstmt.setInt(2, Integer.parseInt(InventoryID));
      pstmt.executeUpdate();
      super.commit();
    }
    catch (Exception e) {
      super.rollback();
    }
  }
}
