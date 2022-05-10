package mil.stratis.view.admin;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.common.util.RegUtils;
import mil.stratis.common.util.Util;
import mil.stratis.model.services.AppModuleImpl;
import mil.stratis.model.services.SysAdminImpl;
import mil.stratis.view.user.UserInfo;
import mil.stratis.view.util.ADFUtils;
import mil.stratis.view.util.ELUtils;
import mil.stratis.view.util.JSFUtils;
import mil.usmc.mls2.stratis.common.model.enumeration.AuditLogTypeEnum;
import mil.usmc.mls2.stratis.core.domain.event.UserPermissionEvent;
import mil.usmc.mls2.stratis.core.service.EventService;
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
import oracle.jbo.Row;
import org.apache.myfaces.trinidad.context.RequestContext;

import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Slf4j
public class Admin_ModifyUser extends AdminBackingHandler {

  public static final String USER_ID = "userbean.userId";
  public static final String USERBEAN = "userbean";
  private transient RichSelectOneChoice userStatus;
  private transient RichInputText userFirstname;
  private transient RichInputText userCac;
  private transient RichTable adminUserMgmtTable;
  private transient RichSelectManyShuttle typeShuttle;
  private transient RichSelectManyShuttle groupShuttle;

  private transient RichInputText userUsername;
  private transient RichInputText userLastname;
  private transient RichInputText userMiddlename;

  private boolean userFlag = true;
  private boolean isNew = true;

  //Has Cleared variables ensure the usertype/usergroup selection boxes are cleared for user creation
  private boolean hasClearedSTV = false;
  private boolean hasClearedSV = false;
  private boolean hasClearedAI = false;
  private long oldID = -1;

  protected transient List<String> selectedValues = new ArrayList<>();
  protected transient List<SelectItem> allItems = new ArrayList<>();
  protected transient List<Integer> selectedTypeValues = new ArrayList<>();
  protected transient List<SelectItem> allTypeItems = new ArrayList<>();
  protected transient List<String> tempAllText = new ArrayList<>();
  protected transient List<Integer> tempAllValues = new ArrayList<>();
  private int defaultUserROWS = 3;
  private String defaultGroups = "";
  private int defaultTypeROWS = 3;
  private String defaultTypes = "";

  public Admin_ModifyUser() {

    //* required to show initial user update screen with grey, read only and buttons undisabled
    RequestContext afContext = RequestContext.getCurrentInstance();
    if (!afContext.isPostback()) {
      setUserFlag(true);
      isNew = false;
      clearAllClears();
    }
  }

  //Clears all the hasCleared variables.
  final void clearAllClears() {
    hasClearedSTV = false;
    hasClearedSV = false;
    hasClearedAI = false;
  }

  @SuppressWarnings("unused")
  public void submitCreateUser(ActionEvent event) {
    setUserFlag(false);
    isNew = true;
    clearAllClears();
    resetAllFields();
    clearAllFields();
  }

  @SuppressWarnings("unused")
  public void submitUpdateUser(ActionEvent event) {
    val globalConstants = ContextUtils.getBean(GlobalConstants.class);

    if (returnSelectedRow() != null) {
      long uid = Long.parseLong(JSFUtils.getManagedBeanValue(USER_ID).toString());
      long userID = getUserTableID();
      if (uid != userID) {
        setUserFlag(false);
        isNew = false;
        clearAllClears();
        resetAllFields();
        updateAllFields();
      }
      else {
        JSFUtils.addFacesErrorMessage(globalConstants.getInvalidSelectionTag(), "Cannot update current user account");
      }
    }
    else {
      JSFUtils.addFacesErrorMessage(globalConstants.getInvalidSelectionTag(), "Must Select a User to Update");
    }
  }

  public void clearAllFields() {
    userFirstname.setValue("");
    userLastname.setValue("");
    userCac.setValue("");
    userMiddlename.setValue("");
  }

