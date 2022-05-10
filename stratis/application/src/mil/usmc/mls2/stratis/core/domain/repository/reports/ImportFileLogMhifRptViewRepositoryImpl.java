package mil.usmc.mls2.stratis.core.domain.repository.reports;

import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.core.domain.model.reports.ImportFileLogMhifRptView;
import mil.usmc.mls2.stratis.core.domain.model.reports.ImportFileLogMhifRptViewSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.mapper.reports.ImportFileLogMhifRptViewMapper;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.reports.ImportFileLogMhifRptViewEntityRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
public class ImportFileLogMhifRptViewRepositoryImpl implements ImportFileLogMhifRptViewRepository {

  private static final ImportFileLogMhifRptViewMapper IMPORT_FILE_LOG_Mhif_RPT_VIEW_MAPPER = ImportFileLogMhifRptViewMapper.INSTANCE;

  private final ImportFileLogMhifRptViewEntityRepository importFileLogMhifRptViewEntityRepository;

  @Override
  public List<ImportFileLogMhifRptView> search(ImportFileLogMhifRptViewSearchCriteria criteria) {
    val results = importFileLogMhifRptViewEntityRepository.search(criteria);
    return IMPORT_FILE_LOG_Mhif_RPT_VIEW_MAPPER.entityListToModelList(results);
  }
}
