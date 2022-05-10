package mil.usmc.mls2.stratis.core.infrastructure.mapper;

import mil.usmc.mls2.stratis.core.domain.model.RefGabf;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.RefGabfEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.Set;

@Mapper
public interface RefGabfMapper {

  RefGabfMapper INSTANCE = Mappers.getMapper(RefGabfMapper.class);

  RefGabf entityToModel(RefGabfEntity refGabfEntity);

  RefGabf entityToModel(RefGabfEntity refGabfEntity, @MappingTarget RefGabf refGabf);

  Set<RefGabf> entitySetToModelSet(Set<RefGabfEntity> refGabfEntitySet);

  RefGabfEntity modelToEntity(RefGabf refGabf);

  Set<RefGabfEntity> modelSetToEntitySet(Set<RefGabf> refGabfSet);
}
