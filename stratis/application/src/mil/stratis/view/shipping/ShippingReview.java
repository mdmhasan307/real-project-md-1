package mil.stratis.view.shipping;

import lombok.extern.slf4j.Slf4j;
import mil.stratis.common.util.RegUtils;
import mil.stratis.common.util.Util;
import mil.stratis.model.datatype.ship.ShippingBarcode;
import mil.stratis.model.datatype.ship.ShippingFloor;
import mil.stratis.model.services.ShippingServiceImpl;
import mil.stratis.view.util.JSFUtils;
import oracle.adf.view.rich.component.rich.data.RichTable;
import oracle.adf.view.rich.component.rich.input.RichInputText;
import org.apache.myfaces.trinidad.context.RequestContext;
import org.apache.myfaces.trinidad.event.DisclosureEvent;

import javax.faces.event.ActionEvent;

@Slf4j
public class ShippingReview extends ShippingHandler {

  private final int iUserId;

  /**
   * variables for the review location
   */
  private transient RichTable containerReviewTable;
  private transient RichInputText txtEnterLocation;
  private transient RichInputText txtEnterContainer;
  private transient RichInputText txtDispositionExpectedLocation;
  private transient RichInputText txtDispositionCurrentLocation;
  private transient RichInputText txtDispositionExpectedAAC;
  private transient RichInputText txtDispositionCurrentAAC;

  private transient RichInputText txtDispositionShippingId;
  private transient RichInputText txtDispositionShippingManifestId;
  private transient RichInputText txtDispositionScn;

  boolean hasDisposition0 = false;
  boolean hasDisposition1 = false;
  boolean hasDisposition2 = false;
  boolean hasDisposition3 = false;
  boolean hasDisposition4 = false;
  boolean hasDispositionUnknown1 = false;
  boolean hasDispositionUnknown2 = false;
  boolean hasBarcodeReviewResults = false;
  private String strShippingId = "";
  private String strScn = "";
  private String strShippingManifestId = "";
  boolean hasContainerReviewResults = false;
  boolean showTableResults = false;
  private transient RichTable tableShippingReview2;

  public ShippingReview() {
    init();

    //* Get the current user ID
    iUserId = getUserId();

    //* required to show initial screen with unrendered
    RequestContext afContext = RequestContext.getCurrentInstance();
    if (!afContext.isPostback()) {
      setShowTableResults(false);
    }
  }

  /**
   * This function is the main backing function for the Shipping
   * Review Location page.  On submit, the operation to perform is
   * to review location of shipping manifest ids and/or barcodes
   * for a specific floor location id
   * This is the Container Review tab only.
   */
  public void reviewLocation(@SuppressWarnings("all") ActionEvent event) {
  }

  public String getStrScn() {
    return strScn;
  }

  public void setStrScn(String strScn) {
    this.strScn = strScn;
  }

  public String getStrShippingManifestId() {
    return strShippingManifestId;
  }

  public void setStrShippingManifestId(String strShippingManifestId) {
    this.strShippingManifestId = strShippingManifestId;
  }

  public String getStrShippingId() {
    return strShippingId;
  }

  public void setStrShippingId(String strShippingId) {
    this.strShippingId = strShippingId;
  }

  public void setHasContainerReviewResults(boolean hasContainerReviewResults) {
    this.hasContainerReviewResults = hasContainerReviewResults;
  }

  public boolean getHasContainerReviewResults() {
    return hasContainerReviewResults;
  }

  public void setContainerReviewTable(RichTable containerReviewTable) {
    this.containerReviewTable = containerReviewTable;
  }

  public RichTable getContainerReviewTable() {
    return containerReviewTable;
  }

  public void setTxtEnterLocation(RichInputText txtEnterLocation) {
    this.txtEnterLocation = txtEnterLocation;
  }

  public RichInputText getTxtEnterLocation() {
    return txtEnterLocation;
  }

  public void setTxtEnterContainer(RichInputText txtEnterContainer) {
    this.txtEnterContainer = txtEnterContainer;
  }

  public RichInputText getTxtEnterContainer() {
    return txtEnterContainer;
  }

  public final void setShowTableResults(boolean show) {
    RequestContext afContext = RequestContext.getCurrentInstance();
    //* for use in the web, maintaining state
    afContext.getPageFlowScope().put("showTableResults", show);
    this.showTableResults = show;
  }

