package mil.stratis.view.admin;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.common.util.RegUtils;
import mil.stratis.common.util.Util;
import mil.stratis.model.services.GCSSMCTransactionsImpl;
import mil.stratis.model.services.PickingAMImpl;
import mil.stratis.model.services.SysAdminImpl;
import mil.stratis.model.services.WorkLoadManagerImpl;
import mil.stratis.model.view.pick.SerialOrLotNumListImpl;
import mil.stratis.view.session.MdssBackingBean;
import mil.stratis.view.user.UserInfo;
import mil.stratis.view.util.JSFUtils;
import mil.usmc.mls2.stratis.common.domain.model.LabelType;
import mil.usmc.mls2.stratis.core.utility.AdfLogUtility;
import mil.usmc.mls2.stratis.core.utility.ContextUtils;
import mil.usmc.mls2.stratis.core.utility.LabelPrintUtil;
import oracle.adf.view.rich.component.rich.data.RichTable;
import oracle.adf.view.rich.component.rich.input.RichInputText;
import oracle.adf.view.rich.component.rich.input.RichSelectBooleanRadio;
import oracle.adf.view.rich.component.rich.input.RichSelectOneChoice;
import oracle.adf.view.rich.component.rich.layout.RichPanelGroupLayout;
import oracle.adf.view.rich.component.rich.nav.RichButton;
import oracle.adf.view.rich.component.rich.nav.RichCommandButton;
import oracle.jbo.Row;
import oracle.jbo.ViewObject;
import oracle.jbo.uicli.binding.JUCtrlValueBindingRef;
import org.apache.myfaces.trinidad.context.RequestContext;
import org.apache.myfaces.trinidad.model.CollectionModel;
import org.apache.myfaces.trinidad.model.RowKeySet;
import org.apache.myfaces.trinidad.model.RowKeySetImpl;

import javax.faces.event.ActionEvent;
import java.util.*;

@Slf4j
public class ExceptionBacking extends MdssBackingBean {

  protected transient Object detailState = 0;
  protected transient Object detailType = 0;
  protected transient Object pageState = 0;
  protected transient Object refusalDetailState = 0;
  protected int errorId = 0;
  protected int errorQueueId = 0;
  protected int qtyRefused = 0;
  protected String rowSCN = null;

  // 11/2017 ZSL  RichCommandButton -> RichButton
  private transient RichButton detailButton;
  private transient RichButton refusalDetailButton;
  private transient RichCommandButton saveDetailPage;
  private transient RichCommandButton saveDetailPage2;
  private transient RichCommandButton saveDetailPage3;
  private transient RichCommandButton saveDetailPage4;
  private transient RichCommandButton saveDetailPage5;
  private transient RichCommandButton saveDetailPage6;
  private transient RichCommandButton saveDetailPage7;
  private transient RichCommandButton saveDetailPage8;
  private transient RichCommandButton saveDetailPage9;
  private transient RichCommandButton saveDetailPage10;
  private transient RichCommandButton saveDetailPage11;
  private transient RichCommandButton saveDetailPage12;
  private transient RichCommandButton saveDetailPage13;
  private transient RichCommandButton saveDetailPage14;
  private transient RichCommandButton saveDetailPage15;
  private transient RichCommandButton saveDetailPage16;
  private transient RichSelectBooleanRadio detailRadio1;
  private transient RichSelectBooleanRadio detailRadio2;
  private transient RichSelectBooleanRadio detailRadio3;
  private transient RichSelectBooleanRadio detailRadio4;
  private transient RichSelectBooleanRadio detailRadio5;
  private transient RichSelectBooleanRadio detailRadio6;
  private transient RichSelectBooleanRadio detailRadio7;
  private transient RichSelectBooleanRadio detailRadio8;
  private transient RichSelectBooleanRadio detailRadio9;
  private transient RichSelectBooleanRadio detailRadio10;
  private transient RichSelectBooleanRadio detailRadio11;
  private transient RichSelectBooleanRadio detailRadio12;
  private transient RichSelectBooleanRadio detailRadio13;
  private transient RichSelectBooleanRadio detailRadio14;
  private transient RichSelectBooleanRadio detailRadio15;
  private transient RichSelectBooleanRadio detailRadio16;
  private transient RichSelectBooleanRadio refuseDetailRadio1;
  private transient RichSelectBooleanRadio refuseDetailRadio2;
  private transient RichSelectBooleanRadio refuseDetailRadio3;
  private transient RichSelectBooleanRadio refuseDetailRadio4;
  private transient RichSelectOneChoice exceptionFilterData;
  private transient RichCommandButton saveRefuseDetails;
  private transient RichButton exceptionFilter;
  private transient RichSelectBooleanRadio detailRadio17;
  private transient RichSelectBooleanRadio detailRadio18;
  private transient RichSelectBooleanRadio detailRadio19;
  private transient RichSelectBooleanRadio detailRadio20;
  private transient RichSelectBooleanRadio detailRadio21;
  private transient RichSelectBooleanRadio detailRadio22;
  private transient RichSelectBooleanRadio detailRadio23;
  private transient RichSelectBooleanRadio detailRadio24;
  private transient RichSelectBooleanRadio detailRadio25;

  private transient RichPanelGroupLayout serialNumberPanel;
  private transient RichSelectBooleanRadio detailStowLossGcss;

  private transient RichInputText serialOrLotNum;
  private transient RichInputText startSerial;
  private transient RichInputText endSerial;

  private String selectedErrorLabel;
  private String errorDetails;
  private String length;
  private String width;
  private String height;
  private String qty;
  private transient RichTable exceptionTable;
  private transient RichTable refusalTable;
  private transient RichTable locationTable;

  protected enum DETAIL_TYPE {
    NONE(0),
    INFORMATION_DISPLAY(1),
    PICK_BYPASS(2),
    STOW_BYPASS(3),
    RECEIPT_ERROR(4),
    IMPORT_ERROR_GBOF(5),
    IMPORT_ERROR_GCSS_TRANS(6),
    IMPORT_ERROR_FTP(7),
    STOW_LOSS_SASSY(8),
    MATS_A5J(9),
    MHIF_UI_MANUAL(10),
    UI_CONVERSION_FOLLOWUP(11),
    LOG_SERIAL_NUM(12),
    STOW_DELAY(13),
    STOW_LOSS_GCSS(14),
    EXPIRATION(15),
    WALKTHRU(16),
    COURIER(17);
    private int value;

    DETAIL_TYPE(int v) { value = v; }

    protected int value() { return value; }
  }

  private static int detailAideMaxValue = 83;
  private static int MAX_LEN_COST_JON = 12;
  private static int MAX_LEN_DOCUMENT_NUMBER = 14;
  private static int MAX_LEN_RIC_FROM = 3;
  private static int MAX_LEN_FUND_CODE = 2;
  private static int MAX_LEN_MEDIAANDSTATUS_CODE = 1;
  private static int MAX_LEN_RDD = 3;

  // Quick and dirty for now - should recode this possibly as a map.
  private static final String[] detailAide = {
      // 0 - None
      "",
      // 1 - RECEIPT ERROR - FSC Remakring
      "Run MHIF Import or MHIF Immediate Export.",
      // 2 - RECEIPT ERROR - QTY Received
      "Verify item was Transhipped.",
      // 3 - RECEIPT ERROR - QTY Received
      "",
      // 4 - RECEIPT ERROR - QTY Received
      "",
      // 5 - RECEIPT ERROR - QTY Received
      "User Input Error.",
      // 6 - RECEIPT ERROR - QTY Received
      "STRATIS Auto-Converted U/I for this Receipt.",
      // 7 - RECEIPT ERROR - QTY Received
      "Correct UI and invoice quantity through Receiving process.",
      // 8 - RECEIPT ERROR - QTY Received
      "Verify weight of receipted item.",
      // 9 - RECEIPT ERROR - QTY Received
      "Verify weight of receipted item.",
      // 10, 11, 12
      "", "", "",
      // 13 - Pick Bypass1 - Need Assistance
      "Operator needs Assistance.",
      // 14 - Pick Bypass2 - Possible Partial Issue
      "",
      // 15 - Pick Bypass4 - Possible Denial
      "",
      // 16 - Pick Bypass3 - Disparity of U/I, FSC, or NIIN
      "",
      // 17 - Stow Bypass1
      "",
      // 18 - Stow Bypass2
      "",
      // 19, 20, 21, 22
      "", "", "", "",
      // 23 - Stow Bypass3
      "",
      // 24
      "",
      // 25 - RECEIPT ERROR - QTY Received
      "Verify that item was repacked correctly after U/I conversion.  RCN = ",
      // 26 - IMPORT ERROR - GBOF AAC
      "Obtain additional document numbers not processed through Direct Table Access selecting Ref_Dataload_Log table.",
      // 27 - IMPORT ERROR - GBOF NIIN
      "Obtain additional document numbers not processed through Direct Table Access selecting Ref_Dataload_Log table.",
      // 28 - IMPORT ERROR - GBOF Skip error
      "Obtain additional document numbers not processed through Direct Table Access selecting Ref_Dataload_Log table.",
      // 29
      "",
      // 30 - IMPORT ERROR - FTP Connection
      "Please check username, password, and connection information and try again.",
      // 31 - IMPORT ERROR - FTP Download
      "File path may be incorrect (on local server or remote server) or file may not be present.",
      // 32 - IMPORT ERROR - DASF CONFIGURATION
      "File name and path needs to be setup in System Admin prior to import.",
      // 33
      "",
      // 34 - IMPORT ERROR - GABF CONFIGURATION
      "File name and path needs to be setup in System Admin prior to import.",
      // 35
      "",
      // 36 - IMPORT ERROR - MHIF CONFIGURATION
      "File name and path needs to be setup in System Admin prior to import.",
      // 37
      "",
      // 38 - IMPORT ERROR - GBOF CONFIGURATION
      "File name and path needs to be setup in System Admin prior to import.",
      // 39
      "",
      // 40 - IMPORT ERROR - MATS CONFIGURATION
      "File name and path needs to be setup in System Admin prior to import.",
      // 41
      "",
      // 42 - RECEIPT ERROR - Cube Recorded
      "Verify correct cubic dimensions were entered.",
      // 43 - RECEIPT ERROR - Weight Recorded
      "Verify correct weight and quantity were measured.",
      // 44 - NIIN Expiration Exception
      "NIIN ",
      // 45 - RECEIPT ERROR - QTY Received
      "NIIN not in STRATIS. Run MHIF Import or MHIF Immediate.",
      // 46 - SHELFLIFE
      "Could not extend Shelf Life - Expiration.  Location = ",
      // 47 - Stow Loss
      "Item lost between Receiving and Stowing. Investigate item lost, misplaced, or receipted incorrectly.",
      // 48 - Pick Bypass5 - Location Locked
      "Location was locked during attempted Pick.",
      // 49 - Stow Bypass4
      "",
      // 50
      "",
      // 51 - NIIN LOC SURVEY INVENTORY EXISTS
      "NIIN added ",
      // 52 - NIIN INQUIRY
      "Location was placed under audit during NIIN Inquiry.",
      // 53 - GABF OVERLAY
      "",
      // 54 - CONDITION CODE EXPIRATION
      "",
      // 55 - MATS A5J
      "Check Ref_MATS through Direct Table Access for additional details.",
      // 56 - MHIF U/I Conversion - Manual Conversion
      "Manual qty conversion required.",
      // 57 - U/I Conversion Followup - Measurement Request
      "Weight auto-converted. Change Dimensions if necessary",
      // 58 - LOT_SERIAL# Exception - Item picked from location different than expected
      "",
      //  59, 60, 61, 62, 63, 64, 65,
      "", "", "", "", "", "", "",
      // 66, 67, 68, 69, 70, 71, 72,
      "", "", "", "", "", "", "",
      // 73, 74, 75, 76, 77
      "", "", "", "", "",
      // 78 - COURIER WALK THRU IMPORT failed to create
      "Courier Walk Thru ",
      // 79
      "",
      // 80 - STOW_DELAY
      "",
      //81 COURIER, 82 WALKTHRU
      "",
      "",
  };

