package mil.usmc.mls2.stratis.core.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@AllArgsConstructor
public class StowDetailsInput {

  private int stowId;
  private String location;
  private String lastNiin;
  private String stowQty;
  private String stowLoss;
  private String stowRelocate;
}
