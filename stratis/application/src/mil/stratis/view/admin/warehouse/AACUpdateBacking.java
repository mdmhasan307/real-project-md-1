package mil.stratis.view.admin.warehouse;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.common.util.RegUtils;
import mil.stratis.common.util.Util;
import mil.stratis.model.services.WarehouseSetupImpl;
import mil.stratis.view.admin.AdminBackingHandler;
import mil.stratis.view.user.UserInfo;
import mil.stratis.view.util.ADFUtils;
import mil.stratis.view.util.ELUtils;
import mil.stratis.view.util.JSFUtils;
import mil.usmc.mls2.stratis.common.model.enumeration.AuditLogTypeEnum;
import mil.usmc.mls2.stratis.core.domain.event.domain.CustomerUpdateEvent;
import mil.usmc.mls2.stratis.core.domain.model.Customer;
import mil.usmc.mls2.stratis.core.service.EventService;
import mil.usmc.mls2.stratis.core.utility.AdfLogUtility;
import mil.usmc.mls2.stratis.core.utility.ContextUtils;
import mil.usmc.mls2.stratis.core.utility.GlobalConstants;
import oracle.adf.model.BindingContext;
import oracle.adf.model.binding.DCIteratorBinding;
import oracle.adf.view.rich.component.rich.RichPopup;
import oracle.adf.view.rich.component.rich.data.RichTable;
import oracle.adf.view.rich.component.rich.input.RichInputText;
import oracle.adf.view.rich.component.rich.input.RichSelectManyShuttle;
import oracle.adf.view.rich.component.rich.input.RichSelectOneChoice;
import oracle.adf.view.rich.context.AdfFacesContext;
import oracle.adf.view.rich.event.DialogEvent;
import oracle.binding.AttributeBinding;
import oracle.jbo.Row;
import oracle.jbo.uicli.binding.JUCtrlValueBindingRef;
import org.apache.myfaces.trinidad.context.RequestContext;

import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
public class AACUpdateBacking extends AdminBackingHandler {

  private static final int MAX_LEN_AAC_NAME = 6;
  private static final int MAX_LEN_STATE_NAME = 2;

  private transient RichSelectOneChoice route;
  private transient RichSelectOneChoice shippingRoute;
  private transient RichSelectOneChoice defaultFloor;
  private transient RichSelectOneChoice rtransShip;
  private transient RichInputText aac;
  private transient RichInputText city;
  private transient RichInputText state;
  private transient RichInputText zipCode;
  private transient RichInputText address1;
  private transient RichInputText address2;
  private transient RichTable aacTable;
  private transient RichSelectManyShuttle shuttleFLOORs;
  private transient RichSelectOneChoice restrictTransship;
  private transient RichInputText customerName;

  private List<String> selectedFLOORValues = new ArrayList<>();
  private List<SelectItem> allFLOORItems = new ArrayList<>();

  private boolean aacFlag = false;
  private boolean newFlag = false;

  private String defaultFloors = "";
  private int defaultFloorsCOLS = 3;

  private String aacIterator = "CustomerView1Iterator";

  public AACUpdateBacking() {
    //* required to show initial aac update screen with grey, read only and buttons undisabled
    RequestContext afContext = RequestContext.getCurrentInstance();
    if (!afContext.isPostback()) {
      setInitialAacFlag();
    }
  }

  public void dialogListener(DialogEvent dialogEvent) {
    if (dialogEvent.getOutcome().equals(DialogEvent.Outcome.ok)) {
      RichPopup popup;
      submitDeleteAac(null);
      popup = (RichPopup) JSFUtils.findComponentInRoot("confirmDelete");
      popup.hide();

      //Refresh accTable
      AdfFacesContext.getCurrentInstance().addPartialTarget(aacTable);
    }
  }

  @SuppressWarnings("unused") //called from .jspx
  public void submitResetAac(ActionEvent actionEvent) {
    resetFocusAac();
    resetKeepPosition(aacIterator);
    setAacFlag(false);
  }

  @SuppressWarnings("unused") //called from .jspx
  public void submitCancelAac(ActionEvent actionEvent) {
    resetFocusAac();
    cancel(aacIterator);
    setAacFlag(true);
    newFlag = false;
  }

  @SuppressWarnings("unused") //called from .jspx
  public void submitUpdateAac(ActionEvent event) {
    resetFocusAac();
    setAacFlag(false);
    newFlag = false;
  }