  public boolean getShowTableResults() {
    RequestContext afContext = RequestContext.getCurrentInstance();
    boolean flag = showTableResults;
    Object obj = afContext.getPageFlowScope().get("showTableResults");
    if (obj != null)
      flag = Boolean.parseBoolean(obj.toString());

    return showTableResults || flag;
  }

  public void setHasBarcodeReviewResults(boolean hasBarcodeReviewResults) {
    this.hasBarcodeReviewResults = hasBarcodeReviewResults;
  }

  public boolean getHasBarcodeReviewResults() {
    return hasBarcodeReviewResults;
  }

  @SuppressWarnings("unused") //called from .jspx
  public boolean getHasDisposition0() {
    return hasDisposition0;
  }

  public void setHasDisposition0(boolean hasDisposition) {
    this.hasDisposition0 = hasDisposition;
  }

  public void setHasDisposition1(boolean hasDisposition) {
    this.hasDisposition1 = hasDisposition;
  }

  public boolean getHasDisposition1() {
    return hasDisposition1;
  }

  public void setHasDisposition2(boolean hasDisposition) {
    this.hasDisposition2 = hasDisposition;
  }

  public boolean getHasDisposition2() {
    return hasDisposition2;
  }

  public void setHasDisposition3(boolean hasDisposition) {
    this.hasDisposition3 = hasDisposition;
  }

  public boolean getHasDisposition3() {
    return hasDisposition3;
  }

  public void setHasDisposition4(boolean hasDisposition) {
    this.hasDisposition4 = hasDisposition;
  }

  public boolean getHasDisposition4() {
    return hasDisposition4;
  }

  public void setHasDispositionUnknown1(boolean hasDisposition) {
    this.hasDispositionUnknown1 = hasDisposition;
  }

  public boolean getHasDispositionUnknown1() {
    return hasDispositionUnknown1;
  }

  public void setHasDispositionUnknown2(boolean hasDisposition) {
    this.hasDispositionUnknown2 = hasDisposition;
  }

  public boolean getHasDispositionUnknown2() {
    return hasDispositionUnknown2;
  }

  /**
   * This method resets the (disposition) message to not display
   */
  public void resetHasDisposition() {
    setHasDisposition0(false);
    setHasDisposition1(false);
    setHasDisposition2(false);
    setHasDisposition3(false);
    setHasDisposition4(false);
    setHasDispositionUnknown1(false);
    setHasDispositionUnknown2(false);
  }

  /**
   * Reset barcode container table which holds values
   * on the barcode review tab, set when a barcode is found
   */
  public void cancelBarcodeReview(@SuppressWarnings("all") ActionEvent event) {

    getTxtEnterContainer().setValue(null);
    getTxtEnterLocation().setValue(null);
    getTxtDispositionCurrentAAC().setValue(null);
    getTxtDispositionExpectedAAC().setValue(null);
    getTxtDispositionCurrentLocation().setValue(null);
    getTxtDispositionExpectedLocation().setValue(null);

    getTxtDispositionScn().setValue(null);
    getTxtDispositionShippingId().setValue(null);
    getTxtDispositionShippingManifestId().setValue(null);
    setShowTableResults(false);
  }

  public void clearReviewLocationDisclose(@SuppressWarnings("all") DisclosureEvent disclosureEvent) {
    getTxtEnterContainer().setValue("");
    getTxtEnterLocation().setValue("");
  }

