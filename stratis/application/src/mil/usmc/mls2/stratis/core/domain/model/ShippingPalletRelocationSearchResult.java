package mil.usmc.mls2.stratis.core.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

@Slf4j
@Data
@AllArgsConstructor
@Builder
@ToString
public class ShippingPalletRelocationSearchResult implements Serializable {
    String aac;
    String floorLocation;
    String leadTcn;
    boolean foundLocation;
}
