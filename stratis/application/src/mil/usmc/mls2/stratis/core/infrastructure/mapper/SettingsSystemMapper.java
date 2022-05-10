package mil.usmc.mls2.stratis.core.infrastructure.mapper;

import mil.usmc.mls2.stratis.core.domain.model.SettingsSystem;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.common.model.SettingsSystemEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface SettingsSystemMapper {

  SettingsSystemMapper INSTANCE = Mappers.getMapper(SettingsSystemMapper.class);

  SettingsSystem entityToModel(SettingsSystemEntity settingsSystemEntity);

  List<SettingsSystem> entityListToModelList(List<SettingsSystemEntity> settingsSystemEntity);

  SettingsSystemEntity modelToEntity(SettingsSystem settingsSystem);

//  List<SettingsSystemEntity> modelListToEntityList(List<SettingsSystem> settingsSystem);
}