  public void updateAllFields() {
    Row r = returnSelectedRow();

    if (r.getAttribute("FirstName") != null) {
      userFirstname.setValue(r.getAttribute("FirstName").toString());
    }
    else {
      userFirstname.setValue("");
    }
    if (r.getAttribute("LastName") != null) {
      userLastname.setValue(r.getAttribute("LastName").toString());
    }
    else {
      userLastname.setValue("");
    }
    if (r.getAttribute("CacNumber") != null) {
      userCac.setValue(r.getAttribute("CacNumber").toString());
    }
    else {
      userCac.setValue("");
    }
    if (r.getAttribute("MiddleName") != null) {
      userMiddlename.setValue(r.getAttribute("MiddleName").toString());
    }
    else {
      userMiddlename.setValue("");
    }
    if (r.getAttribute("Status") != null) {
      userStatus.setValue(r.getAttribute("Status").toString());
    }
  }

  public void resetAllFields() {
    userFirstname.resetValue();
    userLastname.resetValue();
    userCac.resetValue();
    userMiddlename.resetValue();
    groupShuttle.resetValue();
    typeShuttle.resetValue();
  }

  public void dialogListener(DialogEvent dialogEvent) {
    if (dialogEvent.getOutcome().equals(DialogEvent.Outcome.ok)) {
      RichPopup popup;
      popup = (RichPopup) JSFUtils.findComponentInRoot("confirmDelete");
      if (returnSelectedRow() != null) {
        submitDeleteUser(null);
        popup.hide();
        //Refresh accTable
        AdfFacesContext.getCurrentInstance().addPartialTarget(adminUserMgmtTable);
      }
      else {
        JSFUtils.addFacesErrorMessage("INVALID SELECTION", "Must Select a User to Delete");
        popup.hide();
      }
    }
  }

  @SuppressWarnings("unused")
  public void submitDeleteUser(ActionEvent event) {
    AppModuleImpl appModule = getAppModule();
    long userID = getUserTableID();
    long uid = Long.parseLong(JSFUtils.getManagedBeanValue(USER_ID).toString());
    if (uid != userID) {
      if (appModule.deleteUser(userID)) {
        appModule.dbCommit();
        DCIteratorBinding iter = ADFUtils.findIterator(userIterator);
        iter.executeQuery();
        displaySuccessMessage("User deleted.");

        //potentially clear user session
        try {
          SysAdminImpl service = getSysAdminModule();
          service.clearUserLogin(userID);
        }
        catch (Exception e) { log.trace("invalid error"); }
        finally {
          log.trace("session invalidated logged out user");
        }
      }
      else {
        displayMessage("ERROR: Can not delete user");
      }
    }
    else {
      displayMessage("ERROR: Can not delete current user account");
    }
  }

  @SuppressWarnings("unused")
  public void submitUpdateUserType(ActionEvent event) {
    //no-op
  }

  private static String userIterator = "CompleteUserList1Iterator";

