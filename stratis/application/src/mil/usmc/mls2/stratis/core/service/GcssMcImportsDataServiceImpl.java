package mil.usmc.mls2.stratis.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mil.usmc.mls2.stratis.core.domain.model.GcssMcImportsData;
import mil.usmc.mls2.stratis.core.domain.model.GcssMcImportsDataSearchCriteria;
import mil.usmc.mls2.stratis.core.domain.repository.GcssMcImportsDataRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class GcssMcImportsDataServiceImpl implements GcssMcImportsDataService {

  private final GcssMcImportsDataRepository gcssMcImportsDataRepository;

  @Override
  @Transactional
  public void save(GcssMcImportsData gcssMcImportsData) {
    gcssMcImportsDataRepository.save(gcssMcImportsData);
  }

  @Override
  @Transactional(readOnly = true)
  public Set<GcssMcImportsData> search(GcssMcImportsDataSearchCriteria gcssMcImportsDataSearchCriteria) {
    return gcssMcImportsDataRepository.search(gcssMcImportsDataSearchCriteria);
  }

  @Override
  @Transactional
  public void delete(GcssMcImportsData data) {
    gcssMcImportsDataRepository.delete(data);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<GcssMcImportsData> getMostRecentRecordForProcessing(String interfaceName) {
    return gcssMcImportsDataRepository.getMostRecentRecordForProcessing(interfaceName);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<GcssMcImportsData> getOldestRecordForProcessing(String interfaceName) {
    return gcssMcImportsDataRepository.getOldestRecordForProcessing(interfaceName);
  }

  @Override
  @Transactional
  public void updateIgnoreAllPreviousDataByInterface(Integer gcssmcImportId, String interfaceName) {
    gcssMcImportsDataRepository.updateIgnoreAllPreviousDataByInterface(gcssmcImportId, interfaceName);
  }
}