package mil.stratis.view.user;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.common.util.Util;
import mil.stratis.model.datatype.user.UserRights;
import mil.stratis.model.services.LoginModuleImpl;
import mil.stratis.view.session.MdssBackingBean;
import mil.stratis.view.util.ADFUtils;
import mil.stratis.view.util.JSFUtils;
import mil.usmc.mls2.stratis.common.domain.exception.ValidationException;
import mil.usmc.mls2.stratis.common.domain.model.SortOrder;
import mil.usmc.mls2.stratis.core.domain.event.UserLoggedInEvent;
import mil.usmc.mls2.stratis.core.domain.event.UserLoggedOutEvent;
import mil.usmc.mls2.stratis.core.domain.event.UserLoginFailedEvent;
import mil.usmc.mls2.stratis.core.domain.model.EquipmentSearchCriteria;
import mil.usmc.mls2.stratis.core.manager.UserSessionManager;
import mil.usmc.mls2.stratis.core.service.EquipmentService;
import mil.usmc.mls2.stratis.core.service.EventService;
import mil.usmc.mls2.stratis.core.service.UserTypeService;
import mil.usmc.mls2.stratis.core.utility.AdfLogUtility;
import mil.usmc.mls2.stratis.core.utility.CertificateManager;
import mil.usmc.mls2.stratis.core.utility.ContextUtils;
import mil.usmc.mls2.stratis.core.utility.GlobalConstants;
import oracle.adf.model.binding.DCBindingContainer;
import oracle.adf.model.binding.DCIteratorBinding;
import oracle.adf.view.rich.component.rich.input.RichSelectOneChoice;
import org.apache.myfaces.trinidad.component.core.input.CoreInputText;
import org.apache.myfaces.trinidad.component.core.nav.CoreCommandButton;
import org.apache.myfaces.trinidad.context.RequestContext;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@NoArgsConstructor
public class UserBacking extends MdssBackingBean {

  private List equipLoginSelectedValues = new ArrayList();
  private List equipLoginAllValues = new ArrayList();
  private List allUserTypes = new ArrayList();
  private String userstring = "";
  private String usertypestring = "";

  private boolean loginFailed = false;

  // the user profile backing objects
  private transient CoreInputText userprofilefirstname;
  private transient CoreInputText userprofilemiddlename;
  private transient CoreInputText userprofilelastname;
  private transient CoreCommandButton userprofileupdatebutton;

  private boolean validateUsingCac = true;

  private int selectedUserId = 0;
  private transient RichSelectOneChoice userTypeSelect;
  private transient RichSelectOneChoice userWorkstationSelect;
  private transient RichSelectOneChoice userIdSelect;

  // A property to toggle user login forcing a logout
  private int loginStatus = 0;

  private String loginStatusMessage = "";

  // Injected user info bean

  private UserInfo userInfo;
  private transient RichSelectOneChoice homePageSelect;

  private String defaultHomePage = "desktop";
  
