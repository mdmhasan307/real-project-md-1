package mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository;

import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.RefSlcEntity;

import java.util.Optional;

public interface RefSlcEntityRepositoryCustom {
    Optional<RefSlcEntity> findByCode(String code);
}
