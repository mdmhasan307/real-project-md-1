package mil.usmc.mls2.stratis.core.service.reports;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.view.reports.ReportColumnBean;
import mil.usmc.mls2.stratis.core.domain.model.reports.ImportFileLogMatsRptView;
import mil.usmc.mls2.stratis.core.domain.model.reports.ImportFileLogMatsRptViewSearchCriteria;
import mil.usmc.mls2.stratis.core.domain.repository.reports.ImportFileLogMatsRptViewRepository;
import mil.usmc.mls2.stratis.core.infrastructure.mapper.reports.ImportFileLogMatsRptViewMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImportFileLogMatsRptViewServiceImpl implements ImportFileLogMatsRptViewService {

  private final ImportFileLogMatsRptViewRepository importFileLogMatsRptViewRepository;
  private static final ImportFileLogMatsRptViewMapper IMPORT_FILE_LOG_MATS_RPT_VIEW_MAPPER = ImportFileLogMatsRptViewMapper.INSTANCE;

  /**
   * Used to return a spring friendly set of results (note reports are not yet spring controlled, this is for ease of future conversion)
   */
  @Override
  @Transactional(readOnly = true)
  public List<ImportFileLogMatsRptView> search(ImportFileLogMatsRptViewSearchCriteria criteria) {
    return importFileLogMatsRptViewRepository.search(criteria);
  }

  /**
   * Used to return the search results in the ADF Report Friendly objects.
   */
  @Override
  @Transactional(readOnly = true)
  public List<ReportColumnBean> searchForAdfReport(ImportFileLogMatsRptViewSearchCriteria criteria) {
    val results = search(criteria);
    return IMPORT_FILE_LOG_MATS_RPT_VIEW_MAPPER.modelListToReportColumnList(results);
  }
}
