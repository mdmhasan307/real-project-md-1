package mil.usmc.mls2.stratis.core.infrastructure.mapper;

import mil.usmc.mls2.stratis.core.domain.model.RefSlc;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.RefSlcEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper
public interface RefSlcMapper {

  RefSlcMapper INSTANCE = Mappers.getMapper(RefSlcMapper.class);

  RefSlcEntity modelToEntity(RefSlc refSlc);

  RefSlcEntity modelToEntity(RefSlc refSlc, @MappingTarget RefSlcEntity refSlcEntity);

  RefSlc entityToModel(RefSlcEntity refSlcEntity);

  RefSlc entityToModel(RefSlcEntity refSlcEntity, @MappingTarget RefSlc refSlc);
}
