package mil.usmc.mls2.stratis.core.service.reports;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.view.reports.ReportColumnBean;
import mil.usmc.mls2.stratis.core.domain.model.reports.ReceiptHistRptView;
import mil.usmc.mls2.stratis.core.domain.model.reports.ReceiptHistRptViewSearchCriteria;
import mil.usmc.mls2.stratis.core.domain.repository.reports.ReceiptHistRptViewRepository;
import mil.usmc.mls2.stratis.core.infrastructure.mapper.reports.ReceiptHistRptViewMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReceiptHistRptViewServiceImpl implements ReceiptHistRptViewService {

  private final ReceiptHistRptViewRepository receiptHistRptViewRepository;
  private static final ReceiptHistRptViewMapper RECEIPT_HIST_RPT_VIEW_MAPPER = ReceiptHistRptViewMapper.INSTANCE;

  /**
   * Used to return a spring friendly set of results (note reports are not yet spring controlled, this is for ease of future conversion)
   */
  @Override
  @Transactional(readOnly = true)
  public List<ReceiptHistRptView> search(ReceiptHistRptViewSearchCriteria criteria) {
    return receiptHistRptViewRepository.search(criteria);
  }

  /**
   * Used to return the search results in the ADF Report Friendly objects.
   */
  @Override
  @Transactional(readOnly = true)
  public List<ReportColumnBean> searchForAdfReport(ReceiptHistRptViewSearchCriteria criteria) {
    val results = search(criteria);
    return RECEIPT_HIST_RPT_VIEW_MAPPER.modelListToReportColumnList(results);
  }
}
