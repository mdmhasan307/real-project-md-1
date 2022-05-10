package mil.stratis.view.admin;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.common.util.PadUtil;
import mil.stratis.common.util.Util;
import mil.stratis.model.services.ImportFilesImpl;
import mil.stratis.model.services.SysAdminImpl;
import mil.stratis.view.user.UserInfo;
import mil.stratis.view.util.ADFUtils;
import mil.stratis.view.util.JSFUtils;
import mil.usmc.mls2.stratis.core.infrastructure.configuration.Profiles;
import mil.usmc.mls2.stratis.core.service.common.SettingsSystemService;
import mil.usmc.mls2.stratis.core.utility.ContextUtils;
import oracle.adf.model.BindingContext;
import oracle.adf.model.binding.DCBindingContainer;
import oracle.adf.model.binding.DCIteratorBinding;
import oracle.adf.view.rich.component.rich.data.RichTable;
import oracle.adf.view.rich.component.rich.input.RichInputText;
import oracle.adf.view.rich.component.rich.input.RichSelectOneChoice;
import oracle.adf.view.rich.component.rich.nav.RichButton;
import oracle.adf.view.rich.component.rich.output.RichOutputText;
import oracle.jbo.Key;
import oracle.jbo.Row;
import oracle.jbo.uicli.binding.JUCtrlValueBindingRef;
import org.apache.myfaces.trinidad.event.DisclosureEvent;
import org.apache.myfaces.trinidad.event.PollEvent;
import org.springframework.core.env.Environment;

import javax.faces.event.ActionEvent;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.StringTokenizer;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

@Slf4j
@NoArgsConstructor
public class SystemAdminBacking extends AdminBackingHandler {

  private transient RichTable loggedinuserTable;

  //* Site File Interface variables
  private transient RichButton interfaceimportbutton;
  private transient RichButton interfacetransmitbutton;
  private String interfaceoutputmessage = "";
  private String interfaceoutputmessagestyle = "";
  boolean pollFlag = false;
  String pollMessage = "";
  private transient RichOutputText interfacePollMessage;
  private transient RichTable interfaceTable;
  private transient RichInputText pollStatus;
  private transient Object activeRuns = null;

  //* 54 Access variables
  private transient RichInputText sysAdminTableAccessColumn;
  private transient RichInputText sysAdminTableAccessWhere;
  private transient RichSelectOneChoice sysAdminTableAccessCommand;
  private transient RichSelectOneChoice sysAdminTableAccessTable;
  private String sysAdminTableAccessOutput = "";

  DCIteratorBinding deleteiterator;
  //* External Variables (popup and niin)
  transient Object CheckDeleteType = 0;
  String DeleteRedirect = "";

  //MHIF Autorun
  private transient RichSelectOneChoice MHIFAutorun;

  //classification
  private transient RichSelectOneChoice classificationColor;
  private transient RichInputText classificationText;

  // Injected user info bean
  private UserInfo userInfo;

  public void pollActiveSessions(@SuppressWarnings("all") PollEvent pollEvent) {
    DCIteratorBinding dciter = ADFUtils.findIterator("UsersView1Iterator");
    Key current_row_key = dciter.getCurrentRow().getKey();
    dciter.executeQuery();
    dciter.setCurrentRowWithKey(current_row_key.toStringFormat(true));
  }

  public void killUsers(ActionEvent event) {
    SysAdminImpl service = getSysAdminModule();
    DCIteratorBinding containerIter = ADFUtils.findIterator("UsersView1Iterator");
    Object oldRowKey = loggedinuserTable.getRowKey();
    Iterator selection = loggedinuserTable.getSelectedRowKeys().iterator();
    try {
      while (selection.hasNext()) {
        Object rowKey = selection.next();
        loggedinuserTable.setRowKey(rowKey);
        JUCtrlValueBindingRef rowData = (JUCtrlValueBindingRef) loggedinuserTable.getRowData();
        Row r = rowData.getRow();
        if (r.getAttribute("UserId") != null) {
          int userId = Integer.parseInt(r.getAttribute("UserId").toString());
          service.clearUserLogin(userId);
        }
      }
    }
    catch (Exception e) {log.trace("Exception caught and ignored", e);}
    finally {
      loggedinuserTable.setRowKey(oldRowKey);
    }
    // Refresh the table.
    if (containerIter != null) {
      containerIter.executeQuery();
    }
  }

