package mil.usmc.mls2.stratis.core.service;

import mil.usmc.mls2.stratis.core.domain.model.AuditLog;

import java.util.List;
import java.util.Optional;

public interface AuditLogService {

  Optional<AuditLog> findById(Integer id);

  List<AuditLog> getAll();
}
