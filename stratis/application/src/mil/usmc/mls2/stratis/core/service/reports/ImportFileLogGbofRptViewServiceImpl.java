package mil.usmc.mls2.stratis.core.service.reports;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.view.reports.ReportColumnBean;
import mil.usmc.mls2.stratis.core.domain.model.reports.ImportFileLogGbofRptView;
import mil.usmc.mls2.stratis.core.domain.model.reports.ImportFileLogGbofRptViewSearchCriteria;
import mil.usmc.mls2.stratis.core.domain.repository.reports.ImportFileLogGbofRptViewRepository;
import mil.usmc.mls2.stratis.core.infrastructure.mapper.reports.ImportFileLogGbofRptViewMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImportFileLogGbofRptViewServiceImpl implements ImportFileLogGbofRptViewService {

  private final ImportFileLogGbofRptViewRepository importFileLogGbofRptViewRepository;
  private static final ImportFileLogGbofRptViewMapper IMPORT_FILE_LOG_GBOF_RPT_VIEW_MAPPER = ImportFileLogGbofRptViewMapper.INSTANCE;

  /**
   * Used to return a spring friendly set of results (note reports are not yet spring controlled, this is for ease of future conversion)
   */
  @Override
  @Transactional(readOnly = true)
  public List<ImportFileLogGbofRptView> search(ImportFileLogGbofRptViewSearchCriteria criteria) {
    return importFileLogGbofRptViewRepository.search(criteria);
  }

  /**
   * Used to return the search results in the ADF Report Friendly objects.
   */
  @Override
  @Transactional(readOnly = true)
  public List<ReportColumnBean> searchForAdfReport(ImportFileLogGbofRptViewSearchCriteria criteria) {
    val results = search(criteria);
    return IMPORT_FILE_LOG_GBOF_RPT_VIEW_MAPPER.modelListToReportColumnList(results);
  }
}
