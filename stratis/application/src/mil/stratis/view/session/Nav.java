package mil.stratis.view.session;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.common.util.Util;
import mil.stratis.model.datatype.user.UserRights;
import mil.stratis.model.services.LoginModuleImpl;
import mil.stratis.model.services.WarehouseSetupImpl;
import mil.stratis.view.BackingHandler;
import mil.stratis.view.util.JSFUtils;
import mil.usmc.mls2.stratis.core.domain.model.*;
import mil.usmc.mls2.stratis.core.infrastructure.configuration.StratisConfig;
import mil.usmc.mls2.stratis.core.infrastructure.util.UserSiteSelectionTracker;
import mil.usmc.mls2.stratis.core.service.*;
import mil.usmc.mls2.stratis.core.service.multitenancy.DateService;
import mil.usmc.mls2.stratis.core.utility.AdfLogUtility;
import mil.usmc.mls2.stratis.core.utility.ContextUtils;
import org.apache.commons.collections4.CollectionUtils;

import javax.faces.event.ValueChangeEvent;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@NoArgsConstructor //ADF BackingHandlers all need a no args constructor
@SuppressWarnings("Duplicates")
public class Nav extends BackingHandler {

  private String sForwardAction;
  private String shutDownMsg = "";
  // object for a while stuff to draw on the footer page
  transient Object footerMainSwitcher = 1;
  transient Object headerMainSwitcher = 0;

  public void setForwardActionVal(ValueChangeEvent valueChangeEvent) {
    sForwardAction = valueChangeEvent.getNewValue().toString();
    log.trace("forward value: {}", sForwardAction);
  }

  public void setSForwardAction(String sForwardAction) {
    log.trace("forward action: {}", sForwardAction);
    this.sForwardAction = sForwardAction;
  }

  public String getShutDownMsg() {
    return shutDownMsg;
  }

  public String getSForwardAction() {
    return sForwardAction;
  }

  public void setFooterMainSwitcher(Object footerMainSwitcher) {

    log.trace("footer switcher value: {}", footerMainSwitcher);
    this.footerMainSwitcher = footerMainSwitcher;
  }

  /**
   * Used in the Footer.jspx switcher
   */
  public Object getFooterMainSwitcher() {
    log.trace("footerMainSwitcher value: {}", footerMainSwitcher);
    return footerMainSwitcher;
  }

  public void setHeaderMainSwitcher(Object headerMainSwitcher) {
    log.trace("header switcher value: {}", headerMainSwitcher);
    this.headerMainSwitcher = headerMainSwitcher;
  }

  /**
   * This function is determines if a user is logged in and has appropriate permissions
   * Used in the Header.jspx switch.
   */
  public Object getHeaderMainSwitcher() {

    // check if we are logged in, if we are not we need to kick them to the login
    UserRights loginRights;
    Object loginId;
    Object loginKey;
    Object returnValue = 0;

    loginRights = (UserRights) JSFUtils.getManagedBeanValue("userbean.userRights");
    loginId = JSFUtils.getManagedBeanValue("userbean.userId");
    loginKey = JSFUtils.getManagedBeanValue("userbean.loginKey");

    if (loginKey == null) {
      loginKey = 0;
    }

    val login = loginId != null ? Integer.parseInt(loginId.toString()) : null;
    // if we have any rights show the normal ajax footer
    if (loginRights != null && loginRights.getUsersRights() > 0 && login != null && login != -1) {
      // ensure we are still logged in
      LoginModuleImpl service = getLoginModule();

      if (service.checkUserLoggedIn(login, loginKey.toString())) {
        // check for a system messageH
        if (service.systemShutdownMessage() != null && login != 1) {
          returnValue = 2;
        }
        else {
          // return 1 to draw the footer
          returnValue = 1;
        }
      }
    }

    // kill the user rights to kill all actions
    if (returnValue.equals(0)) {
      // clear the user bean values
      clearUserBeanValues();
    }

    log.trace("The value that set the footer: {}", returnValue);

    setFooterMainSwitcher(1);

    return returnValue;
  }

