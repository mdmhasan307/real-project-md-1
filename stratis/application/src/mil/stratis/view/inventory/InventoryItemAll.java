package mil.stratis.view.inventory;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.model.view.inv.SerialOrLotNumListInvImpl;
import mil.stratis.view.session.MdssBackingBean;
import mil.stratis.view.util.JSFUtils;
import mil.usmc.mls2.stratis.core.utility.AdfLogUtility;
import mil.usmc.mls2.stratis.core.utility.ContextUtils;
import mil.usmc.mls2.stratis.core.utility.LocationSortBuilder;
import oracle.adf.view.rich.component.rich.input.RichInputText;
import oracle.adf.view.rich.context.AdfFacesContext;
import oracle.jbo.Row;
import oracle.jbo.ViewObject;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import java.util.ArrayList;

@Slf4j
@NoArgsConstructor //ADF BackingHandlers all need a no args constructor
public class InventoryItemAll extends MdssBackingBean {

  private transient RichInputText scanLocLabel;
  private String actionForwardStr = "";
  private String errStr = "";
  private transient Object expObj = 0;
  private transient Object srlChoice = 0;
  private transient RichInputText enterNiinLastTwoDigits;
  private transient RichInputText enterQty;
  protected transient RichInputText reEnterQty;
  private transient RichInputText srlOrLotConNum;
  private transient RichInputText serialNumber;
  private transient RichInputText startSerial;
  private transient RichInputText endSerial;

  // listener for each time the page is loaded

  @Override
  public void onPageLoad() {

    if (!this.isPostback()) {
      init();
    }
    this.errStr = "";
  }

  // initial page loading function

  public void init() { // 0 is normal pick
    val sortBuilder = ContextUtils.getBean(LocationSortBuilder.class);
    val sortOrder = sortBuilder.getSortString(LocationSortBuilder.QueryType.ADF);

    getInventoryAMService().buildQueueRSForInventoryItem(
        Long.parseLong(JSFUtils.getManagedBeanValue("userbean.workstationId").toString()),
        Long.parseLong(JSFUtils.getManagedBeanValue("userbean.userId").toString()),
        true, sortOrder);
  }

