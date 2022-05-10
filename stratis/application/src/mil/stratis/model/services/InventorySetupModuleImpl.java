package mil.stratis.model.services;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.common.util.Util;
import mil.stratis.model.threads.inventory.*;
import mil.stratis.model.view.invsetup.*;
import mil.stratis.model.view.site.SiteInfoViewImpl;
import mil.usmc.mls2.stratis.core.utility.AdfDbCtxLookupUtils;
import mil.usmc.mls2.stratis.core.utility.AdfLogUtility;
import mil.usmc.mls2.stratis.core.utility.ContextUtils;
import oracle.jbo.Row;
import oracle.jbo.SessionData;
import oracle.jbo.ViewCriteria;
import oracle.jbo.ViewCriteriaRow;
import oracle.jbo.server.ApplicationModuleImpl;
import oracle.jbo.server.ViewLinkImpl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@NoArgsConstructor //ADF BackingHandlers all need a no args constructor
public class InventorySetupModuleImpl extends ApplicationModuleImpl {

  public static final String INVENTORY_ID = "InventoryId";
  public static final String NIIN_ID = "NiinId";
  public static final String INVENTORY_ITEM_ID = "InventoryItemId";
  public static final String NIIN_LOC_ID = "NiinLocId";
  public static final String LOT_CONTROL_FLAG = "LotControlFlag";
  public static final String INV_TYPE = "InvType";
  public static final String INVENTORY = "INVENTORY";
  public static final String STATUS = "Status";
  private int UserId = 0;
  private int WorkStationId = 0;

  /**
   * function to create an inventory based on last counted date.
   */
  public int createCountInventory(List<String> newcounts, int iUserId, String dbConnectionForUser) {
    int result = 0;
    if (newcounts.size() < 1)
      return result;
    Object InventoryID = null;

    try {
      StringBuilder SelectedCounts = new StringBuilder();
      for (int i = 0; i < newcounts.size(); i++) {
        if (i != 0)
          SelectedCounts.append(", ");
        SelectedCounts.append(newcounts.get(i));
      }
      String DescString = "Selected Count Inventory: " + SelectedCounts + " Days";
      InventoryID = createMasterInventoryRow(iUserId, DescString).toString();

      if (InventoryID != null) {
        result = 1;
        AdfDbCtxLookupUtils adfDbCtxLookupUtils = ContextUtils.getBean(AdfDbCtxLookupUtils.class);
        LastCountedInventoryThread task = new LastCountedInventoryThread(this.getDBTransaction(), dbConnectionForUser, adfDbCtxLookupUtils);
        task.init(newcounts, iUserId, InventoryID);
        Thread.sleep(5000); // 5 sec
      }
    }
    catch (Exception e) {
      this.getDBTransaction().rollback();
      AdfLogUtility.logException(e);
      result *= -1;

      if (InventoryID != null) {
        // delete the master row
        deleteMasterInventoryRow(InventoryID);
      }
    }
    return result;
  }

  private Object createMasterInventoryRow(int iUserId, String descString) {
    Object inventoryID;
    // build the master inventory table
    InventoryViewImpl masterinventory = getInventoryView1();
    Row masterrow = masterinventory.createRow();
    masterrow.setAttribute("Description", descString);
    masterrow.setAttribute("CreatedBy", iUserId);
    Timestamp d = new Timestamp(System.currentTimeMillis());
    masterrow.setAttribute("CreatedDate", d);
    masterrow.setAttribute("RequestDate", d);
    masterrow.setAttribute("ModifiedDate", d);
    masterinventory.insertRow(masterrow);
    this.getDBTransaction().commit();

    masterrow.refresh(Row.REFRESH_WITH_DB_FORGET_CHANGES);
    inventoryID = masterrow.getAttribute(INVENTORY_ID);

    return inventoryID;
  }

  private void deleteMasterInventoryRow(Object InventoryID) {
    try (PreparedStatement indlps = getDBTransaction().createPreparedStatement("delete from inventory where inventory_id= ?", 0)) {
      indlps.setObject(1, InventoryID);
      indlps.executeUpdate();
      indlps.close();
      this.getDBTransaction().commit();
      getInventoryView1().executeQuery();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  private boolean checkPickForTheInventoryItemRow(Object inventoryItemID) {
    boolean last = false;
    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
        "select count(*) from picking p, issue i, inventory_item inv, niin_info nf " +
            "where p.scn = i.scn and i.niin_id = nf.niin_id and i.niin_id = inv.niin_id " +
            "and p.status not in ('SHIPPED', 'PICK REFUSED') and inv.inventory_id " +
            "in (select unique inventory_id from inventory_item where inventory_item_id=?)", 0)) {
      pstmt.setObject(1, inventoryItemID);
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          int count = rs.getInt(1);
          if (count > 0)
            last = true;
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return last;
  }

  private boolean checkLastInventoryItemRow(Object inventoryItemID) {
    boolean last = false;
    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
        "select count(*) from inventory_item where status not in ('LOCSURVEYCOMPLETED') " +
            "and inventory_id= (select inventory_id from inventory_item where inventory_item_id=?)", 0)) {
      pstmt.setObject(1, inventoryItemID);
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          int count = rs.getInt(1);
          if (count < 2)
            last = true;
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return last;
  }