  public UserInfo getUserInfo() {
    return userInfo;
  }

  // Setter for injected user info bean.

  public void setUserInfo(UserInfo bean) {
    userInfo = bean;
  }

  public void setPollFlag(boolean show) {
    this.pollFlag = show;
  }

  public boolean getPollFlag() {
    return pollFlag;
  }

  public void setPollMessage(String msg) {
    this.pollMessage = msg;
  }

  public String getPollMessage() {
    return pollMessage;
  }

  public void saveSystemDefaults(ActionEvent event) {
    if (event != null) { /* NOOP prevents unused parameter warning */ }
    SysAdminImpl service = getSysAdminModule();
    if (service != null) {
      service.setConfigValue("MHIF_AUTORUN", (String) MHIFAutorun.getValue());
      service.setConfigValue("STRATIS_CLASS_COLOR", (String) classificationColor.getValue());
      service.setConfigValue("STRATIS_CLASS_TEXT", (String) classificationText.getValue());
      // Commit the rest of the changes.
      service.commit();
    }
    else {
      log.error("Unable to save system defaults (saveSystemDefaults) - service is null.");
    }
  }

  public void regenerateSystemUuid(ActionEvent event) {
    log.trace("In method regenerateSystemUuid...");
    val settingsSystemService = ContextUtils.getBean(SettingsSystemService.class);
    settingsSystemService.regenerateSystemUuid();
    log.info("Completed method regenerateSystemUuid.");
  }

  public String getSystemUuid() {
    if (!isIntegrationProfileEnabled()) {
      return "Integration Not Enabled";
    }
    return ContextUtils.getBean(SettingsSystemService.class).getSystemUuidString();
  }

  public Boolean getSystemUuidModifiable() {
    return isIntegrationProfileEnabled();
  }

  private boolean isIntegrationProfileEnabled() {
    val environment = ContextUtils.getBean(Environment.class);
    return Profiles.isIntegrationAny(environment);
  }

  @SuppressWarnings("SameParameterValue")
  private void refreshTable(String iterName) {
    DCBindingContainer bindings =
        (DCBindingContainer) BindingContext.getCurrent().getCurrentBindingsEntry();
    DCIteratorBinding iter = bindings.findIteratorBinding(iterName);
    if (iter != null) {
      String rowKeyStr = iter.getCurrentRow().getKey().toStringFormat(true);
      iter.executeQuery();
      if (rowKeyStr != null) {
        iter.setCurrentRowWithKey(rowKeyStr);
      }
    }
  }

