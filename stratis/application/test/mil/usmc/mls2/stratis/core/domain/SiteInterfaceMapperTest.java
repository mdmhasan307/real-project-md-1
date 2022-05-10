package mil.usmc.mls2.stratis.core.domain;

import mil.stratis.common.util.crypto.CustomEncryptor;
import mil.usmc.mls2.stratis.core.domain.model.SiteInfo;
import mil.usmc.mls2.stratis.core.infrastructure.mapper.SiteInfoMapper;
import mil.usmc.mls2.stratis.core.infrastructure.mapper.SiteInterfaceMapper;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.SiteInfoEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.SiteInterfaceEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class SiteInterfaceMapperTest {
    private static final SiteInterfaceMapper SITE_INTERFACE_MAPPER = SiteInterfaceMapper.INSTANCE;
    private static final SiteInfoMapper SITE_INFO_MAPPER = SiteInfoMapper.INSTANCE;

    @Test
    @DisplayName("Test to ensure Optional mapping works as expected")
    void mappingOptionalTest() {

        Optional<SiteInterfaceEntity> optExists = Optional.of(new SiteInterfaceEntity());
        Optional<SiteInterfaceEntity> optDoesntExist = Optional.empty();

        Optional oHere = optExists.map(SITE_INTERFACE_MAPPER::entityToModel);
        Optional oNotHere = optDoesntExist.map(SITE_INTERFACE_MAPPER::entityToModel);
        assertThat(oHere.isPresent()).as("Check present option was mapped").isTrue();
        assertThat(oNotHere.isPresent()).as("Check absent option was not mapped").isFalse();
        SiteInterfaceEntity sie = optExists.get();
        assertThat(sie).isNotNull();
    }

    @Test
    @DisplayName("See if we can call Optional.map with ")
    void mapModelToEntity() {

        SiteInfo siteInfo = new SiteInfo();
        siteInfo.setAac("ANAAC");
        siteInfo.setCity("Washington");
        siteInfo.setState("DC");

        SiteInfoEntity siteInfoEntity = new SiteInfoEntity();

        Optional<SiteInfoEntity> oHere = Optional.of(siteInfoEntity);
        Optional<SiteInfoEntity> nHere = oHere.map(e -> SITE_INFO_MAPPER.modelToEntity(siteInfo, e));

        nHere.ifPresent(this::saveAndFlush);


    }

    private void saveAndFlush(SiteInfoEntity siteInfoEntity) {
        Object o = siteInfoEntity;
    }

}
