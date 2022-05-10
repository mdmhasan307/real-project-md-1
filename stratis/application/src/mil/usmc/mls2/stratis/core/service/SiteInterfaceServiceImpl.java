package mil.usmc.mls2.stratis.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mil.usmc.mls2.stratis.core.domain.model.SiteInterface;
import mil.usmc.mls2.stratis.core.domain.repository.SiteInterfaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SiteInterfaceServiceImpl implements SiteInterfaceService {

  private final SiteInterfaceRepository siteInterfaceRepository;

  @Override
  @Transactional(readOnly = true)
  public Optional<SiteInterface> getByInterfaceName(String interfaceName) {
    return siteInterfaceRepository.getByInterfaceName(interfaceName);
  }

  @Override
  @Transactional
  public void save(SiteInterface siteInterface) {
    siteInterfaceRepository.save(siteInterface);
  }
}