package mil.stratis.view.admin.warehouse;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.common.util.RegUtils;
import mil.stratis.common.util.Util;
import mil.stratis.model.services.WarehouseSetupImpl;
import mil.stratis.view.admin.AdminBackingHandler;
import mil.stratis.view.user.UserInfo;
import mil.stratis.view.util.ADFUtils;
import mil.stratis.view.util.ELUtils;
import mil.stratis.view.util.JSFUtils;
import mil.usmc.mls2.stratis.common.model.enumeration.AuditLogTypeEnum;
import mil.usmc.mls2.stratis.core.domain.event.domain.CreateNiinEvent;
import mil.usmc.mls2.stratis.core.service.EventService;
import mil.usmc.mls2.stratis.core.utility.AdfLogUtility;
import mil.usmc.mls2.stratis.core.utility.ContextUtils;
import mil.usmc.mls2.stratis.core.utility.GlobalConstants;
import oracle.adf.model.binding.DCIteratorBinding;
import oracle.adf.view.rich.component.rich.RichPopup;
import oracle.adf.view.rich.component.rich.data.RichTable;
import oracle.adf.view.rich.component.rich.input.RichInputDate;
import oracle.adf.view.rich.component.rich.input.RichInputText;
import oracle.adf.view.rich.component.rich.input.RichSelectOneChoice;
import oracle.adf.view.rich.context.AdfFacesContext;
import oracle.adf.view.rich.event.DialogEvent;
import oracle.jbo.Row;
import oracle.jbo.uicli.binding.JUCtrlValueBindingRef;
import org.apache.myfaces.trinidad.context.RequestContext;

import javax.faces.event.ActionEvent;
import java.util.Iterator;

@Slf4j
public class NiinUpdateBacking extends AdminBackingHandler {

  private static final int MAX_LEN_NIIN = 9;
  private static final int MAX_LEN_FSC = 4;

  private static final String NIIN_INFO_ITERATOR = "NiinInfoView1Iterator";
  public static final String NIIN_FLAG = "niinFlag";

  private transient RichInputText fsc;
  private transient RichInputText niin;
  private transient RichInputText nomenclature;
  private transient RichInputDate activityDate;
  private transient RichInputText newNsn;

  private transient RichInputText unitOfIssue;
  private transient RichInputText cube;
  private transient RichInputText weight;
  private transient RichInputText length;
  private transient RichInputText width;
  private transient RichInputText height;
  private transient RichInputText price;
  private transient RichInputText tamcn;
  private transient RichInputText partNumber;
  private transient RichInputText cageCode;
  private transient RichInputText shelfLifeExtension;
  private transient RichSelectOneChoice threshold;
  private transient RichTable niinTable;
  private boolean niinFlag = true;
  private boolean newFlag = false;
  private transient RichSelectOneChoice ui;
  private transient RichSelectOneChoice cognizanceCode;
  private transient RichSelectOneChoice scc;
  private transient RichSelectOneChoice typeOfMaterial;
  private transient RichSelectOneChoice supplyClass;
  private transient RichSelectOneChoice shelfLifeCode;

  public NiinUpdateBacking() {
    //* required to show initial niin update screen with grey, read only and buttons undisabled
  }

