package mil.usmc.mls2.stratis.core.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

@Slf4j
@Data
@AllArgsConstructor
@ToString
public class ReviewLocationInput implements Serializable {
  private String location;
  private String barcode;
  private String overrideCommand;
  private String aac;
  private String scn;
  private String shippingManifestId;
  private String shippingId;
  private String floorLocation;
  private Integer floorLocationId;

}