  @SuppressWarnings("unused")
  public void pollRunningInterfaces(PollEvent pollEvent) {
    DCIteratorBinding siteInterfacesIter = ADFUtils.findIterator("SiteInterfacesView1Iterator");
    refreshTable("SiteInterfacesView1Iterator");

    // which file do we have
    Row row = siteInterfacesIter.getCurrentRow();
    String workfile = row.getAttribute("InterfaceName").toString();
    Object statusObj = row.getAttribute("Status");
    String status = "";

    if (!Util.isEmpty(statusObj))
      status = statusObj.toString();
    log.debug("polling...");

    if (workfile.equals("DASF")) {
      if (status.equals("RUNNING")) {
        interfaceoutputmessage = "DASF Running....";
        interfaceoutputmessagestyle = "color:#BBBBBB;font-weight:bolder;text-decoration:blink";
        setPollMessage(interfaceoutputmessage);
        getInterfacePollMessage().setValue(getPollMessage());
        getInterfacePollMessage().setInlineStyle(interfaceoutputmessagestyle);
      }
      else {
        interfaceoutputmessage = "";
        setPollMessage(interfaceoutputmessage);
        getInterfacePollMessage().setValue(getPollMessage());
      }

      if (getPollFlag() && isActiveRun(workfile)) {
        // get the import service
        ImportFilesImpl service = getImportFilesService();

        if (service.checkDASFFinished()) {

          String statusNOW = service.getSiteInterfaceStatus("DASF");
          if (statusNOW.equals("FAILED") || statusNOW.equals("LOADFAILED")) {
            interfaceoutputmessage = "DASF import failed.";
            interfaceoutputmessagestyle = "color:#CC0000;font-weight:bolder;text-decoration:blink";
            setPollMessage(interfaceoutputmessage);
            getInterfacePollMessage().setValue(getPollMessage());
            getInterfacePollMessage().setInlineStyle(interfaceoutputmessagestyle);
          }
          else {
            interfaceoutputmessage = "DASF was successfully imported.";
            interfaceoutputmessagestyle = "color:green;font-weight:bolder;text-decoration:blink";
            setPollMessage(interfaceoutputmessage);
            getInterfacePollMessage().setValue(getPollMessage());
            getInterfacePollMessage().setInlineStyle(interfaceoutputmessagestyle);
          }
        }
      }
    }
    else if (workfile.equals("MHIF")) {
      if (status.equals("RUNNING")) {
        interfaceoutputmessage = "MHIF Running....";
        interfaceoutputmessagestyle = "color:#BBBBBB;font-weight:bolder;text-decoration:blink";
        setPollMessage(interfaceoutputmessage);
        getInterfacePollMessage().setValue(getPollMessage());
        getInterfacePollMessage().setInlineStyle(interfaceoutputmessagestyle);
      }
      else {
        interfaceoutputmessage = "";
        setPollMessage(interfaceoutputmessage);
        getInterfacePollMessage().setValue(getPollMessage());
      }

      if (getPollFlag() && isActiveRun(workfile)) {
        // get the import service
        ImportFilesImpl service = getImportFilesService();

        if (service.checkMHIFFinished()) {

          String statusNOW = service.getSiteInterfaceStatus("MHIF");
          if (statusNOW.equals("FAILED") || statusNOW.equals("LOADFAILED")) {
            interfaceoutputmessage = "MHIF import failed.";
            interfaceoutputmessagestyle = "color:#CC0000;font-weight:bolder;text-decoration:blink";
            setPollMessage(interfaceoutputmessage);
            getInterfacePollMessage().setValue(getPollMessage());
            getInterfacePollMessage().setInlineStyle(interfaceoutputmessagestyle);
          }
          else if (statusNOW.equals("COMPLETED-ERROR")) {
            interfaceoutputmessage = "MHIF processed with errors.";
            interfaceoutputmessagestyle = "color:#CC0000;font-weight:bolder;text-decoration:blink";
            setPollMessage(interfaceoutputmessage);
            getInterfacePollMessage().setValue(getPollMessage());
            getInterfacePollMessage().setInlineStyle(interfaceoutputmessagestyle);
          }
          else {
            interfaceoutputmessage = "MHIF was successfully imported.";
            interfaceoutputmessagestyle = "color:green;font-weight:bolder;text-decoration:blink";
            setPollMessage(interfaceoutputmessage);
            getInterfacePollMessage().setValue(getPollMessage());
            getInterfacePollMessage().setInlineStyle(interfaceoutputmessagestyle);
          }
        }
      }
    }
    if (workfile.equals("MATS")) {
      if (status.equals("RUNNING")) {
        interfaceoutputmessage = "MATS Running....";
        interfaceoutputmessagestyle = "-color:#BBBBBB;font-weight:bolder;text-decoration:blink";
        setPollMessage(interfaceoutputmessage);
        getInterfacePollMessage().setValue(getPollMessage());
        getInterfacePollMessage().setInlineStyle(interfaceoutputmessagestyle);
      }
      else {
        interfaceoutputmessage = "";
        setPollMessage(interfaceoutputmessage);
        getInterfacePollMessage().setValue(getPollMessage());
      }

      if (getPollFlag() && isActiveRun(workfile)) {
        // get the import service
        ImportFilesImpl service = getImportFilesService();

        if (service.checkMATSFinished()) {

          String statusNOW = service.getSiteInterfaceStatus("MATS");
          if (statusNOW.equals("FAILED") || statusNOW.equals("LOADFAILED")) {
            interfaceoutputmessage = "MATS import failed.";
            interfaceoutputmessagestyle = "color:#CC0000;font-weight:bolder;text-decoration:blink";
            setPollMessage(interfaceoutputmessage);
            getInterfacePollMessage().setValue(getPollMessage());
            getInterfacePollMessage().setInlineStyle(interfaceoutputmessagestyle);
          }
          else {
            interfaceoutputmessage = "MATS was successfully imported.";
            interfaceoutputmessagestyle = "color:green;font-weight:bolder;text-decoration:blink";
            setPollMessage(interfaceoutputmessage);
            getInterfacePollMessage().setValue(getPollMessage());
            getInterfacePollMessage().setInlineStyle(interfaceoutputmessagestyle);
          }
        }
      }
    }
    if (workfile.equals("GABF")) {
      if (status.equals("RUNNING")) {
        interfaceoutputmessage = "GABF Running....";
        interfaceoutputmessagestyle = "color:#BBBBBB;font-weight:bolder;text-decoration:blink";
        setPollMessage(interfaceoutputmessage);
        getInterfacePollMessage().setValue(getPollMessage());
        getInterfacePollMessage().setInlineStyle(interfaceoutputmessagestyle);
      }
      else {
        interfaceoutputmessage = "";
        setPollMessage(interfaceoutputmessage);
        getInterfacePollMessage().setValue(getPollMessage());
      }

      if (getPollFlag() && isActiveRun(workfile)) {
        // get the import service
        ImportFilesImpl service = getImportFilesService();

        if (service.checkGABFFinished()) {

          String statusNOW = service.getSiteInterfaceStatus("GABF");
          if (statusNOW.equals("FAILED") || statusNOW.equals("LOADFAILED")) {
            interfaceoutputmessage = "GABF import failed.";
            interfaceoutputmessagestyle = "color:#CC0000;font-weight:bolder;text-decoration:blink";
            setPollMessage(interfaceoutputmessage);
            getInterfacePollMessage().setValue(getPollMessage());
            getInterfacePollMessage().setInlineStyle(interfaceoutputmessagestyle);
          }
          else {
            interfaceoutputmessage = "GABF was successfully imported.";
            interfaceoutputmessagestyle = "color:green;font-weight:bolder;text-decoration:blink";
            setPollMessage(interfaceoutputmessage);
            getInterfacePollMessage().setValue(getPollMessage());
            getInterfacePollMessage().setInlineStyle(interfaceoutputmessagestyle);
          }
        }
      }
    }
    if (workfile.equals("GBOF")) {
      if (status.equals("RUNNING")) {
        interfaceoutputmessage = "GBOF Running....";
        interfaceoutputmessagestyle = "color:#BBBBBB;font-weight:bolder;text-decoration:blink";
        setPollMessage(interfaceoutputmessage);
        getInterfacePollMessage().setValue(getPollMessage());
        getInterfacePollMessage().setInlineStyle(interfaceoutputmessagestyle);
      }
      else {
        interfaceoutputmessage = "";
        setPollMessage(interfaceoutputmessage);
        getInterfacePollMessage().setValue(getPollMessage());
      }

      if (getPollFlag() && isActiveRun(workfile)) {
        // get the import service
        ImportFilesImpl service = getImportFilesService();

        if (service.checkGBOFFinished()) {

          String statusNOW = service.getSiteInterfaceStatus("GBOF");
          if (statusNOW.equals("FAILED") || statusNOW.equals("LOADFAILED")) {
            interfaceoutputmessage = "GBOF import failed.";
            interfaceoutputmessagestyle = "color:#CC0000;font-weight:bolder;text-decoration:blink";
            setPollMessage(interfaceoutputmessage);
            getInterfacePollMessage().setValue(getPollMessage());
            getInterfacePollMessage().setInlineStyle(interfaceoutputmessagestyle);
          }
          else {
            interfaceoutputmessage = "GBOF was successfully imported.";
            interfaceoutputmessagestyle = "color:green;font-weight:bolder;text-decoration:blink";
            setPollMessage(interfaceoutputmessage);
            getInterfacePollMessage().setValue(getPollMessage());
            getInterfacePollMessage().setInlineStyle(interfaceoutputmessagestyle);
          }
        }
      }
    }
  }