  @SuppressWarnings("unused") //called from .jspx
  public void submitSaveAac(ActionEvent event) {
    resetFocusAac();
    boolean error = false;

    val globalConstants = ContextUtils.getBean(GlobalConstants.class);
    //* handle required fields
    if (isEmpty(getAac())) {
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "AAC is required.");
      setFocus(getAac());
      error = true;
    }
    if (isEmpty(getCustomerName())) {
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "Customer Name is required.");
      setFocus(getCustomerName());
      error = true;
    }
    if (isEmpty(getAddress1())) {
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "Address 1 is required.");
      setFocus(getAddress1());
      error = true;
    }

    if (isEmpty(getCity())) {
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "City is required.");
      setFocus(getCity());
      error = true;
    }
    if (isEmpty(getState())) {
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "State is required.");
      setFocus(getState());
      error = true;
    }
    if (isEmpty(getZipCode())) {
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "Zip Code is required.");
      setFocus(getZipCode());
      error = true;
    }
    if (isEmpty(getRestrictTransship())) {
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "Restrict Transshipment is required.");
      setFocus(getRestrictTransship());
      error = true;
    }

    WarehouseSetupImpl service = getWarehouseSetupModule();

    if ((service.checkAAC(Util.trimUpperCaseClean(getAac().getValue())) > 0) && newFlag) {
      JSFUtils.addFacesErrorMessage("DUPLICATE FIELD", "AAC already exists.");
      setFocus(getAac());
      error = true;
    }

    if (error)
      return;

    //* handle case requirements
    String localAac = Util.trimUpperCaseClean(getAac().getValue());
    String name = Util.trimUpperCaseClean(getCustomerName().getValue());
    String localAddress1 = Util.trimUpperCaseClean(getAddress1().getValue());
    String localAddress2 = Util.trimUpperCaseClean(getAddress2().getValue());
    String localCity = Util.trimUpperCaseClean(getCity().getValue());
    String localState = Util.trimUpperCaseClean(getState().getValue());
    String zipcode = Util.trimUpperCaseClean(getZipCode().getValue());

    ELUtils.set("#{bindings.Aac1.inputValue}", localAac);
    ELUtils.set("#{bindings.CustomerName.inputValue}", name);
    ELUtils.set("#{bindings.Address1.inputValue}", localAddress1);
    ELUtils.set("#{bindings.Address2.inputValue}", localAddress2);
    ELUtils.set("#{bindings.City.inputValue}", localCity);
    ELUtils.set("#{bindings.State.inputValue}", localState);
    ELUtils.set("#{bindings.ZipCode.inputValue}", zipcode);

    Customer customer = Customer.builder()
        .aac(localAac)
        .name(name)
        .address1(localAddress1)
        .address2(localAddress2)
        .city(localCity)
        .state(localState)
        .zipCode(zipcode)
        .build();

    if (customer.getAac().length() != MAX_LEN_AAC_NAME) {
      JSFUtils.addFacesErrorMessage(globalConstants.getInvalidFieldTag(), "AAC must be " + MAX_LEN_AAC_NAME + " alphanumeric characters");
      setFocus(getAac());
      error = true;
    }
    else if (RegUtils.isNotAlphaNumeric(customer.getAac())) {
      JSFUtils.addFacesErrorMessage(globalConstants.getInvalidFieldTag(), "AAC must be alphanumeric characters only");
      setFocus(getAac());
      error = true;
    }

    if (RegUtils.isNotAlpha(customer.getCity())) {
      JSFUtils.addFacesErrorMessage(globalConstants.getInvalidFieldTag(), "City must be alpha characters only");
      setFocus(getCity());
      error = true;
    }
    if (customer.getState().length() != MAX_LEN_STATE_NAME) {
      JSFUtils.addFacesErrorMessage(globalConstants.getInvalidFieldTag(), "State must be " + MAX_LEN_STATE_NAME + " alpha characters");
      setFocus(getState());
      error = true;
    }
    else if (RegUtils.isNotAlpha(customer.getState())) {
      JSFUtils.addFacesErrorMessage(globalConstants.getInvalidFieldTag(), "State must be alpha characters only");
      setFocus(getState());
      error = true;
    }

    if (error)
      return;

    try {
      String iteratorName = aacIterator;
      String function = "Customer";
      String fields = "AAC";

      boolean donotEXECUTEQUERY = false;
      int customerId = getCustomerId();

      if (customerId == 0)
        donotEXECUTEQUERY = true;

      saveIteratorKeepPosition(iteratorName, function, fields, getAacTable(), donotEXECUTEQUERY);

      if (customerId != 0)
        auditLogCustomerUpdate(customer, AuditLogTypeEnum.SUCCESS);

      setAacFlag(true);
      newFlag = false;

      customerId = getCustomerId();

      service.addDefaultFloorLocations(selectedFLOORValues, customerId);

      log.trace("customer id: {}", customerId);
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      setAacFlag(false);
      newFlag = false;
      auditLogCustomerUpdate(customer, AuditLogTypeEnum.FAILURE);
    }
  }

  private void auditLogCustomerUpdate(Customer customer, AuditLogTypeEnum outcome) {
    val userInfo = (UserInfo) JSFUtils.getManagedBeanValue("userbean");
    CustomerUpdateEvent auditLogEvent = CustomerUpdateEvent.builder(outcome)
        .userInfo(userInfo)
        .customer(customer)
        .build();
    ContextUtils.getBean(EventService.class).publishEvent(auditLogEvent);
  }

  @SuppressWarnings("unused")
  public void submitDeleteAac(ActionEvent event) {

    resetFocusAac();
    String deleteKey = "";

    Object oldRowKey = aacTable.getRowKey();
    Iterator<Object> selection = aacTable.getSelectedRowKeys().iterator();
    try {
      while (selection.hasNext()) {
        Object rowKey = selection.next();
        aacTable.setRowKey(rowKey);
        JUCtrlValueBindingRef rowData = (JUCtrlValueBindingRef) aacTable.getRowData();
        Row r = rowData.getRow();
        val customerId = "CustomerId";
        if (r.getAttribute(customerId) != null)
          deleteKey = (r.getAttribute(customerId).toString());
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    finally {
      aacTable.setRowKey(oldRowKey);
    }

    if (deleteKey == null || deleteKey.equals("")) {
      displayMessage("[ERROR] Unknown Record for deletion.");
    }
    else {
      try {
        int id = Integer.parseInt(deleteKey);
        String message;
        WarehouseSetupImpl service = getWarehouseSetupModule();
        if (service == null) {
          displayMessage("[MODULE] Error occurred while trying to delete ");
          return;
        }
        message = service.deleteCustomer(id);
        if (message.equals("")) {

          DCIteratorBinding iter = ADFUtils.findIterator(aacIterator);
          iter.executeQuery();

          displaySuccessMessage("Customer deleted.");
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

  @SuppressWarnings("unused")
  public void refreshFLOORSelectedList(ValueChangeEvent event) {
    //NO-OP (called from front end)
  }

  public void resetFocusAac() {
    //NO-OP
  }

  private oracle.binding.BindingContainer getBindings() {
    return BindingContext.getCurrent().getCurrentBindingsEntry();
  }

  public final void setInitialAacFlag() {
    setAacFlag(true);
    newFlag = false;
  }

  public void setAacFlag(boolean show) {
    this.aacFlag = show;
  }

  public boolean getAacFlag() {
    return aacFlag;
  }

  public void setNewFlag(boolean show) {
    this.newFlag = show;
  }

  public boolean getNewFlag() {
    return newFlag;
  }

  public void setSelectedFLOORValues(List<String> selectedFLOORValues) {
    this.selectedFLOORValues = selectedFLOORValues;
  }

  public List<String> getSelectedFLOORValues() {
    // clear the list before use, then fill with list of String, not list of SelectItem

    if (selectedFLOORValues != null) selectedFLOORValues.clear();
    else selectedFLOORValues = new ArrayList<>();

    List<String> nameList = new ArrayList<>();
    List<String> idList = new ArrayList<>();

    int customerId = getCustomerId();

    WarehouseSetupImpl service = getWarehouseSetupModule();
    service.buildFloorLocationSelectedFilterList(nameList, idList, customerId);
    selectedFLOORValues.addAll(idList);

    return selectedFLOORValues;
  }

  public void setAllFLOORItems(List<SelectItem> allFLOORItems) {
    this.allFLOORItems = allFLOORItems;
  }

  public List<SelectItem> getAllFLOORItems() {
    // clear the list before use, then fill
    if (allFLOORItems != null) {
      allFLOORItems.clear();
    }
    else {
      allFLOORItems = new ArrayList<>();
    }

    List<String> nameList = new ArrayList<>();
    List<String> idList = new ArrayList<>();

    int customerId = getCustomerId();

    WarehouseSetupImpl service = getWarehouseSetupModule();
    service.buildFloorLocationFilterList(nameList, idList, customerId);

    for (int i = 0; i < nameList.size(); i++) {
      allFLOORItems.add(new SelectItem(idList.get(i), nameList.get(i)));
    }

    return allFLOORItems;
  }

  private int getCustomerId() {
    int customerId = 0;
    Object value;
    AttributeBinding attr = null;

    val alist = getBindings().getAttributeBindings();
    boolean found = false;
    for (Object o : alist) {
      attr = (AttributeBinding) o;
      if (attr != null) {
        String name = attr.getName();
        log.trace("Attr name: {}", name);
        if (name.compareTo("CustomerId") == 0) {
          found = true;
          break;
        }
      }
    }

    if (!found) {
      log.trace("Was unable to find the correct attribute: CustomerId");
    }
    else {
      value = attr.getInputValue();
      customerId = Util.cleanInt(value);
    }
    return customerId;
  }

  public void setDefaultFloors(String defaultFloors) {
    this.defaultFloors = defaultFloors;
  }

  public String getDefaultFloors() {
    List<String> nameList = new ArrayList<>();
    List<String> idList = new ArrayList<>();
    int customerId = getCustomerId();

    WarehouseSetupImpl service = getWarehouseSetupModule();
    service.buildFloorLocationSelectedFilterList(nameList, idList, customerId);
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

    //* set the size of the default floor locations text area box
    defaultFloorsCOLS = numRows;
    defaultFloors = sb.toString();

    return defaultFloors;
  }

  public void setDefaultFloorsCOLS(int defaultFloorsCOLS) {
    this.defaultFloorsCOLS = defaultFloorsCOLS;
  }

  public int getDefaultFloorsCOLS() {
    return defaultFloorsCOLS;
  }

  public void setRoute(RichSelectOneChoice route) {
    this.route = route;
  }

  public RichSelectOneChoice getRoute() {
    return route;
  }

  public void setShippingRoute(RichSelectOneChoice shippingRoute) {
    this.shippingRoute = shippingRoute;
  }

  public RichSelectOneChoice getShippingRoute() {
    return shippingRoute;
  }

  public void setDefaultFloor(RichSelectOneChoice defaultFloor) {
    this.defaultFloor = defaultFloor;
  }

  public RichSelectOneChoice getDefaultFloor() {
    return defaultFloor;
  }

  public void setRtransShip(RichSelectOneChoice rtransShip) {
    this.rtransShip = rtransShip;
  }

  @SuppressWarnings("unused") //called from .jspx
  public RichSelectOneChoice getRtransShip() {
    return rtransShip;
  }

  public void setAac(RichInputText aac) {
    this.aac = aac;
  }

  public RichInputText getAac() {
    return aac;
  }

  public void setCity(RichInputText city) {
    this.city = city;
  }

  public RichInputText getCity() {
    return city;
  }

  public void setState(RichInputText state) {
    this.state = state;
  }

  public RichInputText getState() {
    return state;
  }

  public void setZipCode(RichInputText zipCode) {
    this.zipCode = zipCode;
  }

  public RichInputText getZipCode() {
    return zipCode;
  }

  public void setAddress1(RichInputText address1) {
    this.address1 = address1;
  }

  public RichInputText getAddress1() {
    return address1;
  }

  public void setAddress2(RichInputText address2) {
    this.address2 = address2;
  }

  public RichInputText getAddress2() {
    return address2;
  }

  public void setAacTable(RichTable aacTable) {
    this.aacTable = aacTable;
  }

  public RichTable getAacTable() {
    return aacTable;
  }

  public void setShuttleFLOORs(RichSelectManyShuttle shuttleFLOORs) {
    this.shuttleFLOORs = shuttleFLOORs;
  }

  public RichSelectManyShuttle getShuttleFLOORs() {
    return shuttleFLOORs;
  }

  @SuppressWarnings("unused") //called from .jspx
  public void setRestrictTransship(RichSelectOneChoice restrictTransship) {
    this.restrictTransship = restrictTransship;
  }

  public RichSelectOneChoice getRestrictTransship() {
    return restrictTransship;
  }

  public void setCustomerName(RichInputText customerName) {
    this.customerName = customerName;
  }

  public RichInputText getCustomerName() {
    return customerName;
  }

  public boolean isAacFlag() {
    return getAacFlag();
  }

  public boolean isNewFlag() {
    return getNewFlag();
  }

  public void setAacIterator(String aacIterator) {
    this.aacIterator = aacIterator;
  }

  public String getAacIterator() {
    return aacIterator;
  }
}