  public void submitBarcodeReview(@SuppressWarnings("all") ActionEvent event) {

    //* validate input fields
    Object objContainer = getTxtEnterContainer().getValue();
    String container = "";
    boolean error = false;
    if (objContainer == null) {
      //* show error
      JSFUtils.addFacesErrorMessage("Shipping Barcode", "Shipping Barcode is a required field.");
      error = true;
    }
    else {
      container = objContainer.toString().trim().toUpperCase();
      if (RegUtils.isNotAlphaNumeric(container)) {
        //* show error
        JSFUtils.addFacesErrorMessage("Shipping Barcode", "Shipping Barcode must be greater than or equal to 5 characters, alphanumeric only.");
        error = true;
      }
      if (container.length() < 5) {
        //* show error
        JSFUtils.addFacesErrorMessage("Shipping Barcode", "Shipping Barcode must be greater than or equal to 5 characters.");
        error = true;
      }
    }

    if (error) return;

    Object objReviewLocation = getTxtEnterLocation().getValue();
    String reviewLocation = "";
    if (objReviewLocation != null) {
      reviewLocation = objReviewLocation.toString().trim().toUpperCase();
      if (!Util.isEmpty(reviewLocation)) {
        if (RegUtils.isNotAlphaNumeric(reviewLocation)) {
          //* show error
          JSFUtils.addFacesErrorMessage("Location", "Location must be 5-character Floor Location or 17-character Lead TCN, alphanumeric only.");
          error = true;
        }
        log.debug("review location length {}", reviewLocation.length());

        if (reviewLocation.length() != 5 && reviewLocation.length() != 17) {
          //* show error
          JSFUtils.addFacesErrorMessage("Location", "Location must be 5-character Floor Location or 17-character Lead TCN.");
          error = true;
        }
      }
      else {
        //* show error
        JSFUtils.addFacesErrorMessage("Location", "Location is a required field.");
        error = true;
      }
    }

    if (error) return;

    ShippingServiceImpl service = getShippingServiceModule();
    ShippingBarcode shippingBarcode = service.validateContainerIdentifier(container);
    log.debug(shippingBarcode.toString());
    if (!shippingBarcode.hasShippingItems()) {
      if (service.shippingHistoryExist(container)) {
        //* show error
        JSFUtils.addFacesErrorMessage("Shipping Barcode", "CONTAINER ALREADY PICKED UP OR DELIVERED.");
      }
      else {
        //* show error
        JSFUtils.addFacesErrorMessage("Shipping Barcode", "CONTAINER DOES NOT EXIST");
      }
      return;
    }

    //* validate the review location
    ShippingFloor shippingFloor = service.validateStageLocator(reviewLocation, getWorkstationId());

    if (shippingFloor.getFloorLocationId() == 0) {
      if (service.findFloorLocation(reviewLocation, 0, false).getFloorLocationId() == 0) {
        //* show error
        JSFUtils.addFacesErrorMessage("Location", "LOCATION DOES NOT EXIST (IN THIS WAREHOUSE)");
        return;
      }
      //* show error
      JSFUtils.addFacesErrorMessage("Location", "LOCATION DOES NOT EXIST");
      return;
    }

    String warehouseIdOfCurrentWorkstation = service.getWorkstationWarehouseId(String.valueOf(getWorkstationId()));

    //* added 10/6/08 to not allow review barcode update of another building's containers
    int packedWarehouseId = service.packedInWarehouseHH(shippingBarcode.getPackingConsolidationId());
    if (service.isNotAllowedToShipHH(Util.cleanInt(warehouseIdOfCurrentWorkstation), packedWarehouseId)) {

      JSFUtils.addFacesErrorMessage("Shipping Barcode", "CONTAINER PACKED IN ANOTHER WAREHOUSE");
      return;
    }

    if (shippingFloor.isInUse()) {
      JSFUtils.addFacesErrorMessage("Location", "LOCATION ALREADY IN USE BY MANIFESTATION");
      return;
    }

    boolean wrongCustomer = false;
    String customerCurrFloor = service.getCustomerCurrentFloorLocation(shippingBarcode.getCustomerId(), getWorkstationId());
    if (!Util.isEmpty(customerCurrFloor)) {
      log.debug("customer current floor {} mismatch? {} - {}", customerCurrFloor, shippingFloor.getFloorLocationId(), shippingFloor.getFloorLocation());
      if (!customerCurrFloor.equals(shippingFloor.getFloorLocation())) {
        wrongCustomer = true;
      }
    }

    //* check for conflicts with review location and barcode location
    if (shippingBarcode.isAlreadyAssigned()) {
      if (shippingBarcode.isAssignedToAnotherWarehouse(Util.cleanInt(warehouseIdOfCurrentWorkstation))) {
        JSFUtils.addFacesErrorMessage("Shipping Barcode", "SHIPPING BARCODE IS ASSIGNED TO ANOTHER WAREHOUSE");
        return;
      }

      if (shippingBarcode.getItem().getFloorLocationId() != shippingFloor.getFloorLocationId()) {
        setShowTableResults(true);
        setStrShippingId(String.valueOf(shippingBarcode.getItem().getShippingId()));
        getTxtDispositionShippingId().setValue(getStrShippingId());
        setStrShippingManifestId(String.valueOf(shippingBarcode.getItem().getShippingManifestId()));
        getTxtDispositionShippingManifestId().setValue(getStrShippingManifestId());
        getTxtDispositionCurrentAAC().setValue(shippingBarcode.getCustomerId());

        //* set Disposition Message
        String locationToBeReviewed = shippingFloor.getFloorLocation();
        setHasDisposition2(true);
        getTxtDispositionCurrentLocation().setValue(shippingBarcode.getItem().getFloorLocation());
        getTxtDispositionExpectedLocation().setValue(locationToBeReviewed);

        return;
      }

      if (shippingFloor.hasShippingContainers() && shippingBarcode.getCustomerId() != shippingFloor.getContainer().getCustomerId()) {
        setShowTableResults(true);
        setStrShippingId(String.valueOf(shippingBarcode.getItem().getShippingId()));
        getTxtDispositionShippingId().setValue(getStrShippingId());
        setStrShippingManifestId(String.valueOf(shippingBarcode.getItem().getShippingManifestId()));
        getTxtDispositionShippingManifestId().setValue(getStrShippingManifestId());
        //* set Disposition Message
        setStrScn(shippingBarcode.getItem().getScn());
        getTxtDispositionScn().setValue(getStrScn());

        String scnAac = shippingFloor.getContainer().getAac();
        setHasDisposition1(true);
        getTxtDispositionCurrentAAC().setValue(scnAac);
        getTxtDispositionExpectedAAC().setValue(service.getCustomerAAC(String.valueOf(shippingBarcode.getCustomerId())));

        return;
      }

      JSFUtils.addFacesErrorMessage("ALREADY AT LOCATION", "THIS SHIPPING BARCODE IS ASSIGNED TO THIS LOCATION ALREADY");
    }
    else {
      if (wrongCustomer) {
        JSFUtils.addFacesErrorMessage("Review Contents", "This shipping barcode is unprocessed and cannot be processed due to SHIPPING BARCODE's CUSTOMER CURRENTLY ASSIGNED A FLOOR ALREADY IN THIS WAREHOUSE.");
        return;
      }

      if (shippingFloor.hasShippingContainers() && shippingBarcode.getCustomerId() != shippingFloor.getContainer().getCustomerId()) {
        JSFUtils.addFacesErrorMessage("Review Contents", "This shipping barcode is unprocessed and cannot be processed due to customer conflict.");
        return;
      }

      //* no errors, so add the container
      int result = service.assignMaterial(shippingBarcode, shippingFloor, getUserId(), getWorkstationId());
      if (result <= 0) {
        //* show error
        JSFUtils.addFacesErrorMessage("Review Contents", "Unable to review the contents of this location at this time.  Please try again later.");
      }
      else {
        displaySuccessMessage(new StringBuilder("THIS SHIPPING BARCODE WAS UNPROCESSED and HAS BEEN ADDED TO LOCATION"));
      }
    }
  }