  // function to import/export and transmit the select file

  public void saveImportExport(ActionEvent action) {
    if (action != null) {}
    DCIteratorBinding siteInterfacesIter = ADFUtils.findIterator("SiteInterfacesView1Iterator");

    Row row = siteInterfacesIter.getCurrentRow();

    siteInterfacesIter.getDataControl().commitTransaction();
    refreshIteratorKeepPosition(siteInterfacesIter, getInterfaceTable());
  }

  public void setInterfaceoutputmessage(String interfaceoutputmessage) {
    this.interfaceoutputmessage = interfaceoutputmessage;
  }

  public String getInterfaceoutputmessage() {
    return interfaceoutputmessage;
  }

  public void setInterfaceoutputmessagestyle(String interfaceoutputmessagestyle) {
    this.interfaceoutputmessagestyle = interfaceoutputmessagestyle;
  }

  public String getInterfaceoutputmessagestyle() {
    return interfaceoutputmessagestyle;
  }

  /**
   * This function to execute a custom built prepared statement
   * and populate the results into an output message formatted as an
   * HTML table.
   * NOTE: INSERT, UPDATE, DELETE removed from UI 1/10/08
   */
  @SuppressWarnings("unused")
  public void returnPreparedStatement(ActionEvent event) {
    // check that we have all fields first
    boolean sendquery = true;
    String command = "";
    String table = "";
    String columns = "";
    String whereClause = "";
    boolean isSplat = false;

    if (sysAdminTableAccessCommand.getValue() == null) {
      // add an error
      JSFUtils.addFacesErrorMessage("REQUIRED FIELD MISSING", "Requested Command.");

      // say not to send
      sendquery = false;
    }
    else {
      command = sysAdminTableAccessCommand.getValue().toString();
    }

    // if we are still good check if this is delete, as we don't need columns
    if (sendquery) {

      if (sysAdminTableAccessColumn.getValue() == null) {
        // add an error
        JSFUtils.addFacesErrorMessage("REQUIRED FIELD MISSING", "Requested Columns.");

        // say not to send
        sendquery = false;
      }
      else {
        columns = sysAdminTableAccessColumn.getValue().toString();
        if (columns.trim().equals("*"))
          isSplat = true;
      }

      if (sysAdminTableAccessTable.getValue() == null) {
        // add an error
        JSFUtils.addFacesErrorMessage("REQUIRED FIELD MISSING", "Requested Table.");

        // say not to send
        sendquery = false;
      }
      else {
        table = sysAdminTableAccessTable.getValue().toString();
      }
    }

    if (sendquery) {
      PreparedStatement pstmt = null;
      ResultSet rs = null;
      try {
        // get the module for the prepared statment
        SysAdminImpl service = getSysAdminModule();
        if (service != null) {
          // get the fields
          StringTokenizer columnTitles = new StringTokenizer(columns, ",");
          int columnTitleSize = columnTitles.countTokens();

          //* start building the table for output
          sysAdminTableAccessOutput = "";
          sysAdminTableAccessOutput += "<table border='1' cellpadding='1' cellspacing='0' style='font-size:10px;font-family: Helvetica, serif;'>";

          //* build the sql statement
          String sql = "";
          if (command.equals("SELECT")) {
            sql = command + " " + columns + " FROM " + table;
            if (sysAdminTableAccessWhere.getValue() != null) {
              whereClause = sysAdminTableAccessWhere.getValue().toString();
              if (!whereClause.trim().equals("") && !whereClause.contains(";")) {
                sql += " WHERE " + whereClause;
              }
            }
            sql = "select * from (" + sql + ") where rownum < 101";
            // create the prepared statment
            log.debug(sql);
            pstmt = service.getDBTransaction().createPreparedStatement(sql, 0);
            // get the result set
            rs = pstmt.executeQuery();
            boolean hasRows = false;
            if (rs != null) {
              //* populate the table rows
              int r = 0;
              while (rs.next()) {
                //* build the table header with column titles
                if (r == 0) {
                  sysAdminTableAccessOutput += "<tr>";
                  if (!isSplat) {
                    while (columnTitles.hasMoreTokens()) {
                      sysAdminTableAccessOutput += "<th>" + escapeHtml(columnTitles.nextToken()) + "</th>";
                    }
                  }
                  else {
                    //* when an asterisk (*) or splat is entered
                    ResultSetMetaData rsmd = rs.getMetaData();
                    columnTitleSize = rsmd.getColumnCount();
                    for (int c = 1; c <= columnTitleSize; c++) {
                      sysAdminTableAccessOutput += "<th>" + escapeHtml(rsmd.getColumnName(c)) + "</th>";
                    }
                  }
                  sysAdminTableAccessOutput += "</tr>";
                  hasRows = true;
                }
                sysAdminTableAccessOutput += "<tr>";
                for (int i = 1; i <= columnTitleSize; i++) {
                  sysAdminTableAccessOutput += "<td>" + escapeHtml(rs.getString(i)) + "</td>";
                }
                sysAdminTableAccessOutput += "</tr>";
                r++;
              }
            }

            if (!hasRows) {
              sysAdminTableAccessOutput += "<tr><td>There are no rows</td></tr>";
            }
          }
          //* end building the table for output
          sysAdminTableAccessOutput += "</table>";

          if (rs != null)
            rs.close();
        }
        else {
          // say we had an error
          JSFUtils.addFacesErrorMessage("SESSION ERROR", "Invalid Module State.");
        }
      }
      catch (Exception e) {
        sysAdminTableAccessOutput = "ERROR: " + e.getMessage();
        log.trace(e.getLocalizedMessage());
      }
      finally {
        if (rs != null) { try { rs.close(); } catch (SQLException e) { rs = null; } }
        if (pstmt != null) { try { pstmt.close(); } catch (SQLException e) { pstmt = null; } }
      }
    }
  }