  public void confirmCACLogin(ActionEvent event) {
    final ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
    HttpServletRequest req =
        (HttpServletRequest) externalContext.getRequest();
    String edipi = null;
    int userId = 0;
    int userTypeId = 0;
    int workStationId = 0;

    String txtUsername = "";

    ArrayList<String> idArray;

    Object obj;
    // get the username and workstation from the login page
    LoginModuleImpl service;
    val equipmentService = ContextUtils.getBean(EquipmentService.class);

    service = getLoginModule();

    // int to hold the user id if we get a proper login

    if (userTypeSelect != null) {
      obj = userTypeSelect.getValue();
      if (obj != null) {
        userTypeId = Integer.parseInt(obj.toString());
        SelectItem s;
        for (int i = 0; i < allUserTypes.size(); i++) {
          s = (SelectItem) allUserTypes.get(i);
          if (s.getValue().equals(obj)) {
            usertypestring = s.getLabel();
          }
        }
      }
    }

    if (userWorkstationSelect != null) {
      obj = userWorkstationSelect.getValue();
      if (obj != null) {
        workStationId = Integer.parseInt(obj.toString());
      }
    }

    if (userIdSelect != null) {
      obj = userIdSelect.getValue();
      if (obj != null) {
        userId = Integer.parseInt(obj.toString());
      }
    }

    if (homePageSelect != null) {
      obj = homePageSelect.getValue();
      if (obj != null) {
        defaultHomePage = obj.toString();
      }
    }

    loginStatusMessage = "";

    if (userTypeId == 0) {
      loginStatusMessage = "ERROR: You must choose a role.";
      return;
    }

    if (workStationId == 0) {
      loginStatusMessage = "ERROR: You must choose a workstation.";
      return;
    }

    if (!validateUsingCac && selectedUserId == 0) {
      loginStatusMessage = "ERROR: You must choose a user.";
      return;
    }

    try {
      val certUtils = ContextUtils.getBean(CertificateManager.class);
      val certInformation = certUtils.getCertInformationFromRequest(req);
      if (certInformation.isPresent()) {
        edipi = certInformation.get().getEdipi();
        log.trace("idnum: {}", edipi);
        if ((edipi.length() > 0) && (userWorkstationSelect.getValue() != null) &&
            (userTypeSelect.getValue() != null)) {
          userId = service.getUserLoginIdCAC(edipi, "");
          if (userId > 0) {
            userId = service.checkUserLoginReturn(userId, userTypeId);
          }
          if (userId > 0) {
            // get the user bean
            txtUsername = service.getCacUsername();

            val user = (UserInfo) JSFUtils.getManagedBeanValue("userbean");

            user.setUsername(txtUsername);
            user.setDisplayName(service.getDisplayName());
            user.setLoginKey(service.getLoginKey());
            user.setUserTypeId(userTypeId);
            user.setUsertypestring(usertypestring);
            Object workstation = userWorkstationSelect.getValue();

            // If we get here we have a valid user and workstation id.

            user.setWorkstationId(workStationId);

            val newWorkstation = equipmentService.setWorkstation(userId, workStationId);

            user.setComCommandIndex("1");
            user.setComPrintIndex("2");
            user.setWorkstationType(newWorkstation.getDescription());
            user.setWorkstationName(newWorkstation.getName());
            user.setUserRights(new UserRights(service.getLoginUserRights(), user.getWorkstationType()));

            user.setUserId(userId);

            try {
              user.setUrl("https://" + externalContext.getRequestHeaderMap().get("Host") + "" + externalContext.getRequestContextPath());
            }
            catch (Exception e) {
              log.warn("Failed to automatically determine url using ADF context ", e);
            }

            setUserInfo(user);

            userInfo = user;

            // publish event
            val userLoggedInEvent = UserLoggedInEvent.builder()
                .userInfo(userInfo)
                .build();

            val eventPublisher = ContextUtils.getBean(EventService.class);
            eventPublisher.publishEvent(userLoggedInEvent);

            UserSessionManager.addUserSession(userInfo.getUserId(), req.getSession().getId());

            // clear all the bindings for this function
            userWorkstationSelect.setValue(null);

            // say they passed the login
            loginFailed = false;

            //MLS2STRAT-223 - set footerbean.footerMainSwitcher to 1 to indicate a good user session that does not need kicked.
            JSFUtils.setManagedBeanValue("footerbean.footerMainSwitcher", 1);

            // check if this is a non mech workstation
            String mechanizedFlag = null;
            if (newWorkstation.getWac() != null) {
              mechanizedFlag = newWorkstation.getWac().getMechanizedFlag();
            }

            // update the session bean
            Long modifyrights = service.getLoginUserRights();

            // flip the bits over
            modifyrights = ~modifyrights;

            equipmentService.checkAndSetNonMech(mechanizedFlag, modifyrights, user);
            JSFUtils.setManagedBeanValue("userbean", user);

            val globalConstants = ContextUtils.getBean(GlobalConstants.class);
            val sessionTimout = userInfo.isAdmin()
                ? globalConstants.getAdminSessionTimeout()
                : globalConstants.getUserSessionTimeout();
            req.getSession().setMaxInactiveInterval(sessionTimout * 60);
          }
          else {
            if (userId == -2) {
              log.error("User id has expired");
              loginFailed = true;
              loginStatusMessage = "Login failed - user account expired.";  // Must be set so action redirect will work.

              // publish event
              val userLoginFailedEvent = UserLoginFailedEvent.builder()
                  .userInfo(userInfo)
                  .message(loginStatusMessage)
                  .build();

              val eventPublisher = ContextUtils.getBean(EventService.class);
              eventPublisher.publishEvent(userLoginFailedEvent);
            }
            else {

              // tell them they failed the login
              // userId of zero means something went wrong during the process.
              log.error("Invalid user id:  {}", userId);

              loginFailed = true;
              loginStatusMessage = "Login failed.";  // Must be set so action redirect will work.

              // publish event
              val userLoginFailedEvent = UserLoginFailedEvent.builder()
                  .userInfo(userInfo)
                  .message(loginStatusMessage)
                  .build();

              val eventPublisher = ContextUtils.getBean(EventService.class);
              eventPublisher.publishEvent(userLoginFailedEvent);
            }
            // clear the values
            userWorkstationSelect.setValue(null);
            userTypeSelect.setValue(null);
          }
        }
      }
    }
    catch (ValidationException e) {
      loginStatusMessage = "ERROR: No PIV Authentication Certificate was found.";
      log.info("No valid PIV Auth cert found.");
      return;
    }
  }