  // Quick and dirty for now - should recode this possibly as a map.
  private final static int[] detailTypes = {
      // 0 - None
      0,
      // 1 - RECEIPT ERROR - FSC Remakring
      DETAIL_TYPE.RECEIPT_ERROR.value(),
      // 2 - RECEIPT ERROR - QTY Received
      DETAIL_TYPE.RECEIPT_ERROR.value(),
      // 3 - RECEIPT ERROR - QTY Received
      DETAIL_TYPE.RECEIPT_ERROR.value(),
      // 4 - RECEIPT ERROR - QTY Received
      DETAIL_TYPE.RECEIPT_ERROR.value(),
      // 5 - RECEIPT ERROR - QTY Received
      DETAIL_TYPE.RECEIPT_ERROR.value(),
      // 6 - RECEIPT ERROR - QTY Received
      DETAIL_TYPE.RECEIPT_ERROR.value(),
      // 7 - RECEIPT ERROR - QTY Received
      DETAIL_TYPE.RECEIPT_ERROR.value(),
      // 8 - RECEIPT ERROR - QTY Received
      DETAIL_TYPE.RECEIPT_ERROR.value(),
      // 9 - RECEIPT ERROR - QTY Received
      DETAIL_TYPE.RECEIPT_ERROR.value(),
      // 10, 11, 12
      0, 0, 0,
      // 13 - Pick Bypass1 - Need Assistance
      DETAIL_TYPE.PICK_BYPASS.value(),
      // 14 - Pick Bypass2 - Possible Partial Issue
      DETAIL_TYPE.PICK_BYPASS.value(),
      // 15 - Pick Bypass4 - Possible Denial
      DETAIL_TYPE.PICK_BYPASS.value(),
      // 16 - Pick Bypass3 - Disparity of U/I, FSC, or NIIN
      DETAIL_TYPE.PICK_BYPASS.value(),
      // 17 - Stow Bypass1
      DETAIL_TYPE.STOW_BYPASS.value(),
      // 18 - Stow Bypass2
      DETAIL_TYPE.STOW_BYPASS.value(),
      // 19, 20, 21, 22
      0, 0, 0, 0,
      // 23 - Stow Bypass3
      DETAIL_TYPE.STOW_BYPASS.value(),
      // 24
      0,
      // 25 - RECEIPT ERROR - QTY Received
      DETAIL_TYPE.INFORMATION_DISPLAY.value(),
      // 26 - IMPORT ERROR - GBOF AAC
      DETAIL_TYPE.IMPORT_ERROR_GBOF.value(),
      // 27 - IMPORT ERROR - GBOF NIIN
      DETAIL_TYPE.IMPORT_ERROR_GBOF.value(),
      // 28 - IMPORT ERROR - GBOF Skip error
      DETAIL_TYPE.IMPORT_ERROR_GBOF.value(),
      // 29
      0,
      // 30 - IMPORT ERROR - FTP Connection
      DETAIL_TYPE.IMPORT_ERROR_FTP.value(),
      // 31 - IMPORT ERROR - FTP Download
      DETAIL_TYPE.IMPORT_ERROR_FTP.value(),
      // 32 - IMPORT ERROR - DASF CONFIGURATION
      DETAIL_TYPE.IMPORT_ERROR_GCSS_TRANS.value(),
      // 33
      0,
      // 34 - IMPORT ERROR - GABF CONFIGURATION
      DETAIL_TYPE.IMPORT_ERROR_GCSS_TRANS.value(),
      // 35
      0,
      // 36 - IMPORT ERROR - MHIF CONFIGURATION
      DETAIL_TYPE.IMPORT_ERROR_GCSS_TRANS.value(),
      // 37
      0,
      // 38 - IMPORT ERROR - GBOF CONFIGURATION
      DETAIL_TYPE.IMPORT_ERROR_GCSS_TRANS.value(),
      // 39
      0,
      // 40 - IMPORT ERROR - MATS CONFIGURATION
      DETAIL_TYPE.IMPORT_ERROR_GCSS_TRANS.value(),
      // 41
      0,
      // 42 - RECEIPT ERROR - Cube Recorded
      DETAIL_TYPE.RECEIPT_ERROR.value(),
      // 43 - RECEIPT ERROR - Weight Recorded
      DETAIL_TYPE.RECEIPT_ERROR.value(),
      // 44 - NIIN Expiration Exception
      DETAIL_TYPE.INFORMATION_DISPLAY.value(),
      // 45 - RECEIPT ERROR - QTY Received
      DETAIL_TYPE.RECEIPT_ERROR.value(),
      // 46 - SHELFLIFE
      DETAIL_TYPE.INFORMATION_DISPLAY.value(),
      // 47 - Stow Loss
      DETAIL_TYPE.STOW_LOSS_SASSY.value(),
      // 48 - Pick Bypass5 - Location Locked
      DETAIL_TYPE.PICK_BYPASS.value(),
      // 49 - Stow Bypass4
      DETAIL_TYPE.STOW_BYPASS.value(),
      // 50
      0,
      // 51 - NIIN LOC SURVEY INVENTORY EXISTS
      DETAIL_TYPE.INFORMATION_DISPLAY.value(),
      // 52 - NIIN INQUIRY
      DETAIL_TYPE.INFORMATION_DISPLAY.value(),
      // 53 - GABF OVERLAY
      DETAIL_TYPE.INFORMATION_DISPLAY.value(),
      // 54 - CONDITION CODE EXPIRATION
      DETAIL_TYPE.INFORMATION_DISPLAY.value(),
      // 55 - MATS A5J
      DETAIL_TYPE.MATS_A5J.value(),
      // 56 - MHIF U/I Conversion - Manual Conversion
      DETAIL_TYPE.MHIF_UI_MANUAL.value(),
      // 57 - U/I Conversion Followup - Measurement Request
      DETAIL_TYPE.UI_CONVERSION_FOLLOWUP.value(),
      // 58 - LOT_SERIAL# Exception - Item picked from location different than expected
      DETAIL_TYPE.LOG_SERIAL_NUM.value(),
      //  59, 60, 61, 62, 63, 64, 65,
      0, 0, 0, 0, 0, 0, 0,
      // 66, 67, 68, 69, 70, 71, 72,
      0, 0, 0, 0, 0, 0, 0,
      // 73, 74, 75, 76, 77
      0, 0, 0, 0, 0,
      // 78 - COURIER WALK THRU IMPORT failed to create
      DETAIL_TYPE.INFORMATION_DISPLAY.value(),
      // 79
      DETAIL_TYPE.PICK_BYPASS.value(),
      // 80 - STOW_DELAY
      DETAIL_TYPE.STOW_DELAY.value(),
      //81 - Courier walk-thru
      DETAIL_TYPE.COURIER.value(),
      //82 - Walkthru error
      DETAIL_TYPE.WALKTHRU.value(),
  };

  // Index is eid, then string and detail type.

