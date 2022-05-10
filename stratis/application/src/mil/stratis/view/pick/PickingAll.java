package mil.stratis.view.pick;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.common.util.Util;
import mil.stratis.model.view.pick.PickingGenQryVOImpl;
import mil.stratis.model.view.pick.SerialOrLotNumListImpl;
import mil.stratis.view.session.MdssBackingBean;
import mil.stratis.view.user.UserInfo;
import mil.stratis.view.util.ADFUtils;
import mil.stratis.view.util.JSFUtils;
import mil.usmc.mls2.stratis.common.domain.model.LabelType;
import mil.usmc.mls2.stratis.core.utility.AdfLogUtility;
import mil.usmc.mls2.stratis.core.utility.ContextUtils;
import mil.usmc.mls2.stratis.core.utility.LabelPrintUtil;
import mil.usmc.mls2.stratis.core.utility.LocationSortBuilder;
import oracle.adf.model.BindingContext;
import oracle.adf.view.rich.component.rich.data.RichTable;
import oracle.adf.view.rich.component.rich.input.RichInputText;
import oracle.adf.view.rich.component.rich.input.RichSelectOneChoice;
import oracle.adf.view.rich.context.AdfFacesContext;
import oracle.binding.BindingContainer;
import oracle.jbo.Row;
import oracle.jbo.ViewObject;
import oracle.jbo.uicli.binding.JUCtrlListBinding;
import oracle.jbo.uicli.binding.JUCtrlValueBindingRef;
import org.apache.myfaces.trinidad.event.LaunchEvent;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import java.util.ArrayList;
import java.util.Iterator;

@Slf4j
@NoArgsConstructor
public class PickingAll extends MdssBackingBean {

  String actionForwardStr = "";
  String errStr = "error";
  private boolean uiDialog = false;
  String eidStr = "";
  Object expObj = 0;
  Object expObjBkp = 0;
  Object srlChoice = 0;
  private RichInputText scanLocLabel;
  private RichInputText enterNiinLastTwoDigits;
  private RichInputText enterQtyPicked;
  String pramStr1 = "";
  private RichTable customerList;
  long selInd2 = 0;
  private RichTable routeList;
  private RichTable priorityList;
  private RichTable walkThruList;
  public RichInputText availQty;
  public RichInputText availQtyR;
  private String textForDialog = "";
  private RichInputText srlOrLotConNum;
  private RichInputText startSerial, endSerial;
  private RichSelectOneChoice bypassList;
  
  // listener for each time the page is loaded

  public void onPageLoad() {
    //If is not postback
    //
    if (!ADFUtils.isPostback()) {
      this.pramStr1 = "";
      init();
    }

    this.setExpObj(0);
    this.errStr = "error";
    this.uiDialog = false;
    this.pramStr1 = "";

    if (this.customerList != null) {
      this.customerList.setValue(null);
    }

    if (this.enterNiinLastTwoDigits != null) {
      this.enterNiinLastTwoDigits.setValue(null);
    }
    if (this.enterQtyPicked != null) {
      this.enterQtyPicked.setValue(null);
    }
    if (this.availQty != null) {
      this.availQty.setValue(null);
    }
    if (this.availQtyR != null) {
      this.availQtyR.setValue(null);
    }
  }

  // initial page loading function

  public void init() {
    this.selInd2 = Long.parseLong(JSFUtils.getManagedBeanValue("userbean.selected2Index").toString());
    log.trace("=== ZLIM === selInd2={},  expObj= {}", this.selInd2, this.getExpObj());
    log.trace("=== ZLIM === userId={}", JSFUtils.getManagedBeanValue("userbean.userId"));
    getPickingAMService().buildCustomerRS(Long.parseLong(JSFUtils.getManagedBeanValue("userbean.selected2Index").toString()), Long.parseLong(JSFUtils.getManagedBeanValue("userbean.workstationId").toString()), JSFUtils.getManagedBeanValue("userbean.userId").toString(), this.getPramStr1(), "");
    log.trace("=== ZLIM ===  buildCustomerRS() done");
    getPickingAMService().buildRouteRS(Long.parseLong(JSFUtils.getManagedBeanValue("userbean.selected2Index").toString()), Long.parseLong(JSFUtils.getManagedBeanValue("userbean.workstationId").toString()), JSFUtils.getManagedBeanValue("userbean.userId").toString(), this.getPramStr1(), "");
    getPickingAMService().buildPriorityRS(Long.parseLong(JSFUtils.getManagedBeanValue("userbean.selected2Index").toString()), Long.parseLong(JSFUtils.getManagedBeanValue("userbean.workstationId").toString()), JSFUtils.getManagedBeanValue("userbean.userId").toString(), this.getPramStr1(), "");
    getPickingAMService().buildWalkThruRS(Long.parseLong(JSFUtils.getManagedBeanValue("userbean.selected2Index").toString()), Long.parseLong(JSFUtils.getManagedBeanValue("userbean.workstationId").toString()), JSFUtils.getManagedBeanValue("userbean.userId").toString(), this.getPramStr1(), "");

    if (selInd2 == 0) {
      val sortBuilder = ContextUtils.getBean(LocationSortBuilder.class);
      val sortOrder = sortBuilder.getSortString(LocationSortBuilder.QueryType.ADF);
      getPickingAMService().buildPickRS(Long.parseLong(JSFUtils.getManagedBeanValue("userbean.selected2Index").toString()), Long.parseLong(JSFUtils.getManagedBeanValue("userbean.workstationId").toString()), JSFUtils.getManagedBeanValue("userbean.userId").toString(), this.getPramStr1(), "", sortOrder);
    }
    errStr = "";
    if (expObj != null) {
      this.setExpObj(0);
    }
    if (enterNiinLastTwoDigits != null) {
      this.setEnterNiinLastTwoDigits(null);
    }

    if (enterQtyPicked != null) {
      this.setEnterQtyPicked(null);
    }

    if (srlOrLotConNum != null) {
      this.getSrlOrLotConNum().setValue("");
    }

    if (startSerial != null) {
      this.getSrlOrLotConNum().setValue("");
    }

    if (endSerial != null) {
      this.getSrlOrLotConNum().setValue("");
    }

    if (srlChoice != null) {
      this.setSrlChoice(0);
    }

    if (this.scanLocLabel != null) {
      this.scanLocLabel.setValue(null);
    }
  }

  public String actionForwardStrForPickingNormalOnCancel() {
    //Checks if there are any picks and
    //takes you to next pick if any exist. Other comes out of Pick Normal
    JSFUtils.setManagedBeanValue("userbean.specialMessage", ""); //Reset informational message
    try {
      Row ro = getPickingAMService().getPickingGenQryVO1().getCurrentRow();
      if ((ro.getAttribute("BypassCount") == null) || (Integer.parseInt(ro.getAttribute("BypassCount").toString()) <= 0)) {
        this.getPickingAMService().skipPicksForLocationOrStatusChange(); //skip picks if location or status changed
        if (getPickingAMService().getPickingGenQryVO1().hasNext()) {
          getPickingAMService().getPickingGenQryVO1().next();
          this.setExpObj(0);
          actionForwardStr = "";
          performCarouselMove(1);
        }
        else {
          int uid = Integer.parseInt(JSFUtils.getManagedBeanValue("userbean.userId").toString());
          getPickingAMService().getPickingGenQryVO1().clearCache();
          getPickingAMService().clearAssignedUserInPicking(uid);
          this.setExpObj(0);
          actionForwardStr = "GoPickingHome";
        }
      }
      else {
        actionForwardStr = "";
        this.setExpObj(2);
      }
      errStr = "";
      return actionForwardStr;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return null;
  }

  public String actionForwardStrForPickingNormalOnSubmit() {
    //Checks if there are any errors. If no errors forwards you to
    //NIIN verification else remains in same window for fixing.
    try {
      errStr = "";
      JSFUtils.setManagedBeanValue("userbean.specialMessage", ""); //Reset informational message
      Object lL = this.getScanLocLabel().getValue();
      Row ro = getPickingAMService().getPickingGenQryVO1().getCurrentRow();
      if (ro.getAttribute("Locked").toString().equalsIgnoreCase("Y")) {
        errStr = errStr + "LOC_LOCKED";
        JSFUtils.addFacesErrorMessage("LOC_LOCKED", "Location locked - ByPass.");
      }
      if ((lL == null) || (lL.toString().length() <= 8)) {
        errStr = errStr + "LOC_ERR_SZ";
        JSFUtils.addFacesErrorMessage("LOC_ERR_SZ", "Invalid location label size.");
      }

      if ((lL != null) && !lL.toString().equalsIgnoreCase(ro.getAttribute("LocationLabel").toString())) {
        errStr = errStr + "LOC_ERR_MATCH";
        JSFUtils.addFacesErrorMessage("LOC_ERR_MATCH", "Location entry mismatch. Re-enter location or bypass pick.");
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
            ro.setAttribute("NsnRemark", "N");
            String FSC = "";
            if (ro.getAttribute("Fsc") != null) {
              FSC = ro.getAttribute("Fsc").toString();
            }
            if (!FSC.isEmpty()) {
              msgStr = "Remark FSC on Location Inventory to " + FSC;
            }
            else {
              msgStr = "Remark FSC on Location Inventory";
            }
            JSFUtils.addFacesInformationMessage(msgStr);
          }
          if (rStr.contains("NEED_REPACK")) {
            JSFUtils.addFacesInformationMessage(getWorkloadManagerService().buildDialogTxt(rStr, "Repack location inventory from :2 :1 to :4 :3"));
          }

          actionForwardStr = "GoPickingNormalNiinVer";

          this.performCarouselMove(0);
          errStr = "";
          this.scanLocLabel.setValue(null);
          return actionForwardStr;
        }
        else
          //Picking going through normal process
          actionForwardStr = "GoPickingNormalNiinVer";
        this.performCarouselMove(0);
      }
      else
        actionForwardStr = "";
      errStr = "";
      this.scanLocLabel.setValue(null);

      return actionForwardStr;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return null;
  }

