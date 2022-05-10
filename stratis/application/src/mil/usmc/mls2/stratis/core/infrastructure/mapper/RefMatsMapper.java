package mil.usmc.mls2.stratis.core.infrastructure.mapper;

import mil.usmc.mls2.stratis.core.domain.model.RefMats;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.RefMatsEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.Set;

// FUTURE INNOV MikeL/ChrisW consider standardization of Mapper objects - do or don't use MapStruct?  Refactor Mappers to live with entities?
@Mapper
@SuppressWarnings("unused")
public interface RefMatsMapper {

  RefMatsMapper INSTANCE = Mappers.getMapper(RefMatsMapper.class);

  RefMats entityToModel(RefMatsEntity refMatsEntity);

  RefMats entityToModel(RefMatsEntity refMatsEntity, @MappingTarget RefMats refMats);

  Set<RefMats> entitySetToModelSet(Set<RefMatsEntity> refMatsEntitySet);

  RefMatsEntity modelToEntity(RefMats refMats);

  Set<RefMatsEntity> modelSetToEntitySet(Set<RefMats> refMatsSet);
}
