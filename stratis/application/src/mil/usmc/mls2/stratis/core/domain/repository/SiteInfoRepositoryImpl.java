package mil.usmc.mls2.stratis.core.domain.repository;

import lombok.RequiredArgsConstructor;
import mil.usmc.mls2.stratis.core.domain.model.SiteInfo;
import mil.usmc.mls2.stratis.core.domain.model.StaticSiteInfo;
import mil.usmc.mls2.stratis.core.infrastructure.mapper.SiteInfoMapper;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.SiteInfoEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.SiteInfoEntityRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
public class SiteInfoRepositoryImpl implements SiteInfoRepository {

  private static final SiteInfoMapper SITE_INFO_MAPPER = SiteInfoMapper.INSTANCE;

  private final SiteInfoEntityRepository siteInfoEntityRepository;

  @Override
  public Optional<SiteInfo> getRecord() {
    return getSiteInfoEntityRecord().map(SITE_INFO_MAPPER::entityToModel);
  }

  @Override
  public Optional<StaticSiteInfo> getStaticRecord() {
    return siteInfoEntityRepository.getRecord().map(SITE_INFO_MAPPER::entityToStaticModel);
  }

  private Optional<SiteInfoEntity> getSiteInfoEntityRecord() {
    return siteInfoEntityRepository.getRecord();
  }

  public void save(SiteInfo siteInfo) {
    getSiteInfoEntityRecord()
        .map(e -> SITE_INFO_MAPPER.modelToEntity(siteInfo, e))
        .ifPresent(siteInfoEntityRepository::saveAndFlush);
  }
}
