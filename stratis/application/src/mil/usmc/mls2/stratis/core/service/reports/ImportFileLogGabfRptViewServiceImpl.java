package mil.usmc.mls2.stratis.core.service.reports;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.view.reports.ReportColumnBean;
import mil.usmc.mls2.stratis.core.domain.model.reports.ImportFileLogGabfRptView;
import mil.usmc.mls2.stratis.core.domain.model.reports.ImportFileLogGabfRptViewSearchCriteria;
import mil.usmc.mls2.stratis.core.domain.repository.reports.ImportFileLogGabfRptViewRepository;
import mil.usmc.mls2.stratis.core.infrastructure.mapper.reports.ImportFileLogGabfRptViewMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImportFileLogGabfRptViewServiceImpl implements ImportFileLogGabfRptViewService {

  private final ImportFileLogGabfRptViewRepository importFileLogGabfRptViewRepository;
  private static final ImportFileLogGabfRptViewMapper IMPORT_FILE_LOG_GABF_RPT_VIEW_MAPPER = ImportFileLogGabfRptViewMapper.INSTANCE;

  /**
   * Used to return a spring friendly set of results (note reports are not yet spring controlled, this is for ease of future conversion)
   */
  @Override
  @Transactional(readOnly = true)
  public List<ImportFileLogGabfRptView> search(ImportFileLogGabfRptViewSearchCriteria criteria) {
    return importFileLogGabfRptViewRepository.search(criteria);
  }

  /**
   * Used to return the search results in the ADF Report Friendly objects.
   */
  @Override
  @Transactional(readOnly = true)
  public List<ReportColumnBean> searchForAdfReport(ImportFileLogGabfRptViewSearchCriteria criteria) {
    val results = search(criteria);
    return IMPORT_FILE_LOG_GABF_RPT_VIEW_MAPPER.modelListToReportColumnList(results);
  }
}