  /**
   * protected enum EXCPT {
   * NONE(0,""),
   * RECEIPT_FSC_1( 1, ""),
   * RECEIPT_DOCNO_RI_2( 2, ""),
   * RECEIPT_DOCNO_DASF_3( 3, ""),
   * RECEIPT_QTYRECV_4( 4, ""),
   * RECEIPT_QTYINDUC_5( 5, ""),
   * RECEIPT_UI_CONV_6( 6, ""),
   * RECEIPT_UI_NCON_7( 7, ""),
   * RECEIPT_CUBE_8( 8, ""),
   * RECEIPT_WGT_9( 9, ""),
   * PICK_REFUSE3_10( 10, ""),
   * PICK_REFUSE2_11( 11, ""),
   * PICK_REFUSE1_12( 12, ""),
   * PICK_BYPASS1_13( 13, ""),
   * PICK_BYPASS2_14( 14, ""),
   * PICK_BYPASS4_15( 15, ""),
   * PICK_BYPASS3_16( 16, ""),
   * STOW_BYPASS1_17( 17, ""),
   * STOW_BYPASS2_18( 18, ""),
   * STOW_REFUSE1_19( 19, ""),
   * INV_COUNT2_20( 20, ""),
   * INV_COUNT1_21( 21, ""),
   * STOW_REFUSE2_22( 22, ""),
   * STOW_BYPASS3_23( 23, ""),
   * STOW_REFUSE3_24( 24, ""),
   * NEED_REPACK_25( 25, ""),
   * GBOF_PROCESS1_26( 26, ""),
   * GBOF_PROCESS2_27( 27, ""),
   * GBOF_PROCESS3_28( 28, ""),
   * MHIF_PROCESS1_29( 29, ""),
   * FTP_ERROR1_30( 30, ""),
   * FTP_ERROR2_31( 31, ""),
   * DASF_IMPORT1_32( 32, ""),
   * DASF_IMPORT2_33( 33, ""),
   * GABF_IMPORT1_34( 34, ""),
   * GABF_IMPORT2_35( 35, ""),
   * MHIF_IMPORT1_36( 36, ""),
   * MHIF_IMPORT2_37( 37, ""),
   * GBOF_IMPORT1_38( 38, ""),
   * GBOF_IMPORT2_39( 39, ""),
   * MATS_IMPORT1_40( 40, ""),
   * MATS_IMPORT2_41( 41, ""),
   * RECEIPT_CUBE_REQ_42( 42, ""),
   * RECEIPT_WEIGHT_REQ_43( 43, ""),
   * EXPIRATION1_44( 44, ""),
   * RECEIPT_NSN_INV_45( 45, ""),
   * EXP_REMARK_46( 46, ""),
   * STOW_LOSS_D9_47( 47, ""),
   * PICK_BYPASS5_48( 48, ""),
   * STOW_BYPASS4_49( 49, ""),
   * INV_BYPASS1_50( 50, ""),
   * INV_LOCSRVY_EXP_51( 51, ""),
   * NIIN_INQUIRY_52( 52, ""),
   * GABF_PROCESS1_53( 53, ""),
   * CCEXPIRATION_54( 54, ""),
   * MATS_PROCESS1_55( 55, ""),
   * MHIF_IMPORT3_56( 56, ""),
   * MHIF_IMPORT4_57( 57, ""),
   * PICK_SRLOT_WARN_58( 58, ""),
   * COURIER_IMPORT1_59( 59, ""),
   * PICK_BYPASS6_60( 60, ""),
   * STOW_DELAY_61( 61, "");
   * <p>
   * private int eid;
   * private String label;
   * EXCPT( int n, String str ) { eid = n; label = str; }
   * protected int eid() { return eid; }
   * protected String label() { return label; }
   * }
   **/

  // Injected user info bean
  private UserInfo userInfo;

  /**************************************************************************
   * Description: This function refreshes the error list on page load
   * ***********************************************************************/
  @Override
  public void onPageLoad() {

    SysAdminImpl service = (SysAdminImpl) getStratisRootService().getSysAdmin1();

    service.refreshErrorList();
    service.refreshRefusalBuffer();
  }

  /**************************************************************************
   * Description: This function takes the selected detail page and saves the
   *              radio decision selected by the user.
   * ***********************************************************************/

  public void submitRefusalDetails(ActionEvent event) {
    if (event != null) {
      //NO-OP can't remove parameter due to adf
    }
    refusalDetailState = 0;

    SysAdminImpl service = (SysAdminImpl) getStratisRootService().getSysAdmin1();

    if (this.refuseDetailRadio1.isSelected()) {
      //Submit all rows for this SCN back into picking
      log.info("Refusal Buffer: Try to Re-Pick:  {}", rowSCN);
      if (service.deleteRefusedPicks(rowSCN)) {
        //Delete all refused picks for the given SCN
        int retVal = service.reassignRefusedPicks(rowSCN, qtyRefused);
        if (retVal != 0) {
          //Error Message
          JSFUtils.addFacesErrorMessage("Refusal", "Error - Could not re-assign refused picks.");
        }
      }
      else {
        JSFUtils.addFacesErrorMessage("Refusal", "Error - Error removing refused picks before re-assignment.");
      }
    }
    else if (this.refuseDetailRadio2.isSelected()) {
      //Allow picks so far to be processed and partially refuse picks in refusal buffer for the given SCN
      log.info("Refusal Buffer: Partial Refusal:  {}", rowSCN);
      if (rowSCN == null) {
        JSFUtils.addFacesErrorMessage("Refusal", "Error - Lost selected detail row.");
      }
      else {
        if (service.releasePartialIssue(rowSCN)) {
          //Partial Release Flag was set in packing
          if (!(service.markRefusedPicks(rowSCN))) {
            JSFUtils.addFacesErrorMessage("Refusal", "Error - Could not delete refused picks after released to packing.");
          }
        }
        else {
          JSFUtils.addFacesErrorMessage("Refusal", "Error - Unable to set partial release flag in Packing.");
        }
      }
    }
    else if (this.refuseDetailRadio3.isSelected()) {
      //Refuse entire issue for the given SCN
      log.info("Refusal Buffer: Full Refusal:  {}", rowSCN);
      if (rowSCN == null) {
        JSFUtils.addFacesErrorMessage("Refusal", "Error - Lost selected detail row.");
      }
      else {
        //Created denial transaction
        if (!service.refuseIssue(rowSCN)) {
          //Error Message
          JSFUtils.addFacesErrorMessage("Refusal", "Error - Could not refuse Issue.");
        }
      }
    }
    else {
      log.info("Refusal Buffer: Do Nothing:  {}", rowSCN);
    }
    rowSCN = null;
    qtyRefused = 0;

    service.refreshRefusalBuffer();
    pageState = 1;
  }

  /**************************************************************************
   * Description: This function takes the selected detail page and saves the
   *              radio decision selected by the user.
   * ***********************************************************************/

