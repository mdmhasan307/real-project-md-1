package mil.usmc.mls2.stratis.core.domain.repository;

import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.core.domain.model.AuditLog;
import mil.usmc.mls2.stratis.core.infrastructure.mapper.AuditLogMapper;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.AuditLogEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.AuditLogEntityRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
class AuditLogRepositoryImpl implements AuditLogRepository {

  private final AuditLogEntityRepository entityRepository;
  private final AuditLogMapper mapper;

  @Override
  public void persist(AuditLog audit) {
    val entity = audit.getAuditLogId() != null ? entityRepository.findById(audit.getAuditLogId()).orElseGet(AuditLogEntity::new) : new AuditLogEntity();
    apply(entity, audit);
    entityRepository.persist(entity);
    audit.setAuditLogId(entity.getAuditLogId());
  }

  @Override
  public Optional<AuditLog> findById(Integer id) {
    return entityRepository.findById(id).map(mapper::map);
  }

  @Override
  public List<AuditLog> getAll() {
    return entityRepository.findAll(Sort.by(Sort.Direction.ASC, "name")).stream().map(mapper::map).collect(Collectors.toList());
  }

  private void apply(AuditLogEntity entity, AuditLog input) {
    entity.setAuditLogId(input.getAuditLogId());
    entity.setCategory(input.getCategory());
    entity.setDescription(input.getDescription());
    entity.setEquipmentNumber(input.getEquipmentNumber());
    entity.setEvent(input.getEvent());
    entity.setSource(input.getSource());
    entity.setTimestamp(input.getTimestamp());
    entity.setType(input.getType());
    entity.setUserId(input.getUserId());
  }
}