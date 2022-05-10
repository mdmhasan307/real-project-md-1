package mil.usmc.mls2.stratis.core.domain.repository.reports;

import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.core.domain.model.reports.ImportFileLogMatsRptView;
import mil.usmc.mls2.stratis.core.domain.model.reports.ImportFileLogMatsRptViewSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.mapper.reports.ImportFileLogMatsRptViewMapper;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.reports.ImportFileLogMatsRptViewEntityRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
public class ImportFileLogMatsRptViewRepositoryImpl implements ImportFileLogMatsRptViewRepository {

  private static final ImportFileLogMatsRptViewMapper IMPORT_FILE_LOG_MATS_RPT_VIEW_MAPPER = ImportFileLogMatsRptViewMapper.INSTANCE;

  private final ImportFileLogMatsRptViewEntityRepository importFileLogMatsRptViewEntityRepository;

  @Override
  public List<ImportFileLogMatsRptView> search(ImportFileLogMatsRptViewSearchCriteria criteria) {
    val results = importFileLogMatsRptViewEntityRepository.search(criteria);
    return IMPORT_FILE_LOG_MATS_RPT_VIEW_MAPPER.entityListToModelList(results);
  }
}
