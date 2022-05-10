package mil.stratis.view.shipping;

import lombok.extern.slf4j.Slf4j;
import mil.stratis.common.util.RegUtils;
import mil.stratis.common.util.Util;
import mil.stratis.model.datatype.ship.ShippingBarcode;
import mil.stratis.model.datatype.ship.ShippingFloor;
import mil.stratis.model.services.ShippingServiceImpl;
import mil.stratis.view.util.JSFUtils;
import oracle.adf.view.rich.component.rich.input.RichInputText;
import oracle.adf.view.rich.component.rich.nav.RichButton;
import oracle.adf.view.rich.component.rich.output.RichOutputLabel;
import oracle.adf.view.rich.context.AdfFacesContext;
import org.apache.commons.lang3.StringUtils;

import javax.faces.event.ActionEvent;
import java.util.Map;

@Slf4j
public class ShippingAddContainer extends ShippingHandler {

  /**
   * variables for the first page
   */
  private transient RichInputText singleContainerId;
  private transient RichInputText singleStageLocator;

  private transient RichButton singleSubmitBtn; // ZSL 12/2017
  private boolean displayAssignment = false;

  /**
   * variables for the second page
   */
  private transient RichInputText singleStageLocatorEnter;
  private transient RichOutputLabel singleLblContainerIdentifier;
  private transient RichOutputLabel singleLblFirstLocation;
  private transient RichOutputLabel singleLblOtherLocations;

  public ShippingAddContainer() {
    init();

    //* required to show initial screen with unrendered
    AdfFacesContext afContext = AdfFacesContext.getCurrentInstance();
    if (!afContext.isPostback()) {
      setDisplayAssignment(false);
    }
  }

