package mil.usmc.mls2.stratis.core.domain.repository;

import lombok.RequiredArgsConstructor;
import mil.usmc.mls2.stratis.core.domain.model.ShippingManifest;
import mil.usmc.mls2.stratis.core.domain.model.ShippingManifestSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.mapper.ShippingManifestMapper;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.ShippingManifestEntityRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
public class ShippingManifestRepositoryImpl implements ShippingManifestRepository {

  private final ShippingManifestEntityRepository entityRepository;
  private final ShippingManifestMapper mapper;

  @Override
  public List<ShippingManifest> search(ShippingManifestSearchCriteria criteria) {
    return entityRepository.search(criteria).stream().map(mapper::map).collect(Collectors.toList());
  }

  @Override
  public Long count(ShippingManifestSearchCriteria criteria) {
    return entityRepository.count(criteria);
  }
}