  public void setSysAdminTableAccessOutput(String output) {
    sysAdminTableAccessOutput = output;
  }

  public String getSysAdminTableAccessOutput() {
    return sysAdminTableAccessOutput;
  }

  @SuppressWarnings("unused") //called from Admin_SystemAdmin.jspx
  public void systemAdminTabChange(DisclosureEvent event) {
    // see what page we are on
    String myid = event.getComponent().getId();

    if (myid.equals("KillActiveSession")) {
      DCIteratorBinding useriterator = ADFUtils.findIterator("UsersView1Iterator");
      // refresh the view
      useriterator.executeQuery();
    }
    else if (myid.equals("SetupSystemDefaults")) {
      super.logDbPoolInfo();

      SysAdminImpl Service = getSysAdminModule();
      String retVal = Service.getConfigValue("MHIF_AUTORUN");
      if (retVal.equals("Y")) {
        MHIFAutorun.setValue("Y");
      }
      else {
        MHIFAutorun.setValue("N");
      }

      String classColor = Service.getConfigValue("STRATIS_CLASS_COLOR");
      String classText = Service.getConfigValue("STRATIS_CLASS_TEXT");
      classificationColor.resetValue();
      if (classColor != null && Util.isNotEmpty(classColor)) {
        classificationColor.setValue(classColor);
      }
      else {
        classificationColor.setValue("green");
      }
      classificationText.resetValue();
      if (classText != null && Util.isNotEmpty(classText)) {
        classificationText.setValue(classText);
      }
      else {
        classificationText.setValue("UNCLASSIFIED, FOR OFFICIAL USE ONLY");
      }
    }
  }