  public void submitDetailPage(ActionEvent event) {
    detailType = 0;
    detailState = 0;
    int uID = Integer.parseInt(JSFUtils.getManagedBeanValue("userbean.userId").toString());

    SysAdminImpl service = (SysAdminImpl) getStratisRootService().getSysAdmin1();

    if (errorId > 0) {
      if (errorId == 25 || errorId == 44 || errorId == 45 || errorId == 46 || errorId == 51 || errorId == 52 || errorId == 53 || errorId == 54 || errorId == 58 || errorId == 78) {
        //Standard Errors
        if ((this.detailRadio1.isSelected()) || (this.detailRadio16.isSelected() && errorId == 58) || (this.detailRadio22.isSelected() && errorId == 44) || (this.detailRadio22.isSelected() && errorId == 46) || (this.detailRadio23.isSelected() && errorId == 82)) {
          //Close out the current error
          if (errorId == 46) {
            //reset flag
            service.updateExpRemark(errorQueueId);
          }
          if (errorId == 52) {
            //reset under_audit flag
            service.updateUnderAudit(errorQueueId);
          }
          service.deleteError(errorQueueId, uID);
          service.refreshErrorList();
        }
      }
      else if (errorId >= 30 && errorId <= 31) {
        //SFTP Interface Errors
        if (this.detailRadio8.isSelected()) {
          service.deleteError(errorQueueId, uID);
          service.refreshErrorList();
        }
      }
      else if (errorId == 32 || errorId == 34 || errorId == 36 || errorId == 38 || errorId == 40) {
        //Interface Setup Errors
        if (this.detailRadio7.isSelected()) {
          //Close out the current error
          service.deleteError(errorQueueId, uID);
          service.refreshErrorList();
        }
      }
      else if (errorId >= 26 && errorId <= 28) {
        //GBOF Processing Errors
        if (this.detailRadio6.isSelected()) {
          //Close out the current error
          service.deleteError(errorQueueId, uID);
          service.refreshErrorList();
        }
      }
      else if (((errorId > 0) && (errorId < 10)) || (errorId == 42) || (errorId == 43)) {
        //Receiving Errors
        if (this.detailRadio5.isSelected()) {
          //Close out the current error
          service.deleteError(errorQueueId, uID);
          service.refreshErrorList();
        }
      }
      else if ((errorId > 12 && errorId < 17) || errorId == 48 || errorId == 79) {
        //Pick Bypass Errors
        if (this.detailRadio2.isSelected()) {
          //Reset Status to Pick Ready
          if (service.updatePick(errorQueueId, "PICK READY")) {
            //if the function worked, remove it from the error_queue
            service.deleteError(errorQueueId, uID);
            service.refreshErrorList();
          }
        }
        else if (this.detailRadio3.isSelected() && service.updatePick(errorQueueId, "PICK REFUSED")) {
          //Move pick to Refusal Buffer -- ONLY IF NOT A REWAREHOUSE and if the function worked, remove it from the error_queue
          service.deleteError(errorQueueId, uID);
          service.refreshErrorList();
        }
      }
      else if (errorId == 17 || errorId == 18 || errorId == 23 || errorId == 49) {
        //Stow Bypass Errors
        if (this.detailRadio4.isSelected() && service.updateStow(errorQueueId)) {
          //Reset Stow status to STOW READY and if the function worked, remove it from the error_queue
          service.deleteError(errorQueueId, uID);
          service.refreshErrorList();
        }
      }
      else if (errorId == 47) {
        //Stow Loss Error
        if (this.detailRadio9.isSelected()) {
          //Reset Status to STOWED (don't use STOWED, gives impression actually stowed)
          if (service.updateToStowLoss(errorQueueId, "STOWLOSS47", uID)) {
            //if the function worked, remove it from the error_queue
            service.deleteError(errorQueueId, uID);
            service.refreshErrorList();
          }
        }
        else if (this.detailRadio20.isSelected() || this.detailRadio10.isSelected()) {
          //Update status to STOW LOSS1
          if (service.updateToStowLoss(errorQueueId, "STOW LOSS1", uID)) {
            //if the function worked, remove it from the error_queue
            service.deleteError(errorQueueId, uID);
            service.refreshErrorList();
          }
        }
        else if (this.detailRadio21.isSelected()) {
          String sid = "";
          //Generate Pick
          sid = service.createStow(errorQueueId, String.valueOf(uID));
          if (!sid.equals("ERROR")) {
            service.deleteError(errorQueueId, uID);
            service.refreshErrorList();
            String prnStr = getWorkloadManagerService().generatePrintSIDLabel(sid, 0, 0);
            //Perform printing of each string
            if ((prnStr != null) && (prnStr.length() > 0)) {
              val labelPrintUtil = ContextUtils.getBean(LabelPrintUtil.class);
              labelPrintUtil.printLabel(userInfo, prnStr, new String[] { sid }, LabelType.STOW_LABEL);
            }
          }
        }
      }
      else if (errorId == 55) {
        //A5J Missing Condition Code
        if (this.detailRadio11.isSelected()) {
          if (service.updateMatsA5J(errorQueueId, "A")) {
            //if the function worked, remove it from the error_queue
            service.deleteError(errorQueueId, uID);
            service.refreshErrorList();
          }
        }
        else if (this.detailRadio12.isSelected()) {
          if (service.updateMatsA5J(errorQueueId, "F")) {
            //if the function worked, remove it from the error_queue
            service.deleteError(errorQueueId, uID);
            service.refreshErrorList();
          }
        }
      }
      else if (errorId == 56) {
        //Manual U/I Quantity Conversion
        if (this.detailRadio13.isSelected()) {
          if (qty == null || qty.equals("")) {
            //If the value of QTY was not entered in the GUI, display an error message
            JSFUtils.addFacesErrorMessage("Error", "Value QTY is required with this selection.");
          }
          else {
            int iQty = Integer.parseInt(qty);
            if (service.updateConvertedQuantity(errorQueueId, iQty, uID)) {
              //if the function worked, remove it from the error_queue
              service.deleteError(errorQueueId, uID);
              service.refreshErrorList();
            }
            qty = null;
          }
        }
      }
      else if (errorId == 57) {
        //U/I Conversion - Dimensional Adjustment
        if (this.detailRadio14.isSelected()) {
          if (service.clearDimensionUpdates(errorQueueId, uID)) {
            //if the function worked, remove it from the error_queue
            service.deleteError(errorQueueId, uID);
            service.refreshErrorList();
          }
        }
        else if (this.detailRadio15.isSelected()) {
          if (length == null || length.equals("") || width == null || width.equals("") || height == null || height.equals("")) {
            String convError = "The following values are required for this selection: ";
            if (length == null || length.equals("")) {
              convError += " Length";
            }
            if (width == null || width.equals("")) {
              convError += " Width";
            }
            if (height == null || height.equals("")) {
              convError += " Height";
            }
            JSFUtils.addFacesErrorMessage("Error", convError);
          }
          else {
            double iLength = Double.parseDouble(length);
            double iWidth = Double.parseDouble(width);
            double iHeight = Double.parseDouble(height);
            if (service.updateDimensions(errorQueueId, iLength, iWidth, iHeight)) {
              //Clear any other dimensional updates for this NIIN that are related.
              service.clearDimensionUpdates(errorQueueId, uID);
              //if the function worked, remove it from the error_queue
              service.deleteError(errorQueueId, uID);
              service.refreshErrorList();
            }
            length = null;
            width = null;
            height = null;
          }
        }
      }

      else if (errorId == 80) {  // STOW_DELAY
        if (this.detailRadio17.isSelected()) {
          //Close out the current error
          service.deleteError(errorQueueId, uID);
          service.refreshErrorList();
        }
        else if (this.detailRadio18.isSelected()) {
          if (service.updateToStowLoss(errorQueueId, "STOW LOSS1", uID)) {
            //if the function worked, remove it from the error_queue
            service.deleteError(errorQueueId, uID);
            service.refreshErrorList();
          }
        }
        else if (this.detailRadio19.isSelected())
          detailState = 0;
      }
      else if (errorId == 81) {
        if (this.detailRadio25.isSelected()) {
          HashMap courierIssue = service.getCourierInformation(errorQueueId);

          if (courierIssue == null) {
            JSFUtils.addFacesErrorMessage("Error has occured in Courier Walkthru");
            return;
          }
          else if (courierIssue.get("error") != null && (!courierIssue.get("error").toString().equals("N"))) {
            JSFUtils.addFacesErrorMessage(courierIssue.get("error").toString());
            return;
          }

          log.info("hardcard walkthru started...");
          int totalPickedQty = 0;
          boolean error = false;
          // make a map of the locations to be affected
          List<Map<String, Object>> listScans = new ArrayList<>();
          Map<String, Object> locmap;
          String locationIds = "";
          Object locationLabel;
          //* Sum up the total quantity needed
          int countLocationIds = 0;
          selectAllRowsInTable(getLocationTable());
          Iterator selection = getLocationTable().getSelectedRowKeys().iterator();
          try {
            while (selection.hasNext()) {
              Object rowKey = selection.next();
              getLocationTable().setRowKey(rowKey);
              JUCtrlValueBindingRef rowData = (JUCtrlValueBindingRef) getLocationTable().getRowData();
              Row row = rowData.getRow();

              //* check the qty to be moved against what is available
              Object qtyAvailAfterPendingPicksObj = row.getAttribute("Left");
              Object qtyToBeMovedObj = row.getAttribute("PickAmmount");
              Object totalQtyAtLocationObj = row.getAttribute("Qty");
              Object locationIdObj = row.getAttribute("LocationId");

              if (qtyToBeMovedObj == null) {
                JSFUtils.addFacesErrorMessage("INPUT ERROR",
                    "Required to enter a quantity to pick for every selected location.");
                return;
              }

              int moveAmount = Integer.parseInt(qtyToBeMovedObj.toString());

              //* Validate each selected row has no conflicts
              //* ignore selected walkthru picks with request of 0 or negative
              if (moveAmount > 0) {
                locationLabel = row.getAttribute("LocationLabel");
                if (qtyAvailAfterPendingPicksObj != null) {
                  //* For walkthru, you cannot pick
                  //* if pending picks leaves 0 left to pick
                  //* if the qty requesting to pick exceeds qty available at the location
                  if (Integer.parseInt(qtyAvailAfterPendingPicksObj.toString()) <
                      moveAmount) {

                    JSFUtils.addFacesErrorMessage("VALIDATION ERROR",
                        "Location " + locationLabel +
                            " - Qty requested exceeds location qty.");
                    return;
                  }
                }
                // build hashmap for use in creating hardcard walk thrus
                Object niinLocId = row.getAttribute("NiinLocId");
                log.info("qty={}, currentqty={}", qtyToBeMovedObj, totalQtyAtLocationObj);

                locmap = new HashMap<>();
                locmap.put("qty", qtyToBeMovedObj);
                locmap.put("niinlocid", niinLocId);
                locmap.put("currentqty", totalQtyAtLocationObj);
                locmap.put("locationid", locationIdObj);
                locmap.put("locationlabel", locationLabel);
                listScans.add(locmap);

                //* added 03/27/09 - add location tracking
                if (countLocationIds != 0) locationIds += ",";
                locationIds += row.getAttribute("LocationId");
                countLocationIds++;
              }

              // add this to the total picked count
              totalPickedQty += moveAmount;
              log.info("total picked quantity is now  {}", totalPickedQty);
            }
          }
          catch (Exception e) {
            log.error(e.getMessage());
            JSFUtils.addFacesErrorMessage("ERROR",
                "A system error has occurred during validation, please try again later.");
            error = true;
            return;
          }

          //* return if total walkthru picks not greater than 0, nothing to pick
          if (totalPickedQty < 1) {
            //* show message to user
            JSFUtils.addFacesErrorMessage("PICK QTY INVALID",
                "The total quantity to pick must be greater than 0: " +
                    totalPickedQty +
                    " no transaction completed.  (Ensure you have " +
                    "1) selected the checkbox for the walk thru(s) to create and " +
                    "2) entered a QTY to pick for each of the selected walk thru(s))");
            return;
          }

          int issueQty = Integer.parseInt(courierIssue.get("qty").toString());
          if (totalPickedQty != issueQty) {
            JSFUtils.addFacesErrorMessage("Mismatch between Courier Quantity (" + issueQty + ") and Selected Qty (" + totalPickedQty + ")");
            return;
          }

          //* if serial or lot control, add variables to process scope
          if (courierIssue.get("serialized").toString().equals("Y")) {
            if (!serialNumberPanel.isRendered()) {
              serialNumberPanel.setRendered(true);
              RequestContext afContext = RequestContext.getCurrentInstance();
              afContext.getPageFlowScope().put("totalPickedQty", String.valueOf(totalPickedQty));
              afContext.getPageFlowScope().put("niin_id", hashValue(courierIssue, "niin_id"));
              afContext.getPageFlowScope().put("listScans", listScans);
              afContext.getPageFlowScope().put("locationIds", locationIds);
            }
            else {
              String results;
              if (listScans != null && listScans.size() > 1) {

                // validation on the serial/lot control table
                results = getPickingAMService().validateSerialOrLotNumVOList(
                    hashValue(courierIssue, "niin_id"), hashValue(courierIssue, "niin"), Integer.toString(totalPickedQty), true, false, "A", Util.cleanString(locationIds),
                    true,
                    listScans);
              }
              else {
                // validation on the serial/lot control table
                results = getPickingAMService().validateSerialOrLotNumVOList(
                    hashValue(courierIssue, "niin_id"), hashValue(courierIssue, "niin"), Integer.toString(totalPickedQty), true, false, "A", Util.cleanString(locationIds),
                    false,
                    listScans);
              }
              String locationResults = "";
              String serialTrackNumId = "";
              if (results.length() > 0) {

                if (!results.contains("location")) {
                  JSFUtils.addFacesErrorMessage("WALK THRU ERROR", results);
                  return;
                }

                try {
                  StringTokenizer st = new StringTokenizer(results, ",");
                  while (st.hasMoreTokens()) {
                    locationResults = st.nextToken();
                    serialTrackNumId = st.nextToken();
                  }

                  if (!Util.isEmpty(locationResults))
                    JSFUtils.addFacesErrorMessage("WALK THRU WARNING", locationResults);
                }
                catch (Exception e) {
                  log.error(e.getMessage());
                  //ignore, do nothing
                }

                Object nscn = service.createIssueForWalkThru("Z0A", hashValue(courierIssue, "documentnumber"),
                    String.valueOf(totalPickedQty), "W",
                    hashValue(courierIssue, "issueprioritydesignator"),
                    hashValue(courierIssue, "issueprioritygroup"), hashValue(courierIssue, "customerid"), hashValue(courierIssue, "niinid"), null,
                    hashValue(courierIssue, "supaddress"),
                    hashValue(courierIssue, "fundcode"), hashValue(courierIssue, "mediastatuscode"), null,
                    hashValue(courierIssue, "costJon"),
                    hashValue(courierIssue, "rdd"), hashValue(courierIssue, "niin"), "A", true);
                if (nscn == null) {
                  // give them an error
                  JSFUtils.addFacesErrorMessage("WALK THRU",
                      "Unable to create Walk Thru - " +
                          "Possible reason Duplicate Document Numbers are not allowed.");
                  return;
                }

                log.info("do a hardcard for scn  {}", nscn);
                // call the workload function to create the records and spool files
                int result = getPickingAMService().hardcardWalkThru(nscn.toString(), listScans,
                    Integer.parseInt(JSFUtils.getManagedBeanValue("userbean.userId").toString()), true, false,
                    hashValue(courierIssue, "niinid"));
                if (result >= 0) {
                  // clear all the feilds and say the transaction was completed
                  //* generate the warning exception for serial numbers used from locations not their own
                  JSFUtils.addFacesInformationMessage("Walk-Thru successful.");
                  if (results.length() > 0 && results.contains("location")) {
                    WorkLoadManagerImpl wlm = getWorkloadManagerModule();
                    if (!Util.isEmpty(serialTrackNumId)) {
                      wlm.createErrorQueueRecord("58", "SRL_LOT_NUM_TRACK_ID", serialTrackNumId, "1", null);
                    }
                  }
                }
                else {
                  // give them an error
                  JSFUtils.addFacesErrorMessage("WALK THRU",
                      "Unable to create Walk Thru - An error has occurred while trying to create " + hashValue(courierIssue, "documentnumber") + ".");
                  log.info("hardcardWalkThru result is  {}", result);
                  return;
                }
              }
            }
          }
          else {
            if (!this.validateDocumentNumber(hashValue(courierIssue, "documentnumber")) || !this.validateFundCode(hashValue(courierIssue, "fundcode")) ||
                !this.validateMS(hashValue(courierIssue, "mediastatuscode")) || !this.validateRdd(hashValue(courierIssue, "rdd")) || !this.validateRic(hashValue(courierIssue, "ricFrom")) ||
                !validateCostJON(hashValue(courierIssue, "costJon"))) {
              return;
            }

            //* call the create issue function
            Object nscn = service.createIssueForWalkThru("Z0A", hashValue(courierIssue, "documentnumber"),
                String.valueOf(totalPickedQty), "W",
                hashValue(courierIssue, "issueprioritydesignator"),
                hashValue(courierIssue, "issueprioritygroup"), hashValue(courierIssue, "customerid"), hashValue(courierIssue, "niinid"), null,
                hashValue(courierIssue, "supaddress"),
                hashValue(courierIssue, "fundcode"), hashValue(courierIssue, "mediastatuscode"), null,
                hashValue(courierIssue, "costJon"),
                hashValue(courierIssue, "rdd"), hashValue(courierIssue, "niin"), "A", true);

            if (nscn == null) {
              JSFUtils.addFacesErrorMessage("WALK THRU",
                  "Unable to create Walk Thru - " +
                      "Possible reason Duplicate Document Numbers are not allowed.");
              return;
            }

            log.info("do a hardcard for scn  {}", nscn);
            // call the workload function to create the records and spool files
            int result = getPickingAMService().hardcardWalkThru(nscn.toString(), listScans,
                Integer.parseInt(JSFUtils.getManagedBeanValue("userbean.userId").toString()),
                false, false, null);
            if (result >= 0) {
              // clear all the feilds and say the transaction was completed
              JSFUtils.addFacesInformationMessage("Walk-Thru successful.");
            }
            else {
              // give them an error
              JSFUtils.addFacesErrorMessage("WALK THRU",
                  "Unable to create Walk Thru - An error has occurred while trying to create " + hashValue(courierIssue, "documentnumber") + ".");
              log.info("hardcardWalkThru result is  {}", result);

              return;
            }

            // clear all the stuff
          }
        }
        else if (this.detailRadio24.isSelected()) {
          //Create a history issue
          String message = service.rejectCourier(errorQueueId, uID);
          if (message.isEmpty()) {
            service.deleteError(errorQueueId, uID);
            service.refreshErrorList();
          }
          else {
            JSFUtils.addFacesErrorMessage(message);
          }
        }
      }

      exceptionFilter.setDisabled(false);
    }
  }

