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
public class ShippingRemarkAacInput implements Serializable {
  private String leadTcn;
  private String remarkAac;
}
