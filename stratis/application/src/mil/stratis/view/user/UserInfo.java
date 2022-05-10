package mil.stratis.view.user;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.common.util.JNDIUtils;
import mil.stratis.common.util.Util;
import mil.stratis.model.datatype.user.UserRights;
import mil.stratis.model.services.LoginModuleImpl;
import mil.stratis.model.services.WarehouseSetupImpl;
import mil.stratis.view.BackingHandler;
import mil.stratis.view.util.ADFUtils;
import mil.stratis.view.util.JSFUtils;
import mil.usmc.mls2.stratis.common.domain.model.LabelType;
import mil.usmc.mls2.stratis.common.model.enumeration.AuditLogTypeEnum;
import mil.usmc.mls2.stratis.core.domain.event.AdminPageAccessEvent;
import mil.usmc.mls2.stratis.core.domain.event.WorkstationPageAccessFailureEvent;
import mil.usmc.mls2.stratis.core.domain.model.Equipment;
import mil.usmc.mls2.stratis.core.service.EventService;
import mil.usmc.mls2.stratis.core.service.SiteInfoService;
import mil.usmc.mls2.stratis.core.utility.AdfLogUtility;
import mil.usmc.mls2.stratis.core.utility.BarcodeUtils;
import mil.usmc.mls2.stratis.core.utility.ContextUtils;
import mil.usmc.mls2.stratis.core.utility.DateConstants;
import net.jawr.web.util.StringUtils;
import oracle.adf.model.binding.DCIteratorBinding;
import oracle.adf.view.rich.component.rich.input.RichInputText;
import oracle.jbo.Row;
import oracle.jbo.ViewObject;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.myfaces.trinidad.context.RequestContext;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.StringTokenizer;

@Slf4j
public class UserInfo extends BackingHandler implements Serializable {

  //following string is ZPL (Zebra Print Language) for a sample label.
  private static final String SAMPLE_ZPL_LABEL = "^XA\\n^PRC\\n^LH0,0^FS\\n^LL609\\n^MD0\\n^MNN\\n^LH0,0^FS\\n^CWI,510W8ZHX.FNT^FS\\n^FO67,35^AIN,29,0^CI0^FR^FDSTRATIS MECH (H)   ISSUE:  CAMP PENDELTON^FS\\n^FO67,66^AIN,29,0^CI0^FR^FDPACKING STATION:  CPCX012251^FS\\n^FO67,97^AIN,29,0^CI0^FR^FDFSC/NIIN:  1290009049375   CC:  A^FS\\n^FO67,129^AIN,29,0^CI0^FR^FDDOC NO:  M2832771297761    SCN:  000000LP^FS\\n^FO67,160^AIN,29,0^CI0^FR^FDPIN:  000000LP    PIN QTY:  20^FS\\n^FO67,191^AIN,29,0^CI0^FR^FDUSER NAME:  stratis    DATE/TIME:  20071001/1013^FS\\n^FO67,222^AIN,29,0^CI0^FR^FDDESC:  ^FS\\n^BY2,3.0^FO281,322^B3N,N,202,Y,N^FR^FD000000LP^FS\\n^PQ1,0,0,N\\n^XZ";

  public void setBarcodeinput(RichInputText barcodeinput) {
    this.barcodeinput = barcodeinput;
  }

  public RichInputText getBarcodeinput() {
    return barcodeinput;
  }

  public String[] getHtmlPrintData() {
    return htmlPrintData;
  }

  public void setHtmlPrintData(String[] htmlPrintData) {
    this.htmlPrintData = htmlPrintData;
  }

  public LabelType getHtmlPrintType() {
    return htmlPrintType;
  }

  public void setHtmlPrintType(LabelType htmlPrintType) {
    this.htmlPrintType = htmlPrintType;
  }

  // Tier 1 tabs (main navigation)

  protected enum TABS_TOP {
    HOME(0),
    ADMIN(7),
    RECEIVING(1),
    STOWING(2),
    PICKING(3),
    PACKING(4),
    SHIPPING(5),
    INVENTORY(6),
    REPORTS(8),
    REPRINT(9),
    USER_PREF(10),
    WALKTHRU(11);
    private final int value;

    TABS_TOP(int v) { value = v; }

    protected int value() { return value; }
  }

  protected enum TABS_HOME {
    HOME(0),
    DOD_USER_RESPONSIBILITIES(1),
    DOD_RULES_OF_BEHAVIOR(2),
    DOD_WARNING_BANNER(3);
    private final int value;

    TABS_HOME(int v) { value = v; }

    protected int value() { return value; }
  }

  // Tier 2
  protected enum TABS_ADMIN {
    SYSTEM_SETUP(0),
    WAREHOUSE_SETUP(1),
    WARHOUSE_MANAGEMENT(2),
    USER_MANAGEMENT(3),
    DEPLOYMENT(4),
    EXCEPTION_PROCESSING(5),
    INVENTORY_MANAGEMENT(6),
    INTERFACES(7),
    SYSTEM_ADMIN(8),
    SYSTEM_ADMIN_EXPORTS(9);
    private final int value;

    TABS_ADMIN(int v) { value = v; }

    protected int value() { return value; }
  }

  // Tier 3
  protected enum TABS_WAREHOUSE_SETUP {
    HOME(0),
    STORAGE_BIN(1),
    DIVIDER_TYPE(3),
    WAC(4),
    BUILDING(5),
    MECHANIZED_LOCATION(6),
    NON_MECHANIZED_LOCATION(7),
    WORKSTATION(8),
    PORTS(9),
    CONSOLIDATION_TRIWALL(10),
    SHIPPING_FLOOR_LOCATION(11);
    private final int value;

    TABS_WAREHOUSE_SETUP(int v) { value = v; }

    protected int value() { return value; }
  }

  // Tier 3
  protected enum TABS_WAREHOUSE_MANAGEMENT {
    HOME(0),
    NIIN_UPDATE(1),
    NIIN_INQUIRY(2),
    RE_WAREHOUSE(3),
    CHANGE_CC(4),
    CHANGE_LOCATION(5),
    AAC_UPDATE(6),
    WALK_THRU(7),
    ROUTE_RIC_UPDATE(8),
    SHIPPING_ROUTE_UPDATE(9);
    private final int value;

    TABS_WAREHOUSE_MANAGEMENT(int v) { value = v; }

    protected int value() { return value; }
  }

  // Tier 2
  protected enum TABS_RECEIVING {
    GENERAL(0),
    CANCEL_SID(1);
    private final int value;

    TABS_RECEIVING(int v) { value = v; }

    protected int value() { return value; }
  }

  // Tier 2
  protected enum TABS_STOWING {
    NORMAL(0),
    REASSIGN_SID(1);
    private final int value;

    TABS_STOWING(int v) { value = v; }

    protected int value() { return value; }
  }

  protected enum TABS_PICKING {
    NORMAL(0),
    BY_AAC(1),
    BY_ROUTE(2),
    BY_PRIORITY(3),
    BY_WTQ(4);
    private final int value;

    TABS_PICKING(int v) { value = v; }

    protected int value() { return value; }
  }

  protected enum TABS_PACKING {
    NORMAL(0),
    CLOSE_CARTON(1);
    private final int value;

    TABS_PACKING(int v) { value = v; }

    protected int value() { return value; }
  }

  protected enum TABS_SHIPPING {
    ADD_CONTAINER(0),
    REVIEW_CONTENTS(1),
    MANIFEST_SHIPMENT(2),
    ACKNOWLEDGE_SHIPMENT(3),
    REMARK_AAC(4),
    RELOCATE_PALLET(5),
    TRANS_SHIPMENT(6),
    UPDATE_AAC(7);
    private final int value;