  public void switchWorkstation(ActionEvent event) {
    loginStatusMessage = "";

    try {
      int workstationId = -1;
      if (userWorkstationSelect != null) {
        Object obj = userWorkstationSelect.getValue();
        if (obj != null) {
          workstationId = Integer.parseInt(obj.toString());
        }
      }

      if (workstationId == -1) {
        loginStatusMessage = "ERROR: You must choose a workstation.";
        return;
      }

      val userbean = (UserInfo) JSFUtils.getManagedBeanValue("userbean");
      userbean.setWorkstationId(workstationId);
      val equipmentService = ContextUtils.getBean(EquipmentService.class);
      val userRights = equipmentService.getNewUserRights(userbean.getUserId(), userbean.getUserTypeId());
      equipmentService.switchWorkstation(userbean, userRights);

      JSFUtils.setManagedBeanValue("userbean", userbean);
    }
    catch (Exception e) {
      log.error(e.getMessage());
    }
  }

  /*
   */
  public String goSwitchWorkstation() {

    //    ApplicationScope
    // get the current id
    Object currentidobj = JSFUtils.getManagedBeanValue("userbean.userId");
    Object currUser = JSFUtils.getManagedBeanValue("userbean.username");
    usertypestring = (String) JSFUtils.getManagedBeanValue("userbean.usertypestring");
    //default the "default view" to be desktop on the switch workstation page.
    defaultHomePage = "desktop";
    if (currentidobj != null) {
      // open the service and update this users record
      LoginModuleImpl service = getLoginModule();

      if (service.getDBTransaction().isDirty()) {
        log.trace("[UserBacking] dirty transaction being rolled back");
        service.getDBTransaction().rollback();
      }
      service.clearUserWorkstation(Integer.parseInt(currentidobj.toString()));
    }

    // clear the user bean values
    JSFUtils.setManagedBeanValue("userbean.workstationId", -1);
    JSFUtils.setManagedBeanValue("userbean.selected", -1);
    JSFUtils.setManagedBeanValue("userbean.selected2", -1);

    if (userWorkstationSelect != null)
      userWorkstationSelect.setValue(null);

    return "GoWorkstation";
  }

  public void clearLogin(ActionEvent event) {
    // call the clear login function
    clearUserLogin(true);
    log.trace("clearing user login");
  }

