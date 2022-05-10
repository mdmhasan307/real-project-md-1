package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import lombok.val;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.QSiteInfoEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.SiteInfoEntity;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Optional;

import static mil.usmc.mls2.stratis.core.infrastructure.util.SearchUtils.selectFrom;

public class SiteInfoEntityRepositoryCustomImpl implements SiteInfoEntityRepositoryCustom {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public Optional<SiteInfoEntity> getRecord() {
    val qSiteInfo = QSiteInfoEntity.siteInfoEntity;
    val query = selectFrom(qSiteInfo, entityManager);
    return Optional.ofNullable(query.fetchOne());
  }
}