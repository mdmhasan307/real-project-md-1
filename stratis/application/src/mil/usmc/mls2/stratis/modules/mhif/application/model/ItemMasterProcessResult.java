package mil.usmc.mls2.stratis.modules.mhif.application.model;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;
import mil.usmc.mls2.stratis.common.model.enumeration.I136ProcessingStatusEnum;

import java.util.List;

/**
 * NiinsProcessed: represents successfully processed niins.
 */
@Value
@Builder
@Accessors(fluent = true)
public class ItemMasterProcessResult {

  int totalNiins;
  int niinsProcessed;
  int niinsErrored;
  int niinsSkipped;

  List<String> messages;

  I136ProcessingStatusEnum status;

  public I136ProcessingStatusEnum status() {
    return status != null ? status : calcStatus();
  }

  public static ItemMasterProcessResult ofStatusWithTotal(I136ProcessingStatusEnum stat, int totalNiins) {
    return ItemMasterProcessResult.builder()
        .status(stat)
        .totalNiins(totalNiins)
        .build();
  }

  private I136ProcessingStatusEnum calcStatus() {
    return (totalNiins == niinsProcessed) ? I136ProcessingStatusEnum.SUCCESS : I136ProcessingStatusEnum.SUCCESS_PARTIAL;
  }
}
