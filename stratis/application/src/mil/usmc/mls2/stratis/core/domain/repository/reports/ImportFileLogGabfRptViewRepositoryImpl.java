package mil.usmc.mls2.stratis.core.domain.repository.reports;

import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.core.domain.model.reports.ImportFileLogGabfRptView;
import mil.usmc.mls2.stratis.core.domain.model.reports.ImportFileLogGabfRptViewSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.mapper.reports.ImportFileLogGabfRptViewMapper;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.reports.ImportFileLogGabfRptViewEntityRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
public class ImportFileLogGabfRptViewRepositoryImpl implements ImportFileLogGabfRptViewRepository {

  private static final ImportFileLogGabfRptViewMapper IMPORT_FILE_LOG_Gabf_RPT_VIEW_MAPPER = ImportFileLogGabfRptViewMapper.INSTANCE;

  private final ImportFileLogGabfRptViewEntityRepository importFileLogGabfRptViewEntityRepository;

  @Override
  public List<ImportFileLogGabfRptView> search(ImportFileLogGabfRptViewSearchCriteria criteria) {
    val results = importFileLogGabfRptViewEntityRepository.search(criteria);
    return IMPORT_FILE_LOG_Gabf_RPT_VIEW_MAPPER.entityListToModelList(results);
  }
}