  @SuppressWarnings("unused")
  public void submitSaveUser(ActionEvent event) {
    boolean error = isMissingRequired();
    val globalConstants = ContextUtils.getBean(GlobalConstants.class);
    if (error) return;

    String cac = Util.trimClean(getUserCac().getValue());

    if (RegUtils.isNotAlphaNumeric(cac)) {
      JSFUtils.addFacesErrorMessage(globalConstants.getInvalidFieldTag(), "CAC Information must be alphanumeric characters only.");
      error = true;
    }

    //* added 12/29/08 - scr to encrypt users.first_name
    String firstName = Util.trimUpperCaseClean(getUserFirstname().getValue());
    if (RegUtils.isNotAlphaNumeric(firstName)) {
      JSFUtils.addFacesErrorMessage(globalConstants.getInvalidFieldTag(), "First Name must be alphanumeric characters only.");
      error = true;
    }

    if (selectedTypeValues == null || selectedTypeValues.isEmpty()) {
      JSFUtils.addFacesErrorMessage(globalConstants.getInvalidEntryTag(), "A User Type must be selected.");
      error = true;
    }

    if (selectedValues == null || selectedValues.isEmpty()) {
      JSFUtils.addFacesErrorMessage(globalConstants.getInvalidEntryTag(), "A User Group must be selected.");
      error = true;
    }

    if (error) return;
    //check for double ADMN accounts
    Map<Integer, String> alltypesMap;
    AppModuleImpl service = getAppModule();
    alltypesMap = service.returnAllTypes();
    String typeName;
    boolean adminError = false;
    boolean hasAdmin = false;
    Integer theInt;
    boolean isFSR = false;

    for (Integer selectedTypeValue : selectedTypeValues) {
      theInt = selectedTypeValue;
      typeName = alltypesMap.get(theInt);
      if (typeName.equals("AUD") || typeName.equals("SUP") || typeName.equals("ADM")) {
        if (!hasAdmin) {
          hasAdmin = true;
        }
        else {
          adminError = true;
        }
      }
      else if (typeName.equals("FSR")) {
        isFSR = true;
      }
    }

    if (adminError && hasAdmin) {
      String message = "CAC number already associated to admin level account.";
      JSFUtils.addFacesErrorMessage("INVALID TYPE", message);
      error = true;
      logPermissionErrorMessage(message);
    }

    long userID;
    if (!isNew) userID = getUserTableID();
    else userID = -1;

    if (isFSR) {
      String fsrName = service.hasFSR();
      if (fsrName.length() > 0) {
        String[] split = fsrName.split(",");
        if (Integer.parseInt(split[0]) != userID) {
          error = true;
          JSFUtils.addFacesErrorMessage("INVALID TYPE", "FSR Account already assigned to " + split[1] + ".");
          String moddedUser = userID != -1 ? getUserUsername().getValue().toString() : "New User";
          logPermissionEvent("FSR", moddedUser, UserPermissionEvent.PermissionType.TYPE, UserPermissionEvent.Action.ADD, AuditLogTypeEnum.FAILURE);
        }
      }
    }

    if (error) return;

    //check if there are any groups (this is a check to see if anyone picked an group that had no corresponding user type, and skirts the there needs to be a user group selected.
    //See Redmine #23917 for more details.
    String entry;
    String[] split;
    int count = 0;
    for (Integer selectedTypeValue : selectedTypeValues) {
      theInt = selectedTypeValue;
      for (String selectedValue : selectedValues) {
        entry = selectedValue;
        split = entry.split(",");
        if (Integer.parseInt(split[0]) == theInt) {
          count++;
        }
      }
    }

    if (count == 0) {
      //no groups found that correspond to a user type
      JSFUtils.addFacesErrorMessage(globalConstants.getInvalidEntryTag(), "A valid User Group must be selected.");
      return;
    }

    //* set flag back on (true) to turn on readonly+style and hide certain ui buttons

    if (isNew) {
      createUser(cac, firstName);
    }
    else {
      updateUser(cac, firstName, userID);
    }
  }

  void createUser(String cac, String firstName) {
    String lastName = Util.trimUpperCaseClean(getUserLastname().getValue());
    String middleName = Util.trimUpperCaseClean(getUserMiddlename().getValue());
    AppModuleImpl service = getAppModule();
    UserInfo userInfo = (UserInfo) JSFUtils.getManagedBeanValue(USERBEAN);
    StringBuilder message = new StringBuilder();
    int result = service.createUser(firstName, lastName, middleName, cac, userStatus.getValue().toString(), userInfo.getUserId(), message);
    try {
      if (result > 0) {
        saveUserTypes(result);
        saveUserGroups(result);
        //* set flag back on (true) to turn on readonly+style and hide certain ui buttons
        setUserFlag(true);
        isNew = false;
        clearAllClears();
        displaySuccessMessage(message.toString());
        resetKeepPosition(userIterator);
      }
      else {
        //display error message
        displayMessage(message.toString());
        logPermissionErrorMessage(message.toString());
      }
    }
    catch (Exception e) {
      displayMessage("ERROR: " + result);
    }
  }

