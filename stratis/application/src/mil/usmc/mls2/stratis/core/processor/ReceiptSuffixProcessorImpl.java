package mil.usmc.mls2.stratis.core.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.usmc.mls2.stratis.common.domain.exception.StratisRuntimeException;
import mil.usmc.mls2.stratis.common.domain.model.SortOrder;
import mil.usmc.mls2.stratis.core.domain.model.Receipt;
import mil.usmc.mls2.stratis.core.domain.model.ReceiptHistory;
import mil.usmc.mls2.stratis.core.domain.model.ReceiptHistorySearchCriteria;
import mil.usmc.mls2.stratis.core.domain.model.ReceiptSearchCriteria;
import mil.usmc.mls2.stratis.core.service.ReceiptHistoryService;
import mil.usmc.mls2.stratis.core.service.ReceiptService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReceiptSuffixProcessorImpl implements ReceiptSuffixProcessor {

  private final ReceiptService receiptService;
  private final ReceiptHistoryService receiptHistoryService;

  @Override
  @Transactional
  public String generateSuffix(String documentNumber) {
    val criteria = ReceiptSearchCriteria.builder()
        .documentNumber(documentNumber)
        .checkSuffixNull(false)
        .build();
    criteria.setSort("suffix", SortOrder.DESC);

    val currentSuffix = receiptService.search(criteria).stream()
        .findFirst()
        .map(Receipt::getSuffix)
        .map(String::toUpperCase)
        .map(suffix -> {
          if (suffix.equalsIgnoreCase("Z"))
            throw new StratisRuntimeException("Suffixes A-Z have all been utilized. Please see a supervisor.");
          return Character.toString((char) (suffix.charAt(0) + 1));
        })
        .orElse("A");

    val historyCriteria = ReceiptHistorySearchCriteria.builder()
        .documentNumber(documentNumber)
        .checkSuffixNull(false)
        .build();
    historyCriteria.setSort("suffix", SortOrder.DESC);
    val historySuffix = receiptHistoryService.search(historyCriteria).stream()
        .findFirst()
        .map(ReceiptHistory::getSuffix)
        .map(String::toUpperCase)
        .map(suffix -> {
          if (suffix.equalsIgnoreCase("Z"))
            throw new StratisRuntimeException("Suffixes A-Z have all been utilized please see a supervisor");
          return Character.toString((char) (suffix.charAt(0) + 1));
        })
        .orElse("A");

    return currentSuffix.compareToIgnoreCase(historySuffix) > 0 ? currentSuffix : historySuffix;
  }
}
