package mil.usmc.mls2.stratis.core.service.reports;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.view.reports.ReportColumnBean;
import mil.usmc.mls2.stratis.core.domain.model.reports.ImportFileLogMhifRptView;
import mil.usmc.mls2.stratis.core.domain.model.reports.ImportFileLogMhifRptViewSearchCriteria;
import mil.usmc.mls2.stratis.core.domain.repository.reports.ImportFileLogMhifRptViewRepository;
import mil.usmc.mls2.stratis.core.infrastructure.mapper.reports.ImportFileLogMhifRptViewMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImportFileLogMhifRptViewServiceImpl implements ImportFileLogMhifRptViewService {

  private final ImportFileLogMhifRptViewRepository importFileLogMhifRptViewRepository;
  private static final ImportFileLogMhifRptViewMapper IMPORT_FILE_LOG_MHIF_RPT_VIEW_MAPPER = ImportFileLogMhifRptViewMapper.INSTANCE;

  /**
   * Used to return a spring friendly set of results (note reports are not yet spring controlled, this is for ease of future conversion)
   */
  @Override
  @Transactional(readOnly = true)
  public List<ImportFileLogMhifRptView> search(ImportFileLogMhifRptViewSearchCriteria criteria) {
    return importFileLogMhifRptViewRepository.search(criteria);
  }

  /**
   * Used to return the search results in the ADF Report Friendly objects.
   */
  @Override
  @Transactional(readOnly = true)
  public List<ReportColumnBean> searchForAdfReport(ImportFileLogMhifRptViewSearchCriteria criteria) {
    val results = search(criteria);
    return IMPORT_FILE_LOG_MHIF_RPT_VIEW_MAPPER.modelListToReportColumnList(results);
  }
}
