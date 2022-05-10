package mil.usmc.mls2.stratis.core.infrastructure.mapper;

import mil.usmc.mls2.stratis.core.domain.model.RefGabfSerial;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.RefGabfSerialEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.Set;

@Mapper
public interface RefGabfSerialMapper {

  RefGabfSerialMapper INSTANCE = Mappers.getMapper(RefGabfSerialMapper.class);

  RefGabfSerial entityToModel(RefGabfSerialEntity refGabfSerialEntity);

  RefGabfSerial entityToModel(RefGabfSerialEntity refGabfSerialEntity, @MappingTarget RefGabfSerial refGabfSerial);

  Set<RefGabfSerial> entitySetToModelSet(Set<RefGabfSerialEntity> refGabfSerialEntitySet);

  RefGabfSerialEntity modelToEntity(RefGabfSerial refGabfSerial);

  Set<RefGabfSerialEntity> modelSetToEntitySet(Set<RefGabfSerial> refGabfSerialSet);
}
