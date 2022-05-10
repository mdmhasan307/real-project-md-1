package mil.stratis.view.print;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.common.util.RegUtils;
import mil.stratis.common.util.Util;
import mil.stratis.model.services.PackingModuleImpl;
import mil.stratis.model.services.ShippingServiceImpl;
import mil.stratis.view.admin.AdminBackingHandler;
import mil.stratis.view.util.JSFUtils;
import mil.usmc.mls2.stratis.common.domain.model.LabelType;
import mil.usmc.mls2.stratis.core.utility.AdfLogUtility;
import mil.usmc.mls2.stratis.core.utility.ContextUtils;
import mil.usmc.mls2.stratis.core.utility.LabelPrintUtil;
import mil.stratis.view.user.UserInfo;
import oracle.adf.view.rich.component.rich.input.RichInputText;
import oracle.adf.view.rich.component.rich.input.RichSelectOneRadio;
import oracle.adf.view.rich.component.rich.nav.RichCommandButton;
import oracle.adf.view.rich.component.rich.output.RichOutputText;
import org.apache.myfaces.trinidad.render.ExtendedRenderKitService;
import org.apache.myfaces.trinidad.util.Service;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

@Slf4j
@NoArgsConstructor
public class ReprintBacking extends AdminBackingHandler {

  private transient RichInputText containerSummaryTxtSelection;
  private transient RichSelectOneRadio formContainerSummaryRadioType;

  //* 1348 Variables
  private transient RichSelectOneRadio form1348RadioType;
  private transient RichInputText form1348TxtSelection;

  //* Floor Location Variables
  private transient RichInputText floorLoc_TxtSelection;
  private transient RichInputText floorLocFloorLocationTxtSelection;
  private transient RichInputText floorLocAACTxtSelection;
  private transient RichInputText floorLocAreaTxtSelection;

  //* Location Variables
  private transient RichInputText locTxtCopies;
  private transient RichInputText locTxtSelection;

  //* NIIN Variables
  private transient RichSelectOneRadio niinRadioType;
  private transient RichInputText niinTxtCopies;
  private transient RichInputText niinTxtSelection;

  //* Picking Variables
  private transient RichInputText pickTxtCopies;
  private transient RichInputText pickTxtSelection;

  //* Stowing Variables
  private transient RichInputText stowTxtCopies;
  private transient RichInputText stowTxtSelection;
  private transient RichSelectOneRadio stowRadioType;

  //* Shipping Manifest Variables
  private transient RichInputText manifestTxtSelection;
  private transient RichCommandButton downloadCommandButton;

  //* Carousel Command Variables
  private transient RichInputText carouselTxtSelection;
  private transient RichOutputText outputStr;

  public void configureURI(String protocol, String serverName, int port, String contextPath) {
    JSFUtils.setManagedBeanValue("userbean.siteProtocol", protocol);
    JSFUtils.setManagedBeanValue("userbean.siteServerName", serverName);
    JSFUtils.setManagedBeanValue("userbean.siteServerPort", port);
    JSFUtils.setManagedBeanValue("userbean.siteContextPath", contextPath);
  }

  /****************************************************
   * SHIPPING MANIFEST
   * ***************************************************/

  public void submitReprintManifest(@SuppressWarnings("All") ActionEvent event) {
    boolean error = false;
    //* Check all required fields first
    if (isEmpty(getManifestTxtSelection())) {
      JSFUtils.addFacesErrorMessage("MISSING REQUIRED FIELD", "Must enter a Selection");
      error = true;
    }

    if (error) return;

    String selection = getManifestTxtSelection().getValue().toString().trim().toUpperCase();
    getManifestTxtSelection().setValue(selection);
    if (RegUtils.isNotAlphaNumeric(selection)) {
      JSFUtils.addFacesErrorMessage(selection, "Invalid input (alphanumeric values only)");
      return;
    }

    if (getWorkloadManagerService().existManifestForReprint(selection)) {
      JSFUtils.setManagedBeanValue("userbean.winPrintManifest", selection);
      String url = JSFUtils.getManagedBeanValue("userbean.winPrintURL").toString();
      createPopUpWindows(url);
      clearManifestForm();
    }
    else {
      JSFUtils.addFacesErrorMessage(selection, "Does not exist.");
    }
  }