  public boolean isActiveRun(String interface_name) {
    String active = PadUtil.padItZeros(Long.toBinaryString(Long.parseLong(getActiveRuns().toString())), 5);
    log.debug("isActiveRun {} -  {}", interface_name, active);
    if (interface_name.equals("DASF"))
      return (active.charAt(0) == '1');
    if (interface_name.equals("GABF"))
      return (active.charAt(1) == '1');
    if (interface_name.equals("GBOF"))
      return (active.charAt(2) == '1');
    if (interface_name.equals("MATS"))
      return (active.charAt(3) == '1');
    if (interface_name.equals("MHIF"))
      return (active.charAt(4) == '1');

    return false;
  }

  public void activateRun(String interface_name) {
    long active = Long.parseLong(getActiveRuns().toString());
    if (interface_name.equals("DASF")) {
      activeRuns = active | 0X10L;
    }
    else if (interface_name.equals("GABF")) {
      activeRuns = active | 0X8L;
    }
    else if (interface_name.equals("GBOF")) {
      activeRuns = active | 0X4L;
    }
    else if (interface_name.equals("MATS")) {
      activeRuns = active | 0X2L;
    }
    else if (interface_name.equals("MHIF")) {
      activeRuns = active | 0X1L;
    }
    setActiveRuns(activeRuns);
  }

  private Object getActiveRuns() {

    if (activeRuns == null)
      setActiveRuns(0X0L);
    return this.activeRuns;
  }

  public String getClassificationString() {
    SysAdminImpl Service = getSysAdminModule();
    String classText = Service.getConfigValue("STRATIS_CLASS_TEXT");
    if (classText == null || Util.isEmpty(classText)) {
      classText = "UNCLASSIFIED, FOR OFFICIAL USE ONLY ";
    }
    return classText;
  }

  public String getClassificationBackgroundColor() {
    SysAdminImpl Service = getSysAdminModule();
    String classColor = Service.getConfigValue("STRATIS_CLASS_COLOR");
    if (classColor == null || Util.isEmpty(classColor)) {
      classColor = "green ";
    }
    return classColor;
  }

