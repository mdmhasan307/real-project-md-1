package mil.usmc.mls2.stratis.modules.smv.utility;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MappingConstants {

  public static final String SMV_HOME = "/mobile/home";

  public static final String TEST_PRINT = "/mobile/testprint";
  public static final String DOWNLOAD_ZPL = "mobile/downloadzpl";
  public static final String PRINT = "/mobile/print";

  public static final String RECEIVING_HOME = "/mobile/receiving";
  public static final String RECEIVING_DETAIL = "mobile/receiving/detail";
  public static final String RECEIVING_CONTROLLED_NIIN = "/mobile/receiving/controlledNiinDetails";
  public static final String RECEIVING_LOCATION = "mobile/receiving/stowDetailLocation";

  public static final String PICKING_HOME = "/mobile/picking";
  public static final String PICKING_DETAIL = "/mobile/picking/detail";
  public static final String PICKING_CONTROLLED_NIIN_DETAIL = "/mobile/picking/controlledNiinDetail";
  public static final String PICKING_REFUSE = "/mobile/picking/refuse";
  public static final String PICKING_BYPASS = "/mobile/picking/bypass";

  public static final String STOWING_HOME = "/mobile/stowing";
  public static final String STOWING_DETAILS = "/mobile/stowing/detail";
  public static final String STOWING_BYPASS = "/mobile/stowing/bypass";

  public static final String INVENTORY_HOME = "/mobile/inventory";
  public static final String INVENTORY_DETAIL = "/mobile/inventory/inventory";
  public static final String SHELF_LIFE_DETAIL = "/mobile/inventory/shelfLife";
  public static final String LOCATION_SURVEY = "/mobile/inventory/locationSurvey";
  public static final String LOCATION_SURVEY_DETAIL = "/mobile/inventory/locationSurvey/detail";

  public static final String SHIPPING_HOME = "/mobile/shipping/";
  public static final String SHIPPING_ADD_CONTAINER = "/mobile/shipping/addcontainer";
  public static final String SHIPPING_MANIFEST = "/mobile/shipping/manifest";
  public static final String SHIPPING_REVIEW_LOCATION_HOME = "/mobile/shipping/reviewlocation";
  public static final String SHIPPING_REVIEW_LOCATION_BY_BARCODE = "/mobile/shipping/reviewlocation/barcode";
  public static final String SHIPPING_REVIEW_LOCATION_BY_CONTAINER = "/mobile/shipping/reviewlocation/container";
  public static final String SHIPPING_ACKNOWLEDGE_SHIPMENT = "/mobile/shipping/acknowledgeshipment";
  public static final String SHIPPING_PALLET_RELOCATION = "/mobile/shipping/palletrelocation";
  public static final String SHIPPING_TRANSSHIPMENT = "/mobile/shipping/transshipment";
  public static final String SHIPPING_REMARK_AAC = "/mobile/shipping/remarkaac";
}
