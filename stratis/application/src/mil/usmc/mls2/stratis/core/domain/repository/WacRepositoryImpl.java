package mil.usmc.mls2.stratis.core.domain.repository;

import lombok.RequiredArgsConstructor;
import mil.usmc.mls2.stratis.core.domain.model.Wac;
import mil.usmc.mls2.stratis.core.infrastructure.mapper.WacMapper;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.WacEntityRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
class WacRepositoryImpl implements WacRepository {

  private final WacEntityRepository entityRepository;
  private final WacMapper mapper;

  @Override
  public Optional<Wac> findById(Integer id) {

    return entityRepository.findById(id).map(mapper::map);
  }


  @Override
  public List<Wac> getAll() {

    return entityRepository.findAll(Sort.by(Sort.Direction.ASC, "wacNumber"))
        .stream()
        .map(mapper::map)
        .collect(Collectors.toList());
  }
}