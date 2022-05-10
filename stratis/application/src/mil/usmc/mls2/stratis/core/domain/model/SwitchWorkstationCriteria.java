package mil.usmc.mls2.stratis.core.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

@Slf4j
@Data
@AllArgsConstructor
public class SwitchWorkstationCriteria implements Serializable {

  private int workstationId;
  private String workstationName;
  private int userTypeId;
  private String userTypeName;
}
