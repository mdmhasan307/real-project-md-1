package mil.stratis.view.admin.warehouse;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.common.util.RegUtils;
import mil.stratis.common.util.Util;
import mil.stratis.model.services.SysAdminImpl;
import mil.stratis.model.services.WorkLoadManagerImpl;
import mil.stratis.view.session.MdssBackingBean;
import mil.stratis.view.util.ADFUtils;
import mil.stratis.view.util.JSFUtils;
import mil.usmc.mls2.stratis.core.utility.AdfLogUtility;
import mil.usmc.mls2.stratis.core.utility.ContextUtils;
import mil.usmc.mls2.stratis.core.utility.GlobalConstants;
import oracle.adf.model.binding.DCIteratorBinding;
import oracle.adf.view.rich.component.rich.data.RichTable;
import oracle.adf.view.rich.component.rich.input.RichInputText;
import oracle.adf.view.rich.component.rich.nav.RichButton;
import oracle.jbo.Row;
import org.apache.myfaces.trinidad.event.SortEvent;
import org.apache.myfaces.trinidad.model.SortCriterion;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import java.util.List;

@Slf4j
@NoArgsConstructor
public class RewarehouseBacking extends MdssBackingBean {

  private transient RichInputText txtNIINSearch;
  private transient RichInputText txtLocTo;
  private transient RichInputText txtWacTo;
  private transient RichButton btnRewarehouseLoc;
  private transient RichButton btnRewarehouseWac;
  private transient RichButton btnCancel;

  private transient RichTable fromTable;

  @Override
  public void onPageLoad() {
    if (!this.isPostback()) {
      enableOrDisableButtons(true);
      if (getTxtNIINSearch() != null) {
        getTxtNIINSearch().setValue("");
      }
    }
  }

  @SuppressWarnings("unused")
  public void findRewarehouseLocs(ActionEvent actionEvent) {
    enableOrDisableButtons(true);
    populateTable(null);
  }

  @SuppressWarnings("unused")
  public void tableSortListener(SortEvent sortEvent) {
    List<SortCriterion> activeSortCriteria = sortEvent.getSortCriteria();
    SortCriterion sc = activeSortCriteria.get(0);
    populateTable(sc);
  }

  private void populateTable(SortCriterion sort) {
    SysAdminImpl service = getSysAdminModule();
    String niin = Util.trimUpperCaseClean(getInputTextValue(getTxtNIINSearch()));
    if (RegUtils.isNotAlphaNumeric(niin)) {
      val globalConstants = ContextUtils.getBean(GlobalConstants.class);
      JSFUtils.addFacesErrorMessage(globalConstants.getInvalidFieldTag(), "NIIN field to filter on must be alphanumeric only.");
      return;
    }
    if (sort != null) {
      service.getRewarehouseLocations(niin, sort.getProperty(), sort.isAscending());
    }
    else {
      service.getRewarehouseLocations(niin, null, false);
    }
  }

  /**
   * Rewarehouse by Location.
   */
  @SuppressWarnings("unused") //called from .jspx
  public void rewarehouseLoc(ActionEvent actionEvent) {
    val globalConstants = ContextUtils.getBean(GlobalConstants.class);
    if (Util.isEmpty(getInputTextValue(getTxtLocTo()))) {
      JSFUtils.addFacesErrorMessage("REQUIRED FIELD", "Location to Rewarehouse To is a required field.");
      return;
    }
    else {
      String locStr = Util.cleanString(getInputTextValue(getTxtLocTo()));
      if (locStr.length() != 9) {
        JSFUtils.addFacesErrorMessage("INVALID LENGTH", "Location to Rewarehouse To must be 9 characters");
        return;
      }

      if (RegUtils.isNotAlphaNumeric(locStr)) {
        JSFUtils.addFacesErrorMessage(globalConstants.getInvalidFieldTag(), "Location to Rewarehouse To must be alphanumeric characters only");
        return;
      }
    }

    completeRewarehouse(true);
  }

