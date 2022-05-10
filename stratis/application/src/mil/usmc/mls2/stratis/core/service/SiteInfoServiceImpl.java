package mil.usmc.mls2.stratis.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.usmc.mls2.stratis.common.domain.exception.StratisRuntimeException;
import mil.usmc.mls2.stratis.core.domain.model.SiteInfo;
import mil.usmc.mls2.stratis.core.domain.model.StaticSiteInfo;
import mil.usmc.mls2.stratis.core.domain.repository.SiteInfoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SiteInfoServiceImpl implements SiteInfoService {

  private final SiteInfoRepository siteInfoRepository;

  @Override
  @Transactional(readOnly = true)
  public SiteInfo getRecord() {

    val record = siteInfoRepository.getRecord();
    if (record.isPresent()) {
      return record.get();
    }
    throw new StratisRuntimeException("Site Information not found");
  }

  @Override
  @Transactional(readOnly = true)
  public StaticSiteInfo getStaticRecord() {
    val record = siteInfoRepository.getStaticRecord();
    if (record.isPresent()) {
      return record.get();
    }
    throw new StratisRuntimeException("Site Information not found");
  }

  @Override
  @Transactional
  public void save(SiteInfo siteInfo) {
    siteInfoRepository.save(siteInfo);
  }
}