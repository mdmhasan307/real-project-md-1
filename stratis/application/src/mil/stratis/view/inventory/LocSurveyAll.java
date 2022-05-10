package mil.stratis.view.inventory;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.view.session.MdssBackingBean;
import mil.stratis.view.util.JSFUtils;
import mil.usmc.mls2.stratis.core.utility.AdfLogUtility;
import mil.usmc.mls2.stratis.core.utility.ContextUtils;
import mil.usmc.mls2.stratis.core.utility.LocationSortBuilder;
import oracle.adf.view.rich.component.rich.input.RichInputText;
import oracle.jbo.Row;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

@Slf4j
@NoArgsConstructor //ADF BackingHandlers all need a no args constructor
public class LocSurveyAll extends MdssBackingBean {

  private transient RichInputText scanLocLabel;
  private transient RichInputText scanNIINLabel;
  private String actionForwardStr = "";
  private String errStr = "";
  private transient Object expObj = 0;
  private transient String cc = "";
  private transient RichInputText enterNiinLastTwoDigits;
  private transient RichInputText enterQty;
  protected transient RichInputText reEnterQty;
  private transient Object curRowNl = null;
  private String hasNewNiin = "N";

  // listener for each time the page is loaded
  @Override
  public void onPageLoad() {

    if (!this.isPostback()) {
      init();
    }
    this.errStr = "";
  }

  // initial page loading function
  public void init() {
    val sortBuilder = ContextUtils.getBean(LocationSortBuilder.class);
    val sortOrder = sortBuilder.getSortString(LocationSortBuilder.QueryType.ADF);

    getInventoryAMService().buildQueueRSForLocSurvey(
        Long.parseLong(JSFUtils.getManagedBeanValue("userbean.workstationId").toString()),
        Long.parseLong(JSFUtils.getManagedBeanValue("userbean.userId").toString()),
        true, sortOrder);

    getInventoryAMService().clearNiinLocVOList();
    getInventoryAMService().clearNIINLocDetailsVOList();
    getInventoryAMService().getNIINLocDetailsVOList();
    this.setExpObj(0);
  }

