package mil.stratis.view.shipping;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.common.util.RegUtils;
import mil.stratis.common.util.Util;
import mil.stratis.model.services.ShippingServiceImpl;
import mil.stratis.view.util.ADFUtils;
import oracle.adf.model.binding.DCIteratorBinding;
import oracle.adf.view.rich.component.rich.input.RichInputText;
import oracle.adf.view.rich.component.rich.input.RichSelectOneChoice;
import oracle.adf.view.rich.context.AdfFacesContext;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import java.util.HashMap;
import java.util.Locale;

@Slf4j
public class TransshipmentHandler extends ShippingHandler {

  private static final int MAX_LEN_BARCODE = 10;
  private static final int MAX_LEN_CONTRACT_NUM = 13;
  private static final int MAX_LEN_DOCUMENT_NUM = 14;
  private static final int MAX_LEN_DEMAND_SUFFIX = 1;
  private static final int MAX_LEN_AAC = 6;
  private static final int MIN_LEN_TRACKING_NUM = 10;
  private static final int MAX_LEN_TRACKING_NUM = 18;
  private static final int MAX_LEN_NSN = 13;
  private static final int MAX_LEN_SMIC = 2;
  private static final int MAX_LEN_CALL_NUM = 5;
  private static final int MAX_LEN_LINE_NUM = 5;
  private static final int MAX_LEN_SHIPMENT_NUM = 7;
  private static final int MAX_LEN_TCN = 17;
  private static final int MAX_LEN_RIC_FROM = 3;
  private static final int MAX_LEN_UI = 2;
  private static final String RENDER_MINI_AAC_FORM_NAME = "renderMiniAACForm";

  private static final String UPPERCASE = "text-transform:uppercase;";
  private static final String ITERATOR_NAME = "CustomerView1Iterator";

  private boolean renderMiniAACForm;

  private transient RichInputText txtTCNTcn;
  private transient RichInputText txtConsigneeTcn;
  private transient RichInputText txtTrackingNumTcn;
  private transient RichInputText txtBarcodeTcn;
  private transient RichInputText txtDocNumTcn;
  private transient RichInputText txtSuffixTcn;
  private transient RichInputText txtNSNTcn;
  private transient RichInputText txtSMICTcn;
  private transient RichInputText txtRicFromTcn;
  private transient RichInputText txtUITcn;
  private transient RichInputText txtQtyTcn;
  private transient RichInputText txtPriceTcn;
  private transient RichSelectOneChoice ccTcn;
  private transient RichInputText restrictShipTcn;
  private transient RichInputText supportedTcn;
  private transient RichInputText aacCreateTcn;
  private transient RichInputText nameCreateTcn;
  private transient RichInputText address1CreateTcn;
  private transient RichInputText zipCodeCreateTcn;
  private transient RichInputText address2CreateTcn;
  private transient RichInputText stateCreateTcn;
  private transient RichInputText cityCreateTcn;

  private transient RichInputText txtDocNumDocnum;
  private transient RichInputText txtSuffixDocnum;
  private transient RichInputText txtBarcodeDocnum;
  private transient RichInputText txtAACDocnum;
  private transient RichInputText txtTrackingNumDocnum;
  private transient RichInputText txtNSNDocnum;
  private transient RichInputText txtSMICDocnum;
  private transient RichInputText txtRicFromDocnum;
  private transient RichInputText txtUIDocnum;
  private transient RichInputText txtQtyDocnum;
  private transient RichInputText txtPriceDocnum;
  private transient RichSelectOneChoice ccDocnum;
  private transient RichInputText restrictShipDocnum;
  private transient RichInputText supportedDocnum;
  private transient RichInputText aacCreateDocnum;
  private transient RichInputText nameCreateDocnum;
  private transient RichInputText address1CreateDocnum;
  private transient RichInputText zipCodeCreateDocnum;
  private transient RichInputText address2CreateDocnum;
  private transient RichInputText stateCreateDocnum;
  private transient RichInputText cityCreateDocnum;

  private transient RichInputText txtContractNumCtr;
  private transient RichInputText txtBarcodeCtr;
  private transient RichInputText txtQtyInvoicedCtr;
  private transient RichInputText txtQtyReceivedCtr;
  private transient RichInputText txtDocNumCtr;
  private transient RichInputText txtSuffixCtr;
  private transient RichInputText txtAACCtr;
  private transient RichInputText txtTrackingNumCtr;
  private transient RichInputText txtNSNCtr;
  private transient RichInputText txtSMICCtr;
  private transient RichInputText txtLineNumCtr;
  private transient RichInputText txtCallNumCtr;
  private transient RichInputText txtShipmentNumCtr;
  private transient RichInputText txtBilledCtr;
  private transient RichInputText txtTailDateCtr;
  private transient RichSelectOneChoice purposeCtr;
  private transient RichSelectOneChoice ccCtr;
  private transient RichInputText restrictShipCtr;
  private transient RichInputText supportedCtr;
  private transient RichInputText aacCreateCtr;
  private transient RichInputText nameCreateCtr;
  private transient RichInputText address1CreateCtr;
  private transient RichInputText zipCodeCreateCtr;
  private transient RichInputText address2CreateCtr;
  private transient RichInputText stateCreateCtr;
  private transient RichInputText cityCreateCtr;

  public TransshipmentHandler() {
    init();
  }

  private void resetFocusTcn() {
    getTxtTCNTcn().setContentStyle(UPPERCASE);
    getTxtConsigneeTcn().setContentStyle(UPPERCASE);
    getTxtDocNumTcn().setContentStyle(UPPERCASE);
    getTxtSuffixTcn().setContentStyle(UPPERCASE);
    getTxtBarcodeTcn().setContentStyle(UPPERCASE);
    getTxtNSNTcn().setContentStyle(UPPERCASE);
    getTxtTrackingNumTcn().setContentStyle(UPPERCASE);
    getTxtSMICTcn().setContentStyle(UPPERCASE);
    getTxtRicFromTcn().setContentStyle(UPPERCASE);
    getTxtUITcn().setContentStyle(UPPERCASE);
    getTxtQtyTcn().setContentStyle("");
    getTxtPriceTcn().setInlineStyle("");

    getSupportedTcn().setValue("N");
    getRestrictShipTcn().setValue("N");
  }

  private void resetFormTcn() {
    resetFocusTcn();

    getTxtTCNTcn().setValue("");
    getTxtConsigneeTcn().setValue("");
    getTxtDocNumTcn().setValue("");
    getTxtSuffixTcn().setValue("");
    getTxtBarcodeTcn().setValue("");
    getTxtNSNTcn().setValue("");
    getTxtTrackingNumTcn().setValue("");
    getTxtSMICTcn().setValue("");
    getTxtRicFromTcn().setValue("");
    getTxtUITcn().setValue("");
    getTxtQtyTcn().setValue("");
    getTxtPriceTcn().setValue("");
    getCcTcn().setValue(null);
  }

  private void resetAACFormTcn() {
    getNameCreateTcn().setValue("");
    getAddress1CreateTcn().setValue("");
    getAddress2CreateTcn().setValue("");
    getZipCodeCreateTcn().setValue("");
    getStateCreateTcn().setValue("");
    getCityCreateTcn().setValue("");
  }

  private void disableFormTcn() {
    getTxtTCNTcn().setDisabled(true);
    getTxtConsigneeTcn().setDisabled(true);
    getTxtDocNumTcn().setDisabled(true);
    getTxtSuffixTcn().setDisabled(true);
    getTxtBarcodeTcn().setDisabled(true);
    getTxtNSNTcn().setDisabled(true);
    getTxtTrackingNumTcn().setDisabled(true);
    getTxtSMICTcn().setDisabled(true);
    getTxtRicFromTcn().setDisabled(true);
    getTxtUITcn().setDisabled(true);
    getTxtQtyTcn().setDisabled(true);
    getTxtPriceTcn().setDisabled(true);
    getCcTcn().setDisabled(true);
  }

  private void enableFormTcn() {
    getTxtTCNTcn().setDisabled(false);
    getTxtConsigneeTcn().setDisabled(false);
    getTxtDocNumTcn().setDisabled(false);
    getTxtSuffixTcn().setDisabled(false);
    getTxtBarcodeTcn().setDisabled(false);
    getTxtNSNTcn().setDisabled(false);
    getTxtTrackingNumTcn().setDisabled(false);
    getTxtSMICTcn().setDisabled(false);
    getTxtRicFromTcn().setDisabled(false);
    getTxtUITcn().setDisabled(false);
    getTxtQtyTcn().setDisabled(false);
    getTxtPriceTcn().setDisabled(false);
    getCcTcn().setDisabled(false);
  }

  public void submitByTCNAAC(ActionEvent actionEvent) {
    try {
      DCIteratorBinding iter = ADFUtils.findIterator(ITERATOR_NAME);
      disableFormTcn();
      // commit the change
      iter.getDataControl().commitTransaction();
      submitByTCN(actionEvent);
    }
    catch (Exception e) {
      log.trace(e.getLocalizedMessage());
    }
  }

  public void resetByTCNAAC(ActionEvent actionEvent) {
    try {
      resetAACFormTcn();
      enableFormTcn();
      cancelTransshipment(actionEvent);
    }
    catch (Exception e) {
      log.trace(e.getLocalizedMessage());
    }
  }

  public void cancelTransshipment(@SuppressWarnings("all") ActionEvent actionEvent) {
    try {
      //* clear renderMiniAACForm
      setRenderMiniAACForm(false);
      //* for use in the web, maintaining state
      AdfFacesContext afContext = AdfFacesContext.getCurrentInstance();
      afContext.getPageFlowScope().put(RENDER_MINI_AAC_FORM_NAME, renderMiniAACForm);
    }
    catch (Exception e) {
      log.trace(e.getLocalizedMessage());
    }
  }

