package mil.usmc.mls2.stratis.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mil.usmc.mls2.stratis.core.domain.model.Customer;
import mil.usmc.mls2.stratis.core.domain.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings({"Duplicates", "squid:S1192"})
class CustomerServiceImpl implements CustomerService {

  private final CustomerRepository repository;

  @Override
  @Transactional(readOnly = true)
  public Optional<Customer> getByAac(String aac) {
    return repository.getByAac(aac);
  }

  @Override
  @Transactional
  public void save(Customer customer) {
    repository.save(customer);
  }
}