  public void submitReprintAMSCMOS(ActionEvent event) {
    boolean error = false;
    if (isEmpty(getManifestTxtSelection())) {
      JSFUtils.addFacesErrorMessage("MISSING REQUIRED FIELD", "Must enter a Selection");
      error = true;
    }

    if (error) return;

    String selection = getManifestTxtSelection().getValue().toString().trim().toUpperCase();
    getManifestTxtSelection().setValue(selection);
    if (RegUtils.isNotAlphaNumeric(selection)) {
      JSFUtils.addFacesErrorMessage(selection, "Invalid input (alphanumeric values only)");
      return;
    }

    //check that it exists
    if (!getWorkloadManagerService().existManifestForReprint(selection)) {
      JSFUtils.addFacesErrorMessage("Does not exist.");
      error = true;
    }

    //check that it has been printed
    ShippingServiceImpl shippingService = getShippingServiceModule();
    String manifest = shippingService.getShippingManifestIdForLDCONHist(selection);
    if (!error && Util.isEmpty(manifest)) {
      JSFUtils.addFacesErrorMessage("Shipping Manifest must be printed first.");
      error = true;
    }

    if (!error) {
      FacesContext context = FacesContext.getCurrentInstance();
      ExtendedRenderKitService erks = Service.getService(context.getRenderKit(), ExtendedRenderKitService.class);
      String id = downloadCommandButton.getClientId(context);
      erks.addScript(context, "customHandler(\"" + id + "\");");
    }
    else {
      clearManifestForm();
    }
  }

  public void downloadZip(FacesContext context, OutputStream out) throws IOException {
    String selection = getManifestTxtSelection().getValue().toString().trim().toUpperCase();
    String workstationId = JSFUtils.getManagedBeanValue("userbean.workstationId").toString();
    ShippingServiceImpl shippingService = getShippingServiceModule();
    String filename = shippingService.exportAMSCMOS(selection, Integer.parseInt(workstationId), true);
    if (!Util.isEmpty(filename) && !filename.contains("Error")) {
      String path = shippingService.findAMSPath();

      HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();
      response.reset();
      response.setContentType("application/x-zip-compressed");
      response.setHeader("Content-Disposition",
          "attachment; filename=\"" + filename + "\"");

      onDownload(path, filename, out);
    }
  }

  public void onDownload(String path, String documentName, OutputStream out) {

    log.trace("onDownload {} {}", path, documentName);
    File srcdoc = new File(path + documentName);

    if (srcdoc.exists()) {
      log.trace("downloading...");
      byte[] b;
      int n;
      int result;
      try (FileInputStream fis = new FileInputStream(srcdoc)) {
        do {
          n = fis.available();
          if (n > 0) {
            b = new byte[n];
            result = fis.read(b);
            out.write(b, 0, b.length);
            if (result == -1) break;
          }
        }
        while (n > 0);
      }
      catch (IOException e) {
        log.trace(e.getLocalizedMessage(), e);
      }
      try {
        out.flush();
      }
      catch (IOException e) {
        log.trace(e.getLocalizedMessage());
      }
    }
  }

  private void clearManifestForm() {
    getManifestTxtSelection().setValue("");
  }

