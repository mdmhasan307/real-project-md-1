package mil.stratis.view.admin.warehouse;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.common.util.RegUtils;
import mil.stratis.common.util.Util;
import mil.stratis.model.services.PickingAMImpl;
import mil.stratis.model.services.SysAdminImpl;
import mil.stratis.model.services.WorkLoadManagerImpl;
import mil.stratis.model.util.AppModuleDBTransactionGetterImpl;
import mil.stratis.model.view.pick.SerialOrLotNumListImpl;
import mil.stratis.view.session.MdssBackingBean;
import mil.stratis.view.util.ADFUtils;
import mil.stratis.view.util.JSFUtils;
import mil.usmc.mls2.stratis.common.domain.exception.StratisRuntimeException;
import mil.usmc.mls2.stratis.core.utility.AdfLogUtility;
import mil.usmc.mls2.stratis.core.utility.ContextUtils;
import mil.usmc.mls2.stratis.core.utility.GlobalConstants;
import oracle.adf.model.binding.DCIteratorBinding;
import oracle.adf.view.rich.component.rich.data.RichTable;
import oracle.adf.view.rich.component.rich.input.RichInputText;
import oracle.adf.view.rich.component.rich.input.RichSelectOneChoice;
import oracle.binding.OperationBinding;
import oracle.jbo.Row;
import oracle.jbo.ViewCriteria;
import oracle.jbo.ViewCriteriaRow;
import oracle.jbo.ViewObject;
import oracle.jbo.uicli.binding.JUCtrlValueBindingRef;
import org.apache.myfaces.trinidad.context.RequestContext;
import org.apache.myfaces.trinidad.model.CollectionModel;
import org.apache.myfaces.trinidad.model.RowKeySet;
import org.apache.myfaces.trinidad.model.RowKeySetImpl;
import org.apache.myfaces.trinidad.model.UploadedFile;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

@Slf4j
public class WalkThruBacking extends MdssBackingBean {

  private static final int MAX_LEN_COST_JON = 12;
  private static final int MAX_LEN_DOCUMENT_NUMBER = 14;
  private static final int MAX_LEN_RIC_FROM = 3;
  private static final int MAX_LEN_FUND_CODE = 2;
  private static final int MAX_LEN_MEDIAANDSTATUS_CODE = 1;
  private static final int MAX_LEN_RDD = 3;
  public static final String SERIAL_FLAG = "serialFlag";
  public static final String SERIAL_UPDATE_FLAG = "serialUpdateFlag";
  public static final String PICK_AMMOUNT = "PickAmmount";
  public static final String USER_ID = "userbean.userId";

  private transient Object pageState = 0;
  private transient UploadedFile file;

  private transient RichInputText niinSearchText;
  private transient RichInputText fscSearchText;
  private transient RichInputText nomSearchText;
  private transient RichInputText aacSearchText;
  private String shipAddress1DisplayText;
  private String shipAddress2DisplayText;
  private String shipAddress3DisplayText;
  private String shipAddress4DisplayText;
  private String transMessage;
  private transient Object shipAddressCustId = null;

  private String z0DocumentId = "";

  private transient RichInputText txtDocNum;
  private transient RichInputText txtRicFrom;
  private transient RichInputText txtMediaStatusCode;
  private transient RichInputText txtRDD;
  private transient RichInputText txtSuppAddress;
  private transient RichInputText txtCostJon;
  private transient RichInputText txtFundCode;
  private transient RichInputText txtShipAddress1;
  private transient RichInputText txtShipAddress2;
  private transient RichInputText txtShipAddress3;
  private transient RichInputText txtShipAddress4;
  private transient RichTable niinTable;
  private transient RichTable locationTable;
  private transient RichSelectOneChoice selectPriority;

  private boolean serialFlag = false;
  private boolean serialUpdateFlag = false;
  private transient RichInputText serialOrLotNum;
  private transient RichInputText startSerial;
  private transient RichInputText endSerial;
  private boolean aacNotRequired = false;

  public WalkThruBacking() {

    RequestContext afContext = RequestContext.getCurrentInstance();
    if (!afContext.isPostback()) {
      setPageState(0);
      setInitialSerialFlags();
    }
  }

