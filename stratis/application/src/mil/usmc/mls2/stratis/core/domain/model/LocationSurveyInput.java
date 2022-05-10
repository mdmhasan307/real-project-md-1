package mil.usmc.mls2.stratis.core.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationSurveyInput implements Serializable {

  private Integer inventoryItemId;
  private String location;
  private String niin;

  @Builder.Default
  private List<String> scannedNiins = new ArrayList<>();
}