  public void submitReprintFloorLocation(ActionEvent event) {
    if (event != null) {
      //NO-OP can't remove parameter due to adf
    }
    boolean error = false;
    //* Check all required fields first
    if (isEmpty(getFloorLocFloorLocationTxtSelection())) {
      JSFUtils.addFacesErrorMessage("MISSING REQUIRED FIELD", "Floor Location - Must enter a Selection");
      error = true;
    }
    if (isEmpty(getFloorLocAACTxtSelection())) {
      JSFUtils.addFacesErrorMessage("MISSING REQUIRED FIELD", "AAC - Must enter a Selection");
      error = true;
    }
    if (isEmpty(getFloorLocAreaTxtSelection())) {
      JSFUtils.addFacesErrorMessage("MISSING REQUIRED FIELD", "Area - Must enter a Selection");
      error = true;
    }

    if (error) return;

    String selection_floor = getFloorLocFloorLocationTxtSelection().getValue().toString().trim().toUpperCase();
    getFloorLocFloorLocationTxtSelection().setValue(selection_floor);
    String selection_aac = getFloorLocAACTxtSelection().getValue().toString().trim().toUpperCase();
    getFloorLocAACTxtSelection().setValue(selection_aac);
    String selection_area = getFloorLocAreaTxtSelection().getValue().toString().trim().toUpperCase();
    getFloorLocAreaTxtSelection().setValue(selection_area);
    log.debug("entered ... {}, {}, {}", selection_floor, selection_aac, selection_area);
    if (RegUtils.isNotAlphaNumeric(selection_floor)) {
      JSFUtils.addFacesErrorMessage(selection_floor, "Invalid input (alphanumeric values only)");
      error = true;
    }
    if (RegUtils.isNotAlphaNumeric(selection_aac)) {
      JSFUtils.addFacesErrorMessage(selection_aac, "Invalid input (alphanumeric values only)");
      error = true;
    }
    if (RegUtils.isNotAlphaNumericPlusSpace(selection_area)) {
      JSFUtils.addFacesErrorMessage(selection_area, "Invalid input (alphanumeric values only)");
      error = true;
    }

    if (error) return;

    if (!getWorkloadManagerService().existFloorForReprint(selection_floor)) {
      JSFUtils.addFacesErrorMessage("MISSING REQUIRED FIELD", "Floor Location - Does not exist in the system.");
      error = true;
    }
    if (!getWorkloadManagerService().existAACForReprint(selection_aac)) {
      JSFUtils.addFacesErrorMessage("MISSING REQUIRED FIELD", "AAC - Does not exist in the system.");
      error = true;
    }

    if (error) return;

    JSFUtils.setManagedBeanValue("userbean.winPrintFloorLocation",
        selection_floor + "," + selection_aac + "," + selection_area);
    String url = JSFUtils.getManagedBeanValue("userbean.winPrintURL").toString();
    createPopUpWindows(url);
  }

  private void clearFloorLocationForm() {
    getFloorLocFloorLocationTxtSelection().setValue("");
    getFloorLocAACTxtSelection().setValue("");
    getFloorLocAreaTxtSelection().setValue("");
  }

  /****************************************************
   * CONTAINER SUMMARY
   * ***************************************************/
  public String printContainerSummary(String barcode, String no_of_copies) {
    return printContainerSummary(barcode, no_of_copies, getStratisUrlContextPath(), getPackingModule());
  }

  public String printContainerSummary(String barcode, String no_of_copies, String urlContextPath, PackingModuleImpl service) {
    if (no_of_copies != null) {}
    return service.printContainerSummary(barcode, urlContextPath);
  }

  public void submitReprintContainerSummary(ActionEvent event) {
    if (event != null) {
      //NO-OP can't remove parameter due to adf
    }
    boolean error = false;
    //* Check all required fields first
    if (getFormContainerSummaryRadioType().getValue() == null) {
      JSFUtils.addFacesErrorMessage("MISSING REQUIRED FIELD", "Must select a Type");
      error = true;
    }
    if (isEmpty(getContainerSummaryTxtSelection())) {
      JSFUtils.addFacesErrorMessage("MISSING REQUIRED FIELD", "Must enter a Selection");
      error = true;
    }

    if (error) return;

    String selection = getContainerSummaryTxtSelection().getValue().toString().trim().toUpperCase();
    getContainerSummaryTxtSelection().setValue(selection);
    if (RegUtils.isNotAlphaNumeric(selection)) {
      JSFUtils.addFacesErrorMessage(selection, "Invalid input (alphanumeric values only)");
      return;
    }

    String type = getFormContainerSummaryRadioType().getValue().toString();
    String pin = "";
    if (!type.equals("barcode")) {
      pin = selection;
      selection = getWorkloadManagerService().findBarcodeForPIN(pin);
    }
    log.debug("entered {} {}", type, selection);

    if (getWorkloadManagerService().existBarcodeForReprint(selection)) {
      JSFUtils.setManagedBeanValue("userbean.winPrintContainerSummary", selection);
      String url = JSFUtils.getManagedBeanValue("userbean.winPrintURL").toString();
      createPopUpWindows(url);
      clearContainerSummaryForm();
    }
    else {
      JSFUtils.addFacesErrorMessage(selection, "Does not exist.");
    }
  }

  private void clearContainerSummaryForm() {
    getContainerSummaryTxtSelection().setValue("");
  }

  /****************************************************
   * 1348
   * ***************************************************/

