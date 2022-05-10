package mil.stratis.view.admin.warehouse;

import lombok.val;
import mil.stratis.common.util.Util;
import mil.stratis.model.services.WarehouseSetupImpl;
import mil.stratis.view.admin.AdminBackingHandler;
import mil.stratis.view.user.UserInfo;
import mil.stratis.view.util.ADFUtils;
import mil.stratis.view.util.JSFUtils;
import mil.usmc.mls2.stratis.common.model.enumeration.AuditLogTypeEnum;
import mil.usmc.mls2.stratis.core.domain.event.domain.ChangeLocationEvent;
import mil.usmc.mls2.stratis.core.service.EventService;
import mil.usmc.mls2.stratis.core.utility.ContextUtils;
import mil.usmc.mls2.stratis.core.utility.GlobalConstants;
import oracle.adf.model.binding.DCIteratorBinding;
import oracle.adf.view.rich.component.rich.input.RichInputText;
import oracle.adf.view.rich.component.rich.nav.RichButton;
import org.apache.myfaces.trinidad.context.RequestContext;

import javax.faces.event.ActionEvent;

public class ChangeLocationBacking extends AdminBackingHandler {

  public static final String SERIAL_FLAG = "serialFlag";
  public static final String SERIAL_UPDATE_FLAG = "serialUpdateFlag";
  private transient RichInputText fromLoc;
  private transient RichInputText toLoc;
  private transient RichInputText niinSearch;

  private transient RichButton updateButton;
  private int iUserId;
  private transient RichInputText changeLocTo;
  private transient RichInputText changeLocFrom;
  private transient RichInputText changeNiin;
  private transient RichInputText changeQty;
  private transient RichButton cancelButton;

  private boolean serialFlag = false;
  private boolean serialUpdateFlag = false;
  private transient RichInputText serialOrLotNum;

  boolean hasConflict = false;

  public ChangeLocationBacking() {

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
   * This function populates all to and from fields for matching niins.
   */
  @SuppressWarnings("unused") //called from .jspx
  public void verifyLocations(ActionEvent actionEvent) {
    WarehouseSetupImpl service = getWarehouseSetupModule();
    if (service == null) {
      return;
    }
    service.filterChangeLocationTables(fromLoc.getValue(), niinSearch.getValue());

    // clear the locations and niin values
    fromLoc.setValue(null);
    toLoc.setValue(null);
    niinSearch.setValue(null);
  }

  @SuppressWarnings("unused") //called from .jspx
  public void updateQty(ActionEvent actionEvent) {
    String message;
    String toLocation = "";
    String fromLocation = "";
    String niin = "";
    int qty = 0;

    WarehouseSetupImpl service = getWarehouseSetupModule();
    if (service == null) {
      return;
    }
    boolean error = false;
    //* validate the input

    val globalConstants = ContextUtils.getBean(GlobalConstants.class);
    if (Util.isEmpty(getChangeLocFrom())) {
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "From Location is required.");
      error = true;
    }
    else
      fromLocation = Util.trimUpperCaseClean(getChangeLocFrom().getValue());

    if (Util.isEmpty(getChangeLocTo())) {
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "To Location is required.");
      error = true;
    }
    else
      toLocation = Util.trimUpperCaseClean(getChangeLocTo().getValue());

