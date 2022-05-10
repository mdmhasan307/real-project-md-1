package mil.usmc.mls2.stratis.core.domain.repository;

import lombok.RequiredArgsConstructor;
import mil.usmc.mls2.stratis.core.domain.model.RefSlc;
import mil.usmc.mls2.stratis.core.infrastructure.mapper.RefSlcMapper;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.RefSlcEntityRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RefSlcRepositoryImpl implements RefSlcRepository {

  private static final RefSlcMapper REF_SLC_MAPPER = RefSlcMapper.INSTANCE;

  private final RefSlcEntityRepository entityRepository;

  public Optional<RefSlc> findByShelfLifeCode(String shelfLifeCode) {
    return entityRepository.findByCode(shelfLifeCode).map(REF_SLC_MAPPER::entityToModel);
  }
}
