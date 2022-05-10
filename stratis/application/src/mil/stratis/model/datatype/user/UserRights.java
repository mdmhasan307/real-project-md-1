package mil.stratis.model.datatype.user;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserRights {

  // the user rights defines
  public static final long STRATIS_RECEIVE_RIGHTS = 0XFL; //15
  public static final long STRATIS_RECEIVE_RIGHTS_GENERAL = 0X1L;
  public static final long STRATIS_RECEIVE_RIGHTS_CANCELSID = 0X2L;

  public static final long STRATIS_STOW_RIGHTS = 0XF0L;
  public static final long STRATIS_STOW_RIGHTS_NORMAL = 0X10L;
  public static final long STRATIS_REASSIGN_SID_RIGHTS = 0X20L;

  public static final long STRATIS_PICK_RIGHTS = 0XF00L;
  public static final long STRATIS_PICK_RIGHTS_NORMAL = 0X100L;
  public static final long STRATIS_PICK_RIGHTS_AAC = 0X200L;
  public static final long STRATIS_PICK_RIGHTS_ROUTE = 0X400L;
  public static final long STRATIS_PICK_RIGHTS_PRIORITY = 0X800L;
  public static final long STRATIS_PICK_RIGHTS_WTQ = 0X900L;

  public static final long STRATIS_PACK_RIGHTS = 0XF000L;
  public static final long STRATIS_PACK_RIGHTS_NORMAL = 0X1000L;
  public static final long STRATIS_PACK_RIGHTS_CLOSE = 0X2000L;

  public static final long STRATIS_SHIP_RIGHTS = 0XFF0000L;
  public static final long STRATIS_SHIP_RIGHTS_ADD_CONTAINER = 0X010000L;
  public static final long STRATIS_SHIP_RIGHTS_REVIEW_CONTENTS = 0X020000L;
  public static final long STRATIS_SHIP_RIGHTS_MANIFEST_SHIPMENT = 0X040000L;
  public static final long STRATIS_SHIP_RIGHTS_ACKNOWLEDGE_SHIP = 0X080000L;
  public static final long STRATIS_SHIP_RIGHTS_REMARK_AAC = 0X100000L;
  public static final long STRATIS_SHIP_RIGHTS_RELOCATE_PALLET = 0X200000L;
  public static final long STRATIS_SHIP_RIGHTS_TRANSSHIPMENT = 0X400000L;
  public static final long STRATIS_SHIP_RIGHTS_UPDATE_AAC = 0X800000L;

  public static final long STRATIS_INV_RIGHTS = 0XF000000L;
  public static final long STRATIS_INV_RIGHTS_INVENTORY = 0X1000000L;
  public static final long STRATIS_INV_RIGHTS_LOCSURVAY = 0X2000000L;

  public static final long STRATIS_ADMIN_RIGHTS = 0XFFFFFF00000000L;
  public static final long STRATIS_ADMIN_RIGHTS_SYSTEMSETUP = 0X00100000000000L;
  public static final long STRATIS_ADMIN_RIGHTS_WAREHOUSESETUP = 0X00200000000000L;
  public static final long STRATIS_ADMIN_RIGHTS_WAREHOUSEMANAGE = 0X004FFF00000000L;
  public static final long STRATIS_ADMIN_RIGHTS_WM_NIINUPDATE = 0X00000100000000L;
  public static final long STRATIS_ADMIN_RIGHTS_WM_NIININQUIRY = 0X00000200000000L;
  public static final long STRATIS_ADMIN_RIGHTS_WM_REWAREHOUSE = 0X00000400000000L;
  public static final long STRATIS_ADMIN_RIGHTS_WM_CHANGECC = 0X00000800000000L;
  public static final long STRATIS_ADMIN_RIGHTS_WM_CHANGELOCATION = 0X00001000000000L;
  public static final long STRATIS_ADMIN_RIGHTS_WM_AACUPDATE = 0X00002000000000L;
  public static final long STRATIS_ADMIN_RIGHTS_WM_WALKTHRU = 0X00004000000000L;
  public static final long STRATIS_ADMIN_RIGHTS_WM_RICUPDATE = 0X00010000000000L;
  public static final long STRATIS_ADMIN_RIGHTS_WM_SHIPROUTEUPDATE = 0X00020000000000L;

  public static final long STRATIS_ADMIN_RIGHTS_USERMANAGE = 0X00800000000000L;
  public static final long STRATIS_ADMIN_RIGHTS_DEPLOYMENT = 0X10000000000000L;
  public static final long STRATIS_ADMIN_RIGHTS_EXCEPTIONHANDLE = 0X20000000000000L;

  public static final long STRATIS_ADMIN_RIGHTS_INVENTORYMANAGE = 0X4F000000000000L;
  public static final long STRATIS_ADMIN_RIGHTS_INVM_SCHEDULE = 0X01000000000000L;
  public static final long STRATIS_ADMIN_RIGHTS_INVM_REVIEWRELEASE = 0X02000000000000L;

  public static final long STRATIS_ADMIN_RIGHTS_SYSTEMADMIN = 0X80000000000000L;
  public static final long STRATIS_REPORTS_RIGHTS = 0XF00000000000000L;
  public static final long STRATIS_REPORTS_RIGHTS_VIEW = 0X100000000000000L;
  public static final long STRATIS_REPORTS_RIGHTS_ADD = 0X200000000000000L;
  public static final long STRATIS_REPORTS_RIGHTS_EDIT = 0X400000000000000L;
  public static final long STRATIS_REPORTS_RIGHTS_DELETE = 0X800000000000000L;
  public static final long STRATIS_PRINT_RIGHTS = 0XF0000000L;
  public static final long STRATIS_WALKTHRU_RIGHTS = 0XF2000000L;

  private final long usersRights;
  private final String workstationType;

  public UserRights(long rights, String workstationType) {
    this.usersRights = rights;
    this.workstationType = workstationType;
  }

  public long getUsersRights() {
    return usersRights;
  }

  /**
   * This function returns whether stowing rights are given
   */
  public boolean isStowing() {
    // check rights
    long checkbit = usersRights & STRATIS_STOW_RIGHTS;

    // ensure this is a stowing station too
    if (checkbit > 0 && !workstationType.contains("Stow")) checkbit = 0;

    return (checkbit > 0);
  }

  public boolean isStowingNormal() {
    long checkbit = usersRights & UserRights.STRATIS_STOW_RIGHTS_NORMAL;
    return (checkbit > 0);
  }

  public boolean isStowingReassignSid() {
    long checkbit = usersRights & UserRights.STRATIS_REASSIGN_SID_RIGHTS;
    return (checkbit > 0);
  }

  /**
   * This function returns whether shipping rights are given
   */
  public boolean isShipping() {
    // check rights
    long checkbit = usersRights & STRATIS_SHIP_RIGHTS;

    // ensure this is a shipping station too
    if (checkbit > 0 && !workstationType.contains("Ship")) checkbit = 0;

    return (checkbit > 0);
  }

  /**
   * This function returns whether picking rights are given
   */
  public boolean isPicking() {
    // check rights
    long checkbit = usersRights & STRATIS_PICK_RIGHTS;

    // ensure this is a picking station too
    if (checkbit > 0 && !workstationType.contains("Stow")) checkbit = 0;

    return (checkbit > 0);
  }

  public boolean isPacking() {
    // check rights
    long checkbit = usersRights & UserRights.STRATIS_PACK_RIGHTS;

    // ensure this is a receiving station too
    if (checkbit > 0 && !workstationType.contains("Pack")) checkbit = 0;

    return (checkbit > 0);
  }

  public boolean isPackingNormal() {
    long checkbit = usersRights & UserRights.STRATIS_PACK_RIGHTS_NORMAL;
    return (checkbit > 0);
  }

  public boolean isPackingClose() {
    long checkbit = usersRights & UserRights.STRATIS_PACK_RIGHTS_CLOSE;
    return (checkbit > 0);
  }

  /**
   * This function returns whether inventory rights are given
   *
   * @return boolean
   */
  public boolean isInventory() {
    // check rights
    long checkbit = usersRights & STRATIS_INV_RIGHTS;

    // ensure this is a inventory station too
    if (checkbit > 0 && !workstationType.contains("Stow")) checkbit = 0;

    return (checkbit > 0);
  }

  // if this is an admin level user
  public boolean isAdminLevelUser() {
    long checkbit = usersRights & UserRights.STRATIS_ADMIN_RIGHTS_EXCEPTIONHANDLE;
    return (checkbit > 0);
  }

  /**
   * This function returns whether admin rights are given
   *
   * @return boolean
   */
  public boolean isAdmin() {
    // check rights
    long checkbit = usersRights & STRATIS_ADMIN_RIGHTS;
    return (checkbit > 0);
  }

  public boolean isAdminSystemSetup() {
    long checkbit = usersRights & UserRights.STRATIS_ADMIN_RIGHTS_SYSTEMSETUP;
    return (checkbit > 0);
  }

  public boolean isAdminWarehouseSetup() {
    long checkbit = usersRights & UserRights.STRATIS_ADMIN_RIGHTS_WAREHOUSESETUP;
    return (checkbit > 0);
  }

  public boolean isAdminWarehouseManage() {
    long checkbit = usersRights & UserRights.STRATIS_ADMIN_RIGHTS_WAREHOUSEMANAGE;
    return (checkbit > 0);
  }

  public boolean isAdminWMNIINUpdate() {
    long checkbit = usersRights & UserRights.STRATIS_ADMIN_RIGHTS_WM_NIINUPDATE;
    return (checkbit > 0);
  }

  public boolean isAdminWMNIINInquiry() {
    long checkbit = usersRights & UserRights.STRATIS_ADMIN_RIGHTS_WM_NIININQUIRY;
    return (checkbit > 0);
  }

  public boolean isAdminWMRewarehouse() {
    long checkbit = usersRights & UserRights.STRATIS_ADMIN_RIGHTS_WM_REWAREHOUSE;
    return (checkbit > 0);
  }

  public boolean isAdminWMChangeCC() {
    long checkbit = usersRights & UserRights.STRATIS_ADMIN_RIGHTS_WM_CHANGECC;
    return (checkbit > 0);
  }

  public boolean isAdminWMChangeLocation() {
    long checkbit = usersRights & UserRights.STRATIS_ADMIN_RIGHTS_WM_CHANGELOCATION;
    return (checkbit > 0);
  }

  public boolean isAdminWMAACUpdate() {
    long checkbit = usersRights & UserRights.STRATIS_ADMIN_RIGHTS_WM_AACUPDATE;
    return (checkbit > 0);
  }

  public boolean isAdminWMWalkThru() {
    long checkbit = usersRights & UserRights.STRATIS_ADMIN_RIGHTS_WM_WALKTHRU;
    return (checkbit > 0);
  }

  public boolean isAdminWMRICUpdate() {
    long checkbit = usersRights & UserRights.STRATIS_ADMIN_RIGHTS_WM_RICUPDATE;
    return (checkbit > 0);
  }

  public boolean isAdminWMShipRouteUpdate() {
    long checkbit = usersRights & UserRights.STRATIS_ADMIN_RIGHTS_WM_SHIPROUTEUPDATE;
    return (checkbit > 0);
  }

  public boolean isAdminUserManage() {
    long checkbit = usersRights & UserRights.STRATIS_ADMIN_RIGHTS_USERMANAGE;
    return (checkbit > 0);
  }

  public boolean isAdminDeployment() {
    long checkbit = usersRights & UserRights.STRATIS_ADMIN_RIGHTS_DEPLOYMENT;
    return (checkbit > 0);
  }

  public boolean isAdminExceptionHandle() {
    return isAdminLevelUser();
  }

  public boolean isAdminInventoryManage() {
    long checkbit = usersRights & UserRights.STRATIS_ADMIN_RIGHTS_INVENTORYMANAGE;
    return (checkbit > 0);
  }

  public boolean isAdminINVMReviewRelease() {
    long checkbit = usersRights & UserRights.STRATIS_ADMIN_RIGHTS_INVM_REVIEWRELEASE;
    return (checkbit > 0);
  }

  public boolean isAdminINVMSchedule() {
    long checkbit = usersRights & UserRights.STRATIS_ADMIN_RIGHTS_INVM_SCHEDULE;
    return (checkbit > 0);
  }

  public boolean isAdminSystemAdmin() {
    long checkbit = usersRights & UserRights.STRATIS_ADMIN_RIGHTS_SYSTEMADMIN;
    return (checkbit > 0);
  }

  public boolean isAdminSystemAdminInterfaces() {
    long checkbit = usersRights & UserRights.STRATIS_ADMIN_RIGHTS_SYSTEMADMIN;
    return (checkbit > 0);
  }

  public boolean isAdminSystemAdminExports() {
    long checkbit = usersRights & UserRights.STRATIS_ADMIN_RIGHTS_SYSTEMADMIN;
    return (checkbit > 0);
  }

  public boolean isReports() {
    long checkbit = usersRights & STRATIS_REPORTS_RIGHTS;
    return (checkbit > 0);
  }

  public boolean isReportsView() {
    long checkbit = usersRights & UserRights.STRATIS_REPORTS_RIGHTS_VIEW;
    return (checkbit > 0);
  }

  public boolean isReportsAdd() {
    long checkbit = usersRights & UserRights.STRATIS_REPORTS_RIGHTS_ADD;
    return (checkbit > 0);
  }

  public boolean isReportsEdit() {
    long checkbit = usersRights & UserRights.STRATIS_REPORTS_RIGHTS_EDIT;
    return (checkbit > 0);
  }

  public boolean isReportsDelete() {
    long checkbit = usersRights & UserRights.STRATIS_REPORTS_RIGHTS_DELETE;
    return (checkbit > 0);
  }

  public boolean isPrint() {
    long checkbit = usersRights & UserRights.STRATIS_PRINT_RIGHTS;
    return (checkbit > 0);
  }

  // function return whether walkthru rights are given
  public boolean isWalkThru() {
    long checkbit = usersRights & UserRights.STRATIS_WALKTHRU_RIGHTS;
    return (checkbit > 0);
  }

  /**
   * This function returns whether receiving rights are given
   *
   * @return boolean
   */
  public boolean isReceiving() {
    // check rights
    long checkbit = usersRights & STRATIS_RECEIVE_RIGHTS;

    // ensure this is a receiving station too
    if (checkbit > 0 && !workstationType.contains("Receiving")) checkbit = 0;

    return (checkbit > 0);
  }

  public boolean isReceivingGeneral() {
    long checkbit = usersRights & UserRights.STRATIS_RECEIVE_RIGHTS_GENERAL;
    return (checkbit > 0);
  }

  public boolean isReceivingCancelSID() {
    long checkbit = usersRights & UserRights.STRATIS_RECEIVE_RIGHTS_CANCELSID;
    return (checkbit > 0);
  }

  public boolean isPickingNormal() {
    long checkbit = usersRights & STRATIS_PICK_RIGHTS_NORMAL;
    return (checkbit > 0);
  }

  public boolean isPickingAAC() {
    long checkbit = usersRights & STRATIS_PICK_RIGHTS_AAC;
    return (checkbit > 0);
  }

  public boolean isPickingRoute() {
    long checkbit = usersRights & STRATIS_PICK_RIGHTS_ROUTE;
    return (checkbit > 0);
  }

  public boolean isPickingPriority() {
    long checkbit = usersRights & STRATIS_PICK_RIGHTS_PRIORITY;
    return (checkbit > 0);
  }

  public boolean isPickingWTQ() {
    long checkbit = usersRights & STRATIS_PICK_RIGHTS_WTQ;
    return (checkbit > 0);
  }

  public boolean isShippingAddContainer() {
    long checkbit = usersRights & STRATIS_SHIP_RIGHTS_ADD_CONTAINER;
    return (checkbit > 0);
  }

  public boolean isShippingReviewContents() {
    long checkbit = usersRights & STRATIS_SHIP_RIGHTS_REVIEW_CONTENTS;
    return (checkbit > 0);
  }

  public boolean isShippingManifestShipment() {
    long checkbit = usersRights & STRATIS_SHIP_RIGHTS_MANIFEST_SHIPMENT;
    return (checkbit > 0);
  }

  public boolean isShippingAcknowledgeShip() {
    long checkbit = usersRights & STRATIS_SHIP_RIGHTS_ACKNOWLEDGE_SHIP;
    return (checkbit > 0);
  }

  public boolean isShippingRemarkAAC() {
    long checkbit = usersRights & STRATIS_SHIP_RIGHTS_REMARK_AAC;
    return (checkbit > 0);
  }

  public boolean isShippingRelocatePallet() {
    long checkbit = usersRights & STRATIS_SHIP_RIGHTS_RELOCATE_PALLET;
    return (checkbit > 0);
  }

  public boolean isShippingTransshipment() {
    long checkbit = usersRights & STRATIS_SHIP_RIGHTS_TRANSSHIPMENT;
    return (checkbit > 0);
  }

  public boolean isShippingUpdateAAC() {
    long checkbit = usersRights & UserRights.STRATIS_SHIP_RIGHTS_UPDATE_AAC;
    return (checkbit > 0);
  }

  public boolean isInvInventory() {
    long checkbit = usersRights & STRATIS_INV_RIGHTS_INVENTORY;
    return (checkbit > 0);
  }

  public boolean isInvLocSurvay() {
    long checkbit = usersRights & STRATIS_INV_RIGHTS_LOCSURVAY;
    return (checkbit > 0);
  }

  public boolean isInvShelfLife() {
    long checkbit = usersRights & STRATIS_INV_RIGHTS_INVENTORY;
    return (checkbit > 0);
  }

  public static long userRightLookUp(String right) {
    long returnValue;

    switch (right) {
      case "AAC Update":
        returnValue = STRATIS_ADMIN_RIGHTS_WM_AACUPDATE;
        break;
      case "Acknowledge Shipment":
        returnValue = STRATIS_SHIP_RIGHTS_ACKNOWLEDGE_SHIP;
        break;
      case "Add Container":
        returnValue = STRATIS_SHIP_RIGHTS_ADD_CONTAINER;
        break;
      case "Cancel SID":
        returnValue = STRATIS_RECEIVE_RIGHTS_CANCELSID;
        break;
      case "Change Condition Code":
        returnValue = STRATIS_ADMIN_RIGHTS_WM_CHANGECC;
        break;
      case "Change Location":
        returnValue = STRATIS_ADMIN_RIGHTS_WM_CHANGELOCATION;
        break;
      case "Close Carton":
        returnValue = STRATIS_PACK_RIGHTS_CLOSE;
        break;
      case "Exception Processing":
        returnValue = STRATIS_ADMIN_RIGHTS_EXCEPTIONHANDLE;
        break;
      case "General Receiving":
        returnValue = STRATIS_RECEIVE_RIGHTS_GENERAL;
        break;
      case "Inventory":
        returnValue = STRATIS_INV_RIGHTS_INVENTORY;
        break;
      case "Inventory Management":
        returnValue = STRATIS_ADMIN_RIGHTS_INVENTORYMANAGE;
        break;
      case "Inventory Schedule":
        returnValue = STRATIS_ADMIN_RIGHTS_INVM_SCHEDULE;
        break;
      case "Location Survey":
        returnValue = STRATIS_INV_RIGHTS_LOCSURVAY;
        break;
      case "Manifest Shipment":
        returnValue = STRATIS_SHIP_RIGHTS_MANIFEST_SHIPMENT;
        break;
      case "NIIN Inquiry":
        returnValue = STRATIS_ADMIN_RIGHTS_WM_NIININQUIRY;
        break;
      case "NIIN Update":
        returnValue = STRATIS_ADMIN_RIGHTS_WM_NIINUPDATE;
        break;
      case "Normal Pack":
        returnValue = STRATIS_PACK_RIGHTS_NORMAL;
        break;
      case "Normal Pick":
        returnValue = STRATIS_PICK_RIGHTS_NORMAL;
        break;
      case "Normal Stow":
        returnValue = STRATIS_STOW_RIGHTS_NORMAL;
        break;
      case "Pick by AAC":
        returnValue = STRATIS_PICK_RIGHTS_AAC;
        break;
      case "Pick by Priority":
        returnValue = STRATIS_PICK_RIGHTS_PRIORITY;
        break;
      case "Pick by Route":
        returnValue = STRATIS_PICK_RIGHTS_ROUTE;
        break;
      case "Pick by WTQ":
        returnValue = STRATIS_PICK_RIGHTS_WTQ;
        break;
      case "Reassign SID":
        returnValue = STRATIS_REASSIGN_SID_RIGHTS;
        break;
      case "Relocate Pallet":
        returnValue = STRATIS_SHIP_RIGHTS_RELOCATE_PALLET;
        break;
      case "Remark AAC":
        returnValue = STRATIS_SHIP_RIGHTS_REMARK_AAC;
        break;
      case "Reprint":
        returnValue = STRATIS_PRINT_RIGHTS;
        break;
      case "Reports":
        returnValue = STRATIS_REPORTS_RIGHTS;
        break;
      case "Review Contents":
        returnValue = STRATIS_SHIP_RIGHTS_REVIEW_CONTENTS;
        break;
      case "Rewarehouse":
        returnValue = STRATIS_ADMIN_RIGHTS_WM_REWAREHOUSE;
        break;
      case "Route (RIC) Update":
        returnValue = STRATIS_ADMIN_RIGHTS_WM_RICUPDATE;
        break;

      case "Shipping Route Update":
        returnValue = STRATIS_ADMIN_RIGHTS_WM_SHIPROUTEUPDATE;
        break;
      case "System Admin":
        returnValue = STRATIS_ADMIN_RIGHTS;
        break;
      case "System Setup":
        returnValue = STRATIS_ADMIN_RIGHTS_SYSTEMSETUP;
        break;
      case "Transshipment":
        returnValue = STRATIS_SHIP_RIGHTS_TRANSSHIPMENT;
        break;
      case "User Management":
        returnValue = STRATIS_ADMIN_RIGHTS_USERMANAGE;
        break;
      case "Walk Thru":
        returnValue = STRATIS_ADMIN_RIGHTS_WM_WALKTHRU;
        break;
      case "Warehouse Management":
        returnValue = STRATIS_ADMIN_RIGHTS_WAREHOUSEMANAGE;
        break;
      case "Warehouse Setup":
        returnValue = STRATIS_ADMIN_RIGHTS_WAREHOUSESETUP;
        break;
      case "Interfaces":
      case "Shelf Life":
      default:
        returnValue = 0;
        break;
    }

    return returnValue;
  }
}
