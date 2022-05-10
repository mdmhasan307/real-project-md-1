package mil.stratis.view.admin.warehouse;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.common.util.Util;
import mil.stratis.model.services.AppModuleImpl;
import mil.stratis.model.services.PickingAMImpl;
import mil.stratis.model.services.SysAdminImpl;
import mil.stratis.model.view.pick.SerialOrLotNumListImpl;
import mil.stratis.view.admin.AdminBackingHandler;
import mil.stratis.view.util.ADFUtils;
import mil.stratis.view.util.JSFUtils;
import mil.usmc.mls2.stratis.core.utility.AdfLogUtility;
import mil.usmc.mls2.stratis.core.utility.ContextUtils;
import mil.usmc.mls2.stratis.core.utility.DateConstants;
import mil.usmc.mls2.stratis.core.utility.GlobalConstants;
import oracle.adf.model.binding.DCIteratorBinding;
import oracle.adf.view.rich.component.rich.RichPopup;
import oracle.adf.view.rich.component.rich.data.RichTable;
import oracle.adf.view.rich.component.rich.input.RichInputDate;
import oracle.adf.view.rich.component.rich.input.RichInputText;
import oracle.adf.view.rich.component.rich.input.RichSelectBooleanCheckbox;
import oracle.adf.view.rich.component.rich.input.RichSelectOneChoice;
import oracle.adf.view.rich.context.AdfFacesContext;
import oracle.adf.view.rich.event.DialogEvent;
import oracle.jbo.Key;
import oracle.jbo.Row;
import oracle.jbo.RowSetIterator;
import oracle.jbo.ViewObject;
import oracle.jbo.uicli.binding.JUCtrlValueBindingRef;
import org.apache.commons.lang3.StringUtils;
import org.apache.myfaces.trinidad.context.RequestContext;

import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
public class NiinInquiryBacking extends AdminBackingHandler {

  public static final String NIIN_INQUIRY_FLAG = "niinInquiryFlag";
  public static final String NIIN_INQUIRY_UPDATE_FLAG = "niinInquiry_UpdateFlag";
  public static final String NIIN_INQUIRY_SERIAL_FLAG = "niinInquiry_SerialFlag";
  public static final String INVALID_INPUT = "INVALID INPUT";
  public static final String NIIN_ID = "niinId";
  private transient RichTable niinTable;
  private transient RichTable niinFilteredTable;
  private transient RichTable niinLocationTable;
  private transient RichTable niinSerialTable;
  private transient RichTable locationTable;
  private transient RichInputText txtNiin;
  private transient RichInputText txtQty;
  private transient RichInputText txtLocation;
  private transient RichInputDate txtDOM;
  private transient RichInputDate txtExpDate;

  private transient RichInputText txtLotControl;
  private transient RichInputText txtSerial;
  private transient RichInputText startSerial;
  private transient RichInputText endSerial;

  private transient RichSelectOneChoice selectCC;
  private transient RichSelectOneChoice selectAudit;
  private transient RichSelectOneChoice selectLocked;

  private boolean niinInquiryFlag = false;
  private boolean niinInquiryUpdateFlag = false;
  private boolean niinInquirySerialFlag = false;
  private boolean niinInquiryFilterFlag;
  int iUserId;

  //* For immediate transaction generation
  private transient RichInputText txtImmedQtyTrans;
  private transient RichSelectOneChoice selectImmedQtyTrans;
  private transient RichSelectOneChoice selectImmedCCTrans;
  private transient RichSelectOneChoice selectGenTrans;
  private transient RichSelectBooleanCheckbox chkGenTrans;

  //* for Serial
  private transient RichInputText txtNiinSerial;
  private transient RichInputText txtSerialAdd;
  private transient RichInputText txtLotAdd;
  private transient RichTable serialTable;
  private transient RichSelectOneChoice selectGenTrans2;
  private transient RichSelectBooleanCheckbox chkGenTrans2;

  // For filtering
  private transient RichInputText txtNiinFilter;
  private transient RichInputText txtNomenclatureFilter;
  private transient RichInputText serialOrLotNum;

  private static String niinInfoIterator = "NiinInfoView_CC_Change2Iterator";
  private static String niinLocationIterator = "NiinLoc_LocationView_CC_Change2Iterator";

  private List<SelectItem> allSerialLocation;
  protected transient RichSelectOneChoice serialLocationNavList;

  protected transient RichSelectOneChoice serialLocationRemoveNavList;

  boolean requireQtyFlag = false;

