package mil.usmc.mls2.stratis.core.domain.repository.reports;

import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.core.domain.model.reports.ImportFileLogGbofRptView;
import mil.usmc.mls2.stratis.core.domain.model.reports.ImportFileLogGbofRptViewSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.mapper.reports.ImportFileLogGbofRptViewMapper;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.reports.ImportFileLogGbofRptViewEntityRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
public class ImportFileLogGbofRptViewRepositoryImpl implements ImportFileLogGbofRptViewRepository {

  private static final ImportFileLogGbofRptViewMapper IMPORT_FILE_LOG_Gbof_RPT_VIEW_MAPPER = ImportFileLogGbofRptViewMapper.INSTANCE;

  private final ImportFileLogGbofRptViewEntityRepository importFileLogGbofRptViewEntityRepository;

  @Override
  public List<ImportFileLogGbofRptView> search(ImportFileLogGbofRptViewSearchCriteria criteria) {
    val results = importFileLogGbofRptViewEntityRepository.search(criteria);
    return IMPORT_FILE_LOG_Gbof_RPT_VIEW_MAPPER.entityListToModelList(results);
  }
}
