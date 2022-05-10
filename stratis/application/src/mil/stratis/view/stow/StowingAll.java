package mil.stratis.view.stow;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.common.util.RegUtils;
import mil.stratis.view.session.MdssBackingBean;
import mil.stratis.view.util.JSFUtils;
import mil.stratis.view.user.UserInfo;
import mil.usmc.mls2.stratis.common.domain.model.LabelType;
import mil.usmc.mls2.stratis.core.domain.model.Equipment;
import mil.usmc.mls2.stratis.core.service.EquipmentService;
import mil.usmc.mls2.stratis.core.service.StowService;
import mil.usmc.mls2.stratis.core.utility.*;
import oracle.adf.model.BindingContext;
import oracle.adf.view.rich.component.rich.input.RichInputText;
import oracle.adf.view.rich.context.AdfFacesContext;
import oracle.binding.BindingContainer;
import oracle.jbo.Row;
import oracle.jbo.server.ViewRowImpl;
import oracle.jbo.uicli.binding.JUCtrlListBinding;
import org.apache.myfaces.trinidad.event.LaunchEvent;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;

@Slf4j
@NoArgsConstructor
public class StowingAll extends MdssBackingBean {

  private transient String actionForwardStr = "";
  private transient String errStr = "error";
  private transient Object expObj = 0;
  private transient RichInputText scanSIDLabel;
  private transient RichInputText scanLocLabel;
  private transient RichInputText enterNiinLastTwoDigits;
  private transient RichInputText enterQtyStowed;
  private transient RichInputText stowArea;
  private transient RichInputText availQty;
  private transient RichInputText availQtyR;
  private boolean uiDialog = false;
  private String textForDialog = "";
  String reLocateStr = "N";

  // listener for each time the page is loaded

  @Override
  public void onPageLoad() {

    if (!this.isPostback()) {
      init();
    }
  } // initial page loading function

  public void init() { //561 as sample
    getStowingAMService().getScannedStowSIDs(Integer.parseInt(JSFUtils.getManagedBeanValue("userbean.workstationId").toString()), Long.parseLong(JSFUtils.getManagedBeanValue("userbean.userId").toString()));

    this.getStowingAMService().getScannedSIDDetails("x"); //reset the reassign VO
    this.getWorkloadManagerService().clearLocationList(); //reset the location list

    this.actionForwardStr = "";
    this.errStr = "error";

    JSFUtils.setManagedBeanValue("userbean.specialMessage", ""); //Reset informational message
  }

