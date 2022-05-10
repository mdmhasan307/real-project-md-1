package mil.stratis.model.services;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.common.util.RegUtils;
import mil.stratis.common.util.StringUtil;
import mil.stratis.common.util.Util;
import mil.stratis.model.datatype.ship.ShippingBarcode;
import mil.stratis.model.datatype.ship.ShippingContainer;
import mil.stratis.model.datatype.ship.ShippingFloor;
import mil.stratis.model.datatype.ship.ShippingItem;
import mil.stratis.model.view.rcv.ReceiptViewImpl;
import mil.stratis.model.view.ship.*;
import mil.usmc.mls2.stratis.common.domain.exception.FakeException;
import mil.usmc.mls2.stratis.core.utility.AdfLogUtility;
import mil.usmc.mls2.stratis.core.utility.AmsCmosUtils;
import mil.usmc.mls2.stratis.core.utility.ContextUtils;
import oracle.jbo.Row;
import oracle.jbo.server.ApplicationModuleImpl;
import oracle.jbo.server.ViewObjectImpl;
import org.apache.commons.lang3.StringUtils;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

@Slf4j
@NoArgsConstructor //ADF ModuleImpls all need a no args constructor
public class ShippingServiceImpl extends ApplicationModuleImpl {

  public void refreshShippingHomeReports(int wId) {
    try {
      UnprocessBarcodeRptViewImpl view = getUnprocessBarcodeRptView1();
      view.setNamedWhereClauseParam("EQUIPMENT_NUMBER", wId);
      view.executeQuery();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  /**
   * Builds an object of shipping barcode information to be used in validation.
   *
   * @return ShippingBarcode
   * @see mil.stratis.model.datatype.ship.ShippingBarcode
   */
  public ShippingBarcode validateContainerIdentifier(String txtContainerIdentifier) {
    return buildShippingBarcode(txtContainerIdentifier);
  }

  /**
   * Builds an object of shipping floor information to be used in validation.
   *
   * @return ShippingFloor
   * @see mil.stratis.model.datatype.ship.ShippingFloor
   */
  public ShippingFloor validateStageLocator(String txtStageLocator, int workstationId) {
    return buildShippingFloor(txtStageLocator, workstationId, true);
  }

  /**
   * Builds an object of shipping barcode information.
   *
   * @return ShippingBarcode
   */
  public ShippingBarcode buildShippingBarcode(String barcodeOrTcn) {
    ShippingBarcode shippingBarcode = new ShippingBarcode();

    //* build the ShippingBarcode object to return
    // does the barcode exist, get the view
    try (PreparedStatement barcodeps = this.getDBTransaction().createPreparedStatement("SELECT " + "    s.SHIPPING_ID, " +
        "    s.SHIPPING_MANIFEST_ID, " +
        "    s.PACKING_CONSOLIDATION_ID, " +
        "    p.CONSOLIDATION_BARCODE, " + "    s.TCN, " +
        "    m.LEAD_TCN, " + "    m.MANIFEST, " +
        "    m.CUSTOMER_ID, " + "    c.AAC, " +
        "    m.FLOOR_LOCATION_ID, " +
        "    f.FLOOR_LOCATION, " + "    s.LAST_REVIEW_DATE, " +
        "    s.SCN, " + "    ic.AAC AS SCN_AAC, " +
        "    p.CUSTOMER_ID AS PACKING_CUSTOMER_ID, " +
        "    m.EQUIPMENT_NUMBER " +
        "FROM ((SHIPPING s INNER JOIN (ISSUE i INNER JOIN CUSTOMER ic ON i.CUSTOMER_ID = ic.CUSTOMER_ID) ON s.SCN = i.SCN) " +
        "INNER JOIN PACKING_CONSOLIDATION p ON s.PACKING_CONSOLIDATION_ID = p.PACKING_CONSOLIDATION_ID) " +
        "LEFT OUTER JOIN ((SHIPPING_MANIFEST m INNER JOIN CUSTOMER c ON m.CUSTOMER_ID = c.CUSTOMER_ID) INNER JOIN FLOOR_LOCATION f ON m.FLOOR_LOCATION_ID = f.FLOOR_LOCATION_ID) ON s.SHIPPING_MANIFEST_ID = m.SHIPPING_MANIFEST_ID " +
        "WHERE consolidation_barcode = ?", 0)) {

      barcodeps.setString(1, barcodeOrTcn);
      try (ResultSet rs = barcodeps.executeQuery()) {
        int i = 0;
        while (rs.next()) {
          ShippingItem item = new ShippingItem();
          item.setShippingId(StringUtil.cleanInt(rs.getString(1)));
          item.setShippingManifestId(StringUtil.cleanInt(rs.getString(2)));
          item.setTcn(StringUtil.clean(rs.getString(5)));
          item.setPackingConsolidationId(StringUtil.cleanInt(rs.getString(3)));
          item.setScn(StringUtil.clean(rs.getString(13)));
          item.setLeadTcn(StringUtil.clean(rs.getString(6)));
          item.setLdcon(StringUtil.clean(rs.getString(7)));
          item.setCustomerId(StringUtil.cleanInt(rs.getString(8)));
          item.setAac(StringUtil.clean(rs.getString(9)));
          item.setFloorLocationId(StringUtil.cleanInt(rs.getString(10)));
          item.setFloorLocation(StringUtil.clean(rs.getString(11)));
          item.setWorkstationId(StringUtil.cleanInt(rs.getString(16)));

          if (i == 0) {
            shippingBarcode.setBarcode(StringUtil.trimUpperCaseClean(rs.getString(4)));
            shippingBarcode.setPackingConsolidationId(StringUtil.cleanInt(rs.getString(3)));
            shippingBarcode.setCustomerId(StringUtil.cleanInt(rs.getString(15)));
            shippingBarcode.setWorkstationId(StringUtil.cleanInt(rs.getString(16)));
            shippingBarcode.setWarehouseId(StringUtil.cleanInt(getWarehouseIdForWorkstation(shippingBarcode.getWorkstationId())));
            shippingBarcode.setItem(item);
            i++;
          }
          shippingBarcode.addShippingItem(item);
        }
      }
    }
    catch (Exception e) {
      log.error("Error in Shipping Barcode {} in {}", barcodeOrTcn, e.getStackTrace()[0].getMethodName(), e);
    }
    log.debug("buildShippingBarcode {}", shippingBarcode);
    return shippingBarcode;
  }

  /**
   * Builds an object of shipping floor information.
   *
   * @return ShippingFloor
   */
  public ShippingFloor buildShippingFloor(String floorLocationOrLeadTcn, int workstationId, boolean useWarehouse) {
    log.debug("build shipping floor {} workstationId= {} warehouse {}", floorLocationOrLeadTcn, workstationId, useWarehouse);
    ShippingFloor shippingFloor = findFloorLocation(floorLocationOrLeadTcn, workstationId, useWarehouse);
    int floorLocationId = shippingFloor.getFloorLocationId();
    if (floorLocationId > 0) {
      //* get the Lead TCN or floor Location record(s)
      try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
          "select shipping_manifest_id, aac, c.CUSTOMER_ID, Lead_TCN, manifest_date, manifest, manifest_print_date, equipment_number " +
              "from Shipping_Manifest m, Customer c " +
              "where m.customer_id=c.customer_id " +
              "and (m.lead_tcn=? or m.floor_location_id=?)", 0)) {
        pstmt.setString(1, floorLocationOrLeadTcn);
        pstmt.setInt(2, floorLocationId);
        try (ResultSet rs = pstmt.executeQuery()) {
          if (rs.next()) {
            ShippingContainer shippingContainer = new ShippingContainer();
            shippingContainer.setShippingManifestId(rs.getInt(1));
            shippingContainer.setAac(rs.getString(2));
            shippingContainer.setCustomerId(rs.getInt(3));
            shippingContainer.setLeadTcn(rs.getString(4));

            shippingContainer.setFloorLocation(shippingFloor.getFloorLocation());
            shippingContainer.setFloorLocationId(shippingFloor.getFloorLocationId());
            shippingContainer.setManifestDate(rs.getString(5));
            shippingContainer.setLdcon(rs.getString(6));
            shippingContainer.setManifestPrintDate(rs.getString(7));
            shippingFloor.addShippingContainer(shippingContainer);

            shippingFloor.setWorkstationId(rs.getInt(8));
            shippingFloor.setContainer(shippingContainer);
          }
        }
      }
      catch (Exception e) {
        AdfLogUtility.logException(e);
      }
    }

    log.trace("buildShippingFloor {}", shippingFloor);
    return shippingFloor;
  }

