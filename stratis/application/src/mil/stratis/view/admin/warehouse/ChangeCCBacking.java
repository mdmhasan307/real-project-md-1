package mil.stratis.view.admin.warehouse;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.common.util.Util;
import mil.stratis.model.services.AppModuleImpl;
import mil.stratis.model.services.PickingAMImpl;
import mil.stratis.model.services.WarehouseSetupImpl;
import mil.stratis.view.BackingHandler;
import mil.stratis.view.util.ADFUtils;
import mil.stratis.view.util.JSFUtils;
import mil.usmc.mls2.stratis.core.utility.AdfLogUtility;
import mil.usmc.mls2.stratis.core.utility.ContextUtils;
import mil.usmc.mls2.stratis.core.utility.GlobalConstants;
import oracle.adf.model.binding.DCIteratorBinding;
import oracle.adf.view.rich.component.rich.input.RichInputText;
import oracle.adf.view.rich.component.rich.input.RichSelectOneChoice;
import oracle.binding.OperationBinding;
import oracle.jbo.Row;
import org.apache.myfaces.trinidad.context.RequestContext;

import javax.faces.event.ActionEvent;
import java.sql.Date;

@Slf4j
public class ChangeCCBacking extends BackingHandler {

  public static final String SERIAL_FLAG = "serialFlag";
  public static final String SERIAL_UPDATE_FLAG = "serialUpdateFlag";
  private transient RichSelectOneChoice ccupdatetext;
  private transient RichSelectOneChoice ccUpdate;
  boolean renderFlag = false;
  boolean renderFlag2 = false;
  boolean disableTable = false;
  int iUserId;
  boolean resetFlag = false;

  private boolean serialFlag = false;
  private boolean serialUpdateFlag = false;
  private transient RichInputText serialOrLotNum;

  public ChangeCCBacking() {

    Object id = JSFUtils.getManagedBeanValue("userbean.userId");
    if (id != null) {
      try {
        //* Convert the id object to an int
        iUserId = Integer.parseInt(id.toString());
      }
      catch (Exception e) {
        iUserId = 0;
      }
    }

    RequestContext afContext = RequestContext.getCurrentInstance();
    if (!afContext.isPostback()) {
      setInitialSerialFlags();
    }
  }

  /**
   * This function populates the table for a location.
   */
  @SuppressWarnings("unused")
  public void getWarehouse(ActionEvent actionEvent) {
    // enable tables, render components for use
    setDisableTable(true);
    setRenderFlag2(true);

    //* for use in the web, maintaining state
    RequestContext afContext = RequestContext.getCurrentInstance();
    afContext.getPageFlowScope().put("disableTable", disableTable);
    afContext.getPageFlowScope().put("renderFlag2", renderFlag2);

    AppModuleImpl service = getAppModule();
    service.getLocations();
    ccupdatetext.setValue(null);
  }

  /**
   * Reset the form.
   */
  @SuppressWarnings("unused")
  public void resetTable(ActionEvent actionEvent) {
    // enable tables, render components for use
    setDisableTable(false);
    setRenderFlag2(false);

    //* for use in the web, maintaining state
    RequestContext afContext = RequestContext.getCurrentInstance();
    afContext.getPageFlowScope().put("disableTable", disableTable);
    afContext.getPageFlowScope().put("renderFlag2", renderFlag2);

    ccupdatetext.setValue(null);
  }

  public final void setInitialSerialFlags() {
    RequestContext afContext = RequestContext.getCurrentInstance();
    Object obj = afContext.getPageFlowScope().get(SERIAL_FLAG);
    if (obj == null)
      setSerialFlag(false);

    Object obj2 = afContext.getPageFlowScope().get(SERIAL_UPDATE_FLAG);
    if (obj2 == null)
      setSerialUpdateFlag(false);
  }