    TABS_SHIPPING(int v) { value = v; }

    protected int value() { return value; }
  }

  protected enum TABS_INVENTORY {
    INVENTORY(0),
    LOCATION_SURVEY(1),
    SHELF_LIFE(2);
    private final int value;

    TABS_INVENTORY(int v) { value = v; }

    protected int value() { return value; }
  }

  protected enum TABS_REPORTS {
    VIEW(0),
    ADHOC(1),
    EDIT(2),
    DELETE(3);
    private final int value;

    TABS_REPORTS(int v) { value = v; }

    protected int value() { return value; }
  }

  protected enum TABS_WALKTHRU {
    VIEW(0),
    WALKTHRU(0);
    private final int value;

    TABS_WALKTHRU(int v) { value = v; }

    protected int value() { return value; }
  }

  protected enum TABS_REPRINT {
    REPRINT(0);
    private final int value;

    TABS_REPRINT(int v) { value = v; }

    protected int value() { return value; }
  }

  private static final int USER_INFO = 0;

  // select index is the tier 1 tab (top)
  // selected index 2 is the tier two tab
  // selected index 3 is the tier three tab

  protected int selectedIndex = -1;
  protected int selected2Index = -1;
  protected int selected3Index = -1;

  protected UserRights userRights = null;

  protected String username = "";
  protected String displayName = "";
  protected String previousLogin = "";
  protected String loginKey = "";
  protected String workstationType = "";
  protected String workstationName = "";
  protected String barcodeTest = "";
  protected String forceForwardPage = "";
  protected String barcodeText = "";
  protected String barcodePath = "";
  protected String usertypestring;

  protected String sitename = "";

  protected int workstationId = 0;
  protected int userId = 0;
  protected int userTypeId = 0;

  protected String siteURL = "";
  protected String siteProtocol = "";
  protected String siteServerName = "";
  protected String siteContextPath = "";

  protected int siteServerPort = 0;

  // flag for whether this is a non mech workstation
  protected transient Object nonmech = 0;

  protected transient Object useprintcom = 0;
  protected String printcomstring = SAMPLE_ZPL_LABEL;
  protected String printcomport = "1";

  protected String returncomport = "";
  protected String returncomtimeoutseconds = "";
  protected String returncombaud = "";
  protected String returncomparity = "";
  protected String returncomdatabits = "";  // aka byte size
  protected String returncomstopbits = "";

  protected transient Object useprintcom2 = 0;
  protected String printcomstring2 = SAMPLE_ZPL_LABEL;
  protected String printcomport2 = "1";

  protected String returncomport2 = "";
  protected String returncomtimeoutseconds2 = "";
  protected String returncombaud2 = "";
  protected String returncomparity2 = "";
  protected String returncomdatabits2 = "";  // aka byte size
  protected String returncomstopbits2 = "";

  protected transient boolean useHTMLPrint = false;
  protected transient String[] htmlPrintData = null;
  protected transient LabelType htmlPrintType = LabelType.NONE;

  // object to force the redirection
  protected transient Object forceredirect = 0;

  protected transient RichInputText barcodeinput;

  protected transient Object useprintframe = 0;
  protected String winPrintOrientation = "";
  protected String hison = "";

  protected int frameprintcopies = 1;

  protected String comPrintIndex = "2";
  protected String comCommandIndex = "1";

  protected String specialMessage = " ";
  protected String warningShutdownMessage = "";

  protected String helpURL = "";

  protected String debugMessage = "";

  protected transient Object resetHelpURL;

  private Boolean useCacLogin = null;
  private String stratisVersion = "";
  private String url = "";

  private transient Object cleanDirtyTransaction;

  public UserInfo() {
    workstationKey.put(TABS_TOP.RECEIVING.value, "Receiving");
    workstationKey.put(TABS_TOP.STOWING.value, "Stow");
    workstationKey.put(TABS_TOP.PICKING.value, "Stow");
    workstationKey.put(TABS_TOP.PACKING.value, "Pack");
    workstationKey.put(TABS_TOP.SHIPPING.value, "Ship");
    workstationKey.put(TABS_TOP.INVENTORY.value, "Stow");

    workstationRights.put(TABS_TOP.RECEIVING.value, UserRights.STRATIS_RECEIVE_RIGHTS);
    workstationRights.put(TABS_TOP.STOWING.value, UserRights.STRATIS_STOW_RIGHTS);
    workstationRights.put(TABS_TOP.PICKING.value, UserRights.STRATIS_PICK_RIGHTS);
    workstationRights.put(TABS_TOP.PACKING.value, UserRights.STRATIS_PACK_RIGHTS);
    workstationRights.put(TABS_TOP.SHIPPING.value, UserRights.STRATIS_SHIP_RIGHTS);
    workstationRights.put(TABS_TOP.INVENTORY.value, UserRights.STRATIS_INV_RIGHTS);
  }

  public String getSitename() {
    return sitename;
  }

  public void setSitename(String sitename) {
    this.sitename = sitename;
  }

  public boolean getUseHTMLPrint() {
    return useHTMLPrint;
  }

  public void setUseHTMLPrint(boolean useHTMLPrint) {
    this.useHTMLPrint = useHTMLPrint;
  }

  // get/set user rights
  public void setUserRights(UserRights rights) {
    this.userRights = rights;
  }

  public UserRights getUserRights() {
    return userRights;
  }

  /**
   * Theorey here is going to be that any time we change a tier 1 tab
   * we are going to be clearing the tier 2 and tier 3 sub tab values.
   * They should get set by the page that is calling this if needed.
   */

  public void setSelected(int selected) {
    selectedIndex = selected;
    selected2Index = -1;
    selected3Index = -1;
    log.trace("selected: {}", selected);
  }

  public void setSelected2(int selected) {
    log.trace("selected: {}", selected);
    selected2Index = selected;
  }

  public void setSelected3(int selected) {
    log.trace("selected: {}", selected);
    selected3Index = selected;
  }

  // function to select the home tab
  public void getHomeisSelected() {
    setSelected(TABS_TOP.HOME.value());
  }

  public boolean isHomeSelected() {
    return selectedIndex == TABS_TOP.HOME.value();
  }

  public boolean isNotReceiving() {
    return !isReceiving();
  }

  // function return whether receiving rights are given
  public boolean isReceiving() {
    return !userRights.isReceiving();
  }

  // function to select the receiving tab
  public void getReceivingisSelected() {
    setSelected(1);
  }

  public boolean isReceivingSelected() {
    return selectedIndex == TABS_TOP.RECEIVING.value();
  }

  public boolean isReceivingSelectedCancelSID() {
    return selectedIndex == TABS_TOP.RECEIVING.value();
  }

  public boolean isNotStowing() {
    return !isStowing();
  }

  // function return whether stowing rights are given
  public boolean isStowing() {
    return !userRights.isStowing();
  }

  public boolean isStowingSelected() {
    return selectedIndex == TABS_TOP.STOWING.value();
  }

  public boolean isNotPicking() {
    return !isPicking();
  }

  // function return whether picking rights are given
  public boolean isPicking() {
    return !userRights.isPicking();
  }

  public boolean isPickingSelected() {
    return selectedIndex == TABS_TOP.PICKING.value();
  }

  // function return whether packing rights are given
  public boolean isPacking() {
    return !userRights.isPacking();
  }

  public boolean isPackingSelected() {
    return selectedIndex == TABS_TOP.PACKING.value();
  }

  public boolean isNotShipping() {
    return !isShipping();
  }

  // function return whether shipping rights are given
  public boolean isShipping() {
    return !userRights.isShipping();
  }