  /**
   * This function is the main backing function for the Shipping Add
   * Container page.  On submit, validates the container id (or barcode)
   * and the stage locator (or floor location or lead tcn).
   * <p>
   * If validation is successful, then the barcode (and all its shipping items)
   * are either added to the shipping manifest with a new lead tcn
   * or assigned an existing shipping manifest.
   * <p>
   * The rules for valid container are
   * 1) Required field
   * 2) greater than 5 characters and alphanumeric
   * 3) this is a shipping barcode
   * 4) exists in Shipping table and is not assigned yet to a floor
   * <p>
   * <p>
   * The rules for valid stage location are
   * 1) Optional field
   * 2) alphanumeric characters
   * 3) this is a 5-character floor location or a 17-character lead tcn
   * 4) in_use flag is N for this warehouse
   * 5) locked for this warehouse and shipping area
   * 6) not assigned to another customer for this warehouse and shipping area
   */
  public void submitAddSingleContainer(ActionEvent event) {

    if (getDisplayAssignment() && getSingleStageLocatorEnter().isRendered()) {
      submitContainerAssignment(event);
      return;
    }

    //* validate input fields
    Object objContainerId = getSingleContainerId().getValue();
    String containerId = "";
    boolean error = false;
    if (objContainerId == null) {
      //* show error
      JSFUtils.addFacesErrorMessage("Container Identifier", "Container Identifier is a required field.");
      error = true;
    }
    else {
      containerId = objContainerId.toString().trim().toUpperCase();
      if (RegUtils.isNotAlphaNumeric(containerId)) {
        //* show error
        JSFUtils.addFacesErrorMessage("Container Identifier", "Container Identifier must be Shipping Barcode greater than or equal to 5 characters, alphanumeric only.");
        error = true;
      }
      if (containerId.length() < 5) {
        //* show error
        JSFUtils.addFacesErrorMessage("Container Identifier", "Container Identifier must be Shipping Barcode greater than or equal to 5 characters.");
        error = true;
      }
    }

    if (error) return;

    Object objStageLocator = getSingleStageLocator().getValue();
    String stageLocator = "";
    if (objStageLocator != null) {
      stageLocator = objStageLocator.toString().trim().toUpperCase();
      if (!Util.isEmpty(stageLocator)) {
        if (RegUtils.isNotAlphaNumeric(stageLocator)) {
          //* show error
          JSFUtils.addFacesErrorMessage("Enter Stage Location", "Stage Locator (optional) must be 5-character Floor Location or 17-character Lead TCN, alphanumeric only.");
          error = true;
        }

        if (stageLocator.length() != 5 && stageLocator.length() != 17) {
          //* show error
          JSFUtils.addFacesErrorMessage("Enter Stage Location", "Stage Locator (optional) must be 5-character Floor Location or 17-character Lead TCN.");
          error = true;
        }
      }
    }

    if (error) return;

    ShippingServiceImpl service = getShippingServiceModule();
    ShippingBarcode shippingBarcode = service.validateContainerIdentifier(containerId);

    if (!shippingBarcode.hasShippingItems()) {
      //* show error
      JSFUtils.addFacesErrorMessage("Container Identifier", "CONTAINER DOES NOT EXIST");
      return;
    }

    if (shippingBarcode.isAlreadyAssigned()) {
      //* show error
      JSFUtils.addFacesErrorMessage("Container Identifier", "CONTAINER ALREADY ASSIGNED");
      return;
    }

    String warehouseIdOfCurrentWorkstation = service.getWorkstationWarehouseId(String.valueOf(getWorkstationId()));
    //* added 10/6/08 to not allow review barcode update of another building's containers
    int packedWarehouseId = service.packedInWarehouseHH(shippingBarcode.getPackingConsolidationId());
    if (service.isNotAllowedToShipHH(Util.cleanInt(warehouseIdOfCurrentWorkstation), packedWarehouseId)) {
      JSFUtils.addFacesErrorMessage("Container Identifier", "CONTAINER PACKED IN ANOTHER WAREHOUSE");
      return;
    }

    //* validate the optional stage locator
    if (!Util.isEmpty(stageLocator)) {
      ShippingFloor shippingFloor = validateStageLocator(stageLocator, shippingBarcode);
      if (shippingFloor == null) return;
      if (!shippingFloor.hasError()) {
        //* no errors, so add the container
        int result = service.assignMaterial(shippingBarcode, shippingFloor, getUserId(), getWorkstationId());
        if (result <= 0) {
          //* show error
          JSFUtils.addFacesErrorMessage("Add Container", "Unable to add container at this time.  Please try again later.");
        }
        else {
          displaySuccessMessage(new StringBuilder("Container successfully added."));
        }

        getSingleContainerId().setValue(null);
        getSingleStageLocator().setValue(null);
        return;
      } //* else go to assignment screen
    }

    AdfFacesContext afContext = AdfFacesContext.getCurrentInstance();
    afContext.getPageFlowScope().put("shippingBarcode", shippingBarcode);

    //* display the container assignment screen
    setDisplayAssignment(true);

    getSingleLblContainerIdentifier().setValue(containerId);

    //* fill in the best locations
    Map<Integer, String> hm = service.getBestFloorLocations(shippingBarcode.getCustomerId(), getWorkstationId());
    if (!hm.isEmpty() && hm.get(1) != null) {
      getSingleLblFirstLocation().setValue(hm.get(1));
      getSingleStageLocatorEnter().setValue(getSingleLblFirstLocation().getValue());

      //* only show up to 10
      int w = 2;
      int showCount = 10;
      StringBuilder showStr = new StringBuilder();
      int w2 = 0;
      while (w < showCount) {
        if (hm.get(w) != null) {
          if (w2 != 0) showStr.append(", ");
          showStr.append(hm.get(w));

          w++;
          w2++;
        }
        else {
          w = 100;
        }
      }

      if (StringUtils.isNotEmpty(showStr)) {
        getSingleLblOtherLocations().setValue(showStr.toString());
      }
    }
  }

