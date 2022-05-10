package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class SiteInterfaceEntityRepositoryCustomImpl implements SiteInterfaceEntityRepositoryCustom {

  @PersistenceContext
  private EntityManager entityManager;
}