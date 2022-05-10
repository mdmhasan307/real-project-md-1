package mil.stratis.view.admin;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mil.stratis.view.util.JSFUtils;
import oracle.adf.view.rich.component.rich.RichPopup;
import oracle.adf.view.rich.component.rich.data.RichTable;
import oracle.adf.view.rich.component.rich.input.RichInputText;
import oracle.adf.view.rich.component.rich.input.RichSelectOneChoice;
import oracle.adf.view.rich.event.DialogEvent;

import javax.faces.event.ActionEvent;

@Slf4j
@NoArgsConstructor
public class AdminSetupBacking extends AdminBackingHandler {

  private boolean siteRemoteConnFlag = true;

  private transient RichTable connectionTable;
  private transient RichInputText hostName;
  private transient RichInputText ipAddress;
  private transient RichInputText port;
  private transient RichInputText username;
  private transient RichInputText password;
  private transient RichInputText hostDirectory;
  private transient RichInputText localDirectory;
  private transient RichInputText certificatePath;
  private transient RichSelectOneChoice protocolList;
  private transient RichSelectOneChoice certificateFlagList;
  
  /**
   * Set site remote connections flag back on (true) to turn on readonly+style
   * and hide certain ui buttons on Admin Setup.
   */
  public void setSiteRemoteConnFlag(boolean show) {
    this.siteRemoteConnFlag = show;
  }

  public boolean getSiteRemoteConnFlag() {
    return siteRemoteConnFlag;
  }

  private static String connectionIterator = "SiteRemoteConnectionsView1Iterator";

  public void submitResetSiteConnection(ActionEvent event) {
    if (event != null) {
      //NO-OP can't remove parameter due to adf
    }
    resetKeepPosition(connectionIterator);
    setSiteRemoteConnFlag(false);
  }

  public void dialogListener(DialogEvent dialogEvent) {
    if (dialogEvent.getOutcome().equals(DialogEvent.Outcome.ok)) {
      RichPopup popup;
      popup = (RichPopup) JSFUtils.findComponentInRoot("confirmDelete");
      popup.hide();
    }
  }

  public void setConnectionTable(RichTable connectionTable) {
    this.connectionTable = connectionTable;
  }

  public boolean isSiteRemoteConnFlag() {
    return siteRemoteConnFlag;
  }

  public RichTable getConnectionTable() {
    return connectionTable;
  }

  public void setHostName(RichInputText hostName) {
    this.hostName = hostName;
  }

  public RichInputText getHostName() {
    return hostName;
  }

  public void setIpAddress(RichInputText ipAddress) {
    this.ipAddress = ipAddress;
  }

  public RichInputText getIpAddress() {
    return ipAddress;
  }

  public void setPort(RichInputText port) {
    this.port = port;
  }

  public RichInputText getPort() {
    return port;
  }

  public void setUsername(RichInputText username) {
    this.username = username;
  }

  public RichInputText getUsername() {
    return username;
  }

  public void setPassword(RichInputText password) {
    this.password = password;
  }

  public RichInputText getPassword() {
    return password;
  }

  public void setHostDirectory(RichInputText hostDirectory) {
    this.hostDirectory = hostDirectory;
  }

  public RichInputText getHostDirectory() {
    return hostDirectory;
  }

  public void setLocalDirectory(RichInputText localDirectory) {
    this.localDirectory = localDirectory;
  }

  public RichInputText getLocalDirectory() {
    return localDirectory;
  }

  public void setCertificatePath(RichInputText certificatePath) {
    this.certificatePath = certificatePath;
  }

  public RichInputText getCertificatePath() {
    return certificatePath;
  }

  public void setProtocolList(RichSelectOneChoice protocolList) {
    this.protocolList = protocolList;
  }

  public RichSelectOneChoice getProtocolList() {
    return protocolList;
  }

  public void setCertificateFlagList(RichSelectOneChoice certificateFlagList) {
    this.certificateFlagList = certificateFlagList;
  }

  public RichSelectOneChoice getCertificateFlagList() {
    return certificateFlagList;
  }

  public static void setConnectionIterator(String connectionIterator) {
    AdminSetupBacking.connectionIterator = connectionIterator;
  }

  public static String getConnectionIterator() {
    return connectionIterator;
  }
}