  public boolean isShippingSelected() {
    return selectedIndex == TABS_TOP.SHIPPING.value();
  }

  public boolean isNotInventory() {
    return !isInventory();
  }

  // function return whether inventory rights are given
  public boolean isInventory() {
    return !userRights.isInventory();
  }

  // function to return whether the admin tab is selected
  public boolean isInventorySelected() {
    return selectedIndex == TABS_TOP.INVENTORY.value();
  }

  public int getUsersAdminAuthorizationStatus() {
    if (!isLoggedIn()) {
      return 0;
    }
    else if (!isAdmin()) {
      String vid = FacesContext.getCurrentInstance().getViewRoot().getViewId();
      String user = (JSFUtils.getManagedBeanValue("userbean.userId")).toString();
      EventService eventPublisher = ContextUtils.getBean(EventService.class);
      UserInfo userInfo = (UserInfo) JSFUtils.getManagedBeanValue("userbean");
      AdminPageAccessEvent event = AdminPageAccessEvent.builder().page(vid).userInfo(userInfo).result(AuditLogTypeEnum.FAILURE).build();
      eventPublisher.publishEvent(event);
      log.warn("Attempted access to {} by non admin  {}", vid, user);
      return -1;
    }
    else {
      //user is logged in, and they have STRATIS_ADMIN_RIGHTS
      return 1;
    }
  }

  private final Map<Integer, String> workstationKey = new HashedMap();
  private final Map<Integer, Long> workstationRights = new HashedMap();
  private final long[][] receivingPagePermissions = {{}, // home
      {userRights.STRATIS_RECEIVE_RIGHTS_GENERAL, userRights.STRATIS_RECEIVE_RIGHTS_CANCELSID}, //receiving
      {UserRights.STRATIS_STOW_RIGHTS_NORMAL, UserRights.STRATIS_REASSIGN_SID_RIGHTS}, //stowing
      {UserRights.STRATIS_PICK_RIGHTS_NORMAL, UserRights.STRATIS_PICK_RIGHTS_AAC, UserRights.STRATIS_PICK_RIGHTS_ROUTE, UserRights.STRATIS_PICK_RIGHTS_PRIORITY, UserRights.STRATIS_PICK_RIGHTS_WTQ}, //picking
      {UserRights.STRATIS_PACK_RIGHTS_NORMAL, UserRights.STRATIS_PACK_RIGHTS_CLOSE}, //packing
      {UserRights.STRATIS_SHIP_RIGHTS_ADD_CONTAINER, UserRights.STRATIS_SHIP_RIGHTS_REVIEW_CONTENTS, UserRights.STRATIS_SHIP_RIGHTS_MANIFEST_SHIPMENT, UserRights.STRATIS_SHIP_RIGHTS_ACKNOWLEDGE_SHIP, UserRights.STRATIS_SHIP_RIGHTS_REMARK_AAC, UserRights.STRATIS_SHIP_RIGHTS_RELOCATE_PALLET, UserRights.STRATIS_SHIP_RIGHTS_TRANSSHIPMENT, UserRights.STRATIS_SHIP_RIGHTS_UPDATE_AAC}, //shipping
      {UserRights.STRATIS_INV_RIGHTS_INVENTORY, UserRights.STRATIS_INV_RIGHTS_LOCSURVAY, UserRights.STRATIS_INV_RIGHTS_INVENTORY} //INVENTORY
  };

  public int getUserWorkstationAccess() {

    if (!isLoggedIn()) {
      return -1;
    }

    if (!workstationRights.containsKey(selectedIndex)) {
      //pages all can access
      return 1;
    }

    if (isAdmin() && selectedIndex != TABS_TOP.RECEIVING.value && selectedIndex != TABS_TOP.ADMIN.value) {
      //block admin from accessing any pages other than admin and receiving. Other allowed ones checked above
      logWSPageAccessFailure(WorkstationPageAccessFailureEvent.FailureType.ADMIN);
      return 0;
    }
    WorkstationPageAccessFailureEvent.FailureType failureType;

    if (workstationType.contains(workstationKey.get(selectedIndex)) &&
        ((userRights.getUsersRights() & workstationRights.get(selectedIndex)) > 0)) {
      //can view the tab now check the page a -1 page is a home page that the user has rights to if they can view the tab
      if (selected2Index == -1 || (userRights.getUsersRights() & receivingPagePermissions[selectedIndex][selected2Index]) > 0) {
        //can view page
        return 1;
      }
      else {
        failureType = WorkstationPageAccessFailureEvent.FailureType.PERMISSION;
      }
    }
    else {
      failureType = WorkstationPageAccessFailureEvent.FailureType.WORKSTATION;
    }
    logWSPageAccessFailure(failureType);
    return 0;
  }

  private void logWSPageAccessFailure(WorkstationPageAccessFailureEvent.FailureType type) {
    String vid = FacesContext.getCurrentInstance().getViewRoot().getViewId();
    EventService eventPublisher = ContextUtils.getBean(EventService.class);
    UserInfo userInfo = (UserInfo) JSFUtils.getManagedBeanValue("userbean");
    WorkstationPageAccessFailureEvent event = WorkstationPageAccessFailureEvent.builder().page(vid).userInfo(userInfo).result(type).build();
    eventPublisher.publishEvent(event);
  }

  // function return whether admin rights are given
  public boolean isAdmin() {
    // check rights
    return userRights.isAdmin();
  }

  // function return whether the admin tab is disabled in the menu
  public boolean isAdminDisabled() {
    return !userRights.isAdmin();
  }

  // function to return whether the admin tab is selected
  public boolean isAdminSelected() {
    return selectedIndex == TABS_TOP.ADMIN.value();
  }

  // function return whether reports rights are given
  public boolean isReports() {
    return !userRights.isReports();
  }

  public boolean isReportsSelected() {
    return selectedIndex == TABS_TOP.REPORTS.value();
  }

  // function to return whether print rights are given
  public boolean isPrint() {
    return !userRights.isPrint();
  }

  // function to return whether the print tab is selected
  public boolean isPrintSelected() {
    return selectedIndex == TABS_TOP.REPRINT.value();
  }

  // function to return whether user preferences was selected
  public boolean isUserPrefSelected() {
    return selectedIndex == TABS_TOP.USER_PREF.value();
  }

  // function return whether walkthru rights are given
  public boolean isWalkThru() {
    return !userRights.isWalkThru();
  }

  // function to return whether the walkthru tab is selected
  public boolean isWalkThruSelected() {
    return selectedIndex == TABS_TOP.WALKTHRU.value();
  }

  public boolean isReceiving_General() {
    return !userRights.isReceivingGeneral();
  }

  public boolean isReceiving_General_Selected() {
    return selected2Index == TABS_RECEIVING.GENERAL.value();
  }

  public boolean isReceiving_CancelSID() {
    return !userRights.isReceivingCancelSID();
  }

  public boolean isReceiving_CancelSID_Selected() {
    return selected2Index == TABS_RECEIVING.CANCEL_SID.value();
  }

  public boolean isStowing_Normal() {
    return !userRights.isStowingNormal();
  }

  public boolean isStowing_Normal_Selected() {
    return selected2Index == TABS_STOWING.NORMAL.value();
  }

  public boolean isStowing_ReassignSid() {
    return !userRights.isStowingReassignSid();
  }

  public boolean isStowing_ReassignSid_Selected() {
    return selected2Index == TABS_STOWING.REASSIGN_SID.value();
  }

  public boolean isPicking_Normal() {
    return !userRights.isPickingNormal();
  }

  public boolean isPicking_Normal_Selected() {
    return selected2Index == TABS_PICKING.NORMAL.value();
  }

