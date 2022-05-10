package mil.stratis.view.receiving;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.common.dm.ItemControlValidation;
import mil.stratis.common.util.JNDIUtils;
import mil.stratis.common.util.RegUtils;
import mil.stratis.common.util.Util;
import mil.stratis.model.services.ReceiptAMImpl;
import mil.stratis.model.view.rcv.NiinInfoLVOImpl;
import mil.stratis.model.view.rcv.ReceiptViewImpl;
import mil.stratis.model.view.rcv.SerialOrLoNumRImpl;
import mil.stratis.model.view.rcv.StowCancelVOImpl;
import mil.stratis.view.BackingHandler;
import mil.stratis.view.user.UserInfo;
import mil.stratis.view.util.JSFUtils;
import mil.usmc.mls2.stratis.common.domain.exception.StratisRuntimeException;
import mil.usmc.mls2.stratis.common.domain.model.LabelType;
import mil.usmc.mls2.stratis.core.service.gcss.I136NiinService;
import mil.usmc.mls2.stratis.core.utility.AdfLogUtility;
import mil.usmc.mls2.stratis.core.utility.ContextUtils;
import mil.usmc.mls2.stratis.core.utility.LabelPrintUtil;
import oracle.adf.share.ADFContext;
import oracle.adf.view.rich.component.rich.RichPopup;
import oracle.adf.view.rich.component.rich.input.RichInputDate;
import oracle.adf.view.rich.component.rich.input.RichInputText;
import oracle.adf.view.rich.component.rich.input.RichSelectBooleanCheckbox;
import oracle.adf.view.rich.component.rich.input.RichSelectOneChoice;
import oracle.adf.view.rich.component.rich.output.RichOutputText;
import oracle.adf.view.rich.context.AdfFacesContext;
import oracle.binding.AttributeBinding;
import oracle.binding.BindingContainer;
import oracle.jbo.Row;
import oracle.jbo.ViewObject;
import oracle.jbo.domain.Date;
import org.apache.myfaces.trinidad.event.LaunchEvent;
import org.springframework.beans.factory.annotation.Value;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

@Slf4j
public class ReceiptGeneral extends BackingHandler {

  private final boolean debug = true;
  private transient RichInputText docAndSuStr;
  private transient RichInputText suffixStr;
  private transient RichInputText nsnStr;
  private transient RichInputText riQUiPrStr;
  private transient RichInputText twoDScnStr;
  private transient RichOutputText processingMsg;
  private transient RichPopup popupDasf;
  private BindingContainer bindings;
  private List wacList = new ArrayList();
  private List locList = new ArrayList();
  private String sForwardAction = "";
  private transient Object wacId = null;
  private transient RichSelectOneChoice existingLocation;
  private final transient Row[] errorRow = null;
  private double uiConvFactor = 1;
  private boolean uiDialog = false;
  private String cubeReceiptStr = "0";
  private String textForDialog = "";
  private boolean dialogResponse = false;
  private String quantityReceived = "0";
  private String eId = "-1";
  private String rcnStr = "-1";
  private transient RichInputText stowArea;
  private String issQty = "0";
  private boolean recepitWindowRender = false;
  private transient RichInputText consolidationBarCode;
  private boolean saveReceiptRender = false;
  private boolean uiDiff = true;
  private boolean uiSame = true;
  private transient RichInputText srlOrLotConNum, srlConNumFrom, srlConNumTo;
  private transient RichInputText startSerial1, endSerial1, startSerial2, endSerial2, startSerial3, endSerial3, serialArea;
  private String numSerial1, numSerial2, numSerial3;
  private String fullSerialString = "";
  private transient RichInputText lotConNum;
  private transient RichInputText qtyLot;
  private transient RichInputText cancelSIDStr;
  private String errStr = "";
  private transient RichInputDate expirationDate;
  private transient RichSelectBooleanCheckbox chkPartialShipment;
  private transient ReceiptAMImpl service = null;
  private String isControlled = "";
  String displayNone = "";
  private boolean uiFlag = false;
  private final TreeSet<String> serialSet = new TreeSet<String>();
  boolean reviewSerial = true;

  @Value("${stratis.dasf.overage.split:true}")
  private boolean dasfOverageSplitEnabled;

  boolean inProgress = true;

  public ReceiptGeneral() {

    AdfFacesContext afContext = AdfFacesContext.getCurrentInstance();
    if (!afContext.isPostback()) {
      setUiFlag(false);
      textForDialog = "";
    }
  }

  public void onCheck(ValueChangeEvent valueChangeEvent) {

    RichSelectBooleanCheckbox cb = (RichSelectBooleanCheckbox) valueChangeEvent.getSource();
  }

