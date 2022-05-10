package mil.usmc.mls2.stratis.core.domain.repository.reports;

import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.core.domain.model.reports.ReceiptHistRptView;
import mil.usmc.mls2.stratis.core.domain.model.reports.ReceiptHistRptViewSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.mapper.reports.ReceiptHistRptViewMapper;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.reports.ReceiptHistRptViewEntityRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
public class ReceiptHistRptViewRepositoryImpl implements ReceiptHistRptViewRepository {

  private static final ReceiptHistRptViewMapper RECEIPT_HIST_RPT_VIEW_MAPPER = ReceiptHistRptViewMapper.INSTANCE;

  private final ReceiptHistRptViewEntityRepository receiptHistRptViewEntityRepository;

  @Override
  public List<ReceiptHistRptView> search(ReceiptHistRptViewSearchCriteria criteria) {
    val results = receiptHistRptViewEntityRepository.search(criteria);
    return RECEIPT_HIST_RPT_VIEW_MAPPER.entityListToModelList(results);
  }
}