    if (Util.isEmpty(getChangeNiin())) {
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "NIIN is required.");
      error = true;
    }
    else
      niin = Util.trimUpperCaseClean(getChangeNiin().getValue());

    if (Util.isEmpty(getChangeQty())) {
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "Quantity is required.");
      error = true;
    }
    else {
      String qtyStr = Util.trimClean(getChangeQty().getValue());
      qty = Util.cleanInt(qtyStr);
      if (qty <= 0) {
        JSFUtils.addFacesErrorMessage("INVALID FIELD", "Quantity must be a positive integer");
        error = true;
      }
    }

    if (error)
      return;

    String serialControlFlag = service.isNiinSerialOrLot(niin, true) ? "Y" : "N";
    String lotControlFlag = service.isNiinSerialOrLot(niin, false) ? "Y" : "N";

    //* if serial or lot control, and there is a condition code difference add variables to process scope
    if ((serialControlFlag.equals("Y") || lotControlFlag.equals("Y"))) {
      //* added 04-02-09, do not allow change location of serialized,
      JSFUtils.addFacesErrorMessage("CHG LOC", "NIIN " + niin + " is a controlled item.  Please use rewarehouse.");
      return;
    }
    else {

      //* submit updates to the database

      message = service.changeLocation(fromLocation, toLocation, niin, qty, true, iUserId);

      if (message.length() > 0) {
        JSFUtils.addFacesErrorMessage("CHG LOC", message);
        auditLogLocationUpdate(fromLocation, toLocation, niin, AuditLogTypeEnum.FAILURE);
        return;
      }
      else {
        displaySuccessMessage("Successful Change Location from " + fromLocation + " to " + toLocation);
        auditLogLocationUpdate(fromLocation, toLocation, niin, AuditLogTypeEnum.SUCCESS);
      }
    }

    // re-enable the table - finished change location
    this.changeLocTo.setValue(null);
    this.changeLocFrom.setValue(null);
    this.changeQty.setValue(null);
    this.changeNiin.setValue(null);
    DCIteratorBinding iter = ADFUtils.findIterator("ChangeLocationFromView1Iterator");
    iter.executeQuery();
  }

  private void auditLogLocationUpdate(String fromLocation, String toLocation, String niin, AuditLogTypeEnum resultStatus) {
    val userInfo = (UserInfo) JSFUtils.getManagedBeanValue("userbean");
    ChangeLocationEvent auditLogEvent = ChangeLocationEvent.builder(resultStatus)
        .userInfo(userInfo)
        .fromLocation(fromLocation)
        .toLocation(toLocation)
        .niin(niin)
        .build();
    ContextUtils.getBean(EventService.class).publishEvent(auditLogEvent);
  }

  public void onPageLoad() {
    //do nothing
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
   * and hide certain ui buttons on Warehouse Management - Change Location.
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
  public void btnReviewSubmitYes1(ActionEvent event) {
    //NO-OP  called from .jspx
  }

  @SuppressWarnings("unused")
  public void btnReviewSubmitNo1(ActionEvent event) {
    //NO-OP  called from .jspx
  }

  @SuppressWarnings("unused")
  public void btnReviewSubmitYes2(ActionEvent event) {
    //NO-OP  called from .jspx
  }

  @SuppressWarnings("unused")
  public void btnReviewSubmitNo2(ActionEvent event) {
    //NO-OP  called from .jspx
  }

  @SuppressWarnings("unused") //called from .jspx
  public void backtrack(ActionEvent actionEvent) {
    getChangeLocTo().setValue(null);
    getChangeLocFrom().setValue(null);
    getChangeQty().setValue(null);
    getChangeNiin().setValue(null);
  }

  public void setHasConflict(boolean hasConflict) {
    this.hasConflict = hasConflict;
  }

  public boolean getHasConflict() {
    return hasConflict;
  }

  public void setFromLoc(RichInputText fromLoc) {
    this.fromLoc = fromLoc;
  }

  public RichInputText getFromLoc() {
    return fromLoc;
  }

  public void setToLoc(RichInputText toLoc) {
    this.toLoc = toLoc;
  }

  public RichInputText getToLoc() {
    return toLoc;
  }

  public void setNiinSearch(RichInputText niinSearch) {
    this.niinSearch = niinSearch;
  }

  public RichInputText getNiinSearch() {
    return niinSearch;
  }

  public void setUpdateButton(RichButton updateButton) {
    this.updateButton = updateButton;
  }

  public RichButton getUpdateButton() {
    return updateButton;
  }

  public void setIUserId(int iUserId) {
    this.iUserId = iUserId;
  }

  public int getIUserId() {
    return iUserId;
  }

  public void setChangeLocTo(RichInputText changeLocTo) {
    this.changeLocTo = changeLocTo;
  }

  public RichInputText getChangeLocTo() {
    return changeLocTo;
  }

  @SuppressWarnings("unused") //called from .jspx
  public void setChangeLocFrom(RichInputText changeLocFrom) {
    this.changeLocFrom = changeLocFrom;
  }

  public RichInputText getChangeLocFrom() {
    return changeLocFrom;
  }

  public void setChangeNiin(RichInputText changeNiin) {
    this.changeNiin = changeNiin;
  }

  public RichInputText getChangeNiin() {
    return changeNiin;
  }

  public void setChangeQty(RichInputText changeQty) {
    this.changeQty = changeQty;
  }

  public RichInputText getChangeQty() {
    return changeQty;
  }

  public void setCancelButton(RichButton cancelButton) {
    this.cancelButton = cancelButton;
  }

  public RichButton getCancelButton() {
    return cancelButton;
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
