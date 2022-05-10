package mil.usmc.mls2.stratis.core.service.reports;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.view.reports.StdReportColumnBean;
import mil.usmc.mls2.stratis.core.domain.model.reports.InterfaceTransactionRptView;
import mil.usmc.mls2.stratis.core.domain.model.reports.InterfaceTransactionRptViewSearchCriteria;
import mil.usmc.mls2.stratis.core.domain.repository.reports.InterfaceTransactionRptViewRepository;
import mil.usmc.mls2.stratis.core.infrastructure.mapper.reports.InterfaceTransactionRptViewMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterfaceTransactionRptViewServiceImpl implements InterfaceTransactionRptViewService {

  private final InterfaceTransactionRptViewRepository interfaceTransactionRptViewRepository;
  private static final InterfaceTransactionRptViewMapper INTERFACE_TRANSACTION_RPT_VIEW_MAPPER = InterfaceTransactionRptViewMapper.INSTANCE;

  /**
   * Used to return a spring friendly set of results (note reports are not yet spring controlled, this is for ease of future conversion)
   */
  @Override
  @Transactional(readOnly = true)
  public List<InterfaceTransactionRptView> search(InterfaceTransactionRptViewSearchCriteria criteria) {
    return interfaceTransactionRptViewRepository.search(criteria);
  }

  /**
   * Used to return the search results in the ADF Report Friendly objects.
   */
  @Override
  @Transactional(readOnly = true)
  public List<StdReportColumnBean> searchForAdfReport(InterfaceTransactionRptViewSearchCriteria criteria) {
    val results = search(criteria);
    return INTERFACE_TRANSACTION_RPT_VIEW_MAPPER.modelListToReportColumnList(results);
  }
}
