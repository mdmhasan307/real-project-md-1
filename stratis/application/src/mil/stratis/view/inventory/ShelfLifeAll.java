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

@Slf4j
@NoArgsConstructor //ADF BackingHandlers all need a no args constructor
public class ShelfLifeAll extends MdssBackingBean {

  private transient RichInputText scanLocLabel;
  private String actionForwardStr = "";
  private String errStr = "error";
  private transient Object expObj = 0;

  // listener for each time the page is loaded

  @Override
  public void onPageLoad() {
    if (!this.isPostback()) {
      init();
    }
    this.errStr = "";
    JSFUtils.setManagedBeanValue("userbean.specialMessage", ""); //Reset informational message
  }

  // initial page loading function

  public void init() { // 0 is normal pick
    val sortBuilder = ContextUtils.getBean(LocationSortBuilder.class);
    val sortOrder = sortBuilder.getSortString(LocationSortBuilder.QueryType.ADF);
    //561 as sample
    getInventoryAMService().buildQueueRS(Long.parseLong(JSFUtils.getManagedBeanValue("userbean.workstationId").toString()), sortOrder);
  }

  public void setScanLocLabel(RichInputText scanLocLabel) {
    this.scanLocLabel = scanLocLabel;
  }

  public RichInputText getScanLocLabel() {
    return scanLocLabel;
  }

  public void locationValidator(@SuppressWarnings("all") FacesContext facesContext,
                                @SuppressWarnings("all") UIComponent uiComponent, Object object) {
    try {

      String lL = getInventoryAMService().getShelfLifeVO1().getCurrentRow().getAttribute("LocationLabel").toString();
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
  public String actionForwardStrForShelfLifeNormalOnSubmit() {
    //Checks if there are any errors. If no errors forwards you to
    //NIIN verification else remains in same window for fixing.
    try {
      Object lL = this.getScanLocLabel().getValue();
      if ((lL == null) || (lL.toString().length() <= 8)) {
        errStr = errStr + "LOC_ERR_SZ";
        JSFUtils.addFacesErrorMessage("LOC_ERR_SZ", "Invalid location label size.");
      }
      if (errStr.length() <= 0) {
        //Shelf life going through normal process
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

  /**
   * DESCRIPTION : Has the logic to move carousel to the next pick.
   */
  public void performCarouselMove(int flag) {
    //Call the carousel move
    //flag == 1 means that it is comming from cancel or bypass or refuse so goal is to just move on
    try {
      if (this.getWorkloadManagerService().isMechanized(getInventoryAMService().getShelfLifeVO1().getCurrentRow().getAttribute("WacId").toString())) {
        String mvCmdStr = "";
        if (flag == 1) {
          //Make the move based on the new stow which is going to start from location screen
          if (getInventoryAMService().getShelfLifeVO1().hasPrevious()) {
            getInventoryAMService().getShelfLifeVO1().next();
          }
          mvCmdStr = this.getWorkloadManagerService().performCarouselMove(getInventoryAMService().getShelfLifeVO1().getCurrentRow());
        }
        else { //Going to NIIN verification
          if (getInventoryAMService().getShelfLifeVO1().hasNext()) {
            //It it has next stow so get the side of that next stow
            String curSide = getInventoryAMService().getShelfLifeVO1().getCurrentRow().getAttribute("Side").toString();
            String nextSide = getInventoryAMService().getShelfLifeVO1().next().getAttribute("Side").toString();
            if (!curSide.equalsIgnoreCase(nextSide)) { //looks like the side are opposite
              //Get move command
              mvCmdStr = this.getWorkloadManagerService().performCarouselMove(getInventoryAMService().getShelfLifeVO1().getCurrentRow());
            }
            //Reset the pointer to previous stow becuase we moved it in the previous statement
            getInventoryAMService().getShelfLifeVO1().previous();
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
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  public String actionForwardStrForShelfLifeNormalOnCancel() {
    //Check if there is next other wise go home
    try {
      if (getInventoryAMService().getShelfLifeVO1().hasNext()) {
        getInventoryAMService().getShelfLifeVO1().next();
        this.setExpObj(0);
        actionForwardStr = "";
        performCarouselMove(1);
      }
      else {
        getInventoryAMService().getShelfLifeVO1().clearCache();
        this.setExpObj(0);
        actionForwardStr = "GoInventory";
      }
      errStr = "";
      this.scanLocLabel.setValue(null);
      return actionForwardStr;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return null;
  }

  @SuppressWarnings("unused")  //called from .jspx
  public String actionForwardStrForShelfLifeNormalOnException() {
    //Check if there is next other wise go home
    try {
      Row ro = getInventoryAMService().getShelfLifeVO1().getCurrentRow(); //46 is NSN_REMARK error in error table
      this.getWorkloadManagerService().createErrorQueueRecord("46", "NIIN_LOC_ID", ro.getAttribute("NiinLocId").toString(), JSFUtils.getManagedBeanValue("userbean.userId").toString(), null);
      if (getInventoryAMService().getShelfLifeVO1().hasNext()) {
        getInventoryAMService().getShelfLifeVO1().next();
        this.setExpObj(0);
        actionForwardStr = "";
        performCarouselMove(1);
      }
      else {
        getInventoryAMService().getShelfLifeVO1().clearCache();
        this.setExpObj(0);
        actionForwardStr = "GoInventory";
      }
      errStr = "";
      this.scanLocLabel.setValue(null);
      return actionForwardStr;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return null;
  }

  @SuppressWarnings("unused")  //called from .jspx
  public String actionForwardStrForShelfLifeNormalOnConfirm() {
    //Check if there is next other wise go home
    try {

      int uid = Integer.parseInt(JSFUtils.getManagedBeanValue("userbean.userId").toString());
      Row ro = getInventoryAMService().getShelfLifeVO1().getCurrentRow(); //Split YYYY-MM-DD
      val niinLocId = ro.getAttribute("NiinLocId").toString();
      errStr = this.getInventoryAMService().updateExpirationRemarkConfirm(niinLocId, ro.getAttribute("NewExpirationDate").toString().substring(0, 10), JSFUtils.getManagedBeanValue("userbean.userId").toString()); //46 is NSN_REMARK error in error table
      this.getWorkloadManagerService().deleteErrorQueueRecord("46", "NIIN_LOC_ID", niinLocId, uid);
      if (getInventoryAMService().getShelfLifeVO1().hasNext()) {
        getInventoryAMService().getShelfLifeVO1().next();
        this.setExpObj(0);
        actionForwardStr = "";
        performCarouselMove(1);
      }
      else {
        getInventoryAMService().getShelfLifeVO1().clearCache();
        this.setExpObj(0);
        actionForwardStr = "GoInventory";
      }
      errStr = "";
      this.scanLocLabel.setValue(null);
      return actionForwardStr;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return null;
  }

  public void setActionForwardStr(String actionForwardStr) {
    this.actionForwardStr = actionForwardStr;
  }

  public String getActionForwardStr() {
    return actionForwardStr;
  }

  public void setErrStr(String errStr) {
    this.errStr = errStr;
  }

  public String getErrStr() {
    return errStr;
  }

  public void setExpObj(Object expObj) {
    this.expObj = expObj;
  }

  public Object getExpObj() {
    return expObj;
  }
}