  public void resolveBarcodeReviewConflict(@SuppressWarnings("all") ActionEvent event) {
  }

  private void removeConflict() {
    getTxtEnterContainer().setValue(null);
    getTxtEnterLocation().setValue(null);
    getTxtDispositionCurrentAAC().setValue(null);
    getTxtDispositionExpectedAAC().setValue(null);
    getTxtDispositionCurrentLocation().setValue(null);
    getTxtDispositionExpectedLocation().setValue(null);

    getTxtDispositionScn().setValue(null);
    getTxtDispositionShippingId().setValue(null);
    getTxtDispositionShippingManifestId().setValue(null);
    setShowTableResults(false);
  }

  public void setTableShippingReview2(RichTable tableShippingReview2) {
    this.tableShippingReview2 = tableShippingReview2;
  }

  public RichTable getTableShippingReview2() {
    return tableShippingReview2;
  }

  public void setTxtDispositionCurrentAAC(RichInputText txtDisposition) {
    this.txtDispositionCurrentAAC = txtDisposition;
  }

  public RichInputText getTxtDispositionCurrentAAC() {
    return txtDispositionCurrentAAC;
  }

  public void setTxtDispositionExpectedAAC(RichInputText txtDisposition) {
    this.txtDispositionExpectedAAC = txtDisposition;
  }

  public RichInputText getTxtDispositionExpectedAAC() {
    return txtDispositionExpectedAAC;
  }

  @SuppressWarnings("unused") //called from .jspx
  public void setTxtDispositionCurrentLocation(RichInputText txtDisposition) {
    this.txtDispositionCurrentLocation = txtDisposition;
  }

  public RichInputText getTxtDispositionCurrentLocation() {
    return txtDispositionCurrentLocation;
  }

