package mil.usmc.mls2.stratis.core.domain.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PickLabelData {
  private String mechStation;
  private String city;
  private String fscniin;
  private String cc;
  private String docno;
  private String packingStation;
  private String scn;
  private String pin;
  private String pinQty;
  private String username;
  private String datetime;
  private String nomenclature;
}
