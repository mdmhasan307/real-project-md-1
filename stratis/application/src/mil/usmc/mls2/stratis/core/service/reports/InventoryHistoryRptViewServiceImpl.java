package mil.usmc.mls2.stratis.core.service.reports;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.view.reports.ReportColumnBean;
import mil.usmc.mls2.stratis.core.domain.model.reports.InventoryHistoryRptView;
import mil.usmc.mls2.stratis.core.domain.model.reports.InventoryHistoryRptViewSearchCriteria;
import mil.usmc.mls2.stratis.core.domain.repository.reports.InventoryHistoryRptViewRepository;
import mil.usmc.mls2.stratis.core.infrastructure.mapper.reports.InventoryHistoryRptViewMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryHistoryRptViewServiceImpl implements InventoryHistoryRptViewService {

  private final InventoryHistoryRptViewRepository inventoryHistoryRptViewRepository;
  private static final InventoryHistoryRptViewMapper RECEIPT_HIST_RPT_VIEW_MAPPER = InventoryHistoryRptViewMapper.INSTANCE;

  /**
   * Used to return a spring friendly set of results (note reports are not yet spring controlled, this is for ease of future conversion)
   */
  @Override
  @Transactional(readOnly = true)
  public List<InventoryHistoryRptView> search(InventoryHistoryRptViewSearchCriteria criteria) {
    return inventoryHistoryRptViewRepository.search(criteria);
  }

  /**
   * Used to return the search results in the ADF Report Friendly objects.
   */
  @Override
  @Transactional(readOnly = true)
  public List<ReportColumnBean> searchForAdfReport(InventoryHistoryRptViewSearchCriteria criteria) {
    val results = search(criteria);
    return RECEIPT_HIST_RPT_VIEW_MAPPER.modelListToReportColumnList(results);
  }
}