  public void submitPrint1348(ActionEvent event) {
    if (event != null) {
      //NO-OP can't remove parameter due to adf
    }
    boolean error = false;
    //* Check all required fields first
    if (getForm1348RadioType().getValue() == null) {
      JSFUtils.addFacesErrorMessage("MISSING REQUIRED FIELD", "Must select a Type");
      error = true;
    }
    if (isEmpty(getForm1348TxtSelection())) {
      JSFUtils.addFacesErrorMessage("MISSING REQUIRED FIELD", "Must enter a Selection");
      error = true;
    }

    if (error) return;

    String selection = getForm1348TxtSelection().getValue().toString().trim().toUpperCase();
    getForm1348TxtSelection().setValue(selection);
    if (RegUtils.isNotAlphaNumeric(selection)) {
      JSFUtils.addFacesErrorMessage(selection, "Invalid input (alphanumeric values only)");
      return;
    }

    String type = getForm1348RadioType().getValue().toString();
    String document_number = "";
    String barcode = "";
    boolean found = false;
    boolean hist = false;
    String strOfQuantities = "";
    if (type.equals("barcode")) {
      barcode = selection;
      found = getWorkloadManagerService().existSCNForReprintBarcode(barcode);
      if (found) strOfQuantities = getWorkloadManagerService().getSCNQuantitiesForReprintBarcode(barcode);
      else
        hist = getWorkloadManagerService().existSCNForReprintBarcodeHist(barcode);
      if (hist) strOfQuantities = getWorkloadManagerService().getSCNQuantitiesForReprintBarcodeHist(barcode);
    }
    else {
      document_number = selection;
      found = getWorkloadManagerService().existSCNForReprintDocumentNumber(document_number);
      if (found) strOfQuantities = getWorkloadManagerService().getSCNQuantitiesForReprintDocumentNumber(document_number);
      else
        hist = getWorkloadManagerService().existSCNForReprintDocumentNumberHist(document_number);
      if (hist) strOfQuantities = getWorkloadManagerService().getSCNQuantitiesForReprintDocumentNumberHist(document_number);
    }
    log.debug("entered {} {}", type, selection);

    String url = JSFUtils.getManagedBeanValue("userbean.winPrintURL").toString();
    if (found) {
      JSFUtils.setManagedBeanValue("userbean.winPrintSCN", strOfQuantities);
      createPopUpWindows(url);
      clear1348Form();
    }
    else {
      if (hist) {
        JSFUtils.setManagedBeanValue("userbean.winPrintSCNHist", strOfQuantities);
        createPopUpWindows(url);
        clear1348Form();
      }
      else {
        JSFUtils.addFacesErrorMessage(selection, "Does not exist.");
      }
    }
  }

  private void clear1348Form() {

    getForm1348TxtSelection().setValue("");
  }

  /**
   * This method prints Location Labels
   */
  public void submitPrintLocation(ActionEvent event) {
    if (event != null) {
      //NO-OP can't remove parameter due to adf
    }
    boolean error = false;
    //* Check all required fields first
    if (isEmpty(getLocTxtSelection())) {
      JSFUtils.addFacesErrorMessage("MISSING REQUIRED FIELD", "Must enter a Selection");
      error = true;
    }
    if (isEmpty(getLocTxtCopies())) {
      JSFUtils.addFacesErrorMessage("MISSING REQUIRED FIELD", "Must enter number of copies");
      error = true;
    }
    else {
      if (isNaN(getLocTxtCopies())) {
        JSFUtils.addFacesErrorMessage("INVALID INPUT", "Copies must be a valid number");
        error = true;
      }
    }

    if (error) return;

    int copies = Integer.parseInt(getLocTxtCopies().getValue().toString());
    String location = getLocTxtSelection().getValue().toString().trim().toUpperCase();
    getLocTxtSelection().setValue(location);
    if (RegUtils.isNotAlphaNumeric(location)) {
      JSFUtils.addFacesErrorMessage(location, "Invalid input (alphanumeric values only)");
      return;
    }

    String prnStr = this.getWorkloadManagerService().generatePrintLocationLabel(location, copies, 0);
    if ((prnStr != null) && (prnStr.length() > 0)) {
      val labelPrintUtil = ContextUtils.getBean(LabelPrintUtil.class);
      UserInfo userInfo = (UserInfo) JSFUtils.getManagedBeanValue("userbean");
      String[] locations = new String[copies];
      Arrays.fill(locations, location);
      labelPrintUtil.printLabel(userInfo, prnStr, locations, LabelType.LOCATION_LABEL);
      displaySuccessMessage("Print successfully completed.");
      clearLocationForm();
    }
    else {
      JSFUtils.addFacesErrorMessage(location, "Does not exist.");
    }
  }

