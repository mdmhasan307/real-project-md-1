package mil.usmc.mls2.stratis.common.model;

import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor(staticName = "of")
public class ApiErrorDetail {

  String key;
  String message;
}