  /**
   * This function will return a HashMap of String of floor locations with an order number.
   */
  public Map<Integer, String> getBestFloorLocations(int customerId, int workstationId) {
    HashMap<Integer, String> hm = new HashMap<>();
    int order = 1;
    String sqlParamClause = "";
    //* get current floor of customer, if one exists
    log.trace("getting best customer floor location for {} {}", customerId, workstationId);
    String floorLocation = getCustomerCurrentFloorLocation(customerId, workstationId);
    if (!StringUtil.isEmpty(floorLocation)) {
      hm.put(order++, floorLocation);
      sqlParamClause = "and floor_location <> ?";
    }

    String warehouseId = getWorkstationWarehouseId(String.valueOf(workstationId));

    //* get customer default floor locations, if any exists
    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
        "select distinct floor_location from (select floor_location " + "from cust_floor_location c, floor_location f " +
            "where c.floor_location_id=f.floor_location_id " +
            "and c.customer_id=? and warehouse_id=? and nvl(in_use, 'N') <> 'Y' " + sqlParamClause +
            "order by cust_floor_location_id)", 0)) {

      //*  not in shipping_manifest algorithm for customer and shipping area
      pstmt.setInt(1, customerId);
      pstmt.setString(2, warehouseId);
      if (!StringUtil.isEmpty(sqlParamClause))
        pstmt.setString(3, floorLocation);

      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          String floor = rs.getString(1);
          if (!StringUtil.isEmpty(floor)) {
            hm.put(order++, floor);
          }
        }
      }
    }
    catch (SQLException e) {
      AdfLogUtility.logException(e);
    }

    if (order < 5) {
      //* get warehouse default floor locations, if any exists
      try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement("select floor_location " + "from floor_location " +
          "where warehouse_id=? and nvl(in_use, 'N') <> 'Y' " +
          "and floor_location_id not in " +
          "(select nvl(floor_location_id,'') as floor_location_id from shipping_manifest where equipment_number " +
          "in (select equipment_number from equip where warehouse_id=?)) " +
          "and floor_location_id not in " +
          "(select distinct nvl(floor_location_id,'') as floor_location_id from cust_floor_location) " +
          "and rownum < 11 " + "order by floor_location", 0)) {
        pstmt.setString(1, warehouseId);
        pstmt.setString(2, warehouseId);

        try (ResultSet rs = pstmt.executeQuery()) {
          while (rs.next() && order < 6) {
            String floor = rs.getString(1);
            if (!StringUtil.isEmpty(floor)) {
              hm.put(order++, floor);
            }
          }
        }
      }
      catch (SQLException sqle) {
        AdfLogUtility.logException(sqle);
      }
    }
    return hm;
  }

  /**
   * Get the customer's current floor location.  It is dependent on
   * the current workstation used and its associated shipping area.
   */
  public String getCustomerCurrentFloorLocation(int customerId, int workstationId) {
    String currentFloorLocationId = "";
    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
        "select floor_location, m.floor_location_id " +
            "from shipping_manifest m, floor_location f " +
            "where m.floor_location_id=f.floor_location_id and customer_id=? " + "and nvl(in_use, 'N') <> 'Y' " +
            "and equipment_number in " +
            "(select equipment_number from equip where description='Shipping Station' " +
            "and warehouse_id=(select warehouse_id from equip where description='Shipping Station' and equipment_number=?) " +
            "and nvl(shipping_area,'NA') = (select nvl(shipping_area, 'NA') from equip where description='Shipping Station' and equipment_number=?)) " +
            "and rownum<=1", 0)) {
      //* find match for the floor location
      //* with the same customer
      pstmt.setInt(1, customerId);
      pstmt.setInt(2, workstationId);
      pstmt.setInt(3, workstationId);

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          currentFloorLocationId = rs.getString(1);
        }
      }
    }
    catch (SQLException sqle) {
      AdfLogUtility.logException(sqle);
    }
    return currentFloorLocationId;
  }

  /**
   * This method will make calls to addToLocation to assign material to a floor location
   * on a case by case basis.  First Case - if the Lead Tcn entered for review
   * is not manifested and shares the same customer as the material wish to assign,
   * then add to location.  Second Case - get the best shipping manifest location
   * to assign to. If one does not exist, then create the Shipping Manifest row
   * using ShippingManifestView and then add to location.
   * Thid Case (Last) - if one exist, then add to location.
   * 1st level of errors (# greater than 0 - new shippingManifestId, 0 - default, -1 - exception, -999 - AS1 failed)
   * 2nd level of errors (-777 - exception occurred ShippingManifestView row insert, -888 - shipping manifest id not created)
   */
  public int assignMaterial(ShippingBarcode shippingBarcode, ShippingFloor shippingFloor, int iUserId,
                            int iWorkstationId) {

    int success = 0;

    //* process assignment based on Floor Location to be Reviewed
    String shippingManifestId =
        getShippingManifestId(shippingFloor.getFloorLocationId(), shippingBarcode.getCustomerId(), iWorkstationId);

    if (StringUtil.isEmpty(shippingManifestId)) {

      String localDeliverySuffix = generateLDCONByWarehouse(getWarehouseIdForWorkstation(iWorkstationId));
      //* shippingManifestId is empty, means no matches found for unmanifested, same customer, same floor
      ShippingManifestViewImpl view = null;
      try {
        //* create a new row in shipping_manifest and update the shipping table to the new shippingManifestId
        view = getShippingManifestView1();
        Row row = view.createRow();
        row.setAttribute("LeadTcn", shippingBarcode.getItem().getTcn());
        row.setAttribute("FloorLocationId", shippingFloor.getFloorLocationId());
        row.setAttribute("CustomerId", shippingBarcode.getCustomerId());
        //* give it a manifest LDCON number, but no date
        row.setAttribute("Manifest", localDeliverySuffix);

        row.setAttribute("PickedUpFlag", "N");
        row.setAttribute("DeliveredFlag", "N");
        row.setAttribute("CreatedBy", iUserId);
        row.setAttribute("CreatedDate", new Date(System.currentTimeMillis()));
        row.setAttribute("EquipmentNumber", iWorkstationId);
        view.insertRow(row);
        view.getDBTransaction().commit();

        //* refresh the row to get the ShippingManifestId generated by database insert
        row.refresh(Row.REFRESH_WITH_DB_FORGET_CHANGES);
        Object smidObj = row.getAttribute("ShippingManifestId");
        if (smidObj != null) {
          shippingManifestId = smidObj.toString();
          success = 1;
        }
        else {
          success = -888;
          throw new FakeException("Fake exception used to trigger catch below");
        }

        log.debug("new row in shipping_manifest shippingManifestId =  {}", shippingManifestId);
      }
      catch (Exception e) {
        if (view != null)
          view.getDBTransaction().rollback();
        AdfLogUtility.logException(e);
        if (success != -888)
          success = -777;
      }
    }

    //* return now if there are errors
    if (success < 0) {
      log.debug("there were errors  {}", success);
      return success;
    }

    List<String> shippingItems = new ArrayList<>();
    int result;
    //* find match for the floor location consolidating to
    //* with the same customer (and where its not the same shipping manifest)
    //* get the shipping manifest id where floor and the customer id
    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
        "select shipping_id from shipping where packing_consolidation_id " +
            "in (select packing_consolidation_id from shipping where shipping_id=?) ", 0)) {
      pstmt.setInt(1, shippingBarcode.getItem().getShippingId());
      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          shippingItems.add(rs.getString(1));
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }

    //* add to this location
    for (String shippingId : shippingItems) {
      result = addToLocation(Integer.parseInt(shippingId), shippingManifestId, iUserId, iWorkstationId);
      if (result < 0) {
        return result;
      }
    }

    return Integer.parseInt(shippingManifestId);
  }

  private String getShippingManifestId(int floorLocationId, int customerId, int workstationId) {
    String shippingManifestId = "";

    //* find match for the floor location consolidating to
    //* with the same customer (and where its not the same shipping manifest)
    //* get the shipping manifest id where floor and the customer id
    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
        "select shipping_manifest_id from shipping_manifest m " +
            "where manifest_date IS NULL and floor_location_id=? and customer_id=? and " +
            "equipment_number in (select equipment_number from equip where nvl(shipping_area,'NA') = " +
            "(select nvl(shipping_area, 'NA') from equip where equipment_number=?)) ", 0)) {
      pstmt.setInt(1, floorLocationId);
      pstmt.setInt(2, customerId);
      pstmt.setInt(3, workstationId);

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          shippingManifestId = rs.getString(1);
        }
      }
    }
    catch (SQLException sqle) {
      AdfLogUtility.logException(sqle);
    }
    return shippingManifestId;
  }

  /**
   * Add a shipping row to an existing shipping floor location.
   * Send to Workload Manager an AS1 request.
   * errors (0 - default, 1 - success, -1 - exception, -999 - AS1 failed)
   */
  private int addToLocation(int shippingId, String shippingManifestId, int iUserId, int iWorkstationId) {
    log.debug("associate shipping row {} to shipping_manifest {}", shippingId, shippingManifestId);
    int success;
    //* ShippingManifestId is not empty, found a match, proceed to assign

    String issueType = "";
    try (PreparedStatement pstmtUpdateShipping = getDBTransaction().createPreparedStatement(
        "update Shipping set shipping_manifest_id=?, modified_by=?, modified_date=?, equipment_number=? " +
            "where shipping_id=?", 0)) {

      Date currDate = new Date(System.currentTimeMillis());

      //* update the shipping table to this shippingManifestId
      pstmtUpdateShipping.setString(1, shippingManifestId);
      pstmtUpdateShipping.setInt(2, iUserId);
      pstmtUpdateShipping.setDate(3, currDate);
      pstmtUpdateShipping.setInt(4, iWorkstationId);
      pstmtUpdateShipping.setInt(5, shippingId);
      pstmtUpdateShipping.executeUpdate();
      getDBTransaction().commit();
      //* send AS1 when floor associated with shipping row

      int result = 0;
      try (PreparedStatement pstmtGetIssueTypes = getDBTransaction().createPreparedStatement(
          "select nvl(issue_type,'') from issue " +
              "where scn IN (select scn from shipping where shipping_id = ?)", 0)) {

        pstmtGetIssueTypes.setInt(1, shippingId);

        //* send AS1 when floor associated with shipping row
        try (ResultSet rs = pstmtGetIssueTypes.executeQuery()) {
          if (rs.next())
            issueType = rs.getString(1);
        }
      }

      if (issueType == null)
        issueType = "";

      if (!issueType.equalsIgnoreCase("W"))
        result = sendToWorkloadBySCN(String.valueOf(shippingId), 1);

      if (result >= 0 || result == -111) {
        log.debug("AS1 sent with result  {}", result);

        //* update the status in issue table for the scn
        try (PreparedStatement pstmtUpdateIssueStatus = getDBTransaction().createPreparedStatement(
            "update issue set status='SHIPPING', modified_by=?, modified_date=? " +
                "where scn in (select scn from shipping where shipping_id=?)", 0)) {
          pstmtUpdateIssueStatus.setInt(1, iUserId);
          pstmtUpdateIssueStatus.setDate(2, currDate);
          pstmtUpdateIssueStatus.setInt(3, shippingId);
          pstmtUpdateIssueStatus.executeUpdate();
        }
        //* update the status in issue table for the scn
        try (PreparedStatement pstmtUpdatePickingStatus = getDBTransaction().createPreparedStatement(
            "update picking set status='SHIPPING', modified_by=?, modified_date=? " +
                "where scn in (select scn from shipping where shipping_id=?)", 0)) {
          pstmtUpdatePickingStatus.setInt(1, iUserId);
          pstmtUpdatePickingStatus.setDate(2, currDate);
          pstmtUpdatePickingStatus.setInt(3, shippingId);
          pstmtUpdatePickingStatus.executeUpdate();
          getDBTransaction().commit();
          success = 1;
        }
      }
      else {
        //* undo shipping update
        pstmtUpdateShipping.setString(1, "");
        pstmtUpdateShipping.setInt(2, iUserId);
        pstmtUpdateShipping.setDate(3, currDate);
        pstmtUpdateShipping.setInt(4, shippingId);
        pstmtUpdateShipping.executeUpdate();
        getDBTransaction().commit();
        success = -999;
      }
    }
    catch (Exception e) {
      getDBTransaction().rollback();
      AdfLogUtility.logException(e);
      success = -1;
    }
    return success;
  }

  /**
   * This function updates the database table Shipping_Manifest to
   * reflect container(s) consolidated.
   *
   * @param shippingManifestId         - this is the current container selected
   * @param customerId                 - this is the current container's aac (id)
   * @param selectedConsolidateToValue - consolidation adding to - this is a floor location id
   * @param userId                     - user id of current logged in user
   * @return int
   */
  public int addContainerToConsolidation(String shippingManifestId, String customerId,
                                         String selectedConsolidateToValue, int userId) {
    int success = -1;
    try {
      //* find (unmanifested) match for the floor location consolidating to
      //* with the same customer (and where its not the same shipping manifest)
      //* get the shipping manifest id where floor and the customer id
      String existingShippingManifestId =
          getExistConsolidate(selectedConsolidateToValue, customerId, shippingManifestId);

      if (StringUtils.isNotBlank(existingShippingManifestId)) {
        //* if there exist 1 shipping manifest id already with same floor and customer,
        //* then we have to use it by reassign the selected container
        //* to the new Lead TCN (existing) and deleting the old

        if (reassignShippingIds(shippingManifestId, existingShippingManifestId, userId)) {
          deleteOldShippingManifestId(shippingManifestId);
          success = 2;
        }
      }
      else {
        //* if there are no matching shipping manifest rows with same floor and customer,
        //* then we make the selected container the Lead TCN for the floor and customer
        //* by just changing the floor location
        if (changeFloor(shippingManifestId, selectedConsolidateToValue, userId))
          success = 1;
      }
    }
    catch (Exception e) {
      log.debug("add consolidation error =  {}", e.getMessage());
      success = 0;
    }
    return success;
  }

  /**
   * This method returns any existing Shipping Manifest Id where
   * the customer and floor are the same as the floor consolidating to.
   * The floor consolidating too must not be manifested.
   *
   * @return String
   */
  private String getExistConsolidate(String selectedConsolidateToValue, String customerId,
                                     String shippingManifestId) {
    String existingShippingManifestId = "";

    //* find match for the floor location consolidating to
    //* with the same customer (and where its not the same shipping manifest)
    //* get the shipping manifest id where floor and the customer id
    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
        "select shipping_manifest_id from shipping_manifest " + "where floor_location_id=? and customer_id=? " +
            "and shipping_manifest_id<>? and manifest_date IS NULL and rownum<=1", 0)) {

      pstmt.setString(1, selectedConsolidateToValue);
      pstmt.setString(2, customerId);
      pstmt.setString(3, shippingManifestId);

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          existingShippingManifestId = rs.getString(1);
        }
      }
    }
    catch (SQLException sqle) {
      AdfLogUtility.logException(sqle);
    }
    return existingShippingManifestId;
  }

  /**
   * This method updates the floor location for a given container
   * (specified by its shipping manifest id)
   * Use this method when a Lead TCN has had its floor location changed
   * due to either Container Consolidation, Pallet Relocation, or
   * Override Location during review.
   * To change floor, only have to update floor location id for a Lead TCN
   * in the database.
   * <p>
   * PRE: A check has already been performed to verify there is no current
   * Lead TCN with the same floor and same customer which we can reassign to
   *
   * @param shippingManifestId - database row id of a container
   * @param floorLocationId    - floor location to change to
   * @param userId             - user id of current logged in user
   * @return boolean (true for success)
   */
  private boolean changeFloor(String shippingManifestId, String floorLocationId, int userId) {

    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
        "update Shipping_Manifest set floor_location_id=?, modified_by=?, modified_date=? " +
            "where shipping_manifest_id=?", 0)) {

      Date currDate = new Date(System.currentTimeMillis());
      //* STEP 1 - update the manifest floor and modified info

      pstmt.setInt(1, Integer.parseInt(floorLocationId));
      pstmt.setInt(2, userId);
      pstmt.setDate(3, currDate);
      pstmt.setInt(4, Integer.parseInt(shippingManifestId));
      pstmt.executeUpdate();
      getDBTransaction().commit();
      return true;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      getDBTransaction().rollback();
      return false;
    }
  }

  /**
   * Use this method when a Lead TCN needs to be "reassigned" to a new Lead TCN
   * either due to Container Consolidation, Pallet Relocation,
   * or Override location during review.
   * To reassign means that the TCN (or shipping ids) of the original Lead TCN
   * are changed to reflect (or reassigned to) a new Lead TCN.
   * Once all of the originals have been reassigned, the original Lead TCN is
   * permanently deleted (since it is no longer used - a dangling pointer).
   *
   * @param oldShippingManifestId - this is the id of the old container being reassigned
   * @param newShippingManifestId - this is the id of container reassigning to
   * @param userId                - user id of current logged in user
   * @return boolean (true for success)
   */
  private boolean reassignShippingIds(String oldShippingManifestId, String newShippingManifestId, int userId) {
    try {
      //* sql for reassigning
      Date d = new Date(System.currentTimeMillis());

      //* STEP 1 - update the manifest ids of the old manifest
      try (PreparedStatement pstmtReassign = getDBTransaction().createPreparedStatement(
          "update Shipping set shipping_manifest_id=?, modified_by=?, modified_date=? " +
              "where shipping_id IN (select shipping_id from Shipping where shipping_manifest_id=?)", 0)) {
        pstmtReassign.setString(1, newShippingManifestId);
        pstmtReassign.setInt(2, userId);
        pstmtReassign.setDate(3, d);
        pstmtReassign.setString(4, oldShippingManifestId);
        pstmtReassign.executeUpdate();
      }

      //* STEP 2 - update the manifest modified info of the new manifest
      try (PreparedStatement pstmtUpdate = getDBTransaction().createPreparedStatement(
          "update Shipping_Manifest set modified_by=?, modified_date=? " +
              "where shipping_manifest_id=?", 0)) {
        pstmtUpdate.setInt(1, userId);
        pstmtUpdate.setDate(2, d);
        pstmtUpdate.setString(3, newShippingManifestId);
        pstmtUpdate.executeUpdate();
      }
      getDBTransaction().commit();
      return true;
    }
    catch (Exception e) {
      log.debug("reassign error", e);
      getDBTransaction().rollback();
      return false;
    }
  }

  /**
   * Delete the old Shipping Manifest Id row (or Lead TCN)
   */
  private void deleteOldShippingManifestId(String shippingManifestId) {
    try (PreparedStatement pstmtUpdate = getDBTransaction().createPreparedStatement(
        "delete from Shipping_Manifest where shipping_manifest_id=?", 0)) {

      pstmtUpdate.setString(1, shippingManifestId);
      pstmtUpdate.executeUpdate();
      getDBTransaction().commit();
    }
    catch (Exception e) {
      log.debug("delete old shipping manifest id error", e);
      getDBTransaction().rollback();
      throw new FakeException("Fake exception used to trigger catch below");
    }
  }

  /**
   * This function updates the database table Shipping_Manifest to
   * reflect pallet(s) relocated.
   *
   * @param selectedRelocateToValue - this is a floor location id
   * @param userId                  - user id of current logged in user
   * @return boolean
   */
  public boolean relocatePallet(String shippingManifestId,
                                String selectedRelocateToValue,
                                int userId) {

    try {
      //* regardless whether or not there are matching shipping manifest
      //* rows with same floor and customer,
      //* Only change the floor location
      boolean success = changeFloor(shippingManifestId, selectedRelocateToValue, userId);
      getDBTransaction().commit();
      return success;
    }
    catch (Exception e) {
      log.debug("relocate pallet error =  {}", e.getMessage());
      getDBTransaction().rollback();
      return false;
    }
  }

  private String getExistRemark(String leadTCN, String customerId) {
    String existingShippingManifestId = "";
    //* find match for the floor location consolidating to
    //* with the same customer (and where its not the same shipping manifest)
    //* get the shipping manifest id where floor and the customer id
    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
        "select shipping_manifest_id from shipping_manifest " +
            "where lead_tcn<>? and floor_location_id IN (select floor_location_id from Shipping_Manifest where lead_tcn=?)" +
            "and customer_id=? and manifest_date IS NOT NULL", 0)) {

      pstmt.setString(1, leadTCN);
      pstmt.setString(2, leadTCN);
      pstmt.setString(3, customerId);

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          existingShippingManifestId = rs.getString(1);
        }
      }
    }
    catch (SQLException sqle) {
      AdfLogUtility.logException(sqle);
    }
    return existingShippingManifestId;
  }

  /**
   * Use this method when a Lead TCN needs to be "reassigned" to a new Lead TCN
   * either due to Container Consolidation, Pallet Relocation,
   * or Override location during review.
   * To reassign means that the TCN (or shipping ids) of the original Lead TCN
   * are changed to reflect (or reassigned to) a new Lead TCN.
   * Once all of the originals have been reassigned, the original Lead TCN is
   * permanently deleted (since it is no longer used - a dangling pointer).
   *
   * @param oldShippingManifestId - this is the lead TCN of the old container being reassigned
   * @param newShippingManifestId - this is the id of container reassigning to
   * @param userId                - user id of current logged in user
   * @return boolean (true for success)
   */
  private boolean reassignRemark(String oldShippingManifestId, String newShippingManifestId, int userId) {
    try {
      //* sql for reassigning
      Date currDate = new Date(System.currentTimeMillis());

      //* STEP 1 - update the manifest ids of the old manifest
      try (PreparedStatement pstmtReassign = getDBTransaction().createPreparedStatement(
          "update Shipping set shipping_manifest_id=?, modified_by=?, modified_date=? " +
              "where shipping_id IN (select shipping_id from Shipping s, Shipping_Manifest m " +
              "where s.shipping_manifest_id=m.shipping_manifest_id and lead_tcn=?)", 0)) {
        pstmtReassign.setString(1, newShippingManifestId);
        pstmtReassign.setInt(2, userId);
        pstmtReassign.setDate(3, currDate);
        pstmtReassign.setString(4, oldShippingManifestId);
        pstmtReassign.executeUpdate();
      }

      //* STEP 2 - update the manifest modified info of the new manifest
      try (PreparedStatement pstmtUpdate = getDBTransaction().createPreparedStatement(
          "update Shipping_Manifest set modified_by=?, modified_date=? where shipping_manifest_id=?", 0)) {
        pstmtUpdate.setInt(1, userId);
        pstmtUpdate.setDate(2, currDate);
        pstmtUpdate.setString(3, newShippingManifestId);
        pstmtUpdate.executeUpdate();
      }
      return true;
    }
    catch (Exception e) {
      log.debug("reassign error", e);
      return false;
    }
  }

  /**
   * Delete the old Shipping Manifest Id row (or Lead TCN)
   */
  private void deleteOldRemark(String leadTCN) {
    try (PreparedStatement pstmtUpdate = getDBTransaction().createPreparedStatement(
        "delete from Shipping_Manifest where lead_tcn=?", 0)) {
      pstmtUpdate.setString(1, leadTCN);
      pstmtUpdate.executeUpdate();
    }
    catch (Exception e) {
      log.debug("delete old shipping manifest id error", e);
      throw new FakeException("Fake exception used to trigger catch below");
    }
  }

  /**
   * This function updates the database table Shipping_Manifest to
   * reflect aac remarked for shipments.
   *
   * @param leadTCN    - this is the lead TCN of the container to be remarked
   * @param customerId - this is the aac to remark to
   * @param userId     - user id of current logged in user
   * @return List  - of all ldcons which will need to be reprinted
   */
  public List<String> remarkShipment(String leadTCN, String customerId, int userId) {
    List<String> ldcons = new ArrayList<>();
    Date currDate = new Date(System.currentTimeMillis());
    try {
      try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
          "update Issue set customer_id=?, modified_by=?, modified_date=? " + "where scn IN (select scn from shipping s, shipping_manifest m " +
              "where s.shipping_manifest_id=m.shipping_manifest_id and m.lead_tcn=?)", 0)) {
        pstmt.setString(1, customerId);
        pstmt.setInt(2, userId);
        pstmt.setDate(3, currDate);
        pstmt.setString(4, leadTCN);
        pstmt.executeUpdate();
      }

      //* find match for the floor location consolidating to
      //* with the same customer (and where its not the same shipping manifest)
      //* get the shipping manifest id where floor and the customer id
      String existingShippingManifestId = getExistRemark(leadTCN, customerId);

      if (StringUtils.isNotBlank(existingShippingManifestId)) {
        //* if there exist 1 shipping manifest id already with same floor and customer,
        //* then we have to use it by reassign the selected container
        //* to the new Lead TCN (existing) and deleting the old

        if (reassignRemark(leadTCN, existingShippingManifestId, userId)) {
          deleteOldRemark(leadTCN);
        }
      }
      else {
        try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
            "update Shipping_Manifest set customer_id=?, modified_by=?, modified_date=? " +
                "where delivered_date IS NULL and lead_tcn=?", 0)) {
          pstmt.setString(1, customerId);
          pstmt.setInt(2, userId);
          pstmt.setDate(3, currDate);
          pstmt.setString(4, leadTCN);
          pstmt.executeUpdate();
        }

        //* collect ldcons to reprint
        try (PreparedStatement pstmtLDCON = getDBTransaction().createPreparedStatement(
            "select manifest from Shipping_Manifest where manifest_date is not null and lead_tcn=?", 0)) {
          pstmtLDCON.setString(1, leadTCN);
          try (ResultSet rs = pstmtLDCON.executeQuery()) {
            while (rs.next()) {
              String ldcon = rs.getString(1);
              if (StringUtils.isNotBlank(ldcon))
                ldcons.add(ldcon);
            }
          }
        }
      }
      getDBTransaction().commit();
    }
    catch (Exception e) {
      log.debug("remark error", e);
      getDBTransaction().rollback();
      throw new FakeException("Fake exception used to trigger catch in calling method");
    }
    return ldcons;
  }

  /**
   * Validates the Lead TCN is a lead tcn and not just a tcn.
   * Returns blank warehouse id if the lead tcn is not in the shipping_manifest table
   */
  public String validateRemark(String leadTCN) {

    String warehouseId = "";
    //* find match for the floor location consolidating to
    //* with the same customer (and where its not the same shipping manifest)
    //* get the shipping manifest id where floor and the customer id
    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
        "select warehouse_id from shipping_manifest s, equip e " +
            "where s.equipment_number=e.equipment_number and lead_tcn=?", 0)) {

      pstmt.setString(1, leadTCN);
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          warehouseId = rs.getString(1);
        }
      }
    }
    catch (SQLException sqle) {
      AdfLogUtility.logException(sqle);
    }
    return (warehouseId == null) ? "" : warehouseId;
  }

  /**
   * This function populates two lists, one list of LD CON id and
   * another list of LD CON (names).  LD CON stands for local delivery
   * contract number.  The lists are used to build select option (af:selectOneChoice)
   *
   * @return HashMap
   */
  public Map<String, String> fillCombineWithLDCONLists(String manifestFloor) {
    HashMap<String, String> hm = new HashMap<>();
    //* count the customer id(s) of the floor location about to manifest
    try {
      int count = 0;
      String customerId = "";
      try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
          "select distinct customer_id from shipping_manifest " +
              "where manifest_date IS NULL " + "and floor_location_id=?", 0)) {
        pstmt.setString(1, manifestFloor);

        try (ResultSet rs = pstmt.executeQuery()) {
          while (rs.next()) {
            customerId = rs.getString(1);
            count++;
          }
        }
      }

      //* if there is 1 customer id, then list the ldcon combine options (if any)
      //* if there are 1+ customers, then will generate 1+ manifests (with no combine option)
      if (count == 1) {
        log.debug("making combine with list.... manifest floor is {} and customer id is {}", manifestFloor, customerId);

        //* get the view
        ViewObjectImpl view = getAllLDCON();

        //* setting to -1 returns all rows, setting to n will return n rows
        view.setRangeSize(-1);

        if (manifestFloor != null) {
          view.setWhereClause("FLOOR_LOCATION_ID = :floorLocationId");
          view.defineNamedWhereClauseParam("floorLocationId", null, null);
          view.setNamedWhereClauseParam("floorLocationId", manifestFloor);

          view.addWhereClause(" AND CUSTOMER_ID = :customerId");
          view.defineNamedWhereClauseParam("customerId", null, null);
          view.setNamedWhereClauseParam("customerId", customerId);
        }

        //* fill the view with all possible ldcons available to be combined with
        view.executeQuery();
        Row[] rows = view.getAllRowsInRange();

        //* populate the hashmap with the returned rows
        for (Row row : rows) {
          Object nameObj = row.getAttribute("Manifest");
          Object idObj = row.getAttribute("ShippingManifestId");

          if ((nameObj != null) && (idObj != null))
            hm.put(idObj.toString(), nameObj.toString());
        }
      }
    }
    catch (Exception e) {
      log.debug("manifest error", e);
    }
    return hm;
  }

  public void setManifestPrintDate(String ldcon) {
    Date currDate = new Date(System.currentTimeMillis());

    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
        "update Shipping_Manifest set manifest_print_date=? where manifest=?", 0)) {
      pstmt.setDate(1, currDate);
      pstmt.setString(2, ldcon.toUpperCase());
      pstmt.executeUpdate();
      getDBTransaction().commit();
    }
    catch (Exception e) {
      getDBTransaction().rollback();
      AdfLogUtility.logException(e);
    }
  }

  /**
   * Returns a full string formatted as html.
   * This is the actual manifest to be printed.
   */
  public String printManifest(String ldcon, String autoprint, String urlContextPath) {
    log.debug("printing manifest  {}", ldcon);
    StringBuilder html = new StringBuilder(4000);
    String floorLocation = "";
    String aac = "";
    String printdate = "";
    String shippingManifestId = "";
    String leadTcn = "";
    boolean history = false;

    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
        "select aac, floor_location, manifest_print_date, shipping_manifest_id, lead_tcn " +
            "from shipping_manifest m, customer c, floor_location f " +
            "where m.customer_id=c.customer_id and m.floor_location_id=f.floor_location_id " +
            "and manifest=?", 0)) {
      pstmt.setString(1, ldcon.toUpperCase());

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          aac = escapeHtml(rs.getString(1));
          floorLocation = escapeHtml(rs.getString(2));
          printdate = escapeHtml(rs.getString(3));
          shippingManifestId = rs.getString(4);
          leadTcn = escapeHtml(rs.getString(5));
        }
        else {
          try (PreparedStatement ps2 = getDBTransaction().createPreparedStatement(
              "select aac, floor_location, manifest_print_date, shipping_manifest_id, lead_tcn " +
                  "from shipping_manifest_hist m, customer c where m.customer_id=c.customer_id " +
                  "and manifest=?", 0)) {

            ps2.setString(1, ldcon.toUpperCase());
            try (ResultSet rs2 = ps2.executeQuery()) {
              if (rs2.next()) {
                aac = escapeHtml(rs2.getString(1));
                floorLocation = escapeHtml(rs2.getString(2));
                printdate = escapeHtml(rs2.getString(3));
                shippingManifestId = rs2.getString(4);
                leadTcn = escapeHtml(rs2.getString(5));
                history = true;
              }
            }
          }
        }
      }
    }
    catch (SQLException sqle) {
      AdfLogUtility.logException(sqle);
    }

    if (!StringUtils.isNotBlank(printdate) && !history) {
      setManifestPrintDate(ldcon);
    }

    if (aac != null && floorLocation != null) {

      String shippingRoute = escapeHtml(getShippingRouteByFloor(floorLocation));
      if (!StringUtil.isEmpty(shippingRoute))
        shippingRoute = " - " + shippingRoute;

      html.append("<html>");
      html.append("<head>");

      html.append("<style>");
      html.append("table {font-size:8px}");
      html.append(".manifestTable {font-weight:bold;border-bottom: 1px solid black}");
      html.append(".boldBarcode {font-size:10px; font-weight:bold}");
      html.append(".boldLine {font-size:10px; font-weight:bold}");
      html.append(".floorLine {font-size:12px;font-weight:bold}");
      html.append(".signLine {font-size:10px}");
      html.append("</style>");

      html.append("</head>");
      if (autoprint.equals("1")) {
        html.append("<body onload='javascript:window.print();'>");
      }
      else {
        html.append("<body>");
      }
      html.append("<table border='0' cellpadding='2' cellspacing='0'>");
      html.append("<tr><td align='center' colspan='16'>===> STRATIS CONTAINER MANIFEST <===</td></tr>");
      html.append("<tr><td colspan='16'>&nbsp;</td></tr>");

      html.append("<tr>");
      html.append("<td colspan='5' class='boldBarcode'>&nbsp;&nbsp;&nbsp;&nbsp;LD-CON-NO:&nbsp;&nbsp;").append(escapeHtml(ldcon.toUpperCase())).append("</td>");
      html.append("<td colspan='6' class='boldBarcode'>RUC:&nbsp;&nbsp;").append(escapeHtml(aac)).append("</td>");
      html.append("<td colspan='5' class='boldBarcode'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;LEAD TCN:&nbsp;&nbsp;").append(escapeHtml(leadTcn)).append("</td>");
      html.append("</tr>");
      html.append("<tr>");
      html.append("<td>&nbsp;</td>");
      html.append("<td colspan='5'><img src='").append(urlContextPath).append("/SlotImage?type=BARCODE&amp;bc=").append(escapeHtml(ldcon.toUpperCase())).append("&amp;bt=BAR39'></td>");
      html.append("<td colspan='6'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src='").append(urlContextPath).append("/SlotImage?type=BARCODE&amp;bc=").append(escapeHtml(aac)).append("&amp;bt=BAR39'></td>");
      html.append("<td colspan='5'>&nbsp;&nbsp;<img src='").append(urlContextPath).append("/SlotImage?type=BARCODE&amp;bc=").append(escapeHtml(leadTcn)).append("&amp;bt=BAR39'></td>");
      html.append("</tr>");

      html.append("<tr><td colspan='16'>&nbsp;</td></tr>");
      html.append("<tr><td colspan='16' align='left' class='boldLine'>DELIVERY AAC:&nbsp;&nbsp;").append(escapeHtml(aac)).append(escapeHtml(shippingRoute)).append("</td></tr>");
      html.append("<tr><td colspan='9' align='left' class='floorLine'>&nbsp;&nbsp;&nbsp;FLOOR LOCATION&nbsp;:&nbsp;").append(escapeHtml(floorLocation)).append("</td>");
      html.append("<td colspan='3' class='floorLine'></td>");
      html.append("<td colspan='4'>&nbsp;</td>");
      html.append("</tr>");
      html.append("<tr><td colspan='16'>&nbsp;</td></tr>");

      html.append("<tr>");
      html.append("<td class='manifestTable'>&nbsp;</td>");
      html.append("<td class='manifestTable'>BARCODE</td>");
      html.append("<td class='manifestTable'>&nbsp;</td>");
      html.append("<td class='manifestTable'>DOCUMENT NUMBER</td>");
      html.append("<td class='manifestTable'>&nbsp;</td>");
      html.append("<td class='manifestTable'>NSN</td>");
      html.append("<td class='manifestTable'>&nbsp;</td>");
      html.append("<td class='manifestTable'>QTY</td>");
      html.append("<td class='manifestTable'>&nbsp;</td>");
      html.append("<td class='manifestTable'>UI</td>");
      html.append("<td class='manifestTable'>&nbsp;</td>");
      html.append("<td class='manifestTable'>NOMENCLATURE</td>");
      html.append("<td class='manifestTable'>&nbsp;</td>");
      html.append("<td class='manifestTable'>MARK FOR UIC</td>");
      html.append("<td class='manifestTable'>&nbsp;</td>");
      html.append("<td class='manifestTable'>SUPP ADD</td>");
      html.append("</tr>");

      String manifestSql;
      if (history) {
        manifestSql = "select s.tcn, s.quantity, n.niin, n.fsc, n.scc, i.routing_id_from, n.ui, i.qty, i.cc, " +
            "n.cognizance_code, n.price, i.supplementary_address, p.consolidation_barcode, n.nomenclature " +
            "from shipping_hist s, issue_hist i, packing_consolidation_hist p, niin_info n where s.scn=i.scn and i.packing_consolidation_id=p.packing_consolidation_id(+) and i.niin_id=n.niin_id " +
            "and s.shipping_manifest_id=?";
      }
      else {
        manifestSql = "select s.tcn, s.quantity, n.niin, n.fsc, n.scc, i.routing_id_from, n.ui, i.qty, i.cc, " +
            "n.cognizance_code, n.price, i.supplementary_address, p.consolidation_barcode, n.nomenclature " +
            "from shipping s INNER JOIN issue i on s.scn=i.scn LEFT JOIN niin_info n on i.niin_id=n.niin_id " +
            "INNER JOIN packing_consolidation p on i.packing_consolidation_id=p.packing_consolidation_id " +
            "where s.shipping_manifest_id=?";
      }

      try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(manifestSql, 0)) {
        pstmt.setString(1, shippingManifestId);
        try (ResultSet rs = pstmt.executeQuery()) {
          while (rs.next()) {

            String tcn = escapeHtml(rs.getString(1));
            if (tcn == null)
              tcn = "";
            else if (tcn.equals(""))
              tcn = "";

            String quantity = escapeHtml(rs.getString(2));
            if (quantity == null)
              quantity = "";
            else if (quantity.equals(""))
              quantity = "";

            String niin = escapeHtml(rs.getString(3));
            if (niin == null)
              niin = "";
            else if (niin.equals(""))
              niin = "";
            String fsc = escapeHtml(rs.getString(4));
            if (fsc == null)
              fsc = "";
            else if (fsc.equals(""))
              fsc = "";
            String barcode2 = fsc + niin;

            String ui = escapeHtml(rs.getString(7));
            if (ui == null)
              ui = "";
            else if (ui.equals(""))
              ui = "";
            String suppAdd = escapeHtml(rs.getString(12));
            if (suppAdd == null)
              suppAdd = "";
            else if (suppAdd.equals(""))
              suppAdd = "";

            String barcode = escapeHtml(rs.getString(13));
            if (barcode == null)
              barcode = "";
            else if (barcode.equals(""))
              barcode = "";

            String nomenclature = escapeHtml(rs.getString(14));
            if (nomenclature == null)
              nomenclature = "";
            else if (nomenclature.equals(""))
              nomenclature = "";

            html.append("<tr>");
            html.append("<td><input type='checkbox'/></td>");
            html.append("<td>&nbsp;&nbsp;").append(escapeHtml(barcode)).append("</td>");
            html.append("<td>&nbsp;</td>");
            html.append("<td>").append(escapeHtml(tcn)).append("</td>");
            html.append("<td>&nbsp;</td>");
            html.append("<td>").append(escapeHtml(barcode2)).append("</td>");
            html.append("<td>&nbsp;</td>");
            html.append("<td align='center'>").append(escapeHtml(quantity)).append("</td>");
            html.append("<td>&nbsp;</td>");
            html.append("<td align='center'>").append(escapeHtml(ui)).append("</td>");
            html.append("<td>&nbsp;</td>");
            html.append("<td>").append(escapeHtml(nomenclature)).append("</td>");
            html.append("<td>&nbsp;</td>");
            html.append("<td>").append(escapeHtml(aac)).append("</td>");
            html.append("<td>&nbsp;</td>");
            html.append("<td>").append(escapeHtml(suppAdd)).append("</td>");
            html.append("</tr>");
          }
        }
      }
      catch (SQLException sqle) {
        AdfLogUtility.logException(sqle);
      }

      html.append("<tr><td colspan='16'>&nbsp;</td></tr>");
      html.append("<tr><td colspan='16'>&nbsp;</td></tr>");
      html.append("<tr><td colspan='16'>&nbsp;</td></tr>");
      html.append("<tr><td colspan='16' class='signLine'>PRINT CUSTOMER NAME: ______________________________________</td></tr>");
      html.append("<tr><td colspan='16'>&nbsp;</td></tr>");
      html.append("<tr><td colspan='16'>&nbsp;</td></tr>");
      html.append("<tr><td colspan='16' class='signLine'>SIGNATURE: _______________________________________________</td></tr>");

      html.append("</table>");
      html.append("</body>");
      html.append("</html>");
    }
    return html.toString();
  }

  public String getShippingRouteByFloor(String floorLocation) {
    String shippingRoute = "";

    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
        "select route_name from floor_location f, shipping_route r where f.route_id=r.route_id and f.floor_location=?", 0)) {

      pstmt.setString(1, floorLocation);

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          shippingRoute = rs.getString(1);
          if (shippingRoute == null)
            shippingRoute = "";
        }
      }
    }
    catch (SQLException sqle) {
      AdfLogUtility.logException(sqle);
    }
    return shippingRoute;
  }

  public String exportAMSCMOS(String ldcon, int workstationId, boolean hist) {
    ImportFilesImpl importFiles = (ImportFilesImpl) getImportFiles1();
    String shippingManifestId =
        (hist) ? getShippingManifestIdForLDCONHist(ldcon) : getShippingManifestIdForLDCON(ldcon);
    String result;
    if (StringUtils.isNotBlank(shippingManifestId)) {
      result = importFiles.exportAMSCMOS(Integer.parseInt(shippingManifestId), workstationId);
      log.debug("result from export AMS CMOS {} =  {}", ldcon, result);
    }
    else {
      result = "Error - Shipping Manifest must be printed first ";
    }
    return result;
  }

  private String getLDCONByShippingManifestId(String shippingManifestId) {
    String ldcon = "";

    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
        "select manifest from shipping_manifest where shipping_manifest_id=?", 0)) {

      pstmt.setString(1, shippingManifestId);

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          ldcon = rs.getString(1);
        }
      }
    }
    catch (SQLException sqle) {
      AdfLogUtility.logException(sqle);
    }
    return ldcon;
  }

  /**
   * This function updates the database table Shipping_Manifest to
   * reflect manifested shipments.
   *
   * @param workstationId    - this is the warehouse building (e.g., 2253)
   * @param floor            - this is a floor location id
   * @param ldconCombineWith - this is the ldcon to combine with (optional)
   * @param userId           - user id of current logged in user
   * @return List
   */
  public List<String> manifestFloor(int workstationId, String floor, String ldconCombineWith, int userId) {
    List<String> ldcons = new ArrayList<>();
    try {
      if (StringUtils.isNotBlank(ldconCombineWith)) {
        manifestCombineLDCON(floor, ldconCombineWith, userId);
        ldcons.add(getLDCONByShippingManifestId(ldconCombineWith));
      }
      else {
        getWarehouseIdForWorkstation(workstationId);
        ldcons = manifestSingleLDCON(floor, userId);
      }

      TempManifestReviewImpl view = getTempManifestReview1();
      view.executeQuery();
      Row[] rows = view.getAllRowsInRange();
      for (Row row : rows) {
        row.remove();
      }
      view.getDBTransaction().commit();

      for (String ldcon : ldcons) {
        Row row = view.createRow();
        row.setAttribute("Manifest", ldcon);
        view.insertRow(row);
      }
      view.getDBTransaction().commit();
    }
    catch (Exception e) {
      log.debug("manifest error", e);
    }

    return ldcons;
  }

  /**
   * @param floor              - selected floor to manifest (floor location id)
   * @param shippingManifestId - id of row containing ldcon to combine with
   */
  private void manifestCombineLDCON(String floor, String shippingManifestId, int userId) {
    try {
      Date d = new Date(System.currentTimeMillis());

      //* Step 1 - change the manifest floor to combine with LDCON (i.e., new shipping manifest id)
      //* Update the shipping table old shipping manifest ids to new shipping manifest id
      try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
          "update Shipping set shipping_manifest_id=?, modified_by=?, modified_date=? " +
              "where shipping_id IN (select s.shipping_id from Shipping s, Shipping_Manifest m " +
              "where s.shipping_manifest_id=m.shipping_manifest_id " +
              "and m.manifest_date is NULL and m.floor_location_id=?)", 0)) {
        pstmt.setString(1, shippingManifestId);
        pstmt.setInt(2, userId);
        pstmt.setDate(3, d);
        pstmt.setString(4, floor);
        pstmt.executeUpdate();
      }

      //* STEP 2 - safe to delete (stale) old shipping ids from the shipping_manifest table
      try (PreparedStatement pstmtDEL = getDBTransaction().createPreparedStatement(
          "delete from Shipping_Manifest where floor_location_id=? and manifest_date IS NULL " +
              "and shipping_manifest_id NOT IN (select shipping_manifest_id from Shipping)", 0)) {
        pstmtDEL.setString(1, floor);
        pstmtDEL.executeUpdate();
      }

      //* STEP 3 - update LDCON manifest that we are combining with
      try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
          "update Shipping_Manifest set manifest_date=?, modified_by=?, modified_date=? " +
              "where shipping_manifest_id=?", 0)) {
        pstmt.setDate(1, d);
        pstmt.setInt(2, userId);
        pstmt.setDate(3, d);
        pstmt.setString(4, shippingManifestId);
        pstmt.executeUpdate();
      }

      //* commit all transactions
      getDBTransaction().commit();

      //* set the floor location to "in use", db batch nightly job will clear flag after delivery
      try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
          "update floor_location set in_use='Y' where floor_location_id=?", 0)) {
        pstmt.setString(1, floor);
        pstmt.executeUpdate();
        getDBTransaction().commit();
      }
    }
    catch (SQLException sqle) {
      getDBTransaction().rollback();
      throw new FakeException("Fake exception used to trigger catch in calling method");
    }
  }

  /**
   * @param floor - this is a floor location Id
   * @author Veronica M. Berryman
   */
  protected List<String> manifestSingleLDCON(String floor, int userId) {

    List<String> ldcons = new ArrayList<>();
    try {
      StringBuilder ldcon = new StringBuilder();
      String temp = "";
      Date currDate = new Date(System.currentTimeMillis());

      //* get the customer id(s) of the floor location about to manifest
      //* sql for when the ldcon to combine with is NOT provided
      try (PreparedStatement psGetCustIds = getDBTransaction().createPreparedStatement(
          "select shipping_manifest_id, customer_Id, manifest from Shipping_Manifest where manifest_date IS NULL " +
              "and floor_location_id=? order by customer_Id", 0)) {

        psGetCustIds.setString(1, floor);

        try (PreparedStatement psManifestUpdate = getDBTransaction().createPreparedStatement(
            "update Shipping_Manifest set manifest_date=?, modified_by=?, modified_date=? " +
                "where shipping_manifest_id=?", 0)) {

          try (ResultSet rs = psGetCustIds.executeQuery()) {
            while (rs.next()) {
              String id = rs.getString(1);
              String customerId = rs.getString(2);
              //* check to see if the customer is duplicated
              //* must generate a new local delivery contract number (LD CON) per customer
              if (!temp.equals(customerId)) {
                //* this is a new customer, generate LD CON
                ldcon.setLength(0);
                ldcon.append(rs.getString(3));
                temp = customerId;
                ldcons.add(ldcon.toString());
              }

              //* create a batch to execute for each manifest (1 per customer on this floor)
              if (ldcon.length() > 0) {
                psManifestUpdate.setDate(1, currDate);
                psManifestUpdate.setInt(2, userId);
                psManifestUpdate.setDate(3, currDate);
                psManifestUpdate.setString(4, id);

                psManifestUpdate.addBatch();
              }
            }
          }
          psManifestUpdate.executeBatch();
        }
      }
      getDBTransaction().commit();

      try (PreparedStatement psFloorUpdate = getDBTransaction().createPreparedStatement(
          "update floor_location set in_use='Y' where floor_location_id=?", 0)) {
        //* set the floor location to "in use", db batch nightly job will clear flag after delivery
        psFloorUpdate.setString(1, floor);
        psFloorUpdate.executeUpdate();
        getDBTransaction().commit();
      }
    }
    catch (SQLException e) {
      getDBTransaction().rollback();
      AdfLogUtility.logException(e);
      throw new FakeException("Fake exception used to trigger catch in calling method");
    }
    return ldcons;
  }

  private String generateLDCONByWarehouse(String warehouseId) {
    String ldcon = "";
    if (StringUtils.isNotBlank(warehouseId)) {
      try {
        //* get the ldcon prefix and ldcon serial number of the current logged in workstation
        //* each workstation ties to a warehouse building number,
        //* which has a unique local_delivery_prefix and serial
        int serial = 0;
        try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
            "select local_delivery_prefix, local_delivery_suffix from warehouse where warehouse_id=?", 0)) {

          pstmt.setString(1, warehouseId);

          try (ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
              String prefix = rs.getString(1);
              String suffix = rs.getString(2);
              ldcon = prefix;
              if (!StringUtils.isNotBlank(suffix)) {
                serial = 1;
              }
              else {
                serial = Integer.parseInt(suffix);
                serial++;
              }

              if (serial < 100000 && serial > 9999) {
                ldcon += serial;
              }
              else if (serial < 10000 && serial > 999) {
                ldcon += "0" + serial;
              }
              else if (serial < 1000 && serial > 99) {
                ldcon += "00" + serial;
              }
              else if (serial < 100 && serial > 9) {
                ldcon += "000" + serial;
              }
              else if (serial < 10) {
                ldcon += "0000" + serial;
              }
              else {
                serial = 1;
                ldcon += "00001";
              }
            }
          }
        }

        try (PreparedStatement pstmtUpdate = getDBTransaction().createPreparedStatement(
            "update warehouse set local_delivery_suffix=? where warehouse_id=?", 0)) {
          pstmtUpdate.setString(1, String.valueOf(serial));
          pstmtUpdate.setString(2, warehouseId);
          pstmtUpdate.executeUpdate();
        }
        getDBTransaction().commit();
      }
      catch (Exception e) {
        log.error("Error fetching warehouse id {} in {}", warehouseId, e.getStackTrace()[0].getMethodName(), e);
        getDBTransaction().rollback();
      }
    }
    return ldcon;
  }

  private String getWarehouseIdForWorkstation(int workstationId) {
    String warehouseId = "";
    if (workstationId > 0) {
      try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
          "select warehouse_id from Equip where equipment_number=?", 0)) {

        pstmt.setInt(1, workstationId);

        try (ResultSet rs = pstmt.executeQuery()) {
          if (rs.next()) {
            warehouseId = rs.getString(1);
          }
        }
      }
      catch (Exception e) {
        log.error("error fetching warehouse id# for workstation id {} information", workstationId, e);
      }
    }
    return warehouseId;
  }

  /**
   * This method will return the warehouse ID of another shipping manifest
   * row for a specific floor.
   * <p>
   * Primarily used by Shipping handheld.
   */
  public int findAnotherFloorManifest(String floor) {
    int warehouseId = 0;
    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
        "select warehouse_id from floor_location where floor_location=?  " +
            "and warehouse_id=(select warehouse_id from equip where equipment_number=(  " +
            "select equipment_number from shipping_manifest m, floor_location f  " +
            "where m.floor_location_id=f.floor_location_id and floor_location=? and m.manifest_date is null and rownum=1 ))", 0)) {

      pstmt.setString(1, floor);
      pstmt.setString(2, floor);

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          warehouseId = rs.getInt(1);
        }
      }
    }
    catch (SQLException sqle) {
      AdfLogUtility.logException(sqle);
    }
    return warehouseId;
  }

  /**
   * This function updates the database table Shipping_Manifest to
   * reflect manifested shipments.
   *
   * @param floor            - this is a floor location id
   * @param ldconCombineWith - this is the ldcon to combine with (optional)
   * @param userId           - user id of current logged in user
   * @return List
   */
  public List<String> manifestFloorForHandheld(String floor, String ldconCombineWith, int userId) {
    List<String> ldcons = new ArrayList<>();
    try {
      if (StringUtils.isNotBlank(ldconCombineWith)) {
        manifestCombineLDCON(floor, ldconCombineWith, userId);
        ldcons.add(ldconCombineWith);
      }
      else {
        ldcons = manifestSingleLDCON(floor, userId);
      }
    }
    catch (Exception e) {
      log.debug("manifest from handheld error", e);
    }

    return ldcons;
  }

  public List<String> manifestWarehouseFloorForHandheld(String floor, int userId) {
    List<String> ldcons = new ArrayList<>();
    try {
      ldcons = manifestSingleLDCON(floor, userId);
    }
    catch (Exception e) {
      log.debug("manifest from handheld error", e);
    }
    return ldcons;
  }

  private boolean donotGenerateAS2() {
    boolean donot = false;
    if (this.getSiteGCCSSMCFlag().equalsIgnoreCase("Y")) {
      return true;
    }

    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
        "select generate_as2 from site_info", 0)) {

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          String flag = rs.getString(1);
          if (flag == null)
            flag = "Y";
          donot = flag.equals("N");
        }
      }
    }
    catch (Exception e) {
      log.debug("donotGenerateAS2 error", e);
    }
    return donot;
  }

  private boolean donotGenerateASWalkthru(String shippingManifestId) {
    boolean donot = false;
    if (this.getSiteGCCSSMCFlag().equalsIgnoreCase("Y")) {
      donot = true;

      try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
          "select nvl(issue_type,'') from issue i, shipping s, shipping_manifest m " +
              "where i.scn=s.scn and s.shipping_manifest_id(+)=m.shipping_manifest_id and " +
              "m.shipping_manifest_id=?", 0)) {
        pstmt.setString(1, shippingManifestId);
        try (ResultSet rs = pstmt.executeQuery()) {
          if (rs.next()) {
            String flag = rs.getString(1);
            if (flag == null)
              flag = "Y";
            donot = flag.equals("W");
          }
        }
      }
      catch (Exception e) {
        log.debug("donotGenerateAS2 error", e);
      }
    }
    return donot;
  }

  /**
   * Send AS1 or AS2 transaction for each SCN for a given shipping manifest.
   */
  public int sendToWorkload(String shippingManifestId, int asNumber) {
    //* verify generate_as2 flag is not disabled in site_info
    if (asNumber == 2 && donotGenerateAS2())
      return -111;
    //* verify if walkthru, do not send
    if (donotGenerateASWalkthru(shippingManifestId))
      return -111;

    TransactionsImpl trans = null;
    GCSSMCTransactionsImpl gtrans = null;
    if (this.getSiteGCCSSMCFlag().equalsIgnoreCase("N")) {
      if (isBWAByManifest(shippingManifestId))
        return 0;
      trans = getTransactionsService();
    }
    else {
      if (isGCSSTransshippingByManifest(shippingManifestId))
        return 0;
      gtrans = getGCSSMCTransactionsService();
      //* do not send AS_ for gcss
    }

    //* have to send the AS2 prior to marking as delivered,
    //* since marking as delivered immediately triggers rows to history table
    Map<String, String> scns = getScnShippingHM(shippingManifestId);
    Iterator<String> it = scns.keySet().iterator();
    int result = 0;
    while (it.hasNext()) {
      String shippingId = it.next();
      String scn = scns.get(shippingId);
      //* send AS2 Transaction
      if (!StringUtil.isEmpty(scn)) {
        int result1;
        if (gtrans == null && trans != null)
          result1 = trans.sendAsxTrans(scn, asNumber);
        else if (gtrans != null)
          result1 = gtrans.sendAsxTrans(scn, asNumber);
        else
          throw new IllegalStateException("Both transaction implementations are null");
        if (result1 < 0)
          return result1;
        else
          result += result1;
        log.debug("sendAsxTrans for scn {} completed with result  {}", scn, result1);
      }
    }
    return result;
  }

  public int sendToWorkloadBySCN(String shippingId, int asNumber) {
    //* verify generate_as2 flag is not disabled in site_info
    if (asNumber == 2 && donotGenerateAS2())
      return -111;

    TransactionsImpl trans = null;
    GCSSMCTransactionsImpl gtrans = null;
    if (this.getSiteGCCSSMCFlag().equalsIgnoreCase("N")) {
      if (isBWA(shippingId))
        return 0;
      trans = getTransactionsService();
    }
    else {
      if (isGCSSTransshipping(shippingId))
        return 0;
      gtrans = getGCSSMCTransactionsService();
      //* do not send AS_ for gcss transshipping only
    }

    //* have to send the AS2 prior to marking as delivered,
    //* since marking as delivered immediately triggers rows to history table
    int result = 0;
    //* send AS2 Transaction
    String scn = getScnShipping(shippingId);
    if (scn != null && !scn.equals("")) {
      if (gtrans == null && trans != null)
        trans.sendAsxTrans(scn, asNumber);
      else if (gtrans != null)
        gtrans.sendAsxTrans(scn, asNumber);
      else
        throw new IllegalStateException("Both transaction implementations are null");
      log.debug("sendAsxTrans for scn {} completed with result  {}", scn, result);
    }
    else {
      result = -999;
    }

    return result;
  }

  /**
   * This function updates the database table Shipping_Manifest to
   * reflect shipments delivered
   * PRE:  AS2 has already been sent to workload manager first;
   * reason, the database triggers send to history table on delivered_date set.
   *
   * @param shippingManifestId - this is the id of the manifest row delivered
   * @param driver             - this is the driver who picked up
   * @param userId             - this is a logged in user id
   * @return boolean
   */
  public boolean submitAcknowledgeShipment(String shippingManifestId, String driver, int userId, String type) {
    log.debug("submit acknowledge shipment for  {}", shippingManifestId);
    boolean success;
    try {
      String status = "SHIPPED";
      String reverseStatus = "SHIPPING";

      //* update the status in issue table for the scn
      try (PreparedStatement pstmtUpdateIssue = getDBTransaction().createPreparedStatement(
          "update issue set status=?, modified_by=?, modified_date=? " +
              "where scn in (select scn from shipping s, shipping_manifest sm " +
              "where s.shipping_manifest_id=sm.shipping_manifest_id and sm.shipping_manifest_id=?)", 0)) {

        pstmtUpdateIssue.setString(1, status);
        pstmtUpdateIssue.setInt(2, userId);
        pstmtUpdateIssue.setDate(3, new Date(System.currentTimeMillis()));
        pstmtUpdateIssue.setString(4, shippingManifestId);
        pstmtUpdateIssue.executeUpdate();

        try (PreparedStatement pstmtUpdatePicking = getDBTransaction().createPreparedStatement(
            "update picking set status=?, modified_by=?, modified_date=? " +
                "where scn in (select scn from shipping s, shipping_manifest sm " +
                "where s.shipping_manifest_id=sm.shipping_manifest_id and sm.shipping_manifest_id=?)", 0)) {
          pstmtUpdatePicking.setString(1, status);
          pstmtUpdatePicking.setInt(2, userId);
          pstmtUpdatePicking.setDate(3, new Date(System.currentTimeMillis()));
          pstmtUpdatePicking.setString(4, shippingManifestId);
          pstmtUpdatePicking.executeUpdate();

          getDBTransaction().commit();

          log.debug("sending to workload manager for AS2");
          //* have to send the AS2 prior to marking as delivered,
          //* since marking as delivered immediately triggers rows to history table
          int result = sendToWorkload(shippingManifestId, 2);
          if (result < 0 && result != -111) {
            log.debug("one or more AS2 failed ...  {}", result);
            //* one or more AS2 failed, so reverse things[?]

            pstmtUpdateIssue.setString(1, reverseStatus);
            pstmtUpdateIssue.setInt(2, userId);
            pstmtUpdateIssue.setDate(3, new Date(System.currentTimeMillis()));
            pstmtUpdateIssue.setString(4, shippingManifestId);
            pstmtUpdateIssue.executeUpdate();

            pstmtUpdatePicking.setString(1, reverseStatus);
            pstmtUpdatePicking.setInt(2, userId);
            pstmtUpdatePicking.setDate(3, new Date(System.currentTimeMillis()));
            pstmtUpdatePicking.setString(4, shippingManifestId);
            pstmtUpdatePicking.executeUpdate();

            getDBTransaction().commit();

            //* if success is false on return, NOT ok to set to delivered
            success = false;
          }
          else {
            success = true;
          }
        }
      }

      if (success) {
        String sqlUpdate;
        if (type.equalsIgnoreCase("D")) {
          //* this is delivery
          sqlUpdate =
              "update Shipping_Manifest set delivered_flag=?, delivered_date=sysdate, modified_by=?, modified_date=? " +
                  "where shipping_manifest_id=?";
        }
        else {
          //* this is pickup
          sqlUpdate =
              "update Shipping_Manifest set delivered_flag=?, delivered_date=sysdate, modified_by=?, modified_date=?, " +
                  "picked_up_flag=?, picked_up_date=?, driver=? " + "where shipping_manifest_id=?";
        }

        try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(sqlUpdate, 0)) {
          Date d = new Date(System.currentTimeMillis());
          if (type.equalsIgnoreCase("D")) {
            //* this is delivery
            pstmt.setString(1, "Y");
            pstmt.setInt(2, userId);
            pstmt.setDate(3, d);
            pstmt.setString(4, shippingManifestId);
          }
          else {
            //* this is pickup
            pstmt.setString(1, "N");
            pstmt.setInt(2, userId);
            pstmt.setDate(3, d);
            pstmt.setString(4, "Y");
            pstmt.setDate(5, d);
            pstmt.setString(6, driver);
            pstmt.setString(7, shippingManifestId);
          }
          pstmt.executeUpdate();
        }
        getDBTransaction().commit();

        log.debug("successfully delivered  {}", shippingManifestId);
        //* if success is true on return, ok to send AS2
        success = true;
      }
    }
    catch (Exception e) {
      log.debug("acknowledge error", e);
      getDBTransaction().rollback();
      success = false;
      AdfLogUtility.logException(e);
    }
    return success;
  }

  /**
   * Returns a scn for a given shipping id.
   *
   * @return String
   */
  public String getScnShipping(String shippingId) {
    String scn = "";
    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
        "select scn from Shipping where shipping_id=?", 0)) {
      pstmt.setInt(1, Integer.parseInt(shippingId));

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          scn = rs.getString(1);
        }
      }
    }
    catch (Exception e) {
      log.debug("get scn shipping error", e);
    }
    return scn;
  }

  /**
   * Returns a hashmap of all scn and respective shipping ids for a given shipping manifest id.
   *
   * @return HashMap
   */
  private Map<String, String> getScnShippingHM(String shippingManifestId) {
    HashMap<String, String> hm = new HashMap<>();

    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
        "select scn, shipping_id from Shipping where shipping_manifest_id=?", 0)) {
      pstmt.setInt(1, Integer.parseInt(shippingManifestId));

      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          String scn = rs.getString(1);
          String id = rs.getString(2);
          if (scn != null)
            hm.put(id, scn);
        }
      }
    }
    catch (Exception e) {
      log.debug("get scn shipping error", e);
    }
    return hm;
  }

  /**
   * This method sets the last review date for a TCN
   *
   * @param userId - user id of current logged in user
   */
  private void setLastReviewDate(String shippingId, int userId) throws Exception {

    Date currentDate = new Date(System.currentTimeMillis());
    try (PreparedStatement pstmtUpdate = getDBTransaction().createPreparedStatement(
        "update Shipping set last_review_date=?, modified_by=?, modified_date=? where shipping_id=?", 0)) {

      //* sql for setting last review date
      pstmtUpdate.setDate(1, currentDate);
      pstmtUpdate.setInt(2, userId);
      pstmtUpdate.setDate(3, currentDate);
      pstmtUpdate.setInt(4, Integer.parseInt(shippingId));
      pstmtUpdate.executeUpdate();
    }
    catch (Exception e) {
      log.debug("set review only error", e);
      throw e;
    }
  }

  /**
   * This method only updates the last review date for
   * a barcode or TCN.
   *
   * @param userId - user id of current logged in user
   */
  public void donotOverrideAAC(String shippingId, int userId) {
    try {
      setLastReviewDate(shippingId, userId);
      getDBTransaction().commit();
    }
    catch (Exception e) {
      getDBTransaction().rollback();
    }
  }

  /**
   * This method overrides the aac of the SCN (Issue) with the expected
   * AAC of the review location.
   *
   * @param userId - user id of current logged in user
   */
  public int overrideAAC(String aac, String scn, String shippingId, int userId) {
    int result;

    log.debug("overriding scn= {} and aac=  {}", scn, aac);
    Date currDate = new Date(System.currentTimeMillis());

    //* sql for override aac
    try {
      try (PreparedStatement pstmtUpdate = getDBTransaction().createPreparedStatement(
          "begin update Issue set customer_id=(select customer_id from Customer " +
              "where aac=?), modified_by=?, modified_date=? where scn=?; end;", 0)) {
        pstmtUpdate.setString(1, aac);
        pstmtUpdate.setInt(2, userId);
        pstmtUpdate.setDate(3, currDate);
        pstmtUpdate.setString(4, scn);
        pstmtUpdate.executeUpdate();
      }

      try (PreparedStatement pstmtUpdate = getDBTransaction().createPreparedStatement(
          "begin update shipping_manifest set customer_id=(select customer_id from Customer " +
              "where aac=?), modified_by=?, modified_date=? " +
              "where shipping_manifest_id=(select shipping_manifest_id from shipping where scn=?); end;", 0)) {
        pstmtUpdate.setString(1, aac);
        pstmtUpdate.setInt(2, userId);
        pstmtUpdate.setDate(3, currDate);
        pstmtUpdate.setString(4, scn);
        pstmtUpdate.executeUpdate();
      }

      if (shippingId != null)
        setLastReviewDate(shippingId, userId);
      getDBTransaction().commit();
      result = 1;
    }
    catch (Exception e) {
      log.debug("override aac error", e);
      getDBTransaction().rollback();
      result = -1;
    }
    return result;
  }

  /**
   * This method only updates the last review date for
   * a barcode or TCN.
   *
   * @param userId - user id of current logged in user
   */
  public void donotOverrideLocation(String shippingId, int userId) {
    try {
      setLastReviewDate(shippingId, userId);
      getDBTransaction().commit();
    }
    catch (Exception e) {
      getDBTransaction().rollback();
    }
  }

  /**
   * This method overrides the barcode's floor location with the
   * expected review floor location.
   *
   * @param newFloorLocation          - this is the floor location to override to
   * @param currentAAC                - this is the current AAC for this TCN
   * @param currentShippingManifestId - this is the current shipping manifest id of the TCN (or barcode)
   * @param shippingId                - this is the shipping id of the TCN (or barcode)
   * @param userId                    - this is the user id of the logged in user
   */
  public int overrideLocation(String newFloorLocation, String currentAAC, String currentCustomerId,
                              String currentShippingManifestId, String shippingId, int userId, int workstationId) {
    int result;
    log.debug("overriding newFloorLocation= {} and currentaac= {} and customerId = {} and shippingManifestId= {} and shippingId= {}",
        newFloorLocation, currentAAC, currentCustomerId, currentShippingManifestId, shippingId);
    try {
      ShippingFloor shippingFloor = buildShippingFloor(newFloorLocation, workstationId, true);
      int floorLocationIdInt = shippingFloor.getFloorLocationId();
      if (floorLocationIdInt > 0) {
        if (shippingFloor.isInUse()) {
          result = -3;
          return result;
        }

        String floorLocationId = String.valueOf(floorLocationIdInt);
        String customerId;
        if (StringUtil.isEmpty(currentCustomerId))
          customerId = getCustomerId(currentAAC);
        else
          customerId = currentCustomerId;

        if (shippingFloor.hasShippingContainers()) {
          String floorCurrentCustomer = String.valueOf(shippingFloor.getContainer().getCustomerId());
          if (!floorCurrentCustomer.equals(customerId)) {
            result = -2;
            return result;
          }
        }

        result = addContainerToConsolidation(currentShippingManifestId, customerId, floorLocationId, userId);
        if (result > 0)
          setLastReviewDate(shippingId, userId);
        getDBTransaction().commit();
      }
      else {
        result = -4;
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      getDBTransaction().rollback();
      result = -1;
    }
    return result;
  }

  /**
   * This method returns true if the TCN or Barcode record is
   * found in the Shipping History table.
   *
   * @return boolean
   */
  public boolean shippingHistoryExist(String barcodeOrTcn) {
    boolean exist = false;
    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
        "select s.shipping_id from Shipping_Hist s, Packing_Consolidation_Hist p " +
            "where s.packing_consolidation_id=p.packing_consolidation_id " +
            "and (UPPER(p.consolidation_barcode)=? or UPPER(s.tcn)=?)", 0)) {

      pstmt.setString(1, barcodeOrTcn);
      pstmt.setString(2, barcodeOrTcn);

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          String id = rs.getString(1);
          if (id != null)
            exist = true;
        }
      }
    }
    catch (SQLException sqle) {
      AdfLogUtility.logException(sqle);
    }
    return exist;
  }

  /**
   * This method will make calls to addToLocation to assign material to a floor location
   * on a case by case basis.  First Case - if the Lead Tcn entered for review
   * is not manifested and shares the same customer as the material wish to assign,
   * then add to location.  Second Case - get the best shipping manifest location
   * to assign to. If one does not exist, then create the Shipping Manifest row
   * using ShippingManifestView and then add to location.
   * Thid Case (Last) - if one exist, then add to location.
   * 1st level of errors (# greater than 0 - new shippingManifestId, 0 - default, -1 - exception, -999 - AS1 failed)
   * 2nd level of errors (-777 - exception occurred ShippingManifestView row insert, -888 - shipping manifest id not created)
   *
   * @param currentShippingId - id of (unprocessed) shipping row to assign
   * @param currentCustomerId - id of (unprocessed) shipping row to assign
   * @param currentTcn        - id of (unprocessed) shipping row to assign
   */
  private int assignMaterial(String currentShippingId, String currentCustomerId, String currentTcn,
                             ShippingFloor shippingFloor, int iUserId, int iWarehouseId) {

    int success = 0;

    String expectedShippingManifestId;
    String expectedCustomerId;
    if (shippingFloor.hasShippingContainers()) {
      //* Lead TCN to be Reviewed
      for (ShippingContainer container : shippingFloor.getContainers()) {
        expectedShippingManifestId = String.valueOf(container.getShippingManifestId());
        expectedCustomerId = String.valueOf(container.getCustomerId());

        if (!container.isManifested() && currentCustomerId.equals(expectedCustomerId)) {
          //* add to this location
          int result = addToLocation(currentShippingId, expectedShippingManifestId, iUserId);
          if (result > 0)
            return container.getShippingManifestId();
          else
            return result;
        }
      }
    }

    //* process assignment based on Floor Location to be Reviewed
    String shippingManifestId =
        getShippingManifestId(String.valueOf(shippingFloor.getFloorLocationId()), currentCustomerId);
    if (StringUtil.isEmpty(shippingManifestId)) {

      log.debug("no shipping_manifest matches found, must create one");
      String localDeliverySuffix = generateLDCONByWarehouse(String.valueOf(iWarehouseId));
      //* shippingManifestId is empty, means no matches found for unmanifested, same customer, same floor
      ShippingManifestViewImpl view = null;
      try {
        //* create a new row in shipping_manifest and update the shipping table to the new shippingManifestId
        view = getShippingManifestView1();
        Row row = view.createRow();
        row.setAttribute("LeadTcn", currentTcn);
        row.setAttribute("FloorLocationId", shippingFloor.getFloorLocationId());
        row.setAttribute("CustomerId", currentCustomerId);
        //* give it a manifest LDCON number, but no date
        row.setAttribute("Manifest", localDeliverySuffix);

        row.setAttribute("PickedUpFlag", "N");
        row.setAttribute("DeliveredFlag", "N");
        row.setAttribute("CreatedBy", iUserId);
        row.setAttribute("CreatedDate", new Date(System.currentTimeMillis()));
        view.insertRow(row);
        view.getDBTransaction().commit();

        //* refresh the row to get the ShippingManifestId generated by database insert
        row.refresh(Row.REFRESH_WITH_DB_FORGET_CHANGES);
        Object smidObj = row.getAttribute("ShippingManifestId");
        if (smidObj != null) {
          shippingManifestId = smidObj.toString();
          success = 1;
        }
        else {
          success = -888;
          throw new FakeException("Fake exception used to trigger catch below");
        }

        log.debug("new row in shipping_manifest shippingManifestId =  {}", shippingManifestId);
      }
      catch (Exception e) {
        if (view != null) view.getDBTransaction().rollback();
        AdfLogUtility.logException(e);
        if (success != -888)
          success = -777;
      }
    }

    //* return now if there are errors
    if (success < 0) {
      log.debug("there were errors  {}", success);
      return success;
    }

    //* add to this location
    int result = addToLocation(currentShippingId, shippingManifestId, iUserId);
    if (result > 0)
      return Integer.parseInt(shippingManifestId);
    else
      return result;
  }

  /**
   * Add a shipping row to an existing shipping floor location.
   * Send to Workload Manager an AS1 request.
   * errors (0 - default, 1 - success, -1 - exception, -999 - AS1 failed)
   */
  private int addToLocation(String shippingId, String shippingManifestId, int iUserId) {
    log.debug("associate shipping row {} to shipping_manifest  {}", shippingId, shippingManifestId);
    int success;
    String issueType = "";
    try {
      Date currDate = new Date(System.currentTimeMillis());

      //* update the shipping table to this shippingManifestId
      try (PreparedStatement pstmtUpdate1 = getDBTransaction().createPreparedStatement(
          "update Shipping set shipping_manifest_id=?, modified_by=?, modified_date=? where shipping_id=?", 0)) {
        pstmtUpdate1.setString(1, shippingManifestId);
        pstmtUpdate1.setInt(2, iUserId);
        pstmtUpdate1.setDate(3, currDate);
        pstmtUpdate1.setString(4, shippingId);
        pstmtUpdate1.executeUpdate();
        getDBTransaction().commit();

        int result = 0;
        try (PreparedStatement pstmtSelect1 = getDBTransaction().createPreparedStatement(
            "select nvl(issue_type,'') from issue where scn IN (select scn from shipping where shipping_id = ?)", 0)) {
          pstmtSelect1.setString(1, shippingId);

          //* send AS1 when floor associated with shipping row
          try (ResultSet rs = pstmtSelect1.executeQuery()) {
            if (rs.next())
              issueType = rs.getString(1);
          }
        }

        if (!issueType.equalsIgnoreCase("W"))
          sendToWorkloadBySCN(shippingId, 1);

        log.debug("AS1 sent with result  {}", result);

        //* update the status in issue table for the scn
        try (PreparedStatement pstmtUpdate = getDBTransaction().createPreparedStatement(
            "update issue set status='SHIPPING', modified_by=?, modified_date=? " +
                "where scn in (select scn from shipping where shipping_id=?)", 0)) {
          pstmtUpdate.setInt(1, iUserId);
          pstmtUpdate.setDate(2, currDate);
          pstmtUpdate.setString(3, shippingId);
          pstmtUpdate.executeUpdate();
        }

        //* update the status in picking table for the scn
        try (PreparedStatement pstmtUpdate2 = getDBTransaction().createPreparedStatement(
            "update picking set status='SHIPPING', modified_by=?, modified_date=? " +
                "where scn in (select scn from shipping where shipping_id=?)", 0)) {
          pstmtUpdate2.setInt(1, iUserId);
          pstmtUpdate2.setDate(2, currDate);
          pstmtUpdate2.setString(3, shippingId);
          pstmtUpdate2.executeUpdate();
        }
        getDBTransaction().commit();
        success = 1;
      }
    }
    catch (Exception e) {
      getDBTransaction().rollback();
      AdfLogUtility.logException(e);
      success = -1;
    }
    return success;
  }

  /**
   * Fills a HashMap with values from one consolidation barcode
   * or tcn database record
   * For Handheld Use Only.
   *
   * @return HashMap
   */
  public Map<String, String> fillAllBarcodeReviewHH(String floorLocationOrLeadTcn, String barcode, int iUserId,
                                                    int iWorkstationId, int iWarehouseId) {
    HashMap<String, String> hm = new HashMap<>();
    try {
      ShippingBarcode shippingBarcode = validateContainerIdentifier(barcode);
      log.debug(shippingBarcode.toString());
      if (!shippingBarcode.hasShippingItems()) {
        if (shippingHistoryExist(barcode)) {
          hm.put("message", "0"); //CONTAINER ALREADY PICKED UP OR DELIVERED.
        }
        else {
          hm.put("message", "-2"); // CONTAINER DOES NOT EXIST
        }
        return hm;
      }

      //* validate the review location
      ShippingFloor shippingFloor = validateStageLocator(floorLocationOrLeadTcn, iWorkstationId);
      log.debug(shippingFloor.toString());
      if (shippingFloor.getFloorLocationId() == 0) {
        if (findFloorLocation(floorLocationOrLeadTcn, 0, false).getFloorLocationId() == 0) {
          hm.put("message", "-3"); // LOCATION DOES NOT EXIST IN THIS WAREHOUSE
          return hm;
        }
        hm.put("message", "-1"); // UNKNOWN, LOCATION NOT FOUND
        return hm;
      }

      //* added 10/6/08 to not allow review barcode update of another building's containers
      int packedWarehouseId = packedInWarehouseHH(shippingBarcode.getPackingConsolidationId());
      if (isNotAllowedToShipHH(iWarehouseId, packedWarehouseId)) {
        String errorMsg = "-8," + getBuildingName(packedWarehouseId);
        hm.put("message", errorMsg);
        return hm;
      }
      String warehouseIdOfCurrentWorkstation = getWorkstationWarehouseId(String.valueOf(iWorkstationId));

      if (shippingFloor.isInUse()) {
        hm.put("message", "-7"); // LOCATION ALREADY IN USE BY MANIFESTATION
        return hm;
      }

      boolean wrongCustomer = false;
      String customerCurrFloor =
          getCustomerCurrentFloorLocation(shippingBarcode.getCustomerId(), iWorkstationId);
      if (!StringUtil.isEmpty(customerCurrFloor)) {
        log.debug("customer current floor {} mismatch? {} - {}", customerCurrFloor, shippingFloor.getFloorLocationId(), shippingFloor.getFloorLocation());
        if (!customerCurrFloor.equals(shippingFloor.getFloorLocation())) {
          wrongCustomer = true;
        }
      }

      //* check for conflicts with review location and barcode location
      if (shippingBarcode.isAlreadyAssigned()) {
        if (shippingBarcode.isAssignedToAnotherWarehouse(StringUtil.cleanInt(warehouseIdOfCurrentWorkstation))) {
          hm.put("message", "-4"); // BARCODE IS ASSIGNED TO ANOTHER WAREHOUSE
          return hm;
        }

        if (shippingBarcode.getItem().getFloorLocationId() != shippingFloor.getFloorLocationId()) {
          String expectedFloor = shippingFloor.getFloorLocation();
          String floor = shippingBarcode.getItem().getFloorLocation();
          hm.put("message", expectedFloor);
          hm.put("FloorLocation", floor); // current
          hm.put("Aac", getCustomerAAC(String.valueOf(shippingBarcode.getCustomerId()))); // expected
          ShippingItem shippingItem = shippingBarcode.getItem();
          hm.put("ShippingManifestId", String.valueOf(shippingItem.getShippingManifestId()));
          hm.put("ShippingId", String.valueOf(shippingItem.getShippingId()));

          return hm;
        }

        if (shippingFloor.hasShippingContainers() && shippingBarcode.getCustomerId() != shippingFloor.getContainer().getCustomerId()) {
          hm.put("message", "1"); // This barcode is processed and has customer conflict
          hm.put("ScnAac", shippingFloor.getContainer().getAac()); // current aac
          hm.put("Aac", getCustomerAAC(String.valueOf(shippingBarcode.getCustomerId()))); // expected
          hm.put("Scn", shippingBarcode.getItem().getScn());
          hm.put("ShippingId", String.valueOf(shippingBarcode.getItem().getShippingId()));
          return hm;
        }

        hm.put("message", "4"); // ALREADY INCLUDED - THIS BARCODE IS ASSIGNED TO THIS LOCATION ALREADY
        return hm;
      }
      else {
        if (wrongCustomer) {
          hm.put("message",
              "-5"); // This barcode is unprocessed and cannot be processed due to BARCODE's CUSTOMER CURRENTLY ASSIGNED A FLOOR ALREADY IN THIS WAREHOUSE
          return hm;
        }

        if (shippingFloor.hasShippingContainers() && shippingBarcode.getCustomerId() != shippingFloor.getContainer().getCustomerId()) {
          hm.put("message",
              "-6"); // This barcode is unprocessed and cannot be processed due to customer conflict
          return hm;
        }

        //* no errors, so add the container
        int result = assignMaterial(shippingBarcode, shippingFloor, iUserId, iWorkstationId);
        if (result <= 0) {
          hm.put("message", "-999"); // error
          return hm;
        }
        else {
          hm.put(barcode, "ADDED"); //THIS BARCODE WAS UNPROCESSED and HAS BEEN ADDED TO LOCATION
          hm.put("message", "3");
        }
      }
      return hm;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }

    return hm;
  }

  /**
   * Returns true if TCN exist in shipping or in shipping_hist tables.
   *
   * @return boolean
   */
  public boolean existTCN(String tcn) {
    boolean exist = false;
    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
        "select tcn from shipping where tcn=? and rownum < 2 union select tcn from shipping_hist " +
            "where tcn=? and rownum < 2", 0)) {

      pstmt.setString(1, tcn);
      pstmt.setString(2, tcn);

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          String tcnFound = rs.getString(1);
          if (tcnFound != null)
            exist = true;
        }
      }
    }
    catch (SQLException sqle) {
      AdfLogUtility.logException(sqle);
    }
    return exist;
  }

  /**
   * Returns true if document number exist in receipt or receipt_hist tables.
   */
  public boolean existDocumentNumber(String documentNumber) {
    boolean exist = false;
    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
        "select document_number from receipt where document_number=? and rownum =1 union select document_number " +
            "from receipt_hist where document_number=? and rownum =1", 0)) {
      pstmt.setString(1, documentNumber);
      pstmt.setString(2, documentNumber);

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          String found = rs.getString(1);
          if (found != null)
            exist = true;
        }
      }
    }
    catch (SQLException sqle) {
      AdfLogUtility.logException(sqle);
    }
    return exist;
  }

  /**
   * Returns true if the handheld logged in building matches
   * the building of the packing consolidation.
   */
  public boolean isNotAllowedToShipHH(int warehouseId, int packedWarehouseId) {
    return warehouseId != packedWarehouseId;
  }

  public int packedInWarehouseHH(int packingConsolidationId) {
    int found = 0;
    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
        "select e.warehouse_id from packing_consolidation p, packing_station s, equip e " +
            "where p.packing_station_id =s.packing_station_id " +
            "and s.equipment_number=e.equipment_number and p.packing_consolidation_id=? and rownum =1", 0)) {
      pstmt.setInt(1, packingConsolidationId);

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          found = rs.getInt(1);
        }
      }
    }
    catch (SQLException sqle) {
      AdfLogUtility.logException(sqle);
    }
    return found;
  }

  public String getBuildingName(int warehouseId) {
    String found = "";
    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
        "select building from warehouse where warehouse_id=?", 0)) {
      pstmt.setInt(1, warehouseId);

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          found = rs.getString(1);
        }
      }
    }
    catch (SQLException sqle) {
      AdfLogUtility.logException(sqle);
    }
    return (found == null) ? "" : found;
  }

  /**
   * Returns true if shipping consolidation barcode exists in packing_consolidation or packing_consolidation_hist tables.
   * Does not indicate whether or not the barcode has been packed and is now in shipping -
   * Refer to existBarcodeInShipping method.
   *
   * @return boolean
   */
  public boolean existBarcode(String barcode) {
    boolean exist = false;
    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
        "select consolidation_barcode from packing_consolidation where UPPER(consolidation_barcode)=? and rownum =1" +
            "union select consolidation_barcode from packing_consolidation_hist " +
            "", 0)) {
      pstmt.setString(1, barcode);
      pstmt.setString(2, barcode);

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          String barcodeFound = rs.getString(1);
          if (barcodeFound != null)
            exist = true;
        }
      }
    }
    catch (SQLException sqle) {
      AdfLogUtility.logException(sqle);
    }
    return exist;
  }

  /**
   * This method determines if a barcode is in shipping.  Differs from
   * existBarcode method in that, the barcode may exist in the
   * packing_consolidation table, but may not be in the Shipping phase yet.
   * Returns true if the barcode exist in the Shipping table.
   *
   * @return boolean
   */
  public boolean existBarcodeInShipping(String barcode) {
    boolean exist = false;
    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
        "select p.consolidation_barcode from shipping s, packing_consolidation p " +
            "where s.packing_consolidation_id=p.packing_consolidation_id " +
            "and UPPER(p.consolidation_barcode)=?", 0)) {

      pstmt.setString(1, barcode);

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          String barcodeFound = rs.getString(1);
          if (barcodeFound != null)
            exist = true;
        }
      }
    }
    catch (SQLException sqle) {
      AdfLogUtility.logException(sqle);
    }
    return exist;
  }

  public String getCustomerId(String aac) {
    return getSingleReturnValue("select customer_id from Customer where aac=?", aac);
  }

  public String getCustomerAAC(String customerId) {
    return getSingleReturnValue("select aac from Customer where customer_id=?", customerId);
  }

  public boolean isCustomerRestricted(int customerId) {
    boolean restricted = true;
    String restrict;
    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
        "select nvl(restrict_ship,'N') from Customer where customer_id=?", 0)) {
      pstmt.setInt(1, customerId);

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          restrict = rs.getString(1);
          if (restrict.equals("N"))
            restricted = false;
        }
      }
    }
    catch (SQLException sqle) {
      AdfLogUtility.logException(sqle);
    }
    return restricted;
  }

  public String getRoutingId(String ric) {
    String sql = "select route_id from Route where route_name=?";
    return getSingleReturnValue(sql, ric);
  }

  public String getFloorLocationId(String floorLocation) {
    String sql = "select floor_location_id from Floor_Location where floor_location=?";
    return getSingleReturnValue(sql, floorLocation);
  }

  public String getFloorLocationWorkstation(String floorLocationId) {
    String sql = "select equipment_number from Floor_Location where floor_location_id=?";
    return getSingleReturnValue(sql, floorLocationId);
  }

  public String getWorkstationShippingArea(String workstationId) {
    String sql = "select shipping_area from equip where equipment_number=?";
    return getSingleReturnValue(sql, workstationId);
  }

  public String getWorkstationWarehouseId(String workstationId) {
    String sql = "select warehouse_id from equip where equipment_number=?";
    return getSingleReturnValue(sql, workstationId);
  }

  /**
   * Returns true if the floor is in use for manifesting until delivery.
   * Floor_location.In_use flag is either Y or N.  A database trigger
   * resets the in_use flag at night on the day of a delivery.
   *
   * @return boolean
   */
  public boolean isFloorInUse(int floorLocationId) {
    boolean isInUse = true;
    String inUse;
    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
        "select nvl(in_use,'N') from Floor_Location " + "where floor_location_id=?", 0)) {

      pstmt.setInt(1, floorLocationId);

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          inUse = rs.getString(1);
          if (inUse.equals("N"))
            isInUse = false;
        }
      }
    }
    catch (SQLException sqle) {
      AdfLogUtility.logException(sqle);
    }
    return isInUse;
  }

  /**
   * Returns true if a floor is locked to the workstation input.  A floor is locked if has an warehouse
   * to which it is assigned to.  This means no other workstation outside the
   * specified warehouse may use this floor.
   * A floor location is set to its warehouse in Warehouse Setup and
   * also in floor location setup.
   */
  public boolean isFloorLocked(int floorLocationId, int workstationId) {
    int count = 0;
    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
        "select count(*) from Floor_Location where floor_location_id=? and equipment_number=?", 0)) {
      pstmt.setInt(1, floorLocationId);
      pstmt.setInt(2, workstationId);

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          count = rs.getInt(1);
        }
      }
    }
    catch (SQLException sqle) {
      AdfLogUtility.logException(sqle);
    }
    return (count != 0);
  }

  public String findAMSPath() {
    //updated to pull AMS path from application.yml
    val amsUtils = ContextUtils.getBean(AmsCmosUtils.class);
    return amsUtils.getAmsCmosPath();
  }

  public String findNiinId(String nsn) {
    String niinId = "";
    if (nsn.length() > 10) {
      String fsc = nsn.substring(0, 4);
      String niin = nsn.substring(4);
      try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
          "select niin_id from niin_info where niin=? and fsc=?", 0)) {

        pstmt.setString(1, niin);
        pstmt.setString(2, fsc);

        try (ResultSet rs = pstmt.executeQuery()) {
          if (rs.next()) {
            niinId = rs.getString(1);
          }
        }
      }
      catch (SQLException sqle) {
        AdfLogUtility.logException(sqle);
      }
    }
    return niinId;
  }

  /**
   * Find a floor location and create a Shipping floor object
   * Set useWarehouse to TRUE if you wish to query floor location specifically by warehouse.
   *
   * @return ShippingFloor
   */
  public ShippingFloor findFloorLocation(String floor, int workstationId, boolean useWarehouse) {
    ShippingFloor shippingFloor = new ShippingFloor();
    String warehouseId = "";

    String sql = "select floor_location_id, nvl(in_use, 'N'), warehouse_id " + "from floor_location where floor_location=?";
    if (useWarehouse) {
      warehouseId = getWorkstationWarehouseId(String.valueOf(workstationId));
      sql += " and warehouse_id=?";
      log.debug("{}, {}, {}, {}", sql, floor, workstationId, warehouseId);
    }

    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(sql, 0)) {
      pstmt.setString(1, floor);
      if (useWarehouse)
        pstmt.setString(2, warehouseId);

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          shippingFloor.setFloorLocationId(rs.getInt(1));
          shippingFloor.setFloorLocation(floor);
          shippingFloor.setInUseFlag(rs.getString(2));
          shippingFloor.setWarehouseId(rs.getInt(3));
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return shippingFloor;
  }

  /**
   * This method is the main interface from the web and handheld in proper submission
   * of any transshipment data.
   *
   * @return StringBuffer
   * @see mil.stratis.model.services.WorkLoadManagerImpl
   */
  public StringBuilder transship(Map<String, String> hm, int userId, int workstationId) {
    StringBuilder msg = new StringBuilder();

    String tcn = hm.get("tcn");
    String contractNumber = hm.get("contract_number");
    String documentNumber = hm.get("document_number");

    String barcode = hm.get("barcode");

    //* do some validations
    if (StringUtils.isNotBlank(tcn)) {
      //* transshipment by tcn
      //* does the tcn already exist in shipping table
      if (existTCN(tcn)) {
        //* tcn already exist in shipping table
        msg.append("TCN already exist in Shipping.");
      }
    }
    else if (StringUtils.isNotBlank(contractNumber) && StringUtil.isEmpty(documentNumber)) {
      documentNumber = getTransactionsService().returnNextAvailableDocumentNumber();
      hm.put("document_number", documentNumber);
    }

    if (existDocumentNumber(documentNumber)) {
      //* document number already exist in receipt or receipt_hist table
      msg.append("Document Number already exist in Receipt or Receipt History.");
    }

    if (existBarcode(barcode)) {
      //* barcode already exist in packing_consolidation or packing_consolidation_hist table
      msg.append("Shipping Barcode already exist in Packing Consolidation or Packing Consolidation History.");
    }

    //* no errors, proceed to transship
    if (msg.length() < 1) {
      log.debug("hm  {}", hm);
      int rcn = createReceiptRow(hm);
      log.debug("newly created receipt  {}", rcn);
      if (rcn > 0) {
        boolean failed = false;
        try {
          log.debug("initiateTransshipment {}, {}, {}, {}", rcn, barcode, userId, workstationId);
          String resultScn =
              getWorkLoadManagerService().initiateTransShipment(rcn, barcode, userId, workstationId);

          if (resultScn.contains("Error")) {
            log.debug("resultScn is  {}", resultScn);
            msg.append("An error occurred while initiating transshipment.");
            failed = true;
          }
          else {
            updateTransshipment(hm.get("call_number"), hm.get("shipment_number"), hm.get("line_number"),
                hm.get("billed_amount"), resultScn, tcn, userId);
          }
        }
        catch (Exception e) {
          msg.append("An exception occurred while initiating trannshipment.");
          failed = true;
        }

        // first make sure initiateTransshipment is backing out (or undoing) its rows
        if (failed)
          //* remove Receipt row
          deleteReceiptRow(rcn);
      }
      else {
        msg.append("An error occurred while creating a record for this transshipment.");
      }
    }

    return msg;
  }

  /**
   * This method deletes a row in the Receipt database table given an RCN
   * Returns true or false based on successful deletion.
   */
  private void deleteReceiptRow(int rcn) {
    try {
      try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
          "delete from receipt_issue where rcn=?", 0)) {
        pstmt.setInt(1, rcn);
        pstmt.executeUpdate();
      }
      getDBTransaction().commit();

      try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
          "delete from receipt where rcn=?", 0)) {
        pstmt.setInt(1, rcn);
        pstmt.executeUpdate();
      }
      getDBTransaction().commit();
    }
    catch (SQLException sqle) {
      AdfLogUtility.logException(sqle);
    }
  }

  /**
   * This method creates a row in the Receipt database table, via the
   * ReceiptView view object
   * Returns the rcn generated from successful insert or a negative number if failed.
   *
   * @return long
   * @see mil.stratis.model.view.rcv.ReceiptViewImpl
   */
  private int createReceiptRow(Map<String, String> hm) {
    int rcn = 0;
    try {

      ReceiptViewImpl view = getTransshipmentReceiptView1();
      Row row = view.createRow();
      row.setAttribute("Rpd", "3");
      //* add non-empty elements of hashmap to row
      String documentNumber = hm.get("document_number");
      if (StringUtils.isNotBlank(documentNumber))
        row.setAttribute("DocumentNumber", documentNumber);

      String demandSuffix = hm.get("suffix");
      if (StringUtils.isNotBlank(demandSuffix))
        row.setAttribute("Suffix", demandSuffix);

      String contractNumber = hm.get("contract_number");
      if (StringUtils.isNotBlank(contractNumber))
        row.setAttribute("ContractNumber", contractNumber);

      String consignee = hm.get("aac");
      if (StringUtils.isNotBlank(consignee))
        row.setAttribute("Consignee", consignee);

      //TraceabilityNumber, ClassCommodityNumber, SerialNumber
      String trackingNumber = hm.get("tracking_number");
      if (StringUtils.isNotBlank(trackingNumber))
        row.setAttribute("TraceabilityNumber", trackingNumber);

      String price = hm.get("price");
      if (StringUtils.isNotBlank(price))
        row.setAttribute("Price", price);

      String niinId = hm.get("niin_id");
      if (StringUtils.isNotBlank(niinId))
        row.setAttribute("NiinId", niinId);

      String ricFrom = hm.get("ricFrom");
      if (StringUtils.isNotBlank(ricFrom)) {
        row.setAttribute("RoutingId", ricFrom);
      }

      String ui = hm.get("ui");
      if (StringUtils.isNotBlank(ui))
        row.setAttribute("Ui", ui);

      //* NOTE: These rows are required by database
      String qty = hm.get("quantity");
      if (StringUtils.isNotBlank(qty)) {
        row.setAttribute("QuantityStowed", "0");
        row.setAttribute("QuantityReleased", "0");
        row.setAttribute("QuantityMeasured", "0");
        row.setAttribute("QuantityInducted", qty);
        row.setAttribute("QuantityInvoiced", qty);
      }

      String quantityInvoiced = hm.get("quantity_invoiced");
      if (StringUtils.isNotBlank(quantityInvoiced))
        row.setAttribute("QuantityInvoiced", quantityInvoiced);

      String cc = hm.get("cc");
      if (StringUtils.isNotBlank(cc))
        row.setAttribute("Cc", cc);

      String pc = hm.get("pc");
      if (StringUtils.isNotBlank(pc))
        row.setAttribute("Pc", pc);

      row.setAttribute("DocumentId", "A5A");
      row.setAttribute("Status", "TRANSSHIPPING");
      row.setAttribute("CreatedBy", hm.get("userId"));
      row.setAttribute("WorkStation", hm.get("workstation_id"));
      view.insertRow(row);
      view.getDBTransaction().commit();

      //* refresh the row to get the SCN generated by database insert
      row.refresh(Row.REFRESH_WITH_DB_FORGET_CHANGES);
      Object rcnObj = row.getAttribute("Rcn");
      if (rcnObj != null)
        rcn = Integer.parseInt(rcnObj.toString());
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      rcn = -1;
    }
    return rcn;
  }

  /**
   * This method is used primarily to update a Shipping row after transship and initiateTransshipment
   * have been invoked successfully.
   */
  private void updateTransshipment(String callNumber, String shipmentNumber, String lineNumber,
                                   String billedAmount, String scn, String tcn, int userId) {

    Date currDate = new Date(System.currentTimeMillis());

    //* update Shipping table qty
    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
        "update Shipping set call_number=?, shipment_number=?, line_number=?, tailgate_date=?, billed_amount=?, " +
            "modified_by=?, modified_date=?, tcn=? where scn=?", 0)) {
      pstmt.setString(1, StringUtils.isNotBlank(callNumber) ? callNumber : "");
      pstmt.setString(2, StringUtils.isNotBlank(shipmentNumber) ? shipmentNumber : "");
      pstmt.setString(3, StringUtils.isNotBlank(lineNumber) ? lineNumber : "");
      pstmt.setDate(4, currDate);
      pstmt.setString(5, StringUtils.isNotBlank(billedAmount) ? billedAmount : "");
      pstmt.setInt(6, userId);
      pstmt.setDate(7, currDate);
      pstmt.setString(8, tcn);
      pstmt.setString(9, scn);
      pstmt.executeUpdate();

      //* commit all transactions
      getDBTransaction().commit();
    }
    catch (Exception e) {
      log.debug("update transshipment error", e);
      //* rollback all transactions
      getDBTransaction().rollback();
    }
  }

  /**
   * Container's getter for ListAvailableCustomers.
   */
  public ViewObjectImpl getListAvailableCustomers() {
    return (ViewObjectImpl) findViewObject("ListAvailableCustomers");
  }

  /**
   * Container's getter for ListContainersRemark.
   */
  public ViewObjectImpl getListContainersRemark() {
    return (ViewObjectImpl) findViewObject("ListContainers_Remark");
  }

  /**
   * Container's getter for ShippedTo.
   */
  public ViewObjectImpl getShippedTo() {
    return (ViewObjectImpl) findViewObject("ShippedTo");
  }

  /**
   * Container's getter for RemarkTo.
   */
  public ViewObjectImpl getRemarkTo() {
    return (ViewObjectImpl) findViewObject("RemarkTo");
  }

  /**
   * Container's getter for AllLDCON.
   */
  public ViewObjectImpl getAllLDCON() {
    return (ViewObjectImpl) findViewObject("AllLDCON");
  }

  /**
   * Container's getter for ListBarcodeInfo.
   */
  public ViewObjectImpl getListBarcodeInfo() {
    return (ViewObjectImpl) findViewObject("ListBarcodeInfo");
  }

  /**
   * Container's getter for AllShippingFloorsManifest.
   */
  public ViewObjectImpl getAllShippingFloorsManifest() {
    return (ViewObjectImpl) findViewObject("AllShippingFloors_Manifest");
  }

  /**
   * Container's getter for UndeliveredLDCONList.
   */
  public ViewObjectImpl getUndeliveredLDCONList() {
    return (ViewObjectImpl) findViewObject("UndeliveredLDCONList");
  }

  /**
   * Container's getter for printLDCONList.
   */
  public ViewObjectImpl getprintLDCONList() {
    return (ViewObjectImpl) findViewObject("printLDCONList");
  }

  /**
   * Container's getter for printContainerList.
   */
  public ViewObjectImpl getprintContainerList() {
    return (ViewObjectImpl) findViewObject("printContainerList");
  }

  /**
   * Container's getter for printAACList.
   */
  public ViewObjectImpl getprintAACList() {
    return (ViewObjectImpl) findViewObject("printAACList");
  }

  /**
   * Container's getter for ListPallets.
   */
  public ViewObjectImpl getListPallets() {
    return (ViewObjectImpl) findViewObject("ListPallets");
  }

  /**
   * Container's getter for ListContainersConsolidate.
   */
  public ViewObjectImpl getListContainersConsolidate() {
    return (ViewObjectImpl) findViewObject("ListContainersConsolidate");
  }

  /**
   * Container's getter for AllCustomersConsolidate.
   */
  public ViewObjectImpl getAllCustomersConsolidate() {
    return (ViewObjectImpl) findViewObject("AllCustomersConsolidate");
  }

  /**
   * Used by handheld for shipping acknowledge delivery.
   */
  public String getShippingManifestIdForLDCON(String ldcon) {
    String shippingManifestId = "";

    //* get the shipping manifest id where manifest (or ldcon)
    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
        "select shipping_manifest_id from shipping_manifest where manifest=? and manifest_date IS NOT NULL " +
            "and manifest_print_date IS NOT NULL", 0)) {
      pstmt.setString(1, ldcon);

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          shippingManifestId = rs.getString(1);
        }
      }
    }
    catch (SQLException sqle) {
      AdfLogUtility.logException(sqle);
    }
    return shippingManifestId;
  }

  public String getShippingManifestIdForLDCONHist(String ldcon) {
    String shippingManifestId = "";

    //* get the shipping manifest id where manifest (or ldcon)
    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
        "select shipping_manifest_id from shipping_manifest " +
            "where manifest=? and manifest_date IS NOT NULL and manifest_print_date IS NOT NULL " +
            "union " +
            "select shipping_manifest_id from shipping_manifest_hist " +
            "where manifest=? and manifest_date IS NOT NULL and manifest_print_date IS NOT NULL ", 0)) {

      pstmt.setString(1, ldcon);
      pstmt.setString(2, ldcon);

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          shippingManifestId = rs.getString(1);
        }
      }
    }
    catch (SQLException sqle) {
      AdfLogUtility.logException(sqle);
    }
    return shippingManifestId;
  }

  public int getEquipmentNumber(String wacId) {
    String equipmentNumber = "";

    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
        "select equipment_number from wac w, equip e where w.wac_id=e.wac_id and w.wac_id=?", 0)) {

      pstmt.setString(1, wacId);

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          equipmentNumber = rs.getString(1);
        }
      }
    }
    catch (SQLException sqle) {
      AdfLogUtility.logException(sqle);
    }

    int workstationId = 0;
    if (!RegUtils.isNotNumeric(equipmentNumber))
      workstationId = Integer.parseInt(equipmentNumber);
    return workstationId;
  }

  /**
   * Used by assign material for handheld
   */
  private String getShippingManifestId(String floorLocationId, String customerId) {
    String shippingManifestId = "";

    //* find match for the floor location consolidating to
    //* with the same customer (and where its not the same shipping manifest)
    //* get the shipping manifest id where floor and the customer id
    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
        "select shipping_manifest_id from shipping_manifest " +
            "where floor_location_id=? and customer_id=? and manifest_date is NULL", 0)) {
      pstmt.setString(1, floorLocationId);
      pstmt.setString(2, customerId);

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          shippingManifestId = rs.getString(1);
        }
      }
    }
    catch (SQLException sqle) {
      AdfLogUtility.logException(sqle);
    }
    return shippingManifestId;
  }

  /**
   * Used by Shipping Handheld to associate a shipping row to a shipping manifest floor.
   *
   * @return HashMap
   */
  public Map<String, String> assignMaterialForHandheld(String floorLocationId, String customerId, String barcode,
                                                       String iUserId, String iWarehouseId) {
    log.debug("assignMaterial {} {} {}", floorLocationId, customerId, barcode);

    HashMap<String, String> hm = new HashMap<>();

    try {

      //* does the barcode exist, get the view
      ViewObjectImpl barcodeView = getListBarcodeInfo();

      //* setting to 1, where -1 returns all rows, setting to n will return n rows
      barcodeView.setRangeSize(-1);

      barcodeView.setWhereClause("UPPER(CONSOLIDATION_BARCODE) = :consolidationBarcode");
      barcodeView.defineNamedWhereClauseParam("consolidationBarcode", null, null);
      barcodeView.setNamedWhereClauseParam("consolidationBarcode", barcode);

      //* fill the view with barcode for review
      barcodeView.executeQuery();
      Row[] barcodeRows = barcodeView.getAllRowsInRange();

      ShippingFloor shippingFloor = new ShippingFloor();
      shippingFloor.setFloorLocationId(Integer.parseInt(floorLocationId));

      //* process all barcodeRows which have never been reviewed (no lastReviewDate)
      //* then display table to the user with messages (e.g., conflicts)
      int rowFoundCount = 0;
      int rowAssignedCount = 0;
      int rowAssignedCountError = 0;
      for (Row barcodeRow : barcodeRows) {
        boolean rowFound = false;

        Object objShippingManifestId = barcodeRow.getAttribute("ShippingManifestId");

        if (objShippingManifestId == null) {
          rowFound = true;
          //* assignMaterial
          int result =
              assignMaterial(String.valueOf(barcodeRow.getAttribute("ShippingId")), getCustomerId(String.valueOf(barcodeRow.getAttribute("ScnAac"))),
                  String.valueOf(barcodeRow.getAttribute("Tcn")), shippingFloor,
                  Integer.parseInt(iUserId), Integer.parseInt(iWarehouseId));

          if (result > 0) {
            rowAssignedCount++;
            log.debug("Assigned to shippingManifestId  {}", result);
            hm.put(String.valueOf(barcodeRow.getAttribute("Tcn")), "ASSIGNED");
          }
          else {
            rowAssignedCountError++;
            if (result == -999) {
              hm.put(String.valueOf(barcodeRow.getAttribute("Tcn")), "AS1 FAILED");
            }
            else {
              hm.put(String.valueOf(barcodeRow.getAttribute("Tcn")), "UNKNOWN ERROR");
            }
          }
        }

        if (!rowFound) {
          hm.put(String.valueOf(barcodeRow.getAttribute("Tcn")), "ALREADY ASSIGNED");
          rowFoundCount++;
        }
      }

      String msg = "";
      if (rowAssignedCount > 0)
        msg += rowAssignedCount + " SCANNED IN";
      if (rowFoundCount > 0) {
        if (rowAssignedCount > 0)
          msg += ", ";
        msg += rowFoundCount + " ALREADY IN";
      }
      if (rowAssignedCountError > 0) {
        if (rowAssignedCount > 0 || rowFoundCount > 0)
          msg += ", ";
        msg += rowAssignedCountError + " FAILED SCAN";
      }

      hm.put("message", msg);
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      hm.put("exception", "Unable to scan into shipping at this time.");
    }
    return hm;
  }

  /**
   * SQL Utility Method
   * Pass in a sql select statement with 0 parameters and get back first
   * field in resultset.
   *
   * @return String
   */
  public String getSingleReturnValue(String sql, String param) {
    String result = "";
    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(sql, 0)) {

      pstmt.setString(1, param);

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          result = rs.getString(1);
        }
      }
    }
    catch (SQLException sqle) {
      AdfLogUtility.logException(sqle);
    }
    return result;
  }

  /**
   * SQL Utility Method
   * Pass in a sql select statement with zero parameters and
   * get back first field in resultset.
   * REQUIRED: Field must be an int value.
   *
   * @return int
   */
  public int getCountValue(String sql) {
    int result = 0;
    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(sql, 0)) {
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          result = rs.getInt(1);
        }
      }
    }
    catch (SQLException sqle) {
      AdfLogUtility.logException(sqle);
    }
    return result;
  }

  /**
   * Container's getter for TransshipmentReceiptView1.
   */
  public ReceiptViewImpl getTransshipmentReceiptView1() {
    return (ReceiptViewImpl) findViewObject("TransshipmentReceiptView1");
  }

  /**
   * Container's getter for ShippingManifestView1.
   */
  public ShippingManifestViewImpl getShippingManifestView1() {
    return (ShippingManifestViewImpl) findViewObject("ShippingManifestView1");
  }

  /**
   * Container's getter for ListBarcodeInfo_INNER.
   */
  @SuppressWarnings("java:S100") //(FUTURE)  to rename this, iterators would need updated.  and unsure of ADF impact.
  public ConsolidationBarcodeInfoList_INNERImpl getListBarcodeInfo_INNER() {
    return (ConsolidationBarcodeInfoList_INNERImpl) findViewObject("ListBarcodeInfo_INNER");
  }

  /**
   * Container's getter for AllFloors_Pallet.
   */
  public AllFloorsImpl getAllFloorsPallet() {
    return (AllFloorsImpl) findViewObject("AllFloors_Pallet");
  }

  /**
   * Container's getter for TempManifestReview1.
   */
  public TempManifestReviewImpl getTempManifestReview1() {
    return (TempManifestReviewImpl) findViewObject("TempManifestReview1");
  }

  public WorkLoadManagerImpl getWorkLoadManagerService() {
    try {
      return (WorkLoadManagerImpl) getImportFilesService().getWorkLoadManager1();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return null;
  }

  public ImportFilesImpl getImportFilesService() {
    try {
      return (ImportFilesImpl) this.getImportFiles1();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return null;
  }

  public TransactionsImpl getTransactionsService() {
    try {
      return (TransactionsImpl) getWorkLoadManagerService().getTransactions1();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return null;
  }

  public GCSSMCTransactionsImpl getGCSSMCTransactionsService() {
    try {
      return getTransactionsService().getGCSSMCTransactions1();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return null;
  }

  /**
   * Container's getter for UnprocessBarcodeRptView1.
   */
  public UnprocessBarcodeRptViewImpl getUnprocessBarcodeRptView1() {
    return (UnprocessBarcodeRptViewImpl) findViewObject("UnprocessBarcodeRptView1");
  }

  /**
   * Container's getter for ManifestedTodayRptView1.
   */
  public ManifestedTodayRptViewImpl getManifestedTodayRptView1() {
    return (ManifestedTodayRptViewImpl) findViewObject("ManifestedTodayRptView1");
  }

  /**
   * Container's getter for UnmanifestRptView1.
   */
  public UnmanifestRptViewImpl getUnmanifestRptView1() {
    return (UnmanifestRptViewImpl) findViewObject("UnmanifestRptView1");
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

  public boolean isGCSSTransshipping(String shippingId) {
    boolean flag = false;
    try (PreparedStatement stR = getDBTransaction().createPreparedStatement(
        "select nvl(issue_type,'') from shipping s, issue i where s.scn=i.scn and s.shipping_id=? and i.issue_type='T'", 0)) {
      stR.setString(1, shippingId);
      try (ResultSet rs = stR.executeQuery()) {
        if (rs.next()) {
          flag = true;
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return flag;
  }

  /**
   * We do not send AS_ transactions for document_id BWA.
   */
  public boolean isBWA(String shippingId) {
    boolean flag = false;
    try (PreparedStatement stR = getDBTransaction().createPreparedStatement(
        "select nvl(document_id,'') from shipping s, issue i where s.scn=i.scn and s.shipping_id=? and i.document_id='BWA'", 0)) {
      stR.setString(1, shippingId);
      try (ResultSet rs = stR.executeQuery()) {
        if (rs.next()) {
          flag = true;
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return flag;
  }

  private boolean isBWAByManifest(String shippingManifestId) {
    boolean flag = false;
    try (PreparedStatement stR = getDBTransaction().createPreparedStatement(
        "select nvl(document_id,'') from shipping s, shipping_manifest m, issue i " +
            "where m.shipping_manifest_id=s.shipping_manifest_id and s.scn=i.scn " +
            "and m.shipping_manifest_id=? and document_id='BWA'", 0)) {
      stR.setString(1, shippingManifestId);
      try (ResultSet rs = stR.executeQuery()) {
        if (rs.next()) {
          flag = true;
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return flag;
  }

  private boolean isGCSSTransshippingByManifest(String shippingManifestId) {
    boolean flag = false;
    try (PreparedStatement stR = getDBTransaction().createPreparedStatement("select nvl(issue_type,'') from shipping s, shipping_manifest m, issue i where m.shipping_manifest_id=s.shipping_manifest_id and s.scn=i.scn and m.shipping_manifest_id=? and issue_type='T'", 0)) {
      stR.setString(1, shippingManifestId);
      try (ResultSet rs = stR.executeQuery()) {
        if (rs.next()) {
          flag = true;
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return flag;
  }

  /**
   * Returns ref_dasf_id if the document is on the DASF.
   *
   * @return boolean
   */
  public String isDASF(String documentNumber, String niin, int qty) {
    String refDasfId = "";
    log.debug("? {} {} ++ {}", documentNumber, niin, qty);

    String sql = "select ref_dasf_id, to_char(quantity_due) from ref_dasf " + "where document_number=?";
    if (StringUtils.isNotBlank(niin))
      sql += " and niin=?";

    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(sql, 0)) {

      pstmt.setString(1, documentNumber.trim());
      if (StringUtils.isNotBlank(niin))
        pstmt.setString(2, niin.trim());

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          refDasfId = rs.getString(1);

          int qty1 = Util.cleanInt(rs.getString(2));
          log.debug("quantity {}", qty1);
          if (qty1 < 1) {
            refDasfId = null;
          }
        }
      }
    }
    catch (SQLException sqle) {
      AdfLogUtility.logException(sqle);
    }
    if (StringUtils.isNotBlank(refDasfId))
      log.debug("is on DASF {} {} ++ {}", documentNumber, niin, qty);
    return refDasfId;
  }

  /**
   * Returns ref_dasf_id if the document is on the DASF.
   */
  public void updateDASF(String refDasfId, int qty) {

    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
        "update ref_dasf set quantity_due= " + "  case " + "    when ((to_char(quantity_due)-?) < 0) then 0 " +
            " else (to_char(quantity_due)-?) " + "  end " +
            " where ref_dasf_id=?", 0)) {

      pstmt.setString(1, String.valueOf(qty));
      pstmt.setString(2, String.valueOf(qty));
      pstmt.setString(3, refDasfId);

      pstmt.executeUpdate();
      getDBTransaction().commit();
    }
    catch (Exception sqle) {
      getDBTransaction().rollback();
      AdfLogUtility.logException(sqle);
    }
    if (StringUtils.isNotBlank(refDasfId))
      log.debug("update DASF {} ++ {}", refDasfId, qty);
  }

  /**
   * Container's getter for ImportFiles1.
   */
  public ApplicationModuleImpl getImportFiles1() {
    return (ApplicationModuleImpl) findApplicationModule("ImportFiles1");
  }

  /**
   * Container's getter for AllShippingAacs_Manifest.
   */
  @SuppressWarnings("java:S100") //(FUTURE) to rename this, ViewUsage xml would need updated.  and unsure of ADF impact.
  public ViewObjectImpl getAllShippingAacs_Manifest() {
    return (ViewObjectImpl) findViewObject("AllShippingAacs_Manifest");
  }

  /**
   * Container's getter for ListContainers_Remark.
   *
   * @return ListContainers_Remark
   */
  @SuppressWarnings("java:S100") //(FUTURE) to rename this, ViewUsage xml would need updated.  and unsure of ADF impact.
  public UndeliveredContainersListImpl getListContainers_Remark() {
    return (UndeliveredContainersListImpl) findViewObject("ListContainers_Remark");
  }

  /**
   * Container's getter for AllShippingFloors_Manifest.
   *
   * @return AllShippingFloors_Manifest
   */
  @SuppressWarnings("java:S100") //(FUTURE) to rename this, ViewUsage xml would need updated.  and unsure of ADF impact.
  public UnmanifestedFloorsListImpl getAllShippingFloors_Manifest() {
    return (UnmanifestedFloorsListImpl) findViewObject("AllShippingFloors_Manifest");
  }

  /**
   * Container's getter for AllCustomers_Consolidate.
   *
   * @return AllCustomers_Consolidate
   */
  @SuppressWarnings("java:S100") //(FUTURE) to rename this, ViewUsage xml would need updated.  and unsure of ADF impact.
  public ViewObjectImpl getAllCustomers_Consolidate() {
    return (ViewObjectImpl) findViewObject("AllCustomers_Consolidate");
  }
}
