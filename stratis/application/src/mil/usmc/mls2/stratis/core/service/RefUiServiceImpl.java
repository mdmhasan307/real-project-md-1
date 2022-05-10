package mil.usmc.mls2.stratis.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.usmc.mls2.stratis.common.domain.exception.StratisRuntimeException;
import mil.usmc.mls2.stratis.core.domain.model.Receipt;
import mil.usmc.mls2.stratis.core.domain.model.RefUi;
import mil.usmc.mls2.stratis.core.domain.model.RefUiSearchCriteria;
import mil.usmc.mls2.stratis.core.domain.repository.RefUiRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings({"Duplicates", "squid:S1192"})
class RefUiServiceImpl implements RefUiService {

  private final RefUiRepository refUiRepository;

  @Override
  @Transactional(readOnly = true)
  public Optional<RefUi> findById(Integer id) {
    return refUiRepository.findById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public Long count(RefUiSearchCriteria criteria) {
    return refUiRepository.count(criteria);
  }

  @Override
  @Transactional(readOnly = true)
  public List<RefUi> search(RefUiSearchCriteria criteria) {
    return refUiRepository.search(criteria);
  }

  @Override
  @Transactional(readOnly = true)
  public RefUi findSingleMatch(RefUiSearchCriteria criteria) {
    val refUis = search(criteria);
    if (CollectionUtils.isEmpty(refUis)) {
      return null;
    }
    if (refUis.size() > 1) {
      throw new StratisRuntimeException("More then one RefUi found for criteria");
    }
    return refUis.get(0);
  }

  @Override
  @Transactional(readOnly = true)
  public Boolean convertReceiptUI(String to, String from, Receipt receipt) {
    val search = RefUiSearchCriteria.builder().uiFrom(from).uiTo(to).build();
    val refui = findSingleMatch(search);
    if (refui != null && (refui.getConversionFactor().intValue() != 1)) {
      receipt.convertReceiptUI(Long.valueOf(Math.round(receipt.getQuantityInvoiced() * refui.getConversionFactor())).intValue(),
          receipt.getPrice() / refui.getConversionFactor());
      return true;
    }
    if (refui == null) {
      return null;
    }
    return false;
  }
}
