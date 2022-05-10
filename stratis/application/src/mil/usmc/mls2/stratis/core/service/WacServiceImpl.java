package mil.usmc.mls2.stratis.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mil.usmc.mls2.stratis.core.domain.model.Wac;
import mil.usmc.mls2.stratis.core.domain.repository.WacRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings({"Duplicates", "squid:S1192"})
class WacServiceImpl implements WacService {

  private final WacRepository wacRepository;

  @Override
  @Transactional(readOnly = true)
  public Optional<Wac> findById(Integer id) {
    return wacRepository.findById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Wac> getAll() {
    return wacRepository.getAll();
  }
}