  public boolean isPicking_AAC() {
    return !userRights.isPickingAAC();
  }

  public boolean isPicking_AAC_Selected() {
    return selected2Index == TABS_PICKING.BY_AAC.value();
  }

  public boolean isPicking_Route() {
    return !userRights.isPickingRoute();
  }

  public boolean isPicking_Route_Selected() {
    return selected2Index == TABS_PICKING.BY_ROUTE.value();
  }

  public boolean isPicking_Priority() {
    return !userRights.isPickingPriority();
  }

  public boolean isPicking_Priority_Selected() {
    return selected2Index == TABS_PICKING.BY_PRIORITY.value();
  }

  public boolean isPicking_Wtq() {
    return !userRights.isPickingWTQ();
  }

  public boolean isPicking_Wtq_Selected() {
    return selected2Index == TABS_PICKING.BY_WTQ.value();
  }

  public boolean isPacking_Normal() {
    return !userRights.isPackingNormal();
  }

  public boolean isPacking_Normal_Selected() {
    return selected2Index == TABS_PACKING.NORMAL.value();
  }

  public boolean isPacking_Close() {
    return !userRights.isPackingClose();
  }

  public boolean isPacking_Close_Selected() {
    return selected2Index == TABS_PACKING.CLOSE_CARTON.value();
  }

  public boolean isShipping_AddContainer() {
    return !userRights.isShippingAddContainer();
  }

  public boolean isShipping_AddContainer_Selected() {
    return selected2Index == TABS_SHIPPING.ADD_CONTAINER.value();
  }

  public boolean isShipping_ReviewContents() {
    return !userRights.isShippingReviewContents();
  }

  public boolean isShipping_ReviewContents_Selected() {
    return selected2Index == TABS_SHIPPING.REVIEW_CONTENTS.value();
  }

  public boolean isShipping_ManifestShipment() {
    return !userRights.isShippingManifestShipment();
  }

  public boolean isShipping_ManifestShipment_Selected() {
    return selected2Index == TABS_SHIPPING.MANIFEST_SHIPMENT.value();
  }

  public boolean isShipping_AcknowledgeShip() {
    return !userRights.isShippingAcknowledgeShip();
  }

  public boolean isShipping_AcknowledgeShip_Selected() {
    return selected2Index == TABS_SHIPPING.ACKNOWLEDGE_SHIPMENT.value();
  }

  public boolean isShipping_Remark_AAC() {
    return !userRights.isShippingRemarkAAC();
  }

  public boolean isShipping_Remark_AAC_Selected() {
    return selected2Index == TABS_SHIPPING.REMARK_AAC.value();
  }

  public boolean isShipping_Relocate_Pallet() {
    return !userRights.isShippingRelocatePallet();
  }

  public boolean isShipping_Relocate_Pallet_Selected() {
    return selected2Index == TABS_SHIPPING.RELOCATE_PALLET.value();
  }

  public boolean isShipping_Transshipment() {
    return !userRights.isShippingTransshipment();
  }

  public boolean isShipping_Transshipment_Selected() {
    return selected2Index == TABS_SHIPPING.TRANS_SHIPMENT.value();
  }

  public boolean isShipping_Update_AAC() {
    return !userRights.isShippingUpdateAAC();
  }

  public boolean isShipping_Update_AAC_Selected() {
    return selected2Index == TABS_SHIPPING.UPDATE_AAC.value();
  }

  public boolean isInv_Inventory() {
    return !userRights.isInvInventory();
  }

  public boolean isInv_Inventory_Selected() {
    return selected2Index == TABS_INVENTORY.INVENTORY.value();
  }

  public boolean isInv_LocSurvay() {
    return !userRights.isInvLocSurvay();
  }

  public boolean isInv_LocSurvay_Selected() {
    return selected2Index == TABS_INVENTORY.LOCATION_SURVEY.value();
  }

  public boolean isInv_ShelfLife() {
    return !userRights.isInvShelfLife();
  }

  public boolean isInv_ShelfLife_Selected() {
    return selected2Index == TABS_INVENTORY.SHELF_LIFE.value();
  }

  public boolean isAdmin_SystemSetup() {
    return !userRights.isAdminSystemSetup();
  }

  public boolean isAdmin_SystemSetup_Selected() {
    return selected2Index == TABS_ADMIN.SYSTEM_SETUP.value();
  }

  public boolean isAdmin_WarehouseSetup() {
    return !userRights.isAdminWarehouseSetup();
  }

  public boolean isAdmin_WarehouseSetup_Selected() {
    return selected2Index == TABS_ADMIN.WAREHOUSE_SETUP.value();
  }

  public boolean isAdmin_WS_Home_Selected() {
    return selected3Index == TABS_WAREHOUSE_SETUP.HOME.value();
  }

  public boolean isAdmin_WS_StorageBin_Selected() {
    return selected3Index == TABS_WAREHOUSE_SETUP.STORAGE_BIN.value();
  }

  public boolean isAdmin_WS_DividerType_Selected() {
    return selected3Index == TABS_WAREHOUSE_SETUP.DIVIDER_TYPE.value();
  }

  public boolean isAdmin_WS_WAC_Selected() {
    return selected3Index == TABS_WAREHOUSE_SETUP.WAC.value();
  }

  public boolean isAdmin_WS_Building_Selected() {
    return selected3Index == TABS_WAREHOUSE_SETUP.BUILDING.value();
  }

  public boolean isAdmin_WS_MechanizedLocation_Selected() {
    return selected3Index == TABS_WAREHOUSE_SETUP.MECHANIZED_LOCATION.value();
  }

  public boolean isAdmin_WS_NonMechanizedLocation_Selected() {
    return selected3Index == TABS_WAREHOUSE_SETUP.NON_MECHANIZED_LOCATION.value();
  }

  public boolean isAdmin_WS_Workstation_Selected() {
    return selected3Index == TABS_WAREHOUSE_SETUP.WORKSTATION.value();
  }

  public boolean isAdmin_WS_Ports_Selected() {
    return selected3Index == TABS_WAREHOUSE_SETUP.PORTS.value();
  }

  public boolean isAdmin_WS_ConsolidationTriwall_Selected() {
    return selected3Index == TABS_WAREHOUSE_SETUP.CONSOLIDATION_TRIWALL.value();
  }

  public boolean isAdmin_WS_ShippingFloorLocation_Selected() {
    return selected3Index == TABS_WAREHOUSE_SETUP.SHIPPING_FLOOR_LOCATION.value();
  }

  public boolean isAdmin_WarehouseManage() {
    return !userRights.isAdminWarehouseManage();
  }

  public boolean isAdmin_WarehouseManage_Selected() {
    return selected2Index == TABS_ADMIN.WARHOUSE_MANAGEMENT.value();
  }

  public boolean isAdmin_WM_Home_Selected() {
    return selected3Index == TABS_WAREHOUSE_MANAGEMENT.HOME.value();
  }

  public boolean isAdmin_WM_NIINUpdate() {
    return !userRights.isAdminWMNIINUpdate();
  }

  public boolean isAdmin_WM_NIINUpdate_Selected() {
    return selected3Index == TABS_WAREHOUSE_MANAGEMENT.NIIN_UPDATE.value();
  }

  public boolean isAdmin_WM_NIINInquiry() {
    return !userRights.isAdminWMNIINInquiry();
  }

  public boolean isAdmin_WM_NIINInquiry_Selected() {
    return selected3Index == TABS_WAREHOUSE_MANAGEMENT.NIIN_INQUIRY.value();
  }

  public boolean isAdmin_WM_Rewarehouse() {
    return !userRights.isAdminWMRewarehouse();
  }