  private void clearUserBeanValues() {
    JSFUtils.setManagedBeanValue("userbean.username", "");
    JSFUtils.setManagedBeanValue("userbean.displayName", "");
    JSFUtils.setManagedBeanValue("userbean.userRights", null);
    JSFUtils.setManagedBeanValue("userbean.workstationId", -1);
    JSFUtils.setManagedBeanValue("userbean.userId", -1);
    JSFUtils.setManagedBeanValue("userbean.selected", -1);
    JSFUtils.setManagedBeanValue("userbean.selected2", -1);
    JSFUtils.setManagedBeanValue("userbean.loginKey", "");
  }

  /**
   * @return String
   */
  public String outputSystemStatusFooter() {

    String rstring = "";

    try {

      // call the warehouse setup module to get the system message
      LoginModuleImpl service = getLoginModule();

      Object loginUsername = JSFUtils.getManagedBeanValue("userbean.username");
      if (loginUsername != null && loginUsername.toString().equalsIgnoreCase("STRATIS") || service.isSystemShutdownUser(JSFUtils.getManagedBeanValue("userbean.userId"))) {
        return rstring;
      }

      Object sysmess = service.systemShutdownMessage();
      if (sysmess != null) {
        rstring = "<script>";
        rstring += "alert('" + sysmess + "\\nSystem will be shutdown soon.');";
        rstring += "self.location.href='login.jspx';";
        rstring += "</script>";

        clearUserBeanValues();
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }

    return rstring;
  }

  /**
   * This function returns an in session refresh kickout
   */
  @SuppressWarnings("unused") //called from .jspx
  public String kickoutuser() {

    String returnstring = "";

    // check if we are logged in, if we are not we need to kick them to the login
    UserRights loginrights = (UserRights) JSFUtils.getManagedBeanValue("userbean.userRights");
    Object loginid = JSFUtils.getManagedBeanValue("userbean.userId");
    Object loginkey = JSFUtils.getManagedBeanValue("userbean.loginKey");

    if (loginkey == null) {
      loginkey = 0;
    }

    // if we have any rights show the normal ajax footer
    if ((loginrights != null) && (loginrights.getUsersRights() > 0)) {
      val login = loginid != null ? Integer.parseInt(loginid.toString()) : null;
      if ((login != null) && (login != -1)) {
        // ensure we are still logged in
        LoginModuleImpl service = getLoginModule();

        if (!service.checkUserLoggedIn(login, loginkey.toString())) {
          // kick them out
          returnstring = "KICKME";
        }
      }
      else {
        // kick them out
        returnstring = "KICKME";
      }
    }
    else {
      // kick them out
      returnstring = "KICKME";
    }
    if (returnstring.equals("KICKME")) {
      JSFUtils.setManagedBeanValue("footerbean.footerMainSwitcher", 0);
    }
    return returnstring;
  }

  /**
   * This function returns the status message if there is one
   */
  public String outputSystemStatus() {
    String rstring = "";

    try {
      // call the LOGIN module to get the system message
      LoginModuleImpl service = getLoginModule();

      // get current logged in user
      Object loginUsername = JSFUtils.getManagedBeanValue("userbean.username");
      if (loginUsername != null && loginUsername.toString().equalsIgnoreCase("STRATIS") ||
          service.isSystemShutdownUser(JSFUtils.getManagedBeanValue("userbean.userId"))) {
        return rstring;
      }

      Object sysmess = service.systemShutdownMessage();
      if (sysmess != null) {
        rstring = sysmess.toString();
        JSFUtils.setManagedBeanValue("footerbean.footerMainSwitcher", 2);
        shutDownMsg = rstring;
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }

    return rstring;
  }

  public String getCurrentSiteName() {
    StratisConfig multiTenancyConfig = ContextUtils.getBean(StratisConfig.class);
    if (multiTenancyConfig.isDisplayCurrentSiteName())
      return UserSiteSelectionTracker.getSiteName();
    return "";
  }

  /**
   * This function outputs the current box of locations and what they are doing.
   * Used in WarehouseDisplay.jsp
   */
  public String getWorkloadStatus() {

    // check if we are logged in, if we are not we need to kick them to the login
    Object loginworkstation = JSFUtils.getManagedBeanValue("userbean.workstationId");

    val loginWorkstationStr = loginworkstation != null ? loginworkstation.toString() : null;
    if ((loginworkstation != null) && (Long.parseLong(loginWorkstationStr) != -1)) {

      // call the warehouse setup module to get a list of all the wacs and how many pending items they have
      WarehouseSetupImpl service = getWarehouseSetupModule();

      // the list to answer with
      List<String> answers = new ArrayList<>();

      service.buildWACList(Integer.parseInt(loginWorkstationStr), answers);

      StringBuilder output = new StringBuilder();
      if (CollectionUtils.isNotEmpty(answers)) {
        // build the response table
        output.append("<table border=\"1\" cellspacing=\"1\" cellpadding=\"1\" style=\"border-color: #666666; border-width:thin;\">");
        output.append("<tr style=\"background-color: #FFCC66; color: #666666; font-size: 12px; font-weight: bold; padding-left: 3px; padding-right: 3px;\"><th>WAC</th><th>Jobs</th></tr>");

        for (int i = 0; i < answers.size(); i += 2) {
          output.append("<tr style=\"font-size: 12px; color: black; padding-left: 4px; padding-right: 4px; text-decoration: none; border-color: #666666; border-width:thick;\">");

          output.append("<td align=\"right\">");
          output.append(answers.get(i));
          output.append("</td>");

          output.append("<td align=\"right\">");
          output.append(answers.get(i + 1));
          output.append("</td>");

          output.append("</tr>");
        }

        output.append("</table>");
      }
      else {
        // say there is no work
        output.append("No Pending Work.");
      }

      return output.toString();
    }
    else {
      // don't do anything
      return "";
    }
  }

  /**
   * This function outputs the info on the current WAC selected
   * Used in WorkstationDisplay.jsp
   */
  public String getWorkloadWACStatus() {

    // check if we are logged in, if we are not we need to kick them to the login
    UserRights loginrights = (UserRights) JSFUtils.getManagedBeanValue("userbean.userRights");

    if ((loginrights != null) && (loginrights.getUsersRights() > 0)) {

      // show all the info about this wac
      // check if we are logged in, if we are not we need to kick them to the login
      Object loginworkstation = JSFUtils.getManagedBeanValue("userbean.workstationId");

      String output2 = "";

      val loginWorkstationStr = loginworkstation != null ? loginworkstation.toString() : null;
      if ((loginworkstation != null) && (Long.parseLong(loginWorkstationStr) != -1)) {

        // check if they are still have a proper login
        // the list to get the answers
        val answers = loadWorkstationStats(Integer.parseInt(loginWorkstationStr));

        // build the response
        if (answers.size() > 1) {
          // build the response table
          output2 = "<table border=\"1\" cellspacing=\"1\" cellpadding=\"1\" style=\"border-color: #666666; border-width:thin;\">";

          output2 += "<tr style=\"background-color: #FFCC66; color: #666666; font-size: 12px; font-weight: bold; padding-left: 3px; padding-right: 3px;\"><th>WAC</th>";
          output2 += "<th>Stow</th>";
          output2 += "<th>Pick</th>";
          output2 += "<th>Inventory</th>";
          output2 += "<th>Loc Survey</th>";
          output2 += "<th>NIIN Update</th>";
          output2 += "<th>Item EXP</th></tr>";

          output2 += "<tr style=\"font-size: 12px; color: black; padding-left: 4px; padding-right: 4px; text-decoration: none; border-color: #666666; border-width:thick;\">";

          output2 += "<td align=\"right\">";
          output2 += answers.get(0);
          output2 += "</td>";

          output2 += "<td align=\"right\">";
          output2 += answers.get(1);
          output2 += "</td>";

          output2 += "<td align=\"right\">";
          output2 += answers.get(2);
          output2 += "</td>";

          output2 += "<td align=\"right\">";
          output2 += answers.get(3);
          output2 += "</td>";

          output2 += "<td align=\"right\">";
          output2 += answers.get(4);
          output2 += "</td>";

          output2 += "<td align=\"right\">";
          output2 += answers.get(5);
          output2 += "</td>";

          output2 += "<td align=\"right\">";
          output2 += answers.get(6);
          output2 += "</td>";

          output2 += "</table>";
        }
        else {
          output2 = "WAC not assigned to workstation.";
        }
      }

      return output2;
    }
    else {
      JSFUtils.setManagedBeanValue("footerbean.footerMainSwitcher", 0);
      // kick them to the login page
      return "KICKME";
    }
  }

  /**
   * This function outputs the info on the current WAC selected
   * Used in *_Home.jspx
   */
  public String getWorkloadWACStatus2() {

    // check if we are logged in, if we are not we need to kick them to the login
    UserRights loginrights = (UserRights) JSFUtils.getManagedBeanValue("userbean.userRights");

    if ((loginrights != null) && (loginrights.getUsersRights() > 0)) {

      // show all the info about this wac
      // check if we are logged in, if we are not we need to kick them to the login
      Object loginworkstation = JSFUtils.getManagedBeanValue("userbean.workstationId");

      String output2 = "";

      val loginWorkstationStr = loginworkstation != null ? loginworkstation.toString() : null;
      if ((loginworkstation != null) && (Long.parseLong(loginWorkstationStr) != -1)) {
        // check if they are still have a proper login
        // the list to get the answers
        val answers = loadWorkstationStats(Integer.parseInt(loginWorkstationStr));

        // build the response
        if (answers.size() > 1) {
          // build the response table

          output2 = "<table align=\"center\" border=\"1\" cellspacing=\"1\" cellpadding=\"1\" style=\"border-color: #666666; border-width:thin;\">";

          output2 += "<tr style=\"font-size: 18px; color: black; padding-left: 4px; padding-right: 4px; text-decoration: none; border-color: #666666; border-width:thick;\">";
          output2 += "<th>PENDING WORK LOAD FOR THIS WAC " + answers.get(0) + "</th></tr>";
          output2 += "<tr><td>";
          output2 += "<table align=\"center\" border=\"1\" cellspacing=\"1\" cellpadding=\"1\" style=\"border-color: #666666; border-width:thin;\">";
          output2 += "<tr style=\"background-color: #FFCC66; color: #666666; font-size: 15px; font-weight: bold; padding-left: 3px; padding-right: 3px;\">";
          output2 += "<th>Stow</th>";
          output2 += "<th>Pick</th>";
          output2 += "<th>Inventory</th>";
          output2 += "<th>Location Survey</th>";
          output2 += "<th>NIIN Update</th>";
          output2 += "<th>Shelf Life</th></tr>";

          output2 += "<tr style=\"font-size: 15px; color: black; padding-left: 4px; padding-right: 4px; text-decoration: none; border-color: #666666; border-width:thick;\"><td align=\"right\">";

          output2 += answers.get(1);
          output2 += "</td>";

          output2 += "<td align=\"right\">";
          output2 += answers.get(2);
          output2 += "</td>";

          output2 += "<td align=\"right\">";
          output2 += answers.get(3);
          output2 += "</td>";

          output2 += "<td align=\"right\">";
          output2 += answers.get(4);
          output2 += "</td>";

          output2 += "<td align=\"right\">";
          output2 += answers.get(5);
          output2 += "</td>";

          output2 += "<td align=\"right\">";
          output2 += answers.get(6);
          output2 += "</td>";

          output2 += "</tr></table></td></tr></table>";
        }
        else {
          output2 = "WAC not assigned to workstation.";
        }
      }

      return output2;
    }
    else {
      // kick them to the login page
      JSFUtils.setManagedBeanValue("footerbean.footerMainSwitcher", 0);
      return "KICKME";
    }
  }

  /**
   * This function outputs the info on the current WAC selected
   * Used in *_Home.jspx
   */
  public String getWorkloadWACFull() {

    StringBuilder output = new StringBuilder();
    //select wac_id, wac_number from wac order by wac_number
    // call the warehouse setup module to get a list of all the wacs and how many pending items they have
    WarehouseSetupImpl service = getWarehouseSetupModule();

    List<String> wacs = new ArrayList<>();

    try (PreparedStatement pstmt = service.getDBTransaction().createPreparedStatement("select wac_id, wac_number from wac order by wac_number", 0)) {
      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          String wacId = rs.getString(1);
          wacs.add(wacId + "," + rs.getString(2));
        }
      }
    }
    catch (SQLException sqle) {
      log.trace(sqle.getLocalizedMessage());
    }

    output.append("<table align=\"center\" border=\"0\" cellspacing=\"1\" cellpadding=\"1\" style=\"border-color: #666666; border-width:thin;\">");
    output.append("<tr style=\"font-size: 18px; color: black; padding-left: 4px; padding-right: 4px; text-decoration: none; border-color: #666666; border-width:thick;\">");
    output.append("<th>WORK LOAD REPORT</th></tr>");
    output.append("<tr><td>");
    output.append("<table align=\"center\" border=\"1\" cellspacing=\"1\" cellpadding=\"1\" style=\"border-color: #666666; border-width:thin;\">");
    output.append("<tr style=\"background-color: #FFCC66; color: #666666; font-size: 15px; font-weight: bold; padding-left: 3px; padding-right: 3px;\">");
    output.append("<th>WAC</th>");
    output.append("<th>STOW</th>");
    output.append("<th>PICK</th>");
    output.append("<th>PACK</th>");

    output.append("<th>SHIP</th><th>INVEN</th><th>SHELF LIFE</th><th>LOC SURVEY</th><th>WT PICKS</tr>");
    int tStows = 0;
    int tPicks = 0;
    int tPacks = 0;
    int tShips = 0;
    int tInvs = 0;
    int tLocSuvs = 0;
    int tShls = 0;
    int tWtqs = 0;

    for (String wacIdandNum : wacs) {
      // check if they are still have a proper login
      // the list to get the answers
      String wac = wacIdandNum.split(",")[0];
      String wacLabel = wacIdandNum.split(",")[1];
      List<String> answers = new ArrayList<>();

      service.buildSelectedWACFull(Util.cleanInt(wac), wacLabel, answers);
      if (answers.size() > 1) {
        // build the response table

        output.append("<tr style=\"font-size: 15px; color: black; padding-left: 4px; padding-right: 4px; text-decoration: none; border-color: #666666; border-width:thick;\">");
        output.append("<td align=\"right\">");
        output.append(answers.get(0));
        output.append("</td>");

        output.append("<td align=\"right\">");
        output.append(answers.get(1));
        output.append("</td>");
        tStows = tStows + Integer.parseInt(answers.get(1));

        output.append("<td align=\"right\">");
        output.append(answers.get(2));
        output.append("</td>");
        tPicks = tPicks + Integer.parseInt(answers.get(2));

        output.append("<td align=\"right\">");
        output.append(answers.get(3));
        output.append("</td>");
        tPacks = tPacks + Integer.parseInt(answers.get(3));

        output.append("<td align=\"right\">");
        output.append(answers.get(4));
        output.append("</td>");
        tShips = tShips + Integer.parseInt(answers.get(4));

        output.append("<td align=\"right\">");
        output.append(answers.get(5));
        output.append("</td>");
        tInvs = tInvs + Integer.parseInt(answers.get(5));

        output.append("<td align=\"right\">");
        output.append(answers.get(6));
        output.append("</td>");
        tLocSuvs = tLocSuvs + Integer.parseInt(answers.get(6));

        output.append("<td align=\"right\">");
        output.append(answers.get(7));
        output.append("</td>");
        tShls = tShls + Integer.parseInt(answers.get(7));

        output.append("<td align=\"right\">");
        output.append(answers.get(8));
        output.append("</td>");
        tWtqs = tWtqs + Integer.parseInt(answers.get(8));

        output.append("</tr>");
      }
    }

    // build the response table

    output.append("<tr style=\"font-size: 15px; color: black; padding-left: 4px; padding-right: 4px; text-decoration: none; border-color: #666666; border-width:thick;\"><td align=\"right\">");

    output.append("TOTAL");
    output.append("</td>");

    output.append("<td align=\"right\">");
    output.append(tStows);
    output.append("</td>");

    output.append("<td align=\"right\">");
    output.append(tPicks);
    output.append("</td>");

    output.append("<td align=\"right\">");
    output.append(tPacks);
    output.append("</td>");

    output.append("<td align=\"right\">");
    output.append(tShips);
    output.append("</td>");

    output.append("<td align=\"right\">");
    output.append(tInvs);
    output.append("</td>");

    output.append("<td align=\"right\">");
    output.append(tLocSuvs);
    output.append("</td>");

    output.append("<td align=\"right\">");
    output.append(tShls);
    output.append("</td>");

    output.append("<td align=\"right\">");
    output.append(tWtqs);
    output.append("</td>");

    output.append("</tr>");

    output.append("</table></td></tr>");
    output.append("</table>");

    return output.toString();
  }