  public String hashValue(HashMap map, String key) {
    if (map.get(key) == null)
      return "";
    else
      return map.get(key).toString();
  }

  private void selectAllRowsInTable(RichTable rt) {
    RowKeySet rks = new RowKeySetImpl();
    CollectionModel model = (CollectionModel) rt.getValue();
    int rowcount = model.getRowCount();

    for (int i = 0; i < rowcount; i++) {
      model.setRowIndex(i);
      Object key = model.getRowKey();
      rks.add(key);
    }
    rt.setSelectedRowKeys(rks);
  }

  public boolean validateDocumentNumber(String docStr) {
    boolean retVal = true;
    if (docStr.length() != MAX_LEN_DOCUMENT_NUMBER) {
      JSFUtils.addFacesErrorMessage("INVALID LENGTH", "Document Number must be " + MAX_LEN_DOCUMENT_NUMBER + " character(s)");
      retVal = false;
    }
    else if (RegUtils.isNotAlphaNumeric(docStr)) {
      JSFUtils.addFacesErrorMessage("INVALID FIELD", "Document Number From must be alphanumeric characters only");
      retVal = false;
    }

    if (retVal) {
      SysAdminImpl service = getSysAdminModule();
      String msg = service.checkDuplicateDocNumInIssue(docStr);
      if (msg != null && msg.length() > 0) {
        JSFUtils.addFacesErrorMessage(msg);
        retVal = false;
      }
    }

    return retVal;
  }

  public boolean validateRic(String ricStr) {
    boolean retVal = true;
    if (ricStr.length() != MAX_LEN_RIC_FROM) {
      JSFUtils.addFacesErrorMessage("INVALID LENGTH", "RIC From must be " + MAX_LEN_RIC_FROM + " character(s)");
      retVal = false;
    }
    else if (RegUtils.isNotAlphaNumeric(ricStr)) {
      JSFUtils.addFacesErrorMessage("INVALID FIELD", "RIC From must be alphanumeric characters only");
      retVal = false;
    }
    return retVal;
  }

  /**
   * Validate Media and Status code.  Media and Status Code is not required.  Must be 2 characters and may be alphanumeric.
   *
   * @return boolean
   */
  public boolean validateMS(String msStr) {
    boolean retVal = true;
    if (msStr.length() != MAX_LEN_MEDIAANDSTATUS_CODE) {
      JSFUtils.addFacesErrorMessage("INVALID LENGTH", "Media and Status Code must be " + MAX_LEN_MEDIAANDSTATUS_CODE + " character(s)");
      retVal = false;
    }
    else if (RegUtils.isNotAlphaNumeric(msStr)) {
      JSFUtils.addFacesErrorMessage("INVALID FIELD", "Media and Status Code must be alphanumeric characters only");
      retVal = false;
    }

    return retVal;
  }

  /**
   * Validate Fund code.  Fund Code is required.  Must be 2 characters and must be alphabet.
   *
   * @return boolean
   */
  public boolean validateFundCode(String fundStr) {
    boolean retVal = true;
    if (fundStr.length() != MAX_LEN_FUND_CODE) {
      JSFUtils.addFacesErrorMessage("INVALID LENGTH", "Fund Code must be " + MAX_LEN_FUND_CODE + " character(s)");
      retVal = false;
    }
    else if (RegUtils.isNotAlpha(fundStr)) {
      JSFUtils.addFacesErrorMessage("INVALID FIELD", "Fund Code must be alphabetic characters only");
      retVal = false;
    }
    return retVal;
  }

  /**
   * Validate Required Delivery Date.  RDD is required.
   * Must be 3 characters and may be alphanumeric (e.g.N01, E##),
   * but is generally a julian date.
   *
   * @return boolean
   */
  public boolean validateRdd(String rddStr) {
    boolean retVal = true;
    if (rddStr.length() != MAX_LEN_RDD) {
      JSFUtils.addFacesErrorMessage("INVALID LENGTH", "Required Delivery Date (RDD) must be " + MAX_LEN_RDD + " alphanumeric character(s)");
      retVal = false;
    }
    else if (RegUtils.isNotAlphaNumeric(rddStr)) {
      JSFUtils.addFacesErrorMessage("INVALID FIELD", "Required Delivery Date (RDD) must be alphanumeric characters only");
      retVal = false;
    }
    return retVal;
  }

  public boolean validateCostJON(String cjStr) {
    boolean retVal = true;
    if (cjStr.length() != MAX_LEN_COST_JON) {
      JSFUtils.addFacesErrorMessage("INVALID FIELD", "Cost JON must be " + MAX_LEN_COST_JON + " alphanumeric character(s)");
      retVal = false;
    }
    else if (RegUtils.isNotAlphaNumeric(cjStr)) {
      JSFUtils.addFacesErrorMessage("INVALID FIELD", "Cost JON must be alphanumeric characters only");
      retVal = false;
    }
    return retVal;
  }

