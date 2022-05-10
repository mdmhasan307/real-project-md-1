package mil.usmc.mls2.stratis.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.usmc.mls2.stratis.common.domain.exception.StratisRuntimeException;
import mil.usmc.mls2.stratis.common.domain.model.SortOrder;
import mil.usmc.mls2.stratis.core.domain.model.Receipt;
import mil.usmc.mls2.stratis.core.domain.model.ReceiptSearchCriteria;
import mil.usmc.mls2.stratis.core.domain.repository.ReceiptRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReceiptServiceImpl implements ReceiptService {

  private final ReceiptRepository receiptRepository;

  @Override
  @Transactional(readOnly = true)
  public Optional<Receipt> findById(Integer id) {
    return receiptRepository.findById(id);
  }

  @Override
  public List<Receipt> search(ReceiptSearchCriteria criteria) {
    return receiptRepository.search(criteria);
  }

  @Override
  public Long count(ReceiptSearchCriteria criteria) {
    return receiptRepository.count(criteria);
  }

  @Override
  public void save(Receipt receipt) { receiptRepository.save(receipt); }
}
