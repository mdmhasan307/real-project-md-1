package mil.stratis.view.admin;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.common.util.Util;
import mil.stratis.model.services.WarehouseSetupImpl;
import mil.stratis.view.user.UserInfo;
import mil.stratis.view.util.ADFUtils;
import mil.stratis.view.util.JSFUtils;
import mil.usmc.mls2.stratis.common.model.enumeration.AuditLogTypeEnum;
import mil.usmc.mls2.stratis.core.domain.event.UserGroupEvent;
import mil.usmc.mls2.stratis.core.domain.event.UserGroupPrivilegeEvent;
import mil.usmc.mls2.stratis.core.service.EventService;
import mil.usmc.mls2.stratis.core.utility.AdfLogUtility;
import mil.usmc.mls2.stratis.core.utility.ContextUtils;
import mil.usmc.mls2.stratis.core.utility.GlobalConstants;
import oracle.adf.model.binding.DCIteratorBinding;
import oracle.adf.view.rich.component.rich.RichPopup;
import oracle.adf.view.rich.component.rich.data.RichTable;
import oracle.adf.view.rich.component.rich.input.RichInputText;
import oracle.adf.view.rich.component.rich.input.RichSelectManyShuttle;
import oracle.adf.view.rich.component.rich.output.RichOutputText;
import oracle.adf.view.rich.context.AdfFacesContext;
import oracle.adf.view.rich.event.DialogEvent;
import oracle.jbo.Row;
import oracle.jbo.RowIterator;
import oracle.jbo.uicli.binding.JUCtrlValueBindingRef;
import org.apache.myfaces.trinidad.context.RequestContext;

import javax.faces.component.UISelectItems;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Slf4j
public class Admin_ModifyGroup extends AdminBackingHandler {

  public static final String GROUP_ID = "GroupId";
  public static final String GROUP_NAME = "GroupName";
  private static String groupIterator = "GroupViewAll1Iterator";

  private boolean groupFlag = true;
  private boolean isNew = false;
  private transient RichTable groupTable2;
  private transient RichInputText inputText1;
  private transient RichSelectManyShuttle selectManyShuttle1;
  private transient UISelectItems selectItems1;
  private List<SelectItem> allList = new ArrayList<>();
  private List<Integer> selectedList = new ArrayList<>();
  private int defaultGroupROWS = 3;
  private String defaultPrivs = "";
  private String oldGroupName = "";

  private transient RichOutputText userName;

  public Admin_ModifyGroup() {

    //* required to show initial aac update screen with grey, read only and buttons undisabled
    RequestContext afContext = RequestContext.getCurrentInstance();
    if (!afContext.isPostback()) {
      setGroupFlag(true);
      isNew = false;
      oldGroupName = "";
    }
  }

  public void setSelectItems1(UISelectItems selectItems1) {
    this.selectItems1 = selectItems1;
  }

  public UISelectItems getSelectItems1() {
    return selectItems1;
  }

  public void setAllList(List<SelectItem> allList) {
    this.allList = allList;
  }

  public List<SelectItem> getAllList() {
    allList.clear();
    // get all the priviledges
    Map<Integer, String> privMap;
    WarehouseSetupImpl service = getWarehouseSetupModule();
    privMap = service.getAllPrivs();

    for (Map.Entry<Integer, String> entry : privMap.entrySet()) {
      Integer i = entry.getKey();
      String str = entry.getValue();
      allList.add(new SelectItem(i, str));
    }
    return allList;
  }

  public void setSelectedList(List<Integer> selectedList) {
    this.selectedList = selectedList;
  }

  public List<Integer> getSelectedList() {

    // clear the list
    if (selectedList != null) {
      selectedList.clear();
    }
    else {
      selectedList = new ArrayList<>();
    }
    // get the rights of the given group
    Map<Integer, String> nameMap;
    long groupID;
    groupID = getGroupTableID();
    WarehouseSetupImpl service = getWarehouseSetupModule();
    nameMap = service.getGroupPrivs(groupID);

    for (Map.Entry<Integer, String> entry : nameMap.entrySet()) {
      Integer i = entry.getKey();
      selectedList.add(i);
    }

    return selectedList;
  }

  /**
   * This was pulled in from 703.00.00.03 to resolve a reference in the jspx.
   * Not sure who took it out or why it was missing.
   * Does it work?
   */
  @SuppressWarnings("unused")
  public void refreshSelectedList(ValueChangeEvent event) {
    // send the list to be update in the current row
  }

