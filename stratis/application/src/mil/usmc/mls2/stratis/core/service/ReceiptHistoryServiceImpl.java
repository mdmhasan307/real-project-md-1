package mil.usmc.mls2.stratis.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mil.usmc.mls2.stratis.core.domain.model.ReceiptHistory;
import mil.usmc.mls2.stratis.core.domain.model.ReceiptHistorySearchCriteria;
import mil.usmc.mls2.stratis.core.domain.repository.ReceiptHistoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReceiptHistoryServiceImpl implements ReceiptHistoryService {
    private final ReceiptHistoryRepository receiptHistoryRepository;

    @Override
    public List<ReceiptHistory> search(ReceiptHistorySearchCriteria criteria) {
        return receiptHistoryRepository.search(criteria);
    }

    @Override
    public Long count(ReceiptHistorySearchCriteria criteria) {
        return receiptHistoryRepository.count(criteria);
    }
}
