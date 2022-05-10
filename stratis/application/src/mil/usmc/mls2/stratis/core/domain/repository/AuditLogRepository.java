package mil.usmc.mls2.stratis.core.domain.repository;

import mil.usmc.mls2.stratis.core.domain.model.AuditLog;

import java.util.List;
import java.util.Optional;

public interface AuditLogRepository {

  void persist(AuditLog audit);

  Optional<AuditLog> findById(Integer id);

  List<AuditLog> getAll();
}