  public void clearLogin() {
    // call the clear login function
    clearUserLogin(true);
    log.trace("clearing user login");
  }

  // function to clear the current login

  public void clearUserLogin(boolean doInvalidateSession) {
    // get the current id and username
    Object currentidobj = JSFUtils.getManagedBeanValue("userbean.userId");
    String currentnameobj = (String) JSFUtils.getManagedBeanValue("userbean.username");
    if (currentidobj != null) {
      // open the service and update this users record
      LoginModuleImpl service = getLoginModule();

      if (service.getDBTransaction().isDirty()) {
        log.trace("[UserBacking] dirty transaction being rolled back");
        service.getDBTransaction().rollback();
      }
      val curObjInt = Integer.parseInt(currentidobj.toString());
      service.clearLoggedInUser(curObjInt);
      service.clearUserWorkstation(curObjInt);

      // publish event
      val event = UserLoggedOutEvent.builder()
          .userInfo(userInfo)
          .build();

      val eventPublisher = ContextUtils.getBean(EventService.class);
      eventPublisher.publishEvent(event);
    }
    // clear the user bean values
    JSFUtils.setManagedBeanValue("userbean.username", "");
    JSFUtils.setManagedBeanValue("userbean.displayName", "");
    JSFUtils.setManagedBeanValue("userbean.userRights", null);
    JSFUtils.setManagedBeanValue("userbean.workstationId", -1);
    JSFUtils.setManagedBeanValue("userbean.userId", -1);
    JSFUtils.setManagedBeanValue("userbean.selected", -1);
    JSFUtils.setManagedBeanValue("userbean.selected2", -1);
    JSFUtils.setManagedBeanValue("userbean.loginKey", "");
    JSFUtils.setManagedBeanValue("userbean.workstationType", "");
    JSFUtils.setManagedBeanValue("userbean.workstationName", "");
    JSFUtils.setManagedBeanValue("userbean.helpURL", "");

    if (userWorkstationSelect != null) userWorkstationSelect.setValue(null);
    if (userTypeSelect != null) userTypeSelect.setValue(null);

    selectedUserId = 0;

    if (doInvalidateSession) {
      try {
        LoginModuleImpl service = getLoginModule();

        //* check if any interfaces are currently running before killing the session
        FacesContext fc = FacesContext.getCurrentInstance();
        ExternalContext ectx = fc.getExternalContext();
        HttpServletResponse response = (HttpServletResponse) ectx.getResponse();

        HttpSession session = (HttpSession) ectx.getSession(false);

        if (service.isAnythingRunning()) {
          ectx.redirect("Close.html");
          return;
        }

        log.trace("clearing user login");

        //If your application calls the invalidate() method on the HTTP Session to terminate the current session at logoff time,
        //you must use a "Redirect" to navigate back to a home page to require accessing an ADF Model binding container.
        //The redirect to a databound page ensures that the ADF Binding Context gets created again after invalidating the HTTP Session.
        if (session != null) {
          //Note this will throw an exception if the session is already invalidated.  So always set the respose to close.html.
          try {
            UserSessionManager.removeSessionFromUser(userInfo.getUserId(), session.getId());
            try {
              session.invalidate();
            }
            catch (IllegalStateException e) {
              // do nothing ADF has a session invalidate bug when running in tomcat, which throws a session already invalidated IllegalStateException
            }
          }
          finally {
            log.trace("session invalidated");
            ectx.redirect("Close.html");
          }
        }
      }
      catch (Exception ioe) {
        log.error("Error in {}", ioe.getStackTrace()[0].getMethodName(), ioe);
      }
    }
  }

