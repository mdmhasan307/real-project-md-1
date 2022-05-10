package mil.stratis.view.admin.warehouse;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.common.util.RegUtils;
import mil.stratis.common.util.Util;
import mil.stratis.model.services.AppModuleImpl;
import mil.stratis.model.services.WarehouseSetupImpl;
import mil.stratis.model.services.WorkLoadManagerImpl;
import mil.stratis.model.view.loc.TempMechCreateRowsImpl;
import mil.stratis.view.admin.AdminBackingHandler;
import mil.stratis.view.util.ADFUtils;
import mil.stratis.view.util.ELUtils;
import mil.stratis.view.util.JSFUtils;
import mil.usmc.mls2.stratis.core.utility.AdfLogUtility;
import mil.usmc.mls2.stratis.core.utility.ContextUtils;
import mil.usmc.mls2.stratis.core.utility.GlobalConstants;
import oracle.adf.model.binding.DCIteratorBinding;
import oracle.adf.view.rich.component.rich.RichPopup;
import oracle.adf.view.rich.component.rich.data.RichTable;
import oracle.adf.view.rich.component.rich.input.RichInputText;
import oracle.adf.view.rich.component.rich.input.RichSelectManyShuttle;
import oracle.adf.view.rich.component.rich.input.RichSelectOneChoice;
import oracle.adf.view.rich.context.AdfFacesContext;
import oracle.adf.view.rich.event.DialogEvent;
import oracle.binding.OperationBinding;
import oracle.jbo.Row;
import oracle.jbo.uicli.binding.JUCtrlValueBindingRef;
import org.apache.myfaces.trinidad.context.RequestContext;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Slf4j
public class WarehouseSetupBacking extends AdminBackingHandler {

  private static final int MIN_LEN_SHIPPING_ROUTE_NAME = 1;
  private static final int MAX_LEN_ROUTE_NAME = 3;
  public static final String ROUTE_ID = "RouteId";
  public static final String BORDER_COLOR_RED = "border-color:red;";
  public static final String TEXT_TRANSFORM_UPPERCASE = "text-transform:uppercase;";
  public static final String INVALID_FIELD = "INVALID FIELD";
  public static final String ROUTE_FLAG = "routeFlag";
  public static final String READ_ONLY_FIELD = "ReadOnlyField";
  public static final String SHIPPING_ROUTE_FLAG = "shippingRouteFlag";

  int iUserId;

  //* MECHANIZED (CAROUSEL) VARIABLES
  private transient RichSelectOneChoice sideHorizontal;
  private transient RichSelectOneChoice wacIdHorizontal;
  private transient RichInputText baysHorizontal;
  private transient RichSelectOneChoice wacIdVertical;
  private transient RichSelectOneChoice binTypeHorizontal;
  private transient RichSelectOneChoice binTypeVertical;

  //* NON MECH (BULK) LOCATION VARIABLES
  private transient RichTable nonMechLocationTable;
  private transient RichInputText bulkAddRow;
  private transient RichInputText bulkAddLevel;
  private transient RichInputText bulkAddShelfGroup;
  private transient RichInputText bulkAddLocationsPerShelf;
  private transient RichInputText bulkLocation;
  private transient RichSelectOneChoice bulkAddWAC;

  //* ROUTE VARIABLES
  private transient RichTable routeTable;
  private boolean routeFlag = false;
  private boolean routeRefresh = false;
  private transient RichInputText routeName;
  private transient RichInputText routeDescription;

  //* SHIPPING ROUTE VARIABLES
  private transient RichTable shippingRouteTable;
  private boolean shippingRouteFlag = false;
  private boolean shippingRouteRefresh = false;
  private transient RichInputText shippingRouteName;
  private transient RichInputText shippingRouteDescription;
  private transient RichSelectManyShuttle shuttleFLOORs;

  public WarehouseSetupBacking() {

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

    //* required to show initial route update screen with grey, read only and buttons undisabled
    RequestContext afContext = RequestContext.getCurrentInstance();
    if (!afContext.isPostback()) {
      setInitialRouteFlag();
      setInitialShippingRouteFlag();
    }
  }

  //* Bulk (Non-Mechanized) Locations
  // function to create new bulk locations
  @SuppressWarnings("unused")  //called from .jspx
  public void createBulkLocations(ActionEvent event) {
    resetFocusNonMech();
    boolean createLocations = true;
    val globalConstants = ContextUtils.getBean(GlobalConstants.class);

    if (bulkAddRow.getValue() == null) {
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "Row is required.");
      setFocus(getBulkAddRow());
      createLocations = false;
    }

