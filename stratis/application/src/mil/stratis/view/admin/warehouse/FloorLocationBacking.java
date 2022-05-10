package mil.stratis.view.admin.warehouse;

import lombok.extern.slf4j.Slf4j;
import mil.stratis.common.util.RegUtils;
import mil.stratis.common.util.Util;
import mil.stratis.model.services.WarehouseSetupImpl;
import mil.stratis.view.admin.AdminBackingHandler;
import mil.stratis.view.util.ADFUtils;
import mil.stratis.view.util.ELUtils;
import mil.stratis.view.util.JSFUtils;
import oracle.adf.model.binding.DCIteratorBinding;
import oracle.adf.view.rich.component.rich.RichPopup;
import oracle.adf.view.rich.component.rich.data.RichTable;
import oracle.adf.view.rich.component.rich.input.RichInputText;
import oracle.adf.view.rich.component.rich.input.RichSelectManyShuttle;
import oracle.adf.view.rich.event.DialogEvent;
import oracle.binding.OperationBinding;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.myfaces.trinidad.context.RequestContext;

import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class FloorLocationBacking extends AdminBackingHandler {

  private static final String FLOOR_ITERATOR = "SingleFloorLocationView1Iterator";
  public static final String FLOOR_LOCATION_FLAG = "floorLocationFlag";

  //* FLOOR LOCATION VARIABLES
  private transient RichInputText floorLocation;
  private transient RichTable floorLocationTable;
  private boolean floorLocationFlag = false;
  private static final int MAX_LEN_FLOORLOCATION_NAME = 5;
  private String floorDefaultBuildings = "";
  private int floorDefaultBuildingsCOLS = 3;
  private String floorInUseBuildings = "";
  private boolean floorLocationRefresh = false;
  private List<String> selectedFLOORValues = new ArrayList<>();
  private List<SelectItem> allFLOORItems = new ArrayList<>();
  private transient RichSelectManyShuttle shuttleFLOORs;

  public FloorLocationBacking() {
    super();
    RequestContext afContext = RequestContext.getCurrentInstance();
    if (!afContext.isPostback()) {
      setInitialFloorLocationFlag();
    }
  }

  @SuppressWarnings("unused")
  public void submitCancelFloorLocation(ActionEvent event) {
    //NO-OP can't remove parameter due to adf
    resetFocusFloorLocation();
    cancel(FLOOR_ITERATOR);
    setFloorLocationFlag(true);

    setFloorLocationRefresh(true);
  }

  @SuppressWarnings("unused")
  public void submitUpdateFloorLocation(ActionEvent event) {
    //NO-OP can't remove parameter due to adf
    resetFocusFloorLocation();
    setFloorLocationFlag(false);
  }

  public void dialogListener(DialogEvent dialogEvent) {
    if (dialogEvent.getOutcome().equals(DialogEvent.Outcome.ok)) {
      RichPopup popup;
      submitDeleteFloorLocation(null);
      popup = (RichPopup) JSFUtils.findComponentInRoot("confirmDelete");
      popup.hide();
    }
  }

  @SuppressWarnings("unused")
  public void submitDeleteFloorLocation(ActionEvent event) {
    //NO-OP can't remove parameter due to adf
    resetFocusFloorLocation();

    try {
      String message;
      WarehouseSetupImpl service = getWarehouseSetupModule();
      if (service == null) {
        displayMessage("[MODULE] Error occurred while trying to delete ");
        return;
      }
      log.debug("delete this floor  {}", getFloorLocation().getValue().toString());
      message = service.deleteFloorLocationByName(getFloorLocation().getValue().toString());
      if (message.equals("")) {
        DCIteratorBinding iter = ADFUtils.findIterator(FLOOR_ITERATOR);
        iter.executeQuery();
        displaySuccessMessage("Floor Location deleted.");
      }
      else {
        JSFUtils.addFacesInformationMessage("Shipping Floor Location currently has assigned tasks and cannot be deleted.");
      }
    }
    catch (Exception e) {
      displayMessage("[ERROR] Error occurred while trying to delete ");
    }
  }

  @SuppressWarnings("unused")
  public void submitSaveFloorLocation(ActionEvent event) {
    //NO-OP can't remove parameter due to adf
    resetFocusFloorLocation();
    // check if the wac is in the warehouse selected if not give an error
    boolean error = (getFloorLocation().getValue() == null);
    String localFloorLocation = "";
    if (!error) {
      localFloorLocation = Util.trimUpperCaseClean(getFloorLocation().getValue());
      error = (localFloorLocation.isEmpty() || localFloorLocation.equalsIgnoreCase("null"));
    }

    if (error) {
      JSFUtils.addFacesErrorMessage("MISSING REQUIRED FIELD", "Floor Location is required.");
      setFocus(getFloorLocation());
      return;
    }

    if (localFloorLocation.length() != MAX_LEN_FLOORLOCATION_NAME) {
      JSFUtils.addFacesErrorMessage("INVALID FIELD", "Floor Location Name must be " + MAX_LEN_FLOORLOCATION_NAME + " alphanumeric characters");
      setFocus(getFloorLocation());
      return;
    }
    else if (RegUtils.isNotAlphaNumeric(localFloorLocation)) {
      JSFUtils.addFacesErrorMessage("INVALID FIELD", "Floor Location Name must be alphanumeric characters only");
      setFocus(getFloorLocation());
      return;
    }

    if ((selectedFLOORValues == null) || (selectedFLOORValues.isEmpty() && Util.isEmpty(getFloorInUseBuildings()))) {
      JSFUtils.addFacesErrorMessage("MISSING REQUIRED FIELD", "At least one Warehouse Building is required");
      getShuttleFLOORs().setInlineStyle("background-color:red;background-repeat:no-repeat;border-color:red;");
      return;
    }

    //* handle case requirements
    ELUtils.set("#{bindings.FloorLocation1.inputValue}", localFloorLocation);

    try {
      setFloorLocationFlag(true);

      WarehouseSetupImpl service = getWarehouseSetupModule();
      service.saveFloorLocation(localFloorLocation, selectedFLOORValues);
      displaySuccessMessage("Floor Location saved successfully.");
      setFloorLocationRefresh(false);
    }
    catch (Exception e) {
      setFloorLocationFlag(false);
      displayMessage("Floor Location error occurred.");
    }
  }

  public void resetFocusFloorLocation() {
    getFloorLocation().setInlineStyle(uppercase);
    getShuttleFLOORs().setInlineStyle("");
  }

  /**
   * This function was added to handle user's manual refresh.  The table will not
   * keep creating new rows while there is already a new row which is unsaved.
   * The function is used only to call the ExecuteQuery on the iterator;
   * it does not set any variables used by system.
   */
  @SuppressWarnings("unused")
  public void setFloorLocationRefresh(boolean refresh) {
    //* will clean up dirty created rows - also from a manual refresh
    OperationBinding op1 = ADFUtils.getDCBindingContainer().getOperationBinding("ExecuteFloorLocation");
    op1.execute();
  }

  public final void setInitialFloorLocationFlag() {
    RequestContext afContext = RequestContext.getCurrentInstance();
    Object obj = afContext.getPageFlowScope().get(FLOOR_LOCATION_FLAG);
    if (obj == null)
      setFloorLocationFlag(true);
  }

  /**
   * Set floor location flag back on (true) to turn on readonly+style
   * and hide certain ui buttons on Warehouse Setup - Floor Location.
   */
  public void setFloorLocationFlag(boolean show) {
    RequestContext afContext = RequestContext.getCurrentInstance();
    //* for use in the web, maintaining state
    afContext.getPageFlowScope().put(FLOOR_LOCATION_FLAG, show);
    this.floorLocationFlag = show;
  }

  public boolean getFloorLocationFlag() {
    RequestContext afContext = RequestContext.getCurrentInstance();
    boolean flag = floorLocationFlag;
    Object obj = afContext.getPageFlowScope().get(FLOOR_LOCATION_FLAG);
    if (obj != null)
      flag = Boolean.parseBoolean(obj.toString());

    return floorLocationFlag || flag;
  }

  public void setSelectedFLOORValues(List<String> selectedFLOORValues) {
    this.selectedFLOORValues = selectedFLOORValues;
  }

  public List<String> getSelectedFLOORValues() {
    // clear the list before use, then fill with list of String, not list of SelectItem

    if (CollectionUtils.isNotEmpty(selectedFLOORValues)) {
      selectedFLOORValues.clear();
    }
    else {
      selectedFLOORValues = new ArrayList<>();
    }

    List<String> nameList = new ArrayList<>();
    List<String> idList = new ArrayList<>();

    WarehouseSetupImpl service = getWarehouseSetupModule();

    if (service != null) {
      String floor = "";
      if (getFloorLocation() != null) {
        if (!isEmpty(getFloorLocation()))
          floor = getFloorLocation().getValue().toString().trim().toUpperCase();
        service.buildBuildingSelectedFilterList(nameList, idList, floor);
        selectedFLOORValues.addAll(idList);
      }
    }

    return selectedFLOORValues;
  }

  public void setAllFLOORItems(List<SelectItem> allFLOORItems) {
    this.allFLOORItems = allFLOORItems;
  }

  public List<SelectItem> getAllFLOORItems() {
    // clear the list before use, then fill
    allFLOORItems.clear();
    List<String> nameList = new ArrayList<>();
    List<String> idList = new ArrayList<>();

    WarehouseSetupImpl service = getWarehouseSetupModule();
    String floor = "";
    if (!isEmpty(getFloorLocation()))
      floor = getFloorLocation().getValue().toString().trim().toUpperCase();
    service.buildBuildingFilterList(nameList, idList, floor);

    for (int i = 0; i < nameList.size(); i++) {
      allFLOORItems.add(new SelectItem(idList.get(i), nameList.get(i)));
    }

    return allFLOORItems;
  }

  @SuppressWarnings("unused")
  public void refreshFLOORSelectedList(ValueChangeEvent event) {
    //NO-OP can't remove parameter due to adf
  }

  public void setFloorDefaultBuildings(String floorDefaultBuildings) {
    this.floorDefaultBuildings = floorDefaultBuildings;
  }

  public String getFloorDefaultBuildings() {
    List<String> nameList = new ArrayList<>();
    List<String> idList = new ArrayList<>();

    WarehouseSetupImpl service = getWarehouseSetupModule();
    String floor = "";
    if (!isEmpty(getFloorLocation()))
      floor = getFloorLocation().getValue().toString().trim().toUpperCase();
    service.buildBuildingSelectedFilterList(nameList, idList, floor);
    int i = 0;
    int numRows = 3;
    StringBuilder sb = new StringBuilder();
    for (String name : nameList) {
      if (i != 0)
        sb.append(", ");
      sb.append(name);

      i++;

      if (i > 14 && i % 15 == 0) numRows++;
    }

    //* set the size of the default buildings text area box
    floorDefaultBuildingsCOLS = numRows;
    floorDefaultBuildings = sb.toString();

    return floorDefaultBuildings;
  }

  public void setFloorInUseBuildings(String floorInUseBuildings) {
    this.floorInUseBuildings = floorInUseBuildings;
  }

  public String getFloorInUseBuildings() {
    List<String> nameList = new ArrayList<>();
    List<String> idList = new ArrayList<>();

    WarehouseSetupImpl service = getWarehouseSetupModule();
    String floor = "";
    if (!isEmpty(getFloorLocation()))
      floor = getFloorLocation().getValue().toString().trim().toUpperCase();
    service.buildBuildingInUseFilterList(nameList, idList, floor);
    int i = 0;
    StringBuilder sb = new StringBuilder();
    for (String name : nameList) {
      if (i != 0)
        sb.append(", ");
      sb.append(name);
      i++;
    }

    //* set the size of the default buildings text area box
    floorInUseBuildings = sb.toString();

    return floorInUseBuildings;
  }

  public void setDefaultFloorsCOLS(int floorDefaultBuildingsCOLS) {
    this.floorDefaultBuildingsCOLS = floorDefaultBuildingsCOLS;
  }

  public int getFloorDefaultBuildingsCOLS() {
    return floorDefaultBuildingsCOLS;
  }

  /************************************************************
   *
   * END FLOOR LOCATION
   * **********************************************************/
  public void setFloorLocation(RichInputText floorLocation) {
    this.floorLocation = floorLocation;
  }

  public RichInputText getFloorLocation() {
    return floorLocation;
  }

  public void setFloorLocationTable(RichTable floorLocationTable) {
    this.floorLocationTable = floorLocationTable;
  }

  public RichTable getFloorLocationTable() {
    return floorLocationTable;
  }

  @SuppressWarnings("unused") //called from .jspx
  public boolean isFloorLocationFlag() {
    return floorLocationFlag;
  }

  public void setFloorLocationRefresh1(boolean floorLocationRefresh) {
    this.floorLocationRefresh = floorLocationRefresh;
  }

  public boolean isFloorLocationRefresh() {
    return floorLocationRefresh;
  }

  public void setShuttleFLOORs(RichSelectManyShuttle shuttleFLOORs) {
    this.shuttleFLOORs = shuttleFLOORs;
  }

  public RichSelectManyShuttle getShuttleFLOORs() {
    return shuttleFLOORs;
  }
}