  public void submitByTCN(@SuppressWarnings("all") ActionEvent actionEvent) {
    //* clear border of previous red outlined boxes
    resetFocusTcn();
    boolean moreRequiredFields = false;
    FacesContext facesContext = FacesContext.getCurrentInstance();

    //* clear renderMiniAACForm
    setRenderMiniAACForm(false);
    //* for use in the web, maintaining state
    AdfFacesContext afContext = AdfFacesContext.getCurrentInstance();
    afContext.getPageFlowScope().put(RENDER_MINI_AAC_FORM_NAME, renderMiniAACForm);

    //* check required fields first
    Object objTcn = getTxtTCNTcn().getValue();
    String tcn = isEmpty(objTcn) ? "" : objTcn.toString().trim().toUpperCase(Locale.US);
    if (tcn.isEmpty()) {
      moreRequiredFields = true;
      facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
          "Transportation Control Number (TCN) is a required field.", null));
      setFocus(getTxtTCNTcn());
    }
    else {
      //* tcn must be 17 characters
      if (tcn.length() != MAX_LEN_TCN) {
        facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
            "Transportation Control Number (TCN) must be " + MAX_LEN_TCN + " alphanumeric characters.", null));
        setFocus(getTxtTCNTcn());
        moreRequiredFields = true;
      }
      else {
        if (RegUtils.isNotAlphaNumeric(tcn)) {
          facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
              "Transportation Control Number (TCN) must be " + MAX_LEN_TCN + " alphanumeric characters.", null));
          setFocus(getTxtTCNTcn());
          moreRequiredFields = true;
        }
        //* first character can not be a Y or y
        if (RegUtils.containsFirstCharY(tcn)) {
          facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
              "First Character of Transportation Control Number (TCN) cannot be a 'Y'.", null));
          setFocus(getTxtTCNTcn());
          moreRequiredFields = true;
        }
      }
    }

    Object objBarcode = getTxtBarcodeTcn().getValue();
    String barcode = isEmpty(objBarcode) ? "" : objBarcode.toString().trim().toUpperCase(Locale.US);
    if (barcode.isEmpty()) {
      moreRequiredFields = true;
      facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
          "Shipping Barcode is a required field.", null));
      setFocus(getTxtBarcodeTcn());
    }
    else {
      //* barcode must be 13 characters
      if (barcode.length() > MAX_LEN_BARCODE || barcode.length() < 5) {
        facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
            "Shipping Barcode must be greater than or equal to 5 and less than or equal to " + MAX_LEN_BARCODE + " alphanumeric characters.", null));
        setFocus(getTxtBarcodeTcn());
        moreRequiredFields = true;
      }
      else {
        if (RegUtils.isNotAlphaNumeric(barcode)) {
          facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
              "Shipping Barcode must be alphanumeric characters.", null));
          setFocus(getTxtBarcodeTcn());
          moreRequiredFields = true;
        }
      }
    }

    if (moreRequiredFields) return;

    //* verify the barcode and tcn do not already exist
    StringBuilder msg = new StringBuilder();
    //* invoke the service to update the data
    ShippingServiceImpl shippingService = getShippingServiceModule();

    if (shippingService.existTCN(tcn)) {
      msg.append("TCN [").append(tcn).append("] already exist in Shipping.");
      facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg.toString(), null));
      setFocus(getTxtTCNTcn());
      return;
    }
    if (shippingService.existBarcode(barcode)) {
      msg.append("Shipping Barcode [").append(barcode).append("] already exist.");
      facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg.toString(), null));
      setFocus(getTxtBarcodeTcn());
      return;
    }

    boolean moreInvalidFields = false;

    //* document number must be 14 characters
    Object objDocumentNumber = getTxtDocNumTcn().getValue();
    String documentNumber = isEmpty(objDocumentNumber) ? "" : objDocumentNumber.toString().trim().toUpperCase(Locale.US);
    if (documentNumber.isEmpty()) {
      //* Doc # Set to first 14 characters of TCN
      documentNumber = tcn.substring(0, 14);
    }
    else {
      if (documentNumber.length() != MAX_LEN_DOCUMENT_NUM) {
        facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
            "Document Number must be " + MAX_LEN_DOCUMENT_NUM + " alphanumeric characters.", null));
        setFocus(getTxtDocNumTcn());
        moreInvalidFields = true;
      }
      else {
        if (RegUtils.isNotAlphaNumeric(documentNumber)) {
          facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
              "Document Number must be " + MAX_LEN_DOCUMENT_NUM + " alphanumeric characters.", null));
          setFocus(getTxtDocNumTcn());
          moreInvalidFields = true;
        }
        //* first character can not be a Y or y
        if (RegUtils.containsFirstCharY(documentNumber)) {
          facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
              "First Character of Document Number cannot be a 'Y'.", null));
          setFocus(getTxtDocNumTcn());
          moreInvalidFields = true;
        }
      }
    }

    Object objDemandSuffix = getTxtSuffixTcn().getValue();
    String demandSuffix = isEmpty(objDemandSuffix) ? "" : objDemandSuffix.toString().trim().toUpperCase(Locale.US);
    if (!demandSuffix.isEmpty()) {
      //* demand suffix must be 1 character
      if (demandSuffix.length() != MAX_LEN_DEMAND_SUFFIX) {
        facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
            "Demand Suffix must be " + MAX_LEN_DEMAND_SUFFIX + " alphanumeric character.", null));
        setFocus(getTxtSuffixTcn());
        moreInvalidFields = true;
      }
      else {
        if (RegUtils.isNotAlphaNumeric(demandSuffix)) {
          facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
              "Demand Suffix must be " + MAX_LEN_DEMAND_SUFFIX + " alphanumeric character.", null));
          setFocus(getTxtSuffixTcn());
          moreInvalidFields = true;
        }
      }
    }

    HashMap<String, String> hm = new HashMap<>();
    //* validate Tracking Number
    msg = isValidTrackingNum(getTxtTrackingNumTcn());
    if (msg.length() > 0) {
      facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg.toString(), null));
      msg.setLength(0);
      setFocus(getTxtTrackingNumTcn());
      moreInvalidFields = true;
    }
    else {
      Object objTrackingNumber = getTxtTrackingNumTcn().getValue();
      hm.put("tracking_number", isEmpty(objTrackingNumber) ? "" : objTrackingNumber.toString().trim().toUpperCase());
    }

    //* validate Unit of Issue (U/I)
    msg = isValidAlphaNumeric(getTxtUITcn(), "Unit of Issue (U/I)", MAX_LEN_UI);
    if (msg.length() > 0) {
      facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg.toString(), null));
      msg.setLength(0);
      setFocus(getTxtUITcn());
      moreInvalidFields = true;
    }
    else {
      Object objUi = getTxtUITcn().getValue();
      hm.put("ui", isEmpty(objUi) ? "EA" : objUi.toString().trim().toUpperCase());
    }

    //* validate Quantity
    msg = isValidNumeric(getTxtQtyTcn());
    if (msg.length() > 0) {
      facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg.toString(), null));
      msg.setLength(0);
      setFocus(getTxtQtyTcn());
      moreInvalidFields = true;
    }
    else {
      Object objQuantity = getTxtQtyTcn().getValue();
      hm.put("quantity", isEmpty(objQuantity) ? "1" : objQuantity.toString().trim());
    }

    //* Price must be numeric only, if not empty
    Object objPrice = getTxtPriceTcn().getValue();
    if (!isEmpty(objPrice)) {
      String price = objPrice.toString().trim();
      if (RegUtils.isNotDollar(price)) {
        facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
            "Price must be of the decimal format #.#.", null));
        setFocus(getTxtPriceTcn());
        moreInvalidFields = true;
      }
      else {
        hm.put("price", price);
      }
    }

    //* cognizance and condition code
    Object objCc = getCcTcn().getValue();
    hm.put("cc", isEmpty(objCc) ? "A" : objCc.toString().trim().toUpperCase());
    Object objSmic = getTxtSMICTcn().getValue();
    hm.put("smic", isEmpty(objSmic) ? "" : objSmic.toString().trim().toUpperCase());

    Object ricTcn = getTxtRicFromTcn().getValue();
    if (!isEmpty(ricTcn)) {
      //* validate RIC From, must be 3 characters
      msg = isValidAlphaNumeric(getTxtRicFromTcn(), "Routing Identification Code (RIC) From", MAX_LEN_RIC_FROM);
      if (msg.length() > 0) {
        facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg.toString(), null));
        msg.setLength(0);
        setFocus(getTxtRicFromTcn());
        moreInvalidFields = true;
      }
      else {
        String resultRicTcn = shippingService.getRoutingId(ricTcn.toString().trim().toUpperCase());
        if (isEmpty(resultRicTcn)) {
          msg.append("Routing Identifier Code (RIC) [").append(ricTcn).append("] not a valid route.");
          facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg.toString(), null));
          msg.setLength(0);
          setFocus(getTxtRicFromTcn());
          moreInvalidFields = true;
        }
        else {
          hm.put("ricFrom", ricTcn.toString().trim().toUpperCase());
          hm.put("route_id", resultRicTcn);
        }
      }
    }

    Object nsnTcn = getTxtNSNTcn().getValue();
    if (!isEmpty(nsnTcn)) {
      //* validate National Stock Number (NSN) must be 13 characters, if not empty
      msg = isValidAlphaNumeric(getTxtNSNTcn(), "National Stock Number (NSN)", MAX_LEN_NSN);
      if (msg.length() > 0) {
        facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg.toString(), null));
        msg.setLength(0);
        setFocus(getTxtNSNTcn());
        moreInvalidFields = true;
      }
      else {
        String resultNiinIdTcn = shippingService.findNiinId(nsnTcn.toString().trim().toUpperCase());
        if (isEmpty(resultNiinIdTcn)) {
          msg.append("National Stock Number (NSN) [").append(nsnTcn).append("] not found in system.");
          facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg.toString(), null));
          msg.setLength(0);
          setFocus(getTxtNSNTcn());
          moreInvalidFields = true;
        }
        else {
          hm.put("nsn", nsnTcn.toString().trim().toUpperCase());
          hm.put("niin_id", resultNiinIdTcn);
        }
      }
    }

    //* 03/28/2011 - add for transshipment AFGH requirement
    //* if document number, niin, qty on dasf, then allow to continue transship
    //* else, display message to user - Item not on any DASF, Take gear to Receiving.
    if (moreInvalidFields) {
      return;
    }

    String refDasfId = shippingService.isDASF(documentNumber, "", Util.cleanInt(hm.get("quantity")));

    if (Util.isEmpty(refDasfId)) {

      facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Item not on any DASF, Take gear to Receiving.", null));

      //* send to receive, clear all fields
      resetFormTcn();
      enableFormTcn();
      return;
    }

    if (Util.isNotEmpty(refDasfId)) {
      Object consigneeTcn = getTxtConsigneeTcn().getValue();
      if (isEmpty(consigneeTcn)) {
        //* verify 1st 6 characters of tcn, if invalid, fix or consignee is required
        String aac = tcn.substring(0, 6).toUpperCase();
        String resultCustomerId = shippingService.getCustomerId(aac);
        if (isEmpty(resultCustomerId)) {
          msg.append("1st 6 characters of TCN [")
              .append(aac)
              .append("] not a valid customer.  ")
              .append("Either enter a valid customer in the first 6 characters of TCN or as the Consignee.");
          facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg.toString(), null));
          msg.setLength(0);
          setFocus(getTxtTCNTcn());

          setRenderMiniAACForm(true);
          afContext.getPageFlowScope().put(RENDER_MINI_AAC_FORM_NAME, renderMiniAACForm);
          getAacCreateTcn().setValue(aac);
          disableFormTcn();
          return;
        }
        else {
          if (shippingService.isCustomerRestricted(Integer.parseInt(resultCustomerId))) {
            msg.append("AAC [").append(aac).append("] is a RESTRICTED customer.");
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg.toString(), null));
            msg.setLength(0);
            setFocus(getTxtConsigneeTcn());
            moreInvalidFields = true;
          }
          else {
            hm.put("aac", aac);
            hm.put("customer_id", resultCustomerId);
          }
        }
      }
      else {
        //* validate AAC
        msg = isValidAAC(getTxtConsigneeTcn());
        if (msg.length() > 0) {
          facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg.toString(), null));
          msg.setLength(0);
          setFocus(getTxtConsigneeTcn());
          moreInvalidFields = true;
        }
        else {
          String resultConsigneeTcn = shippingService.getCustomerId(consigneeTcn.toString().trim().toUpperCase());
          if (isEmpty(resultConsigneeTcn)) {
            val consignee = consigneeTcn.toString().trim().toUpperCase();
            msg.append("Consignee [").append(consignee).append("] not a valid customer.");
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg.toString(), null));
            msg.setLength(0);
            setFocus(getTxtConsigneeTcn());

            setRenderMiniAACForm(true);
            afContext.getPageFlowScope().put(RENDER_MINI_AAC_FORM_NAME, renderMiniAACForm);
            getAacCreateTcn().setValue(consignee);
            disableFormTcn();
            return;
          }
          else {
            if (shippingService.isCustomerRestricted(Integer.parseInt(resultConsigneeTcn))) {
              msg.append("AAC [").append(consigneeTcn.toString().toUpperCase()).append("] is a RESTRICTED customer.");
              facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg.toString(), null));
              msg.setLength(0);
              setFocus(getTxtConsigneeTcn());
              moreInvalidFields = true;
            }
            else {
              hm.put("aac", consigneeTcn.toString().trim().toUpperCase());
              hm.put("customer_id", resultConsigneeTcn);
            }
          }
        }
      }

      //* there are no invalid fields, submit to database
      if (!moreInvalidFields) {

        hm.put("tcn", tcn);
        hm.put("document_number", documentNumber);
        hm.put("suffix", demandSuffix);
        hm.put("user_id", String.valueOf(getUserId()));
        hm.put("workstation_id", String.valueOf(getWorkstationId()));
        hm.put("barcode", barcode);

        msg = shippingService.transship(hm, getUserId(), getWorkstationId());
        if (msg.length() > 0) {
          facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg.toString(), null));
        }
        else {
          //* success, clear all fields
          resetFormTcn();
          enableFormTcn();
          facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Transshipment added successfully.", null));
          //* 03/28/2011 - add for transshipment AFGH requirement
          //* update document number, niin, qty on dasf reflective of successful transshipment
          shippingService.updateDASF(refDasfId, Util.cleanInt(hm.get("quantity")));
        }
        log.debug("[debug] finished! transshipment complete...");
      }
    }
  }

  private void resetFocusDocnum() {
    getTxtDocNumDocnum().setContentStyle(UPPERCASE);
    getTxtSuffixDocnum().setContentStyle(UPPERCASE);
    getTxtBarcodeDocnum().setContentStyle(UPPERCASE);
    getTxtAACDocnum().setContentStyle(UPPERCASE);
    getTxtTrackingNumDocnum().setContentStyle(UPPERCASE);
    getTxtNSNDocnum().setContentStyle(UPPERCASE);
    getTxtSMICDocnum().setContentStyle(UPPERCASE);
    getTxtRicFromDocnum().setContentStyle(UPPERCASE);
    getTxtUIDocnum().setContentStyle(UPPERCASE);
    getTxtQtyDocnum().setInlineStyle("");
    getTxtPriceDocnum().setInlineStyle("");

    getSupportedDocnum().setValue("N");
    getRestrictShipDocnum().setValue("N");
  }

  private void resetFormDocnum() {
    resetFocusDocnum();

    getTxtDocNumDocnum().setValue("");
    getTxtSuffixDocnum().setValue("");
    getTxtBarcodeDocnum().setValue("");
    getTxtAACDocnum().setValue("");
    getTxtTrackingNumDocnum().setValue("");
    getTxtNSNDocnum().setValue("");
    getTxtSMICDocnum().setValue("");
    getTxtRicFromDocnum().setValue("");
    getTxtUIDocnum().setValue("");
    getTxtQtyDocnum().setValue("");
    getTxtPriceDocnum().setValue("");
    getCcDocnum().setValue(null);
  }

  private void resetAACFormDocnum() {
    getNameCreateDocnum().setValue("");
    getAddress1CreateDocnum().setValue("");
    getAddress2CreateDocnum().setValue("");
    getZipCodeCreateDocnum().setValue("");
    getStateCreateDocnum().setValue("");
    getCityCreateDocnum().setValue("");
  }

  private void disableFormDocnum() {
    getTxtDocNumDocnum().setDisabled(true);
    getTxtSuffixDocnum().setDisabled(true);
    getTxtBarcodeDocnum().setDisabled(true);
    getTxtAACDocnum().setDisabled(true);
    getTxtTrackingNumDocnum().setDisabled(true);
    getTxtNSNDocnum().setDisabled(true);
    getTxtSMICDocnum().setDisabled(true);
    getTxtRicFromDocnum().setDisabled(true);
    getTxtUIDocnum().setDisabled(true);
    getTxtQtyDocnum().setDisabled(true);
    getTxtPriceDocnum().setDisabled(true);
    getCcDocnum().setDisabled(true);
  }

  private void enableFormDocnum() {
    getTxtDocNumDocnum().setDisabled(false);
    getTxtSuffixDocnum().setDisabled(false);
    getTxtBarcodeDocnum().setDisabled(false);
    getTxtAACDocnum().setDisabled(false);
    getTxtTrackingNumDocnum().setDisabled(false);
    getTxtNSNDocnum().setDisabled(false);
    getTxtSMICDocnum().setDisabled(false);
    getTxtRicFromDocnum().setDisabled(false);
    getTxtUIDocnum().setDisabled(false);
    getTxtQtyDocnum().setDisabled(false);
    getTxtPriceDocnum().setDisabled(false);
    getCcDocnum().setDisabled(false);
  }

  @SuppressWarnings("unused")  //called from .jspx
  public void resetByDocNumAAC(ActionEvent actionEvent) {
    try {
      resetAACFormDocnum();
      enableFormDocnum();
      cancelTransshipment(actionEvent);
    }
    catch (Exception e) {
      log.trace(e.getLocalizedMessage());
    }
  }

  public void submitByDocNumAAC(ActionEvent actionEvent) {
    try {
      DCIteratorBinding iter = ADFUtils.findIterator(ITERATOR_NAME);
      disableFormDocnum();
      // commit the change
      iter.getDataControl().commitTransaction();
      submitByDocNum(actionEvent);
    }
    catch (Exception e) {
      log.trace(e.getLocalizedMessage());
    }
  }

  public void submitByDocNum(@SuppressWarnings("all") ActionEvent actionEvent) {
    //* clear border of previous red outlined boxes
    resetFocusDocnum();
    boolean moreRequiredFields = false;
    FacesContext facesContext = FacesContext.getCurrentInstance();

    //* clear renderMiniAACForm
    setRenderMiniAACForm(false);
    //* for use in the web, maintaining state
    AdfFacesContext afContext = AdfFacesContext.getCurrentInstance();
    afContext.getPageFlowScope().put(RENDER_MINI_AAC_FORM_NAME, renderMiniAACForm);

    //* check required fields first
    Object objDocumentNumber = getTxtDocNumDocnum().getValue();
    String documentNumber = isEmpty(objDocumentNumber) ? "" : objDocumentNumber.toString().trim().toUpperCase(Locale.US);
    if (documentNumber.isEmpty()) {
      moreRequiredFields = true;
      facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
          "Document Number is a required field.", null));
      setFocus(getTxtDocNumDocnum());
    }
    else {
      //* document number must be 14 characters
      if (documentNumber.length() != MAX_LEN_DOCUMENT_NUM) {
        facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
            "Document Number must be " + MAX_LEN_DOCUMENT_NUM + " alphanumeric characters.", null));
        setFocus(getTxtDocNumDocnum());
        moreRequiredFields = true;
      }
      else {
        if (RegUtils.isNotAlphaNumeric(documentNumber)) {
          facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
              "Document Number must be " + MAX_LEN_DOCUMENT_NUM + " alphanumeric characters.", null));
          setFocus(getTxtDocNumDocnum());
          moreRequiredFields = true;
        }
        //* first character can not be a Y or y
        if (RegUtils.containsFirstCharY(documentNumber)) {
          facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "First Character of Document Number cannot be a 'Y'.", null));
          setFocus(getTxtDocNumDocnum());
          moreRequiredFields = true;
        }
      }
    }

    Object objBarcode = getTxtBarcodeDocnum().getValue();
    String barcode = isEmpty(objBarcode) ? "" : objBarcode.toString().trim().toUpperCase(Locale.US);
    if (barcode.isEmpty()) {
      moreRequiredFields = true;
      facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Shipping Barcode is a required field.", null));
      setFocus(getTxtBarcodeDocnum());
    }
    else {
      //* barcode must be 13 characters
      if (barcode.length() > MAX_LEN_BARCODE || barcode.length() < 5) {
        facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
            "Shipping Barcode must be greater than or equal to 5 and less than or equal to " + MAX_LEN_BARCODE + " alphanumeric characters.", null));
        setFocus(getTxtBarcodeDocnum());
        moreRequiredFields = true;
      }
      else {
        if (RegUtils.isNotAlphaNumeric(barcode)) {
          facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
              "Shipping Barcode must be alphanumeric characters.", null));
          setFocus(getTxtBarcodeDocnum());
          moreRequiredFields = true;
        }
      }
    }

    if (moreRequiredFields) return;

    //* verify the barcode does not already exist
    //* invoke the service to update the data
    ShippingServiceImpl shippingService = getShippingServiceModule();

    if (shippingService.existBarcode(barcode)) {
      facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Shipping Barcode [" + barcode + "] already exist.", null));
      setFocus(getTxtBarcodeDocnum());
      return;
    }

    boolean moreInvalidFields = false;
    HashMap<String, String> hm = new HashMap<>();
    StringBuilder msg;

    //* demand suffix must be 1 character
    Object objDemandSuffix = getTxtSuffixDocnum().getValue();
    String demandSuffix = isEmpty(objDemandSuffix) ? "" : objDemandSuffix.toString().trim().toUpperCase(Locale.US);
    if (!demandSuffix.isEmpty()) {
      if (demandSuffix.length() != MAX_LEN_DEMAND_SUFFIX) {
        facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
            "Demand Suffix must be " + MAX_LEN_DEMAND_SUFFIX + " alphanumeric character.", null));
        setFocus(getTxtSuffixDocnum());
        moreInvalidFields = true;
      }
      else if (RegUtils.isNotAlphaNumeric(demandSuffix)) {
        facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
            "Demand Suffix must be " + MAX_LEN_DEMAND_SUFFIX + " alphanumeric character.", null));
        setFocus(getTxtSuffixDocnum());
        moreInvalidFields = true;
      }
    }

    //* validate Tracking Number
    msg = isValidTrackingNum(getTxtTrackingNumDocnum());
    if (msg.length() > 0) {
      facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg.toString(), null));
      msg.setLength(0);
      setFocus(getTxtTrackingNumDocnum());
      moreInvalidFields = true;
    }
    else {
      Object objTrackingNum = getTxtTrackingNumDocnum().getValue();
      if (!isEmpty(objTrackingNum))
        hm.put("tracking_number", objTrackingNum.toString().trim().toUpperCase());
    }

    //* validate Unit of Issue (U/I)
    msg = isValidAlphaNumeric(getTxtUIDocnum(), "Unit of Issue (U/I)", MAX_LEN_UI);
    if (msg.length() > 0) {
      facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg.toString(), null));
      msg.setLength(0);
      setFocus(getTxtUIDocnum());
      moreInvalidFields = true;
    }
    else {
      Object objUi = getTxtUIDocnum().getValue();
      if (!isEmpty(objUi))
        hm.put("ui", objUi.toString().trim().toUpperCase());
    }

    //* validate Quantity
    msg = isValidNumeric(getTxtQtyDocnum());
    if (msg.length() > 0) {
      facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg.toString(), null));
      msg.setLength(0);
      setFocus(getTxtQtyDocnum());
      moreInvalidFields = true;
    }
    else {
      Object objQty = getTxtQtyDocnum().getValue();
      hm.put("quantity", isEmpty(objQty) ? "1" : objQty.toString().trim());
    }

    //* Price must be numeric only, if not empty
    Object objPrice = getTxtPriceDocnum().getValue();
    if (!isEmpty(objPrice)) {
      String price = objPrice.toString().trim();
      if (RegUtils.isNotDollar(price)) {
        facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
            "Price must be of the decimal format #.#.", null));
        setFocus(getTxtPriceDocnum());
        moreInvalidFields = true;
      }
      else {
        hm.put("price", price);
      }
    }

    //* cognizance and condition code
    Object objCc = getCcDocnum().getValue();
    hm.put("cc", isEmpty(objCc) ? "A" : objCc.toString().trim().toUpperCase());
    Object objSmic = getTxtSMICDocnum().getValue();
    hm.put("smic", isEmpty(objSmic) ? "" : objSmic.toString().trim().toUpperCase());

    //* validate RIC From, must be 3 characters
    msg = isValidAlphaNumeric(getTxtRicFromDocnum(), "RIC From", MAX_LEN_RIC_FROM);
    if (msg.length() > 0) {
      facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg.toString(), null));
      msg.setLength(0);
      setFocus(getTxtRicFromDocnum());
      moreInvalidFields = true;
    }
    else {
      Object ricDocnum = getTxtRicFromDocnum().getValue();
      if (!isEmpty(ricDocnum)) {
        String resultRicDocnum = shippingService.getRoutingId(ricDocnum.toString().trim().toUpperCase());
        if (isEmpty(resultRicDocnum)) {
          msg.append("Routing Identifier Code (RIC) [").append(ricDocnum).append("] not a valid route or not found in system.");
          facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg.toString(), null));
          msg.setLength(0);
          setFocus(getTxtRicFromDocnum());
          moreInvalidFields = true;
        }
        else {
          hm.put("ricFrom", ricDocnum.toString().trim().toUpperCase());
          hm.put("route_id", resultRicDocnum);
        }
      }
    }

    Object nsnDocnum = getTxtNSNDocnum().getValue();
    if (!isEmpty(nsnDocnum)) {
      val nsn = nsnDocnum.toString().trim().toUpperCase();
      String resultNiinIdDocnum = shippingService.findNiinId(nsn);
      if (isEmpty(resultNiinIdDocnum)) {
        msg.append("National Stock Number (NSN) [").append(nsn).append("] not found in system.");
        facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg.toString(), null));
        msg.setLength(0);
        setFocus(getTxtNSNDocnum());
        moreInvalidFields = true;
      }
      else {
        hm.put("nsn", nsnDocnum.toString().trim().toUpperCase());
        hm.put("niin_id", resultNiinIdDocnum);
      }
    }

    //* 03/28/2011 - add for transshipment AFGH requirement
    //* if document number, niin, qty on dasf, then allow to continue transship
    //* else, display message to user - Item not on any DASF, Take gear to Receiving.
    if (moreInvalidFields) {
      return;
    }

    String refDasfId = shippingService.isDASF(documentNumber, "", Util.cleanInt(hm.get("quantity")));

    if (Util.isEmpty(refDasfId)) {

      facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Item not on any DASF, Take gear to Receiving.", null));

      //* send to receive, clear all fields
      resetFormDocnum();
      enableFormDocnum();
      return;
    }

    if (Util.isNotEmpty(refDasfId)) {
      Object aacDocnum = getTxtAACDocnum().getValue();
      if (isEmpty(aacDocnum)) {
        //* verify 1st 6 characters of tcn, if invalid, fix or consignee is required
        String aac = documentNumber.substring(0, 6).toUpperCase();
        String resultCustomerId = shippingService.getCustomerId(aac);
        if (isEmpty(resultCustomerId)) {
          msg.append("1st 6 characters of Document Number [").append(aac).append("] not a valid customer.  ").append("Either enter a valid customer in the first 6 characters of Document Number or as the AAC.");
          facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg.toString(), null));
          msg.setLength(0);
          setFocus(getTxtDocNumDocnum());

          setRenderMiniAACForm(true);
          afContext.getPageFlowScope().put(RENDER_MINI_AAC_FORM_NAME, renderMiniAACForm);
          getAacCreateDocnum().setValue(aac);
          disableFormDocnum();
          return;
        }
        else {
          if (shippingService.isCustomerRestricted(Integer.parseInt(resultCustomerId))) {
            msg.append("AAC [").append(aac).append("] is a RESTRICTED customer.");
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg.toString(), null));
            msg.setLength(0);
            setFocus(getTxtAACDocnum());
            moreInvalidFields = true;
          }
          else {
            hm.put("aac", aac);
            hm.put("customer_id", resultCustomerId);
          }
        }
      }
      else {
        //* validate AAC
        msg = isValidAAC(getTxtAACDocnum());
        if (msg.length() > 0) {
          facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg.toString(), null));
          msg.setLength(0);
          setFocus(getTxtAACDocnum());
          moreInvalidFields = true;
        }
        else {
          val aac = aacDocnum.toString().trim().toUpperCase();
          String resultCustomerId = shippingService.getCustomerId(aac);
          if (isEmpty(resultCustomerId)) {
            msg.append("AAC [").append(aac).append("] not a valid customer.");
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg.toString(), null));
            msg.setLength(0);
            setFocus(getTxtAACDocnum());

            setRenderMiniAACForm(true);
            afContext.getPageFlowScope().put(RENDER_MINI_AAC_FORM_NAME, renderMiniAACForm);
            getAacCreateDocnum().setValue(aac);
            disableFormDocnum();
            return;
          }
          else {
            if (shippingService.isCustomerRestricted(Integer.parseInt(resultCustomerId))) {
              msg.append("AAC [").append(aac).append("] is a RESTRICTED customer.");
              facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg.toString(), null));
              msg.setLength(0);
              setFocus(getTxtAACDocnum());
              moreInvalidFields = true;
            }
            else {
              hm.put("aac", aac);
              hm.put("customer_id", resultCustomerId);
            }
          }
        }
      }

      //* there are no invalid fields, submit to database
      if (!moreInvalidFields) {

        hm.put("document_number", documentNumber);
        hm.put("suffix", demandSuffix);
        hm.put("user_id", String.valueOf(getUserId()));
        hm.put("workstation_id", String.valueOf(getWorkstationId()));
        hm.put("barcode", barcode);

        msg = shippingService.transship(hm, getUserId(), getWorkstationId());
        if (msg.length() > 0) {
          facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg.toString(), null));
        }
        else {
          //* success, clear all fields
          resetFormDocnum();
          enableFormDocnum();
          facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Transshipment added successfully.", null));
          //* 03/28/2011 - add for transshipment AFGH requirement
          //* update document number, niin, qty on dasf reflective of successful transshipment
          shippingService.updateDASF(refDasfId, Util.cleanInt(hm.get("quantity")));
        }
        log.debug("[debug] finished! transshipment complete...");
      }
    }
  }

  private void resetFocusCtr() {
    getTxtContractNumCtr().setContentStyle(UPPERCASE);
    getTxtQtyInvoicedCtr().setContentStyle("");
    getTxtQtyReceivedCtr().setContentStyle("");
    getTxtAACCtr().setContentStyle(UPPERCASE);
    getTxtDocNumCtr().setContentStyle(UPPERCASE);
    getTxtSuffixCtr().setContentStyle(UPPERCASE);
    getTxtSMICCtr().setContentStyle(UPPERCASE);
    getTxtNSNCtr().setContentStyle(UPPERCASE);
    getTxtCallNumCtr().setContentStyle(UPPERCASE);
    getTxtShipmentNumCtr().setContentStyle(UPPERCASE);
    getTxtBilledCtr().setInlineStyle("");
    getTxtTrackingNumCtr().setContentStyle(UPPERCASE);
    getTxtTailDateCtr().setInlineStyle("");
    getTxtBarcodeCtr().setContentStyle(UPPERCASE);

    getSupportedCtr().setValue("N");
    getRestrictShipCtr().setValue("N");
  }

  private void resetFormCtr() {
    resetFocusCtr();
    getTxtContractNumCtr().setValue("");
    getTxtDocNumCtr().setValue("");
    getTxtSuffixCtr().setValue("");
    getTxtBarcodeCtr().setValue("");
    getTxtAACCtr().setValue("");
    getTxtTrackingNumCtr().setValue("");
    getTxtQtyInvoicedCtr().setValue("");
    getTxtQtyReceivedCtr().setValue("");
    getTxtNSNCtr().setValue("");
    getTxtSMICCtr().setValue("");
    getTxtCallNumCtr().setValue("");
    getTxtShipmentNumCtr().setValue("");
    getTxtTailDateCtr().setValue("");
    getTxtBilledCtr().setValue("");
    getCcCtr().setValue(null);
    getPurposeCtr().setValue(null);
  }

  private void resetAACFormCtr() {
    getNameCreateCtr().setValue("");
    getAddress1CreateCtr().setValue("");
    getAddress2CreateCtr().setValue("");
    getZipCodeCreateCtr().setValue("");
    getStateCreateCtr().setValue("");
    getCityCreateCtr().setValue("");
  }

  private void disableFormCtr() {
    getTxtContractNumCtr().setDisabled(true);
    getTxtDocNumCtr().setDisabled(true);
    getTxtSuffixCtr().setDisabled(true);
    getTxtBarcodeCtr().setDisabled(true);
    getTxtAACCtr().setDisabled(true);
    getTxtTrackingNumCtr().setDisabled(true);
    getTxtQtyInvoicedCtr().setDisabled(true);
    getTxtQtyReceivedCtr().setDisabled(true);
    getTxtNSNCtr().setDisabled(true);
    getTxtSMICCtr().setDisabled(true);
    getTxtCallNumCtr().setDisabled(true);
    getTxtShipmentNumCtr().setDisabled(true);
    getTxtTailDateCtr().setDisabled(true);
    getTxtBilledCtr().setDisabled(true);
    getCcCtr().setDisabled(true);
    getPurposeCtr().setDisabled(true);
  }

  private void enableFormCtr() {
    getTxtContractNumCtr().setDisabled(false);
    getTxtDocNumCtr().setDisabled(false);
    getTxtSuffixCtr().setDisabled(false);
    getTxtBarcodeCtr().setDisabled(false);
    getTxtAACCtr().setDisabled(false);
    getTxtTrackingNumCtr().setDisabled(false);
    getTxtQtyInvoicedCtr().setDisabled(false);
    getTxtQtyReceivedCtr().setDisabled(false);
    getTxtNSNCtr().setDisabled(false);
    getTxtSMICCtr().setDisabled(false);
    getTxtCallNumCtr().setDisabled(false);
    getTxtShipmentNumCtr().setDisabled(false);
    getTxtTailDateCtr().setDisabled(false);
    getTxtBilledCtr().setDisabled(false);
    getCcCtr().setDisabled(false);
    getPurposeCtr().setDisabled(false);
  }

  public void resetByContractNumAAC(ActionEvent actionEvent) {
    try {
      resetAACFormCtr();
      enableFormCtr();
      cancelTransshipment(actionEvent);
    }
    catch (Exception e) {
      log.trace(e.getLocalizedMessage());
    }
  }

  public void submitByContractNumAAC(ActionEvent actionEvent) {
    try {
      DCIteratorBinding iter = ADFUtils.findIterator(ITERATOR_NAME);
      disableFormCtr();
      // commit the change
      iter.getDataControl().commitTransaction();
      submitByContractNum(actionEvent);
    }
    catch (Exception e) {
      log.trace(e.getLocalizedMessage());
    }
  }

  public void submitByContractNum(@SuppressWarnings("all") ActionEvent actionEvent) {
    //* clear border of previous red outlined boxes
    resetFocusCtr();
    boolean moreRequiredFields = false;
    FacesContext facesContext = FacesContext.getCurrentInstance();

    //* clear renderMiniAACForm
    setRenderMiniAACForm(false);
    //* for use in the web, maintaining state
    AdfFacesContext afContext = AdfFacesContext.getCurrentInstance();
    afContext.getPageFlowScope().put(RENDER_MINI_AAC_FORM_NAME, renderMiniAACForm);

    //* check required fields first
    //* do not toUpperCase on contract number
    Object objContractNumber = getTxtContractNumCtr().getValue();
    String contractNumber = isEmpty(objContractNumber) ? "" : objContractNumber.toString().trim().toUpperCase(Locale.US);
    if (contractNumber.isEmpty()) {
      moreRequiredFields = true;
      facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
          "Contract Number is a required field.", null));
      setFocus(getTxtContractNumCtr());
    }
    else {
      //* contract number must be 13 characters
      if (contractNumber.length() > MAX_LEN_CONTRACT_NUM) {
        facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
            "Contract Number must be less than or equal to " + MAX_LEN_CONTRACT_NUM + " alphanumeric characters.", null));
        setFocus(getTxtContractNumCtr());
        moreRequiredFields = true;
      }
      else {
        if (RegUtils.isNotAlphaNumeric(contractNumber)) {
          facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
              "Contract Number must be " + MAX_LEN_CONTRACT_NUM + " alphanumeric characters.", null));
          setFocus(getTxtContractNumCtr());
          moreRequiredFields = true;
        }
      }
    }

    Object objQuantityInvoiced = getTxtQtyInvoicedCtr().getValue();
    String quantityInvoiced = isEmpty(objQuantityInvoiced) ? "" : objQuantityInvoiced.toString().trim();
    if (quantityInvoiced.isEmpty()) {
      moreRequiredFields = true;
      facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
          "Quantity Invoiced is a required field.", null));
      setFocus(getTxtQtyInvoicedCtr());
    }
    else {
      //* quantity must be numeric
      if (RegUtils.isNotNumeric(quantityInvoiced)) {
        facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
            "Quantity Invoiced must be positive integer only.", null));
        setFocus(getTxtQtyInvoicedCtr());
        moreRequiredFields = true;
      }
    }

    Object objQuantityReceived = getTxtQtyReceivedCtr().getValue();
    String quantityReceived = isEmpty(objQuantityReceived) ? "" : objQuantityReceived.toString().trim();
    if (quantityReceived.isEmpty()) {
      moreRequiredFields = true;
      facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
          "Quantity Received is a required field.", null));
      setFocus(getTxtQtyReceivedCtr());
    }
    else {
      if (RegUtils.isNotNumeric(quantityReceived)) {
        facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
            "Quantity Received must be positive integer only.", null));
        setFocus(getTxtQtyReceivedCtr());
        moreRequiredFields = true;
      }
    }

    Object objBarcode = getTxtBarcodeCtr().getValue();
    String barcode = isEmpty(objBarcode) ? "" : objBarcode.toString().trim().toUpperCase(Locale.US);
    if (barcode.isEmpty()) {
      moreRequiredFields = true;
      facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
          "Shipping Barcode is a required field.", null));
      setFocus(getTxtBarcodeCtr());
    }
    else {
      //* barcode must be less than 10 characters
      if (barcode.length() > MAX_LEN_BARCODE || barcode.length() < 5) {
        facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
            "Shipping Barcode must be greater than or equal to 5 and less than or equal to " + MAX_LEN_BARCODE + " alphanumeric characters.", null));
        setFocus(getTxtBarcodeCtr());
        moreRequiredFields = true;
      }
      else {
        if (RegUtils.isNotAlphaNumeric(barcode)) {
          facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
              "Shipping Barcode must be alphanumeric characters.", null));
          setFocus(getTxtBarcodeCtr());
          moreRequiredFields = true;
        }
      }
    }

    if (moreRequiredFields) return;

    //* invoke the service to update the data
    ShippingServiceImpl shippingService = getShippingServiceModule();

    if (shippingService.existBarcode(barcode)) {
      facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Shipping Barcode [" + barcode + "] already exist.", null));
      setFocus(getTxtBarcodeCtr());
      return;
    }

    boolean moreInvalidFields = false;
    HashMap<String, String> hm = new HashMap<>();
    StringBuilder msg;
    //* check all fields non-empty values are valid

    boolean docRequired = false;
    //* demand suffix must be 1 character, if document number not empty
    Object objDemandSuffix = getTxtSuffixCtr().getValue();
    String demandSuffix = isEmpty(objDemandSuffix) ? "" : objDemandSuffix.toString().trim().toUpperCase();
    if (demandSuffix.length() == MAX_LEN_DEMAND_SUFFIX) {
      if (RegUtils.isNotAlphaNumeric(demandSuffix)) {
        facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
            "Demand Suffix must be " + MAX_LEN_DEMAND_SUFFIX + " alphanumeric character.", null));
        setFocus(getTxtSuffixCtr());
        moreInvalidFields = true;
      }
      else {
        hm.put("demand_suffix", demandSuffix);
      }
      docRequired = true;
    }

    //* document number must be 14 characters, if not empty
    Object objDocumentNumber = getTxtDocNumCtr().getValue();
    String documentNumber = "";
    if (!isEmpty(objDocumentNumber)) {
      documentNumber = objDocumentNumber.toString().trim().toUpperCase();
      if (documentNumber.length() != MAX_LEN_DOCUMENT_NUM) {
        facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
            "Document Number must be " + MAX_LEN_DOCUMENT_NUM + " alphanumeric characters.", null));
        setFocus(getTxtDocNumCtr());
        moreInvalidFields = true;
      }
      else {
        if (RegUtils.isNotAlphaNumeric(documentNumber)) {
          facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
              "Document Number must be " + MAX_LEN_DOCUMENT_NUM + " alphanumeric characters.", null));
          setFocus(getTxtDocNumCtr());
          moreInvalidFields = true;
        }
        //* first character can not be a Y or y
        if (RegUtils.containsFirstCharY(documentNumber)) {
          facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
              "First Character of Document Number cannot be a 'Y'.", null));
          setFocus(getTxtDocNumCtr());
          moreInvalidFields = true;
        }
      }
      hm.put("document_number", documentNumber);
    }
    else {
      if (docRequired) {
        facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
            "Document Number is required if a Demand Suffix is entered.", null));
        setFocus(getTxtDocNumCtr());
        moreInvalidFields = true;
      }
    }

    //* validate Tracking Number
    msg = isValidTrackingNum(getTxtTrackingNumCtr());
    if (msg.length() > 0) {
      facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg.toString(), null));
      msg.setLength(0);
      setFocus(getTxtTrackingNumCtr());
      moreInvalidFields = true;
    }
    else {
      Object objTrackingNum = getTxtTrackingNumCtr().getValue();
      if (!isEmpty(objTrackingNum))
        hm.put("tracking_number", objTrackingNum.toString().trim().toUpperCase());
    }

    //* validate Special Material Identification Code(SMIC) must be 2 characters, if not empty
    msg = isValidAlphaNumeric(getTxtSMICCtr(), "Special Material Identification Code (SMIC)", MAX_LEN_SMIC);
    if (msg.length() > 0) {
      facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg.toString(), null));
      msg.setLength(0);
      setFocus(getTxtSMICCtr());
      moreInvalidFields = true;
    }
    else {
      Object objSmic = getTxtSMICCtr().getValue();
      if (!isEmpty(objSmic))
        hm.put("smic", objSmic.toString().trim().toUpperCase());
    }

    //* Call Number must be alphanumeric, if not empty
    msg = isValidAlphaNumeric(getTxtCallNumCtr(), "Call Number", MAX_LEN_CALL_NUM);
    if (msg.length() > 0) {
      facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg.toString(), null));
      msg.setLength(0);
      setFocus(getTxtCallNumCtr());
      moreInvalidFields = true;
    }
    else {
      Object obj = getTxtCallNumCtr().getValue();
      if (!isEmpty(obj))
        hm.put("call_number", obj.toString().trim().toUpperCase());
    }

    //* Shipment Number must be alphanumeric, if not empty
    msg = isValidAlphaNumeric(getTxtShipmentNumCtr(), "Shipment Number", MAX_LEN_SHIPMENT_NUM);
    if (msg.length() > 0) {
      facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg.toString(), null));
      msg.setLength(0);
      setFocus(getTxtShipmentNumCtr());
      moreInvalidFields = true;
    }
    else {
      Object obj = getTxtShipmentNumCtr().getValue();
      if (!isEmpty(obj))
        hm.put("shipment_number", obj.toString().trim().toUpperCase());
    }

    //* Line Number must be alphanumeric, if not empty
    msg = isValidAlphaNumeric(getTxtLineNumCtr(), "Line Number", MAX_LEN_LINE_NUM);
    if (msg.length() > 0) {
      facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg.toString(), null));
      msg.setLength(0);
      setFocus(getTxtLineNumCtr());
      moreInvalidFields = true;
    }
    else {
      Object obj = getTxtLineNumCtr().getValue();
      if (!isEmpty(obj))
        hm.put("line_number", obj.toString().trim().toUpperCase());
    }

    //* Billed Amount must be numeric only, if not empty
    Object objBilledAmt = getTxtBilledCtr().getValue();
    if (!isEmpty(objBilledAmt)) {
      String billedAmt = objBilledAmt.toString().trim();
      if (RegUtils.isNotDollar(billedAmt)) {
        facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
            "Billed Amount must be of the decimal format #.#.", null));
        setFocus(getTxtBilledCtr());
        moreInvalidFields = true;
      }
      else {
        hm.put("billed_amount", billedAmt);
      }
    }

    //* Tailgate Date, Condition Code, Cognizance, Purpose
    Object objTailgate = getTxtTailDateCtr().getValue();
    hm.put("tailgate_date", isEmpty(objTailgate) ? "" : objTailgate.toString().trim());
    Object objCc = getCcCtr().getValue();
    hm.put("cc", isEmpty(objCc) ? "A" : objCc.toString().trim().toUpperCase());
    Object objPc = getPurposeCtr().getValue();
    hm.put("pc", isEmpty(objPc) ? "A" : objPc.toString().trim().toUpperCase());

    //* validate National Stock Number (NSN) must be 13 characters, if not empty
    msg = isValidAlphaNumeric(getTxtNSNCtr(), "National Stock Number (NSN)", MAX_LEN_NSN);
    if (msg.length() > 0) {
      facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg.toString(), null));
      msg.setLength(0);
      moreInvalidFields = true;
    }
    else {
      Object nsnCtr = getTxtNSNCtr().getValue();
      if (!isEmpty(nsnCtr)) {
        String resultNiinIdCtr = shippingService.findNiinId(nsnCtr.toString().trim().toUpperCase());
        if (isEmpty(resultNiinIdCtr)) {
          msg.append("National Stock Number (NSN) [").append(nsnCtr).append("] not found in system.");
          facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg.toString(), null));
          msg.setLength(0);
          setFocus(getTxtNSNCtr());
          moreInvalidFields = true;
        }
        else {
          hm.put("nsn", nsnCtr.toString().trim().toUpperCase());
          hm.put("niin_id", resultNiinIdCtr);
        }
      }
    }

    if (moreInvalidFields) {
      return;
    }

    Object aacCtr = getTxtAACCtr().getValue();
    if (isEmpty(aacCtr)) {
      if (isEmpty(documentNumber)) {
        msg.append("Document Number or AAC is required.");
        facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg.toString(), null));
        msg.setLength(0);
        setFocus(getTxtDocNumCtr());
        setFocus(getTxtAACCtr());
        return;
      }
      //* verify 1st 6 characters of tcn, if invalid, fix or consignee is required
      String aac = documentNumber.substring(0, 6).toUpperCase();
      String resultCustomerId = shippingService.getCustomerId(aac);
      if (isEmpty(resultCustomerId)) {
        msg.append("1st 6 characters of Document Number [")
            .append(aac)
            .append("] not a valid customer.  ")
            .append("Either enter a valid customer in the first 6 characters of Document Number or as the AAC.");
        facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg.toString(), null));
        msg.setLength(0);
        setFocus(getTxtDocNumCtr());

        setRenderMiniAACForm(true);
        afContext.getPageFlowScope().put(RENDER_MINI_AAC_FORM_NAME, renderMiniAACForm);
        getAacCreateCtr().setValue(aac);
        disableFormCtr();
        return;
      }
      else {
        if (shippingService.isCustomerRestricted(Integer.parseInt(resultCustomerId))) {
          msg.append("AAC [").append(aac).append("] is a RESTRICTED customer.");
          facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg.toString(), null));
          msg.setLength(0);
          setFocus(getTxtAACCtr());
          moreInvalidFields = true;
        }
        else {
          hm.put("aac", aac);
          hm.put("customer_id", resultCustomerId);
        }
      }
    }
    else {
      //* validate AAC
      msg = isValidAAC(getTxtAACCtr());
      if (msg.length() > 0) {
        facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg.toString(), null));
        msg.setLength(0);
        setFocus(getTxtAACCtr());
        moreInvalidFields = true;
      }
      else {
        String resultCustomerId = shippingService.getCustomerId(aacCtr.toString().trim().toUpperCase());
        val aac = aacCtr.toString().trim().toUpperCase();
        if (isEmpty(resultCustomerId)) {
          msg.append("AAC [").append(aac).append("] not a valid customer.");
          facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg.toString(), null));
          msg.setLength(0);
          setFocus(getTxtAACCtr());

          setRenderMiniAACForm(true);
          afContext.getPageFlowScope().put(RENDER_MINI_AAC_FORM_NAME, renderMiniAACForm);
          getAacCreateCtr().setValue(aac);
          disableFormCtr();
          return;
        }
        else {
          if (shippingService.isCustomerRestricted(Integer.parseInt(resultCustomerId))) {
            msg.append("AAC [").append(aac).append("] is a RESTRICTED customer.");
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg.toString(), null));
            msg.setLength(0);
            setFocus(getTxtAACCtr());
            moreInvalidFields = true;
          }
          else {
            hm.put("aac", aac);
            hm.put("customer_id", resultCustomerId);
          }
        }
      }
    }

    //* there are no invalid fields, submit to database
    if (!moreInvalidFields) {

      hm.put("contract_number", contractNumber);
      hm.put("quantity", quantityReceived);
      hm.put("quantity_invoiced", quantityInvoiced);
      hm.put("user_id", String.valueOf(getUserId()));
      hm.put("workstation_id", String.valueOf(getWorkstationId()));
      hm.put("barcode", barcode);

      msg = shippingService.transship(hm, getUserId(), getWorkstationId());
      if (msg.length() > 0) {
        facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg.toString(), null));
      }
      else {
        //* success, clear all fields
        resetFormCtr();
        enableFormCtr();
        facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Transshipment added successfully.", null));
      }
      log.debug("[debug] finished! transshipment complete...");
    }
  }

  private void setFocus(RichInputText cit) {
    cit.setInlineStyle("border-color:red;");
    cit.setContentStyle("text-transform:uppercase;");
  }

  private StringBuilder isValidAAC(RichInputText cit) {
    StringBuilder msgOutput = new StringBuilder();
    //* mark for AAC must be 6 characters, if not empty
    Object objAac = cit.getValue();
    if (!isEmpty(objAac)) {
      String aac = objAac.toString().trim();
      if (aac.length() != MAX_LEN_AAC) {
        msgOutput.append("AAC must be " + MAX_LEN_AAC + " alphanumeric characters.");
        setFocus(cit);
      }
      else {
        if (RegUtils.isNotAlphaNumeric(aac)) {
          msgOutput.append("AAC must be " + MAX_LEN_AAC + " alphanumeric characters.");
          setFocus(cit);
        }
        //* first character can not be a Y or y
        if (RegUtils.containsFirstCharY(aac)) {
          msgOutput.append("First Character of AAC cannot be a 'Y'.");
          setFocus(cit);
        }
      }
    }
    return msgOutput;
  }

  private StringBuilder isValidTrackingNum(RichInputText cit) {
    StringBuilder msgOutput = new StringBuilder();
    //* Tracking Number must be between 10 and 18 characters, if not empty
    Object objTrackingNum = cit.getValue();
    if (!isEmpty(objTrackingNum)) {
      String trackingNum = objTrackingNum.toString().trim();
      if (trackingNum.length() > MAX_LEN_TRACKING_NUM || trackingNum.length() < MIN_LEN_TRACKING_NUM) {
        msgOutput.append("Tracking Number must be between 10 and 18 alphanumeric characters.");
        setFocus(cit);
      }
      else {
        if (RegUtils.isNotAlphaNumeric(trackingNum)) {
          msgOutput.append("Tracking Number must be between 10 and 18 alphanumeric characters.");
          setFocus(cit);
        }
      }
    }
    return msgOutput;
  }

  private StringBuilder isValidAlphaNumeric(RichInputText cit, String title, int maxLen) {
    StringBuilder msgOutput = new StringBuilder();
    //* must be n characters, if not empty
    Object obj = cit.getValue();
    if (!isEmpty(obj)) {
      String value = obj.toString().trim();
      if (value.length() != maxLen) {
        msgOutput.append(title).append(" must be ").append(maxLen).append(" alphanumeric characters.");
        setFocus(cit);
      }
      else {
        if (RegUtils.isNotAlphaNumeric(value)) {
          msgOutput.append(title).append(" must be ").append(maxLen).append(" alphanumeric characters.");
          setFocus(cit);
        }
      }
    }
    return msgOutput;
  }

  private StringBuilder isValidNumeric(RichInputText cit) {
    StringBuilder msgOutput = new StringBuilder();
    //* title must be numeric, if not empty
    Object obj = cit.getValue();
    if (!isEmpty(obj)) {
      String value = obj.toString().trim();
      if (RegUtils.isNotNumeric(value)) {
        msgOutput.append("Quantity").append(" must be positive integer only.");
        setFocus(cit);
      }
    }
    return msgOutput;
  }

  public void setTxtTCNTcn(RichInputText txtTCNTcn) {
    this.txtTCNTcn = txtTCNTcn;
  }

  public RichInputText getTxtTCNTcn() {
    return txtTCNTcn;
  }

  public void setTxtConsigneeTcn(RichInputText txtConsigneeTcn) {
    this.txtConsigneeTcn = txtConsigneeTcn;
  }

  public RichInputText getTxtConsigneeTcn() {
    return txtConsigneeTcn;
  }

  public void setTxtTrackingNumTcn(RichInputText txtTrackingNumTcn) {
    this.txtTrackingNumTcn = txtTrackingNumTcn;
  }

  public RichInputText getTxtTrackingNumTcn() {
    return txtTrackingNumTcn;
  }

  public void setTxtBarcodeTcn(RichInputText txtBarcodeTcn) {
    this.txtBarcodeTcn = txtBarcodeTcn;
  }

  public RichInputText getTxtBarcodeTcn() {
    return txtBarcodeTcn;
  }

  public void setTxtDocNumTcn(RichInputText txtDocNumTcn) {
    this.txtDocNumTcn = txtDocNumTcn;
  }

  public RichInputText getTxtDocNumTcn() {
    return txtDocNumTcn;
  }

  public void setTxtSuffixTcn(RichInputText txtSuffixTcn) {
    this.txtSuffixTcn = txtSuffixTcn;
  }

  public RichInputText getTxtSuffixTcn() {
    return txtSuffixTcn;
  }

  public void setTxtNSNTcn(RichInputText txtNSNTcn) {
    this.txtNSNTcn = txtNSNTcn;
  }

  public RichInputText getTxtNSNTcn() {
    return txtNSNTcn;
  }

  public void setTxtSMICTcn(RichInputText txtSMICTcn) {
    this.txtSMICTcn = txtSMICTcn;
  }

  public RichInputText getTxtSMICTcn() {
    return txtSMICTcn;
  }

  public void setTxtRicFromTcn(RichInputText txtRicFromTcn) {
    this.txtRicFromTcn = txtRicFromTcn;
  }

  public RichInputText getTxtRicFromTcn() {
    return txtRicFromTcn;
  }

  public void setTxtUITcn(RichInputText txtUITcn) {
    this.txtUITcn = txtUITcn;
  }

  public RichInputText getTxtUITcn() {
    return txtUITcn;
  }

  public void setTxtQtyTcn(RichInputText txtQtyTcn) {
    this.txtQtyTcn = txtQtyTcn;
  }

  public RichInputText getTxtQtyTcn() {
    return txtQtyTcn;
  }

  public void setTxtPriceTcn(RichInputText txtPriceTcn) {
    this.txtPriceTcn = txtPriceTcn;
  }

  public RichInputText getTxtPriceTcn() {
    return txtPriceTcn;
  }

  public void setCcTcn(RichSelectOneChoice ccTcn) {
    this.ccTcn = ccTcn;
  }

  public RichSelectOneChoice getCcTcn() {
    return ccTcn;
  }

  public void setRestrictShipTcn(RichInputText restrictShipTcn) {
    this.restrictShipTcn = restrictShipTcn;
  }

  public RichInputText getRestrictShipTcn() {
    return restrictShipTcn;
  }

  public void setSupportedTcn(RichInputText supportedTcn) {
    this.supportedTcn = supportedTcn;
  }

  public RichInputText getSupportedTcn() {
    return supportedTcn;
  }

  public void setAacCreateTcn(RichInputText aacCreateTcn) {
    this.aacCreateTcn = aacCreateTcn;
  }

  public RichInputText getAacCreateTcn() {
    return aacCreateTcn;
  }

  public void setTxtDocNumDocnum(RichInputText txtDocNumDocnum) {
    this.txtDocNumDocnum = txtDocNumDocnum;
  }

  public RichInputText getTxtDocNumDocnum() {
    return txtDocNumDocnum;
  }

  public void setTxtSuffixDocnum(RichInputText txtSuffixDocnum) {
    this.txtSuffixDocnum = txtSuffixDocnum;
  }

  public RichInputText getTxtSuffixDocnum() {
    return txtSuffixDocnum;
  }

  public void setTxtBarcodeDocnum(RichInputText txtBarcodeDocnum) {
    this.txtBarcodeDocnum = txtBarcodeDocnum;
  }

  public RichInputText getTxtBarcodeDocnum() {
    return txtBarcodeDocnum;
  }

  public void setTxtAACDocnum(RichInputText txtAACDocnum) {
    this.txtAACDocnum = txtAACDocnum;
  }

  public RichInputText getTxtAACDocnum() {
    return txtAACDocnum;
  }

  public void setTxtTrackingNumDocnum(RichInputText txtTrackingNumDocnum) {
    this.txtTrackingNumDocnum = txtTrackingNumDocnum;
  }

  public RichInputText getTxtTrackingNumDocnum() {
    return txtTrackingNumDocnum;
  }

  public void setTxtRicFromDocnum(RichInputText txtRicFromDocnum) {
    this.txtRicFromDocnum = txtRicFromDocnum;
  }

  public RichInputText getTxtRicFromDocnum() {
    return txtRicFromDocnum;
  }

  public void setTxtUIDocnum(RichInputText txtUIDocnum) {
    this.txtUIDocnum = txtUIDocnum;
  }

  public RichInputText getTxtUIDocnum() {
    return txtUIDocnum;
  }

  public void setTxtQtyDocnum(RichInputText txtQtyDocnum) {
    this.txtQtyDocnum = txtQtyDocnum;
  }

  public RichInputText getTxtQtyDocnum() {
    return txtQtyDocnum;
  }

  public void setTxtNSNDocnum(RichInputText txtNSNDocnum) {
    this.txtNSNDocnum = txtNSNDocnum;
  }

  public RichInputText getTxtNSNDocnum() {
    return txtNSNDocnum;
  }

  public void setTxtSMICDocnum(RichInputText txtSMICDocnum) {
    this.txtSMICDocnum = txtSMICDocnum;
  }

  public RichInputText getTxtSMICDocnum() {
    return txtSMICDocnum;
  }

  public void setTxtPriceDocnum(RichInputText txtPriceDocnum) {
    this.txtPriceDocnum = txtPriceDocnum;
  }

  public RichInputText getTxtPriceDocnum() {
    return txtPriceDocnum;
  }

  public void setCcDocnum(RichSelectOneChoice ccDocnum) {
    this.ccDocnum = ccDocnum;
  }

  public RichSelectOneChoice getCcDocnum() {
    return ccDocnum;
  }

  @SuppressWarnings("unused")  //called from .jspx
  public void setRestrictShipDocnum(RichInputText restrictShipDocnum) {
    this.restrictShipDocnum = restrictShipDocnum;
  }

  public RichInputText getRestrictShipDocnum() {
    return restrictShipDocnum;
  }

  public void setSupportedDocnum(RichInputText supportedDocnum) {
    this.supportedDocnum = supportedDocnum;
  }

  public RichInputText getSupportedDocnum() {
    return supportedDocnum;
  }

  public void setAacCreateDocnum(RichInputText aacCreateDocnum) {
    this.aacCreateDocnum = aacCreateDocnum;
  }

  public RichInputText getAacCreateDocnum() {
    return aacCreateDocnum;
  }

  public void setTxtContractNumCtr(RichInputText txtContractNumCtr) {
    this.txtContractNumCtr = txtContractNumCtr;
  }

  public RichInputText getTxtContractNumCtr() {
    return txtContractNumCtr;
  }

  public void setTxtQtyInvoicedCtr(RichInputText txtQtyInvoicedCtr) {
    this.txtQtyInvoicedCtr = txtQtyInvoicedCtr;
  }

  public RichInputText getTxtQtyInvoicedCtr() {
    return txtQtyInvoicedCtr;
  }

  public void setTxtQtyReceivedCtr(RichInputText txtQtyReceivedCtr) {
    this.txtQtyReceivedCtr = txtQtyReceivedCtr;
  }

  public RichInputText getTxtQtyReceivedCtr() {
    return txtQtyReceivedCtr;
  }

  public void setTxtDocNumCtr(RichInputText txtDocNumCtr) {
    this.txtDocNumCtr = txtDocNumCtr;
  }

  public RichInputText getTxtDocNumCtr() {
    return txtDocNumCtr;
  }

  public void setTxtSuffixCtr(RichInputText txtSuffixCtr) {
    this.txtSuffixCtr = txtSuffixCtr;
  }

  public RichInputText getTxtSuffixCtr() {
    return txtSuffixCtr;
  }

  public void setTxtAACCtr(RichInputText txtAACCtr) {
    this.txtAACCtr = txtAACCtr;
  }

  public RichInputText getTxtAACCtr() {
    return txtAACCtr;
  }

  public void setTxtTrackingNumCtr(RichInputText txtTrackingNumCtr) {
    this.txtTrackingNumCtr = txtTrackingNumCtr;
  }

  public RichInputText getTxtTrackingNumCtr() {
    return txtTrackingNumCtr;
  }

  public void setTxtNSNCtr(RichInputText txtNSNCtr) {
    this.txtNSNCtr = txtNSNCtr;
  }

  public RichInputText getTxtNSNCtr() {
    return txtNSNCtr;
  }

  public void setTxtSMICCtr(RichInputText txtSMICCtr) {
    this.txtSMICCtr = txtSMICCtr;
  }

  public RichInputText getTxtSMICCtr() {
    return txtSMICCtr;
  }

  public void setCcCtr(RichSelectOneChoice ccCtr) {
    this.ccCtr = ccCtr;
  }

  public RichSelectOneChoice getCcCtr() {
    return ccCtr;
  }

  public void setPurposeCtr(RichSelectOneChoice purposeCtr) {
    this.purposeCtr = purposeCtr;
  }

  public RichSelectOneChoice getPurposeCtr() {
    return purposeCtr;
  }

  public void setTxtCallNumCtr(RichInputText txtCallNumCtr) {
    this.txtCallNumCtr = txtCallNumCtr;
  }

  public RichInputText getTxtCallNumCtr() {
    return txtCallNumCtr;
  }

  public void setTxtShipmentNumCtr(RichInputText txtShipmentNumCtr) {
    this.txtShipmentNumCtr = txtShipmentNumCtr;
  }

  public RichInputText getTxtShipmentNumCtr() {
    return txtShipmentNumCtr;
  }

  public void setTxtBilledCtr(RichInputText txtBilledCtr) {
    this.txtBilledCtr = txtBilledCtr;
  }

  public RichInputText getTxtBilledCtr() {
    return txtBilledCtr;
  }

  public void setTxtTailDateCtr(RichInputText txtTailDateCtr) {
    this.txtTailDateCtr = txtTailDateCtr;
  }

  public RichInputText getTxtTailDateCtr() {
    return txtTailDateCtr;
  }

  public void setTxtBarcodeCtr(RichInputText txtBarcodeCtr) {
    this.txtBarcodeCtr = txtBarcodeCtr;
  }

  public RichInputText getTxtBarcodeCtr() {
    return txtBarcodeCtr;
  }

  public void setRenderMiniAACForm(boolean renderMiniAACForm) {
    this.renderMiniAACForm = renderMiniAACForm;
  }

  public boolean isRenderMiniAACForm() {
    return renderMiniAACForm;
  }

  public void setTxtLineNumCtr(RichInputText txtLineNumCtr) {
    this.txtLineNumCtr = txtLineNumCtr;
  }

  public RichInputText getTxtLineNumCtr() {
    return txtLineNumCtr;
  }

  public void setRestrictShipCtr(RichInputText restrictShipCtr) {
    this.restrictShipCtr = restrictShipCtr;
  }

  public RichInputText getRestrictShipCtr() {
    return restrictShipCtr;
  }

  public void setSupportedCtr(RichInputText supportedCtr) {
    this.supportedCtr = supportedCtr;
  }

  public RichInputText getSupportedCtr() {
    return supportedCtr;
  }

  public void setAacCreateCtr(RichInputText aacCreateCtr) {
    this.aacCreateCtr = aacCreateCtr;
  }

  public RichInputText getAacCreateCtr() {
    return aacCreateCtr;
  }

  public void setNameCreateTcn(RichInputText nameCreateTcn) {
    this.nameCreateTcn = nameCreateTcn;
  }

  public RichInputText getNameCreateTcn() {
    return nameCreateTcn;
  }

  public void setAddress1CreateTcn(RichInputText address1CreateTcn) {
    this.address1CreateTcn = address1CreateTcn;
  }

  public RichInputText getAddress1CreateTcn() {
    return address1CreateTcn;
  }

  public void setZipCodeCreateTcn(RichInputText zipCodeCreateTcn) {
    this.zipCodeCreateTcn = zipCodeCreateTcn;
  }

  public RichInputText getZipCodeCreateTcn() {
    return zipCodeCreateTcn;
  }

  public void setAddress2CreateTcn(RichInputText address2CreateTcn) {
    this.address2CreateTcn = address2CreateTcn;
  }

  public RichInputText getAddress2CreateTcn() {
    return address2CreateTcn;
  }

  public void setStateCreateTcn(RichInputText stateCreateTcn) {
    this.stateCreateTcn = stateCreateTcn;
  }

  public RichInputText getStateCreateTcn() {
    return stateCreateTcn;
  }

  public void setNameCreateDocnum(RichInputText nameCreateDocnum) {
    this.nameCreateDocnum = nameCreateDocnum;
  }

  public RichInputText getNameCreateDocnum() {
    return nameCreateDocnum;
  }

  public void setAddress1CreateDocnum(RichInputText address1CreateDocnum) {
    this.address1CreateDocnum = address1CreateDocnum;
  }

  public RichInputText getAddress1CreateDocnum() {
    return address1CreateDocnum;
  }

  public void setZipCodeCreateDocnum(RichInputText zipCodeCreateDocnum) {
    this.zipCodeCreateDocnum = zipCodeCreateDocnum;
  }

  public RichInputText getZipCodeCreateDocnum() {
    return zipCodeCreateDocnum;
  }

  public void setAddress2CreateDocnum(RichInputText address2CreateDocnum) {
    this.address2CreateDocnum = address2CreateDocnum;
  }

  public RichInputText getAddress2CreateDocnum() {
    return address2CreateDocnum;
  }

  public void setStateCreateDocnum(RichInputText stateCreateDocnum) {
    this.stateCreateDocnum = stateCreateDocnum;
  }

  public RichInputText getStateCreateDocnum() {
    return stateCreateDocnum;
  }

  public void setNameCreateCtr(RichInputText nameCreateCtr) {
    this.nameCreateCtr = nameCreateCtr;
  }

  public RichInputText getNameCreateCtr() {
    return nameCreateCtr;
  }

  public void setAddress1CreateCtr(RichInputText address1CreateCtr) {
    this.address1CreateCtr = address1CreateCtr;
  }

  public RichInputText getAddress1CreateCtr() {
    return address1CreateCtr;
  }

  public void setZipCodeCreateCtr(RichInputText zipCodeCreateCtr) {
    this.zipCodeCreateCtr = zipCodeCreateCtr;
  }

  public RichInputText getZipCodeCreateCtr() {
    return zipCodeCreateCtr;
  }

  public void setAddress2CreateCtr(RichInputText address2CreateCtr) {
    this.address2CreateCtr = address2CreateCtr;
  }

  public RichInputText getAddress2CreateCtr() {
    return address2CreateCtr;
  }

  public void setStateCreateCtr(RichInputText stateCreateCtr) {
    this.stateCreateCtr = stateCreateCtr;
  }

  public RichInputText getStateCreateCtr() {
    return stateCreateCtr;
  }

  public void setCityCreateTcn(RichInputText cityCreateTcn) {
    this.cityCreateTcn = cityCreateTcn;
  }

  public RichInputText getCityCreateTcn() {
    return cityCreateTcn;
  }

  public void setCityCreateDocnum(RichInputText cityCreateDocnum) {
    this.cityCreateDocnum = cityCreateDocnum;
  }

  public RichInputText getCityCreateDocnum() {
    return cityCreateDocnum;
  }

  public void setCityCreateCtr(RichInputText cityCreateCtr) {
    this.cityCreateCtr = cityCreateCtr;
  }

  public RichInputText getCityCreateCtr() {
    return cityCreateCtr;
  }
}