    if (bulkAddLevel.getValue() == null) {
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "Level is required.");
      setFocus(getBulkAddLevel());
      createLocations = false;
    }

    if (bulkAddShelfGroup.getValue() == null) {
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "Shelf Group is required.");
      setFocus(getBulkAddShelfGroup());
      createLocations = false;
    }

    if (bulkAddLocationsPerShelf.getValue() == null) {
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "Locations Per Shelf is required.");
      setFocus(getBulkAddLocationsPerShelf());
      createLocations = false;
    }

    int wacId = 0;
    if (bulkAddWAC.getValue() == null) {
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "WAC is required.");
      setFocus(getBulkAddWAC());
      createLocations = false;
    }
    else {
      Object bulkwac = bulkAddWAC.getValue();
      try {
        wacId = Integer.parseInt(bulkwac.toString());
      }
      catch (Exception e) {
        createLocations = false;
      }
    }

    if (createLocations) {
      if (isNaN(getBulkAddRow())) {
        JSFUtils.addFacesErrorMessage(globalConstants.getInvalidNumberTag(), "Number of Rows must be an integer.");
        setFocus(getBulkAddRow());
        createLocations = false;
      }
      if (isNaN(getBulkAddShelfGroup())) {
        JSFUtils.addFacesErrorMessage(globalConstants.getInvalidNumberTag(), "Number of Shelf Groups must be an integer.");
        setFocus(getBulkAddShelfGroup());
        createLocations = false;
      }
      if (isNaN(getBulkAddLocationsPerShelf())) {
        JSFUtils.addFacesErrorMessage(globalConstants.getInvalidNumberTag(), "Number of Locations per Shelf Group must be an integer.");
        setFocus(getBulkAddLocationsPerShelf());
        createLocations = false;
      }
      if (isNaN(getBulkAddLevel())) {
        JSFUtils.addFacesErrorMessage(globalConstants.getInvalidNumberTag(), "Number of Levels must be an integer.");
        setFocus(getBulkAddLevel());
        createLocations = false;
      }
    }

    if (createLocations) {
      try {
        // get the application server
        AppModuleImpl service = getAppModule();
        service.createBulkLocations(wacId, Integer.parseInt(bulkAddRow.getValue().toString()),
            Integer.parseInt(bulkAddShelfGroup.getValue().toString()),
            Integer.parseInt(bulkAddLocationsPerShelf.getValue().toString()),
            Integer.parseInt(bulkAddLevel.getValue().toString()));

        // clear the fields for the next one
        bulkAddRow.setValue(null);
        bulkAddLevel.setValue(null);
        bulkAddShelfGroup.setValue(null);
        bulkAddLocationsPerShelf.setValue(null);
        bulkAddWAC.setValue(null);

        // refresh the view
        DCIteratorBinding locationiterator = ADFUtils.findIterator("LocationNonMechView1Iterator");

        locationiterator.executeQuery();
        displaySuccessMessage("Non-Mechanized Location saved successfully.");
      }
      catch (Exception e) {
        log.debug("An error occurred in WarehouseSetup(Non-Mechanized)  {}", e.getMessage());
        displayMessage("Non-Mechanized Location failed to create.");
      }
    }
  }

  @SuppressWarnings("unused")  //called from .jspx
  public void addOneBulkLocation(ActionEvent event) {
    boolean createLocations = true;
    int wacId = 0;
    val globalConstants = ContextUtils.getBean(GlobalConstants.class);

    if (bulkAddWAC.getValue() == null) {
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "WAC is required.");
      setFocus(getBulkAddWAC());
      createLocations = false;
    }
    else {
      Object bulkWac = bulkAddWAC.getValue();
      try {
        wacId = Integer.parseInt(bulkWac.toString());
      }
      catch (Exception e) {
        createLocations = false;
      }
    }
    String bulkLoc;
    if (!isEmpty(getBulkLocation())) {

      bulkLoc = Util.trimUpperCaseClean(getBulkLocation().getValue());
      if (RegUtils.isNotAlphaNumeric(bulkLoc)) {
        JSFUtils.addFacesErrorMessage(globalConstants.getInvalidFieldTag(), "Non-Mechanized Location must be 9 alphanumeric characters only");
        setFocus(getBulkLocation());
        return;
      }

      if (bulkLoc.length() < 9) {
        JSFUtils.addFacesErrorMessage(globalConstants.getInvalidFieldTag(), "Non-Mechanized Location must be 9 alphanumeric characters only");
        setFocus(getBulkLocation());
        return;
      }
    }
    else {
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "Non-Mechanized Location is a required field");
      setFocus(getBulkLocation());
      return;
    }

    if (createLocations) {
      try {
        // get the application server
        AppModuleImpl service = getAppModule();
        service.addOneBulkLocation(wacId, bulkLoc);

        // clear the fields for the next one
        bulkAddRow.setValue(null);
        bulkAddLevel.setValue(null);
        bulkAddShelfGroup.setValue(null);
        bulkAddLocationsPerShelf.setValue(null);
        bulkAddWAC.setValue(null);
        bulkLocation.setValue(null);

        // refresh the view
        DCIteratorBinding locationiterator = ADFUtils.findIterator("LocationNonMechView1Iterator");

        locationiterator.executeQuery();
        displaySuccessMessage("Non-Mechanized Location saved successfully.");
      }
      catch (Exception e) {
        log.debug("An error occurred in WarehouseSetup(Non-Mechanized)  {}", e.getMessage());
        displayMessage("Non-Mechanized Location failed to create.");
      }
    }
  }

  public void resetFocusNonMech() {
    getBulkAddWAC().setInlineStyle("");
    getBulkAddRow().setInlineStyle("");
    getBulkAddShelfGroup().setInlineStyle("");
    getBulkAddLocationsPerShelf().setInlineStyle("");
    getBulkAddLevel().setInlineStyle("");
    getBulkLocation().setInlineStyle(uppercase);
  }

  // function to clear the temp rows in the create carousel buttons
  @SuppressWarnings("unused")
  public void clearCarouselCreateRows(ActionEvent actionEvent) {
    // call the service to do our work for us
    AppModuleImpl service = getAppModule();
    if (service == null) {
      displayMessage("[MODULE] Error in carousel ");
      return;
    }
    service.clearCarouselRow();
  }
  // function to create a range of mech locations

  @SuppressWarnings("unused")  //called from .jspx
  public void createHorizontalCarousel(ActionEvent actionEvent) {
    resetFocusHorizontal();
    boolean error = false;
    val globalConstants = ContextUtils.getBean(GlobalConstants.class);

    if (isEmpty(getWacIdHorizontal())) {
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "WAC is required.");
      setFocus(getWacIdHorizontal());
      error = true;
    }

    if (getBinTypeHorizontal().getValue() == null) {
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "Bin Type is required.");
      setFocus(getBinTypeHorizontal());
      error = true;
    }

    if (isEmpty(getBaysHorizontal())) {
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "Number of bays is required.");
      setFocus(getBaysHorizontal());
      error = true;
    }
    else {
      if (isNaN(getBaysHorizontal())) {
        JSFUtils.addFacesErrorMessage(globalConstants.getInvalidNumberTag(), "Bays is required.");
        setFocus(getBaysHorizontal());
        error = true;
      }
    }
    if (error)
      return;

    int iWacId = Util.cleanInt(getWacIdHorizontal().getValue());
    int iBays = Util.cleanInt(getBaysHorizontal().getValue());
    log.debug("wacId =    {}", iWacId);
    log.debug("bays =    {}", iBays);
    if (iBays > 99) {
      displayMessage("[Invalid Input] Number of bays for Horizontal Carousel should not exceed the maximum 99.");
      setFocus(getBaysHorizontal());
    }

    if (iWacId > 0 && iBays > 0) {
      Map<Integer, String> hmBinMap = new HashMap<>();
      try {
        // get the service
        AppModuleImpl service = getAppModule();
        TempMechCreateRowsImpl view = service.getTempMechCreateRows1();
        Row[] rows = view.getAllRowsInRange();
        // build all rows for each side
        Integer levels = 0;
        for (int rowIdx = 0; rowIdx < rows.length; rowIdx++) {
          // pull the field data out
          String locType = "";
          Object locTypeObj = rows[rowIdx].getAttribute("LocationType");
          if (locTypeObj != null)
            locType = locTypeObj.toString();

          log.debug("{} = {}", rowIdx, locType);
          levels++;
          hmBinMap.put(levels, locType);
        }

        if ((levels > 0) && (levels < 27)) {
          // get the workload manager service
          WorkLoadManagerImpl wmService = getWorkloadManagerModule();
          int msgNum = wmService.createHorizontalCarousel(iWacId, levels, iBays, hmBinMap, iUserId);
          log.debug("msgNum =  {}", msgNum);
          displayCarouselMessage(msgNum);
          // output message to FacesContext
          // clear fields
          view.clearCache();
          clearCarouselFields();
        }
        else {
          // output message to FacesContext
          displayMessage("[Invalid Input] Number of levels for Horizontal Carousel may have exceeded the maximum 26.");
          setFocus(getBinTypeHorizontal());
        }
      }
      catch (Exception e) {
        AdfLogUtility.logException(e);
      }
    }
    else {
      displayMessage("[ERROR] Invalid input wac and/or number of bays.");
    }
  }

  public void clearCarouselFields() {
    getWacIdHorizontal().setValue("");
    getBaysHorizontal().setValue("");
    getBinTypeHorizontal().setValue("");
    getWacIdVertical().setValue("");
    getBinTypeVertical().setValue("");
  }

  public void resetFocusHorizontal() {
    getWacIdHorizontal().setInlineStyle("");
    getBaysHorizontal().setInlineStyle("");
    getBinTypeHorizontal().setInlineStyle("");
  }

  public void resetFocusVertical() {
    getWacIdVertical().setInlineStyle("");
    getBinTypeVertical().setInlineStyle("");
  }

  @SuppressWarnings("unused")  //called from .jspx
  public void resetHorizontalCarousel(ActionEvent event) {
    clearCarouselFields();
    resetFocusHorizontal();
    try {
      // get the service
      AppModuleImpl service = getAppModule();
      TempMechCreateRowsImpl view = service.getTempMechCreateRows1();
      Row[] rows = view.getAllRowsInRange();
      for (Row row : rows)
        if (row != null)
          row.remove();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  @SuppressWarnings("unused")
  public void resetVerticalCarousel(ActionEvent event) {
    clearCarouselFields();
    resetFocusVertical();
    try {
      // get the service
      AppModuleImpl service = getAppModule();
      TempMechCreateRowsImpl view = service.getTempMechCreateRows2();
      Row[] rows = view.getAllRowsInRange();
      for (Row row : rows)
        if (row != null)
          row.remove();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }
  // function to create a range of mech locations

  @SuppressWarnings("unused")  //called from .jspx
  public void createVerticalCarousel(ActionEvent actionEvent) {
    resetFocusVertical();
    boolean error = false;
    val globalConstants = ContextUtils.getBean(GlobalConstants.class);

    if (isEmpty(getWacIdVertical())) {
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "WAC is required.");
      setFocus(getWacIdVertical());
      error = true;
    }

    if (getBinTypeVertical().getValue() == null) {
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "Bin Type is required.");
      setFocus(getBinTypeVertical());
      error = true;
    }

    if (error)
      return;

    int iWacId = Util.cleanInt(getWacIdVertical().getValue());
    log.debug("wacId =    {}", iWacId);

    if (iWacId > 0) {
      Map<Integer, String> hmBinMap = new HashMap<>();
      try {
        // get the service
        AppModuleImpl service = getAppModule();
        TempMechCreateRowsImpl view = service.getTempMechCreateRows2();
        Row[] rows = view.getAllRowsInRange();
        // build all rows for each side
        Integer levels = 0;
        for (int rowIdx = 0; rowIdx < rows.length; rowIdx++) {
          // pull the field data out
          String locType = "";
          Object locTypeObj = rows[rowIdx].getAttribute("LocationType");
          if (locTypeObj != null)
            locType = locTypeObj.toString();

          log.debug("{} = {}", rowIdx, locType);
          levels++;
          hmBinMap.put(levels, locType);
        }

        if ((levels > 0) && (levels < 100)) {
          // get the workload manager service
          WorkLoadManagerImpl wmService = getWorkloadManagerModule();
          int msgNum = wmService.createVerticalCarousel(iWacId, levels, hmBinMap, iUserId);
          // output message (msgNum) to FacesContext
          displayCarouselMessage(msgNum);

          // clear fields
          view.clearCache();
          clearCarouselFields();
        }
        else {
          // output message to FacesContext
          displayMessage("[Invalid Input] Number of levels for Vertical Carousel may have exceeded the maximum 99.");
          setFocus(getBinTypeVertical());
        }
      }
      catch (Exception e) {
        AdfLogUtility.logException(e);
      }
    }
    else {
      displayMessage("[ERROR] Invalid input wac.");
      setFocus(getWacIdVertical());
    }
  }

  public void displayCarouselMessage(int msgNum) {
    String message;
    switch (msgNum) {
      case 0:
        message = "Successful creation.";
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, message, null);
        FacesContext facesContext = FacesContext.getCurrentInstance();
        facesContext.addMessage(null, msg);
        return;
      case -1:
        message = "Failed due to unable to get the WAC number from the WAC table based off of the wacId passed.";
        break;
      case -2:
        message = "Failed due to unable to get the next carousel_id from the SITE_INFO table.";
        break;
      case -3:
        message = "Failed due to unable to retrieve a row from location_classification from the loc_classsification_id in the hashmap.";
        break;
      case -4:
        message = "Failed due to unable to get the number of divided slots for a given bin.";
        break;
      case -5:
        message = "Failed due to unable to get the display_position from divider_slots table.";
        break;
      case -6:
        message = "Failed due to unable to get the loc_header_id back from the location_header after insert.";
        break;
      case -7:
        message = "Failed due to unable to get the location_header_bin_id back from the location_header_bin after insert.";
        break;
      case -8:
        message = "Failed due to unable to use more than Side A and Side B.";
        break;
      case -9:
        message = "Cannot add this bin to this bay.  Check bin dimensions and dividers.   The maximum number of locations per bay is 36.  This exceeds that limit.";
        break;
      case -10:
        message = "Failed due to Bin type (width) too small for Carousel.";
        break;
      case -999:
        message = "Failed during creation of location rows.  Try again.";
        break;
      default:
        message = "Failed.";
    }

    displayMessage(message);
  }

  /************************************************************
   * ROUTE
   * **********************************************************/
  private String routeIterator = "RouteView1Iterator_WarehouseMgmt";

  @SuppressWarnings("unused")
  public void submitUpdateRoute(ActionEvent event) {
    resetFocusRoute();
    setRouteFlag(false);
  }

  public void dialogListener(DialogEvent dialogEvent) {
    if (dialogEvent.getOutcome().equals(DialogEvent.Outcome.ok)) {
      submitDeleteRoute(null);
      RichPopup popup;
      popup = (RichPopup) JSFUtils.findComponentInRoot("confirmDelete");
      popup.hide();

      //Refresh accTable
      AdfFacesContext.getCurrentInstance().addPartialTarget(routeTable);
    }
  }

  @SuppressWarnings("unused") //called from .jspx
  public void dialogListenerShip(DialogEvent dialogEvent) {
    if (dialogEvent.getOutcome().equals(DialogEvent.Outcome.ok)) {
      submitDeleteShippingRoute(null);
      RichPopup popup;
      popup = (RichPopup) JSFUtils.findComponentInRoot("confirmDelete");
      popup.hide();

      //Refresh accTable
      AdfFacesContext.getCurrentInstance().addPartialTarget(shippingRouteTable);
    }
  }

  @SuppressWarnings("unused")
  public void submitDeleteRoute(ActionEvent event) {
    resetFocusRoute();
    String deleteKey = "";

    Object oldRowKey = routeTable.getRowKey();
    Iterator<Object> selection = routeTable.getSelectedRowKeys().iterator();
    try {
      while (selection.hasNext()) {
        Object rowKey = selection.next();
        routeTable.setRowKey(rowKey);
        JUCtrlValueBindingRef rowData = (JUCtrlValueBindingRef) routeTable.getRowData();
        Row r = rowData.getRow();
        if (r.getAttribute(ROUTE_ID) != null)
          deleteKey = (r.getAttribute(ROUTE_ID).toString());
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    finally {
      routeTable.setRowKey(oldRowKey);
    }

    if (deleteKey == null || deleteKey.equals("")) {
      displayMessage("[ERROR] Unknown Record for deletion.");
    }
    else {
      try {
        int id = Integer.parseInt(deleteKey);
        String message;
        WarehouseSetupImpl service = getWarehouseSetupModule();
        message = service.deleteRoute(id);
        if (message.equals("")) {
          DCIteratorBinding iter = ADFUtils.findIterator(routeIterator);
          iter.executeQuery();
          displaySuccessMessage("Route deleted.");
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

  /**
   * This method saves a route into the database.  Displays an appropriate
   * af:message based on success.
   */
  @SuppressWarnings("unused")
  public void submitSaveRoute(ActionEvent actionEvent) {
    resetFocusRoute();
    val globalConstants = ContextUtils.getBean(GlobalConstants.class);

    try {
      //* handle required fields
      if (isEmpty(getRouteName())) {
        JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "Route Name is required.");
        getRouteName().setInlineStyle(BORDER_COLOR_RED);
        getRouteName().setContentStyle(TEXT_TRANSFORM_UPPERCASE);
        return;
      }

      String localRouteName = getRouteName().getValue().toString();
      if (localRouteName.trim().length() != MAX_LEN_ROUTE_NAME) {
        JSFUtils.addFacesErrorMessage(INVALID_FIELD, "Route Name must be " + MAX_LEN_ROUTE_NAME + " alphanumeric characters");
        getRouteName().setInlineStyle(BORDER_COLOR_RED);
        getRouteName().setContentStyle(TEXT_TRANSFORM_UPPERCASE);
        return;
      }
      else if (RegUtils.isNotAlphaNumeric(localRouteName)) {
        JSFUtils.addFacesErrorMessage(INVALID_FIELD, "Route Name must be alphanumeric characters only");
        getRouteName().setInlineStyle(BORDER_COLOR_RED);
        getRouteName().setContentStyle(TEXT_TRANSFORM_UPPERCASE);
        return;
      }

      //* handle case requirements
      ELUtils.set("#{bindings.RouteName.inputValue}", Util.trimUpperCaseClean(getRouteName().getValue()));
      ELUtils.set("#{bindings.Description.inputValue}", Util.trimUpperCaseClean(getRouteDescription().getValue()));

      //* submit save to database
      saveIteratorKeepPosition(routeIterator, "Route", "Name", getRouteTable(), false);

      //* set route flag back on (true) to turn on readonly+style and hide certain ui buttons
      setRouteFlag(true);
    }
    catch (Exception e) {
      log.debug("An exception has been caught in route update  {}", e.getMessage());
      setRouteFlag(false);
    }
  }

  @SuppressWarnings("unused")
  public void submitResetRoute(ActionEvent event) {
    resetFocusRoute();
    resetKeepPosition(routeIterator);
    setRouteFlag(false);
  }

  @SuppressWarnings("unused")
  public void submitCancelRoute(ActionEvent event) {
    resetFocusRoute();
    cancel(routeIterator);
    setRouteFlag(true);
  }

  @SuppressWarnings("unused")
  public void submitCreateRoute(ActionEvent actionEvent) {
    resetFocusRoute();
    cancel(routeIterator);

    //* will clean up dirty created rows - also from a manual refresh
    OperationBinding op1 = ADFUtils.getDCBindingContainer().getOperationBinding("CreateInsert");
    op1.execute();
  }

  public void resetFocusRoute() {
    getRouteName().setInlineStyle(uppercase);
  }

  /**
   * This function was added to handle user's manual refresh.  The table will not
   * keep creating new rows while there is already a new row which is unsaved.
   * The function is used only to call the ExecuteQuery on the iterator;
   * it does not set any variables used by system.
   */
  @SuppressWarnings("unused") //called from .jspx
  public void setRouteRefresh(boolean refresh) {
    //* will clean up dirty created rows - also from a manual refresh
    OperationBinding op1 = ADFUtils.getDCBindingContainer().getOperationBinding("ExecuteRoute");
    op1.execute();
  }

  public final void setInitialRouteFlag() {
    RequestContext afContext = RequestContext.getCurrentInstance();
    Object obj = afContext.getPageFlowScope().get(ROUTE_FLAG);
    if (obj == null)
      setRouteFlag(true);
  }

  /**
   * Set route flag back on (true) to turn on readonly+style
   * and hide certain ui buttons on Warehouse Management - Route Update.
   */
  public void setRouteFlag(boolean show) {
    RequestContext afContext = RequestContext.getCurrentInstance();
    //* for use in the web, maintaining state
    afContext.getPageFlowScope().put(ROUTE_FLAG, show);
    this.routeFlag = show;
  }

  public boolean getRouteFlag() {
    RequestContext afContext = RequestContext.getCurrentInstance();
    boolean flag = routeFlag;
    Object obj = afContext.getPageFlowScope().get(ROUTE_FLAG);
    if (obj != null)
      flag = Boolean.parseBoolean(obj.toString());

    return routeFlag || flag;
  }

  private String routeReadOnlyStyle = READ_ONLY_FIELD;

  public String getRouteReadOnlyStyle() {
    routeReadOnlyStyle = getRouteFlag() ? READ_ONLY_FIELD : "";
    return routeReadOnlyStyle;
  }

  public void setRouteReadOnlyStyle(String param) {
    this.routeReadOnlyStyle = param;
  }

  /************************************************************
   * SHIPPING ROUTE
   * **********************************************************/
  private String shippingRouteIterator = "ShippingRouteView1Iterator_WarehouseSetup";

  @SuppressWarnings("unused")
  public void submitUpdateShippingRoute(ActionEvent event) {
    resetFocusShippingRoute();
    setShippingRouteFlag(false);
  }

  @SuppressWarnings("unused")
  public void submitDeleteShippingRoute(ActionEvent event) {
    resetFocusShippingRoute();
    String deleteKey = "";
    Object oldRowKey = shippingRouteTable.getRowKey();
    Iterator<Object> selection = shippingRouteTable.getSelectedRowKeys().iterator();
    try {
      while (selection.hasNext()) {
        Object rowKey = selection.next();
        shippingRouteTable.setRowKey(rowKey);
        JUCtrlValueBindingRef rowData = (JUCtrlValueBindingRef) shippingRouteTable.getRowData();
        Row r = rowData.getRow();
        if (r.getAttribute(ROUTE_ID) != null)
          deleteKey = r.getAttribute(ROUTE_ID).toString();
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    finally {
      shippingRouteTable.setRowKey(oldRowKey);
    }

    if (deleteKey == null || deleteKey.equals("")) {
      displayMessage("[ERROR] Unknown Record for deletion.");
    }
    else {
      try {
        int id = Integer.parseInt(deleteKey);
        String message;
        WarehouseSetupImpl service = getWarehouseSetupModule();
        message = service.deleteShippingRoute(id);
        if (message.equals("")) {

          DCIteratorBinding iter = ADFUtils.findIterator(shippingRouteIterator);
          iter.executeQuery();
          displaySuccessMessage("Shipping Route deleted.");
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

  /**
   * This method saves a shipping route into the database.  Displays an appropriate
   * af:message based on success.
   */
  @SuppressWarnings("unused")
  public void submitSaveShippingRoute(ActionEvent actionEvent) {
    resetFocusShippingRoute();
    try {
      //* handle required fields
      if (isEmpty(getShippingRouteName())) {
        JSFUtils.addFacesErrorMessage("MISSING REQUIRED FIELD", "Shipping Route Name is required.");
        getShippingRouteName().setInlineStyle(BORDER_COLOR_RED);
        getShippingRouteName().setContentStyle(TEXT_TRANSFORM_UPPERCASE);
        return;
      }

      String localShippingRouteName = getShippingRouteName().getValue().toString().trim();
      if (localShippingRouteName.length() < MIN_LEN_SHIPPING_ROUTE_NAME) {
        JSFUtils.addFacesErrorMessage(INVALID_FIELD, "Shipping Route Name must be >= " + MIN_LEN_SHIPPING_ROUTE_NAME + " alphanumeric character(s)");
        getShippingRouteName().setInlineStyle(BORDER_COLOR_RED);
        getShippingRouteName().setContentStyle(TEXT_TRANSFORM_UPPERCASE);
        return;
      }
      else if (RegUtils.isNotAlphaNumericPlusSpace(localShippingRouteName)) {
        JSFUtils.addFacesErrorMessage(INVALID_FIELD, "Shipping Route Name must be alphanumeric characters only");
        getShippingRouteName().setInlineStyle(BORDER_COLOR_RED);
        getShippingRouteName().setContentStyle(TEXT_TRANSFORM_UPPERCASE);
        return;
      }

      //* handle case requirements
      ELUtils.set("#{bindings.RouteName.inputValue}", Util.trimUpperCaseClean(getShippingRouteName().getValue()));
      ELUtils.set("#{bindings.Description.inputValue}", Util.trimUpperCaseClean(getShippingRouteDescription().getValue()));

      //* submit save to database
      saveIteratorKeepPosition(shippingRouteIterator, "Shipping Route", "Name", getShippingRouteTable(), false);

      //* set shipping route flag back on (true) to turn on readonly+style and hide certain ui buttons
      setShippingRouteFlag(true);
    }
    catch (Exception e) {
      log.debug("An exception has been caught in shipping route update  {}", e.getMessage());
      setShippingRouteFlag(false);
    }
  }

  @SuppressWarnings("unused")
  public void submitResetShippingRoute(ActionEvent event) {
    resetFocusShippingRoute();
    resetKeepPosition(shippingRouteIterator);
    setShippingRouteFlag(false);
  }

  @SuppressWarnings("unused")
  public void submitCancelShippingRoute(ActionEvent event) {
    resetFocusShippingRoute();
    cancel(shippingRouteIterator);
    setShippingRouteFlag(true);
  }

  public void resetFocusShippingRoute() {
    getShippingRouteName().setInlineStyle(uppercase);
  }

  /**
   * This function was added to handle user's manual refresh.  The table will not
   * keep creating new rows while there is already a new row which is unsaved.
   * The function is used only to call the ExecuteQuery on the iterator;
   * it does not set any variables used by system.
   */
  @SuppressWarnings("unused")
  public void setShippingRouteRefresh(boolean refresh) {
    //* will clean up dirty created rows - also from a manual refresh
    OperationBinding op1 = ADFUtils.getDCBindingContainer().getOperationBinding("ExecuteShippingRoute");

    if (op1 != null) {
      op1.execute();
    }
  }

  public final void setInitialShippingRouteFlag() {
    RequestContext afContext = RequestContext.getCurrentInstance();
    Object obj = afContext.getPageFlowScope().get(SHIPPING_ROUTE_FLAG);
    if (obj == null)
      setShippingRouteFlag(true);
  }

  /**
   * Set shipping route flag back on (true) to turn on readonly+style
   * and hide certain ui buttons on Warehouse Management - Shipping Route Update.
   */
  public void setShippingRouteFlag(boolean show) {
    RequestContext afContext = RequestContext.getCurrentInstance();
    //* for use in the web, maintaining state
    afContext.getPageFlowScope().put(SHIPPING_ROUTE_FLAG, show);
    shippingRouteFlag = show;
  }

  public boolean getShippingRouteFlag() {
    RequestContext afContext = RequestContext.getCurrentInstance();
    boolean flag = shippingRouteFlag;
    Object obj = afContext.getPageFlowScope().get(SHIPPING_ROUTE_FLAG);
    if (obj != null) {
      flag = Boolean.parseBoolean(obj.toString());
    }

    return shippingRouteFlag || flag;
  }

  private String shippingRouteReadOnlyStyle = READ_ONLY_FIELD;

  public String getShippingRouteReadOnlyStyle() {
    shippingRouteReadOnlyStyle = getShippingRouteFlag() ? READ_ONLY_FIELD : "";
    return shippingRouteReadOnlyStyle;
  }

  public void setShippingRouteReadOnlyStyle(String param) {
    this.shippingRouteReadOnlyStyle = param;
  }

  public void setIUserId(int iUserId) {
    this.iUserId = iUserId;
  }

  public int getIUserId() {
    return iUserId;
  }

  public String getImageSource() {
    AppModuleImpl service = getAppModule();
    String imageURL;
    if ((service == null) || (binTypeHorizontal.getValue() == null)) {
      imageURL = "../resources/images/bin_images/NoImage.jpg";
      log.debug("HALLOW");
    }
    else {
      imageURL = "../resources/images/bin_images/" + service.getImageSource(binTypeHorizontal.getValue().toString());
      log.debug("HALLOW2");
    }
    log.debug(imageURL);
    return imageURL;
  }

  public void setSideHorizontal(RichSelectOneChoice sideHorizontal) {
    this.sideHorizontal = sideHorizontal;
  }

  public RichSelectOneChoice getSideHorizontal() {
    return sideHorizontal;
  }

  public void setWacIdHorizontal(RichSelectOneChoice wacIdHorizontal) {
    this.wacIdHorizontal = wacIdHorizontal;
  }

  public RichSelectOneChoice getWacIdHorizontal() {
    return wacIdHorizontal;
  }

  public void setBaysHorizontal(RichInputText baysHorizontal) {
    this.baysHorizontal = baysHorizontal;
  }

  public RichInputText getBaysHorizontal() {
    return baysHorizontal;
  }

  public void setWacIdVertical(RichSelectOneChoice wacIdVertical) {
    this.wacIdVertical = wacIdVertical;
  }

  public RichSelectOneChoice getWacIdVertical() {
    return wacIdVertical;
  }

  public void setBinTypeHorizontal(RichSelectOneChoice binTypeHorizontal) {
    this.binTypeHorizontal = binTypeHorizontal;
  }

  public RichSelectOneChoice getBinTypeHorizontal() {
    return binTypeHorizontal;
  }

  public void setBinTypeVertical(RichSelectOneChoice binTypeVertical) {
    this.binTypeVertical = binTypeVertical;
  }

  public RichSelectOneChoice getBinTypeVertical() {
    return binTypeVertical;
  }

  public void setNonMechLocationTable(RichTable nonMechLocationTable) {
    this.nonMechLocationTable = nonMechLocationTable;
  }

  public RichTable getNonMechLocationTable() {
    return nonMechLocationTable;
  }

  public void setRouteTable(RichTable routeTable) {
    this.routeTable = routeTable;
  }

  public RichTable getRouteTable() {
    return routeTable;
  }

  public boolean isRouteFlag() {
    return routeFlag;
  }

  public void setShippingRouteTable(RichTable shippingRouteTable) {
    this.shippingRouteTable = shippingRouteTable;
  }

  public RichTable getShippingRouteTable() {
    return shippingRouteTable;
  }

  public boolean isShippingRouteFlag() {
    return shippingRouteFlag;
  }

  public void setRouteIterator(String routeIterator) {
    this.routeIterator = routeIterator;
  }

  public String getRouteIterator() {
    return routeIterator;
  }

  public void setShippingRouteIterator(String shippingRouteIterator) {
    this.shippingRouteIterator = shippingRouteIterator;
  }

  public String getShippingRouteIterator() {
    return shippingRouteIterator;
  }

  public void setShuttleFLOORs(RichSelectManyShuttle shuttleFLOORs) {
    this.shuttleFLOORs = shuttleFLOORs;
  }

  public RichSelectManyShuttle getShuttleFLOORs() {
    return shuttleFLOORs;
  }

  public void setBulkAddRow(RichInputText bulkAddRow) {
    this.bulkAddRow = bulkAddRow;
  }

  public RichInputText getBulkAddRow() {
    return bulkAddRow;
  }

  public void setBulkAddLevel(RichInputText bulkAddLevel) {
    this.bulkAddLevel = bulkAddLevel;
  }

  public RichInputText getBulkAddLevel() {
    return bulkAddLevel;
  }

  @SuppressWarnings("unused") //called from .jspx
  public void setBulkAddShelfGroup(RichInputText bulkAddShelfGroup) {
    this.bulkAddShelfGroup = bulkAddShelfGroup;
  }

  public RichInputText getBulkAddShelfGroup() {
    return bulkAddShelfGroup;
  }

  public void setBulkAddLocationsPerShelf(RichInputText bulkAddLocationsPerShelf) {
    this.bulkAddLocationsPerShelf = bulkAddLocationsPerShelf;
  }

  public RichInputText getBulkAddLocationsPerShelf() {
    return bulkAddLocationsPerShelf;
  }

  public void setBulkLocation(RichInputText bulkLocation) {
    this.bulkLocation = bulkLocation;
  }

  public RichInputText getBulkLocation() {
    return bulkLocation;
  }

  public void setBulkAddWAC(RichSelectOneChoice bulkAddWAC) {
    this.bulkAddWAC = bulkAddWAC;
  }

  public RichSelectOneChoice getBulkAddWAC() {
    return bulkAddWAC;
  }

  public void setRouteRefresh1(boolean routeRefresh) {
    this.routeRefresh = routeRefresh;
  }

  @SuppressWarnings("unused") //called from .jspx
  public boolean isRouteRefresh() {
    return routeRefresh;
  }

  public void setShippingRouteRefresh1(boolean shippingRouteRefresh) {
    this.shippingRouteRefresh = shippingRouteRefresh;
  }

  public boolean isShippingRouteRefresh() {
    return shippingRouteRefresh;
  }

  public void setRouteName(RichInputText routeName) {
    this.routeName = routeName;
  }

  public RichInputText getRouteName() {
    return routeName;
  }

  public void setRouteDescription(RichInputText routeDescription) {
    this.routeDescription = routeDescription;
  }

  public RichInputText getRouteDescription() {
    return routeDescription;
  }

  public void setShippingRouteName(RichInputText shippingRouteName) {
    this.shippingRouteName = shippingRouteName;
  }

  public RichInputText getShippingRouteName() {
    return shippingRouteName;
  }

  public void setShippingRouteDescription(RichInputText shippingRouteDescription) {
    this.shippingRouteDescription = shippingRouteDescription;
  }

  public RichInputText getShippingRouteDescription() {
    return shippingRouteDescription;
  }
}


