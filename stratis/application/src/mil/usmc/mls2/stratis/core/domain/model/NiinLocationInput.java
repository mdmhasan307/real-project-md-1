package mil.usmc.mls2.stratis.core.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDate;

@Slf4j
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NiinLocationInput implements Serializable {

  private Integer inventoryItemId;
  private Integer niinLocationId;
  private Integer niinId;
  private String cc;

  @DateTimeFormat(pattern = "yyyy-MM-dd")
  private LocalDate expirationDate;
}


