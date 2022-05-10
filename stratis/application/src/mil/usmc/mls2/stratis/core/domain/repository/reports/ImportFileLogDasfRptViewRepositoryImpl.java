package mil.usmc.mls2.stratis.core.domain.repository.reports;

import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.core.domain.model.reports.ImportFileLogDasfRptView;
import mil.usmc.mls2.stratis.core.domain.model.reports.ImportFileLogDasfRptViewSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.mapper.reports.ImportFileLogDasfRptViewMapper;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.reports.ImportFileLogDasfRptViewEntityRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
public class ImportFileLogDasfRptViewRepositoryImpl implements ImportFileLogDasfRptViewRepository {

  private static final ImportFileLogDasfRptViewMapper IMPORT_FILE_LOG_Dasf_RPT_VIEW_MAPPER = ImportFileLogDasfRptViewMapper.INSTANCE;

  private final ImportFileLogDasfRptViewEntityRepository importFileLogDasfRptViewEntityRepository;

  @Override
  public List<ImportFileLogDasfRptView> search(ImportFileLogDasfRptViewSearchCriteria criteria) {
    val results = importFileLogDasfRptViewEntityRepository.search(criteria);
    return IMPORT_FILE_LOG_Dasf_RPT_VIEW_MAPPER.entityListToModelList(results);
  }
}