  private void setActiveRuns(Object activeRuns) {
    this.activeRuns = activeRuns;
  }

  public void setLoggedinuserTable(RichTable loggedinuserTable) {
    this.loggedinuserTable = loggedinuserTable;
  }

  public RichTable getLoggedinuserTable() {
    return loggedinuserTable;
  }

  public void setInterfaceimportbutton(RichButton interfaceimportbutton) {
    this.interfaceimportbutton = interfaceimportbutton;
  }

  public RichButton getInterfaceimportbutton() {
    return interfaceimportbutton;
  }

  public void setInterfacetransmitbutton(RichButton interfacetransmitbutton) {
    this.interfacetransmitbutton = interfacetransmitbutton;
  }

  public RichButton getInterfacetransmitbutton() {
    return interfacetransmitbutton;
  }

  public void setInterfacePollMessage(RichOutputText interfacePollMessage) {
    this.interfacePollMessage = interfacePollMessage;
  }

  public RichOutputText getInterfacePollMessage() {
    return interfacePollMessage;
  }

  public void setInterfaceTable(RichTable interfaceTable) {
    this.interfaceTable = interfaceTable;
  }

  public RichTable getInterfaceTable() {
    return interfaceTable;
  }

  public Object getActiveRuns1() {
    return activeRuns;
  }

  public void setSysAdminTableAccessColumn(RichInputText sysAdminTableAccessColumn) {
    this.sysAdminTableAccessColumn = sysAdminTableAccessColumn;
  }

  public RichInputText getSysAdminTableAccessColumn() {
    return sysAdminTableAccessColumn;
  }

  public void setSysAdminTableAccessWhere(RichInputText sysAdminTableAccessWhere) {
    this.sysAdminTableAccessWhere = sysAdminTableAccessWhere;
  }

  public RichInputText getSysAdminTableAccessWhere() {
    return sysAdminTableAccessWhere;
  }

  public void setSysAdminTableAccessCommand(RichSelectOneChoice sysAdminTableAccessCommand) {
    this.sysAdminTableAccessCommand = sysAdminTableAccessCommand;
  }

  public RichSelectOneChoice getSysAdminTableAccessCommand() {
    return sysAdminTableAccessCommand;
  }

  public void setSysAdminTableAccessTable(RichSelectOneChoice sysAdminTableAccessTable) {
    this.sysAdminTableAccessTable = sysAdminTableAccessTable;
  }

  public RichSelectOneChoice getSysAdminTableAccessTable() {
    return sysAdminTableAccessTable;
  }

  public void setDeleteiterator(DCIteratorBinding deleteiterator) {
    this.deleteiterator = deleteiterator;
  }

  public DCIteratorBinding getDeleteiterator() {
    return deleteiterator;
  }

  public void setCheckDeleteType(Object CheckDeleteType) {
    this.CheckDeleteType = CheckDeleteType;
  }

  public Object getCheckDeleteType() {
    return CheckDeleteType;
  }

  public void setDeleteRedirect(String DeleteRedirect) {
    this.DeleteRedirect = DeleteRedirect;
  }

  public String getDeleteRedirect() {
    return DeleteRedirect;
  }

  public void setMHIFAutorun(RichSelectOneChoice MHIFAutorun) {
    this.MHIFAutorun = MHIFAutorun;
  }

  public RichSelectOneChoice getMHIFAutorun() {
    return MHIFAutorun;
  }

  public void setClassificationColor(RichSelectOneChoice ClassificationColor) {
    this.classificationColor = ClassificationColor;
  }

  public RichSelectOneChoice getClassificationColor() {
    return classificationColor;
  }

  public void setClassificationText(RichInputText ClassificationText) {
    this.classificationText = ClassificationText;
  }

  public RichInputText getClassificationText() {
    return classificationText;
  }

  public void setPollStatus(RichInputText pollStatus) {
    this.pollStatus = pollStatus;
  }

  public RichInputText getPollStatus() {
    return pollStatus;
  }

  public void userAdminTabChange(DisclosureEvent disclosureEvent) {
    String myid = disclosureEvent.getComponent().getId();

    if (myid.equals("modgrp")) {
      log.trace("modgrp");
    }
    if (myid.equals("moduser")) {
      log.trace("moduser");
      DCIteratorBinding useriterator = ADFUtils.findIterator("UsersView1Iterator");
      // refresh the view
      useriterator.executeQuery();
    }
  }
}
