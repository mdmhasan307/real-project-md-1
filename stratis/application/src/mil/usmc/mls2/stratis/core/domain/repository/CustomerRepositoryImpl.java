package mil.usmc.mls2.stratis.core.domain.repository;

import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.core.domain.model.Customer;
import mil.usmc.mls2.stratis.core.infrastructure.mapper.CustomerMapper;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.CustomerEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.RouteEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.ShippingRouteEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.CustomerEntityRepository;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.RouteEntityRepository;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.ShippingRouteEntityRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
class CustomerRepositoryImpl implements CustomerRepository {

  private final CustomerEntityRepository entityRepository;
  private final CustomerMapper mapper;
  private final ShippingRouteEntityRepository shippingRouteEntityRepository;
  private final RouteEntityRepository routeEntityRepository;

  @Override
  public Optional<Customer> getByAac(String aac) {
    val entity = entityRepository.getByAac(aac);
    return entity.map(mapper::map);
  }

  @Override
  public Optional<Customer> findById(Integer customerId) {
    val entity = entityRepository.findById(customerId);
    return entity.map(mapper::map);
  }

  @Override
  @Transactional
  public void save(Customer customer) {
    CustomerEntity entity = new CustomerEntity();
    if (customer.getCustomerId() != null) entity = entityRepository.findById(customer.getCustomerId()).orElseGet(CustomerEntity::new);

    apply(entity, customer);
    val result = entityRepository.save(entity);
    //FUTURE INNOV Backlog the only field we need from the customer from the DB is the ID.  But this should be updated to follow the new mapper model
    customer.setCustomerId(result.getCustomerId());
  }

  private void apply(CustomerEntity entity, Customer input) {
    entity.setAac(input.getAac());
    entity.setAddress1(input.getAddress1());
    entity.setAddress2(input.getAddress2());
    entity.setCity(input.getCity());
    entity.setFloorLocationId(input.getFloorLocationId());
    entity.setName(input.getName());
    entity.setRestrictShip(input.getRestrictShip());

    if (input.getShippingRoute() != null) {
      val shippingRoute = shippingRouteEntityRepository.findById(input.getShippingRoute().getRouteId()).orElseGet(ShippingRouteEntity::new);
      entity.setShippingRoute(shippingRoute);
    }
    entity.setSecurityMarkClass(input.getSecurityMarkClass());

    if (input.getRoute() != null) {
      val route = routeEntityRepository.findById(input.getRoute().getRouteId()).orElseGet(RouteEntity::new);
      entity.setRoute(route);
    }
    entity.setState(input.getState());
    entity.setSupported(input.getSupported());
    entity.setZipCode(input.getZipCode());
    entity.setCustomerId(input.getCustomerId());
  }
}