  public boolean isAdmin_WM_Rewarehouse_Selected() {
    return selected3Index == TABS_WAREHOUSE_MANAGEMENT.RE_WAREHOUSE.value();
  }

  public boolean isAdmin_WM_ChangeCC() {
    return !userRights.isAdminWMChangeCC();
  }

  public boolean isAdmin_WM_ChangeCC_Selected() {
    return selected3Index == TABS_WAREHOUSE_MANAGEMENT.CHANGE_CC.value();
  }

  public boolean isAdmin_WM_ChangeLocation() {
    return !userRights.isAdminWMChangeLocation();
  }

  public boolean isAdmin_WM_ChangeLocation_Selected() {
    return selected3Index == TABS_WAREHOUSE_MANAGEMENT.CHANGE_LOCATION.value();
  }

  public boolean isAdmin_WM_AACUpdate() {
    return !userRights.isAdminWMAACUpdate();
  }

  public boolean isAdmin_WM_AACUpdate_Selected() {
    return selected3Index == TABS_WAREHOUSE_MANAGEMENT.AAC_UPDATE.value();
  }

  public boolean isAdmin_WM_WalkThru() {
    return !userRights.isAdminWMWalkThru();
  }

  public boolean isAdmin_WM_WalkThru_Selected() {
    return selected3Index == TABS_WAREHOUSE_MANAGEMENT.WALK_THRU.value();
  }

  public boolean isAdmin_WM_RICUpdate() {
    return !userRights.isAdminWMRICUpdate();
  }

  public boolean isAdmin_WM_RICUpdate_Selected() {
    return selected3Index == TABS_WAREHOUSE_MANAGEMENT.ROUTE_RIC_UPDATE.value();
  }

  public boolean isAdmin_WM_ShipRouteUpdate() {
    return !userRights.isAdminWMShipRouteUpdate();
  }

  public boolean isAdmin_WM_ShipRouteUpdate_Selected() {
    return selected3Index == TABS_WAREHOUSE_MANAGEMENT.SHIPPING_ROUTE_UPDATE.value();
  }

  public boolean isAdmin_UserManage() {
    return !userRights.isAdminUserManage();
  }

  public boolean isAdmin_UserManage_Selected() {
    return selected2Index == TABS_ADMIN.USER_MANAGEMENT.value();
  }

  public boolean isAdmin_Deployment() {
    return !userRights.isAdminDeployment();
  }

  public boolean isAdmin_Deployment_Selected() {
    return selected2Index == TABS_ADMIN.DEPLOYMENT.value();
  }

  public boolean isAdmin_ExceptionHandle() {
    return !userRights.isAdminExceptionHandle();
  }

  public boolean isAdmin_ExceptionHandle_Selected() {
    return selected2Index == TABS_ADMIN.EXCEPTION_PROCESSING.value();
  }

  public boolean isAdmin_InventoryManage() {
    return !userRights.isAdminInventoryManage();
  }

  public boolean isAdmin_InventoryManage_Selected() {
    return selected2Index == TABS_ADMIN.INVENTORY_MANAGEMENT.value();
  }

  public boolean isAdmin_INVM_ReviewRelease() {
    return !userRights.isAdminINVMReviewRelease();
  }

  public boolean isAdmin_INVM_Schedule() {
    return !userRights.isAdminINVMSchedule();
  }

  public boolean isAdmin_SystemAdmin() {
    return !userRights.isAdminSystemAdmin();
  }

  public boolean isAdmin_SystemAdmin_Selected() {
    return selected2Index == TABS_ADMIN.SYSTEM_ADMIN.value();
  }

  public boolean isAdmin_SystemAdminInterfaces() {
    return !userRights.isAdminSystemAdminInterfaces();
  }

  public boolean isAdmin_SystemAdminExports() {
    return !userRights.isAdminSystemAdminExports();
  }

  public boolean isAdmin_SystemAdminInterfaces_Selected() {
    return selected2Index == TABS_ADMIN.INTERFACES.value();
  }

  public boolean isAdmin_SystemAdminExports_Selected() {
    return selected2Index == TABS_ADMIN.SYSTEM_ADMIN_EXPORTS.value();
  }

  public boolean isReports_View() {
    return !userRights.isReportsView();
  }

  public boolean isReports_View_Selected() {
    return selected2Index == TABS_REPORTS.VIEW.value();
  }

  public boolean isReports_Add() {
    return !userRights.isReportsAdd();
  }

  public boolean isReports_Add_Selected() {
    return selected2Index == TABS_REPORTS.ADHOC.value();
  }

  public boolean isReports_Edit() {
    return !userRights.isReportsEdit();
  }

  public boolean isReports_Edit_Selected() {
    return selected2Index == TABS_REPORTS.VIEW.value();
  }

  public boolean isReports_Delete() {
    return !userRights.isReportsDelete();
  }

  public boolean isReports_Delete_Selected() {
    return selected2Index == TABS_REPORTS.DELETE.value();
  }

  public boolean isUserInfoSelected() {
    return selected2Index == USER_INFO;
  }

  // show welcome because nothing is selected
  public boolean isLoginScreen() {
    return (userRights.getUsersRights() == 0);
  }

  // whether to show the logout item
  public boolean isLoggedIn() {
    return (userRights.getUsersRights() != 0);
  }

  // if this is an admin level user
  public boolean isAdminLevelUser() {
    return userRights.isAdminLevelUser();
  }

  public void setWorkstationId(int workstationId) {
    this.workstationId = workstationId;
  }

  public int getWorkstationId() {
    return workstationId;
  }

  public void setUserTypeId(int userTypeId) {
    this.userTypeId = userTypeId;
  }

  public int getUserTypeId() {
    return userTypeId;
  }

  public int getWorkstationid() {
    return workstationId;
  }

  public void setUserId(int userId) {
    this.userId = userId;
  }

