package mil.usmc.mls2.stratis.core.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Wac {

  private int wacId;
  private Integer tasksPerTrip;
  private Integer sidsPerTrip;
  private Integer warehouseId;
  private String wacNumber;
  private String mechanizedFlag;
  private String secureFlag;
  private String bulkAreaNumber;
  private String carouselNumber;
  private Integer carouselController;
  private Integer carouselOffset;
  private String carouselModel;
  private Integer wacOrder;
  private String packArea;
}