  private void clearLocationForm() {
    getLocTxtCopies().setValue("");
    getLocTxtSelection().setValue("");
  }

  /**
   * This method prints NIIN Labels
   */
  public void submitPrintNIIN(ActionEvent event) {
    if (event != null) {
      //NO-OP can't remove parameter due to adf
    }
    boolean error = false;
    //* Check all required fields first
    if (isEmpty(getNiinTxtSelection())) {
      JSFUtils.addFacesErrorMessage("MISSING REQUIRED FIELD", "Must enter a Selection");
      error = true;
    }
    if (isEmpty(getNiinTxtCopies())) {
      JSFUtils.addFacesErrorMessage("MISSING REQUIRED FIELD", "Must enter number of copies");
      error = true;
    }
    else {
      if (isNaN(getNiinTxtCopies())) {
        JSFUtils.addFacesErrorMessage("INVALID INPUT", "Copies must be a valid number");
        error = true;
      }
    }

    if (error) return;

    int copies = Integer.parseInt(getNiinTxtCopies().getValue().toString());
    String niin = getNiinTxtSelection().getValue().toString().trim().toUpperCase();
    getNiinTxtSelection().setValue(niin);
    if (RegUtils.isNotAlphaNumeric(niin)) {
      JSFUtils.addFacesErrorMessage(niin, "Invalid input (alphanumeric values only)");
      return;
    }
    String type = getNiinRadioType().getValue().toString();
    String prnStr = "";
    boolean NSN = false;
    if (type.equals("NSN")) {
      NSN = true;
    }
    else {
      NSN = false;
    }
    prnStr = this.getWorkloadManagerService().generatePrintFSCNIINLabel(niin, copies, 0, NSN);
    if ((prnStr != null) && (prnStr.length() > 0)) {
      val labelPrintUtil = ContextUtils.getBean(LabelPrintUtil.class);
      UserInfo userInfo = (UserInfo) JSFUtils.getManagedBeanValue("userbean");
      String[] niins = new String[copies];
      Arrays.fill(niins, niin);
      labelPrintUtil.printLabel(userInfo,prnStr.toString(),niins, LabelType.NIIN_LABEL);
      displaySuccessMessage("Print request submitted for " + copies + " copies of NIIN Label " + niin + ".");
      clearNIINForm();
    }
    else {
      JSFUtils.addFacesErrorMessage(niin, "Does not exist.");
    }
    prnStr = "";
  }

  private void clearNIINForm() {
    getNiinTxtCopies().setValue("");
    getNiinTxtSelection().setValue("");
  }

  /**
   * This method prints PIN Labels
   */
  public void submitPrintPIN(ActionEvent event) {
    if (event != null) {
      //NO-OP can't remove parameter due to adf
    }
    boolean error = false;
    //* Check all required fields first
    if (isEmpty(getPickTxtSelection())) {
      JSFUtils.addFacesErrorMessage("MISSING REQUIRED FIELD", "Must enter a Selection");
      error = true;
    }
    if (isEmpty(getPickTxtCopies())) {
      JSFUtils.addFacesErrorMessage("MISSING REQUIRED FIELD", "Must enter number of copies");
      error = true;
    }
    else {
      if (isNaN(getPickTxtCopies())) {
        JSFUtils.addFacesErrorMessage("INVALID INPUT", "Copies must be a valid number");
        error = true;
      }
    }

    if (error) return;

    int copies = Integer.parseInt(getPickTxtCopies().getValue().toString());
    String pin = getPickTxtSelection().getValue().toString().trim().toUpperCase();
    getPickTxtSelection().setValue(pin);
    if (RegUtils.isNotAlphaNumeric(pin)) {
      JSFUtils.addFacesErrorMessage(pin, "Invalid input (alphanumeric values only)");
      return;
    }

    String prnStr = this.getWorkloadManagerService().generatePrintPINLabel(pin, copies, 0);
    if ((prnStr != null) && (prnStr.length() > 0)) {
      // turn the comm on
      val labelPrintUtil = ContextUtils.getBean(LabelPrintUtil.class);
      UserInfo userInfo = (UserInfo) JSFUtils.getManagedBeanValue("userbean");
      String[] picks = new String[copies];
      Arrays.fill(picks, pin);
      labelPrintUtil.printLabel(userInfo, prnStr, picks, LabelType.PICK_LABEL);
      displaySuccessMessage("Print successfully completed");
      clearPickForm();
    }
    else {
      JSFUtils.addFacesErrorMessage(pin, "Does not exist.");
    }
  }

