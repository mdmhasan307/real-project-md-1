package mil.usmc.mls2.stratis.core.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefGabfSerial {

  private Integer refGabfSerialId;
  private Integer refGabfId;
  private String serialNumber;
  private String lotConNum;
  private String cc;
  private Long quantity;
}