package mil.usmc.mls2.stratis.core.domain.repository;

import lombok.RequiredArgsConstructor;
import mil.usmc.mls2.stratis.core.domain.model.ShippingManifestHist;
import mil.usmc.mls2.stratis.core.domain.model.ShippingManifestHistSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.mapper.ShippingManifestHistMapper;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.ShippingManifestHistEntityRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
public class ShippingManifestHistRepositoryImpl implements ShippingManifestHistRepository {

  private final ShippingManifestHistEntityRepository entityRepository;
  private final ShippingManifestHistMapper mapper;

  @Override
  public List<ShippingManifestHist> search(ShippingManifestHistSearchCriteria criteria) {
    return entityRepository.search(criteria).stream().map(mapper::map).collect(Collectors.toList());
  }

  @Override
  public Long count(ShippingManifestHistSearchCriteria criteria) {
    return entityRepository.count(criteria);
  }
}