  private void clearPickForm() {
    getPickTxtCopies().setValue("");
    getPickTxtSelection().setValue("");
  }

  /**
   * This method prints SID Labels
   */
  public void submitPrintSid(ActionEvent event) {
    if (event != null) {
      //NO-OP can't remove parameter due to adf
    }
    boolean error = false;
    //* Check all required fields first
    if (getStowRadioType().getValue() == null) {
      JSFUtils.addFacesErrorMessage("MISSING REQUIRED FIELD", "Must select a Type");
      error = true;
    }
    if (isEmpty(getStowTxtSelection())) {
      JSFUtils.addFacesErrorMessage("MISSING REQUIRED FIELD", "Must enter a Selection");
      error = true;
    }
    if (isEmpty(getStowTxtCopies())) {
      JSFUtils.addFacesErrorMessage("MISSING REQUIRED FIELD", "Must enter number of copies");
      error = true;
    }
    else {
      if (isNaN(getStowTxtCopies())) {
        JSFUtils.addFacesErrorMessage("INVALID INPUT", "Copies must be a valid number");
        error = true;
      }
    }

    if (error) return;
    try {
      int copies = Integer.parseInt(getStowTxtCopies().getValue().toString()); //number of copies
      String type = getStowRadioType().getValue().toString(); // print by sid or document number
      String selection = getStowTxtSelection().getValue().toString().trim().toUpperCase();  // get the sid or document number
      getStowTxtSelection().setValue(selection);
      if (RegUtils.isNotAlphaNumeric(selection)) {
        JSFUtils.addFacesErrorMessage(selection, "Invalid input (alphanumeric values only)");
        return;
      }

      String prnStr = "";
      String sidList = "";
      if (type.equals("sid"))
        sidList = "SIDLIST," + selection;  //print by SID
      else sidList = this.getWorkloadManagerService().genrateStowListForDocNum(selection); //Printing all the SIDs of a document
      if (!sidList.contains("SIDLIST,")) {
        JSFUtils.addFacesErrorMessage(sidList, "Invalid input or No Sids.");
      }
      if (sidList.contains("SIDLIST,")) {
        int sidL = sidList.split(",").length;
        int cou1 = 1;
        while (cou1 < sidL) { // build the prn string for each sid
          prnStr += this.getWorkloadManagerService().generatePrintSIDLabel(sidList.split(",")[cou1], copies, 0);
          cou1++;
        }
        if ((prnStr != null) && (prnStr.length() > 0)) {
          // turn the comm on
          val labelPrintUtil = ContextUtils.getBean(LabelPrintUtil.class);
          UserInfo userInfo = (UserInfo) JSFUtils.getManagedBeanValue("userbean");
          String[] stows = new String[copies*(sidL -1)];//subtract 1 to account for SIDLIST at the start of the list
          for (int i = 1; i < sidL; i++) {
            Arrays.fill(stows, (i-1)*copies,(i-1)*copies + copies, sidList.split(",")[i]);
          }
          labelPrintUtil.printLabel(userInfo, prnStr, stows, LabelType.STOW_LABEL);
          displaySuccessMessage("Print request submitted for " + copies + " copies of Stow Label " + selection + ".");
          clearStowForm();
        }
        else {
          JSFUtils.addFacesErrorMessage(sidList, "Does not exist.");
        }
      }
    }
    catch (Exception e) {
      displayMessage("An error occurred while trying to reprint Stow Label.  Please try again.");
      AdfLogUtility.logException(e);
    }
  }

  private void clearStowForm() {
    getStowRadioType().setValue("");
    getStowTxtCopies().setValue("");
    getStowTxtSelection().setValue("");
  }

