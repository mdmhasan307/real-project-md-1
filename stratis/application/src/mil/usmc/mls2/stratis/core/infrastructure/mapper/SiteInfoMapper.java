package mil.usmc.mls2.stratis.core.infrastructure.mapper;

import mil.usmc.mls2.stratis.core.domain.model.SiteInfo;
import mil.usmc.mls2.stratis.core.domain.model.StaticSiteInfo;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.SiteInfoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SiteInfoMapper {

  SiteInfoMapper INSTANCE = Mappers.getMapper(SiteInfoMapper.class);

  SiteInfo entityToModel(SiteInfoEntity siteInfoEntity);

  StaticSiteInfo entityToStaticModel(SiteInfoEntity siteInfoEntity);

  SiteInfoEntity modelToEntity(SiteInfo siteInfo, @MappingTarget SiteInfoEntity siteInfoEntity);
}
