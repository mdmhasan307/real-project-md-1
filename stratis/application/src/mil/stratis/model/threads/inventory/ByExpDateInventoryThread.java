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
public class ByExpDateInventoryThread extends BaseInventoryThread {

  public ByExpDateInventoryThread(DBTransaction db, String dbConnectionForUser, AdfDbCtxLookupUtils adfDbCtxLookupUtils) {
    super(db, dbConnectionForUser, adfDbCtxLookupUtils);
  }

  List daycounts;
  int iUserId;
  Object InventoryID;

  public void init(List daycounts, int iUserId, Object InventoryID) {
    this.daycounts = daycounts;
    this.iUserId = iUserId;
    this.InventoryID = InventoryID.toString();

    super.kickOffProcess();
  }

  @Override
  protected void doRun() throws SQLException {
    PreparedStatement pstmt = null;
    PreparedStatement pstmtINS = null;
    try {

      //(NiinLocation.EXPIRATION_DATE BETWEEN SYSDATE AND (SYSDATE + :DateVar + 1))

      log.info("running... Count: {} InventoryId: {}", daycounts.size(), InventoryID.toString());
      setStatus("RUNNING");
      String sql = "select niin_id, niin_loc_id, wac_id, qty, l.location_id " +
          "from niin_location n, location l " +
          "where n.niin_loc_id not in " +
          "(select distinct niin_loc_id from inventory_item where inv_type='INVENTORY') " +
          "and n.qty > 0 " +
          "and n.expiration_date between sysdate and (sysdate + ? + 1) " +
          "and n.location_id=l.location_id";

      pstmt = super.createPreparedStatement(sql, 0);

      String sqlINS = "insert into inventory_item (niin_id, niin_loc_id, inventory_id, num_counts, created_by, created_date, modified_date, inv_type, wac_id, priority, status, niin_loc_qty, location_id) values (?,?,?,0,?, sysdate, sysdate, 'INVENTORY',?,1,'INVENTORYPENDING',?,?)";
      pstmtINS = super.createPreparedStatement(sqlINS, 0);
      for (int i = 0; i < daycounts.size(); i++) {
        pstmt.setObject(1, daycounts.get(i));
        try (ResultSet rs = pstmt.executeQuery()) {
          int r = 0;
          log.info("expiration date ..  {}", daycounts.get(i).toString());
          while (rs.next()) {
            pstmtINS.setString(1, rs.getString(1));
            pstmtINS.setString(2, rs.getString(2));
            pstmtINS.setInt(3, Integer.parseInt(InventoryID.toString()));
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
      }
      setStatus("READY");
    }
    catch (Exception e1) {
      log.warn("Exception during processing! ", e1);
      setStatus("FAILED");
      super.rollback();
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
      pstmt.setObject(2, InventoryID);
      pstmt.executeUpdate();
      super.commit();
    }
    catch (Exception e) {
      super.rollback();
    }
  }
}
