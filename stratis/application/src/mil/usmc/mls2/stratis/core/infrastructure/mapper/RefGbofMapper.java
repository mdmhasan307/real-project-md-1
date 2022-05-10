package mil.usmc.mls2.stratis.core.infrastructure.mapper;

import mil.usmc.mls2.stratis.core.domain.model.RefGbof;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.RefGbofEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.Set;

@Mapper
@SuppressWarnings("unused")
public interface RefGbofMapper {

  RefGbofMapper INSTANCE = Mappers.getMapper(RefGbofMapper.class);

  RefGbof entityToModel(RefGbofEntity refGbofEntity);

  RefGbof entityToModel(RefGbofEntity refGbofEntity, @MappingTarget RefGbof refGbof);

  Set<RefGbof> entitySetToModelSet(Set<RefGbofEntity> refGbofEntitySet);

  RefGbofEntity modelToEntity(RefGbof refGbof);

  Set<RefGbofEntity> modelSetToEntitySet(Set<RefGbof> refGbofSet);
}