  public void locationValidator(@SuppressWarnings("all") FacesContext facesContext,
                                UIComponent uiComponent, Object object) {
    try {

      String lL = getInventoryAMService().getInventoryItemVO1().getCurrentRow().getAttribute("LocationLabel").toString();
      String sL = "";
      boolean f = true;
      if (uiComponent.getId().compareTo("shelfLifeNormalCancelId") != 0) {
        if (object != null) {
          sL = object.toString();
        }
        else {
          f = false;
        }
        if (f && (sL.length() <= 0) || (sL.toUpperCase().trim().compareTo(lL.trim()) != 0)) {
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

  @SuppressWarnings("unused")  //called from .jspx
  public String actionForwardStrForInventoryItemNormalOnSubmit() {
    //Checks if there are any errors. If no errors forwards you to
    //NIIN verification else remains in same window for fixing.
    try {
      Object lL = this.getScanLocLabel().getValue();
      if ((lL == null) || (lL.toString().length() <= 8)) {
        errStr = errStr + "LOC_ERR_SZ";
        JSFUtils.addFacesErrorMessage("LOC_ERR_SZ", "Invalid location label size.");
      }
      Row ro;
      ro = getInventoryAMService().getInventoryItemVO1().getCurrentRow();
      if (errStr.length() <= 0) {
        //Inventory Item going through normal process
        //Inventory Item going through normal process
        //Logic check and prompt for old U/I during picking
        String rStr = this.getWorkloadManagerService().getUIDifferenceValues(Long.parseLong((ro.getAttribute("NiinLocId") == null) ? "0" : ro.getAttribute("NiinLocId").toString()));
        //For NSN Remark
        if ((ro.getAttribute("NsnRemark") != null) && (ro.getAttribute("NsnRemark").toString().equalsIgnoreCase("Y"))) {
          rStr = rStr + ", NEED_NSN_RELABEL";
        }

        if (rStr.compareToIgnoreCase("CONV_ERROR") == 0) {
          errStr = errStr + "CONV_ERROR";
          JSFUtils.addFacesErrorMessage("CONV_ERROR", "The U/I at this location is not re-packaged and STRATIS cannot guide you with the conversion. Please talk to supervisor.");
        }
        else if ((rStr.contains("NEED_REPACK")) || (rStr.contains("NEED_NSN_RELABEL")) || (rStr.contains("NEED_EXP_RELABEL"))) {
          if (rStr.contains("NEED_NSN_RELABEL")) {
            String fsc = "";
            String msgStr;
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
          }
          if (rStr.contains("NEED_REPACK")) {
            JSFUtils.addFacesInformationMessage(this.getWorkloadManagerService().buildDialogTxt(rStr, "Repack location inventory from :2 :1 to :4 :3"));
          }
        }
        actionForwardStr = "";
        this.setExpObj(1);
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

  public String actionForwardStrForInventoryItemNormalOnByPass() {
    //Checks if there are any Inventory Item and
    //takes you to next Inventory Item if any exist. Other comes out of Inventory Normal
    try {
      Row ro = getInventoryAMService().getInventoryItemVO1().getCurrentRow();
      if ((ro.getAttribute("BypassCount") == null) || (Integer.parseInt(ro.getAttribute("BypassCount").toString()) <= 0)) {
        if (getInventoryAMService().getInventoryItemVO1().hasNext()) {
          getInventoryAMService().getInventoryItemVO1().next();
          actionForwardStr = "";
          performCarouselMove(1);
        }
        else {
          getInventoryAMService().getInventoryItemVO1().clearCache();
          getInventoryAMService().clearAssignedUserInInventoryItems(getUserBeanUserId());
          actionForwardStr = "GoInventory";
        }
      }
      else {
        actionForwardStr = "";
      }
      this.setExpObj(0);
      this.scanLocLabel.setValue(null);
      errStr = "";
      this.enterNiinLastTwoDigits.setValue(null);
      return actionForwardStr;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return null;
  }

  public void niinLastTwoValidation(@SuppressWarnings("all") FacesContext facesContext,
                                    @SuppressWarnings("all") UIComponent uiComponent,
                                    Object object) {
    try {
      String lL = getInventoryAMService().getInventoryItemVO1().getCurrentRow().getAttribute("Niin").toString();
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
        JSFUtils.addFacesErrorMessage("NIIN_ERR", "Entered last two digits of NIIN do not match STRATIS provided NIIN. Please verify the NIIN you are checking.");
      }
      else {
        errStr = "";
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  @SuppressWarnings("unused")  //called from .jspx
  public void inventoryQtyValidation(@SuppressWarnings("all") FacesContext facesContext,
                                     @SuppressWarnings("all") UIComponent uiComponent,
                                     Object object) {
    try {
      boolean f = true;
      if (object == null) {
        f = false;
        if (errStr.contains("error"))
          errStr = "";
        errStr = errStr + "NIIN_ERR1";
        JSFUtils.addFacesErrorMessage("QTY_ERR1", "Please enter valid quantity.");
        this.getEnterQty().setValue(null);
      }
      if (f) {
        this.getEnterQty().setValue(object);
      }
      else {
        if (errStr.contains("error"))
          errStr = "";
      }
      this.setExpObj(2);
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  public void reEnteredQtyValidation(@SuppressWarnings("all") FacesContext facesContext,
                                     @SuppressWarnings("all") UIComponent uiComponent,
                                     Object object) {

    try {
      int rQty = 0;
      boolean f = true;
      val objStr = object != null ? object.toString() : null;
      if (objStr != null && objStr.length() > 0) {
        rQty = Integer.parseInt(objStr);
      }
      else {
        f = false;
        if (errStr.contains("error"))
          errStr = "";
        errStr = errStr + "NIIN_ERR1";
        JSFUtils.addFacesErrorMessage("QTY_ERR2", "Please enter valid reconfirm quantity.");
      }
      if (f) {
        this.getReEnterQty().setValue(object);
        if ((Integer.parseInt(this.getEnterQty().getValue().toString()) != rQty) && (errStr.length() <= 0)) {
          errStr = errStr + "NIIN_ERR1";
          JSFUtils.addFacesErrorMessage("QTY_ERR2", "Both quantites need to match.");
          this.getReEnterQty().setValue(null);
          this.getEnterQty().setValue(null);
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

  public String actionForwardStrForInventoryItemNormalNiinVerOnSubmit() {
    //Checks if there are any errors. If no errors forwards you to
    //NIIN verification else remains in same window for fixing.
    try {
      Object lL = this.getEnterNiinLastTwoDigits().getValue();
      if (lL == null) {
        errStr = "NIIN_ERR";
        JSFUtils.addFacesErrorMessage("NIIN_ERR", "Last two digits of NIIN required.");
      }
      if (errStr.length() <= 0) {
        actionForwardStr = "";
        this.setExpObj(2);
      }
      else
        actionForwardStr = "";
      errStr = "";

      this.scanLocLabel.setValue(null);
      this.enterNiinLastTwoDigits.setValue(null);
      this.enterQty.setValue(null);
      this.reEnterQty.setValue(null);
      this.setSrlChoice(0);
      return actionForwardStr;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return null;
  }

  @SuppressWarnings("unused")  //called from .jspx
  public String actionForwardStrForInventoryItemNormalQtyVerOnSubmit() {
    //Calls validations via value change listeners.
    //Logic check and launch for Inventory check, and
    //final save and forward to Next Pick
    try {
      int uid = Integer.parseInt(JSFUtils.getManagedBeanValue("userbean.userId").toString());
      Row ro = getInventoryAMService().getInventoryItemVO1().getCurrentRow();
      Object lQ = getEnterQty().getValue();
      String srlOrLotFlag = (((ro.getAttribute("SerialControlFlag") != null && ro.getAttribute("SerialControlFlag").toString().equalsIgnoreCase("Y")) || (ro.getAttribute("LotControlFlag") != null && ro.getAttribute("LotControlFlag").toString().equalsIgnoreCase("Y"))) ? "Y" : "N");
      if (lQ == null) {
        errStr = errStr + "QTY_ERR";
        JSFUtils.addFacesErrorMessage("QTY_ERR", "Missing Quantity.");
      }
      else
        validateSrl(lQ.toString());
      if ((errStr.length() <= 0) && ((Integer.parseInt(lQ.toString()) == Integer.parseInt((this.getReEnterQty().getValue() == null ? "-1" : this.getReEnterQty().getValue().toString()))) || (Integer.parseInt(lQ.toString()) == Integer.parseInt(ro.getAttribute("Qty").toString())))) { //Update the database pick qty and status
        this.getInventoryAMService().updateInventoryItem(Long.parseLong(ro.getAttribute("InventoryItemId").toString()), Integer.parseInt(JSFUtils.getManagedBeanValue("userbean.userId").toString()), Integer.parseInt(lQ.toString()), Integer.parseInt(ro.getAttribute("Qty").toString()), Integer.parseInt(ro.getAttribute("NiinId").toString()), srlOrLotFlag);

        // Close out any Error queue records for this InventoryItem
        this.getWorkloadManagerService().updateErrorQueueRecordStatus("INVENTORY_ITEM_ID", ro.getAttribute("InventoryItemId").toString(), uid);
        this.getInventoryAMService().getSerialOrLotNumListInv1().clearCache();
        if (getInventoryAMService().getInventoryItemVO1().hasNext()) {
          getInventoryAMService().getInventoryItemVO1().next();
          actionForwardStr = "";
          this.performCarouselMove(0);
        }
        else {
          getInventoryAMService().getInventoryItemVO1().clearCache();
          getInventoryAMService().clearAssignedUserInInventoryItems(getUserBeanUserId());
          actionForwardStr = "GoInventory";
        }
      }
      else {
        actionForwardStr = "";
        return actionForwardStr;
      }
      errStr = "";
      this.scanLocLabel.setValue(null);
      this.enterNiinLastTwoDigits.setValue(null);
      this.enterQty.setValue(null);
      this.reEnterQty.setValue(null);
      this.setExpObj(0);
      return actionForwardStr;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return "";
  }

  public void lookupNiins(@SuppressWarnings("all") ActionEvent event) {
    try {
      Row ro = getInventoryAMService().getInventoryItemVO1().getCurrentRow();
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

  @SuppressWarnings("unused")  //possibly called from .jspx
  public void niinOk(@SuppressWarnings("all") ActionEvent event) {
    //Close the dialog window that displays the Niin information based on the
    //location label
    AdfFacesContext.getCurrentInstance().returnFromDialog(null, null);
  }

  public void performCarouselMove(int flag) {
    //Call the carousel move
    //flag == 1 means that it is comming from cancel or bypass or refuse so goal is to just move on
    try {
      if (this.getWorkloadManagerService().isMechanized(getInventoryAMService().getInventoryItemVO1().getCurrentRow().getAttribute("WacId").toString())) {
        String mvCmdStr = "";
        String mvCmdStrFirst;
        mvCmdStrFirst = this.getWorkloadManagerService().performCarouselMove(getInventoryAMService().getInventoryItemVO1().getCurrentRow());

        if (flag == 1) {
          //Make the move based on the new stow which is going to start from location screen
          if (getInventoryAMService().getInventoryItemVO1().hasPrevious()) {
            getInventoryAMService().getInventoryItemVO1().next();
          }
          mvCmdStr = this.getWorkloadManagerService().performCarouselMove(getInventoryAMService().getInventoryItemVO1().getCurrentRow());
        }
        else { //Going to NIIN verification
          if (getInventoryAMService().getInventoryItemVO1().hasNext()) {
            //It it has next stow so get the side of that next stow
            String curSide = getInventoryAMService().getInventoryItemVO1().getCurrentRow().getAttribute("Side").toString();
            String nextSide = getInventoryAMService().getInventoryItemVO1().next().getAttribute("Side").toString();
            if (!curSide.equalsIgnoreCase(nextSide)) { //looks like the side are opposite
              //Get move command
              mvCmdStr = this.getWorkloadManagerService().performCarouselMove(getInventoryAMService().getInventoryItemVO1().getCurrentRow());
            }
            //Reset the pointer to previous stow becuase we moved it in the previous statement
            getInventoryAMService().getInventoryItemVO1().previous();
          }
        }
        mvCmdStr = mvCmdStrFirst + mvCmdStr;
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
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  public void performCarouselMoveOld(int flag) {
    //Call the carousel move
    //flag == 1 means that it is comming from cancel or bypass or refuse so goal is to just move on
    try {
      String mvCmdStr = "";
      if (flag == 1) {
        //Make the move based on the new stow which is going to start from location screen
        if (getInventoryAMService().getInventoryItemVO1().hasPrevious()) {
          getInventoryAMService().getInventoryItemVO1().next();
        }
        mvCmdStr = this.getWorkloadManagerService().performCarouselMove(getInventoryAMService().getInventoryItemVO1().getCurrentRow());
      }
      else { //Going to NIIN verification
        if (getInventoryAMService().getInventoryItemVO1().hasNext()) {
          //It it has next stow so get the side of that next stow
          String curSide = getInventoryAMService().getInventoryItemVO1().getCurrentRow().getAttribute("Side").toString();
          String nextSide = getInventoryAMService().getInventoryItemVO1().next().getAttribute("Side").toString();
          if (!curSide.equalsIgnoreCase(nextSide)) { //looks like the side are opposite
            //Get move command
            mvCmdStr = this.getWorkloadManagerService().performCarouselMove(getInventoryAMService().getInventoryItemVO1().getCurrentRow());
          }
          //Reset the pointer to previous stow becuase we moved it in the previous statement
          getInventoryAMService().getInventoryItemVO1().previous();
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

  public void setExpObj(Object expObj) {
    this.expObj = expObj;
  }

  public Object getExpObj() {
    return expObj;
  }

  public void setScanLocLabel(RichInputText scanLocLabel) {
    this.scanLocLabel = scanLocLabel;
  }

  public RichInputText getScanLocLabel() {
    return scanLocLabel;
  }

  public void setErrStr(String errStr) {
    this.errStr = errStr;
  }

  public String getErrStr() {
    return errStr;
  }

  public void setActionForwardStr(String actionForwardStr) {
    this.actionForwardStr = actionForwardStr;
  }

  public String getActionForwardStr() {
    return actionForwardStr;
  }

  public void setEnterNiinLastTwoDigits(RichInputText enterNiinLastTwoDigits) {
    this.enterNiinLastTwoDigits = enterNiinLastTwoDigits;
  }

  public RichInputText getEnterNiinLastTwoDigits() {
    return enterNiinLastTwoDigits;
  }

  public void setEnterQty(RichInputText enterQty) {
    this.enterQty = enterQty;
  }

  public RichInputText getEnterQty() {
    return enterQty;
  }

  public void setReEnterQty(RichInputText reEnterQty) {
    this.reEnterQty = reEnterQty;
  }

  public RichInputText getReEnterQty() {
    return reEnterQty;
  }

  public void setSrlChoiceVal(@SuppressWarnings("all") ActionEvent event) {
    // Set this switch based on the niin needing serial or lot con num
    Object lQ = getEnterQty().getValue();
    this.validateSrl((lQ == null ? "" : lQ.toString()));
  }

  public void validateSrl(String valStr) {
    // Set this switch based on the niin needing serial or lot con num
    try {
      Row ro = getInventoryAMService().getInventoryItemVO1().getCurrentRow();

      String err = "";
      String srlFlag = (ro.getAttribute("SerialControlFlag") == null ? "N" : ro.getAttribute("SerialControlFlag").toString());
      String lotFlag = (ro.getAttribute("LotControlFlag") == null ? "N" : ro.getAttribute("LotControlFlag").toString());
      if (valStr.length() > 0)
        err = getInventoryAMService().checkNiinNeedsAnySrlOrLotInfo(ro.getAttribute("NiinId").toString(), Integer.parseInt(valStr), srlFlag, lotFlag, ro.getAttribute("InventoryItemId").toString());
      if ((err.length() > 0) && (!err.contains("false"))) {
        if ((srlFlag.equalsIgnoreCase("Y")) && (lotFlag.equalsIgnoreCase("Y"))) {
          this.setSrlChoice(2);
        }
        else if (srlFlag.equalsIgnoreCase("Y")) {
          this.setSrlChoice(1);
        }
        else if (lotFlag.equalsIgnoreCase("Y"))
          this.setSrlChoice(3);
        if ((srlFlag.equalsIgnoreCase("Y")) || (lotFlag.equalsIgnoreCase("Y"))) {
          errStr = "SRL_LOT_ERR";
          JSFUtils.addFacesErrorMessage("SRL_LOT_ERR", err);
        }
      }
      else
        this.setSrlChoice(0);
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  public void setSrlChoice(Object srlChoice) {
    this.srlChoice = srlChoice;
  }

  public Object getSrlChoice() {
    return srlChoice;
  }

  public void deleteSrlLotNumFromList(@SuppressWarnings("all") ActionEvent event) {
    this.getInventoryAMService().getSerialOrLotNumListInv1().removeCurrentRowFromCollection();
  }

  public void addSrlLotNumList(@SuppressWarnings("all") ActionEvent event) {
    try {
      Row ro = getInventoryAMService().getInventoryItemVO1().getCurrentRow();
      String lQ = (getEnterQty().getValue() == null || getEnterQty().getValue().toString().length() <= 0) ? "" : getEnterQty().getValue().toString().trim();
      String kErr;
      String srlNum = (this.getSerialNumber().getValue() == null || this.getSerialNumber().getValue().toString().length() <= 0) ? "" : this.getSerialNumber().getValue().toString().toUpperCase().trim();
      ArrayList<String> serialList = new ArrayList<>();
      if ((srlNum.length() <= 0) && ((this.getStartSerial().getValue() == null) || (this.getEndSerial().getValue() == null))) {
        errStr = errStr + "SL_ERR";
        JSFUtils.addFacesErrorMessage("SL_ERR", "Please enter Serial and/or Lot Con Number.");
        return;
      }
      else
        errStr = "";

      if (srlNum.length() > 0)
        kErr = getInventoryAMService().addSerialOrLotNumVOList(srlNum, "", "1", ro.getAttribute("NiinId").toString(), lQ, ro.getAttribute("InventoryItemId").toString(), ro.getAttribute("LocationId").toString());
      else {
        String savekErr = "";
        kErr = getWorkloadManagerService().generateSerialRange(getStartSerial().getValue().toString(), getEndSerial().getValue().toString(), serialList);
        if (kErr.length() <= 0) {
          for (String s : serialList) {
            kErr = getInventoryAMService().addSerialOrLotNumVOList(s.trim().toUpperCase(), "", "1", ro.getAttribute("NiinId").toString(), lQ, ro.getAttribute("InventoryItemId").toString(), ro.getAttribute("LocationId").toString());
            if (!kErr.isEmpty()) {
              savekErr = kErr;
            }
          }
          this.getEndSerial().setValue("");
          this.getStartSerial().setValue("");
          kErr = savekErr;
        }
      }

      SerialOrLotNumListInvImpl vo = getInventoryAMService().getSerialOrLotNumListInv1();
      vo.setSortBy("SerialNumber");
      vo.setQueryMode(ViewObject.QUERY_MODE_SCAN_VIEW_ROWS);
      vo.executeQuery();

      if (kErr.length() > 0) {
        errStr = errStr + "SL_ERR";
        JSFUtils.addFacesErrorMessage("SL_ERR", kErr);
      }
      this.getSerialNumber().setValue("");
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  public void setSrlOrLotConNum(RichInputText srlOrLotConNum) {
    this.srlOrLotConNum = srlOrLotConNum;
  }

  public RichInputText getSrlOrLotConNum() {
    return srlOrLotConNum;
  }

  public void setSerialNumber(RichInputText serialNumber) {
    this.serialNumber = serialNumber;
  }

  public RichInputText getSerialNumber() {
    return serialNumber;
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
}
