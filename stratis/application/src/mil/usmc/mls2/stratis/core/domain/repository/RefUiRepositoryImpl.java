package mil.usmc.mls2.stratis.core.domain.repository;

import lombok.RequiredArgsConstructor;
import mil.usmc.mls2.stratis.core.domain.model.RefUi;
import mil.usmc.mls2.stratis.core.domain.model.RefUiSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.mapper.RefUiMapper;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.RefUiEntityRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
class RefUiRepositoryImpl implements RefUiRepository {

  private final RefUiEntityRepository entityRepository;
  private final RefUiMapper mapper;

  @Override
  public Optional<RefUi> findById(Integer id) {
    return entityRepository.findById(id).map(mapper::map);
  }

  @Override
  public Long count(RefUiSearchCriteria criteria) {
    return entityRepository.count(criteria);
  }

  @Override
  public List<RefUi> search(RefUiSearchCriteria criteria) {
    return entityRepository.search(criteria).stream().map(mapper::map).collect(Collectors.toList());
  }
}