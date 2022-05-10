package mil.usmc.mls2.stratis.core.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

@Slf4j
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {

  //Incomplete, but unused at this time.
  private String userId;
  private String username;
  private String cacNumber;
}