  public void modifyExceptionState(ActionEvent action) {
    pageState = 0;

    detailState = 0;
    detailType = 0;
    errorId = 0;
    errorQueueId = 0;
    selectedErrorLabel = "";
    errorDetails = "";

    SysAdminImpl service = (SysAdminImpl) getStratisRootService().getSysAdmin1();

    service.refreshErrorList();
  }

  public void modifyRefusalState(ActionEvent action) {
    pageState = 1;

    refusalDetailState = 0;

    SysAdminImpl service = (SysAdminImpl) getStratisRootService().getSysAdmin1();

    service.refreshRefusalBuffer();
  }

  /**
   * Coded to use current selection.
   */
  public void getRefusalDetails(ActionEvent action) {
    Row curRow = null;
    RichTable table = getRefusalTable();
    int qty = 0;
    SysAdminImpl service = (SysAdminImpl) getStratisRootService().getSysAdmin1();
    Object origRowKey = table.getRowKey();  // Grab original key
    RowKeySet keySet = table.getSelectedRowKeys();

    try {
      if (keySet != null) {
        Iterator iter = keySet.iterator();
        if (iter != null && iter.hasNext()) {
          Object rowKey = iter.next();
          table.setRowKey(rowKey);
          JUCtrlValueBindingRef rowData = (JUCtrlValueBindingRef) table.getRowData(rowKey);
          curRow = rowData.getRow();
        }
      }
    }
    finally {
      table.setRowKey(origRowKey);
    }

    if (curRow != null) {
      rowSCN = curRow.getAttribute("SCN").toString();

      if (service.getRefusalDetails(rowSCN)) {
        refusalDetailState = 1;
        qtyRefused = Integer.parseInt(curRow.getAttribute("QTY_REFUSED").toString());
        qty = Integer.parseInt(curRow.getAttribute("QTY").toString());
      }
      else
        rowSCN = null;
    }

    if (qty == qtyRefused) {
      refuseDetailRadio3.setRendered(true);
      refuseDetailRadio2.setRendered(false);
      refuseDetailRadio1.setSelected(false);
      refuseDetailRadio2.setSelected(false);
      refuseDetailRadio3.setSelected(false);
      refuseDetailRadio4.setSelected(true);
    }
    else {
      refuseDetailRadio3.setRendered(false);
      refuseDetailRadio2.setRendered(true);
      refuseDetailRadio1.setSelected(false);
      refuseDetailRadio2.setSelected(false);
      refuseDetailRadio3.setSelected(false);
      refuseDetailRadio4.setSelected(true);
    }
  }

  public void applyExceptionFilter(ActionEvent action) {

    log.trace("()");
    SysAdminImpl service = (SysAdminImpl) getStratisRootService().getSysAdmin1();
    String filterString = Util.cleanString(exceptionFilterData.getValue());
    log.trace("Filter string: {}", filterString);

    if (service != null) {
      service.filterExceptionData(filterString);
    }
    else {
      log.error("Exception processing service - filter not found.");
    }
  }

  /**
   * This is implemented to get the current row.
   */
  public void getExceptionDetails(ActionEvent action) {
    Row curRow = null;
    RichTable table = getExceptionTable();
    SysAdminImpl service = (SysAdminImpl) getStratisRootService().getSysAdmin1();

    exceptionFilter.setDisabled(true);

    Object origRowKey = table.getRowKey();         // Preserve current
    RowKeySet keySet = table.getSelectedRowKeys();

    try {
      if (keySet != null) {
        Iterator iter = keySet.iterator();
        if (iter != null && iter.hasNext()) {
          Object rowKey = iter.next();
          table.setRowKey(rowKey);
          curRow = (Row) table.getRowData(rowKey);
        }
      }
    }
    finally {
      table.setRowKey(origRowKey);
    }

    if (curRow != null) {
      String idStr = curRow.getAttribute("Id").toString();
      if (idStr != null) {

        log.trace("eid: {}", idStr);

        errorQueueId = Integer.parseInt(idStr);
        //Retrieve errorId for the given exception
        HashMap errMap = service.getDetailType(errorQueueId);

        if (Integer.parseInt(errMap.get("eid").toString()) > 0) {
          detailState = DETAIL_TYPE.INFORMATION_DISPLAY.value(); // 1;
          //Display the Exception Details

          errorId = Integer.parseInt(errMap.get("eid").toString());
          String title = curRow.getAttribute("Title").toString();
          StringBuilder buf = new StringBuilder(title).append(". ");
          if (errorId < detailAideMaxValue) {
            buf.append(detailAide[errorId]);
            detailType = detailTypes[errorId];
            detailState = 1;
          }
          else {
            detailState = 0; // Default
          }

          switch (errorId) {

            case 13: // 13 - Pick Bypass1 - Need Assistance
              buf = new StringBuilder(curRow.getAttribute("ErrorDescription").toString()).append(".");
              if ((errMap.get("issue_type") == null ? "" : errMap.get("issue_type").toString()).equals("R")) {
                detailRadio3.setText("Remove Rewarehoused Pick and Stow from workload.");
                buf.append(" (Rewarehoused Pick)");
              }
              else {
                detailRadio3.setText("Send pick to Refusal Buffer, remove from Error List, and close Detail window.");
              }
              break;

            case 14: // 14 - Pick Bypass2 - Possible Partial Issue
              buf = new StringBuilder(curRow.getAttribute("ErrorDescription").toString()).append(".");
              if ((errMap.get("issue_type") == null ? "" : errMap.get("issue_type").toString()).equals("R")) {
                detailRadio3.setText("Remove Rewarehoused Pick and Stow from workload.");
                buf.append(" (Rewarehoused Pick)");
              }
              else {
                detailRadio3.setText("Send pick to Refusal Buffer, remove from Error List, and close Detail window.");
              }
              break;

            case 15: // 15 - Pick Bypass4 - Possible Denial
              buf = new StringBuilder(curRow.getAttribute("ErrorDescription").toString()).append(".");
              if ((errMap.get("issue_type") == null ? "" : errMap.get("issue_type").toString()).equals("R")) {
                detailRadio3.setText("Remove Rewarehoused Pick and Stow from workload.");
                buf.append(" (Rewarehoused Pick)");
              }
              else {
                detailRadio3.setText("Send pick to Refusal Buffer, remove from Error List, and close Detail window.");
              }
              break;

            case 16: // 16 - Pick Bypass3 - Disparity of U/I, FSC, or NIIN
              buf = new StringBuilder(curRow.getAttribute("ErrorDescription").toString()).append(".");
              if ((errMap.get("issue_type") == null ? "" : errMap.get("issue_type").toString()).equals("R")) {
                detailRadio3.setText("Remove Rewarehoused Pick and Stow from workload.");
                buf.append(" (Rewarehoused Pick)");
              }
              else {
                detailRadio3.setText("Send pick to Refusal Buffer, remove from Error List, and close Detail window.");
              }
              break;

            case 17: // 17 - Stow Bypass1 - Another Niin at Location
              buf = new StringBuilder(curRow.getAttribute("ErrorDescription").toString()).append(".");
              break;

            case 18: // 18 - Stow Bypass2 - NIIN Condition Problem
              buf = new StringBuilder(curRow.getAttribute("ErrorDescription").toString()).append(".");
              break;

            case 23: // 23 - Stow Bypass3 - Possible re-assignment
              buf = new StringBuilder(curRow.getAttribute("ErrorDescription").toString()).append(".");
              break;

            case 44: // 44 - NIIN Expiration Exception
              buf = new StringBuilder("Item due to expire within the next 10 days.");
              detailType = DETAIL_TYPE.EXPIRATION.value(); // 15
              break;

            case 45: // 45 - RECEIPT ERROR - NIIN does not exist
              buf = new StringBuilder("NIIN not in STRATIS. Run MHIF Import or MHIF Immediate.");
              break;

            case 46: // 46 - SHELFLIFE
              buf = new StringBuilder("Could not extend expiration date of item. Change CC or extend expiration date to complete Shelf LIfe task in Inventory module.");
              detailType = DETAIL_TYPE.EXPIRATION.value();
              break;

            case 47: // 47 - Stow Loss
              detailType = DETAIL_TYPE.STOW_LOSS_GCSS.value(); // 14
              break;

            case 48: // 48 - Pick Bypass5 - Location Locked
              buf = new StringBuilder(curRow.getAttribute("ErrorDescription").toString()).append(".");
              if ((errMap.get("issue_type") == null ? "" : errMap.get("issue_type").toString()).equals("R")) {
                detailRadio3.setText("Remove Rewarehoused Pick and Stow from workload.");
                buf.append(" (Rewarehoused Pick)");
              }
              else {
                detailRadio3.setText("Send pick to Refusal Buffer, remove from Error List, and close Detail window.");
              }
              break;

            case 49: // 49 - Stow Bypass4 - Location Locked
              buf = new StringBuilder(curRow.getAttribute("ErrorDescription").toString()).append(".");
              break;

            case 51: // 51 - NIIN LOC SURVEY INVENTORY EXISTS
              buf.append(errMap.get("niin")).append(" to location ").append(errMap.get("location"));
              buf.append(". Inventory is already triggered. Recommend REWAREHOUSING for added NIIN(s) once inventory is completed.");
              break;

            case 78: // 78 - COURIER WALK THRU IMPORT failed to create
              buf.append(Util.cleanString(curRow.getAttribute("Notes"))).append(". Issue not created - See Logs for other possible issues not created.");
              selectedErrorLabel = curRow.getAttribute("Title").toString();
              break;

            case 79:
              if ((errMap.get("issue_type") == null ? "" : errMap.get("issue_type").toString()).equals("R")) {
                detailRadio3.setText("Remove Rewarehoused Pick and Stow from workload.");
                buf.append(" (Rewarehoused Pick)");
              }
              else {
                detailRadio3.setText("Send pick to Refusal Buffer, remove from Error List, and close Detail window.");
              }
              break;
            case 81:
              buf = new StringBuilder(curRow.getAttribute("ErrorDescription").toString()).append(".");
              if ((errMap.get("error") != null) && (errMap.get("error").toString().contains("ERROR"))) {
                JSFUtils.addFacesErrorMessage("Error", errMap.get("error").toString());
              }
              serialNumberPanel.setRendered(false);
              break;
            case 82:
              buf = new StringBuilder(curRow.getAttribute("ErrorDescription").toString()).append(".");
              break;
          }
          errorDetails = buf.toString();
        }
        else {
          //Display error message
          JSFUtils.addFacesErrorMessage("Error", "Looks like the transaction that generated this EXCEPTION has already been processed and moved to history");
        }
      }
      else {
        //Display error message
        JSFUtils.addFacesErrorMessage("Row Selection", "Error retrieving row id / No row selected.");
        exceptionFilter.setDisabled(false);
      }
    }
    else {
      //Display error message
      JSFUtils.addFacesErrorMessage("Row Selection", "Error retrieving row key / No row selected.");
      exceptionFilter.setDisabled(false);
    }
  }