  void updateUser(String cac, String firstName, long userID) {
    try {

      String function = "User";
      String fields = "Username/CAC";
      //* handle trim requirements
      ELUtils.set("#{bindings.CacNumber.inputValue}", cac);
      //* added 12/05/08 - scr to encrypt users.first_name, updated 3/16/09
      ELUtils.set("#{bindings.FirstName.inputValue}", firstName);
      ELUtils.set("#{bindings.Status.inputValue}", userStatus.getValue().toString());
      ELUtils.set("#{bindings.LastName.inputValue}", Util.trimUpperCaseClean(getUserLastname().getValue()));
      ELUtils.set("#{bindings.MiddleName.inputValue}", Util.trimUpperCaseClean(getUserMiddlename().getValue()));
      ELUtils.set("#{bindings.LoggedIn.inputValue}", "N");
      ELUtils.set("#{bindings.LoggedInHh.inputValue}", "N");
      ELUtils.set("#{bindings.VisibleFlag.inputValue}", "Y");
      ELUtils.set("#{bindings.Locked.inputValue}", "N");
      //* have to change the password to unlock the account

      //* submit save to database
      saveIteratorKeepPosition(userIterator, function, fields, getAdminUserMgmtTable(), false);
      refreshIteratorKeepPosition(ADFUtils.findIterator(userIterator));
      saveUserTypes(userID);
      saveUserGroups(userID);
      //* set flag back on (true) to turn on readonly+style and hide certain ui buttons
      setUserFlag(true);
      isNew = false;
      clearAllClears();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  /**
   * Saves all user types
   */
  private void saveUserTypes(long uId) {
    AppModuleImpl service = getAppModule();
    Map<Integer, String> typeMap = service.returnSelectUserTypes(uId);
    Map<Integer, String> allTypesMap = service.returnAllTypes();
    ArrayList<Integer> addList = new ArrayList<>();
    Integer theInt;

    for (Integer selectedTypeValue : selectedTypeValues) {
      theInt = selectedTypeValue;
      if (typeMap.containsKey(theInt)) typeMap.remove(theInt);
      else addList.add(theInt);
    }

    String modedUser = Util.trimUpperCaseClean(getUserUsername().getValue());
    for (Integer integer : addList) {
      service.saveUserType(uId, integer);
      String typeName = allTypesMap.get(integer);
      logPermissionEvent(typeName, modedUser, UserPermissionEvent.PermissionType.TYPE, UserPermissionEvent.Action.ADD, AuditLogTypeEnum.SUCCESS);
    }

    for (Map.Entry<Integer, String> entry : typeMap.entrySet()) {
      Integer typeID = entry.getKey();
      String typeName = entry.getValue();
      service.deleteUserType(uId, typeID);
      logPermissionEvent(typeName, modedUser, UserPermissionEvent.PermissionType.TYPE, UserPermissionEvent.Action.DELETE, AuditLogTypeEnum.SUCCESS);
    }
    service.dbCommit();
  }

  /**
   * Saves all user groups
   */
  private void saveUserGroups(long uId) {

    Map<String, String> userGroups;
    AppModuleImpl service = getAppModule();
    ArrayList<String> addList = new ArrayList<>();
    ArrayList<String> deleteList = new ArrayList<>();
    Integer theInt;
    String entry;
    String[] split;
    int uid = Integer.parseInt(JSFUtils.getManagedBeanValue(USER_ID).toString());
    for (Integer selectedTypeValue : selectedTypeValues) {
      theInt = selectedTypeValue;
      Map<Integer, String> groupMap = service.returnAllGroups();
      Map<Integer, String> typeMap = service.returnSelectUserTypes(uId);
      userGroups = service.returnSelectUserGroups(uId, theInt);
      for (String selectedValue : selectedValues) {
        entry = selectedValue;
        split = entry.split(",");
        if (Integer.parseInt(split[0]) == theInt) {
          if (userGroups.containsKey(entry)) {
            userGroups.remove(entry);
          }
          else {
            String group = groupMap.get(Integer.valueOf(split[1]));
            String type = typeMap.get(Integer.valueOf(split[0]));

            addList.add(String.format("%s, %s (%s)", entry, group, type));
          }
        }
      }
      for (Map.Entry<String, String> entry2 : userGroups.entrySet()) {
        String groupID = entry2.getKey() + "," + entry2.getValue();
        deleteList.add(groupID);
      }
    }

    String modedUser = Util.trimUpperCaseClean(getUserUsername().getValue());
    for (String s : addList) {
      split = s.split(",");
      service.saveUserGroup(uId, Integer.parseInt(split[0]), Integer.parseInt(split[1]), uid);
      logPermissionEvent(split[2], modedUser, UserPermissionEvent.PermissionType.GROUP, UserPermissionEvent.Action.ADD, AuditLogTypeEnum.SUCCESS);
    }

    for (String s : deleteList) {
      split = s.split(",");
      service.deleteUserGroup(uId, Integer.parseInt(split[0]), Integer.parseInt(split[1]), uid);
      logPermissionEvent(split[2], modedUser, UserPermissionEvent.PermissionType.GROUP, UserPermissionEvent.Action.DELETE, AuditLogTypeEnum.SUCCESS);
    }
    service.dbCommit();
  }

  private void logPermissionEvent(String permission, String moddedUser, UserPermissionEvent.PermissionType type, UserPermissionEvent.Action action, AuditLogTypeEnum result) {
    UserInfo userInfo = (UserInfo) JSFUtils.getManagedBeanValue(USERBEAN);
    EventService eventPublisher = ContextUtils.getBean(EventService.class);
    UserPermissionEvent userEvent = UserPermissionEvent.builder()
        .modifyingUser(userInfo)
        .permission(permission)
        .modifiedUser(moddedUser)
        .type(type)
        .action(action)
        .result(result).build();
    eventPublisher.publishEvent(userEvent);
  }

  private void logPermissionErrorMessage(String message) {
    UserInfo userInfo = (UserInfo) JSFUtils.getManagedBeanValue(USERBEAN);
    EventService eventPublisher = ContextUtils.getBean(EventService.class);
    UserPermissionEvent userEvent = UserPermissionEvent.builder()
        .modifyingUser(userInfo)
        .message(message)
        .action(UserPermissionEvent.Action.ERROR)
        .result(AuditLogTypeEnum.FAILURE).build();
    eventPublisher.publishEvent(userEvent);
  }

  /**
   * Returns true if any required fields are missing
   */
  private boolean isMissingRequired() {
    boolean error = false;
    val globalConstants = ContextUtils.getBean(GlobalConstants.class);

    if (isEmpty(getUserFirstname())) {
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "Enter information in First Name");
      error = true;
    }

    if (isEmpty(getUserLastname())) {
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "Enter information in Last Name");
      error = true;
    }

    if (isEmpty(getUserStatus())) {
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "Enter information in Status");
      error = true;
    }