  private boolean allCompletedNIINInventoryItems(Object inventoryItemID) {
    log.debug("inventory item id  {}", inventoryItemID);
    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
        "select count(*) from inventory_item where status " +
            "not in ('INVENTORYCOMPLETED','INVENTORYRELEASED', 'INVENTORYCLOSED', 'INVENTORYCANCELLED') " +
            "and inventory_id=(select inventory_id from inventory_item where inventory_item_id=?)", 0)) {
      pstmt.setObject(1, inventoryItemID);
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          int count = rs.getInt(1);
          if (count > 0)
            return false;
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return true;
  }

  private boolean checkLastNIINInventoryItemRow(Object inventoryItemID) {
    boolean last = false;
    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement("select count(*) from inventory_item where status not in ('INVENTORYCOMPLETED','INVENTORYRELEASED','INVENTORYCLOSED','INVENTORYCANCELLED') and inventory_id=(select inventory_id from inventory_item where inventory_item_id=?)", 0)) {
      pstmt.setObject(1, inventoryItemID);
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          int count = rs.getInt(1);
          if (count < 2)
            last = true;
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return last;
  }

  private boolean mismatchSerialLotNumbers(Object niinID) {
    boolean mismatch = false;
    try {
      try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement("select count(*) from inv_serial_lot_num where (serial_number not in (select serial_number from serial_lot_num_track) or lot_con_num not in (select lot_con_num from serial_lot_num_track)) and niin_id=?", 0)) {
        pstmt.setObject(1, niinID);
        try (ResultSet rs = pstmt.executeQuery()) {
          if (rs.next()) {
            int count = rs.getInt(1);
            if (count > 0)
              mismatch = true;
          }
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return mismatch;
  }

  public boolean isNIINInventory(Object inventoryItemID) {
    boolean niinInventory = false;
    try {
      try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement("select count(*) from inventory i, inventory_item m where i.inventory_id=m.inventory_id and inventory_item_id=? and description like '%NIIN%'", 0)) {
        pstmt.setObject(1, inventoryItemID);
        try (ResultSet rs = pstmt.executeQuery()) {
          if (rs.next()) {
            int count = rs.getInt(1);
            if (count > 0)
              niinInventory = true;
          }
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return niinInventory;
  }

  private boolean isNIINSerialOrLot(Object inventoryItemID) {
    boolean niinSerialOrLot = false;
    try {
      try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
          "select nvl(serial_control_flag, 'N'), nvl(lot_control_flag,'N') " +
              "from inventory_item m, niin_info n where m.niin_id=n.niin_id and inventory_item_id=?", 0)) {
        pstmt.setObject(1, inventoryItemID);
        try (ResultSet rs = pstmt.executeQuery()) {
          if (rs.next()) {
            niinSerialOrLot = rs.getString(1).equals("Y") || rs.getString(2).equals("Y");
          }
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return niinSerialOrLot;
  }

  private boolean isNIINSerialOrLot2(int niinID) {
    boolean niinSerialOrLot = false;
    try {
      try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
          "select nvl(serial_control_flag, 'N'), nvl(lot_control_flag,'N') from niin_info n where niin_id=?", 0)) {
        pstmt.setInt(1, niinID);
        try (ResultSet rs = pstmt.executeQuery()) {
          if (rs.next()) {
            niinSerialOrLot = rs.getString(1).equals("Y") || rs.getString(2).equals("Y");
          }
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return niinSerialOrLot;
  }

  public void fillWACLists(List<String> WACName, List<String> WACID) {
    // ensure the lists are clear
    WACName.clear();
    WACID.clear();

    WacViewImpl view = getWacView1();
    view.setRangeSize(-1);
    view.executeQuery();

    Row[] rows = view.getAllRowsInRange();

    for (Row row : rows) {
      // get the values
      Object Nameobj = row.getAttribute("WacNumber"), IDobj = row.getAttribute("WacId");

      if ((Nameobj != null) && (IDobj != null)) {
        // add them to the lists
        WACName.add(Nameobj.toString());
        WACID.add(IDobj.toString());
      }
    }
  }

  /**
   * function to add all items that will expire in the given days.
   */
  public int addInventoryExpirationDays(List<String> days, int iUserId, String dbConnectionForUser) {
    int result = 0;
    Object InventoryID = null;
    int daycount = days.size();
    if (daycount < 1)
      return result;

    try {
      // the selected days to be added on
      StringBuilder SelectedDays = new StringBuilder();
      for (int i = 0; i < daycount; i++) {
        if (i != 0)
          SelectedDays.append(", ");
        SelectedDays.append(days.get(i));
      }

      // get the master inventory view
      String DescString = "Selected Expiration Inventory: " + SelectedDays + " Days";
      InventoryID = createMasterInventoryRow(iUserId, DescString).toString();

      if (InventoryID != null) {
        result = 1;
        AdfDbCtxLookupUtils adfDbCtxLookupUtils = ContextUtils.getBean(AdfDbCtxLookupUtils.class);
        ByExpDateInventoryThread task = new ByExpDateInventoryThread(this.getDBTransaction(), dbConnectionForUser, adfDbCtxLookupUtils);
        task.init(days, iUserId, InventoryID);
        Thread.sleep(5000); // 5 sec
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      result *= -1;
      this.getDBTransaction().rollback();

      if (InventoryID != null) {
        // delete the master row
        deleteMasterInventoryRow(InventoryID);
      }
    }
    return result;
  }

  /**
   * function to create inventories where expiration dates are on or before the date given.
   */
  public int createInventoryByDate(Object expirationDate, int iUserId, String dbConnectionForUser) {
    int result = 0;
    Object InventoryID = null;
    try {
      String DescString = "Selected Expiration Inventory: " + expirationDate;
      InventoryID = createMasterInventoryRow(iUserId, DescString);

      if (InventoryID != null) {
        result = 1;
        AdfDbCtxLookupUtils adfDbCtxLookupUtils = ContextUtils.getBean(AdfDbCtxLookupUtils.class);
        OnBeforeExpDateInventoryThread task = new OnBeforeExpDateInventoryThread(this.getDBTransaction(), dbConnectionForUser, adfDbCtxLookupUtils);
        task.init(expirationDate, iUserId, InventoryID);
        Thread.sleep(5000); // 5 sec
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      this.getDBTransaction().rollback();
      result *= -1;
      if (InventoryID != null) {
        // delete the master row
        deleteMasterInventoryRow(InventoryID);
      }
    }
    return result;
  }

  /**
   * function to create inventories where expiration dates are on or before the date given.
   */
  public int createInventoryByRangeLocations(Object startLoc, Object endLoc,
                                             int iUserId, String dbConnectionForUser) {
    int result = 0;
    Object InventoryID = null;
    try {
      String DescString = "Selected Locations Inventory: From " + startLoc + " To " + endLoc;
      InventoryID = createMasterInventoryRow(iUserId, DescString);

      if (InventoryID != null) {
        result = 1;
        AdfDbCtxLookupUtils adfDbCtxLookupUtils = ContextUtils.getBean(AdfDbCtxLookupUtils.class);
        ByRangeLocationInventoryThread task = new ByRangeLocationInventoryThread(this.getDBTransaction(), dbConnectionForUser, adfDbCtxLookupUtils);
        task.init(startLoc, endLoc, iUserId, InventoryID);
        Thread.sleep(5000); // 5 sec
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      this.getDBTransaction().rollback();
      result *= -1;
      if (InventoryID != null) {
        // delete the master row
        deleteMasterInventoryRow(InventoryID);
      }
    }
    return result;
  }

  /**
   * function to add all locations in a given wac to the inventory.
   */
  public int addInventoryWACs(List<String> wacs, boolean last360, int iUserId, String dbConnectionForUser) {
    int result = 0;

    // how many wacs are coming in
    int waccount = wacs.size();
    // ensure we are adding inventory requests
    if (waccount < 1 || iUserId < 1)
      return result;
    Object InventoryID = null;

    try {

      StringBuilder SelectedWACS = new StringBuilder();
      // get the lists of WACs with ids so we can match without making another call
      List<String> WACName = new ArrayList<>(), WACID = new ArrayList<>();
      fillWACLists(WACName, WACID);
      for (int i = 0; i < waccount; i++) {
        // get the id
        Object checkwacid = wacs.get(i);
        String WacLabel = "";

        for (int j = 0; j < WACName.size(); j++) {
          // check for match
          if (checkwacid.toString().equals(WACID.get(j))) {
            // copy this wacs name
            WacLabel = WACName.get(j);

            // end this loop, break
            j = WACName.size();
          }
        }

        // add this to the text
        if (i != 0)
          SelectedWACS.append(", ");
        SelectedWACS.append(WacLabel);
      }

      // create the new master inventory row
      String DescString = "Selected WAC Inventory: " + SelectedWACS;
      InventoryID = createMasterInventoryRow(iUserId, DescString);
      if (InventoryID != null) {
        result = 1;
        AdfDbCtxLookupUtils adfDbCtxLookupUtils = ContextUtils.getBean(AdfDbCtxLookupUtils.class);
        ByWacInventoryThread task = new ByWacInventoryThread(this.getDBTransaction(), dbConnectionForUser, adfDbCtxLookupUtils);
        task.init(wacs, last360, iUserId, InventoryID);
        Thread.sleep(5000); // 5 sec
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      this.getDBTransaction().rollback();
      result *= -1;

      if (InventoryID != null) {
        // delete the master row
        deleteMasterInventoryRow(InventoryID);
      }
    }
    return result;
  }

  /**
   * function to build a list of the niins in the niinfilter list.
   */
  public void returnNIINFilterList(List<String> display, List<String> id) {
    try {
      NIINFilterListImpl view = getNIINFilterList1();
      view.setRangeSize(-1);

      // get all the rows in range
      Row[] rows = view.getAllRowsInRange();
      for (Row row : rows) {
        // get the values
        Object nameobj = row.getAttribute("Niin");
        Object idobj = row.getAttribute(NIIN_ID);

        if ((nameobj != null) && (idobj != null)) {
          display.add(nameobj.toString());
          id.add(idobj.toString());
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  /**
   * function to create the niinfilter list used by Inventory Management
   */
  public void filterNIINList(String StartNIIN, String EndNIIN, String CC) {
    try {
      NIINFilterListImpl filterview = getNIINFilterList1();
      filterview.executeQuery();

      // search the niin loc for this one
      NiinRetrieveViewImpl niinloc = this.getNiinRetrieveView1();

      ViewCriteria vcl = niinloc.getViewCriteria("NiinRetrieveViewCriteria");
      niinloc.applyViewCriteria(vcl);
      niinloc.setNamedWhereClauseParam("startNIIN", StartNIIN);
      niinloc.setNamedWhereClauseParam("endNIIN", EndNIIN);
      niinloc.setRangeSize(-1);
      niinloc.executeQuery();

      // if we got any hits add this to the table too
      if (niinloc.getAllRowsInRange().length > 0) {
        for (Row row : niinloc.getAllRowsInRange()) {
          // add it to the view
          Row createme = filterview.createRow();
          createme.setAttribute("Niin", row.getAttribute("Niin"));
          createme.setAttribute(NIIN_ID, row.getAttribute(NIIN_ID));
          createme.setAttribute("CC", CC);
          filterview.insertRow(createme);
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      //* no need to rollback on a temp table
    }
  }

  private String getNiinById(Object id) {
    String niin = "";
    try {
      String sql = "select niin from niin_info where niin_id=?";
      try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(sql, 0)) {
        pstmt.setObject(1, id);
        try (ResultSet rs = pstmt.executeQuery()) {
          if (rs.next()) {
            niin = rs.getString(1);
            if (niin == null)
              niin = "";
          }
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return niin;
  }

  /**
   * function to create inventory based on the niins in the niinaddtable view.
   */
  public int createNIINInventories(List<String> niinIds, int iUserId, String dbConnectionForUser) {
    int result = 0;
    int num = niinIds.size();
    if (num < 1)
      return result;

    List<String> inventories = new ArrayList<>();

    try {

      //* 01-23-09, modify where the NIIN inventory can only be for one NIIN
      for (Object niinid : niinIds) {
        String SelectedCounts = getNiinById(niinid);
        cancelExistNIINInventory(Util.cleanInt(niinid), SelectedCounts);

        String DescString = "Selected NIIN Inventory: " + SelectedCounts;
        Object InventoryID = createMasterInventoryRow(iUserId, DescString).toString();

        if (InventoryID != null) {
          inventories.add(niinid + "," + InventoryID);
        }
      }

      result = 1;
      AdfDbCtxLookupUtils adfDbCtxLookupUtils = ContextUtils.getBean(AdfDbCtxLookupUtils.class);
      ByNiinInventoryThread task = new ByNiinInventoryThread(this.getDBTransaction(), dbConnectionForUser, adfDbCtxLookupUtils);
      task.init(inventories, iUserId);
      Thread.sleep(5000); // 5 sec
    }
    catch (Exception e) {
      this.getDBTransaction().rollback();
      AdfLogUtility.logException(e);
      result *= -1;

      if (inventories.size() > 0) {
        for (Object inventory : inventories) {
          // delete the master row
          deleteMasterInventoryRow(inventory.toString().split(",")[1]);
        }
      }
    }
    return result;
  }

  /**
   * function to cancel any existing inventory by niin for the given niin id.
   * Usually happens when a SUPER NIIN Inventory has been created - which
   * will override any existing NIIN Inventory and cancel any
   * inventory items in other non-NIIN Inventory
   *
   * @createdDate 01-23-2009
   */
  public void cancelExistNIINInventory(int niin_id, String niin) {

    if (Util.isEmpty(niin))
      niin = Util.cleanString(getNiinById(niin_id));

    String findDesc = "%NIIN Inventory%" + niin;
    try {
      try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
          "update inventory set status='CANCELLED' where description like ? ", 0)) {
        if (!Util.isEmpty(niin)) {
          //* cancel other existing NIIN inventory than the one found
          pstmt.setString(1, findDesc);
          pstmt.executeUpdate();
          this.getDBTransaction().commit();
        }
      }
      //* cancel the items of other non-niin inventory
      try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
          "update inventory_item set status='INVENTORYCANCELLED' " +
              "where status in ('INVENTORYPENDING', 'INVENTORYREVIEW') " +
              "and transaction_type is null and inv_type='INVENTORY' and niin_id=? ", 0)) {
        pstmt.setInt(1, niin_id);
        pstmt.executeUpdate();
        this.getDBTransaction().commit();
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      this.getDBTransaction().rollback();
    }
  }

  /**
   * function to return a inventory item to be recounted.
   * An item cannot be recount by same user
   */
  public int submitInventoryItemForRecount(int iUserId) {
    int result = 0;
    Object inventoryItemId = null;

    try {
      // get the current row and send it for recount
      InventoryItemFilterViewImpl view = getInventoryItemFilterView1();
      Row row = view.getCurrentRow();
      Object numcountobj = row.getAttribute("NumCounts");
      int numcount = Util.cleanInt(numcountobj);
      inventoryItemId = row.getAttribute(INVENTORY_ITEM_ID);
      recountNiinLocation(inventoryItemId, numcount);

      this.getDBTransaction().commit();
      view.executeQuery();
      result = 1;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      this.getDBTransaction().rollback();
    }

    log.debug("SubmitInventoryItemForRecount: userId {}, Inv Item Id: {}, result: {}", iUserId, inventoryItemId, result);

    return result;
  }

  private void recountNiinLocation(Object inventoryItemId,
                                   int numCount) throws Exception {
    try (PreparedStatement nlps = getDBTransaction().createPreparedStatement(
        "update inventory_item i set assign_to_user=null, num_counts=?, status='INVENTORYPENDING', " +
            "i.location_id = (select n.location_id from niin_location n where i.niin_loc_id=n.niin_loc_id), " +
            "i.wac_id=(select wac_id from niin_location n, location l where n.location_id=l.location_id " +
            "and i.niin_loc_id=n.niin_loc_id) where inventory_item_id = ?", 0)) {
      nlps.setInt(1, numCount);
      nlps.setObject(2, inventoryItemId);
      nlps.executeUpdate();
    }
  }

  /**
   * This function used to send an inventory item to released and send D8 or D9 transaction if qty mismatch.
   */
  public int submitInventoryItemToHost(int iUserId) {
    int result = 0;
    boolean forceParent = false;
    Object inventoryItemId = null;
    try {
      // get the current row and send it to host
      InventoryItemFilterViewImpl view = getInventoryItemFilterView1();
      result = 1;
      // did we have a gain or a loss
      Row crow = view.getCurrentRow();
      result = 2;
      Object numcounted = crow.getAttribute("NumCounted");
      Object cqty = crow.getAttribute("Qty");
      Object niinlocid = crow.getAttribute(NIIN_LOC_ID);
      Object niinidObj = crow.getAttribute(NIIN_ID);
      Integer niinId = Integer.parseInt(niinidObj.toString());
      inventoryItemId = crow.getAttribute(INVENTORY_ITEM_ID);

      if ((numcounted != null) && (cqty != null)) {
        //* added 01-22-09 to determine if serial or lot
        boolean isSerialOrLot = Util.cleanString(crow.getAttribute("SerialControlFlag")).equalsIgnoreCase("Y") || Util.cleanString(crow.getAttribute(LOT_CONTROL_FLAG)).equalsIgnoreCase("Y");

        // 04-20-2010 Added logic to see if ther are picks to be shipped if so do not allow to release
        //04-23-2014 Added logic so picks needs to be shipped for all niins.
        //if (isSerialOrLot) {
        if (checkPickForTheInventoryItemRow(inventoryItemId)) {
          result = -9;
          return result;
        }
        //}
        // turn them into integers
        int numcountedint = Integer.parseInt(numcounted.toString());
        int cqtyint = Integer.parseInt(cqty.toString());
        int invNiinLocQty = Util.cleanInt(crow.getAttribute("NiinLocQty"));

        //* added 1/05/09 - lock prior to release transaction, and unlock immediately after'
        lockNiinLocation(iUserId, niinlocid);
        getDBTransaction().commit();

        // update the niin loc with the new qty
        try (PreparedStatement st = getDBTransaction().createPreparedStatement(
            "update niin_location set qty = ?, modified_by=?, modified_date=sysdate, " +
                "last_inv_date=sysdate, nsn_remark = 'N' where niin_loc_id = ?", 0)) {
          st.setInt(1, (cqtyint - invNiinLocQty + numcountedint));
          st.setInt(2, iUserId);
          st.setObject(3, niinlocid);
          st.executeUpdate();
          getDBTransaction().commit();
        }

        // call the transaction module for the work
        TransactionsImpl trans = getTransactionsService();
        GCSSMCTransactionsImpl gcssTrans = getGCSSMCTransactionsService();

        // see if we have a gain (D8) or a loss (D9) and not a serial or lot controlled
        if (!isSerialOrLot || gcssTrans.getSiteGCCSSMCFlag().equalsIgnoreCase("N")) {

          if (numcountedint > invNiinLocQty) {
            if (gcssTrans.getSiteGCCSSMCFlag().equalsIgnoreCase("Y"))
              gcssTrans.sendDxxGCSSMCTransaction(niinId, numcountedint - invNiinLocQty, "D8A", String.valueOf(inventoryItemId), null, iUserId);
          }
          else if (numcountedint < invNiinLocQty) {
            if (gcssTrans.getSiteGCCSSMCFlag().equalsIgnoreCase("Y"))
              gcssTrans.sendDxxGCSSMCTransaction(niinId, invNiinLocQty - numcountedint, "D9A", String.valueOf(inventoryItemId), null, iUserId);
          }
        }

        result = 89;

        if (isNIINInventory(inventoryItemId)) {
          //* this check must be done before set to INVENTORYRELEASED
          //* rows will be kicked to history FOR ALL and too late to unlock
          //* Added logic to check if there any picks between pick ready to shipped 04/13/2010
          if (checkLastNIINInventoryItemRow(inventoryItemId)) {
            if (isSerialOrLot && gcssTrans.getSiteGCCSSMCFlag().equalsIgnoreCase("Y"))
              gcssTrans.sendDxxGCSSMCTransaction(niinId, inventoryItemId, null, true, iUserId);
            if (isSerialOrLot && gcssTrans.getSiteGCCSSMCFlag().equalsIgnoreCase("N"))
              trans.processSerialLotForD8D9(crow.getAttribute(INVENTORY_ITEM_ID).toString(), niinId.toString());

            unLockNiinLocationALL(iUserId, inventoryItemId);
            forceParent = true;
          }
        }

        log.debug("here  {}", crow.toString());
        //* added 1/05/09 - lock prior to release transaction, and unlock immediately after
        unLockNiinLocation(iUserId, niinlocid);
        this.getDBTransaction().commit();
        releaseNiinLocation(iUserId, inventoryItemId);
        this.getDBTransaction().commit();
        result = 3;

        //* set parent status
        completeParentInventory(iUserId, inventoryItemId, forceParent);
      }
      else {
        result = 6;
      }

      view.executeQuery();
      result = 1;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      this.getDBTransaction().rollback();
      result *= -1;
    }

    log.debug("SubmitInventoryItemToHost: userId {}, Inv Item Id: {}, result: {}", iUserId, inventoryItemId, result);

    return result;
  }

  public int submitInventoryItemRemoveSurvey(int iUserId) {
    int result = 0;
    Object inventoryItemId;
    Object inventoryId;
    try {
      // get the current row and send it for recount
      InventoryItemFilterViewImpl view = getInventoryItemFilterView1();
      Row row = view.getCurrentRow();
      Object niinLocId = row.getAttribute(NIIN_LOC_ID);
      inventoryItemId = row.getAttribute(INVENTORY_ITEM_ID);
      inventoryId = row.getAttribute(INVENTORY_ID);

      unLockNiinLocation(iUserId, niinLocId);

      // update the database
      this.getDBTransaction().commit();
      view.executeQuery();
      result = 1;

      if (checkLastInventoryItemRow(inventoryItemId)) {
        deleteMasterInventoryRow(inventoryId);

        log.debug("SubmitInventoryItem_RemoveSurvey: did deleteMasterInventoryRow on inventoryId - userId {}, Inv Item Id: {}, Inv Id: {}", iUserId, inventoryItemId, inventoryId);
      }
      else {
        deleteInventoryItem(inventoryItemId);
        this.getDBTransaction().commit();

        log.debug("SubmitInventoryItem_RemoveSurvey: did deleteInventoryItem on inventoryItemId - userId {}, Inv Item Id: {}, Inv Id: {}", iUserId, inventoryItemId, inventoryId);
      }
      view.executeQuery();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      result *= -1;
      this.getDBTransaction().rollback();
    }
    return result;
  }

  public int submitInventoryItemRemoveSurvey(Object niinLocId, Object inventoryItemId, Object inventoryId, int iUserId) {
    int result = 0;
    try {
      if (niinLocId != null)
        unLockNiinLocation(iUserId, niinLocId);
      if (checkLastInventoryItemRow(inventoryItemId)) {
        deleteMasterInventoryRow(inventoryId);
      }
      else {
        deleteInventoryItem(inventoryItemId);
        this.getDBTransaction().commit();
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      this.getDBTransaction().rollback();
    }
    return result;
  }

  public int submitCreateInventoryForSelectedInventoryItem(int iUserId) {
    int result = 0;
    Object InventoryID = null;
    try {
      // get the current row and send it back to inventory for this one only
      InventoryItemFilterViewImpl view = getInventoryItemFilterView1();
      Row row = view.getCurrentRow();
      Object location = row.getAttribute("LocationLabel");
      Object niin = row.getAttribute("Niin");

      String DescString = "Selected NIIN Location Inventory: Location " + location + "; NIIN " + niin;
      InventoryID = createMasterInventoryRow(iUserId, DescString);
      if (InventoryID == null)
        return -888;

      row.setAttribute(INVENTORY_ID, InventoryID);
      row.setAttribute(INV_TYPE, INVENTORY);
      row.setAttribute(STATUS, "INVENTORYPENDING");
      row.setAttribute("NumCounts", 0); //* should this count start over or continue
      row.setAttribute("ModifiedBy", iUserId);
      row.setAttribute("ModifiedDate", new Timestamp(System.currentTimeMillis()));

      // update the database
      this.getDBTransaction().commit();
      view.executeQuery();
      result = 1;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      this.getDBTransaction().rollback();

      if (InventoryID != null)
        deleteMasterInventoryRow(InventoryID);
    }
    return result;
  }

  private void lockNiinLocation(int iUserId,
                                Object niinLocId) throws Exception {
    lockOrUnlockNiinLocation(iUserId, niinLocId, "Y");
  }

  private void unLockNiinLocation(int iUserId,
                                  Object niinLocId) throws Exception {
    lockOrUnlockNiinLocation(iUserId, niinLocId, "N");
  }

  private void lockOrUnlockNiinLocation(int iUserId, Object niinLocId,
                                        String Y_or_N) throws Exception {

    try (PreparedStatement nlps = getDBTransaction().createPreparedStatement(
        "update niin_location set locked = ?, modified_by = ?, modified_date = sysdate where niin_loc_id = ?", 0)) {
      nlps.setString(1, "N");
      nlps.setInt(2, iUserId);
      nlps.setObject(3, niinLocId);
      nlps.executeUpdate();
    }
  }

  private void releaseNiinLocation(int iUserId,
                                   Object inventoryItemId) throws Exception {
    try (PreparedStatement nlps = getDBTransaction().createPreparedStatement(
        "update inventory_item set modified_by = ?, modified_date = sysdate, released_by = ?, " +
            "released_date = sysdate, status='INVENTORYRELEASED' where inventory_item_id = ?", 0)) {
      nlps.setInt(1, iUserId);
      nlps.setInt(2, iUserId);
      nlps.setObject(3, inventoryItemId);
      nlps.executeUpdate();
    }
  }

  public void setLastInvDate(int niinLocId) throws Exception {
    try (PreparedStatement nlps = getDBTransaction().createPreparedStatement(
        "update niin_location set last_inv_date =sysdate where niin_loc_id = ?", 0)) {
      nlps.setInt(1, niinLocId);
      nlps.executeUpdate();
    }
  }

  private void unLockNiinLocationALL(int iUserId,
                                     Object inventoryItemId) throws Exception {
    log.debug("unlock_niinlocation_all  {}", inventoryItemId);
    try (PreparedStatement nlps = getDBTransaction().createPreparedStatement(
        "update niin_location set locked = ?, modified_by = ?, modified_date = sysdate " +
            "where niin_loc_id in (select niin_loc_id from inventory_item where inventory_id= " +
            "(select inventory_id from inventory_item where inventory_item_id=?))", 0)) {
      nlps.setString(1, "N");
      nlps.setInt(2, iUserId);
      nlps.setObject(3, inventoryItemId);
      nlps.executeUpdate();
    }

    // Update INV_DONE_FLAG
    try (PreparedStatement nlps = getDBTransaction().createPreparedStatement(
        "update inv_serial_lot_num set inv_done_flag ='Y', timestamp=sysdate where inventory_item_id in  " +
            " (select iii.inventory_item_id from inventory_item iii where iii.inventory_id " +
            "in (select i.inventory_id from inventory i, inventory_item ix " +
            "where i.inventory_id = ix.inventory_id and ix.inventory_item_id = ?))", 0)) {
      nlps.setString(1, inventoryItemId.toString());
      nlps.executeUpdate();
    }
  }

  private void cancelInventoryItem(Object inventoryItemId) throws Exception {
    try (PreparedStatement nlps = getDBTransaction().createPreparedStatement(
        "update inventory_item set status='INVENTORYCANCELLED' where inv_type='INVENTORY' and inventory_item_id = ?", 0)) {
      nlps.setObject(1, inventoryItemId);
      nlps.executeUpdate();
    }
  }

  private void deleteInventoryItem(Object inventoryItemId) throws Exception {
    try (PreparedStatement nlps = getDBTransaction().createPreparedStatement("delete from inventory_item where inventory_item_id = ?", 0)) {
      nlps.setObject(1, inventoryItemId);
      nlps.executeUpdate();
    }
  }

  /**
   * function to delete a total inventory workload.
   */
  public String deleteSelectedInventory(int iUserId) {
    String result;
    Object inventoryId;
    try {
      InventoryViewImpl view = getInventoryView1();
      Row row = view.getCurrentRow();
      inventoryId = row.getAttribute(INVENTORY_ID);
      result = row.getAttribute("Description").toString();
      if (inventoryId != null) {
        try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
            "update niin_location set locked='N', modified_by=?, modified_date=sysdate " +
                "where niin_loc_id in (select distinct niin_loc_id from inventory_item where inventory_id=?)", 0)) {
          pstmt.setInt(1, iUserId);
          pstmt.setInt(2, Integer.parseInt(inventoryId.toString()));
          pstmt.executeUpdate();
          this.getDBTransaction().commit();
        }
        row.remove();
        this.getDBTransaction().commit();
        view.executeQuery();
      }
    }
    catch (Exception e) {
      result = "ERROR";
      AdfLogUtility.logException(e);
      this.getDBTransaction().rollback();
    }
    return result;
  }

  /**
   * function to delete a inventory location item.
   */
  public String deleteSelectedInventoryItem(int iUserId) {
    String result = "";
    try {
      InventoryItemViewImpl view = getInventoryItemView2();
      //* DO NOT CALL row.remove on this view, it removes cascade style

      // get the current row and delete it
      Row row = view.getCurrentRow();
      Object inventoryItemId = row.getAttribute(INVENTORY_ITEM_ID);
      Object niinLocId = row.getAttribute(NIIN_LOC_ID);
      Object inventoryId = row.getAttribute(INVENTORY_ID);
      Object desc = row.getAttribute(INV_TYPE);
      if (row.getAttribute("LocationLabel") != null) {
        result = row.getAttribute("LocationLabel").toString();
      }

      if (niinLocId != null) {
        //* unlock niin_location, update inventory.description
        unLockNiinLocation(iUserId, niinLocId);
      }

      this.getDBTransaction().commit();
      view.executeQuery();

      if (checkLastInventoryItemRow(inventoryItemId)) {
        log.debug("last inventory item  {}", inventoryItemId.toString());
        deleteMasterInventoryRow(inventoryId);
      }
      else {
        if (Util.cleanString(desc).equals(INVENTORY)) {
          //* added 1/05/09 - since kick to history changed on "inventory",
          //* do a cancel instead of delete
          cancelInventoryItem(inventoryItemId);
        }
        else {
          deleteInventoryItem(inventoryItemId);
        }
        this.getDBTransaction().commit();
        view.executeQuery();
        completeParentInventory(iUserId, inventoryItemId, true);
      }
    }
    catch (Exception e) {
      result = "ERROR";
      AdfLogUtility.logException(e);
      this.getDBTransaction().rollback();
    }

    return result;
  }

  /**
   * function to update status of inventory location item's parent to 'COMPLETED'
   * once child items have completed (INVENTORYCOMPLETED or INVENTORYRELEASED or INVENTORYCLOSED).
   */
  public void completeParentInventory(int iUserId, Object inventoryItemId,
                                      boolean forceParent) {
    try {
      boolean ignoreThresholdDueToSrlOrLot = false;
      //* updated added 11/20/08 - VMB
      //* set ignoreThresholdDueToSrlOrLot flag for gcss mode only and for niin inventory only
      //* in which case you would never do an auto release
      if (!forceParent) {
        ignoreThresholdDueToSrlOrLot = isNIINSerialOrLot(inventoryItemId) && (isNIINInventory(inventoryItemId));
      }

      if (allCompletedNIINInventoryItems(inventoryItemId) && !ignoreThresholdDueToSrlOrLot) {
        log.debug("last inventory item  {}", inventoryItemId.toString());
        String sql = "update inventory set status='COMPLETED', modified_by=?, modified_date=sysdate where inventory_id in (select distinct inventory_id from inventory_item where inventory_item_id=?)";
        try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(sql, 0)) {
          pstmt.setInt(1, iUserId);
          pstmt.setObject(2, inventoryItemId);
          pstmt.executeUpdate();
          this.getDBTransaction().commit();
        }
        this.getDBTransaction().commit();
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      this.getDBTransaction().rollback();
    }
  }

  /**
   * This function builds the list to select from for adding a location survey.
   * It does not automatically add the location survey for you.
   */
  public void addLocationSurveyList(String startLoc, String endLoc) {
    try {
      int count = 0;
      try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
          "select count(*) from location l, niin_location n where l.location_id=n.location_id(+) " +
              "and l.location_label between ? and ?", 0)) {
        pstmt.setString(1, startLoc.toUpperCase());
        pstmt.setString(2, endLoc.toUpperCase());
        try (ResultSet rs = pstmt.executeQuery()) {
          if (rs.next()) {
            count = rs.getInt(1);
          }
        }
      }

      if (count > 0) {
        // get the (temp) view object
        LocSurveyCreateViewImpl view = getLocSurveyCreateView1();
        Row newrow = view.createRow();
        newrow.setAttribute("StartLoc", startLoc);
        newrow.setAttribute("EndLoc", endLoc);
        newrow.setAttribute("TotalLocations", count);
        view.insertRow(newrow);
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  /**
   * function to delete a current row in the temp locsurvey table.
   */
  @SuppressWarnings("unused") //called from Admin_InventorySchedule.jspx
  public void DeleteCurrentRowLocSurvey() {
    try {
      LocSurveyCreateViewImpl view = getLocSurveyCreateView1();
      view.getCurrentRow().remove();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  public void clearLocSurveyRecords() {
    // get the temp locsurvey view
    LocSurveyCreateViewImpl view = getLocSurveyCreateView1();
    view.executeEmptyRowSet();
  }

  /**
   * function to add all the rows in the tmp locsurvey table into the inventory table.
   */
  public int createLocSurveyRecords(int iUserId, boolean multiple, String dbConnectionForUser) {
    Object InventoryID = null;
    int result = 0;
    ArrayList<ArrayList> locations = new ArrayList<>();
    ArrayList<Object> startEndLocs;
    try {
      // get the temp locsurvey view
      LocSurveyCreateViewImpl view = getLocSurveyCreateView1();

      // the string for selected surveys
      StringBuilder SelectedSurveys = new StringBuilder();
      if (multiple) {
        Row[] locrows = view.getAllRowsInRange();
        for (int i = 0; i < locrows.length; i++) {
          Object LocStartobj = locrows[i].getAttribute("StartLoc"), LocEndobj = locrows[i].getAttribute("EndLoc");
          if ((LocStartobj != null) && (LocEndobj != null)) {
            if (i != 0)
              SelectedSurveys.append(", ");
            SelectedSurveys.append(LocStartobj).append(" - ").append(LocEndobj);
            startEndLocs = new ArrayList<>();
            startEndLocs.add(LocStartobj);
            startEndLocs.add(LocEndobj);
            locations.add(startEndLocs);
          }
        }
      }
      else {
        Row locrow = view.getCurrentRow();
        Object LocStartobj = locrow.getAttribute("StartLoc"), LocEndobj = locrow.getAttribute("EndLoc");

        if ((LocStartobj != null) && (LocEndobj != null)) {
          SelectedSurveys.append(LocStartobj).append(" - ").append(LocEndobj);
          startEndLocs = new ArrayList<>();
          startEndLocs.add(LocStartobj);
          startEndLocs.add(LocEndobj);
          locations.add(startEndLocs);
        }
      }

      String DescString = "Selected Location Surveys: " + SelectedSurveys;
      InventoryID = createMasterInventoryRow(iUserId, DescString);

      if (InventoryID != null) {
        result = 1;
        AdfDbCtxLookupUtils adfDbCtxLookupUtils = ContextUtils.getBean(AdfDbCtxLookupUtils.class);
        LocSurveyInventoryThread task = new LocSurveyInventoryThread(this.getDBTransaction(), dbConnectionForUser, adfDbCtxLookupUtils);
        task.init(iUserId, InventoryID, locations); //FUTURE LEGACY passing this list of locations may FAIL - 704.01.01.04
        Thread.sleep(5000); // 5 sec
      }
    }
    catch (Exception e) {
      this.getDBTransaction().rollback();
      AdfLogUtility.logException(e);
      result *= -1;

      if (InventoryID != null) {
        // delete the master row
        deleteMasterInventoryRow(InventoryID);
      }
    }
    return result;
  }

  /**
   * function to get the state of currently selected release row is inventory or locsurvey.
   */
  public boolean getInventoryCompletedType() {
    boolean INVENTORY_TYPE = false;
    InventoryItemFilterViewImpl view = getInventoryItemFilterView1();
    if (view.getCurrentRow() != null) {
      Object currentinvtype = view.getCurrentRow().getAttribute(INV_TYPE);
      if (currentinvtype != null) {
        if (!currentinvtype.toString().equals(INVENTORY)) {
          INVENTORY_TYPE = true;
          // F- inventory, T- loc survey
        }
      }
    }

    return INVENTORY_TYPE;
  }

  /**
   * function to create an inventory by niin for the given niin id.
   * <p>
   * updated 01-23-09 - Will only work to create a NIIN Inventory
   * if there are more than 1 niin locations in the case where it is from another INVENTORY;
   * this rule will not apply if the caller is a LOCATION SURVEY
   */
  public void createNIINInventory(int niin_id, boolean fromAnotherInventory,
                                  boolean fromLocSurvey) {

    String descStr;

    try {
      // FUTURE: Why is this SQL executed twice--here then further down as well?
      String sql = "select niin, niin_loc_id, qty, wac_id, lo.location_id from niin_location l, niin_info n, location lo " + "where l.niin_id=n.niin_id and l.location_id=lo.location_id and n.niin_id=? ";

      //* updated 1/05/09 - allow locked and under_audit for inventory by niin - serial cases are required
      boolean foundOne = false;
      int i = 0;
      String niin = "";
      try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(sql, 0)) {
        pstmt.setInt(1, niin_id);
        try (ResultSet rs = pstmt.executeQuery()) {

          while (rs.next() && i < 3) {
            if (i == 0)
              niin = rs.getString(1);
            if (i == 1)
              foundOne = true; //* means we found more than 1
            i++;
          }
        }
      }

      //* added 02-06-09
      //* inventory by location will not create a niin inventory
      //* if there is only one niin to inventory
      //* except if serial or lot
      boolean isSerialOrLot = isNIINSerialOrLot2(niin_id);
      if (!foundOne && fromAnotherInventory && !isSerialOrLot) {
        //* 01-23-09 no niin inventory to create
        //* there is no need to create a NIIN Inventory when the caller inventory only had one row to inventory
        return;
      }

      //* added 01-22-09, if a NIIN Inventory exist already
      //* 1) more than one day has not passed (same day), then update the NIIN inventory,
      //* 2) otherwise, cancel the existing one (consider outdated)

      boolean create_new = existNIINInventory(niin_id, niin);
      if (Util.isEmpty(niin))
        niin = getNiinById(niin_id);
      String fromDescStr = (fromLocSurvey) ? " (Loc Survey)" : " (Discrepancy)";
      descStr = "STRATIS NIIN Inventory" + fromDescStr + ": " + niin;
      if (create_new && !Util.isEmpty(niin)) {
        log.debug("GAT TESTING: CREATE NEW INVENTORY");
        try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(sql, 0)) {
          pstmt.setInt(1, niin_id);
          try (ResultSet rs = pstmt.executeQuery()) {
            Object inventoryId = createMasterInventoryRow(UserId, descStr);
            while (rs.next()) {

              int niin_loc_id = rs.getInt(2);
              int qty = rs.getInt(3);

              try (PreparedStatement inserter = getDBTransaction().createPreparedStatement(
                  "INSERT INTO INVENTORY_ITEM (NIIN_ID,NIIN_LOC_ID,INVENTORY_ID," +
                      "CREATED_BY,CREATED_DATE,MODIFIED_DATE,INV_TYPE,PRIORITY,STATUS," +
                      "NUM_COUNTS,niin_loc_qty,transaction_type, wac_id, location_id) " +
                      "VALUES (?,?,?,?,sysdate,sysdate,'INVENTORY',1,'INVENTORYPENDING',0,?,'NIN',?,?)", 0)) {
                inserter.setInt(1, niin_id);
                inserter.setInt(2, niin_loc_id);
                inserter.setInt(3, Integer.parseInt(inventoryId.toString()));
                inserter.setInt(4, UserId);
                //* added 1/22/09 to support "freezing" niin location qty
                inserter.setInt(5, qty);
                inserter.setInt(6, rs.getInt(4));
                inserter.setInt(7, rs.getInt(5));

                inserter.executeUpdate();
                this.getDBTransaction().commit();
              }
              catch (Exception e) {
                this.getDBTransaction().rollback();
                AdfLogUtility.logException(e);
              }
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
   * function to update an existing inventory by niin for the given niin id.
   * <p>
   * If a NIIN Inventory exist already and it is for one SINGLE NIIN and has NIIN LOCATION CHILDREN
   * 1) more than one day has not passed (same day only), then update the NIIN inventory,
   * 2) otherwise, cancel the existing one (consider outdated)
   * If there are more than one, the latest will be the one updated and the others canceled
   * <p>
   * Update NIIN Inventory rules
   * 1) INVENTORYPENDING - no change
   * 2) INVENTORYREVIEW - automatically sent back for recount
   * 3) INVENTORYRELEASED - no change
   * 4) INVENTORYCLOSED - will be changed to INVENTORYPENDING
   * 5) INVENTORYCANCELLED - will be changed to INVENTORYPENDING
   * 6) ** any new niin loc id not already in the INVENTORY will be added as INVENTORYPENDING
   * <p>
   * Returns True if a new NIIN inventory needs to be created,
   * True is if there are no NIIN Inventory found to exist
   * for updating which meet the criteria above.
   */
  public boolean existNIINInventory(int niin_id, String niin) {

    if (Util.isEmpty(niin))
      niin = Util.cleanString(getNiinById(niin_id));
    if (Util.isEmpty(niin))
      return false;

    String findDesc = "%NIIN Inventory%" + niin;
    log.debug("does a NIIN Inventory exist already for  {}", findDesc);
    boolean foundOne = false;
    try {
      //* determine if there is a SUPer NIIN Inventory
      //* (this is one which was manually created using Inventory Management Schedule)
      //* only a SUPer NIIN Inventory will have a status of Ready
      //* date is not a factor, but the SUPER has to have niin location children
      int inventory_id = 0;
      try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
          "select inventory_id from inventory where description like ? and status='READY' order by inventory_id desc", 0)) {
        pstmt.setString(1, findDesc);
        try (ResultSet rs = pstmt.executeQuery()) {
          if (rs.next()) {
            foundOne = true;
            inventory_id = rs.getInt(1);
            log.debug("Found [{}] SUPER!!  {}", inventory_id, findDesc);
          }
        }
      }

      //* if a SUPer NIIN Inventory has been located, then we will use that one
      //* all others will be cancelled
      if (!foundOne) {
        String sql = "select inventory_id from inventory where description like ? and trunc(created_date) = trunc(sysdate) order by inventory_id desc";

        try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(sql, 0)) {
          pstmt.setString(1, findDesc);
          try (ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
              foundOne = true;
              inventory_id = rs.getInt(1);
              log.debug("Found [{}]  {}", inventory_id, findDesc);
            }
          }
        }
      }

      //* cancel other existing NIIN inventory than the one found
      String sqlCANCEL = "update inventory set status='CANCELLED' where description like ? ";
      if (foundOne)
        sqlCANCEL += "and inventory_id <> ?";

      try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(sqlCANCEL, 0)) {
        pstmt.setString(1, findDesc);
        if (foundOne)
          pstmt.setInt(2, inventory_id);
        pstmt.executeUpdate();
        this.getDBTransaction().commit();
        log.debug("CANCELLED DUPLICATE?  {}", pstmt.getUpdateCount());
      }

      //* cancel the items of other non-niin inventory
      String sqlCANCEL2 = "update inventory_item set status='INVENTORYCANCELLED' where status in ('INVENTORYPENDING', 'INVENTORYREVIEW') and transaction_type is null and inv_type='INVENTORY' and niin_id=? ";
      if (foundOne)
        sqlCANCEL2 += "and inventory_id <> ?";

      try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(sqlCANCEL2, 0)) {
        pstmt.setInt(1, niin_id);
        if (foundOne)
          pstmt.setInt(2, inventory_id);
        pstmt.executeUpdate();
        this.getDBTransaction().commit();
        log.debug("INVENTORYCANCELLED OTHER NON-NIIN Inventory?  {}", pstmt.getUpdateCount());
      }

      //* begin updating existing NIIN Inventory_Items
      if (foundOne) {
        try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
            "update inventory_item i set status='INVENTORYPENDING', " +
                "i.location_id = (select n.location_id from niin_location n where i.niin_loc_id=n.niin_loc_id), " +
                "i.wac_id=(select wac_id from location l where i.location_id=l.location_id) " +
                "where inventory_id = ? and status in ('INVENTORYCANCELLED', 'INVENTORYCLOSED') and inv_type='INVENTORY' ", 0)) {
          pstmt.setInt(1, inventory_id);
          pstmt.executeUpdate();
          this.getDBTransaction().commit();
          log.debug("Update InventoryCancelled and InventoryClosed to InventoryPending  {}", pstmt.getUpdateCount());
        }

        //* send INVENTORYREVIEW automatically back for recount, do not increase the num counts
        try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
            "update inventory_item i set status='INVENTORYPENDING', " +
                "i.location_id = (select n.location_id from niin_location n where i.niin_loc_id=n.niin_loc_id), " +
                "i.wac_id=(select wac_id from location l where i.location_id=l.location_id) " +
                "where inventory_id = ? and status in ('INVENTORYREVIEW') and inv_type='INVENTORY' ", 0)) {
          pstmt.setInt(1, inventory_id);
          pstmt.executeUpdate();
          this.getDBTransaction().commit();
          log.debug("Update InventoryReview to InventoryPending  {}", pstmt.getUpdateCount());
        }

        //* add in any new niin locations
        try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
            "select niin_id, niin_loc_id, qty, wac_id, l.location_id " +
                "from niin_location n, location l where n.location_id=l.location_id and niin_id=? " +
                "and niin_loc_id not in (select niin_loc_id from inventory_item where inventory_id=? and inv_type='INVENTORY')", 0)) {
          pstmt.setInt(1, niin_id);
          pstmt.setInt(2, inventory_id);
          try (ResultSet rsINS = pstmt.executeQuery()) {
            try (PreparedStatement pstmt2 = getDBTransaction().createPreparedStatement(
                "INSERT INTO INVENTORY_ITEM (NIIN_ID,NIIN_LOC_ID,INVENTORY_ID," +
                    "CREATED_BY,CREATED_DATE,MODIFIED_DATE,INV_TYPE,PRIORITY,STATUS," +
                    "NUM_COUNTS,niin_loc_qty,transaction_type, wac_id, location_id) " +
                    "VALUES (?,?,?,?,sysdate,sysdate,'INVENTORY',1,'INVENTORYPENDING',0,?,'NIN',?, ?)", 0)) {
              while (rsINS.next()) {
                pstmt2.setInt(1, niin_id);
                pstmt2.setInt(2, rsINS.getInt(2));
                pstmt2.setInt(3, inventory_id);
                pstmt2.setInt(4, 1); //* use STRATIS user_id
                //* added 1/22/09 to support "freezing" niin location qty
                pstmt2.setInt(5, rsINS.getInt(3));
                pstmt2.setInt(6, rsINS.getInt(4));
                pstmt2.setInt(7, rsINS.getInt(5));
                log.debug("Add new InventoryPending - inventory_id= {} and niin_loc_id=  {}", inventory_id, rsINS.getInt(2));
                pstmt2.addBatch();
              }
            }
          }
        }
        this.getDBTransaction().commit();
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      this.getDBTransaction().rollback();
    }
    return !foundOne;
  }

  /**
   * Spot Check Rules
   * If the niin_info.INVENTORY_THRESHOLD flag is Y, ignore gain/loss rules + Lock NIIN Location + send to Inv Mgmt
   * If the niin_info.INVENTORY_THRESHOLD flag is N, adhere to the gain/loss rules
   * If the niin is serial or lot and we are in GCSS mode, ignore gain/loss rules (for discrepancy only) + Lock NIIN Location + send to Inv Mgmt
   * <p>
   * Gain/Loss Rules:
   * - if we have a gain <b>below</b> site_info.INVENTORY_THRESHOLD_COUNT or <b>below</b> INVENTORY_THRESHOLD_VALUE, send a D8 + adjust NIIN Location
   * - if we have a loss <b>below</b> site_info.INVENTORY_THRESHOLD_COUNT or <b>below</b> INVENTORY_THRESHOLD_VALUE, send a D9 + adjust NIIN Location
   * <p>
   * - if we have a gain <b>above</b> site_info.INVENTORY_THRESHOLD_COUNT or <b>above</b> INVENTORY_THRESHOLD_VALUE, Lock NIIN Location + send to Inv Mgmt
   * - if we have a loss <b>above</b> site_info.INVENTORY_THRESHOLD_COUNT or <b>above</b> INVENTORY_THRESHOLD_VALUE, Lock NIIN Location + send to Inv Mgmt
   * <p>
   * - if the inventory_threshold_count or inventory_threshold_value are set to 0, then Lock NIIN Location + send to Inv Mgmt
   * <p>
   * This function will return a string of error, otherwise a returns a 1 for success.
   *
   * @param niinlocid - Object value of the niin location row id
   * @param count     - int value of the spot check entered
   * @return String
   */
  public String verifyLocationSpotCheck(Object niinlocid, int count) {
    //* build an error string to return
    StringBuilder errStr = new StringBuilder("VerifyLocationSpotCheck - ");

    int qty;
    boolean hasDiscrepancy;
    boolean ignoreInventoryThreshold;
    try {
      //* get the niin location info
      NiinLocationIdSearchViewImpl niinlocview = getNiinLocationIdSearchView1();
      niinlocview.setNamedWhereClauseParam("SearchId", niinlocid);
      niinlocview.executeQuery();
      if (niinlocview.getRowCount() < 1) {
        return "ERROR - NIIN LOCATION NOT FOUND";
      }
      Row niinlocrow = niinlocview.first();
      Object currentqtyobj = niinlocrow.getAttribute("Qty");
      Object inventoryThresholdObj = niinlocrow.getAttribute("InventoryThreshold");

      String flagSrl = (niinlocrow.getAttribute("SerialControlFlag") == null ? "N" : niinlocrow.getAttribute("SerialControlFlag").toString());
      String flagLot = (niinlocrow.getAttribute(LOT_CONTROL_FLAG) == null ? "N" : niinlocrow.getAttribute(LOT_CONTROL_FLAG).toString());
      String CC = niinlocrow.getAttribute("Cc").toString();

      boolean ignoreThresholdDueToSrlOrLot = (flagSrl.equalsIgnoreCase("Y") || flagLot.equalsIgnoreCase("Y"));

      try {
        qty = Integer.parseInt(currentqtyobj.toString());
      }
      catch (Exception int_e) {
        qty = 0;
      }

      //* check the niin_info.INVENTORY_THRESHOLD flag (Y or N), if Y, then we will use the thresholds
      if (Util.isEmpty(inventoryThresholdObj))
        ignoreInventoryThreshold = false;
      else
        ignoreInventoryThreshold = inventoryThresholdObj.toString().equals("Y");

      //* discrepancy if the count and quantity do not match
      hasDiscrepancy = (count != qty);

      if (hasDiscrepancy) {
        Object currentPriceObj = niinlocrow.getAttribute("Price");
        Object currentNiinObj = niinlocrow.getAttribute("Niin");
        Object currentNiinIdObj = niinlocrow.getAttribute(NIIN_ID);
        Integer currentNiinId = Integer.parseInt(currentNiinIdObj.toString());
        boolean overThreshold = false;

        if (!ignoreInventoryThreshold && !ignoreThresholdDueToSrlOrLot) {
          //* get the threshold values
          SiteInfoViewImpl siteview = getSiteInfoView1();
          Row siterow;
          // objects to hold count and $ thresholds
          double dollarMax = 0.0;
          int countMax = 0;
          if (siteview.getRowCount() > 0) {
            siterow = siteview.first();
            Object countMaxObj = siterow.getAttribute("InventoryThresholdCount");
            try {
              countMax = Integer.parseInt(countMaxObj.toString());
            }
            catch (Exception cme) {
              countMax = 0;
            }

            Object dollarMaxObj = siterow.getAttribute("InventoryThresholdValue");
            try {
              dollarMax = Double.parseDouble(dollarMaxObj.toString());
            }
            catch (Exception dme) {
              dollarMax = 0.0;
            }
          }

          if (countMax == 0 || dollarMax == 0.0)
            overThreshold = true;

          int diffAmount;
          if (qty > count) {
            diffAmount = qty - count;
          }
          else {
            diffAmount = count - qty;
          }

          //* check if the count is over the threshold
          if (diffAmount > countMax)
            overThreshold = true;

          //* check the dollar threshold
          // check if the diff price is greater than what is allowed
          double diffOut = diffAmount * Double.parseDouble(currentPriceObj.toString());
          if (diffOut > dollarMax)
            overThreshold = true;

          if (!overThreshold) {
            //* discrepancy below threshold and inventory_threshold flag is N
            // what are we sending, T=gain (D8), F=loss (D9)
            boolean useD8 = (qty < count);

            // add to the spool
            // call the transaction module for the work

            GCSSMCTransactionsImpl gcssTrans = getGCSSMCTransactionsService();
            //* added 1/05/09 - lock prior to release transaction, and unlock immediately after'
            lockNiinLocation(UserId, niinlocid);
            getDBTransaction().commit();

            // update the niin loc with the new qty
            try (PreparedStatement st = getDBTransaction().createPreparedStatement(
                "update niin_location set qty = ?, modified_by=?, modified_date=sysdate where niin_loc_id = ?", 0)) {
              st.setObject(1, count);
              st.setInt(2, UserId);
              st.setObject(3, niinlocid);
              st.executeUpdate();
              getDBTransaction().commit();
            }

            val dicCode = useD8 ? "D8A" : "D9A";

            if (gcssTrans.getSiteGCCSSMCFlag().equalsIgnoreCase("Y"))
              gcssTrans.sendDxxGCSSMCTransaction(currentNiinId, diffAmount, dicCode, "0", CC, UserId);

            unLockNiinLocation(UserId, niinlocid);
          }
        }

        //* discrepancies above threshold and discrepancies for NIINs that are flagged Y
        if (ignoreInventoryThreshold || overThreshold || ignoreThresholdDueToSrlOrLot) {
          //* auto lock niin location and send to inv mgmt
          // lock the given location, send the buffer via status
          // create an inventory by location and then set it to INVENTORYCOMPLETED so that it will show up in the queue
          errStr.append("Creating Inventory Rows ").append(currentNiinObj).append("-- ");
          this.createNIINInventory(Util.cleanInt(currentNiinIdObj), false, false);
        }
        this.getDBTransaction().commit();
      }
      //* do nothing, there are no discrepancies

      // check if we have no items left at this location
      if (count == 0) {
        lockNiinLocation(UserId, niinlocid);
        this.getDBTransaction().commit();
        errStr.append("Creating Pick row - ");
        // check the picking table for any pending picks on this location & reassign them
        try (PreparedStatement pickps = getDBTransaction().createPreparedStatement(
            "select p.scn, nl.niin_id, p.pid from picking p, niin_location nl " +
                "where p.niin_loc_id = nl.niin_loc_id and nl.niin_loc_id = ? and status = 'PICK READY'", 0)) {
          pickps.setObject(1, niinlocid);
          try (ResultSet pickrs = pickps.executeQuery()) {
            WorkLoadManagerImpl workload = getWorkloadManagerService();
            while (pickrs.next()) {
              //This should automatically re-assign any pending picks to other locations that have available quantities
              //or that are not locked.   If there are none, they are re-added to picking with status of PICK_REFUSE
              errStr.append("Creating Pick row -" + "Calling returnBestPickLocation-").append(pickrs.getObject(2)).append(' ').append(pickrs.getObject(3));
              workload.returnBestPickLocation(null, pickrs.getObject(2), pickrs.getObject(3), "A");
            }
          }
        }
      }

      //* no need to commit here

    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      this.getDBTransaction().rollback();
      return "INV_SPOT_CHK_ERROR - Error - Got exception." + errStr + "---" + e.getMessage();
    }
    return "1";
  }

  /**
   * function to decide how an inventory will be set based on thresholds.
   */
  public void verifyInventoryCount(Object inventoryitemid) {
    int qty;
    boolean hasDiscrepancy;
    boolean ignoreInventoryThreshold;
    boolean forceParent = false;
    try {
      // get the inventory item view
      InventoryItemViewImpl inventoryitem = getInventoryItemView1();

      // clear all filters first
      inventoryitem.applyViewCriteria(null);

      ViewCriteria vc = inventoryitem.createViewCriteria();
      ViewCriteriaRow vcr = vc.createViewCriteriaRow();
      vcr.setUpperColumns(true);

      // set the inventory item id
      vcr.setAttribute(INVENTORY_ITEM_ID, "= " + inventoryitemid);

      // execute the query and add the results to the inventory tables
      vc.addElement(vcr);

      inventoryitem.applyViewCriteria(vc);
      inventoryitem.executeQuery();
      if (inventoryitem.getRowCount() > 0) {
        // get the first row we have
        Row inventoryrow = inventoryitem.first();
        boolean ignoreThresholdDueToSrlOrLot;
        String flagSrl = (inventoryrow.getAttribute("SerialControlFlag") == null ? "N" : inventoryrow.getAttribute("SerialControlFlag").toString());
        String flagLot = (inventoryrow.getAttribute(LOT_CONTROL_FLAG) == null ? "N" : inventoryrow.getAttribute(LOT_CONTROL_FLAG).toString());

        // get the count, niinlocid, niinid,
        Object countobj = inventoryrow.getAttribute("NumCounted");
        Object niinlocid = inventoryrow.getAttribute(NIIN_LOC_ID);
        Object inventoryItemId = inventoryrow.getAttribute(INVENTORY_ITEM_ID);
        Object niinLocQty = inventoryrow.getAttribute("NiinLocQty");
        int sendForRecountNumbers = Util.cleanInt(inventoryrow.getAttribute("NumCounts"));
        int count = 0;

        //* updated added 11/20/08 - VMB
        //* set ignoreThresholdDueToSrlOrLot flag for gcss mode only and for niin inventory only
        //* in which case you would never do an auto release

        ignoreThresholdDueToSrlOrLot = (flagSrl.equalsIgnoreCase("Y") || flagLot.equalsIgnoreCase("Y"));

        if (ignoreThresholdDueToSrlOrLot && isNIINInventory(inventoryItemId)) {
          log.debug("verifying inventory count - ignore threshold serial or lot");
          inventoryrow.setAttribute(STATUS, "INVENTORYREVIEW"); // always review this case

          //* 03-15-2011 calculate the cumulative positive or negative adjustments
          int pos = 0;
          int neg = 0;
          int adj = Util.cleanInt(countobj) - Util.cleanInt(niinLocQty);
          if (adj >= 0)
            pos = adj;
          else
            neg = -(adj);

          inventoryrow.setAttribute("CumPosAdj", pos);
          inventoryrow.setAttribute("CumNegAdj", neg);
        }
        else {

          if (countobj != null)
            count = Integer.parseInt(countobj.toString());

          //* get the niin location info
          NiinLocationIdSearchViewImpl niinlocview = getNiinLocationIdSearchView1();
          niinlocview.setNamedWhereClauseParam("SearchId", niinlocid);
          niinlocview.executeQuery();
          niinlocview.getRowCount();
          Row niinlocrow = niinlocview.first();
          Object currentqtyobj = niinlocrow.getAttribute("Qty");
          Object inventoryThresholdObj = niinlocrow.getAttribute("InventoryThreshold");
          Object currentNiinIdObj = niinlocrow.getAttribute(NIIN_ID);
          Integer currentNiinId = Integer.parseInt(currentNiinIdObj.toString());
          try {
            qty = Integer.parseInt(currentqtyobj.toString());
          }
          catch (Exception int_e) {
            qty = 0;
          }

          //* check the niin_info.INVENTORY_THRESHOLD flag (Y or N), if Y, then we will use the thresholds
          if (Util.isEmpty(inventoryThresholdObj))
            ignoreInventoryThreshold = false;
          else
            ignoreInventoryThreshold = inventoryThresholdObj.toString().equals("Y");

          //* discrepancy if the count and quantity do not match
          hasDiscrepancy = (count != qty);

          if (hasDiscrepancy || sendForRecountNumbers > 0) {

            Object currentPriceObj = niinlocrow.getAttribute("Price");

            boolean overThreshold = false;
            if (!ignoreInventoryThreshold && !ignoreThresholdDueToSrlOrLot && sendForRecountNumbers < 1) {

              log.debug("compute for autogenerate d8/d9");
              //* get the threshold values
              SiteInfoViewImpl siteview = getSiteInfoView1();
              Row siterow;
              // objects to hold count and $ thresholds
              double dollarMax = 0.0;
              int countMax = 0;
              if (siteview.getRowCount() > 0) {
                siterow = siteview.first();
                Object countMaxObj = siterow.getAttribute("InventoryThresholdCount");
                try {
                  countMax = Integer.parseInt(countMaxObj.toString());
                }
                catch (Exception cme) {
                  countMax = 0;
                }

                Object dollarMaxObj = siterow.getAttribute("InventoryThresholdValue");
                try {
                  dollarMax = Double.parseDouble(dollarMaxObj.toString());
                }
                catch (Exception dme) {
                  dollarMax = 0.0;
                }
              }

              if (countMax == 0 || dollarMax == 0.0)
                overThreshold = true;

              int diffAmount;
              if (qty > count) {
                diffAmount = qty - count;
              }
              else {
                diffAmount = count - qty;
              }

              //* check if the count is over the threshold
              if (diffAmount > countMax)
                overThreshold = true;

              //* check the dollar threshold
              // check if the diff price is greater than what is allowed
              double diffOut = diffAmount * Double.parseDouble(currentPriceObj.toString());
              if (diffOut > dollarMax)
                overThreshold = true;

              if (!overThreshold) {
                //* discrepancy below threshold and inventory_threshold flag is N
                // what are we sending, T=gain (D8), F=loss (D9)
                boolean useD8 = (qty < count);
                //* added 1/05/09 - lock prior to release transaction, and unlock immediately after'
                lockNiinLocation(UserId, niinlocid);
                getDBTransaction().commit();

                // update the niin loc with the new qty
                try (PreparedStatement st = getDBTransaction().createPreparedStatement(
                    "update niin_location set qty = ?, modified_by=?, modified_date=sysdate where niin_loc_id = ?", 0)) {
                  st.setObject(1, count);
                  st.setInt(2, UserId);
                  st.setObject(3, niinlocid);
                  st.executeUpdate();
                  getDBTransaction().commit();
                }

                // add to the spool
                // call the transaction module for the work
                GCSSMCTransactionsImpl gcssTrans = getGCSSMCTransactionsService();

                val dicCode = useD8 ? "D8A" : "D9A";
                if (isNIINInventory(inventoryItemId)) {
                  //* this check must be done before set to INVENTORYRELEASED
                  //* rows will be kicked to history FOR ALL and too late to unlock

                  if (checkLastNIINInventoryItemRow(inventoryItemId)) {
                    if (gcssTrans.getSiteGCCSSMCFlag().equalsIgnoreCase("Y"))
                      gcssTrans.sendDxxGCSSMCTransaction(currentNiinId, diffAmount, dicCode, String.valueOf(inventoryItemId), null, UserId);

                    unLockNiinLocationALL(UserId, inventoryItemId);
                    forceParent = true;
                  }
                }
                else {
                  if (gcssTrans.getSiteGCCSSMCFlag().equalsIgnoreCase("Y"))
                    gcssTrans.sendDxxGCSSMCTransaction(currentNiinId, diffAmount, dicCode, String.valueOf(inventoryItemId), null, UserId);
                }
                //* added 1/05/09 - lock prior to release transaction, and unlock immediately after
                unLockNiinLocation(UserId, niinlocid);
                inventoryrow.setAttribute(STATUS, "INVENTORYRELEASED");
                inventoryrow.setAttribute("ReleasedBy", 1);
                inventoryrow.setAttribute("ReleasedDate", new Timestamp(System.currentTimeMillis()));
              }
            }

            if (ignoreThresholdDueToSrlOrLot && !isNIINInventory(inventoryItemId)) {
              inventoryrow.setAttribute(STATUS, "INVENTORYCLOSED"); // not released, but should have spawned a new niin inventory
            }
            else {
              //* discrepancies above threshold and discrepancies for NIINs that are flagged Y
              if (ignoreInventoryThreshold || overThreshold || ignoreThresholdDueToSrlOrLot || sendForRecountNumbers > 0) {

                // lock the given location, send the buffer via status
                // create an inventory by location and then set it to INVENTORYREVIEW so that it will show up in the queue
                inventoryrow.setAttribute(STATUS, "INVENTORYREVIEW"); // not released

                //* 01-22-09 calculate the cumulative positive or negative adjustments
                int pos = 0;
                int neg = 0;
                int adj = Util.cleanInt(countobj) - Util.cleanInt(niinLocQty);
                if (adj >= 0)
                  pos = adj;
                else
                  neg = -(adj);

                inventoryrow.setAttribute("CumPosAdj", pos);
                inventoryrow.setAttribute("CumNegAdj", neg);
              }
            }
          }
          else {

            //* do nothing, there are no discrepancies
            if (isNIINInventory(inventoryItemId)) {
              //* this check must be done before set to INVENTORYRELEASED
              //* rows will be kicked to history FOR ALL and too late to unlock
              //* added check for picking rows
              if (checkLastNIINInventoryItemRow(inventoryItemId)) {
                unLockNiinLocationALL(UserId, inventoryItemId);
                forceParent = true;
              }
            }
            unLockNiinLocation(UserId, niinlocid);

            inventoryrow.setAttribute(STATUS, "INVENTORYCOMPLETED");
            //* not really released, but since no discrepancy - must clear the table

          }
        }
        inventoryrow.setAttribute("ModifiedBy", UserId);
        inventoryrow.setAttribute("ModifiedDate", new Timestamp(System.currentTimeMillis()));
        this.getDBTransaction().commit();

        //* set the parent status
        completeParentInventory(UserId, inventoryItemId, forceParent);
      }

      // clear the filter
      inventoryitem.applyViewCriteria(null);

      // update the query
      inventoryitem.executeQuery();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      this.getDBTransaction().rollback();
    }
  }

  /**
   * Container's getter for WacView1
   */
  public WacViewImpl getWacView1() {
    return (WacViewImpl) findViewObject("WacView1");
  }

  /**
   * Container's getter for NIINAddTable1
   */
  public NIINAddTableImpl getNIINAddTable1() {
    return (NIINAddTableImpl) findViewObject("NIINAddTable1");
  }

  /**
   * Container's getter for NIINFilterList1
   */
  public NIINFilterListImpl getNIINFilterList1() {
    return (NIINFilterListImpl) findViewObject("NIINFilterList1");
  }

  /**
   * Container's getter for InventoryItemFilterView1
   */
  public InventoryItemFilterViewImpl getInventoryItemFilterView1() {
    return (InventoryItemFilterViewImpl) findViewObject("InventoryItemFilterView1");
  }

  /**
   * Container's getter for InventoryItemView1
   */
  public InventoryItemViewImpl getInventoryItemView1() {
    return (InventoryItemViewImpl) findViewObject("InventoryItemView1");
  }

  /**
   * Container's getter for InventoryItemLink1
   */
  public ViewLinkImpl getInventoryItemLink1() {
    return (ViewLinkImpl) findViewLink("InventoryItemLink1");
  }

  /**
   * Container's getter for LocSurveyCreateView1
   */
  public LocSurveyCreateViewImpl getLocSurveyCreateView1() {
    return (LocSurveyCreateViewImpl) findViewObject("LocSurveyCreateView1");
  }

  public void prepareSession(SessionData sessionData) {
    super.prepareSession(sessionData);
  }

  public void setUserId(int userId) {
    this.UserId = userId;
  }

  public int getUserId() {
    return UserId;
  }

  public void setWorkStationId(int workStationId) {
    this.WorkStationId = workStationId;
  }

  public int getWorkStationId() {
    return WorkStationId;
  }

  /**
   * Container's getter for NiinLocationIdSearchView1
   */
  public NiinLocationIdSearchViewImpl getNiinLocationIdSearchView1() {
    return (NiinLocationIdSearchViewImpl) findViewObject("NiinLocationIdSearchView1");
  }

  /**
   * Container's getter for SiteInfoView1
   */
  public SiteInfoViewImpl getSiteInfoView1() {
    return (SiteInfoViewImpl) findViewObject("SiteInfoView1");
  }

  /**
   * Container's getter for NiinRetrieveView1
   */
  public NiinRetrieveViewImpl getNiinRetrieveView1() {
    return (NiinRetrieveViewImpl) findViewObject("NiinRetrieveView1");
  }

  // Return serial flag from niin_info

  public String getSerialControlFlag(String niinid) {
    String flag = "N";
    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
        "select nvl(serial_control_flag,'N') from niin_info where niin_id = ?", 0)) {
      pstmt.setString(1, niinid);
      try (ResultSet rs = pstmt.executeQuery()) {
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

  // Return serial flag from niin_info

  public String getLotControlFlag(String niinid) {
    String flag = "N";
    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
        "select nvl(Lot_control_flag,'N') from niin_info where niin_id = ?", 0)) {
      pstmt.setString(1, niinid);
      try (ResultSet rs = pstmt.executeQuery()) {
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

  public GCSSMCTransactionsImpl getGCSSMCTransactionsService() {
    try {
      GCSSMCTransactionsImpl service;
      service = ((TransactionsImpl) getWorkloadManagerService().getTransactions1()).getGCSSMCTransactions1();
      return service;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return null;
  }

  public TransactionsImpl getTransactionsService() {
    try {
      return (TransactionsImpl) getWorkloadManagerService().getTransactions1();
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
   * Container's getter for WorkLoadManager1
   */
  public ApplicationModuleImpl getWorkLoadManager1() {
    return (ApplicationModuleImpl) findApplicationModule("WorkLoadManager1");
  }

  /**
   * Container's getter for InventoryView2.
   *
   * @return InventoryView2
   */
  public InventoryViewImpl getInventoryView1() {
    return (InventoryViewImpl) findViewObject("InventoryView1");
  }

  /**
   * Container's getter for InventoryItemView3.
   *
   * @return InventoryItemView3
   */
  public InventoryItemViewImpl getInventoryItemView2() {
    return (InventoryItemViewImpl) findViewObject("InventoryItemView2");
  }

  /**
   * Container's getter for InventoryItemViewLink.
   *
   * @return InventoryItemViewLink
   */
  public ViewLinkImpl getInventoryItemViewLink() {
    return (ViewLinkImpl) findViewLink("InventoryItemViewLink");
  }
}
