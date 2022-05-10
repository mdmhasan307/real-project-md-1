package mil.stratis.view.admin;

import mil.stratis.common.util.Util;
import mil.stratis.model.services.SysAdminImpl;
import mil.stratis.view.util.ADFUtils;
import mil.stratis.view.util.JSFUtils;
import oracle.adf.model.binding.DCIteratorBinding;
import oracle.adf.view.rich.component.rich.data.RichTable;
import oracle.adf.view.rich.component.rich.input.RichInputText;
import oracle.adf.view.rich.component.rich.input.RichSelectBooleanCheckbox;
import oracle.adf.view.rich.component.rich.input.RichSelectOneChoice;
import oracle.jbo.Row;

import javax.faces.event.ActionEvent;

public class GCSSMCRecon extends AdminBackingHandler {

  int iUserId;

  private transient RichTable niinTable;
  private transient RichInputText txtImmedQtyTrans;
  private transient RichSelectOneChoice selectImmedQtyTrans;
  private transient RichSelectOneChoice selectImmedCCTrans;
  private transient RichSelectOneChoice selectGenTrans;
  private transient RichSelectBooleanCheckbox chkGenTrans;

  private static String niinInfoIterator = "NiinInfoView_CC_Change3Iterator";

  public GCSSMCRecon() {
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

  /**
   * Used to generate GCSS transactions without updating the NIIN_LOCATION qty
   * Only for non-controlled items.
   */
  public void submitImmedQtyTrans(ActionEvent event) {
    if (event != null) {
      //NO-OP can't remove parameter due to adf
    }
    boolean error = false;

    String dNumber = "8";
    String type = "Z";
    String transType = "";
    int qty = 0;

    //* Check all required fields first
    if (isEmpty(getTxtImmedQtyTrans())) {
      JSFUtils.addFacesErrorMessage("MISSING REQUIRED FIELD", "Immediate Quantity for Transaction is required.");
      error = true;
    }
    else if (isNaN(getTxtImmedQtyTrans())) {
      JSFUtils.addFacesErrorMessage("INVALID INPUT", "Immediate Quantity for Transaction must be a valid positive number greater than 0.");
      error = true;
    }

    if (!error) {
      qty = Util.cleanInt(getTxtImmedQtyTrans().getValue());
      if (qty <= 0) {
        JSFUtils.addFacesErrorMessage("INVALID INPUT", "Immediate Quantity for Transaction must be a valid positive number greater than 0.");
        error = true;
      }
    }

    if (isEmpty(getSelectImmedCCTrans())) {
      JSFUtils.addFacesErrorMessage("MISSING REQUIRED FIELD", "Immediate Condition Code must be selected (i.e., A or F)");
      error = true;
    }
    if (isEmpty(getSelectImmedQtyTrans())) {
      JSFUtils.addFacesErrorMessage("MISSING REQUIRED FIELD", "Immediate Transaction Type must be selected (e.g., D8Z)");
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

    Object niin = null;
    Integer niinId = null;
    Object lotControlFlag = null;
    Object serialFlag = null;

    //* do not allow serial and lot control
    //* maximum qty and locked or under_audit do not matter

    try {

      //* get the iterator used to populate the storage bin table
      DCIteratorBinding iter = ADFUtils.findIterator(niinInfoIterator);
      Row row = iter.getCurrentRow();
      niin = row.getAttribute("Niin");
      niinId = Integer.parseInt(row.getAttribute("NiinId").toString());

      serialFlag = row.getAttribute("SerialControlFlag");
      lotControlFlag = row.getAttribute("LotControlFlag");

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
    catch (Exception e) {
      displayMessage("[ERROR] Error occurred while trying to update ");

      return;
    }
  }

  public void submitImmedMassTrans(ActionEvent event) {
    if (event != null) {
      //NO-OP can't remove parameter due to adf
    }
    SysAdminImpl service = getSysAdminModule();
    service.generateMassD();
    displaySuccessMessage(" Transactions Creation Process Complete.");
  }

  public void setIUserId(int iUserId) {
    this.iUserId = iUserId;
  }

  public int getIUserId() {
    return iUserId;
  }

  public void setNiinTable(RichTable niinTable) {
    this.niinTable = niinTable;
  }

  public RichTable getNiinTable() {
    return niinTable;
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

  public static void setNiinInfoIterator(String niinInfoIterator) {
    GCSSMCRecon.niinInfoIterator = niinInfoIterator;
  }

  public static String getNiinInfoIterator() {
    return niinInfoIterator;
  }
}
