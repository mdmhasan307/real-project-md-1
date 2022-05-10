package mil.usmc.mls2.stratis.core.domain.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BarcodeLabelData {
  private String title;
  private String barcode;
}