    if (isEmpty(getUserCac())) {
      JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "Enter information in CAC");
      error = true;
    }

    return error;
  }

  @SuppressWarnings("unused")
  public void submitCancelUser(ActionEvent event) {
    cancel(userIterator);
    setUserFlag(true);
    isNew = false;
    clearAllClears();
    oldID = -1;
  }

  int currentuserId = 0;

  public void setSelectedValues(List<String> selectedValues) {
    this.selectedValues = selectedValues;
  }

  public List<String> getSelectedValues() {
    if (!isNew) {
      long userID;
      userID = getUserTableID();
      Map<String, String> groupMap;
      AppModuleImpl service = getAppModule();

      if (selectedValues != null) selectedValues.clear();
      else selectedValues = new ArrayList<>();

      if (selectedTypeValues != null) {
        for (Integer typeID : selectedTypeValues) {
          groupMap = service.returnSelectUserGroups(userID, typeID);
          for (Map.Entry<String, String> entry : groupMap.entrySet()) {
            String key = entry.getKey();
            selectedValues.add(key);
          }
        }
      }
      else {
        selectedTypeValues = new ArrayList<>();
      }
    }
    else {
      if (!hasClearedSV) {
        if (selectedValues != null) selectedValues.clear();
        else selectedValues = new ArrayList<>();

        hasClearedSV = true;
      }
    }
    return selectedValues;
  }

  public void setAllItems(List<SelectItem> allItems) {
    this.allItems = allItems;
  }

  public List<SelectItem> getAllItems() {
    if (!isNew || !hasClearedAI) {

      Map<Integer, String> groupMap;
      Map<Integer, String> typeMap;

      if (allItems != null) allItems.clear();
      else allItems = new ArrayList<>();

      AppModuleImpl service = getAppModule();
      typeMap = service.returnAllTypes();
      groupMap = service.returnAllGroups();
      if (selectedTypeValues != null) {
        for (Integer selectedTypeValue : selectedTypeValues) {
          for (Map.Entry<Integer, String> entry : groupMap.entrySet()) {
            String typeName = typeMap.get(selectedTypeValue);
            Integer groupID = entry.getKey();
            String groupName = entry.getValue();
            String ids = selectedTypeValue + "," + groupID;
            String str = groupName + " (" + typeName + ")";
            allItems.add(new SelectItem(ids, str));
          }
        }
      }
      else {
        selectedTypeValues = new ArrayList<>();
      }
    }
    else {
      if (allItems != null) allItems.clear();
      else allItems = new ArrayList<>();

      hasClearedAI = true;
    }

    return allItems;
  }

  public void setSelectedTypeValues(List<Integer> selectedTypeValues) {
    this.selectedTypeValues = selectedTypeValues;
  }

  public List<Integer> getSelectedTypeValues() {
    if (!isNew) {
      long userID;
      userID = getUserTableID();

      if (oldID != userID) {
        oldID = userID;
        if (selectedTypeValues != null) selectedTypeValues.clear();
        else selectedTypeValues = new ArrayList<>();

        AppModuleImpl service = getAppModule();
        Map<Integer, String> typeMap = service.returnSelectUserTypes(userID);

        for (Map.Entry<Integer, String> entry : typeMap.entrySet()) {
          Integer i = entry.getKey();
          selectedTypeValues.add(i);
        }
      }
    }
    else {
      if (!hasClearedSTV) {
        if (selectedTypeValues != null) {
          selectedTypeValues.clear();
        }
        else {
          selectedTypeValues = new ArrayList<>();
        }
        hasClearedSTV = true;
      }
    }

    return selectedTypeValues;
  }

  public void setAllTypeItems(List<SelectItem> allTypeItems) {
    this.allTypeItems = allTypeItems;
  }

  public List<SelectItem> getAllTypeItems() {
    if (allTypeItems != null) allTypeItems.clear();
    else allTypeItems = new ArrayList<>();

    // get all the priviledges
    AppModuleImpl service = getAppModule();
    Map<Integer, String> typeMap = service.returnAllTypes();
    for (Map.Entry<Integer, String> entry : typeMap.entrySet()) {
      Integer i = entry.getKey();
      String str = entry.getValue();
      allTypeItems.add(new SelectItem(i, str));
    }
    return allTypeItems;
  }

  public void refreshSelectedList(ValueChangeEvent event) {
    if (event != null) {
      //NO-OP can't remove parameter due to adf
    }
  }

  /**
   * Set user flag back on (true) to turn on readonly+style
   * and hide certain ui buttons on User Management - User Update.
   */
  public final void setUserFlag(boolean show) {
    RequestContext afContext = RequestContext.getCurrentInstance();
    //* for use in the web, maintaining state
    afContext.getPageFlowScope().put("userFlag", show);
    this.userFlag = show;
  }

  public boolean getUserFlag() {
    RequestContext afContext = RequestContext.getCurrentInstance();
    boolean flag = userFlag;
    Object obj = afContext.getPageFlowScope().get("userFlag");
    if (obj != null)
      flag = Boolean.parseBoolean(obj.toString());

    return userFlag || flag;
  }

  private String userReadOnlyStyle = "ReadOnlyField";

  public String getUserReadOnlyStyle() {
    userReadOnlyStyle = getUserFlag() ? "ReadOnlyField" : "";
    return userReadOnlyStyle;
  }

  public void setUserReadOnlyStyle(String param) {
    this.userReadOnlyStyle = param;
  }

  public void setDefaultGroups(String defaultGroups) {
    this.defaultGroups = defaultGroups;
  }

  public String getDefaultGroups() {
    // get the rights of the given group
    AppModuleImpl service = getAppModule();

    long userID = 0;
    userID = getUserTableID();

    Map<String, String> groupMap = service.returnSelectUserGroups(userID);

    int i = 0;
    int numRows = 6;
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<String, String> entry : groupMap.entrySet()) {
      if (i != 0) sb.append(", ");
      sb.append(entry.getValue());

      i++;

      if (i > 14 && i % 15 == 0) numRows++;
    }

    //* set the size of the default groups text area box
    defaultUserROWS = numRows;
    defaultGroups = sb.toString();

    return defaultGroups;
  }

  public void setDefaultTypes(String defaultTypes) {
    this.defaultTypes = defaultTypes;
  }

  public String getDefaultTypes() {
    // get the rights of the given group
    AppModuleImpl service = getAppModule();

    long userID = 0;
    userID = getUserTableID();

    Map<Integer, String> typeMap = service.returnSelectUserTypes(userID);

    int i = 0;
    int numRows = 3;
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<Integer, String> entry : typeMap.entrySet()) {
      if (i != 0) sb.append(", ");
      sb.append(entry.getValue());
      i++;
    }

    //* set the size of the default groups text area box
    defaultTypeROWS = numRows;
    defaultTypes = sb.toString();

    return defaultTypes;
  }

  Row returnSelectedRow() {
    Row r = null;
    if (adminUserMgmtTable != null) {
      Iterator<Object> selection = adminUserMgmtTable.getSelectedRowKeys().iterator();
      try {
        while (selection.hasNext()) {
          Object rowKey = selection.next();
          r = (Row) adminUserMgmtTable.getRowData(rowKey);
        }
      }
      catch (Exception e) {
        log.trace(e.getLocalizedMessage());
      }
    }
    return r;
  }

  long getUserTableID() {
    long userID;

    Row r = returnSelectedRow();
    if (r != null) {
      try {
        userID = Integer.parseInt(r.getAttribute("UserId").toString());
      }
      catch (Exception e) {
        userID = 0;
      }
    }
    else {
      userID = 0;
    }
    return userID;
  }

  public void setDefaultUserROWS(int defaultUserROWS) {
    this.defaultUserROWS = defaultUserROWS;
  }

  public int getDefaultUserROWS() {
    return defaultUserROWS;
  }

  public void setDefaultTypeROWS(int defaultTypeROWS) {
    this.defaultTypeROWS = defaultTypeROWS;
  }

  public int getDefaultTypeROWS() {
    return defaultTypeROWS;
  }

  public void setUserStatus(RichSelectOneChoice userStatus) {
    this.userStatus = userStatus;
  }

  public RichSelectOneChoice getUserStatus() {
    return userStatus;
  }

  public void setUserFirstname(RichInputText userFirstname) {
    this.userFirstname = userFirstname;
  }

  public RichInputText getUserFirstname() {
    return userFirstname;
  }

  public void setUserCac(RichInputText userCac) {
    this.userCac = userCac;
  }

  public RichInputText getUserCac() {
    return userCac;
  }

  public void setAdminUserMgmtTable(RichTable adminUserMgmtTable) {
    this.adminUserMgmtTable = adminUserMgmtTable;
  }

  public RichTable getAdminUserMgmtTable() {
    return adminUserMgmtTable;
  }

  public boolean isUserFlag() {
    return userFlag;
  }

  public boolean isNewFlag() {
    return isNew;
  }

  public void setOldID(long oldID) {
    this.oldID = oldID;
  }

  public long getOldID() {
    return oldID;
  }

  public void setTempAllText(List<String> TempAllText) {
    this.tempAllText = TempAllText;
  }

  public List<String> getTempAllText() {
    return tempAllText;
  }

  public void setTempAllValues(List<Integer> TempAllValues) {
    this.tempAllValues = TempAllValues;
  }

  public List<Integer> getTempAllValues() {
    return tempAllValues;
  }

  public static void setUserIterator(String userIterator) {
    Admin_ModifyUser.userIterator = userIterator;
  }

  public static String getUserIterator() {
    return userIterator;
  }

  public void setCurrentuserid(int currentuserId) {
    this.currentuserId = currentuserId;
  }

  public int getCurrentuserid() {
    return currentuserId;
  }

  public void setUserUsername(RichInputText userUsername) {
    this.userUsername = userUsername;
  }

  public RichInputText getUserUsername() {
    return userUsername;
  }

  public void setUserLastname(RichInputText userLastname) {
    this.userLastname = userLastname;
  }

  public RichInputText getUserLastname() {
    return userLastname;
  }

  public void setUserMiddlename(RichInputText userMiddlename) {
    this.userMiddlename = userMiddlename;
  }

  public RichInputText getUserMiddlename() {
    return userMiddlename;
  }

  public void setGroupShuttle(RichSelectManyShuttle shuttlein) {
    groupShuttle = shuttlein;
  }

  public RichSelectManyShuttle getGroupShuttle() {
    return groupShuttle;
  }

  public void setTypeShuttle(RichSelectManyShuttle shuttlein) {
    this.typeShuttle = shuttlein;
  }

  public RichSelectManyShuttle getTypeShuttle() {
    return typeShuttle;
  }
}
