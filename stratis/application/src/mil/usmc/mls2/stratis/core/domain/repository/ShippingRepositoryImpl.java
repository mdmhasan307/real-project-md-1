package mil.usmc.mls2.stratis.core.domain.repository;

import lombok.RequiredArgsConstructor;
import mil.usmc.mls2.stratis.core.domain.model.Shipping;
import mil.usmc.mls2.stratis.core.domain.model.ShippingSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.mapper.ShippingMapper;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.ShippingEntityRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
public class ShippingRepositoryImpl implements ShippingRepository {

  private final ShippingEntityRepository entityRepository;
  private final ShippingMapper mapper;

  @Override
  public List<Shipping> search(ShippingSearchCriteria criteria) {
    return entityRepository.search(criteria).stream().map(mapper::map).collect(Collectors.toList());
  }

  @Override
  public Long count(ShippingSearchCriteria criteria) {
    return entityRepository.count(criteria);
  }
}
