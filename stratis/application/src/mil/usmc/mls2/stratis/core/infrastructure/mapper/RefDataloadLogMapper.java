package mil.usmc.mls2.stratis.core.infrastructure.mapper;

import mil.usmc.mls2.stratis.core.domain.model.RefDataloadLog;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.RefDataloadLogEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.Set;

@Mapper
public interface RefDataloadLogMapper {

  RefDataloadLogMapper INSTANCE = Mappers.getMapper(RefDataloadLogMapper.class);

  @SuppressWarnings("unused")
  RefDataloadLogEntity modelToEntity(RefDataloadLog RefDataloadLog);

  RefDataloadLogEntity modelToEntity(RefDataloadLog RefDataloadLog, @MappingTarget RefDataloadLogEntity RefDataloadLogEntity);

  @SuppressWarnings("unused")
  RefDataloadLog entityToModel(RefDataloadLogEntity RefDataloadLogEntity);

  RefDataloadLog entityToModel(RefDataloadLogEntity RefDataloadLogEntity, @MappingTarget RefDataloadLog RefDataloadLog);

  Set<RefDataloadLog> entitySetToModelSet(Set<RefDataloadLogEntity> entitySet);
}