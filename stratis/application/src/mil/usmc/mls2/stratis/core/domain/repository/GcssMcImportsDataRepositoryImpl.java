package mil.usmc.mls2.stratis.core.domain.repository;

import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.core.domain.model.GcssMcImportsData;
import mil.usmc.mls2.stratis.core.domain.model.GcssMcImportsDataSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.mapper.GcssMcImportsDataMapper;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.GcssMcImportsDataEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.GcssMcImportsDataEntityRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@Repository
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
public class GcssMcImportsDataRepositoryImpl implements GcssMcImportsDataRepository {

  private static final GcssMcImportsDataMapper GCSSMC_IMPORTS_DATA_MAPPER = GcssMcImportsDataMapper.INSTANCE;

  private final GcssMcImportsDataEntityRepository gcssMcImportsDataEntityRepository;

  @Override
  public void save(GcssMcImportsData gcssMcImportsData) {
    GcssMcImportsDataEntity entity;
    if (gcssMcImportsData.getId() == null) {
      entity = new GcssMcImportsDataEntity();
    }
    else {
      entity = gcssMcImportsDataEntityRepository.findById(gcssMcImportsData.getId()).orElseGet(GcssMcImportsDataEntity::new);
    }
    GCSSMC_IMPORTS_DATA_MAPPER.modelToEntity(gcssMcImportsData, entity);
    gcssMcImportsDataEntityRepository.saveAndFlush(entity);
    // Note: the gcssMcImportsData object may not have the correct ID at this point, if an insert was performed.
  }

  @Override
  public Set<GcssMcImportsData> search(GcssMcImportsDataSearchCriteria gcssMcImportsDataSearchCriteria) {
    val results = gcssMcImportsDataEntityRepository.search(gcssMcImportsDataSearchCriteria);
    return GCSSMC_IMPORTS_DATA_MAPPER.entitySetToModelSet(results);
  }

  @Override
  public void delete(GcssMcImportsData data) {
    gcssMcImportsDataEntityRepository.delete(GCSSMC_IMPORTS_DATA_MAPPER.modelToEntity(data));
  }

  //FIXME optimize
  @Override
  public void delete(GcssMcImportsDataSearchCriteria gcssMcImportsDataSearchCriteria) {
    val results = search(gcssMcImportsDataSearchCriteria);
    results.forEach(result -> gcssMcImportsDataEntityRepository.delete(GCSSMC_IMPORTS_DATA_MAPPER.modelToEntity(result)));
  }

  @Override
  public Optional<GcssMcImportsData> getMostRecentRecordForProcessing(String interfaceName) {
    val result = gcssMcImportsDataEntityRepository.getMostRecentRecordForProcessing(interfaceName);
    return result.map(GCSSMC_IMPORTS_DATA_MAPPER::entityToModel);
  }

  @Override
  public Optional<GcssMcImportsData> getOldestRecordForProcessing(String interfaceName) {
    val result = gcssMcImportsDataEntityRepository.getOldestRecordForProcessing(interfaceName);
    return result.map(GCSSMC_IMPORTS_DATA_MAPPER::entityToModel);
  }

  @Override
  public void updateIgnoreAllPreviousDataByInterface(Integer gcssmcImportId, String interfaceName) {
    gcssMcImportsDataEntityRepository.updateIgnoreAllPreviousDataByInterface(gcssmcImportId, interfaceName);
  }
}