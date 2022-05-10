package mil.usmc.mls2.stratis.core.utility;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Component
@Getter
public class GlobalConstants {

  public static final String LOCALHOST = "127.0.0.1";

  public static final String TASK_EXECUTOR = "threadPoolTaskExecutor";
  public static final Integer SYSTEM_USER_ID = 1;

  public static final String ROOT_COMMON_DB_PACKAGE = "mil.usmc.mls2.stratis.core.infrastructure.persistence.common.model";
  public static final String ROOT_STRATIS_SYSTEM_DB_PACKAGE = "mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model";

  public static final String PERSISTENCE_UNIT = "stratisPersistence";
  public static final String COMMON_PERSISTENCE_UNIT = "stratisCommonPersistence";
  public static final String COMMON_TRANSACTION_MANAGER = "commonTransactionManager";
  public static final String LEGACY_DATABASE_CONNECTION_NAME = "STRATDBSVRDS";

  private final String inventoryItemSessionAttrib = "inventoryItemInput";
  private final String locationSurveySessionAttrib = "locationSurveyInput";
  private final String pickingDetailSessionAttrib = "pickingDetailInput";
  private final String receivingWorkflowContainerSessionAttrib = "receivingWorkflowContainer";
  private final String userbeanSession = "userbean";

  private final String errorTag = "ERROR";
  private final String missingFieldTag = "MISSING REQUIRED FIELD";
  private final String invalidFieldTag = "INVALID FIELD";
  private final String invalidInputTag = "INVALID INPUT";
  private final String invalidNumberTag = "INVALID NUMBER";
  private final String invalidLengthTag = "INVALID LENGTH";
  private final String invalidEntryTag = "INVALID ENTRY";
  private final String invalidSelectionTag = "INVALID SELECTION";
  private final String walkthruTag = "WALK THRU";

  //How many records for Picking,Stowing,Inventory to assign to a user at a time, if the Wac does not have a TasksPerTrip assigned to it.
  private final Integer tasksPerTrip = 12;

  @Value("${stratis.barcodeImagePath.legacy}")
  private String legacyBarcodeImagePath;

  @Value("${stratis.barcodeImagePath.innovation}")
  private String innovationBarcodeImagePath;

  @Value("${stratis.amsCmosPath.legacy}")
  private String legacyAmsCmosPath;

  @Value("${stratis.amsCmosPath.innovation}")
  private String innovationAmsCmosPath;

  @Value("${stratis.session.timeout.admin}")
  private int adminSessionTimeout;

  @Value("${stratis.session.timeout.user}")
  private int userSessionTimeout;

  @Value("${stratis.session.timeout.mats}")
  private int matsSessionTimeout;

  @Value("${stratis.print.applet}")
  private boolean appletPrint;

  @Value("${stratis.smv.enabled}")
  private boolean smvEnabled;

  @Value("${stratis.smv.receiving.enabled}")
  private boolean receivingEnabledBySystem;

  @Value("${stratis.smv.shipping.enabled}")
  private boolean shippingEnabledBySystem;

  @Value("${stratis.smv.stowing.enabled}")
  private boolean stowingEnabledBySystem;

  @Value("${stratis.smv.picking.enabled}")
  private boolean pickingEnabledBySystem;

  @Value("${stratis.smv.inventory.enabled}")
  private boolean inventoryEnabledBySystem;

  @Value("${stratis.smv.locationSurvey.enabled}")
  private boolean locationSurveyEnabledBySystem;

  @Value("${stratis.smv.shelfLife.enabled}")
  private boolean shelfLifeEnabledBySystem;

  @Value("${stratis.smv.printTest.enabled}")
  private boolean printTestEnabledBySystem;

  @Value("${stratis.smv.javascriptPrinting.enabled}")
  private boolean javascriptPrintingEnabledBySystem;

  @Value("${stratis.smv.switchWorkstation.userRole.enabled}")
  private boolean switchUserRoleEnabledBySystem;

  @Value("${stratis.inventory.runInForeground}")
  private boolean runInForeground;

  @Value("${stratis.inventory.useObjectConversionsInThreadedMode}")
  private boolean useObjectConversionsInThreadedMode;
}
