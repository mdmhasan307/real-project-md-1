package mil.usmc.mls2.stratis.core.infrastructure.mapper;

import mil.usmc.mls2.stratis.core.domain.model.SiteInterface;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.SiteInterfaceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SiteInterfaceMapper {

  SiteInterfaceMapper INSTANCE = Mappers.getMapper(SiteInterfaceMapper.class);

  SiteInterface entityToModel(SiteInterfaceEntity siteInterfaceEntity);

  SiteInterfaceEntity modelToEntity(SiteInterface siteInterface);
}
