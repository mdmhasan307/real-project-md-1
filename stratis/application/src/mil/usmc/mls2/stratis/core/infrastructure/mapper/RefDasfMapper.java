package mil.usmc.mls2.stratis.core.infrastructure.mapper;

import mil.usmc.mls2.stratis.core.domain.model.RefDasf;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.RefDasfEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.Set;

@Mapper
public interface RefDasfMapper {

  RefDasfMapper INSTANCE = Mappers.getMapper(RefDasfMapper.class);

  RefDasf entityToModel(RefDasfEntity refDasfEntity);

  Set<RefDasf> entitySetToModelSet(Set<RefDasfEntity> refDasfEntitySet);

  RefDasfEntity modelToEntity(RefDasf refDasf);

  Set<RefDasfEntity> modelSetToEntitySet(Set<RefDasf> refDasfSet);
}
