package mil.usmc.mls2.stratis.core.infrastructure.mapper;

import mil.usmc.mls2.stratis.core.domain.model.GcssMcImportsData;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.GcssMcImportsDataEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.Set;

@Mapper
public interface GcssMcImportsDataMapper {

  GcssMcImportsDataMapper INSTANCE = Mappers.getMapper(GcssMcImportsDataMapper.class);

  @SuppressWarnings("unused")
  GcssMcImportsDataEntity modelToEntity(GcssMcImportsData gcssMcImportsData);

  GcssMcImportsDataEntity modelToEntity(GcssMcImportsData gcssMcImportsData, @MappingTarget GcssMcImportsDataEntity gcssMcImportsDataEntity);

  @SuppressWarnings("unused")
  GcssMcImportsData entityToModel(GcssMcImportsDataEntity gcssMcImportsDataEntity);

  GcssMcImportsData entityToModel(GcssMcImportsDataEntity gcssMcImportsDataEntity, @MappingTarget GcssMcImportsData gcssMcImportsData);

  Set<GcssMcImportsData> entitySetToModelSet(Set<GcssMcImportsDataEntity> entitySet);
}
