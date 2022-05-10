package mil.usmc.mls2.stratis.core.infrastructure.mapper;

import mil.usmc.mls2.stratis.core.domain.model.Spool;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.SpoolEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.Set;

@Mapper
public interface SpoolMapper {

  SpoolMapper INSTANCE = Mappers.getMapper(SpoolMapper.class);

  @SuppressWarnings("unused")
  SpoolEntity modelToEntity(Spool spool);

  SpoolEntity modelToEntity(Spool spool, @MappingTarget SpoolEntity spoolEntity);

  @SuppressWarnings("unused")
  Spool entityToModel(SpoolEntity spoolEntity);

  Spool entityToModel(SpoolEntity spoolEntity, @MappingTarget Spool spool);

  Set<Spool> entitySetToModelSet(Set<SpoolEntity> spoolEntitySet);
}