  @SuppressWarnings("unused") //called from .jspx
  public void setTxtDispositionExpectedLocation(RichInputText txtDisposition) {
    this.txtDispositionExpectedLocation = txtDisposition;
  }

  public RichInputText getTxtDispositionExpectedLocation() {
    return txtDispositionExpectedLocation;
  }

  public void setTxtDispositionScn(RichInputText txtDisposition) {
    this.txtDispositionScn = txtDisposition;
  }

  public RichInputText getTxtDispositionScn() {
    return txtDispositionScn;
  }

  public void setTxtDispositionShippingId(RichInputText txtDisposition) {
    this.txtDispositionShippingId = txtDisposition;
  }

  public RichInputText getTxtDispositionShippingId() {
    return txtDispositionShippingId;
  }

  public void setTxtDispositionShippingManifestId(RichInputText txtDisposition) {
    this.txtDispositionShippingManifestId = txtDisposition;
  }

  public RichInputText getTxtDispositionShippingManifestId() {
    return txtDispositionShippingManifestId;
  }

  /**
   * This is the submit button on the barcode review tab
   * submitted when aac override is set to YES
   */
  @SuppressWarnings("unused") //called from .jspx
  public void btnReviewSubmitYes1(@SuppressWarnings("all") ActionEvent event) {

    //* invoke the service to update the data
    ShippingServiceImpl shippingService = getShippingServiceModule();
    String aac = getTxtDispositionExpectedAAC().getValue().toString();
    String scn = getTxtDispositionScn().getValue().toString();
    String shippingId = getTxtDispositionShippingId().getValue().toString();

    //* override aac and mark as reviewed
    int result = shippingService.overrideAAC(aac, scn, shippingId, iUserId);
    if (result > 0) {
      displaySuccessMessage(new StringBuilder("Successful override AAC."));
    }
    else {
      displayMessage(new StringBuilder("Override AAC failed."));
    }
    removeConflict();
  }

  /**
   * This is the submit button on the barcode review tab
   * submitted when aac override is set to NO
   */
  @SuppressWarnings("unused") //called from .jspx
  public void btnReviewSubmitNo1(@SuppressWarnings("all") ActionEvent event) {

    //* invoke the service to update the data
    ShippingServiceImpl shippingService = getShippingServiceModule();
    String shippingId = getTxtDispositionShippingId().getValue().toString();
    //* do nothing, mark as reviewed only
    shippingService.donotOverrideAAC(shippingId, iUserId);
    removeConflict();
  }

  /**
   * This is the submit button on the barcode review tab
   * submitted when location override is set to YES
   */
  @SuppressWarnings("unused") //called from .jspx
  public void btnReviewSubmitYes2(@SuppressWarnings("all") ActionEvent event) {

    //* invoke the service to update the data
    ShippingServiceImpl shippingService = getShippingServiceModule();

    String floor = getTxtDispositionExpectedLocation().getValue().toString();
    String aac = getTxtDispositionCurrentAAC().getValue().toString();
    String shippingId = getTxtDispositionShippingId().getValue().toString();
    String shippingManifestId = getTxtDispositionShippingManifestId().getValue().toString();
    //* override location and mark as reviewed
    int result = shippingService.overrideLocation(floor, "", aac, shippingManifestId,
        shippingId, iUserId, getWorkstationId());
    if (result > 0) {
      displaySuccessMessage(new StringBuilder("Successful override Location."));
      setHasDisposition2(false);
    }
    else {
      if (result == -2)
        displayMessage(new StringBuilder("Unable to Override Location - Customer conflict on floor."));
      else if (result == -3)
        displayMessage(new StringBuilder("Unable to Override Location - Already Manifested."));
      else
        displayMessage(new StringBuilder("Override Location failed."));
    }
    removeConflict();
  }

  /**
   * This is the submit button on the barcode review tab
   * submitted when location override is set to NO
   */
  @SuppressWarnings("unused") //called from .jspx
  public void btnReviewSubmitNo2(@SuppressWarnings("all") ActionEvent event) {

    //* invoke the service to update the data
    ShippingServiceImpl shippingService = getShippingServiceModule();
    String shippingId = getTxtDispositionShippingId().getValue().toString();
    //* do nothing, mark as reviewed only
    shippingService.donotOverrideLocation(shippingId, iUserId);
    removeConflict();
    setShowTableResults(false);
  }
}