  /**
   * Set serial flag back on (true) to turn on readonly+style
   * and hide certain ui buttons on Warehouse Management - Change CC.
   */
  public void setSerialFlag(boolean show) {
    RequestContext afContext = RequestContext.getCurrentInstance();
    //* for use in the web, maintaining state
    afContext.getPageFlowScope().put(SERIAL_FLAG, show);
    this.serialFlag = show;
  }

  public boolean getSerialFlag() {
    RequestContext afContext = RequestContext.getCurrentInstance();
    boolean flag = serialFlag;
    Object obj = afContext.getPageFlowScope().get(SERIAL_FLAG);
    if (obj != null)
      flag = Boolean.parseBoolean(obj.toString());

    return serialFlag || flag;
  }

  public void setSerialUpdateFlag(boolean show) {
    RequestContext afContext = RequestContext.getCurrentInstance();
    //* for use in the web, maintaining state
    afContext.getPageFlowScope().put(SERIAL_UPDATE_FLAG, show);
    this.serialUpdateFlag = show;
  }

  public boolean getSerialUpdateFlag() {
    RequestContext afContext = RequestContext.getCurrentInstance();
    boolean flag = serialUpdateFlag;
    Object obj = afContext.getPageFlowScope().get(SERIAL_UPDATE_FLAG);
    if (obj != null)
      flag = Boolean.parseBoolean(obj.toString());

    return serialUpdateFlag || flag;
  }

  @SuppressWarnings("unused")
  public void addSerialLotNum(ActionEvent actionEvent) {
    PickingAMImpl service = getPickingAMService();
    RequestContext afContext = RequestContext.getCurrentInstance();
    Object serialControlFlag = afContext.getPageFlowScope().get("serialControlFlag");
    String title = (Util.cleanString(serialControlFlag).equals("Y")) ? "SERIAL NUMBER" : "LOT CONTROL NUMBER";
    String result = "";
    if (getSerialOrLotNum().getValue() == null) {
      JSFUtils.addFacesErrorMessage("INVALID INPUT",
          "Please enter " + title + ".");
      return;
    }

    Object niinId = afContext.getPageFlowScope().get("niin_id");
    Object locationIds = afContext.getPageFlowScope().get("locationIds");
    Object qtyToBeChanged = afContext.getPageFlowScope().get("qtyToBeChanged");
    if (service.getSerialOrLotNumList1().getEstimatedRowCount() > Util.cleanInt(qtyToBeChanged)) {
      JSFUtils.addFacesErrorMessage("SERIAL OR LOT NUMBER",
          "You may not exceed the total quantity for this Change Condition Code request.");
      return;
    }

    if (niinId != null) {
      result = service.addToSerialLotList(Util.trimUpperCaseClean(getSerialOrLotNum().getValue()),
          niinId.toString(), "1", (Util.cleanString(serialControlFlag).equals("Y")), Util.cleanString(locationIds) + ",",
          false);
    }
    if (result.length() > 0) {
      val globalConstants = ContextUtils.getBean(GlobalConstants.class);
      JSFUtils.addFacesErrorMessage(globalConstants.getErrorTag(), result);
    }
    getSerialOrLotNum().setValue("");
  }

  @SuppressWarnings("unused")
  public void deleteSerialLotNum(ActionEvent actionEvent) {
    this.getPickingAMService().getSerialOrLotNumList1().removeCurrentRowFromCollection();
  }

  public void goback() {
    setSerialUpdateFlag(false);
    setSerialFlag(false);
  }

