package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import lombok.RequiredArgsConstructor;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.AuditLogEntity;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@RequiredArgsConstructor
@SuppressWarnings("unused, Duplicates")
public class AuditLogEntityRepositoryCustomImpl implements AuditLogEntityRepositoryCustom {

  @PersistenceContext
  private EntityManager entityManager;

  public void persist(AuditLogEntity audit) {
    entityManager.persist(audit);
  }
}
