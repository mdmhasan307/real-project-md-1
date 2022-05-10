package mil.stratis.model.threads.inventory;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.usmc.mls2.stratis.core.utility.AdfDbCtxLookupUtils;
import oracle.jbo.server.DBTransaction;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * physical button on admin_inventorySchedule.jspx creates the thread
 */
@Slf4j
public class LocSurveyInventoryThread extends BaseInventoryThread {

  public LocSurveyInventoryThread(DBTransaction db, String dbConnectionForUser, AdfDbCtxLookupUtils adfDbCtxLookupUtils) {
    super(db, dbConnectionForUser, adfDbCtxLookupUtils);
  }

  ArrayList<ArrayList> view;
  int iUserId;
  Object InventoryID;

  public void init(int iUserId, Object InventoryID, ArrayList<ArrayList> inputView) {
    setView(inputView);

    this.iUserId = iUserId;
    this.InventoryID = InventoryID.toString();

    super.kickOffProcess();
  }

  /**
   * If fleature flag to do performed threaded with object conversions (per stratis.xml context setting),
   * setView will convert the passed in list of lists of objects into a simple list of list of Strings
   * to ensure that there is no connection between the thread and the user's original request context.
   */
  private void setView(ArrayList<ArrayList> inputView) {
    if (super.isFeatFlagThreadedWithObjectConversions()) {
      log.info("performing object conversions");

      this.view = new ArrayList(inputView.size());

      for (ArrayList list : inputView) {
        ArrayList items = new ArrayList(2);
        items.add(list.get(0).toString());
        items.add(list.get(1).toString());

        log.debug("putting {}, {}, into disconnected list.", items.get(0), items.get(1));
        this.view.add(items);
      }
    }
    else {
      //either running in foreground, or running threaded
      // with feature flag for threadedWithObjectConversions set to false.
      // simply use the list that was passed in.

      this.view = new ArrayList(inputView);
    }
  }

  @Override
  protected void doRun() throws SQLException {
    ArrayList<Object> entry;
    try {
      val inventoryIdStr = InventoryID.toString();
      log.info("running... InventoryId:  {}", inventoryIdStr);
      setStatus("RUNNING");
      String sql = "select niin_id, niin_loc_id, wac_id, qty, l.location_id, location_label " +
          "from niin_location n, location l " +
          "where n.location_id(+)=l.location_id " +
          "and l.location_label between ? and ?";

      // loop threw all the rows and create new inventoryitems for it
      for (ArrayList arrayList : view) {
        entry = arrayList;
        log.info("loc start={} end={}", entry.get(0), entry.get(1));
        int r = 0;

        try (PreparedStatement pstmt = super.createPreparedStatement(sql, 0)) {
          String sqlINS = "insert into inventory_item (niin_id, niin_loc_id, inventory_id, num_counts, created_by, created_date, modified_date, inv_type, wac_id, priority, status, niin_loc_qty, location_id) values (?,?,?,0,?,sysdate,sysdate,'LOCSURVEY',?,1,'LOCSURVEYPENDING',?,?)";
          try (PreparedStatement pstmtINS = super.createPreparedStatement(sqlINS, 0)) {
            pstmt.setString(1, entry.get(0).toString());
            pstmt.setString(2, entry.get(1).toString());
            try (ResultSet rs = pstmt.executeQuery()) {
              while (rs.next()) {
                pstmtINS.setObject(1, rs.getObject(1));
                pstmtINS.setObject(2, rs.getObject(2));
                pstmtINS.setInt(3, Integer.parseInt(inventoryIdStr));
                pstmtINS.setInt(4, iUserId);
                pstmtINS.setObject(5, rs.getObject(3));
                //* added 1/22/09 to support "freezing" niin location qty
                pstmtINS.setObject(6, rs.getObject(4));
                pstmtINS.setObject(7, rs.getObject(5));

                pstmtINS.executeUpdate();
                super.commit();
                log.info("committing...  {}", r++);
              }
            }
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