  /**
   * This method will complete the rewarehouse process based on
   * whether the rewarehouse is by wac or by location.
   */
  private void completeRewarehouse(boolean byLocation) {
    String locTo = Util.trimUpperCaseClean(getInputTextValue(getTxtLocTo()));
    String wac = Util.trimUpperCaseClean(getTxtWacTo().getValue());
    int iUserId = Integer.parseInt((JSFUtils.getManagedBeanValue("userbean.userId")).toString());

    Row row;
    String locFrom;
    try {
      DCIteratorBinding containerIter = ADFUtils.findIterator("RewarehouseTempView1Iterator");
      row = containerIter.getCurrentRow();

      locFrom = (row.getAttribute("Location")).toString().trim().toUpperCase();
    }
    catch (Exception e) {
      String message = "Please select a location to rewarehouse.";
      this.displayMessage(new StringBuilder(message), 0);
      return;
    }
    int quantity = Util.cleanInt(row.getAttribute("InputQTY"));
    int quantityAtLocation = Util.cleanInt(row.getAttribute("LocationQTY"));

    //* validate the rewarehouse input
    if (byLocation && locTo.equalsIgnoreCase(locFrom)) {
      String message = "FROM and TO Location cannot be the same.";
      this.displayMessage(new StringBuilder(message), 0);
      return;
    }

    if (quantity < 1) {
      String message = "There is no quantity entered for the selected row";
      this.displayMessage(new StringBuilder(message), 0);
      return;
    }

    if (quantity > quantityAtLocation) {
      String message = "Quantity exceeds the quantity available at the location";
      this.displayMessage(new StringBuilder(message), 0);
      return;
    }

    int niinLocId = Integer.parseInt((row.getAttribute("NiinLocId")).toString());
    int niinId = Integer.parseInt((row.getAttribute("NiinId")).toString());

    try {
      SysAdminImpl service = getSysAdminModule();
      //* added 01/28/09 to support do not allow rewarehouse to locked locations
      if (service.isNIINLocationLocked(niinLocId)) {
        this.displayMessage(new StringBuilder("Unable to rewarehouse due to locked location. Try again later."), 0);
        return;
      }

      //* if either the receipt or the stow row creation fail, display error
      int rcn = service.createReceiptRow(niinId, quantity, iUserId, (row.getAttribute("LocationCC")).toString());
      if (rcn < 0) {
        this.displayMessage(new StringBuilder("An error occured when creating Receipt.  Please see logs."), 0);
        return;
      }

      WorkLoadManagerImpl service2 = getWorkloadManagerModule();
      int stowId = service.createStowRow(niinLocId, niinId, rcn, iUserId);
      if (stowId < 0) {
        service2.deleteLocation(rcn, stowId);
        this.displayMessage(new StringBuilder("An error occurred while creating Stow.  Please see logs."), 0);
        return;
      }

      String messageStr;

      messageStr = service2.buildLocationList((byLocation) ? locTo : wac, "STOW_ID", String.valueOf(stowId), "ALL", -1);

      if (messageStr.equals("") || messageStr.contains("NO_LOC_MSG Mechanized and Bulk locations are all occupied.") || messageStr.contains("NO_LOC_MSG Mechanized locations are all occupied.") || messageStr.contains("NO_LOC_MSG Bulk locations are all occupied.")) {
        int locId = service2.getBestLocId(locFrom);

        if (locId > 0) {
          // int, int, int, int, int, String, String, int
          if (!service.generateRewarehouse(niinLocId, stowId, quantity, niinId, locId, rcn, (row.getAttribute("LocationCC")).toString(), iUserId)) {
            service2.deleteLocation(rcn, stowId);
            String message = "Rewarehouse Failed";
            this.displayMessage(new StringBuilder(message), 0);
          }
          else {
            String message = "Rewarehouse Succeeded - SID " + service.getSID(stowId);
            this.displayMessage(new StringBuilder(message), 1);

            enableOrDisableButtons(true);
            getTxtLocTo().setValue("");
            getTxtWacTo().setValue("");
          }
        }
        else {
          service2.deleteLocation(rcn, stowId);
          String message;
          if (byLocation)
            message = "Invalid location. Please enter another location.";
          else
            message = "No valid location could be found in the WAC";
          this.displayMessage(new StringBuilder(message), 0);
        }
      }
      else {
        service2.deleteLocation(rcn, stowId);
        this.displayMessage(new StringBuilder(messageStr), 0);
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }

    //* refresh the rewarehouse from list
    findRewarehouseLocs(null);
  }

  /**
   * Rewarehouse by WAC.
   */
  @SuppressWarnings("unused") //called from .jspx
  public void rewarehouseWac(ActionEvent actionEvent) {
    if (Util.isEmpty(getTxtWacTo().getValue())) {
      JSFUtils.addFacesErrorMessage("REQUIRED FIELD", "WAC to Rewarehouse To is a required field.");
      return;
    }
    else {
      String wacStr = Util.cleanString(getTxtWacTo().getValue());
      if (wacStr.length() != 3) {
        JSFUtils.addFacesErrorMessage("INVALID LENGTH", "WAC to Rewarehouse To must be 3 characters");
        return;
      }

      if (RegUtils.isNotAlphaNumeric(wacStr)) {
        JSFUtils.addFacesErrorMessage("INVALID FIELD", "WAC to Rewarehouse To must be alphanumeric characters only");
        return;
      }
    }

    completeRewarehouse(false);
  }

  /**
   * Enable (true) or Disable (false) the buttons to signify a 'rewarehouse location from'
   * selection has yet to be made.
   */
  @SuppressWarnings("unused")
  public void enableOrDisableButtons(boolean state) {
    //NO-OP
  }

  private String getInputTextValue(RichInputText comp) {
    String value = null;
    if (comp != null) {
      value = (String) comp.getValue();
    }
    return value;
  }

  /**
   * Cancel the rewarehouse process (prior to submitting).
   */
  @SuppressWarnings("unused")
  public void cancelButton(ActionEvent actionEvent) {
    enableOrDisableButtons(true);
  }

  /**
   * Display Message to the user.
   */
  public void displayMessage(StringBuilder msgOutput, int flag) {
    /* display message to GUI */
    FacesMessage msg = new FacesMessage();
    if (msgOutput.length() > 0) {
      if (flag == 0) {
        msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, msgOutput.toString(), null);
      }
      else if (flag == 1) {
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, msgOutput.toString(), null);
      }
      FacesContext facesContext = FacesContext.getCurrentInstance();
      facesContext.addMessage(null, msg);
    }
  }

  @SuppressWarnings("unused") //called from .jspx
  public void setTxtNIINSearch(RichInputText txtNIINSearch) {
    this.txtNIINSearch = txtNIINSearch;
  }

  public RichInputText getTxtNIINSearch() {
    return txtNIINSearch;
  }

  public void setTxtLocTo(RichInputText txtLocTo) {
    this.txtLocTo = txtLocTo;
  }

  public RichInputText getTxtLocTo() {
    return txtLocTo;
  }

  public void setTxtWacTo(RichInputText txtWacTo) {
    this.txtWacTo = txtWacTo;
  }

  public RichInputText getTxtWacTo() {
    return txtWacTo;
  }

  public void setBtnRewarehouseLoc(RichButton btnRewarehouseLoc) {
    this.btnRewarehouseLoc = btnRewarehouseLoc;
  }

  public RichButton getBtnRewarehouseLoc() {
    return btnRewarehouseLoc;
  }

  public void setBtnRewarehouseWac(RichButton btnRewarehouseWac) {
    this.btnRewarehouseWac = btnRewarehouseWac;
  }

  public RichButton getBtnRewarehouseWac() {
    return btnRewarehouseWac;
  }

  public void setBtnCancel(RichButton btnCancel) {
    this.btnCancel = btnCancel;
  }

  public RichButton getBtnCancel() {
    return btnCancel;
  }

  public void setFromTable(RichTable fromTable) {
    this.fromTable = fromTable;
  }

  public RichTable getFromTable() {
    return fromTable;
  }
}