  /**
   * This method Send Carousel Commands
   */
  public void submitSendCarouselCommand(ActionEvent event) {
    if (event != null) {
      //NO-OP can't remove parameter due to adf
    }
    boolean error = false;
    //* Check all required fields first
    if (isEmpty(getCarouselTxtSelection())) {
      JSFUtils.addFacesErrorMessage("MISSING REQUIRED FIELD", "Must enter a Selection");
      error = true;
    }
    if (error) return;
    String prnStr = "";
    String commandStr = getCarouselTxtSelection().getValue().toString();
    if (commandStr.split(",").length == 5) {
      if (commandStr.split(",")[0].equalsIgnoreCase("H"))

        prnStr = this.getWorkloadManagerService().moveHanelVerticalCarousel(
            Integer.parseInt(commandStr.split(",")[4]));
      if (commandStr.split(",")[0].equalsIgnoreCase("R")) {
        String x = this.getWorkloadManagerService().getCarouselInfo(commandStr.split(",")[1]);
        // x is string of mech_flag,carousel_number,carousel_controller,carousel_offset,carousel_model

        prnStr = this.getWorkloadManagerService().moveRemstarHorizontalCarousel(
            Integer.parseInt(commandStr.split(",")[3].toString()),
            commandStr.split(",")[2].charAt(0),
            Integer.parseInt(x.split(",")[3].toString()),
            Integer.parseInt(x.split(",")[2].toString()));
      }
      if (commandStr.split(",")[0].equalsIgnoreCase("W"))

        prnStr = this.getWorkloadManagerService().moveWhiteHorizontalCarousel(
            Integer.parseInt(commandStr.split(",")[3].toString()),
            commandStr.split(",")[2].charAt(0));
    }
    else if (commandStr.equalsIgnoreCase("MEASURE") || commandStr.equalsIgnoreCase("CALIBRATE"))
      prnStr = commandStr;
    //Activate this when ready for COM
    if (prnStr.length() > 0) {

      // turn the comm on
      JSFUtils.setManagedBeanValue("userbean.useprintcom2", 1);
      // set the id of which comm to use from the com_port table
      JSFUtils.setManagedBeanValue("userbean.printcomport2", JSFUtils.getManagedBeanValue("userbean.comCommandIndex"));
      // set the string to go out
      JSFUtils.setManagedBeanValue("userbean.printcomstring2", prnStr);
      outputStr.setValue(prnStr);
    }
  }

  /**
   * This method creates a pop up window for a given URL
   */
  void createPopUpWindows(String url) {
    FacesContext fctx = FacesContext.getCurrentInstance();
    ExtendedRenderKitService erks = Service.getRenderKitService(fctx, ExtendedRenderKitService.class);
    StringBuilder script = new StringBuilder();
    script.append("window.open(\"").append(url).append("\");");
    erks.addScript(fctx, script.toString());
  }

  @SuppressWarnings("unused") //called from Print_Home.jspx
  public void setContainerSummaryTxtSelection(RichInputText containerSummaryTxtSelection) {
    this.containerSummaryTxtSelection = containerSummaryTxtSelection;
  }

  public RichInputText getContainerSummaryTxtSelection() {
    return containerSummaryTxtSelection;
  }

  @SuppressWarnings("unused") //called from Print_Home.jspx
  public void setFormContainerSummaryRadioType(RichSelectOneRadio formContainerSummaryRadioType) {
    this.formContainerSummaryRadioType = formContainerSummaryRadioType;
  }

  public RichSelectOneRadio getFormContainerSummaryRadioType() {
    return formContainerSummaryRadioType;
  }

  @SuppressWarnings("unused") //called from Print_Home.jspx
  public void setForm1348RadioType(RichSelectOneRadio form1348RadioType) {
    this.form1348RadioType = form1348RadioType;
  }

  public RichSelectOneRadio getForm1348RadioType() {
    return form1348RadioType;
  }

  @SuppressWarnings("unused") //called from Print_Home.jspx
  public void setForm1348TxtSelection(RichInputText form1348TxtSelection) {
    this.form1348TxtSelection = form1348TxtSelection;
  }

  public RichInputText getForm1348TxtSelection() {
    return form1348TxtSelection;
  }