  private ArrayList<String> loadWorkstationStats(Integer workstationId) {

    val equipmentService = ContextUtils.getBean(EquipmentService.class);
    val workstation = equipmentService.findById(workstationId).orElseGet(Equipment::new);
    val wac = workstation.getWac();

    ArrayList<String> answers = new ArrayList<>();
    if (wac == null) return answers;

    val pickingService = ContextUtils.getBean(PickingService.class);
    val stowService = ContextUtils.getBean(StowService.class);
    val inventoryItemService = ContextUtils.getBean(InventoryItemService.class);
    val niinLocationService = ContextUtils.getBean(NiinLocationService.class);

    val wacId = wac.getWacId();

    answers.add(wac.getWacNumber());

    List<String> stowStatuses = new ArrayList<>();
    stowStatuses.add("STOW READY");
    stowStatuses.add("STOW BYPASS1");
    val stowCriteria = StowSearchCriteria.builder()
        .wacId(wacId)
        .statuses(stowStatuses)
        .build();
    answers.add(String.valueOf(stowService.count(stowCriteria)));

    List<String> pickingStatuses = new ArrayList<>();
    pickingStatuses.add("PICK READY");
    pickingStatuses.add("PICK BYPASS1");
    val pickingCriteria = PickingSearchCriteria.builder()
        .wacId(wacId)
        .statuses(pickingStatuses)
        .build();
    answers.add(String.valueOf(pickingService.count(pickingCriteria)));

    List<String> pendingInventoryStatuses = new ArrayList<>();
    pendingInventoryStatuses.add("INVENTORYPENDING");
    val pendingInventoryCriteria = InventoryItemSearchCriteria.builder()
        .wacId(wacId)
        .statuses(pendingInventoryStatuses)
        .build();
    answers.add(String.valueOf(inventoryItemService.count(pendingInventoryCriteria)));

    List<String> locationSurveyPendingStatuses = new ArrayList<>();
    locationSurveyPendingStatuses.add("LOCSURVEYPENDING");
    val locationSurveyCriteria = InventoryItemSearchCriteria.builder()
        .wacId(wacId)
        .statuses(locationSurveyPendingStatuses)
        .build();
    answers.add(String.valueOf(inventoryItemService.count(locationSurveyCriteria)));

    val niinLocationCriteria = NiinLocationSearchCriteria.builder()
        .wacId(wacId)
        .nsnRemark("Y")
        .build();
    answers.add(String.valueOf(niinLocationService.count(niinLocationCriteria)));

    val dateService = ContextUtils.getBean(DateService.class);
    val shelflifeCriteria = NiinLocationSearchCriteria.builder()
        .wacId(wacId)
        .checkNumExtentsNull(true)
        .expRemark("Y")
        .expirationDate(dateService.getLastDateOfThisMonth())
        .build();
    answers.add(String.valueOf(niinLocationService.count(shelflifeCriteria)));

    return answers;
  }
}