  public void addSerialLotNum(ActionEvent actionEvent) {
    PickingAMImpl service = getPickingAMService();
    RequestContext afContext = RequestContext.getCurrentInstance();
    String result = "";
    if ((this.getSerialOrLotNum().getValue() == null) && ((this.getStartSerial().getValue() == null) || (this.getEndSerial().getValue() == null))) {
      JSFUtils.addFacesErrorMessage("INVALID INPUT",
          "Please enter " + "serial number" + ".");
      return;
    }

    Object niinId = afContext.getPageFlowScope().get("niin_id");
    Object totalPickedQty = afContext.getPageFlowScope().get("totalPickedQty");
    if (service.getSerialOrLotNumList1().getEstimatedRowCount() > Integer.parseInt(totalPickedQty.toString())) {
      JSFUtils.addFacesErrorMessage("SERIAL OR LOT NUMBER",
          "You may not exceed the total picked quantity for this Hardcard Walk Thru.");
      return;
    }

    if (niinId != null) {
      String locationIds = Util.cleanString(afContext.getPageFlowScope().get("locationIds"));
      if (this.getSerialOrLotNum().getValue() != null) {
        result = service.addToSerialLotList(Util.trimUpperCaseClean(getSerialOrLotNum().getValue()),
            niinId.toString(), "1",
            true,
            locationIds,
            false);
      }
      else {
        ArrayList serialList = new ArrayList();
        result = getWorkloadManagerService().generateSerialRange(getStartSerial().getValue().toString(), getEndSerial().getValue().toString(), serialList);
        if (result.length() <= 0) {
          String tempErr = "";
          for (int i = 0; i < serialList.size(); i++) {
            tempErr = service.addToSerialLotList(
                serialList.get(i).toString().trim().toUpperCase(),
                niinId.toString(), "1",
                true,
                locationIds,
                false);
            if (!tempErr.isEmpty()) {
              if (result.isEmpty()) {
                result = tempErr;
              }
              else {
                result = result + "\n" + tempErr;
              }
            }
          }
          this.getEndSerial().setValue("");
          this.getStartSerial().setValue("");
        }
      }

      SerialOrLotNumListImpl vo = service.getSerialOrLotNumList1();
      vo.setSortBy("SerialOrLoNum");
      vo.setQueryMode(ViewObject.QUERY_MODE_SCAN_VIEW_ROWS);
      vo.executeQuery();
    }
    if (result.length() > 0) {
      JSFUtils.addFacesErrorMessage("ERROR", result);
    }
    this.getSerialOrLotNum().setValue("");
  }

  public void deleteSerialLotNum(ActionEvent actionEvent) {
    this.getPickingAMService().getSerialOrLotNumList1().removeCurrentRowFromCollection();
  }

  @Override
  public PickingAMImpl getPickingAMService() {
    PickingAMImpl service = null;
    try {
      service = (PickingAMImpl) getStratisRootService().getPickingAM1();
    }
    catch (Exception e) {
      log.error("Unable to obtain a handle to the service.");
      AdfLogUtility.logException(e);
    }
    return service;
  }

  // Setter for injected user info bean.

  public void setUserInfo(UserInfo bean) {
    userInfo = bean;
  }

  public void setDetailState(Object detailState) {
    this.detailState = detailState;
  }

  public Object getDetailState() {
    return detailState;
  }

  public void setDetailType(Object detailType) {
    this.detailType = detailType;
  }

  public Object getDetailType() {
    return detailType;
  }

  public void setSelectedErrorLabel(String selectedErrorLabel) {
    this.selectedErrorLabel = selectedErrorLabel;
  }

  public String getSelectedErrorLabel() {
    return selectedErrorLabel;
  }

  public void setErrorDetails(String errorDetails) {
    this.errorDetails = errorDetails;
  }

  public String getErrorDetails() {
    return errorDetails;
  }

  public void setPageState(Object pageState) {
    this.pageState = pageState;
  }

  public Object getPageState() {
    return pageState;
  }

  public void setRefusalDetailState(Object refusalDetailState) {
    this.refusalDetailState = refusalDetailState;
  }

  public Object getRefusalDetailState() {
    return refusalDetailState;
  }

  @Override
  public GCSSMCTransactionsImpl getGCSSMCAMService() {
    try {
      return (GCSSMCTransactionsImpl) getStratisRootService().getGCSSMCTransactions1();
    }
    catch (Exception e) {
      log.error(e.getMessage());
    }
    return null;
  }

  public void setLength(String length) {
    this.length = length;
  }

  public String getLength() {
    return length;
  }

  public void setWidth(String width) {
    this.width = width;
  }

  public String getWidth() {
    return width;
  }

  public void setHeight(String height) {
    this.height = height;
  }

  public String getHeight() {
    return height;
  }

  public void setQty(String qty) {
    this.qty = qty;
  }

  public String getQty() {
    return qty;
  }

  public void setErrorId(int eid) {
    errorId = eid;
  }

  public int getErrorId() {
    return errorId;
  }

  public void setDetailButton(RichButton detailButton) {
    this.detailButton = detailButton;
  }

  public RichButton getDetailButton() {
    return detailButton;
  }

  public void setRefusalDetailButton(RichButton refusalDetailButton) {
    this.refusalDetailButton = refusalDetailButton;
  }

  public RichButton getRefusalDetailButton() {
    return refusalDetailButton;
  }

  public void setSaveDetailPage(RichCommandButton saveDetailPage) {
    this.saveDetailPage = saveDetailPage;
  }

  public RichCommandButton getSaveDetailPage() {
    return saveDetailPage;
  }

  public void setSaveDetailPage2(RichCommandButton saveDetailPage2) {
    this.saveDetailPage2 = saveDetailPage2;
  }

  public RichCommandButton getSaveDetailPage2() {
    return saveDetailPage2;
  }

  public void setSaveDetailPage3(RichCommandButton saveDetailPage3) {
    this.saveDetailPage3 = saveDetailPage3;
  }

  public RichCommandButton getSaveDetailPage3() {
    return saveDetailPage3;
  }

  public void setSaveDetailPage4(RichCommandButton saveDetailPage4) {
    this.saveDetailPage4 = saveDetailPage4;
  }

  public RichCommandButton getSaveDetailPage4() {
    return saveDetailPage4;
  }

  public void setSaveDetailPage5(RichCommandButton saveDetailPage5) {
    this.saveDetailPage5 = saveDetailPage5;
  }

  public RichCommandButton getSaveDetailPage5() {
    return saveDetailPage5;
  }

  public void setSaveDetailPage6(RichCommandButton saveDetailPage6) {
    this.saveDetailPage6 = saveDetailPage6;
  }

  public RichCommandButton getSaveDetailPage6() {
    return saveDetailPage6;
  }

  public void setSaveDetailPage7(RichCommandButton saveDetailPage7) {
    this.saveDetailPage7 = saveDetailPage7;
  }

  public RichCommandButton getSaveDetailPage7() {
    return saveDetailPage7;
  }

  public void setSaveDetailPage8(RichCommandButton saveDetailPage8) {
    this.saveDetailPage8 = saveDetailPage8;
  }

  public RichCommandButton getSaveDetailPage8() {
    return saveDetailPage8;
  }

  public void setSaveDetailPage9(RichCommandButton saveDetailPage9) {
    this.saveDetailPage9 = saveDetailPage9;
  }

  public RichCommandButton getSaveDetailPage9() {
    return saveDetailPage9;
  }

  public void setSaveDetailPage10(RichCommandButton saveDetailPage10) {
    this.saveDetailPage10 = saveDetailPage10;
  }

  public RichCommandButton getSaveDetailPage10() {
    return saveDetailPage10;
  }

  public void setSaveDetailPage11(RichCommandButton saveDetailPage11) {
    this.saveDetailPage11 = saveDetailPage11;
  }

  public RichCommandButton getSaveDetailPage11() {
    return saveDetailPage11;
  }

  public void setSaveDetailPage12(RichCommandButton saveDetailPage12) {
    this.saveDetailPage12 = saveDetailPage12;
  }

  public RichCommandButton getSaveDetailPage12() {
    return saveDetailPage12;
  }

  public void setSaveDetailPage13(RichCommandButton saveDetailPage13) {
    this.saveDetailPage13 = saveDetailPage13;
  }

  public RichCommandButton getSaveDetailPage13() {
    return saveDetailPage13;
  }

  public void setSaveDetailPage14(RichCommandButton saveDetailPage14) {
    this.saveDetailPage14 = saveDetailPage14;
  }

  public RichCommandButton getSaveDetailPage14() {
    return saveDetailPage14;
  }

