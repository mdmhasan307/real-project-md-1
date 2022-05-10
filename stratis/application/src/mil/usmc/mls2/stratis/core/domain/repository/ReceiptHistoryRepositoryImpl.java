package mil.usmc.mls2.stratis.core.domain.repository;

import lombok.RequiredArgsConstructor;
import mil.usmc.mls2.stratis.core.domain.model.ReceiptHistory;
import mil.usmc.mls2.stratis.core.domain.model.ReceiptHistorySearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.mapper.ReceiptHistoryMapper;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.ReceiptHistoryEntityRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ReceiptHistoryRepositoryImpl implements ReceiptHistoryRepository {

  public final ReceiptHistoryEntityRepository entityRepository;
  public final ReceiptHistoryMapper mapper;

  @Override
  public List<ReceiptHistory> search(ReceiptHistorySearchCriteria criteria) {
    return entityRepository.search(criteria).stream().map(mapper::map).collect(Collectors.toList());
  }

  @Override
  public Long count(ReceiptHistorySearchCriteria criteria) {
    return entityRepository.count(criteria);
  }
}