  @SuppressWarnings("unused")
  public void submitSaveNiin(ActionEvent actionEvent) {
    resetFocus();
    boolean error = false;
    val globalConstants = ContextUtils.getBean(GlobalConstants.class);
    //* Check all required fields first
    if (isEmpty(getNiin())) {
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "NIIN (National Item Identification Number) is required.");
      error = true;
      setFocus(getNiin());
    }
    if (isEmpty(getFsc())) {
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "FSC (Federal Supply Classification) is required.");
      error = true;
      setFocus(getFsc());
    }
    if (isEmpty(getNomenclature())) {
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "Nomenclature is required.");
      error = true;
      setFocus(getNomenclature());
    }
    if (isEmpty(getScc())) {
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "SCC (Security Classification Code) is required.");
      error = true;
      setFocus(getScc());
    }
    if (isEmpty(getUi())) {
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "UI (Unit of Issue) is required.");
      error = true;
      setFocus(getUi());
    }
    if (isEmpty(getCube())) {
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "Cube is required.");
      error = true;
      setFocus(getCube());
    }
    if (isEmpty(getPrice())) {
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "Price is required.");
      error = true;
      setFocus(getPrice());
    }
    if (isEmpty(getWeight())) {
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "Weight is required.");
      error = true;
      setFocus(getWeight());
    }
    if (isEmpty(getLength())) {
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "Length is required.");
      error = true;
      setFocus(getLength());
    }
    if (isEmpty(getWidth())) {
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "Width is required.");
      error = true;
      setFocus(getWidth());
    }
    if (isEmpty(getHeight())) {
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "Height is required.");
      error = true;
      setFocus(getHeight());
    }
    if (isEmpty(getThreshold())) {
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "Threshold is required.");
      error = true;
      setFocus(getThreshold());
    }
    if (isEmpty(getShelfLifeCode())) {
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "Shelf Life Code is required.");
      error = true;
      setFocus(getShelfLifeCode());
    }

    if (!isEmpty(getNewNsn())) {
      String localNewNsn = Util.trimUpperCaseClean(getNewNsn().getValue());
      if (localNewNsn.length() != 13) {
        JSFUtils.addFacesErrorMessage(globalConstants.getInvalidFieldTag(), "NEW NSN must be 13 numeric characters");
        error = true;
        setFocus(getNewNsn());
      }
      else if (RegUtils.isNotAlphaNumeric(localNewNsn)) {
        JSFUtils.addFacesErrorMessage(globalConstants.getInvalidFieldTag(), "NEW NSN must be alphanumeric characters only");
        error = true;
        setFocus(getNewNsn());
      }
      ELUtils.set("#{bindings.NewNsn.inputValue}", localNewNsn);
    }

    if (error)
      return;

    //* handle case requirements
    String localNiin = Util.trimUpperCaseClean(getNiin().getValue());
    String localFsc = Util.trimUpperCaseClean(getFsc().getValue());
    String localNomenclature = Util.trimUpperCaseClean(getNomenclature().getValue());

    ELUtils.set("#{bindings.Niin.inputValue}", localNiin);
    ELUtils.set("#{bindings.Fsc.inputValue}", localFsc);
    ELUtils.set("#{bindings.Nomenclature.inputValue}", localNomenclature);
    int iUserId = Integer.parseInt((JSFUtils.getManagedBeanValue("userbean.userId")).toString());
    oracle.jbo.domain.Number num = new oracle.jbo.domain.Number(iUserId);
    ELUtils.set("#{bindings.ModifiedBy.inputValue}", num);

    //* Validate fields
    if (localNiin.length() != MAX_LEN_NIIN) {
      JSFUtils.addFacesErrorMessage(globalConstants.getInvalidFieldTag(), "NIIN must be " + MAX_LEN_NIIN + " numeric characters");
      error = true;
      setFocus(getNiin());
    }
    else if (RegUtils.isNotAlphaNumeric(localNiin)) {
      JSFUtils.addFacesErrorMessage(globalConstants.getInvalidFieldTag(), "NIIN must be alphanumeric characters only");
      error = true;
      setFocus(getNiin());
    }

    if (localFsc.length() != MAX_LEN_FSC) {
      JSFUtils.addFacesErrorMessage(globalConstants.getInvalidFieldTag(), "FSC must be " + MAX_LEN_FSC + " numeric characters");
      error = true;
      setFocus(getFsc());
    }
    else if (RegUtils.isNotNumeric(localFsc)) {
      JSFUtils.addFacesErrorMessage(globalConstants.getInvalidFieldTag(), "FSC must be numeric characters only");
      error = true;
      setFocus(getFsc());
    }

    if (isNaD(getPrice())) {
      JSFUtils.addFacesErrorMessage(globalConstants.getInvalidInputTag(), "Price must be a valid decimal.");
      error = true;
      setFocus(getPrice());
    }
    if (isNaD(getCube())) {
      JSFUtils.addFacesErrorMessage(globalConstants.getInvalidInputTag(), "Cube must be numeric only.");
      error = true;
      setFocus(getCube());
    }
    if (isNaD(getLength())) {
      JSFUtils.addFacesErrorMessage(globalConstants.getInvalidInputTag(), "Length must be numeric only.");
      error = true;
      setFocus(getLength());
    }
    if (isNaD(getWidth())) {
      JSFUtils.addFacesErrorMessage(globalConstants.getInvalidInputTag(), "Width must be numeric only.");
      error = true;
      setFocus(getWidth());
    }
    if (isNaD(getHeight())) {
      JSFUtils.addFacesErrorMessage(globalConstants.getInvalidInputTag(), "Height must be numeric only.");
      error = true;
      setFocus(getHeight());
    }
    if (isNaD(getWeight())) {
      JSFUtils.addFacesErrorMessage(globalConstants.getInvalidInputTag(), "Weight must be numeric only.");
      error = true;
      setFocus(getWeight());
    }

    if (error)
      return;

    try {
      String function = "NIIN";
      String fields = "NIIN";
      saveIterator(NIIN_INFO_ITERATOR, function, fields);
      if (newFlag) {
        auditLogCreateNiinEvent(localNiin, localFsc, AuditLogTypeEnum.SUCCESS);
      }

      setNiinFlag(true);
      setNewFlag(false);
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      setNiinFlag(false);
    }
  }

  private void auditLogCreateNiinEvent(String niin, String fsc, AuditLogTypeEnum resultStatus) {
    val userInfo = (UserInfo) JSFUtils.getManagedBeanValue("userbean");
    CreateNiinEvent auditLogEvent = CreateNiinEvent.builder(resultStatus)
        .userInfo(userInfo)
        .niin(niin)
        .fsc(fsc)
        .build();
    ContextUtils.getBean(EventService.class).publishEvent(auditLogEvent);
  }

  public void dialogListener(DialogEvent dialogEvent) {
    if (dialogEvent.getOutcome().equals(DialogEvent.Outcome.ok)) {
      RichPopup popup;
      submitDeleteNiin(null);
      popup = (RichPopup) JSFUtils.findComponentInRoot("confirmDelete");
      popup.hide();

      //Refresh accTable
      AdfFacesContext.getCurrentInstance().addPartialTarget(niinTable);
    }
  }

  @SuppressWarnings("unused")
  public void submitDeleteNiin(ActionEvent actionEvent) {
    resetFocus();
    String deleteKey = "";
    Object oldRowKey = niinTable.getRowKey();
    Iterator<Object> selection = niinTable.getSelectedRowKeys().iterator();
    try {
      while (selection.hasNext()) {
        Object rowKey = selection.next();
        niinTable.setRowKey(rowKey);
        JUCtrlValueBindingRef rowData = (JUCtrlValueBindingRef) niinTable.getRowData();
        Row r = rowData.getRow();
        if (r.getAttribute("NiinId") != null)
          deleteKey = r.getAttribute("NiinId").toString();
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    finally {
      niinTable.setRowKey(oldRowKey);
    }

    if (deleteKey == null || deleteKey.equals("")) {
      displayMessage("[ERROR] Unknown Record for deletion.");
    }
    else {
      try {
        int id = Integer.parseInt(deleteKey);
        String message;
        WarehouseSetupImpl service = getWarehouseSetupModule();
        if (service == null) {
          displayMessage("[MODULE] Error occurred while trying to delete ");
          return;
        }

        message = service.deleteNiin(id);
        if (message.equals("")) {
          DCIteratorBinding iter = ADFUtils.findIterator(NIIN_INFO_ITERATOR);
          iter.executeQuery();
          displaySuccessMessage("NIIN deleted.");
        }
        else {
          displayMessage(message);
        }
      }
      catch (Exception e) {
        displayMessage("[ERROR] Error occurred while trying to delete ");
      }
    }
  }

  @SuppressWarnings("unused")
  public void submitResetNiin(ActionEvent actionEvent) {
    resetKeepPosition(NIIN_INFO_ITERATOR);
    setNiinFlag(false);
  }

  @SuppressWarnings("unused")
  public void submitCancelNiin(ActionEvent actionEvent) {
    resetFocus();
    cancel(NIIN_INFO_ITERATOR);
    setNiinFlag(true);
    setNewFlag(false);
  }

  @SuppressWarnings("unused")
  public void submitUpdateNiin(ActionEvent event) {
    resetFocus();
    setNiinFlag(false);
    setNewFlag(false);
  }

  private void resetFocus() {
    //NO-OP
  }

  public void setInitialNiinFlag() {
    RequestContext afContext = RequestContext.getCurrentInstance();
    Object obj = afContext.getPageFlowScope().get(NIIN_FLAG);
    if (obj == null)
      setNiinFlag(true);
  }

  /**
   * Set aac flag back on (true) to turn on readonly+style
   * and hide certain ui buttons on Warehouse Management - NIIN Update.
   */
  public void setNiinFlag(boolean show) {

    RequestContext afContext = RequestContext.getCurrentInstance();
    //* for use in the web, maintaining state
    afContext.getPageFlowScope().put(NIIN_FLAG, show);
    this.niinFlag = show;
  }

  public boolean getNiinFlag() {
    RequestContext afContext = RequestContext.getCurrentInstance();
    boolean flag = niinFlag;
    Object obj = afContext.getPageFlowScope().get(NIIN_FLAG);
    if (obj != null)
      flag = Boolean.parseBoolean(obj.toString());

    return niinFlag || flag;
  }

  /**
   * Set aac flag back on (true) to turn on readonly+style
   * and hide certain ui buttons on Warehouse Management - NIIN Update.
   */
  public void setNewFlag(boolean show) {

    RequestContext afContext = RequestContext.getCurrentInstance();
    //* for use in the web, maintaining state
    afContext.getPageFlowScope().put("newFlag", show);
    this.newFlag = show;
  }

  public boolean getNewFlag() {
    RequestContext afContext = RequestContext.getCurrentInstance();
    boolean flag = newFlag;
    Object obj = afContext.getPageFlowScope().get("newFlag");
    if (obj != null)
      flag = Boolean.parseBoolean(obj.toString());

    return newFlag || flag;
  }

  public void setFsc(RichInputText fsc) {
    this.fsc = fsc;
  }

  public RichInputText getFsc() {
    return fsc;
  }

  public void setNiin(RichInputText niin) {
    this.niin = niin;
  }

  public RichInputText getNiin() {
    return niin;
  }

  public void setNomenclature(RichInputText nomenclature) {
    this.nomenclature = nomenclature;
  }

  public RichInputText getNomenclature() {
    return nomenclature;
  }

  public void setActivityDate(RichInputDate activityDate) {
    this.activityDate = activityDate;
  }

  public RichInputDate getActivityDate() {
    return activityDate;
  }

  public void setNewNsn(RichInputText newNsn) {
    this.newNsn = newNsn;
  }

  public RichInputText getNewNsn() {
    return newNsn;
  }

  public void setUnitOfIssue(RichInputText unitOfIssue) {
    this.unitOfIssue = unitOfIssue;
  }

  public RichInputText getUnitOfIssue() {
    return unitOfIssue;
  }

  public void setCube(RichInputText cube) {
    this.cube = cube;
  }

  public RichInputText getCube() {
    return cube;
  }

  public void setWeight(RichInputText weight) {
    this.weight = weight;
  }

  public RichInputText getWeight() {
    return weight;
  }

  public void setLength(RichInputText length) {
    this.length = length;
  }

  public RichInputText getLength() {
    return length;
  }

  public void setWidth(RichInputText width) {
    this.width = width;
  }

  public RichInputText getWidth() {
    return width;
  }

  public void setHeight(RichInputText height) {
    this.height = height;
  }

  public RichInputText getHeight() {
    return height;
  }

  public void setPrice(RichInputText price) {
    this.price = price;
  }

  public RichInputText getPrice() {
    return price;
  }

  public void setTamcn(RichInputText tamcn) {
    this.tamcn = tamcn;
  }

  public RichInputText getTamcn() {
    return tamcn;
  }

  public void setPartNumber(RichInputText partNumber) {
    this.partNumber = partNumber;
  }

  public RichInputText getPartNumber() {
    return partNumber;
  }

  public void setCageCode(RichInputText cageCode) {
    this.cageCode = cageCode;
  }

  public RichInputText getCageCode() {
    return cageCode;
  }

  public void setShelfLifeExtension(RichInputText shelfLifeExtension) {
    this.shelfLifeExtension = shelfLifeExtension;
  }

  public RichInputText getShelfLifeExtension() {
    return shelfLifeExtension;
  }

  public void setThreshold(RichSelectOneChoice threshold) {
    this.threshold = threshold;
  }

  public RichSelectOneChoice getThreshold() {
    return threshold;
  }

  public void setNiinTable(RichTable niinTable) {
    this.niinTable = niinTable;
  }

  public RichTable getNiinTable() {
    return niinTable;
  }

  public boolean isNiinFlag() {
    return niinFlag;
  }

  public boolean isNewFlag() {
    return newFlag;
  }

  public void setUi(RichSelectOneChoice ui) {
    this.ui = ui;
  }

  public RichSelectOneChoice getUi() {
    return ui;
  }

  public void setCognizanceCode(RichSelectOneChoice cognizanceCode) {
    this.cognizanceCode = cognizanceCode;
  }

  public RichSelectOneChoice getCognizanceCode() {
    return cognizanceCode;
  }

  public void setScc(RichSelectOneChoice scc) {
    this.scc = scc;
  }

  public RichSelectOneChoice getScc() {
    return scc;
  }

  public void setTypeOfMaterial(RichSelectOneChoice typeOfMaterial) {
    this.typeOfMaterial = typeOfMaterial;
  }

  public RichSelectOneChoice getTypeOfMaterial() {
    return typeOfMaterial;
  }

  public void setSupplyClass(RichSelectOneChoice supplyClass) {
    this.supplyClass = supplyClass;
  }

  public RichSelectOneChoice getSupplyClass() {
    return supplyClass;
  }

  public void setShelfLifeCode(RichSelectOneChoice shelfLifeCode) {
    this.shelfLifeCode = shelfLifeCode;
  }

  public RichSelectOneChoice getShelfLifeCode() {
    return shelfLifeCode;
  }
}
