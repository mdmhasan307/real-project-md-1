package mil.usmc.mls2.stratis.core.domain.repository;

import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.core.domain.model.SiteInterface;
import mil.usmc.mls2.stratis.core.infrastructure.mapper.SiteInterfaceMapper;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.SiteInterfaceEntityRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
public class SiteInterfaceRepositoryImpl implements SiteInterfaceRepository {

  private static final SiteInterfaceMapper SITE_INTERFACE_MAPPER = SiteInterfaceMapper.INSTANCE;

  private final SiteInterfaceEntityRepository siteInterfaceEntityRepository;

  @Override
  public Optional<SiteInterface> getByInterfaceName(String interfaceName) {
    return siteInterfaceEntityRepository.getByInterfaceName(interfaceName)
        .map(SITE_INTERFACE_MAPPER::entityToModel);
  }

  public void save(SiteInterface siteInterface) {
    val entity = SITE_INTERFACE_MAPPER.modelToEntity(siteInterface);
    siteInterfaceEntityRepository.saveAndFlush(entity);
  }
}
