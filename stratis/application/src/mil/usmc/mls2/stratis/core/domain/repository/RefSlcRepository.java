package mil.usmc.mls2.stratis.core.domain.repository;

import mil.usmc.mls2.stratis.core.domain.model.RefSlc;

import java.util.Optional;

public interface RefSlcRepository {

  Optional<RefSlc> findByShelfLifeCode(String shelfLifeCode);
}