  public int getUserId() {
    return userId;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getUsername() {
    return username;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getPreviousLogin() {
    return getLoginModule().getPreviousLogin();
  }

  public void setUsertypestring(String typestring) {
    this.usertypestring = typestring;
  }

  public String getUsertypestring() {
    return usertypestring;
  }

  public void setSelected2Index(int index) {
    log.trace("** index: {}", index);
    selected2Index = index;
  }

  public int getSelected2() {
    log.trace("selected2: {}", selected2Index);
    return selected2Index;
  }

  public long getSelected2Index() {
    return selected2Index;
  }

  public void setLoginKey(String loginKey) {
    this.loginKey = loginKey;
  }

  public String getLoginKey() {
    return loginKey;
  }

  // function to return an action string

  public String returnNormalPickingAction() {

    // call the warehouse setup module to get a list of all the wacs and how many pending items they have
    WarehouseSetupImpl Service = getWarehouseSetupModule();

    //Service.getPickingCount(workstationId); // @ZSL 6/2018  Redundant code
    // if the picking one is more than 0 continue to the page,
    if (Service.getPickingCount(workstationId, userId) > 0) {
      setSelected2Index(0);
      return "GoPickingNormalMain";
    }
    else {
      // otherwise go to the home
      return "GoPicking";
    }
  }

  public String returnActionGoInventoryInventory() {
    WarehouseSetupImpl service = (WarehouseSetupImpl) ADFUtils.getApplicationModuleForDataControl("WarehouseSetupDataControl");
    if (service.getInventoryItemCount(workstationId, userId) > 0)
      return "GoInventoryInventory";
    else
      return "GoInventory";
  }

  public String returnActionGoInventoryLocSurvey() {
    WarehouseSetupImpl service = (WarehouseSetupImpl) ADFUtils.getApplicationModuleForDataControl("WarehouseSetupDataControl");
    if (service.getLocSurveyCount(workstationId) > 0)
      return "GoInventoryLocSurvey";
    else
      return "GoInventory";
  }

  public String returnActionGoInventoryShelfLife() {
    WarehouseSetupImpl service = (WarehouseSetupImpl) ADFUtils.getApplicationModuleForDataControl("WarehouseSetupDataControl");
    if (service.getShelfLifeCount(workstationId) > 0)
      return "GoInventoryShelfLife";
    else
      return "GoInventory";
  }

  public void setWorkstationType(String workstationType) {
    this.workstationType = workstationType;
  }

  public String getWorkstationType() {
    return workstationType;
  }

  public void setWorkstationName(String workstationName) {
    this.workstationName = workstationName;
  }

  public String getWorkstationName() {
    return workstationName;
  }

  public void setBarcodeTest(String barcodeTest) {
    this.barcodeTest = barcodeTest;
  }

  public String getBarcodeTest() {
    return barcodeTest;
  }

  public void setForceForwardPage(String forceForwardPage) {

    this.forceForwardPage = forceForwardPage;
  }

  public String getForceForwardPage() {

    // if we got a request for this string clear the force to show it
    if (forceredirect != null && !forceredirect.equals(0)) {
      forceredirect = 0;

      return forceForwardPage;
    }
    else {
      return "";
    }
  }

  public void setForceredirect(Object forceredirect) {

    this.forceredirect = forceredirect;
  }

  public Object getForceredirect() {
    return forceredirect;
  }

  public void setBarcodeText(String barcodeText) {
    this.barcodeText = barcodeText;
  }

  public String getBarcodeText() {
    return barcodeText;
  }

  public String getBarcodePath() {
    val barcodeUtils = ContextUtils.getBean(BarcodeUtils.class);
    return barcodeUtils.getBarcodeImagePath();
  }

  public void setSiteProtocol(String siteProtocol) {
    this.siteProtocol = siteProtocol;
  }

  public String getSiteProtocol() {
    if (Util.isEmpty(siteURL)) return "https";
    return (siteURL.startsWith("https")) ? "https" : "http";
  }

  public void setSiteURL(String siteURL) {
    this.siteURL = siteURL;
  }

  public String getSiteURL() {
    return siteURL;
  }

  public void setSiteServerPort(int siteServerPort) {
    this.siteServerPort = siteServerPort;
  }

  public int getSiteServerPort() {
    return siteServerPort;
  }

  public void setSiteServerName(String siteServerName) {
    this.siteServerName = siteServerName;
  }

  public String getSiteServerName() {
    return siteServerName;
  }

  public void setSiteContextPath(String siteContextPath) {
    this.siteContextPath = siteContextPath;
  }

  public String getSiteContextPath() {
    return siteContextPath;
  }

  public void setNonmech(Object nonmech) {
    this.nonmech = nonmech;
  }

  public Object getNonmech() {
    return nonmech;
  }

  public void setUseprintcom(Object useprintcom) {
    this.useprintcom = useprintcom;
  }

  public Object getUseprintcom() {
    return useprintcom;
  }

  public void setPrintcomstring(String printcomstring) {
    this.printcomstring = printcomstring;
  }

  public String getPrintcomstring() {
    // reset the use com flag before returning
    useprintcom = 0;

    return printcomstring;
  }

  public void setPrintcomport(String printcomport) {
    this.printcomport = printcomport;
  }

  public String getPrintcomport() {
    return printcomport;
  }

  public void setStratisVersion(String stratisversion) {
    this.stratisVersion = stratisversion;
  }

  public String getStratisVersion() {
    if (stratisVersion.length() == 0) {
      lookupStratisVersion();
    }
    return stratisVersion;
  }

  public void setUrl(String URL) {
    url = URL;
  }

  public String getUrl() {
    if (StringUtils.isEmpty(url)) {
      try {
        ExternalContext eContext = FacesContext.getCurrentInstance().getExternalContext();
        url = "https://" + Util.encodeUTF8(eContext.getRequestHeaderMap().get("Host")) + "" + (Util.encodeUTF8(eContext.getRequestContextPath()));
      }
      catch (Exception e) {
        log.warn("Failed to automatically determine url using ADF context ", e);

        try {
          url = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        }
        catch (Exception e2) {
          log.warn("Failed to automatically determine url using Spring context ", e);
        }
      }

      log.debug("managed to automatically set url to: {}", url);
    }

    return url;
  }

  /**
   * Use JNDI to look up the current version string. If missing then sets a default.
   */
  private String lookupStratisVersion() {
    String str = "DEV_BUILD";
    try {
      str = JNDIUtils.getInstance().getProperty("STRATVER");
      if (str != null) {
        stratisVersion = str;
      }
    }
    catch (Exception e) {
      // Do nothing.
      str = "DEV_BUILD*";
    }

    stratisVersion = str;
    log.trace(stratisVersion);
    return stratisVersion;
  }

  /*
   * Use JNDI to enable/disable CAC login.
   */
  public boolean lookupCacLogin() {
    boolean useCAC = false;
    try {
      String str = JNDIUtils.getInstance().getProperty("CACFLAG");
      if (str != null) {
        useCAC = ((Integer.parseInt(str)) == 1);
      }
      log.trace("str: {}", str);
    }
    catch (Exception e) {
      log.trace("Exception.");
      useCAC = false;    // Default to pass through login.
    }
    return useCAC;
  }

  public void setSpecialMessage(String specialMessage) {
    this.specialMessage = specialMessage;
  }

  public String getSpecialMessage() {
    return specialMessage;
  }

  public void setWarningShutdownMessage(String warningShutdownMessage) {
    this.warningShutdownMessage = warningShutdownMessage;
  }

  public String getWarningShutdownMessage() {
    String shutdownMessage = "";

    try {
      Object sysmess = getLoginModule().systemShutdownWarningMessage();
      if (sysmess != null) {
        shutdownMessage = sysmess.toString();
      }
    }
    catch (Exception e) {
      log.trace("message conversion error: {}", e.getLocalizedMessage());
    }

    return shutdownMessage;
  }

  // function to set all the com settings

  public void updateComSettings(String comportid, String workstationId) {
    boolean useCOM = comportid.equals("1");

    String sql;
    if (useCOM)
      sql = "select baud_rate, parity, byte_size, stop_bits, com_name from com_port c, equip e where e.com_port_equipment_id=c.com_port_id and e.equipment_number=?";
    else
      sql = "select printer_name from equip e where e.equipment_number=?";

    try (PreparedStatement comps = getLoginModule().getDBTransaction().createPreparedStatement(sql, 0)) {
      comps.setObject(1, workstationId);
      try (ResultSet comrs = comps.executeQuery()) {
        if (comrs.next()) {
          if (useCOM) {
            // update the com values
            setReturncomport(comrs.getString(5));
            setReturncombaud(comrs.getString(1));
            setReturncomparity(comrs.getString(2));
            setReturncomstopbits(comrs.getString(4));
          }
          else {
            setReturncomport("PRINT:" + comrs.getString(1));
            setReturncombaud("9600");
            setReturncomparity("None");
            setReturncomstopbits("1");
          }
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  public void setupForPrinting(Optional<Equipment> equipment, String zplPrintString) {
    if (equipment.isPresent()) {
      setUseprintcom(1);
      setReturncomport("PRINT:" + equipment.get().getPrinterName());
      setReturncombaud("9600");
      setReturncomparity("None");
      setReturncomstopbits("1");
      setPrintcomstring(zplPrintString);
      setPrintcomport(getComPrintIndex());
    }
  }

  public void setReturncomport(String returncomport) {
    this.returncomport = returncomport;
  }

  public String getReturncomport() {
    updateComSettings(printcomport, Integer.toString(workstationId));
    return returncomport;
  }

  public void setReturncomtimeoutseconds(String returncomtimeoutseconds) {
    this.returncomtimeoutseconds = returncomtimeoutseconds;
  }

  public String getReturncomtimeoutseconds() {
    return returncomtimeoutseconds;
  }

  public void setReturncombaud(String returncombaud) {
    this.returncombaud = returncombaud;
  }

  public String getReturncombaud() {
    return returncombaud;
  }

  public void setReturncomparity(String returncomparity) {
    this.returncomparity = returncomparity;
  }

  public String getReturncomparity() {
    return returncomparity;
  }

  public void setReturncomdatabits(String returncomdatabits) {
    this.returncomdatabits = returncomdatabits;
  }

  public String getReturncomdatabits() {
    return returncomdatabits;
  }

  public void setReturncomstopbits(String returncomstopbits) {
    this.returncomstopbits = returncomstopbits;
  }

  public String getReturncomstopbits() {
    return returncomstopbits;
  }

  public void setUseprintframe(Object useprintframe) {
    this.useprintframe = useprintframe;
  }

  public Object getUseprintframe() {
    return useprintframe;
  }

  public void setWinPrintFloorLocation(String floorAacArea) {
    String urls = getWinPrintFloorLocation(floorAacArea);
    setWinPrintOrientation("landscape");
    setWinPrintURL(urls);
  }

  public String getWinPrintFloorLocation(String floorAacArea) {
    StringTokenizer st = new StringTokenizer(floorAacArea, ",");
    StringBuilder urls = new StringBuilder();
    String floor = "";
    String aac = "";
    int i = 0;
    while (st.hasMoreTokens()) {
      switch (i) {
        case 0:
          floor = st.nextToken();
          i++;
          break;
        case 1:
          aac = st.nextToken();
          i++;
          break;
        case 2:
          val area = st.nextToken().replace(" ", "%20");

          urls.append(getURI())
              .append("FloorLocationLabel.jspx?floor=")
              .append(floor)
              .append("&aac=")
              .append(aac)
              .append("&area=")
              .append(area)
              .append(' ');
          //* reset i
          i = 0;
          break;
        default:
          // do nothing
      }
    }

    return urls.toString();
  }

  public void setHison(String hison) {
    val hisonUrl = getURI() + "HISON.jspx";
    setWinPrintURL(hisonUrl);
  }

  public String getHison() {
    return hison;
  }

  public void setWinPrintSCN(String winPrintSCN) {
    String urls = getWinPrintSCN(winPrintSCN);
    setWinPrintOrientation("landscape");
    setWinPrintURL(urls);
  }

  public String getWinPrintSCN(String winPrintSCN) {
    StringTokenizer st = new StringTokenizer(winPrintSCN, ",");
    StringBuilder urls = new StringBuilder();
    while (st.hasMoreTokens()) {
      urls.append(getURI())
          .append("1348.jspx?scn=")
          .append(st.nextToken().replace(" ", "%20"))
          .append("&quantity=")
          .append(st.nextToken());
    }

    return urls.toString();
  }

  public void setWinPrintSCNHist(String winPrintSCNHist) {
    String urls = getWinPrintSCNHist(winPrintSCNHist);
    setWinPrintOrientation("landscape");
    setWinPrintURL(urls);
  }

  public String getWinPrintSCNHist(String winPrintSCNHist) {
    StringTokenizer st = new StringTokenizer(winPrintSCNHist, ",");
    StringBuilder urls = new StringBuilder();
    while (st.hasMoreTokens()) {
      urls.append(getURI())
          .append("1348History.jspx?scn=")
          .append(st.nextToken().replace(" ", "%20"))
          .append("&documentNumber=")
          .append(st.nextToken())
          .append("&quantity=")
          .append(st.nextToken());
    }
    return urls.toString();
  }

  public void setWinPrintManifest(String winPrintManifest) {
    String urls = getWinPrintManifest(winPrintManifest);
    setWinPrintURL(urls);
  }

  public String getWinPrintManifest(String winPrintManifest) {
    StringTokenizer st = new StringTokenizer(winPrintManifest, ",");
    StringBuilder urls = new StringBuilder();
    while (st.hasMoreTokens()) {
      urls.append(getURI())
          .append("ws/Shipping_AACUpdate.jspx?autoprint=1&ldcon=")
          .append(st.nextToken().replace(" ", "%20"))
          .append(' ');
    }
    return urls.toString();
  }

  public void setWinPrintContainerSummary(String winPrintContainerSummary) {
    String urls = getWinPrintContainerSummary(winPrintContainerSummary);
    setWinPrintURL(urls);
  }

  public String getWinPrintContainerSummary(String winPrintContainerSummary) {
    StringTokenizer st = new StringTokenizer(winPrintContainerSummary, ",");
    StringBuilder urls = new StringBuilder();
    while (st.hasMoreTokens()) {
      urls.append(getURI())
          .append("ContainerSummary.jspx?barcode=")
          .append(st.nextToken().replace(" ", "%20"))
          .append(' ');
    }
    return urls.toString();
  }

  public String getURI() {
    String protocol = getSiteProtocol().contains("HTTPS") || getSiteProtocol().contains("https") ? "https://" : "http://";
    return protocol + getSiteServerName() + ":" +
        getSiteServerPort() + getSiteContextPath() +
        "/faces/";
  }

  private String winPrintURL = "";

  public void setWinPrintURL(String s) {
    this.winPrintURL = s;
  }

  public String getWinPrintURL() {
    // clear the print flag so this only happens once
    useprintframe = 0;
    return this.winPrintURL;
  }

  public void setWinPrintOrientation(String winPrintOrientation) {
    this.winPrintOrientation = winPrintOrientation;
  }

  public String getWinPrintOrientation() {
    return winPrintOrientation;
  }

  public void setFrameprintcopies(int frameprintcopies) {
    this.frameprintcopies = frameprintcopies;
  }

  public int getFrameprintcopies() {
    return frameprintcopies;
  }

  public void setComPrintIndex(String comPrintIndex) {
    this.comPrintIndex = comPrintIndex;
  }

  public String getComPrintIndex() {
    return comPrintIndex;
  }

  public void setComCommandIndex(String comCommandIndex) {
    this.comCommandIndex = comCommandIndex;
  }

  public String getComCommandIndex() {
    return comCommandIndex;
  }

  /*****************************
   * COM PRINT 2
   * ********************/
  public void setUseprintcom2(Object useprintcom2) {
    this.useprintcom2 = useprintcom2;
  }

  public Object getUseprintcom2() {
    return useprintcom2;
  }

  public void setPrintcomstring2(String printcomstring2) {
    this.printcomstring2 = printcomstring2;
  }

  public String getPrintcomstring2() {
    // reset the use com flag before returning
    useprintcom2 = 0;

    return printcomstring2;
  }

  public void setPrintcomport2(String printcomport2) {
    this.printcomport2 = printcomport2;
  }

  public String getPrintcomport2() {
    return printcomport2;
  }

  public void updateComSettings2(String comportid, String workstationId) {

    boolean useCOM = comportid.equals("1");

    String sql;
    if (useCOM)
      sql = "select baud_rate, parity, byte_size, stop_bits, com_name from com_port c, equip e where e.com_port_equipment_id=c.com_port_id and e.equipment_number=?";
    else
      sql = "select printer_name from equip e where e.equipment_number=?";

    try (PreparedStatement comps = getLoginModule().getDBTransaction().createPreparedStatement(sql, 0)) {
      comps.setObject(1, workstationId);
      try (ResultSet comrs = comps.executeQuery()) {
        if (comrs.next()) {
          if (useCOM) {
            // update the com values
            setReturncomport2(comrs.getString(5));
            setReturncombaud2(comrs.getString(1));
            setReturncomparity2(comrs.getString(2));
            setReturncomstopbits2(comrs.getString(4));
          }
          else {
            setReturncomport2("PRINT:" + comrs.getString(1));
            setReturncombaud2("9600");
            setReturncomparity2("None");
            setReturncomstopbits2("1");
          }
        }
      }
    }
    catch (Exception e) {
      log.trace(e.getLocalizedMessage());
    }
  }

  public void setReturncomport2(String returncomport2) {
    this.returncomport2 = returncomport2;
  }

  public String getReturncomport2() {
    updateComSettings2(printcomport2, Integer.toString(workstationId));
    return returncomport2;
  }

  public void setReturncomtimeoutseconds2(String returncomtimeoutseconds2) {
    this.returncomtimeoutseconds2 = returncomtimeoutseconds2;
  }

  public String getReturncomtimeoutseconds2() {
    return returncomtimeoutseconds2;
  }

  public void setReturncombaud2(String returncombaud2) {
    this.returncombaud2 = returncombaud2;
  }

  public String getReturncombaud2() {
    return returncombaud2;
  }

  public void setReturncomparity2(String returncomparity2) {
    this.returncomparity2 = returncomparity2;
  }

  public String getReturncomparity2() {
    return returncomparity2;
  }

  public void setReturncomdatabits2(String returncomdatabits2) {
    this.returncomdatabits2 = returncomdatabits2;
  }

  public String getReturncomdatabits2() {
    return returncomdatabits2;
  }

  public void setReturncomstopbits2(String returncomstopbits2) {
    this.returncomstopbits2 = returncomstopbits2;
  }

  public String getReturncomstopbits2() {
    return returncomstopbits2;
  }

  /*****************************
   * END COM PRINT 2
   * **********************/

  public void refreshIterator(String iteratorName) {
    log.trace("refreshing: {}", iteratorName);

    DCIteratorBinding iterator;
    try {
      iterator = ADFUtils.findIterator(iteratorName);
      iterator.executeQuery();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  public void refreshIteratorView(String iteratorName) {
    DCIteratorBinding iterator;
    try {
      iterator = ADFUtils.findIterator(iteratorName);

      Row row = iterator.getCurrentRow();
      ViewObject view = iterator.getViewObject();
      view.executeQuery();
      view.setCurrentRow(row);
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  public void saveIterator(String iteratorName) {
    DCIteratorBinding iter;
    try {
      iter = ADFUtils.findIterator(iteratorName);
      iter.getDataControl().commitTransaction();
      iter.executeQuery();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  /**
   * Method defined to clean "dirty" transactions which may cause a new page
   * to have problems.
   */
  public void cleanDirtyTransactions() {

    try {
      log.debug("==== ZSL ========= inside of  cleanDirtyTransactions");
      LoginModuleImpl service = getLoginModule();
      //* check if interfaces running
      if (!service.isAnythingRunning()) {
        log.debug("==== ZSL ========= Start roll back ");
        service.getDBTransaction().rollback();
        log.debug("==== ZSL ========= Finish roll back ");
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  public void setCleanDirtyTransaction(Object cleanDirtyTransaction) {
    this.cleanDirtyTransaction = cleanDirtyTransaction;
  }

  public void subNavigationListener(ActionEvent event) {
    if (event != null) {
      log.error("subNavigationListener:  {}", event.getComponent().getClientId());
    }
  }

  public Object getCleanDirtyTransaction() {
    log.debug("==== ZSL ========= getCleanDirtyTransaction");
    return 0;
  }

  public boolean isCacLogin() {
    if (useCacLogin == null) {
      useCacLogin = lookupCacLogin();
    }
    log.trace("cac enabled: {}", useCacLogin);
    return useCacLogin;
  }

  public boolean isGCSSMC() {
    return true;
  }

  private String getSTRATIS_URLs(String field) {
    String stratisUrl = "";
    PreparedStatement pstmt = null;
    ResultSet rs = null;

    try {
      pstmt = getLoginModule().getDBTransaction().createPreparedStatement("select " + field + " from site_info", 0);
      rs = pstmt.executeQuery();
      if (rs.next()) {
        stratisUrl = rs.getString(1);
      }
      rs.close();
      pstmt.close();
    }
    catch (SQLException sqle) {
      log.error("Error in {}", sqle.getStackTrace()[0].getMethodName(), sqle);
      stratisUrl = "";
    }
    catch (NullPointerException npe) {
      log.error("Error in {}", npe.getStackTrace()[0].getMethodName(), npe);
      log.error("If we have an NPE here than the login service is down.");
    }
    finally {
      if (rs != null) {
        try {
          rs.close();
        }
        catch (SQLException sqle) {
          rs = null;
        }
      }
      if (pstmt != null) {
        try {
          pstmt.close();
        }
        catch (SQLException sqle) {
          pstmt = null;
        }
      }
    }

    return stratisUrl;
  }

  public String getcomWSDetails() {
    try {
      Context env = (Context) new InitialContext().lookup("java:comp/env");
      String hostStr = (String) env.lookup("comWSHost");
      String path = (String) env.lookup("comWSPath");
      return hostStr + "," + path;
    }
    catch (Exception e) {
      log.trace(e.getLocalizedMessage());
    }
    return "";
  }

  public void setResetHelpURL(Object resetHelpURL) {
    this.resetHelpURL = resetHelpURL;
    setHelpURL("");
  }

  public Object getResetHelpURL() {
    return resetHelpURL;
  }

  public void setHelpURL(String url) {
    helpURL = url;
  }

  public String getHelpURL() {
    if (Util.isEmpty(helpURL)) {
      helpURL = StringEscapeUtils.escapeHtml(getSTRATIS_URLs("help_url"));
    }
    return helpURL;
  }

  /**
   * Used to show debug from withing the pages.
   */
  public void setDebugMessage(String string) {
    debugMessage = string;
    log.trace(string);
  }

  // We set this property and then do a call to refreshIterator.

  public void setIteratorName(String iteratorName) {
    RequestContext afContext = RequestContext.getCurrentInstance();
    if (afContext.isPostback()) {
      log.trace("Refreshing iterator: {}", iteratorName);
      refreshIterator(iteratorName);
    }
  }

  public String getDateWithTimeFormatPattern() {
    return DateConstants.SITE_DATE_WITH_TIME_FORMATTER_PATTERN;
  }

  public String getDateFormatPattern() {
    return DateConstants.SITE_DATE_FORMATTER_PATTERN;
  }

  //shouldn't be needed, but here just in case.  This is the default pattern date fields are returned to the front end in adf when using getString.
  public String getStandardAdfRowDatePattern() { return DateConstants.ADF_ROW_DATE_RETURN_PATTERN;}

  public String getAdfDateWithTimeFormatPattern() {
    return DateConstants.ADF_DATE_WITH_TIME_FORMATTER_PATTERN;
  }

  public String getReportDateTimeWithJulianPattern() { return DateConstants.ADF_REPORT_DATE_TIME_WITH_JULIAN; }

  public String getReportDateTimeWithTimezonePattern() { return DateConstants.ADF_REPORT_DATE_TIME_WITH_TIMEZONE;}

  public String getReportDateTimeNoSecondsPattern() { return DateConstants.ADF_REPORT_DATE_TIME_NO_SECONDS;}

  public String getSiteTimeZone() {
    val siteInfoService = ContextUtils.getBean(SiteInfoService.class);
    val siteInfo = siteInfoService.getStaticRecord();
    return siteInfo.getSiteTimezone();
  }
}