  //This function syncs the GroupTable with the iterator. Sometimes during creates, updates, and cancels, the iterator refreshes but the GroupTable selection doesn't
  public void syncGroupTable() {
    long groupID = -1;
    Object oldRowKey = getGroupTable2().getRowKey();
    Iterator<Object> selection = getGroupTable2().getSelectedRowKeys().iterator();
    try {

      while (selection.hasNext()) {
        Object rowKey = selection.next();
        groupTable2.setRowKey(rowKey);
        JUCtrlValueBindingRef rowData = (JUCtrlValueBindingRef) groupTable2.getRowData();
        Row r = rowData.getRow();
        groupID = Long.parseLong(r.getAttribute(GROUP_ID).toString());
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    finally {
      getGroupTable2().setRowKey(oldRowKey);
    }

    //grouptable has a selected id
    if (groupID != -1) {
      DCIteratorBinding iter = ADFUtils.findIterator(groupIterator);
      RowIterator ri = iter.getNavigatableRowIterator();
      Row r;
      boolean foundNew = false;
      r = ri.first();
      while (ri.hasNext() && !foundNew) {

        if (Integer.parseInt(r.getAttribute(GROUP_ID).toString()) == groupID) {
          foundNew = true;
        }
        else {
          r = ri.next();
        }
      }
    }
  }

  public long getGroupTableID() {
    long groupID = -1;
    try {
      DCIteratorBinding iter = ADFUtils.findIterator(groupIterator);
      Row r = iter.getCurrentRow();
      groupID = Integer.parseInt(r.getAttribute(GROUP_ID).toString());
    }
    catch (Exception e) {log.trace("Exception caught and ignored", e);}
    return groupID;
  }

  public String getGroupTableName() {
    String groupID = "";
    try {
      DCIteratorBinding iter = ADFUtils.findIterator(groupIterator);
      Row r = iter.getCurrentRow();
      groupID = r.getAttribute(GROUP_NAME).toString();
    }
    catch (Exception e) {log.trace("Exception caught and ignored", e);}
    return groupID;
  }

  @SuppressWarnings("unused")
  public void submitCreateGroup(ActionEvent event) {
    WarehouseSetupImpl service = getWarehouseSetupModule();
    int uid = Integer.parseInt(JSFUtils.getManagedBeanValue("userbean.userId").toString());
    service.createGroup(uid);
    service.dbCommit();
    DCIteratorBinding iter = ADFUtils.findIterator(groupIterator);
    iter.executeQuery();
    RowIterator ri = iter.getNavigatableRowIterator();
    Row r;
    boolean foundNew = false;
    r = ri.first();
    while (ri.hasNext() && !foundNew) {

      if (r.getAttribute(GROUP_NAME).toString().equals("TEMP")) {
        foundNew = true;
      }
      else {
        r = ri.next();
      }
    }

    if ((inputText1.getValue() != null) && (inputText1.getValue().toString().equals("TEMP"))) {
      inputText1.setValue("");
    }
    isNew = true;
    oldGroupName = "TEMP";
  }

  @SuppressWarnings("unused")
  public void submitUpdateGroup(ActionEvent event) {
    setGroupFlag(false);
    isNew = false;
    DCIteratorBinding iter = ADFUtils.findIterator(groupIterator);
    Row r = iter.getCurrentRow();
    oldGroupName = r.getAttribute(GROUP_NAME).toString();
  }

  @SuppressWarnings("unused")
  public void submitCancelGroup(ActionEvent event) {
    if (!isNew) cancel(groupIterator);
    else {
      //delete Temp Group if user decides to cancel during a create
      WarehouseSetupImpl service = getWarehouseSetupModule();
      int uid = Integer.parseInt(JSFUtils.getManagedBeanValue("userbean.userId").toString());
      long groupID = getGroupTableID();
      service.deleteGroup(groupID, uid);
    }
    isNew = false;
    setGroupFlag(true);
    DCIteratorBinding iter = ADFUtils.findIterator(groupIterator);
    iter.executeQuery();
    oldGroupName = "";
    syncGroupTable();
  }

  public void dialogListener(DialogEvent dialogEvent) {
    if (dialogEvent.getOutcome().equals(DialogEvent.Outcome.ok)) {
      RichPopup popup;
      popup = (RichPopup) JSFUtils.findComponentInRoot("confirmDelete");
      popup.hide();

      //Refresh accTable
      submitDeleteGroup(null);
      AdfFacesContext.getCurrentInstance().addPartialTarget(groupTable2);
    }
  }

  @SuppressWarnings("unused")
  public void submitDeleteGroup(ActionEvent event) {
    UserInfo userInfo = (UserInfo) JSFUtils.getManagedBeanValue("userbean");
    long groupID = getGroupTableID();
    String groupName = getGroupTableName();

    WarehouseSetupImpl service = getWarehouseSetupModule();
    if (service == null) {
      displayMessage("[MODULE] Error occurred while trying to delete ");
      //log delete error
      EventService eventPublisher = ContextUtils.getBean(EventService.class);
      UserGroupEvent groupEvent = UserGroupEvent.builder()
          .modifyingUser(userInfo)
          .group(groupName)
          .action(UserGroupEvent.Action.DELETE)
          .result(AuditLogTypeEnum.FAILURE)
          .build();
      eventPublisher.publishEvent(groupEvent);
      return;
    }
    String message = service.deleteGroup(groupID, userInfo.getUserId());
    DCIteratorBinding iter = ADFUtils.findIterator(groupIterator);
    iter.executeQuery();
    if (!message.equals("")) {
      displaySuccessMessage("Deletion prevented. Remove group from user account(s)");
      //log failure
      EventService eventPublisher = ContextUtils.getBean(EventService.class);
      UserGroupEvent groupEvent = UserGroupEvent.builder()
          .modifyingUser(userInfo)
          .group(groupName)
          .action(UserGroupEvent.Action.DELETE)
          .result(AuditLogTypeEnum.FAILURE)
          .build();
      eventPublisher.publishEvent(groupEvent);
    }
    else {
      displaySuccessMessage("Group Successfully Deleted");
      //log success
      EventService eventPublisher = ContextUtils.getBean(EventService.class);
      UserGroupEvent groupEvent = UserGroupEvent.builder()
          .modifyingUser(userInfo)
          .group(groupName)
          .action(UserGroupEvent.Action.DELETE)
          .result(AuditLogTypeEnum.SUCCESS)
          .build();
      eventPublisher.publishEvent(groupEvent);
    }
  }

  @SuppressWarnings("unused")
  public void submitSaveGroup(ActionEvent event) {
    val globalConstants = ContextUtils.getBean(GlobalConstants.class);

    try {
      if (isEmpty(getInputText1())) {
        JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "Group Name is required.");
        return;
      }

      if (selectedList == null) {
        selectedList = new ArrayList<>();
        JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "Group Privileges are required.");
        return;
      }
      else if (selectedList.isEmpty()) {
        JSFUtils.addFacesErrorMessage(globalConstants.getMissingFieldTag(), "Group Privileges are required.");
        return;
      }

      long groupID = 0;
      groupID = getGroupTableID();
      Integer theInt;
      ArrayList<Integer> addList = new ArrayList<>();
      String groupName = Util.trimUpperCaseClean(getInputText1().getValue());
      WarehouseSetupImpl service = getWarehouseSetupModule();
      UserInfo userInfo = (UserInfo) JSFUtils.getManagedBeanValue("userbean");
      Map<Integer, String> allPrivs = service.getAllPrivs();
      String message = "";
      if (!groupName.equalsIgnoreCase(oldGroupName)) {
        message = service.saveGroup(groupID, groupName, "", userInfo.getUserId());
        EventService eventPublisher = ContextUtils.getBean(EventService.class);
        UserGroupEvent groupEvent = UserGroupEvent.builder()
            .modifyingUser(userInfo)
            .group(groupName)
            .action(UserGroupEvent.Action.ADD)
            .result(AuditLogTypeEnum.SUCCESS)
            .build();
        eventPublisher.publishEvent(groupEvent);
      }
      if (message.isEmpty()) {
        Map<Integer, String> nameMap = service.getGroupPrivs(groupID);
        for (Integer integer : selectedList) {
          theInt = integer;

          if (nameMap.containsKey(theInt)) {
            nameMap.remove(theInt);
          }
          else {

            addList.add(theInt);
          }
        }

        for (Integer privID : addList) {
          service.saveGroupPriv(groupID, privID, userInfo.getUserId());
          //log privilege addition
          EventService eventPublisher = ContextUtils.getBean(EventService.class);
          UserGroupPrivilegeEvent groupPrivilegeEvent = UserGroupPrivilegeEvent.builder()
              .modifyingUser(userInfo)
              .group(groupName)
              .privilege(allPrivs.get(privID))
              .action(UserGroupPrivilegeEvent.Action.ADD)
              .result(AuditLogTypeEnum.SUCCESS)
              .build();
          eventPublisher.publishEvent(groupPrivilegeEvent);
        }

        for (Map.Entry<Integer, String> entry : nameMap.entrySet()) {
          Integer privID = entry.getKey();

          service.deleteGroupPriv(groupID, privID, userInfo.getUserId());
          //log privilage deletion
          EventService eventPublisher = ContextUtils.getBean(EventService.class);
          UserGroupPrivilegeEvent groupPrivilegeEvent = UserGroupPrivilegeEvent.builder()
              .modifyingUser(userInfo)
              .group(groupName)
              .privilege(entry.getValue())
              .action(UserGroupPrivilegeEvent.Action.DELETE)
              .result(AuditLogTypeEnum.SUCCESS)
              .build();
          eventPublisher.publishEvent(groupPrivilegeEvent);
        }
        service.dbCommit();

        setGroupFlag(true);
        isNew = false;
        oldGroupName = "";
        DCIteratorBinding iter = ADFUtils.findIterator(groupIterator);
        iter.executeQuery();
        syncGroupTable();
      }
      else {
        displayMessage("ERROR: " + message);
      }
    }
    catch (Exception e) {
      log.debug("an error occurred while saving group");
      AdfLogUtility.logException(e);
      displayMessage("ERROR: " + "An Error has occurred while saving group.");
      setGroupFlag(false);
    }
  }

