package mil.usmc.mls2.stratis.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mil.usmc.mls2.stratis.core.domain.model.Error;
import mil.usmc.mls2.stratis.core.domain.model.ErrorSearchCriteria;
import mil.usmc.mls2.stratis.common.domain.exception.StratisRuntimeException;
import mil.usmc.mls2.stratis.core.domain.repository.ErrorRepository;
import org.springframework.stereotype.Service;

import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class ErrorServiceImpl implements ErrorService {

  private final ErrorRepository errorRepository;

  @Override
  public List<Error> search(ErrorSearchCriteria criteria) { return errorRepository.search(criteria); }

  @Override
  public Error findByCode(String code) {
    List<Error> errors = errorRepository.search(ErrorSearchCriteria.builder().code(code).build());
    if (errors.isEmpty()) {
      return null;
    }
    else if (errors.size() > 1) {
      throw new StratisRuntimeException("More then one error found for criteria");
    }
    return errors.get(0);
  }

  public void save(Error error) {
    errorRepository.save(error);
  }
}
