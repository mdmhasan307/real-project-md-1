package mil.usmc.mls2.stratis.core.service.reports;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.view.reports.ReportColumnBean;
import mil.usmc.mls2.stratis.core.domain.model.reports.ImportFileLogDasfRptView;
import mil.usmc.mls2.stratis.core.domain.model.reports.ImportFileLogDasfRptViewSearchCriteria;
import mil.usmc.mls2.stratis.core.domain.repository.reports.ImportFileLogDasfRptViewRepository;
import mil.usmc.mls2.stratis.core.infrastructure.mapper.reports.ImportFileLogDasfRptViewMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImportFileLogDasfRptViewServiceImpl implements ImportFileLogDasfRptViewService {

  private final ImportFileLogDasfRptViewRepository importFileLogDasfRptViewRepository;
  private static final ImportFileLogDasfRptViewMapper IMPORT_FILE_LOG_DASF_RPT_VIEW_MAPPER = ImportFileLogDasfRptViewMapper.INSTANCE;

  /**
   * Used to return a spring friendly set of results (note reports are not yet spring controlled, this is for ease of future conversion)
   */
  @Override
  @Transactional(readOnly = true)
  public List<ImportFileLogDasfRptView> search(ImportFileLogDasfRptViewSearchCriteria criteria) {
    return importFileLogDasfRptViewRepository.search(criteria);
  }

  /**
   * Used to return the search results in the ADF Report Friendly objects.
   */
  @Override
  @Transactional(readOnly = true)
  public List<ReportColumnBean> searchForAdfReport(ImportFileLogDasfRptViewSearchCriteria criteria) {
    val results = search(criteria);
    return IMPORT_FILE_LOG_DASF_RPT_VIEW_MAPPER.modelListToReportColumnList(results);
  }
}