  /**
   * This function is the second backing function for the Shipping Add
   * Container page.  If displayAssignment flag is true, then this
   * function is used instead of the main backing function submitAddSingleContainer.
   * <p>
   * User input of stage locator (5-character floor location) is validated.
   * If validation is successful, then the barcode (and all its shipping items)
   * are either added to the shipping manifest with a new lead tcn
   * or assigned an existing shipping manifest.
   * <p>
   * <p>
   * The rules for valid floor location are
   * 1) Required field
   * 2) 5 alphanumeric characters
   * 3) in_use flag is N for this warehouse
   * 4) locked for this warehouse and shipping area
   * 5) not assigned to another customer for this warehouse and shipping area
   */
  private void submitContainerAssignment(@SuppressWarnings("all") ActionEvent event) {
    AdfFacesContext afContext = AdfFacesContext.getCurrentInstance();
    Object obj = afContext.getPageFlowScope().get("shippingBarcode");
    if (obj != null) {
      ShippingBarcode shippingBarcode = (ShippingBarcode) obj;

      //* validate input fields
      Object objStageLocator = getSingleStageLocatorEnter().getValue();
      String stageLocator = "";
      boolean error = false;
      if (objStageLocator == null) {
        //* show error
        JSFUtils.addFacesErrorMessage("Stage Location", "Stage Location is a required field.");
        error = true;
      }
      else {
        stageLocator = objStageLocator.toString().trim().toUpperCase();
        if (RegUtils.isNotAlphaNumeric(stageLocator)) {
          //* show error
          JSFUtils.addFacesErrorMessage("Stage Location", "Stage Location must be equal to 5-character Floor Location, alphanumeric only.");
          error = true;
        }
        if (stageLocator.length() != 5) {
          //* show error
          JSFUtils.addFacesErrorMessage("Stage Location", "Stage Location must be equal to 5-character Floor Location.");
          error = true;
        }
      }

      if (error) return;

      ShippingFloor shippingFloor = validateStageLocator(stageLocator, shippingBarcode);
      if (shippingFloor == null) return;
      if (!shippingFloor.hasError()) {
        //* no errors, so add the container
        ShippingServiceImpl service = getShippingServiceModule();
        int result = service.assignMaterial(shippingBarcode, shippingFloor, getUserId(), getWorkstationId());
        if (result <= 0) {
          //* show error
          JSFUtils.addFacesErrorMessage("Add Container", "Unable to add container at this time.  Please try again later.");
        }
        else {
          displaySuccessMessage(new StringBuilder("Container successfully added."));  //* AS1 also sent
        }

        //* reset the form
        afContext.getPageFlowScope().put("shippingBarcode", null);
        getSingleContainerId().setValue(null);
        getSingleStageLocator().setValue(null);
        getSingleStageLocatorEnter().setValue(null);
        setDisplayAssignment(false);
        return;
      }
      refreshPage();
    }
  }

  /**
   * This is a private method.  The backing bean validates stage locator calls
   * the service to validate stage locator.
   * <p>
   * AF Messages displayed on error
   * 1) LOCATION DOES NOT EXIST.
   * 2) BARCODE's CUSTOMER CURRENTLY ASSIGNED A FLOOR ALREADY IN THIS WAREHOUSE; REQUIRED TO FILL UP FLOOR.  USE FIRST LOCATION.
   * 3) LOCATION ALREADY IN USE BY MANIFESTATION
   * 4) LOCATION ALREADY IN USE BY ANOTHER CUSTOMER
   * 5) LOCATION IS LOCKED BY ANOTHER WAREHOUSE
   * <p>
   * If success, then the method returns a ShippingFloor object.
   * If AF Message #2, then the method returns a ShippingFloor object which hasError==true
   * else, the method returns null
   *
   * @return ShippingFloor
   */
  private ShippingFloor validateStageLocator(String stageLocator, ShippingBarcode shippingBarcode) {
    ShippingServiceImpl service = getShippingServiceModule();
    ShippingFloor shippingFloor = service.validateStageLocator(stageLocator, getWorkstationId());

    if (shippingFloor.getFloorLocationId() == 0) {
      //* show error
      JSFUtils.addFacesErrorMessage("Enter Stage Location", "LOCATION DOES NOT EXIST (IN THIS WAREHOUSE)");
      return null;
    }

    String customerCurrFloor = service.getCustomerCurrentFloorLocation(shippingBarcode.getCustomerId(), getWorkstationId());
    if (!Util.isEmpty(customerCurrFloor)) {
      log.debug("customer current floor {} mismatch? {} - {}", customerCurrFloor, shippingFloor.getFloorLocationId(), shippingFloor.getFloorLocation());
      if (!customerCurrFloor.equals(shippingFloor.getFloorLocation())) {
        displayMessage(new StringBuilder("BARCODE's CUSTOMER CURRENTLY ASSIGNED A FLOOR ALREADY IN THIS WAREHOUSE; REQUIRED TO FILL UP FLOOR.  USE FIRST LOCATION."));
        getSingleStageLocatorEnter().setValue(customerCurrFloor);
        shippingFloor.setError(true);
        return shippingFloor;
      }
    }

    if (shippingFloor.isInUse()) {
      JSFUtils.addFacesErrorMessage("Enter Stage Location", "LOCATION ALREADY IN USE BY MANIFESTATION");
      return null;
    }

    if (shippingFloor.hasShippingContainers() && shippingBarcode.getCustomerId() != shippingFloor.getContainer().getCustomerId()) {
      JSFUtils.addFacesErrorMessage("Enter Stage Location", "LOCATION ALREADY IN USE BY ANOTHER CUSTOMER");
      return null;
    }

    return shippingFloor;
  }