  public void setFloorLoc_TxtSelection(RichInputText floorLoc_TxtSelection) {
    this.floorLoc_TxtSelection = floorLoc_TxtSelection;
  }

  public RichInputText getFloorLoc_TxtSelection() {
    return floorLoc_TxtSelection;
  }

  public void setFloorLocFloorLocationTxtSelection(RichInputText floorLocFloorLocationTxtSelection) {
    this.floorLocFloorLocationTxtSelection = floorLocFloorLocationTxtSelection;
  }

  public RichInputText getFloorLocFloorLocationTxtSelection() {
    return floorLocFloorLocationTxtSelection;
  }

  public void setFloorLocAACTxtSelection(RichInputText floorLocAACTxtSelection) {
    this.floorLocAACTxtSelection = floorLocAACTxtSelection;
  }

  public RichInputText getFloorLocAACTxtSelection() {
    return floorLocAACTxtSelection;
  }

  public void setFloorLocAreaTxtSelection(RichInputText floorLocAreaTxtSelection) {
    this.floorLocAreaTxtSelection = floorLocAreaTxtSelection;
  }

  public RichInputText getFloorLocAreaTxtSelection() {
    return floorLocAreaTxtSelection;
  }

  public void setLocTxtCopies(RichInputText locTxtCopies) {
    this.locTxtCopies = locTxtCopies;
  }

  public RichInputText getLocTxtCopies() {
    return locTxtCopies;
  }

  public void setLocTxtSelection(RichInputText locTxtSelection) {
    this.locTxtSelection = locTxtSelection;
  }

  public RichInputText getLocTxtSelection() {
    return locTxtSelection;
  }

  public void setNiinRadioType(RichSelectOneRadio niinRadioType) {
    this.niinRadioType = niinRadioType;
  }

  public RichSelectOneRadio getNiinRadioType() {
    return niinRadioType;
  }

  public void setNiinTxtCopies(RichInputText niinTxtCopies) {
    this.niinTxtCopies = niinTxtCopies;
  }

  public RichInputText getNiinTxtCopies() {
    return niinTxtCopies;
  }

  public void setNiinTxtSelection(RichInputText niinTxtSelection) {
    this.niinTxtSelection = niinTxtSelection;
  }

  public RichInputText getNiinTxtSelection() {
    return niinTxtSelection;
  }

  public void setPickTxtCopies(RichInputText pickTxtCopies) {
    this.pickTxtCopies = pickTxtCopies;
  }

  public RichInputText getPickTxtCopies() {
    return pickTxtCopies;
  }

  public void setPickTxtSelection(RichInputText pickTxtSelection) {
    this.pickTxtSelection = pickTxtSelection;
  }

  public RichInputText getPickTxtSelection() {
    return pickTxtSelection;
  }

  public void setStowTxtCopies(RichInputText stowTxtCopies) {
    this.stowTxtCopies = stowTxtCopies;
  }

  public RichInputText getStowTxtCopies() {
    return stowTxtCopies;
  }

  public void setStowTxtSelection(RichInputText stowTxtSelection) {
    this.stowTxtSelection = stowTxtSelection;
  }

  public RichInputText getStowTxtSelection() {
    return stowTxtSelection;
  }

  public void setStowRadioType(RichSelectOneRadio stowRadioType) {
    this.stowRadioType = stowRadioType;
  }

  public RichSelectOneRadio getStowRadioType() {
    return stowRadioType;
  }

  public void setManifestTxtSelection(RichInputText manifestTxtSelection) {
    this.manifestTxtSelection = manifestTxtSelection;
  }

  public RichInputText getManifestTxtSelection() {
    return manifestTxtSelection;
  }

  public void setDownloadCommandButton(RichCommandButton downloadCommandButton) {
    this.downloadCommandButton = downloadCommandButton;
  }

  public RichCommandButton getDownloadCommandButton() {
    return downloadCommandButton;
  }

  public void setCarouselTxtSelection(RichInputText carouselTxtSelection) {
    this.carouselTxtSelection = carouselTxtSelection;
  }

  public RichInputText getCarouselTxtSelection() {
    return carouselTxtSelection;
  }

  public void setOutputStr(RichOutputText outputStr) {
    this.outputStr = outputStr;
  }

  public RichOutputText getOutputStr() {
    return outputStr;
  }
}