  public NiinInquiryBacking() {

    //* required to show initial niin update screen with grey, read only and buttons undisabled
    RequestContext afContext = RequestContext.getCurrentInstance();
    if (!afContext.isPostback()) {
      setInitialNiinInquiryFlags();
    }

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
  }

  public final void setInitialNiinInquiryFlags() {
    RequestContext afContext = RequestContext.getCurrentInstance();
    Object obj = afContext.getPageFlowScope().get(NIIN_INQUIRY_FLAG);
    if (obj == null)
      setNiinInquiryFlag(false);

    Object obj2 = afContext.getPageFlowScope().get(NIIN_INQUIRY_UPDATE_FLAG);
    if (obj2 == null)
      setNiinInquiryUpdateFlag(false);

    Object obj3 = afContext.getPageFlowScope().get(NIIN_INQUIRY_SERIAL_FLAG);
    if (obj3 == null)
      setNiinInquirySerialFlag(false);
  }

  @SuppressWarnings("unused")
  public void addSerialLotNum(ActionEvent actionEvent) {
    PickingAMImpl service = getPickingAMService();
    RequestContext afContext = RequestContext.getCurrentInstance();
    String result = "";
    if ((this.getSerialOrLotNum().getValue() == null) && ((this.getStartSerial().getValue() == null) || (this.getEndSerial().getValue() == null))) {
      JSFUtils.addFacesErrorMessage(INVALID_INPUT,
          "Please enter " + "serial number(s)" + ".");
      return;
    }

    long niinId = 0;
    long locationId = 0;
    Object obj = afContext.getPageFlowScope().get(NIIN_ID);
    Object locationobj = afContext.getPageFlowScope().get("locationId");
    if (obj != null)
      niinId = Long.parseLong(obj.toString());

    if (locationobj != null)
      locationId = Long.parseLong(locationobj.toString());

    if (isEmpty(getTxtQty())) {
      val globalConstants = ContextUtils.getBean(GlobalConstants.class);
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "Quantity");
      return;
    }
    else if (isNaN(getTxtQty())) {
      JSFUtils.addFacesErrorMessage(INVALID_INPUT, "Quantity must be a valid positive number.");
      return;
    }

    Object totalPickedQty = getTxtQty().getValue().toString();

    if (service.getSerialOrLotNumList1().getEstimatedRowCount() >= Integer.parseInt(totalPickedQty.toString())) {
      JSFUtils.addFacesErrorMessage("SERIAL OR LOT NUMBER",
          "You may not exceed the total quantity.");
      return;
    }