  public String actionForwardStrForPickingNormalNiinVerOnRefuse() {
    //Refreshes screen to enter the Refuse reason on current screen.
    try {
      Row ro = getPickingAMService().getPickingGenQryVO1().getCurrentRow();
      if (ro.getAttribute("IssueType") == null || !ro.getAttribute("IssueType").toString().equalsIgnoreCase("R")) {
        this.setExpObj(1);
        errStr = "";
        actionForwardStr = "GoPickingNormalNiinVer";
      }
      else {
        errStr = "PICK_REFUSE_ERR";
        JSFUtils.addFacesErrorMessage(errStr, "Cannot Refuse a Rewarehouse pick.");
        this.setExpObj(0);
        actionForwardStr = "";
      }
      return actionForwardStr;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return null;
  }

  public String actionForwardStrForPickingNormalNiinVerOnLocationEmpty() {
    // Performs usual save and calls functions to create error queue records.
    //Calls work load manger to build the transactions. And then forwards to next
    //Pick if ay exist other wise exist Pick.
    try {
      this.getPickingAMService().skipPicksForLocationOrStatusChange();
      goToNextPick();
      errStr = "";
      return actionForwardStr;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return null;
  }

  public String actionForwardStrForPickingNormalNiinVerOnSubmit() {
    //Calls validations via value change listeners.
    //Logic check and launch for Inventory check, and
    //final save and forward to Next Pick
    try {
      int uid = Integer.parseInt(JSFUtils.getManagedBeanValue("userbean.userId").toString());
      Row ro = getPickingAMService().getPickingGenQryVO1().getCurrentRow();
      Object lL = this.getEnterNiinLastTwoDigits().getValue();
      if ((lL == null) || (lL.toString().isEmpty())) {
        errStr = "NIIN_ERR";
        JSFUtils.addFacesErrorMessage("NIIN_ERR", "Last two digits of NIIN required.");
      }
      Object lQ = getEnterQtyPicked().getValue();
      if (lQ == null || lQ.toString().trim().isEmpty()) {
        errStr = errStr + "QTY_ERR";
        JSFUtils.addFacesErrorMessage("QTY_ERR", "Quantity picked is required.");
      }
      else {
        if (!isNumericAA(lQ.toString().trim())) {
          errStr = errStr + "QTY_ERR";
          JSFUtils.addFacesErrorMessage("QTY_ERR", "Quantity picked must be a whole number.");
        }
      }
      if ((lQ != null) && (errStr.length() <= 0) && ((ro.getAttribute("IssueType") != null) && (ro.getAttribute("IssueType").toString().equalsIgnoreCase("R")))) //For RE-WAREHOUSING
      {
        if (Integer.parseInt(ro.getAttribute("PickQty").toString()) != (Integer.parseInt(ro.getAttribute("QtyPicked").toString()) + Integer.parseInt(lQ.toString()))) {
          errStr = errStr + "REWAREHOUSE_QTY_ERR";
          JSFUtils.addFacesErrorMessage("REWAREHOUSE_QTY_ERR", "Bypass if quantity picked does not match.");
          this.setExpObj(0);
          return "";
        }
      }
      //Make sure we are done with the serial number process
      if ((lQ != null) && (errStr.length() <= 0) && ((this.getSrlChoice().toString().equals("1")) && (this.getPickingAMService().getSerialOrLotNumList1().getRowCount() > Integer.parseInt(lQ.toString())))) {
        errStr = errStr + "SRL_ERR";
        JSFUtils.addFacesErrorMessage("SRL_ERR", "There are more Serial/Lot numbers then quantity.");
        return "";
      }
      if ((lQ != null) && ((errStr.length() <= 0) && (this.getExpObj().toString().equalsIgnoreCase("0"))) && ((ro.getAttribute("IssueType") == null) || (!ro.getAttribute("IssueType").toString().equalsIgnoreCase("R")))) //RE-WAREHOUSING code
      {
        String packStation = "";
        val lqInt = Integer.parseInt(lQ.toString());
        if (Integer.parseInt(ro.getAttribute("PickQty").toString()) > (Integer.parseInt(ro.getAttribute("QtyPicked").toString()) + lqInt)) {
          //Get a differet packing station for partial
          packStation = this.getWorkloadManagerService().getPackingStation(Integer.parseInt(ro.getAttribute("Pid").toString()), Integer.parseInt(JSFUtils.getManagedBeanValue("userbean.userId").toString()), lqInt, 1);
        }
        else {
          if (ro.getAttribute("PackingConsolidationId") != null)
            packStation = this.getWorkloadManagerService().getAssignedPackingStationName(ro.getAttribute("PackingConsolidationId").toString());
        }
        if (((packStation == null) || (packStation.length() <= 0)) || (packStation.toUpperCase().contains("ERROR"))) {
          errStr = errStr + "PACK_ERR";
          if (packStation.toUpperCase().contains("ERROR"))
            JSFUtils.addFacesErrorMessage("PACK_ERR_MSG", packStation);
          else
            JSFUtils.addFacesErrorMessage("PACK_ERR_MSG", "Could not get packing station.");
        }
        else {
          //Look on Pick Page to add the special message
          JSFUtils.setManagedBeanValue("userbean.specialMessage", "Information: Send " + ro.getAttribute("Pin") + " to " + packStation);
        }
      }
      else if ((ro.getAttribute("IssueType") != null && ro.getAttribute("IssueType").toString().equalsIgnoreCase("R")) && (ro.getAttribute("Sid") == null)) {
        errStr = errStr + "RWH_ERR_MSG";
        JSFUtils.addFacesErrorMessage("RWH_ERR_MSG", "This pick is rewarehouse and has no STOW created.");
      }
      if (lQ != null) {
        this.validateSrl(lQ.toString());
      }

      if ((lQ != null) && (errStr.length() <= 0) && (this.getExpObj().toString().equalsIgnoreCase("0"))) { //Update the database pick qty and status
        this.getPickingAMService().pickRowStatusChange(Long.parseLong(ro.getAttribute("Pid").toString()), Integer.parseInt(JSFUtils.getManagedBeanValue("userbean.userId").toString()), Integer.parseInt(ro.getAttribute("PickQty").toString()), Integer.parseInt(ro.getAttribute("QtyPicked").toString()), Integer.parseInt(lQ.toString()), Long.parseLong(ro.getAttribute("NiinLocId").toString()));
        this.getPickingAMService().changeWalkThruStatus(ro.getAttribute("DocumentNumber").toString(), Integer.parseInt(JSFUtils.getManagedBeanValue("userbean.userId").toString()));
        getWalkThruAMService().refreshWalkThruTable();
        getWalkThruAMService().setFromPickbyWalkThru(true);
        if ((ro.getAttribute("IssueType") == null) || (!Util.cleanString(ro.getAttribute("IssueType")).equals("R") && !Util.cleanString(ro.getAttribute("IssueType")).equals("W"))) {
          //* send SRO transaction - added 02-13-09
          this.getGCSSMCAMService().sendSROGCSSMCTransaction(ro.getAttribute("Pid").toString());
        }

        //Calling to build the Zebra print string with PIN, Qty of labels, Darkness -30 to 30
        String prnStr = "";
        if ((ro.getAttribute("IssueType") != null) && (ro.getAttribute("IssueType").toString().equalsIgnoreCase("R"))) { //For RE-WAREHOUSING
          prnStr = this.getWorkloadManagerService().generatePrintAllSIDLabelUsingRcn(ro.getAttribute("Sid").toString(), 0, 0, false);
          String[] sids = this.getWorkloadManagerService().getAllSIDLabelUsingRcn(ro.getAttribute("Sid").toString());
          printSidLabel(prnStr, sids);
          String wac = getWorkloadManagerService().getWACforSID(ro.getAttribute("Sid").toString());
          if (!wac.isEmpty()) {
            JSFUtils.setManagedBeanValue("userbean.specialMessage", "Information: Send " + ro.getAttribute("Sid") + " to " + wac);
          }
        }
        else {
          // call with SID, Qty of labels, darkness (-30 to 30)
          prnStr = this.getWorkloadManagerService().generatePrintPINLabel(ro.getAttribute("Pin").toString(), 0, 0);
          printLabel(prnStr, ro.getAttribute("Pin").toString());
        }

        // Close out any Error queue records for this pick
        this.getWorkloadManagerService().updateErrorQueueRecordStatus("PID", ro.getAttribute("Pid").toString(), uid);
        this.getPickingAMService().skipPicksForLocationOrStatusChange();
        goToNextPick();
        if (this.getPickingAMService().getSerialOrLotNumList1() != null)
          this.getPickingAMService().getSerialOrLotNumList1().clearCache();
      }
      else {
        actionForwardStr = "";
      }
      errStr = "";
      this.setEnterNiinLastTwoDigits(null);
      this.setEnterQtyPicked(null);
      this.getSrlOrLotConNum().setValue("");
      return actionForwardStr;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return "";
  }

  private void printLabel(String prnStr, String pin) {
    if ((prnStr != null) && (prnStr.length() > 0)) {
      val labelPrintUtil = ContextUtils.getBean(LabelPrintUtil.class);
      UserInfo userInfo = (UserInfo) JSFUtils.getManagedBeanValue("userbean");
      labelPrintUtil.printLabel(userInfo, prnStr, new String[]{pin}, LabelType.PICK_LABEL);
    }
  }


  private void printSidLabel(String prnStr, String[] sids) {
    if ((prnStr != null) && (prnStr.length() > 0)) {
      val labelPrintUtil = ContextUtils.getBean(LabelPrintUtil.class);
      UserInfo userInfo = (UserInfo) JSFUtils.getManagedBeanValue("userbean");
      labelPrintUtil.printLabel(userInfo, prnStr, sids, LabelType.STOW_LABEL);
    }
  }

  public void locationValidator(FacesContext facesContext,
                                UIComponent uiComponent, Object object) {
    try {

      String lL = getPickingAMService().getPickingGenQryVO1().getCurrentRow().getAttribute("LocationLabel").toString();
      String sL = "";
      boolean f = true;
      if (uiComponent.getId().compareTo("pickNormalCancelId") != 0) {
        if (object != null) {
          sL = object.toString();
        }
        else {
          f = false;
        }
        if (f) {
          if ((sL.length() <= 0) || (sL.toUpperCase().trim().compareTo(lL.trim()) != 0))
            f = false;
        }
      }
      if (!f) {
        errStr = errStr + "LOC_ERR";
        JSFUtils.addFacesErrorMessage("LOC_ERR", "Scanned Location does not match STRATIS provided Location. Please make sure you are at right location.");
      }
      else {
        errStr = "";
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  public void niinLastTwoValidation(FacesContext facesContext,
                                    UIComponent uiComponent,
                                    Object object) {
    try {
      String lL = getPickingAMService().getPickingGenQryVO1().getCurrentRow().getAttribute("Niin").toString();
      lL = lL.substring(lL.length() - 2);
      String sL = "";
      boolean f = true;
      if (object != null) {
        sL = object.toString();
      }
      else
        f = false;
      if (f) {
        if ((sL.length() <= 0) || (sL.toUpperCase().trim().compareTo(lL.trim()) != 0))
          f = false;
      }
      if (!f) {
        if (errStr.contains("error"))
          errStr = "";
        errStr = "NIIN_ERR";
        JSFUtils.addFacesErrorMessage("NIIN_ERR", "Last two digits of NIIN mismatch. Verify and re-enter digits.");
      }
      else {
        errStr = "";
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  public void pickQtyValidation(FacesContext facesContext,
                                UIComponent uiComponent, Object object) {
    try {
      int pQty = Integer.parseInt(getPickingAMService().getPickingGenQryVO1().getCurrentRow().getAttribute("PickQty").toString());
      int qtyP = Integer.parseInt(getPickingAMService().getPickingGenQryVO1().getCurrentRow().getAttribute("QtyPicked").toString());
      int qtyA = getPickingAMService().getNiinLocationQuantity(Long.parseLong(getPickingAMService().getPickingGenQryVO1().getCurrentRow().getAttribute("NiinLocId").toString()));
      int rQty = 0;
      boolean f = true;
      if (object != null) {
        rQty = Integer.parseInt(object.toString());
      }
      else {
        f = false;
        if (errStr.contains("error"))
          errStr = "";
        errStr = errStr + "NIIN_ERR1";
        JSFUtils.addFacesErrorMessage("QTY_ERR1", "Please enter valid quantity.");
      }
      if (f) {
        if ((rQty <= 0)) {
          f = false;
          if (errStr.contains("error"))
            errStr = "";
          errStr = errStr + "NIIN_ERR2";
          JSFUtils.addFacesErrorMessage("QTY_ERR2", "Quantity must be greater than 0.");
        }
        else if ((rQty > (pQty - qtyP))) {
          f = false;
          if (errStr.contains("error"))
            errStr = "";
          errStr = errStr + "NIIN_ERR2";
          JSFUtils.addFacesErrorMessage("QTY_ERR2", "Quantity picked must be less than or equal to quantity requested.");
        }
        else if (((qtyA - rQty) <= 0) && (errStr.length() <= 0)) {
          this.setExpObj(4);
        }
        else if ((rQty < (pQty - qtyP)) && (errStr.length() <= 0)) {
          this.setExpObj(3);
        }
        else if (((qtyA - rQty) < 10) && (errStr.length() <= 0)) {
          this.setExpObj(5);
        }
        else {
          if (errStr.contains("error"))
            errStr = "";
          this.setExpObj(0);
        }
      }
      if ((errStr.length() <= 0) && ((this.getSrlChoice().toString().equals("1")) && (this.getPickingAMService().getSerialOrLotNumList1().getRowCount() == rQty))) {
        //Got serial numbers, so do other things
        this.setExpObj(this.getExpObjBkp());
      }
      else if ((errStr.length() <= 0) && (!this.getExpObj().toString().equalsIgnoreCase("0")))
        //set backup for other choice that should be provided after serial num entry
        this.setExpObjBkp(this.getExpObj());
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  public void selEid(ValueChangeEvent valueChangeEvent) {
    try {
      Row ro = null;
      if (this.getExpObj().toString().equals("1")) { //For Refuse
        ro = getPickingAMService().getPickRefuseQryVO1().getRowAtRangeIndex(Integer.parseInt(valueChangeEvent.getNewValue().toString()));
      }
      else { //For ByPass
        ro = getPickingAMService().getPickByPassQryVO1().getRowAtRangeIndex(Integer.parseInt(valueChangeEvent.getNewValue().toString()));
      }
      if (ro != null)
        this.setEidStr(ro.getAttribute("Eid").toString());
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  public void setEidStr(String eidStr) {
    // Set Eid got from Pick UI Dialog
    this.eidStr = eidStr;
  }

  public String getEidStr() {
    // Get Eid
    return this.eidStr;
  }

  public void setExpObj(Object expObj) {
    this.expObj = expObj;
  }

  public Object getExpObj() {
    return this.expObj;
  }

  public boolean isBypass() {
    return expObj.toString().equals("2");
  }

  public void setSrlChoiceVal(ActionEvent actionEvent) {
    // Set this switch based on the niin needing serial or lot con num
    Object lQ = getEnterQtyPicked().getValue();
    this.validateSrl((lQ == null ? "" : lQ.toString()));
  }

  public String validateSrl(String valStr) {
    // Set this switch based on the niin needing serial or lot con num
    Row ro = getPickingAMService().getPickingGenQryVO1().getCurrentRow();
    Object lQ = valStr;
    String err = "";
    if (lQ != null)
      err = getPickingAMService().checkNiinNeedsAnySrlOrLotInfo(ro.getAttribute("NiinId").toString(), Integer.parseInt(lQ.toString()));
    if (((err.length() > 0) && (!err.contains("false")))) {
      this.setSrlChoice(1);
      errStr = "SRL_LOT_ERR";
      JSFUtils.addFacesErrorMessage("SRL_LOT_ERR", err);
    }
    else
      this.setSrlChoice(0);
    return errStr;
  }

  public void assignExpObj(ActionEvent actionEvent) {
    // Set the switch for Refuse
    if (getPickingAMService().getPickingGenQryVO1().getCurrentRow().getAttribute("IssueType") == null || !getPickingAMService().getPickingGenQryVO1().getCurrentRow().getAttribute("IssueType").toString().equalsIgnoreCase("R"))
      this.setExpObj(1);
  }

  public void assignExpObjForByPass(ActionEvent actionEvent) {
    // Set the switch for By Pass if it is more then one by pass
    try {
      Row ro = getPickingAMService().getPickingGenQryVO1().getCurrentRow();
      if ((ro.getAttribute("BypassCount") == null || ro.getAttribute("BypassCount").toString().length() <= 0) || (Integer.parseInt(ro.getAttribute("BypassCount").toString()) <= 0))
        getPickingAMService().pickRowByPass("PICK BYPASS", 1, ro.getAttribute("Pid"), Integer.parseInt(JSFUtils.getManagedBeanValue("userbean.userId").toString()));
      else {
        setExpObj(2);
      }
      this.setExpObj(0);
      this.setEnterNiinLastTwoDigits(null);
      this.setEnterQtyPicked(null);
      this.setScanLocLabel(null);
      this.setAvailQtyR(null);
      this.setAvailQty(null);
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  public String actionForwardStrForPickingNormalNiinVerOnRefuseDone() {
    try {
      val userId = JSFUtils.getManagedBeanValue("userbean.userId").toString();
      Row ro = getPickingAMService().getPickingGenQryVO1().getCurrentRow();
      val pid = ro.getAttribute("Pid");
      this.getWorkloadManagerService().createErrorQueueRecord(this.getEidStr(), "PID", pid.toString(), userId, null);
      getPickingAMService().pickRowRefused("PICK REFUSED", 0, pid, Integer.parseInt(userId));
      this.getPickingAMService().skipPicksForLocationOrStatusChange();
      goToNextPick();
      errStr = "";
      this.setExpObj(0);
      this.setEnterNiinLastTwoDigits(null);
      this.setEnterQtyPicked(null);
      this.getSrlOrLotConNum().setValue("");
      if (this.getPickingAMService().getSerialOrLotNumList1() != null)
        this.getPickingAMService().getSerialOrLotNumList1().clearCache();
      return actionForwardStr;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return null;
  }

  public String actionForwardStrForPickingNormalNiinVerOnCancel() {
    try {
      if (getExpObj().equals(4)) setExpObj(5);
      else setExpObj(0);

      actionForwardStr = "";
      errStr = "";
      this.setEnterNiinLastTwoDigits(null);
      this.setEnterQtyPicked(null);
      return actionForwardStr;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return null;
  }

  public String actionForwardStrForPickingNormalNiinVerOnByPass() {
    try {
      Row ro = getPickingAMService().getPickingGenQryVO1().getCurrentRow();
      if ((ro.getAttribute("BypassCount") == null) || (Integer.parseInt(ro.getAttribute("BypassCount").toString()) <= 0)) {
        this.getPickingAMService().skipPicksForLocationOrStatusChange();
        if (goToNextPick()) {
          this.setExpObj(0);
        }
      }
      else {
        actionForwardStr = "GoPickingNormalNiinVer";
        this.setExpObj(2);
      }
      errStr = "";
      this.setEnterNiinLastTwoDigits(null);
      this.setEnterQtyPicked(null);
      return actionForwardStr;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return null;
  }

  public void setScanLocLabel(RichInputText scanLocLabel) {
    this.scanLocLabel = scanLocLabel;
  }

  public RichInputText getScanLocLabel() {
    return scanLocLabel;
  }

  public void setEnterNiinLastTwoDigits(RichInputText enterNiinLastTwoDigits) {
    this.enterNiinLastTwoDigits = enterNiinLastTwoDigits;
  }

  public RichInputText getEnterNiinLastTwoDigits() {
    return enterNiinLastTwoDigits;
  }

  public void setEnterQtyPicked(RichInputText enterQtyPicked) {
    this.enterQtyPicked = enterQtyPicked;
  }

  public RichInputText getEnterQtyPicked() {
    return enterQtyPicked;
  }

  public String actionForwardStrForPickingNormalNiinVerOnByPassDone() {
    try {
      Row ro = getPickingAMService().getPickingGenQryVO1().getCurrentRow();
      //Update the pick row with the bypass info
      val pid = ro.getAttribute("Pid");
      val userId = JSFUtils.getManagedBeanValue("userbean.userId").toString();
      int byPassCt = getPickingAMService().pickRowByPass("PICK BYPASS", 2, pid, Integer.parseInt(userId));
      // create the exception or error queue record
      if (byPassCt > 1) {
        BindingContainer bindings = BindingContext.getCurrent().getCurrentBindingsEntry();
        JUCtrlListBinding listBinding = (JUCtrlListBinding) bindings.get("PickByPassList");
        Row r = listBinding.getRowAtRangeIndex(Integer.parseInt(bypassList.getValue().toString()));
        String eid = r.getAttribute("Eid").toString();

        this.getWorkloadManagerService().createErrorQueueRecord(eid, "PID", pid.toString(), userId, null);
      }
      this.getPickingAMService().skipPicksForLocationOrStatusChange();
      goToNextPick();
      errStr = "";
      this.setExpObj(0);
      if (this.getEnterNiinLastTwoDigits() != null)
        this.setEnterNiinLastTwoDigits(null);
      if (this.getEnterQtyPicked() != null)
        this.setEnterQtyPicked(null);
      if (this.getSrlOrLotConNum() != null)
        this.getSrlOrLotConNum().setValue("");
      if (this.getPickingAMService().getSerialOrLotNumList1() != null)
        this.getPickingAMService().getSerialOrLotNumList1().clearCache();
      return actionForwardStr;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return null;
  }

  /**
   * DESCRIPTION : On validation completion calling packing routines and other backend method yo update pick.
   */
  public String actionForwardStrForPickingNormalNiinVerOnQtyDone() {
    try {
      Row ro = getPickingAMService().getPickingGenQryVO1().getCurrentRow();

      Object lQ = getEnterQtyPicked().getValue();
      if (lQ == null || lQ.toString().trim().isEmpty()) {
        errStr = errStr + "QTY_ERR";
        JSFUtils.addFacesErrorMessage("QTY_ERR", "Quantity picked is required.");
      }
      else {
        if (!isNumericAA(lQ.toString().trim())) {
          errStr = errStr + "QTY_ERR";
          JSFUtils.addFacesErrorMessage("QTY_ERR", "Quantity picked must be a whole number.");
        }
      }

      //Update the database pick qty and status
      if ((errStr.length() <= 0) && ((ro.getAttribute("IssueType") == null) || (!ro.getAttribute("IssueType").toString().equalsIgnoreCase("R")))) //RE-WAREHOUSING code
      {
        String packStation = "";
        val pickQty = Integer.parseInt(ro.getAttribute("PickQty").toString());
        val qtyPicked = Integer.parseInt(ro.getAttribute("QtyPicked").toString());
        val enteredQty = Integer.parseInt(this.getEnterQtyPicked().getValue().toString());
        if (pickQty > (qtyPicked + enteredQty)) {
          //Get a differet packing station
          packStation = this.getWorkloadManagerService().getPackingStation(Integer.parseInt(ro.getAttribute("Pid").toString()), Integer.parseInt(JSFUtils.getManagedBeanValue("userbean.userId").toString()), Integer.parseInt(this.getEnterQtyPicked().getValue().toString()), 1);
        }
        else {
          if (ro.getAttribute("PackingConsolidationId") != null)
            packStation = this.getWorkloadManagerService().getAssignedPackingStationName(ro.getAttribute("PackingConsolidationId").toString());
        }
        if (((packStation == null) || (packStation.length() <= 0)) || (packStation.toUpperCase().contains("ERROR"))) {
          errStr = errStr + "PACK_ERR";
          if ((packStation != null) && packStation.toUpperCase().contains("ERROR"))
            JSFUtils.addFacesErrorMessage("PACK_ERR_MSG", packStation);
          else
            JSFUtils.addFacesErrorMessage("PACK_ERR_MSG", "Could not get packing station.");
        }
        else
          JSFUtils.setManagedBeanValue("userbean.specialMessage", "Information: Send " + ro.getAttribute("Pin") + " to " + packStation);
      }
      else if ((ro.getAttribute("IssueType") != null && ro.getAttribute("IssueType").toString().equalsIgnoreCase("R")) && (ro.getAttribute("Sid") == null)) {
        errStr = errStr + "RWH_ERR_MSG";
        JSFUtils.addFacesErrorMessage("RWH_ERR_MSG", "This pick is rewarehouse and has no STOW created.");
      }
      this.validateSrl(this.getEnterQtyPicked().getValue().toString());
      if (errStr.length() <= 0) {
        this.getPickingAMService().pickRowStatusChange(Long.parseLong(ro.getAttribute("Pid").toString()), Integer.parseInt(JSFUtils.getManagedBeanValue("userbean.userId").toString()), Integer.parseInt(ro.getAttribute("PickQty").toString()), Integer.parseInt(ro.getAttribute("QtyPicked").toString()), Integer.parseInt(this.getEnterQtyPicked().getValue().toString()), Long.parseLong(ro.getAttribute("NiinLocId").toString()));
        //Calling to build the Zebra print string with PIN, Qty of labels, Darkeness -30 to 30
        this.getPickingAMService().changeWalkThruStatus(ro.getAttribute("DocumentNumber").toString(), Integer.parseInt(JSFUtils.getManagedBeanValue("userbean.userId").toString()));
        getWalkThruAMService().refreshWalkThruTable();
        getWalkThruAMService().setFromPickbyWalkThru(true);
        String prnStr = "";
        if ((ro.getAttribute("IssueType") != null) && (ro.getAttribute("IssueType").toString().equalsIgnoreCase("R"))) { //For RE-WAREHOUSING
          prnStr = this.getWorkloadManagerService().generatePrintAllSIDLabelUsingRcn(ro.getAttribute("Sid").toString(), 0, 0, false);
          String[] sids = this.getWorkloadManagerService().getAllSIDLabelUsingRcn(ro.getAttribute("Sid").toString());
          printSidLabel(prnStr, sids);
          String wac = getWorkloadManagerService().getWACforSID(ro.getAttribute("Sid").toString());
          if (!wac.isEmpty()) {
            JSFUtils.setManagedBeanValue("userbean.specialMessage", "Information: Send " + ro.getAttribute("Sid") + " to " + wac);
          }
        }
        else {
          // call with SID, Qty of labels, darkness (-30 to 30)
          prnStr = this.getWorkloadManagerService().generatePrintPINLabel(ro.getAttribute("Pin").toString(), 0, 0);
          printLabel(prnStr, ro.getAttribute("Pin").toString());
        }

        this.getInventorySetupModuleService().setUserId(Integer.parseInt(JSFUtils.getManagedBeanValue("userbean.userId").toString()));
        errStr = this.getInventorySetupModuleService().verifyLocationSpotCheck(ro.getAttribute("NiinLocId"), 0);
        if (errStr.toUpperCase().contains("ERROR"))
          JSFUtils.addFacesErrorMessage("INV_ERR_MSG", errStr);
        else
          errStr = "";
        if (errStr.length() <= 0) {
          //Make call to process partial pick to create new pick for the remaining picks.
          if (Integer.parseInt(ro.getAttribute("PickQty").toString()) > (Integer.parseInt(ro.getAttribute("QtyPicked").toString()) + Integer.parseInt(this.getEnterQtyPicked().getValue().toString()))) {
            if ((ro.getAttribute("IssueType") == null) || (!ro.getAttribute("IssueType").toString().equalsIgnoreCase("R"))) {
              try {
                log.trace("Entering PickingAll::getWorkloadManagerService().returnBestPickLocation()");
                this.getWorkloadManagerService().returnBestPickLocation(null, ro.getAttribute("NiinId"), ro.getAttribute("Pid"), ro.getAttribute("Cc").toString());
                log.trace("Leaving PickingAll::getWorkloadManagerService().returnBestPickLocation()");
              }
              catch (Throwable t) {
                AdfLogUtility.logException(t);
                throw t;
              }
            }
          }

          if ((ro.getAttribute("IssueType") == null) || (!Util.cleanString(ro.getAttribute("IssueType")).equals("R") && !Util.cleanString(ro.getAttribute("IssueType")).equals("W"))) {
            //* send SRO transaction - added 02-13-09
            this.getGCSSMCAMService().sendSROGCSSMCTransaction(ro.getAttribute("Pid").toString());
          }

          this.getPickingAMService().skipPicksForLocationOrStatusChange();
          goToNextPick();
        }
        if (this.getPickingAMService().getSerialOrLotNumList1() != null)
          this.getPickingAMService().getSerialOrLotNumList1().clearCache();
      }
      errStr = "";
      this.setExpObj(0);
      this.setEnterNiinLastTwoDigits(null);
      this.setEnterQtyPicked(null);
      this.getSrlOrLotConNum().setValue("");
      return actionForwardStr;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return null;
  }

  public void showQtyPickedErrorMessage(ActionEvent actionEvent) {
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

  /**
   * DESCRIPTION : Call backend based on the inverntory conditions for spot check or for best pick location.
   */
  public String actionForwardStrForPickingNormalNiinVerOnInvQtyDone() {
    try {
      Row ro = getPickingAMService().getPickingGenQryVO1().getCurrentRow();

      Object lQ = getEnterQtyPicked().getValue();
      if (lQ == null || lQ.toString().trim().isEmpty()) {
        errStr = errStr + "QTY_ERR";
        JSFUtils.addFacesErrorMessage("QTY_ERR", "Quantity picked is required.");
      }
      else {
        if (!isNumericAA(lQ.toString().trim())) {
          errStr = errStr + "QTY_ERR";
          JSFUtils.addFacesErrorMessage("QTY_ERR", "Quantity picked must be a whole number.");
        }
      }

      if (errStr.length() <= 0) {
        int actualQty = getPickingAMService().getNiinLocationQuantity(Long.parseLong(ro.getAttribute("NiinLocId").toString()));
        actualQty = actualQty - (this.getEnterQtyPicked().getValue() == null ? 0 : Integer.parseInt(this.getEnterQtyPicked().getValue().toString()));
        actualQty = actualQty - (ro.getAttribute("QtyPicked") == null ? 0 : Integer.parseInt(ro.getAttribute("QtyPicked").toString()));
        if (this.getAvailQty().getValue() == null) {
          errStr = "INV_CHK";
          JSFUtils.addFacesErrorMessage("INV_CHK", "No value entered for inventory count.");
          actionForwardStr = "";
          this.setExpObj(5);
        }
        else if ((Integer.parseInt(this.getAvailQty().getValue().toString()) != actualQty) && (this.getAvailQtyR().getValue() == null || !this.getAvailQty().getValue().equals(this.getAvailQtyR().getValue()))) {
          if (this.getAvailQtyR().getValue() == null) {
            errStr = "INV_CHK";
            JSFUtils.addFacesInformationMessage("Count entered does not match with what is in STRATIS for this location.");
            actionForwardStr = "";
            this.setExpObj(5);
          }
          else if (!this.getAvailQty().getValue().equals(this.getAvailQtyR().getValue())) {
            errStr = "INV_CHK";
            JSFUtils.addFacesErrorMessage("INV_CHK", "Original and re-confirm count must match.");
            actionForwardStr = "";
            this.setExpObj(5);
          }
        }
        else {
          if ((errStr.length() <= 0) && ((ro.getAttribute("IssueType") == null) || (!ro.getAttribute("IssueType").toString().equalsIgnoreCase("R")))) //RE-WAREHOUSING code
          {
            String packStation = "";
            if (Integer.parseInt(ro.getAttribute("PickQty").toString()) > (Integer.parseInt(ro.getAttribute("QtyPicked").toString()) + (this.getEnterQtyPicked().getValue() == null ? 0 : Integer.parseInt(this.getEnterQtyPicked().getValue().toString())))) {
              //Get a differet packing station for partial
              packStation = this.getWorkloadManagerService().getPackingStation(Integer.parseInt(ro.getAttribute("Pid").toString()), Integer.parseInt(JSFUtils.getManagedBeanValue("userbean.userId").toString()), (this.getEnterQtyPicked().getValue() == null ? 0 : Integer.parseInt(this.getEnterQtyPicked().getValue().toString())), 1);
            }
            else {
              if (ro.getAttribute("PackingConsolidationId") != null)
                packStation = this.getWorkloadManagerService().getAssignedPackingStationName(ro.getAttribute("PackingConsolidationId").toString());
            }
            if (((packStation == null) || (packStation.length() <= 0)) || (packStation.toUpperCase().contains("ERROR"))) {
              errStr = errStr + "PACK_ERR";
              if ((packStation != null) && packStation.toUpperCase().contains("ERROR"))
                JSFUtils.addFacesErrorMessage("PACK_ERR_MSG", packStation);
              else
                JSFUtils.addFacesErrorMessage("PACK_ERR_MSG", "Could not get packing station.");
            }
            else
              JSFUtils.setManagedBeanValue("userbean.specialMessage", "Information: Send " + ro.getAttribute("Pin") + " to " + packStation);
          }
          else if ((ro.getAttribute("IssueType") != null && ro.getAttribute("IssueType").toString().equalsIgnoreCase("R")) && (ro.getAttribute("Sid") == null)) {
            errStr = errStr + "RWH_ERR_MSG";
            JSFUtils.addFacesErrorMessage("RWH_ERR_MSG", "This pick is rewarehouse and has no STOW created.");
          }
          this.validateSrl(this.getEnterQtyPicked().getValue().toString());
          if (errStr.length() <= 0) {
            val userId = Integer.parseInt(JSFUtils.getManagedBeanValue("userbean.userId").toString());
            //Update the database pick qty and status
            this.getPickingAMService().pickRowStatusChange(Long.parseLong(ro.getAttribute("Pid").toString()), userId, Integer.parseInt(ro.getAttribute("PickQty").toString()), Integer.parseInt(ro.getAttribute("QtyPicked").toString()), Integer.parseInt(this.getEnterQtyPicked().getValue().toString()), Long.parseLong(ro.getAttribute("NiinLocId").toString()));
            this.getPickingAMService().changeWalkThruStatus(ro.getAttribute("DocumentNumber").toString(), userId);
            getWalkThruAMService().refreshWalkThruTable();
            getWalkThruAMService().setFromPickbyWalkThru(true);
            if ((ro.getAttribute("IssueType") == null) || (!Util.cleanString(ro.getAttribute("IssueType")).equals("R") && !Util.cleanString(ro.getAttribute("IssueType")).equals("W"))) {
              //* send SRO transaction - added 02-13-09
              this.getGCSSMCAMService().sendSROGCSSMCTransaction(ro.getAttribute("Pid").toString());
            }

            //Calling to build the Zebra print string with PIN, Qty of labels, Darkness -30 to 30
            String prnStr = "";
            if ((ro.getAttribute("IssueType") != null) && (ro.getAttribute("IssueType").toString().equalsIgnoreCase("R"))) { //For RE-WAREHOUSING
              prnStr = this.getWorkloadManagerService().generatePrintAllSIDLabelUsingRcn(ro.getAttribute("Sid").toString(), 0, 0, false);
              String[] sids = this.getWorkloadManagerService().getAllSIDLabelUsingRcn(ro.getAttribute("Sid").toString());
              printSidLabel(prnStr, sids);
              String wac = getWorkloadManagerService().getWACforSID(ro.getAttribute("Sid").toString());
              if (!wac.isEmpty()) {
                JSFUtils.setManagedBeanValue("userbean.specialMessage", "Information: Send " + ro.getAttribute("Sid") + " to " + wac);
              }
            }
            else{
              // call with SID, Qty of labels, darkness (-30 to 30)
              prnStr = this.getWorkloadManagerService().generatePrintPINLabel(ro.getAttribute("Pin").toString(), 0, 0);
              printLabel(prnStr, ro.getAttribute("Pin").toString());
            }
            this.getInventorySetupModuleService().setUserId(Integer.parseInt(JSFUtils.getManagedBeanValue("userbean.userId").toString()));
            log.debug("HERRO: {} : QTY:  {}", ro.getAttribute("NiinLocId").toString(), this.getAvailQty().getValue().toString());
            errStr = this.getInventorySetupModuleService().verifyLocationSpotCheck(ro.getAttribute("NiinLocId"), Integer.parseInt(this.getAvailQty().getValue().toString()));
            log.debug("ERRSTR:  {}", errStr);
            if (errStr.toUpperCase().contains("ERROR"))
              JSFUtils.addFacesErrorMessage("INV_ERR_MSG", errStr);
            else
              errStr = "";
            if (errStr.length() <= 0) {
              this.getPickingAMService().skipPicksForLocationOrStatusChange();
              goToNextPick();
            }
            if (this.getPickingAMService().getSerialOrLotNumList1() != null)
              this.getPickingAMService().getSerialOrLotNumList1().clearCache();
          }
          this.setExpObj(0);
          errStr = "";
          this.setEnterNiinLastTwoDigits(null);
          this.setEnterQtyPicked(null);
          this.availQty.setValue(null);
          this.availQtyR.setValue(null);
          this.getSrlOrLotConNum().setValue("");
        }
      }
      return actionForwardStr;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return null;
  }

  /**
   * DESCRIPTION : Getting the picks by AAC.
   */
  public void buildParmStrForAACs(ActionEvent actionEvent) {
    // Get the selected row AAC list into
    try {

      Object oldRowKey = customerList.getRowKey();
      Iterator selection = customerList.getSelectedRowKeys().iterator();
      try {
        while (selection.hasNext()) {
          Object rowKey = selection.next();
          customerList.setRowKey(rowKey);
          JUCtrlValueBindingRef rowData = (JUCtrlValueBindingRef) customerList.getRowData();
          Row r = rowData.getRow();
          if (r.getAttribute("Aac") != null)
            this.setPramStr1(r.getAttribute("Aac").toString());
        }
      }
      catch (Exception e) {
        AdfLogUtility.logException(e);
      }
      finally {
        customerList.setRowKey(oldRowKey);
      }

      if (this.getPramStr1().length() <= 0) {
        JSFUtils.addFacesErrorMessage("SELECT_ERR", "Please make selection and then click <Submit>. Otherwise click <Quit> to go back to Picking Home.");
        this.actionForwardStr = "";
        this.setPramStr1("EMPTY");
      }
      else {
        //selection.clear();
        actionForwardStr = "GoPickingNormalMain";
      } // 0 is normal pick
      //561 as sample
      val sortBuilder = ContextUtils.getBean(LocationSortBuilder.class);
      val sortOrder = sortBuilder.getSortString(LocationSortBuilder.QueryType.ADF);

      getPickingAMService().buildPickRS(Long.parseLong(JSFUtils.getManagedBeanValue("userbean.selected2Index").toString()), Long.parseLong(JSFUtils.getManagedBeanValue("userbean.workstationId").toString()), JSFUtils.getManagedBeanValue("userbean.userId").toString(), this.getPramStr1(), "", sortOrder);
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  /**
   * DESCRIPTION : Getting the picks by Routes.
   */
  public void buildParmStrForRoutes(ActionEvent actionEvent) {
    // Get the selected row Route list into
    try {
      Object oldRowKey = routeList.getRowKey();
      Iterator selection = routeList.getSelectedRowKeys().iterator();
      try {
        while (selection.hasNext()) {
          Object rowKey = selection.next();
          routeList.setRowKey(rowKey);
          JUCtrlValueBindingRef rowData = (JUCtrlValueBindingRef) routeList.getRowData();
          Row r = rowData.getRow();
          if (r.getAttribute("RouteName") != null)
            this.setPramStr1(r.getAttribute("RouteName").toString());
        }
      }
      catch (Exception e) {
        AdfLogUtility.logException(e);
      }
      finally {
        routeList.setRowKey(oldRowKey);
      }

      if (this.getPramStr1().length() <= 0) {
        JSFUtils.addFacesErrorMessage("SELECT_ERR", "Please make selection and then click <Submit>. Otherwise click <Quit> to go back to Picking Home.");
        this.actionForwardStr = "";
        this.setPramStr1("EMPTY");
      }
      else {
        actionForwardStr = "GoPickingNormalMain";
      } // 0 is normal pick
      //561 as sample
      val sortBuilder = ContextUtils.getBean(LocationSortBuilder.class);
      val sortOrder = sortBuilder.getSortString(LocationSortBuilder.QueryType.ADF);
      getPickingAMService().buildPickRS(Long.parseLong(JSFUtils.getManagedBeanValue("userbean.selected2Index").toString()), Long.parseLong(JSFUtils.getManagedBeanValue("userbean.workstationId").toString()), JSFUtils.getManagedBeanValue("userbean.userId").toString(), this.getPramStr1(), "", sortOrder);
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  /**
   * DESCRIPTION : Getting the picks by Walk Thrus.
   */
  public void buildParmStrForWalkThrus(ActionEvent actionEvent) {
    // Get the selected row Route list into
    try {
      Object oldRowKey = walkThruList.getRowKey();
      Iterator selection = walkThruList.getSelectedRowKeys().iterator();
      try {
        while (selection.hasNext()) {
          Object rowKey = selection.next();
          walkThruList.setRowKey(rowKey);
          JUCtrlValueBindingRef rowData = (JUCtrlValueBindingRef) walkThruList.getRowData();
          Row r = rowData.getRow();
          if (r.getAttribute("DocumentNumber") != null)
            this.setPramStr1(r.getAttribute("DocumentNumber").toString());
        }
      }
      catch (Exception e) {
        AdfLogUtility.logException(e);
      }
      finally {
        walkThruList.setRowKey(oldRowKey);
      }

      if (this.getPramStr1().length() <= 0) {
        JSFUtils.addFacesErrorMessage("SELECT_ERR", "Please make selection and then click <Submit>. Otherwise click <Quit> to go back to Picking Home.");
        this.actionForwardStr = "";
        this.setPramStr1("EMPTY");
      }
      else {
        actionForwardStr = "GoPickingNormalMain";
      } // 0 is normal pick
      //561 as sample

      val sortBuilder = ContextUtils.getBean(LocationSortBuilder.class);
      val sortOrder = sortBuilder.getSortString(LocationSortBuilder.QueryType.ADF);

      getPickingAMService().buildPickRS(Long.parseLong(JSFUtils.getManagedBeanValue("userbean.selected2Index").toString()), Long.parseLong(JSFUtils.getManagedBeanValue("userbean.workstationId").toString()), JSFUtils.getManagedBeanValue("userbean.userId").toString(), this.getPramStr1(), "", sortOrder);
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  public void setPramStr1(String pramStr1) {
    if (this.pramStr1.length() <= 0)
      this.pramStr1 = "'" + pramStr1.trim() + "'";
    else
      this.pramStr1 = this.pramStr1 + ", '" + pramStr1.trim() + "'";
  }

  public String getPramStr1() {
    return pramStr1;
  }

  public void setCustomerList(RichTable customerList) {
    this.customerList = customerList;
  }

  public RichTable getCustomerList() {
    return customerList;
  }

  public void setSelInd2(long selInd2) {
    this.selInd2 = selInd2;
  }

  public long getSelInd2() {
    return selInd2;
  }

  public void setRouteList(RichTable routeList) {
    this.routeList = routeList;
  }

  public RichTable getRouteList() {
    return routeList;
  }

  public void setPriorityList(RichTable priorityList) {
    this.priorityList = priorityList;
  }

  public RichTable getPriorityList() {
    return priorityList;
  }

  public RichTable getWalkThruList() {
    return walkThruList;
  }

  public void setWalkThruList(RichTable walkThruList) {
    this.walkThruList = walkThruList;
  }

  public String getActionForwardStr() {
    return this.actionForwardStr;
  }

  /**
   * DESCRIPTION : Getting the picks by priorities.
   */
  public void buildParmStrForPriorities(ActionEvent actionEvent) {
    // Get the selected row Priority list into
    try {
      Object oldRowKey = priorityList.getRowKey();
      Iterator selection = priorityList.getSelectedRowKeys().iterator();
      try {
        while (selection.hasNext()) {
          Object rowKey = selection.next();
          priorityList.setRowKey(rowKey);
          JUCtrlValueBindingRef rowData = (JUCtrlValueBindingRef) priorityList.getRowData();
          Row r = rowData.getRow();
          if (r.getAttribute("IssuePriorityGroup") != null)
            this.setPramStr1(r.getAttribute("IssuePriorityGroup").toString());
        }
      }
      catch (Exception e) {
        AdfLogUtility.logException(e);
      }
      finally {
        priorityList.setRowKey(oldRowKey);
      }

      if (this.getPramStr1().length() <= 0) {
        JSFUtils.addFacesErrorMessage("SELECT_ERR", "Please make selection and then click <Submit>. Otherwise click <Quit> to go back to Picking Home.");
        this.actionForwardStr = "";
        this.setPramStr1("EMPTY");
      }
      else {
        //selection.clear();
        actionForwardStr = "GoPickingNormalMain";
      } // 0 is normal pick
      //561 as sample

      val sortBuilder = ContextUtils.getBean(LocationSortBuilder.class);
      val sortOrder = sortBuilder.getSortString(LocationSortBuilder.QueryType.ADF);
      getPickingAMService().buildPickRS(Long.parseLong(JSFUtils.getManagedBeanValue("userbean.selected2Index").toString()), Long.parseLong(JSFUtils.getManagedBeanValue("userbean.workstationId").toString()), JSFUtils.getManagedBeanValue("userbean.userId").toString(), this.getPramStr1(), "", sortOrder);
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
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

  public void createExceptionInErrorQueue(ActionEvent actionEvent) {
    try {
      getPickingAMService().getPickingGenQryVO1().getCurrentRow();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  public void setAvailQty1(ValueChangeEvent valueChangeEvent) {
    this.getAvailQty().setValue(valueChangeEvent.getNewValue());
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

  public void setTextForDialog(String textForDialog) {
    this.textForDialog = textForDialog;
  }

  public String getTextForDialog() {
    return textForDialog;
  }

  public void setDialogParameters(String actionStr, boolean flag) {
    //Set the dialog window parameters
    actionForwardStr = actionStr;
    uiDialog = flag;
  }

  public void uiDialogDone(@SuppressWarnings("all") ActionEvent actionEvent) {
    // Action Listener for UI dialog.

    try {
      AdfFacesContext.getCurrentInstance().returnFromDialog(null, null);
      this.setDialogParameters("", false);
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  public void setUiDialog(boolean uiDialog) {
    this.uiDialog = uiDialog;
  }

  public boolean isUiDialog() {
    return uiDialog;
  }

  /**
   * DESCRIPTION : Has the logic to move carousel to the next pick.
   */
  public void performCarouselMove(int flag) {
    //Call the carousel move
    //flag == 1 means that it is comming from cancel or bypass or refuse so goal is to just move on
    try {
      if (this.getWorkloadManagerService().isMechanized(getPickingAMService().getPickingGenQryVO1().getCurrentRow().getAttribute("WacId").toString())) {
        String mvCmdStr = "";
        String mvCmdStrFirst = "";
        mvCmdStrFirst = this.getWorkloadManagerService().performCarouselMove(getPickingAMService().getPickingGenQryVO1().getCurrentRow());
        if ((flag == 1) || (actionForwardStr.equalsIgnoreCase("GoPickingNormal"))) {
          //Make the move based on the new stow which is going to start from location screen
          String preSide = getPickingAMService().getPickingGenQryVO1().getCurrentRow().getAttribute("Side").toString();
          String curSide = getPickingAMService().getPickingGenQryVO1().getCurrentRow().getAttribute("Side").toString();
          log.debug("1. curSide = {} preSide =  {}", curSide, preSide);
          if (getPickingAMService().getPickingGenQryVO1().hasPrevious()) {
            preSide = getPickingAMService().getPickingGenQryVO1().previous().getAttribute("Side").toString();
            getPickingAMService().getPickingGenQryVO1().next();
          }
          log.debug("2. curSide = {} preSide =  {}", curSide, preSide);
          if ((flag == 1) || (curSide.equalsIgnoreCase(preSide))) //Case were we had to wait till end to move to new
            log.debug("3. curSide = {} preSide =  {}", curSide, preSide);
          mvCmdStr = this.getWorkloadManagerService().performCarouselMove(getPickingAMService().getPickingGenQryVO1().getCurrentRow());
          log.debug("3. mvCmdStr  {}", mvCmdStr);
        }
        else if ((actionForwardStr.equalsIgnoreCase("GoPickingNormalNiinVer"))) { //Going to NIIN verification
          if (getPickingAMService().getPickingGenQryVO1().hasNext()) {
            //It it has next stow so get the side of that next stow
            String curSide = getPickingAMService().getPickingGenQryVO1().getCurrentRow().getAttribute("Side").toString();
            String nextSide = getPickingAMService().getPickingGenQryVO1().next().getAttribute("Side").toString();
            log.debug("4. curSide = {} nextSide =  {}", curSide, nextSide);
            if (!curSide.equalsIgnoreCase(nextSide)) { //looks like the side are opposite
              //Get move command
              log.debug("5. curSide = {} nextSide =  {}", curSide, nextSide);
              mvCmdStr = this.getWorkloadManagerService().performCarouselMove(getPickingAMService().getPickingGenQryVO1().getCurrentRow());
              log.debug("5. mvCmdStr =  {}", mvCmdStr);
            }
            //Reset the pointer to previous stow becuase we moved it in the previous statement
            getPickingAMService().getPickingGenQryVO1().previous();
          }
        }
        mvCmdStr = mvCmdStrFirst + mvCmdStr;
        //Activate this when ready for COM
        if (mvCmdStr.length() > 0) {

          log.debug("6. mvCmdStr =  {}", mvCmdStr);
          // turn the comm on
          JSFUtils.setManagedBeanValue("userbean.useprintcom2", 1);
          // set the id of which comm to use from the com_port table
          JSFUtils.setManagedBeanValue("userbean.printcomport2", JSFUtils.getManagedBeanValue("userbean.comCommandIndex"));
          // set the string to go out
          JSFUtils.setManagedBeanValue("userbean.printcomstring2", mvCmdStr);
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  /**
   * DESCRIPTION : Has the logic to move carousel to the next pick.
   */
  public void performCarouselMoveOld(int flag) {
    //Call the carousel move
    //flag == 1 means that it is comming from cancel or bypass or refuse so goal is to just move on
    try {
      String mvCmdStr = "";
      if ((flag == 1) || (actionForwardStr.equalsIgnoreCase("GoPickingNormal"))) {
        //Make the move based on the new stow which is going to start from location screen
        String preSide = getPickingAMService().getPickingGenQryVO1().getCurrentRow().getAttribute("Side").toString();
        String curSide = getPickingAMService().getPickingGenQryVO1().getCurrentRow().getAttribute("Side").toString();
        if (getPickingAMService().getPickingGenQryVO1().hasPrevious()) {
          preSide = getPickingAMService().getPickingGenQryVO1().previous().getAttribute("Side").toString();
          getPickingAMService().getPickingGenQryVO1().next();
        }
        if ((flag == 1) || (curSide.equalsIgnoreCase(preSide))) //Case were we had to wait till end to move to new
          mvCmdStr = this.getWorkloadManagerService().performCarouselMove(getPickingAMService().getPickingGenQryVO1().getCurrentRow());
      }
      else if ((actionForwardStr.equalsIgnoreCase("GoPickingNormalNiinVer"))) { //Going to NIIN verification
        if (getPickingAMService().getPickingGenQryVO1().hasNext()) {
          //It it has next stow so get the side of that next stow
          String curSide = getPickingAMService().getPickingGenQryVO1().getCurrentRow().getAttribute("Side").toString();
          String nextSide = getPickingAMService().getPickingGenQryVO1().next().getAttribute("Side").toString();
          if (!curSide.equalsIgnoreCase(nextSide)) { //looks like the side are opposite
            //Get move command
            mvCmdStr = this.getWorkloadManagerService().performCarouselMove(getPickingAMService().getPickingGenQryVO1().getCurrentRow());
          }
          //Reset the pointer to previous stow becuase we moved it in the previous statement
          getPickingAMService().getPickingGenQryVO1().previous();
        }
      }
      //Activate this when ready for COM
      if (mvCmdStr.length() > 0) {
        // turn the comm on
        JSFUtils.setManagedBeanValue("userbean.useprintcom2", 1);
        // set the id of which comm to use from the com_port table
        JSFUtils.setManagedBeanValue("userbean.printcomport2", JSFUtils.getManagedBeanValue("userbean.comCommandIndex"));
        // set the string to go out
        JSFUtils.setManagedBeanValue("userbean.printcomstring2", mvCmdStr);
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  public void lookupNiins(ActionEvent actionEvent) {
    try {
      Row ro = getPickingAMService().getPickingGenQryVO1().getCurrentRow();
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

  public void niinOk(ActionEvent actionEvent) {
    //Close the dialog window that displays the Niin information based on the
    //location label
    AdfFacesContext.getCurrentInstance().returnFromDialog(null, null);
  }

  public void addSrlLotNumList(ActionEvent actionEvent) {
    int qty = 0;
    Row ro = getPickingAMService().getPickingGenQryVO1().getCurrentRow();
    try {
      if (enterQtyPicked.getValue() != null) {
        qty = Integer.parseInt(enterQtyPicked.getValue().toString());
      }
    }
    catch (Exception e) {
      JSFUtils.addFacesErrorMessage("SL_ERR", "Pick Quantity invalid.");
      return;
    }
    ArrayList serialList = new ArrayList();
    String kErr = "";
    String err = "";
    if ((this.getSrlOrLotConNum().getValue() == null) && ((this.getStartSerial().getValue() == null) || (this.getEndSerial().getValue() == null))) {
      errStr = errStr + "SL_ERR";
      JSFUtils.addFacesErrorMessage("SL_ERR", "Please enter Serial or Lot Con Number.");
      return;
    }
    else
      errStr = "";
    if (errStr == null || errStr.length() <= 0) {
      if (this.getSrlOrLotConNum().getValue() != null && (this.getSrlOrLotConNum().getValue().toString().length() > 0))
        kErr = getPickingAMService().addSerialOrLotNumVOList(this.getSrlOrLotConNum().getLocalValue().toString().trim().toUpperCase(), ro.getAttribute("NiinId").toString(), "1", ro.getAttribute("Cc").toString(), ro.getAttribute("NiinLocId").toString(), ro.getAttribute("Niin").toString(), qty);
      else {
        kErr = getWorkloadManagerService().generateSerialRange(getStartSerial().getValue().toString(), getEndSerial().getValue().toString(), serialList);
        if (kErr == null || kErr.length() <= 0) {
          for (int i = 0; i < serialList.size(); i++) {
            err = getPickingAMService().addSerialOrLotNumVOList(serialList.get(i).toString().trim().toUpperCase(), ro.getAttribute("NiinId").toString(), "1", ro.getAttribute("Cc").toString(), ro.getAttribute("NiinLocId").toString(), ro.getAttribute("Niin").toString(), qty);
            if (!err.isEmpty()) {
              if (kErr == null || kErr.isEmpty()) {
                kErr = err;
              }
              else {
                kErr = kErr + "\n" + err;
              }
            }
          }
          this.getEndSerial().setValue("");
          this.getStartSerial().setValue("");
        }
      }

      SerialOrLotNumListImpl vo = getPickingAMService().getSerialOrLotNumList1();
      vo.setSortBy("SerialOrLoNum");
      vo.setQueryMode(ViewObject.QUERY_MODE_SCAN_VIEW_ROWS);
      vo.executeQuery();
    }
    if (kErr == null || kErr.length() > 0) {
      if (kErr != null && !kErr.contains("Warning:")) {
        errStr = errStr + "SL_ERR";
        JSFUtils.addFacesErrorMessage("SL_ERR", kErr);
      }
      else {
        JSFUtils.addFacesErrorMessage(kErr);
      }
    }
    this.getSrlOrLotConNum().setValue("");
    this.startSerial.setValue("");
    this.endSerial.setValue("");
  }

  public void deleteSrlLotNumFromList(ActionEvent actionEvent) {
    this.getPickingAMService().getSerialOrLotNumList1().removeCurrentRowFromCollection();
  }

  private boolean goToNextPick() {
    boolean hasNextPick;
    boolean processNext = false;
    boolean doExit = false;

    val userId = JSFUtils.getManagedBeanValue("userbean.userId").toString();
    int uid = Integer.parseInt(userId);
    //if has next
    PickingGenQryVOImpl pNQ = getPickingAMService().getPickingGenQryVO1();
    if (pNQ.hasNext()) {
      //do
      do {
        //get next pid
        Row ro = pNQ.next();
        String nextPid = ro.getAttribute("Pid").toString();
        //check if the uid is different from DB
        boolean result = getPickingAMService().checkPidsForOtherUserAssigned(nextPid, userId);
        if (result) {
          //if not continue and break
          processNext = true;
          doExit = true;
        }
        else {
          //log
          log.warn("Pre assigned pick  {}", nextPid);
          // check if next if not break but don't continue
          if (!pNQ.hasNext()) {
            doExit = true;
          }
        }
      } while (!doExit);
    }

    if (processNext) {
      actionForwardStr = "GoPickingNormalMain";
      this.performCarouselMove(0);
      hasNextPick = true;
    }
    else {
      getPickingAMService().getPickingGenQryVO1().clearCache();
      getPickingAMService().clearAssignedUserInPicking(uid);
      actionForwardStr = "GoPickingHome";
      hasNextPick = false;
    }
    return hasNextPick;
  }

  public void setSrlOrLotConNum(RichInputText srlOrLotConNum) {
    this.srlOrLotConNum = srlOrLotConNum;
  }

  public RichInputText getSrlOrLotConNum() {
    return srlOrLotConNum;
  }

  public void setSrlChoice(Object srlChoice) {
    this.srlChoice = srlChoice;
  }

  public Object getSrlChoice() {
    return srlChoice;
  }

  public void setExpObjBkp(Object expObjBkp) {
    this.expObjBkp = expObjBkp;
  }

  public Object getExpObjBkp() {
    return expObjBkp;
  }

  private static boolean isNumericAA(String s) {
    return s.matches("[0-9]+");
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

  public void setBypassList(RichSelectOneChoice bypass) {
    this.bypassList = bypass;
  }

  public RichSelectOneChoice getBypassList() {
    return bypassList;
  }
}
