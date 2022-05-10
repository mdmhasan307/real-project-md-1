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
public class Customer {

  private Integer customerId;
  private String address1;
  private String city;
  private String state;
  private String zipCode;
  private String aac;
  private String address2;
  private Route route;
  private String restrictShip;
  private String name;
  private Integer floorLocationId;
  private String supported;
  private ShippingRoute shippingRoute;
  private String securityMarkClass;
}
