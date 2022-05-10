package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.AuditLogEntity;

public interface AuditLogEntityRepositoryCustom {

  void persist(AuditLogEntity audit);
}