  public String actionForwardStrForStowingNormalOnCancel() {
    //Checks if there are any Stows and
    //takes you to next stow if any exist. Otherwise comes out of stow Normal
    try {
      Row ro = getStowingAMService().getStowingGenQryVO1().getCurrentRow();
      if ((ro.getAttribute("BypassCount") == null) || (Integer.parseInt(ro.getAttribute("BypassCount").toString()) <= 0)) {
        this.setExpObj(0);
        if (getStowingAMService().getStowingGenQryVO1().hasNext()) {
          getStowingAMService().getStowingGenQryVO1().next();
          this.performCarouselMove(1); //Move to the next pick
          this.setExpObj(0);
          actionForwardStr = "";
        }
        else {
          getStowingAMService().getStowingGenQryVO1().clearCache();
          this.setExpObj(0);
          actionForwardStr = "GoStowingHome";
        }
      }
      else {
        actionForwardStr = "";
        this.setExpObj(6);
      }
      errStr = "";
      return actionForwardStr;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return null;
  }

  @SuppressWarnings("unused")  //called from .jspx
  public String actionForwardStrForStowingNormalOnSubmit() {
    //Checks if there are any errors. If no errors forwards you to
    //NIIN verification else remains in same window for fixing.
    JSFUtils.setManagedBeanValue("userbean.specialMessage", ""); //Reset informational message
    try {
      Object lL = this.getScanLocLabel().getValue();
      Row ro = getStowingAMService().getStowingGenQryVO1().getCurrentRow();
      if (ro.getAttribute("Locked").toString().equalsIgnoreCase("Y")) {
        errStr = errStr + "LOC_LOCKED";
        JSFUtils.addFacesErrorMessage("LOC_LOCKED", "Location locked - ByPass.");
      }
      if ((lL == null) || (lL.toString().length() <= 8)) {
        errStr = errStr + "LOC_ERR";
        JSFUtils.addFacesErrorMessage("LOC_ERR_LEN", "Invalid location label length.");
      }
      this.setExpObj(0);
      if (errStr.length() <= 0) {
        //Logic check and prompt for old U/I during picking

        String rStr = this.getWorkloadManagerService().getUIDifferenceValues(Long.parseLong((ro.getAttribute("NiinLocId") == null) ? "0" : ro.getAttribute("NiinLocId").toString()));
        //For NSN Remark
        if ((ro.getAttribute("NsnRemark") != null) && (ro.getAttribute("NsnRemark").toString().equalsIgnoreCase("Y"))) {
          rStr = rStr + ", NEED_NSN_RELABEL";
        }

        if (rStr.compareToIgnoreCase("CONV_ERROR") == 0) {
          errStr = errStr + "CONV_ERROR";
          JSFUtils.addFacesErrorMessage("CONV_ERROR", "The U/I at this location is not re-packaged and STRATIS cannot guide you with the conversion. Please talk to supervisor.");
          actionForwardStr = "";
          return actionForwardStr;
        }
        else if ((rStr.contains("NEED_REPACK")) || (rStr.contains("NEED_NSN_RELABEL")) || (rStr.contains("NEED_EXP_RELABEL"))) {
          String msgStr = "";
          if (rStr.contains("NEED_NSN_RELABEL")) {
            String fsc = "";
            if (ro.getAttribute("Fsc") != null) {
              fsc = ro.getAttribute("Fsc").toString();
            }
            if (!fsc.isEmpty()) {
              msgStr = "Remark FSC on Location Inventory to " + fsc;
            }
            else {
              msgStr = "Remark FSC on Location Inventory";
            }
            JSFUtils.addFacesInformationMessage(msgStr);
            ro.setAttribute("NsnRemark", "N");
          }

          if (rStr.contains("NEED_REPACK")) {
            this.getWorkloadManagerService().getErrorRow(rStr.split(",")[0]);
            JSFUtils.addFacesInformationMessage(getWorkloadManagerService().buildDialogTxt(rStr, "Repack location inventory from :2 :1 to :4 :3"));
          }

          actionForwardStr = "GoStowingNormalNiinVer";
          JSFUtils.setManagedBeanValue("userbean.specialMessage", msgStr);
          this.performCarouselMove(0);
          return actionForwardStr;
        }
        else
          //Stowing going through normal process
          actionForwardStr = "GoStowingNormalNiinVer";
        this.performCarouselMove(0); //Move for the opposite side
      }
      else
        actionForwardStr = "";
      errStr = "";
      this.setScanLocLabel(null);
      return actionForwardStr;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return null;
  }

  public void locationValidator(@SuppressWarnings("all") FacesContext facesContext,
                                UIComponent uiComponent, Object object) {
    try {

      if (Long.parseLong(JSFUtils.getManagedBeanValue("userbean.selected2Index").toString()) == 1)
        return;
      String lL = getStowingAMService().getStowingGenQryVO1().getCurrentRow().getAttribute("LocationLabel").toString();
      String sL = "";
      boolean f = true;
      if (uiComponent.getId().compareTo("stowNormalCancelId") != 0) {
        if (object != null) {
          sL = object.toString();
        }
        else {
          f = false;
          errStr = errStr + "LOC_ERR";
          JSFUtils.addFacesErrorMessage("LOC_ERR", "Please scan or enter the complete location label.");
        }
        if (f) {
          if (sL.length() != 9) {
            f = false;
            errStr = errStr + "LOC_ERR";
            JSFUtils.addFacesErrorMessage("LOC_ERR", "Please scan or enter the complete location label.");
          }
          else if ((sL.toUpperCase().trim().substring(0, 3).compareTo(lL.trim().substring(0, 3)) != 0)) {
            f = false;
            errStr = errStr + "LOC_ERR";
            JSFUtils.addFacesErrorMessage("LOC_ERR", "Verify and enter correct location or bypass stow.");
          }
          else if ((sL.toUpperCase().trim().compareTo(lL.trim()) != 0)) {
            //Letting the user choose another empty location in same WAC
            String lIdStr = getStowingAMService().validateStowLocation(sL.toUpperCase().trim());
            if ((lIdStr == null) || (lIdStr.length() <= 0)) {
              f = false;
              errStr = errStr + "STOW_LOC_NOTEMPTY";
              JSFUtils.addFacesErrorMessage("STOW_LOC_NOTEMPTY", "Verify and enter correct location or bypass stow.");
            }
          }
        }
      }
      if (f) {
        errStr = "";
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  public void setExpObj(Object expObj) {
    this.expObj = expObj;
  }

  public Object getExpObj() {
    if (expObj == null)
      expObj = 0;
    return expObj;
  }

  public void setScanLocLabel(RichInputText scanLocLabel) {
    this.scanLocLabel = scanLocLabel;
  }

  public RichInputText getScanLocLabel() {
    return scanLocLabel;
  }

  /**
   * Performs validations for error messaging and calls backend methods fro adding the stow to scanned list of stows.
   */
  public void addScannedSIDToStowList(@SuppressWarnings("all") ActionEvent actionEvent) {
    val stowService = ContextUtils.getBean(StowService.class);
    val equipmentService = ContextUtils.getBean(EquipmentService.class);
    val workstationId = JSFUtils.getManagedBeanValue("userbean.workstationId").toString();
    val workstation = equipmentService.findById(Integer.parseInt(workstationId)).orElseGet(Equipment::new);

    int flag = -2;
    if (this.getScanSIDLabel().getLocalValue() == null) {
      errStr = errStr + "SID_ERR";
      JSFUtils.addFacesErrorMessage("SID_ERR", "Please enter SID.");
      return;
    }

    if (this.getScanSIDLabel().getLocalValue().toString().length() == 9) {
      flag = stowService.updateStowScannedFlag(
          workstation.getWac(),
          this.getScanSIDLabel().getLocalValue().toString().trim().toUpperCase(),
          Integer.parseInt(JSFUtils.getManagedBeanValue("userbean.userId").toString()),
          false);
    }

    if (flag == 3)
      JSFUtils.addFacesErrorMessage("SID_ERR", "SID is assigned to a different WAC.");

    if (flag <= -3) {
      if (flag == -4) {
        JSFUtils.addFacesErrorMessage("SID_ERR", "SID in Exception Processing.");
      }
      else {
        JSFUtils.addFacesErrorMessage("SID_ERR", "Invalid SID.");
      }
      errStr = errStr + "SID_ERR";
    }
    else if (flag == -2) {
      JSFUtils.addFacesErrorMessage("SID_ERR", "SID already assigned.");
    }
    else if (flag == -1) {
      errStr = errStr + "SID_ERR";
      JSFUtils.addFacesErrorMessage("SID_ERR", "SID part of rewarehouse, picks incomplete.");
    }
    else if (flag == 0) {
      errStr = errStr + "SID_ERR";
      JSFUtils.addFacesErrorMessage("NOTICE", "SID previously scanned in.");
    }
    else {
      getStowingAMService().getScannedStowSIDs(
          Integer.parseInt(JSFUtils.getManagedBeanValue("userbean.workstationId").toString()),
          Long.parseLong(JSFUtils.getManagedBeanValue("userbean.userId").toString()));
    }
    this.getScanSIDLabel().setValue("");
  }

  /**
   * This method checks if there are scanned stows. If there then it will take them to next screen to start stowing.
   */
  @SuppressWarnings("unused")  //called from .jspx
  public String actionForwardForStartStowing() {
    val sortBuilder = ContextUtils.getBean(LocationSortBuilder.class);
    val sortString = sortBuilder.getSortString(LocationSortBuilder.QueryType.ADF);
    int stcount = getStowingAMService().buildStowRS(0, Long.parseLong(JSFUtils.getManagedBeanValue("userbean.workstationId").toString()), Long.parseLong(JSFUtils.getManagedBeanValue("userbean.userId").toString()), sortString);
    if (stcount > 0) {
      actionForwardStr = "GoStowingNormalLocation";
      //Call the carousel move
      this.performCarouselMove(1);
    }
    else {
      errStr = errStr + "SID_ERR";
      JSFUtils.addFacesErrorMessage("SID_ERR", "Sorry no SIDs are scanned for stowing.");
      actionForwardStr = "";
    }
    this.scanSIDLabel.setValue(null);
    return actionForwardStr;
  }

  public void niinLastTwoValidation(@SuppressWarnings("all") FacesContext facesContext,
                                    @SuppressWarnings("all") UIComponent uiComponent,
                                    Object object) {
    try {
      if (Long.parseLong(JSFUtils.getManagedBeanValue("userbean.selected2Index").toString()) == 1)
        return;
      String lL = getStowingAMService().getStowingGenQryVO1().getCurrentRow().getAttribute("Niin").toString();
      lL = lL.substring(lL.length() - 2);
      String sL = "";
      boolean f = true;
      if (object != null) {
        sL = object.toString();
      }
      else
        f = false;
      if (f && (sL.length() <= 0) || (sL.toUpperCase().trim().compareTo(lL.trim()) != 0)) {
        f = false;
      }
      if (!f) {
        if (errStr.contains("error"))
          errStr = "";
        errStr = "NIIN_ERR";
        JSFUtils.addFacesErrorMessage("NIIN_ERR", "The digits entered do not match the NIIN.");
        this.setExpObj(0);
      }
      else {
        errStr = "";
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  /**
   * METHOD      : stowQtyValidation
   * AUTHOR      : KISHORE SONNAKUL
   * DESCRIPTION : Checks and validates stow quantity.
   */
  public void stowQtyValidation(@SuppressWarnings("all") FacesContext facesContext,
                                @SuppressWarnings("all") UIComponent uiComponent, Object object) {
    try {
      if (Long.parseLong(JSFUtils.getManagedBeanValue("userbean.selected2Index").toString()) == 1)
        return;
      int sQty = 0;
      int qtyS = 0;
      int rQty = 0;
      if (getStowingAMService().getStowingGenQryVO1().getCurrentRow().getAttribute("StowQty") != null)
        sQty = Integer.parseInt(getStowingAMService().getStowingGenQryVO1().getCurrentRow().getAttribute("StowQty").toString());
      if (getStowingAMService().getStowingGenQryVO1().getCurrentRow().getAttribute("QtyToBeStowed") != null)
        qtyS = Integer.parseInt(getStowingAMService().getStowingGenQryVO1().getCurrentRow().getAttribute("QtyToBeStowed").toString());
      boolean f = true;
      if (object != null) {
        //MCF 08-05-2014: Required separate error message for whole numbers vs. numeric.
        if (RegUtils.isDecimal(object.toString())) {
          f = false;
          errStr = errStr + "NIIN_ERR1";
          JSFUtils.addFacesErrorMessage("QTY_ERR1", "Quantity must be whole number.");
        }
        else {
          if (RegUtils.isNotNumeric(object.toString())) {
            f = false;
            errStr = errStr + "NIIN_ERR1";
            JSFUtils.addFacesErrorMessage("QTY_ERR1", "Quantity can only be numeric.");
          }
          else
            rQty = Integer.parseInt(object.toString());
        }
      }
      else {
        f = false;
        if (errStr.contains("error"))
          errStr = "";
        errStr = errStr + "NIIN_ERR1";
        JSFUtils.addFacesErrorMessage("QTY_ERR1", "Please enter valid quantity.");
      }
      if (f) {
        if (!this.getExpObj().toString().equals("7")) {
          if (((rQty <= 0) || (rQty > (qtyS - sQty))) && (errStr.length() <= 0)) {
            errStr = errStr + "NIIN_ERR2";
            if (rQty <= 0) {
              JSFUtils.addFacesErrorMessage("QTY_ERR2", "Quantity cannot be less than 0.");
            }
            else {
              JSFUtils.addFacesErrorMessage("QTY_ERR2", "Quantity entered is greater than the receipted quantity.");
            }
            this.setExpObj(0);
          }
          else if (((rQty < (qtyS - sQty)) && (errStr.length() <= 0)) && (!this.getExpObj().equals(2))) { // Check if this already was set
            this.setExpObj(3);
          }
        }
      }
      else {
        if (errStr.contains("error"))
          errStr = "";
        this.setExpObj(0);
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  public void setEnterNiinLastTwoDigits(RichInputText enterNiinLastTwoDigits) {
    this.enterNiinLastTwoDigits = enterNiinLastTwoDigits;
  }

  public RichInputText getEnterNiinLastTwoDigits() {
    return enterNiinLastTwoDigits;
  }

  public void setEnterQtyStowed(RichInputText enterQtyStowed) {
    this.enterQtyStowed = enterQtyStowed;
  }

  public RichInputText getEnterQtyStowed() {
    return enterQtyStowed;
  }

  /**
   * Sets things to show Stow Refuse reasons.
   */
  @SuppressWarnings("unused")  //called from .jspx
  public String actionForwardStrForStowingNormalNiinVerOnRefuse() {
    //Refreshes screen to enter the Refuse reason on current screen.
    try {

      this.setExpObj(2);
      this.setReLocateStr("Y");
      errStr = "";
      actionForwardStr = "";
      return actionForwardStr;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return null;
  }

  @SuppressWarnings("unused")  //called from .jspx
  public String actionForwardStrForStowingNormalNiinVerOnLocationFull() {
    //Performs usual save and call functions to create error queue records.
    //Calls work load manger to build the transactions. And then forwards to next
    //stow if any exist other wise exit Stow.
    try {
      if (getStowingAMService().getStowingGenQryVO1().hasNext()) {
        getStowingAMService().getStowingGenQryVO1().next();
        actionForwardStr = "GoStowingNormalLocation";
      }
      else {
        getStowingAMService().getStowingGenQryVO1().clearCache();
        actionForwardStr = "GoStowingHome";
      }
      errStr = "";
      return actionForwardStr;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return null;
  }

  @SuppressWarnings("unused")  //called from .jspx
  public String actionForwardStrForStowingNormalNiinVerOnSubmit() {
    //Calls validations via value change listeners.
    //Logic check and launch for Inventory check, and
    //final save and forward to Next Stow
    try {
      int uid = Integer.parseInt(JSFUtils.getManagedBeanValue("userbean.userId").toString());
      Row ro = getStowingAMService().getStowingGenQryVO1().getCurrentRow();
      Object lL = this.getEnterNiinLastTwoDigits().getValue();
      if (lL == null) {
        errStr = "NIIN_ERR";
        JSFUtils.addFacesErrorMessage("NIIN_ERR", "Last two digits of NIIN required.");
      }
      Object lQ = getEnterQtyStowed().getValue();
      if (lQ == null) {
        errStr = errStr + "QTY_ERR";
        JSFUtils.addFacesErrorMessage("QTY_ERR", "Missing Quantity.");
      }

      if ((errStr.length() <= 0) && (this.getExpObj().equals(0))) { //Update the database pick qty and status
        errStr = this.getStowingAMService().stowRowStatusChange(ro.getAttribute("Sid").toString(), Integer.parseInt(JSFUtils.getManagedBeanValue("userbean.userId").toString()), Integer.parseInt(lQ.toString()), Integer.parseInt((ro.getAttribute("StowQty") == null) ? "0" : ro.getAttribute("StowQty").toString()), this.getExpObj().toString());
        getStowingAMService().getStowingGenQryVO1().getCurrentRow();

        if (errStr.length() == 0) {
          // Close out any Error queue records for this pick
          this.getWorkloadManagerService().updateErrorQueueRecordStatus("SID", ro.getAttribute("Sid").toString(), uid);
          actionForwardStr = forwardToNextStow();
        }
        else {
          showStowErrors(errStr);
          if (errStr.contains("DUPLICATE_STW")) actionForwardStr = forwardToNextStow();
          else actionForwardStr = "";
        }
      }
      else {
        actionForwardStr = "";
      }
      errStr = "";
      this.setEnterNiinLastTwoDigits(null);
      this.setEnterQtyStowed(null);
      return actionForwardStr;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return null;
  }

  /**
   * Determines if there is another stow to process.
   * Handles VO setup and user redirection.
   */
  private String forwardToNextStow() {

    if (getStowingAMService().getStowingGenQryVO1().hasNext()) {
      getStowingAMService().getStowingGenQryVO1().next();
      //Move for the same side at end of a pick
      performCarouselMove(0);
      return "GoStowingNormalLocation";
    }
    else {
      getStowingAMService().getStowingGenQryVO1().clearCache();
      return "GoStowingHome";
    }
  }

  @SuppressWarnings("unused")  //called from .jspx
  public String actionForwardStrForStowingNormalNiinVerOnByPass() {
    try {
      Row ro = getStowingAMService().getStowingGenQryVO1().getCurrentRow();
      if ((ro.getAttribute("BypassCount") == null) || (Integer.parseInt(ro.getAttribute("BypassCount").toString()) <= 0)) {
        if (getStowingAMService().getStowingGenQryVO1().hasNext()) {
          this.setExpObj(0);
          actionForwardStr = "GoStowingNormalLocation";
          getStowingAMService().getStowingGenQryVO1().next();
          this.performCarouselMove(0); //Move for the same side at end of a pick
        }
        else {
          getStowingAMService().getStowingGenQryVO1().clearCache();
          this.setExpObj(0);
          actionForwardStr = "GoStowingHome";
        }
      }
      else {
        actionForwardStr = "";
        this.setExpObj(6);
      }
      errStr = "";
      this.setEnterNiinLastTwoDigits(null);
      this.setEnterQtyStowed(null);
      return actionForwardStr;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return null;
  }

  public String actionForwardStrForStowingNormalNiinVerOnQtyDone() {
    try {
      Row ro = getStowingAMService().getStowingGenQryVO1().getCurrentRow();
      //Update the database stow qty and status REMOVED from here to UpdateStowLocation
      if (this.getExpObj().toString().equals("7")) {
        val sid = ro.getAttribute("Sid").toString();
        val userId = JSFUtils.getManagedBeanValue("userbean.userId").toString();
        errStr = this.getStowingAMService().stowRowStatusChange(sid, Integer.parseInt(userId), Integer.parseInt(this.getEnterQtyStowed().getValue().toString()), Integer.parseInt((ro.getAttribute("StowQty") == null) ? "0" : ro.getAttribute("StowQty").toString()), this.getExpObj().toString());

        //47 is STOW_LOSS_D9 error in error table
        this.getWorkloadManagerService().createErrorQueueRecord("47", "SID", sid, userId, null);
        if (errStr.length() == 0) {
          actionForwardStr = forwardToNextStow();
        }
        else {
          showStowErrors(errStr);
          if (errStr.contains("DUPLICATE_STW")) actionForwardStr = forwardToNextStow();
          else actionForwardStr = "";
        }
        errStr = "";
        this.setAvailQtyR(null);
        this.setAvailQty(null);
        this.setEnterNiinLastTwoDigits(null);
        this.setEnterQtyStowed(null);
      }
      if (errStr.length() != 0) {
        showStowErrors(errStr);
        errStr = "";
        actionForwardStr = "";
      }
      else if (!this.getExpObj().toString().equals("7")) {
        this.setExpObj(2);
        errStr = "";
        actionForwardStr = "";
      }
      else
        this.setExpObj(0);
      return actionForwardStr;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return null;
  }

  //Following method is never used as we do not do INVENTORY checks in Stowing
  @SuppressWarnings("unused")  //called from .jspx
  public String actionForwardStrForStowingNormalNiinVerOnInvQtyDone() {
    try {
      Row ro = getStowingAMService().getStowingGenQryVO1().getCurrentRow();
      if ((((this.getAvailQty().getValue() == null) || (this.getAvailQtyR().getValue() == null)) || (!this.getAvailQty().getValue().equals(this.getAvailQtyR().getValue()))) && (!this.getExpObj().toString().equalsIgnoreCase("7"))) {
        errStr = "INV_CHK";
        JSFUtils.addFacesErrorMessage("INV_CHK", "Quantities do not match. Please correct");
        actionForwardStr = "";
        this.setExpObj(5);
      }
      else {
        errStr = this.getStowingAMService().stowRowStatusChange(ro.getAttribute("Sid").toString(), Integer.parseInt(JSFUtils.getManagedBeanValue("userbean.userId").toString()), Integer.parseInt(this.getEnterQtyStowed().getValue().toString()), Integer.parseInt((ro.getAttribute("StowQty") == null) ? "0" : ro.getAttribute("StowQty").toString()), this.getExpObj().toString());
        if (errStr.length() == 0) {
          actionForwardStr = forwardToNextStow();
        }
        else {
          showStowErrors(errStr);
          if (errStr.contains("DUPLICATE_STW")) actionForwardStr = forwardToNextStow();
          else actionForwardStr = "";
        }
        errStr = "";
        this.setAvailQtyR(null);
        this.setAvailQty(null);
        this.setExpObj(0);
        this.setEnterNiinLastTwoDigits(null);
        this.setEnterQtyStowed(null);
      }
      return actionForwardStr;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return null;
  }

  public void showQtyStowedErrorMessage(@SuppressWarnings("all") ActionEvent actionEvent) {
    try {
      errStr = "";
      actionForwardStr = "";
      this.setExpObj(0);
      errStr = errStr + "NIIN_QTY_ERR2";
      JSFUtils.addFacesErrorMessage("QTY_ERR2", "Invalid quantity.");
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  @SuppressWarnings("unused")  //called from .jspx
  public String actionForwardStrForStowingNormalNiinVerOnRefuseDone() {
    try {
      Row ro = getStowingAMService().getStowingGenQryVO1().getCurrentRow();
      BindingContainer bindings = BindingContext.getCurrent().getCurrentBindingsEntry();
      JUCtrlListBinding listBinding = (JUCtrlListBinding) bindings.get("StowRefuseList");
      ViewRowImpl selectedRow = (ViewRowImpl) listBinding.getSelectedValue();
      String eid = selectedRow.getAttribute("Eid").toString();
      val sid = ro.getAttribute("Sid").toString();
      val userId = JSFUtils.getManagedBeanValue("userbean.userId").toString();
      this.getWorkloadManagerService().createErrorQueueRecord(eid, "SID", sid, userId, null);
      this.getStowingAMService().stowRowRefused("STOW REFUSED", 0, sid, Integer.parseInt(userId));
      if (getStowingAMService().getStowingGenQryVO1().hasNext()) {
        actionForwardStr = "GoStowingNormalLocation";
        getStowingAMService().getStowingGenQryVO1().next();
        this.performCarouselMove(0); //Move for the same side at end of a pick
      }
      else {
        getStowingAMService().getStowingGenQryVO1().clearCache();
        actionForwardStr = "GoStowingHome";
      }
      errStr = "";
      this.setExpObj(0);
      this.setEnterNiinLastTwoDigits(null);
      this.setEnterQtyStowed(null);
      return actionForwardStr;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return null;
  }

  public String actionForwardStrForStowingNormalNiinVerOnCancel() {
    try {
      actionForwardStr = "";
      errStr = "";
      setExpObj(0);
      return actionForwardStr;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return null;
  }

  @SuppressWarnings("unused")  //called from .jspx
  public void assignExpObj(@SuppressWarnings("all") ActionEvent actionEvent) {
    // Set the switch for Refuse
    this.setExpObj(1);
  }

  @SuppressWarnings("unused")  //called from .jspx
  public void assignExpObj7(@SuppressWarnings("all") ActionEvent actionEvent) {
    // Set the switch for Loss
    this.setExpObj(7);
  }

  public void BuildLocationList(@SuppressWarnings("all") ActionEvent actionEvent) {
    try {
      Row r = this.getStowingAMService().getStowingGenQryVO1().getCurrentRow();
      String stArea = "";
      String wareHouseId = "ALL";
      if (this.getStowArea().getSubmittedValue() != null)
        stArea = this.getStowArea().getSubmittedValue().toString();

      if (!Boolean.parseBoolean(JSFUtils.getManagedBeanValue("userbean.adminLevelUser").toString()))
        wareHouseId = this.getWorkloadManagerService().getWareHouseId(stArea, Integer.parseInt(JSFUtils.getManagedBeanValue("userbean.workstationId").toString()));
      if (wareHouseId.contains("NONE")) {
        JSFUtils.addFacesErrorMessage("STOW_AREA_ERR", "Sorry only admin users are allowed to assign locations out side of this warehouse.");
      }
      else {
        String errStr1;
        errStr1 = this.getWorkloadManagerService().buildLocationList(stArea, "STOW_ID", r.getAttribute("StowId").toString(), wareHouseId, -1);
        if (errStr1 != null && errStr1.length() > 0)
          JSFUtils.addFacesErrorMessage("ERR", errStr1);
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  public void setStowArea(RichInputText stowArea) {
    this.stowArea = stowArea;
  }

  public RichInputText getStowArea() {
    return stowArea;
  }

  @SuppressWarnings("unused")  //called from .jspx
  public void UpdateStowLocation(@SuppressWarnings("all") ActionEvent actionEvent) {
    // Call to update the location
    try {
      Row r = this.getWorkloadManagerService().getLocationList1().getCurrentRow();
      String errLs;
      int stowQtyI = -1;

      val userId = JSFUtils.getManagedBeanValue("userbean.userId").toString();
      val userSelectedIndex2 = Long.parseLong(JSFUtils.getManagedBeanValue("userbean.selected2Index").toString());
      if (this.getReLocateStr().equalsIgnoreCase("Y")) {
        Row ro1 = this.getStowingAMService().getStowingGenQryVO1().getCurrentRow();
        ro1.setAttribute("LocationId", r.getAttribute("LocationId"));
        errLs = this.getStowingAMService().updateStowLocationOnly(userId);
      }
      else {
        if ((this.getEnterQtyStowed() != null) && (this.getEnterQtyStowed().getValue() != null))
          stowQtyI = Integer.parseInt(this.getEnterQtyStowed().getValue().toString());
        errLs = this.getStowingAMService().updateStowLocation(r.getAttribute("LocationId"), userId, userSelectedIndex2, stowQtyI);
      }
      this.showStowErrors(errLs);
      if (errLs.length() <= 0) {
        Row s;
        if (Long.parseLong(JSFUtils.getManagedBeanValue("userbean.selected2Index").toString()) <= 0)
          s = this.getStowingAMService().getStowingGenQryVO1().getCurrentRow();
        else
          s = this.getStowingAMService().getStowLocVO1().getCurrentRow();
        // never need to print PICK label when done stowing and we print stow label when changing the location
        // call with SID, Qty of labels, darkness (-30 to 30)
        String prnStr = this.getWorkloadManagerService().generatePrintSIDLabel(s.getAttribute("Sid").toString(), 0, 0);
        if ((prnStr != null) && (prnStr.length() > 0)) {
          val labelPrintUtil = ContextUtils.getBean(LabelPrintUtil.class);
          UserInfo userInfo = (UserInfo) JSFUtils.getManagedBeanValue("userbean");
          labelPrintUtil.printLabel(userInfo, prnStr, new String[] { s.getAttribute("Sid").toString() }, LabelType.STOW_LABEL);
        }
        this.getWorkloadManagerService().clearLocationList();

        this.setScanLocLabel(null); // @ZSL  4/2018 ticket #65004
        if (userSelectedIndex2 <= 0) {
          this.setEnterNiinLastTwoDigits(null);
          this.setEnterQtyStowed(null);
          if (getStowingAMService().getStowingGenQryVO1().hasNext()) {
            actionForwardStr = "GoStowingNormalLocation";
            getStowingAMService().getStowingGenQryVO1().next();
            this.performCarouselMove(0); //Move for the same side at end of a pick
          }
          else {
            getStowingAMService().getStowingGenQryVO1().clearCache();
            actionForwardStr = "GoStowingHome";
          }
        }
        else {
          this.getStowingAMService().getStowLocVO1().clearCache();
          this.getStowingAMService().getStowLocVO1().executeQuery();
          actionForwardStr = "GoStowingReassignSID";
        }
      }
      else
        actionForwardStr = "";
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  public void showStowErrors(String errLs) {
    //Used to display the error messages related to STOW
    try {
      boolean flag = false;
      if (errLs.length() > 0) {
        if (errLs.contains("STOW_LOC_INV")) {
          JSFUtils.addFacesErrorMessage("STOW_LOC_INV", "Stow location missing.");
          flag = true;
        }
        if (errLs.contains("STOW_ERR_CCEXPDIFF")) {
          flag = true;
          JSFUtils.addFacesErrorMessage("STOW_CCEXPDIFF_ERR", "Location at which you are trying to stow has a NIIN with different CC and/or Expiration date. Please ByPass and go to ReAssign SID to pick a different location.");
        }
        if (errLs.contains("STOW_ERR_NOROW")) {
          flag = true;
          JSFUtils.addFacesErrorMessage("STOW_ERR_NOROW", "SID does not exist.");
        }
        if (errLs.contains("STOW_ERR_STOWED")) {
          flag = true;
          JSFUtils.addFacesErrorMessage("STOW_ERR_STOWED", "SID already stowed.");
        }
        if (errLs.contains("STOW_ERR_BYPASS")) {
          flag = true;
          JSFUtils.addFacesErrorMessage("STOW_ERR_BYPASS", "SID in Exception Processing.");
        }
        if (errLs.contains("STOW_ERR_LOCKEDORAUDIT")) {
          flag = true;
          JSFUtils.addFacesErrorMessage("STOW_ERR_LOCKEDORAUDIT", "NIIN_LOCATION is either locked or under audit.");
        }
        if (errLs.contains("STOW_ERR_OTHERS") && errLs.contains("Weight/Cube limit exceeded")) {
          flag = true;
          JSFUtils.addFacesErrorMessage("STOW_ERR_WGTCUB", "Cube/Weight limit exceeded for selected location. Click 'ByPass' (if needed) or re-locate stow using 'Re-Locate Stow' button.");
        }
        if (errLs.contains("STOW_ERR_OTHERS") && (!errLs.contains("Weight/Cube limit exceeded"))) {
          flag = true;
          JSFUtils.addFacesErrorMessage("STOW_ERR_OTHERS", errLs);
        }
        if (errLs.contains("STOW_ERR_TRAN")) {
          flag = true;
          JSFUtils.addFacesErrorMessage("STOW_ERR_TRAN", "Error occured trying to create stow transactions.");
        }
        if (errLs.contains("STOW_ERR_LOCATION")) {
          flag = true;
          JSFUtils.addFacesErrorMessage("STOW_ERR_LOCATION", "Some other NIIN exists at this location." + "Multiple NIIN stowing is not allowed in Mechanized unless all mechanized locations are full." + " Please do ByPass for now and do reassigning of location by going to ReAssign menu tab.");
        }
        if (errLs.contains("STOW_ERR_CUBEWGT")) {
          flag = true;
          JSFUtils.addFacesErrorMessage("STOW_ERR_CUBEWGT", "Cube/Weight limit exceeded for selected location. Click 'ByPass' (if needed) or re-locate stow using 'Re-Locate Stow' button.");
        }
        if (errLs.contains("DUPLICATE_STW")) {
          flag = true;
          JSFUtils.addFacesErrorMessage("Duplicate STW transaction found.");
        }
        if (!flag)
          JSFUtils.addFacesErrorMessage("STOW_ERR_OTHERS", errLs);
      }
      else
        this.setExpObj(0);
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  public void getScannedSIDDetails(@SuppressWarnings("all") ActionEvent actionEvent) {
    try {
      String sidStr = "x";
      if (this.getScanLocLabel().getLocalValue() != null)
        sidStr = this.getScanLocLabel().getLocalValue().toString().toUpperCase();
      String errLs = this.getStowingAMService().getScannedSIDDetails(sidStr);
      this.showStowErrors(errLs);
      this.getWorkloadManagerService().clearLocationList();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  @SuppressWarnings("unused")  //called from .jspx
  public void BuildLocationList2(@SuppressWarnings("all") ActionEvent actionEvent) {
    try {
      Row r = this.getStowingAMService().getStowLocVO1().getCurrentRow();
      if (r == null)
        return;
      String stArea = "";
      String wareHouseId = "ALL";
      if (this.getStowArea().getLocalValue() != null)
        stArea = this.getStowArea().getLocalValue().toString();
      if (!Boolean.parseBoolean(JSFUtils.getManagedBeanValue("userbean.adminLevelUser").toString()))
        wareHouseId = this.getWorkloadManagerService().getWareHouseId(stArea, Integer.parseInt(JSFUtils.getManagedBeanValue("userbean.workstationId").toString()));
      if (wareHouseId.contains("NONE")) {
        JSFUtils.addFacesErrorMessage("STOW_AREA_ERR", "Sorry only admin users are allowed to assign locations outside of this warehouse.");
      }
      else {
        String errStr1;
        errStr1 = this.getWorkloadManagerService().buildLocationList(stArea, "STOW_ID", r.getAttribute("StowId").toString(), wareHouseId, -1);
        this.getStowingAMService().getStowLocVO1().clearCache();
        this.getStowingAMService().getStowLocVO1().executeQuery();
        if (errStr1 != null && errStr1.length() > 0)
          JSFUtils.addFacesErrorMessage("ERR", errStr1);
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  public String getActionForward() {
    // Get Eid
    return actionForwardStr;
  }

  public void setAvailQty(RichInputText availQty) {
    this.availQty = availQty;
  }

  public RichInputText getAvailQty() {
    return availQty;
  }

  public void setAvailQtyR(RichInputText availQtyR) {
    this.availQtyR = availQtyR;
  }

  public RichInputText getAvailQtyR() {
    return availQtyR;
  }

  public void assignExpObjForByPass(@SuppressWarnings("all") ActionEvent actionEvent) {
    // Set the switch for By Pass if it is more then one by pass
    try {
      Row ro = this.getStowingAMService().getStowingGenQryVO1().getCurrentRow();
      if ((ro.getAttribute("BypassCount") == null) || (Integer.parseInt(ro.getAttribute("BypassCount").toString()) <= 0))
        this.getStowingAMService().stowRowByPass("STOW BYPASS", 1, ro.getAttribute("Sid"), Integer.parseInt(JSFUtils.getManagedBeanValue("userbean.userId").toString()));
      else {
        setExpObj(6);
      }
      this.setExpObj(0);
      this.setEnterNiinLastTwoDigits(null);
      this.setEnterQtyStowed(null);
      this.setScanLocLabel(null);
      this.setAvailQtyR(null);
      this.setAvailQty(null);
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  @SuppressWarnings("unused")  //called from .jspx
  public String actionForwardStrForStowingNormalNiinVerOnByPassDone() {
    try {
      Row ro = getStowingAMService().getStowingGenQryVO1().getCurrentRow();
      String sid = ro.getAttribute("Sid").toString();
      //Update the stow row with the bypass info
      val userId = JSFUtils.getManagedBeanValue("userbean.userId").toString();
      int byPassCt = getStowingAMService().stowRowByPass("STOW BYPASS", 2, ro.getAttribute("Sid"), Integer.parseInt(userId));
      ro.refresh(Row.REFRESH_CONTAINEES);
      // create the exception or error queue record

      if (byPassCt > 1) {
        BindingContainer bindings = BindingContext.getCurrent().getCurrentBindingsEntry();
        JUCtrlListBinding listBinding = (JUCtrlListBinding) bindings.get("StowByPassList");
        ViewRowImpl selectedRow = (ViewRowImpl) listBinding.getSelectedValue();
        String eid = selectedRow.getAttribute("Eid").toString();  //gets selected error id
        this.getWorkloadManagerService().createErrorQueueRecord(eid, "SID", sid, userId, null);
      }
      if (getStowingAMService().getStowingGenQryVO1().hasNext()) {
        actionForwardStr = "GoStowingNormalLocation";
        getStowingAMService().getStowingGenQryVO1().next();
        this.performCarouselMove(0); //Move for the same side at end of a pick
      }
      else {
        getStowingAMService().getStowingGenQryVO1().clearCache();
        actionForwardStr = "GoStowingHome";
      }
      errStr = "";
      this.setExpObj(0);
      this.setEnterNiinLastTwoDigits(null);
      this.setEnterQtyStowed(null);
      return actionForwardStr;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return null;
  }

  public void setUiDialog(boolean uiDialog) {
    this.uiDialog = uiDialog;
  }

  public boolean isUiDialog() {
    return uiDialog;
  }

  public void setTextForDialog(String textForDialog) {
    this.textForDialog = textForDialog;
  }

  public String getTextForDialog() {
    return textForDialog;
  }

  public void setAvailQtyR1(ValueChangeEvent valueChangeEvent) {
    this.getAvailQtyR().setValue(valueChangeEvent.getNewValue());
  }

  public void handleUIDialogLaunch(LaunchEvent launchEvent) {
    // Used for dialog launch with a message
    //Sets the paramter that is passed on for display on the dialog window
    try {
      launchEvent.getDialogParameters().put("dialogTxt", this.getTextForDialog());
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  public void setDialogParameters(String actionStr, boolean flag) {
    //Set the dialog window parameters
    actionForwardStr = actionStr;
    uiDialog = flag;
  }

  //(FUTURE) appears to not be used, but need to verify.  .jspx's reference this method from other backing
  //classes.  just want to be sure before removing
  @SuppressWarnings({"unused", "java:S100"})
  public void UIDialogDone(ActionEvent actionEvent) {
    //Close the dialog
    try {
      AdfFacesContext.getCurrentInstance().returnFromDialog(null, null);
      this.setDialogParameters("", false);
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  public void performCarouselMove(int flag) {
    try {
      if (this.getWorkloadManagerService().isMechanized(getStowingAMService().getStowingGenQryVO1().getCurrentRow().getAttribute("WacId").toString())) {
        //Call the carousel move
        //flag == 1 means that it is comming from cancel or bypass or refuse so goal is to just move on

        String mvCmdStr = "";
        String mvCmdStrFirst;
        mvCmdStrFirst = this.getWorkloadManagerService().performCarouselMove(getStowingAMService().getStowingGenQryVO1().getCurrentRow());
        if ((flag == 1) || (actionForwardStr.equalsIgnoreCase("GoStowingNormalLocation"))) {
          //Make the move based on the new stow which is going to start from location screen
          String preSide = getStowingAMService().getStowingGenQryVO1().getCurrentRow().getAttribute("Side").toString();
          String curSide = getStowingAMService().getStowingGenQryVO1().getCurrentRow().getAttribute("Side").toString();
          if (getStowingAMService().getStowingGenQryVO1().hasPrevious()) {
            Object preObj = getStowingAMService().getStowingGenQryVO1().previous().getAttribute("Side");
            if (preObj != null) {
              preSide = preObj.toString();
            }
            getStowingAMService().getStowingGenQryVO1().next();
          }
          if ((flag == 1) || (curSide.equalsIgnoreCase(preSide))) //Case were we had to wait till end to move to new
            mvCmdStr = this.getWorkloadManagerService().performCarouselMove(getStowingAMService().getStowingGenQryVO1().getCurrentRow());
        }
        else if ((actionForwardStr.equalsIgnoreCase("GoStowingNormalNiinVer")) && getStowingAMService().getStowingGenQryVO1().hasNext()) {
          //It it has next stow so get the side of that next stow
          String curSide = getStowingAMService().getStowingGenQryVO1().getCurrentRow().getAttribute("Side").toString();
          String nextSide = getStowingAMService().getStowingGenQryVO1().next().getAttribute("Side").toString();
          if (!curSide.equalsIgnoreCase(nextSide)) { //looks like the side are opposite
            //Get move command
            mvCmdStr = this.getWorkloadManagerService().performCarouselMove(getStowingAMService().getStowingGenQryVO1().getCurrentRow());
          }
          //Reset the pointer to previous stow becuase we moved it in the previous statement
          getStowingAMService().getStowingGenQryVO1().previous();
        }
        //Activate this when ready for COM
        if (mvCmdStr.length() > 0) {
          mvCmdStr = mvCmdStrFirst + mvCmdStr;
          // turn the comm on
          JSFUtils.setManagedBeanValue("userbean.useprintcom", 1);
          // set the id of which comm to use from the com_port table
          JSFUtils.setManagedBeanValue("userbean.printcomport", JSFUtils.getManagedBeanValue("userbean.comCommandIndex"));
          // set the string to go out
          JSFUtils.setManagedBeanValue("userbean.printcomstring", mvCmdStr);
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  public void performCarouselMoveOld(int flag) {
    try {
      //Call the carousel move
      //flag == 1 means that it is comming from cancel or bypass or refuse so goal is to just move on
      String mvCmdStr = "";
      if ((flag == 1) || (actionForwardStr.equalsIgnoreCase("GoStowingNormalLocation"))) {
        //Make the move based on the new stow which is going to start from location screen
        String preSide = getStowingAMService().getStowingGenQryVO1().getCurrentRow().getAttribute("Side").toString();
        String curSide = getStowingAMService().getStowingGenQryVO1().getCurrentRow().getAttribute("Side").toString();
        if (getStowingAMService().getStowingGenQryVO1().hasPrevious()) {
          preSide = getStowingAMService().getStowingGenQryVO1().previous().getAttribute("Side").toString();
          getStowingAMService().getStowingGenQryVO1().next();
        }
        if ((flag == 1) || (curSide.equalsIgnoreCase(preSide))) //Case were we had to wait till end to move to new
          mvCmdStr = this.getWorkloadManagerService().performCarouselMove(getStowingAMService().getStowingGenQryVO1().getCurrentRow());
      }
      else if ((actionForwardStr.equalsIgnoreCase("GoStowingNormalNiinVer")) && getStowingAMService().getStowingGenQryVO1().hasNext()) {
        //It it has next stow so get the side of that next stow
        String curSide = getStowingAMService().getStowingGenQryVO1().getCurrentRow().getAttribute("Side").toString();
        String nextSide = getStowingAMService().getStowingGenQryVO1().next().getAttribute("Side").toString();
        if (!curSide.equalsIgnoreCase(nextSide)) { //looks like the side are opposite
          //Get move command
          mvCmdStr = this.getWorkloadManagerService().performCarouselMove(getStowingAMService().getStowingGenQryVO1().getCurrentRow());
        }
        //Reset the pointer to previous stow becuase we moved it in the previous statement
        getStowingAMService().getStowingGenQryVO1().previous();
      }
      //Activate this when ready for COM
      if (mvCmdStr.length() > 0) {
        // turn the comm on
        JSFUtils.setManagedBeanValue("userbean.useprintcom", 1);
        // set the id of which comm to use from the com_port table
        JSFUtils.setManagedBeanValue("userbean.printcomport", JSFUtils.getManagedBeanValue("userbean.comCommandIndex"));
        // set the string to go out
        JSFUtils.setManagedBeanValue("userbean.printcomstring", mvCmdStr);
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  public void lookupNiins(@SuppressWarnings("all") ActionEvent actionEvent) {
    try {
      Row ro = getStowingAMService().getStowingGenQryVO1().getCurrentRow();
      if ((ro != null) && (ro.getAttribute("NiinId") != null)) {
        //Call the application module to retrieve the Niin information based on the scanned barcode
        //for the popup window
        this.getWorkloadManagerService().getNiinInfo(ro.getAttribute("NiinId").toString());
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  @SuppressWarnings("unused")  //(FUTURE) appears to not be used, but need to verify.  .jspx's reference this method from other backing
  //classes.  just want to be sure before removing
  public void niinOk(@SuppressWarnings("all") ActionEvent actionEvent) {
    //Close the dialog window that displays the Niin information based on the
    //location label
    AdfFacesContext.getCurrentInstance().returnFromDialog(null, null);
  }

  public void setReLocateStr(String reLocateStr) {
    this.reLocateStr = reLocateStr;
  }

  public String getReLocateStr() {
    return reLocateStr;
  }

  public void setScanSIDLabel(RichInputText scanSIDLabel) {
    this.scanSIDLabel = scanSIDLabel;
  }

  public RichInputText getScanSIDLabel() {
    return scanSIDLabel;
  }
}
