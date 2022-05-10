package mil.usmc.mls2.stratis.core.infrastructure.mapper;

import mil.usmc.mls2.stratis.core.domain.model.Customer;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.CustomerEntity;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("Duplicates")
public class CustomerMapper {

  private final CustomerMapper customerMapper;
  private final ShippingRouteMapper shippingRouteMapper;
  private final RouteMapper routeMapper;

  public CustomerMapper(@Lazy CustomerMapper customerMapper, @Lazy ShippingRouteMapper shippingRouteMapper, @Lazy RouteMapper routeMapper) {
    this.customerMapper = customerMapper;
    this.shippingRouteMapper = shippingRouteMapper;
    this.routeMapper = routeMapper;
  }

  public Customer map(CustomerEntity input) {
    if (input == null) return null;

    return Customer.builder()
        .aac(input.getAac())
        .address1(input.getAddress1())
        .address2(input.getAddress2())
        .city(input.getCity())
        .floorLocationId(input.getFloorLocationId())
        .name(input.getName())
        .restrictShip(input.getRestrictShip())
        .shippingRoute(shippingRouteMapper.map(input.getShippingRoute()))
        .securityMarkClass(input.getSecurityMarkClass())
        .route(routeMapper.map(input.getRoute()))
        .state(input.getState())
        .supported(input.getSupported())
        .zipCode(input.getZipCode())
        .customerId(input.getCustomerId())
        .build();
  }
}