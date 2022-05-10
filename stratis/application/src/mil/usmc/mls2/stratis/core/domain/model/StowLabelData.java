package mil.usmc.mls2.stratis.core.domain.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StowLabelData {
  private String mechStation;
  private String city;
  private String location;
  private String fscniin;
  private String ui;
  private String cc;
  private String docno;
  private String rcn;
  private String sid;
  private String sidQty;
  private String exp;
  private String lot;
  private String caseQty;
  private String serialNum;
  private String username;
  private String datetime;
  private String nomenclature;
  private String wacNumber;
}
