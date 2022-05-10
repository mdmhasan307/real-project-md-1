package mil.usmc.mls2.stratis.core.infrastructure.mapper;

import mil.usmc.mls2.stratis.core.domain.model.RefMhif;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.RefMhifEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.Set;

@Mapper
public interface RefMhifMapper {

  RefMhifMapper INSTANCE = Mappers.getMapper(RefMhifMapper.class);

  RefMhif entityToModel(RefMhifEntity refMhifEntity);

  RefMhif entityToModel(RefMhifEntity refMhifEntity, @MappingTarget RefMhif refMhif);

  Set<RefMhif> entitySetToModelSet(Set<RefMhifEntity> refMhifEntitySet);

  RefMhifEntity modelToEntity(RefMhif refMhif);

  Set<RefMhifEntity> modelSetToEntitySet(Set<RefMhif> refMhifSet);
}