  /**
   * Set group flag back on (true) to turn on readonly+style
   * and hide certain ui buttons on Group Management
   */
  public final void setGroupFlag(boolean show) {
    RequestContext afContext = RequestContext.getCurrentInstance();
    //* for use in the web, maintaining state
    afContext.getPageFlowScope().put("groupFlag", show);
    this.groupFlag = show;
  }

  public boolean getGroupFlag() {
    RequestContext afContext = RequestContext.getCurrentInstance();
    boolean flag = groupFlag;
    Object obj = afContext.getPageFlowScope().get("groupFlag");
    if (obj != null)
      flag = Boolean.parseBoolean(obj.toString());

    return groupFlag || flag;
  }

  public void setDefaultPrivs(String defaultPrivs) {
    this.defaultPrivs = defaultPrivs;
  }

  public String getDefaultPrivs() {
    // get the rights of the given group
    Map<Integer, String> nameMap;
    long groupID;
    groupID = getGroupTableID();
    WarehouseSetupImpl service = getWarehouseSetupModule();
    nameMap = service.getGroupPrivs(groupID);

    int i = 0;
    int numRows = 6;
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<Integer, String> entry : nameMap.entrySet()) {
      // do stuff
      if (i != 0)
        sb.append(", ");
      sb.append(entry.getValue());

      i++;

      if (i > 14 && i % 15 == 0) numRows++;
    }

