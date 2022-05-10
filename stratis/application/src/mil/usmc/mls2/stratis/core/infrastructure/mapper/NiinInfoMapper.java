package mil.usmc.mls2.stratis.core.infrastructure.mapper;

import mil.usmc.mls2.stratis.core.domain.model.NiinInfo;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.NiinInfoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.Set;

@Mapper(uses = RefSlcMapper.class)
public interface NiinInfoMapper {

  NiinInfoMapper INSTANCE = Mappers.getMapper(NiinInfoMapper.class);
  
  NiinInfo entityToModel(NiinInfoEntity niinInfoEntity);

  NiinInfo entityToModel(NiinInfoEntity niinInfoEntity, @MappingTarget NiinInfo niinInfo);

  Set<NiinInfo> entitySetToModelSet(Set<NiinInfoEntity> entitySet);
}