    if (niinId != 0) {
      if ((this.getSerialOrLotNum().getValue() != null) && (this.getSerialOrLotNum().getValue().toString().length() > 0)) {
        result = service.addToSerialLotListInquiry(Util.trimUpperCaseClean(getSerialOrLotNum().getValue()),
            niinId, locationId);
      }
      else {
        ArrayList<String> serialList = new ArrayList<>();
        result = getWorkloadManagerService().generateSerialRange(getStartSerial().getValue().toString(), getEndSerial().getValue().toString(), serialList);
        if (result == null || result.length() <= 0) {
          for (String o : serialList) {
            result = service.addToSerialLotListInquiry(
                o.trim().toUpperCase(),
                niinId, locationId);
          }
          this.getEndSerial().setValue("");
          this.getStartSerial().setValue("");
        }
      }
      SerialOrLotNumListImpl vo = service.getSerialOrLotNumList1();
      vo.setSortBy("SerialOrLoNum");
      vo.setQueryMode(ViewObject.QUERY_MODE_SCAN_VIEW_ROWS);
      vo.executeQuery();
    }
    if (StringUtils.isNotBlank(result)) {
      JSFUtils.addFacesErrorMessage("ERROR", result);
    }
    this.getSerialOrLotNum().setValue("");
  }

  public void dialogListener(DialogEvent dialogEvent) {
    if (dialogEvent.getOutcome().equals(DialogEvent.Outcome.ok)) {
      RichPopup popup;
      deleteSerialLotNum(null);
      popup = (RichPopup) JSFUtils.findComponentInRoot("confirmDelete");
      popup.hide();

      //Refresh accTable
      AdfFacesContext.getCurrentInstance().addPartialTarget(serialTable);
    }
  }

  @SuppressWarnings("unused")
  public void deleteSerialLotNum(ActionEvent actionEvent) {
    try {
      this.getPickingAMService().getSerialOrLotNumList1().removeCurrentRowFromCollection();
    }
    catch (Exception e) {log.trace("Exception caught and ignored", e);}
  }

  /**
   * Used to generate GCSS transactions without updating the NIIN_LOCATION qty
   * Only for non-controlled items.
   */
  @SuppressWarnings("unused")
  public void submitImmedQtyTrans(ActionEvent actionEvent) {
    boolean error = false;
    //* reset the flags
    setNiinInquiryFlag(false);
    setNiinInquiryUpdateFlag(false);

    String dNumber = "8";
    String type = "Z";
    String transType = "";
    int qty = 0;

    //* Check all required fields first
    if (isEmpty(getTxtImmedQtyTrans())) {
      val globalConstants = ContextUtils.getBean(GlobalConstants.class);
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "Immediate Quantity for Transaction");
      error = true;
    }
    else if (isNaN(getTxtImmedQtyTrans())) {
      JSFUtils.addFacesErrorMessage(INVALID_INPUT, "Immediate Quantity for Transaction must be a valid positive number greater than 0.");
      error = true;
    }

    if (!error) {
      qty = Util.cleanInt(getTxtImmedQtyTrans().getValue());
      if (qty <= 0) {
        JSFUtils.addFacesErrorMessage(INVALID_INPUT, "Immediate Quantity for Transaction must be a valid positive number greater than 0.");
        error = true;
      }
    }

    val globalConstants = ContextUtils.getBean(GlobalConstants.class);
    if (isEmpty(getSelectImmedCCTrans())) {
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "Immediate Condition Code must be selected (i.e., A or F)");
      error = true;
    }
    if (isEmpty(getSelectImmedQtyTrans())) {
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "Immediate Transaction Type must be selected (e.g., D8Z)");
      error = true;
    }
    else {
      transType = getSelectImmedQtyTrans().getValue().toString();
      dNumber = String.valueOf(transType.charAt(1));
      type = String.valueOf(transType.charAt(2));
    }

    if (error) {
      return;
    }

    Key theKey = null;
    String selectedKey = "";
    Set<Key> selection = (Set) getNiinTable().getSelectedRowKeys();
    for (Key key : selection) {
      theKey = key;
      int attrsInKey = key.getAttributeCount();
      if (attrsInKey > 0) {
        try {
          selectedKey = key.getAttributeValues()[0].toString();
        }
        catch (Exception e) {
          //* null pointer exception
          return;
        }
      }
    }

    //* do not allow serial and lot control
    //* maximum qty and locked or under_audit do not matter
    if (selectedKey == null || selectedKey.equals("")) {
      displayMessage("[ERROR] Unknown Record selection.");
      return;
    }

    try {

      if (theKey != null) {
        //* get the iterator used to populate the storage bin table
        DCIteratorBinding iter = ADFUtils.findIterator(niinInfoIterator);

        //* remove row from table
        RowSetIterator rsIter = iter.getRowSetIterator();
        Row row = rsIter.getRow(theKey);
        Object niin = row.getAttribute("Niin");
        int niinId = Integer.parseInt(row.getAttribute("NiinId").toString());

        Object serialFlag = row.getAttribute("SerialControlFlag");
        Object lotControlFlag = row.getAttribute("LotControlFlag");

        //**********************
        //* if the NIIN is serial or lot controlled, admin will have rules
        boolean isSerial = false;
        boolean isLot = false;
        if (Util.cleanString(serialFlag).equals("Y")) {
          isSerial = true;
        }
        if (Util.cleanString(lotControlFlag).equals("Y")) {
          isLot = true;
        }

        Integer userId = Integer.parseInt(JSFUtils.getManagedBeanValue("userbean.userId").toString());

        if (!isSerial && !isLot) {
          SysAdminImpl service = getSysAdminModule();
          service.generateD(niinId, qty, Util.cleanString(getSelectImmedCCTrans().getValue()), dNumber, type, userId);
          displaySuccessMessage(transType + " Transaction was successfully generated for NIIN " + niin + ".");
        }
        else {
          displayMessage("Must be in GCSS mode to generate an Immediate transaction (non-controlled items only).  ");
        }

        //* reset values
        getTxtImmedQtyTrans().setValue(null);
        getSelectImmedQtyTrans().setValue(null);
        getSelectImmedCCTrans().setValue(null);
      }
    }
    catch (Exception e) {
      displayMessage("[ERROR] Error occurred while trying to update ");
    }
  }

  @SuppressWarnings("unused")
  public void submitSaveNiinLocation(ActionEvent actionEvent) {
    RequestContext afContext = RequestContext.getCurrentInstance();
    boolean error = false;
    //* Check all required fields first
    if (isEmpty(getTxtQty())) {
      JSFUtils.addFacesErrorMessage("MISSING REQUIRED FIELD", "Quantity is required");
      error = true;
    }
    else if (isNaN(getTxtQty())) {
      JSFUtils.addFacesErrorMessage(INVALID_INPUT, "Quantity must be a valid positive number.");
      error = true;
    }
    else if (isEmpty(getTxtExpDate())) {
      JSFUtils.addFacesErrorMessage(INVALID_INPUT, "Expiration Date is required.");
      error = true;
    }

    if (getNiinInquirySerialFlag() && getPickingAMService().getSerialOrLotNumList1().getEstimatedRowCount() != Integer.parseInt(getTxtQty().getValue().toString())) {
      JSFUtils.addFacesErrorMessage(INVALID_INPUT, "Number of entered serial numbers does not match New Quantity.");
      error = true;
    }

    if (getNiinInquiryUpdateFlag() && isEmpty(getSelectCC())) {
      JSFUtils.addFacesErrorMessage("MISSING REQUIRED FIELD", "Condition Code (CC) is required.");
      error = true;
    }
    if (error)
      return;

    DateFormat shortFormat = new SimpleDateFormat(DateConstants.SITE_DATE_INPUT_FORMATTER_PATTERN);
    String expDate = "";
    String dom = "";
    Date dateExpiration;
    boolean didParse = false;
    try {
      expDate = getTxtExpDate().getValue().toString();

      if (getTxtExpDate().getValue() != null) {
        dateExpiration = (Date) getTxtExpDate().getValue();
        expDate = shortFormat.format(dateExpiration);
        didParse = true;
      }
      else {
        expDate = DateConstants.DEFAULT_EXPIRATION_DATE;
        didParse = true;
      }
    }
    catch (Exception e) {log.trace("Exception caught and ignored", e);}

    if (!didParse) {
      expDate = getTxtExpDate().getValue().toString();
      expDate = expDate.substring(0, 10);
    }

    try {
      int niinId = 0;

      Object obj = afContext.getPageFlowScope().get(NIIN_ID);
      if (obj != null)
        niinId = Integer.parseInt(obj.toString());

      if (niinId != 0) {
        int origQty = Util.cleanInt(afContext.getPageFlowScope().get("orig_qty"));
        int newQty = Util.cleanInt(getTxtQty().getValue());
        Object locationLabel = getTxtLocation().getValue();

        int niinLocId;
        SysAdminImpl service = getSysAdminModule();

        if (getNiinInquiryUpdateFlag()) {
          Object niinLocationId = afContext.getPageFlowScope().get("niinLocationId");
          niinLocId = Integer.parseInt(niinLocationId.toString());
          if (chkGenTrans != null) {
            getChkGenTrans().isSelected();
          }
          int result = service.updateNiinLoc(niinLocId, niinId, getTxtQty().getValue().toString(), iUserId);

          if (result >= 0) {
            service.updateNiinLocDates(niinLocId, dom, expDate);
            //* check if transaction should be generated
            //Do the serial number flags.
            if (getNiinInquirySerialFlag()) {
              getPickingAMService().createSerialInquiry(niinId, niinLocId, getSelectCC().getValue().toString(), expDate);
            }

            displaySuccessMessage("NIIN " + getTxtNiin().getValue() + " was successfully updated location " + locationLabel.toString() + ".");

            //03-15-2013 Logging portion: MCF
            log.debug("NIIN Inquiry Update: userId:{} NIINId:{} old qty:{} new qty: {}", iUserId, niinId, origQty, newQty);

            //* refresh the niin locations to show just added
            DCIteratorBinding iter = ADFUtils.findIterator(niinLocationIterator);
            iter.executeQuery();
            setNiinInquiryFlag(false);
            setNiinInquiryUpdateFlag(false);
          }
          else {
            displayMessage("FAILED -- An error occurred while updating existing location.  Click Cancel to continue");
          }
        }
      }
      else {
        displayMessage("FAILED -- niin id and location id is invalid.  Click Cancel to continue");
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  /**
   * Set niin inquiry flag back on (true) to turn on readonly+style
   * and hide certain ui buttons on Warehouse Management - NIIN Inquiry.
   */
  public void setNiinInquiryFlag(boolean show) {
    RequestContext afContext = RequestContext.getCurrentInstance();
    //* for use in the web, maintaining state
    afContext.getPageFlowScope().put(NIIN_INQUIRY_FLAG, show);
    this.niinInquiryFlag = show;
  }

  public boolean getNiinInquiryFlag() {
    RequestContext afContext = RequestContext.getCurrentInstance();
    boolean flag = niinInquiryFlag;
    Object obj = afContext.getPageFlowScope().get(NIIN_INQUIRY_FLAG);
    if (obj != null)
      flag = Boolean.parseBoolean(obj.toString());

    return niinInquiryFlag || flag;
  }

  public void setNiinInquiryUpdateFlag(boolean show) {
    RequestContext afContext = RequestContext.getCurrentInstance();
    //* for use in the web, maintaining state
    afContext.getPageFlowScope().put(NIIN_INQUIRY_UPDATE_FLAG, show);
    this.niinInquiryUpdateFlag = show;
  }

  public boolean getNiinInquiryUpdateFlag() {
    RequestContext afContext = RequestContext.getCurrentInstance();
    boolean flag = niinInquiryUpdateFlag;
    Object obj = afContext.getPageFlowScope().get(NIIN_INQUIRY_UPDATE_FLAG);
    if (obj != null)
      flag = Boolean.parseBoolean(obj.toString());

    return niinInquiryUpdateFlag || flag;
  }

  public void setNiinInquirySerialFlag(boolean show) {
    RequestContext afContext = RequestContext.getCurrentInstance();
    //* for use in the web, maintaining state
    afContext.getPageFlowScope().put(NIIN_INQUIRY_SERIAL_FLAG, show);
    this.niinInquirySerialFlag = show;
  }

  public boolean getNiinInquirySerialFlag() {
    RequestContext afContext = RequestContext.getCurrentInstance();
    boolean flag = niinInquirySerialFlag;
    Object obj = afContext.getPageFlowScope().get(NIIN_INQUIRY_SERIAL_FLAG);
    if (obj != null)
      flag = Boolean.parseBoolean(obj.toString());

    return niinInquirySerialFlag || flag;
  }

  public void setNiinInquiryFilterFlag(boolean show) {
    this.niinInquiryFilterFlag = show;
  }

  public boolean getNiinInquiryFilterFlag() {
    return niinInquiryFilterFlag;
  }

  /**
   * ADD EXISTING NIIN TO NEW LOCATION.
   */
  @SuppressWarnings("unused")
  public void addNiinInquiryParams(ActionEvent event) {
    boolean hasLocation = false;
    Object niin = null;
    Object niinId = null;
    Object nomenclature = null;
    Object lotControlFlag = null;
    Object serialFlag = null;
    Object cc = null;
    Object underAudit = null;
    Object locked = null;
    Object expirationDate = null;
    Object dom = null;
    Object serialNumber = null;
    Object lotControlNumber = null;
    Object qty = null;
    Object locationId = null;
    Object locationLabel = null;
    Object niinLocationId = null;

    Object oldRowKey = niinTable.getRowKey();
    Iterator<Object> selection = niinTable.getSelectedRowKeys().iterator();
    Object oldRowKey2 = null;
    try {
      while (selection.hasNext()) {
        Object rowKey = selection.next();
        niinTable.setRowKey(rowKey);
        JUCtrlValueBindingRef rowData = (JUCtrlValueBindingRef) niinTable.getRowData();
        Row row = rowData.getRow();
        niin = row.getAttribute("Niin");
        niinId = row.getAttribute("NiinId");
        nomenclature = row.getAttribute("Nomenclature");
        serialFlag = row.getAttribute("SerialControlFlag");
        lotControlFlag = row.getAttribute("LotControlFlag");
      }

      log.debug("niin inquiry method");
      if (getNiinInquiryUpdateFlag()) {
        oldRowKey2 = niinLocationTable.getRowKey();

        for (Object rowKey : niinLocationTable.getSelectedRowKeys()) {
          hasLocation = true;
          niinLocationTable.setRowKey(rowKey);
          JUCtrlValueBindingRef rowData = (JUCtrlValueBindingRef) niinLocationTable.getRowData();
          Row rowUpdate = rowData.getRow();
          niinLocationId = rowUpdate.getAttribute("NiinLocId");
          locationId = rowUpdate.getAttribute("LocationId");
          locationLabel = rowUpdate.getAttribute("LocationLabel");
          qty = rowUpdate.getAttribute("Qty");
          cc = rowUpdate.getAttribute("Cc");
          underAudit = rowUpdate.getAttribute("UnderAudit");
          expirationDate = rowUpdate.getAttribute("ExpirationDate");
          dom = rowUpdate.getAttribute("DateOfManufacture");
          locked = rowUpdate.getAttribute("Locked");
          serialNumber = rowUpdate.getAttribute("SerialNumber");
          lotControlNumber = rowUpdate.getAttribute("LotConNum");
        }

        if (Util.cleanString(locked).equals("Y")) {
          displayMessage("The location is locked due to inventory.  Try again later.");
          setNiinInquiryFlag(false);
          setNiinInquiryUpdateFlag(false);
          setNiinInquirySerialFlag(false);
          return;
        }
      }
    }
    catch (Exception e) {
      displayMessage("[ERROR] Error occurred while trying to update ");
      setNiinInquiryFlag(false);
      setNiinInquiryUpdateFlag(false);
      setNiinInquirySerialFlag(false);
      return;
    }
    finally {
      niinLocationTable.setRowKey(oldRowKey2);
      niinTable.setRowKey(oldRowKey);
    }

    if (!hasLocation) {
      displayMessage("[ERROR] No Location was found ");
      setNiinInquiryFlag(false);
      setNiinInquiryUpdateFlag(false);
      setNiinInquirySerialFlag(false);
    }

    RequestContext afContext = RequestContext.getCurrentInstance();
    afContext.getPageFlowScope().put("niin", niin);
    afContext.getPageFlowScope().put(NIIN_ID, niinId);
    afContext.getPageFlowScope().put("nomenclature", nomenclature);
    afContext.getPageFlowScope().put("serialFlag", serialFlag);
    afContext.getPageFlowScope().put("lotControlFlag", lotControlFlag);
    afContext.getPageFlowScope().put("orig_qty", qty);
    afContext.getPageFlowScope().put("locationId", locationId);

    if (getNiinInquiryUpdateFlag()) {
      afContext.getPageFlowScope().put("niinLocationId", niinLocationId);
      afContext.getPageFlowScope().put("locationLabel", locationLabel);
      afContext.getPageFlowScope().put("lotControlNumber", lotControlNumber);
      afContext.getPageFlowScope().put("serialNumber", serialNumber);

      afContext.getPageFlowScope().put("qty", qty);
      afContext.getPageFlowScope().put("cc", cc);
      afContext.getPageFlowScope().put("underAudit", underAudit);
      afContext.getPageFlowScope().put("locked", locked);
      afContext.getPageFlowScope().put("dom", dom);
      afContext.getPageFlowScope().put("expDate", expirationDate);
    }

    if (getNiinInquirySerialFlag()) {
      if (Util.cleanString(serialFlag).equals("N") || Util.cleanString(lotControlFlag).equals("Y"))
        afContext.getPageFlowScope().put("requireQtyFlag", true);

      if (Util.cleanString(serialFlag).equals("Y")) {
        //* populate the serial table with serial
        SerialOrLotNumListImpl view = getPickingAMService().getSerialOrLotNumList1();
        view.executeQuery();
        getPickingAMService().buildSerialLotList(Util.cleanInt(niinId), Util.cleanString(serialFlag).equals("Y"), Util.cleanInt(locationId));
      }
    }
  }

  /**
   * Sets the list used for the combine with pull down
   * binding to the form.
   */
  public void setAllSerialLocation(List<SelectItem> allSerialLocation) {
    this.allSerialLocation = allSerialLocation;
  }

  /**
   * Returns the list used for the combine with pull down
   * This list is dependent on which niin id is currently selected
   * if none, selected and none available, then displays None Available
   * if none, selected and at least 1 available, then displays Make a Selection.
   */
  public List<SelectItem> getAllSerialLocation() {
    //* clear the list first
    if (allSerialLocation == null)
      allSerialLocation = new ArrayList<>();
    allSerialLocation.clear();

    RequestContext afContext = RequestContext.getCurrentInstance();
    int niinId = Util.cleanInt(afContext.getPageFlowScope().get(NIIN_ID));

    //* invoke the service to fetch the data
    SysAdminImpl service = getSysAdminModule();
    Map<String, String> hm = service.fillSerialLocationLists(niinId, false);

    //* fill the new list (allSerialLocation)
    if (hm != null) {
      if (hm.isEmpty()) {
        getSerialLocationNavList().setUnselectedLabel("<None Available>");
        getSerialLocationNavList().setRequired(false);
      }
      else {
        getSerialLocationNavList().setUnselectedLabel("<Make a Selection>");
        for (Map.Entry<String, String> entry : hm.entrySet()) {
          val locationId = entry.getKey();
          val value = entry.getValue();
          allSerialLocation.add(new SelectItem(locationId, value));
        }
      }
    }

    return allSerialLocation;
  }

  public void setRequireQty(boolean requireQtyFlag) {
    this.requireQtyFlag = requireQtyFlag;
  }

  public boolean getRequireQtyFlag() {
    RequestContext afContext = RequestContext.getCurrentInstance();
    boolean flag = requireQtyFlag;
    Object obj = afContext.getPageFlowScope().get("requireQtyFlag");
    if (obj != null) {
      flag = Boolean.parseBoolean(obj.toString());
    }
    return requireQtyFlag || flag;
  }

  boolean gcssmcFlag = false;

  public void setGcssmcFlag(boolean gcssmcFlag) {
    this.gcssmcFlag = gcssmcFlag;
  }

  public boolean getGcssmcFlag() {
    boolean gcssmc = false;
    try {
      AppModuleImpl service = (AppModuleImpl) getStratisRootService().getAppModule1();
      String sql = "select count(*) from site_info where nvl(gcss_mc, 'N') = 'Y'";
      try (PreparedStatement pstmt = service.getDBTransaction().createPreparedStatement(sql, 0)) {
        try (ResultSet rs = pstmt.executeQuery()) {
          if (rs.next()) {
            int count = rs.getInt(1);
            if (count > 0) gcssmc = true;
          }
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      gcssmc = false;
    }
    setGcssmcFlag(gcssmc);
    return gcssmc;
  }

  public void setNiinTable(RichTable niinTable) {
    this.niinTable = niinTable;
  }

  public RichTable getNiinTable() {
    return niinTable;
  }

  public void setNiinFilteredTable(RichTable niinFilteredTable) {
    this.niinFilteredTable = niinFilteredTable;
  }

  public RichTable getNiinFilteredTable() {
    return niinFilteredTable;
  }

  public void setNiinLocationTable(RichTable niinLocationTable) {
    this.niinLocationTable = niinLocationTable;
  }

  public RichTable getNiinLocationTable() {
    return niinLocationTable;
  }

  public void setNiinSerialTable(RichTable niinSerialTable) {
    this.niinSerialTable = niinSerialTable;
  }

  public RichTable getNiinSerialTable() {
    return niinSerialTable;
  }

  public void setLocationTable(RichTable locationTable) {
    this.locationTable = locationTable;
  }

  public RichTable getLocationTable() {
    return locationTable;
  }

  public void setTxtNiin(RichInputText txtNiin) {
    this.txtNiin = txtNiin;
  }

  public RichInputText getTxtNiin() {
    return txtNiin;
  }

  public void setTxtQty(RichInputText txtQty) {
    this.txtQty = txtQty;
  }

  public RichInputText getTxtQty() {
    return txtQty;
  }

  public void setTxtLocation(RichInputText txtLocation) {
    this.txtLocation = txtLocation;
  }

  public RichInputText getTxtLocation() {
    return txtLocation;
  }

  public void setTxtDOM(RichInputDate txtDOM) {
    this.txtDOM = txtDOM;
  }

  public RichInputDate getTxtDOM() {
    return txtDOM;
  }

  public void setTxtExpDate(RichInputDate txtExpDate) {
    this.txtExpDate = txtExpDate;
  }

  public RichInputDate getTxtExpDate() {
    return txtExpDate;
  }

  public void setTxtLotControl(RichInputText txtLotControl) {
    this.txtLotControl = txtLotControl;
  }

  public RichInputText getTxtLotControl() {
    return txtLotControl;
  }

  public void setTxtSerial(RichInputText txtSerial) {
    this.txtSerial = txtSerial;
  }

  public RichInputText getTxtSerial() {
    return txtSerial;
  }

  public void setStartSerial(RichInputText startSerial) {
    this.startSerial = startSerial;
  }

  public RichInputText getStartSerial() {
    return startSerial;
  }

  public void setEndSerial(RichInputText endSerial) {
    this.endSerial = endSerial;
  }

  public RichInputText getEndSerial() {
    return endSerial;
  }

  public void setSelectCC(RichSelectOneChoice selectCC) {
    this.selectCC = selectCC;
  }

  public RichSelectOneChoice getSelectCC() {
    return selectCC;
  }

  public void setSelectAudit(RichSelectOneChoice selectAudit) {
    this.selectAudit = selectAudit;
  }

  public RichSelectOneChoice getSelectAudit() {
    return selectAudit;
  }

  public void setSelectLocked(RichSelectOneChoice selectLocked) {
    this.selectLocked = selectLocked;
  }

  public RichSelectOneChoice getSelectLocked() {
    return selectLocked;
  }

  public boolean isNiinInquiryFlag() {
    return niinInquiryFlag;
  }

  @SuppressWarnings("unused") //called from .jspx
  public boolean isNiinInquiryUpdateFlag() {
    return niinInquiryUpdateFlag;
  }

  @SuppressWarnings("unused") //called from .jspx
  public boolean isNiinInquirySerialFlag() {
    return niinInquirySerialFlag;
  }

  public void setIUserId(int iUserId) {
    this.iUserId = iUserId;
  }

  public int getIUserId() {
    return iUserId;
  }

  public void setTxtImmedQtyTrans(RichInputText txtImmedQtyTrans) {
    this.txtImmedQtyTrans = txtImmedQtyTrans;
  }

  public RichInputText getTxtImmedQtyTrans() {
    return txtImmedQtyTrans;
  }

  public void setSelectImmedQtyTrans(RichSelectOneChoice selectImmedQtyTrans) {
    this.selectImmedQtyTrans = selectImmedQtyTrans;
  }

  public RichSelectOneChoice getSelectImmedQtyTrans() {
    return selectImmedQtyTrans;
  }

  public void setSelectImmedCCTrans(RichSelectOneChoice selectImmedCCTrans) {
    this.selectImmedCCTrans = selectImmedCCTrans;
  }

  public RichSelectOneChoice getSelectImmedCCTrans() {
    return selectImmedCCTrans;
  }

  public void setSelectGenTrans(RichSelectOneChoice selectGenTrans) {
    this.selectGenTrans = selectGenTrans;
  }

  public RichSelectOneChoice getSelectGenTrans() {
    return selectGenTrans;
  }

  public void setChkGenTrans(RichSelectBooleanCheckbox chkGenTrans) {
    this.chkGenTrans = chkGenTrans;
  }

  public RichSelectBooleanCheckbox getChkGenTrans() {
    return chkGenTrans;
  }

  public void setTxtNiinSerial(RichInputText txtNiinSerial) {
    this.txtNiinSerial = txtNiinSerial;
  }

  public RichInputText getTxtNiinSerial() {
    return txtNiinSerial;
  }

  public void setTxtSerialAdd(RichInputText txtSerialAdd) {
    this.txtSerialAdd = txtSerialAdd;
  }

  public RichInputText getTxtSerialAdd() {
    return txtSerialAdd;
  }

  public void setTxtLotAdd(RichInputText txtLotAdd) {
    this.txtLotAdd = txtLotAdd;
  }

  public RichInputText getTxtLotAdd() {
    return txtLotAdd;
  }

  public void setSerialTable(RichTable serialTable) {
    this.serialTable = serialTable;
  }

  public RichTable getSerialTable() {
    return serialTable;
  }

  public void setSelectGenTrans2(RichSelectOneChoice selectGenTrans2) {
    this.selectGenTrans2 = selectGenTrans2;
  }

  public RichSelectOneChoice getSelectGenTrans2() {
    return selectGenTrans2;
  }

  public void setChkGenTrans2(RichSelectBooleanCheckbox chkGenTrans2) {
    this.chkGenTrans2 = chkGenTrans2;
  }

  public RichSelectBooleanCheckbox getChkGenTrans2() {
    return chkGenTrans2;
  }

  public void setTxtNiinFilter(RichInputText txtNiinFilter) {
    this.txtNiinFilter = txtNiinFilter;
  }

  public RichInputText getTxtNiinFilter() {
    return txtNiinFilter;
  }

  public void setTxtNomenclatureFilter(RichInputText txtNomenclatureFilter) {
    this.txtNomenclatureFilter = txtNomenclatureFilter;
  }

  public RichInputText getTxtNomenclatureFilter() {
    return txtNomenclatureFilter;
  }

  public void setSerialOrLotNum(RichInputText serialOrLotNum) {
    this.serialOrLotNum = serialOrLotNum;
  }

  public RichInputText getSerialOrLotNum() {
    return serialOrLotNum;
  }

  public static void setNiinInfoIterator(String niinInfoIterator) {
    NiinInquiryBacking.niinInfoIterator = niinInfoIterator;
  }

  public static String getNiinInfoIterator() {
    return niinInfoIterator;
  }

  public static void setNiinLocationIterator(String niinLocationIterator) {
    NiinInquiryBacking.niinLocationIterator = niinLocationIterator;
  }

  public static String getNiinLocationIterator() {
    return niinLocationIterator;
  }

  public void setSerialLocationRemoveNavList(RichSelectOneChoice serialLocationRemoveNavList) {
    this.serialLocationRemoveNavList = serialLocationRemoveNavList;
  }

  public RichSelectOneChoice getSerialLocationRemoveNavList() {
    return serialLocationRemoveNavList;
  }

  public boolean isRequireQtyFlag() {
    return requireQtyFlag;
  }

  public boolean isGcssmcFlag() {
    return gcssmcFlag;
  }

  public void setSerialLocationNavList(RichSelectOneChoice serialLocationNavList) {
    this.serialLocationNavList = serialLocationNavList;
  }

  public RichSelectOneChoice getSerialLocationNavList() {
    return serialLocationNavList;
  }
}