  /**
   * This is tied to Create Receipt button. Calls the parsing and validation routines depending
   * on which ever (1D or 2D) barcodes are used.
   */
  public String commandButton_action() {

    inProgress = true;

    try {
      boolean errors = false;
      BindingContainer bindings = getBindings();

      //* pre-fill the error list view object
      getWorkloadManagerService().fillErrorList();

      sForwardAction = "";
      setUiDiff(false);
      setUiSame(true);
      setUiFlag(false);
      //Check if cubiscan readable desktop
      if (getWorkloadManagerService().hasCubiScan(JSFUtils.getManagedBeanValue("userbean.workstationId").toString()).equalsIgnoreCase("Y")) {
        getCubiscanReading();
        JSFUtils.addFacesErrorMessage("Note:", "Please wait for popup window with CUBISCAN Readings before clicking <Save Receipt>.");
      }
      if (getTwoDScnStr().getLocalValue() != null) {
        if (getTwoDScnStr().getLocalValue().toString().length() > 0)
          errors = TwoDScanCheck(); // false return means no errors
        if (!errors) {
          recepitWindowRender = true;
          saveReceiptRender = true;
          errors = parseAndUpdateValuesUsingTwoDBarcode(bindings); // false return means error
          if (!errors) {
            recepitWindowRender = false;
            saveReceiptRender = false;
            inProgress = true;
          }
        }
        if (errors) {
          return sForwardAction;
        }
      }
      else {
        inProgress = true;
        int erVal = OneDScanCheck(); //if return value is 1 that means it is an existing receipt
        switch (erVal) {
          case 999:
            //* confirm partial shipment
            break;
          case 0:
            //* case of new valid 1D barcodes

            recepitWindowRender = true;
            saveReceiptRender = true;
            errors = parseAndUpdateValuesUsingOneDBarcodes(bindings); // false means error
            if (!errors) {
              recepitWindowRender = false;
              saveReceiptRender = false;
            }
            break;

          case 1: //* existing receipt
            break;
          case -1:
            errors = true;
            break;

          default:
        }

        if (errors) {
          return sForwardAction;
        }
      }
      return sForwardAction;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return "";
  }

  /**
   * Parses and tokenizes attributes from fixed length 1D barcode inputs.
   * It also obtains NIIN information and Conversion factors if U/I are different.
   */
  public boolean parseAndUpdateValuesUsingOneDBarcodes(BindingContainer bindings) {

    try {
      String bldPrice = "";
      double c1 = 0;
      String slC = "-1";
      boolean isElevenCharacter = false;

      int uid = Integer.parseInt(JSFUtils.getManagedBeanValue("userbean.userId").toString());
      String docNumber = Util.trimUpperCaseClean(getDocAndSuStr().getLocalValue());
      String nsn = Util.trimUpperCaseClean(getNsnStr().getLocalValue());
      String riQntUiPrice = Util.trimUpperCaseClean(getRiQUiPrStr().getLocalValue());
      if (service == null) service = getReceiptAMService();
      ReceiptViewImpl voRecp = service.getReceiptView1();
      Row ro = voRecp.createRow();
      ro.setAttribute("Status", "RECEIPT DRAFT");

      if (getChkPartialShipment().isSelected()) ro.setAttribute("PartialShipmentIndicator", "Y");
      else ro.setAttribute("PartialShipmentIndicator", "N");

      //Default to A5A
      ro.setAttribute("DocumentId", "A5A");
      //Parse Document No - 14 and Suffix - 1
      ro.setAttribute("DocumentNumber", docNumber.substring(0, 14));
      //Parse FSC - 4 and NIIN - 9 (NIIN not needed)
      ro.setAttribute("Fsc", nsn.substring(0, 4));

      //Parse RoutingID (RIC- 3), UI - 2, QTY - 5, Condition Code (CC- 1),
      //Distribution Code (DISTR - 2), Unit Price (UP - 7)
      ro.setAttribute("RoutingId", riQntUiPrice.substring(0, 3));
      ro.setAttribute("Ui", riQntUiPrice.substring(3, 5));
      val riFiveTen = riQntUiPrice.substring(5, 10);
      if (validateQuantityInvoiced(riFiveTen)) {
        ro.setAttribute("QuantityInvoiced", riFiveTen);
      }
      else {
        return false;
      }
      ro.setAttribute("Cc", riQntUiPrice.substring(10, 11));
      if (riQntUiPrice.length() > 11) {
        ro.setAttribute("DodDistCode", riQntUiPrice.substring(11, 13));
        //Building the price
        if (validatePrice(riQntUiPrice)) {
          if (riQntUiPrice.length() == 20) {
            bldPrice = riQntUiPrice.substring(13, 20);
          }
          else if (riQntUiPrice.length() == 24) {
            bldPrice = riQntUiPrice.substring(13, 24);
            isElevenCharacter = true;
          }
          bldPrice = bldPrice.substring(0, bldPrice.length() - 2) + "." + bldPrice.substring(bldPrice.length() - 2);
          if (isNumericB(bldPrice)) {
            if (isElevenCharacter) {
              BigDecimal bigDecimalPrice = new BigDecimal(bldPrice);
              ro.setAttribute("Price", String.valueOf(bigDecimalPrice));
            }
            else {
              double doublePrice = Double.parseDouble(bldPrice);
              ro.setAttribute("Price", String.valueOf(doublePrice));
            }
          }
          else {
            JSFUtils.addFacesErrorMessage("Price is invalid. Price must be numeric.");
            return false;
          }
        }
        else {
          return false;
        }
      }
      else
        ro.setAttribute("Price", "0.0");

      val niin = nsn.substring(4, 13);
      if (niin.length() > 0) {
        long nId = getNiinInfoFromDB(niin);
        //Get the error code for niin errors
        Row er = getWorkloadManagerService().getErrorRow("RECEIPT_NSN_INV");
        if (nId > 0) {
          setNIINControlled(nId);
          //Remove the row from error queue if it is there
          if (er != null)
            getWorkloadManagerService().deleteErrorQueueRecord(er.getAttribute("Eid").toString(), "DOCNUM_SUF", docNumber, uid);
          ro.setAttribute("NiinId", nId);
          setUiConvFactor(service.validateUICheckAndReturnConvFactor(ro.getAttribute("Ui").toString().toUpperCase(Locale.US)));
          if (getUiConvFactor() != 1 && getUiConvFactor() != -1) {

            performUIValidConversionValidation(ro);
            ro.setAttribute("QuantityInvoiced", String.valueOf(Math.round(Util.cleanDouble(ro.getAttribute("QuantityInvoiced")) * getUiConvFactor())));
            c1 = c1 / getUiConvFactor();
            ro.setAttribute("Price", String.valueOf(c1));
          }
          if (getUiConvFactor() != -1)
            CopyNiinInfoMesurementsToReceipt(ro);
          slC = service.getShelfLifeCode(ro);
          if (slC.compareTo("-1") != 0)
            ro.setAttribute("ShelfLifeCode", slC);
        }
        else {
          //Generate row in error queue
          if (er != null)
            createErrorQueueRecord(er.getAttribute("Eid").toString(), "DOCNUM_SUF", docNumber);
          return false;
        }
      }
      voRecp.insertRow(ro);
      voRecp.setCurrentRow(ro);
      AttributeBinding dNum = (AttributeBinding) bindings.getControlBinding("ReceiptView1DocumentNumber");
      dNum.setInputValue(docNumber.substring(0, 14));
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return true;
  }

  /**
   * Parses and tokenizes attributes from 2D barcode.
   * It also obtains NIIN information and Conversion factors if U/I are different. Provides restrictions
   * so that only admin level user can edit if it was already processed as Receipt Complete.
   */
  public boolean parseAndUpdateValuesUsingTwoDBarcode(BindingContainer bindings) {

    String datIdFormatHeader = "%";
    String supplyDocNumSep = datIdFormatHeader + "12S";
    String nSNSep = datIdFormatHeader + "N";
    String routingIdSep = datIdFormatHeader + "V";
    String qtyUiSep = datIdFormatHeader + "7Q";
    String ccSep = datIdFormatHeader + "2R";
    String unitPriceSep = datIdFormatHeader + "12Q";
    String partNumSep = datIdFormatHeader + "1P";
    String prjCdSep = datIdFormatHeader + "03";
    String shpToSep = datIdFormatHeader + "27";
    String suppAddr = datIdFormatHeader + "81";
    String dodSep = datIdFormatHeader + "B6";
    String reqPriDesigSep = datIdFormatHeader + "B7";
    String trcNumSep = datIdFormatHeader + "1T";
    String parShpInd = datIdFormatHeader + "B8";
    String srLSep = datIdFormatHeader + "S";
    String riInd = datIdFormatHeader + "7V";
    int strLen = 0;
    double c1 = 0;
    String slC = "";

    try {
      int uid = Integer.parseInt(JSFUtils.getManagedBeanValue("userbean.userId").toString());
      if (service == null) service = getReceiptAMService();
      ReceiptViewImpl voRecp = null;
      Row ro = null;
      String docNumber = "";
      if (getTwoDScnStr().getLocalValue().toString().contains(supplyDocNumSep)) {
        docNumber = getTwoDScnStr().getLocalValue().toString().toUpperCase(Locale.US).split(supplyDocNumSep)[1].split(datIdFormatHeader)[0].trim();
        ro = service.getExistingReceipt(docNumber);
        if (ro != null) {
          voRecp = service.getReceiptView1();
          voRecp.setCurrentRow(ro);
          getNiinInfoFromDBByNiinId(Long.valueOf(ro.getAttribute("NiinId").toString()));

          return true;
        }
        else {

          //* check if is partial
          if (service.validateDocumentInHistory(docNumber.substring(0, 14) + "," + ((docNumber.length() <= 14) ? "#" : docNumber.substring(14, 15)))) {
            if (!getChkPartialShipment().isSelected()) {

              JSFUtils.addFacesInformationMessage("Doc Number " + docNumber.substring(0, 14) +
                  " previously processed. Confirm and submit selecting partial shipment option. " +
                  "Click Cancel button if not partial shipment. ");
              return false;
            }
          }
        }
      }
      voRecp = service.getReceiptView1();
      ro = voRecp.createRow();
      ro.setAttribute("Status", "RECEIPT DRAFT");
      ro.setAttribute("DocumentId", "A5A");
      if (getChkPartialShipment().isSelected()) ro.setAttribute("PartialShipmentIndicator", "Y");
      else ro.setAttribute("PartialShipmentIndicator", "N");

      //Parse Document No - 14 and Suffix - 1
      if (getTwoDScnStr().getLocalValue().toString().toUpperCase(Locale.US).contains(supplyDocNumSep)) {
        ro.setAttribute("DocumentNumber", docNumber.substring(0, 14));
        if (getTwoDScnStr().getLocalValue().toString().toUpperCase(Locale.US).split(supplyDocNumSep)[1].split(datIdFormatHeader)[0].toUpperCase(Locale.US).trim().length() == 15)
          ro.setAttribute("Suffix", docNumber.substring(14, 15));
      }
      //Parse FSC - 4 and NIIN - 9 (NIIN not needed)
      if (getTwoDScnStr().getLocalValue().toString().toUpperCase(Locale.US).contains(nSNSep)) {
        ro.setAttribute("Fsc", getTwoDScnStr().getLocalValue().toString().toUpperCase(Locale.US).split(nSNSep)[1].split(datIdFormatHeader)[0].toUpperCase(Locale.US).trim().substring(0, 4));
      }

      //Parse RoutingID (RIC- 3), UI - 2, QTY - 5, Condition Code (CC- 1),
      //Distribution Code (DISTR - 2), Unit Price (UP - 7)

      if (getTwoDScnStr().getLocalValue().toString().toUpperCase(Locale.US).contains(routingIdSep)) {
        if (getTwoDScnStr().getLocalValue().toString().toUpperCase(Locale.US).split(routingIdSep)[1].split(datIdFormatHeader).length > 0)
          ro.setAttribute("RoutingId", getTwoDScnStr().getLocalValue().toString().toUpperCase(Locale.US).split(routingIdSep)[1].split(datIdFormatHeader)[0].toUpperCase(Locale.US).trim().substring(0, 3));
      }

      if (getTwoDScnStr().getLocalValue().toString().toUpperCase(Locale.US).contains(qtyUiSep)) {
        strLen = getTwoDScnStr().getLocalValue().toString().toUpperCase(Locale.US).split(qtyUiSep)[1].split(datIdFormatHeader)[0].toUpperCase(Locale.US).trim().length();
        if (strLen > 2) {
          ro.setAttribute("Ui", getTwoDScnStr().getLocalValue().toString().toUpperCase(Locale.US).split(qtyUiSep)[1].split(datIdFormatHeader)[0].toUpperCase(Locale.US).trim().substring(strLen - 2, strLen));
          ro.setAttribute("QuantityInvoiced", getTwoDScnStr().getLocalValue().toString().toUpperCase(Locale.US).split(qtyUiSep)[1].split(datIdFormatHeader)[0].toUpperCase(Locale.US).trim().substring(0, strLen - 2));
        }
      }
      if (getTwoDScnStr().getLocalValue().toString().toUpperCase(Locale.US).contains(ccSep)) {
        if (getTwoDScnStr().getLocalValue().toString().toUpperCase(Locale.US).split(ccSep)[1].split(datIdFormatHeader).length > 0)
          ro.setAttribute("Cc", getTwoDScnStr().getLocalValue().toString().toUpperCase(Locale.US).split(ccSep)[1].split(datIdFormatHeader)[0].toUpperCase(Locale.US).trim().substring(0, 1));
      }
      if (getTwoDScnStr().getLocalValue().toString().toUpperCase(Locale.US).contains(unitPriceSep)) {
        strLen = getTwoDScnStr().getLocalValue().toString().toUpperCase(Locale.US).split(unitPriceSep)[1].split(datIdFormatHeader)[0].toUpperCase(Locale.US).trim().length();
        if (strLen > 3)
          ro.setAttribute("Price", getTwoDScnStr().getLocalValue().toString().toUpperCase(Locale.US).split(unitPriceSep)[1].split(datIdFormatHeader)[0].toUpperCase(Locale.US).trim().substring(0, strLen - 3));
      }
      if (getTwoDScnStr().getLocalValue().toString().toUpperCase(Locale.US).contains(partNumSep)) {
        if (getTwoDScnStr().getLocalValue().toString().toUpperCase(Locale.US).split(partNumSep)[1].split(datIdFormatHeader).length > 0)
          ro.setAttribute("PartNumber", getTwoDScnStr().getLocalValue().toString().toUpperCase(Locale.US).split(partNumSep)[1].split(datIdFormatHeader)[0].toUpperCase(Locale.US).trim());
      }
      if (getTwoDScnStr().getLocalValue().toString().toUpperCase(Locale.US).contains(prjCdSep)) {
        if (getTwoDScnStr().getLocalValue().toString().toUpperCase(Locale.US).split(prjCdSep)[1].split(datIdFormatHeader).length > 0)
          ro.setAttribute("ProjectCode", getTwoDScnStr().getLocalValue().toString().toUpperCase(Locale.US).split(prjCdSep)[1].split(datIdFormatHeader)[0].toUpperCase(Locale.US).trim());
      }
      if (getTwoDScnStr().getLocalValue().toString().toUpperCase(Locale.US).contains(shpToSep)) {
        if (getTwoDScnStr().getLocalValue().toString().toUpperCase(Locale.US).split(shpToSep)[1].split(datIdFormatHeader).length > 0)
          ro.setAttribute("Consignee", getTwoDScnStr().getLocalValue().toString().toUpperCase(Locale.US).split(shpToSep)[1].split(datIdFormatHeader)[0].toUpperCase(Locale.US).trim());
      }
      if (getTwoDScnStr().getLocalValue().toString().toUpperCase(Locale.US).contains(suppAddr)) {
        if (getTwoDScnStr().getLocalValue().toString().toUpperCase(Locale.US).split(suppAddr)[1].split(datIdFormatHeader).length > 0)
          ro.setAttribute("SupplementaryAddress", getTwoDScnStr().getLocalValue().toString().toUpperCase(Locale.US).split(suppAddr)[1].split(datIdFormatHeader)[0].toUpperCase(Locale.US).trim());
      }
      if (getTwoDScnStr().getLocalValue().toString().toUpperCase(Locale.US).contains(dodSep)) {
        if (getTwoDScnStr().getLocalValue().toString().toUpperCase(Locale.US).split(dodSep)[1].split(datIdFormatHeader).length > 0)
          ro.setAttribute("DodDistCode", getTwoDScnStr().getLocalValue().toString().toUpperCase(Locale.US).split(dodSep)[1].split(datIdFormatHeader)[0].toUpperCase(Locale.US).trim());
      }
      if (getTwoDScnStr().getLocalValue().toString().contains(reqPriDesigSep)) {
        if (getTwoDScnStr().getLocalValue().toString().toUpperCase(Locale.US).split(reqPriDesigSep)[1].split(datIdFormatHeader).length > 0)
          ro.setAttribute("Rpd", getTwoDScnStr().getLocalValue().toString().toUpperCase(Locale.US).split(reqPriDesigSep)[1].split(datIdFormatHeader)[0].toUpperCase(Locale.US).trim());
      }
      if (getTwoDScnStr().getLocalValue().toString().toUpperCase(Locale.US).contains(parShpInd)) {
        if (getTwoDScnStr().getLocalValue().toString().toUpperCase(Locale.US).split(parShpInd)[1].split(datIdFormatHeader).length > 0)
          ro.setAttribute("PartialShipmentIndicator", getTwoDScnStr().getLocalValue().toString().toUpperCase(Locale.US).split(parShpInd)[1].split(datIdFormatHeader)[0].toUpperCase(Locale.US).trim());
      }
      if (getTwoDScnStr().getLocalValue().toString().toUpperCase(Locale.US).contains(trcNumSep)) {
        if (getTwoDScnStr().getLocalValue().toString().toUpperCase(Locale.US).split(trcNumSep)[1].split(datIdFormatHeader).length > 0)
          ro.setAttribute("TraceabilityNumber", getTwoDScnStr().getLocalValue().toString().toUpperCase(Locale.US).split(trcNumSep)[1].split(datIdFormatHeader)[0].toUpperCase(Locale.US).trim());
      }
      if (getTwoDScnStr().getLocalValue().toString().toUpperCase(Locale.US).contains(srLSep)) {
        if (getTwoDScnStr().getLocalValue().toString().toUpperCase(Locale.US).split(srLSep)[1].split(datIdFormatHeader).length > 0)
          ro.setAttribute("SerialNumber", getTwoDScnStr().getLocalValue().toString().toUpperCase(Locale.US).split(srLSep)[1].split(datIdFormatHeader)[0].toUpperCase(Locale.US).trim());
      }
      if (getTwoDScnStr().getLocalValue().toString().toUpperCase(Locale.US).contains(riInd)) {
        if (getTwoDScnStr().getLocalValue().toString().toUpperCase(Locale.US).split(riInd)[1].split(datIdFormatHeader).length > 0)
          ro.setAttribute("Ri", getTwoDScnStr().getLocalValue().toString().toUpperCase(Locale.US).split(riInd)[1].split(datIdFormatHeader)[0].toUpperCase(Locale.US).trim());
      }

      if (getTwoDScnStr().getLocalValue().toString().contains(nSNSep)) {
        long nId = getNiinInfoFromDB(getTwoDScnStr().getLocalValue().toString().split(nSNSep)[1].split(datIdFormatHeader)[0].trim().substring(4, 13));
        //Get the error code for niin erros
        Row er = getWorkloadManagerService().getErrorRow("RECEIPT_NSN_INV");

        if (nId > 0) {
          //Remove the row from error queue if it is there
          if (er != null)
            getWorkloadManagerService().deleteErrorQueueRecord(er.getAttribute("Eid").toString(), "DOCNUM_SUF", docNumber, uid);
          ro.setAttribute("NiinId", nId);
          setUiConvFactor(service.validateUICheckAndReturnConvFactor(ro.getAttribute("Ui").toString()));
          if (getUiConvFactor() == -1) {
            JSFUtils.addFacesErrorMessage("RECEIPT_UI_CONV", "Unable to convert unit of issue. See Supervisor");
            return false;
          }
          else if ((getUiConvFactor() > 0) && (getUiConvFactor() != 1)) {

            performUIValidConversionValidation(ro);
            ro.setAttribute("QuantityInvoiced", String.valueOf(Math.round(Double.parseDouble(ro.getAttribute("QuantityInvoiced").toString()) * getUiConvFactor())));
            c1 = Float.valueOf(ro.getAttribute("Price").toString());
            c1 = c1 / getUiConvFactor();
            ro.setAttribute("Price", String.valueOf(c1));
          }
          if (getUiConvFactor() != -1)
            CopyNiinInfoMesurementsToReceipt(ro);
          else {

          }
          slC = service.getShelfLifeCode(ro);
          if (slC.compareTo("-1") != 0)
            ro.setAttribute("ShelfLifeCode", slC);
        }
        else {
          if (er != null)
            createErrorQueueRecord(er.getAttribute("Eid").toString(), "DOCNUM_SUF", docNumber);
          return false;
        }
      }
      setNIINControlled(Long.valueOf(ro.getAttribute("NiinId").toString()));
      voRecp.insertRow(ro);
      voRecp.setCurrentRow(ro);
      AttributeBinding dNum = (AttributeBinding) bindings.getControlBinding("ReceiptView1DocumentNumber");
      dNum.setInputValue(getTwoDScnStr().getLocalValue().toString().toUpperCase(Locale.US).split(supplyDocNumSep)[1].split(datIdFormatHeader)[0].toUpperCase(Locale.US).trim().substring(0, 14));
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return true;
  }

  /**
   * Calls the backend to get niin info based on NIIN_ID. Otherwise displays error message.
   */
  private long getNiinInfoFromDBByNiinId(long niinId) {

    try {
      if (service == null) service = getReceiptAMService();
      long r = service.getNiinInfoFromDBNiinId(niinId);
      setNIINControlled(niinId);
      if (r <= 0) {
        JSFUtils.addFacesErrorMessage("NSN_INV", "NIIN not recognized in system. Immediate MHIF request sent to host.");
      }

      return r;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return 0;
  }

  /**
   * Calls the backend to get if a NIIN is controlled.
   */
  private void setNIINControlled(long niinId) {
    isControlled = getWorkloadManagerService().isControlled(niinId);
  }

  /**
   * Calls the backend to get niin info based on NIIN. Otherwise displays error message.
   */
  private long getNiinInfoFromDB(String nnStr) {

    try {
      String er = "";
      if (service == null) service = getReceiptAMService();

      long r = service.getNiinInfoFromDB(nnStr);
      if (r <= 0) {
        val shouldCallGcss = getWorkloadManagerService().createNiinInfoFromRefMhifOrCallGCSS(nnStr);
        if (shouldCallGcss) {
          val i136NiinService = ContextUtils.getBean(I136NiinService.class);
          val result = i136NiinService.processI136Niin(nnStr);
          if (result.status().isSuccess()) {
            r = service.getNiinInfoFromDB(nnStr);
          }
        }
        else
          r = service.getNiinInfoFromDB(nnStr);
      }
      if (r <= 0) {
        JSFUtils.addFacesErrorMessage("NSN_INV", "NIIN not recognized in system. Immediate MHIF request sent to host.");
      }
      return r;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return 0;
  }

  /**
   * Check if the scanned in 1D barcodes are of right length.
   * Also check if this document was already created and if so pull that information up into Receipt View.
   */
  private int OneDScanCheck() {

    // Main method for 1D scan errors
    int flag = 0;

    if (getTwoDScnStr().getLocalValue() == null) {
      if ((getDocAndSuStr().getLocalValue() == null) && (getNsnStr().getLocalValue() == null) && (getRiQUiPrStr().getLocalValue() == null)) {
        flag = -1;
        JSFUtils.addFacesErrorMessage("2D_SCAN_STR_MIS", "2D Scan is missing.");
        return flag;
      }
    }

    //* missing required fields
    if (getDocAndSuStr().getLocalValue() == null) {
      flag = -1;
      JSFUtils.addFacesErrorMessage("DOCUMENT_NUMBER_STR_MIS", "Document Number is missing.");
    }
    if (getNsnStr().getLocalValue() == null) {
      flag = -1;
      JSFUtils.addFacesErrorMessage("NSN_STR", "NSN is missing.");
    }
    if (getRiQUiPrStr().getLocalValue() == null) {
      flag = -1;
      JSFUtils.addFacesErrorMessage("RIFrom_QTY_UI_PRC_STR_MIS", "RI From and/or QTY and/or UI and/or Price is missing.");
    }
    if (flag < 0) return flag;

    //* invalid length
    String docNumber = Util.trimUpperCaseClean(getDocAndSuStr().getLocalValue());
    String nsn = Util.trimUpperCaseClean(getNsnStr().getLocalValue());
    String third = Util.trimUpperCaseClean(getRiQUiPrStr().getLocalValue());
    if (docNumber.length() < 14 || RegUtils.isNotAlphaNumeric(docNumber)) {
      flag = -1;
      JSFUtils.addFacesErrorMessage("DOCUMENT_NUMBER_LEN", "Document Number is invalid.");
    }
    if (nsn.length() < 13 || RegUtils.isNotAlphaNumeric(nsn)) {
      flag = -1;
      JSFUtils.addFacesErrorMessage("NSN_LEN", "NSN is invalid.");
    }
    if (third.length() < 11 || RegUtils.isNotAlphaNumericPlusSpace(third)) {
      flag = -1;
      JSFUtils.addFacesErrorMessage("RIFrom_QTY_UI_PRC_LEN", "RI From and/or QTY and/or UI and/or Price is invalid.");
    }
    if (flag < 0) return flag;
    try {
      if (service == null) service = getReceiptAMService();
      //See if this receipt is already existing
      //* check if is partial
      val doc = docNumber.substring(0, 14);
      if (service.validateDocumentInHistory(doc) && !getChkPartialShipment().isSelected()) {
        JSFUtils.addFacesInformationMessage("Doc Number " + doc +
            " previously processed. Confirm and submit selecting partial shipment option. Click Cancel button if not partial shipment.");
        return 999;
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return flag;
  }

  /**
   * Tokenize the 2D into individual attributes and check if they are of valid values.
   */
  public boolean TwoDScanCheck() {

    //Main method to Check the 2D scan string for errors
    boolean flag = true;
    boolean errors = false;
    String headerComplianceInd = "[)>";
    String datIdFormatHeader = "%";
    String supplyDocNumSep = datIdFormatHeader + "12S";
    String nSNSep = datIdFormatHeader + "N";
    String routingIdSep = datIdFormatHeader + "V";
    String qtyUiSep = datIdFormatHeader + "7Q";
    String ccSep = datIdFormatHeader + "2R";
    String unitPriceSep = datIdFormatHeader + "12Q";
    String prjCdSep = datIdFormatHeader + "03";
    String shpToSep = datIdFormatHeader + "27";
    String suppAddr = datIdFormatHeader + "81";

    try {
      if ((getTwoDScnStr().getLocalValue() != null)) {
        if ((getTwoDScnStr().getLocalValue().toString().substring(0, 3).compareTo(headerComplianceInd) != 0) && (getTwoDScnStr().getLocalValue().toString().length() > 6)) {
          flag = false;
          JSFUtils.addFacesErrorMessage("NON_COMPLIANT_2D", "Non-compliant 2D Data.");
          errors = true;
        }
        else {
          datIdFormatHeader = getTwoDScnStr().getLocalValue().toString().substring(3, 4);
        }
        if (errors) return errors;

        // Document Number and Suffix validation
        if (!(getTwoDScnStr().getLocalValue().toString().contains(supplyDocNumSep))) {
          flag = false;
          errors = true;
          JSFUtils.addFacesErrorMessage("DOCUMENT_NUMBER_MIS", "Document Number missing.");
        }
        else if (getTwoDScnStr().getLocalValue().toString().split(supplyDocNumSep)[1].split(datIdFormatHeader)[0] != null) {
          flag = validateDocumentNumberString(getTwoDScnStr().getLocalValue().toString().split(supplyDocNumSep)[1].split(datIdFormatHeader)[0].trim());
          if (!flag)
            errors = true;
        }
        //NSN validation
        if (!(getTwoDScnStr().getLocalValue().toString().contains(nSNSep))) {
          flag = true;
          errors = true;
          JSFUtils.addFacesErrorMessage("NSN_MIS", "NSN missing.");
        }
        else if (getTwoDScnStr().getLocalValue().toString().split(nSNSep)[1].split(datIdFormatHeader)[0] != null) {
          flag = validateNSNString(getTwoDScnStr().getLocalValue().toString().split(nSNSep)[1].split(datIdFormatHeader)[0].toUpperCase(Locale.US).trim());
          if (!flag)
            errors = true;
        }
        //Routing Id validation
        if (!(getTwoDScnStr().getLocalValue().toString().contains(routingIdSep))) {
          flag = false;
          errors = true;
          JSFUtils.addFacesErrorMessage("ROUTING_ID_MIS", "Routing ID missing.");
        }
        else if (getTwoDScnStr().getLocalValue().toString().split(routingIdSep)[1].split(datIdFormatHeader)[0] != null) {
          flag = validateRoutingIdString(getTwoDScnStr().getLocalValue().toString().split(routingIdSep)[1].split(datIdFormatHeader)[0].trim());
          if (!flag)
            errors = true;
        }
        //Quantity and UI validation
        if (!(getTwoDScnStr().getLocalValue().toString().contains(qtyUiSep))) {
          flag = false;
          errors = true;
          JSFUtils.addFacesErrorMessage("QTY_UI_MIS", "QTY and/or UI missing.");
        }
        else {
          String ui = getTwoDScnStr().getLocalValue().toString().split(qtyUiSep)[1].split(datIdFormatHeader)[0].trim();
          ui = ui.substring(ui.length() - 2);
          flag = validateQuantityUiString(ui);
          if (!flag)
            errors = true;
        }
        //Condition Code validation
        if (!(getTwoDScnStr().getLocalValue().toString().contains(ccSep))) {
          flag = false;
          errors = true;
          JSFUtils.addFacesErrorMessage("CC_MIS", "CC missing.");
        }
        else if (getTwoDScnStr().getLocalValue().toString().split(ccSep)[1].split(datIdFormatHeader)[0] != null) {
          flag = validateConditionCodeString(getTwoDScnStr().getLocalValue().toString().split(ccSep)[1].split(datIdFormatHeader)[0].trim());
          if (!flag)
            errors = true;
        }
        //Unit Price validation
        if (!(getTwoDScnStr().getLocalValue().toString().contains(unitPriceSep))) {
          flag = true;
          errors = true;
          JSFUtils.addFacesErrorMessage("UNIT_PRICE_MIS", "Unit Price missing.");
        }
        else if (getTwoDScnStr().getLocalValue().toString().split(unitPriceSep)[1].split(datIdFormatHeader)[0] != null) {
          flag = validateUnitPriceString2D(getTwoDScnStr().getLocalValue().toString().split(unitPriceSep)[1].split(datIdFormatHeader)[0].trim());
          if (!flag)
            errors = true;
        }
        //Ship To validation
        if (!(getTwoDScnStr().getLocalValue().toString().contains(shpToSep))) {
          flag = false;
          errors = true;
          JSFUtils.addFacesErrorMessage("SHIP_TO_MIS", "Ship To missing.");
        }
        else if (getTwoDScnStr().getLocalValue().toString().split(shpToSep)[1].split(datIdFormatHeader)[0] != null) {
          flag = validateShipToString(getTwoDScnStr().getLocalValue().toString().split(shpToSep)[1].split(datIdFormatHeader)[0].trim());
          if (!flag)
            errors = true;
        }

        //Supplementary Address validation
        if (!(getTwoDScnStr().getLocalValue().toString().contains(suppAddr))) {
          if (flag)
            flag = true;
        }
        else if (getTwoDScnStr().getLocalValue().toString().split(suppAddr)[1].split(datIdFormatHeader).length > 0) {
          flag = validateSupplmentaryAddressString(getTwoDScnStr().getLocalValue().toString().split(suppAddr)[1].split(datIdFormatHeader)[0].trim());
          if (!flag)
            errors = true;
        }
        //Project Code Validation
        if (!(getTwoDScnStr().getLocalValue().toString().contains(prjCdSep))) {
          if (flag)
            flag = true;
        }
        else if (getTwoDScnStr().getLocalValue().toString().split(prjCdSep)[1].split(datIdFormatHeader)[0] != null) {
          flag = validateProjectCodeString(getTwoDScnStr().getLocalValue().toString().split(prjCdSep)[1].split(datIdFormatHeader)[0].trim());
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return !errors;
  }

  /**
   * validate the document number length.
   */
  public boolean validateDocumentNumberString(String d) {

    boolean valid = false;
    try {
      if (d != null) {
        if ((d.length() == 14) || (d.length() == 15)) {
          if (RegUtils.isNotAlphaNumeric(d)) {
            JSFUtils.addFacesErrorMessage("DOCUMENT_NUMBER_LEN", "Document Number Invalid.");
          }
          else
            valid = true;
        }
        else {
          JSFUtils.addFacesErrorMessage("DOCUMENT_NUMBER_LEN", "Document Number Invalid.");
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return valid;
  }

  /**
   * validate the NSN.
   */
  public boolean validateNSNString(String d) {

    try {
      if (d != null) {
        if ((d.length() >= 13) && (d.length() <= 15))
          return true;
        else
          JSFUtils.addFacesErrorMessage("NSN_LEN", "NSN Invalid.");
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return false;
  }

  public boolean validateRoutingId(String d) {
    if (d != null) {}
    //Check against data base and see if the ROUTING ID is of this receipt location
    return true;
  }

  /**
   * validate the U/I.
   */
  public boolean validateQuantityUiString(String d) {
    try {
      if (d != null) {
        if (d.length() > 1) {
          if (!getReceiptAMService().validateUi(d)) {
            JSFUtils.addFacesErrorMessage("RECEIPT_UI_ER", "The UI for this Receipt is not supported by STRATIS.");
            return false;
          }
          else
            return true;
        }
        else
          JSFUtils.addFacesErrorMessage("QTY_UI_LEN", "QTY and/or UI Invalid.");
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return false;
  }

  /**
   * validate the CC
   */
  public boolean validateConditionCodeString(String d) {
    try {
      if (d != null) {
        if (d.length() == 1) {
          if (!getReceiptAMService().validateConditionCode(d)) {
            JSFUtils.addFacesErrorMessage("RECEIPT_CC_ER", "The CC for this Receipt is not supported by STRATIS.");
            return false;
          }
          else
            return true;
        }
        else
          JSFUtils.addFacesErrorMessage("CC_LEN", "CC Invalid.");
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return false;
  }

  /**
   * validate the Unit Price
   */
  public boolean validateUnitPriceString2D(String d) {
    try {
      String v = d.split("U")[0];
      if (v == null)
        JSFUtils.addFacesErrorMessage("UNIT_PRICE_LEN", "Unit Price Invalid.");
      if (d != null) {
        v = d.split("U")[0];
        if (v == null)
          JSFUtils.addFacesErrorMessage("UNIT_PRICE_LEN", "Unit Price Invalid.");
        else if (((v.length() >= 3) && (v.length() <= 8)) && (isNumericB(v)))
          return true;
        else
          JSFUtils.addFacesErrorMessage("UNIT_PRICE_LEN", "Unit Price Invalid.");
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return false;
  }

  public boolean validateUnitPrice(String d) {
    if (d != null) {}
    //Check against data base and see if the Unit Price is valid -- NOT NEEDED
    return true;
  }

  public boolean validatePrice(String riQuantCCPrice) {
    //the 7  character price 13, 14, 15, 16, 17, 18, 19, 20
    //the 11 character price 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24
    if (riQuantCCPrice.length() < 20 || riQuantCCPrice.length() > 24 || (riQuantCCPrice.length() > 20 && riQuantCCPrice.length() < 24)) {
      JSFUtils.addFacesErrorMessage("Price must  be 7 or 11 character long.");
      return false;
    }
    else if (!riQuantCCPrice.toLowerCase(Locale.US).startsWith("  ", 11)) {
      JSFUtils.addFacesErrorMessage("Double space required between condition code and price.");
      return false;
    }
    else if (riQuantCCPrice.length() == 20) {
      if (!isNumericB(riQuantCCPrice.substring(13, 20))) {
        JSFUtils.addFacesErrorMessage("Price must be numeric.");
        return false;
      }
    }
    else if (riQuantCCPrice.length() == 24) {
      if (!isNumericB(riQuantCCPrice.substring(13, 24))) {
        JSFUtils.addFacesErrorMessage("Price must be numeric.");
        return false;
      }
    }
    return true;
  }

  /**
   * validate the Project Code
   */
  public boolean validateProjectCodeString(String d) {
    try {
      if (d != null) {
        if (d.length() <= 3) {
          if (!getReceiptAMService().validateProjectCode(d)) {
            JSFUtils.addFacesErrorMessage("RECEIPT_PJC_ER", "The Project Code for this Receipt is not supported by STRATIS.");
            return false;
          }
          else
            return true;
        }
        else
          JSFUtils.addFacesErrorMessage("PROJECT_CODE_LEN", "Project Code Invalid.");
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return false;
  }

  /**
   * validate the Ship To.
   */
  public boolean validateShipToString(String d) {
    try {
      if (d != null) {
        if (d.length() == 6)
          return true;
        else
          JSFUtils.addFacesErrorMessage("SHIP_TO_LEN", "Ship To AAC Invalid.");
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return false;
  }

  /**
   * validate the Supplementary address.
   */
  public boolean validateSupplmentaryAddressString(String d) {
    try {
      if (d != null) {
        if (RegUtils.isNotAlphaNumeric(d)) {
          JSFUtils.addFacesErrorMessage("SUPPLEMENTARY_ADDRESS_LEN", "Supplementary address (AAC) Invalid.");
          return false;
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return true;
  }

  /**
   * validate the Ship From.
   */
  public boolean validateShipFromString(String d) {
    try {
      if (d != null) {
        if ((d.length() == 0) || (d.length() == 6))
          return true;
        else
          JSFUtils.addFacesErrorMessage("SHIP_FROM_LEN", "Ship From AAC Invalid.");
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return false;
  }

  /**
   * validate the Routing ID.
   */
  public boolean validateRoutingIdString(String d) {
    try {
      if (d != null) {
        if (d.length() == 3) {

          return true;
        }
        else
          JSFUtils.addFacesErrorMessage("ROUTING_ID_LEN", "RI Invalid.");
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return false;
  }

  /**
   * validate the DoD.
   */
  public boolean validateDODDistributionCodeString(String d) {
    try {
      if (d != null) {
        if ((d.length() == 0) || (d.length() == 3))
          return true;
        else
          JSFUtils.addFacesErrorMessage("DOD_DIST_CD_LEN", "DOD distribution code Invalid.");
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return false;
  }

  /**
   * validate the Requisition Priority Designator.
   */
  public boolean validateRequisitionPriorityDesignatorString(String d) {
    try {
      if (d != null) {
        if ((d.length() == 0) || (d.length() == 2))
          return true;
        else
          JSFUtils.addFacesErrorMessage("PRIORITY_DESIGNATOR_LEN", "Priority designator Invalid.");
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return false;
  }

  /**
   * validate the Quantity Measured.
   */
  public boolean validateQuantityMeasuredString(String d) {
    try {
      if (d != null) {
        if (d.length() == 0)
          return true;
        else if ((Long.valueOf(d) != null) && (Long.valueOf(d) > 0))
          return true;
        else
          JSFUtils.addFacesErrorMessage("QTY_MEASURED", "Quantity Measured is Invalid.");
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return false;
  }

  public void setWacList(List list) {
    wacList = list;
  }

  /**
   * build WAC list for GUI.
   */
  public List getWacList() {
    try {
      wacList.clear();
      List v = new ArrayList();
      List d = new ArrayList();
      getReceiptAMService().fillWacList(v, d);
      if ((v.size() > 0) && (d.size() > 0)) {
        for (int i = 0; i < v.size(); i++) {
          wacList.add(new SelectItem(v.get(i), d.get(i).toString()));
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return wacList;
  }

  public void setLocList(List list) {
    locList = list;
  }

  /**
   * build Location list for GUI.
   */
  public List getLocList() {
    try {
      if (locList != null) {
        locList.clear();
        locList = new ArrayList();
      }
      if (wacId != null) {
        List v = new ArrayList();
        List d = new ArrayList();
        getReceiptAMService().fillLocList(v, d, wacId);
        if ((v.size() > 0) && (d.size() > 0)) {
          for (int i = 0; i < v.size(); i++) {
            locList.add(new SelectItem(v.get(i), d.get(i).toString()));
          }
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return locList;
  }

  /**
   * Gets the WAC ID for filtering locations.
   */
  public void getLocationList(ValueChangeEvent valueChangeEvent) {

    try {
      wacId = valueChangeEvent.getNewValue();
      getLocList();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  /**
   * Calling backend to update the mesurements and calculate the cube.
   */
  private void CopyNiinInfoMesurementsToReceipt(Row ro) {
    try {
      setCubeReceiptStr(getReceiptAMService().copyNiinInfoMesurementsToReceipt(ro));
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  public void dialogYes(ActionEvent event) {
    SaveReceipt(true);
    getPopupDasf().hide();
  }

  public void dialogNo(ActionEvent event) {
    getPopupDasf().hide();
  }

  /**
   * Calls backend Save and peforms further validations and
   * then calls MoveOnToOthersFromReceipt method.
   */
  public void SaveReceipt(ActionEvent event) {
    SaveReceipt(false);
  }

  public void SaveReceipt(boolean allowDASFOverage) {

    log.trace("begin ReceiptGeneral.SaveReceipt");
    try {
      if (service == null)
        service = getReceiptAMService();
      String cubiscanLine = "";
      recepitWindowRender = true;
      saveReceiptRender = true;
      Row r = service.getReceiptView1().getCurrentRow();
      if (getWorkloadManagerService().hasCubiScan(JSFUtils.getManagedBeanValue("userbean.workstationId").toString()).equalsIgnoreCase("Y")) {
        cubiscanLine =
            service.getCubiScanData(JSFUtils.getManagedBeanValue("userbean.workstationName").toString());
        if ((cubiscanLine != null && cubiscanLine.length() > 10) && (cubiscanLine.contains("W"))) {
          r.setAttribute("Weight", cubiscanLine.split("K")[1].split(",")[0]);
          r.setAttribute("Length", cubiscanLine.split("L")[1].split(",")[0]);
          r.setAttribute("Width", cubiscanLine.split("W")[1].split(",")[0]);
          r.setAttribute("Height", cubiscanLine.split(",H")[1].split(",")[0]);
          r.setAttribute("Cube",
              String.valueOf((Double.parseDouble(cubiscanLine.split("L")[1].split(",")[0]) *
                  (Double.parseDouble(cubiscanLine.split("W")[1].split(",")[0]) *
                      (Double.parseDouble(cubiscanLine.split(",H")[1].split(",")[0]))))));
          r.setAttribute("QuantityMeasured", "1");
          service.updateCubiScanStatus(JSFUtils.getManagedBeanValue("userbean.workstationName").toString(),
              "COMPLETED");
        }
      }
      if (getChkPartialShipment().isSelected() && r.getAttribute("Suffix") == null) {
        service.addSuffixToReceipt();
      }
      eId = performReceiptValidationsBeforeSave(allowDASFOverage); // Error Id or "-1"

      if (eId.compareTo("1") == 0) {
        JNDIUtils lookup = JNDIUtils.getInstance();
        String stringSetting = lookup.getProperty("stratis.dasf.overage.split");
        if ("false".equalsIgnoreCase(stringSetting)) {
          JSFUtils.addFacesErrorMessage("RECIPT_DASF_QTY", "The Qty being received is greater than the Qty on the DASF - please confirm the correct Qty.");
        }
        else {
          RichPopup.PopupHints hints = new RichPopup.PopupHints();
          getPopupDasf().show(hints);
          sForwardAction = "";
        }
      }

      log.debug("eId = {}", eId);
      val userId = Long.parseLong(JSFUtils.getManagedBeanValue("userbean.userId").toString());
      if (eId.compareTo("-1") == 0) {

        log.debug("eId compared to -1");
        sForwardAction =
            service.saveReceipt(userId); // Create Receipt

        log.debug("sForwardAction = {}", sForwardAction);
        eId = performReceiptValidationsAfterSave();

        log.debug("eId is now = {}", eId);
        if (eId.compareTo("-1") == 0) {

          log.debug("eId compared to -1 again");
          sForwardAction =
              getReceiptAMService().moveOnToOthersFromReceipt(userId, ""); // everthign is of move on to Stow Details

          log.debug("sForwardAction is now = {}", sForwardAction);
          //Clear backed up locationlist
          getWorkloadManagerService().clearLocationList();
        }
        else {
          service.saveReceiptError(); // Set Receipt Status to Receipt InError
          //7.23.2014: Added change to receipt error so that FSC errors are information messages.
          if (getTextForDialog().contains("Change item") || getTextForDialog().contains("Unable to convert unit of issue")) {
            JSFUtils.addFacesInformationMessage(getTextForDialog());
          }
          else {
            JSFUtils.addFacesErrorMessage("RECEIPT_DISCREPANCY", getTextForDialog());
          }
        }
      }
    }
    catch (StratisRuntimeException sre) {
      AdfLogUtility.logException(sre);
      JSFUtils.addFacesErrorMessage("SUFFIX_OUT_OF_BOUNDS", sre.getMessage());
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    ADFContext.getCurrent().getSessionScope().put("allowDASFOverage", allowDASFOverage);

    log.trace("end ReceiptGeneral.SaveReceipt");
  }

  /**
   * Calls backend FinalUpdateOnReceiptAndStow. If no errors are returned then
   * calls generatePrintSIDLabel and sets the userbean for com printing of the label
   */
  public void FinalUpdateOnReceiptAndStow(ActionEvent event) {

    if (event != null) {
      //NO-OP can't remove parameter due to adf
    }
    setUiFlag(false);
    try {
      if (service == null) service = getReceiptAMService();
      boolean allowDASFOverage = (boolean) ADFContext.getCurrent().getSessionScope().get("allowDASFOverage");
      String prnStr = "";
      String errorCodes = service.finalUpdateOnReceiptAndStow(Long.parseLong(JSFUtils.getManagedBeanValue("userbean.userId").toString()), allowDASFOverage);
      // Get the SID list for the RCN if no errors
      if (errorCodes.contains("SIDLIST,")) {
        int sidL = errorCodes.split(",").length;
        String[] sidList = new String[sidL - 1];
        int cou1 = 1;
        String sid = errorCodes.split(",")[cou1];
        while (cou1 < sidL) { // note that first item is not sid but indicator of SIDLIST so skip it
          sidList[cou1 - 1] = errorCodes.split(",")[cou1];
          prnStr = prnStr + getWorkloadManagerService().generatePrintSIDLabel(errorCodes.split(",")[cou1], 0, 0);
          //Perform printing of each string
          cou1++;
        }
        errorCodes = "";
        if ((prnStr != null) && (prnStr.length() > 0)) {
          val labelPrintUtil = ContextUtils.getBean(LabelPrintUtil.class);
          UserInfo userInfo = (UserInfo) JSFUtils.getManagedBeanValue("userbean");
          labelPrintUtil.printLabel(userInfo, prnStr, sidList, LabelType.STOW_LABEL);
        }
      }
      if (errorCodes.length() > 0) {
        log.debug("An error occurred attempting to finalize a receipt...");
        showStowErrors(errorCodes);

        if (errorCodes.contains("D6_DUPLICATE")) sForwardAction = "GoReceivingGeneral";
        else sForwardAction = "";
      }
      else {
        log.info("Successfully finalized receipt.");
        serialSet.clear();
        reviewSerial = true;
        sForwardAction = "GoReceivingGeneral";
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  public void StowCancelDetails(ActionEvent event) {
    String sidStr = "";
    if ((cancelSIDStr == null) || (cancelSIDStr.getValue() == null)) {
      JSFUtils.addFacesErrorMessage("Required field", "SID is required.");
      return;
    }
    else {
      sidStr = cancelSIDStr.getValue().toString().trim();
    }

    if (sidStr.isEmpty()) {
      JSFUtils.addFacesErrorMessage("Required field", "SID is required.");
      return;
    }

    StowCancelVOImpl vos = getReceiptAMService().getStowCancelVO1();
    vos.clearCache();
    vos.setNamedWhereClauseParam("sidStr", sidStr);
    vos.executeQuery();

    Row r = vos.first();
    if (r != null) {
      String status = r.getAttribute("Status").toString();
      //Check if SID has already been stowed.
      if (status.equals("STOWED")) {
        vos.clearCache();
        JSFUtils.addFacesErrorMessage("SID Issue", "SID stowed. Unable to continue process.");
      }
      //Check other SIDS associated with the RCN if they've been stowed.
      else {
        String rcn = r.getAttribute("Rcn").toString();
        if (getReceiptAMService().getStowedNumberForAReceipt(rcn) > 0) {
          vos.clearCache();
          JSFUtils.addFacesErrorMessage("SID Issue", "SID associated with completed task. Unable to continue process.");
        }
      }
    }
    else {
      JSFUtils.addFacesErrorMessage("No Results", "No SIDs found.");
    }
  }

  public void StowCancelVOUpdate(ActionEvent event) {
    if (event != null) {
      //NO-OP can't remove parameter due to adf
    }
    try {
      String errStr = getReceiptAMService().stowCancelVOUpdate(Long.parseLong(JSFUtils.getManagedBeanValue("userbean.userId").toString()));
      if (errStr.equalsIgnoreCase("EMPTY_REASON")) {
        sForwardAction = "";
        JSFUtils.addFacesErrorMessage("EMPTY_REASON", "Please enter cancel reason.");
      }
      else if (errStr.equalsIgnoreCase("EMPTY_ROW")) {
        sForwardAction = "";
        JSFUtils.addFacesErrorMessage("EMPTY_ROW", "Please select a SID.");
      }
      else {
        JSFUtils.addFacesInformationMessage("SID canceled.");
        sForwardAction = "GoReceivingCancelSID";
        cancelSIDStr.setValue("");
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  public void CreateStowForAReceipt(ActionEvent event) {
    if (event != null) {
      //NO-OP can't remove parameter due to adf
    }
    try {
      getReceiptAMService().createStowForAReceipt(Long.parseLong(JSFUtils.getManagedBeanValue("userbean.userId").toString()), null);
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  public void UpdateStowForAReceipt(ActionEvent event) {
    if (event != null) {
      //NO-OP can't remove parameter due to adf
    }
    try {
      setUiFlag(false);
      Row r = getWorkloadManagerService().getLocationList1().getCurrentRow();
      if (r != null) log.trace(Util.cleanString(r.getAttribute("LocationId")));

      String errLs = getReceiptAMService().updateStowForAReceipt(((r == null || r.getAttribute("LocationId") == null) ? null : r.getAttribute("LocationId")), Long.parseLong(JSFUtils.getManagedBeanValue("userbean.userId").toString()), "N");
      //Clear out the location selection list
      getWorkloadManagerService().clearLocationList();
      showStowErrors(errLs);
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  public void UpdateLocationOnAllRows(ActionEvent event) {
    if (event != null) {
      //NO-OP can't remove parameter due to adf
    }
    try {
      String errLs = "";
      setUiFlag(false);
      Row r = getWorkloadManagerService().getLocationList1().getCurrentRow();
      if (r != null) {
        log.trace("Updating all rows {}", Util.cleanString(r.getAttribute("LocationId")));
        getReceiptAMService().updateAllRows(r.getAttribute("LocationId"));
        //Clear out the location selection list
        getWorkloadManagerService().clearLocationList();
      }
      showStowErrors(errLs);
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  public void EditStowForAReceipt(ActionEvent event) {
    if (event != null) {
      //NO-OP can't remove parameter due to adf
    }
    try {
      getReceiptAMService().editStowForAReceipt();
      getWorkloadManagerService().clearLocationList();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  public void DeleteStowForAReceipt(ActionEvent event) {
    if (event != null) {
      //NO-OP can't remove parameter due to adf
    }
    try {
      getReceiptAMService().deleteStowForAReceipt(Long.parseLong(JSFUtils.getManagedBeanValue("userbean.userId").toString()), "", "");
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  public void UIDialogDone(ActionEvent actionEvent) {
    // Action Listener for UI dialog.
    setDialogResponse(actionEvent.getComponent().getId().compareTo("acceptDialogId") == 0);
    AdfFacesContext.getCurrentInstance().returnFromDialog(null, null);
  }

  public void handleUIDialogLaunch(LaunchEvent launchEvent) {

    try {
      launchEvent.getDialogParameters().put("dialogTxt", getTextForDialog());
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  public void processNewLocationLabel(ValueChangeEvent valueChangeEvent) {
    try {
      val facesMessages = FacesContext.getCurrentInstance().getMessages();
      if (facesMessages.hasNext())
        facesMessages.remove();
      if (!getReceiptAMService().setStowLocation(valueChangeEvent.getNewValue().toString())) {
        JSFUtils.addFacesErrorMessage("NEW_LOCATION_INV", "New location entered for this STOW is not valid.");
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  public void getStowLocationList(ValueChangeEvent valueChangeEvent) {
    getReceiptAMService().getStowLocationList(valueChangeEvent.getNewValue());
  }

  public String performReceiptValidationsBeforeSave(boolean allowDASFOverage) {
    log.trace("begin ReceiptGeneral.performReceiptValidationsBeforeSave");

    try {
      Row r = getReceiptAMService().getReceiptView1().getCurrentRow();
      String suffix = r.getAttribute("Suffix") != null ? r.getAttribute("Suffix").toString() : "";
      String documentNumberSuffix = Util.trimUpperCaseClean(r.getAttribute("DocumentNumber").toString() + suffix);
      String nsn = Util.trimUpperCaseClean(getNsnStr().getLocalValue());
      sForwardAction = "";
      uiDialog = false;
      String eIdStr = "";
      setUiDiff(false);
      setUiSame(true);
      // Required validations

      if (r.getAttribute("Cc") != null && !getReceiptAMService().validateConditionCode(r.getAttribute("Cc").toString())) {
        JSFUtils.addFacesErrorMessage("RECEIPT_CC_ER", "The CC for this Receipt is not supported by STRATIS.");
        eIdStr = "0";
      }
      if (r.getAttribute("ProjectCode") != null && !getReceiptAMService().validateProjectCode(r.getAttribute("ProjectCode").toString())) {
        JSFUtils.addFacesErrorMessage("RECEIPT_PJC_ER", "The Project Code for this Receipt is not supported by STRATIS.");
        eIdStr = "0";
      }
      if (r.getAttribute("Pc") != null && !getReceiptAMService().validatePurposeCode(r.getAttribute("Pc").toString())) {
        JSFUtils.addFacesErrorMessage("RECEIPT_PC_ER", "The Purpose Code for this Receipt is not supported by STRATIS.");
        eIdStr = "0";
      }
      if (r.getAttribute("ShelfLifeCode") != null && getReceiptAMService().validateShelfLifeCode(r.getAttribute("ShelfLifeCode").toString()) == -1) {
        JSFUtils.addFacesErrorMessage("RECEIPT_SLC_ER", "The Shelf Life Code for this Receipt is not supported by STRATIS.");
        eIdStr = "0";
      }
      if (r.getAttribute("Consignee") != null) {
        if (!validateShipToString(r.getAttribute("Consignee").toString())) {
          eIdStr = "0";
        }
        else
          r.setAttribute("Consignee", r.getAttribute("Consignee").toString().toUpperCase(Locale.US));
      }
      if (r.getAttribute("ShippedFrom") != null) {
        if (!validateShipFromString(r.getAttribute("ShippedFrom").toString())) {
          eIdStr = "0";
        }
        else
          r.setAttribute("ShippedFrom", r.getAttribute("ShippedFrom").toString().toUpperCase(Locale.US));
      }
      if (r.getAttribute("SupplementaryAddress") != null) {
        if (!validateSupplmentaryAddressString(r.getAttribute("SupplementaryAddress").toString())) {
          eIdStr = "0";
        }
        else
          r.setAttribute("SupplementaryAddress", r.getAttribute("SupplementaryAddress").toString().toUpperCase(Locale.US));
      }
      if (r.getAttribute("QuantityMeasured") != null && !validateQuantityMeasuredString(r.getAttribute("QuantityMeasured").toString())) {
        eIdStr = "0";
      }
      if (!validateQuantityInvoiced(r.getAttribute("QuantityInvoiced"))) {
        eIdStr = "0";
      }

      if (!validateQuantityReceived(getQuantityReceived())) {
        eIdStr = "0";
      }
      if (!allowDASFOverage && !validateDASFQty(documentNumberSuffix, getQuantityReceived())) {
        log.info("The dasf quantity is less than the receipt quantity {}", documentNumberSuffix);
        eIdStr = "1";
      }
      if (!validateQuantityInducted(r.getAttribute("QuantityInducted"))) {
        eIdStr = "0";
      }
      if (checkForPreviousTransactions(documentNumberSuffix, r.getAttribute("QuantityInducted"))) {
        log.info("Duplicate D6 transaction found.");
        eIdStr = "0";
      }
      if (!validateRI(r.getAttribute("Ri"))) {
        eIdStr = "0";
      }
      else
        r.setAttribute("Ri", r.getAttribute("Ri").toString().toUpperCase(Locale.US));
      if (eIdStr.length() > 0) {
        log.trace("end return eIdStr from ReceiptGeneral.performReceiptValidationsBeforeSave {}", eIdStr);
        return eIdStr;
      }
      log.trace("end 1.return default -1 from ReceiptGeneral.performReceiptValidationsBeforeSave ");
      return "-1";
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    log.trace("end 2.return default -1 from ReceiptGeneral.performReceiptValidationsBeforeSave ");
    return "-1";
  }

  /**
   * Set ui flag back on (true) to indicate error displayed and user save should continue
   */
  public final void setUiFlag(boolean show) {
    AdfFacesContext afContext = AdfFacesContext.getCurrentInstance();
    //* for use in the web, maintaining state
    afContext.getPageFlowScope().put("uiFlag", show);
    uiFlag = show;
  }

  public boolean getUiFlag() {
    AdfFacesContext afContext = AdfFacesContext.getCurrentInstance();
    boolean flag = uiFlag;
    Object obj = afContext.getPageFlowScope().get("uiFlag");
    if (obj != null)
      flag = Boolean.valueOf(obj.toString());

    return uiFlag || flag;
  }

  public String performReceiptValidationsAfterSave() {
    log.trace("begin ReceiptGeneral.performReceiptValidationsAfterSave");
    try {
      Row r = getReceiptAMService().getReceiptView1().getCurrentRow();
      Row er = null;
      boolean f = true;
      sForwardAction = "";
      uiDialog = false;
      String eIdStr = "";
      int uid = Integer.parseInt(JSFUtils.getManagedBeanValue("userbean.userId").toString());
      // Required validations

      //--------------------------------------------------------
      //Following is the UI validation for non-conversion UI

      r.setAttribute("Ui", r.getAttribute("Ui").toString().toUpperCase(Locale.US));
      double d = getReceiptAMService().validateUICheckAndReturnConvFactor(r.getAttribute("Ui").toString());
      NiinInfoLVOImpl nfv = getReceiptAMService().getNiinInfoLVO2();
      Row nr = nfv.getCurrentRow();
      //see if ERROR RECORD exists as Open in queue then just proceed
      // Other wise creaete the error queue record.
      // if everything good then delete any existing error record.
      f = true;
      setUiSame(true);
      setUiDiff(false);
      if (d == -1 && getUiFlag() != true)
        f = false;
      setUiFlag(false);
      er = getWorkloadManagerService().getErrorRow("RECEIPT_UI_NCON");
      if (!f) {

        if (Boolean.parseBoolean(JSFUtils.getManagedBeanValue("userbean.adminLevelUser").toString())) {
          setUiSame(false);
          setUiDiff(true);
          setUiFlag(true);
        }
        if (er != null) {
          setTextForDialog("Unable to convert unit of issue. See Supervisor.");
        }
        eIdStr = processErrorQueueRecord(er.getAttribute("Eid").toString(), "RCN", r.getAttribute("Rcn").toString());
        if (eIdStr.compareTo("-1") != 0) {
          log.trace("end 1.return eidStr from ReceiptGeneral.performReceiptValidationsAfterSave {}", eIdStr);
          return eIdStr;
        }
      }
      //--------------------------------------------------------
      //Following is the FSC validation
      f = getReceiptAMService().validateFsc(r.getAttribute("Fsc").toString());
      er = getWorkloadManagerService().getErrorRow("RECEIPT_FSC");
      if (!f) {
        if (er != null) {
          setTextForDialog(getWorkloadManagerService().buildDialogTxt(r.getAttribute("Fsc").toString() + "," + nr.getAttribute("Fsc").toString().trim(), er.getAttribute("ErrorDescription").toString()));
          eIdStr = processErrorQueueRecord(er.getAttribute("Eid").toString(), "RCN", r.getAttribute("Rcn").toString());
        }
        if (eIdStr.compareTo("-1") != 0) {
          log.trace("end 2.return eidStr from ReceiptGeneral.performReceiptValidationsAfterSave {}", eIdStr);
          return eIdStr;
        }
      }
      else {
        getWorkloadManagerService().deleteErrorQueueRecord(er.getAttribute("Eid").toString(), "RCN", r.getAttribute("Rcn").toString(), uid);
      }
      //--------------------------------------------------------
      //Following is the Quantity Receipt validation
      f = validateQuantityReceipt(r);
      er = getWorkloadManagerService().getErrorRow("RECEIPT_QTYRECV");
      if (!f) {
        if (er != null) {
          setTextForDialog(getWorkloadManagerService().buildDialogTxt(r.getAttribute("QuantityInvoiced").toString() + "," + getQuantityReceived(), er.getAttribute("ErrorDescription").toString()));
          eIdStr = processErrorQueueRecord(er.getAttribute("Eid").toString(), "RCN", r.getAttribute("Rcn").toString());
        }
        if (eIdStr.compareTo("-1") != 0) {
          log.trace("end 3.return eidStr from ReceiptGeneral.performReceiptValidationsAfterSave {}", eIdStr);
          return eIdStr;
        }
      }
      else {
        getWorkloadManagerService().deleteErrorQueueRecord(er.getAttribute("Eid").toString(), "RCN", r.getAttribute("Rcn").toString(), uid);
      }
      //--------------------------------------------------------
      //Following is the Quantity Inducted validation
      f = validateQuantityInducted(r);
      er = getWorkloadManagerService().getErrorRow("RECEIPT_QTYINDUC");
      if (!f) {
        if (er != null) {
          setTextForDialog(getWorkloadManagerService().buildDialogTxt("", er.getAttribute("ErrorDescription").toString()));
          eIdStr = processErrorQueueRecord(er.getAttribute("Eid").toString(), "RCN", r.getAttribute("Rcn").toString());
        }
        if (eIdStr.compareTo("-1") != 0) {
          log.trace("end 4.return eidStr from ReceiptGeneral.performReceiptValidationsAfterSave {}", eIdStr);
          return eIdStr;
        }
      }
      else {
        getWorkloadManagerService().deleteErrorQueueRecord(er.getAttribute("Eid").toString(), "RCN", r.getAttribute("Rcn").toString(), uid);
      }
      //--------------------------------------------------------
      //Following is the Cube validation
      String s1 = getReceiptAMService().validateNiinMeasurementsCube(r);
      if (s1.length() > 0) {
        er = getWorkloadManagerService().getErrorRow(s1);
        if (er != null) {
          setTextForDialog(getWorkloadManagerService().buildDialogTxt("", er.getAttribute("ErrorDescription").toString()));
          eIdStr = processErrorQueueRecord(er.getAttribute("Eid").toString(), "RCN", r.getAttribute("Rcn").toString());
        }
        if (eIdStr.compareTo("-1") != 0) {
          log.trace("end 5.return eidStr from ReceiptGeneral.performReceiptValidationsAfterSave {}", eIdStr);
          return eIdStr;
        }
      }
      else {
        getWorkloadManagerService().deleteErrorQueueRecord(er.getAttribute("Eid").toString(), "RCN", r.getAttribute("Rcn").toString(), uid);
      }
      //--------------------------------------------------------
      //Following is the Weight validation
      s1 = getReceiptAMService().validateNiinMeasurementsWeight(r);
      if (s1.length() > 0) {
        er = getWorkloadManagerService().getErrorRow(s1);
        if (er != null) {
          setTextForDialog(getWorkloadManagerService().buildDialogTxt("", er.getAttribute("ErrorDescription").toString()));
          eIdStr = processErrorQueueRecord(er.getAttribute("Eid").toString(), "RCN", r.getAttribute("Rcn").toString());
          if (eIdStr.compareTo("-1") != 0) {
          }
          log.trace("end 6.return eidStr from ReceiptGeneral.performReceiptValidationsAfterSave {}", eIdStr);
          return eIdStr;
        }
      }
      else {
        getWorkloadManagerService().deleteErrorQueueRecord(er.getAttribute("Eid").toString(), "RCN", r.getAttribute("Rcn").toString(), uid);
      }
      //---------------------------------------------------------
      log.trace("end 7.return default -1 from ReceiptGeneral.performReceiptValidationsAfterSave ");
      return "-1";
    }
    catch (
        Exception e) {
      AdfLogUtility.logException(e);
    }
    log.trace("end 8.return default -1 from ReceiptGeneral.performReceiptValidationsAfterSave ");
    return "-1";
  }

  public boolean performUIValidConversionValidation(Row r) {
    //Perform the conversion from Receipt UI to STRATIS UI and show the message.
    try {
      Row er = null;
      boolean f = true;
      r.setAttribute("Ui", r.getAttribute("Ui").toString().toUpperCase(Locale.US));
      double d = getReceiptAMService().validateUICheckAndReturnConvFactor(r.getAttribute("Ui").toString());
      NiinInfoLVOImpl nfv = getReceiptAMService().getNiinInfoLVO2();
      Row nr = nfv.getCurrentRow();
      if ((d > 0) && (d != 1))
        f = false;
      if (!f) {
        er = getWorkloadManagerService().getErrorRow("RECEIPT_UI_CONV");
        if (er != null) {
          JSFUtils.addFacesInformationMessage(getWorkloadManagerService().buildDialogTxt(r.getAttribute("Ui").toString() + "," + nr.getAttribute("Ui").toString().trim() + "," + d + "," + Math.round(Double.parseDouble(r.getAttribute("QuantityInvoiced").toString()) * d), er.getAttribute("ErrorDescription").toString()));
        }
        return dialogResponse; // Retrun the dialog response to descide further action
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return true;
  }

  public boolean performUIValidConversionValidationOld(Row r) {
    //Perform the conversion from Receipt UI to STRATIS UI and show the message.
    try {
      Row er = null;
      boolean f = true;
      sForwardAction = "dialog:GoReceivingGeneralUIDialog";
      uiDialog = true;
      double d = getReceiptAMService().validateUICheckAndReturnConvFactor(r.getAttribute("Ui").toString());
      NiinInfoLVOImpl nfv = getReceiptAMService().getNiinInfoLVO2();
      Row nr = nfv.getCurrentRow();
      if ((d > 0) && (d != 1))
        f = false;
      if (!f) {
        er = getWorkloadManagerService().getErrorRow("RECEIPT_UI_CONV");
        if (er != null) {
          setTextForDialog(getWorkloadManagerService().buildDialogTxt(nr.getAttribute("Ui").toString().trim() + "," + r.getAttribute("Ui") + "," + d + "," + Math.round(Double.parseDouble(r.getAttribute("QuantityInvoiced").toString()) * d), er.getAttribute("ErrorDescription").toString()));
        }
        return dialogResponse; // Retrun the dialog response to descide further action
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return true;
  }

  public void AssignQuantityReceived(ValueChangeEvent valueChangeEvent) {
    setQuantityReceived(valueChangeEvent.getNewValue().toString());
  }

  public boolean validateQuantityReceipt(Row r) {
    try {
      if (getQuantityReceived().length() <= 0)
        return false;
      if (Long.valueOf(getQuantityReceived().length()) == null)
        return false;
      if (getQuantityReceived().compareTo(r.getAttribute("QuantityInvoiced").toString()) != 0)
        return false;
      if (Integer.parseInt(getQuantityReceived()) <= 0)
        return false;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return true;
  }

  public boolean validateQuantityInducted(Row r) {
    try {
      if ((r.getAttribute("QuantityInducted") == null) || (getQuantityReceived() == null))
        return false;
      if (getQuantityReceived().compareTo(r.getAttribute("QuantityInducted").toString()) != 0)
        return false;
      if (Integer.parseInt(r.getAttribute("QuantityInducted").toString()) <= 0)
        return false;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return true;
  }

  public boolean validateQuantityInvoiced(Object d) {
    try {
      if ((d == null) || (!isNumericAA(d.toString()))) {
        JSFUtils.addFacesErrorMessage("RECIPT_QTYINV", "Invalid quantity invoiced.");
        return false;
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return true;
  }

  private boolean validateDASFQty(String documentNumberSuffix, String qtyReceived) {
    String documentNumber = documentNumberSuffix.substring(0, 14);
    return getReceiptAMService().validateDASFQty(documentNumber, qtyReceived);
  }

  private boolean checkForPreviousTransactions(String documentNumberSuffix, Object d) {
    String documentNumber = documentNumberSuffix.substring(0, 14);
    String suffix = documentNumberSuffix.length() > 14
        ? documentNumberSuffix.substring(14, 15)
        : "";
    int quantityInducted = Integer.parseInt(d.toString());
    boolean result = getReceiptAMService().checkPreviousTransactions(documentNumber, suffix, quantityInducted);
    if (result) {
      JSFUtils.addFacesErrorMessage("RECIPT_D6_TRANSACTION", "Duplicate D6 transaction found.");
    }
    return result;
  }

  public boolean validateQuantityInducted(Object d) {
    try {
      if (d != null) {
        Integer.parseInt(d.toString());
      }
      if (((d == null) || (!isNumericAA(d.toString()))) || (Integer.parseInt(d.toString()) <= 0)) {
        JSFUtils.addFacesErrorMessage("RECIPT_QTYIND", "Invalid quantity inducted.");
        return false;
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return true;
  }

  public boolean validateQuantityReceived(String d) {
    try {
      if (((d == null) || (!isNumericAA(d))) || (Integer.parseInt(d) <= 0)) {
        JSFUtils.addFacesErrorMessage("RECIPT_QTYREC", "Invalid quantity received.");
        return false;
      }
    }
    catch (Exception e) {
      JSFUtils.addFacesErrorMessage("RECIPT_QTYREC", "Invalid quantity received.");
      return false;
    }
    return true;
  }

  /**
   * Create error record without dialog prompt
   */
  public String createErrorQueueRecord(String eId, String keyTypStr,
                                       String keyIdStr) {
    //Check if the error record is already created in DB
    //otherwise call the routine to create the record and display the message dialog.
    try {
      if (!getWorkloadManagerService().findErrorQueueRecord(eId, keyTypStr, keyIdStr)) { // First time only
        getWorkloadManagerService().createErrorQueueRecord(eId, keyTypStr, keyIdStr, JSFUtils.getManagedBeanValue("userbean.userId").toString(), null);
        return eId;
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return "-1";
  }

  /**
   * Create error record and set dialog prompt
   */
  public String processErrorQueueRecord(String eId, String keyTypStr,
                                        String keyIdStr) {
    //Check if the error record is already created in DB
    //otherwise call the routine to create the record and display the message dialog.
    try {
      if (!getWorkloadManagerService().findErrorQueueRecord(eId, keyTypStr, keyIdStr)) { // First time only
        getWorkloadManagerService().createErrorQueueRecord(eId, keyTypStr, keyIdStr, JSFUtils.getManagedBeanValue("userbean.userId").toString(), null);
        sForwardAction = "dialog:GoReceivingGeneralUIDialog";
        uiDialog = true;
        return eId;
      }
      if // RECEIPT_FSC warning
        // RECEIPT_QTYRECV warning
        // RECEIPT_UI_CONV warning
        // RECEIPT_CUBE warning
        // RECEIPT_WGT warning
      ((eId.compareTo("1") == 0) || (eId.compareTo("4") == 0) || (eId.compareTo("6") == 0) || (eId.compareTo("8") == 0) || (eId.compareTo("9") == 0)) { // Subsequent do not need dialogs.
        sForwardAction = "";
        uiDialog = false;
        return "-1";
      }
      else { // Need to show dialog always
        sForwardAction = "dialog:GoReceivingGeneralUIDialog";
        uiDialog = true;
        return eId;
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return "-1";
  }

  public void BuildLocationList(ActionEvent event) {
    if (event != null) {
      //NO-OP can't remove parameter due to adf
    }
    try {
      Row r = getReceiptAMService().getStowView2().getCurrentRow();
      String errStr1 = getReceiptAMService().updateStowForAReceipt(((r == null) ? "1" : r.getAttribute("LocationId")), Long.parseLong(JSFUtils.getManagedBeanValue("userbean.userId").toString()), "Y");
      if (errStr1 != null && errStr1.length() > 0) {
        log.trace("an error occurred during buildLocationlist {}", errStr1);
        getWorkloadManagerService().clearLocationList();
        showStowErrors(errStr1);
        return;
      }
      String stowWAC = "";
      if (stowArea != null && stowArea.getValue() != null) {
        stowWAC = getStowArea().getValue().toString();
      }
      if (r != null) {
        errStr1 = getWorkloadManagerService().buildLocationList(stowWAC, Util.cleanString(r.getAttribute("ExpirationDate")), r.getAttribute("StowId").toString(), "ALL", ((r.getAttribute("QtyToBeStowed") == null) ? 0 : Integer.parseInt(r.getAttribute("QtyToBeStowed").toString())));
        if (errStr1 != null && errStr1.length() > 0)
          JSFUtils.addFacesErrorMessage("ERR", errStr1);
      }
      else {
        log.error("Error in ReceiptGeneral.BuildLocationList: Couldn't get Row from StowView.");
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  /**
   * Used to display stow errors messages and redirect the user
   */
  private void showStowErrors(String errorCodes) {
    try {
      String s;
      if (errorCodes.length() > 0) {
        if (errorCodes.contains("EXP_DATE")) {
          s = errorCodes.split("EXP_DATE")[1];
          log.error("Receipt has expired items for the SID(s) {}", s.split(";")[0]);
          JSFUtils.addFacesErrorMessage("EXP_DATE", "Receipt has expired items for the SID(s) " + s.split(";")[0]);
        }
        if (errorCodes.contains("MANUFACTURE_DATE")) {
          log.error("Manufacture or expiration date entry required.");
          JSFUtils.addFacesErrorMessage("Manufacture or expiration date entry required.");
        }
        if (errorCodes.contains("MANUFACTURE_AFTER")) {
          log.error("Manufacture date must be before the current date.");
          JSFUtils.addFacesErrorMessage("Manufacture date must be before the current date.");
        }
        if (errorCodes.contains("STOW_QTY_INV")) {
          log.error("Invalid Stow Quantity.");
          JSFUtils.addFacesErrorMessage("STOW_QTY_INV", "Invalid Stow Quantity.");
        }
        if (errorCodes.contains("STOW_LOC_INV")) {
          s = errorCodes.split("STOW_LOC_INV")[1];
          log.error("Please select and assign location for SID(s) {}", s.split(";")[0]);
          JSFUtils.addFacesErrorMessage("STOW_LOC_INV", "Please select and assign location for SID(s) " + s.split(";")[0]);
        }
        if (errorCodes.contains("STOW_LOCEXP_INV")) {
          s = errorCodes.split("STOW_LOCEXP_INV")[1];
          log.error("NIINs of selected location have different expiration dates compare to the SID(s) {} expiration date(s). Please assign different location(s).", s.split(";")[0]);
          JSFUtils.addFacesErrorMessage("STOW_LOCEXP_INV", "NIINs of selected location have different expiration dates compare to the SID(s) " + s.split(";")[0] + " expiration date(s). Please assign different location(s).");
        }
        if (errorCodes.contains("STOW_CC_MIS")) {
          s = errorCodes.split("STOW_CC_MIS")[1];
          log.error("CC(s) of NIIN(s) at this location do not match for SID(s) {}. Please assign different location(s).", s.split(";")[0]);
          JSFUtils.addFacesErrorMessage("STOW_CC_MIS", "CC(s) of NIIN(s) at this location do not match for SID(s) " + s.split(";")[0] + ". Please assign different location(s).");
        }
        if (errorCodes.contains("DUPLICATE_SERIAL_NUM")) {
          s = errorCodes.split("DUPLICATE_SERIAL_NUM")[1];
          log.error("Serial numbers of these SID(s) {} are already in STRATIS system.", s.split(";")[0]);
          JSFUtils.addFacesErrorMessage("DUPLICATE_SERIAL_NUM", "Serial numbers of these SID(s) " + s.split(";")[0] + " are already in STRATIS system.");
        }
        if (errorCodes.contains("DUPLICATE_LOT_NUM")) {
          s = errorCodes.split("DUPLICATE_LOT_NUM")[1];
          log.error("Lot control number of these SID(s) {} are already in STRATIS system.", s.split(";")[0]);
          JSFUtils.addFacesErrorMessage("DUPLICATE_LOT_NUM", "Lot control number of these SID(s) " + s.split(";")[0] + " are already in STRATIS system.");
        }
        if (errorCodes.contains("INV_QTY_WITH_SERIAL_NUM")) {
          s = errorCodes.split("INV_QTY_WITH_SERIAL_NUM")[1];
          log.error("These SID(s) {} have serial number so quantity should be 1 for each stow.", s.split(";")[0]);
          JSFUtils.addFacesErrorMessage("INV_QTY_WITH_SERIAL_NUM", "These SID(s) " + s.split(";")[0] + " have serial number so quantity should be 1 for each stow.");
        }
        if (errorCodes.contains("MISSING_SERIAL_NUM")) {
          s = errorCodes.split("MISSING_SERIAL_NUM")[1];
          log.error("Serial numbers of these SID(s) {} are missing.", s.split(";")[0]);
          JSFUtils.addFacesErrorMessage("MISSING_SERIAL_NUM", "Serial numbers of these SID(s) " + s.split(";")[0] + " are missing.");
        }
        if (errorCodes.contains("MISSING_LOT_CON_NUM")) {
          s = errorCodes.split("MISSING_LOT_CON_NUM")[1];
          log.error("Lot controll numbers of these SID(s) {} are missing.", s.split(";")[0]);
          JSFUtils.addFacesErrorMessage("MISSING_LOT_CON_NUM", "Lot controll numbers of these SID(s) " + s.split(";")[0] + " are missing.");
        }
        if (errorCodes.contains("D6_DUPLICATE")) {
          log.error("Duplicate D6 transaction found.");
          JSFUtils.addFacesErrorMessage("Duplicate D6 transaction found.");
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  public void setIssQty(ValueChangeEvent valueChangeEvent) {
    try {
      setIssQty(valueChangeEvent.getNewValue().toString());
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  public boolean validateRI(Object object) {
    try {
      if ((object == null) || (object.toString().length() != 3)) {
        JSFUtils.addFacesErrorMessage("RI_ERR", "Please enter valid RI to send charges.");
        return false;
      }
      else
        return true;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return false;
  }

  public boolean validateConsolidationBarCode(Object object) {
    try {
      if ((object == null) || ((object.toString().length() < 5) || (object.toString().length() > 10))) {
        JSFUtils.addFacesErrorMessage("CON_BAR_ERR", "Shipping Barcode must be 5-10 alphanumeric characters only.");
        return false;
      }
      else if (getReceiptAMService().validateConsolidationBarcode(object.toString())) {
        JSFUtils.addFacesErrorMessage("CON_BAR_ERR", "Consolidation barcode already in use.");
        return false;
      }
      else {
        return true;
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return false;
  }

  private static boolean isNumericAA(String s) {
    //One or more digits
    return s.matches("[0-9]+");
  }

  private static boolean isNumericB(String s) {
    return s.matches("([0-9]+.[0-9]+)|[0-9]*");
  }

  public String validateForTranshipment() {
    try {
      boolean f = true;
      String rId = " ";
      String eIdStr = "";
      Row r = getReceiptAMService().getReceiptView1().getCurrentRow();
      Row er = null;
      String docNo = r.getAttribute("DocumentNumber").toString();
      docNo = docNo + ((r.getAttribute("Suffix") == null) ? "" : r.getAttribute("Suffix").toString().trim());
      int uid = Integer.parseInt(JSFUtils.getManagedBeanValue("userbean.userId").toString());
      if (r.getAttribute("RoutingId") != null)
        rId = r.getAttribute("RoutingId").toString(); //get route Id
      er = getWorkloadManagerService().getErrorRow("RECEIPT_DOCNO_RI");
      f = getReceiptAMService().validateDocumentNumberRoute(docNo, rId); // true means it is TRANSHIPMENT
      if (f) {
        if (er != null) {
          setTextForDialog(getWorkloadManagerService().buildDialogTxt(docNo, er.getAttribute("ErrorDescription").toString()));
          eIdStr = processErrorQueueRecord(er.getAttribute("Eid").toString(), "RCN", r.getAttribute("Rcn").toString());
        }
        if (eIdStr.compareTo("-1") != 0)
          return eIdStr;
      }
      else {
        if (er != null)
          getWorkloadManagerService().deleteErrorQueueRecord(er.getAttribute("Eid").toString(), "RCN", r.getAttribute("Rcn").toString(), uid);
      }
      return "-1";
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return "-1";
  }

  public String getCubiscanReading() {
    String prnStr = "MEASURE";
    //Activate this when ready for COM
    if (prnStr.length() > 0) {
      // turn the comm on
      JSFUtils.setManagedBeanValue("userbean.useprintcom2", 1);
      // set the id of which comm to use from the com_port table
      JSFUtils.setManagedBeanValue("userbean.printcomport2", JSFUtils.getManagedBeanValue("userbean.comCommandIndex"));
      // set the string to go out
      JSFUtils.setManagedBeanValue("userbean.printcomstring2", prnStr);
    }
    return "GoReceivingGeneral";
  }

  public String getDisplayNone() {
    displayNone = "";
    return displayNone;
  }

  public void setDisplayNone(String s) {
    displayNone = s;
  }

  public void addSrlLotNumList(ActionEvent event) {
    if (event != null) {
      //NO-OP can't remove parameter due to adf
    }
    try {
      Row ro = getReceiptAMService().getIssueBackordersVO1().getCurrentRow();

      String kErr = "";
      String srlNum = getSrlOrLotConNum().getLocalValue() == null ? "" : getSrlOrLotConNum().getLocalValue().toString().toUpperCase(Locale.US).trim();
      DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
      String expDate1 = (getExpirationDate().getLocalValue() == null || getExpirationDate().getLocalValue().toString().length() <= 0) ? "" : df.format(getExpirationDate().getLocalValue());

      Date expDate2 = null;
      if (expDate1 != null && expDate1.length() > 0)
        expDate2 = new Date(expDate1);
      String actQty = "";
      if (Integer.parseInt(ro.getAttribute("Iqty").toString()) > Integer.parseInt(ro.getAttribute("AvailQty").toString()))
        actQty = ro.getAttribute("AvailQty").toString();
      else
        actQty = ro.getAttribute("Iqty").toString();
      if (ro.getAttribute("SerialControlFlag").toString().equalsIgnoreCase("Y")) {
        if ((getSrlOrLotConNum().getValue() == null) || (getSrlOrLotConNum().getValue().toString().length() <= 0)) {
          if ((getSrlConNumTo().getValue() == null) || (getSrlConNumTo().getValue().toString().length() <= 0) || (getSrlConNumFrom().getValue() == null) || (getSrlConNumFrom().getValue().toString().length() <= 0)) {
            errStr = errStr + "SL_ERR";
            JSFUtils.addFacesErrorMessage("SL_ERR", "Please enter Serial Number.");
            return;
          }
        }
        if ((expDate2 != null) && (!validateExpiration(expDate2))) { //04-19-2010
          errStr = errStr + "SL_ERR";
          JSFUtils.addFacesErrorMessage("SL_ERR", "Invalid Expiration Date.");
          return;
        }
      }
      else
        errStr = "";
      if (errStr == null || errStr.length() <= 0) {
        ArrayList<String> serialnums = new ArrayList<String>();
        if ((getSrlOrLotConNum().getValue() != null) && (getSrlOrLotConNum().getValue().toString().length() > 0)) {
          serialnums.add(srlNum);
        }
        else {
          errStr = getWorkloadManagerService().generateSerialRange(getSrlConNumFrom().getValue().toString(), getSrlConNumTo().getValue().toString(), serialnums);
        }
        if (errStr == null || errStr.length() <= 0) {
          for (int i = 0; i < serialnums.size(); i++) {
            srlNum = serialnums.get(i);
            errStr = getReceiptAMService().checkSerial(ro.getAttribute("Rcn").toString(), srlNum, "", ro.getAttribute("NiinId").toString());
            if (errStr.length() <= 0) {
              kErr = getReceiptAMService().addSerialOrLotNumVOList(srlNum, "", "", actQty, expDate2);
            }
            else {
              JSFUtils.addFacesErrorMessage("SL_ERR", errStr);
            }
          }

          SerialOrLoNumRImpl vo = getReceiptAMService().getSerialOrLoNumR1();
          vo.setSortBy("SerialOrLoNum");
          vo.setQueryMode(ViewObject.QUERY_MODE_SCAN_VIEW_ROWS);
          vo.executeQuery();
        }
        else {
          kErr = errStr;
        }
      }

      if (kErr.length() > 0) {
        errStr = errStr + "SL_ERR";
        if (kErr.contains("Quantity did not match")) {
          getSrlOrLotConNum().setValue("");
          getSrlConNumTo().setValue("");
          getSrlConNumFrom().setValue("");
          getExpirationDate().setValue("");
        }
        else
          JSFUtils.addFacesErrorMessage("SL_ERR", kErr);
      }
      else {
        getSrlOrLotConNum().setValue("");
        getSrlConNumTo().setValue("");
        getSrlConNumFrom().setValue("");
        getExpirationDate().setValue("");
      }
    }
    catch (Exception e) {

      AdfLogUtility.logException(e);
    }
  }

  public void deleteSrlLotNumFromList(ActionEvent event) {
    if (event != null) {
      //NO-OP can't remove parameter due to adf
    }
    try {
      if (getReceiptAMService().getSerialOrLoNumR1().getRowCount() > 0) {
        getReceiptAMService().getSerialOrLoNumR1().removeCurrentRowFromCollection();
      }
    }
    catch (Exception e) {log.trace("Exception caught and ignored", e);}
  }

  public String validateSerialLotControl(int relQty, int typeCon) {
    // typeCon 0 = lot, 1 = serial, 2 = both
    try {
      int rowCou = getReceiptAMService().getSerialOrLoNumR1().getRowCount();
      if (rowCou == 0)
        return "No Valid Serial Numbers found.";
      if (typeCon == 0 && rowCou != relQty)
        return "Release qty " + relQty + " must match Serial Number qty.";
      int cou = 0;
      int qty = 0;
      Row ro = null;
      boolean error = false;
      while ((cou < rowCou) && (!error)) {
        if (cou == 0)
          ro = getReceiptAMService().getSerialOrLoNumR1().first();
        else
          ro = getReceiptAMService().getSerialOrLoNumR1().next();
        qty = qty + Integer.parseInt(ro.getAttribute("QtyLot") == null ? "0" : ro.getAttribute("QtyLot").toString());
        if (typeCon > 0 && (ro.getAttribute("LotConNum") == null || ro.getAttribute("LotConNum").toString().length() <= 0))
          error = true;
        cou++;
      }
      if (typeCon > 0 && error)
        return "Missing Lot number(s).";
      if (typeCon > 0 && qty != relQty) {
        log.trace("Lot number qty {} does not match release qty {}", qty, relQty);
        return "Release qty must match Lot Number qty.";
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return "";
  }

  /**
   * Further validate for duplicate serial/lot numbers
   */
  public String furtherValidateSerialLotControl(int relQty, int typeCon,
                                                String niinId) {
    if (relQty > 0) {}

    // typeCon 0 = lot, 1 = serial, 2 = both
    String priorList = "";
    try {
      int rowCou = getReceiptAMService().getSerialOrLoNumR1().getRowCount();
      int cou = 0;
      int i = 0;
      Row ro = null;
      boolean error = false;
      while (cou < rowCou) {
        if (cou == 0)
          ro = getReceiptAMService().getSerialOrLoNumR1().first();
        else
          ro = getReceiptAMService().getSerialOrLoNumR1().next();
        if (typeCon > 0 && (ro.getAttribute("SerialOrLoNum") == null || ro.getAttribute("SerialOrLoNum").toString().length() <= 0)) {
          String serialNum = ro.getAttribute("SerialOrLoNum").toString().trim();
          ItemControlValidation itemControlValidation = new ItemControlValidation(serialNum, niinId, getReceiptAMService().getDBTransaction());
          error = !(itemControlValidation.isItemValid());
          if (error) {
            if (i != 0)
              priorList += ", ";
            priorList += serialNum;
            i++;
          }
        }
        cou++;
      }
      if (typeCon > 0 && error) {

        return "Some Serial Numbers already exist on hand or have been issued prior. Confirm following : " + priorList;
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return "";
  }

  /**
   * Validation method for DateOfManufacture
   */
  public boolean validateExpiration(Date data) {
    if (data != null) {
      return data.compareTo(Date.getCurrentDate()) >= 0;
    }
    return false;
  }

  public void CancelCrossDock(ActionEvent event) {
    if (event != null) {
      //NO-OP can't remove parameter due to adf
    }
    try {
      Row ro = getReceiptAMService().getIssueBackordersVO1().getCurrentRow();
      String rcn = Util.cleanString(ro.getAttribute("Rcn"));
      sForwardAction = getReceiptAMService().cancelMyCrossdock(rcn);
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      sForwardAction = "";
    }
  }

  public void SaveCrossDock(ActionEvent event) {
    if (event != null) {
      //NO-OP can't remove parameter due to adf
    }
    log.trace("begin ReceiptGeneral.SaveCrossdock");
    try {
      Row ro = getReceiptAMService().getIssueBackordersVO1().getCurrentRow();
      int flag = -1;
      int iQtyV = Integer.parseInt(ro.getAttribute("Iqty").toString());
      int aVailQtyV = Integer.parseInt(ro.getAttribute("AvailQty").toString());
      if (aVailQtyV < iQtyV)
        iQtyV = aVailQtyV;
      if (ro.getAttribute("SerialControlFlag").toString().equalsIgnoreCase("Y") || ro.getAttribute("LotControlFlag").toString().equalsIgnoreCase("Y")) {
        if (ro.getAttribute("SerialControlFlag").toString().equalsIgnoreCase("Y") && ro.getAttribute("LotControlFlag").toString().equalsIgnoreCase("Y"))
          flag = 2;
        else if (ro.getAttribute("SerialControlFlag").toString().equalsIgnoreCase("Y"))
          flag = 0;
        else if (ro.getAttribute("LotControlFlag").toString().equalsIgnoreCase("Y"))
          flag = 1;
        errStr = validateSerialLotControl(iQtyV, flag);
        if (errStr.length() > 0)
          JSFUtils.addFacesErrorMessage("SR_OR_LOT_ERR", errStr);
        else {
          errStr = furtherValidateSerialLotControl(iQtyV, flag, ro.getAttribute("NiinId").toString());
          if (errStr.length() > 0)
            JSFUtils.addFacesErrorMessage("SRL_ERR", errStr);
        }
      }
      if ((errStr.length() <= 0) && (validateConsolidationBarCode(getConsolidationBarCode().getValue()))) {
        sForwardAction = getReceiptAMService().saveCrossDock(Long.parseLong(JSFUtils.getManagedBeanValue("userbean.userId").toString()));
      }
      else
        sForwardAction = "";
      if (sForwardAction.compareTo("QTY_RELEASED_INV_M_R") == 0) {
        JSFUtils.addFacesErrorMessage("QTY_RELEASED_INV_M_R", "If quantity backorder is more then available quantity, then use maximum available quantity.");
        sForwardAction = "";
      }
      else if (sForwardAction.compareTo("QTY_RELEASED_INV") == 0) {
        JSFUtils.addFacesErrorMessage("QTY_RELEASED_INV", "Please enter correct quantity.");
        sForwardAction = "";
      }
      else if (sForwardAction.compareTo("D6_DUPLICATE") == 0) {
        log.error("Duplicate D6 transaction found.");
        JSFUtils.addFacesErrorMessage("Duplicate D6 transaction found.");
        sForwardAction = "GoReceivingGeneral";
      }
      else if (sForwardAction.length() > 0) {
        getWorkloadManagerService().initiateCrossDock(ro.getAttribute("Scn").toString(), getConsolidationBarCode().getValue().toString().trim().toUpperCase(Locale.US), iQtyV, Integer.parseInt(JSFUtils.getManagedBeanValue("userbean.userId").toString()), Integer.parseInt(JSFUtils.getManagedBeanValue("userbean.workstationId").toString()));
        //Setting the applet for 1348 print
        JSFUtils.setManagedBeanValue("userbean.useprintframe", "1");
        JSFUtils.setManagedBeanValue("userbean.winPrintSCN", ro.getAttribute("Scn").toString() + "," + iQtyV);
        getConsolidationBarCode().setValue("");
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    log.trace("endn ReceiptGeneral.SaveCrossdock :: sForwardAction is {}", sForwardAction);
  }

  public void saveTransshipment(ActionEvent event) {
    if (event != null) {
      //NO-OP can't remove parameter due to adf
    }
    try {
      Row ro = getReceiptAMService().getReceiptTransshipmentVO1().getCurrentRow();
      if (validateConsolidationBarCode(getConsolidationBarCode().getValue())) {
        val rcn = Integer.parseInt(ro.getAttribute("Rcn").toString());
        val userId = JSFUtils.getManagedBeanValue("userbean.userId").toString();
        sForwardAction = getReceiptAMService().saveTransshipment(rcn, Long.parseLong(userId));
        //Perform initiate transshipment
        String scnStr = getWorkloadManagerService().initiateTransShipment(rcn, getConsolidationBarCode().getValue().toString().toUpperCase(Locale.US), Integer.parseInt(userId), Integer.parseInt(JSFUtils.getManagedBeanValue("userbean.workstationId").toString()));
        if (!scnStr.contains("SCN")) {
          JSFUtils.addFacesErrorMessage("TRANSSHIPMENT_", scnStr);
          sForwardAction = "";
        }
        else {
          //Setting the applet for 1348 print
          JSFUtils.setManagedBeanValue("userbean.useprintframe", "1");
          JSFUtils.setManagedBeanValue("userbean.winPrintSCN", scnStr.split(" ")[1] + "," + ro.getAttribute("QuantityInducted"));
        }
      }
      else
        sForwardAction = "";
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  public void computeNum1(ActionEvent event) {
    if (event != null) {
      //NO-OP can't remove parameter due to adf
    }
    String num = "";
    num = computeNum(startSerial1, endSerial1);
    if (num.length() > 0) {
      numSerial1 = num;
    }
  }

  public void computeNum2(ActionEvent event) {
    if (event != null) {
      //NO-OP can't remove parameter due to adf
    }
    String num = "";
    num = computeNum(startSerial1, endSerial1);
    if (num.length() > 0) {
      numSerial1 = num;
    }
    num = computeNum(startSerial2, endSerial2);
    if (num.length() > 0) {
      numSerial2 = num;
    }
  }

  public void computeNum3(ActionEvent actionEvent) {
    if (actionEvent != null) {
      //NO-OP can't remove parameter due to adf
    }
    String num = "";
    num = computeNum(startSerial1, endSerial1);
    if (num.length() > 0) {
      numSerial1 = num;
    }
    num = computeNum(startSerial2, endSerial2);
    if (num.length() > 0) {
      numSerial2 = num;
    }
    num = computeNum(startSerial3, endSerial3);
    if (num.length() > 0) {
      numSerial3 = num;
    }
  }

  public void clearRanges(ActionEvent actionEvent) {
    if (actionEvent != null) {
      //NO-OP can't remove parameter due to adf
    }
    clearSerialRanges();
  }

  public void clearArea(ActionEvent actionEvent) {
    if (actionEvent != null) {
      //NO-OP can't remove parameter due to adf
    }
    clearSerialFields();
  }

  public String computeNum(RichInputText startSerial, RichInputText endSerial) {
    String errStr = "";
    ArrayList serialList = new ArrayList();
    if ((startSerial.getValue() == null) || (endSerial.getValue() == null)) {
      JSFUtils.addFacesErrorMessage("SerialNumber", "Please enter a valid range");
      return "";
    }
    else {
      errStr = getWorkloadManagerService().generateSerialRange(startSerial.getValue().toString(), endSerial.getValue().toString(), serialList);
      if (errStr.length() > 0) {
        JSFUtils.addFacesErrorMessage("SerialNumber", errStr);
        return "";
      }
      else
        return Integer.toString(serialList.size());
    }
  }

  public void computeArea(ActionEvent actionEvent) {
    if (actionEvent != null) {
      //NO-OP can't remove parameter due to adf
    }
    String errStr = "";
    sForwardAction = "";
    boolean duplicate = false;
    duplicate = compileSerials();
    if (duplicate) {
      JSFUtils.addFacesErrorMessage("SerialNumber", "Duplicate Serial Number entered.");
    }
    else {

      if (serialSet.size() > 0) {
        errStr = getReceiptAMService().updateAllSerials(serialSet, Long.parseLong(JSFUtils.getManagedBeanValue("userbean.userId").toString()));
        if (errStr.isEmpty()) {
          serialSet.clear();
          reviewSerial = false;
          sForwardAction = "GoReceivingGeneralDetail";
          clearSerialFields();
        }
        else {
          JSFUtils.addFacesErrorMessage("SerialNumber", errStr);
          processingMsg.setVisible(false);
        }
      }
    }
  }

  public void reviewSerials(ActionEvent actionEvent) {
    if (actionEvent != null) {
      //NO-OP can't remove parameter due to adf
    }
    boolean duplicate = false;
    duplicate = compileSerials();
    clearSerialFields();
    String fullSerialList = "";
    for (String s : serialSet) {
      fullSerialList = fullSerialList + s + "\n";
    }
    serialArea.setValue(fullSerialList);
    reviewSerial = false;
    if (duplicate) {
      JSFUtils.addFacesErrorMessage("SerialNumber", "Duplicate Serial Number entered.");
    }
  }

  public void goReviewSerial(ActionEvent event) {
    if (event != null) {
      //NO-OP can't remove parameter due to adf
    }
    reviewSerial = false;
    ADFContext.getCurrent().getSessionScope().put("reviewSerial", false);
  }

  /**
   * Creates a set out of all the serial numbers entered
   */
  public void clearSerialFields() {
    clearSerialRanges();
    clearSerialArea();
  }

  /**
   * Clears the serialRanges
   */
  public void clearSerialRanges() {
    startSerial1.setValue("");
    endSerial1.setValue("");
    numSerial1 = "";
    startSerial2.setValue("");
    endSerial2.setValue("");
    numSerial2 = "";
    startSerial3.setValue("");
    endSerial3.setValue("");
    numSerial3 = "";
  }

  /**
   * Clears serial Area
   */
  public void clearSerialArea() {
    serialArea.setValue("");
  }

  /**
   * Creates a set out of all the serial numbers entered
   */
  //05-30-2014 Added || and ! to the add function. Set add returns true if it was added successfully. This way ensures if a duplicate is entered, a true would be put into the duplicate flag and returned.
  public boolean compileSerials() {
    serialSet.clear();
    String errStr = "";
    sForwardAction = "";
    boolean duplicateFlag = false;
    if (((startSerial1.getValue() != null) && (endSerial1.getValue() != null)) || ((startSerial2.getValue() != null) && (endSerial2.getValue() != null)) || ((startSerial3.getValue() != null) && (endSerial3.getValue() != null)) || (serialArea.getValue() != null)) {
      //We have entries to process
      if (serialArea.getValue() != null) {
        String area = serialArea.getValue().toString().trim();
        if (area.length() > 0) {
          String[] theArray = area.split("\\n+");
          for (int i = 0; i < theArray.length; i++) {
            duplicateFlag = duplicateFlag || !serialSet.add(theArray[i].trim().toUpperCase(Locale.US));
          }
        }
      }

      if ((startSerial1.getValue() != null) && (endSerial1.getValue() != null)) {
        ArrayList serialList = new ArrayList();
        errStr = getWorkloadManagerService().generateSerialRange(startSerial1.getValue().toString(), endSerial1.getValue().toString(), serialList);
        if (errStr.length() <= 0) {
          for (int i = 0; i < serialList.size(); i++)
            duplicateFlag = duplicateFlag || !serialSet.add((String) serialList.get(i));
        }
      }

      if ((startSerial2.getValue() != null) && (endSerial2.getValue() != null)) {
        ArrayList serialList = new ArrayList();
        errStr = getWorkloadManagerService().generateSerialRange(startSerial2.getValue().toString(), endSerial2.getValue().toString(), serialList);
        if (errStr.length() <= 0) {
          for (int i = 0; i < serialList.size(); i++)
            duplicateFlag = duplicateFlag || !serialSet.add((String) serialList.get(i));
        }
      }

      if ((startSerial3.getValue() != null) && (endSerial3.getValue() != null)) {
        ArrayList serialList = new ArrayList();
        errStr = getWorkloadManagerService().generateSerialRange(startSerial3.getValue().toString(), endSerial3.getValue().toString(), serialList);
        if (errStr.length() <= 0) {
          for (int i = 0; i < serialList.size(); i++)
            duplicateFlag = duplicateFlag || !serialSet.add((String) serialList.get(i));
        }
      }

      if (serialSet.size() <= 0) {
        JSFUtils.addFacesErrorMessage("SerialNumber", "No Valid Serial Numbers found");
      }
    }
    else {
      JSFUtils.addFacesErrorMessage("SerialNumber", "Please enter serial numbers");
    }
    return duplicateFlag;
  }

  public void qtyInvValidation(FacesContext facesContext,
                               UIComponent uiComponent,
                               Object object) {
    boolean hasError = false;
    String message = "";
    if (facesContext != null) {}
    if (uiComponent != null) {}
    try {
      String qtyInv = "";
      if (object != null) {
        qtyInv = object.toString();
        if (RegUtils.isDecimal(qtyInv) || RegUtils.isNotNumeric(qtyInv)) {
          message = "Qty must be a whole number.";
          hasError = true;
        }
      }
      else {
        hasError = true;
        message = "Qty must be a whole number.";
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    if (hasError) {
      throw new ValidatorException(new FacesMessage(message));
    }
  }

  public void setExpirationDate(RichInputDate expirationDate) {
    this.expirationDate = expirationDate;
  }

  public RichInputDate getExpirationDate() {
    return expirationDate;
  }

  public String getForwardAction() {
    log.trace("action: {}", sForwardAction);
    return sForwardAction;
  }

  public void setQuantityReceived(String quantityReceived) {
    this.quantityReceived = quantityReceived;
  }

  public String getQuantityReceived() {
    return quantityReceived;
  }

  public void setIsControlled(String isControlled) {
    this.isControlled = isControlled;
  }

  public String getIsControlled() {
    return isControlled;
  }

  // whether to show the logout item
  public boolean isReviewSerial() {
    if (ADFContext.getCurrent().getSessionScope().get("reviewSerial") != null) {
      reviewSerial = false;
      return reviewSerial;
    }
    else
      return reviewSerial;
  }

  public void setReviewSerial(boolean reviewSerial) {
    this.reviewSerial = reviewSerial;
  }

  public boolean getReviewSerial() {
    return reviewSerial;
  }

  public void setRcnStr(String rcnStr) {
    this.rcnStr = rcnStr;
  }

  public String getRcnStr() {
    return rcnStr;
  }

  public void setEId(String eId) {
    this.eId = eId;
  }

  public String getEId() {
    return eId;
  }

  public void setStowArea(RichInputText stowInput) {
    stowArea = stowInput;
  }

  public RichInputText getStowArea() {
    return stowArea;
  }

  public void setIssQty(String issQty) {
    this.issQty = issQty;
  }

  public String getIssQty() {
    return issQty;
  }

  public void setRecepitWindowRender(boolean recepitWindowRender) {
    this.recepitWindowRender = recepitWindowRender;
  }

  public boolean isRecepitWindowRender() {
    return recepitWindowRender;
  }

  public void setConsolidationBarCode(RichInputText consolidationBarCode) {
    this.consolidationBarCode = consolidationBarCode;
  }

  public RichInputText getConsolidationBarCode() {
    return consolidationBarCode;
  }

  public void setSaveReceiptRender(boolean saveReceiptRender) {
    this.saveReceiptRender = saveReceiptRender;
  }

  public boolean isSaveReceiptRender() {
    return saveReceiptRender;
  }

  public void setUiDiff(boolean uiDiff) {
    this.uiDiff = uiDiff;
  }

  public boolean isUiDiff() {
    return uiDiff;
  }

  public void setUiSame(boolean uiSame) {
    this.uiSame = uiSame;
  }

  public boolean isUiSame() {
    return uiSame;
  }

  public void setExistingLocation(RichSelectOneChoice existingLocation) {
    this.existingLocation = existingLocation;
  }

  public RichSelectOneChoice getExistingLocation() {
    return existingLocation;
  }

  public void setUiConvFactor(double uiConvFactor) {
    this.uiConvFactor = uiConvFactor;
  }

  public double getUiConvFactor() {
    return uiConvFactor;
  }

  public void setUiDialog(boolean uiDialog) {
    this.uiDialog = uiDialog;
  }

  public boolean getUiDialog() {
    return uiDialog;
  }

  public void setCubeReceiptStr(String cubeReceiptStr) {
    this.cubeReceiptStr = cubeReceiptStr;
  }

  public String getCubeReceiptStr() {
    return cubeReceiptStr;
  }

  public void setTextForDialog(String textForDialog) {
    this.textForDialog = textForDialog;
  }

  public String getTextForDialog() {
    return textForDialog;
  }

  public void setDialogResponse(boolean dialogResponse) {
    this.dialogResponse = dialogResponse;
  }

  public boolean isDialogResponse() {
    return dialogResponse;
  }

  public void setDocAndSuStr(RichInputText docAndSuStr) {
    this.docAndSuStr = docAndSuStr;
  }

  public RichInputText getDocAndSuStr() {
    return docAndSuStr;
  }

  public void setPopupDasf(RichPopup popupDasf) {
    this.popupDasf = popupDasf;
  }

  public RichPopup getPopupDasf() {
    return popupDasf;
  }

  public void setNsnStr(RichInputText nsnStr) {
    this.nsnStr = nsnStr;
  }

  public RichInputText getNsnStr() {
    return nsnStr;
  }

  public void setCancelSIDStr(RichInputText cancelSID) {
    this.cancelSIDStr = cancelSID;
  }

  public RichInputText getCancelSIDStr() {
    return cancelSIDStr;
  }

  public void setRiQUiPrStr(RichInputText riQUiPrStr) {
    this.riQUiPrStr = riQUiPrStr;
  }

  public RichInputText getRiQUiPrStr() {
    return riQUiPrStr;
  }

  public void setTwoDScnStr(RichInputText twoDScnStr) {
    this.twoDScnStr = twoDScnStr;
  }

  public RichInputText getTwoDScnStr() {
    return twoDScnStr;
  }

  public BindingContainer getBindings() {
    return bindings;
  }

  public void setBindings(BindingContainer bindings) {
    this.bindings = bindings;
  }

  public void setLotConNum(RichInputText lotConNum) {
    this.lotConNum = lotConNum;
  }

  public RichInputText getLotConNum() {
    return lotConNum;
  }

  public void setSrlOrLotConNum(RichInputText srlOrLotConNum) {
    this.srlOrLotConNum = srlOrLotConNum;
  }

  public RichInputText getSrlOrLotConNum() {
    return srlOrLotConNum;
  }

  public void setSrlConNumTo(RichInputText srlConNumTo) {
    this.srlConNumTo = srlConNumTo;
  }

  public RichInputText getSrlConNumTo() {
    return srlConNumTo;
  }

  public void setSrlConNumFrom(RichInputText srlConNumFrom) {
    this.srlConNumFrom = srlConNumFrom;
  }

  public RichInputText getSrlConNumFrom() {
    return srlConNumFrom;
  }

  public void setQtyLot(RichInputText qtyLot) {
    this.qtyLot = qtyLot;
  }

  public RichInputText getQtyLot() {
    return qtyLot;
  }

  public void setSuffixStr(RichInputText suffixStr) {
    this.suffixStr = suffixStr;
  }

  public RichInputText getSuffixStr() {
    return suffixStr;
  }

  public void setChkPartialShipment(RichSelectBooleanCheckbox chkPartialShipment) {
    this.chkPartialShipment = chkPartialShipment;
  }

  public RichSelectBooleanCheckbox getChkPartialShipment() {
    return chkPartialShipment;
  }

  public void setStartSerial1(RichInputText startSerial1) {
    this.startSerial1 = startSerial1;
  }

  public RichInputText getStartSerial1() {
    return startSerial1;
  }

  public void setEndSerial1(RichInputText endSerial1) {
    this.endSerial1 = endSerial1;
  }

  public RichInputText getEndSerial1() {
    return endSerial1;
  }

  public void setNumSerial1(String numSerial1) {
    this.numSerial1 = numSerial1;
  }

  public String getNumSerial1() {
    return numSerial1;
  }

  public void setStartSerial2(RichInputText startSerial2) {
    this.startSerial2 = startSerial2;
  }

  public RichInputText getStartSerial2() {
    return startSerial2;
  }

  public void setEndSerial2(RichInputText endSerial2) {
    this.endSerial2 = endSerial2;
  }

  public RichInputText getEndSerial2() {
    return endSerial2;
  }

  public void setNumSerial2(String numSerial2) {
    this.numSerial2 = numSerial2;
  }

  public String getNumSerial2() {
    return numSerial2;
  }

  public void setStartSerial3(RichInputText startSerial3) {
    this.startSerial3 = startSerial3;
  }

  public RichInputText getStartSerial3() {
    return startSerial3;
  }

  public void setEndSerial3(RichInputText endSerial3) {
    this.endSerial3 = endSerial3;
  }

  public RichInputText getEndSerial3() {
    return endSerial3;
  }

  public void setNumSerial3(String numSerial3) {
    this.numSerial3 = numSerial3;
  }

  public String getNumSerial3() {
    return numSerial3;
  }

  public void setProcessingMsg(RichOutputText message) {
    this.processingMsg = message;
  }

  public RichOutputText getProcessingMsg() {
    return processingMsg;
  }

  public void setSerialArea(RichInputText area) {
    serialArea = area;
  }

  public String getFullSerialString() {
    return fullSerialString;
  }

  public void setFullSerialString(String string) {
    fullSerialString = string;
  }

  public RichInputText getSerialArea() {
    if (ADFContext.getCurrent().getSessionScope().get("reviewSerial") != null) {
      fullSerialString = getReceiptAMService().getAllSerialsfromStows();
      return serialArea;
    }
    else {
      return serialArea;
    }
  }

  public void setInProgress(boolean inProgress) {
    this.inProgress = inProgress;
  }

  public boolean isInProgress() {
    return inProgress;
  }
}

