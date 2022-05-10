package mil.usmc.mls2.stratis.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mil.usmc.mls2.stratis.core.domain.model.AuditLog;
import mil.usmc.mls2.stratis.core.domain.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings({"Duplicates", "squid:S1192"})
class AuditLogServiceImpl implements AuditLogService {

  private final AuditLogRepository auditLogRepository;

  @Override
  @Transactional(readOnly = true)
  public Optional<AuditLog> findById(Integer id) {
    return auditLogRepository.findById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public List<AuditLog> getAll() {
    return auditLogRepository.getAll();
  }
}
