package mil.usmc.mls2.stratis.core.infrastructure.mapper;

import mil.usmc.mls2.stratis.core.domain.model.Route;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.RouteEntity;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("Duplicates")
public class RouteMapper {

  private final RouteMapper routeMapper;

  public RouteMapper(@Lazy RouteMapper routeMapper) {
    this.routeMapper = routeMapper;
  }

  public Route map(RouteEntity input) {
    if (input == null) return null;

    return Route.builder()
        .description(input.getDescription())
        .routeId(input.getRouteId())
        .routeName(input.getRouteName())
        .build();
  }
}