  public void locationValidator(@SuppressWarnings("all") FacesContext facesContext,
                                UIComponent uiComponent, Object object) {
    try {
      String lL = getInventoryAMService().getLocSurveyVO1().getCurrentRow().getAttribute("LocationLabel").toString();
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
      else {errStr = "";}
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  @SuppressWarnings("unused")  //called from .jspx
  public String actionForwardStrForLocSurveyNormalOnSubmit() {
    //Checks if there are any errors. If no errors forwards you to
    //NIIN verification else remains in same window for fixing.
    try {
      Object lL = this.getScanLocLabel().getValue();
      if ((lL == null) || (lL.toString().length() <= 8)) {
        errStr = errStr + "LOC_ERR_SZ";
        JSFUtils.addFacesErrorMessage("LOC_ERR_SZ", "Invalid location label size.");
      }
      if (errStr.length() <= 0) {
        //Inventory Item going through normal process
        actionForwardStr = "";
        this.setExpObj(1);
        this.performCarouselMove(0);
      }
      else actionForwardStr = "";
      errStr = "";
      this.scanLocLabel.setValue(null);
      return actionForwardStr;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return null;
  }

  public String actionForwardStrForLocSurveyNormalOnByPass() {
    //Checks if there are any Inventory Item and
    //takes you to next Inventory Item if any exist. Other comes out of Inventory Normal
    try {
      Row ro = getInventoryAMService().getLocSurveyVO1().getCurrentRow();
      if ((ro.getAttribute("BypassCount") == null) ||
          (Integer.parseInt(ro.getAttribute("BypassCount").toString()) <= 0)) {
        if (getInventoryAMService().getLocSurveyVO1().hasNext()) {
          getInventoryAMService().getLocSurveyVO1().next();
          actionForwardStr = "";
          getInventoryAMService().clearNIINLocDetailsVOList();
          getInventoryAMService().getNIINLocDetailsVOList();
          performCarouselMove(1);
        }
        else {
          getInventoryAMService().getLocSurveyVO1().clearCache();
          getInventoryAMService().clearNIINLocDetailsVOList();
          getInventoryAMService().clearAssignedUserInInventoryItems(getUserBeanUserId());
          actionForwardStr = "GoInventory";
        }
      }
      else {
        actionForwardStr = "";
      }
      this.setExpObj(0);
      this.setCurRowNl(null);
      this.getScanNIINLabel().setValue("");
      this.setHasNewNiin("N");
      this.getInventoryAMService().clearNiinLocVOList();
      errStr = "";
      return actionForwardStr;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return null;
  }

  public void addScannedNIINToNiinLocList(@SuppressWarnings("all") ActionEvent event) {
    Row ro = getInventoryAMService().getLocSurveyVO1().getCurrentRow();

    if (this.getScanNIINLabel().getValue() == null) {
      errStr = errStr + "NIIN_ERR";
      JSFUtils.addFacesErrorMessage("NIIN_ERR", "Please enter NIIN.");
      return;
    }
    else errStr = "";

    int flag = getInventoryAMService().addNiinLocVOList(this.getScanNIINLabel().getLocalValue().toString().trim().toUpperCase(), ro.getAttribute("LocationLabel").toString());

    if (flag <= -2) {
      errStr = errStr + "NIIN_ERR";
      JSFUtils.addFacesErrorMessage("NIIN_ERR", "Invalid NIIN.");
    }
    else if (flag < 0) {
      errStr = errStr + "NIIN_ERR";
      JSFUtils.addFacesErrorMessage("NIIN_ERR", "NIIN non-existent within STRATIS. See Supervisor.");
    }
    else if (flag == 0) {
      errStr = errStr + "NIIN_ERR";
      JSFUtils.addFacesErrorMessage("NOTICE", "NIIN previously scanned in.");
    }
    else {
      this.getInventoryAMService().getNiinLocVO1();
      if ((flag == 2) && (this.getHasNewNiin().equalsIgnoreCase("N")))
        this.setHasNewNiin("Y");
    }
    this.getScanNIINLabel().setValue("");
  }

  @SuppressWarnings("unused")//(FUTURE) appears to truely be not called (even from .jspx) verify/remove in future
  public String actionForwardStrForLocSurveyNormalNewNIINOnSubmit() {
    //Calls validations via value change listeners.
    //Logic check and launch for Inventory check, and
    //final save and forward to Next Pick
    try {
      return actionForwardStr;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return "";
  }

  public String actionForwardForStartAddNewNIINs() {
    try {
      int stcount = getInventoryAMService().getNiinLocVO1().getRowCount();
      errStr = "";
      int en = 0;
      if (this.getHasNewNiin().equalsIgnoreCase("Y")) {
        if (stcount > 0) {
          this.setExpObj(2);
          if (this.curRowNl == null) {
            this.setCurRowNl(1);
          }
          else {
            if (getInventoryAMService().getNiinLocVO1().getCurrentRow().getAttribute("NewRow").toString().equalsIgnoreCase("Y"))
              en = this.validateCurrentNewNIIN();
            if ((en <= 0)) { //Go for next when validations succeed
              if (validateCC()) {
                this.setCurRowNl(Integer.parseInt(this.getCurRowNl().toString()) + 1);
              }
              else {
                errStr = errStr + "CC_MISMATCH";
                JSFUtils.addFacesErrorMessage("CC_MISTMATCH", "Condition Code Mismatch");
              }
            }
          }
        }
        else {
          this.setExpObj(3);
          return actionForwardStr;
        }
      }
      if (((stcount <= 0) || (this.getHasNewNiin().equalsIgnoreCase("N")) || ((Integer.parseInt(this.getCurRowNl().toString()) > stcount) ||
          (this.setNextEditableRow(Integer.parseInt(this.getCurRowNl().toString())) <= 0))) && (errStr.length() <= 0)) {
        cc = "";
        this.getInventoryAMService().processLocationSurvey(Integer.parseInt(JSFUtils.getManagedBeanValue("userbean.userId").toString()));
        this.getInventoryAMService().clearNiinLocVOList();
        if (getInventoryAMService().getLocSurveyVO1().hasNext()) {
          getInventoryAMService().getLocSurveyVO1().next();
          actionForwardStr = "";
          performCarouselMove(1);
          getInventoryAMService().clearNiinLocVOList();
          getInventoryAMService().clearNIINLocDetailsVOList();
          getInventoryAMService().getNIINLocDetailsVOList();
        }
        else {
          getInventoryAMService().getLocSurveyVO1().clearCache();
          getInventoryAMService().clearNIINLocDetailsVOList();
          getInventoryAMService().clearNiinLocVOList();
          getInventoryAMService().clearAssignedUserInInventoryItems(getUserBeanUserId());
          actionForwardStr = "GoInventory";
        }
        this.setExpObj(0);
        this.setCurRowNl(null);
        this.setHasNewNiin("N");
        errStr = "";
      }
      else {
        actionForwardStr = "";
      }
      return actionForwardStr;
    }
    catch (
        Exception e) {
      AdfLogUtility.logException(e);
    }
    return "";
  }

  public String actionForwardForGoBackToNIINList() {
    try {
      this.setExpObj(1);
      this.setCurRowNl(null);
      actionForwardStr = "";
      return actionForwardStr;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return "";
  }

  @SuppressWarnings("unused")  //called from .jspx
  public String actionForwardForDeleteNIIN() {
    try {
      getInventoryAMService().getNiinLocVO1().removeCurrentRow();
      this.setCurRowNl(null);
      if (this.setNextEditableRow(1) <= 0)
        actionForwardStr = this.actionForwardForGoBackToNIINList();
      else
        actionForwardStr = this.actionForwardForStartAddNewNIINs();
      return actionForwardStr;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return "";
  }

  @SuppressWarnings("unused")  //called from .jspx
  public String actionForwardStrForResetExp() {
    try {
      this.setExpObj(1);
      return actionForwardStr;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return "";
  }

  @SuppressWarnings("unused")  //called from .jspx
  public String actionForwardStrForLocationEmpty() {

    try {
      errStr = "";
      this.getInventoryAMService().processLocationSurvey(Integer.parseInt(JSFUtils.getManagedBeanValue("userbean.userId").toString()));
      this.getInventoryAMService().clearNiinLocVOList();
      if (getInventoryAMService().getLocSurveyVO1().hasNext()) {
        getInventoryAMService().getLocSurveyVO1().next();
        actionForwardStr = "";
        performCarouselMove(1);
        getInventoryAMService().clearNiinLocVOList();
        getInventoryAMService().clearNIINLocDetailsVOList();
        getInventoryAMService().getNIINLocDetailsVOList();
      }
      else {
        getInventoryAMService().getLocSurveyVO1().clearCache();
        getInventoryAMService().clearNiinLocVOList();
        getInventoryAMService().clearNIINLocDetailsVOList();
        getInventoryAMService().clearAssignedUserInInventoryItems(getUserBeanUserId());
        actionForwardStr = "GoInventory";
      }
      this.setExpObj(0);
      this.setCurRowNl(null);
      errStr = "";
      return actionForwardStr;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return "";
  }

  public boolean hasNewNIINInTheList() {
    try {
      boolean newNiin = false;
      Row r1 = this.getInventoryAMService().getNiinLocVO1().first();
      if (r1 != null && r1.getAttribute("NewRow").toString().equalsIgnoreCase("Y"))
        newNiin = true;
      while ((this.getInventoryAMService().getNiinLocVO1().hasNext()) && (!newNiin)) {
        r1 = this.getInventoryAMService().getNiinLocVO1().next();
        if (r1 != null && r1.getAttribute("NewRow").toString().equalsIgnoreCase("Y"))
          newNiin = true;
      }
      return newNiin;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return false;
  }

  public void performCarouselMove(int flag) {
    //Call the carousel move
    //flag == 1 means that it is comming from cancel or bypass or refuse so goal is to just move on
    try {
      if (this.getWorkloadManagerService().isMechanized(getInventoryAMService().getLocSurveyVO1().getCurrentRow().getAttribute("WacId").toString())) {
        String mvCmdStr = "";
        String mvCmdStrFirst;
        mvCmdStrFirst = this.getWorkloadManagerService().performCarouselMove(getInventoryAMService().getLocSurveyVO1().getCurrentRow());

        //Make the move based on the new stow which is going to start from location screen
        if (flag == 1)
          mvCmdStr = this.getWorkloadManagerService().performCarouselMove(getInventoryAMService().getLocSurveyVO1().getCurrentRow());
        else { //Going to NIIN verification
          if (getInventoryAMService().getLocSurveyVO1().hasNext()) {
            //It it has next stow so get the side of that next stow
            String curSide = getInventoryAMService().getLocSurveyVO1().getCurrentRow().getAttribute("Side").toString();
            String nextSide = getInventoryAMService().getLocSurveyVO1().next().getAttribute("Side").toString();
            if (!curSide.equalsIgnoreCase(nextSide)) { //looks like the side are opposite
              //Get move command
              mvCmdStr = this.getWorkloadManagerService().performCarouselMove(getInventoryAMService().getLocSurveyVO1().getCurrentRow());
            }
            //Reset the pointer to previous stow because we moved it in the previous statement
            getInventoryAMService().getLocSurveyVO1().previous();
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

  /**
   * DESCRIPTION : Has the logic to move carousel to the next Inventory.
   */
  public void performCarouselMoveOld(int flag) {
    //Call the carousel move
    //flag == 1 means that it is comming from cancel or bypass or refuse so goal is to just move on
    try {
      String mvCmdStr = "";
      if (flag == 1) {
        //Make the move based on the new stow which is going to start from location screen
        if (getInventoryAMService().getLocSurveyVO1().hasPrevious()) {
          getInventoryAMService().getLocSurveyVO1().next();
        }
        mvCmdStr = this.getWorkloadManagerService().performCarouselMove(getInventoryAMService().getLocSurveyVO1().getCurrentRow());
      }
      else { //Going to NIIN verification
        if (getInventoryAMService().getLocSurveyVO1().hasNext()) {
          //It it has next stow so get the side of that next stow
          String curSide = getInventoryAMService().getLocSurveyVO1().getCurrentRow().getAttribute("Side").toString();
          String nextSide = getInventoryAMService().getLocSurveyVO1().next().getAttribute("Side").toString();
          if (!curSide.equalsIgnoreCase(nextSide)) { //looks like the side are opposite
            //Get move command
            mvCmdStr = this.getWorkloadManagerService().performCarouselMove(getInventoryAMService().getLocSurveyVO1().getCurrentRow());
          }
          //Reset the pointer to previous stow becuase we moved it in the previous statement
          getInventoryAMService().getLocSurveyVO1().previous();
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

  public int setNextEditableRow(int pos) {
    if (pos == 1) {
      getInventoryAMService().getNiinLocVO1().first();
      return 1;
    }
    else {
      if (getInventoryAMService().getNiinLocVO1().hasNext()) {
        getInventoryAMService().getNiinLocVO1().next();
        return 1;
      }
    }
    return 0;
  }

  public int validateCurrentNewNIIN() {
    Row r = getInventoryAMService().getNiinLocVO1().getCurrentRow();
    r.setAttribute("Qty", (r.getAttribute("Qty") == null ? "0" : r.getAttribute("Qty").toString()));

    if ((r.getAttribute("Cc") == null) ||
        (!(r.getAttribute("Cc").toString().equalsIgnoreCase("A") || r.getAttribute("Cc").toString().equalsIgnoreCase("F")))) {
      errStr = errStr + "NIIN_CC_ERR";
      JSFUtils.addFacesErrorMessage("NIIN_CC_ERR", "Invalid NIIN CC.");
    }
    if (((r.getAttribute("ExpirationDate") == null) || (!isNumericAA(r.getAttribute("ExpirationDate").toString()))) ||
        (r.getAttribute("ExpirationDate").toString().length() != 6)) {
      errStr = errStr + "NIIN_EXP_DT_ERR";
      JSFUtils.addFacesErrorMessage("NIIN_EXP_DT_ERR", "Invalid NIIN Expiration.");
    }
    else {
      if (((Integer.parseInt(r.getAttribute("ExpirationDate").toString().substring(0, 2)) > 12 ||
          Integer.parseInt(r.getAttribute("ExpirationDate").toString().substring(0, 2)) <= 0) ||
          (Integer.parseInt(r.getAttribute("ExpirationDate").toString().substring(2, 6)) < 2000))
      ) {
        errStr = errStr + "NIIN_EXP_DT_ERR";
        JSFUtils.addFacesErrorMessage("NIIN_EXP_DT_ERR", "Invalid NIIN Expiration.");
      }
    }

    if (errStr != null && errStr.length() > 0) return 1;
    else return 0;
  }

  //MCF 07/09/2014: Added new check to CC. Basically, all condition codes for NIINs on a location need to be the same.
  public boolean validateCC() {
    Row r = getInventoryAMService().getNiinLocVO1().getCurrentRow();
    if (r.getAttribute("Cc") != null) {
      if (cc.isEmpty()) {
        cc = r.getAttribute("Cc").toString().trim();
        return true;
      }
      else {
        return cc.equals(r.getAttribute("Cc").toString().trim());
      }
    }
    return false;
  }

  private static boolean isNumericAA(String s) {
    if (s == null || s.length() <= 0) return false;
    return s.matches("[0-9]+");
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

  public void setScanNIINLabel(RichInputText scanNIINLabel) {
    this.scanNIINLabel = scanNIINLabel;
  }

  public RichInputText getScanNIINLabel() {
    return scanNIINLabel;
  }

  public void setCurRowNl(Object curRowNl) {
    this.curRowNl = curRowNl;
  }

  public Object getCurRowNl() {
    return curRowNl;
  }

  public void setHasNewNiin(String hasNewNiin) {
    this.hasNewNiin = hasNewNiin;
  }

  public String getHasNewNiin() {
    return hasNewNiin;
  }
}