  public void setSaveDetailPage15(RichCommandButton saveDetailPage15) {
    this.saveDetailPage15 = saveDetailPage15;
  }

  public RichCommandButton getSaveDetailPage15() {
    return saveDetailPage15;
  }

  public void setSaveDetailPage16(RichCommandButton saveDetailPage16) {
    this.saveDetailPage16 = saveDetailPage16;
  }

  public RichCommandButton getSaveDetailPage16() {
    return saveDetailPage16;
  }

  public void setDetailRadio1(RichSelectBooleanRadio detailRadio1) {
    this.detailRadio1 = detailRadio1;
  }

  public RichSelectBooleanRadio getDetailRadio1() {
    return detailRadio1;
  }

  public void setDetailRadio2(RichSelectBooleanRadio detailRadio2) {
    this.detailRadio2 = detailRadio2;
  }

  public RichSelectBooleanRadio getDetailRadio2() {
    return detailRadio2;
  }

  public void setDetailRadio3(RichSelectBooleanRadio detailRadio3) {
    this.detailRadio3 = detailRadio3;
  }

  public RichSelectBooleanRadio getDetailRadio3() {
    return detailRadio3;
  }

  public void setDetailRadio4(RichSelectBooleanRadio detailRadio4) {
    this.detailRadio4 = detailRadio4;
  }

  public RichSelectBooleanRadio getDetailRadio4() {
    return detailRadio4;
  }

  public void setDetailRadio5(RichSelectBooleanRadio detailRadio5) {
    this.detailRadio5 = detailRadio5;
  }

  public RichSelectBooleanRadio getDetailRadio5() {
    return detailRadio5;
  }

  public void setDetailRadio6(RichSelectBooleanRadio detailRadio6) {
    this.detailRadio6 = detailRadio6;
  }

  public RichSelectBooleanRadio getDetailRadio6() {
    return detailRadio6;
  }

  public void setDetailRadio7(RichSelectBooleanRadio detailRadio7) {
    this.detailRadio7 = detailRadio7;
  }

  public RichSelectBooleanRadio getDetailRadio7() {
    return detailRadio7;
  }

  public void setDetailRadio8(RichSelectBooleanRadio detailRadio8) {
    this.detailRadio8 = detailRadio8;
  }

  public RichSelectBooleanRadio getDetailRadio8() {
    return detailRadio8;
  }

  public void setDetailRadio9(RichSelectBooleanRadio detailRadio9) {
    this.detailRadio9 = detailRadio9;
  }

  public RichSelectBooleanRadio getDetailRadio9() {
    return detailRadio9;
  }

  public void setDetailRadio10(RichSelectBooleanRadio detailRadio10) {
    this.detailRadio10 = detailRadio10;
  }

  public RichSelectBooleanRadio getDetailRadio10() {
    return detailRadio10;
  }

  public void setDetailRadio11(RichSelectBooleanRadio detailRadio11) {
    this.detailRadio11 = detailRadio11;
  }

  public RichSelectBooleanRadio getDetailRadio11() {
    return detailRadio11;
  }

  public void setDetailRadio12(RichSelectBooleanRadio detailRadio12) {
    this.detailRadio12 = detailRadio12;
  }

  public RichSelectBooleanRadio getDetailRadio12() {
    return detailRadio12;
  }

  public void setDetailRadio13(RichSelectBooleanRadio detailRadio13) {
    this.detailRadio13 = detailRadio13;
  }

  public RichSelectBooleanRadio getDetailRadio13() {
    return detailRadio13;
  }

  public void setDetailRadio14(RichSelectBooleanRadio detailRadio14) {
    this.detailRadio14 = detailRadio14;
  }

  public RichSelectBooleanRadio getDetailRadio14() {
    return detailRadio14;
  }

  public void setDetailRadio15(RichSelectBooleanRadio detailRadio15) {
    this.detailRadio15 = detailRadio15;
  }

  public RichSelectBooleanRadio getDetailRadio15() {
    return detailRadio15;
  }

  public void setDetailRadio16(RichSelectBooleanRadio detailRadio16) {
    this.detailRadio16 = detailRadio16;
  }

  public RichSelectBooleanRadio getDetailRadio16() {
    return detailRadio16;
  }

  public void setRefuseDetailRadio1(RichSelectBooleanRadio refuseDetailRadio1) {
    this.refuseDetailRadio1 = refuseDetailRadio1;
  }

  public RichSelectBooleanRadio getRefuseDetailRadio1() {
    return refuseDetailRadio1;
  }

  public void setRefuseDetailRadio2(RichSelectBooleanRadio refuseDetailRadio2) {
    this.refuseDetailRadio2 = refuseDetailRadio2;
  }

  public RichSelectBooleanRadio getRefuseDetailRadio2() {
    return refuseDetailRadio2;
  }

  public void setRefuseDetailRadio3(RichSelectBooleanRadio refuseDetailRadio3) {
    this.refuseDetailRadio3 = refuseDetailRadio3;
  }

  public RichSelectBooleanRadio getRefuseDetailRadio3() {
    return refuseDetailRadio3;
  }

  public void setRefuseDetailRadio4(RichSelectBooleanRadio refuseDetailRadio4) {
    this.refuseDetailRadio4 = refuseDetailRadio4;
  }

  public RichSelectBooleanRadio getRefuseDetailRadio4() {
    return refuseDetailRadio4;
  }

  public void setExceptionFilterData(RichSelectOneChoice exceptionFilterData) {
    this.exceptionFilterData = exceptionFilterData;
  }

  public RichSelectOneChoice getExceptionFilterData() {
    return exceptionFilterData;
  }

  public void setSaveRefuseDetails(RichCommandButton saveRefuseDetails) {
    this.saveRefuseDetails = saveRefuseDetails;
  }

  public RichCommandButton getSaveRefuseDetails() {
    return saveRefuseDetails;
  }

  public void setExceptionFilter(RichButton exceptionFilter) {
    this.exceptionFilter = exceptionFilter;
  }

  public RichButton getExceptionFilter() {
    return exceptionFilter;
  }

  public void setExceptionTable(RichTable exceptionTable) {
    this.exceptionTable = exceptionTable;
  }

  public RichTable getExceptionTable() {
    return exceptionTable;
  }

  public void setRefusalTable(RichTable refusalTable) {
    this.refusalTable = refusalTable;
  }

  public RichTable getRefusalTable() {
    return refusalTable;
  }

  public void setDetailRadio17(RichSelectBooleanRadio detailRadio17) {
    this.detailRadio17 = detailRadio17;
  }

  public RichSelectBooleanRadio getDetailRadio17() {
    return detailRadio17;
  }

  public void setDetailRadio18(RichSelectBooleanRadio detailRadio18) {
    this.detailRadio18 = detailRadio18;
  }

  public RichSelectBooleanRadio getDetailRadio18() {
    return detailRadio18;
  }

  public void setDetailRadio19(RichSelectBooleanRadio detailRadio19) {
    this.detailRadio19 = detailRadio19;
  }

  public RichSelectBooleanRadio getDetailRadio19() {
    return detailRadio19;
  }

  public void setDetailRadio20(RichSelectBooleanRadio detailRadio20) {
    this.detailRadio20 = detailRadio20;
  }

  public RichSelectBooleanRadio getDetailRadio20() {
    return detailRadio20;
  }

  public void setDetailRadio21(RichSelectBooleanRadio detailRadio21) {
    this.detailRadio21 = detailRadio21;
  }

  public void setDetailRadio22(RichSelectBooleanRadio detailRadio22) {
    this.detailRadio22 = detailRadio22;
  }

  public void setDetailRadio23(RichSelectBooleanRadio detailRadio23) {
    this.detailRadio23 = detailRadio23;
  }

  public void setDetailRadio24(RichSelectBooleanRadio detailRadio24) {
    this.detailRadio24 = detailRadio24;
  }

  public void setDetailRadio25(RichSelectBooleanRadio detailRadio25) {
    this.detailRadio25 = detailRadio25;
  }

  public void setSerialNumberPanel(RichPanelGroupLayout serialNumberPanel2) {
    this.serialNumberPanel = serialNumberPanel2;
  }

  public RichSelectBooleanRadio getDetailRadio21() {
    return detailRadio21;
  }

  public RichSelectBooleanRadio getDetailRadio22() {
    return detailRadio22;
  }

  public RichSelectBooleanRadio getDetailRadio23() {
    return detailRadio23;
  }

  public RichSelectBooleanRadio getDetailRadio24() {
    return detailRadio24;
  }

  public RichSelectBooleanRadio getDetailRadio25() {
    return detailRadio25;
  }

  public RichPanelGroupLayout getSerialNumberPanel() {
    return serialNumberPanel;
  }

  public void setLocationTable(RichTable locationTable) {
    this.locationTable = locationTable;
  }

  public RichTable getLocationTable() {
    return locationTable;
  }

  public void setSerialOrLotNum(RichInputText serialOrLotNum) {
    this.serialOrLotNum = serialOrLotNum;
  }

  public RichInputText getSerialOrLotNum() {
    return serialOrLotNum;
  }

  public void setStartSerial(RichInputText startSerial) {
    this.startSerial = startSerial;
  }

  public RichInputText getStartSerial() {
    return startSerial;
  }

  public void setEndSerial(RichInputText endSerial) {
    this.endSerial = endSerial;
  }

  public RichInputText getEndSerial() {
    return endSerial;
  }

  public void setDetailStowLossGcss(RichSelectBooleanRadio detailStowLossGcss) {
    this.detailStowLossGcss = detailStowLossGcss;
  }

  public RichSelectBooleanRadio getDetailStowLossGcss() {
    return detailStowLossGcss;
  }

  public static void setDetailAideMaxValue(int detailAideMaxValue) {
    ExceptionBacking.detailAideMaxValue = detailAideMaxValue;
  }

  public static int getDetailAideMaxValue() {
    return detailAideMaxValue;
  }

  public UserInfo getUserInfo() {
    return userInfo;
  }
}


