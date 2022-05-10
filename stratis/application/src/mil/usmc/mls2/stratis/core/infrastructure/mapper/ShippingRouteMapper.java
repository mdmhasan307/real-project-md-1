package mil.usmc.mls2.stratis.core.infrastructure.mapper;

import mil.usmc.mls2.stratis.core.domain.model.ShippingRoute;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.ShippingRouteEntity;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("Duplicates")
public class ShippingRouteMapper {

  private final ShippingRouteMapper shippingRouteMapper;

  public ShippingRouteMapper(@Lazy ShippingRouteMapper shippingRouteMapper) {
    this.shippingRouteMapper = shippingRouteMapper;
  }

  public ShippingRoute map(ShippingRouteEntity input) {
    if (input == null) return null;

    return ShippingRoute.builder()
        .description(input.getDescription())
        .routeId(input.getRouteId())
        .routeName(input.getRouteName())
        .build();
  }
}