  @Override
  public void onPageLoad() {
    if (!this.isPostback()) {
      transMessage = "";
    }
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
   * and hide certain ui buttons on Warehouse Management - Walk Thru
   * - Manual Hardcard Walk Thru only.
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

  /**
   * COURIER WALK THRU.
   */
  public final void setPageState(Object pageS) {
    RequestContext afContext = RequestContext.getCurrentInstance();
    //* for use in the web, maintaining state
    afContext.getPageFlowScope().put("pageState", pageS);
    this.pageState = pageS;
  }

  public Object getPageState() {
    RequestContext afContext = RequestContext.getCurrentInstance();
    Object flag = pageState;
    Object obj = afContext.getPageFlowScope().get("pageState");
    if (obj != null)
      flag = obj;

    return flag;
  }

  @SuppressWarnings("unused")
  public void setManualState(ActionEvent actionEvent) {
    setPageState(0);
  }

  @SuppressWarnings("unused")
  public void setCourierState(ActionEvent actionEvent) {
    setPageState(1);
  }

  public void setFile(UploadedFile file) {
    this.file = file;
  }

  public UploadedFile getFile() {
    return file;
  }

  @SuppressWarnings("unused")
  public void onFileUploadValueChangeListener(ValueChangeEvent valueChangeEvent) {
    file = (UploadedFile) valueChangeEvent.getNewValue();
  }

  /**
   * This function takes a file selected by the user on their client PC and uploads it to the selected
   * temporary directory defined in WEB.XML.  If the file uploads sucessfully, it will then process it
   * as a Courior IOR/Walk-Thru file.
   */
  public void doUploaded() {

    UploadedFile localFile = getFile();
    val context = FacesContext.getCurrentInstance();
    String fileUploadLoc = context.getExternalContext().getInitParameter("oracle.adf.view.faces.UPLOAD_TEMP_DIR");
    boolean exists = (new File(fileUploadLoc)).exists();
    if (!exists) {
      val tempDirectoryCreated = (new File(fileUploadLoc)).mkdirs();
      if (!tempDirectoryCreated) throw new StratisRuntimeException("Failed to create temporary upload directory");
    }
    if (localFile != null) {
      try (FileOutputStream out = new FileOutputStream(fileUploadLoc + localFile.getFilename())) {
        FacesMessage message = new FacesMessage("Successfully uploaded file " + localFile.getFilename() +
            " (" + localFile.getLength() + " bytes)");
        context.addMessage("ok", message);

        try (InputStream in = localFile.getInputStream()) {
          log.debug("have a correct request..........");
          byte[] buffer = new byte[128];
          int br;
          do {
            br = in.read(buffer);
            if (br > 0) {
              out.write(buffer, 0, br);
            }
          }
          while (br > 0);
        }

        SysAdminImpl service = getSysAdminModule();
        service.runCourierWalkThru(fileUploadLoc + localFile.getFilename());
      }
      catch (Exception e) {
        log.debug(e.toString());
      }
    }
    else {
      JSFUtils.addFacesErrorMessage("Unable to load file.");
    }
  }

  @SuppressWarnings("unused")
  public void cancelPickProcess(ActionEvent actionEvent) {
    clearWalkThruFields();
  }

  /**
   * Filters the NIIN Info table by FSC, NIIN, and/or Nomenclature.
   */
  @SuppressWarnings("unused")
  public void filterNIINSeachTable(ActionEvent event) {
    getSysAdminModule().getNiinLocationWalkThruView2().clearCache();
    DCIteratorBinding niinsearchit = ADFUtils.findIterator("NiinInfoWalkThruView1Iterator");

    // clear the last filter and lets set it again
    niinsearchit.getViewObject().applyViewCriteria(null);

    // make a new viewcriteria
    ViewCriteria niinvc = niinsearchit.getViewObject().createViewCriteria();
    ViewCriteriaRow niinvcr = niinvc.createViewCriteriaRow();
    niinvcr.setUpperColumns(true);

    if (niinSearchText.getValue() != null) {
      String niin = Util.trimUpperCaseClean(niinSearchText.getValue());
      if (!RegUtils.isNotAlphaNumeric(niin))
        niinvcr.setAttribute("Niin", " like '%" + niin + "%'");
    }
    if (fscSearchText.getValue() != null) {
      String fsc = Util.trimUpperCaseClean(fscSearchText.getValue());
      if (!RegUtils.isNotAlphaNumeric(fsc))
        niinvcr.setAttribute("Fsc", " like '%" + fsc + "%'");
    }
    if (nomSearchText.getValue() != null) {
      String nomen = Util.trimUpperCaseClean(nomSearchText.getValue());
      if (!RegUtils.isNotAlphaNumeric(nomen))
        niinvcr.setAttribute("Nomenclature", " like '%" + nomen + "%'");
    }

    niinvc.addElement(niinvcr);

    niinsearchit.getViewObject().applyViewCriteria(niinvc);
    niinsearchit.getViewObject().executeQuery();
    aacNotRequired = true;

    searchAACEvent(null);
  }

  /**
   * Searches customer table for the AAC in the populated field.  If the field is empty,
   * it grabs from the 1st 6 of the entered document number, if possible.
   * If neither AAC are present and/or the AAC cannot be found in the customer table,
   * then an error is displayed to the UI.
   */
  @SuppressWarnings("unused")
  public void searchAACEvent(ActionEvent event) {
    boolean search = false;
    String aac = "";
    val globalConstants = ContextUtils.getBean(GlobalConstants.class);

    if (getAacSearchText().getValue() == null) {

      if (getTxtDocNum().getValue() != null && validateDocumentNumber()) {
        aac = getTxtDocNum().getValue().toString().trim().toUpperCase().substring(0, 6);
        search = true;
      }
    }
    else {
      search = true;
      aac = aacSearchText.getValue().toString().trim().toUpperCase();
    }

    if (RegUtils.isNotAlphaNumeric(aac)) search = false;

    if (search) {
      try {

        SysAdminImpl service = getSysAdminModule();

        String sql = "select name, address_1, address_2, city, state, zip_code, customer_id from customer where aac like ? and nvl(restrict_ship,'N')='N'";
        try (PreparedStatement aacps = service.getDBTransaction().createPreparedStatement(sql, 0)) {
          aacps.setString(1, "%" + aac + "%");
          try (ResultSet aacrs = aacps.executeQuery()) {
            if (aacrs.next()) {
              // fill the address sets
              if (aacrs.getObject(1) != null) {
                shipAddress1DisplayText = aacrs.getString(1);
              }
              else {
                shipAddress1DisplayText = "";
              }

              if (aacrs.getObject(2) != null) {
                shipAddress2DisplayText = aacrs.getString(2);
              }
              else {
                shipAddress2DisplayText = "";
              }

              if (aacrs.getObject(3) != null) {
                shipAddress3DisplayText = aacrs.getString(3);
              }
              else {
                shipAddress3DisplayText = "";
              }

              if ((aacrs.getObject(4) != null) && (aacrs.getObject(5) != null) && (aacrs.getObject(6) != null)) {
                shipAddress4DisplayText = aacrs.getString(4) + " " + aacrs.getString(5) + " " + aacrs.getString(6);
              }
              else {
                shipAddress4DisplayText = "";
              }

              shipAddressCustId = aacrs.getObject(7);
              aacSearchText.setValue(aac);
            }
            else {
              //* no aac found
              JSFUtils.addFacesErrorMessage(globalConstants.getWalkthruTag(), "Unable to locate AAC " + aac + " or AAC is restricted. Please re-enter a valid AAC.");
            }
          }
        }
      }
      catch (Exception e) {
        AdfLogUtility.logException(e);
        JSFUtils.addFacesErrorMessage(globalConstants.getWalkthruTag(),
            "An AAC is required in order to perform an AAC search. " +
                "Please re-enter a valid (unrestricted) AAC in the 1st 6 of the document or in the AAC field.");
      }
    }
    else {
      if (!aacNotRequired)
        JSFUtils.addFacesErrorMessage(globalConstants.getWalkthruTag(),
            "An AAC is required in order to perform an AAC search. " +
                "Please re-enter a valid (unrestricted) AAC in the 1st 6 of the document or in the AAC field.");
    }
  }

  /**
   * HARDCARD WALKTHRU
   * This function is used to process a walkthru for already picked items.
   */
  @SuppressWarnings("unused")
  public void processWalkThruAlreadyPicked(ActionEvent event) {
    transMessage = "";
    // check for the fields
    if (checkWalkThruRequiredFields()) {
      goback();
      return;
    }

    if (!this.validateDocumentNumber() || this.isFundCodeInValid() ||
        !this.validateMS() || !this.validateRdd() || !this.validateRic() ||
        isCostJONInvalid()) {
      goback();
      return;
    }

    log.debug("hardcard walkthru started...");
    int totalPickedQty = 0;
    // make a map of the locations to be affected
    ArrayList<Map<String, Object>> listScans = new ArrayList<>();
    Map<String, Object> locmap;
    StringBuilder locationIds = new StringBuilder();
    Object locationLabel = null;
    //* Sum up the total quantity needed
    int countLocationIds = 0;
    selectAllRowsInTable(getLocationTable());
    Iterator<Object> selection = getLocationTable().getSelectedRowKeys().iterator();
    val globalConstants = ContextUtils.getBean(GlobalConstants.class);
    try {
      while (selection.hasNext()) {
        Object rowKey = selection.next();
        getLocationTable().setRowKey(rowKey);
        JUCtrlValueBindingRef rowData = (JUCtrlValueBindingRef) getLocationTable().getRowData();
        Row row = rowData.getRow();

        //* check the qty to be moved against what is available
        Object qtyAvailAfterPendingPicks = row.getAttribute("Left");
        Object qtyToBeMoved = row.getAttribute(PICK_AMMOUNT);
        Object totalQtyAtLocation = row.getAttribute("Qty");
        Object locationId = row.getAttribute("LocationId");

        if (qtyToBeMoved == null) {
          JSFUtils.addFacesErrorMessage("INPUT ERROR",
              "Required to enter a quantity to pick for every selected location.");

          goback();
          return;
        }
        int moveAmount = Integer.parseInt(qtyToBeMoved.toString());

        //* Validate each selected row has no conflicts
        //* ignore selected walkthru picks with request of 0 or negative
        if (moveAmount > 0) {
          locationLabel = row.getAttribute("LocationLabel");
          //* For walkthru, you cannot pick
          //* if pending picks leaves 0 left to pick
          //* if the qty requesting to pick exceeds qty available at the location
          if (qtyAvailAfterPendingPicks != null && Integer.parseInt(qtyAvailAfterPendingPicks.toString()) <
              moveAmount) {

            JSFUtils.addFacesErrorMessage("VALIDATION ERROR", "Location " + locationLabel + " - Qty requested exceeds location qty.");

            goback();
            return;
          }
          //* build hashmap for use in creating hardcard walk thrus
          Object niinLocId = row.getAttribute("NiinLocId");
          log.debug("qty={}, currentqty= {}", qtyToBeMoved, totalQtyAtLocation);

          locmap = new HashMap<>();
          locmap.put("qty", qtyToBeMoved);
          locmap.put("niinlocid", niinLocId);
          locmap.put("currentqty", totalQtyAtLocation);
          locmap.put("locationid", locationId);
          locmap.put("locationlabel", locationLabel);
          listScans.add(locmap);

          if (countLocationIds != 0) locationIds.append(',');
          locationIds.append(row.getAttribute("LocationId"));
          countLocationIds++;
        }

        // add this to the total picked count
        totalPickedQty += moveAmount;
        log.debug("total picked quantity is now  {}", totalPickedQty);
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);

      JSFUtils.addFacesErrorMessage(globalConstants.getErrorTag(),
          "A system error has occurred during validation, please try again later.");

      goback();
      return;
    }

    //* return if total walkthru picks not greater than 0, nothing to pick
    if (totalPickedQty < 1) {
      //* show message to user
      JSFUtils.addFacesErrorMessage("PICK QTY INVALID",
          "The total quantity to pick must be greater than 0: " +
              totalPickedQty +
              " no transaction completed.  (Ensure you have " +
              "1) selected the checkbox for the walk thru(s) to create and " +
              "2) entered a QTY to pick for each of the selected walk thru(s))");
      goback();
      return;
    }

    //* HARDCARD - Generate the issue and the picks
    SysAdminImpl service = getSysAdminModule();

    //* get the priority level
    Integer priDesignator =
        Integer.parseInt(getSelectPriority().getValue().toString());
    Integer priorityDesignatorGroup = getPriorityDesignatorGroup(priDesignator);

    //* get the niin_id value from the NIINTable
    long niinId = 0;
    Object niin = null;
    Object nomenclature = null;
    Object serialControlFlag = null;
    Object lotControlFlag = null;
    Object oldRowKey = getNiinTable().getRowKey();
    try {
      selection = getNiinTable().getSelectedRowKeys().iterator();
      while (selection.hasNext()) {
        Object rowKey = selection.next();
        getNiinTable().setRowKey(rowKey);
        JUCtrlValueBindingRef rowData = (JUCtrlValueBindingRef) getNiinTable().getRowData();
        Row row = rowData.getRow();
        Object obj = row.getAttribute("NiinId");
        niin = row.getAttribute("Niin");
        nomenclature = row.getAttribute("Nomenclature");
        serialControlFlag = row.getAttribute("SerialControlFlag");
        lotControlFlag = row.getAttribute("LotControlFlag");
        if (obj != null)
          niinId = Long.parseLong(obj.toString());
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    finally {
      getNiinTable().setRowKey(oldRowKey);
    }

    if (niinId < 1) {
      JSFUtils.addFacesErrorMessage(globalConstants.getErrorTag(),
          "A system error has occurred while getting the NIIN, please try again later.");
      goback();
      return;
    }

    //* if serial or lot control, add variables to process scope
    if (Util.cleanString(serialControlFlag).equals("Y") || Util.cleanString(lotControlFlag).equals("Y")) {

      //* add variables to process scope
      RequestContext afContext = RequestContext.getCurrentInstance();
      afContext.getPageFlowScope().put("niin", niin);
      afContext.getPageFlowScope().put("location", Util.cleanString(locationLabel));
      afContext.getPageFlowScope().put("nomenclature", nomenclature);
      afContext.getPageFlowScope().put("serialControlFlag", serialControlFlag);
      afContext.getPageFlowScope().put("lotControlFlag", lotControlFlag);

      afContext.getPageFlowScope().put("documentNumber", getTxtDocNum().getValue());
      afContext.getPageFlowScope().put("totalPickedQty", String.valueOf(totalPickedQty));
      afContext.getPageFlowScope().put("pri_designator", priDesignator);
      afContext.getPageFlowScope().put("pri_designator_group", priorityDesignatorGroup);
      afContext.getPageFlowScope().put("ShipAddressCustId", shipAddressCustId);
      afContext.getPageFlowScope().put("niin_id", niinId);
      afContext.getPageFlowScope().put("suppAddress", getTxtSuppAddress().getValue());
      afContext.getPageFlowScope().put("fundCode", getTxtFundCode().getValue());
      afContext.getPageFlowScope().put("mediaStatusCode", getTxtMediaStatusCode().getValue());
      afContext.getPageFlowScope().put("costJON", getTxtCostJon().getValue());
      afContext.getPageFlowScope().put("rdd", getTxtRDD().getValue());
      afContext.getPageFlowScope().put("listScans", listScans);
      afContext.getPageFlowScope().put("locationIds", locationIds.toString());

      if (Util.cleanString(serialControlFlag).equals("N") || Util.cleanString(lotControlFlag).equals("Y"))
        afContext.getPageFlowScope().put("requireQtyFlag", true);
    }
    else {
      //* call the create issue function
      Object nscn = service.createIssueForWalkThru("Z0A", getTxtDocNum().getValue(),
          String.valueOf(totalPickedQty), "W",
          priDesignator, priorityDesignatorGroup,
          shipAddressCustId, niinId, null,
          getTxtSuppAddress().getValue(),
          getTxtFundCode().getValue(),
          getTxtMediaStatusCode().getValue(), null,
          getTxtCostJon().getValue(),
          getTxtRDD().getValue(),
          niin, "A", true);

      if (nscn == null) {
        if (shipAddressCustId == null) log.debug("found a problem");
        // give them an error
        JSFUtils.addFacesErrorMessage("WALK THRU",
            "Unable to create Walk Thru - " +
                "Possible reason Duplicate Document Numbers are not allowed.");
        return;
      }

      log.debug("do a hardcard for scn  {}", nscn);
      // call the workload function to create the records and spool files
      int result = getPickingAMService().hardcardWalkThru(nscn.toString(), listScans,
          Integer.parseInt(JSFUtils.getManagedBeanValue(USER_ID).toString()),
          false, false, null);
      if (result >= 0) {
        // clear all the feilds and say the transaction was completed
        transMessage = getZ0DocumentId() + " transactions created.";
      }
      else {
        // give them an error
        JSFUtils.addFacesErrorMessage("WALK THRU",
            "Unable to create Walk Thru - An error has occurred while trying to create " + getZ0DocumentId() + ".");
        log.debug("hardcardWalkThru result is  {}", result);

        return;
      }

      // clear all the stuff
      clearWalkThruFields();
    }
  }

  /**
   * function to process a (normal) walk thru for items to be picked.
   */
  @SuppressWarnings("unused")
  public void processWalkThruCreatePick(ActionEvent event) {
    transMessage = "";
    SerialOrLotNumListImpl view = getPickingAMService().getSerialOrLotNumList1();
    view.clearCache();

    // check for fields
    if (checkWalkThruRequiredFields())
      return;

    if (!this.validateDocumentNumber() || this.isFundCodeInValid() ||
        !this.validateMS() || !this.validateRdd() || !this.validateRic() ||
        isCostJONInvalid()) {
      return;
    }

    int moveAmount;
    log.debug("normal walkthru started...");
    int totalQtyToPick = 0;
    boolean error = false;

    val globalConstants = ContextUtils.getBean(GlobalConstants.class);
    try {
      //* Sum up the total quantity needed
      selectAllRowsInTable(getLocationTable());

      for (Object rowKey : getLocationTable().getSelectedRowKeys()) {
        getLocationTable().setRowKey(rowKey);
        JUCtrlValueBindingRef rowData = (JUCtrlValueBindingRef) getLocationTable().getRowData();
        Row row = rowData.getRow();
        Object qtyAvailAfterPendingPicks = row.getAttribute("Left");
        Object qtyToBeMoved = row.getAttribute(PICK_AMMOUNT);
        Object totalQtyAtLocation = row.getAttribute("Qty");
        if (qtyToBeMoved == null) {
          JSFUtils.addFacesErrorMessage("INPUT ERROR",
              "Required to enter a quantity to pick for every selected location.");

          return;
        }
        moveAmount = Integer.parseInt(qtyToBeMoved.toString());

        //* Validate each selected row has no conflicts
        //* ignore selected walkthru picks with request of 0 or negative
        if (moveAmount > 0) {
          //* For walkthru, you cannot pick
          //* if pending picks leaves 0 left to pick
          //* if the qty requesting to pick exceeds qty available at the location
          if (qtyAvailAfterPendingPicks != null && Integer.parseInt(qtyAvailAfterPendingPicks.toString()) <
              moveAmount) {
            Object locationLabel = row.getAttribute("LocationLabel");
            JSFUtils.addFacesErrorMessage("VALIDATION ERROR", "Location " + locationLabel + " - Qty requested exceeds location qty.");

            return;
          }
          //* build hashmap for use in creating walk thrus
          log.debug("qty={}, currentqty= {}", qtyToBeMoved, totalQtyAtLocation);
        }

        // add this to the total picked count
        totalQtyToPick += moveAmount;
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);

      JSFUtils.addFacesErrorMessage(globalConstants.getErrorTag(),
          "A system error has occurred during validation, please try again later.");
      return;
    }

    //* return if total walkthru picks not greater than 0, nothing to pick
    if (totalQtyToPick < 1) {
      //* show message to user
      JSFUtils.addFacesErrorMessage("PICK QTY INVALID",
          "The total quantity to pick must be greater than 0: " +
              totalQtyToPick +
              " no transaction completed.  (Ensure you have " +
              "1) selected the checkbox for the walk thru(s) to create and " +
              "2) entered a QTY to pick for each of the selected walk thru(s))");
      return;
    }

    // generate the issue and the picks
    // create the issue first

    SysAdminImpl service = getSysAdminModule();
    WorkLoadManagerImpl workloadManager = getWorkloadManagerService();

    // get the priority level
    int priDesignator =
        Integer.parseInt(getSelectPriority().getValue().toString());
    Integer priorityDesignatorGroup = getPriorityDesignatorGroup(priDesignator);

    //* get the niin_id value from the NIINTable
    long niinId = 0;
    Object niin = null;
    Object oldRowKey = getNiinTable().getRowKey();
    try {
      for (Object rowKey : getNiinTable().getSelectedRowKeys()) {
        getNiinTable().setRowKey(rowKey);
        JUCtrlValueBindingRef rowData = (JUCtrlValueBindingRef) getNiinTable().getRowData();
        Row row = rowData.getRow();
        //* check the qty to be moved against what is available
        Object obj = row.getAttribute("NiinId");
        niin = row.getAttribute("Niin");
        if (obj != null) niinId = Long.parseLong(obj.toString());
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    finally {
      getNiinTable().setRowKey(oldRowKey);
    }

    if (niinId < 1) {
      val message = "A system error has occurred while getting the NIIN, please try again later.";
      JSFUtils.addFacesErrorMessage(globalConstants.getErrorTag(), message);
      log.info(message);
      return;
    }

    // call the create issue function
    Object nscn = service.createIssueForWalkThru("Z0A", getTxtDocNum().getValue(), String.valueOf(totalQtyToPick),
        "W", priDesignator,
        priorityDesignatorGroup,
        shipAddressCustId, niinId, null,
        getTxtSuppAddress().getValue(),
        getTxtFundCode().getValue(),
        getTxtMediaStatusCode().getValue(), null,
        getTxtCostJon().getValue(),
        getTxtRDD().getValue(),
        niin, "A", false);
    String scn;
    if (nscn == null) {
      JSFUtils.addFacesErrorMessage("WALK THRU",
          "Unable to create Walk Thru - Possible reason Duplicate Document Numbers are not allowed.");
      return;
    }
    else {
      scn = String.valueOf(nscn);
    }
    try {
      for (Object rowKey : getLocationTable().getSelectedRowKeys()) {
        getLocationTable().setRowKey(rowKey);
        JUCtrlValueBindingRef rowData = (JUCtrlValueBindingRef) getLocationTable().getRowData();
        Row row = rowData.getRow();

        Number niinLocId = workloadManager.getNumberFromRowObject(row.getAttribute("NiinLocId"));
        int qtyToBeMoved = workloadManager.getNumberFromRowObject(row.getAttribute(PICK_AMMOUNT)).intValue();
        int userId = Integer.parseInt(JSFUtils.getManagedBeanValue(USER_ID).toString());

        log.trace("Check changes for insertPicking.");
        log.trace("niinLocId: {} qtyToBeMoved: {} userId: {}", niinLocId, qtyToBeMoved, userId);

        if (qtyToBeMoved > 0) {
          int pid = workloadManager.insertPicking(niinLocId, qtyToBeMoved, scn, "PICK READY", new AppModuleDBTransactionGetterImpl(workloadManager));
          if (pid < 1) {
            JSFUtils.addFacesErrorMessage("PICKING ERROR",
                "Inserting the pick failed for Walk Thru");
            log.debug("returned insert picking result is (PID) {}", pid);
            error = true;
          }
          else {
            String packingStationName = workloadManager.getPackingStation(pid, userId, qtyToBeMoved, 0);

            if (packingStationName.contains("Error")) {
              JSFUtils.addFacesErrorMessage("PACKING ERROR",
                  packingStationName +
                      " Getting a packing station failed for Walk Thru");

              error = true;
            }
          }
        }
        if (error) {
          //* perform a rollback if possible
          return;
        }
      }

      // clear all the fields and say the transaction was completed
      transMessage = "Walk Thru Picks generated successfully.";
      clearWalkThruFields();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  private void selectAllRowsInTable(RichTable rt) {
    RowKeySet rks = new RowKeySetImpl();
    CollectionModel model = (CollectionModel) rt.getValue();
    int rowcount = model.getRowCount();

    for (int i = 0; i < rowcount; i++) {
      model.setRowIndex(i);
      Object key = model.getRowKey();
      rks.add(key);
    }
    rt.setSelectedRowKeys(rks);
  }

  // function to check if all the needed fields have been filled in

  public boolean checkWalkThruRequiredFields() {
    boolean goBack = false;
    val globalConstants = ContextUtils.getBean(GlobalConstants.class);

    if (getTxtDocNum().getValue() == null) {
      goBack = true;
      // add to the error message
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(),
          "Document Number is required.");
    }
    if (!goBack && shipAddressCustId == null) {
      //* try re-running search AAC to use 1st 6 of document number
      searchAACEvent(null);
      if (shipAddressCustId == null) {
        goBack = true;
        JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(),
            "Customer Required, re-enter AAC field and click on goggles to search DB");
      }
    }

    if (getTxtSuppAddress().getValue() != null) {
      getTxtSuppAddress().setValue(getTxtSuppAddress().getValue().toString().trim().toUpperCase());
    }

    if (getTxtFundCode().getValue() == null) {
      goBack = true;
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(),
          "Fund Code is required.");
    }

    if (getTxtRicFrom().getValue() == null) {
      goBack = true;
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(),
          "RIC From is required.");
    }

    if (getTxtCostJon().getValue() == null) {
      goBack = true;
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(),
          "Cost JON is required.");
    }

    if (getSelectPriority().getValue() == null) {
      goBack = true;
      // add to the error message
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(),
          "Issue Priority is required.");
    }

    if (getTxtRDD().getValue() == null) {
      goBack = true;
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(),
          "Required Delivery Date is required.");
    }

    return goBack;
  }

