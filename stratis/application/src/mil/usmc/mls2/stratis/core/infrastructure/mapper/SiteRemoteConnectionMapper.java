package mil.usmc.mls2.stratis.core.infrastructure.mapper;

import mil.usmc.mls2.stratis.core.domain.model.SiteRemoteConnection;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.SiteRemoteConnectionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.Set;

@Mapper
public interface SiteRemoteConnectionMapper {

  SiteRemoteConnectionMapper INSTANCE = Mappers.getMapper(SiteRemoteConnectionMapper.class);

  SiteRemoteConnection entityToModel(SiteRemoteConnectionEntity siteRemoteConnectionEntity);

  Set<SiteRemoteConnection> entitySetToModelSet(Set<SiteRemoteConnectionEntity> siteRemoteConnectionEntitySet);
}