    //* set the size of the default floor locations text area box
    defaultGroupROWS = numRows;
    defaultPrivs = sb.toString();

    return defaultPrivs;
  }

  public void setDefaultGroupROWS(int defaultGroupROWS) {
    this.defaultGroupROWS = defaultGroupROWS;
  }

  public int getDefaultGroupROWS() {
    return defaultGroupROWS;
  }

  public static void setGroupIterator(String groupIterator) {
    Admin_ModifyGroup.groupIterator = groupIterator;
  }

  public static String getGroupIterator() {
    return groupIterator;
  }

  public boolean isGroupFlag() {
    return groupFlag;
  }

  public void setGroupTable2(RichTable groupTable2) {
    this.groupTable2 = groupTable2;
  }

  public RichTable getGroupTable2() {
    return groupTable2;
  }

  public void setInputText1(RichInputText inputText1) {
    this.inputText1 = inputText1;
  }

  public RichInputText getInputText1() {
    return inputText1;
  }

  public void setSelectManyShuttle1(RichSelectManyShuttle selectManyShuttle1) {
    this.selectManyShuttle1 = selectManyShuttle1;
  }

  public RichSelectManyShuttle getSelectManyShuttle1() {
    return selectManyShuttle1;
  }

  public void setUserName(RichOutputText userName) {
    this.userName = userName;
  }

  public RichOutputText getUserName() {
    return userName;
  }
}