  public void goback() {
    setSerialUpdateFlag(false);
    setSerialFlag(false);
  }

  @SuppressWarnings("unused")
  public void addSerialLotNum(ActionEvent actionEvent) {
    PickingAMImpl service = getPickingAMService();
    val globalConstants = ContextUtils.getBean(GlobalConstants.class);

    RequestContext afContext = RequestContext.getCurrentInstance();
    Object serialControlFlag = afContext.getPageFlowScope().get("serialControlFlag");
    String title = (Util.cleanString(serialControlFlag).equals("Y")) ? "SERIAL NUMBER" : "LOT CONTROL NUMBER";
    StringBuilder result = new StringBuilder();
    if ((this.getSerialOrLotNum().getValue() == null) && ((this.getStartSerial().getValue() == null) || (this.getEndSerial().getValue() == null))) {
      JSFUtils.addFacesErrorMessage(globalConstants.getInvalidInputTag(),
          "Please enter " + title + ".");
      return;
    }

    Object niinId = afContext.getPageFlowScope().get("niin_id");
    Object totalPickedQty = afContext.getPageFlowScope().get("totalPickedQty");
    if (service.getSerialOrLotNumList1().getEstimatedRowCount() > Integer.parseInt(totalPickedQty.toString())) {
      JSFUtils.addFacesErrorMessage("SERIAL OR LOT NUMBER",
          "You may not exceed the total picked quantity for this Hardcard Walk Thru.");
      return;
    }

    if (niinId != null) {
      String locationIds = Util.cleanString(afContext.getPageFlowScope().get("locationIds"));
      if (this.getSerialOrLotNum().getValue() != null) {
        result = new StringBuilder(service.addToSerialLotList(Util.trimUpperCaseClean(getSerialOrLotNum().getValue()),
            niinId.toString(), "1",
            (Util.cleanString(serialControlFlag).equals("Y")),
            locationIds,
            false));
      }
      else {
        ArrayList<String> serialList = new ArrayList<>();
        result = new StringBuilder(getWorkloadManagerService().generateSerialRange(getStartSerial().getValue().toString(), getEndSerial().getValue().toString(), serialList));
        if (result.length() <= 0) {
          String tempErr;
          for (String serial : serialList) {
            tempErr = service.addToSerialLotList(
                serial.trim().toUpperCase(),
                niinId.toString(), "1",
                (Util.cleanString(serialControlFlag).equals("Y")),
                locationIds,
                false);
            if (!tempErr.isEmpty()) {
              if (result.length() == 0) {
                result = new StringBuilder(tempErr);
              }
              else {
                result.append('\n').append(tempErr);
              }
            }
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
    if (result.length() > 0) {
      JSFUtils.addFacesErrorMessage("ERROR", result.toString());
    }
    this.getSerialOrLotNum().setValue("");
  }

  @SuppressWarnings("unused")
  public void deleteSerialLotNum(ActionEvent actionEvent) {
    this.getPickingAMService().getSerialOrLotNumList1().removeCurrentRowFromCollection();
  }

  boolean requireQtyFlag = false;

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

  /**
   * Save this hardcard to the database with the associated serial/lot control info.
   */
  @SuppressWarnings("unused")
  public void saveHardcardSerial(ActionEvent actionEvent) {
    RequestContext afContext = RequestContext.getCurrentInstance();
    Object documentNumber = afContext.getPageFlowScope().get("documentNumber");
    Object niin = afContext.getPageFlowScope().get("niin");
    Object niinId = afContext.getPageFlowScope().get("niin_id");
    Object locationIds = afContext.getPageFlowScope().get("locationIds");
    Object totalPickedQty = afContext.getPageFlowScope().get("totalPickedQty");
    Object serialControlFlag = afContext.getPageFlowScope().get("serialControlFlag");
    Object lotControlFlag = afContext.getPageFlowScope().get("lotControlFlag");
    Object objListScans = afContext.getPageFlowScope().get("listScans");
    ArrayList listScans = null;
    if (objListScans != null) {
      listScans = (ArrayList) objListScans;
    }
    String results;
    if (listScans != null && listScans.size() > 1) {

      // validation on the serial/lot control table
      results = getPickingAMService().validateSerialOrLotNumVOList(
          niinId.toString(), niin.toString(), totalPickedQty.toString(), Util.cleanString(serialControlFlag).equals("Y"), Util.cleanString(lotControlFlag).equals("Y"), "A", Util.cleanString(locationIds),
          true,
          listScans);
    }
    else {
      // validation on the serial/lot control table
      results = getPickingAMService().validateSerialOrLotNumVOList(
          niinId.toString(), niin.toString(), totalPickedQty.toString(), Util.cleanString(serialControlFlag).equals("Y"), Util.cleanString(lotControlFlag).equals("Y"), "A", Util.cleanString(locationIds),
          false,
          listScans);
    }
    String locationResults = "";
    String serialTrackNumId = "";
    if (results.length() > 0) {

      if (!results.contains("location")) {
        JSFUtils.addFacesErrorMessage("WALK THRU ERROR", results);
        return;
      }

      try {
        StringTokenizer st = new StringTokenizer(results, ",");
        while (st.hasMoreTokens()) {
          locationResults = st.nextToken();
          serialTrackNumId = st.nextToken();
        }

        if (!Util.isEmpty(locationResults))
          JSFUtils.addFacesErrorMessage("WALK THRU WARNING", locationResults);
      }
      catch (Exception e) {
        AdfLogUtility.logException(e);
        //ignore, do nothing
      }
    }

    Object priDesignator = afContext.getPageFlowScope().get("pri_designator");
    Object priDesignatorGroup = afContext.getPageFlowScope().get("pri_designator_group");
    Object localShipAddressCustId = afContext.getPageFlowScope().get("ShipAddressCustId");

    Object suppAddress = afContext.getPageFlowScope().get("suppAddress");
    Object fundCode = afContext.getPageFlowScope().get("fundCode");
    Object mediaStatusCode = afContext.getPageFlowScope().get("mediaStatusCode");
    Object costJON = afContext.getPageFlowScope().get("costJON");
    Object rdd = afContext.getPageFlowScope().get("rdd");

    SysAdminImpl service = getSysAdminModule();
    //* call the create issue function
    Object nscn = service.createIssueForWalkThru("Z0A", documentNumber,
        String.valueOf(totalPickedQty), "W",
        priDesignator, priDesignatorGroup,
        localShipAddressCustId, niinId, null,
        suppAddress, fundCode, mediaStatusCode,
        null, costJON, rdd,
        niin, "A", true);

    if (nscn == null) {
      if (localShipAddressCustId == null) log.debug("found a problem");
      // give them an error
      JSFUtils.addFacesErrorMessage("WALK THRU",
          "Unable to create Walk Thru - " +
              "Possible reason Duplicate Document Numbers are not allowed.");
      return;
    }

    log.debug("do a hardcard for scn  {}", nscn);
    // call the workload function to create the records and spool files
    int result = getPickingAMService().hardcardWalkThru(nscn.toString(), listScans,
        Integer.parseInt(JSFUtils.getManagedBeanValue(USER_ID).toString()), Util.cleanString(serialControlFlag).equals("Y"), Util.cleanString(lotControlFlag).equals("Y"),
        niinId.toString());
    if (result >= 0) {
      // clear all the feilds and say the transaction was completed
      transMessage = getZ0DocumentId() + " transactions created.";
      //* generate the warning exception for serial numbers used from locations not their own
      if (results.length() > 0 && results.contains("location")) {
        WorkLoadManagerImpl wlm = getWorkloadManagerModule();
        if (!Util.isEmpty(serialTrackNumId)) wlm.createErrorQueueRecord("58", "SRL_LOT_NUM_TRACK_ID", serialTrackNumId, "1", null);
      }
    }
    else {
      // give them an error
      JSFUtils.addFacesErrorMessage("WALK THRU",
          "Unable to create Walk Thru - An error has occurred while trying to create " + getZ0DocumentId() + ".");
      log.debug("hardcardWalkThru result is  {}", result);
      return;
    }

    // clear all the stuff
    clearWalkThruFields();
    goback();
    SerialOrLotNumListImpl view = getPickingAMService().getSerialOrLotNumList1();
    view.clearCache();

    getSysAdminModule().getNiinLocationWalkThruView2().clearCache();
  }

  @SuppressWarnings("unused")
  public void cancelHardcardSerial(ActionEvent actionEvent) {
    SerialOrLotNumListImpl view = getPickingAMService().getSerialOrLotNumList1();
    view.clearCache();
    cancelPickProcess(actionEvent);
    getSysAdminModule().getNiinLocationWalkThruView2().clearCache();
  }

  /**
   * This function will clear the walk thru fields and
   * refresh the niin location table, returning
   * the state of the page to initial state.
   */
  public void clearWalkThruFields() {
    RowKeySet selection = getLocationTable().getSelectedRowKeys();
    selection.clear();

    getTxtDocNum().setValue(null);
    niinSearchText.setValue(null);

    shipAddress1DisplayText = "";
    shipAddress2DisplayText = "";
    shipAddress3DisplayText = "";
    shipAddress4DisplayText = "";

    OperationBinding op = ADFUtils.getDCBindingContainer().getOperationBinding("ExecuteNiinLocation");
    op.execute();
    //* also clear the top of the form
    clearFields(null);
  }

  /**
   * Returns the priority designator group based on the issue's priority
   */
  private Integer getPriorityDesignatorGroup(int issuePriorityDesignator) {
    int priDesignatorGroup = 3;
    if ((issuePriorityDesignator == 1) || (issuePriorityDesignator == 2) ||
        (issuePriorityDesignator == 3) || (issuePriorityDesignator == 7) ||
        (issuePriorityDesignator == 8)) {
      priDesignatorGroup = 1;
    }
    else if ((issuePriorityDesignator == 4) || (issuePriorityDesignator == 5) ||
        (issuePriorityDesignator == 6) || (issuePriorityDesignator == 9) || (issuePriorityDesignator == 10)) {
      priDesignatorGroup = 2;
    }
    return priDesignatorGroup;
  }

  public boolean validateDocumentNumber() {
    boolean retVal = true;
    String docStr = "";
    Object temp = getTxtDocNum().getValue();
    val globalConstants = ContextUtils.getBean(GlobalConstants.class);

    if (temp != null) {
      docStr = temp.toString().trim().toUpperCase();
      getTxtDocNum().setValue(docStr);

      if (docStr.length() != MAX_LEN_DOCUMENT_NUMBER) {
        JSFUtils.addFacesErrorMessage(globalConstants.getInvalidLengthTag(), "Document Number must be " + MAX_LEN_DOCUMENT_NUMBER + " character(s)");
        retVal = false;
      }
      else if (RegUtils.isNotAlphaNumeric(docStr)) {
        JSFUtils.addFacesErrorMessage(globalConstants.getInvalidFieldTag(), "Document Number From must be alphanumeric characters only");
        retVal = false;
      }
    }
    else {
      JSFUtils.addFacesErrorMessage("Document # is a required field");
      retVal = false;
    }
    if (retVal) {
      SysAdminImpl service = getSysAdminModule();
      String msg = service.checkDuplicateDocNumInIssue(docStr);
      if (msg != null && msg.length() > 0) {
        JSFUtils.addFacesErrorMessage(msg);
        retVal = false;
      }
    }

    return retVal;
  }

  public boolean validateRic() {
    boolean retVal = true;
    Object temp = getTxtRicFrom().getValue();
    val globalConstants = ContextUtils.getBean(GlobalConstants.class);

    if (temp != null) {
      String ricStr = temp.toString().trim().toUpperCase();
      getTxtRicFrom().setValue(ricStr);
      if (ricStr.length() != MAX_LEN_RIC_FROM) {
        JSFUtils.addFacesErrorMessage(globalConstants.getInvalidLengthTag(), "RIC From must be " + MAX_LEN_RIC_FROM + " character(s)");
        retVal = false;
      }
      else if (RegUtils.isNotAlphaNumeric(ricStr)) {
        JSFUtils.addFacesErrorMessage(globalConstants.getInvalidFieldTag(), "RIC From must be alphanumeric characters only");
        retVal = false;
      }
    }
    else {
      JSFUtils.addFacesErrorMessage("RIC is a required field");
      retVal = false;
    }

    return retVal;
  }

  /**
   * Validate Media and Status code.  Media and Status Code is not required.  Must be 2 characters and may be alphanumeric.
   *
   * @return boolean
   */
  public boolean validateMS() {
    boolean retVal = true;
    Object temp = getTxtMediaStatusCode().getValue();
    val globalConstants = ContextUtils.getBean(GlobalConstants.class);

    if (temp != null) {
      String msStr = temp.toString().trim().toUpperCase();
      getTxtMediaStatusCode().setValue(msStr);
      if (msStr.length() != MAX_LEN_MEDIAANDSTATUS_CODE) {
        JSFUtils.addFacesErrorMessage(globalConstants.getInvalidLengthTag(), "Media and Status Code must be " + MAX_LEN_MEDIAANDSTATUS_CODE + " character(s)");
        retVal = false;
      }
      else if (RegUtils.isNotAlphaNumeric(msStr)) {
        JSFUtils.addFacesErrorMessage(globalConstants.getInvalidFieldTag(), "Media and Status Code must be alphanumeric characters only");
        retVal = false;
      }
    }

    return retVal;
  }

  /**
   * Validate Fund code.  Fund Code is required.  Must be 2 characters and must be alphabet.
   *
   * @return boolean
   */
  public boolean isFundCodeInValid() {
    boolean invalid = false;
    Object temp = getTxtFundCode().getValue();
    val globalConstants = ContextUtils.getBean(GlobalConstants.class);

    if (temp != null) {
      String fundStr = temp.toString().trim().toUpperCase();
      getTxtFundCode().setValue(fundStr);
      if (fundStr.length() != MAX_LEN_FUND_CODE) {
        JSFUtils.addFacesErrorMessage(globalConstants.getInvalidLengthTag(), "Fund Code must be " + MAX_LEN_FUND_CODE + " character(s)");
        invalid = true;
      }
      else if (RegUtils.isNotAlpha(fundStr)) {
        JSFUtils.addFacesErrorMessage(globalConstants.getInvalidFieldTag(), "Fund Code must be alphabetic characters only");
        invalid = true;
      }
    }
    else {
      JSFUtils.addFacesErrorMessage("Fund Code is a required field");
      invalid = true;
    }

    return invalid;
  }

  /**
   * Validate Required Delivery Date.  RDD is required.
   * Must be 3 characters and may be alphanumeric (e.g.N01, E##),
   * but is generally a julian date.
   *
   * @return boolean
   */
  public boolean validateRdd() {
    boolean retVal = true;
    Object temp = getTxtRDD().getValue();
    val globalConstants = ContextUtils.getBean(GlobalConstants.class);

    if (temp != null) {
      String rddStr = temp.toString().trim().toUpperCase();
      getTxtRDD().setValue(rddStr);
      if (rddStr.length() != MAX_LEN_RDD) {
        JSFUtils.addFacesErrorMessage(globalConstants.getInvalidLengthTag(), "Required Delivery Date (RDD) must be " + MAX_LEN_RDD + " alphanumeric character(s)");
        retVal = false;
      }
      else if (RegUtils.isNotAlphaNumeric(rddStr)) {
        JSFUtils.addFacesErrorMessage(globalConstants.getInvalidFieldTag(), "Required Delivery Date (RDD) must be alphanumeric characters only");
        retVal = false;
      }
    }
    else {
      JSFUtils.addFacesErrorMessage("Required Delivery Date (RDD) is a required field");
      retVal = false;
    }

    return retVal;
  }

  public boolean isCostJONInvalid() {
    boolean invalid = false;
    Object temp = getTxtCostJon().getValue();
    val globalConstants = ContextUtils.getBean(GlobalConstants.class);

    if (temp != null) {
      String cjStr = temp.toString().trim().toUpperCase();
      getTxtCostJon().setValue(cjStr);
      if (cjStr.length() != MAX_LEN_COST_JON) {
        JSFUtils.addFacesErrorMessage(globalConstants.getInvalidFieldTag(), "Cost JON must be " + MAX_LEN_COST_JON + " alphanumeric character(s)");
        invalid = true;
      }
      else if (RegUtils.isNotAlphaNumeric(cjStr)) {
        JSFUtils.addFacesErrorMessage(globalConstants.getInvalidFieldTag(), "Cost JON must be alphanumeric characters only");
        invalid = true;
      }
    }
    else {
      JSFUtils.addFacesErrorMessage("Cost JON is a required field");
      invalid = true;
    }

    return invalid;
  }

  @SuppressWarnings("unused")
  public void clearFields(ActionEvent actionEvent) {
    getSelectPriority().setValue(null);
    getTxtDocNum().setValue(null);
    getTxtSuppAddress().setValue(null);
    shipAddressCustId = null;
    getTxtRDD().setValue(null);
    getTxtFundCode().setValue(null);
    getTxtCostJon().setValue(null);
    getTxtMediaStatusCode().setValue(null);
    getTxtRicFrom().setValue(null);

    niinSearchText.setValue(null);
    fscSearchText.setValue(null);
    nomSearchText.setValue(null);
    aacSearchText.setValue(null);
    shipAddress1DisplayText = "";
    shipAddress2DisplayText = "";
    shipAddress3DisplayText = "";
    shipAddress4DisplayText = "";
  }

  public void setZ0DocumentId(String z0DocumentId) {
    this.z0DocumentId = z0DocumentId;
  }

  private boolean conusFlag = false;

  public String getZ0DocumentId() {
    if (Util.isEmpty(z0DocumentId)) setZ0DocumentId((getConusFlag()) ? "Z0A" : "Z01");
    return Util.isEmpty(z0DocumentId) ? "Z0A" : this.z0DocumentId;
  }

  public boolean getConusFlag() {
    RequestContext afContext = RequestContext.getCurrentInstance();
    boolean flag = conusFlag;
    Object obj = afContext.getPageFlowScope().get("conusFlag");
    if (obj != null) {
      flag = Boolean.parseBoolean(obj.toString());
    }
    else {
      try {
        SysAdminImpl service = getSysAdminModule();

        String sql = "select nvl(conus_ocunus_flag,'Y') from site_info";
        try (PreparedStatement pstmt = service.getDBTransaction().createPreparedStatement(sql, 0)) {
          try (ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) flag = Util.cleanString(rs.getString(1)).equals("Y");
          }
        }
      }
      catch (Exception e) {
        AdfLogUtility.logException(e);
      }
    }
    setConusFlag(conusFlag || flag);
    return conusFlag || flag;
  }

  public void setConusFlag(boolean show) {
    RequestContext afContext = RequestContext.getCurrentInstance();
    //* for use in the web, maintaining state
    afContext.getPageFlowScope().put("conusFlag", show);
    this.conusFlag = show;
  }

  public Object getPageState1() {
    return pageState;
  }

  public void setNiinSearchText(RichInputText niinSearchText) {
    this.niinSearchText = niinSearchText;
  }

  public RichInputText getNiinSearchText() {
    return niinSearchText;
  }

  public void setFscSearchText(RichInputText fscSearchText) {
    this.fscSearchText = fscSearchText;
  }

  public RichInputText getFscSearchText() {
    return fscSearchText;
  }

  public void setNomSearchText(RichInputText nomSearchText) {
    this.nomSearchText = nomSearchText;
  }

  public RichInputText getNomSearchText() {
    return nomSearchText;
  }

  public void setAacSearchText(RichInputText aacSearchText) {
    this.aacSearchText = aacSearchText;
  }

  public RichInputText getAacSearchText() {
    return aacSearchText;
  }

  public void setShipAddress1DisplayText(String shipAddress1DisplayText) {
    this.shipAddress1DisplayText = shipAddress1DisplayText;
  }

  public String getShipAddress1DisplayText() {
    return shipAddress1DisplayText;
  }

  public void setShipAddress2DisplayText(String shipAddress2DisplayText) {
    this.shipAddress2DisplayText = shipAddress2DisplayText;
  }

  public String getShipAddress2DisplayText() {
    return shipAddress2DisplayText;
  }

  public void setShipAddress3DisplayText(String shipAddress3DisplayText) {
    this.shipAddress3DisplayText = shipAddress3DisplayText;
  }

  public String getShipAddress3DisplayText() {
    return shipAddress3DisplayText;
  }

  public void setShipAddress4DisplayText(String shipAddress4DisplayText) {
    this.shipAddress4DisplayText = shipAddress4DisplayText;
  }

  public String getShipAddress4DisplayText() {
    return shipAddress4DisplayText;
  }

  public void setTransMessage(String transMessage) {
    this.transMessage = transMessage;
  }

  public String getTransMessage() {
    return transMessage;
  }

  public void setShipAddressCustId(Object shipAddressCustId) {
    this.shipAddressCustId = shipAddressCustId;
  }

  public Object getShipAddressCustId() {
    return shipAddressCustId;
  }

  public String getZ0DocumentId1() {
    return z0DocumentId;
  }

  public void setTxtDocNum(RichInputText txtDocNum) {
    this.txtDocNum = txtDocNum;
  }

  public RichInputText getTxtDocNum() {
    return txtDocNum;
  }

  public void setTxtRicFrom(RichInputText txtRicFrom) {
    this.txtRicFrom = txtRicFrom;
  }

  public RichInputText getTxtRicFrom() {
    return txtRicFrom;
  }

  public void setTxtMediaStatusCode(RichInputText txtMediaStatusCode) {
    this.txtMediaStatusCode = txtMediaStatusCode;
  }

  public RichInputText getTxtMediaStatusCode() {
    return txtMediaStatusCode;
  }

  public void setTxtRDD(RichInputText txtRDD) {
    this.txtRDD = txtRDD;
  }

  public RichInputText getTxtRDD() {
    return txtRDD;
  }

  public void setTxtSuppAddress(RichInputText txtSuppAddress) {
    this.txtSuppAddress = txtSuppAddress;
  }

  public RichInputText getTxtSuppAddress() {
    return txtSuppAddress;
  }

  public void setTxtCostJon(RichInputText txtCostJon) {
    this.txtCostJon = txtCostJon;
  }

  public RichInputText getTxtCostJon() {
    return txtCostJon;
  }

  public void setTxtFundCode(RichInputText txtFundCode) {
    this.txtFundCode = txtFundCode;
  }

  public RichInputText getTxtFundCode() {
    return txtFundCode;
  }

  public void setTxtShipAddress1(RichInputText txtShipAddress1) {
    this.txtShipAddress1 = txtShipAddress1;
  }

  public RichInputText getTxtShipAddress1() {
    return txtShipAddress1;
  }

  public void setTxtShipAddress2(RichInputText txtShipAddress2) {
    this.txtShipAddress2 = txtShipAddress2;
  }

  public RichInputText getTxtShipAddress2() {
    return txtShipAddress2;
  }

  public void setTxtShipAddress3(RichInputText txtShipAddress3) {
    this.txtShipAddress3 = txtShipAddress3;
  }

  public RichInputText getTxtShipAddress3() {
    return txtShipAddress3;
  }

  public void setTxtShipAddress4(RichInputText txtShipAddress4) {
    this.txtShipAddress4 = txtShipAddress4;
  }

  public RichInputText getTxtShipAddress4() {
    return txtShipAddress4;
  }

  public void setNiinTable(RichTable niinTable) {
    this.niinTable = niinTable;
  }

  public RichTable getNiinTable() {
    return niinTable;
  }

  public void setLocationTable(RichTable locationTable) {
    this.locationTable = locationTable;
  }

  public RichTable getLocationTable() {
    return locationTable;
  }

  public void setSelectPriority(RichSelectOneChoice selectPriority) {
    this.selectPriority = selectPriority;
  }

  public RichSelectOneChoice getSelectPriority() {
    return selectPriority;
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

  public void setAacNotRequired(boolean aacNotRequired) {
    this.aacNotRequired = aacNotRequired;
  }

  public boolean isAacNotRequired() {
    return aacNotRequired;
  }

  public boolean isRequireQtyFlag() {
    return requireQtyFlag;
  }

  public boolean isConusFlag() {
    return conusFlag;
  }
}