  public String getUserstring() {
    int userID = -1;
    ArrayList<String> idArray;
    try {
      LoginModuleImpl service = getLoginModule();
      HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
      if (request != null) {

        try {
          val certUtils = ContextUtils.getBean(CertificateManager.class);
          val certInformation = certUtils.getCertInformationFromRequest(request);
          if (certInformation.isPresent()) {
            val edipi = certInformation.get().getEdipi();
            log.trace("idnum: {}", edipi);
            if (edipi.length() > 0) {
              userID = service.getUserLoginIdCAC(edipi, "");
              if (userID > 0) {
                userstring = service.getCacUsername();
              }
              else {
                userstring = "NO USER";
              }
            }
            else {
              userstring = "CERT Not Found";
            }
          }
          else {
            userstring = "NO PIV Auth Cert Found";
            log.debug("No valid PIV Auth cert found.");
          }
        }
        catch (ValidationException e) {
          userstring = "NO PIV Auth Cert Found";
          log.debug("No valid PIV Auth cert found.");
        }
      }
      else {
        userstring = "Invalid request";
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return userstring;
  }

  public void setUserstring(String userstring) {
    this.userstring = userstring;
  }

  public String getUsertypestring() {
    return usertypestring;
  }

  public void setUsertypestring(String userstring) {
    this.usertypestring = userstring;
  }

  public void setEquipLoginSelectedValues(List values) {
    equipLoginSelectedValues = values;
  }

  public List getEquipLoginSelectedValues() {
    return equipLoginSelectedValues;
  }

  public void setEquipLoginAllValues(List values) {
    equipLoginAllValues = values;
  }

  public List getEquipLoginAllValues() {
    try {
      // clear the list and then fill it with all the equipment
      equipLoginAllValues.clear();

      val equipmentService = ContextUtils.getBean(EquipmentService.class);
      EquipmentSearchCriteria criteria = EquipmentSearchCriteria.builder()
          .includeDummy(false).build();
      criteria.setSort("name", SortOrder.ASC);
      val workstations = equipmentService.search(criteria);

      workstations.forEach(x -> {
        equipLoginAllValues.add(new SelectItem(x.getEquipmentNumber(),
            x.getName()));
      });
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      equipLoginAllValues.add(new SelectItem(1, "WorkStation Error"));
    }
    return equipLoginAllValues;
  }

  public void setUserprofilefirstname(CoreInputText userprofilefirstname) {
    this.userprofilefirstname = userprofilefirstname;
  }

  public CoreInputText getUserprofilefirstname() {
    return userprofilefirstname;
  }

  public void setUserprofilemiddlename(CoreInputText userprofilemiddlename) {
    this.userprofilemiddlename = userprofilemiddlename;
  }

  public CoreInputText getUserprofilemiddlename() {
    return userprofilemiddlename;
  }

  public void setUserprofilelastname(CoreInputText userprofilelastname) {
    this.userprofilelastname = userprofilelastname;
  }

  public CoreInputText getUserprofilelastname() {
    return userprofilelastname;
  }

  public void setUserprofileupdatebutton(CoreCommandButton userprofileupdatebutton) {
    this.userprofileupdatebutton = userprofileupdatebutton;
  }

  public CoreCommandButton getUserprofileupdatebutton() {
    return userprofileupdatebutton;
  }

  public void setAllUserTypes(List types) {
    allUserTypes = types;
  }

  public List getAllUserTypes() {

    HashMap<Integer, String> userTypesMap;

    //EXPEDITED LOGIN, REMOVE.
    allUserTypes.clear();

    val userTypeService = ContextUtils.getBean(UserTypeService.class);
    val userTypes = userTypeService.getAll();
    userTypes.forEach(x -> {
      allUserTypes.add(new SelectItem(x.getUserTypeId(), x.getType()));
    });

    return allUserTypes;
  }

  // function to save user profile info, without the password

  public void updateUserProfile(ActionEvent event) {
    userprofileupdatebutton.setDisabled(true);
    boolean error = false;

    if (Util.isEmpty(userprofilelastname.getValue())) {
      JSFUtils.addFacesErrorMessage("REQUIRED FIELDS", "Last Name is required.");
      error = true;
    }
    else {
      //* validation
    }

    if (error) {
      userprofileupdatebutton.setDisabled(false);
      return;
    }

    try {
      DCBindingContainer dcbinding = ADFUtils.getDCBindingContainer();
      DCIteratorBinding userIterator = (DCIteratorBinding) dcbinding.get("LoginUserView1Iterator");
      String updateUserId = Util.cleanString(userIterator.getCurrentRow().getAttribute("UserId"));
      if (!Util.isEmpty(updateUserId)) {

        getLoginModule().updateUserProfile(Util.trimUpperCaseClean(userprofilelastname.getValue()), Util.trimUpperCaseClean(userprofilemiddlename.getValue()), "",
            updateUserId);
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }

    // enable the button
    userprofileupdatebutton.setDisabled(false);
  }

  public void dodBannerAgree(ActionEvent event) {
    RequestContext.getCurrentInstance().returnFromDialog(null, null);
  }

  public void setSelectedUserId(int id) {
    selectedUserId = id;
    log.trace("setting user id: {}", id);
  }

  public int getSelectedUserId() {
    return selectedUserId;
  }

  public void setUserTypeSelect(RichSelectOneChoice userTypeSelect) {

    this.userTypeSelect = userTypeSelect;
  }

  public RichSelectOneChoice getUserTypeSelect() {

    return userTypeSelect;
  }

  public void setUserWorkstationSelect(RichSelectOneChoice userWorkstationSelect) {

    this.userWorkstationSelect = userWorkstationSelect;
  }

  public RichSelectOneChoice getUserWorkstationSelect() {

    return userWorkstationSelect;
  }

  public int getLoginStatus() {
    return loginStatus;
  }

  public void setLoginStatusMessage(String loginStatusMessage) {

    this.loginStatusMessage = loginStatusMessage;
  }

  public String getLoginStatusMessage() {

    return loginStatusMessage;
  }

  public boolean getDisallowLoginDueToNoPivAuthCert() {
    try {
      val request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
      val certUtils = ContextUtils.getBean(CertificateManager.class);
      certUtils.getCertInformationFromRequest(request);
      return false;
    }
    catch (ValidationException e) {
      return true;
    }
  }

  public String getLoginAction() {
    if (loginStatusMessage.length() > 0) {
      return "GoLogin";
    }
    return mobileViewCheck("GoWarehouseHome");
  }

  public String getSwitchWorkstationAction() {
    if (loginStatusMessage.length() > 0) {
      return "GoWorkstation";
    }

    return mobileViewCheck("GoWarehouseHome");
  }

  private String mobileViewCheck(String nonMobileAction) {
    if ("mobile".equals(defaultHomePage)) {
      try {
        FacesContext fc = FacesContext.getCurrentInstance();
        ExternalContext ectx = fc.getExternalContext();
        String contextPath = ectx.getRequestContextPath();
        ectx.redirect(contextPath + "/app/mobile");
      }
      catch (Exception e) {
        log.error(e.getMessage());
      }
    }
    else {
      return nonMobileAction;
    }
    return null;
  }

  public void setLoginFailed(boolean loginFailed) {
    this.loginFailed = loginFailed;
  }

  public boolean isLoginFailed() {
    return loginFailed;
  }

  public void setUserIdSelect(RichSelectOneChoice userIdSelect) {
    this.userIdSelect = userIdSelect;
  }

  public RichSelectOneChoice getUserIdSelect() {
    return userIdSelect;
  }

  public RichSelectOneChoice getHomePageSelect() {
    return homePageSelect;
  }

  public void setHomePageSelect(RichSelectOneChoice homePageSelect) {
    this.homePageSelect = homePageSelect;
  }

  public String getDefaultHomePage() {
    return defaultHomePage;
  }

  public void setDefaultHomePage(String defaultHomePage) {
    this.defaultHomePage = defaultHomePage;
  }

  public void setUserInfo(UserInfo bean) {
    userInfo = bean;
    validateUsingCac = userInfo.isCacLogin();
    log.trace("Use cac login: {}", validateUsingCac);
  }
}