  @SuppressWarnings("unused")  //called from .jspx
  public void changeWarehouseCC(ActionEvent actionEvent) {
    //* verify if serial or not
    Object niin;
    Object serialControlFlag;
    try {
      DCIteratorBinding ccchangeit = ADFUtils.findIterator("NiinLoc_LocationView1Iterator");
      Row row = ccchangeit.getCurrentRow();
      Object currentCC = row.getAttribute("Cc");
      Object niinid = row.getAttribute("NiinId");
      Object niinlocid = row.getAttribute("NiinLocId");
      Object locid = row.getAttribute("LocationId");
      log.debug("cc= {}, NiinId= {}, NiinLocation= {}", currentCC.toString(),
          niinid.toString(),
          niinlocid.toString());

      //*  do nothing if same cc selected to change to
      if (ccupdatetext.getValue().equals(Util.cleanString(currentCC))) {
        val globalConstants = ContextUtils.getBean(GlobalConstants.class);
        JSFUtils.addFacesErrorMessage(globalConstants.getErrorTag(), "Must select a different Condition Code to change to.");
        goback();
        return;
      }

      Object qty = row.getAttribute("Qty");
      if (Util.cleanInt(qty) < 1) {
        JSFUtils.addFacesErrorMessage("CHANGE CC ERROR", "Must select a location with quantity greater than 0 " + currentCC + ".");
        goback();
        return;
      }

      //MCF-05-13-2014: CHECK IF THERE'S WORKLOAD OUT FOR NIIN_LOCATION
      //PICK, INVENTORY, and STOW
      String message = checkDependencies(niinlocid, locid);
      if (!message.isEmpty()) {
        JSFUtils.addFacesErrorMessage(message);
        return;
      }

      niin = row.getAttribute("Niin");
      Object locationLabel = row.getAttribute("LocationLabel");
      //* cannot change cc to another condition code if location has more than one niin
      //* must change location first - added 12/12/08
      AppModuleImpl appService = getAppModule();
      val cc = currentCC.toString();
      if (appService.checkCCNIIN(Util.cleanString(locid), cc)) {
        JSFUtils.addFacesErrorMessage("CHANGE CC ERROR", "Cannot change due to multiple NIIN in this location " + locationLabel + "\n with condition code of " + cc + ". \nChange Location for this niin " + niin.toString() + " and try again.");
        goback();
        return;
      }

      serialControlFlag = row.getAttribute("SerialControlFlag");

      //* if serial or lot control, add variables to process scope
      String newCC;
      //*  update the database
      if (ccupdatetext.getValue().equals("A")) {
        row.setAttribute("Cc", "A");
        newCC = "A";
      }
      else {
        //* removed 12/8/08 - never lock, will not be able to pick for a5j, bwa, a5a condition code F for example
        //* UnLock location if change cc from A to F
        newCC = "F";
        row.setAttribute("Cc", "F");
      }
      row.setAttribute("Locked", "N");
      row.setAttribute("ModifiedBy", iUserId);
      row.setAttribute("ModifiedDate", new Date(System.currentTimeMillis()));

      // commit the change
      ccchangeit.getDataControl().commitTransaction();
      log.debug("Niin_Location updated.");
      // send the transaction
      AppModuleImpl appModule = getAppModule();

      int result;

      if (Util.cleanString(serialControlFlag).equals("Y")) {
        log.debug("SERIALIZED");
        result = appModule.sendDACGCSSMCTransactionsSerial(Integer.parseInt(niinid.toString()), Integer.parseInt(niinlocid.toString()), Integer.parseInt(locid.toString()), currentCC.toString(), newCC);
        //DACS need to be sent for each serial number in GCSS.
        //Need to update all serial_lot_num_track entries to the correct CC.
      }
      else {
        result = appModule.sendDACGCSSMCTransaction(Integer.parseInt(niinid.toString()),
            Integer.parseInt(niinlocid.toString()),
            currentCC.toString(), null, null, qty);
      }

      if (result < 0) {
        val globalConstants = ContextUtils.getBean(GlobalConstants.class);
        JSFUtils.addFacesErrorMessage(globalConstants.getErrorTag(), "An error occurred while sending DAC transaction");
        //* rollback - undo the condition code change
        if (ccupdatetext.getValue().equals("A")) {
          row.setAttribute("Cc", "F");
        }
        else {
          row.setAttribute("Cc", "A");
        }
        // commit the change
        ccchangeit.getDataControl().commitTransaction();
        log.debug("Niin_Location rollback updated.");
      }
      else {
        log.debug("DAC Transaction sent.");

        // update the view
        ccchangeit.executeQuery();
        OperationBinding op = ADFUtils.getDCBindingContainer().getOperationBinding("RefreshChangeLoc");
        op.execute();
        JSFUtils.addFacesInformationMessage("Condition Code was successfully changed to " + newCC + " for NIIN: " + niin);
      }

      //* clear field only on success
      ccupdatetext.setValue(null);
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  //Checks dependencies for a change CC action (05-13-2014 - MCF)
  private String checkDependencies(Object niinlocid, Object locid) {
    WarehouseSetupImpl service = getWarehouseSetupModule();
    //Checks Picks
    int count = service.checkDependencies("select count(*) from picking p where p.niin_loc_id = ? AND (((UPPER(p.STATUS) = 'PICK READY') OR (UPPER(p.STATUS) = 'PICK BYPASS1')))", Integer.parseInt(niinlocid.toString()));
    if (count > 0) {
      return "Change CC failed. Picks outstanding for this NIIN Location.";
    }

    //Checks regular inventories
    count = service.checkDependencies("select count(*) from inventory_item where niin_loc_id = ?", Integer.parseInt(niinlocid.toString()));
    if (count > 0) {
      return "Change CC failed. Inventories outstanding for this NIIN Location.";
    }

    //Checks location surveys
    count = service.checkDependencies("select count(*) from inventory_item where inv_type='LOCSURVEY' and location_id = ?", Integer.parseInt(locid.toString()));
    if (count > 0) {
      return "Change CC failed. Location Surveys outstanding for this Location.";
    }

    //Checks Stows
    count = service.checkDependencies("select count(*) from stow s inner join receipt r on s.rcn = r.rcn inner join niin_location nl on nl.location_id = s.location_id and nl.niin_id = r.niin_id where nl.niin_loc_id = ?", Integer.parseInt(niinlocid.toString()));
    if (count > 0) {
      return "Change CC failed. Stows outstanding for this NIIN Location.";
    }

    return "";
  }

  public void setRenderFlag(boolean renderFlag) {
    this.renderFlag = renderFlag;
  }

  public boolean getRenderFlag() {
    return renderFlag;
  }

  public void setRenderFlag2(boolean renderFlag2) {
    this.renderFlag2 = renderFlag2;
  }

  public boolean getRenderFlag2() {
    return renderFlag2;
  }

  public void setDisableTable(boolean disableTable) {
    this.disableTable = disableTable;
  }

  public boolean getDisableTable() {
    return disableTable;
  }

  public void setResetFlag(boolean resetFlag) {
    this.resetFlag = resetFlag;

    // clear the last values
    ccupdatetext.setValue(null);
    resetTable(null);
  }

  public boolean getResetFlag() {
    return resetFlag;
  }

  public void setCcupdatetext(RichSelectOneChoice ccupdatetext) {
    this.ccupdatetext = ccupdatetext;
  }

  public RichSelectOneChoice getCcupdatetext() {
    return ccupdatetext;
  }

  public void setCcUpdate(RichSelectOneChoice ccUpdate) {
    this.ccUpdate = ccUpdate;
  }

  public RichSelectOneChoice getCcUpdate() {
    return ccUpdate;
  }

  public void setIUserId(int iUserId) {
    this.iUserId = iUserId;
  }

  public int getIUserId() {
    return iUserId;
  }

  public boolean isSerialFlag() {
    return serialFlag;
  }

  public boolean isSerialUpdateFlag() {
    return serialUpdateFlag;
  }

  public void setSerialOrLotNum(RichInputText serialOrLotNum) {
    this.serialOrLotNum = serialOrLotNum;
  }

  public RichInputText getSerialOrLotNum() {
    return serialOrLotNum;
  }
}
