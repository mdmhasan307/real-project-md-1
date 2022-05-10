package mil.usmc.mls2.stratis.core.service.reports;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.view.reports.StdReportColumnBean;
import mil.usmc.mls2.stratis.core.domain.model.reports.EmployeeWorkloadHistoryRptView;
import mil.usmc.mls2.stratis.core.domain.model.reports.EmployeeWorkloadHistoryRptViewSearchCriteria;
import mil.usmc.mls2.stratis.core.domain.repository.reports.EmployeeWorkloadHistoryRptViewRepository;
import mil.usmc.mls2.stratis.core.infrastructure.mapper.reports.EmployeeWorkloadHistoryRptViewMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeWorkloadHistoryRptViewServiceImpl implements EmployeeWorkloadHistoryRptViewService {

  private final EmployeeWorkloadHistoryRptViewRepository employeeWorkloadHistoryRptViewRepository;
  private static final EmployeeWorkloadHistoryRptViewMapper RECEIPT_HIST_RPT_VIEW_MAPPER = EmployeeWorkloadHistoryRptViewMapper.INSTANCE;

  /**
   * Used to return a spring friendly set of results (note reports are not yet spring controlled, this is for ease of future conversion)
   */
  @Override
  @Transactional(readOnly = true)
  public List<EmployeeWorkloadHistoryRptView> search(EmployeeWorkloadHistoryRptViewSearchCriteria criteria) {
    return employeeWorkloadHistoryRptViewRepository.search(criteria);
  }

  /**
   * Used to return the search results in the ADF Report Friendly objects.
   */
  @Override
  @Transactional(readOnly = true)
  public List<StdReportColumnBean> searchForAdfReport(EmployeeWorkloadHistoryRptViewSearchCriteria criteria) {
    val results = search(criteria);
    val list  = RECEIPT_HIST_RPT_VIEW_MAPPER.modelListToReportColumnList(results);
    StdReportColumnBean totalBean = new StdReportColumnBean();
    totalBean.setEmployee("Grand Totals");
    list.stream().forEach(t -> {
      totalBean.setReceipts(sumIntStrings(t.getReceipts(), totalBean.getReceipts()));
      totalBean.setReceiptsDollarValue(sumDoubleStrings(t.getReceiptsDollarValue(), totalBean.getReceiptsDollarValue()));

      totalBean.setStows(sumIntStrings(t.getStows(), totalBean.getStows()));
      totalBean.setStowsDollarValue(sumDoubleStrings(t.getStowsDollarValue(), totalBean.getStowsDollarValue()));

      totalBean.setPicks(sumIntStrings(t.getPicks(), totalBean.getPicks()));
      totalBean.setPicksDollarValue(sumDoubleStrings(t.getPicksDollarValue(), totalBean.getPicksDollarValue()));

      totalBean.setPacks(sumIntStrings(t.getPacks(), totalBean.getPacks()));
      totalBean.setPacksDollarValue(sumDoubleStrings(t.getPacksDollarValue(), totalBean.getPacksDollarValue()));

      totalBean.setInvs(sumIntStrings(t.getInvs(), totalBean.getInvs()));
      totalBean.setInvsDollarValue(sumDoubleStrings(t.getInvsDollarValue(), totalBean.getInvsDollarValue()));

      totalBean.setTotals(sumIntStrings(t.getTotals(), totalBean.getTotals()));
    });
    list.add(totalBean);
    return list;
  }

  private String sumIntStrings(String count, String total) {
    count = count == null ? "0" : count;
    total = total == null ? "0" : total;
    return Integer.toString(Integer.parseInt(count) + Integer.parseInt(total));
  }

  private String sumDoubleStrings(String count, String total) {
    count = count == null ? "0" : count;
    total = total == null ? "0" : total;
    return Double.toString(Double.parseDouble(count) + Double.parseDouble(total));
  }
}