  /**
   * This method is called from the Cancel Button.
   * It clears the processScope variables,if any and resets the form
   */
  public void cancelAddSingleContainer(@SuppressWarnings("all") ActionEvent event) {
    AdfFacesContext afContext = AdfFacesContext.getCurrentInstance();
    afContext.getPageFlowScope().put("shippingBarcode", null);
    getSingleContainerId().setValue(null);
    getSingleStageLocator().setValue(null);

    getSingleLblContainerIdentifier().setValue(null);
    getSingleStageLocatorEnter().setValue(null);
    getSingleLblFirstLocation().setValue(null);
    getSingleLblOtherLocations().setValue(null);
    setDisplayAssignment(false);
  }

  public void submitAddMultipleContainers(@SuppressWarnings("all") ActionEvent event) {
  }

  public void setSingleContainerId(RichInputText singleContainerId) {
    this.singleContainerId = singleContainerId;
  }

  public RichInputText getSingleContainerId() {
    return singleContainerId;
  }

  public void setSingleStageLocator(RichInputText singleStageLocator) {
    this.singleStageLocator = singleStageLocator;
  }

  public RichInputText getSingleStageLocator() {
    return singleStageLocator;
  }

  public void setSingleSubmitBtn(RichButton singleSubmitBtn) {
    this.singleSubmitBtn = singleSubmitBtn;
  }

  public RichButton getSingleSubmitBtn() {
    return singleSubmitBtn;
  }

  /**
   * Set the displayAssignment variable in the processScope
   */
  public final void setDisplayAssignment(boolean show) {

    AdfFacesContext afContext = AdfFacesContext.getCurrentInstance();
    //* for use in the web, maintaining state
    afContext.getPageFlowScope().put("displayAssignment", show);
    this.displayAssignment = show;
  }

  /**
   * Get the displayAssignment variable from the processScope
   *
   * @return boolean
   */
  public boolean getDisplayAssignment() {
    AdfFacesContext afContext = AdfFacesContext.getCurrentInstance();
    boolean flag = displayAssignment;
    Object obj = afContext.getPageFlowScope().get("displayAssignment");
    if (obj != null)
      flag = Boolean.parseBoolean(obj.toString());

    return displayAssignment || flag;
  }

  public void setSingleLblContainerIdentifier(RichOutputLabel singleLblContainerIdentifier) {
    this.singleLblContainerIdentifier = singleLblContainerIdentifier;
  }

  public RichOutputLabel getSingleLblContainerIdentifier() {
    return singleLblContainerIdentifier;
  }

  public void setSingleLblFirstLocation(RichOutputLabel singleLblFirstLocation) {
    this.singleLblFirstLocation = singleLblFirstLocation;
  }

  public RichOutputLabel getSingleLblFirstLocation() {
    return singleLblFirstLocation;
  }

  public void setSingleLblOtherLocations(RichOutputLabel singleLblOtherLocations) {
    this.singleLblOtherLocations = singleLblOtherLocations;
  }

  public RichOutputLabel getSingleLblOtherLocations() {
    return singleLblOtherLocations;
  }

  public void setSingleStageLocatorEnter(RichInputText singleStageLocatorEnter) {
    this.singleStageLocatorEnter = singleStageLocatorEnter;
  }

  public RichInputText getSingleStageLocatorEnter() {
    return singleStageLocatorEnter;
  }
}
